 //
 // MenuHandler.java
 //
 
 package restless.fixbe.view;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 
 public class MenuHandler implements ActionListener {
 
 	public static final String CMD_LOAD_DEF = "loadDef";
 	public static final String CMD_OPEN = "open";
 	public static final String CMD_SAVE = "save";
 	public static final String CMD_SAVE_AS = "saveAs";
 	public static final String CMD_EXIT = "exit";
 	public static final String CMD_ABOUT = "about";
 
 	private EditorFrame editor;
 
 	public MenuHandler(EditorFrame editor) {
 		this.editor = editor;
 	}
 
 	public void actionPerformed(ActionEvent e) {
     final String cmd = e.getActionCommand();
     if (cmd.equals(CMD_LOAD_DEF)) editor.loadDef();
     else if (cmd.equals(CMD_OPEN)) editor.open();
     else if (cmd.equals(CMD_SAVE)) editor.save();
     else if (cmd.equals(CMD_SAVE_AS)) editor.saveAs();
     else if (cmd.equals(CMD_EXIT)) editor.exit();
     else if (cmd.equals(CMD_ABOUT)) editor.about();
 	}
 
 }
