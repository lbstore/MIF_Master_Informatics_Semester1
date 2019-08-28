/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.objectdbjavafx.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;
import lt.lb.commons.F;
import lt.lb.commons.UUIDgenerator;

/**
 *
 * @author laim0nas100
 */
@PersistenceCapable
public class MetaList {

    public static final String clsName = MetaList.class.getName();
    @PrimaryKey
    public String id = UUIDgenerator.nextUUID(this.getClass().getSimpleName());

    public HashMap<String, EAValue> attributes = new HashMap<>();

    public void addAttribute(EAValue m) {
        this.attributes.put(m.getMeta().getName(), m);
    }

    public Boolean hasMetaName(String name) {
        return this.attributes.containsKey(name);
    }

    public static <T> T getOrNull(Class<T> type, Object ob) {
        if (ob == null) {
            return null;
        }
        if (F.instanceOf(ob, type)) {
            return (T) ob;
        } else {
            return null;
        }
    }

    public EAValue get(String name) {
        return this.attributes.get(name);
    }

    public String getString(String name) {

        EAValue get = this.get(name);
        if (get instanceof EAVString) {
            EAVString val = (EAVString) get;
            return val.get();
        }
        return null;
    }

    public Double getDecimal(String name) {
        EAValue get = this.get(name);
        if (get instanceof EAVDecimal) {
            EAVDecimal val = (EAVDecimal) get;
            return val.get();
        }
        return null;
    }

    public Date getDate(String name) {
        EAValue get = this.get(name);
        if (get instanceof EAVDate) {
            EAVDate val = (EAVDate) get;
            return val.get();
        }
        return null;
    }

    public Long getInteger(String name) {
        EAValue get = this.get(name);
        if (get instanceof EAVInteger) {
            EAVInteger val = (EAVInteger) get;
            return val.get();
        }
        return null;
    }

    public Boolean getBoolean(String name) {
        EAValue get = this.get(name);
        if (get instanceof EAVBoolean) {
            EAVBoolean val = (EAVBoolean) get;
            return val.get();
        }
        return null;
    }
}
