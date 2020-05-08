 
 package pt.uac.cafeteria.model.domain;
 
 import java.util.Calendar;
 
 /**
  * Represents a meal
  */
 public class Meal {
 
     /** Enumerates meal day time */
     public enum Time {
        LUNCH  { @Override public String toString() { return "Almoco"; } },
        DINNER { @Override public String toString() { return "Jantar"; } };
     }
 
     /** Enumerates dish types */
     public enum Type {
         MEAT  { @Override public String toString() { return "Carne"; } },
         FISH { @Override public String toString() { return "Peixe"; } },
         VEGETARIAN { @Override public String toString() { return "Vegetariano"; } };
     }
 
     /** Meal day time of service */
     private final Time time;
 
     /** Service date of the meal */
     private final Calendar date;
 
     /** Type of meal */
     private final Type type;
 
     /** Soup description */
     private String soup;
 
     /** Main course description */
     private String mainCourse;
 
     /** Dessert description */
     private String dessert;
 
     /**
      * Default Contructor
      *
      * @param type          meal day time of service
      * @param date          service date of the meal
      * @param dishType      Type of meal
      * @param Soup          soup description
      * @param mainCourse    main course description
      * @param dessert       dessert description
      */
     Meal(Calendar date, Time time, Type type, String soup, String mainCourse, String dessert) {
         this.time = time;
         this.date = date;
         this.type = type;
         this.soup = soup;
         this.mainCourse = mainCourse;
         this.dessert = dessert;
     }
 
    /** Returns the day time of service of a meal */
     Time getTime() {
         return this.time;
     }
 
     /** Returns the date of a meal */
     public Calendar getDate() {
         return this.date;
     }
 
     /** Returns the dish type of a meal */
     Type getType() {
         return this.type;
     }
 
     /** Returns the soup description of a meal */
     public String getSoup() {
         return this.soup;
     }
 
     /**
      * Changes the soup description of a meal
      *
      * @param Soup      the soup description that will be defined
      */
     public void setSoup(String soup) {
         this.soup = soup;
     }
 
     /** Returns the main course description of a meal */
     public String getMainCourse() {
         return this.mainCourse;
     }
 
     /**
      * Changes the main course description of a meal
      *
      * @param mainCourse    the main course description that will be defined
      */
     public void setMainCourse(String mainCourse) {
         this.mainCourse = mainCourse;
     }
 
     /** Returns the dessert description of a meal */
     public String getDessert() {
         return this.dessert;
     }
 
     /**
      * Changes the dessert description of a meal
      *
      * @param dessert       the dessert description that will be defined
      */
     public void setDessert(String dessert) {
         this.dessert = dessert;
     }
 
     /** Returns a string that describes a meal */
     @Override
     public String toString() {
         return "\n:Time " + this.time +
                 "\nDate: " + this.date +
                 "\nType: " + this.type +
                 "\nSoup: " + this.soup +
                 "\nMain course: " + this.mainCourse +
                 "\nDessert: " + this.dessert;
     }
 }
