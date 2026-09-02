package com.saasybyte.saasycore.auth

import com.saasybyte.saasycore.invitecode.InviteCode
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Service
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import java.util.Date

@Service
class JwtService(private val securityProperties: SecurityProperties) {

    private val signingKey: PrivateKey by lazy {
        parsePrivateKey(securityProperties.jwt.privateKey)
    }

    private fun parsePrivateKey(pem: String): PrivateKey {
        val base64 = pem
            .replace("-----BEGIN EC PRIVATE KEY-----", "")
            .replace("-----END EC PRIVATE KEY-----", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")
        val keyBytes = Base64.getDecoder().decode(base64)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePrivate(keySpec)
    }

    fun mintToken(inviteCode: InviteCode): String {
        val usageRemaining = maxOf(
            0,
            inviteCode.usageBudgetSeconds - inviteCode.usageConsumedSeconds
        )

        return Jwts.builder()
            .subject(inviteCode.id.toString())
            .claim("invite_code_id", inviteCode.id.toString())
            .claim("window_expires_at", inviteCode.windowExpiresAt?.toEpochSecond())
            .claim("usage_remaining_seconds", usageRemaining)
            .issuedAt(Date())
            .expiration(inviteCode.windowExpiresAt?.let { Date.from(it.toInstant()) })
            .signWith(signingKey)
            .compact()
    }

}
