 package ca2;
 
 public class Student {
     
     private String firstName;
     private String lastName;
     private int ID;
     private int age;
     private boolean sex;        //0 = Female
     private double currentGPA;
 
     public Student(int ID, String firstName, String lastName, int age, boolean sex, double currentGPA) {
         this.ID = ID;
         this.firstName = firstName;
         this.lastName = lastName;  
         this.age = age;
         this.sex = sex;
         this.currentGPA = currentGPA;
     }
 
     public int getID() {
         return ID;
     }
 
     public void setID(int ID) {
         this.ID = ID;
     }
 
     public int getAge() {
         return age;
     }
 
     public void setAge(int age) {
         this.age = age;
     }
 
     public double getCurrentGPA() {
         return currentGPA;
     }
 
     public void setCurrentGPA(double currentGPA) {
         this.currentGPA = currentGPA;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public boolean isSex() {
         return sex;
     }
 
     public void setSex(boolean sex) {
         this.sex = sex;
     }
 
     public String toString() {
        return "firstName: " + firstName + "\nlastName: " + lastName + "\nID: " + ID + "\nage: " + age + "\nsex: " + sex + "\ncurrentGPA: " + currentGPA;
     }
 }
