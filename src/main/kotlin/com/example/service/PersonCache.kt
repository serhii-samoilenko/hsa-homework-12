package com.example.service

import com.example.model.Person
import io.quarkus.redis.datasource.ReactiveRedisDataSource
import io.quarkus.redis.datasource.keys.RedisKeyNotFoundException
import io.smallrye.mutiny.Uni
import javax.inject.Singleton

@Singleton
class PersonCache(private val redisDataSource: ReactiveRedisDataSource) {
    private val valueCommands = redisDataSource.value(String::class.java, Person::class.java)
    private val keyCommands = redisDataSource.key()

    fun set(key: String, person: Person): Uni<Void> = valueCommands.set(key, person)

    fun setx(key: String, person: Person, ttl: Long): Uni<Void> = valueCommands.psetex(key, ttl, person)

    fun get(key: String): Uni<Person> = valueCommands.get(key)

    fun getx(key: String): Uni<Pair<Person, Long>?> =
        keyCommands.ttl(key)
            .chain { ttl ->
                valueCommands.get(key).chain { person ->
                    if (person == null) {
                        Uni.createFrom().nullItem()
                    } else {
                        Uni.createFrom().item(person to ttl)
                    }
                }
            }.onFailure().recoverWithItem { ex ->
                if (ex is RedisKeyNotFoundException) {
                    null
                } else {
                    throw ex
                }
            }

    fun persist(personList: List<Person>, ttl: Long) {
        valueCommands.mset(personList.associateBy { it.id }).await().indefinitely()
        personList.map {
            keyCommands.pexpire(it.id, ttl).await().indefinitely()
        }
    }

    fun drop() {
        redisDataSource.flushall().await().indefinitely()
    }
}
