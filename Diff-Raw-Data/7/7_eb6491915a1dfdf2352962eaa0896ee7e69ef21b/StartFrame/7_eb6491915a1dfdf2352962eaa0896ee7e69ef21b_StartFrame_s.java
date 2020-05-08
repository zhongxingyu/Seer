 package ch.eiafr.mafiaspace;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.*;
 
 public class StartFrame extends JFrame implements ActionListener {
     
    private static String WORLDS_DIRECTORY = "levels";
     
     private JLabel       lbTitle     = new JLabel("MafiaSpaceLife");
     private JLabel       lbWelcome   = new JLabel("Welcome to MafiaSpaceLife!"); 
     private JLabel       lbChoose    = new JLabel("Choose a world and the way you want to launch it (graphical or console UI).");
     private JList        listWorlds  = new JList(getWorldFiles());
     private JLabel       lbWorlds    = new JLabel("Worlds:");
     private JScrollPane  scrollList  = new JScrollPane(listWorlds);
     private JLabel       lbUI        = new JLabel("User interface:");
     private ButtonGroup  buttons     = new ButtonGroup();
     private JRadioButton btConsole   = new JRadioButton("Console UI", true);
     private JRadioButton btGraphical = new JRadioButton("Graphical UI");
     private JButton      btLaunch    = new JButton("Launch");
     
     public StartFrame() {
         setSize(500, 320);
         setTitle("MafiaSpaceLife");
         setLocationRelativeTo(null);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         buildUI();
         setVisible(true);
         setResizable(false);
     }
     
     private void buildUI() {
         lbTitle.setFont(lbTitle.getFont().deriveFont(20.f));
         buttons.add(btConsole);
         buttons.add(btGraphical);
         btLaunch.addActionListener(this);
         
         setLayout(new GridBagLayout());
         
         GridBagConstraints gbc = GridBagHelper.createGDC();
         gbc.fill = GridBagConstraints.NONE;
         GridBagHelper.modifyGDC(gbc, 3, 1, 1, 1, 0, 0);
         gbc.anchor = GridBagConstraints.CENTER;
         add(lbTitle, gbc);
         
         GridBagHelper.modifyGDC(gbc, 3, 1, 1, 1, 0, 1);
         gbc.fill = GridBagConstraints.BOTH;
         gbc.anchor = GridBagConstraints.WEST;
         add(lbWelcome, gbc);
         
         GridBagHelper.modifyGDC(gbc, 3, 1, 1, 1, 0, 2);
         add(lbChoose, gbc);
         
         GridBagHelper.modifyGDC(gbc, 1, 1, 1, 1, 0, 3);
         add(lbWorlds, gbc);
         
         listWorlds.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         GridBagHelper.modifyGDC(gbc, 2, 1, 1, 1, 1, 3);
         add(scrollList, gbc);
         
         GridBagHelper.modifyGDC(gbc, 1, 1, 1, 1, 0, 4);
         add(lbUI, gbc);
         
         GridBagHelper.modifyGDC(gbc, 1, 1, 1, 1, 1, 4);
         add(btConsole, gbc);
         GridBagHelper.modifyGDC(gbc, 1, 1, 1, 1, 2, 4);
         add(btGraphical, gbc);
         
         GridBagHelper.modifyGDC(gbc, 3, 1, 1, 1, 0, 5);
         add(btLaunch, gbc);
     }
     
     @Override
     public void actionPerformed(ActionEvent event) {
         WorldManagerFactory managerFactory = new WorldManagerFactory();
         ReaderFactory       readerFactory  = new ReaderFactory();
         
         Object[] values = listWorlds.getSelectedValues();
         
         if(values.length == 0) {
             JOptionPane.showMessageDialog(this, "Please select an available world", "No world selected", JOptionPane.WARNING_MESSAGE);
             return;
         }
         
         // Create the world and the world manager from the world file
         final Reader       worldReader  = readerFactory.createReader(WORLDS_DIRECTORY + "/" + values[0]);
         final World        world        = worldReader.readWorld(WORLDS_DIRECTORY + "/" + values[0]);
         final WorldManager worldManager = managerFactory.createWorldManager(world.getType());
         
         worldManager.setWorld(world);
         
         // Launch the user interface
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 if(btConsole.isSelected())
                     new ConsoleUI(worldManager);
                 else if(btGraphical.isSelected())
                     new GraphicUI(worldManager);
             }
         });
         
         dispose();
     }
     
     private String[] getWorldFiles() {
         File directory = new File(WORLDS_DIRECTORY);
         File[] files = directory.listFiles();
         String[] filenames = new String[files.length];
         
         for(int i = 0; i < filenames.length; i++)
             filenames[i] = files[i].getName();
         
         return filenames;
     }
 }
