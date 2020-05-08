 package vn.hust.smie;
 
 import java.util.Calendar;
 
 public class User {
 
     public static final int SEX_FEMALE = 0;
     public static final int SEX_MALE = 1;
 
     public static final int ACTIVITY_FEW = 0;
     public static final int ACTIVITY_NORMAL = 1;
     public static final int ACTIVITY_MANY = 2;
 
    public static final int BMI_SEVERELY_UNDERWEIGHT = -2;
     public static final int BMI_UNDERWEIGHT = -1;
     public static final int BMI_NORMAL = 0;
     public static final int BMI_OVERWEIGHT = 1;
     public static final int BMI_OBESE = 2;
 
     /* Private info */
     private String name;
 
     private int sex;
     private int yearOfBirth;
     private double height; // in m
     private double weight; // in kg
     private int activity;
 
     public User() {
 	name = "User";
 	sex = SEX_MALE;
 	yearOfBirth = 1990;
 	height = 170;
 	weight = 55;
 	activity = ACTIVITY_NORMAL;
     }
 
     public String getName() {
 	return name;
     }
 
     public void setName(String name) {
 	this.name = name;
     }
 
     public int getSex() {
 	return sex;
     }
 
     public void setSex(int sex) {
 	if (sex == SEX_MALE || sex == SEX_FEMALE) this.sex = sex;
     }
 
     public int getYearOfBirth() {
 	return yearOfBirth;
     }
 
     public void setYearOfBirth(int year) {
 	this.yearOfBirth = year;
     }
 
     /**
      * Get height
      * @return Height in meter (m)
      */
     public double getHeight() {
 	return height;
     }
 
     /**
      * Set height
      * @param height Height in meter (m)
      */
     public void setHeight(double height) {
 	this.height = height;
     }
 
     /**
      * Get weight
      * @return Weight in kilogram (kg)
      */
     public double getWeight() {
 	return weight;
     }
 
     /**
      * Set weight
      * @param weight Weight in kilogram (kg)
      */
     public void setWeight(double weight) {
 	this.weight = weight;
     }
 
     public int getActivity() {
 	return activity;
     }
 
     public void setActivity(int activity) {
 	this.activity = activity;
     }
 
     public int getAge() {
 	return Calendar.getInstance().get(Calendar.YEAR) - yearOfBirth;
     }
 
     /**
      * Get Body Mass Index (BMI)
      * 
      * @return bmi BMI in kg/(m^2)
      */
     public double getBmi() {
 	return weight / height / height;
     }
 
     public int evaluateBmi() {
	if (getBmi() < 16.0)
	    return BMI_SEVERELY_UNDERWEIGHT;
 	else if (getBmi() < 18.5)
 	    return BMI_UNDERWEIGHT;
 	else if (getBmi() < 25.0)
 	    return BMI_NORMAL;
 	else if (getBmi() < 30.0)
 	    return BMI_OVERWEIGHT;
 	else
 	    return BMI_OBESE;
     }
 
     /* (non-Javadoc)
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
 	return "User [name=" + name + ", sex=" + sex + ", yearOfBirth=" + yearOfBirth + ", height="
 		+ height + ", weight=" + weight + ", activity=" + activity + "]";
     }
 
 }
