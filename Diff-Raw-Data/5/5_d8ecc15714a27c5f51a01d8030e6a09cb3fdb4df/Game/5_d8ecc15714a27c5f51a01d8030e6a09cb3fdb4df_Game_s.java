 package com.biofuels.fof.kosomodel;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 
 import com.biofuels.fof.kosomodel.gameStage.GameStage;
 
 
 
 
 public class Game {
 
   private final String roomName;
   private final boolean hasPassword;
   private boolean contracts=false;
   private boolean management=false;
   private final String password;
   private ConcurrentHashMap<Integer, Farm> farms;  //used because doesn't allow annoying null mappings
   private long maxPlayers;
   private RoundManager roundManager;
   private int readyFarmers;
   private int gameYear=0;
   private int fieldsPerFarm=2;
 
   //TODO have real prices
   public static final double CORNPRICE = 200;
   public static final double GRASSPRICE = 100;
   public static final double COVERPRICE = 25;
 
   /*  private class RoundManager{
 
 
     public RoundManager(boolean contracts, boolean management){
       this.contracts = contracts;
       this.management = management;
     }
   }*/
 
 
   public Game(String name, long maxPlayers) {
     roomName = name;
     farms = new ConcurrentHashMap<>();
     hasPassword = false;
     password = "";
     roundManager = new RoundManager();
     roundManager.Init(this);
     this.maxPlayers = maxPlayers;
     roundManager.AdvanceStage();
   }
 
   public Game(String name, String pass, long maxPlayers) {
     roomName = name;
     farms = new ConcurrentHashMap<>();
     hasPassword = true;
     password = pass;
     this.maxPlayers = maxPlayers;
     roundManager = new RoundManager();
     roundManager.Init(this);
     roundManager.AdvanceStage();
   }
 
   public String getRoomName() {
     return roomName;
   }
 
   public boolean hasFarmer(String name) {
 
     for(Farm f:farms.values()){
       if (f.getName().equals(name))
         return true;
     }
     return false;
   }
 
   public void addFarmer(String newPlayer, int clientID) {
     Farm f = new Farm(newPlayer, 1000, this);
     f.setClientID(clientID);
    f.getFields().add(new Field());
    f.getFields().add(new Field());
     farms.put(clientID, f);
 
   }
 
   public Boolean hasPassword(){
     return hasPassword;
   }
 
   public String getPassword(){
     return password;
   }
 
   public long getMaxPlayers() {
     return maxPlayers;
   }
 
   public boolean isContracts() {
     return contracts;
   }
 
   public void setContracts(boolean contracts) {
     this.contracts = contracts;
   }
 
   public boolean isManagement() {
     return management;
   }
 
   public void setManagement(boolean management) {
     this.management = management;
   }
 
   public boolean isFull(){
     return(farms.size() >= maxPlayers);
   }
 
   public void setField(int clientID, int field, Crop crop){
     farms.get(clientID).getFields().get(field).setCrop(crop);
   }
 
   public ArrayList<String> getFieldsFor(Integer clientID) {
     ArrayList<String> cropList = new ArrayList<>();
     for(Field f:farms.get(clientID).getFields()){
       cropList.add(f.getCrop().toString());
     }
     return cropList;
   }
 
   public ArrayList<Farm> getFarms() {
 
     return new ArrayList<>(farms.values());
   }
 
   public Farm getFarm(String name) {
     for(Farm f:farms.values()){
       if (f.getName().equals(name))
         return f;
     }
     return null;
   }
 
   public void rejoinFarmer(String farmerName, Integer clientID) {
 
     Farm farm = getFarm(farmerName);
     farms.remove(getFarm(farmerName).getClientID());
     farm.setClientID(clientID);
     farms.put(clientID, farm);
   }
 
   public Farm getFarm(Integer clientID) {
 
     return farms.get(clientID);
   }
 
   public void rerankFarms(){
     ArrayList<Double> econScores = new ArrayList<>();
     ArrayList<Double> envScores = new ArrayList<>();
     ArrayList<Double> energyScores = new ArrayList<>();
     ArrayList<Double> sustainabilityScores = new ArrayList<>();
     for(Farm f:farms.values()){
       econScores.add((double)f.getEconScore());
       envScores.add(f.getEnvScore());
       energyScores.add(f.getEnergyScore());
     }
     Collections.sort(econScores);
     Collections.sort(envScores);
     Collections.sort(energyScores);
 
     //FIXME Should not be running calculations twice, esp given concurrency issues
     for(Farm f:farms.values()){
       f.setEconRank(envScores.size() - econScores.indexOf((double)f.getEconScore()));
       f.setEnvRank(envScores.size() - envScores.indexOf(f.getEnvScore()));
       f.setEnergyRank(envScores.size() - energyScores.indexOf(f.getEnergyScore()));
       //System.out.println("ene: " + f.getEnergyScore() + "env: " + f.getEnvScore() + "econ: " + f.getEconScore());
       f.setOverallScore((f.getEnergyScore() + f.getEnvScore() + f.getEconScore()) / 3);
       sustainabilityScores.add(f.getOverallScore());
     }
 
     Collections.sort(sustainabilityScores);
 
     for(Farm f:farms.values()){
       f.setOverallRank(sustainabilityScores.size() - sustainabilityScores.indexOf(f.getOverallScore()));
     }
   }
 
   public void changeSettings(int fields, boolean contracts, boolean management) {
     int currFields = fieldsPerFarm;
     fieldsPerFarm = fields;
     if(farms.size()>0){
       currFields = ((Farm)farms.values().toArray()[0]).getFields().size();
     }
     for(Farm fa:farms.values()){
       fa.setReady(false);
     }
     resetReadyFarmers();
 
     if(fields < currFields){
       System.out.println("destroying fields not implemented yet");
     }
     else if(fields > currFields){
       for(Farm f:farms.values()){
         for(int i = 0;i<fields - currFields;i++){
           f.getFields().add(new Field());
         }
       }
     }
     this.contracts = contracts;
     this.management = management;
     roundManager.resetStages();
   }
 
   public List<Field> getFields(Integer clientID) {
     return farms.get(clientID).getFields();
   }
 
   public int getYear() {
     return gameYear;
   }
 
   public void setYear(int year){
     gameYear = year;
   }
 
   public int getStageNumber() {
     return roundManager.getCurrentStageNumber();
   }
 
   public List<String> getEnabledStages() {
     ArrayList<String> ret = new ArrayList<String>();
     List<GameStage> stages = roundManager.getEnabledStages();
     for (GameStage s:stages){
       ret.add(s.getName());
     }
     return ret;
   }
 
   public void advanceStage() {
     roundManager.AdvanceStage();
     for(Farm fa:farms.values()){
       fa.setReady(false);
     }
     if (this.getStageNumber() == 0)
       setYear(getYear()+1);
     resetReadyFarmers();
   }
 
   public boolean isFinalRound() {
     return false;
   }
 
   public String getStageName() {
     return roundManager.getCurrentStageName();
   }
 
   public int getCapitalRank(Farm farm) {
 
     return -1;
   }
 
   public void sellFarmerCrops() {
     for(Farm f:farms.values()){
       int profit = 0;
       for(Field fi:f.getFields()){
         double yield = fi.calculateYield();
 
         if(fi.getCrop().equals(Crop.CORN)){
           profit += Game.CORNPRICE * yield;
         }
         else if(fi.getCrop().equals(Crop.GRASS)){
           profit += Game.GRASSPRICE * yield;
         }
         else if(fi.getCrop().equals(Crop.COVER)){
           profit += Game.COVERPRICE * yield;
         }
 
         fi.setLastYield(yield);
       }
       f.setCapital(f.getCapital()+profit);
     }
   }
 
 
 
   public void clearFields() {
     for(Farm f:farms.values()){
       for(Field fi:f.getFields()){
         if(fi.getCrop().equals(Crop.CORN))
           fi.setCrop(Crop.FALLOW);
       }
     }
   }
 
   public void farmerReady() {
     readyFarmers++;
   }
 
   public int getReadyFarmers() {
     return readyFarmers;
   }
 
   public void resetReadyFarmers() {
     this.readyFarmers = 0;
   }
 
   public int getFieldsPerFarm() {
     return fieldsPerFarm;
   }
 
   public int getLargestEarnings() {
     // FIXME probably a better method. go away im sleepy
     int max = -1;
     for(Farm f:farms.values()){
       if(f.getCapital() > max){
         max = f.getCapital();
       }
     }
     return max;
   }
 
 
 }
