 package net.lotusdev.medusa.client;
 
 import java.awt.Component;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JTree;
 import javax.swing.tree.DefaultTreeCellRenderer;
 
import net.lotusdev.medusa.client.Util;

 public class CustomTreeCellRenderer extends DefaultTreeCellRenderer {
 	public static final ImageIcon EMAIL = Util.getIcon("page_white_text");
 	public static final ImageIcon INBOX = Util.getIcon("accept");
 	public static final ImageIcon SENT = Util.getIcon("application_edit");
 	public static final ImageIcon TIMEOUT = Util.getIcon("clock");
 	public static final ImageIcon SETTINGS = Util.getIcon("cog");
 
 	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
 		JLabel lbl = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
 
 		if(value.toString().equals("Sent Items")) {
 			lbl.setIcon(SENT);
 		}else if(value.toString().equals("Inbox")) {
 			lbl.setIcon(INBOX);
 		}else if(value.toString().equals("Settings")) {
 			lbl.setIcon(SETTINGS);
 		}else if(value.toString().equals("Email")) {
 			lbl.setIcon(EMAIL);
 		}else if(value.toString().equals("Timeout")) {
 			lbl.setIcon(TIMEOUT);
 		}else {
 			lbl.setIcon(INBOX);
 		}
 		
 		return lbl;
 	}
 }
