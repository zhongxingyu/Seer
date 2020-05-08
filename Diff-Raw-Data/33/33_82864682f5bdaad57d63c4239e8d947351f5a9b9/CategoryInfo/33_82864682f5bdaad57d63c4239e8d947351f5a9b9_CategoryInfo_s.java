 package ru.yandex.hackaton.server.db.model;
 
import javax.persistence.*;
 
 @MappedSuperclass
 public abstract class CategoryInfo extends BaseModel<Integer>{
     @Id
     @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "districtsInfoGen")
     @SequenceGenerator(name = "districtsInfoGen", sequenceName = "distInfo_id")
     private Integer id;
 
     @Column
     private Integer districtId;
 
     @Column(nullable = false)
     private String name;
 
     @Column(nullable = false)
     private String address;
 
 
     public String getAddress() {
         return address;
     }
 
     public void setAddress(String address) {
         this.address = address;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Integer getDistrictId() {
         return districtId;
     }
 
     public void setDistrictId(Integer districtId) {
         this.districtId = districtId;
     }
 
     @Override
     public Integer getId() {
         return id;
     }
 
     @Override
     public void setId(Integer integer) {
         id = integer;
     }
 }
