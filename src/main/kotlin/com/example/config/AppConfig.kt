package com.example.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithName
import kotlin.time.Duration

@ConfigMapping(prefix = "app")
interface AppConfig {

    @WithName("entity-count")
    fun entityCount(): Int

    @WithName("prepopulate-percentage")
    fun prepopulatePercentage(): Int

    @WithName("generation-delay")
    fun generationDelay(): String
}
