 package gui;
 
 import fap_java.Game;
 import fap_java.KListener;
 import fap_java.Params;
 import fap_java.Player;
 import fap_java.Tools;
 
 import java.awt.Graphics;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 public class CharacterSelection extends FAPanel implements NeedingFocus,AnimPanel {
     private JButton btnNext = new JButton();
     private ArrayList<CharacterDisplay> charList;
     private ArrayList<ArrowSelect> arrowList;
     private ArrayList<PlayerSelect> players;
     private ArrayList<Integer> timers;
     private StandardKListener kl;
     
     private ThreadGUI theThread;
     
     // Displaying
     private static int characDisplayOrigX = 0;
     private static int characDisplayIncrement = 100;
     
     private int arroSelectOrigY = -20;
     
     private Game advGame;
     
     public CharacterSelection(TheFrame theFrame, JPanel jPanel) {
         super(theFrame, jPanel);
         
         players = ((PlayerSelection)prevPanel).getPlayers();
         advGame = null;
         initEverything();
         
     }
     
     public CharacterSelection(TheFrame theFrame, JPanel jPanel, Game game) {
         super(theFrame,jPanel);
         players = new ArrayList<PlayerSelect>();
         this.advGame = game;
         for(int i=0;i<advGame.getPlayers().size();i++){
             Player p = advGame.getPlayers().get(i);
             PlayerSelect ps = new PlayerSelect(null);
             ps.setControler(p.getControler());
             ps.setId(i);
             if(p.getFsm() != null){
                 ps.setIsFSM(1);
             }
             players.add(ps);
         }
         initEverything();
     }
     
     public void initEverything(){
         swordX = minxS;
         cloudsX = minxC;
         
         this.setLayout(null);
         this.setSize(Constants.frameDimension);
 
         btnGoBack.setText("Retour");
         btnGoBack.setSize(120, 40);
         btnGoBack.setLocation(20, 20);
         
         btnNext.setText("Suivant");
         btnNext.setSize(120, 40);
         btnNext.setLocation(this.getWidth()-30-btnNext.getWidth(), 20);
         btnNext.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 nextFrame();
             }
         });
         
         if(advGame == null){
             this.add(btnGoBack);
         }
         this.add(btnNext);
         this.validate();
         this.repaint();
         
         charList = new ArrayList<CharacterDisplay>();
         arrowList = new ArrayList<ArrowSelect>();
         
         int k=1;
         for(int i=1;i<10;i++){
             if(i != 2 && i != 7){
                 charList.add(new CharacterDisplay(this.characDisplayOrigX+k*this.characDisplayIncrement+Tools.randRange(-10, 10),300+Tools.randRange(0,50),i,this));
                 k++;
             }
         }
         
         timers = new ArrayList<Integer>();
         for(int r=0;r<Params.nPlayersOn1Computer;r++){
             arrowList.add(null);
             timers.add(0);
         }
         
         for(int j=0;j<players.size();j++){
             PlayerSelect ps = players.get(j);
             if(ps.getIsFSM() == 0){
                 ps.setPc(Tools.randRange(1,9,Params.excludedChars));
                 ArrowSelect as = new ArrowSelect(ps,this);
                 arrowList.set(ps.getControler(),as);
             }
         }
         
         
         initFocus();
         
         theThread = new ThreadGUI(this);
         theThread.setRunning(false);
         new Thread(this.theThread).start();
         theThread.setRunning(true);
     }
     
     private void initKListener() {
         kl = new StandardKListener();
         this.addKeyListener(kl);
     }
     
     public void initFocus() {
         initKListener();
         this.setFocusable(true);
         requestFocus();
     }
     
     public void nextFrame(){
         // Set skills for FSMs :
         for(int i=0;i<players.size();i++){
             PlayerSelect ps = players.get(i);
             if(ps.getIsFSM() != 0){
                 ps.setPc(Tools.randRange(1, 9, Params.excludedChars));
             }
             if(advGame != null){
                 Player formerP = advGame.getPlayers().get(i);
                 int fsmLevel;
                formerP.getTeam().getPlayersInThisTeam().remove(formerP);
                 if(formerP.getFsm() != null){
                     fsmLevel = formerP.getFsm().getLevel();
                 }
                 else{
                     fsmLevel = 0;
                 }
                 Player p = Game.generatePlayer(ps.getPc(), i, formerP.getCurrent(), formerP.getTeam(), fsmLevel, formerP.getControler(), advGame);
                 advGame.getPlayers().set(i, p);
             }
         }
         // Proceeding to next panel
         JPanel nextPanel;
         if(advGame != null){
             nextPanel = new LoadingScreen(this.parent,advGame,this,advGame.getMap().getFileID());
         }
         else{
             nextPanel = new MapSelect(parent,this);
         }
         parent.changePanel(nextPanel);
     }
     
     public void paintComponent(Graphics g){
         super.paintComponent(g);
         
         for(int i=0;i<charList.size();i++){
             charList.get(i).paintComponent(g);
         }
         for(int j=0;j<arrowList.size();j++){
             ArrowSelect ar = arrowList.get(j);
             if(ar != null){
                 ar.paintComponent(g);
             }
         }
     }
 
     public static void setCharacDisplayOrigX(int characDisplayOrigX) {
         CharacterSelection.characDisplayOrigX = characDisplayOrigX;
     }
 
     public static int getCharacDisplayOrigX() {
         return characDisplayOrigX;
     }
 
     public static void setCharacDisplayIncrement(int characDisplayIncrement) {
         CharacterSelection.characDisplayIncrement = characDisplayIncrement;
     }
 
     public static int getCharacDisplayIncrement() {
         return characDisplayIncrement;
     }
 
     public void setCharList(ArrayList<CharacterDisplay> charList) {
         this.charList = charList;
     }
 
     public ArrayList<CharacterDisplay> getCharList() {
         return charList;
     }
 
     public void setPlayers(ArrayList<PlayerSelect> players) {
         this.players = players;
     }
 
     public ArrayList<PlayerSelect> getPlayers() {
         return players;
     }
 
     public void executeAnim() {
         for(int i=0;i<timers.size();i++){
             timers.set(i, timers.get(i)+1);
             ArrowSelect ar = this.arrowList.get(i);
             if(ar!=null && ar.isNeedAnim()){
                 ar.executeAnim();
             }
         }
         
         ArrayList<Integer> pressed = kl.getPressed();
         for(int i=0;i<pressed.size();i++){
             int code = pressed.get(i);
             for(int j = 0;j<arrowList.size();j++){
                 ArrowSelect ar = arrowList.get(j);
                 if(ar!=null && timers.get(j)>10){
                     for(int k=0;k<=4;k++){
                         if(code == Params.controlsList[ar.getPs().getControler()][k]){
                             timers.set(j, 0);
                             int pc = ar.getPs().getPc();
                             CharacterDisplay cd = null;
                             for(int l = 0;l<charList.size();l++){
                                 CharacterDisplay charD = charList.get(l);
                                 if(charD.getPc() == pc){
                                     if(k==2){
                                         cd = charList.get((l+1)%charList.size());
                                     }else if(k==3){
                                         int id = (l-1);
                                         if(l==0){
                                             id = charList.size()-1;
                                         }
                                         cd = charList.get(id);
                                     }
                                     
                                 }
                             }
                             // Set new pc  value
                             if(cd !=null){
                                 ar.getPs().setPc(cd.getPc());
                                 ar.computeWantedPosition();
                                 ar.computeSpeed();
                                 ar.setNeedAnim(true);
                             }
                         }
                     }
                 }
             }
         }
         this.repaint();
     }
 
     public void endAnim() {
         // NEVAH !
     }
 
     public void setArroSelectOrigY(int arroSelectOrigY) {
         this.arroSelectOrigY = arroSelectOrigY;
     }
 
     public int getArroSelectOrigY() {
         return arroSelectOrigY;
     }
 
     public void releaseFocus() {
     }
 }
