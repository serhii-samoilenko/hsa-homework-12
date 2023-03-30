package com.example.api

import com.example.model.Person
import com.example.service.PersonService
import io.smallrye.mutiny.Uni
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType.APPLICATION_JSON

@ApplicationScoped
@Path("/person")
class PersonResource(private val personService: PersonService) {

    @GET
    @Produces(APPLICATION_JSON)
    fun getPerson(
        @QueryParam("key") key: String,
    ): Uni<Person> {
        return personService.getPerson(key)
    }

    @GET
    @Path("/random")
    @Produces(APPLICATION_JSON)
    fun getRandomPerson(
        @QueryParam("distributed") distributed: Boolean = false,
        @QueryParam("probabilistic") probabilistic: Boolean = false,
    ): Uni<Person> {
        return personService.getRandomPerson(probablyDistributed = distributed, useProbabilisticCache = probabilistic)
    }
}
