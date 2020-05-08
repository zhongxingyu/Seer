 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package userinterface;
 
 import checkboxtree.TreeCheckingModel;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.Box.Filler;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.CompoundBorder;
 import javax.swing.tree.TreePath;
 import model.MarkedUpText;
 import model.TagInstance;
 import model.TagType;
 import model.TextSection;
 
 /**
  *
  * @author Brittany Nkounkou
  */
 public class SearchView extends View {
 
     private JPanel north;
     private JScrollPane center;
     
     private MainFrame mainFrame;
     private TreeCheckingModel filesModel;
     private TreeCheckingModel tagsModel;
     private JButton search;
     private List<Entry<MarkedUpText, TagInstance>> searchResults;
     private JPanel results;
 
     public SearchView(MainFrame mf, String t, TreeCheckingModel fm, TreeCheckingModel tm) {
         super(t);
         mainFrame = mf;
         filesModel = fm;
         tagsModel = tm;
         searchResults = new ArrayList<Entry<MarkedUpText, TagInstance>>();
         initialize();
     }
 
     private void initialize() {
         north = new JPanel();
         
         add(north, BorderLayout.PAGE_START);
         north.setBackground(new Color(250, 250, 250));
         north.setLayout(new BoxLayout(north, BoxLayout.X_AXIS));
         north.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(1,1,1,1,Color.DARK_GRAY),
                 BorderFactory.createEmptyBorder(5, 5, 5, 5)));
         
         center = new JScrollPane();
         center.setBackground(Color.WHITE);
         center.setBorder(BorderFactory.createEmptyBorder());    
         add(center, BorderLayout.CENTER);
 
         search = new JButton("Search");
         search.setAlignmentY((float) 0.0);
         north.add(search);
 
         search.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 performSearch();
             }
         });
 
         results = new JPanel();
         results.setLayout(new BoxLayout(results, BoxLayout.Y_AXIS));
         results.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
         
         center.setViewportView(results);
     }
 
     private void performSearch() {
         searchResults.retainAll(new ArrayList<TagType>());
 
         TreePath[] checkedTags = tagsModel.getCheckingPaths();
         List<TagType> tagsToSearch = new ArrayList<TagType>(checkedTags.length);
         for (int i = 0; i < checkedTags.length; i++) {
             tagsToSearch.add((TagType) checkedTags[i].getLastPathComponent());
         }
 
         TreePath[] checkedFiles = filesModel.getCheckingPaths();
         for (int i = 0; i < checkedFiles.length; i++) {
             Object o = checkedFiles[i].getLastPathComponent();
             if (o instanceof MarkedUpText) {
                 MarkedUpText mut = (MarkedUpText) o;
                 List<TagInstance> tags = mut.searchTags(tagsToSearch);
                 for (int j = 0; j < tags.size(); j++) {
                     searchResults.add(new AbstractMap.SimpleEntry<MarkedUpText, TagInstance>(mut, tags.get(j)));
                 }
             }
         }
 
         showResults();
     }
 
     private void showResults() {
         results.removeAll();
         Filler filler = new Box.Filler(new Dimension(10, 10), new Dimension(10, 10), new Dimension(10, 10));
         filler.setAlignmentX((float)0.0);
         results.add(filler);
         
         for (int i = 0; i < searchResults.size(); i++) {
             Entry<MarkedUpText, TagInstance> emt = searchResults.get(i);
             MarkedUpText mut = emt.getKey();
             TagInstance ti = emt.getValue();
             
             String tagName = ti.getTagType().getPathString();
             String taggedText = getTaggedText(mut, ti.getTextSection());
             String filename = mut.getName();
             
             results.add(new SearchResultPanel(mainFrame, mut, tagName, taggedText, filename));
             filler = new Box.Filler(new Dimension(10, 10), new Dimension(10, 10), new Dimension(10, 10));
             filler.setAlignmentX((float)0.0);
             results.add(filler);
         }
         center.setViewportView(results);
     }
     
     private String getTaggedText(MarkedUpText markedUpText, TextSection textSection) {
         int offset = textSection.getOffset();
         int length = textSection.getLength();
         int start = offset;
         int end = offset + length; 
         
         String text = markedUpText.getSourceText().getText();
         int lastNewLine = text.indexOf('\n', 0);
         
         while (lastNewLine > -1 && lastNewLine < end) {
             if (lastNewLine < start) {
                 start++;
                 offset++;
                 end++;
             }
             else if (lastNewLine == start) {
                 start++;
                 offset += 2;
                 length += 2;
                 end++;
             }
             else {
                 length++;
                 end++;
             }
             lastNewLine = text.indexOf('\n', lastNewLine+1);
         }
 
         return text.substring(offset, offset+length);
     }
 
     public TreeCheckingModel getFilesModel() {
         return filesModel;
     }
 
     public TreeCheckingModel getTagsModel() {
         return tagsModel;
     }
 }
