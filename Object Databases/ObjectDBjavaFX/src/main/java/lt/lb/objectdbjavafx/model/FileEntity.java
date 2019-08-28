/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.objectdbjavafx.model;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;
import lt.lb.commons.UUIDgenerator;


/**
 *
 * @author laim0nas100
 */
@PersistenceCapable
public class FileEntity {
public static final String clsName = FileEntity.class.getName();
    @PrimaryKey
    private String id = UUIDgenerator.nextUUID(this.getClass().getSimpleName());
    
    public Long count(){
        return 1L;
    }
    private MetaList meta;
    
    @Override
    public String toString(){
        return id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MetaList getMeta() {
        return meta;
    }

    public void setMeta(MetaList meta) {
        this.meta = meta;
    }
    
    

}
