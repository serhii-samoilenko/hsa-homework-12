package com.example.service

import com.example.config.AppConfig
import com.example.model.Person
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics.counter
import io.quarkus.logging.Log
import io.quarkus.runtime.Startup
import org.apache.commons.lang3.RandomStringUtils
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration

@Suppress("UnstableApiUsage")
@Startup
@ApplicationScoped
class PersonService(
    private val config: AppConfig,
    private val personCache: PersonCache,
    private val registry: MeterRegistry,
) {
    private val firstNamesCount = sqrt(config.entityCount().toDouble()).toInt()
    private val lastNamesCount = firstNamesCount
    private val firstNames: List<String>
    private val lastNames: List<String>
    private val generationDelayMs = Duration.parse(config.generationDelay()).inWholeMilliseconds
    private val hitsCounter = registry.counter("hit_count")
    private val misesCounter = registry.counter("miss_count")

    init {
        Log.info("Creating pool of $firstNamesCount first names and $lastNamesCount last names")
        firstNames = (1..firstNamesCount).map { randomName(19) }.toList()
        lastNames = (1..lastNamesCount).map { randomName(20) }.toList()
    }

    @Timed(value = "lookup_time", extraTags = ["method", "getPerson"])
    @Counted(value = "lookup_count", extraTags = ["method", "getPerson"])
    fun getPerson(firstName: String, lastName: String): Person {
        val key = "$firstName $lastName"
//        val cachedPerson = personCache[key]
//        if (cachedPerson != null) {
//            hitsCounter.increment()
//            return cachedPerson
//        }
//        misesCounter.increment()
        val person = Person.generateRandom(firstName, lastName)
//        personCache[key] = person
        return person
    }

    fun getUniformRandomPerson() = getPerson(firstNames.random(), lastNames.random())

    fun getSkewedRandomPerson(skew: Double) = getPerson(firstNames.skewedRandom(skew), lastNames.skewedRandom(skew))

    internal fun generatePersonSlowly(firstName: String, lastName: String): Person {
        Thread.sleep(generationDelayMs)
        return Person.generateRandom(firstName, lastName)
    }

    @PostConstruct
    fun init() {
        Log.info(
            "Pre-populating cache with around ${config.prepopulatePercentage()}% of total" +
                " ${firstNames.size} first names and ${lastNames.size} last names",
        )
        personCache.drop()
        val batchSize = 1000
        // Save random persons to Redis
        val count: Int = (firstNamesCount * lastNamesCount * config.prepopulatePercentage() / 100)
        (1..count)
            .asSequence()
            .map { Person.generateRandom(firstName = firstNames.random(), lastName = lastNames.random()) }
            .chunked(batchSize)
            .forEach { personList ->
                Log.info("Persisting ${personList.size} persons")
                personCache.persist(personList)
            }
    }

    companion object {
        private fun randomName(length: Int) = RandomStringUtils.randomAlphabetic(length, length + 2)
    }
}

private fun <E> List<E>.skewedRandom(skew: Double): E {
    if (size == 1) return first()
    val midpoint = (size * skew).toInt()
    return when (Random.nextBoolean()) {
        true -> get(Random.nextInt(midpoint))
        false -> get(midpoint + Random.nextInt(size - midpoint))
    }
}
