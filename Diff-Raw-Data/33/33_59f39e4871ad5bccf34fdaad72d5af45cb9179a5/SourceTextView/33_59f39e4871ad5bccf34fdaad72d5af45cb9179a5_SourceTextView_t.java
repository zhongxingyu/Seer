 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package userinterface;
 
 import checkboxtree.TreeCheckingEvent;
 import checkboxtree.TreeCheckingListener;
 import checkboxtree.TreeCheckingModel;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextPane;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Style;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyleContext;
 import javax.swing.text.StyledDocument;
 import javax.swing.tree.TreePath;
 import model.Comment;
 import model.MarkedUpText;
 import model.TagInstance;
 import model.TagType;
 import model.TextSection;
 
 /**
  *
  * @author Brittany Nkounkou
  */
 public class SourceTextView extends View {
     
     private JPanel west;
     private JPanel tools;
     private JScrollPane leftPane;
     private JPanel left;
     private JScrollPane center;
     
     private static final Color commentColor = new Color(225,225,225);
     private static final Color[] tagColors = {
     new Color(136,0,0),
     new Color(0,136,0),
     new Color(0,0,136),
     new Color(0,136,136),
     new Color(136,0,136),
     new Color(136,136,0),
     new Color(255,102,0),
     new Color(0,255,102),
     new Color(102,0,255),
     new Color(0,102,255),
     new Color(255,0,102),
     new Color(102,255,0)};
     
     private MainFrame mainFrame;
     private MarkedUpText markedUpText;
     private TreeCheckingModel tagsModel;
     private JButton tag;
     private JButton comment;
     private JCheckBox viewComments;
     private JTextPane text;
     private Style defaultStyle;
     private Style commentStyle;
     private Style bcommentStyle;
     private HashMap<String, Integer> tagToColorIndex;
     private int prevColorIndex;
     private int vertScrollPos;
     private int selectionStart;
     private int selectionEnd;
     private int boldDot;
     private JTextArea emptyLeft;
     
     public MarkedUpText getMarkedUpText() {
         return markedUpText;
     }
     
     public TreeCheckingModel getTagsModel() {
         return tagsModel;
     }
     
     public SourceTextView(MainFrame mf, MarkedUpText mut, TreeCheckingModel tm) {
         super(mut.toString());
         mainFrame = mf;
         markedUpText = mut;
         tagsModel = tm;
         prevColorIndex = -1;
         vertScrollPos = 0;
         selectionStart = -1;
         selectionEnd = -1;
         boldDot= -1;
         
         emptyLeft = new JTextArea("Click on a tag and/or comment to see details here.");
         emptyLeft.setAlignmentX(Component.CENTER_ALIGNMENT);
         emptyLeft.setBackground(new Color(250, 250, 250));
         emptyLeft.setBorder(BorderFactory.createEmptyBorder());
         emptyLeft.setPreferredSize(new Dimension(200, 50));
         emptyLeft.setMinimumSize(new Dimension(200, 25));
         emptyLeft.setMaximumSize(new Dimension(200, 1000));
         emptyLeft.setEditable(false);
         emptyLeft.setLineWrap(true);
         emptyLeft.setWrapStyleWord(true);
         emptyLeft.setVisible(true);
         emptyLeft.setForeground(Color.GRAY);
         emptyLeft.setFocusable(false);
         
         initialize();
         
         initializeColorIndices();
         
         tagsModel.addTreeCheckingListener(new TreeCheckingListener() {
             @Override
             public void valueChanged(TreeCheckingEvent e) {
                initializeCenter();
                comment.setEnabled(false);
                tag.setEnabled(false);
             } 
         });
         
         initializeTools();
         initializeCenter();
         center.getVerticalScrollBar().setValue(0);
        comment.setEnabled(false);
        tag.setEnabled(false);
         /*
         center.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
             @Override
             public void adjustmentValueChanged(AdjustmentEvent e) {
                 if (!e.getValueIsAdjusting()) {
                     refreshLeft();
                 }
             }
         });*/
     }
     
     private void initialize() {
         west = new JPanel();
         west.setMinimumSize(new Dimension(200, 200));
         west.setMaximumSize(new Dimension(200, 1000));
         west.setLayout(new BorderLayout());
         
         tools = new JPanel();
         tools.setBackground(new Color(250,250,250));
         west.add(tools, BorderLayout.NORTH);
 
         left = new JPanel();
         left.setBackground(new Color(250,250,250));
         left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
         //left.setLayout(new FlowLayout(FlowLayout.CENTER));
         
         leftPane = new JScrollPane();
         leftPane.setBackground(new Color(250,250,250));
         //left.setPreferredSize(new Dimension(200, 200));
         leftPane.setMinimumSize(new Dimension(200, 200));
         leftPane.setMaximumSize(new Dimension(200, 1000));
         leftPane.setViewportView(left);
         leftPane.setBorder(BorderFactory.createEmptyBorder());
         west.add(leftPane, BorderLayout.CENTER);
         
         add(west, BorderLayout.LINE_START);
         
         center = new JScrollPane();
         center.setBackground(Color.WHITE);
         center.setBorder(BorderFactory.createEmptyBorder());
         center.setViewportBorder(BorderFactory.createMatteBorder(0,1,0,1,Color.DARK_GRAY));
         
         add(center, BorderLayout.CENTER);
     }
     
     private void initializeTools() {
         tools.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
         tools.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
         
         tag = new JButton("Tag");
         
         comment = new JButton("Comment");
         
         viewComments = new JCheckBox("View Comments");
         viewComments.setSelected(true);
         viewComments.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e) {
                 initializeCenter();
                comment.setEnabled(false);
                tag.setEnabled(false);
             }
         });
         
         tools.add(tag);
         tools.add(comment);
         tools.add(viewComments);
     }
     
     private void initializeCenter() {
         vertScrollPos = center.getVerticalScrollBar().getValue();
         text = new JTextPane();
         
         text.setBorder(BorderFactory.createEmptyBorder(70, 10, 70, 10));
         text.setEditable(false);
         text.addCaretListener(new CaretListener() {
             @Override
             public void caretUpdate(CaretEvent e) {
                 textSelected(e);
             }
         });
         text.addFocusListener(new FocusListener() {
             @Override
             public void focusGained(FocusEvent e) {
             }
 
             @Override
             public void focusLost(FocusEvent e) {
                 Object o = e.getOppositeComponent();
                 if (tag != o && comment != o) {
                     tag.setEnabled(false);
                     comment.setEnabled(false);
                 }
                 else if (tag == o)
                     addTag();
                 else
                     addComment();
             }
         });
         refreshTextContent();
         
         center.setViewportView(text);
         
         refreshLeft();
         boldDot = -1;
     }
     
     private void initializeColorIndices() {
         tagToColorIndex = new HashMap<String, Integer>();
         List<TagInstance> tags = markedUpText.getTags();
         for (int i = 0; i < tags.size(); i++) {
             TagInstance ti = tags.get(i);
             String styleName = "t"+ti.getTagType().getPathString();
             if (!tagToColorIndex.containsKey(styleName)) {
                 tagToColorIndex.put(styleName, nextColorIndex());
             }
         }
     }
     
     private void refreshTextContent() {
         left.removeAll();
         left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
         left.revalidate();
         left.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
         
         text.setText(markedUpText.getSourceText().getText());
         StyledDocument doc = text.getStyledDocument();
         //text.setStyledDocument(doc);
         doc.addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(DocumentEvent e) {}
             @Override
             public void removeUpdate(DocumentEvent e) {}
             @Override
             public void changedUpdate(DocumentEvent e) {
                 center.getVerticalScrollBar().setValue(vertScrollPos);
             }
         });
         
         defaultStyle = doc.addStyle("default", StyleContext.getDefaultStyleContext().
                 getStyle(StyleContext.DEFAULT_STYLE));
         
         commentStyle = doc.addStyle("comment", defaultStyle);
         StyleConstants.setBackground(commentStyle, commentColor);
         
         bcommentStyle = doc.addStyle("bcomment", commentStyle);
         StyleConstants.setBackground(bcommentStyle, commentColor);
         StyleConstants.setBold(bcommentStyle, true);
         
         // comments
         if (viewComments.isSelected()) {
             List<Comment> comments = markedUpText.getComments();
             for (int i = 0; i < comments.size(); i++) {
                 Comment c = comments.get(i);
                 TextSection ts = c.getTextSection();
                 int offset = ts.getOffset();
                 int length = ts.getLength();
                 Style style;
                 if (boldDot >= offset && boldDot < offset+length) {
                     style = bcommentStyle;
                     left.add(new CommentDetail(c.getComment(), c.getOwner()));
                     left.add(new Box.Filler(new Dimension(10, 10), new Dimension(10, 10), new Dimension(10, 10)));
                 }
                 else
                     style = commentStyle;
                 doc.setCharacterAttributes(offset, length, style, false);
                 //TODO: display actual comment on left
             }
         }
         
         // tags
         
         TreePath[] tp = tagsModel.getCheckingPaths();
         
         HashSet<String> activeStyleNames = new HashSet<String>(tp.length);
         for (int i = 0; i < tp.length; i++) {
             activeStyleNames.add("t"+((TagType)tp[i].getLastPathComponent()).getPathString());
         }
         
         List<TagInstance> tags = markedUpText.getTags();
         for (int i = 0; i < tags.size(); i++) {
             TagInstance ti = tags.get(i);
             String styleName = "t" + ti.getTagType().getPathString();
             
             if (activeStyleNames.contains(styleName)) {
                 
                 int colorIndex;
                 if (tagToColorIndex.containsKey(styleName)) {
                     colorIndex = tagToColorIndex.get(styleName);
                 } else {
                     colorIndex = nextColorIndex();
                     tagToColorIndex.put(styleName, colorIndex);
                 }
                 
                 Style tagStyle = doc.addStyle(styleName, defaultStyle);
                 StyleConstants.setForeground(tagStyle, tagColors[colorIndex]);
                 StyleConstants.setUnderline(tagStyle, true);
                 
                 TextSection ts = ti.getTextSection();
                 int offset = ts.getOffset();
                 int length = ts.getLength();
                 
                 Style style;
                 if (boldDot >= offset && boldDot < offset+length) {
                     style = doc.getStyle("b"+styleName);
                     if (style == null) {
                         style = doc.addStyle("b"+styleName, tagStyle);
                         StyleConstants.setBold(style, true);
                     }
                     left.add(new TagDetail(ti.getTagType().getPathString(), ti.getOwner(), tagColors[colorIndex]));
                     left.add(new Box.Filler(new Dimension(10, 10), new Dimension(10, 10), new Dimension(10, 10)));
                 }
                 else
                     style = tagStyle;
                 
                 doc.setCharacterAttributes(offset, length, style, false);
                 //TODO: display tag label on left (and user?)
             }
         }
         
         if (left.getComponentCount()==0)
             left.add(emptyLeft);
         left.revalidate();
         left.repaint();
     }
     
     private int nextColorIndex() {
         prevColorIndex += 1;
         if (prevColorIndex == tagColors.length) {
             prevColorIndex = 0;
         }
         return prevColorIndex;
     }
     
     private void textSelected(CaretEvent evt) {
         //Get the location in the text
         int dot = evt.getDot();
         int mark = evt.getMark();
         
         
         if (evt.getSource().equals(text) && dot == mark && dot > 0 && dot < text.getDocument().getLength()) {// no selection
             selectionStart = -1;
             selectionEnd = -1;
             comment.setEnabled(false);
             tag.setEnabled(false);
             /*AttributeSet as = text.getStyledDocument().getCharacterElement(dot).getAttributes().copyAttributes();
             if (as.containsAttribute(StyleConstants.Background, commentColor) ||
                 as.containsAttribute(StyleConstants.Underline, true)) {
                 initializeCenter(dot);
             }*/
             boldDot = dot;
             initializeCenter();
         }
         else {
             if (dot < mark) {
                 selectionStart = dot;
                 selectionEnd = mark;
             } else {
                 selectionStart = mark;
                 selectionEnd = dot;
             }
             comment.setEnabled(true);
             tag.setEnabled(true);
         }
     }
     
     private void addTag() {
         int offset = selectionStart;
         int length = selectionEnd-selectionStart;
         
         try {
             String commentedText = text.getText(offset, length);
             AddTagDialog atd = new AddTagDialog(mainFrame, commentedText, mainFrame.getBlankTagTree());
             atd.setVisible(true);
             
             if (atd.hasResults()) {
                 TagType tt = atd.getSelectedTagType();
                 markedUpText.addTag(tt, new TextSection(offset, length));
             }
             
         } catch (BadLocationException ex) {
             (new ErrorDialog(mainFrame,"Invalid selection")).setVisible(true);
         } finally {
             initializeCenter();
         }
     }
     
     private void addComment() {
         int offset = selectionStart;
         int length = selectionEnd-selectionStart;
         
         try {
             String commentedText = text.getText(offset, length);
             AddCommentDialog acd = new AddCommentDialog(mainFrame, commentedText);
             acd.setVisible(true);
             
             if (acd.hasResults()) {
                 String c = acd.getCommentText();
                 markedUpText.addComment(c, new TextSection(offset, length));
             }
         } catch (BadLocationException ex) {
             (new ErrorDialog(mainFrame,"Invalid selection")).setVisible(true);
         } finally {
             initializeCenter();
         }
     }
     
     private void refreshLeft() {
         /*
         center.getViewport().getViewRect();
         text.getCaret().getMagicCaretPosition();
         
         // comments
         if (viewComments.isSelected()) {
             List<Comment> comments = markedUpText.getComments();
             for (int i = 0; i < comments.size(); i++) {
                 Comment com = comments.get(i);
                 TextSection ts = com.getTextSection();
                 int offset = ts.getOffset();
                 int length = ts.getLength();
                 Caret car = text.getCaret();
                 car.setVisible(true);
                 car.setDot(offset);
                 Point p = car.getMagicCaretPosition();
                 car.setVisible(false);
                 if (p != null) {
                     System.out.println(p.x + " " + p.y);
                     try {
                         left.add(new JLabel(text.getText(offset, length)));
                     } catch (BadLocationException ex) {
                         Exceptions.printStackTrace(ex);
                     }
                 }
             }
         }
         
         // tags
         
         TreePath[] tp = tagsModel.getCheckingPaths();
         
         List<TagInstance> tags = markedUpText.getTags();
         for (int i = 0; i < tags.size(); i++) {
             TagInstance ti = tags.get(i);
             TextSection ts = ti.getTextSection();
             int offset = ts.getOffset();
             int length = ts.getLength();
             Caret car = text.getCaret();
             car.setDot(offset);
             Point p = car.getMagicCaretPosition();
             if (p != null) {
                 System.out.println(p.x + " " + p.y);
                 try {
                     left.add(new JLabel(text.getText(offset, length)));
                 } catch (BadLocationException ex) {
                     Exceptions.printStackTrace(ex);
                 }
             }
         }*/
     }
 }
