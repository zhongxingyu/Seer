 package View;
 
 import Controller.AnimationEngine;
 import Enums.Weather;
 import Model.WeatherAPI;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 import javax.swing.border.BevelBorder;
 
 /**
  * Not done:
  * 
  * Currently only works with todays date, obv need to get specific date from
  * calendar selection
  * 
  * All the animations should later be put into the animation engine
  * 
  * Animations arent done yet
  * 
  * @author Kristian
  */
 public class DayCard extends JPanel{
     
     private int pic = 0;
     private WeatherAPI weather;
     private ArrayList<AnimationEngine> engines;
     private Weather current;
     private GregorianCalendar date;
     
     /**
      * Default constructor, created with todays date as date.
      */
     public DayCard(){
         this(new GregorianCalendar());
     }
     
     /**
      * Constructor with one parameter, create the day card corresponding to
      * a specific date.
      * @param cal 
      */
     public DayCard(GregorianCalendar cal){
         //Initialize all variables
         this.date = cal;
         weather = new WeatherAPI();
         weather.setWeather(new GregorianCalendar());
         setPreferredSize(new Dimension(300,400));
         this.setBorder(new BevelBorder(BevelBorder.RAISED));
         this.engines = new ArrayList();
         
         //attempt to get weather information
         try {
             this.current = weather.getWeather(this.date);
         } catch (MalformedURLException ex) {
             Logger.getLogger(DayCard.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(DayCard.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         setTemperature();
         
         //Adds and starts the animation engines
         addEngine(new AnimationEngine(this));
         startEngines();
     }
     
     
     /**
      * Function that iterates over the animation engines in the list
      * and starts these. Each engine runs as its own thread.
      */
     private void startEngines(){
         Iterator iterator = this.engines.iterator();
         AnimationEngine temp;
         Thread thread;
         
         while(iterator.hasNext()){
             temp = (AnimationEngine)iterator.next();
             thread = new Thread(temp);
             thread.start();            
         }
     }
     
     /**
      * Overriden paintcomponent function, when called the animations are updated
      * @param g 
      */
     @Override
    public void paintComponent(Graphics g){        
         Graphics temp = g.create();      
                 
        temp.setColor(Color.white);
         
         temp.drawString("Temperature in Uppsala: " + String.valueOf(this.weather.getAvgTemp()) + "C", 30, 30);
         
         //Draw animation according to current weather.
         if(this.current == Weather.CLOUDY){
             paintCloud(temp);
         }
         else if(this.current == Weather.RAINY){
         
             paintRain(temp,pic);
         }
         else if(this.current == Weather.SUNNY){
             try {
                 paintSun(temp,pic);
             } catch (IOException ex) {
                 Logger.getLogger(DayCard.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         
         temp.dispose();
     }
 
     /**
      * Paints sun representing sunny weather.
      * @param g Graphics object to be used
      * @param i Which of the image to be used
      * @throws IOException 
      */
     public void paintSun(Graphics g, int i) throws IOException {
         BufferedImage img = null;
         
         try{
             if(i==0){
                 //img = ImageIO(this.getClass().getResource("addtask.jpg"));
                 img = ImageIO.read(this.getClass().getResource("sun.jpg"));
             }
         else if(i==1){
             img = ImageIO.read(this.getClass().getResource("sun2.jpg"));
         }
         
         }
         catch(IOException e){
             System.out.println(e.toString());
         }
         
         g.drawImage(img,250,20,30,30,null);
         
         //Update choice of images (switching between 0 and 1, animation shifts 
         //between two pictures to create a shining sun
         pic = (pic-1)*(pic-1);
     }
 
     public void paintCloud(Graphics g) {
         BufferedImage img = null;
         
         try{
             img = ImageIO.read(this.getClass().getResource("clody.png"));
         }
         catch(IOException e){
             System.out.println(e.toString());
         }
         
         g.drawImage(img,250,20,30,30,null);
     }
     
     /**
      * Function that draw a rain animation
      * @param g Graphics object to be used.
      * @param i i is the number used to choose between pictures to draw
      */
     public void paintRain(Graphics g, int i){
         BufferedImage img = null;
         
         try{
         if(i==0){
             img = ImageIO.read(this.getClass().getResource("rainpng.png"));
         }
         else if(i==1){
             img = ImageIO.read(this.getClass().getResource("rainpng2.png"));
         }
         else if(i==2){
             img = ImageIO.read(this.getClass().getResource("rainpng3.png"));
         }
         
         }
         catch(IOException e){
             System.out.println(e.toString());
         }
         
         g.drawImage(img,250,20,30,30,null);
         
         //Update choice of images (switching between 0,1 and 2, animation shifts 
         //between three pictures to create a rain animation
         if(pic == 2){
             pic = 0;
         }
         else{
             pic += 1;
         }
     }
     
     /**
      * Function that draw a snow animation
      * @param g Graphics object to be used.
      */
     public void paintSnow(Graphics g){
         BufferedImage img = null;
         try {
             img = ImageIO.read(this.getClass().getResource("snow.jpg"));
         }       
         catch (IOException e) {
         }
         g.drawImage(img,280, pic, 30, 30,null);
     }
     
     /**
      * Adds an animationengine to the list of engines
      * @param eng 
      */
     private void addEngine(AnimationEngine eng){
         this.engines.add(eng);
     }
     
     /**
      * Function that updates the current weather
      */
     /**********************************************************
      ********* Getters and setters ****************************
      **********************************************************/
     
     public void setWeather(){
         try {
             this.current = this.weather.getWeather(date);
         } catch (MalformedURLException ex) {
             Logger.getLogger(DayCard.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(DayCard.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public void setDate(GregorianCalendar cal){
         this.date = cal;
     }
     
     public GregorianCalendar getDate(GregorianCalendar cal){
         return this.date;
         
     }
 
     private void setTemperature() {
         try {
             this.weather.setAvgTemp(date);
         } catch (IOException ex) {
             Logger.getLogger(DayCard.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
