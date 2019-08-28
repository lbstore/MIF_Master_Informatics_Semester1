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
public class EAVDecimal extends EAValue{
    
    public static final String clsName = EAVDecimal.class.getName();
    private Double decimal;

    @Override
    public Double get() {
        return decimal;
    }

    @Override
    public void set(Object v) {
        decimal = F.cast(v);
    }
    
    
}
