package com.saasybyte.saasycore.invitecode

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "invite")
data class InviteCodeProperties(
    val windowDurationDays: Int = 30,
    val usageBudgetSeconds: Int = 1200,
    val maxValidationAttempts: Int = 50
)
