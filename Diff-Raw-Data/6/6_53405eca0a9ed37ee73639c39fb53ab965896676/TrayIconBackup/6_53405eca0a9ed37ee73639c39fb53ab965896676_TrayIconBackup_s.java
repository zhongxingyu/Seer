 
 /*	Copyright 2013 Florian Mahon <florian@faivre-et-mahon.ch>
  * 
  *    This file is part of backuptools.
  *    
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ch.fetm.backuptools.gui; 
  
import java.awt.AWTException;
 import java.awt.Image;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.SystemTray;
 import java.awt.TrayIcon;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
import ch.fetm.backuptools.BackupAgentFactory;
 import ch.fetm.backuptools.common.BackupAgenConfigManager;
 import ch.fetm.backuptools.common.BackupAgentConfig;
 import ch.fetm.backuptools.common.IBackupAgent;
  
 public class TrayIconBackup {
 	private static BackupAgentConfig _config;	
 	private IBackupAgent  agent;
     private TrayIcon trayIcon;
     private SystemTray tray;
 	
     public static void main(String[] args) {	
     	
         setLookAndFeel();        
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
             	IBackupAgent agent;
             	
             	_config = BackupAgenConfigManager.readConfigurationInFile();
 
             	if(_config == null){
                 	_config = new BackupAgentConfig();
                 	JDialogBackuptoolsConfiguration.showDialog(_config);
                 	BackupAgenConfigManager.writeConfigurationFile(_config);
                 }
 
             	agent = BackupAgentFactory.create(_config);
             	
             	new TrayIconBackup(agent);
             }
         });
     }
 
 	private static void setLookAndFeel() {
 		try {
             UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
         } catch (UnsupportedLookAndFeelException ex) {
             try {
 				UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
         } catch (Exception e){
         	e.printStackTrace();
         }
 	}
     
 	private void onClickConfiguration() {
 	 if(JDialogBackuptoolsConfiguration.OK_RESULT == JDialogBackuptoolsConfiguration.showDialog(_config)){
 		 agent = BackupAgentFactory.create(_config);
 	 }
 	}
 	
 	public TrayIconBackup(IBackupAgent agent) {
     	this.agent = agent;
         if (!SystemTray.isSupported()) {
             System.out.println("SystemTray is not supported");
             return;
         }
         PopupMenu popup = new PopupMenu();
         trayIcon = new TrayIcon(createImage("g3051.png", "Backuptools agent"));
         tray     = SystemTray.getSystemTray();
          
         MenuItem aboutItem         = new MenuItem("About");
         MenuItem configurationItem = new MenuItem("Configuration");
         MenuItem RunItem           = new MenuItem("Run backup");
         MenuItem restoreItem       = new MenuItem("Restore");
         MenuItem exitItem          = new MenuItem("Exit");
         
         popup.add(aboutItem);
         popup.addSeparator();
         popup.add(configurationItem);
         popup.add(RunItem);
         popup.add(restoreItem);
         popup.addSeparator();
         popup.add(exitItem);
          
         trayIcon.setPopupMenu(popup);
          
         try {
             tray.add(trayIcon);
             
         } catch (AWTException e) {
             System.out.println("TrayIcon could not be added.");
             return;
         }
         
         restoreItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				onClickRestore();
 			}
 		});
         exitItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				onClickExit();
 			}
 		});
         RunItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				onClickBackup();
 			}
 		});        
         configurationItem.addActionListener(new ActionListener() {			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				onClickConfiguration();
 			}
 
 		});
         aboutItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				onClickAbout();
 			}
 		});
     }
      
     protected void onClickAbout() {
 		JDialogAbout dialog = new JDialogAbout();
 		dialog.setVisible(true);
 	}
 
 	protected void onClickRestore() {
 		RestoreAgentGUI restore = new RestoreAgentGUI(agent);
 		restore.setVisible(true);
 	}
 
 	protected void onClickExit() {
 		tray.remove(trayIcon);
 		System.exit(0);
 	}
 
 	private void onClickBackup() {
 		agent.doBackup();
 	}
 
 	protected Image createImage(String path, String description) {
         URL imageURL = getClass().getClassLoader().getResource(path);
          
         if (imageURL == null) {
             System.err.println("Resource not found: " + path);
             return null;
         } else {
             return (new ImageIcon(imageURL, description)).getImage();
         }
     }
 }
