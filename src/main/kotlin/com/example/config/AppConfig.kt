package com.example.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithName

@ConfigMapping(prefix = "app")
interface AppConfig {

    @WithName("entity-group-sizes")
    fun entityGroupSizes(): IntArray

    @WithName("entity-group-probabilities")
    fun entityGroupProbabilities(): List<Double>

    @WithName("prepopulate-percentage")
    fun prepopulatePercentage(): Int

    @WithName("generation-delay")
    fun generationDelay(): String
}
