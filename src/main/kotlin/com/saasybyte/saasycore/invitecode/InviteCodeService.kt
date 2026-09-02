package com.saasybyte.saasycore.invitecode

import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.OffsetDateTime
import java.util.UUID

@Service
class InviteCodeService(
    private val repository: InviteCodeRepository,
    private val inviteProperties: InviteCodeProperties
) {
    private val secureRandom = SecureRandom()
    private val alphanumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

    fun generate(count: Int, usageBudgetSeconds: Int? = null): List<InviteCode> {
        val budget = usageBudgetSeconds ?: inviteProperties.usageBudgetSeconds
        val codes = (1..count).map { generateCode() }
        return repository.insertBatch(codes, budget)
    }

    fun claim(): InviteCode? {
        val unclaimed = repository.findOneUnclaimed() ?: return null
        val updated = unclaimed.copy(handedOutAt = OffsetDateTime.now())
        return repository.update(updated)
    }

    fun validate(code: String): ValidationResult {
        val inviteCode = repository.findByCode(code)
            ?: return ValidationResult.NotFound

        // Check if code has been handed out (claimed)
        if (inviteCode.handedOutAt == null) {
            return ValidationResult.NotHandedOut
        }

        // Increment failed attempts first
        val withIncrementedAttempts = inviteCode.copy(
            failedValidationAttempts = inviteCode.failedValidationAttempts + 1
        )
        repository.update(withIncrementedAttempts)

        // Check if max attempts exceeded
        if (withIncrementedAttempts.failedValidationAttempts >= inviteProperties.maxValidationAttempts) {
            return ValidationResult.InvalidatedByAttempts
        }

        val now = OffsetDateTime.now()
        var updated = withIncrementedAttempts

        // First redemption - set window
        if (updated.firstRedeemedAt == null) {
            updated = updated.copy(
                firstRedeemedAt = now,
                windowExpiresAt = now.plusDays(inviteProperties.windowDurationDays.toLong())
            )
        }

        // Check if window expired
        if (updated.windowExpiresAt != null && updated.windowExpiresAt.isBefore(now)) {
            repository.update(updated)
            return ValidationResult.WindowExpired
        }

        // Check if budget exhausted
        if (updated.usageConsumedSeconds >= updated.usageBudgetSeconds) {
            repository.update(updated)
            return ValidationResult.BudgetExhausted
        }

        // Success - reset failed attempts
        updated = updated.copy(failedValidationAttempts = 0)
        val saved = repository.update(updated)

        return ValidationResult.Success(saved)
    }

    fun recordUsage(inviteCodeId: UUID, secondsConsumed: Int): UsageResult? {
        val inviteCode = repository.findById(inviteCodeId) ?: return null

        val newConsumed = inviteCode.usageConsumedSeconds + secondsConsumed
        val updated = inviteCode.copy(usageConsumedSeconds = newConsumed)
        val saved = repository.update(updated)

        val remaining = maxOf(0, saved.usageBudgetSeconds - saved.usageConsumedSeconds)
        val exhausted = saved.usageConsumedSeconds >= saved.usageBudgetSeconds

        return UsageResult(remaining, exhausted)
    }

    private fun generateCode(): String {
        return buildString(8) {
            repeat(8) {
                append(alphanumeric[secureRandom.nextInt(alphanumeric.length)])
            }
        }
    }

    sealed class ValidationResult {
        data object NotFound : ValidationResult()
        data object NotHandedOut : ValidationResult()
        data object InvalidatedByAttempts : ValidationResult()
        data object WindowExpired : ValidationResult()
        data object BudgetExhausted : ValidationResult()
        data class Success(val inviteCode: InviteCode) : ValidationResult()
    }

    data class UsageResult(
        val usageRemainingSeconds: Int,
        val budgetExhausted: Boolean
    )
}
