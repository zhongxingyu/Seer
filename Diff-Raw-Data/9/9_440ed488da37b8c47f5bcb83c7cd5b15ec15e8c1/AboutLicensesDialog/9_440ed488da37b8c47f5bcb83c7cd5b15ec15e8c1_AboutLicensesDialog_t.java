 /*******************************************************************************
  * Mission Control Technologies is Copyright 2007-2012 NASA Ames Research Center
  *
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use 
  * this file except in compliance with the License. See the MCT Open Source 
  * Licenses file distributed with this work for additional information regarding copyright 
  * ownership. 
  * You may obtain a copy of the License at 
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  *
  * Unless required by applicable law or agreed to in writing, software distributed 
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES 
  * OR CONDITIONS OF ANY KIND, either express or implied. See the License for 
  * the specific language governing permissions and limitations under the License.
  *******************************************************************************/
 package gov.nasa.arc.mct.gui.dialogs;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Properties;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.border.EmptyBorder;
 
 @SuppressWarnings("serial")
 public class AboutLicensesDialog extends JDialog {
 
    private static ImageIcon mctLogoIcon = new ImageIcon(AboutLicensesDialog.class.getResource("/images/mctlogo.png"));
 
     public AboutLicensesDialog(JFrame frame) {
         super(frame);
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
         Image image = mctLogoIcon.getImage().getScaledInstance(320, 80, Image.SCALE_SMOOTH);
         JLabel label = new JLabel(new ImageIcon(image));
         JPanel labelPanel = new JPanel();
         labelPanel.setBackground(Color.white);
         labelPanel.add(label, BorderLayout.CENTER);
         labelPanel.setBorder(new EmptyBorder(5,5,5,5));
         Container contentPane = getContentPane();
         contentPane.setLayout(new BorderLayout());
         
         contentPane.add(labelPanel, BorderLayout.NORTH);
         
         // Modified the AboutDialog to add the Version and Build numbers to the screen - JOe...
         
         JTextArea license = new JTextArea(100, 100);
         license.setText("Mission Control Technologies, Copyright (c) 2009-2012, United States Government as represented by the Administrator of the National Aeronautics and Space Administration. All rights reserved.\n\nThe MCT platform is licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this application except in compliance with the License. You may obtain a copy of the License at\nhttp://www.apache.org/licenses/LICENSE-2.0.\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.\n\nMCT includes source code licensed under additional open source licenses, as described below. See the MCT Open Source Licenses file included with this distribution for additional information.\n\n\u2022  Apache Ant, Apache Batik, Apache Commons Collection, Apache Commons Codec, Apache Derby, Apache Felix, and Apache log4j are Copyright (c) 1999-2012, the Apache Software Foundation. Licensed under the Apache 2.0 license.\nhttp://www.apache.org/licenses/LICENSE-2.0\n\n\u2022  ANTR 3 is Copyright (c) 2010 Terence Parr. All rights reserved. Licensed under the ANTR 3 BSD license.\nhttp://www.antlr.org/license.html\n\n\u2022  Hibernate, c3po, dom4j, javassist, and Java Transaction API are Copyright (c) 2001-2012 by Red Hat, Inc. All rights reserved. Licensed under the GNU Lesser General Public License.\nhttp://olex.openlogic.com/licenses/lgpl-v2_1-license\n\n\u2022  MySQL is Copyright (c) 1997, 2012, Oracle and/or its affiliates. All rights reserved. Licensed under the GNU General Public License.\nhttp://www.gnu.org/licenses/gpl.html.\n\n\u2022  Oracle Berkeley DB Java Edition is Copyright (c) 2002, 2012 Oracle and/or its affiliates. All rights reserved.\nLicensed under the Open Source License for Oracle Berkeley DB Java Edition.\nhttp://www.oracle.com/technetwork/database/berkeleydb/downloads/jeoslicense-086837.html\n\n\u2022  slf4j is Copyright (c) 2004-2011 QOS.ch All rights reserved. Licensed under the MIT license.\nhttp://www.slf4j.org/license.html");
         license.setLineWrap(true);
         license.setWrapStyleWord(true);
         license.setEditable(false);
         JPanel licensePanel = new JPanel(new GridLayout(0,  1));
         licensePanel.add(license);
         licensePanel.setBackground(Color.white);
         licensePanel.setBorder(BorderFactory.createEmptyBorder(20,40, 20, 40));
         
         contentPane.add(licensePanel, BorderLayout.CENTER);
         
         JPanel panel = new JPanel();
         panel.setBackground(Color.white);
         JButton close = new JButton("Close");
         close.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 AboutLicensesDialog.this.setVisible(false);
             }
 
         });
         panel.add(close);
         contentPane.add(panel, BorderLayout.SOUTH);
         setBackground(Color.WHITE);
         setSize(800, 730);
         setResizable(false);
         setLocationRelativeTo(frame);
         setTitle("About MCT Licenses");
     }
     
     public static String getBuildNumber() {
         String buildnumber = "Not Found";
         try {
             Properties p = new Properties();
             p.load(ClassLoader.getSystemResourceAsStream("properties/version.properties"));
             buildnumber = p.getProperty("build.number");
             
         } catch (Exception e) {
             // if not found, just ignore any exceptions - it's not critical...
         }
         
         return buildnumber;
         
     }
 
 }
