package com.saasybyte.saasycore.invitecode

import com.saasybyte.saasycore.api.InviteCodesApi
import com.saasybyte.saasycore.api.model.ClaimResponse
import com.saasybyte.saasycore.api.model.GenerateRequest
import com.saasybyte.saasycore.api.model.GenerateResponse
import com.saasybyte.saasycore.api.model.ValidateRequest
import com.saasybyte.saasycore.api.model.ValidateResponse
import com.saasybyte.saasycore.auth.JwtService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class InviteCodeController(
    private val service: InviteCodeService,
    private val jwtService: JwtService
) : InviteCodesApi {

    override fun generateInviteCodes(generateRequest: GenerateRequest): ResponseEntity<GenerateResponse> {
        val codes = service.generate(generateRequest.count, generateRequest.usageBudgetSeconds)
        return ResponseEntity.ok(GenerateResponse(codes.map { it.code }))
    }

    override fun claimInviteCode(): ResponseEntity<ClaimResponse> {
        val claimed = service.claim()
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ClaimResponse(null, "No unclaimed codes available"))

        return ResponseEntity.ok(ClaimResponse(claimed.code, null))
    }

    override fun validateInviteCode(validateRequest: ValidateRequest): ResponseEntity<ValidateResponse> {
        return when (val result = service.validate(validateRequest.code)) {
            is InviteCodeService.ValidationResult.NotFound ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ValidateResponse(null, "Invalid code"))

            is InviteCodeService.ValidationResult.NotHandedOut ->
                ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ValidateResponse(null, "Code has not been issued"))

            is InviteCodeService.ValidationResult.InvalidatedByAttempts ->
                ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ValidateResponse(null, "Code invalidated due to too many failed attempts"))

            is InviteCodeService.ValidationResult.WindowExpired ->
                ResponseEntity.status(HttpStatus.GONE)
                    .body(ValidateResponse(null, "Code window has expired"))

            is InviteCodeService.ValidationResult.BudgetExhausted ->
                ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(ValidateResponse(null, "Usage budget exhausted"))

            is InviteCodeService.ValidationResult.Success -> {
                val token = jwtService.mintToken(result.inviteCode)
                ResponseEntity.ok(ValidateResponse(token, null))
            }
        }
    }
}
