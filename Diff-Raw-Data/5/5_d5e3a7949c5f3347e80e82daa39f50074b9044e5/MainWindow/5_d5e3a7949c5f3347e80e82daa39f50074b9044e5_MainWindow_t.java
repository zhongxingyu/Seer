 package mvc;
 
 import java.awt.Dimension;
 import java.awt.event.ActionListener;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.WindowConstants;
 
 public class MainWindow extends JFrame {
 
   private static final long serialVersionUID = -7219620606214091173L;
     private JMenuBar MenuBar;
     private JMenu MenuFile;
     private JMenu MenuEdit;
     private JMenuItem ItemQuit;
     private JMenuItem ItemImport;
     private JMenuItem ItemDelete;
     private JMenuItem ItemAdvance;
     private JMenuItem ItemRetreat;
 
     private JSplitPane splitPane;
     private JScrollPane scrollImagePane;
     private JPanel imagePanel;
     private JList listPane;
     private JScrollPane scrollListPane;
 
     /** Creates new form NewJFrame2 */
     public MainWindow() {
         initComponents();
     }
 
     private void initComponents() {
       // Dimensions de la fenetre principale, du paneau des images et de la liste
         Dimension mainWDim = new Dimension(800, 800);
         Dimension imagesWDim = new Dimension(1200, 1200);
         Dimension listWDim = new Dimension(300, mainWDim.height - 150);
 
         // taille de la fenetre principale
         this.setPreferredSize(mainWDim);
         this.setSize(mainWDim);
         this.setMinimumSize(mainWDim);
         this.setResizable(false);
 
         // Fenetre des images
         this.imagePanel = new JPanel();
         this.imagePanel.setLayout(null);
         this.imagePanel.setPreferredSize(imagesWDim);
         this.imagePanel.setSize(imagesWDim);
         this.imagePanel.setMinimumSize(imagesWDim);
 
         // Fenetre de la liste
         this.listPane = new JList();
         this.listPane.setLayout(null);
         this.listPane.setPreferredSize(listWDim);
         this.listPane.setSize(listWDim);
         this.listPane.setMinimumSize(listWDim);
 
         // Fenetre de scroll image (plus petite que celle des images)
         this.scrollImagePane = new JScrollPane(this.imagePanel);
 
         // Fenetre de scroll list (moins large que la JList)
         this.scrollListPane = new JScrollPane(this.listPane);
 
         // fenetre de split
         this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
             this.scrollImagePane ,  this.scrollListPane);
 
         this.splitPane.setDividerLocation(mainWDim.width * 3 / 4);
 
         // Remplissage de la fenetre principale
         this.setContentPane(splitPane);
 
        MenuBar = new JMenuBar();
        MenuFile = new JMenu();
        MenuEdit = new JMenu();
         ItemQuit = new JMenuItem();
         ItemImport = new JMenuItem();
         ItemDelete = new JMenuItem();
         ItemAdvance= new JMenuItem();
         ItemRetreat = new JMenuItem();
 
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 
         MenuFile.setText("Fichier");
         ItemImport.setText("Importer");
         MenuFile.add(ItemImport);
         ItemQuit.setText("Quitter");
         MenuFile.add(ItemQuit);
         MenuBar.add(MenuFile);
 
         MenuEdit.setText("Edition");
         ItemDelete.setText("Supprimer");
         MenuEdit.add(ItemDelete);
         ItemAdvance.setText("Avancer");
         MenuEdit.add(ItemAdvance);
         ItemRetreat.setText("Reculer");
         MenuEdit.add(ItemRetreat);
         MenuBar.add(MenuEdit);
 
         setJMenuBar(MenuBar);
 
         this.setTitle("Images");
         pack();
     }
 
     public void addItemQuitListener(ActionListener actLst) {
       ItemQuit.addActionListener(actLst);
   }
 
     public void addItemImportListener(ActionListener actLst) {
       ItemImport.addActionListener(actLst);
   }
 
     public void addItemDeleteListener(ActionListener actLst) {
       ItemDelete.addActionListener(actLst);
   }
 
     public void addItemAdvanceListener(ActionListener actLst) {
       ItemAdvance.addActionListener(actLst);
   }
 
     public void addItemRetreatListener(ActionListener actLst) {
       ItemRetreat.addActionListener(actLst);
   }
 
     public JPanel getPanel() {
       return this.imagePanel;
     }
 
     public JList getImageList() {
       return this.listPane;
     }
 }
