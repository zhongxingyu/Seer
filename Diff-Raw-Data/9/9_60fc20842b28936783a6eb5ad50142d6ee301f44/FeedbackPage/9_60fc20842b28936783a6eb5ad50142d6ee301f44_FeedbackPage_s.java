 package com.core.test;
 
 import com.core.lesson.*;
 import com.core.util.Utils;
 import com.core.util.ResourceManager;
 import com.core.util.FixedLengthTextArea;
 import org.json.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.border.*;
 
 
 public class FeedbackPage extends LessonPage {
 
     private JPanel contentPanel;
     private JSONObject feedback;
     private ButtonGroup[] groups;
     private int questionCount;
     private JTextArea[] comments;
     private String commentText;
     private Integer questionStartID = 0;
 
     public FeedbackPage(JSONObject json) {
         try {
             feedback = ResourceManager.getJSON(json.getString("data"));
             commentText = feedback.getString("comment");
             this.questionCount = feedback.getJSONArray("questions").length();
             questionStartID = feedback.getInt("startID");
         } catch (JSONException e) {
         }
         comments = new JTextArea[4];
         groups = new ButtonGroup[4];
         contentPanel = getContentPanel();
         contentPanel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
 
         setLayout(new java.awt.BorderLayout());
 
         JPanel secondaryPanel = new JPanel();
         secondaryPanel.add(contentPanel, BorderLayout.NORTH);
         add(secondaryPanel, BorderLayout.CENTER);
     }
 
     public HashMap getSubmit() {
         LinkedHashMap data = new LinkedHashMap();
         String comm = "";
         for(int i=0; i<questionCount; i++) {
             JRadioButton button = Utils.getSelection(groups[i]);
             if (button != null)
                 data.put("Question_"+(questionStartID+i+1), button.getText());
             String comment = "";
             if (!comments[i].getText().equals(commentText))
                 comment = comments[i].getText();
             data.put("Comment_"+(questionStartID+i+1), comment);
            data.put("linebreak", "\n\r");
         }
         return data;
     }
 
     private boolean checkAnswer() {
         for(int i=0; i<questionCount; i++) {
             JRadioButton button = Utils.getSelection(groups[i]);
             if (button == null)
                 return false;
             if (comments[i].getText().equals(commentText) ||
                     comments[i].getText().length() < 5)
                 return false;
         }
         return true;
     }
 
 
     private JPanel getContentPanel() {
         JPanel contentPanel = new JPanel();
         //contentPanel.setLayout(new GridLayout());
         JPanel instructionPanel = createInstructionPanel();
         JPanel introPanel = createIntroPanel();
         contentPanel.setLayout(new BorderLayout());
 
         //contentPanel.add(introPanel, java.awt.BorderLayout.NORTH);
         contentPanel.add(instructionPanel,BorderLayout.NORTH);
         contentPanel.add(introPanel, BorderLayout.CENTER);
 
         return contentPanel;
 
     }
 
     private JPanel createInstructionPanel() {
     	JPanel instructionPanel = new JPanel();
         instructionPanel.setPreferredSize(new Dimension(400, 160));
         instructionPanel.setLayout(new java.awt.BorderLayout());
     	JEditorPane introArea = new JEditorPane();
         introArea.setContentType("text/html; charset=utf-8");
         //introArea.setText((String)textContent);
         try {
             introArea.setText(feedback.getString("instruction"));
         } catch (JSONException e) {
             System.out.println("error when trying to get json value");
         }
         introArea.setEditable(false);
         //Get JFrame background color  
         Color color = getBackground();
         introArea.setBackground(color);
         instructionPanel.add(introArea, BorderLayout.CENTER);
         return instructionPanel;
     }
 
     private JPanel createIntroPanel() {
         JPanel introPanel = new JPanel();
         //Group the radio buttons.
         introPanel.setLayout(new GridBagLayout());
         //introPanel.setBorder(new LineBorder(Color.BLACK, 1));
         //introPanel.setPreferredSize(new Dimension(650,550));
         GridBagConstraints gBC = new GridBagConstraints();
         gBC.fill = GridBagConstraints.VERTICAL;
         gBC.weightx = 0.1;
         gBC.gridx = 0;
         gBC.gridy = 0;
         gBC.anchor = GridBagConstraints.CENTER;
         gBC.fill = GridBagConstraints.BOTH;
         //gBC.insets=new Insets(2, 2, 2, 2);
         gBC.insets=new Insets(0, 0, 0, 0);
         JSONArray questions = null;
         try {
             questions = feedback.getJSONArray("questions");
             for(int i=0; i<questions.length(); i++) {
                 JSONObject question = questions.getJSONObject(i);
                 JPanel feedbackPanel = new JPanel();
                 //feedbackPanel.setPreferredSize(new Dimension(380, 37));
                 feedbackPanel.setBorder(new LineBorder(Color.BLACK, 1));
                 //feedbackPanel.setLayout(new javax.swing.BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                 //feedbackPanel.setLayout(new BorderLayout());
                 gBC.gridy = i*2;
                 gBC.gridx = 0;
                 //gBC.gridheight = 100;
                 //gBC.ipady = 20;     //This component has more breadth compared to other buttons
                 gBC.weightx = 0.0;
                 gBC.gridheight = 2;
                 JLabel situationLabel = new JLabel();
                 situationLabel.setText(question.getString("title"));
                 //situationLabel.setPreferredSize(new Dimension(100, 20));
                 gBC.insets=new Insets(0, 0, 0, 5);
                 introPanel.add(situationLabel, gBC);
                 JLabel subjectLabel = new JLabel();
                 subjectLabel.setText("<html>"+question.getString("description")+"</html>");
                 subjectLabel.setPreferredSize(new Dimension(200, 50));
                 gBC.gridx = 1;
                 gBC.insets=new Insets(0, 5, 0, 5);
                 introPanel.add(subjectLabel, gBC);
                 gBC.insets=new Insets(0, 0, 0, 0);
                 ButtonGroup group = new ButtonGroup();
                 JSONArray options = question.getJSONArray("options");
                 for(int j=0; j<options.length(); j++) {
                     JRadioButton radioButton = new JRadioButton(options.getString(j));
                     radioButton.setActionCommand(options.getString(j));
                     //radioButton.setSelected(true);
                     radioButton.addActionListener(new ActionListener(){
                         public void actionPerformed(ActionEvent e){
                             if (checkAnswer()) {
                                 sendMessage("READY");
                             }
                         }
                     }) ;
                     group.add(radioButton);
                     feedbackPanel.add(radioButton);
                 }
                 //feedbackPanel.add(group);
                 gBC.gridheight=1;
                 //height of the cell
                 gBC.ipady = 10;
                 gBC.gridx = 2;
                 introPanel.add(feedbackPanel, gBC);
                 //comment.setPreferredSize(new Dimension(300, 40));
                 //JScrollPane scrollpane = new JScrollPane();
                 gBC.ipady = 45;
                 gBC.gridy = i*2+1;
                 //gBC.weightx = 1.0;
                 //gBC.weighty = 1.0;
 
                 //introPanel.add(comment, gBC);
                 JTextArea comment = new FixedLengthTextArea(200,20,50);
 
                 //comment.setBorder (new LineBorder(Color.black, 2));
                 comment.setLineWrap(true);
                 comment.setWrapStyleWord(true);
                 comment.setPreferredSize(new Dimension(300, 20));
                 //comment.setMaximumSize(new Dimension(300, 10));
                 comment.setText(commentText);
                 JScrollPane scrollPane = new JScrollPane(comment);
                 scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                 scrollPane.setPreferredSize(new Dimension(300, 20));
                 //scrollPane.setBounds(10,60,780,500);
                 introPanel.add(scrollPane, gBC);
                 //introPanel.add(comment, gBC);
                 //scrollpane.add(comment);
                 comments[i] = comment;
                 comments[i].addKeyListener(new CommentKeyListener());
                 comments[i].addFocusListener(new FocusListener(){
                     public void focusGained(FocusEvent e) {
                       JTextArea comment = (JTextArea)e.getComponent();
                       if (comment.getText().equals(commentText))
                           comment.setText("");
                     }
                     public void focusLost(FocusEvent e) {
                       JTextArea comment = (JTextArea)e.getComponent();
                       if (comment.getText().equals(""))
                           comment.setText(commentText);
                     }
                 }) ;
                 groups[i] = group;
             }
         } catch (JSONException e) {
         }
         //introPanel.setBorder (new LineBorder(Color.blue, 3));
         return introPanel;
     }
 
     public class CommentKeyListener extends KeyAdapter{
         public void keyPressed(KeyEvent ke){
             char i = ke.getKeyChar();
             if (checkAnswer())
                 sendMessage("READY");
         }
     }
 
 }
