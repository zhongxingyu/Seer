 /*
 * Copyright 2011 Danish Maritime Safety Administration. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Danish Maritime Safety Administration ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of Danish Maritime Safety Administration.
 *
 */
 package dk.frv.eavdam.buttons;
 
 import com.bbn.openmap.gui.OMToolComponent;
 import com.bbn.openmap.gui.OpenMapFrame;
 import com.bbn.openmap.gui.Tool;
 import dk.frv.eavdam.data.EAVDAMData;
 import dk.frv.eavdam.data.Options;
 import dk.frv.eavdam.io.EmailSender;
 import dk.frv.eavdam.menus.OptionsMenuItem;
 import dk.frv.eavdam.utils.XMLHandler;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Frame;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import javax.mail.MessagingException;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.JToolBar;
 
 /**
  * Class for the button in the toolbar that sends station data to defined e-mail recipients.
  */
 public class SendEmailButton extends OMToolComponent implements ActionListener, Tool {
 
 	private static final long serialVersionUID = 6706092075829947101L;
 	
 	private JButton sendEmailButton = null;
 	private JToolBar jToolBar;
     private OpenMapFrame openMapFrame;
  
  	/**
 	 * Creates the button.
 	 */
 	public SendEmailButton() {
 		super();
 		
 		ImageIcon icon = new ImageIcon("share/data/images/email.png");
 		if (icon != null || icon.getImage() == null) {
 			sendEmailButton = new JButton(icon);
 		} else {
 			sendEmailButton = new JButton("E-mail");
 		}
 
         sendEmailButton.setBorder(BorderFactory.createEmptyBorder());
 		sendEmailButton.addActionListener(this);
 		sendEmailButton.setToolTipText("Send the data file to e-mail recipients");
 		add(sendEmailButton);
 		
 	}
 	
 	 public void actionPerformed(ActionEvent e) {
 	    
 		EAVDAMData exportData = XMLHandler.exportData();	
 
		if (exportData.getStations() == null || exportData.getStations().length == 0) {
             JOptionPane.showMessageDialog(openMapFrame, "You have no own stations", "Error", JOptionPane.ERROR_MESSAGE);
 		} else {
 			 int response = JOptionPane.showConfirmDialog(openMapFrame, "This will e-mail the latest data file to all defined recipients. Are you sure you want to do this?", "Confirm action", JOptionPane.YES_NO_OPTION);
 			 if (response == JOptionPane.YES_OPTION) {
 				Options options = OptionsMenuItem.loadOptions();
 				try {
 					if (options.getEmailTo() == null || options.getEmailTo().isEmpty()) {
 						JOptionPane.showMessageDialog(openMapFrame, "No e-mail recipients defined!"); 
 					} else {
 						EmailSender.sendDataToEmail(options.getEmailTo(), options.getEmailFrom(), options.getEmailSubject(),
 							options.getEmailHost(), options.isEmailAuth(), options.getEmailUsername(), options.getEmailPassword());
 						JOptionPane.showMessageDialog(openMapFrame, "E-mails were sent succesfully."); 
 					}
 				} catch (IOException ex) {
 					ex.printStackTrace();
 					JOptionPane.showMessageDialog(openMapFrame, "The following error occurred when trying to send the e-mails: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);      
 				} catch (MessagingException ex) {
 					ex.printStackTrace();
 					JOptionPane.showMessageDialog(openMapFrame, "The following error occurred when trying to send the e-mails: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
 				}
 			 } else if (response == JOptionPane.NO_OPTION) { 
 				// ignore        
 			 }
 		}
      }
 
 	 public void findAndInit(Object obj) {
 		if (obj instanceof OpenMapFrame) {
 			this.openMapFrame = (OpenMapFrame) obj;
 		}
 	}	    
 
 	 public void findAndUndo(Object obj) {}
 
 }
