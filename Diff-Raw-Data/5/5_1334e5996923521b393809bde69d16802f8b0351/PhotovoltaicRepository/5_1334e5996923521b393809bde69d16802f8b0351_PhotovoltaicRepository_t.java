 package edu.hawaii.ihale.photovoltaics;
 
 /**
  * Singleton object to store data for the Photovoltaics system.
  * @author Team Maka
  *
  */
 @edu.umd.cs.findbugs.annotations.SuppressWarnings(value =
   "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", 
   justification = "Singleton data storage class.")
 public class PhotovoltaicRepository {
   private static PhotovoltaicRepository instance = null;
   //private double goalPH = 7, goalTemp = 78, goalOxygen = .5;
   private static String energy,power,joules;
   /**
    * Constructor.
    */
   private PhotovoltaicRepository() {
     
   }
   
   /**
    * Returns the singleton instance.
    * @return the singleton instance.
    */
   public static synchronized PhotovoltaicRepository getInstance() {
       if (instance == null) {
           instance = new PhotovoltaicRepository();
           PhotovoltaicRepository.energy = "1443.5";
           PhotovoltaicRepository.power = "2226.2";
           PhotovoltaicRepository.joules = "2130813014";
       }
       return instance;
   }
 
   /**
    * Sets the energy level.
    * @param energy the energy level.
    */
   public synchronized void setEnergy(String energy) {
     PhotovoltaicRepository.energy = energy;
   }
   
   /**
    * Returns the current energy.
    * @return the current engery.
    */
   public synchronized String getEnergy() {
     return energy;
   }
 
   /**
    * Sets the power level.
    * @param power the new power level.
    */
   public synchronized void setPower(String power) {
     PhotovoltaicRepository.power = power;
   }
 
   /**
    * Returs the current power level.
    * @return the current power level.
    */
  public synchronized String getPower() {
     return power;
   }
 
   /**
    * Sets the joules value.
    * @param joules the new joules value.
    */
   public synchronized void setJoules(String joules) {
     PhotovoltaicRepository.joules = joules;
   }
 
   /**
    * Returns the current joules value.
    * @return the current joules value.
    */
  public synchronized String getJoules() {
     return joules;
   }
 }
