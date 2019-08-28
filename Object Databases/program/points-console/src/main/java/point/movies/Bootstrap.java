/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package point.movies;

import com.objectdb.Utilities;
import javax.jdo.PersistenceManager;

/**
 *
 * @author laim0nas100
 */
public class Bootstrap {
    static PersistenceManager pm = Test2.pm;
    public static void main(String...args){
        pm.currentTransaction().begin();
        Studio studio = new Studio();
        studio.name = "Queens Indie Film";
        pm.makePersistent(studio);
        pm.currentTransaction().commit();
    }
}
