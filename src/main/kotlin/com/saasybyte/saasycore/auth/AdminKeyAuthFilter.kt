package com.saasybyte.saasycore.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class AdminKeyAuthFilter(
    private val securityProperties: SecurityProperties
) : OncePerRequestFilter() {

    private val protectedPaths = setOf("/invite-codes/generate", "/invite-codes/claim")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.servletPath

        if (path in protectedPaths) {
            val adminKey = request.getHeader("X-Admin-Key")

            if (adminKey == securityProperties.admin.apiKey) {
                val auth = UsernamePasswordAuthenticationToken(
                    "admin",
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_ADMIN"))
                )
                SecurityContextHolder.getContext().authentication = auth
            }
        }

        filterChain.doFilter(request, response)
    }
}
