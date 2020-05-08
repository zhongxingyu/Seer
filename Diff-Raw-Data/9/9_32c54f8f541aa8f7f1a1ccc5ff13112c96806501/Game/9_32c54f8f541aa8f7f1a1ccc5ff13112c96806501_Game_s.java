 package fap_java;
 
 import characters.*;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 
 import org.xml.sax.helpers.DefaultHandler;
 
 public class Game extends JPanel {
 
     private transient CMap map;
     private transient TheThread thread;
     private transient ArrayList<Player> players = new ArrayList<Player>();
     private transient KListener kl;
     private transient ScoreBar scoreHandler;
     private transient ArrayList<Team> teams = new ArrayList<Team>();
     
     private ArrayList<Element> objects = new ArrayList<Element>();
     
     //Parameters to be given when starting a new game
    private String whoIsPlaying = "1,8,4,6"; // This is linked with skill, here player 1 is magician
     private String wichTeam = "0,1,0,2"; // Here player n2 is in team n0
     private boolean randStart = false;
 
     public Game() {
 
         this.setLayout(null);
         this.setBackground(Color.white);
         map = new CMap();
         map = XMLparser.parseMap(5);
         //Parse ParamTable
         XMLparser.parseParams();
 
         thread = new TheThread(this);
         thread.setRunning(false);
         new Thread(this.thread).start();
         thread.setRunning(true);
 
         kl = new KListener(this);
         this.addKeyListener(kl);
         this.setFocusable(true);
         requestFocus();
 
         initTeams();
         
         initPlayers();
 
         scoreHandler = new ScoreBar(this);
         
         //Testing :
         //Arrow arr = new Arrow(players.get(0).getCurrent(),2,this,players.get(0));
         //objects.add(arr);
     }
 
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         map.paintComponent(g);
         for (int i = 0; i < players.size(); i++) {
             players.get(i).paintComponent(g);
         }
         for(int j=0;j<objects.size();j++){
             objects.get(j).paintComponent(g);
         }
         this.scoreHandler.paintComponent(g);
     }
 
     public ArrayList<Player> getPlayers() {
         return players;
     }
 
     public CMap getMap() {
         return map;
     }
 
     public TheThread getThread() {
         return thread;
     }
 
     public void refreshHealthPoints() {
         ArrayList<Cell> myMap = map.getMyMap();
         for (int j = 0; j < myMap.size(); j++) {
             Cell c = myMap.get(j);
             c.refreshHealthPoints(this);
         }
         for (int i = 0; i < players.size(); i++) {
             Player p = players.get(i);
             Cell c = p.getCurrent();
             c.activateCell(p);
         }
     }
 
     public Player isOccupied(Cell c) {
         Player p = null;
         if (c != null && map.containsCell(c) != -1) {
             for (int i = 0; i < players.size(); i++) {
                 Player q = players.get(i);
                 if (q.getI() == c.getI() && q.getJ() == c.getJ()) {
                     p = q;
                     break;
                 }
             }
         }
         return p;
     }
 
     public void initPlayers() {
         ArrayList<Cell> startCellsAL = map.getStartCells();
         for (int i = 0; i < whoIsPlaying.length(); i += 2) {
             //Note : the number in "isPlaying" is also the character of the player. If 0, the player is disabled
             int charac = Integer.parseInt(""+whoIsPlaying.charAt(i));
             boolean isPlaying = charac!=0;
             if (isPlaying) {
                 int pid = i / 2;
                 Cell c;
                 if (randStart) {
                     int rand = Tools.randRange(0, startCellsAL.size() - 1);
                     c = startCellsAL.get(rand);
                     startCellsAL.remove(rand);
                 } else {
                     c = startCellsAL.get(pid);
                 }
                 Team team = teams.get(Integer.parseInt(""+wichTeam.charAt(i)));
                 Player p;
                 switch(charac){
                     case 1:
                         p = new Knight(pid, c, this,team);
                         break;
                     /*case 2:
                         p = new Warlock(pid, c, this,team);
                         break;*/
                     case 3:
                         p = new Miner(pid, c, this,team);
                         break;
                     case 4:
                         p = new Warlock(pid, c, this,team);
                         break;
                     case 5:
                         p = new Archer(pid, c, this,team);
                         break;
                     case 6:
                         p = new Vampire(pid, c, this,team);
                         break;
                     case 7:
                         p = new NoCharacter(pid, c, this,team);
                         break;
                     case 8:
                         p = new Magician(pid, c, this,team);
                         break;
                     case 9:
                         p = new Booster(pid, c, this,team);
                         break;
                 default:
                     p = new Knight(pid, c, this,team);
                     break;
                 }
                 players.add(p);
             }
         }
     }
 
     public void updateCellsByOwner() {
         for (int j = 0; j < teams.size(); j++) {
             Team te = teams.get(j);
             te.setNCells(0);
         }
         ArrayList<Cell> cells = map.getMyMap();
         for (int i = 0; i < cells.size(); i++) {
             Cell c = cells.get(i);
             if (c.getOwner() != null) {
                 Team te = c.getOwner();
                 te.setNCells(te.getNCells() + 1);
             }
         }
 
     }
 
 
     public ScoreBar getScoreHandler() {
         return scoreHandler;
     }
 
     public int getRWidth() {
         return this.getWidth();
     }
 
     public void initTeams() {
         /*
         Team te = new Team();
         teams.add(0, te);
         te = new Team();
         teams.add(1, te);
         te = new Team();
         teams.add(2, te);
         te = new Team();
         teams.add(3, te);
         */
         for (int i = 0; i < wichTeam.length(); i += 2) {
             int idT = Integer.parseInt(""+wichTeam.charAt(i));
             if(idT>=teams.size()){
                 Team te = new Team();
                 teams.add(idT, te);
             }
         }
     }
 
     public void setTeams(ArrayList<Team> teams) {
         this.teams = teams;
     }
 
     public ArrayList<Team> getTeams() {
         return teams;
     }
 
     public void computeObjects() {
         for(int j=0;j<objects.size();j++){
             Element e = objects.get(j);
             if(e instanceof Arrow){
                 Arrow a = (Arrow)e;
                 a.effect();
             }
         }
     }
     
     public void addObject(Element e){
         objects.add(e);
     }
     
     public void deleteObject(Element e){
         if(objects.contains(e)){
             objects.remove(e);
         }
     }
 }
