package com.example.service

import com.example.model.Person
import io.quarkus.redis.datasource.RedisDataSource
import io.quarkus.redis.datasource.value.ValueCommands
import javax.inject.Singleton

@Singleton
class PersonCache(redisDataSource: RedisDataSource) {
    private val commands: ValueCommands<String, Person> = redisDataSource.value(Person::class.java)

    operator fun get(key: String): Person? {
        return commands[key]
    }

    operator fun set(key: String, person: Person) {
//        commands.setex(key, 1, person) // Expires after 1 second
        commands.set(key, person)
    }

    fun persist(personList: List<Person>) {
        commands.mset(personList.associateBy { it.id })
    }
}
