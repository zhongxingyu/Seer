 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package test;
 
 import java.awt.Color;
 
 /**
  *
  * @author Jörg Woditschka
  * @author Nadir Yuldashev
  */
 public class ActivityPhase {
     
     public boolean redBullPressed = false;
     public boolean duploPressed = false;
     public boolean OmniSensePressed = false;
     public boolean doNotPaintFlag = false;
     public boolean paintStudents = false;
     public int barNum = 0;
     public int studentDisplayed = -1;
     private javax.swing.JLabel label_timer;
     private javax.swing.JLabel label_redBull;
     private javax.swing.JLabel label_duplo;
     private javax.swing.JLabel label_omniSense;
     private javax.swing.JProgressBar KnowledgeBar;
     private javax.swing.JProgressBar MotivationBar;
     private javax.swing.JProgressBar TirednessBar;
     private javax.swing.JButton[] studButtons;
     private Game1 game;
     
     public ActivityPhase(javax.swing.JLabel label_timer, javax.swing.JProgressBar jKnowledgeBar,javax.swing.JProgressBar jMotivationBar,javax.swing.JProgressBar jTirednessBar, javax.swing.JLabel label_redBull, javax.swing.JLabel label_duplo, javax.swing.JLabel label_omniSense, javax.swing.JButton[] studButtons) {
         this.label_timer = label_timer;
         this.label_redBull = label_redBull;
         this.label_duplo = label_duplo;
         this.label_omniSense = label_omniSense;
         this.KnowledgeBar = jKnowledgeBar;
         this.MotivationBar = jMotivationBar;
         this.TirednessBar = jTirednessBar;
         this.studButtons = studButtons;
         game = Sims_1._maingame;
         game.initArray();
         game.initRoom();
         activityPhaseMain();
     }
     
     private void activityPhaseMain(){
         Thread runTimer = new Thread(new Timer(label_timer, game, KnowledgeBar, MotivationBar,TirednessBar, this));
         runTimer.start();  
     }
     
     public void barClicked(){
         paintStudents=true;
         
         if(this.barNum==0){
             for(int i=0; i<30; i++){
             Color color = new Color(220, 220, 220);
             System.out.println(studButtons[i]);
             studButtons[i].setBackground(color);
             studButtons[i].setOpaque(true);
             }
         }else if(this.barNum==1){
             for(int i=0; i<30; i++){
             Color color = new Color((int)(game.studentArray[i].getKnowledge()*2.55), 0, 0);
             System.out.println(studButtons[i]);
             studButtons[i].setBackground(color);
             studButtons[i].setOpaque(true);
             }
         } else if(this.barNum==2){
             for(int i=0; i<30; i++){
             Color color = new Color(0, (int)(game.studentArray[i].getMotivation()*2.55), 0);
             System.out.println(studButtons[i]);
             studButtons[i].setBackground(color);
             studButtons[i].setOpaque(true);
             }
         } else if(this.barNum==3){
             for(int i=0; i<30; i++){
             Color color = new Color(0, 0, (int)(game.studentArray[i].getTiredness()*2.55));
             System.out.println(studButtons[i]);
             studButtons[i].setBackground(color);
             studButtons[i].setOpaque(true);
             }
         }
     }
     
     public void StudentClicked(int studNum) {
                 this.studentDisplayed=studNum;
         if (redBullPressed) {
             if (game.redBull.amount > 0) {
                 game.redBull.amount -= 1;
                 label_redBull.setText(game.redBull.amount+"x");
                 game.studentArray[studNum].setKnowledge(game.studentArray[studNum].getKnowledge() + game.redBull.knowledge);
                 game.studentArray[studNum].setMotivation(game.studentArray[studNum].getMotivation() + game.redBull.motivation);
                 game.studentArray[studNum].setTiredness(game.studentArray[studNum].getTiredness() + game.redBull.tiredness);
             }
         } else if (duploPressed) {
             if (game.duplo.amount > 0) {
                 game.duplo.amount -= 1;
                 label_duplo.setText(game.duplo.amount+"x");
                 game.studentArray[studNum].setKnowledge(game.studentArray[studNum].getKnowledge() + game.duplo.knowledge);
                 game.studentArray[studNum].setMotivation(game.studentArray[studNum].getMotivation() + game.duplo.motivation);
                 game.studentArray[studNum].setTiredness(game.studentArray[studNum].getTiredness() + game.duplo.tiredness);
             }
         } else if (OmniSensePressed) {
             if (game.omniSenseAudio.amount > 0) {
                 game.omniSenseAudio.amount -= 1;
                 label_omniSense.setText(game.omniSenseAudio.amount+"x");
                 game.studentArray[studNum].setKnowledge(game.studentArray[studNum].getKnowledge() + game.omniSenseAudio.knowledge);
                 game.studentArray[studNum].setMotivation(game.studentArray[studNum].getMotivation() + game.omniSenseAudio.motivation);
                 game.studentArray[studNum].setTiredness(game.studentArray[studNum].getTiredness() + game.omniSenseAudio.tiredness);
             }
         }
         displayStudentBars();
     }
     
     public void displayStudentBars(){
        
         MotivationBar.setValue((int)game.studentArray[this.studentDisplayed].getMotivation());
         TirednessBar.setValue((int)game.studentArray[this.studentDisplayed].getTiredness());
        KnowledgeBar.setValue((int)((game.studentArray[this.studentDisplayed].getKnowledge())*250)); // 250 is the facot we set so we see growth faster, change later by Nadir
         KnowledgeBar.repaint();
         MotivationBar.repaint();
         TirednessBar.repaint();
         this.doNotPaintFlag = true;
         barClicked();
     }
 
 }
