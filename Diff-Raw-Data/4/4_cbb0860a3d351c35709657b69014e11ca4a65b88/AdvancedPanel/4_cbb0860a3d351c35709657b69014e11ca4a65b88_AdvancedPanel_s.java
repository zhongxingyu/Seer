 package com.mh.ui;
 
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import com.Main;
 import com.mh.model.HMConstants;
 import com.mh.service.BackupService;
 import com.mh.ui.exception.OnFailure;
 import com.mh.ui.exception.OnSuccess;
 import com.mh.ui.util.Utils;
 import com.mh.ui.validation.ValidationMsg;
 import com.mh.ui.validation.Validator;
 import com.mh.ui.validation.annotations.Required;
 import com.mh.ui.validation.event.ValidationEvent;
 import com.mh.ui.validation.listener.ValidationListener;
 
 public class AdvancedPanel extends JPanel{
 	
 	private Main main;
 	@Required(message="Backup file can't be empty",target="backupFile")
 	private JTextField backupFile;
 	private JFileChooser backupFileChooser;
 	@Required(message="Restore file can't be empty",target="restoreFile")
 	private JTextField restoreFile;
 	private JFileChooser restoreFileChooser;
 	private JButton bChooser;
 	private JButton backup;
 	private JButton rChooser;
 	private JButton restore;
 	private String backupPath=null;
 	private String restorePath=null;
 	
 	protected boolean hasErrors=false;
 	protected List<ValidationMsg> resultList;
 	List<ValidationListener> validationRegistry;
 	
 	public AdvancedPanel(Main main){
 		this.main =main;
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{10, 20, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gridBagLayout.columnWeights = new double[]{0.2, 0.1, 0.5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 		
 		JLabel backupfileName = new JLabel("BackUp File :");
 		GridBagConstraints gbc_backupfileName = Utils.getConStraints(1,2,null);
 		add(backupfileName, gbc_backupfileName);
 		
		backupFile = new JTextField(Utils.getConfigValue("backupChooserDir")+"backup.sql");
 		GridBagConstraints gbc_backupFile = Utils.getConStraints(2,2,null);
 		gbc_backupFile.fill = GridBagConstraints.HORIZONTAL;
 		add(backupFile, gbc_backupFile);
 		
 		bChooser = new JButton("Location");
 		GridBagConstraints gbc_bChooser = Utils.getConStraints(3,2,null);
 		add(bChooser, gbc_bChooser);
 		bChooser.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				backupPath="";
 				if(!validateThis("backupFile")){
 					backupFileChooser =  new JFileChooser();
 					File []result= Utils.choosedFiles(backupFileChooser,AdvancedPanel.this.main,HMConstants.BACKUP_DIR,JFileChooser.DIRECTORIES_ONLY,false);
 					if(result != null){
 						for(File f: result){
 							backupPath = f.getAbsolutePath().toString();
 							break;
 						}
 						String []file =(backupFile.getText()!=null)?backupFile.getText().split(Pattern.quote(File.separator)) : new String[]{"backup.sql"};
 						int length = file.length;
 						String actualFilename = file[length-1];
 						backupPath+=File.separator+actualFilename;
 						backupFile.setText(backupPath);
 					}
 				}
 			}
 
 		});
 		
 		backup = new JButton("Backup");
 		GridBagConstraints gbc_backup = Utils.getConStraints(4,2,null);
 		add(backup, gbc_backup);
 		backup.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(!validateThis("backupFile")){
 					try {
 						new BackupService().backupDB(Utils.getConfigValue("DBName"), 
 								Utils.getConfigValue("DBUserName"), Utils.getConfigValue("DBPassword"), backupFile.getText());
 					} catch (OnSuccess e) {
 						JOptionPane.showMessageDialog(AdvancedPanel.this, e.getMessage());
 					} catch (OnFailure e) {
 						JOptionPane.showMessageDialog(AdvancedPanel.this, e.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
 					} catch (Throwable e) {
 						JOptionPane.showMessageDialog(AdvancedPanel.this, e.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
 						e.printStackTrace();
 					}
 				}
 			}
 			
 		});
 		
 		//restore
 		JLabel restorefileName = new JLabel("Restore File :");
 		GridBagConstraints gbc_restorefileName = Utils.getConStraints(1,4,null);
 		add(restorefileName, gbc_restorefileName);
 		
 		restoreFile = new JTextField();
 		GridBagConstraints gbc_restoreFile = Utils.getConStraints(2,4,null);
 		gbc_restoreFile.fill = GridBagConstraints.HORIZONTAL;
 		add(restoreFile, gbc_restoreFile);
 		
 		rChooser = new JButton("File");
 		GridBagConstraints gbc_rChooser = Utils.getConStraints(3,4,null);
 		add(rChooser, gbc_rChooser);
 		rChooser.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				restorePath = "";
 					restoreFileChooser =  new JFileChooser();
 					File []result= Utils.choosedFiles(restoreFileChooser,AdvancedPanel.this.main,HMConstants.BACKUP_DIR,JFileChooser.FILES_ONLY,false);
 					if(result != null){
 						for(File f: result){
 							restorePath = f.getAbsolutePath().toString();
 							break;
 						}
 						restoreFile.setText(restorePath);
 					}
 			}
 
 			
 		});
 		
 		restore = new JButton("Restore");
 		GridBagConstraints gbc_restore = Utils.getConStraints(4,4,null);
 		add(restore, gbc_restore);
 		restore.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(!validateThis("restoreFile")){
 					try {
 						new BackupService().dropAndRestoreDB(Utils.getConfigValue("DBName"), 
 								Utils.getConfigValue("DBUserName"), Utils.getConfigValue("DBPassword"), restorePath);
 					} catch (OnSuccess e) {
 						JOptionPane.showMessageDialog(AdvancedPanel.this, e.getMessage());
 					} catch (OnFailure e) {
 						JOptionPane.showMessageDialog(AdvancedPanel.this, e.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
 					} catch (Throwable e) {
 						JOptionPane.showMessageDialog(AdvancedPanel.this, e.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
 						e.printStackTrace();
 					}
 				}
 			}
 			
 		});
 		
 	}
 	
 	protected boolean validateThis(String field) {
 		if(resultList == null){
 			resultList=new ArrayList<ValidationMsg>();
 		}else{
 			resultList.clear();
 		}
 		
 		hasErrors = Validator.validateWithFilter(this, resultList, Arrays.asList(new String[]{field}));
 		
 		//update registered listeners
 		if(validationRegistry!=null){
 			for(ValidationListener listener:validationRegistry){
 				ValidationEvent vEvent =  new ValidationEvent(); 
 				vEvent.setValid(!hasErrors);
 				vEvent.setResultList(resultList);
 				vEvent.setParent(this.main.settings);
 				vEvent.setDy(20);
 				if(hasErrors){
 					listener.onFailure(vEvent);
 				}else{
 					listener.onSuccess(vEvent);
 				}
 			}
 		}
 		repaint();
 		return hasErrors;
 	}
 	
 
 	public void addClientValidationListener(Component glassPane) {
 		// TODO Auto-generated method stub
 		if(validationRegistry==null){
 			validationRegistry = new ArrayList<ValidationListener>();
 		}
 		if(!validationRegistry.contains(glassPane)){
 			validationRegistry.add((ValidationListener) glassPane);
 		}
 		
 	}
 
 }
