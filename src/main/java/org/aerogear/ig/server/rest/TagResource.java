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

import org.aerogear.ig.server.model.Tag;
import org.aerogear.ig.server.model.Task;
import org.aerogear.ig.server.util.InvalidParentException;
import org.omg.CORBA.DynAnyPackage.Invalid;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.List;

@Stateless
@Path("/tags")
public class TagResource {

    @PersistenceContext(unitName = "aerogear-ig-server")
    private EntityManager em;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Tag create(Tag tag, @Context UriInfo uriInfo) {
        // extract Task ID (if set) from the path
        Long taskId = (uriInfo.getPathSegments().size() != 1?
                Long.valueOf(uriInfo.getPathSegments().get(3).getPath())
                :null);

        if (taskId != null) {
            Task task = em.find(Task.class, taskId);

            if (task == null) {
                throw new InvalidParentException(String.format("Task with ID %s not found for this tag!", taskId));
            }

            task.getTags().add(tag);
        }

        em.persist(tag);

        return tag;
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Tag update(@PathParam("id")
                      Long id, Tag tag, @Context UriInfo uriInfo) {
        tag.setId(id);

        // extract Task ID (if set) from the path
        Long taskId = (uriInfo.getPathSegments().size() != 1?
                Long.valueOf(uriInfo.getPathSegments().get(3).getPath())
                :null);

        if (taskId != null) {
            Task task = em.find(Task.class, taskId);

            if (task == null) {
                throw new InvalidParentException(String.format("Task with ID %s not found for this tag!", taskId));
            }

            // remove existing and add the updated one
            if (task.getTags().contains(tag)) {
                task.getTags().remove(tag);
                task.getTags().add(tag);
            }
        }

        tag = em.merge(tag);

        return tag;
    }

    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> deleteById(@PathParam("id")
                                 Long id) {
        List<Long> taskIds = em.createQuery("SELECT t.id FROM Task t INNER JOIN t.tags o WHERE o.id = :id", Long.class)
                .setParameter("id", id)
                .getResultList();

        Tag tag = em.find(Tag.class, id);
        for (Task task : tag.getTasks()) {
            task.getTags().remove(tag);
        }
        em.merge(tag);
        em.remove(tag);

        return taskIds;
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Tag findById(@PathParam("id")
                        Long id) {
        return em.find(Tag.class, id);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Tag> listAll(@Context UriInfo uriInfo) {
        // extract Task ID (if set) from the path
        Long taskId = (uriInfo.getPathSegments().size() != 1?
                Long.valueOf(uriInfo.getPathSegments().get(3).getPath())
                :null);

        List<Tag> result ;

        if (taskId != null) {
            result = em.createQuery("SELECT t FROM Tag t INNER JOIN t.tasks p WHERE p.id = :taskId", Tag.class)
                    .setParameter("taskId",taskId)
                    .getResultList();
        } else {
            result = em.createQuery("SELECT t FROM Tag t", Tag.class).getResultList();
        }
        return result;
    }
}