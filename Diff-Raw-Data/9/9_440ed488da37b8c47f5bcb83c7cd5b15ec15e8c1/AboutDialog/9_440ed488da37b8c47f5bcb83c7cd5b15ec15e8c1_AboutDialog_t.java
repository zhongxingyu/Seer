 /*******************************************************************************
  * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
  * as represented by the Administrator of the National Aeronautics and Space 
  * Administration. All rights reserved.
  *
  * The MCT platform is licensed under the Apache License, Version 2.0 (the 
  * "License"); you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
  * License for the specific language governing permissions and limitations under 
  * the License.
  *
  * MCT includes source code licensed under additional open source licenses. See 
  * the MCT Open Source Licenses file included with this distribution or the About 
  * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
  * information. 
  *******************************************************************************/
 /**
  * AboutDialog.java Aug 18, 2008
  * 
  * This code is property of the National Aeronautics and Space Administration
  * and was produced for the Mission Control Technologies (MCT) Project.
  * 
  */
 package gov.nasa.arc.mct.gui.dialogs;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.MessageFormat;
 import java.util.Properties;
 import java.util.ResourceBundle;
 
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
 public class AboutDialog extends JDialog {
 
     private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("AboutResource");
    private static ImageIcon mctLogoIcon = new ImageIcon(AboutDialog.class.getResource("/images/mctlogo.png"));
 
     public static final String MCT_VERSION  = "mct.version";
     public static final String MCT_BUILD    = "mct.build";
     public static final String MCT_REVISION = "mct.revision";
     
     public AboutDialog(JFrame frame) {
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
         Properties version = getVersionProperties();
         String versioning = MessageFormat.format(BUNDLE.getString("Versioning"),
                                 version.getProperty(MCT_VERSION),
                                 version.getProperty(MCT_BUILD),
                                 version.getProperty(MCT_REVISION));
         String about     = BUNDLE.getString("About");
                 
         JTextArea license = new JTextArea(120, 100);
         license.setText(versioning + about);
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
                 AboutDialog.this.setVisible(false);
             }
 
         });
         panel.add(close);
         contentPane.add(panel, BorderLayout.SOUTH);
         setBackground(Color.WHITE);
         setSize(460, 600);
         setResizable(false);
         setLocationRelativeTo(frame);
         setTitle("About MCT");
     }
     
     private static Properties versionProperties = null;
     public static Properties getVersionProperties() {
         
         if (versionProperties == null) {
             versionProperties = new Properties();
             try {            
                 versionProperties.load(ClassLoader.getSystemResourceAsStream("properties/version.properties"));            
             } catch (Exception e) {
                 versionProperties.setProperty(MCT_VERSION, "unknown (missing version.properties)");
                 versionProperties.setProperty(MCT_BUILD, "unknown (missing version.properties)");
                 versionProperties.setProperty(MCT_REVISION, "unknown (missing version.properties)");
             }
         }
         
         return versionProperties;
         
     }
     
     public static String getBuildNumber() {
         return getVersionProperties().getProperty(MCT_BUILD);
     }
 
 }
