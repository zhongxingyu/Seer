 package ru.yandex.hackaton.server.db.model;
 
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;
 
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
 
    @Column(nullable = false)
    private String location;

 
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
 
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
