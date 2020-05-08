 package GUI;
 
 import messaging.Song;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 
 public class PlaySongScreen extends JPanel {
     private JPanel titlePanel;
     private JPanel choosePanel;
     private JPanel playPanel;
     private JPanel backPanel;
 
     private Song song = null;
 
     public PlaySongScreen(final CardLayout cl, final JPanel mainPanel) {
         // Set the size, color and layout of this panel
         this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
         this.setSize(new Dimension(1000, 800));
         this.setBackground(Color.WHITE);
 
         // Panel for Title
         titlePanel = new JPanel();
         titlePanel.setSize(new Dimension(1000, 200));
         titlePanel.setBackground(Color.WHITE);
 
         // Add image to the image panel
         JLabel dxlogoLabel = new JLabel();
         dxlogoLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
 //        dxlogoLabel.setIcon(new ImageIcon("Resources/images/playsongtitle.jpg"));
         titlePanel.add(dxlogoLabel);
 
         // Panel for choosing file
         choosePanel = new JPanel();
         choosePanel.setSize(new Dimension(1000, 50));
         choosePanel.setBackground(Color.WHITE);
 
         // Text Field to show file location
         final JTextField filelocationTextField = new JTextField();
         filelocationTextField.setEditable(false);
         filelocationTextField.setPreferredSize(new Dimension(300, 25));
         filelocationTextField.setText("No Song Selected...");
         choosePanel.add(filelocationTextField);
 
 
         // Button to open a File Chooser
         JButton opensongButton = new JButton();
         opensongButton.setText("Choose Song");
         opensongButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 JFileChooser fc = new JFileChooser();
                 FileNameExtensionFilter songFilter = new FileNameExtensionFilter("Song File","song");
                 fc.setFileFilter(songFilter);
                 int rVal = fc.showOpenDialog(PlaySongScreen.this);
                 if (rVal == JFileChooser.APPROVE_OPTION) {
                     File selected = fc.getSelectedFile();
                     filelocationTextField.setText(selected.getPath());
                 }
                 try {
                     File file = new File(filelocationTextField.getText());
                     String contents = readFile(file);
                     song = new Song();
                     song.intializeFromSerializedString(contents);
                 }
                 catch(Exception e) {
                     // read file failed.
                 }
             }
 
         });
         choosePanel.add(opensongButton);
 
         // Panel to hold the Play Button
         playPanel = new JPanel();
         playPanel.setSize(new Dimension(1000, 200));
         playPanel.setBackground(Color.WHITE);
 
         final VisualPlayBackPanel visualizer = new VisualPlayBackPanel(1000,50);
 
 
         // Play Button
        JButton playButton = new JButton(new ImageIcon("Resources/images/play.jpg"));
         playButton.setBackground(Color.WHITE);
         playButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
         playButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (song == null) {
                     filelocationTextField.setText("Please Choose a Song...");
                 } else {
                     if(!song.isPlaying()) {
                         visualizer.loadSong(song);
                     }
                     if(song.play()) {
                         visualizer.play();
                     }
                 }
             }
         });
         playPanel.add(playButton);
 
         // pause button
        JButton pauseButton = new JButton(new ImageIcon("Resources/images/playbutton.png"));
         pauseButton.setBackground(Color.WHITE);
         pauseButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
         pauseButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 // TODO pause button action
                 if(song != null) {
                     song.pause();
                     visualizer.stop();
                 }
             }
         });
         playPanel.add(pauseButton);
 
         // stop button
         JButton stopButton = new JButton(new ImageIcon("Resources/images/stopbutton.png"));
         stopButton.setBackground(Color.WHITE);
         stopButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
         stopButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 // TODO stop button action
                 if(song != null) {
                     song.stop();
                     visualizer.stop();
                 }
             }
         });
         playPanel.add(stopButton);
 
 
         // Panel to hold the Back Button
         backPanel = new JPanel();
         backPanel.setSize(new Dimension(1000, 50));
         backPanel.setBackground(Color.WHITE);
 
         // Back button
         JButton backButton = new JButton();
         backButton.setText("Back");
         backButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
         backButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 visualizer.stop();
                 cl.show(mainPanel, "splash");
             }
         });
         backPanel.add(backButton);
 
         // Add all of the panels
         this.add(titlePanel);
         this.add(choosePanel);
         this.add(playPanel);
         this.add(backPanel);
         this.add(visualizer);
 
     }
 
     private String readFile(File file) throws IOException {
         String path = file.getPath();
         FileInputStream stream = new FileInputStream(new File(path));
         try {
             FileChannel fc = stream.getChannel();
             MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
             return Charset.defaultCharset().decode(bb).toString();
         } finally {
             stream.close();
         }
     }
 
 }
