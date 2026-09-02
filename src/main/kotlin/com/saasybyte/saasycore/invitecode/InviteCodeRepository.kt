package com.saasybyte.saasycore.invitecode

import org.jooq.DSLContext
import org.jooq.impl.DSL.*
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class InviteCodeRepository(private val dsl: DSLContext) {

    fun insertBatch(codes: List<String>, usageBudgetSeconds: Int): List<InviteCode> {
        val now = OffsetDateTime.now()

        val insertedIds = codes.map { code ->
            val id = UUID.randomUUID()
            dsl.insertInto(table("invite_codes"))
                .columns(
                    field("id"),
                    field("code"),
                    field("usage_budget_seconds"),
                    field("created_at"),
                    field("updated_at")
                )
                .values(id, code, usageBudgetSeconds, now, now)
                .execute()
            id
        }

        return insertedIds.map { id -> findById(id)!! }
    }

    fun findById(id: UUID): InviteCode? {
        return dsl.select()
            .from(table("invite_codes"))
            .where(field("id").eq(id))
            .and(field("deleted_at").isNull)
            .fetchOne()
            ?.let { mapToInviteCode(it) }
    }

    fun findByCode(code: String): InviteCode? {
        return dsl.select()
            .from(table("invite_codes"))
            .where(field("code").eq(code))
            .and(field("deleted_at").isNull)
            .fetchOne()
            ?.let { mapToInviteCode(it) }
    }

    fun findOneUnclaimed(): InviteCode? {
        return dsl.select()
            .from(table("invite_codes"))
            .where(field("handed_out_at").isNull)
            .and(field("deleted_at").isNull)
            .limit(1)
            .fetchOne()
            ?.let { mapToInviteCode(it) }
    }

    fun update(inviteCode: InviteCode): InviteCode {
        val now = OffsetDateTime.now()
        dsl.update(table("invite_codes"))
            .set(field("handed_out_at"), inviteCode.handedOutAt)
            .set(field("first_redeemed_at"), inviteCode.firstRedeemedAt)
            .set(field("window_expires_at"), inviteCode.windowExpiresAt)
            .set(field("usage_consumed_seconds"), inviteCode.usageConsumedSeconds)
            .set(field("failed_validation_attempts"), inviteCode.failedValidationAttempts)
            .set(field("updated_at"), now)
            .where(field("id").eq(inviteCode.id))
            .execute()

        return findById(inviteCode.id)!!
    }

    private fun mapToInviteCode(record: org.jooq.Record): InviteCode {
        return InviteCode(
            id = record.get("id", UUID::class.java)!!,
            code = record.get("code", String::class.java)!!,
            handedOutAt = record.get("handed_out_at", OffsetDateTime::class.java),
            firstRedeemedAt = record.get("first_redeemed_at", OffsetDateTime::class.java),
            windowExpiresAt = record.get("window_expires_at", OffsetDateTime::class.java),
            usageBudgetSeconds = record.get("usage_budget_seconds", Int::class.java)!!,
            usageConsumedSeconds = record.get("usage_consumed_seconds", Int::class.java)!!,
            failedValidationAttempts = record.get("failed_validation_attempts", Int::class.java)!!,
            createdAt = record.get("created_at", OffsetDateTime::class.java)!!,
            updatedAt = record.get("updated_at", OffsetDateTime::class.java)!!,
            deletedAt = record.get("deleted_at", OffsetDateTime::class.java)
        )
    }
}
