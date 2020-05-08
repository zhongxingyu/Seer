 package beans;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import tools.GenericConnection;
 import tools.ParseStuffbox;
 
 /**
  *
  * @author xbmc
  */
 public class KolMafiaBean implements Serializable{
     private String name;
     private String password;
     private String manualAdventure;
     
     private Boolean showCharacter;
     private Boolean showMap;
     private Boolean showAdventure;
     private Boolean showGraphicalCLI;
     private Boolean showPurchases;
     private Boolean showGearChanger;
     private Boolean showConsumables;
     private Boolean showSkillCasting;
             
     private GenericConnection connection;
     private List<String> logs;
             
     public KolMafiaBean(String name, String password){
         this.name=name;
         this.password=password;
         this.logs=new ArrayList();
         this.connection = new GenericConnection(this);
         
         this.showCharacter = false;
         this.showAdventure = false;
         this.showGraphicalCLI = false;
         this.showPurchases = false;
         this.showGearChanger = false;
         this.showConsumables = false;
         this.showSkillCasting = false;
     }
     
     public KolMafiaBean(){
 
     }
     
     public String getName(){
         return name;
     }
     
     public String getPassword(){
         return password;
     }
     
     public String getManualAdventure(){
         if(manualAdventure==null || manualAdventure.isEmpty()){
             manualAdventure=getConnection().get("main.php");
         }
         if(manualAdventure!=null && manualAdventure.contains("<body") && manualAdventure.contains("body>")){
             String processed = manualAdventure.split("<body")[1].split("body>")[0];
             if(processed.contains("<script>") && processed.contains("</script>")){
                 Integer start = processed.indexOf("<script>");
                 Integer stop = processed.lastIndexOf("</script>");
                 processed = processed.substring(0, start)+processed.subSequence(stop+9, processed.length());
             }
             return this.manualAdventure="<div"+processed+"div>";
         } else{
             return manualAdventure;
         }
     }
     
     public String getAutomaticAdventure(){
         return AdventureBean.getAutomaticAdventures();
     }
     
     public GenericConnection getConnection(){
         return connection;
     }
     
     public String getLog(){
         StringBuilder sb = new StringBuilder();
         for(String log : getLogs()){
             if(log.length()>35){
                sb.append(log.subSequence(0, 33)).append("...<br/>");
             } else{
                sb.append(log).append("<br/>");
             }
         }
        return "<pre><center>"+sb.toString()+"</center></pre>";
     }
     
     public void setManualAdventure(String manualAdventure){
         this.manualAdventure=manualAdventure;
     }
     
     public String getCharacterPane(){
         String characterpane = getConnection().get("charpane.php");
         Integer start = characterpane.indexOf("<center>");
         Integer end = characterpane.lastIndexOf("</center>");
         return characterpane.substring(start, end).replace("target=mainpane", "");
     }
     
     public String getGearChanger(){
         String result = getConnection().get("inventory.php?which=2");
         return ParseStuffbox.process(result);
     }
     
     public String getConsumables(){
         String result = getConnection().get("inventory.php?which=1");
         return ParseStuffbox.process(result);
     }
     
     public String getSkillCasting(){
         String skillCasting = getConnection().get("skills.php");
         Integer start = skillCasting.indexOf("<center>");
         Integer end = skillCasting.lastIndexOf("</center>");
         skillCasting=skillCasting.substring(start, end);
         return skillCasting;
     }
     
     public String getPurchases(){
         return getConnection().get("mall.php");
     }
     
     public void addToLogs(String log){
         log=log.replaceAll("<br/>", "<br>");
         String[] logLines=log.split("<br>");
         for(String logLine : logLines){
             getLogs().add(logLine);
         }
     }
 
     /**
      * @param name the name to set
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * @param password the password to set
      */
     public void setPassword(String password) {
         this.password = password;
     }
 
     /**
      * @return the showCharacter
      */
     public Boolean getShowCharacter() {
         return showCharacter;
     }
 
     /**
      * @param showCharacter the showCharacter to set
      */
     public void setShowCharacter(Boolean showCharacter) {
         this.showCharacter = showCharacter;
     }
 
     /**
      * @return the showAdventure
      */
     public Boolean getShowAdventure() {
         return showAdventure;
     }
 
     /**
      * @param showAdventure the showAdventure to set
      */
     public void setShowAdventure(Boolean showAdventure) {
         this.showAdventure = showAdventure;
     }
 
     /**
      * @return the showGraphicalCLI
      */
     public Boolean getShowGraphicalCLI() {
         return showGraphicalCLI;
     }
 
     /**
      * @param showGraphicalCLI the showGraphicalCLI to set
      */
     public void setShowGraphicalCLI(Boolean showGraphicalCLI) {
         this.showGraphicalCLI = showGraphicalCLI;
     }
 
     /**
      * @return the showPurchases
      */
     public Boolean getShowPurchases() {
         return showPurchases;
     }
 
     /**
      * @param showPurchases the showPurchases to set
      */
     public void setShowPurchases(Boolean showPurchases) {
         this.showPurchases = showPurchases;
     }
 
     /**
      * @return the showGearChanger
      */
     public Boolean getShowGearChanger() {
         return showGearChanger;
     }
 
     /**
      * @param showGearChanger the showGearChanger to set
      */
     public void setShowGearChanger(Boolean showGearChanger) {
         this.showGearChanger = showGearChanger;
     }
 
     /**
      * @return the showSkillCasting
      */
     public Boolean getShowSkillCasting() {
         return showSkillCasting;
     }
 
     /**
      * @param showSkillCasting the showSkillCasting to set
      */
     public void setShowSkillCasting(Boolean showSkillCasting) {
         this.showSkillCasting = showSkillCasting;
     }
 
     /**
      * @param connection the connection to set
      */
     public void setConnection(GenericConnection connection) {
         this.connection = connection;
     }
 
     /**
      * @return the logs
      */
     public List<String> getLogs() {
         return logs;
     }
 
     /**
      * @param logs the logs to set
      */
     public void setLogs(List<String> logs) {
         this.logs = logs;
     }
 
     /**
      * @return the showConsumables
      */
     public Boolean getShowConsumables() {
         return showConsumables;
     }
 
     /**
      * @param showConsumables the showConsumables to set
      */
     public void setShowConsumables(Boolean showConsumables) {
         this.showConsumables = showConsumables;
     }
 
     /**
      * @return the showMap
      */
     public Boolean getShowMap() {
         return showMap;
     }
 
     /**
      * @param showMap the showMap to set
      */
     public void setShowMap(Boolean showMap) {
         this.showMap = showMap;
     }
 }
