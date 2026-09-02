package com.saasybyte.saasycore.auth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security")
data class SecurityProperties(
    val jwt: JwtProperties,
    val admin: AdminProperties,
    val cors: CorsProperties
) {
    data class JwtProperties(
        val privateKey: String
    )

    data class AdminProperties(
        val apiKey: String
    )

    data class CorsProperties(
        val allowedOrigins: List<String>
    )
}
