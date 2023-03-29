package com.example.service

import com.example.model.Person
import io.quarkus.redis.datasource.ReactiveRedisDataSource
import io.quarkus.redis.datasource.value.ReactiveValueCommands
import io.smallrye.mutiny.Uni
import javax.inject.Singleton

@Singleton
class PersonCache(private val redisDataSource: ReactiveRedisDataSource) {
    private val commands: ReactiveValueCommands<String, Person> = redisDataSource.value(String::class.java, Person::class.java)

    fun get(key: String): Uni<Person> {
        return commands.get(key)
    }

    fun set(key: String, person: Person): Uni<Void> {
        return commands.set(key, person)
    }

    fun persist(personList: List<Person>): Uni<Void> {
        return commands.mset(personList.associateBy { it.id })
    }

    fun drop(): Uni<Void> {
        return redisDataSource.flushall()
    }
}
