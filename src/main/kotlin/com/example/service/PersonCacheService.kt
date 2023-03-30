package com.example.service

import com.example.model.Person
import io.micrometer.core.annotation.Counted
import io.micrometer.core.instrument.MeterRegistry
import io.smallrye.mutiny.Uni
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Singleton
import kotlin.math.absoluteValue
import kotlin.math.ln
import kotlin.random.Random

@Singleton
class PersonCacheService(
    private val cache: PersonCache,
    registry: MeterRegistry,
) {
    private val meanComputationTime = AtomicLong(10)
    private val hitsCounter = registry.counter("hit_count")
    private val misesCounter = registry.counter("miss_count")
    private val recomputeCounter = registry.counter("recompute_count")

    @Counted(value = "lookup_count")
    fun getOrCompute(key: String, ttl: Long, supplier: () -> Person): Uni<Person> =
        cache.get(key)
            .chain { cached ->
                if (cached != null) {
                    hitsCounter.increment()
                    Uni.createFrom().item(cached)
                } else {
                    misesCounter.increment()
                    computeAndCache(key, ttl, supplier)
                }
            }

    @Counted(value = "lookup_count")
    fun getOrComputeProbabilistic(key: String, ttl: Long, supplier: () -> Person): Uni<Person> =
        cache.getx(key).chain { pair ->
            if (pair == null) {
                misesCounter.increment()
                computeAndCache(key, ttl, supplier)
            } else {
                val (person, remainingTtl) = pair
                if (meanComputationTime.get() * ln(Random.nextDouble()).absoluteValue >= remainingTtl) {
                    recomputeCounter.increment()
                    computeAndCache(key, ttl, supplier)
                } else {
                    hitsCounter.increment()
                    Uni.createFrom().item(person)
                }
            }
        }

    private fun computeAndCache(key: String, ttl: Long, supplier: () -> Person): Uni<Person> {
        val start = System.currentTimeMillis()
        val computedValue = supplier()
        val delta = System.currentTimeMillis() - start
        meanComputationTime.getAndUpdate { (it + delta) / 2 }
        return cache.setx(key, computedValue, ttl).chain { _ ->
            Uni.createFrom().item(computedValue)
        }
    }
}
