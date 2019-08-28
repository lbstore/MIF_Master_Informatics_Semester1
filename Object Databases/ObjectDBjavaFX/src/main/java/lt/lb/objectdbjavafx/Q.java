/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.objectdbjavafx;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import lt.lb.commons.F;
import lt.lb.commons.Lambda;
import lt.lb.commons.threads.Futures;
import lt.lb.commons.threads.UnsafeRunnable;
import lt.lb.objectdbjavafx.model.FileEntity;

/**
 *
 * @author laim0nas100
 */
public class Q {

    public static final String packPrefix = "lt.lb.objectdbjavafx.model.";

    public static String esc(String str) {
        return "\"" + str + "\"";
    }

    public static void persist(FileEntity ent) {
        Q.submit(pm -> {
            pm.makePersistent(ent);
        });
    }

    public static <T> Optional<T> getFirst(Query q, Object... params) {
        List<T> list = getAll(q, params);
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(list.get(0));
        }
    }

    public static <T> void getFirst(Consumer<T> cons, Query q, Object... params) {
        Optional<T> first = getFirst(q, params);
        first.ifPresent(cons);
    }

    public static <T> List<T> getAll(Query q, Object... params) {
        List<T> cast = F.cast(q.executeWithArray(params));
        return cast;
    }

    public static <T> void getAll(Consumer<List<T>> cons, Query q, Object... params) {
        cons.accept(F.cast(q.executeWithArray(params)));
    }

    public static <V> Future<V> submit(Callable<V> call) {
        return submit(call, false);
    }

    public static <V> Future<V> submit(Callable<V> call, boolean readonly) {
        FutureTask<V> future = Futures.of(call);
        submit(UnsafeRunnable.from(future), readonly);
        return future;
    }

    public static Optional<Throwable> submit(UnsafeRunnable run) {
        return submit(run, false);
    }

    private static ThreadLocal<Boolean> inside = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public static Optional<Throwable> submit(UnsafeRunnable run, boolean readonly) {

        if (inside.get()) { // nested call
            return F.checkedRun(run);
        } else {
            inside.set(true);
        }
        Transaction t = Main.pm.get().currentTransaction();

        if (!t.isActive()) {
            t.begin();
        }
        Optional<Throwable> checkedRun = F.checkedRun(run);
        if (!readonly && t.isActive()) {
            if (checkedRun.isPresent()) {
                t.rollback();
            } else {
                t.commit();
            }
        }
        inside.set(false);
        return checkedRun;
    }

    public static Optional<Throwable> submit(Lambda.L1<PersistenceManager> pm, boolean readonly) {
        return submit(() -> {
            pm.accept(Main.pm.get());
        }, readonly);
    }

    public static Optional<Throwable> submit(Lambda.L1<PersistenceManager> pm) {
        return submit(pm, false);
    }

}
