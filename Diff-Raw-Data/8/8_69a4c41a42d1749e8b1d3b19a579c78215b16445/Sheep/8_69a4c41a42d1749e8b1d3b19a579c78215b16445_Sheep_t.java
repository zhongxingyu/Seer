 package model;
 
 import util.Vec2;
 
 public class Sheep
 {
     public final static String SEX_MALE = "m";
     public final static String SEX_FEMALE = "f";
 
     public static int pulseMax = 150;
     public static int pulseMin = 50;
 
     int id;
     int farmerid;
     //int age;
     int mileage;
     int healthflags;
     long birthdate;
     int pulse;
     String sex;
     String name;
     Vec2 pos;
 
 
     SheepHistory posHistory;
     SheepMedicalHistory medicalHistory;
 
     public long getBirthdate() {
         return birthdate;
     }
 
     public void setBirthdate(long birthdate) {
         this.birthdate = birthdate;
     }
 
     public SheepMedicalHistory getMedicalHistory() {
         return medicalHistory;
     }
 
     public void setMedicalHistory(SheepMedicalHistory medicalHistory) {
         this.medicalHistory = medicalHistory;
     }
 
     public SheepHistory getPosHistory() {
         return posHistory;
     }
 
     public void setPosHistory(SheepHistory posHistory) {
         this.posHistory = posHistory;
     }
 
     public Sheep(int id, long birthdate, int healthflags, int mileage, int farmerid, String name, String sex) {
         //this.age = age;
         this.id = id;
         this.mileage = mileage;
         this.farmerid = farmerid;
         this.healthflags = healthflags;
         this.birthdate = birthdate;
         this.name = name;
         this.pos = new Vec2(0,0);
 
         if(sex.equals(SEX_MALE) || sex.equals(SEX_FEMALE)){
             this.sex = sex;
         }else{
             this.sex = "f";
         }
         this.pulse = -1;
     }
 
     public Sheep(int id, long birthdate, int healthflags, int mileage, int farmerid, int pulse, String name, String sex) {
         //this.age = age;
         this.id = id;
         this.mileage = mileage;
         this.farmerid = farmerid;
         this.healthflags = healthflags;
         this.birthdate = birthdate;
         this.name = name;
         this.pulse = pulse;
        this.pos = new Vec2(0.0d, 0.0d);
 
         if(sex == SEX_MALE || sex == SEX_FEMALE){
             this.sex = sex;
         }else{
             this.sex = "f";
         }
     }
 
    public Sheep(int id, long birthdate, int healthflags, int mileage, int farmerid, double pos_x, double pos_y, int pulse, String name, String sex) {
         //this.age = age;
         this.id = id;
         this.mileage = mileage;
         this.farmerid = farmerid;
         this.healthflags = healthflags;
         this.birthdate = birthdate;
         this.name = name;
         this.pulse = pulse;
         this.pos = new Vec2(pos_x, pos_y);
 
         if(sex == SEX_MALE || sex == SEX_FEMALE){
             this.sex = sex;
         }else{
             this.sex = "f";
         }
     }
 
    public Sheep(int id, long birthdate, int healthflags, int mileage, int farmerid, double pos_x, double pos_y, String name, String sex) {
         //this.age = age;
         this.id = id;
         this.mileage = mileage;
         this.farmerid = farmerid;
         this.healthflags = healthflags;
         this.birthdate = birthdate;
         this.name = name;
         this.pulse = 0;
         this.pos = new Vec2(pos_x, pos_y);
 
         if(sex == SEX_MALE || sex == SEX_FEMALE){
             this.sex = sex;
         }else{
             this.sex = "f";
         }
     }
 
 
     public Vec2 getPos() {
         return pos;
     }
 
     public void setPos(Vec2 pos) {
         this.pos = pos;
     }
 
     public int getPulse() {
         return pulse;
     }
 
     public void setPulse(int pulse) {
         this.pulse = pulse;
     }
 
     public boolean hasFlag(int flag){
         return (healthflags & flag) > 0;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public int getHealthflags() {
         return healthflags;
     }
 
     public void setHealthflags(int healthflags) {
         this.healthflags = healthflags;
     }
 
     /*public int getAge() {
         return age;
     }
 
     public void setAge(int age) {
         this.age = age;
     }*/
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public int getMileage() {
         return mileage;
     }
 
     public void setMileage(int mileage) {
         this.mileage = mileage;
     }
 
     public int getFarmerid() {
         return farmerid;
     }
 
     public void setFarmerid(int farmerid) {
         this.farmerid = farmerid;
     }
 
     public String getSex() {
         return sex;
     }
 
     public void setSex(String sex) {
         this.sex = sex;
     }
 }
