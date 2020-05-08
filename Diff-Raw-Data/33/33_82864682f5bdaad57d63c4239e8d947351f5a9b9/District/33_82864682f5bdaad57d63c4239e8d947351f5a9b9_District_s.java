 package ru.yandex.hackaton.server.db.model;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 import ru.yandex.hackaton.server.geocoder.geo.Line;
 
 /**
  * @author Sergey Polovko
  */
 @Entity
 @Table(name = "districts")
 public class District extends BaseModel<Integer> {
 
     @Id
     @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "districtsGen")
     @SequenceGenerator(name = "districtsGen", sequenceName = "districts_id")
     private Integer id;
 
     @Column(nullable = false)
     private String name;
 
     @Column(nullable = false)
     private String borders;
 
 
     @Override
     public Integer getId() {
         return id;
     }
 
     @Override
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getBorders() {
         return borders;
     }
 
     public void setBorders(String borders) {
         this.borders = borders;
     }
 
     public Line getGmlLine() {
         return Line.parseGml(this.borders);
     }
 
     public Line getWktLine() {
         return Line.parseWkt(this.borders);
     }
 }
