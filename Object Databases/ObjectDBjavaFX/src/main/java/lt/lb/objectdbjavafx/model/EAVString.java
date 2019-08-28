/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.objectdbjavafx.model;

import javax.jdo.annotations.PersistenceCapable;

/**
 *
 * @author laim0nas100
 */
@PersistenceCapable
public class EAVString extends EAValue {

    public static final String clsName = EAVString.class.getName();
    private String string;

    @Override
    public String get() {
        return string;
    }

    @Override
    public void set(Object v) {
        this.string = (String) v;
    }

}
