package com.saasybyte.saasycore.invitecode

import java.time.OffsetDateTime
import java.util.UUID

data class InviteCode(
    val id: UUID,
    val code: String,
    val handedOutAt: OffsetDateTime?,
    val firstRedeemedAt: OffsetDateTime?,
    val windowExpiresAt: OffsetDateTime?,
    val usageBudgetSeconds: Int,
    val usageConsumedSeconds: Int,
    val failedValidationAttempts: Int,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val deletedAt: OffsetDateTime?
)
