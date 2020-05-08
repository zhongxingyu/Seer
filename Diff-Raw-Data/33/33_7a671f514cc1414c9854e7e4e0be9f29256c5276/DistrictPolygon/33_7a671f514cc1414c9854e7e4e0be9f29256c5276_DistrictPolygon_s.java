 package ru.yandex.hackaton.server.resources;
 
import javafx.util.Pair;
 import ru.yandex.hackaton.server.db.model.District;
 import ru.yandex.hackaton.server.geocoder.geo.Line;
 import ru.yandex.hackaton.server.geocoder.geo.Point;
 
import java.util.ArrayList;
import java.util.List;

 /**
  * Created with IntelliJ IDEA.
  * User: eoff
  * Date: 15.09.13
  * Time: 5:57
  * To change this template use File | Settings | File Templates.
  */
 public class DistrictPolygon {
     private int id;
     private String name;
     private List<Double[]> coords;
 
     public DistrictPolygon(District district) {
         this.id = district.getId();
         this.name = district.getName();
         coords = new ArrayList<>();
         for (Point point : Line.parseWkt(district.getBorders()).points()) {
             coords.add(new Double[]{point.getLat(), point.getLon()});
         }
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public List<Double[]> getCoords() {
         return coords;
     }
 
     public void setCoords(List<Double[]> coords) {
         this.coords = coords;
     }
 }
