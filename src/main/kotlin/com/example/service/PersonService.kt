package com.example.service

import com.example.config.AppConfig
import com.example.model.Person
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
    private val cacheService: PersonCacheService,
) {
    private val entityIds: IntRange = (0 until config.entityGroupSizes().sum())
    private val generationDelayMs = Duration.parse(config.generationDelay()).inWholeMilliseconds
    private val ttl = Duration.parse(config.cacheTtl()).inWholeMilliseconds
    private val entityGroups = getEntityGroups(config.entityGroupSizes(), config.entityGroupProbabilities())

    fun getPerson(key: String): Uni<Person> {
        return personCache.get(key)
    }

    fun getRandomPerson(probablyDistributed: Boolean, useProbabilisticCache: Boolean): Uni<Person> {
        val id = if (probablyDistributed) {
            probablyDistributedRandomId().toString()
        } else {
            randomId().toString()
        }
        val supplier = { generatePersonSlowly(id) }
        return if (useProbabilisticCache) {
            cacheService.getOrComputeProbabilistic(id, ttl, supplier)
        } else {
            cacheService.getOrCompute(id, ttl, supplier)
        }
    }

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
        personCache.drop()
        val batchSize = 1000
        // Save random persons to Redis
        entityIds
            .asSequence()
            .filter { Random.nextInt(0, 100) < config.prepopulatePercentage() }
            .map { Person.generateRandom(it.toString()) }
            .chunked(batchSize)
            .forEach { personList ->
                Log.info("Persisting ${personList.size} persons")
                personCache.persist(personList, ttl)
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
