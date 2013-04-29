/*
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aerogear.ig.server.rest;

import org.aerogear.ig.server.model.Project;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Stateless
@Path("/projects")
public class ProjectResource {

    @PersistenceContext(unitName = "aerogear-ig-server")
    private EntityManager em;

    @Inject
    TaskResource tasks;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Project create(Project entity) {
        em.persist(entity);
        return entity;
    }

    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> deleteById(@PathParam("id") Long id) {
        List<Long> taskIds = em.createQuery("SELECT t.id FROM Task t INNER JOIN t.project p WHERE p.id = :id", Long.class)
                .setParameter("id", id)
                .getResultList();

        Project project = em.find(Project.class, id);

        em.createQuery("UPDATE Task t SET t.project.id = null WHERE t.project.id = :id")
                .setParameter("id", id)
                .executeUpdate();

        em.remove(project);

        return taskIds;
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Project findById(@PathParam("id") Long id) {
        return em.find(Project.class, id);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Project> listAll() {
        return em.createQuery("SELECT p FROM Project p", Project.class).getResultList();
    }

    /*
     * handles nested resource 'Tasks'
     */
    @Path("/{id:[0-9][0-9]*}/tasks")
    public TaskResource listTasks(@PathParam("id") Long id) {
        return tasks;
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Project update(@PathParam("id") Long id, Project entity) {
        entity.setId(Long.valueOf(id));
        entity = em.merge(entity);
        return entity;
    }
}