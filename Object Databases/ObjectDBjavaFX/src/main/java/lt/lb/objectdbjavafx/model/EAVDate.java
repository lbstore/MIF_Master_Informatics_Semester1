/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.objectdbjavafx.model;

import java.util.Date;
import javax.jdo.annotations.PersistenceCapable;
import lt.lb.commons.F;

/**
 *
 * @author laim0nas100
 */
@PersistenceCapable
public class EAVDate extends EAValue{

    public static final String clsName = EAVDate.class.getName();
    private Date date;
    @Override
    public Date get() {
        return date;
    }

    @Override
    public void set(Object v) {
        date = F.cast(v);
    }
    
}
