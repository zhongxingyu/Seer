/* $Id: GeneralPanel.java,v 1.8 2006-09-07 14:54:02 hampelratte Exp $
  * 
  * Copyright (c) 2005, Henrik Niehaus & Lazy Bones development team
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  * 3. Neither the name of the project (Lazy Bones) nor the names of its 
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package lazybones.gui;
 
 import info.clearthought.layout.TableLayout;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 
 import lazybones.LazyBones;
 import lazybones.Logger;
 import lazybones.VDRConnection;
 
 public class GeneralPanel implements ActionListener {
     private Logger LOG = Logger.getLogger();
     
 	private final String lHost = LazyBones.getTranslation("host", "Host");
 
 	private final String lPort = LazyBones.getTranslation("port", "Port");
 
 	private final String lTimeout = LazyBones.getTranslation("timeout", "Timeout");
 
 	private final String lExperts = LazyBones.getTranslation("experts", "Experts");
     /*
     private final String lWOLEnabled = LazyBones.getTranslation("WOLEnabled", "Enable Wake-on-LAN");
     
     private final String lWOLMac = LazyBones.getTranslation("WOLMac", "Wake-on-LAN Mac-Adress");
     
     private final String lWOLBroadc = LazyBones.getTranslation("WOLBroadc", "Wake-on-LAN Broadcast-Adress");
     */
 	private final String lFuzzyness = LazyBones.getTranslation("percentageOfEquality",
 			"Fuzzylevel program titles");
 
 	private final String ttFuzzyness = LazyBones.getTranslation(
 			"percentageOfEquality.tooltip",
 			"Percentage of equality of program titles");
 
     private JTextField host;
 
     private JTextField port;
 
     private JTextField timeout;
     
     private JTextField tWOLMac;
     
     private JTextField tWOLBroadc;
     
     private JCheckBox cWOLEnabled;
 
     private JLabel labPercentageOfEquality;
     private JSpinner percentageOfEquality;
 
     private final String lSupressMatchDialog = LazyBones.getTranslation(
 			"supressMatchDialog", "Supress match dialog");
 
 	private final String ttSupressMatchDialog = LazyBones.getTranslation(
 			"supressMatchDialog.tooltip",
 			"Do not show EPG selection dialog for non matching VDR timer");
 
 	private JCheckBox supressMatchDialog;
     
     private final String lLogConnectionErrors = LazyBones.getTranslation(
             "logConnectionErrors",
             "Show error dialogs on connection problems");
     
     private JCheckBox logConnectionErrors;
     
     private final String lLogEPGErrors = LazyBones.getTranslation(
             "logEPGErrors",
             "Show error dialogs, if EPG data are missing");
     
     private JCheckBox logEPGErrors;
     
     private final String lShowTimerOptionsDialog = LazyBones.getTranslation(
             "showTimerOptionsDialog",
             "Show timer options dialog on timer creation");
     
     private JCheckBox showTimerOptionsDialog;
 	
 
 	public GeneralPanel() {
         initComponents();
     }
     
     private void initComponents() {
         host = new JTextField(10);
         host.setText(LazyBones.getProperties().getProperty("host"));
         port = new JTextField(10);
         port.setText(LazyBones.getProperties().getProperty("port"));
         timeout = new JTextField(10);
         timeout.setText(LazyBones.getProperties().getProperty("timeout"));
         
         cWOLEnabled = new JCheckBox();
         boolean wolEnabled = Boolean.TRUE.toString().equals(
                 LazyBones.getProperties().getProperty("WOLEnabled"));
         cWOLEnabled.setSelected(wolEnabled);
         cWOLEnabled.addActionListener(this);
         String mac = LazyBones.getProperties().getProperty("WOLMac");
         tWOLMac = new JTextField();
         tWOLMac.setText(mac);
         tWOLMac.setEnabled(wolEnabled);
         String broadc = LazyBones.getProperties().getProperty("WOLBroadc");
         tWOLBroadc = new JTextField();
         tWOLBroadc.setText(broadc);
         tWOLBroadc.setEnabled(wolEnabled);
 
         int percentageThreshold = Integer.parseInt(LazyBones.getProperties().getProperty(
                 "percentageThreshold"));
         percentageOfEquality = new JSpinner();
         percentageOfEquality.setModel(new SpinnerNumberModel(percentageThreshold,0,100,1));
         percentageOfEquality.setToolTipText(ttFuzzyness);
         ((JSpinner.DefaultEditor) percentageOfEquality.getEditor())
                 .getTextField().setColumns(2);
         labPercentageOfEquality = new JLabel(lFuzzyness);
         labPercentageOfEquality.setLabelFor(percentageOfEquality);
 		labPercentageOfEquality.setToolTipText(ttFuzzyness);
 
 		supressMatchDialog = new JCheckBox();
         supressMatchDialog.setSelected(Boolean.TRUE.toString().equals(
 				LazyBones.getProperties().getProperty("supressMatchDialog")));
         supressMatchDialog.setToolTipText(ttSupressMatchDialog);
         
         logConnectionErrors = new JCheckBox();
         logConnectionErrors.setSelected(Boolean.TRUE.toString().equals(
                 LazyBones.getProperties().getProperty("logConnectionErrors")));
         
         logEPGErrors = new JCheckBox();
         logEPGErrors.setSelected(Boolean.TRUE.toString().equals(
                 LazyBones.getProperties().getProperty("logEPGErrors")));
         
         showTimerOptionsDialog = new JCheckBox();
         showTimerOptionsDialog.setSelected(Boolean.TRUE.toString().equals(
                 LazyBones.getProperties().getProperty("showTimerOptionsDialog")));
     }
 
     public JPanel getPanel() {
         final double P = TableLayout.PREFERRED;
         double[][] size = {{10, P, 10, P, 10},
                            {10, P, 10, P, 10, P, 20, P, 10}};
         TableLayout surround = new TableLayout(size);
 		JPanel panel = new JPanel(surround);
 
 		panel.add(new JLabel(lHost), "1,1,1,1");
 		panel.add(host,              "3,1,3,1");
 		
 		panel.add(new JLabel(lPort), "1,3,1,3");
 		panel.add(port,              "3,3,3,3");
 		
 		panel.add(new JLabel(lTimeout), "1,5,1,5");
 		panel.add(timeout,              "3,5,3,5");
 
         double[][] size2 = {{10, P, 10, P, 10},
                             {10, P, 10, P, 10, P, 10, P, 10, P, 10, P, 10, P, 10, P, 10}};
         TableLayout layout2 = new TableLayout(size2);
         JPanel experts = new JPanel(layout2);
         experts.setBorder(BorderFactory.createTitledBorder(lExperts));
         /*
         experts.add(new JLabel(lWOLEnabled), "1,1,1,1");
         experts.add(cWOLEnabled,             "3,1,3,1");
         
         experts.add(new JLabel(lWOLMac),     "1,3,1,3");
         experts.add(tWOLMac,                 "3,3,3,3");
         
         experts.add(new JLabel(lWOLBroadc),  "1,5,1,5");
         experts.add(tWOLBroadc,              "3,5,3,5");
 		*/
         experts.add(labPercentageOfEquality, "1,7,1,7");
         experts.add(percentageOfEquality,    "3,7,3,7");
 		
         experts.add(new JLabel(lSupressMatchDialog),     "1,9,1,9");
         experts.add(supressMatchDialog,                  "3,9,3,9");
         
         experts.add(new JLabel(lLogConnectionErrors),    "1,11,1,11");
         experts.add(logConnectionErrors,                 "3,11,3,11");
         
         experts.add(new JLabel(lLogEPGErrors),           "1,13,1,13");
         experts.add(logEPGErrors,                        "3,13,3,13");
         
         experts.add(new JLabel(lShowTimerOptionsDialog), "1,15,1,15");
         experts.add(showTimerOptionsDialog,              "3,15,3,15");
 
         panel.add(experts, "1,7,3,7");
 		return panel;
 	}
 
     public void saveSettings() {
         String h = host.getText();
         int p = 2001;
         int t = 500;
         try {
             p = Integer.parseInt(port.getText());
         } catch (NumberFormatException nfe) {
             String mesg = LazyBones.getTranslation(
                    "invalidPort",
                    "<html>You have entered a wrong value for the port.<br>Port 2001 will be used instead.</html>");
             LOG.log(mesg, Logger.OTHER, Logger.ERROR);
             p = 2001;
             port.setText("2001");
         }
         try {
             t = Integer.parseInt(timeout.getText());
         } catch (NumberFormatException nfe) {
             String mesg = LazyBones.getTranslation(
                                             "invalidTimeout",
                                             "<html>You have entered a wrong value for the timeout.<br>A timeout of 500 ms will be used instead.</html>");
             LOG.log(mesg, Logger.OTHER, Logger.ERROR);
             t = 500;
            timeout.setText("500");
         }
 
         VDRConnection.host = h;
         VDRConnection.port = p;
         VDRConnection.timeout = t;
 
         LazyBones.getProperties().setProperty("host", h);
         LazyBones.getProperties().setProperty("port", Integer.toString(p));
         LazyBones.getProperties().setProperty("timeout", Integer.toString(t));
         LazyBones.getProperties().setProperty("WOLMac", tWOLMac.getText());
         LazyBones.getProperties().setProperty("WOLBroadc", tWOLBroadc.getText());
         LazyBones.getProperties().setProperty("percentageThreshold",
                 percentageOfEquality.getValue().toString());
         LazyBones.getProperties().setProperty("supressMatchDialog",
 				"" + supressMatchDialog.isSelected());
         LazyBones.getProperties().setProperty("logConnectionErrors",
                 "" + logConnectionErrors.isSelected());
         LazyBones.getProperties().setProperty("logEPGErrors",
                 "" + logEPGErrors.isSelected());
         LazyBones.getProperties().setProperty("showTimerOptionsDialog",
                 "" + showTimerOptionsDialog.isSelected());
         LazyBones.getProperties().setProperty("WOLEnabled",
                 "" + cWOLEnabled.isSelected());
     }
 
     public void actionPerformed(ActionEvent e) {
         if(e.getSource() == cWOLEnabled) {
             tWOLBroadc.setEnabled(cWOLEnabled.isSelected());
             tWOLMac.setEnabled(cWOLEnabled.isSelected());
         }
     }
 }
