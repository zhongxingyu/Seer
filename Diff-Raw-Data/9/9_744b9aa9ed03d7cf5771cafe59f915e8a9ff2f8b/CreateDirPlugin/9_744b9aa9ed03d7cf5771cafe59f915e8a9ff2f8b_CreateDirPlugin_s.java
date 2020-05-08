 package cz.zcu.kiv.kc.plugin.createDir;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.FileAlreadyExistsException;
 import java.nio.file.Files;
 import java.nio.file.InvalidPathException;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 import cz.zcu.kiv.kc.plugin.I18N;
 
 import cz.zcu.kiv.kc.interfaces.ICreateDirPlugin;
 import cz.zcu.kiv.kc.plugin.AbstractPlugin;
 
 public class CreateDirPlugin extends AbstractPlugin implements ICreateDirPlugin {
 
 	public CreateDirPlugin()
 	{
 		super();
 		UIManager.put("ClassLoader", getClass().getClassLoader());
 	}
 	@Override
 	public void executeAction(List<File> selectedFiles, String destinationPath,
 			String sourcePath) {
 		String name = askForName(I18N.getText("newDirName"));
 		if (name == null) {
 			JOptionPane.showMessageDialog(this.mainWindow,
 					I18N.getText("newDirOpCanceled"),
 					I18N.getText("newDirOpCanceledTitle"),
 					JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		if (name.trim().isEmpty()) {
 			JOptionPane.showMessageDialog(this.mainWindow, 
 				I18N.getText("newDirWrongInput"),
 				I18N.getText("newDirWrongInputTitle"),
 				JOptionPane.ERROR_MESSAGE
 			);
 			return;
 		}
 		
 		File newDir = new File(sourcePath + File.separator + name);
 		try
 		{
 			Files.createDirectory(newDir.toPath());
 		}
 		catch (FileAlreadyExistsException e)
 		{
 			JOptionPane.showMessageDialog(
 				this.mainWindow,
				"Ji existuje soubor/adres se zadanm jmnem.",
				"Chyba",
 				JOptionPane.ERROR_MESSAGE
 			);
 		}
 		catch (InvalidPathException e)
 		{
 			JOptionPane.showMessageDialog(
 				this.mainWindow,
				"Jmno obsahuje nepovolen znaky.",
				"Chyba",
 				JOptionPane.ERROR_MESSAGE
 			);
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		//System.out.println(new File(sourcePath + File.separator + name).mkdir());
 		sendEvent(sourcePath);		
 	}
 
 	@Override
 	public String getName() {
 		return "newDir";
 	}
 
 }
