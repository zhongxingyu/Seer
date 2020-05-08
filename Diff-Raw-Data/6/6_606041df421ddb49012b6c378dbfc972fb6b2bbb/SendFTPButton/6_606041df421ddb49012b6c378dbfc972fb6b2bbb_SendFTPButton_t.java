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
 
 public class SendFTPButton extends OMToolComponent implements ActionListener, Tool {
 
 	private static final long serialVersionUID = 6706092075829947101L;
 	protected JButton sendFTPButton = null;
 	protected JToolBar jToolBar;
     private OpenMapFrame openMapFrame;
 	
 	private StationLayer stationLayer;
 	private EavdamMenu eavdamMenu;
  
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
 	    
 		XMLHandler.exportData();
 		 
 	    int response = JOptionPane.showConfirmDialog(openMapFrame, "This will send your latest data file to all defined ftp directories and download\n" +
 		    "new data files of other users from them. Are you sure you want to do this?", "Confirm action", JOptionPane.YES_NO_OPTION);
         if (response == JOptionPane.YES_OPTION) {
             Options options = OptionsMenuItem.loadOptions();
 			String ownFileName = XMLHandler.getLatestDataFileName();
 			if (ownFileName != null && ownFileName.indexOf("/") != -1) {
 				ownFileName = ownFileName.substring(ownFileName.lastIndexOf("/")+1);
 			}				
             List<FTP> ftps = options.getFTPs();
             if (ftps != null && !ftps.isEmpty()) {
                 String errors = "";				
                 for (FTP ftp : ftps) {
                     try {
                         FTPClient ftpClient = FTPHandler.connect(ftp);
 						FTPHandler.sendDataToFTP(ftpClient, XMLHandler.getLatestDataFileName());                                       
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
                 if (errors.isEmpty()) {
                     JOptionPane.showMessageDialog(openMapFrame, "Data exhanged succesfully.");
                 } else {
                    JOptionPane.showMessageDialog(openMapFrame, "The following ftp sites had problems when exchanging data:\n" + errors, "Error", JOptionPane.ERROR_MESSAGE); 
                 }
             } else {
                 JOptionPane.showMessageDialog(openMapFrame, "No FTP sites defined.", "Error", JOptionPane.ERROR_MESSAGE);         
             } 
         } else if (response == JOptionPane.NO_OPTION) { 
             // ignore        
         }
 		 
 		EAVDAMData data = XMLHandler.importData();
 		if (eavdamMenu != null) {
 			//eavdamMenu.setShowOnMapMenu(new ShowOnMapMenu(eavdamMenu));
 			if (eavdamMenu.getStationInformationMenu() != null && eavdamMenu.getStationInformationMenu().getStationInformationMenuItem() != null) {
 				eavdamMenu.getStationInformationMenu().getStationInformationMenuItem().setData(data);
 			}
 		}
 		if (stationLayer != null) {
 			stationLayer.updateStations();
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
