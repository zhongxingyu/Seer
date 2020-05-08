 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.sola.services.ejb.administrative.repository.entities;
 
 import java.math.BigDecimal;
 import javax.persistence.Column;
 import javax.persistence.Id;
 import javax.persistence.Table;
import org.sola.services.common.repository.AccessFunctions;
 import org.sola.services.common.repository.entities.AbstractVersionedEntity;
 
 /**
  *
  * @author rizzom
  */
 @Table(name = "ba_unit_area", schema = "administrative")
 public class BaUnitArea extends AbstractVersionedEntity {
     public static final String QUERY_WHERE_BYBAUNITID = "baUnitId";
     public static final String QUERY_WHERE_BYUNITAREAID = "ba_unit_id = #{" + QUERY_WHERE_BYBAUNITID + "}";
     public static final String QUERY_ORDER_BYCHANGETIME = " change_time desc ";
     
     @Id
     @Column(name = "id")
     private String id;
     @Column(name = "ba_unit_id")
     private String baUnitId;
     @Column(name = "size")
     private BigDecimal size;
     @Column(name = "type_code")
     private String typeCode;
    @Column(insertable=false, updatable=false, name = "calculated_area_size")
    @AccessFunctions(onSelect = "administrative.get_calculated_area_size_action(ba_unit_id)")
    private BigDecimal calculatedAreaSize;

    public BigDecimal getCalculatedAreaSize() {
        return calculatedAreaSize;
    }

    public void setCalculatedAreaSize(BigDecimal calculatedAreaSize) {
        this.calculatedAreaSize = calculatedAreaSize;
    }
     
     public String getBaUnitId() {
         return baUnitId;
     }
 
     public void setBaUnitId(String baUnitId) {
         this.baUnitId = baUnitId;
     }
 
     public String getId() {
         id = id == null ? generateId() : id;
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public BigDecimal getSize() {
         return size;
     }
 
     public void setSize(BigDecimal size) {
         this.size = size;
     }
 
     public String getTypeCode() {
         return typeCode;
     }
 
     public void setTypeCode(String typeCode) {
         this.typeCode = typeCode;
     }
     
     
     
     
 }
