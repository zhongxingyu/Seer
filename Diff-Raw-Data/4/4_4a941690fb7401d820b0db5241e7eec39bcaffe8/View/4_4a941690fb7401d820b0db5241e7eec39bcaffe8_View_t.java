 /**
  * Write a description of class View here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Queue;
 
 import javax.sound.sampled.*;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextPane;
 import javax.swing.JLabel;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.border.EmptyBorder;
 
 import java.awt.Dimension;
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.GridLayout;
 import java.awt.FlowLayout;
 import java.awt.Image;
 import java.awt.event.*;
 import java.awt.Color;
 import java.awt.Font;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.Timer;
 
 public class View extends javax.swing.JFrame
 {
     // This panel is used to hold the current page title and remaining points
     JPanel pageStartPanel;
     // This panel is renewed when new things are shown so the reference is needed. 
     JPanel cards;
     // This label is used to hold the remainging points
     JLabel points;
     JLabel currentPaneTitle;
     Controller controller;
     JScrollPane sp;
     private int currXScroll;
     private JTextArea reading;
     private boolean showHint = false;
     
     // constructor
     public View(Controller controller)
     {
         this.controller = controller;
         // initialize the page start panel and add a border
         pageStartPanel = new JPanel(new BorderLayout());
         pageStartPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
 
         // Initial points to 100
         points = new JLabel("Calculating Points...");
         points.setFont(new Font("Dialog", Font.PLAIN, 32));
         // add the points and title label to the pageStartPanel
         pageStartPanel.add(points, BorderLayout.LINE_END);
         // initialize the centerPanel
         cards = new JPanel();
         cards.setLayout(new java.awt.CardLayout());
         reading = new JTextArea();
         this.getContentPane().add(cards, BorderLayout.CENTER);
         this.getContentPane().add(pageStartPanel, BorderLayout.PAGE_START);
         this.setTitle("Delayed Consequence");
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setSize(800, 600);
         this.setMinimumSize(new Dimension(800, 600));
         // frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
         this.setLocationRelativeTo(null);
         // frame.setUndecorated(true);
         this.setVisible(true);
         //this.setResizable(false);
         currXScroll = 40;
     }
     
     public void setBeginPanelColorDefault()
     {
         pageStartPanel.setBackground(javax.swing.UIManager.getColor ( "Panel.background" ));
     }
     
     public void gameOver(int points)
     {
         pageStartPanel.setBackground(javax.swing.UIManager.getColor ( "Panel.background" ));
         JPanel newPanel = new JPanel(new BorderLayout());
         newPanel.setBorder(new EmptyBorder(50,50,50,50));  
         JTextArea reading = new JTextArea();
         reading.setWrapStyleWord(true);
         reading.setLineWrap(true);
         reading.setEditable(false);
         reading.setFont(new Font("Dialog", Font.PLAIN, Setup.getFeedbackFont()));
         reading.setBounds(10, 0, 774, 496);
         newPanel.add(reading);
         JScrollPane sp = new javax.swing.JScrollPane(reading,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         sp.setViewportView(reading);
         reading.setEditable(false);
         reading.append("You managed to save " + points + " points.");
         newPanel.add(reading, BorderLayout.CENTER);
             
         
         cards.add(newPanel, "Read now!");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards); 
         
     }
         
     public void setPoints(int points)
     {
         this.points.setText("Remaining Points: " + points);
     }
     
     public void showInstructionSheet()
     {
         JPanel newPanel = new JPanel(new BorderLayout());         
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         JTextArea introTextArea = new JTextArea();
         introTextArea.setWrapStyleWord(true);
         introTextArea.setLineWrap(true);
         introTextArea.setEditable(false);
         introTextArea.setFont(new Font("Dialog", Font.PLAIN, 14));
         newPanel.add(introTextArea);
         JScrollPane sp = new javax.swing.JScrollPane(introTextArea,JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         sp.setViewportView(introTextArea);
         ArrayList<String> introduction = Setup.getITS();
         introTextArea.setEditable(false);
         for(int i = 0; i < introduction.size(); i++)
         {
             introTextArea.append(introduction.get(i));
             introTextArea.append("\n");
         }
         JPanel beginButtonPanel = new JPanel(new BorderLayout());
         JButton beginButton = new JButton("Begin");
         beginButton.addActionListener(new BeginButtonAction());        
         newPanel.add(introTextArea, BorderLayout.CENTER);
         beginButtonPanel.add(beginButton, BorderLayout.LINE_END);
         newPanel.add(beginButtonPanel, BorderLayout.PAGE_END);
         cards.add(newPanel, "Instruction Sheet");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards);
     } 
     
      // nested class
     public class BeginButtonAction implements ActionListener
     {
 
         public void actionPerformed(ActionEvent e)
         {
             System.out.println("DEBUG: BeginButtonAction.actionPerformed() - begin button clicked, invoke getBaselineCondition().");
             if(Setup.isRandomPres())
             {
                  controller.getBaselineCondition();
             } else {
                 controller.manualProgram();
             }
            
         }
     }
     
    
     
     public void establishPreference(char leftButtonChar, char rightButtonChar, int group, int leftIndex, int rightIndex)
     {
         JPanel newPanel = new JPanel(new GridLayout(2,2,10,10));
         JLabel leftFiller = new JLabel();
         JLabel rightFiller = new JLabel();  
         JButton left = new JButton("" + leftButtonChar);
         JButton right = new JButton("" + rightButtonChar);
         
         newPanel.setBorder(new EmptyBorder(50,50,50,50));   
         left.setFont(new Font("Dialog", Font.BOLD,  Setup.getSymbolSize()));
         left.addActionListener(new ButtonAction(group, leftIndex, left));
         right.addActionListener(new ButtonAction(group, rightIndex, right));
         right.setFont(new Font("Dialog", Font.BOLD,  Setup.getSymbolSize()));
         
         Random rand = new Random();
         int which = rand.nextInt(9);
         switch(which)
         {
             case 0: newPanel.add(left);
                     newPanel.add(right);
                     newPanel.add(leftFiller);
                     newPanel.add(rightFiller); break;
             case 1: newPanel.add(rightFiller);
                     newPanel.add(left);
                     newPanel.add(right);
                     newPanel.add(leftFiller); break;
             case 2: newPanel.add(leftFiller);
                     newPanel.add(rightFiller);
                     newPanel.add(left);
                     newPanel.add(right); break;
             case 3: newPanel.add(right);
                     newPanel.add(leftFiller);
                     newPanel.add(rightFiller);
                     newPanel.add(left); break;
             case 4: newPanel.add(rightFiller);
                     newPanel.add(right);
                     newPanel.add(leftFiller);
                     newPanel.add(left);
             case 5: newPanel.add(left);
                     newPanel.add(rightFiller);
                     newPanel.add(right);
                     newPanel.add(leftFiller);
             case 6: newPanel.add(leftFiller);
                     newPanel.add(left);
                     newPanel.add(rightFiller);
                     newPanel.add(right);
             case 7: newPanel.add(right);
                     newPanel.add(leftFiller);
                     newPanel.add(left);
                     newPanel.add(rightFiller);
             case 8: newPanel.add(rightFiller);
                     newPanel.add(left);
                     newPanel.add(leftFiller);
                     newPanel.add(right);          
         }
         
         cards.add(newPanel, "Baseline Condition");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards);
     }    
     
     public void presentCondition(char leftButtonChar, char rightButtonChar, int group, int leftIndex, int rightIndex)
     {
         JPanel newPanel = new JPanel(new GridLayout(1,2,40,40));
         newPanel.setBorder(new EmptyBorder(50,50,50,50));   
         JButton left = new JButton("" + leftButtonChar);
         JButton right = new JButton("" + rightButtonChar);
         left.setFont(new Font("Dialog", Font.BOLD, Setup.getSymbolSize()));
         left.addActionListener(new ButtonAction(group, leftIndex, left));
         right.addActionListener(new ButtonAction(group, rightIndex, right));
         right.setFont(new Font("Dialog", Font.BOLD,  Setup.getSymbolSize()));
         newPanel.add(left);
         newPanel.add(right);
         cards.add(newPanel, "Baseline Condition");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards);
     }
     
      public class ButtonAction implements ActionListener
     {
         int group;
         int index;
         JButton button;
         // constructor
         public ButtonAction(int group, int index, JButton button)
         {
             this.group = group;
             this.index = index;
             this.button = button;
         }
         public void actionPerformed(ActionEvent e)
         {
           
            button.setEnabled(false);
             ActionListener taskPerformer = new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                
               System.out.println("DEBUG - ButtonAction.actionPerformed() - Symbol clicked incrementHitCount(" + group + "," + index + ")");
             controller.incrementHitCount(group, index);
             if(Setup.isRandomPres()) 
             {
                 System.out.println("DEBUG - Setup.isRandomPres(): " + Setup.isRandomPres());
                 System.out.println("DEBUG - ButtonAction.actionPerformed() - invokeContinueBaselineCondition");
                 controller.continueBaselineCondition();
             } else {
                 System.out.println("DEBUG - ButtonAction.actionPerformed() - manualProgram");
                 controller.manualProgram();
             }
             
             }
             };
               button.setBackground(Color.YELLOW);
               button.setOpaque(true);
               button.setBorderPainted(false);
             Timer timer = new Timer(Setup.getBasePause(), taskPerformer);
             timer.setRepeats(false);
             timer.start();       
         
             
         }
     }
     
     /**
      * DVRC VIEW SEQUENCE ******************************************************************************
      */
     
     public void dvrc(char leftButtonChar, char rightButtonChar, int group, int leftIndex, int rightIndex)
     {
         JPanel newPanel = new JPanel(new GridLayout(2,2,10,10));
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(142,180,227));
         JLabel leftFiller = new JLabel();
         JLabel rightFiller = new JLabel();
         newPanel.add(leftFiller);
         newPanel.add(rightFiller);
         pageStartPanel.setBackground(new Color(142,180,227));        
         JButton left = new JButton("" + leftButtonChar);
         JButton right = new JButton("" + rightButtonChar);
         left.setFont(new Font("Dialog", Font.BOLD,  Setup.getSymbolSize()));
         left.addActionListener(new dvrcButtonOne(group, leftIndex, left));
         right.addActionListener(new dvrcButtonOne(group, rightIndex, right));
         right.setFont(new Font("Dialog", Font.BOLD,  Setup.getSymbolSize()));
         newPanel.add(left);
         newPanel.add(right);
         cards.add(newPanel, "Baseline Condition");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards);
     }
     
     public void dvrc2()
     {
         JPanel newPanel = new JPanel(new BorderLayout());
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(240,240,240));
         pageStartPanel.setBackground(new Color(240,240,240)); 
         reading = new JTextArea();
         reading.setWrapStyleWord(true);
         reading.setLineWrap(true);
         reading.setEditable(false);
         reading.setFont(new Font("Dialog", Font.PLAIN, 14));
         reading.setBounds(10, 0, 774, 496);
         newPanel.add(reading);
         sp = new javax.swing.JScrollPane(reading,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         sp.setViewportView(reading);
         ArrayList<String> introduction = Setup.getRI();
         reading.setEditable(false);
         for(int i = 0; i < introduction.size(); i++)
         {
             reading.append(introduction.get(i));
             reading.append("\n");
         }       
         newPanel.add(sp, BorderLayout.CENTER);
         sp.getVerticalScrollBar().setValue(currXScroll);
         reading.setCaretPosition(currXScroll);
         cards.add(newPanel, "Read now!");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards);  
         
         ActionListener taskPerformer = new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 currXScroll = reading.getCaretPosition();
                 dvrc3(controller.getCharCubeChar(Model.DVRC_ENUM, controller.getDVRCleft()), controller.getCharCubeChar(Model.DVRC_ENUM, controller.getDVRCright()), Model.DVRC_ENUM, controller.getDVRCleft(), controller.getDVRCright());
                 
             }
             };
         Timer timer = new Timer(Setup.getRD(), taskPerformer);
         timer.setRepeats(false);
         timer.start();       
     }
     
     public void dvrc3(char leftButtonChar, char rightButtonChar, int group, int leftIndex, int rightIndex)
     {
         JPanel newPanel = new JPanel(new GridLayout(2,2,10,10));
         JLabel leftFiller = new JLabel();
         JLabel rightFiller = new JLabel();
         newPanel.add(leftFiller);
         newPanel.add(rightFiller);
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(217,150,148));
         pageStartPanel.setBackground(new Color(217,150,148));        
         JButton left = new JButton("" + leftButtonChar);
         JButton right = new JButton("" + rightButtonChar);
         left.setFont(new Font("Dialog", Font.BOLD,  Setup.getSymbolSize()));
         left.addActionListener(new dvrcButtonTwo(group, leftIndex, left));
         right.addActionListener(new dvrcButtonTwo(group, rightIndex, right));
         right.setFont(new Font("Dialog", Font.BOLD,  Setup.getSymbolSize()));
         newPanel.add(left);
         newPanel.add(right);
         cards.add(newPanel, "Baseline Condition");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards);
     }
     
     public void dvrc4()
     {
         final JPanel newPanel = new JPanel(new BorderLayout());
         controller.updatePoints();
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(179,162,199));
         pageStartPanel.setBackground(new Color(179,162,199));   
         JTextArea reading = new JTextArea();
         reading.setWrapStyleWord(true);
         reading.setLineWrap(true);
         reading.setEditable(false);
         reading.setFont(new Font("Dialog", Font.PLAIN, Setup.getFeedbackFont()));
         reading.setBounds(10, 0, 774, 496);
         newPanel.add(reading);
         JScrollPane sp = new javax.swing.JScrollPane(reading,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         sp.setViewportView(reading);
         reading.setEditable(false);
         
         // Load text file
         if(showHint)
         {
              reading.append(Setup.getDvrcLeastTXT());
         } 
         
         newPanel.add(reading, BorderLayout.CENTER);
         showHint = false;
         
         cards.add(newPanel, "Read now!");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards); 
         
         
          ActionListener taskPerformer = new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 JButton continueButton = new JButton("Continue");
                 continueButton.addActionListener(new feedbackContinueButton());
                 newPanel.add(continueButton, BorderLayout.SOUTH);
                 revalidate();
                 repaint();
             }
             };
         Timer timer = new Timer(Setup.getFeedbackDelay(), taskPerformer);
         timer.setRepeats(false);
         timer.start();       
     }
     
     public class feedbackContinueButton implements ActionListener
     {
         public void actionPerformed(ActionEvent e)
         {
             controller.printConditionStats();
             if(Setup.isRandomPres()) {
                 controller.presentCondition();
             } else {
                 controller.manualProgram();
             }
             
             
         }
     }
     
     public class dvrcButtonTwo implements ActionListener
     {
         int group;
         int index;
         JButton button;
         // constructor
         public dvrcButtonTwo(int group, int index, JButton button)
         {
             this.group = group;
             this.index = index;
             this.button = button;
         }
         public void actionPerformed(ActionEvent e)
         {
             
             ActionListener taskPerformer = new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                   System.out.println("DEBUG - dvrcButtonAction.actionPerformed() - Symbol clicked incrementConditionCount(" + group + "," + index + ")");
                   controller.incrementConditionCount(Model.DVRC_ENUM,index);
                   System.out.println("DEBUG - dvrcButtonAction.actionPerformed() - invokeContinueBaselineCondition");
                   dvrc4();
                 }
             };
               button.setBackground(Color.YELLOW);
               button.setOpaque(true);
               button.setBorderPainted(false);
             Timer timer = new Timer(Setup.getBasePause(), taskPerformer);
             timer.setRepeats(false);
             timer.start();   
             
             
             
         }
     }
     
      public class dvrcButtonOne implements ActionListener
     {
         int group;
         int index;
         JButton button;
         // constructor
         public dvrcButtonOne(int group, int index, JButton button)
         {
             this.group = group;
             this.index = index;
             this.button = button;
         }
         public void actionPerformed(ActionEvent e)
         {
             
             
             ActionListener taskPerformer = new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     System.out.println("DEBUG - dvrcButtonAction.actionPerformed() - Symbol clicked incrementConditionCount(" + group + "," + index + ")");
                     if(index == controller.dvrcMostPreferred)
                     {
                         showHint = true;
                     }  
                     controller.incrementConditionCount(Model.DVRC_ENUM, index);
                     controller.calculatePointLoss(group, index);
                     System.out.println("DEBUG - dvrcButtonAction.actionPerformed() - invoke dvrc2()");
                     dvrc2();
                 }
             };
               button.setBackground(Color.YELLOW);
               button.setOpaque(true);
               button.setBorderPainted(false);
             Timer timer = new Timer(Setup.getBasePause(), taskPerformer);
             timer.setRepeats(false);
             timer.start();       
             
             
             
             
         }
     }
     
     
     
     
     
      /**
      * DVR VIEW SEQUENCE ******************************************************************************
      */
     
     public void dvr(char leftButtonChar, char rightButtonChar, int group, int leftIndex, int rightIndex)
     {
         JPanel newPanel = new JPanel(new GridLayout(2,2,10,10));
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(255,255,0));
         pageStartPanel.setBackground(new Color(255,255,0));  
         JLabel leftFiller = new JLabel();
         JLabel rightFiller = new JLabel();
         newPanel.add(leftFiller);
         newPanel.add(rightFiller);
         JButton left = new JButton("" + leftButtonChar);
         JButton right = new JButton("" + rightButtonChar);
         left.setFont(new Font("Dialog", Font.BOLD,  Setup.getSymbolSize()));
         left.addActionListener(new dvrButtonOne(group, leftIndex, left));
         right.addActionListener(new dvrButtonOne(group, rightIndex, right));
         right.setFont(new Font("Dialog", Font.BOLD,  Setup.getSymbolSize()));
         newPanel.add(left);
         newPanel.add(right);
         cards.add(newPanel, "Baseline Condition");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards);
     }
     
     public void dvr2()
     {
         JPanel newPanel = new JPanel(new BorderLayout());
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(240,240,240));
         pageStartPanel.setBackground(new Color(240,240,240)); 
         reading = new JTextArea();
         reading.setWrapStyleWord(true);
         reading.setLineWrap(true);
         reading.setEditable(false);
         reading.setFont(new Font("Dialog", Font.PLAIN, 14));
         reading.setBounds(10, 0, 774, 496);
         newPanel.add(reading);
         sp = new javax.swing.JScrollPane(reading,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         sp.setViewportView(reading);
         ArrayList<String> introduction = Setup.getRI();
         reading.setEditable(false);
         for(int i = 0; i < introduction.size(); i++)
         {
             reading.append(introduction.get(i));
             reading.append("\n");
         }       
         newPanel.add(sp, BorderLayout.CENTER);
         cards.add(newPanel, "Read now!");
         sp.getVerticalScrollBar().setValue(currXScroll);
         reading.setCaretPosition(currXScroll);
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards);  
         
         ActionListener taskPerformer = new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 currXScroll = reading.getCaretPosition();
                 dvr3(controller.getCharCubeChar(Model.DVR_ENUM, controller.getDVRleft()), controller.getCharCubeChar(Model.DVR_ENUM, controller.getDVRright()), Model.DVR_ENUM, controller.getDVRleft(), controller.getDVRright());
             }
             };
         Timer timer = new Timer(Setup.getRD(), taskPerformer);
         timer.setRepeats(false);
         timer.start();       
     }
     
     public void dvr3(char leftButtonChar, char rightButtonChar, int group, int leftIndex, int rightIndex)
     {
         JPanel newPanel = new JPanel(new GridLayout(2,2,10,10));
         JLabel leftFiller = new JLabel();
         JLabel rightFiller = new JLabel();
         newPanel.add(leftFiller);
         newPanel.add(rightFiller);
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(195,214,155));
         pageStartPanel.setBackground(new Color(195,214,155));        
         JButton left = new JButton("" + leftButtonChar);
         JButton right = new JButton("" + rightButtonChar);
         left.setFont(new Font("Dialog", Font.BOLD,  Setup.getSymbolSize()));
         left.addActionListener(new dvrButtonTwo(group, leftIndex, left));
         right.addActionListener(new dvrButtonTwo(group, rightIndex, right));
         right.setFont(new Font("Dialog", Font.BOLD,  Setup.getSymbolSize()));
         newPanel.add(left);
         newPanel.add(right);
         cards.add(newPanel, "Baseline Condition");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards);
     }
     
     public void dvr4()
     {
         final JPanel newPanel = new JPanel(new BorderLayout());
         controller.updatePoints();
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(196,189,151));
         pageStartPanel.setBackground(new Color(196,189,151));   
         JTextArea reading = new JTextArea();
         reading.setWrapStyleWord(true);
         reading.setLineWrap(true);
         reading.setEditable(false);
         reading.setFont(new Font("Dialog", Font.PLAIN, Setup.getFeedbackFont()));
         reading.setBounds(10, 0, 774, 496);
         newPanel.add(reading);
         JScrollPane sp = new javax.swing.JScrollPane(reading,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         sp.setViewportView(reading);
         reading.setEditable(false);
         if(showHint)
         {
             reading.append(Setup.getDvrMostTXT());
         } else {
             reading.append(Setup.getDvrLeastTXT());
         }
         
         
         newPanel.add(reading, BorderLayout.CENTER);
         showHint = false;
         cards.add(newPanel, "Read now!");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards); 
         
         
            
          ActionListener taskPerformer = new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 JButton continueButton = new JButton("Continue");
                 continueButton.addActionListener(new feedbackContinueButton());
                 newPanel.add(continueButton, BorderLayout.SOUTH);
                 revalidate();
                 repaint();
             }
             };
         Timer timer = new Timer(Setup.getFeedbackDelay(), taskPerformer);
         timer.setRepeats(false);
         timer.start();           
     }
     
     public class dvrButtonTwo implements ActionListener
     {
         int group;
         int index;
         JButton button;
         // constructor
         public dvrButtonTwo(int group, int index, JButton button)
         {
             this.group = group;
             this.index = index;
             this.button = button;
         }
         public void actionPerformed(ActionEvent e)
         {
             ActionListener taskPerformer = new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                   System.out.println("DEBUG - dvrButtonAction.actionPerformed() - Symbol clicked incrementConditionCount(" + group + "," + index + ")");
                     controller.incrementConditionCount(Model.DVR_ENUM,index);
                     System.out.println("DEBUG - dvrButtonAction.actionPerformed() - invokeContinueBaselineCondition");
                     dvr4();
                 }
             };
               button.setBackground(Color.YELLOW);
               button.setOpaque(true);
               button.setBorderPainted(false);
             Timer timer = new Timer(Setup.getBasePause(), taskPerformer);
             timer.setRepeats(false);
             timer.start();   
             
             
             
         }
     }
     
      public class dvrButtonOne implements ActionListener
     {
         int group;
         int index;
         JButton button;
         // constructor
         public dvrButtonOne(int group, int index, JButton button)
         {
             this.group = group;
             this.index = index;
             this.button = button;
         }
         public void actionPerformed(ActionEvent e)
         {
             ActionListener taskPerformer = new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     if(index == controller.dvrMostPreferred)
                     {
                         showHint = true;
                     }  
                     System.out.println("DEBUG - dvrButtonAction.actionPerformed() - Symbol clicked incrementConditionCount(" + group + "," + index + ")");
                     controller.incrementConditionCount(Model.DVR_ENUM, index);
                     System.out.println("DEBUG - dvrButtonAction.actionPerformed() - invoke dvr2()");
                     dvr2();
                         }
                     };
               button.setBackground(Color.YELLOW);
               button.setOpaque(true);
               button.setBorderPainted(false);
             Timer timer = new Timer(Setup.getBasePause(), taskPerformer);
             timer.setRepeats(false);
             timer.start();   
             
             
             
         }
     }
     
     
     /**
      * DC VIEW SEQUENCE ******************************************************************************
      */
     
     public void dc(char leftButtonChar, char rightButtonChar, int group, int leftIndex, int rightIndex)
     {
         JPanel newPanel = new JPanel(new GridLayout(2,2,10,10));
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(166,166,166));
         JLabel leftFiller = new JLabel();
         JLabel rightFiller = new JLabel();
         newPanel.add(leftFiller);
         newPanel.add(rightFiller);
         pageStartPanel.setBackground(new Color(166,166,166));        
         JButton left = new JButton("" + leftButtonChar);
         JButton right = new JButton("" + rightButtonChar);
         left.setFont(new Font("Dialog", Font.BOLD,  Setup.getSymbolSize()));
         left.addActionListener(new dcButtonOne(group, leftIndex, left));
         right.addActionListener(new dcButtonOne(group, rightIndex, right));
         right.setFont(new Font("Dialog", Font.BOLD,  Setup.getSymbolSize()));
         newPanel.add(left);
         newPanel.add(right);
         cards.add(newPanel, "Baseline Condition");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards);
     }
     
     public void dc2()
     {
         JPanel newPanel = new JPanel(new BorderLayout());
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(240,240,240));
         pageStartPanel.setBackground(new Color(240,240,240)); 
         reading = new JTextArea();
         reading.setWrapStyleWord(true);
         reading.setLineWrap(true);
         reading.setEditable(false);
         reading.setFont(new Font("Dialog", Font.PLAIN, 14));
         reading.setBounds(10, 0, 774, 496);
         newPanel.add(reading);
         sp = new javax.swing.JScrollPane(reading,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         sp.setViewportView(reading);
         ArrayList<String> introduction = Setup.getRI();
         reading.setEditable(false);
         for(int i = 0; i < introduction.size(); i++)
         {
             reading.append(introduction.get(i));
             reading.append("\n");
         }       
         newPanel.add(sp, BorderLayout.CENTER);
         cards.add(newPanel, "Read now!");
         sp.getVerticalScrollBar().setValue(currXScroll);
         reading.setCaretPosition(currXScroll);
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards);  
         
         ActionListener taskPerformer = new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 currXScroll = reading.getCaretPosition();
                 dc3(controller.getCharCubeChar(Model.DC_ENUM, controller.getDCleft()), controller.getCharCubeChar(Model.DC_ENUM, controller.getDCright()), Model.DC_ENUM, controller.getDCleft(), controller.getDCright());
            }
             };
         Timer timer = new Timer(Setup.getRD(), taskPerformer);
         timer.setRepeats(false);
         timer.start();       
     }
     
     public void dc3(char leftButtonChar, char rightButtonChar, int group, int leftIndex, int rightIndex)
     {
         JPanel newPanel = new JPanel(new GridLayout(2,2,10,10));
         JLabel leftFiller = new JLabel();
         JLabel rightFiller = new JLabel();
         newPanel.add(leftFiller);
         newPanel.add(rightFiller);
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(152,72,7));
         pageStartPanel.setBackground(new Color(152,72,7));        
         JButton left = new JButton("" + leftButtonChar);
         JButton right = new JButton("" + rightButtonChar);
         left.setFont(new Font("Dialog", Font.BOLD, Setup.getSymbolSize()));
         left.addActionListener(new dcButtonTwo(group, leftIndex, left));
         right.addActionListener(new dcButtonTwo(group, rightIndex, right));
         right.setFont(new Font("Dialog", Font.BOLD,  Setup.getSymbolSize()));
         newPanel.add(left);
         newPanel.add(right);
         cards.add(newPanel, "Baseline Condition");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards);
     }
     
     public void dc4()
     {
         final JPanel newPanel = new JPanel(new BorderLayout());
         controller.updatePoints();
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(255,255,255));
         pageStartPanel.setBackground(new Color(255,255,255));   
         JTextArea reading = new JTextArea();
         reading.setWrapStyleWord(true);
         reading.setLineWrap(true);
         reading.setEditable(false);
         reading.setFont(new Font("Dialog", Font.PLAIN, Setup.getFeedbackFont()));
         reading.setBounds(10, 0, 774, 496);
         newPanel.add(reading);
         JScrollPane sp = new javax.swing.JScrollPane(reading,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         sp.setViewportView(reading);
         reading.setEditable(false);
         
         
         if(showHint)
         {
             reading.append(Setup.getDcMostTXT());
         } else {
             reading.append(Setup.getDcLeastTXT());
         }
         
         newPanel.add(reading, BorderLayout.CENTER);
         showHint = false;
         cards.add(newPanel, "Read now!");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards); 
         
         
           
          ActionListener taskPerformer = new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 JButton continueButton = new JButton("Continue");
                 continueButton.addActionListener(new feedbackContinueButton());
                 newPanel.add(continueButton, BorderLayout.SOUTH);
                 revalidate();
                 repaint();
             }
             };
         Timer timer = new Timer(Setup.getFeedbackDelay(), taskPerformer);
         timer.setRepeats(false);
         timer.start();          
     }
     
     public class dcButtonTwo implements ActionListener
     {
         int group;
         int index;
         JButton button;
         // constructor
         public dcButtonTwo(int group, int index, JButton button)
         {
             this.group = group;
             this.index = index;
             this.button = button;
         }
         public void actionPerformed(ActionEvent e)
         {
             ActionListener taskPerformer = new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     System.out.println("DEBUG - dcButtonAction.actionPerformed() - Symbol clicked incrementConditionCount(" + group + "," + index + ")");
                     controller.incrementConditionCount(Model.DC_ENUM,index);
                     System.out.println("DEBUG - dcButtonAction.actionPerformed() - invokeContinueBaselineCondition");
                     dc4();
                 }
             };
               button.setBackground(Color.YELLOW);
               button.setOpaque(true);
               button.setBorderPainted(false);
             Timer timer = new Timer(Setup.getBasePause(), taskPerformer);
             timer.setRepeats(false);
             timer.start();   
             
             
             
         }
     }
     
      public class dcButtonOne implements ActionListener
     {
         int group;
         int index;
         JButton button;
         // constructor
         public dcButtonOne(int group, int index, JButton button)
         {
             this.group = group;
             this.index = index;
             this.button = button;
         }
         public void actionPerformed(ActionEvent e)
         {
             
             ActionListener taskPerformer = new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     if(index == controller.dcMostPreferred)
                     {
                         showHint = true;
                     }  
                     controller.calculatePointLoss(group, index);
                     System.out.println("DEBUG - dcButtonAction.actionPerformed() - Symbol clicked incrementConditionCount(" + group + "," + index + ")");
                     controller.incrementConditionCount(Model.DC_ENUM, index);
                     System.out.println("DEBUG - dcButtonAction.actionPerformed() - invoke dc2()");
                     dc2();
                 }
             };
               button.setBackground(Color.YELLOW);
               button.setOpaque(true);
               button.setBorderPainted(false);
             Timer timer = new Timer(Setup.getBasePause(), taskPerformer);
             timer.setRepeats(false);
             timer.start();   
             
             
             
         }
     }
     
     
     
     
     /**
      * IC VIEW SEQUENCE ******************************************************************************
      */
     
     public void ic(char leftButtonChar, char rightButtonChar, int group, int leftIndex, int rightIndex)
     {
         JPanel newPanel = new JPanel(new GridLayout(2,2,10,10));
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(250,192,144));
         pageStartPanel.setBackground(new Color(250,192,144));
         JLabel leftFiller = new JLabel();
         JLabel rightFiller = new JLabel();
         newPanel.add(leftFiller);
         newPanel.add(rightFiller);
         JButton left = new JButton("" + leftButtonChar);
         JButton right = new JButton("" + rightButtonChar);
         left.setFont(new Font("Dialog", Font.BOLD,  Setup.getSymbolSize()));
         left.addActionListener(new icButtonOne(group, leftIndex, left));
         right.addActionListener(new icButtonOne(group, rightIndex, right));
         right.setFont(new Font("Dialog", Font.BOLD,  Setup.getSymbolSize()));
         newPanel.add(left);
         newPanel.add(right);
         cards.add(newPanel, "Baseline Condition");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards);
     }
     
     
     public void ic2()
     {
         final JPanel newPanel = new JPanel(new BorderLayout());
         controller.updatePoints();
         newPanel.setBorder(new EmptyBorder(50,50,50,50));
         newPanel.setBackground(new Color(217,217,217));
         pageStartPanel.setBackground(new Color(217,217,217));   
         JTextArea reading = new JTextArea();
         reading.setWrapStyleWord(true);
         reading.setLineWrap(true);
         reading.setEditable(false);
         reading.setFont(new Font("Dialog", Font.PLAIN, Setup.getFeedbackFont()));
         reading.setBounds(10, 0, 774, 496);
         newPanel.add(reading);
         JScrollPane sp = new javax.swing.JScrollPane(reading,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         sp.setViewportView(reading);
         reading.setEditable(false);
         
         if(showHint)
         {
             reading.append(Setup.getIcMostTXT());
         } else {
             reading.append(Setup.getIcLeastTXT());
         }
         
         newPanel.add(reading, BorderLayout.CENTER);
         showHint = false;
         cards.add(newPanel, "Read now!");
         CardLayout cl = (CardLayout) cards.getLayout();
         cl.next(cards); 
         
         
           
          ActionListener taskPerformer = new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 JButton continueButton = new JButton("Continue");
                 continueButton.addActionListener(new feedbackContinueButton());
                 newPanel.add(continueButton, BorderLayout.SOUTH);
                 revalidate();
                 repaint();
             }
             };
         Timer timer = new Timer(Setup.getFeedbackDelay(), taskPerformer);
         timer.setRepeats(false);
         timer.start();            
     }
     
     
      public class icButtonOne implements ActionListener
     {
         int group;
         int index;
         JButton button;
         // constructor
         public icButtonOne(int group, int index, JButton button)
         {
             this.group = group;
             this.index = index;
             this.button  = button;
         }
         public void actionPerformed(ActionEvent e)
         {
             
             ActionListener taskPerformer = new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     if(index == controller.icMostPreferred)
                     {
                         showHint = true;
                     }  
                     controller.calculatePointLoss(group, index);
                     System.out.println("DEBUG - dcButtonAction.actionPerformed() - Symbol clicked incrementConditionCount(" + group + "," + index + ")");
                     controller.incrementConditionCount(Model.IC_ENUM, index);
                     System.out.println("DEBUG - dcButtonAction.actionPerformed() - invoke dc2()");
                     ic2();
                 }
             };
               button.setBackground(Color.YELLOW);
               button.setOpaque(true);
               button.setBorderPainted(false);
             Timer timer = new Timer(Setup.getBasePause(), taskPerformer);
             timer.setRepeats(false);
             timer.start();   
             
              
             
         }
     }
     
     /**
      * END SEQUENCES *********************
      */
     
      public static void playWinSound(){
         try{
             AudioInputStream audio = AudioSystem.getAudioInputStream(new File("winning.wav"));
             Clip clip = AudioSystem.getClip();
             clip.open(audio);
             clip.start();   
         }catch(Exception e){
             System.out.println(e);
         }
      }
      
      public static void playLossSound(){
         try{
             AudioInputStream audio = AudioSystem.getAudioInputStream(new File("losing.wav"));
             Clip clip = AudioSystem.getClip();
             clip.open(audio);
             clip.start();
         }catch(Exception e){
             System.out.println(e);
         }
      }
     
     
     
     public void timerScreen(int a)
     {
         Timer timer = new Timer( 1000, new ActionListener(){
                     @Override
                     public void actionPerformed( ActionEvent e ){
                         CardLayout cl = (CardLayout) cards.getLayout();
                         cl.next(cards);
                     }
                 } );
         timer.start();
         timer.setRepeats(false);
     } 
 
     
    
 }
