 import java.util.ArrayList;
 import java.util.List;
 
 /* Bauernhof class with unique name and an amount of Traktors */
 @AuthorClass(author="Jakub Zarzycki")
 public class Bauernhof{
   private final String name;
   private final MyMap traktoren;
 
   /* creates new Bauernhof with given name and no Traktors */
   public Bauernhof(String name){
     this.name = name;
     this.traktoren = new MyMap();
   }
 
   /* returns the name */
   @AuthorMethod(author="Julian Schrittwieser")
   public String getName() {
     return this.name;
   }
 
   /* Adds the Traktor t to the traktoren Map */
   public void addTraktor(Traktor t){
     traktoren.put(t.getID(), t);
   }
 
   /* removes the Traktor with the given id from traktoren Map */
   public void delTraktor(String id){
     traktoren.remove(id);
   }
 
   /* changes the Maschine of the Traktor with given id to the m Maschine */
   @AuthorMethod(author="Julian Schrittwieser")
   public void changeTraktor(String id, Maschine m){
     getTraktorForID(id).changeEinsatzart(m);
   }
 
   /* increments stunden of the Traktor with given id */
   @AuthorMethod(author="Julian Schrittwieser")
   public void incrStunden(String id) {
     getTraktorForID(id).incrStunden();
   }
 
   /* returns the Traktor with given id from the traktoren Map,
     if the given id references to another Object the method throws a RuntimeException */
   @AuthorMethod(author="Julian Schrittwieser")
   private Traktor getTraktorForID(Object id) {
     Object t = traktoren.get(id);
     if(t instanceof Traktor)
       return (Traktor) t;
     else
       throw new RuntimeException("invalid state - this.traktoren should only "
         + "contain values of type Traktor");
   }
 
   /* returns the average hours of all Traktors */
   @AuthorMethod(author="Lukas Macsek")
   public double getHoursComplete(){
     double sum=0;
 
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
       sum +=t.getStunden();
     }
     if(traktoren.size()==0) return 0;
     return sum/traktoren.size();
   }
 
   /* returns the average hours of Traktors with DuengerStreuer */
   @AuthorMethod(author="Lukas Macsek")
   public double getHoursDuenger(){
     double sum=0;
     int count = 0;
 
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
       if(t.getBehaelter().isDefined()) {
         count++;
         sum += t.getStunden();
       }
     }
     if(count==0) return 0;
     return sum/count;
   }
 
   /* returns the average hours of Traktors with DrillMaschine */
   @AuthorMethod(author="Lukas Macsek")
   public double getHoursDrill(){
     double sum =0;
     int count=0;
 
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
       if(t.getSaeschere().isDefined()) {
         count++;
         sum += t.getStunden();
       }
     }
     if(count==0) return 0;
     return sum/count;
   }
 
   /* returns the average hours of DieselTraktors */
   @AuthorMethod(author="Lukas Macsek")
   public double getHoursDiesel(){
     double sum =0;
     int count=0;
 
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
       if(t instanceof BiogasTraktor) {
         count++;
         sum += t.getStunden();
       }
     }
     if(count==0) return 0;
     return sum/count;
   }
 
   /* returns the average hours of BiogasTraktors */
   @AuthorMethod(author="Lukas Macsek")
   public double getHoursBiogas(){
     double sum =0;
     int count=0;
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
       if(t instanceof DieselTraktor) {
         count++;
         sum += t.getStunden();
       }
     }
     if(count==0) return 0;
     return sum/count;
   }
 
   /* returns the average Verbrauch of all DieselTraktors with Drillmaschine */
   @AuthorMethod(author="Lukas Macsek")
   public double getDieselDrill(){
     double sum =0;
     int count=0;
 
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
       if(t instanceof DieselTraktor) {
         if(t.getSaeschere().isDefined()) {
           count++;
           sum += ((DieselTraktor) t).getVerbrauch();
         }
       }
     }
     if(count==0) return 0;
     return sum/count;
   }
 
   /* returns the average Verbrauch of all DieselTraktors with DuengerStreuer */
   @AuthorMethod(author="Lukas Macsek")
   public double getDieselDuenger(){
     double sum =0;
     int count=0;
 
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
       if(t instanceof DieselTraktor) {
         if(t.getBehaelter().isDefined()) {
           count++;
           sum += ((DieselTraktor) t).getVerbrauch();
         }
       }
     }
     if(count==0) return 0;
     return sum/count;
   }
 
   /* returns the average Verbrauch of all DieselTraktors */
   @AuthorMethod(author="Lukas Macsek")
    public double getDiesel(){
     double sum =0;
     int count=0;
 
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
       if(t instanceof DieselTraktor) {
         count++;
         sum += ((DieselTraktor) t).getVerbrauch();
       }
     }
     if(count==0) return 0;
     return sum/count;
   }
 
   /* returns the average Verbrauch of all BiogasTraktors with Drillmaschine */
   @AuthorMethod(author="Lukas Macsek")
   public double getGasDrill(){
     double sum =0;
     int count=0;
 
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
 
       if(t instanceof BiogasTraktor) {
         if(t.getSaeschere().isDefined()) {
           count++;
           sum += ((BiogasTraktor) t).getVerbrauch();
         }
       }
     }
     if(count==0) return 0;
     return sum/count;
   }
 
   /* returns the average Verbrauch of all BiogasTraktors with DuengerStreuer */
   @AuthorMethod(author="Lukas Macsek")
   public double getGasDuenger(){
     double sum =0;
     int count=0;
 
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
 
       if(t instanceof BiogasTraktor) {
         if(t.getBehaelter().isDefined()) {
           count++;
           sum += ((BiogasTraktor) t).getVerbrauch();
         }
       }
     }
     if(count==0) return 0;
     return sum/count;
   }
 
   /* returns the average Verbrauch of all BiogasTraktors */
   @AuthorMethod(author="Lukas Macsek")
    public double getGas(){
     double sum =0;
     int count=0;
 
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
 
       if(t instanceof BiogasTraktor) {
         count++;
         sum += ((BiogasTraktor) t).getVerbrauch();
       }
     }
     if(count==0) return 0;
     return sum/count;
   }
 
   /* returns the max amount of saeschere of all Traktors */
   @AuthorMethod(author="Lukas Macsek")
   public int getMaxSaeschere(){
     int max=0;
     int temp=0;
 
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
       if(t.getSaeschere().isDefined()){
         temp=t.getSaeschere().get().intValue();
         if (temp>max)
           max=temp;
       }
     }
     return max;
   }
 
   /* returns the min amount of saeschere of all Traktors */
   @AuthorMethod(author="Lukas Macsek")
   public int getMinSaeschere(){
     int min=Integer.MAX_VALUE;;
     int temp=0;
 
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
       if(t.getSaeschere().isDefined()){
         temp=t.getSaeschere().get().intValue();
         if (temp<min)
           min=temp;
       }
     }
    return min;
   }
 
   /* returns the max amount of saeschere of BiogasTraktors */
   @AuthorMethod(author="Lukas Macsek")
   public int getMaxSaeschereGas(){
     int max=0;
     int temp=0;
 
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
       if(t instanceof BiogasTraktor){
         if(t.getSaeschere().isDefined()){
           temp=t.getSaeschere().get().intValue();
           if (temp>max)
             max=temp;
         }
       }
     }
     return max;
   }
   
   /* returns the max amount of saeschere of DieselTraktors */
   @AuthorMethod(author="Lukas Macsek")
   public int getMaxSaeschereDiesel(){
     int max=0;
     int temp=0;
 
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
       if(t instanceof DieselTraktor){
         if(t.getSaeschere().isDefined()){
           temp=t.getSaeschere().get().intValue();
           if (temp>max)
             max=temp;
         }
       }
     }
     return max;
   }
   
   /* returns the min amount of saeschere of BiogasTraktors */
   @AuthorMethod(author="Lukas Macsek")
   public int getMinSaeschereGas(){
     int min=Integer.MAX_VALUE;
     int temp=0;
 
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
       if(t instanceof BiogasTraktor){
         if(t.getSaeschere().isDefined()){
           temp=t.getSaeschere().get().intValue();
           if (temp<min)
             min=temp;
         }
       }
     }
 
    return min;
   }
   
   /* returns the min amount of saeschere of DieselTraktors */
   @AuthorMethod(author="Lukas Macsek")
   public int getMinSaeschereDiesel(){
     int min=Integer.MAX_VALUE;
     int temp=0;
 
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
       if(t instanceof BiogasTraktor){
         if(t.getSaeschere().isDefined()){
           temp=t.getSaeschere().get().intValue();
           if (temp<min)
             min=temp;
         }
       }
     }
 
    return min;
   }
 
   /* returns the average behaelter value of all DieselTraktors */
   @AuthorMethod(author="Lukas Macsek")
   public double getCapacityDiesel(){
     double sum=0;
     int count=0;
 
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
       if(t instanceof DieselTraktor){
         if(t.getBehaelter().isDefined()){
           sum+= t.getBehaelter().get().intValue();
           count++;
         }
       }
     }
     if(count==0) return 0;
     return sum/count;
   }
 
   /* returns the average behaelter value of all BiogasTraktors */
   @AuthorMethod(author="Lukas Macsek")
   public double getCapacityGas(){
     double sum=0;
     int count=0;
 
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
       if(t instanceof BiogasTraktor){
         if(t.getBehaelter().isDefined()){
           sum+= t.getBehaelter().get().intValue();
           count++;
         }
       }
     }
     if(count==0) return 0;
     return sum/count;
   }
 
   /* returns the average behaelter value of all Traktors */
   @AuthorMethod(author="Lukas Macsek")
   public double getCapacity(){
     double sum=0;
     int count=0;
 
     for(Object id : traktoren) {
       Traktor t = getTraktorForID(id);
       if(t.getBehaelter().isDefined()){
         sum+= t.getBehaelter().get().intValue();
         count++;
       }
     }
     if(count==0) return 0;
     return sum/count;
   }
 }
