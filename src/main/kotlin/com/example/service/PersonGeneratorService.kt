package com.example.service

import com.example.config.AppConfig
import com.example.model.Person
import io.quarkus.logging.Log
import io.quarkus.runtime.Startup
import org.apache.commons.lang3.RandomStringUtils
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import kotlin.math.sqrt

@Startup
@ApplicationScoped
class PersonGeneratorService(
    private val config: AppConfig,
) {
    private val firstNamesCount = sqrt(config.entityCount().toDouble()).toInt()
    private val lastNamesCount = firstNamesCount
    private val firstNames: List<String>
    private val lastNames: List<String>

    init {
        Log.info("Creating pool of $firstNamesCount first names and $lastNamesCount last names")
        firstNames = (1..firstNamesCount).map { randomName(19) }.toList()
        lastNames = (1..lastNamesCount).map { randomName(20) }.toList()
    }

    fun generatePerson() = Person.generateRandom(firstName = firstNames.random(), lastName = lastNames.random())

    fun generatePerson(firstName: String, lastName: String) = Person.generateRandom(firstName, lastName)

    @PostConstruct
    fun init() {
        Log.info("Deleting all existing records from DB")
        Log.info(
            "Pre-populating DB with around ${config.prepopulatePercentage()}% of total" +
                " ${firstNames.size} first names and ${lastNames.size} last names",
        )
        val batchSize = 1000
        // Save random persons to mongo in batches:
        val count: Int = (firstNamesCount * lastNamesCount * config.prepopulatePercentage() / 100)
        (1..count)
            .asSequence()
            .map { Person.generateRandom(firstName = firstNames.random(), lastName = lastNames.random()) }
//            .map { it.toEntity() }
            .chunked(batchSize)
            .forEach { personList ->
                Log.info("Persisting ${personList.size} persons")
//                personRepository.persist(personList)
            }
        Log.info("DB now contains...")
    }

    companion object {
        private fun randomName(length: Int) = RandomStringUtils.randomAlphabetic(length, length + 2)
    }
}
