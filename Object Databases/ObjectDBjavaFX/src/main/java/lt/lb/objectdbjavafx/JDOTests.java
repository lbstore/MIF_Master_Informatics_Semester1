/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.objectdbjavafx;

import java.util.List;
import java.util.Optional;
import javax.jdo.Query;
import lt.lb.commons.Log;
import lt.lb.objectdbjavafx.model.EAVString;
import lt.lb.objectdbjavafx.model.EAValue;
import lt.lb.objectdbjavafx.model.FileEntity;
import lt.lb.objectdbjavafx.model.FileEntityFolder;
import lt.lb.objectdbjavafx.model.Meta;

/**
 *
 * @author laim0nas100
 */
public class JDOTests {

    public static void selectAllFolders() {
        Log.print("Select all folders");
        Q.submit(pm -> {
            Log.printLines(Q.getAll(pm.newQuery(FileEntityFolder.class)));
        });
    }

    public static void selectJustFiles() {
        Log.print("Select all files");
        Q.submit(pm -> {
            Query q = pm.newQuery(FileEntity.class);
            String cls = FileEntityFolder.class.getName();

            q.setFilter("!(this instanceof " + cls + ")");
            Log.printLines(Q.getAll(q));
        });
    }

    public static void selectEAVbyMetaRoot() {
        Log.print("Select EAValue by meta [root]");
        Meta metaByName = FS.getMetaByName("root");
        List<EAValue> valuesByMeta = FS.getValuesByMeta(metaByName);
        Log.printLines(valuesByMeta);
    }

    public static void selectEAVbyMetaFileName() {
        Log.print("Select EAValue by meta [fileName]");
        Meta metaByName = FS.getMetaByName("fileName");
        List<EAValue> valuesByMeta = FS.getValuesByMeta(metaByName);
        Log.printLines(valuesByMeta);
    }

    public static void fullTextSearch1() {
        Log.print("Full text search test with [other]]");
        Log.printLines(FS.fullTextSearch("other"));
    }

    public static void fullTextSearch2() {
        Log.print("Full text search test with [text]]");
        Log.printLines(FS.fullTextSearch("text"));
    }


    public static void queryTest(String str) {
        Optional<Throwable> submit = Q.submit(pm -> {
            Log.println("Execute:  ", str);
            Query query = pm.newQuery(str);
            List<Object> all = Q.getAll(query);
            Log.printLines(all);
        });
        FS.report(submit);
    }

    public static void selectByTypeTests() {
        Log.print("Select subtype of EAValue:");
        Log.print("By JDOQL query");
        queryTest("Select s from " + EAValue.clsName + " as s where s instanceof " + EAVString.clsName);
        Log.print("With JDO interface");
        FS.report(Q.submit(pm -> {
            Query newQuery = pm.newQuery(EAValue.class);
            newQuery.setFilter("this instanceof " + EAVString.clsName);
            Log.printLines(Q.getAll(newQuery));
        }));
        Log.print("With explicit subclass");
        FS.report(Q.submit(pm -> {
            Log.printLines(Q.getAll(pm.newQuery(EAVString.class)));
        }));

    }



}
