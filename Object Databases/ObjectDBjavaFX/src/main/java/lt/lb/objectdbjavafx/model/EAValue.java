/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.objectdbjavafx.model;

import java.io.Serializable;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;
import lt.lb.commons.UUIDgenerator;
import lt.lb.commons.interfaces.ValueProxy;

/**
 *
 * @author laim0nas100
 */
@PersistenceCapable
public class EAValue implements Serializable, ValueProxy<Object> {

    public static final String clsName = EAValue.class.getName();
    
    @PrimaryKey
    private String id = UUIDgenerator.nextUUID(this.getClass().getSimpleName());

    private Meta meta;

    public EAValue() {
    }

    @Override
    public String toString() {
        return this.id + " " + get();
    }

    @Override
    public Object get() {
        return null;
    }

    @Override
    public void set(Object v) {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }
    
    

}
