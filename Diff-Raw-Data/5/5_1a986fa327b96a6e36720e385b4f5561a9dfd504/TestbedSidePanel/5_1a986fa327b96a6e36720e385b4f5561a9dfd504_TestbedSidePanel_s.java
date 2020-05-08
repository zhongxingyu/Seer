 /*******************************************************************************
  * Copyright (c) 2013, Daniel Murphy
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  * 	* Redistributions of source code must retain the above copyright notice,
  * 	  this list of conditions and the following disclaimer.
  * 	* Redistributions in binary form must reproduce the above copyright notice,
  * 	  this list of conditions and the following disclaimer in the documentation
  * 	  and/or other materials provided with the distribution.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  ******************************************************************************/
 package org.jbox2d.testbed.framework.j2d;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.ListCellRenderer;
 import javax.swing.SwingConstants;
 import javax.swing.border.EtchedBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.jbox2d.common.Color3f;
 import org.jbox2d.dynamics.ContactManager;
 import org.jbox2d.testbed.framework.TestbedController;
 import org.jbox2d.testbed.framework.TestbedModel;
 import org.jbox2d.testbed.framework.TestbedModel.ListItem;
 import org.jbox2d.testbed.framework.TestbedSetting;
 import org.jbox2d.testbed.framework.TestbedSetting.SettingType;
 import org.jbox2d.testbed.framework.TestbedSettings;
 import org.jbox2d.testbed.framework.TestbedTest;
 
 
 /**
  * The testbed side panel. Facilitates test and setting changes.
  * 
  * @author Daniel Murphy
  */
 @SuppressWarnings("serial")
 public class TestbedSidePanel extends JPanel implements ChangeListener, ActionListener {
 
   private static final String SETTING_TAG = "settings";
   private static final String LABEL_TAG = "label";
 
   final TestbedModel model;
   final TestbedController controller;
 
   public JComboBox tests;
 
   private JButton resetButton = new JButton("Reset");
   private JButton quitButton = new JButton("Quit");
   private static JButton nextLevel = new JButton("Next Level");
   private static JButton playButton = new JButton("Play");
   private JLabel levelNumberLabel, titleLabel, descriptionLabel;
 
   public enum Counter { 
 	  POSITIVES ("Positives"), NEGATIVES ("Negatives");
 	  
 	  String name;
 	  JLabel label;
 	  int count;
 	  private Counter(String name) {
 		  this.name = name;
 		  this.count = 0;
 	  }
 	  public String getName() {
 		  return name;
 	  }
 	  public void setLabel(JLabel label) {
 		  this.label = label;
 	  }
 	  public void setCount(int count) {
 		  this.count = count;
 	  }
 	  public int getCount() {
 		  return count;
 	  }
 	  public void updateLabel() {
 		  label.setText(name + ": " + count);
 	  }
 	  public int updateCount(){
 	    updateCounter(this, count-1);
	    System.out.println("Joanie ");
	    System.out.println(getCount());
 	    return getCount();
 	  }
   };
   
   public TestbedSidePanel(TestbedModel argModel, TestbedController argController) {
     model = argModel;
     controller = argController;
     initComponents();
     addListeners();
 
     model.addTestChangeListener(new TestbedModel.TestChangedListener() {
       @Override
       public void testChanged(TestbedTest argTest, int argIndex) {
         tests.setSelectedIndex(argIndex);
         
       }
     });
   }
 
   public void initComponents() {
     setLayout(new BorderLayout());
     setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 
     TestbedSettings settings = model.getSettings();
 
     JPanel top = new JPanel();
     top.setLayout(new GridLayout(0, 1));
     top.setBorder(BorderFactory.createCompoundBorder(new EtchedBorder(EtchedBorder.LOWERED),
         BorderFactory.createEmptyBorder(10, 10, 10, 10)));
     tests = new JComboBox(model.getComboModel());
     tests.setMaximumRowCount(30);
     tests.setMaximumSize(new Dimension(250, 20));
     tests.addActionListener(this);
     tests.setRenderer(new ListCellRenderer() {
       JLabel categoryLabel = null;
       JLabel testLabel = null;
 
       @Override
       public Component getListCellRendererComponent(JList list, Object ovalue,
           int index, boolean isSelected, boolean cellHasFocus) {
         ListItem value = (ListItem) ovalue;
         if (value.isCategory()) {
           if (categoryLabel == null) {
             categoryLabel = new JLabel();
             categoryLabel.setOpaque(true);
             categoryLabel.setBackground(new Color(.5f, .5f, .6f));
             categoryLabel.setForeground(Color.white);
             categoryLabel.setHorizontalAlignment(SwingConstants.CENTER);
             categoryLabel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
           }
           categoryLabel.setText(value.category);
           return categoryLabel;
         } else {
           if (testLabel == null) {
             testLabel = new JLabel();
             testLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 1, 0));
           }
 
           testLabel.setText(value.test.getTestName());
 
           if (isSelected) {
             testLabel.setBackground(list.getSelectionBackground());
             testLabel.setForeground(list.getSelectionForeground());
           } else {
             testLabel.setBackground(list.getBackground());
             testLabel.setForeground(list.getForeground());
           }
           return testLabel;
         }
       }
     });
 
     addLevelDescriptionArea(top);
     updateLevelDescription(1, "Get charged up!", "Fun fun fun! Try to get to the star!"); // example to update level description
 
     top.add(new JLabel("Choose a level:"));
     top.add(tests);
 
     addCounters(top);
     
 
     add(top, "North");
 
     JPanel middle = new JPanel();
     middle.setLayout(new GridLayout(0, 1));
     middle.setBorder(BorderFactory.createCompoundBorder(new EtchedBorder(EtchedBorder.LOWERED),
         BorderFactory.createEmptyBorder(5, 10, 5, 10)));
 
     add(middle, "Center");
     
     
 
     resetButton.setAlignmentX(CENTER_ALIGNMENT);
 
     playButton.setAlignmentX(CENTER_ALIGNMENT);
     
     quitButton.setAlignmentX(CENTER_ALIGNMENT);
     
     nextLevel.setAlignmentX(CENTER_ALIGNMENT);
     
     nextLevel.setEnabled(false);
     
     Box buttonGroups = Box.createHorizontalBox();
     JPanel buttons1 = new JPanel();
     buttons1.setLayout(new GridLayout(0, 1));
     buttons1.add(resetButton);
 
 
 
 
     JPanel buttons3 = new JPanel();
     buttons3.setLayout(new GridLayout(0, 1));
     buttons3.add(quitButton);
     
     JPanel buttons4 = new JPanel();
     buttons4.setLayout(new GridLayout(0, 1));
     buttons4.add(nextLevel);
 
     JPanel buttons5 = new JPanel();
     buttons5.setLayout(new GridLayout(0, 1));
     buttons5.add(playButton);
     
     buttonGroups.add(buttons1);
     buttonGroups.add(buttons5);
     buttonGroups.add(buttons4);
     buttonGroups.add(buttons3);
     
     
     add(buttonGroups, "South");
   }
 
   public void addListeners() {
     
 
     resetButton.addActionListener(new ActionListener() {
       @Override
       public void actionPerformed(ActionEvent e) {
         model.getSettings().pause=true;
         ContactManager.win=false;
         controller.resetTest();
         model.getDebugDraw().drawString(20,200, "", Color3f.WHITE);
         playButton.setEnabled(true);
         controller.enableAddCharges();
       }
     });
 
     quitButton.addActionListener(new ActionListener() {
       @Override
       public void actionPerformed(ActionEvent e) {
         System.exit(0);
       }
     });
     playButton.addActionListener(new ActionListener() {
       @Override
       public void actionPerformed(ActionEvent e) {
         controller.disableAddCharges();
         playButton.setEnabled(false);
         model.getSettings().pause=false;
       }
     });
     nextLevel.addActionListener(new ActionListener() {
       @Override
       public void actionPerformed(ActionEvent e) {
         controller.playTest(tests.getSelectedIndex()+1);
         nextLevel.setEnabled(false);
         model.getSettings().pause=true;
         ContactManager.win=false;
         model.getDebugDraw().drawString(20,200, "", Color3f.WHITE);
         playButton.setEnabled(true);
       }
     });
     
   }
 
   
   private void addCounters(JPanel panel) {
 	  for (Counter counter : Counter.values()) {
 		  JPanel counterPanel = new JPanel();
 		  panel.add(counterPanel);
 		  
 		  JLabel label = new JLabel();
 		  counterPanel.add(label);
 		  counter.setLabel(label);
 		  counter.updateLabel();
 		  if (counter.name=="Positives"){
 		    model.posCounter=counter;
 		    updateCounter(counter,5);
 		  }
 		  if (counter.name=="Negatives"){
 		    model.negCounter=counter;
 		    updateCounter(counter,5);
 		  }
 	  }
   }
   
   public static void updateCounter(Counter counter, int count) {
 	  counter.setCount(count);
 	  counter.updateLabel();
   }
   
   
   private void addSettings(JPanel argPanel, TestbedSettings argSettings, SettingType argIgnore) {
     for (TestbedSetting setting : argSettings.getSettings()) {
       if (setting.settingsType == argIgnore) {
         continue;
       }
       switch (setting.constraintType) {
         case RANGE:
           JLabel text = new JLabel(setting.name + ": " + setting.value);
           JSlider slider = new JSlider(setting.min, setting.max, setting.value);
           slider.setMaximumSize(new Dimension(200, 20));
           slider.addChangeListener(this);
           slider.setName(setting.name);
           slider.putClientProperty(SETTING_TAG, setting);
           slider.putClientProperty(LABEL_TAG, text);
           argPanel.add(text);
           argPanel.add(slider);
           break;
         case BOOLEAN:
           JCheckBox checkbox = new JCheckBox(setting.name);
           checkbox.setSelected(setting.enabled);
           checkbox.addChangeListener(this);
           checkbox.putClientProperty(SETTING_TAG, setting);
           argPanel.add(checkbox);
           break;
       }
     }
   }
   
   private void addLevelDescriptionArea(JPanel panel) {
 	  levelNumberLabel = new JLabel("", JLabel.CENTER);
 	  Font font = new Font("Serif", Font.PLAIN, 18);
 	  levelNumberLabel.setFont(font);
 	  
 	  titleLabel = new JLabel("", JLabel.CENTER);
 	  titleLabel.setFont(font);
 	  descriptionLabel = new JLabel();
 	  
 	  panel.add(levelNumberLabel);
 	  panel.add(titleLabel);
 	  panel.add(descriptionLabel);
 	  
   }
   
   public void updateLevelDescription(int levelNumber, String title, String description) {
 	  levelNumberLabel.setText("Level: " + levelNumber);
 	  titleLabel.setText(title);
 	  descriptionLabel.setText(description);
   }
 
   public void stateChanged(ChangeEvent e) {
     JComponent component = (JComponent) e.getSource();
     TestbedSetting setting = (TestbedSetting) component.getClientProperty(SETTING_TAG);
 
     switch (setting.constraintType) {
       case BOOLEAN:
         JCheckBox box = (JCheckBox) e.getSource();
         setting.enabled = box.isSelected();
         break;
       case RANGE:
         JSlider slider = (JSlider) e.getSource();
         setting.value = slider.getValue();
         JLabel label = (JLabel) slider.getClientProperty(LABEL_TAG);
         label.setText(setting.name + ": " + setting.value);
         break;
     }
   }
 
   public void actionPerformed(ActionEvent e) {
     controller.playTest(tests.getSelectedIndex());
   }
   public static void enableNextLevel(){
     nextLevel.setEnabled(true);
   }
   public static void disableNextLevel(){
     nextLevel.setEnabled(false);
   }
 
 }
