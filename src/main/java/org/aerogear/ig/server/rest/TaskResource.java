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
import org.aerogear.ig.server.model.Task;
import org.aerogear.ig.server.util.InvalidParentException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Stateless
@Path("/tasks")
public class TaskResource {

    @PersistenceContext(unitName = "aerogear-ig-server")
    private EntityManager em;

    @Inject
    TagResource tags;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Task create(Task task, @Context UriInfo uriInfo) {
        // extract Project ID (if set) from the path
        Long projectId = (uriInfo.getPathSegments().size() != 1?
                         Long.valueOf(uriInfo.getPathSegments().get(1).getPath())
                         :null);

        if (projectId != null) {
            Project proj = em.find(Project.class, projectId);

            if (proj == null) {
                throw new InvalidParentException(String.format("Project with ID %s not found for this task", projectId));
            }

            task.setProject(proj);
        }

        em.persist(task);

        return task;
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Task update(@PathParam("id")
                       Long id, Task task, @Context UriInfo uriInfo) {
        task.setId(id);

        // extract Project ID (if set) from the path
        Long projectId = (uriInfo.getPathSegments().size() != 2?
                         Long.valueOf(uriInfo.getPathSegments().get(1).getPath())
                         :null);

        if (projectId != null) {
            Project proj = em.find(Project.class, projectId);

            if (proj == null) {
                throw new InvalidParentException(String.format("Project with ID %s not found for this task!", projectId));
            }

            task.setProject(proj);
        }

        task = em.merge(task);

        return task;
    }

    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteById(@PathParam("id")
                           Long id) {
        Task result = em.find(Task.class, id);
        em.remove(result);
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Task findById(@PathParam("id")
                         Long id) {
        return em.find(Task.class, id);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Task> listAll(@Context UriInfo uriInfo) {
        // extract Project ID (if set) from the path
        Long projectId = (uriInfo.getPathSegments().size() != 1?
                         Long.valueOf(uriInfo.getPathSegments().get(1).getPath())
                         :null);

        List<Task> results;

        if (projectId != null) {
            results = em.createQuery("SELECT t FROM Task t INNER JOIN t.project p WHERE p.id = :projId", Task.class)
                    .setParameter("projId", projectId)
                    .getResultList();
        } else {
            results = em.createQuery("SELECT t FROM Task t", Task.class).getResultList();
        }

        return results;
    }

    /*
     * handles nested resource 'Tags'
     */
    @Path("/{id:[0-9][0-9]*}/tags")
    public TagResource listTasks(@PathParam("id") Long id) {
        return tags;
    }
}