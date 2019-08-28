/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.objectdbjavafx.model;

import javax.jdo.annotations.PersistenceCapable;
import lt.lb.commons.F;

/**
 *
 * @author laim0nas100
 */
@PersistenceCapable
public class EAVInteger extends EAValue{
    public static final String clsName = EAVInteger.class.getName();
    
    private Long integer;

    @Override
    public Long get() {
        return integer;
    }

    @Override
    public void set(Object v) {
        integer = F.cast(v);
    }
    
}
