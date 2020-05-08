 package org.powertac.tourney.actions;
 
 import org.apache.myfaces.custom.fileupload.UploadedFile;
 import org.powertac.tourney.beans.*;
 import org.powertac.tourney.services.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 import javax.faces.context.FacesContext;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 @Component("actionAdmin")
 @Scope("request")
 public class ActionAdmin
 {
   @Autowired
   private Upload upload;
 
   @Autowired
   private Scheduler scheduler;
 
   private String sortColumn = null;
   private boolean sortAscending = true;
   private int rowCount = 5;
 
   private String sortColumnMachine = null;
   private boolean sortAscendingMachine = true;
   private int rowCountMachine = 5;
 
   private String sortColumnUsers = null;
   private boolean sortAscendingUsers = true;
   private int rowCountUsers = 5;
 
   private String sortColumnTournaments = null;
   private boolean sortAscendingTournaments = true;
   private int rowCountTournaments = 5;
 
   private String sortColumnGames = null;
   private boolean sortAscendingGames = true;
   private int rowCountGames = 5;
 
   private String newLocationName = "";
   private Date newLocationStartTime = null;
   private Date newLocationEndTime = null;
 
   private int machineId = -1;
 
   private String newName = "";
   private String newUrl = "";
   private String newViz = "";
   private String newQueue = "";
 
   private UploadedFile pom;
   private String pomName;
   private TournamentProperties properties = new TournamentProperties();
 
   private String message = "";
 
   public ActionAdmin ()
   {
   }
   
   public List<Game> getGameList()
   {
     List<Game> games = new ArrayList<Game>();
     
     Database db = new Database();
     try{
       db.startTrans();
       games = db.getGames();
       db.commitTrans();
     }catch (SQLException e){
       db.abortTrans();
       e.printStackTrace();
     }
 
     return games;
   }
   
   public List<User> getUserList()
   {
     List<User> users = new ArrayList<User>();
     
     Database db = new Database();
     try {
       db.startTrans();
       users = db.getAllUsers();
       db.commitTrans();
     }
     catch (SQLException e){
       db.abortTrans();
       e.printStackTrace();
     }
 
     return users;
   }
 
   public String getPomName ()
   {
     return pomName;
   }
 
   public void setPomName (String pomName)
   {
     this.pomName = pomName;
   }
 
   public UploadedFile getPom ()
   {
     return pom;
   }
 
   public void setPom (UploadedFile pom)
   {
     this.pom = pom;
   }
 
   public void submitPom ()
   {
     if (pom == null) {
       System.out.println("Pom was null");
       return;
     }
 
     System.out.println("Pom is not null");
     User currentUser =
         (User) FacesContext.getCurrentInstance().getExternalContext()
             .getSessionMap().get(User.getKey());
 
    Database db = new Database();
    try {
      db.startTrans();
      int id = db.addPom(currentUser.getUsername(), this.getPomName());
 
      TournamentProperties properties = new TournamentProperties();
       upload.setUploadedFile(pom);
       upload.setUploadLocation(properties.getProperty("pomLocation"));
      upload.submit("pom." + id + ".xml");

      db.commitTrans();
    }
    catch (SQLException e) {
      db.abortTrans();
      e.printStackTrace();
    }
   }
 
   public List<Database.Pom> getPomList ()
   {
     List<Database.Pom> poms = new ArrayList<Database.Pom>();
 
     Database db = new Database();
 
     try {
       db.startTrans();
       poms = db.getPoms();
       db.commitTrans();
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
     }
 
     return poms;
   }
 
   public List<Tournament> getTournamentList(){
     List<Tournament> ts = new ArrayList<Tournament>();
     
     Database db = new Database();
     try{
       db.startTrans();
       ts = db.getTournaments("pending");
       ts.addAll(db.getTournaments("in-progress"));
       db.commitTrans();
     
     }catch(Exception e){
       db.abortTrans();
     }
 
     return ts;
   }
   
   public List<Location> getLocationList ()
   {
     List<Location> locations = new ArrayList<Location>();
     Database db = new Database();
 
     try {
       db.startTrans();
       locations = db.getLocations();
       db.commitTrans();
 
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
     }
 
     return locations;
   }
 
   public List<Broker> getRegistered (int tourneyId)
   {
     List<Broker> registered = new ArrayList<Broker>();
     Database db = new Database();
 
     try {
       db.startTrans();
       registered = db.getBrokersInTournament(tourneyId);
       db.commitTrans();
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
     }
 
     return registered;
   }
 
   public List<Machine> getMachineList ()
   {
     List<Machine> machines = new ArrayList<Machine>();
     Database db = new Database();
 
     try {
       db.startTrans();
       machines = db.getMachines();
       db.commitTrans();
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
     }
 
     return machines;
   }
 
   public void toggleAvailable (Machine m)
   {
     Database db = new Database();
 
     try {
       db.startTrans();
       if (m.isAvailable()) {
         db.setMachineAvailable(m.getMachineId(), false);
       }
       else {
         db.setMachineAvailable(m.getMachineId(), true);
       }
       db.commitTrans();
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
     }
   }
   public void toggleStatus(Machine m){
     Database db = new Database();
     
     try{
       db.startTrans();
       if(m.isInProgress()){
         db.setMachineStatus(m.getMachineId(), "idle");
       }else{
         db.setMachineStatus(m.getMachineId(), "running");
       }
       db.commitTrans();
     }catch(Exception e){
       db.abortTrans();
       e.printStackTrace();
     }
   }
   
   public void editMachine(Machine m)
   {
     machineId = m.getMachineId();
     newName = m.getName();
     newUrl = m.getUrl();
     newViz = m.getVizUrl();
     newQueue = m.getVizQueue();
   }
   
   public void saveMachine()
   {
     newUrl = newUrl.replace("https://", "").replace("http://", "");
     newViz = newViz.replace("https://", "").replace("http://", "");
 
     if (newName.isEmpty() || newUrl.isEmpty() || newViz.isEmpty() || newQueue.isEmpty()) {
       System.out.println("Some machine fields are empty!");
       message = "Error : machine not saved, some fields were empty!";
   	  return;
   	}  
 	  
     // It's a new machine
     if (machineId == -1) {
       addMachine();
       return;
     }
 	  
     Database db = new Database();
     try {
       db.startTrans();
       db.editMachine(newName, newUrl, newViz, newQueue, machineId);
       db.commitTrans();
       resetMachineData();
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
       message = "Error : machine not edited " + e.getMessage();
     }
   }
 
   public void deleteMachine (Machine m)
   {
     Database db = new Database();
     try {
       db.startTrans();
       db.deleteMachine(m.getMachineId());
       db.commitTrans();
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
       message = "Error : machine not added " + e.getMessage();
     }
   }
 
   public void addMachine ()
   {
     Database db = new Database();
     try {
       db.startTrans();
       db.addMachine(newName, newUrl, newViz, newQueue);
       db.commitTrans();
       
       resetMachineData();
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
       message = "Error : machine not added " + e.getMessage();
     }
   }
 
   public void restartGame (Game g)
   {
     Database db = new Database();
     int gameId = g.getGameId();
     System.out.println("[INFO] Restarting Game " + gameId + " has status: "
                        + g.getStatus());
     Tournament t = new Tournament();
     try {
       db.startTrans();
       t = db.getTournamentByGameId(gameId);
 
       db.setMachineStatus(g.getMachineId(), "idle");
       System.out.println("[INFO] Setting machine: " + g.getMachineId()
                          + " to idle");
       db.commitTrans();
 
     }
     catch (SQLException e) {
       db.abortTrans();
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
 
     if (g.getStatus().equalsIgnoreCase("boot-failed") ||
         g.getStatus().equalsIgnoreCase("boot-pending") ||
         g.getStatus().equalsIgnoreCase("boot-in-progress") ) {
       System.out.println("[INFO] Attempting to restart bootstrap " + gameId);
       scheduler.deleteBootTimer(gameId);
 
       RunBootstrap runBootstrap = new RunBootstrap(gameId, t.getPomId());
       scheduler.runBootTimer(gameId, runBootstrap, new Date());
     }
     else if (g.getStatus().equalsIgnoreCase("game-failed") ||
              g.getStatus().equalsIgnoreCase("game-pending") ||
             g.getStatus().equalsIgnoreCase("boot-complete") ) {
       System.out.println("[INFO] Attempting to restart sim " + gameId);
       scheduler.deleteSimTimer(gameId);
 
       RunGame runGame = new RunGame(gameId, t.getPomId());
       scheduler.runSimTimer(gameId, runGame, new Date());
     }
   }
 
   public void deleteGame (Game g)
   {
     // TODO: ARE YOU SURE?
     scheduler.deleteBootTimer(g.getGameId());
     scheduler.deleteSimTimer(g.getGameId());
   }
 
   public void refresh ()
   {
 
   }
 
   public void deleteLocation (Location l)
   {
     Database db = new Database();
     try {
       db.startTrans();
       db.deleteLocation(l.getLocationId());
       db.commitTrans();
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
     }
   }
 
   public void addLocation ()
   {
     if (newLocationName.isEmpty() || (newLocationStartTime == null) || (newLocationEndTime == null)) {
       System.out.println("Some location fields are empty!");
       return;
     }
 	  
     Database db = new Database();
     try {
       db.startTrans();
       db.addLocation(newLocationName, newLocationStartTime, newLocationEndTime);
       db.commitTrans();
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
     }
   }
 
   private void resetMachineData() {
     machineId = -1;
     newName = "";
     newUrl = "";
     newViz = "";
     newQueue = "";
   }
 
 
   //<editor-fold desc="Setters and Getters">
   public int getRowCount ()
   {
     return rowCount;
   }
   public void setRowCount (int rowCount)
   {
     this.rowCount = rowCount;
   }
 
   public boolean isSortAscending ()
   {
     return sortAscending;
   }
   public void setSortAscending (boolean sortAscending)
   {
     this.sortAscending = sortAscending;
   }
 
   public String getSortColumn ()
   {
     return sortColumn;
   }
   public void setSortColumn (String sortColumn)
   {
     this.sortColumn = sortColumn;
   }
 
   public String getNewLocationName ()
   {
     return newLocationName;
   }
   public void setNewLocationName (String newLocationName)
   {
     this.newLocationName = newLocationName;
   }
 
   public Date getNewLocationStartTime ()
   {
     return newLocationStartTime;
   }
   public void setNewLocationStartTime (Date newLocationStartTime)
   {
     this.newLocationStartTime = newLocationStartTime;
   }
 
   public Date getNewLocationEndTime ()
   {
     return newLocationEndTime;
   }
   public void setNewLocationEndTime (Date newLocationEndTime)
   {
     this.newLocationEndTime = newLocationEndTime;
   }
 
   public String getSortColumnMachine ()
   {
     return sortColumnMachine;
   }
   public void setSortColumnMachine (String sortColumnMachine)
   {
     this.sortColumnMachine = sortColumnMachine;
   }
 
   public boolean isSortAscendingMachine ()
   {
     return sortAscendingMachine;
   }
   public void setSortAscendingMachine (boolean sortAscendingMachine)
   {
     this.sortAscendingMachine = sortAscendingMachine;
   }
 
   public int getRowCountMachine ()
   {
     return rowCountMachine;
   }
   public void setRowCountMachine (int rowCountMachine)
   {
     this.rowCountMachine = rowCountMachine;
   }
 
   public String getNewName ()
   {
     return newName;
   }
   public void setNewName (String newName)
   {
     this.newName = newName;
   }
 
   public String getNewUrl ()
   {
     return newUrl;
   }
   public void setNewUrl (String newUrl)
   {
     this.newUrl = newUrl;
   }
 
   public String getNewViz ()
   {
     return newViz;
   }
   public void setNewViz (String newViz)
   {
     this.newViz = newViz;
   }
 
   public String getNewQueue ()
   {
     return newQueue;
   }
   public void setNewQueue (String newQueue)
   {
     this.newQueue = newQueue;
   }
 
   public int getMachineId ()
   {
 	  return machineId;
   }
   public void setMachineId(int machineId) {
 	  this.machineId = machineId;
   }
 
   public String getMessage() {
 	  return message;
   }
   
   public String getSortColumnUsers ()
   {
     return sortColumnUsers;
   }
   public void setSortColumnUsers (String sortColumnUsers)
   {
     this.sortColumnUsers = sortColumnUsers;
   }
 
   public boolean isSortAscendingUsers ()
   {
     return sortAscendingUsers;
   }
   public void setSortAscendingUsers (boolean sortAscendingUsers)
   {
     this.sortAscendingUsers = sortAscendingUsers;
   }
 
   public int getRowCountUsers ()
   {
     return rowCountUsers;
   }
   public void setRowCountUsers (int rowCountUsers)
   {
     this.rowCountUsers = rowCountUsers;
   }
 
   public String getSortColumnTournaments ()
   {
     return sortColumnTournaments;
   }
   public void setSortColumnTournaments (String sortColumnTournaments)
   {
     this.sortColumnTournaments = sortColumnTournaments;
   }
 
   public boolean isSortAscendingTournaments ()
   {
     return sortAscendingTournaments;
   }
   public void setSortAscendingTournaments (boolean sortAscendingTournaments)
   {
     this.sortAscendingTournaments = sortAscendingTournaments;
   }
 
   public int getRowCountTournaments ()
   {
     return rowCountTournaments;
   }
   public void setRowCountTournaments (int rowCountTournaments)
   {
     this.rowCountTournaments = rowCountTournaments;
   }
 
   public String getSortColumnGames ()
   {
     return sortColumnGames;
   }
   public void setSortColumnGames (String sortColumnGames)
   {
     this.sortColumnGames = sortColumnGames;
   }
 
   public boolean isSortAscendingGames ()
   {
     return sortAscendingGames;
   }
   public void setSortAscendingGames (boolean sortAscendingGames)
   {
     this.sortAscendingGames = sortAscendingGames;
   }
 
   public int getRowCountGames ()
   {
     return rowCountGames;
   }
   public void setRowCountGames (int rowCountGames)
   {
     this.rowCountGames = rowCountGames;
   }
   //</editor-fold>
 }
