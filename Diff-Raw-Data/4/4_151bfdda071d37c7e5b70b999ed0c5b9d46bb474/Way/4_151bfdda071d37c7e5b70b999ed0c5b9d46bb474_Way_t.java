 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package models;
 
 import java.util.*;
 import javax.persistence.*;
 import play.db.jpa.Model;
 
 /**
  *
  * @author Antoine
  */
 @Entity
 public class Way extends Model {
 
     @ManyToOne
     public City startCity;
     
     @ManyToOne
     public City finishCity;
     
     @ManyToOne
     public User driver;
     
     @ManyToMany
     public List<User> passengers;
     
     @ManyToMany
     public List<City> cities;
     
     public Double distance;
     
     public Date dateHourStart;
     
     @ManyToOne
     public Car car;
     
     public Integer placeAvailable;
     
     public Double cost;
 
     public Way(City startCity, City finishCity, User driver, Date dateHourStart, Car car, Integer placeAvailable, Double cost) {
         this.startCity = startCity;
         this.finishCity = finishCity;
         this.driver = driver;
         this.passengers = new ArrayList<User>();
         this.cities = new ArrayList<City>();
         this.distance = new Double(0);
         this.dateHourStart = dateHourStart;
         this.car = car;
         this.placeAvailable = placeAvailable;
         this.cost = cost;
     }
     
     public Way(City startCity, City finishCity, Car car){
         this.startCity = startCity;
         this.finishCity = finishCity;
         this.car = car;
         this.cities = new ArrayList<City>();
     }
 
     /**
      * Retourne les trajets auquels l'utilisateur à participer comme passager
      * @param user
      * @return une liste de trajet
      */
     public static ArrayList<Way> waysByUserAsPassenger(User user) {
         ArrayList<Way> waysParticipation = new ArrayList<Way>();
 
         List<models.Way> ways = models.Way.findAll();
         for (Way way : ways) {
             if (way.passengers.contains(user)) {
                 waysParticipation.add(way);
             }
         }
         return waysParticipation;
     }
 
     /**
      * Recherche le chemin à prendre
      */
     public void calculateWay() throws Exception {
         long start = System.currentTimeMillis();
 
         TreeSet<Node> citiesTree = new TreeSet();
         Node node = new Node();
 
         citiesTree.add(new Node(null, this.startCity, new Long(-1), this.finishCity, 0));
         while (citiesTree.isEmpty() || node.getCity() != finishCity) {
             node = citiesTree.first();
             citiesTree.remove(node);
 
             for (Road road : node.getCity().roads()) {
                 City cityTmp = (road.firstCity.id == node.getCity().id) ? road.secondCity : road.firstCity;
                 citiesTree.add(new Node(node, cityTmp, road.id, this.finishCity, node.getDistanceCovored() + road.distance()));
             }
             if ((System.currentTimeMillis() - start) > 10000) {
                 throw new Exception("error during the calcul");
             }
         }
         saveWay(node);
         this.distance = calculDistanceInKm(cities);
     }
 
     /**
      * Calcul et retourne les routes à emprunter
      * @param node
      */
     private void saveWay(Node node) {
         Node nodeTmp = node;
         List<Road> roads = new ArrayList();
         this.cities.clear();
 
         while (nodeTmp.getPrevNode() != null) {
             roads.add((Road) Road.findById(nodeTmp.getIdRoad()));
             nodeTmp = nodeTmp.getPrevNode();
         }
 
         cities.add(finishCity);
         for (Road road : roads) {
             if (cities.get(cities.size() - 1).name.equals(road.firstCity.name)) {
                 cities.add(road.secondCity);
             } else {
                 cities.add(road.firstCity);
             }
         }
     }
 
     private double calculDistanceInKm(List<City> way) {
         double distanceCovored = 0;
         for (int i = 0; i < way.size() - 1; i++) {
             distanceCovored += Math.acos(Math.sin(way.get(i).latitude * Math.PI / 180) * Math.sin(way.get(i + 1).latitude * Math.PI / 180)
                     + Math.cos(way.get(i).latitude * Math.PI / 180) * Math.cos(way.get(i + 1).latitude * Math.PI / 180)
                     * Math.cos((way.get(i + 1).longitude * Math.PI / 180) - (way.get(i).longitude * Math.PI / 180))) * 6371;
         }
         return distanceCovored;
     }
     
     public double cost() throws Exception{
        double litreByKm = this.car.cost/100.0;
         double cost_fuel = 1.5290858352582;
         double tollByKm = 0.0599893844621958;
         double costWay = 0;
         
         this.calculateWay();
        System.out.println("calcul ="+distance+"*"+litreByKm+"*"+cost_fuel+" + "+distance+"*"+tollByKm);
         costWay = (this.distance*litreByKm)*cost_fuel + this.distance*tollByKm;
         
         return costWay;
     }
 }
