 package archetypeEditor;
 
 import entityFramework.IComponent;
 import entityFramework.IEntityArchetype;
 import graphics.Texture2D;
 import graphics.TextureFont;
 
 import javax.swing.JFileChooser;
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import org.newdawn.slick.openal.Audio;
 
 import scripting.LineScript;
 
 import content.ContentManager;
 
 import java.awt.Desktop;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 
 public class AssetPanel extends JPanel {
 	private JTextField textField;
 	private String fileEnding;
 	private String absolutePath;
 	private Field field;
 	private IComponent comp;
 	private File resourcesDirectory = new File("resources");
 	/**
 	 * Create the panel.
 	 */
 	public AssetPanel(IComponent comp, Field field) {
 		setLayout(null);
 
 		JLabel lblAsset = new JLabel("Asset:");
 		lblAsset.setBounds(10, 8, 31, 14);
 		add(lblAsset);
 
 		if(field.getType() == Texture2D.class){
 			fileEnding = "png";
 		} else if(field.getType() == Audio.class){
 			fileEnding = "ogg";
 		}else if(field.getType() == IEntityArchetype.class){
 			fileEnding = "archetype";
 		}else if(field.getType() == LineScript.class){
 			fileEnding = "script";
 		}else if(field.getType() == TextureFont.class){
 			fileEnding = "xml";
 		}else{
 			throw new Error("CLASS NOT SUPPORTED BY ASSETPANEL");
 		}
 
 		textField = new JTextField();
 		textField.setBounds(51, 5, 147, 20);
 		add(textField);
 		textField.setColumns(10);
 
 		JButton btnNewButton = new JButton("Select");
 
 		btnNewButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser chooser = new JFileChooser();
 				chooser.setCurrentDirectory(getResourcesDirectory());
 				FileNameExtensionFilter filter = new FileNameExtensionFilter(
 						fileEnding, fileEnding);
 				chooser.setFileFilter(filter);
 				int returnVal = chooser.showOpenDialog(getParent());
 				if(returnVal == JFileChooser.APPROVE_OPTION) {
 					setText(chooser.getSelectedFile().getName());
 					absolutePath = chooser.getSelectedFile().getAbsolutePath();
 				}
 			}
 		}
 				);
 		btnNewButton.setBounds(10, 36, 89, 23);
 		add(btnNewButton);
 
 		JButton btnNewButton_1 = new JButton("Preview");
 		btnNewButton_1.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {//TODO TEMPORARY. Opens with OS preferred application or notepad
 				String filePath = "resources" + File.separator;
 
 				//OPEN TEXTFILES WITH NOTEPAD
 				if(getText().endsWith(".script")||getText().endsWith(".archetype"))
 				{
 					if (getText().endsWith(".archetype")){
 						filePath += "archetypes" + File.separator + getText();
 					}
 					else if (getText().endsWith(".script")){
 						filePath += "scripts" + File.separator + getText();
 					}else{
 						throw new Error("INVALID FILE");
 					}
 					openFileWithNotepad(filePath);
 				}else{//OPEN OTHER TYPES OF FILES WITH USERS PREFERRED APPLICATION OR OPENGLSTUFF
 					if(getText().endsWith(".png")){
 						filePath += "textures" + File.separator + getText();
 						openFileWithDefaultApplication(filePath);
 					}else if (getText().endsWith(".ogg")){
 						int soundsIndex = absolutePath.lastIndexOf("sounds", absolutePath.length());
 						filePath = absolutePath.substring(soundsIndex + "sounds".length() + 1);
 						Audio sound = ContentManager.loadSound(filePath);
 						AudioPlayerFrame af = new AudioPlayerFrame(sound);
 						af.setVisible(true);						
 					} 
 
 				}
 
 			}
 
 			private void openFileWithNotepad(String filePath) {
 				Runtime load = Runtime.getRuntime();
 				String program = "\"C:\\WINDOWS\\system32\\notepad.exe\"";;
 				File toOpen = new File(filePath);
 				try {
 					load.exec(program + " " + toOpen.getAbsolutePath());
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 
 			private void openFileWithDefaultApplication(String filePath) {
 				File textureFile = new File(filePath);
 				Desktop dt = Desktop.getDesktop();;
 				try {
 					dt.open(textureFile);
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		});
 		btnNewButton_1.setBounds(109, 36, 89, 23);
 		add(btnNewButton_1);
 
 
 
 	}
 
 	public void setText(String text){
 		this.textField.setText(text);
 	}
 
 	public String getText(){
 		return this.textField.getText();
 	}
 
 	public File getResourcesDirectory(){
 		return this.resourcesDirectory;
 	}
 
 	private void setFieldValue(String fileName){
 		Object newFieldValue;
 		if(field.getType() == Texture2D.class){
 			newFieldValue = ContentManager.loadTexture(getText());
 		} else if(field.getType() == Audio.class){
 			int soundsIndex = absolutePath.lastIndexOf("sounds", absolutePath.length());
 			String filePath = absolutePath.substring(soundsIndex + "sounds".length() + 1);
 			newFieldValue = ContentManager.loadSound(filePath);
 		}else if(field.getType() == IEntityArchetype.class){
 			newFieldValue = ContentManager.loadArchetype(getText());
 		}else if(field.getType() == LineScript.class){
 			newFieldValue = ContentManager.loadScript(getText());
 		}else if(field.getType() == TextureFont.class){
 			newFieldValue = ContentManager.loadArchetype(getText());
 		}else{
 			throw new Error("CLASS NOT SUPPORTED BY ASSETPANEL");
 		}
 
 
 		try {
 			this.field.set(comp, newFieldValue);
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.out.println("ERROR WHEN SETTING COMPONENT ASSET FIELD");
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.out.println("ERROR WHEN SETTING COMPONENT ASSET FIELD");
 		}
 	}
 }
