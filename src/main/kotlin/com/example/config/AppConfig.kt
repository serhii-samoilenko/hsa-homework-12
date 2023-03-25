package com.example.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithName

@ConfigMapping(prefix = "app")
interface AppConfig {

    @WithName("entity-count")
    fun entityCount(): Int

    @WithName("prepopulate-percentage")
    fun prepopulatePercentage(): Int
}
