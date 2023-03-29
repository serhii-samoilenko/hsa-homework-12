package com.example.service

import com.example.config.AppConfig
import com.example.model.Person
import io.micrometer.core.annotation.Counted
import io.micrometer.core.instrument.MeterRegistry
import io.quarkus.logging.Log
import io.quarkus.runtime.Startup
import io.smallrye.mutiny.Uni
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import kotlin.random.Random
import kotlin.time.Duration

@Startup
@ApplicationScoped
class PersonService(
    private val config: AppConfig,
    private val personCache: PersonCache,
    registry: MeterRegistry,
) {
    private val entityIds: IntRange = (0 until config.entityGroupSizes().sum())
    private val generationDelayMs = Duration.parse(config.generationDelay()).inWholeMilliseconds
    private val entityGroups = getEntityGroups(config.entityGroupSizes(), config.entityGroupProbabilities())
    private val hitsCounter = registry.counter("hit_count")
    private val misesCounter = registry.counter("miss_count")

    @Counted(value = "lookup_count")
    fun getPerson(key: String): Uni<Person> =
        personCache.get(key)
            .chain { cached ->
                if (cached != null) {
                    hitsCounter.increment()
                    Uni.createFrom().item(cached)
                } else {
                    misesCounter.increment()
                    val person = generatePersonSlowly(key)
                    personCache.set(key, person).chain { _ ->
                        Uni.createFrom().item(person)
                    }
                }
            }

    fun getUniformRandomPerson() = getPerson(randomId().toString())

    fun getProbablyDistributedRandomPerson() = getPerson(probablyDistributedRandomId().toString())

    private fun generatePersonSlowly(id: String): Person {
        Thread.sleep(generationDelayMs)
        return Person.generateRandom(id)
    }

    @PostConstruct
    fun init() {
        Log.info(
            "Pre-populating cache with around ${config.prepopulatePercentage()}% of total" +
                " ${entityIds.count()} entities",
        )
        personCache.drop().await().indefinitely()
        val batchSize = 1000
        // Save random persons to Redis
        entityIds
            .asSequence()
            .filter { Random.nextInt(0, 100) < config.prepopulatePercentage() }
            .map { Person.generateRandom(it.toString()) }
            .chunked(batchSize)
            .forEach { personList ->
                Log.info("Persisting ${personList.size} persons")
                personCache.persist(personList).await().indefinitely()
            }
    }

    private fun randomId(): Int {
        return Random.nextInt(entityIds.count())
    }

    private fun probablyDistributedRandomId(): Int {
        val randomValue: Double = Random.nextDouble()
        var cumulativeProbability = 0.0
        entityGroups.forEach {
            cumulativeProbability += it.second
            if (randomValue <= cumulativeProbability) {
                return it.first.random()
            }
        }
        return entityIds.random()
    }

    companion object {
        private fun getEntityGroups(sizes: IntArray, probabilities: List<Double>): List<Pair<IntRange, Double>> {
            var start = 0
            return sizes.map { size ->
                val range = start until (start + size)
                start += size
                range
            }.zip(probabilities)
        }
    }
}
