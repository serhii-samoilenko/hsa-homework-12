package com.example.api

import com.example.service.PersonGeneratorService
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType.APPLICATION_JSON
import javax.ws.rs.core.Response

@ApplicationScoped
@Path("/person")
class PersonResource(
    private val personGeneratorService: PersonGeneratorService,
) {

    @GET
    @Produces(APPLICATION_JSON)
    fun getPerson(
        @QueryParam("firstName") firstName: String,
        @QueryParam("lastName") lastName: String,
    ): Response {
        val entity = personGeneratorService.generatePerson(firstName, lastName)
        return Response.ok(entity).build()
    }

    @GET
    @Path("/random")
    @Produces(APPLICATION_JSON)
    fun getRandomPerson(): Response {
        val entity = personGeneratorService.generatePerson()
        return Response.ok(entity).build()
    }
}
