 /*
  *
  */
 package HangMan;
 
 import db.FileManager;
 import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import sound.MP3;
 
 /**
  *
  * @Author Mohamed Omar Mohamed Mohsen
  *
  * @Function HangManForm.java This one class game that is based on the famous
  * "Hangman" game...you will go though stages tring to solve the secret games
  * and get the highest score
  *
  * @Created on May 15, 2007, 9:27 PM
  *
  * @
  *
  */
 public class HangManForm extends javax.swing.JFrame {
     
     ArrayList<String> secretWords = null;
     String currentSecretWord;
     int maxTries = 5;
     int numOfTries;
     int correctGuess;
     ArrayList wrongLetters;
     String secrerWordLetters[];
     int foundLetters[];
     Random rand;
     int lblMogOrignalLocationX;
     int lblMogOrignalLocationY;
     String imagesMog[] = {"Mog_1.gif", "Mog_2.gif", "Mog_2.gif", "Mog_3.gif", "Mog_3.gif"};
     String imagePiranna = "Piranha.gif";
     int score;
     int bonus;
 //    JDBCHelper jh = null;
     FileManager mng = null;
     Graphics2D g2d;
     int stageNum = 0;
     boolean continuePlaying = false;
     boolean theBossHasCome = false;
     int VICTORY = 0;
     int INTRO = 1;
     int BATTLE = 2;
     int BOSS = 3;
     int BOSSWARNING = 4;
     int HIGHSCORE = 5;
     MP3 victory;
     MP3 intro;
     MP3 battle;
     MP3 boss;
     MP3 bossWarning;
     MP3 highScore;
     JButton keyBoardBtns[] = new JButton[26];
     String soundPackage = "sound/";
     String imageFolderPath = "images/";
 
     /**
      * Creates new form HangManForm
      */
     public HangManForm() {
         initComponents();
         
         btnRestart.setVisible(false);
         
         panelGame.setLayout(null);
         panelMainMenu.setLayout(null);
         panelImages.setLayout(null);
         
         
         
         score = 0;
         
         lblMogOrignalLocationX = lblMog.getLocation().x;
         lblMogOrignalLocationY = lblMog.getLocation().y;
 
 //        try {
 //            jh = new JDBCHelper();
 //            mng = new JDBCManager(jh.getConnection());
 //        } catch (SQLException ex) {
 //            ex.printStackTrace();
 //        } catch (ClassNotFoundException ex) {
 //            ex.printStackTrace();
 //        }
         mng = new FileManager();
         
         keyBoardBtns[0] = btnA;
         keyBoardBtns[1] = btnB;
         keyBoardBtns[2] = btnC;
         keyBoardBtns[3] = btnD;
         keyBoardBtns[4] = btnE;
         keyBoardBtns[5] = btnF;
         keyBoardBtns[6] = btnG;
         keyBoardBtns[7] = btnH;
         keyBoardBtns[8] = btnI;
         keyBoardBtns[9] = btnJ;
         keyBoardBtns[10] = btnK;
         keyBoardBtns[11] = btnL;
         keyBoardBtns[12] = btnM;
         keyBoardBtns[13] = btnN;
         keyBoardBtns[14] = btnO;
         keyBoardBtns[15] = btnP;
         keyBoardBtns[16] = btnQ;
         keyBoardBtns[17] = btnR;
         keyBoardBtns[18] = btnS;
         keyBoardBtns[19] = btnT;
         keyBoardBtns[20] = btnU;
         keyBoardBtns[21] = btnV;
         keyBoardBtns[22] = btnW;
         keyBoardBtns[23] = btnX;
         keyBoardBtns[24] = btnY;
         keyBoardBtns[25] = btnZ;
 
         //restart();
         
         lblTries.setVisible(false);
         
         addActionListenerToAllKeyboardButtons();
         putImagesOnLabels();
         getSoundFiles();
         //loadAllSoundFiles();
         
         showMainMenuPanel();
     }
     
     private MP3 createMP3(String soundFileName) {
 //        try {
 //            File file = new File(getClass().getClassLoader().getResource(soundPackage+soundFileName).toURI());
 //            System.out.println("createMP3(): "+file.getAbsolutePath());
         return new MP3(soundFileName);
 //        } catch (URISyntaxException ex) {
 //            Logger.getLogger(HangManForm.class.getName()).log(Level.SEVERE, null, ex);
 //        }
 //        return null;
     }
 
     /**
      * This Method will get all the necessery sound files from the sound package
      */
     public void getSoundFiles() {
 
 //        String soundFile = "FF_VII_Victory_Fanfare.mp3";
 //        URL url = soundPackage+soundFile);
 //        File file = new File(url.getFile());
 //        String absoluteFilePath = file.getAbsolutePath().replace("%20"," ");
         victory = createMP3("FF_VII_Victory_Fanfare.mp3");
         intro = createMP3("intro_2.mp3");
         battle = createMP3("FF_VII_Battle_Theme.mp3");
         boss = createMP3("Kingdom_Hearts_FF_VII_One_Winged_Angel.mp3");
         bossWarning = createMP3("bossstage.mp3");
         highScore = createMP3("high_score.mp3");
     }
 
     /**
      * This method will start and stop all the sound file to get hem loaded
      */
     public void loadAllSoundFiles() {
         victory.play();
         victory.close();
         
         intro.play();
         intro.close();
         
         battle.play();
         battle.close();
         
         boss.play();
         boss.close();
         
         bossWarning.play();
         bossWarning.close();
         
         highScore.play();
         highScore.close();
         
     }
 
     /**
      * If you win the game this method will be called then it will call
      * newSecretWord() and resetLabels()
      */
     public void continueTheGame() {
         newSecretWord();
         
         resetLabels();
     }
 
     /**
      * This method will reset the necessary label for the next stage or for new
      * game. It will also call putTheMogBack();
      */
     public void resetLabels() {
         
         numOfTries = 0;
         correctGuess = 0;
         bonus = 0;
         
         lblMsg.setText("");
         lblSecretWord.setText("");
         lblWorngLetters.setText("");
         lblTries.setText("");
         
         lblScore.setText("Score: " + score);
         lblStage.setText("Stage: " + stageNum);
         
         wrongLetters = new ArrayList();
         
         lblTries.setText("you have used <br>" + numOfTries + "/" + maxTries + " tries");
         
         for (int i = 0; i < currentSecretWord.length(); i++) {
             lblSecretWord.setText(lblSecretWord.getText() + "_ ");
             secrerWordLetters[i] = currentSecretWord.charAt(i) + "";
             foundLetters[i] = 0;
             //wrongLetters[i] = '';
         }
         
         putTheMogBack();
     }
     
     private ImageIcon createImageIcon(String fileName) {
         return new ImageIcon(getClass().getClassLoader().getResource(fileName));
     }
 
     /**
      * This method will return the moggle picture back to the start point
      */
     public void putTheMogBack() {
         lblMog.setLocation(lblPiranna.getLocation().x, lblPiranna.getLocation().y - 100);
 //        lblMog.setIcon(new ImageIcon(imageFolderPath+imagesMog[0]));
         lblMog.setIcon(createImageIcon(imageFolderPath + imagesMog[0]));
     }
 
     /**
      * put the images on the labels
      */
     public void putImagesOnLabels() {
         
         lblMog.setText("");
         lblPiranna.setText("");
         
         String imagesMog[] = {"Mog_1.gif", "Mog_2.gif", "Mog_2.gif", "Mog_3.gif", "Mog_3.gif"};
         String imagePiranna = "Piranha.gif";
         
         lblMog.setIcon(createImageIcon(imageFolderPath + imagesMog[0]));
         lblPiranna.setIcon(createImageIcon(imageFolderPath + imagePiranna));
         
     }
 
     /**
      * This method will restart the game, it will call newSecretWord(),
      * resetLabels(), putTheMogBack() and enableButtons()
      */
     public void restart() {
         theBossHasCome = false;
         continuePlaying = true;
         score = 0;
         stageNum = 1;
         
         newSecretWord();
         
         resetLabels();
         putTheMogBack();
         enableButtons();
         
         btnRestart.setEnabled(false);
     }
 
     /**
      * This method will get the new secret word
      */
     public void newSecretWord() {
         if (!theBossHasCome) {
             rand = new Random();
             if (secretWords == null) {
                 secretWords = mng.getAllSecretWords();
             }
             
             int randomNumber = rand.nextInt(secretWords.size());
             currentSecretWord = secretWords.get(randomNumber);
             secretWords.remove(randomNumber);
         } else {
             System.out.println("stageNum: " + stageNum);
             currentSecretWord = mng.getTheBoss(stageNum);
         }
         System.out.println("The current secret word is: " + currentSecretWord
                 + ", and it's length is: " + currentSecretWord.length());
         
         secrerWordLetters = new String[currentSecretWord.length()];
         foundLetters = new int[currentSecretWord.length()];
         
         
     }
 
     /**
      * This method will get the top ten scores from the DB
      */
     public String getTopTenScores() {
         String toPrint = "";
         int USER_NAME = 0;
         int USER_SCORE = 1;
         
         
         String[][] scores = mng.getTopTenScores();
         toPrint = "<html><table aling = \"center\">";
         toPrint += "<tr>"
                 + "<th>User Name</th>"
                 + "<th>Score</th>"
                 + "</tr>"
                 + "<tr>"
                 + "<th></th>"
                 + "<th></th>"
                 + "</tr>"
                 + "";
         for (int i = 0; i < scores.length; i++) {
             toPrint += "<tr>"
                     + "<td>" + scores[i][USER_NAME] + "</td>"
                     + "<td>" + scores[i][USER_SCORE] + "</td>"
                     + "</tr>";
         }
         toPrint += "</html>";
         return toPrint;
     }
 
     /**
      * This method will recieve the letter from the button in the keyboardPanel
      * and check it against the secret word this method will call
      * reprintLabels()
      *
      * @Parameters char letter: this will be recieved from the pressed button
      */
     public void checkButtonPressed(char letter) {
         System.out.println("CheckingButtonPressed: " + letter);
         numOfTries++;
         System.out.println("numOfTries: " + numOfTries);
         boolean correctAnswer = false;
         for (int i = 0; i < currentSecretWord.length(); i++) {
             
             if (secrerWordLetters[i].equalsIgnoreCase(letter + "")) {
                 correctGuess++;
                 foundLetters[i] = 1;
                 correctAnswer = true;
             }
         }
         if (correctAnswer) {
             System.out.println("Correct Answer!!");
             numOfTries--;
         } else {
             int location = 10;
             while (location != 0) {
                 try {
                     Thread.currentThread().sleep(10);
                 } catch (InterruptedException ex) {
                     ex.printStackTrace();
                 }
                 lblMog.setLocation(lblMog.getLocation().x, lblMog.getLocation().y + 2);
                 location--;
             }
             wrongLetters.add(letter);
         }
         reprintLabels();
     }
 
     /**
      * This method will reprint the labels in the gamePanel depending on the
      * game status. this method will call chackGameStatus()
      */
     public void reprintLabels() {
         System.out.println("reprintSecretWord");
         
         lblSecretWord.setText("");
         for (int i = 0; i < currentSecretWord.length(); i++) {
             if (foundLetters[i] == 0) {
                 lblSecretWord.setText(lblSecretWord.getText() + "_ ");
             } else {
                 lblSecretWord.setText(lblSecretWord.getText() + secrerWordLetters[i]);
             }
         }
         
         lblWorngLetters.setText("");
         for (Object elem : wrongLetters) {
             lblWorngLetters.setText(lblWorngLetters.getText() + " " + elem.toString());
         }
         
         if (numOfTries != 0) {
             lblMog.setIcon(createImageIcon(imageFolderPath + imagesMog[numOfTries - 1]));
         }
         
         lblTries.setText("you have used <br>" + numOfTries + "/" + maxTries + " tries");
         lblScore.setText("Score: " + score);
         
         System.out.println("secretWord status: " + lblSecretWord.getText());
         checkGameStatus();
     }
 
     /**
      * This method will check is finished or not. if the game is finished, if
      * will check if the player win or lose This method will call several
      * mothods depending on the game status - win: play the victory sound file,
      * it will also check the Boss stage - lose: will check if the score is new
      * high score and invoke gameOver()
      */
     public void checkGameStatus() {
         System.out.println("checkGameStatus");
         boolean win = true;
         boolean gameFinished = false;
         if (numOfTries == maxTries || correctGuess == foundLetters.length) {
             
             gameFinished = true;
             btnRestart.setEnabled(true);
             
             for (int i = 0; i < foundLetters.length; i++) {
                 if (foundLetters[i] == 0) {
                     win = false;
                     break;
                 }
             }
             if (gameFinished) {
                 if (win) {
                     stageNum++;
                     lblMsg.setText("Congrats");
                     lblSecretWord.setText(currentSecretWord);
                     
                     if (numOfTries == 0) {
                         numOfTries = 1;
                         bonus = currentSecretWord.length() * 10;
                         lblMsg.setText("Perfect!!");
                     }
                     
                     score += currentSecretWord.length() * 10 / numOfTries + bonus;
                     lblScore.setText("Score: " + score);
                     //disableButtons();
                     
                     
                     playSoundFile(VICTORY);
                     
                     JOptionPane.showOptionDialog(null, "You won! click OK for the next stage", "Save The Moggle The Game", JOptionPane.YES_OPTION,
                             JOptionPane.INFORMATION_MESSAGE, null, null, null);
                     
                     if (stageNum == 5) {
                         /*get the boss*/
                         theBossHasCome = true;
                         playSoundFile(BOSSWARNING);
                         JOptionPane.showOptionDialog(null, "The boss has come", "Save The Moggle The Game", JOptionPane.YES_OPTION,
                                 JOptionPane.INFORMATION_MESSAGE, null, null, null);
                         playSoundFile(BOSS);
                         continueTheGame();
                     } else if (stageNum < 5) {
                         System.out.println("continue the battle");
                         playSoundFile(BATTLE);
                         continueTheGame();
                     } else if (stageNum > 5) {
                         JOptionPane.showOptionDialog(null, "You beat the boss and saved the moggle, thank you for playing", "Save The Moggle The Game", JOptionPane.YES_OPTION,
                                 JOptionPane.INFORMATION_MESSAGE, null, null, null);
                         gameOver();
                     }
                     
                     stopSoundFiles();
                     
                     enableButtons();
                 } else {
                     gameOver();
                 }
             }
         }
     }
 
     /**
      * This method will be invoked if the player lose the game or beat the boss
      */
     public void gameOver() {
         lblMsg.setText("Game Over");
         lblSecretWord.setText(currentSecretWord);
         score -= currentSecretWord.length() * 5 / numOfTries;
         if (score < 0) {
             score = 0;
         }
         lblScore.setText("Score: " + score);
         disableButtons();
         continuePlaying = false;
         
         
         if (mng.isNewHighScore(score)) {
             String playerName = JOptionPane.showInputDialog("You got new high score!! Enter your name", "Player");
             mng.addNewHighScore(playerName, score);
         }
         
         
         int choice = JOptionPane.showOptionDialog(null, "Game Over! Play Again?", "Save The Moggle The Game", JOptionPane.YES_NO_OPTION,
                 JOptionPane.INFORMATION_MESSAGE, null, null, null);
         
         if (choice == 0) {
             restart();
         } else {
             btnMainMenu.doClick();
         }
     }
 
     /**
      * This method will disable all the keyboardPanel buttons
      */
     public void disableButtons() {
         // <editor-fold defaultstate="collapsed" desc=" comminted code">
 //        btnA.setEnabled(false);
 //        btnB.setEnabled(false);
 //        btnC.setEnabled(false);
 //        btnD.setEnabled(false);
 //        btnE.setEnabled(false);
 //        btnF.setEnabled(false);
 //        btnG.setEnabled(false);
 //        btnH.setEnabled(false);
 //        btnI.setEnabled(false);
 //        btnJ.setEnabled(false);
 //        btnK.setEnabled(false);
 //        btnL.setEnabled(false);
 //        btnM.setEnabled(false);
 //        btnN.setEnabled(false);
 //        btnO.setEnabled(false);
 //        btnP.setEnabled(false);
 //        btnQ.setEnabled(false);
 //        btnR.setEnabled(false);
 //        btnS.setEnabled(false);
 //        btnT.setEnabled(false);
 //        btnU.setEnabled(false);
 //        btnV.setEnabled(false);
 //        btnW.setEnabled(false);
 //        btnX.setEnabled(false);
 //        btnY.setEnabled(false);
 //        btnZ.setEnabled(false);
         // </editor-fold>
         for (int i = 0; i < keyBoardBtns.length; i++) {
             keyBoardBtns[i].setEnabled(false);
         }
     }
 
     /**
      * This method will enable all the keyboardPanel buttons
      */
     public void enableButtons() {
         // <editor-fold defaultstate="collapsed" desc=" comminted code">
         /*
          btnA.setEnabled(true);
          btnB.setEnabled(true);
          btnC.setEnabled(true);
          btnD.setEnabled(true);
          btnE.setEnabled(true);
          btnF.setEnabled(true);
          btnG.setEnabled(true);
          btnH.setEnabled(true);
          btnI.setEnabled(true);
          btnJ.setEnabled(true);
          btnK.setEnabled(true);
          btnL.setEnabled(true);
          btnM.setEnabled(true);
          btnN.setEnabled(true);
          btnO.setEnabled(true);
          btnP.setEnabled(true);
          btnQ.setEnabled(true);
          btnR.setEnabled(true);
          btnS.setEnabled(true);
          btnT.setEnabled(true);
          btnU.setEnabled(true);
          btnV.setEnabled(true);
          btnW.setEnabled(true);
          btnX.setEnabled(true);
          btnY.setEnabled(true);
          btnZ.setEnabled(true);
          */
         // </editor-fold>
         for (int i = 0; i < keyBoardBtns.length; i++) {
             keyBoardBtns[i].setEnabled(true);
         }
     }
 
     /**
      * This method will reviece an integer and play a sound file according to
      * the integer. it will invoke the stopSoundFiles() before playing any sound
      * file
      *
      * @Parameter: int soundFileNumber: will represent on of this values int
      * VICTORY = 0; int INTRO = 1; int BATTLE = 2; int BOSS = 3; int BOSSWARNING
      * = 4; int HIGHSCORE = 5;
      */
     public void playSoundFile(int soundFileNumber) {
         
         stopSoundFiles();
 
         //getSoundFiles();
         
         if (soundFileNumber == VICTORY) {
             victory.play();
         }
         if (soundFileNumber == BATTLE) {
             battle.play();
         }
         if (soundFileNumber == INTRO) {
             intro.play();
         }
         if (soundFileNumber == BOSS) {
             boss.play();
         }
         if (soundFileNumber == BOSSWARNING) {
             bossWarning.play();
         }
         if (soundFileNumber == HIGHSCORE) {
             highScore.play();
         }
         
     }
 
     /**
      * This method will stop all the playing sound files
      */
     public void stopSoundFiles() {
         
         if (victory.isPlayingNow()) {
             victory.close();
         }
         if (intro.isPlayingNow()) {
             intro.close();
         }
         if (battle.isPlayingNow()) {
             battle.close();
         }
         if (boss.isPlayingNow()) {
             boss.close();
         }
         if (bossWarning.isPlayingNow()) {
             bossWarning.close();
         }
         if (highScore.isPlayingNow()) {
             highScore.close();
         }
         
     }
 
     /**
      * This methos will set all the panels visiblity to false and set the
      * gamePanel visiblity to true
      */
     public void showGamePanel() {
         if (theBossHasCome) {
             playSoundFile(BOSS);
         } else {
             playSoundFile(BATTLE);
         }
         panelMainMenu.setVisible(false);
         panelHighScore.setVisible(false);
         panelGame.setVisible(true);
         
         panelMainMenuBackground.setVisible(false);
         panelHighScoreBackground.setVisible(false);
         panelGameBackground.setVisible(true);
         
     }
 
     /**
      * This methos will set all the panels visiblity to false and set the
      * mainMenuPanel visiblity to true
      */
     public void showMainMenuPanel() {
         
         playSoundFile(INTRO);
         
         panelMainMenu.setVisible(true);
         panelHighScore.setVisible(false);
         panelGame.setVisible(false);
         
         panelMainMenuBackground.setVisible(true);
         panelHighScoreBackground.setVisible(false);
         panelGameBackground.setVisible(false);
         
     }
 
     /**
      * This methos will set all the panels visiblity to false and set the
      * highScorePanel visiblity to true
      */
     public void showHighScorePanel() {
         
         playSoundFile(HIGHSCORE);
         
         panelMainMenu.setVisible(false);
         panelHighScore.setVisible(true);
         panelGame.setVisible(false);
         
         panelMainMenuBackground.setVisible(false);
         panelHighScoreBackground.setVisible(true);
         panelGameBackground.setVisible(false);
         
     }
 
     /**
      * This method will add the action listener to all buttons in the
      * keyBoardPanel
      */
     public void addActionListenerToAllKeyboardButtons() {
         for (int i = 0; i < keyBoardBtns.length; i++) {
             keyBoardBtns[i].addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent evt) {
                     btnActionPerformed(evt);
                 }
             });
         }
     }
 
     /**
      * This is the action listener for all buttons in the keyBoardPanel
      */
     private void btnActionPerformed(java.awt.event.ActionEvent evt) {
         for (int i = 0; i < keyBoardBtns.length; i++) {
             if (keyBoardBtns[i].equals(evt.getSource())) {
                 System.out.println("btn " + keyBoardBtns[i].getText() + " in btnActionPerformed pressed!!");
                 keyBoardBtns[i].setEnabled(false);
                 checkButtonPressed(keyBoardBtns[i].getText().charAt(0));
             }
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
         panelImages = new javax.swing.JPanel();
         panelGame = new javax.swing.JPanel();
         panelGameBackground = new javax.swing.JPanel();
         lblTries = new javax.swing.JLabel();
         btnRestart = new javax.swing.JButton();
         lblSecretWord = new javax.swing.JLabel();
         lblWorngLetters = new javax.swing.JLabel();
         btnMainMenu = new javax.swing.JButton();
         lblMsg = new javax.swing.JLabel();
         lblScore = new javax.swing.JLabel();
         lblStage = new javax.swing.JLabel();
         lblPiranna = new javax.swing.JLabel();
         panelKeyboard = new javax.swing.JPanel();
         btnJ = new javax.swing.JButton();
         btnK = new javax.swing.JButton();
         btnL = new javax.swing.JButton();
         btnM = new javax.swing.JButton();
         btnN = new javax.swing.JButton();
         btnO = new javax.swing.JButton();
         btnP = new javax.swing.JButton();
         btnQ = new javax.swing.JButton();
         btnR = new javax.swing.JButton();
         btnS = new javax.swing.JButton();
         btnT = new javax.swing.JButton();
         btnU = new javax.swing.JButton();
         btnV = new javax.swing.JButton();
         btnW = new javax.swing.JButton();
         btnX = new javax.swing.JButton();
         btnY = new javax.swing.JButton();
         btnZ = new javax.swing.JButton();
         btnA = new javax.swing.JButton();
         btnB = new javax.swing.JButton();
         btnC = new javax.swing.JButton();
         btnD = new javax.swing.JButton();
         btnE = new javax.swing.JButton();
         btnF = new javax.swing.JButton();
         btnG = new javax.swing.JButton();
         btnH = new javax.swing.JButton();
         btnI = new javax.swing.JButton();
         lblMog = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         panelMainMenu = new javax.swing.JPanel();
         panelMainMenuBackground = new javax.swing.JPanel();
         jLabel5 = new javax.swing.JLabel();
         btnExit = new javax.swing.JButton();
         btnStart = new javax.swing.JButton();
         btnHighScore = new javax.swing.JButton();
         panelHighScore = new javax.swing.JPanel();
         btnFromHighScoreToMainMenu = new javax.swing.JButton();
         panelHighScoreBackground = new javax.swing.JPanel();
         lblHighScore = new javax.swing.JLabel();
         jLabel6 = new javax.swing.JLabel();
 
         panelImages.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         javax.swing.GroupLayout panelImagesLayout = new javax.swing.GroupLayout(panelImages);
         panelImages.setLayout(panelImagesLayout);
         panelImagesLayout.setHorizontalGroup(
             panelImagesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 45, Short.MAX_VALUE)
         );
         panelImagesLayout.setVerticalGroup(
             panelImagesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 102, Short.MAX_VALUE)
         );
 
         getContentPane().setLayout(null);
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("HangMan");
         setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         setMinimumSize(new java.awt.Dimension(640, 480));
         panelGame.setLayout(null);
 
         panelGame.setMaximumSize(new java.awt.Dimension(640, 480));
         panelGame.setMinimumSize(new java.awt.Dimension(640, 480));
         panelGameBackground.setMinimumSize(new java.awt.Dimension(640, 480));
         panelGameBackground.setOpaque(false);
         javax.swing.GroupLayout panelGameBackgroundLayout = new javax.swing.GroupLayout(panelGameBackground);
         panelGameBackground.setLayout(panelGameBackgroundLayout);
         panelGameBackgroundLayout.setHorizontalGroup(
             panelGameBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         panelGameBackgroundLayout.setVerticalGroup(
             panelGameBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         panelGame.add(panelGameBackground);
         panelGameBackground.setBounds(0, 0, 640, 480);
 
         lblTries.setFont(new java.awt.Font("Tahoma", 0, 18));
         lblTries.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblTries.setText("0/5 tries");
         lblTries.setDoubleBuffered(true);
         panelGame.add(lblTries);
         lblTries.setBounds(10, 310, 100, 22);
 
         btnRestart.setText("Restart");
         btnRestart.setDoubleBuffered(true);
         btnRestart.setEnabled(false);
         btnRestart.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRestartActionPerformed(evt);
             }
         });
 
         panelGame.add(btnRestart);
         btnRestart.setBounds(10, 450, 110, 20);
 
         lblSecretWord.setFont(new java.awt.Font("Tahoma", 0, 18));
         lblSecretWord.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         lblSecretWord.setText("secretWord");
         lblSecretWord.setDoubleBuffered(true);
         panelGame.add(lblSecretWord);
         lblSecretWord.setBounds(10, 250, 620, 22);
 
         lblWorngLetters.setFont(new java.awt.Font("Tahoma", 0, 18));
         lblWorngLetters.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblWorngLetters.setText("worngLetters");
         lblWorngLetters.setDoubleBuffered(true);
         panelGame.add(lblWorngLetters);
         lblWorngLetters.setBounds(10, 280, 102, 22);
 
         btnMainMenu.setText("Main Menu");
         btnMainMenu.setDoubleBuffered(true);
         btnMainMenu.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnMainMenuActionPerformed(evt);
             }
         });
 
         panelGame.add(btnMainMenu);
         btnMainMenu.setBounds(10, 425, 110, 20);
 
         lblMsg.setFont(new java.awt.Font("Tahoma", 0, 18));
         lblMsg.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblMsg.setText("Msg");
         lblMsg.setDoubleBuffered(true);
         panelGame.add(lblMsg);
         lblMsg.setBounds(150, 180, 330, 50);
 
         lblScore.setFont(new java.awt.Font("Tahoma", 0, 18));
         lblScore.setText("score:");
         lblScore.setDoubleBuffered(true);
         panelGame.add(lblScore);
         lblScore.setBounds(10, 200, 150, 22);
 
         lblStage.setFont(new java.awt.Font("Tahoma", 0, 18));
         lblStage.setText("Stage: ");
         lblStage.setDoubleBuffered(true);
         panelGame.add(lblStage);
         lblStage.setBounds(10, 170, 140, 22);
 
         lblPiranna.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Piranha.gif")));
         lblPiranna.setText("piranna");
         panelGame.add(lblPiranna);
         lblPiranna.setBounds(160, 340, 33, 48);
 
         panelKeyboard.setOpaque(false);
         btnJ.setText("J");
         btnJ.setMaximumSize(new java.awt.Dimension(50, 30));
         btnJ.setMinimumSize(new java.awt.Dimension(50, 30));
         btnJ.setOpaque(false);
 
         btnK.setText("K");
         btnK.setMaximumSize(new java.awt.Dimension(50, 30));
         btnK.setMinimumSize(new java.awt.Dimension(50, 30));
         btnK.setOpaque(false);
 
         btnL.setText("L");
         btnL.setMaximumSize(new java.awt.Dimension(50, 30));
         btnL.setMinimumSize(new java.awt.Dimension(50, 30));
         btnL.setOpaque(false);
 
         btnM.setText("M");
         btnM.setMaximumSize(new java.awt.Dimension(50, 30));
         btnM.setMinimumSize(new java.awt.Dimension(50, 30));
         btnM.setOpaque(false);
 
         btnN.setText("N");
         btnN.setMaximumSize(new java.awt.Dimension(50, 30));
         btnN.setMinimumSize(new java.awt.Dimension(50, 30));
         btnN.setOpaque(false);
 
         btnO.setText("O");
         btnO.setMaximumSize(new java.awt.Dimension(50, 30));
         btnO.setMinimumSize(new java.awt.Dimension(50, 30));
         btnO.setOpaque(false);
 
         btnP.setText("P");
         btnP.setMaximumSize(new java.awt.Dimension(50, 30));
         btnP.setMinimumSize(new java.awt.Dimension(50, 30));
         btnP.setOpaque(false);
 
         btnQ.setText("Q");
         btnQ.setMaximumSize(new java.awt.Dimension(50, 30));
         btnQ.setMinimumSize(new java.awt.Dimension(50, 30));
         btnQ.setOpaque(false);
 
         btnR.setText("R");
         btnR.setMaximumSize(new java.awt.Dimension(50, 30));
         btnR.setMinimumSize(new java.awt.Dimension(50, 30));
         btnR.setOpaque(false);
 
         btnS.setText("S");
         btnS.setMaximumSize(new java.awt.Dimension(50, 30));
         btnS.setMinimumSize(new java.awt.Dimension(50, 30));
         btnS.setOpaque(false);
 
         btnT.setText("T");
         btnT.setMaximumSize(new java.awt.Dimension(50, 30));
         btnT.setMinimumSize(new java.awt.Dimension(50, 30));
         btnT.setOpaque(false);
 
         btnU.setText("U");
         btnU.setMaximumSize(new java.awt.Dimension(50, 30));
         btnU.setMinimumSize(new java.awt.Dimension(50, 30));
         btnU.setOpaque(false);
 
         btnV.setText("V");
         btnV.setMaximumSize(new java.awt.Dimension(50, 30));
         btnV.setMinimumSize(new java.awt.Dimension(50, 30));
         btnV.setOpaque(false);
 
         btnW.setText("W");
         btnW.setMaximumSize(new java.awt.Dimension(50, 30));
         btnW.setMinimumSize(new java.awt.Dimension(50, 30));
         btnW.setOpaque(false);
 
         btnX.setText("X");
         btnX.setMaximumSize(new java.awt.Dimension(50, 30));
         btnX.setMinimumSize(new java.awt.Dimension(50, 30));
         btnX.setOpaque(false);
 
         btnY.setText("Y");
         btnY.setMaximumSize(new java.awt.Dimension(50, 30));
         btnY.setMinimumSize(new java.awt.Dimension(50, 30));
         btnY.setOpaque(false);
 
         btnZ.setText("Z");
         btnZ.setMaximumSize(new java.awt.Dimension(50, 30));
         btnZ.setMinimumSize(new java.awt.Dimension(50, 30));
         btnZ.setOpaque(false);
 
         btnA.setText("A");
         btnA.setMaximumSize(new java.awt.Dimension(50, 30));
         btnA.setMinimumSize(new java.awt.Dimension(50, 30));
         btnA.setOpaque(false);
 
         btnB.setText("B");
         btnB.setMaximumSize(new java.awt.Dimension(50, 30));
         btnB.setMinimumSize(new java.awt.Dimension(50, 30));
         btnB.setOpaque(false);
 
         btnC.setText("C");
         btnC.setMaximumSize(new java.awt.Dimension(50, 30));
         btnC.setMinimumSize(new java.awt.Dimension(50, 30));
         btnC.setOpaque(false);
 
         btnD.setText("D");
         btnD.setMaximumSize(new java.awt.Dimension(50, 30));
         btnD.setMinimumSize(new java.awt.Dimension(50, 30));
         btnD.setOpaque(false);
 
         btnE.setText("E");
         btnE.setMaximumSize(new java.awt.Dimension(50, 30));
         btnE.setMinimumSize(new java.awt.Dimension(50, 30));
         btnE.setOpaque(false);
 
         btnF.setText("F");
         btnF.setMaximumSize(new java.awt.Dimension(50, 30));
         btnF.setMinimumSize(new java.awt.Dimension(50, 30));
         btnF.setOpaque(false);
 
         btnG.setText("G");
         btnG.setMaximumSize(new java.awt.Dimension(50, 30));
         btnG.setMinimumSize(new java.awt.Dimension(50, 30));
         btnG.setOpaque(false);
 
         btnH.setText("H");
         btnH.setMaximumSize(new java.awt.Dimension(50, 30));
         btnH.setMinimumSize(new java.awt.Dimension(50, 30));
         btnH.setOpaque(false);
 
         btnI.setText("I");
         btnI.setMaximumSize(new java.awt.Dimension(50, 30));
         btnI.setMinimumSize(new java.awt.Dimension(50, 30));
         btnI.setOpaque(false);
 
         javax.swing.GroupLayout panelKeyboardLayout = new javax.swing.GroupLayout(panelKeyboard);
         panelKeyboard.setLayout(panelKeyboardLayout);
         panelKeyboardLayout.setHorizontalGroup(
             panelKeyboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelKeyboardLayout.createSequentialGroup()
                 .addGroup(panelKeyboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(panelKeyboardLayout.createSequentialGroup()
                         .addContainerGap()
                         .addGroup(panelKeyboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(panelKeyboardLayout.createSequentialGroup()
                                 .addComponent(btnA, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(btnB, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(btnC, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(btnD, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(btnE, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(btnF, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(btnG, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(btnH, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(btnI, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(panelKeyboardLayout.createSequentialGroup()
                                 .addComponent(btnR, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(btnS, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(btnT, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(btnU, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(btnV, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(btnW, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(btnY, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(btnX, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(btnZ, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))))
                     .addGroup(panelKeyboardLayout.createSequentialGroup()
                         .addGap(28, 28, 28)
                         .addComponent(btnJ, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnK, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnL, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnM, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnN, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnO, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnP, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnQ, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         panelKeyboardLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnA, btnB, btnC, btnD, btnE, btnF, btnG, btnH, btnI, btnJ, btnK, btnL, btnM, btnN, btnO, btnP, btnQ, btnR, btnS, btnT, btnU, btnV, btnW, btnX, btnY, btnZ});
 
         panelKeyboardLayout.setVerticalGroup(
             panelKeyboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelKeyboardLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelKeyboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnG, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelKeyboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnJ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnK, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnO, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnQ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(panelKeyboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(panelKeyboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(btnS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(btnR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(panelKeyboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(btnT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(btnU, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(panelKeyboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(btnV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(btnW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(btnY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(btnX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(btnZ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
 
         panelKeyboardLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnA, btnB});
 
         panelGame.add(panelKeyboard);
         panelKeyboard.setBounds(55, 4, 510, 120);
 
         lblMog.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Mog_1.gif")));
         lblMog.setText("mog");
         lblMog.setMaximumSize(new java.awt.Dimension(32, 42));
         lblMog.setMinimumSize(new java.awt.Dimension(32, 42));
         panelGame.add(lblMog);
         lblMog.setBounds(160, 310, 33, 42);
 
         jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/BACKGROUND FIGHT.jpg")));
         jLabel2.setDoubleBuffered(true);
         panelGame.add(jLabel2);
         jLabel2.setBounds(0, 0, 640, 480);
 
         getContentPane().add(panelGame);
         panelGame.setBounds(0, 0, 640, 480);
 
         panelMainMenu.setMaximumSize(new java.awt.Dimension(640, 480));
         panelMainMenu.setMinimumSize(new java.awt.Dimension(640, 480));
         panelMainMenuBackground.setMinimumSize(new java.awt.Dimension(640, 480));
         jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Cubes.png")));
         jLabel5.setText("jLabel2");
         jLabel5.setMaximumSize(new java.awt.Dimension(640, 480));
         jLabel5.setMinimumSize(new java.awt.Dimension(640, 480));
 
         javax.swing.GroupLayout panelMainMenuBackgroundLayout = new javax.swing.GroupLayout(panelMainMenuBackground);
         panelMainMenuBackground.setLayout(panelMainMenuBackgroundLayout);
         panelMainMenuBackgroundLayout.setHorizontalGroup(
             panelMainMenuBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
         );
         panelMainMenuBackgroundLayout.setVerticalGroup(
             panelMainMenuBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
         );
 
         btnExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/button 1.jpg")));
         btnExit.setDoubleBuffered(true);
         btnExit.setMaximumSize(new java.awt.Dimension(140, 40));
         btnExit.setMinimumSize(new java.awt.Dimension(140, 40));
         btnExit.setPreferredSize(new java.awt.Dimension(140, 40));
         btnExit.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnExitActionPerformed(evt);
             }
         });
 
         btnStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/START BUTTON.jpg")));
         btnStart.setDoubleBuffered(true);
         btnStart.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnStartActionPerformed(evt);
             }
         });
 
         btnHighScore.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/HIGH SCORE.jpg")));
         btnHighScore.setDoubleBuffered(true);
         btnHighScore.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnHighScoreActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout panelMainMenuLayout = new javax.swing.GroupLayout(panelMainMenu);
         panelMainMenu.setLayout(panelMainMenuLayout);
         panelMainMenuLayout.setHorizontalGroup(
             panelMainMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelMainMenuLayout.createSequentialGroup()
                 .addGap(240, 240, 240)
                 .addGroup(panelMainMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(btnHighScore, javax.swing.GroupLayout.PREFERRED_SIZE, 140, Short.MAX_VALUE)
                     .addComponent(btnExit, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                     .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
             .addComponent(panelMainMenuBackground, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
         );
         panelMainMenuLayout.setVerticalGroup(
             panelMainMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainMenuLayout.createSequentialGroup()
                 .addContainerGap(256, Short.MAX_VALUE)
                 .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnHighScore, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnExit, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(92, 92, 92))
             .addComponent(panelMainMenuBackground, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
         );
 
         panelMainMenuLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnExit, btnHighScore, btnStart});
 
         getContentPane().add(panelMainMenu);
         panelMainMenu.setBounds(0, 0, 640, 480);
 
         panelHighScore.setLayout(null);
 
         panelHighScore.setMaximumSize(new java.awt.Dimension(640, 480));
         panelHighScore.setMinimumSize(new java.awt.Dimension(640, 480));
         btnFromHighScoreToMainMenu.setText("Main Menu");
         btnFromHighScoreToMainMenu.setDoubleBuffered(true);
         btnFromHighScoreToMainMenu.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnMainMenuActionPerformed(evt);
             }
         });
 
         panelHighScore.add(btnFromHighScoreToMainMenu);
         btnFromHighScoreToMainMenu.setBounds(10, 425, 110, 20);
 
         panelHighScoreBackground.setMinimumSize(new java.awt.Dimension(640, 480));
         panelHighScoreBackground.setOpaque(false);
         javax.swing.GroupLayout panelHighScoreBackgroundLayout = new javax.swing.GroupLayout(panelHighScoreBackground);
         panelHighScoreBackground.setLayout(panelHighScoreBackgroundLayout);
         panelHighScoreBackgroundLayout.setHorizontalGroup(
             panelHighScoreBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         panelHighScoreBackgroundLayout.setVerticalGroup(
             panelHighScoreBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         panelHighScore.add(panelHighScoreBackground);
         panelHighScoreBackground.setBounds(0, 0, 640, 480);
 
         lblHighScore.setFont(new java.awt.Font("Tahoma", 0, 18));
         lblHighScore.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblHighScore.setText("jLabel2");
         lblHighScore.setVerticalAlignment(javax.swing.SwingConstants.TOP);
         lblHighScore.setDoubleBuffered(true);
         lblHighScore.setMaximumSize(new java.awt.Dimension(640, 400));
         lblHighScore.setMinimumSize(new java.awt.Dimension(640, 400));
         lblHighScore.setPreferredSize(new java.awt.Dimension(640, 400));
         panelHighScore.add(lblHighScore);
         lblHighScore.setBounds(0, 60, 640, 400);
 
         jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/HIGH score background.jpg")));
         panelHighScore.add(jLabel6);
         jLabel6.setBounds(0, 0, 640, 480);
 
         getContentPane().add(panelHighScore);
         panelHighScore.setBounds(0, 0, 640, 480);
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * This is the action listener for btnHighScore
      */
     private void btnHighScoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHighScoreActionPerformed
         showHighScorePanel();
         String top10 = getTopTenScores();
         lblHighScore.setText(top10);
     }//GEN-LAST:event_btnHighScoreActionPerformed
     /**
      * This is the action listener for btnExit
      */
     private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
         System.exit(0);
     }//GEN-LAST:event_btnExitActionPerformed
     /**
      * This is the action listener for btnStart
      */
     private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
         if (continuePlaying) {
             if (JOptionPane.showOptionDialog(null, "There is an incompleted game! Do you want to continue? \n"
                     + "    (\"Yes\" to continue, \"No\" to start new game)", "Save The Moggle The Game", JOptionPane.YES_NO_OPTION,
                     JOptionPane.WARNING_MESSAGE, null, null, null)
                     == 1) {
                 restart();
             }
         } else {
             restart();
         }
         showGamePanel();
         
     }//GEN-LAST:event_btnStartActionPerformed
     /**
      * This is the action listener for btnMainMenu
      */
     private void btnMainMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMainMenuActionPerformed
 // TODO add your handling code here:
         showMainMenuPanel();
     }//GEN-LAST:event_btnMainMenuActionPerformed
     /**
      * This is the action listener for btnRestart
      */
     private void btnRestartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRestartActionPerformed
         // TODO add your handling code here:
         System.out.println("btn Restart pressed!!");
         restart();
     }//GEN-LAST:event_btnRestartActionPerformed
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 HangManForm frame = new HangManForm();
                 frame.setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnA;
     private javax.swing.JButton btnB;
     private javax.swing.JButton btnC;
     private javax.swing.JButton btnD;
     private javax.swing.JButton btnE;
     private javax.swing.JButton btnExit;
     private javax.swing.JButton btnF;
     private javax.swing.JButton btnFromHighScoreToMainMenu;
     private javax.swing.JButton btnG;
     private javax.swing.JButton btnH;
     private javax.swing.JButton btnHighScore;
     private javax.swing.JButton btnI;
     private javax.swing.JButton btnJ;
     private javax.swing.JButton btnK;
     private javax.swing.JButton btnL;
     private javax.swing.JButton btnM;
     private javax.swing.JButton btnMainMenu;
     private javax.swing.JButton btnN;
     private javax.swing.JButton btnO;
     private javax.swing.JButton btnP;
     private javax.swing.JButton btnQ;
     private javax.swing.JButton btnR;
     private javax.swing.JButton btnRestart;
     private javax.swing.JButton btnS;
     private javax.swing.JButton btnStart;
     private javax.swing.JButton btnT;
     private javax.swing.JButton btnU;
     private javax.swing.JButton btnV;
     private javax.swing.JButton btnW;
     private javax.swing.JButton btnX;
     private javax.swing.JButton btnY;
     private javax.swing.JButton btnZ;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel lblHighScore;
     private javax.swing.JLabel lblMog;
     private javax.swing.JLabel lblMsg;
     private javax.swing.JLabel lblPiranna;
     private javax.swing.JLabel lblScore;
     private javax.swing.JLabel lblSecretWord;
     private javax.swing.JLabel lblStage;
     private javax.swing.JLabel lblTries;
     private javax.swing.JLabel lblWorngLetters;
     private javax.swing.JPanel panelGame;
     private javax.swing.JPanel panelGameBackground;
     private javax.swing.JPanel panelHighScore;
     private javax.swing.JPanel panelHighScoreBackground;
     private javax.swing.JPanel panelImages;
     private javax.swing.JPanel panelKeyboard;
     private javax.swing.JPanel panelMainMenu;
     private javax.swing.JPanel panelMainMenuBackground;
     // End of variables declaration//GEN-END:variables
 }
