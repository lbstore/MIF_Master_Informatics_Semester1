package point;

import java.nio.file.Path;
import java.nio.file.Paths;
import javax.persistence.*;
import java.util.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import lt.lb.commons.F;
import lt.lb.commons.Log;

public class Main {

    public static void main(String[] args) {
        // Open a database connection
        // (create a new database if it doesn't exist yet):

        String path = "$objectdb/db/points.odb";
        Path get = Paths.get(path);
        Log.print("ok");
        Log.print(get.toAbsolutePath().toString());

        EntityManagerFactory emf
                = Persistence.createEntityManagerFactory("../db/points.odb");
        EntityManager em = emf.createEntityManager();

        // Store 1000 Point objects in the database:
        em.getTransaction().begin();
        for (int i = 0; i < 1000; i++) {
            Point p = new Point(i, i);
            em.persist(p);
        }
        em.getTransaction().commit();

        // Find the number of Point objects in the database:
        Query q1 = em.createQuery("SELECT COUNT(p) FROM Point p");
        Log.println("Total Points: " + q1.getSingleResult());

        // Find the average X value:
        CriteriaBuilder cb = em.getCriteriaBuilder();

        F.unsafeRun(() -> {
            CriteriaQuery<Double> query = cb.createQuery(Double.class);
            Root<Point> root = query.from(Point.class);
            Expression<Double> avg = cb.avg(root.get("x"));
            query.select(avg);
            TypedQuery<Double> finalQuery = em.createQuery(query);
             Log.print("Average X:"+finalQuery.getSingleResult());
        });

//        Query q2 = em.createQuery("SELECT AVG(p.x) FROM Point p");
//        Log.println("Average X: " + q2.getSingleResult());
       

        // Retrieve all the Point objects from the database:
        TypedQuery<Point> q
                = em.createQuery("SELECT p FROM Point p", Point.class);
        List<Point> results = q.getResultList();
        for (Point p : results) {
//            Log.println(p);
        }

        // Close the database connection:
        em.close();
        emf.close();
    }
}
