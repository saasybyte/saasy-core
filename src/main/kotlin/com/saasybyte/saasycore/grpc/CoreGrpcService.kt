package com.saasybyte.saasycore.grpc

import com.saasybyte.saasycore.invitecode.InviteCodeService
import io.grpc.Status
import io.grpc.StatusException
import org.springframework.grpc.server.service.GrpcService
import saasy.core.v1.Core
import saasy.core.v1.CoreServiceGrpcKt
import java.util.UUID

@GrpcService
class CoreGrpcService(
    private val inviteCodeService: InviteCodeService
) : CoreServiceGrpcKt.CoreServiceCoroutineImplBase() {

    override suspend fun recordUsage(request: Core.RecordUsageRequest): Core.RecordUsageResponse {
        val inviteCodeId = try {
            UUID.fromString(request.inviteCodeId)
        } catch (e: IllegalArgumentException) {
            throw StatusException(Status.INVALID_ARGUMENT.withDescription("Invalid invite_code_id format"))
        }

        val result = inviteCodeService.recordUsage(inviteCodeId, request.secondsConsumed)
            ?: throw StatusException(Status.NOT_FOUND.withDescription("Invite code not found"))

        return Core.RecordUsageResponse.newBuilder()
            .setUsageRemainingSeconds(result.usageRemainingSeconds)
            .setBudgetExhausted(result.budgetExhausted)
            .build()
    }
}
