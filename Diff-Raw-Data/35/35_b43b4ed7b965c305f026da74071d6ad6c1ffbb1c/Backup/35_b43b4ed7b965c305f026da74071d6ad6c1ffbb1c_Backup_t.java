 package model;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 
 import view.NepalView;
 
 public class Backup {
 	private int BUFFER = 10485760;
 	private String date;
 	private StringBuffer temp = null;
     private FileWriter  fichero = null;
     private PrintWriter pw = null;
 	private NepalView nepalView;
 	
 	public Backup(NepalView nepalView) {
 		this.nepalView = nepalView;
 		Date date = new Date();
 		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
 		this.date = dateFormat.format(date);
 		
        File hideDire = new File(System.getProperty( "user.home" )+"/.proyectonepal");
         if(!hideDire.exists()){
         	hideDire.mkdir();
         }
		
        File hideDire2 = new File(System.getProperty( "user.home" )+"/.proyectonepal/backup");
        if(!hideDire2.exists()){
        	hideDire2.mkdir();
        }
 	}
 
 	/**
 	 * @return the date
 	 */
 	public String getDate() {
 		return date;
 	}
 	
 	public void makeBackup() {
 		boolean task = false;
 		String userHome = System.getProperty( "user.home" );
 		Process runtimeProcess = null;
 		
 		try {
 			runtimeProcess = Runtime.getRuntime().exec("mysqldump --host=localhost --user=root --password=@juan@2468 nepal");
         //se guarda en memoria el backup
         InputStream in = runtimeProcess.getInputStream();
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
         temp = new StringBuffer();
         int count;
         char[] cbuf = new char[BUFFER];
         while ((count = br.read(cbuf, 0, BUFFER)) != -1)
             temp.append(cbuf, 0, count);
         br.close();
         in.close();        
         /* se crea y escribe el archivo SQL */
         fichero = new FileWriter(userHome+"/.proyectonepal/backup/"+ date +".sql");
         pw = new PrintWriter(fichero);                                                    
         pw.println(temp.toString());  
         task=true;
 		} catch (Exception ex){
             showBadMessage();
             ex.printStackTrace();
         } finally {
         	try {           
         		if (null != fichero) fichero.close();
         	} catch (Exception e2) {
         		e2.printStackTrace();
         		showBadMessage();
         	}
         }
 		if(task) showOkMessage();
 		else showBadMessage();
 	}
 	
 	public String getLastBackup() {
 		File fl = new File(System.getProperty( "user.home" )+"/.proyectonepal/backup/");
 		File[] files = fl.listFiles(new FileFilter() {			
 			public boolean accept(File file) {
 				return file.isFile();
 			}
 		});
		if(files!=null) {
			long lastMod = Long.MIN_VALUE;
			File choise = null;
			for (File file : files) {
				if (file.lastModified() > lastMod) {
					choise = file;
					lastMod = file.lastModified();
				}
 			}
			if(choise!=null) {
				return choise.getName();
			} 
 		}
		return "none";
		
 	}
 	
     private void showOkMessage() {
     	Object[] options = {"Cerrar"};
     	JOptionPane infoPane = new JOptionPane(
     			"Backup diario creado satisfactoriamente!",
                 JOptionPane.DEFAULT_OPTION,
                 JOptionPane.INFORMATION_MESSAGE,
                 null,
                 options,
                 "Cerrar");
     	JDialog dialog = new JDialog();
     	dialog = infoPane.createDialog(dialog, "Ok");
     	dialog.setLocationRelativeTo(dialog);
     	dialog.setVisible(true);
     }
     
     private void showBadMessage() {
     	Object[] options = {"Cerrar"};
     	JOptionPane infoPane = new JOptionPane(
     			"Error creando backup diario",
                 JOptionPane.DEFAULT_OPTION,
                 JOptionPane.ERROR_MESSAGE,
                 null,
                 options,
                 "Cerrar");
     	JDialog dialog = new JDialog();
     	dialog = infoPane.createDialog(nepalView, "Error");
     	dialog.setLocationRelativeTo(nepalView);
     	dialog.setVisible(true);
     }
 }
