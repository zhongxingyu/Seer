 package Main;
 
 import Listeners.GameMenuMouseListener;
 import Settings.PlayerSettings;
 import Settings.Settings;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 import javax.imageio.ImageIO;
 ;
 import javax.swing.*;
 import javax.sound.sampled.*;
 import javax.swing.*;
 import javax.sound.sampled.*;
 
 /**
  *
  * @author
  * haavamoa
  */
 
 
 public class GameMenu extends JPanel {
 
     public PlayerSettings playerSettings = new PlayerSettings();
     public Settings settings = new Settings();
     public BufferedImage img;
     private JPanel pan = this;
     public String currentPage;
     public Clip clip;
     private JFrame frame;
     private String songName;
     public AudioInputStream ais;
     final private String scrumHINT = "<html><b style=\"color:red; font-size:1.2em\">Info:</b>I scrum jobber man iterativt i \"sprints\" med en fast lengde på 2-4 uker.</br></br>Før første sprint må man gjøre litt forarbeid som f.eks å definere problemet man skal løse.</html>";
     final private String upHINT = "<html><b style=\"color:red; font-size:1.2em\">Info:</b> UP er 'use-case drevet, arkitektur-sentralt, iterativ og inkrementell'. Det må defineres hvem som gjør hva, når det skal gjøres og hvordan man skal nå et bestemt mål.</html>";
     final private String waterfallHINT = "<html><b style=\"color:red; font-size:1.2em\">Info:</b>I fossefallsmetoden jobber man sekvensiellt, først spesifiserer man krav og dokumenterer design før programmeringen begynner.</html>";
     final private String spiralHINT = "<html><br></br><br></br><br></br><br></br><b style=\"color:red; font-size:1.2em\">Info:</b>I denne model skal du arrangere alle aktivitetene i en form av spiral:<ul><li>Bestemme målene, alternativer og begrensninger</li><li>Risikoanalyse og evaluering av alternativer</li><li>Gjennomføring av den fasen av utviklingen.Planlegging av neste fase</li></ul></html>";
 
     public GameMenu(JFrame frame) {
         try {
             img = ImageIO.read(new File("./res/img/menu.png"));
         } catch (IOException e) {
             e.printStackTrace();
         }
         setLayout(new GridBagLayout());
         setPreferredSize(new Dimension(settings.WITDH, settings.HEIGHT));
         setSize(settings.WITDH, settings.HEIGHT);
 
         this.frame = frame;
         try {
             clip = AudioSystem.getClip();
             startMusic();
         } catch (Exception e) {
             e.printStackTrace();
         }
         startMenuSetup();
     }
 
     public void setButtonSetup(final JButton button) {
         button.setOpaque(false);
         button.setContentAreaFilled(false);
         button.setBorderPainted(false);
         button.setFocusPainted(false);
         button.setBorder(null);
         GameMenuMouseListener gamemenulistener = new GameMenuMouseListener(button, this, frame);
         button.addMouseListener(gamemenulistener);
     }
 
     public void startMenuSetup() {
         currentPage = "startMenu";
         final GridBagConstraints c = new GridBagConstraints();
         JButton button;
 
         button = new JButton();
         button.setToolTipText("Start Spill");
         button.setName("startGame");
         setButtonSetup(button);
         button.setIcon(new ImageIcon("./res/img/Startspill.png"));
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(100, 0, 0, 0);
         c.gridx = 0;
         c.gridy = 1;
         add(button, c);
 
         button = new JButton();
         button.setToolTipText("Last Spill");
         button.setName("loadGame");
         setButtonSetup(button);
         button.setIcon(new ImageIcon("./res/img/Lastspill.png"));
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(100, 0, 0, 100);
         c.gridx = 1;
         c.gridy = 1;
         add(button, c);
 
         button = new JButton();
         button.setToolTipText("Avslutt Spill");
         button.setName("exitGame");
         setButtonSetup(button);
         button.setIcon(new ImageIcon("./res/img/Avsluttspill.png"));
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(100, 0, 0, 120);
         c.gridx = 2;
         c.gridy = 1;
         add(button, c);
 
         button = new JButton();
         button.setToolTipText("Innstillinger");
         button.setName("settings");
         setButtonSetup(button);
         button.setIcon(new ImageIcon("./res/img/Innstillinger.png"));
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(400, 80, 0, 100);
         c.gridx = 0;
         c.gridy = 1;
         add(button, c);
 
         if (settings.sound) {
             JLabel musicLabel = new JLabel("Now playing: " + songName);
             musicLabel.setForeground(Color.white);
             c.fill = GridBagConstraints.HORIZONTAL;
             c.insets = new Insets(400, 0, 0, 0);
             c.gridx = 1;
             c.gridy = 1;
             add(musicLabel, c);
         }
 
     }
 
     public void nameSetup() {
         currentPage = "name";
         final GridBagConstraints c = new GridBagConstraints();
         JButton button;
         button = new JButton();
         button.setToolTipText("Tilbake til Hovedmenyen");
         button.setName("previous");
         setButtonSetup(button);
         button.setIcon(new ImageIcon("./res/img/tilbake.png"));
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(0, 110, 0, 110);
         c.gridx = 0;
         c.gridy = 1;
         add(button, c);
 
         final JButton button2 = new JButton();
         button2.setToolTipText("Gå til Systemtype valg");
         button2.setName("next");
         if (playerSettings.getPlayerName() == null || playerSettings.getPlayerName().equals("")) {
             button2.setEnabled(false);
             button2.setOpaque(false);
             button2.setContentAreaFilled(false);
             button2.setBorderPainted(false);
             button2.setBorder(null);
         } else {
             setButtonSetup(button2);
         }
         button2.setIcon(new ImageIcon("./res/img/frem.png"));
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(0, 110, 0, 110);
         c.gridx = 2;
         c.gridy = 1;
 
         add(button2, c);
 
         final JLabel playerName = new JLabel();
         if (playerSettings.getPlayerName() == null) {
             playerName.setText("Skriv inn navnet ditt!");
         } else {
             playerName.setText(playerSettings.getPlayerName());
         }
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(0, 0, 0, 0);
         c.gridx = 1;
         c.gridy = 2;
         add(playerName, c);
 
         final JLabel playerPic = new JLabel();
         if (playerSettings.getPlayerName() == null) {
             playerPic.setIcon(new ImageIcon("./res/player/playeridledown.png"));
         } else {
             playerPic.setIcon(new ImageIcon("./res/img/os.gif"));
         }
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(0, 0, 0, 0);
         c.gridx = 1;
         c.gridy = 3;
         add(playerPic, c);
 
         final JTextField name = new JTextField((playerSettings.getPlayerName() == null) ? "Skriv inn navnet ditt her" : playerSettings.getPlayerName());
         name.setToolTipText("Skriv inn navnet ditt");
         name.setName("navn");
         name.setMinimumSize(new Dimension(200, 20));
         name.addFocusListener(new FocusListener() {
             @Override
             public void focusGained(FocusEvent e) {
                 name.selectAll();
             }
 
             @Override
             public void focusLost(FocusEvent e) {
             }
         });
         name.addKeyListener(new KeyListener() {
             @Override
             public void keyTyped(KeyEvent e) {
             }
 
             @Override
             public void keyPressed(KeyEvent e) {
             }
 
             @Override
             public void keyReleased(KeyEvent e) {
                 if (!name.getText().equals("")) {
                     button2.setEnabled(true);
                     setButtonSetup(button2);
                     playerSettings.setPlayerName(name.getText());
                     playerPic.setIcon(new ImageIcon("./res/img/os.gif"));
                     playerName.setText(playerSettings.getPlayerName());
                     int picCenter = (playerSettings.getPlayerName().length() * 8) / 4;
                     c.fill = GridBagConstraints.HORIZONTAL;
                     c.insets = new Insets(0, picCenter, 0, picCenter);
                     c.gridx = 1;
                     c.gridy = 3;
                     remove(playerPic);
                     add(playerPic, c);
                     revalidate();
                     repaint();
                 } else {
                     playerName.setText("Skriv inn navnet ditt!");
                     button2.setEnabled(false);
                     playerPic.setIcon(new ImageIcon("./res/player/playeridledown.png"));
                 }
             }
         });
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(0, 0, 0, 0);
         c.gridx = 1;
         c.gridy = 1;
 
         add(name, c);
 
 
 
     }
 
     public void settingsSetup() {
         currentPage = "settings";
         GridBagConstraints c = new GridBagConstraints();
         JButton button;
 
         button = new JButton();
         button.setToolTipText("Lyd");
         button.setName("Lyd");
         setButtonSetup(button);
         button.setIcon((settings.sound) ? new ImageIcon("./res/img/speakeron.png") : new ImageIcon("./res/img/speakermute.png"));
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(0, 60, 0, 220);
         c.gridx = 1;
         c.gridy = 1;
         add(button, c);
 
         button = new JButton();
         button.setToolTipText("Tilbake til hovedmenyen");
         button.setName("previous");
         setButtonSetup(button);
         button.setIcon(new ImageIcon("./res/img/tilbake.png"));
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(0, 20, 0, 40);
         c.gridx = 0;
         c.gridy = 1;
         add(button, c);
     }
 
     public void systemTypeSetup() {
         currentPage = "systemType";
         GridBagConstraints c = new GridBagConstraints();
         JButton button;
 
         button = new JButton();
         button.setToolTipText("Tilbake til navn valg");
         button.setName("previous");
         setButtonSetup(button);
         button.setIcon(new ImageIcon("./res/img/tilbake.png"));
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(0, 100, 0, 100);
         c.gridx = 0;
         c.gridy = 1;
         add(button, c);
 
         DefaultComboBoxModel model = new DefaultComboBoxModel();
         model.addElement("Spillsystem");
         model.addElement("Sikkerhetskritiskesystem");
         model.addElement("Sanntidssystem");
         model.addElement("Informasjonssystem");
         model.addElement("Telekommunikasjonssystem");
         model.addElement("Feiltolerantsystem");
         if (playerSettings.system == null) {
             playerSettings.setSystem(model.getElementAt(0).toString());
         }
         final JComboBox comboBox = new JComboBox(model);
         comboBox.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 String system = comboBox.getSelectedItem().toString();
                 playerSettings.setSystem(system);
             }
         });
         if (playerSettings.getSystem() != null) {
             int i = model.getIndexOf(playerSettings.getSystem());
             comboBox.setSelectedIndex(i);
         }
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(0, 0, 0, 0);
         c.gridx = 1;
         c.gridy = 1;
         add(comboBox, c);
 
         button = new JButton();
         button.setToolTipText("Gå til Utviklingsmodell valg");
         button.setName("next");
         setButtonSetup(button);
         button.setIcon(new ImageIcon("./res/img/frem.png"));
 
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(0, 95, 0, 95);
         c.gridx = 2;
         c.gridy = 1;
 
         add(button, c);
     }
 
     public void devMethodSetup() {
         currentPage = "devmethod";
 
         GridBagConstraints c = new GridBagConstraints();
         JButton button;
 
         button = new JButton();
         button.setToolTipText("Tilbake til systemtype valg");
         button.setName("previous");
         setButtonSetup(button);
         button.setIcon(new ImageIcon("./res/img/tilbake.png"));
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(0, 110, 0, 70);
         c.gridx = 0;
         c.gridy = 1;
         add(button, c);
 
         final JLabel devmethodPic = new JLabel();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(-130, 50, 0, 28);
         c.gridx = 1;
         c.gridy = 1;
         add(devmethodPic, c);
 
         final JLabel label = new JLabel();
         label.setMinimumSize(new Dimension(280, 300));
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(130, 0, 0, 0);
         c.gridx = 1;
         c.gridy = 1;
         add(label, c);
 
         DefaultComboBoxModel model = new DefaultComboBoxModel();
         model.addElement("SCRUM");
         model.addElement("Fossefallsmetoden");
         model.addElement("Spiralmetoden");
         model.addElement("Unified Processing");
         if (playerSettings.getDevMethod() == null) {
             playerSettings.setDevMethod(model.getElementAt(0).toString());
         }
 
 
         final JComboBox comboBox = new JComboBox(model);
         comboBox.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 String devMethod = comboBox.getSelectedItem().toString();
                 playerSettings.setDevMethod(devMethod);
                 if (devMethod.equalsIgnoreCase("SCRUM")) {
                     label.setText(scrumHINT);
                     devmethodPic.setIcon(new ImageIcon("./res/img/scrum.png"));
                 } else if (devMethod.equalsIgnoreCase("Fossefallsmetoden")) {
                     label.setText(waterfallHINT);
                     devmethodPic.setIcon(new ImageIcon("./res/img/fossefall.png"));
                 } else if (devMethod.equalsIgnoreCase("Spiralmetoden")) {
                     label.setText(spiralHINT);
                     devmethodPic.setIcon(new ImageIcon("./res/img/spiral.png"));
                 } else if (devMethod.equalsIgnoreCase("Unified Processing")) {
                     label.setText(upHINT);
                     devmethodPic.setIcon(new ImageIcon("./res/img/up.png"));
                 }
 
             }
         });
         if (playerSettings.getDevMethod() != null) {
             int i = model.getIndexOf(playerSettings.getDevMethod());
             comboBox.setSelectedIndex(i);
         }
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(0, 0, 0, 0);
         c.gridx = 1;
         c.gridy = 1;
         add(comboBox, c);
 
         button = new JButton();
         button.setToolTipText("Gå til vannskelighetsgrad valg");
         button.setName("next");
         setButtonSetup(button);
         button.setIcon(new ImageIcon("./res/img/frem.png"));
 
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(0, 70, 0, 105);
         c.gridx = 2;
         c.gridy = 1;
 
         add(button, c);
     }
 
     public void difficulitySetup() {
         currentPage = "difficulity";
         GridBagConstraints c = new GridBagConstraints();
         JButton button;
 
         button = new JButton();
         button.setToolTipText("Tilbake til Utviklingsmodell valg");
         button.setName("previous");
         setButtonSetup(button);
         button.setIcon(new ImageIcon("./res/img/tilbake.png"));
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(0, 170, 0, 160);
         c.gridx = 0;
         c.gridy = 1;
         add(button, c);
 
         DefaultComboBoxModel model = new DefaultComboBoxModel();
         model.addElement("Enkelt");
         model.addElement("Normalt");
         model.addElement("Vanskelig");
         if (playerSettings.difficulity == -1) {
             playerSettings.setDifficulity(0);
         }
         final JComboBox comboBox = new JComboBox(model);
         comboBox.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 String difficulity = comboBox.getSelectedItem().toString();
                 if (difficulity.equalsIgnoreCase("enkelt")) {
                     playerSettings.setDifficulity(PlayerSettings.EASY);
                 } else if (difficulity.equalsIgnoreCase("normalt")) {
                     playerSettings.setDifficulity(PlayerSettings.NORMAL);
                 } else if (difficulity.equalsIgnoreCase("vanskelig")) {
                     playerSettings.setDifficulity(PlayerSettings.HARD);
                 }
             }
         });
         if (playerSettings.difficulity != -1) {
             comboBox.setSelectedIndex(playerSettings.difficulity);
         }
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(0, 0, 0, 0);
         c.gridx = 1;
         c.gridy = 1;
         add(comboBox, c);
 
         button = new JButton();
         button.setToolTipText("Gå til Informasjonsskjerm");
         button.setName("next");
         setButtonSetup(button);
         button.setIcon(new ImageIcon("./res/img/frem.png"));
 
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(0, 170, 0, 160);
         c.gridx = 2;
         c.gridy = 1;
 
         add(button, c);
     }
 
     public void chosenSettingsSetup() {
         currentPage = "chosenSettings";
         JButton button;
         GridBagConstraints c = new GridBagConstraints();
 
         button = new JButton();
         button.setToolTipText("Tilbake til navn valg");
         button.setName("previous");
         setButtonSetup(button);
         button.setIcon(new ImageIcon("./res/img/tilbake.png"));
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(0, -300, 0, 0);
         c.gridx = 0;
         c.gridy = 4;
         add(button, c);
 
 
         JLabel name = new JLabel((playerSettings.getPlayerName() == null) ? "Skriv inn navnet ditt her" : playerSettings.getPlayerName());
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(120, 40, 0, 0);
         c.gridx = 1;
         c.gridy = 2;
         add(name, c);
 
         JLabel system = new JLabel(playerSettings.getSystem());
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(30, 40, 0, 0);
         c.gridx = 1;
         c.gridy = 3;
         add(system, c);
 
         JLabel devMethod = new JLabel(playerSettings.getDevMethod());
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(35, 40, 0, 0);
         c.gridx = 1;
         c.gridy = 4;
         add(devMethod, c);
 
         String dif = "";
         if (playerSettings.difficulity == PlayerSettings.EASY) {
             dif = "Enkelt";
         } else if (playerSettings.difficulity == PlayerSettings.NORMAL) {
             dif = "Normal";
         } else if (playerSettings.difficulity == PlayerSettings.HARD) {
             dif = "Hard";
         }
         JLabel difficulity = new JLabel(dif);
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(35, 40, 0, 0);
         c.gridx = 1;;
         c.gridy = 5;
         add(difficulity, c);
 
         button = new JButton();
         button.setToolTipText("Start spillet");
         button.setName("startGame");
         setButtonSetup(button);
         button.setIcon(new ImageIcon("./res/img/startgold01.gif"));
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(20, 0, 0, 0);
         c.gridx = 1;
         c.gridy = 6;
         add(button, c);
 
         JLabel wavingMan = new JLabel();
         wavingMan.setToolTipText("Hei der " + playerSettings.getPlayerName() + ", er du klar for en utrolig reise?");
         wavingMan.setName("wavingMan");
         wavingMan.setIcon(new ImageIcon("./res/img/os.gif"));
         c.fill = GridBagConstraints.HORIZONTAL;
         c.insets = new Insets(0, 200, 0, 0);
         c.gridx = 1;
         c.gridy = 5;
         add(wavingMan, c);
 
     }
 
     public void startMusic() throws Exception {
         ArrayList<String> musicFiles = new ArrayList();
         musicFiles.add("wakemeup.mid");
         musicFiles.add("mario1.mid");
         musicFiles.add("pokemon1.mid");
         musicFiles.add("pokemon2.mid");
         musicFiles.add("pokemon3.mid");
         musicFiles.add("pokemon4.mid");
         musicFiles.add("zelda1.mid");
         musicFiles.add("aoe2.mid");
         musicFiles.add("tetris1.mid");
         musicFiles.add("sandstorm.mid");
         musicFiles.add("itsmylife.mid");
         musicFiles.add("jurassicpark.mid");
         musicFiles.add("jamesbond.mid");
         Random rn = new Random();
         int songnr = rn.nextInt(13);
         String song = musicFiles.get(songnr);
 
         //Description of songs
         if (song.contains("aoe")) {
             songName = "Theme from Age of Empires";
         } else if (song.contains("pokemon")) {
             songName = "Theme from Pokemon";
         } else if (song.contains("mario")) {
             songName = "Theme from Super Mario";
         } else if (song.contains("zelda")) {
             songName = "Theme from Zelda";
         } else if (song.contains("sandstorm")) {
             songName = "Darude - Sandstorm";
         } else if (song.contains("tetris")) {
             songName = "Theme from Tetris";
         } else if (song.contains("wakemeup")) {
             songName = "Avicii - Wake me up";
         } else if (song.contains("itsmylife")) {
             songName = "Bon Jovi - Its my life";
         } else if (song.contains("jurassicpark.mid")) {
             songName = "Theme from Jurassic Park";
         } else if (song.contains("jamesbond")) {
             songName = "Theme from James Bond";
         }
         File file = new File("./res/music/" + song);
         // getAudioInputStream() also accepts a File or InputStream
         ais = AudioSystem.
                 getAudioInputStream(file);
         clip.open(ais);
         clip.loop(Clip.LOOP_CONTINUOUSLY);
 
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         // paint the background image and scale it to fill the entire space
         g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
     }
 
     public BufferedImage getImg() {
         return img;
     }
 
     public void setImg(BufferedImage img) {
         this.img = img;
     }
 }
