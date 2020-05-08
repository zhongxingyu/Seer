 package ru.spbau.bioinf.tagfinder.ui;
 
 import java.util.Map;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import ru.spbau.bioinf.tagfinder.Configuration;
 import ru.spbau.bioinf.tagfinder.Protein;
 
 import java.util.List;
 
 public class TagFinder extends JFrame {
 
     private Configuration conf;
     private List<Protein> proteins;
 
 
     public TagFinder(String[] args) throws Exception {
         super("TagFinder");
         conf = new Configuration(args);
         proteins = conf.getProteins();
         Map<Integer,Integer> msAlignResults = conf.getMSAlignResults();
         JTabbedPane tabs = new JTabbedPane();
         JPanel scanPanel = new ScanPanel(conf, conf.getScans(), proteins, msAlignResults);
         tabs.addTab("Scan", scanPanel);
        JPanel proteinPanel = new ProteinPanel(proteins);
        tabs.addTab("Protein", proteinPanel);
         this.getContentPane().add(tabs);
     }
 
     public static void main(String[] args) throws Exception {
         TagFinder frame = new TagFinder(args);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(1000, 500);
         frame.setVisible(true);
     }
 
 }
