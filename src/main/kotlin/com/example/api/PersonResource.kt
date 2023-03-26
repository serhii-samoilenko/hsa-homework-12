package com.example.api

import com.example.service.PersonService
import io.micrometer.core.annotation.Counted
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType.APPLICATION_JSON
import javax.ws.rs.core.MediaType.TEXT_PLAIN
import javax.ws.rs.core.Response

@ApplicationScoped
@Path("/person")
class PersonResource(private val personService: PersonService) {

    @GET
    @Produces(APPLICATION_JSON)
    @Counted(value = "get_count", extraTags = ["method", "getPerson"])
    fun getPerson(
        @QueryParam("firstName") firstName: String,
        @QueryParam("lastName") lastName: String,
    ): Response {
        val entity = personService.getPerson(firstName, lastName)
        return Response.ok(entity).build()
    }

    @GET
    @Path("/random")
    @Produces(APPLICATION_JSON)
    @Counted(value = "random_count", extraTags = ["method", "getRandomPerson"])
    fun getRandomPerson(
        @QueryParam("skew") skew: Double?,
    ): Response {
        val entity = if (skew != null) {
            personService.getSkewedRandomPerson(skew)
        } else {
            personService.getUniformRandomPerson()
        }
        return Response.ok(entity).build()
    }

    @GET
    @Path("/wtf")
    @Produces(TEXT_PLAIN)
    @Counted(value = "wtf_count", extraTags = ["method", "getWtf"])
    fun getWtf(): Response {
        return Response.ok("OK").build()
    }
}
