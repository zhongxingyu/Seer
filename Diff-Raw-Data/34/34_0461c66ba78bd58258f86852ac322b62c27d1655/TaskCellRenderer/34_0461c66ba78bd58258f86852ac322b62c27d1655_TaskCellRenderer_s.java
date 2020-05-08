 
 package be.artesis.timelog.gui;
 
 import be.artesis.timelog.view.Taak;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.font.TextAttribute;
 import java.util.Map;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 
  
 
 class TaskCellRenderer extends JLabel implements ListCellRenderer {
     
     private Taak t;
     private static DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
     
      public TaskCellRenderer() {
          setOpaque(true);
           
      }
     @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
  {
 		JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
 		if (value.getClass().equals(String.class)) {
 			// FIXME Font voor < new > shizzle aanpassen
			renderer.setFont(list.getFont());
 		} else {
 			t = (Taak) value;
 
 			if (t.getCompleted()) {
 				Font f = new Font("tahoma", Font.BOLD, 11);
 				Map attributes = f.getAttributes();
 				attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
 				Font font = new Font(attributes);
 
 				renderer.setFont(font);
 			} else if (t.overTijd()) {
 				renderer.setForeground(Color.red);
 				// renderer.setIcon(null);
 			}
 		}
 		return renderer;
 	}
      
  }
