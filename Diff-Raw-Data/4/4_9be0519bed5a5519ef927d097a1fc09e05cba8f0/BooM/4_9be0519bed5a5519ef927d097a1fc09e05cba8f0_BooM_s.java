 package boom;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class BooM {
     public static PlaySound playSound = new PlaySound();
     public static JFrame f = new JFrame("BooM! Game");
     public static int bomb,step = 5, player1Health = 3, player2Health = 3, itemValue;
     public static int player = 1;
     public static int item1, item2;
     public static Ran ran = new Ran();
     public static JLabel BG,l1, l2, l3, l4, l5, l6,count1, item;
     public static String soundType = "boom.wav";
     public static String iHasLife = "\\iHasLife.png";
     public static String loseLife = "\\youloseLife.png";
     public static String youHasLife = "\\youHasLife.gif";
     public static JLabel boom, player1, player2, player1HealthLabel, player2HealthLabel;
     public static String imageDir = System.getProperty("user.dir") + "\\src\\images";
     public static String soundDir = System.getProperty("user.dir") + "\\src\\sound";
     public static Timer timer;
     public static boolean start = false,win=false;
     
     public static void init(){
         item = new JLabel();
         item.setBounds(443, 250, 150, 100);
         item.setOpaque(false);
         item.setIcon(new ImageIcon(imageDir + iHasLife));
         item.setVisible(false);
         
         player1HealthLabel = new JLabel();
         player1HealthLabel.setBounds(0, 130, 280, 100);
         player1HealthLabel.setOpaque(false);
         player1HealthLabel.setIcon(new ImageIcon(imageDir + "\\P1has3.gif"));
         
         player2HealthLabel = new JLabel();
         player2HealthLabel.setBounds(750, 130, 280, 100);
         player2HealthLabel.setOpaque(false);
         player2HealthLabel.setIcon(new ImageIcon(imageDir + "\\P2has3.gif"));
         
         count1 = new JLabel();
         ImageIcon count = new ImageIcon(imageDir + "\\5.jpg");
         count1.setBounds(540, 376, 33, 57);
         count1.setOpaque(false);
         count1.setIcon(count);
         
         player1= new JLabel();
         ImageIcon p1img = new ImageIcon(imageDir + "\\player1.gif");
         player1.setBounds(-30, 350, 200, 200);
         player1.setOpaque(false);
         player1.setIcon(p1img);
         player2= new JLabel();
         ImageIcon p2img = new ImageIcon(imageDir + "\\player2.gif");
         player2.setBounds(810, 320, 220, 240);
         player2.setOpaque(false);
         player2.setIcon(p2img);
         player2.setVisible(false);
         
         boom= new JLabel();
         ImageIcon boomImg = new ImageIcon(imageDir + "\\explode.gif");
         boom.setBounds(359, 350, 300, 180);
         boom.setOpaque(true);
         boom.setIcon(boomImg);
         boom.setVisible(false);
         
         BG= new JLabel();
         ImageIcon bg = new ImageIcon(imageDir + "\\BGimg.jpg");
         BG.setBounds(0,0,1000,600);
         BG.setOpaque(true);
         BG.setIcon(bg);
         
         l1= new JLabel();
         l2= new JLabel();
         l3= new JLabel();
         l4= new JLabel();
         l5= new JLabel();
         l6= new JLabel();
         l1.setBounds(204, 280, 200, 50);
         l1.setOpaque(false);
         l1.setIcon(new ImageIcon(imageDir + "\\lineL.png"));
         l2.setBounds(204, 380, 200, 50);
         l2.setOpaque(false);
         l2.setIcon(new ImageIcon(imageDir + "\\lineL.png"));
         l3.setBounds(204, 480, 200, 50);
         l3.setOpaque(false);
         l3.setIcon(new ImageIcon(imageDir + "\\lineL.png"));
         
         l4.setBounds(663, 280, 200, 50);
         l4.setOpaque(false);
         l4.setIcon(new ImageIcon(imageDir + "\\lineR.png"));
         l5.setBounds(663, 380, 200, 50);
         l5.setOpaque(false);
         l5.setIcon(new ImageIcon(imageDir + "\\lineR.png"));
         l6.setBounds(663, 480, 200, 50);
         l6.setOpaque(false);
         l6.setIcon(new ImageIcon(imageDir + "\\lineR.png"));
         
         boom.addMouseListener(new MouseAdapter() { 
         public void mouseClicked(MouseEvent e) 
         { 
             boom.setVisible(false);
             reset();
         }}); 
         
         l1.addMouseListener(new MouseAdapter() { 
         public void mouseClicked(MouseEvent e) 
         {
             item.setVisible(false);
             if(!start){
                 start=true;
                 timer.start();
             }
             if(item1 == 1 || item2 == 1){
                 showItem();
                 getItem();
             }
             if(bomb == 1){
                 showBoom();
             }
             else{
                 changePlayer();
             }
             l1.setVisible(false);
         }}); 
         
         l2.addMouseListener(new MouseAdapter() { 
         public void mouseClicked(MouseEvent e) 
         { 
             item.setVisible(false);
             if(!start){
                 start=true;
                 timer.start();
             }
             if(item1 == 2 || item2 == 2){
                 showItem();
                 getItem();
             }
             if(bomb == 2){
                 showBoom();
             }
             else{
                 changePlayer();
             }
             l2.setVisible(false);
         }}); 
         
         l3.addMouseListener(new MouseAdapter() { 
         public void mouseClicked(MouseEvent e) 
         { 
             item.setVisible(false);
             if(!start){
                 start=true;
                 timer.start();
             }
             if(item1 == 3 || item2 == 3){
                 showItem();
                 getItem();
             }
             if(bomb == 3){
                 showBoom();
             }
             else{
                 changePlayer();
             }
             l3.setVisible(false);
         }}); 
         
         l4.addMouseListener(new MouseAdapter() { 
         public void mouseClicked(MouseEvent e) 
         { 
             item.setVisible(false);
             if(!start){
                 start=true;
                 timer.start();
             }
             if(item1 == 4 || item2 == 4){
                 showItem();
                 getItem();
             }
             if(bomb == 4){
                 showBoom();
             }
             else{
                 changePlayer();
             }
             l4.setVisible(false);
         }}); 
         
         l5.addMouseListener(new MouseAdapter() { 
         public void mouseClicked(MouseEvent e) 
         { 
             item.setVisible(false);
             if(!start){
                 start=true;
                 timer.start();
             }
             if(item1 == 5 || item2 == 5){
                 showItem();
                 getItem();
             }
             if(bomb == 5){
                 showBoom();
             }
             else{
                 changePlayer();
             }
             l5.setVisible(false);
         }}); 
         
         l6.addMouseListener(new MouseAdapter() { 
         public void mouseClicked(MouseEvent e) 
         { 
             item.setVisible(false);
             if(!start){
                 start=true;
                 timer.start();
             }
             if(item1 == 6 || item2 == 6){
                 showItem();
                 getItem();
             }
             if(bomb == 6){
                 showBoom();
             }
             else{
                 changePlayer();
             }
             l6.setVisible(false); 
         }}); 
     }
     
     public static void getItem(){
         if(itemValue == 1){
             if(player == 1){
                 if(player1Health < 3){
                     player1Health++;
                 }
             }
             else{
                 if(player2Health < 3){
                     player2Health++;
                 }
             }
         }
         else if(itemValue == 2){
             if(player == 1){
                 player2Health--;
             }
             else{
                 player1Health--;
             }
         }
         else{
             if(player == 1){
                 if(player2Health < 3){
                     player2Health++;
                 }
             }
             else{
                 if(player1Health < 3){
                     player1Health++;
                 }
             }
         }
         updateHealth();
         if(player1Health <= 0 || player2Health <= 0){
             showBoom();
         }
     }
     
     public static void showItem(){
         playSound.play("item.wav");
         ran = new Ran();
         itemValue = ran.RandomItem();
         if(itemValue == 1){
             item.setIcon(new ImageIcon(imageDir + iHasLife));
         }
         else if(itemValue == 2){
             item.setIcon(new ImageIcon(imageDir + loseLife));
         }
         else{
             item.setIcon(new ImageIcon(imageDir + youHasLife));
         }
         item.setVisible(true);
     }
     
     public static void showBoom(){
         timer.stop();
         l1.setVisible(false);
         l2.setVisible(false);
         l3.setVisible(false);
         l4.setVisible(false);
         l5.setVisible(false);
         l6.setVisible(false);
         
         
         if(player == 1){
             player1Health--;
             if(player1Health <= 0){
                 win=true;
                 changePlayer();
                 timer.stop();
                 boom.setBounds(200, 100, 600, 400);
                 boom.setOpaque(false);
                 boom.setIcon(new ImageIcon(imageDir + "\\player2win.gif"));
                 soundType = "win.wav";
             }
         }
         else{
             player2Health--;
             if(player2Health <= 0){
                 win=true;
                 changePlayer();
                 timer.stop();
                 boom.setBounds(200, 100, 600, 400);
                 boom.setOpaque(false);
                 boom.setIcon(new ImageIcon(imageDir + "\\player1win.gif"));
                 soundType = "win.wav";
             }
         }
         playSound.play(soundType);
         boom.setVisible(true); 
         updateHealth();
     }
     
     public static void updateHealth(){
         player1HealthLabel.setIcon(new ImageIcon(imageDir + "\\P1has" + player1Health + ".gif"));
         player2HealthLabel.setIcon(new ImageIcon(imageDir + "\\P2has" + player2Health + ".gif"));
     }
     
     public static void changePlayer(){ 
         step=5;
         ImageIcon count = new ImageIcon(imageDir + "\\5.jpg");
         count1.setBounds(540, 376, 33, 57);
         count1.setOpaque(false);
         count1.setIcon(count);
         timer.start();
         
         if(player == 1){       
             player2.setVisible(true);
             player1.setVisible(false);
             player = 2;
         }
         else{
             player2.setVisible(false);
             player1.setVisible(true);
             player = 1;
         }
     }
     
     public static void reset(){
         if(win){
             soundType = "boom.wav";
             win=false;
             player1Health=3;
             player2Health=3;
             updateHealth();
             ImageIcon boomImg = new ImageIcon(imageDir + "\\explode.gif");
             boom.setBounds(359, 350, 300, 180);
             boom.setOpaque(true);
             boom.setIcon(boomImg);
             boom.setVisible(false);
         }
         step=5;
         ImageIcon count = new ImageIcon(imageDir + "\\5.jpg");
         count1.setIcon(count);
         timer.start();
         bomb = ran.Randombomb();
         randomItems();
         l1.setVisible(true);
         l2.setVisible(true);
         l3.setVisible(true);
         l4.setVisible(true);
         l5.setVisible(true);
         l6.setVisible(true);
         changePlayer();
         item.setVisible(false);
     }
     
     public static void randomItems(){
         item1 = ran.Randombomb();
         ran = new Ran();
         item2 = ran.Randombomb();
     }
     
     public static void main(String[] args) {
         int delay = 1000; //milliseconds
         ActionListener taskPerformer = new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 step--;
                 ImageIcon count = new ImageIcon(imageDir + "\\"+step+".jpg");
                 count1.setBounds(540, 376, 33, 57);
                 count1.setOpaque(false);
                 count1.setIcon(count); 
                 if(step<=0) { 
                     timer.stop();
                     showBoom();
                 }
                 playSound.play("clock.wav");
             }
         };
         timer = new Timer(delay, taskPerformer);
         randomItems();
         init();
         bomb = ran.Randombomb();
         f.setSize(1000, 600);
         f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         JLayeredPane layer = new JLayeredPane();
         layer.setPreferredSize(new Dimension(1000, 600));
         layer.setBorder(BorderFactory.createTitledBorder("BooM! Game"));        
         
         layer.add(boom);
         layer.add(item);
         layer.add(player1HealthLabel);
         layer.add(player2HealthLabel);
         layer.add(count1);
         layer.add(player1);
         layer.add(player2);  
         layer.add(l1);
         layer.add(l2);
         layer.add(l3);
         layer.add(l4);
         layer.add(l5);
         layer.add(l6);
         layer.add(BG);
         
         f.add(layer); 
         f.setVisible(true);
     }
 }
