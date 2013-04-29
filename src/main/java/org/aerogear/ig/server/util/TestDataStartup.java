package org.aerogear.ig.server.util;

import org.aerogear.ig.server.model.Project;
import org.aerogear.ig.server.model.Tag;
import org.aerogear.ig.server.model.Task;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.*;

import static org.aerogear.ig.server.util.DateBuilder.newDateBuilder;

@Singleton
@Startup
public class TestDataStartup {

    @PersistenceContext(unitName = "aerogear-ig-server")
    private EntityManager em;

    @PostConstruct
    public void initDB() {
        // initialize some dummy data to start with
        Tag t1 = new Tag();
        t1.setTitle("tag1");
        t1.setStyle("style-100-100-100");

        em.persist(t1);

        Tag t2 = new Tag();
        t2.setTitle("tag2");
        t2.setStyle("style-100-100-100");

        em.persist(t2);

        Project p1 = new Project();
        p1.setTitle("Project");
        p1.setStyle("style-100-100-100");
        em.persist(p1);

        Task k1 = new Task();
        k1.setTitle("task1");
        k1.setDescription("empty descr");
        k1.setTags(Arrays.asList(t1));
        k1.setDate(Calendar.getInstance());
        k1.setProject(p1);

        em.persist(k1);

        Task k2 = new Task();
        k2.setTitle("task2");
        k2.setDescription("empty descr");
        k2.setTags(Arrays.asList(t1, t2));
        k2.setDate(Calendar.getInstance());
        k2.setProject(p1);

        em.persist(k2);

        Project p2 = new Project();
        p2.setTitle("Project 2");
        p2.setStyle("style-100-100-100");

        em.persist(p2);
    }
}
