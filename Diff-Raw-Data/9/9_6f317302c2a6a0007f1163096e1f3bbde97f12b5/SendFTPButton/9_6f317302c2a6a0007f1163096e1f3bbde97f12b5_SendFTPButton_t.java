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
 import dk.frv.eavdam.data.FTP;
 import dk.frv.eavdam.data.Options;
 import dk.frv.eavdam.io.FTPHandler;
 import dk.frv.eavdam.io.derby.DerbyDBInterface;
 import dk.frv.eavdam.menus.EavdamMenu;
 import dk.frv.eavdam.menus.IssuesMenuItem;
 import dk.frv.eavdam.menus.ShowOnMapMenu;
 import dk.frv.eavdam.menus.OptionsMenuItem;
 import dk.frv.eavdam.layers.StationLayer;
 import dk.frv.eavdam.utils.DBHandler;
 import dk.frv.eavdam.utils.XMLHandler;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Frame;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.List;
 import javax.mail.MessagingException;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.JToolBar;
 import org.apache.commons.net.ftp.FTPClient;
 
 /**
  * Class representing the button in the toolbar that exchanges station data with defined ftp sites.
  */
 public class SendFTPButton extends OMToolComponent implements ActionListener, Tool {
 
 	private static final long serialVersionUID = 6706092075829947101L;
 	
 	private JButton sendFTPButton = null;
 	private JToolBar jToolBar;
     private OpenMapFrame openMapFrame;
 	
 	private StationLayer stationLayer;
 	private EavdamMenu eavdamMenu;
  
  	/**
 	 * Creates the button.
 	 */
 	public SendFTPButton() {
 		super();
 		
 		ImageIcon icon = new ImageIcon("share/data/images/ftp.png");
 		if (icon != null || icon.getImage() == null) {
 			sendFTPButton = new JButton(icon);
 		} else {
 			sendFTPButton = new JButton("FTP");
 		}
 
         sendFTPButton.setBorder(BorderFactory.createEmptyBorder());
 		sendFTPButton.addActionListener(this);
 		sendFTPButton.setToolTipText("Exchange data with ftp servers");
 		add(sendFTPButton);
 		
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 	    
 		EAVDAMData exportData = XMLHandler.exportData();
 		 
         String errors = "";			 
 		 
 		String infoText = "This will send your latest data file to all defined ftp directories and download\n" +
 			"new data files of other users from them. Are you sure you want to do this?";
		if (exportData.getStations() == null || exportData.getStations().length == 0) {
 			infoText =  "You have no own stations, so this will only download new data files of other\n" +
 				"users from the defined ftp directories. Are you sure you want to do this?";
 		}
 	    int response = JOptionPane.showConfirmDialog(openMapFrame, infoText, "Confirm action", JOptionPane.YES_NO_OPTION);
         if (response == JOptionPane.YES_OPTION) {
             Options options = OptionsMenuItem.loadOptions();
 			String ownFileName = XMLHandler.getLatestDataFileName();
 			if (ownFileName != null && ownFileName.indexOf("/") != -1) {
 				ownFileName = ownFileName.substring(ownFileName.lastIndexOf("/")+1);
 			}				
             List<FTP> ftps = options.getFTPs();
             if (ftps != null && !ftps.isEmpty()) {			
                 for (FTP ftp : ftps) {
                     try {
                         FTPClient ftpClient = FTPHandler.connect(ftp);
 						if (exportData.getStations() == null || exportData.getStations().length == 0) {
 							FTPHandler.deleteDataFromFTP(ftpClient, ownFileName);
 						} else {						
 							FTPHandler.sendDataToFTP(ftpClient, XMLHandler.getLatestDataFileName());                                       
 						}
 						FTPHandler.importDataFromFTP(ftpClient, XMLHandler.importDataFolder, ownFileName);
 						FTPHandler.disconnect(ftpClient);
                     } catch (IOException ex) {
                         System.out.println(ex.getMessage());
                         ex.printStackTrace();
                         errors += "- " + ex.getMessage() + "\n";
                     } catch (Exception ex) {
                         System.out.println(ex.getMessage());
                         ex.printStackTrace();
                         errors += "- " + ex.getMessage() + "\n";						
 					}
 				}
 				
 				EAVDAMData data = XMLHandler.importData();
 				IssuesMenuItem.healthCheckMayBeOutdated = true;
 				if (eavdamMenu != null) {
 					if (eavdamMenu.getStationInformationMenu() != null && eavdamMenu.getStationInformationMenu().getStationInformationMenuItem() != null) {
 						eavdamMenu.getStationInformationMenu().getStationInformationMenuItem().setData(data);
 					}
 				}
 				if (stationLayer != null) {
 					stationLayer.updateStations();
 				}
 
 				if (errors.isEmpty()) {
 					JOptionPane.showMessageDialog(openMapFrame, "Data exhanged succesfully.");
 				} else {
 					JOptionPane.showMessageDialog(openMapFrame, "The following ftp sites had problems when exchanging data:\n" + errors, "Error", JOptionPane.ERROR_MESSAGE); 
 				}
 				
             } else {
                 JOptionPane.showMessageDialog(openMapFrame, "No FTP sites defined.", "Error", JOptionPane.ERROR_MESSAGE);         
             } 
         }		 
 		
 	}
 
 	@Override
 	 public void findAndInit(Object obj) {
 		if (obj instanceof OpenMapFrame) {
 			this.openMapFrame = (OpenMapFrame) obj;
 		} else if (obj instanceof StationLayer) {
 		    this.stationLayer = (StationLayer) obj;
 		} else if (obj instanceof EavdamMenu) {
 			this.eavdamMenu = (EavdamMenu) obj;
 		}
 	}	    
 
 	 public void findAndUndo(Object obj) {}
 
 }
