 package actions;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JComboBox;
 
 import panels.MasterPanel;
 
 public class SwitchSidePanelAction implements ActionListener{
 
 	MasterPanel master;
 	
 	public SwitchSidePanelAction(MasterPanel m)
 	{
 		master = m;
 		
 	}
 	
	// TODO implement this
	public SwitchSidePanelAction(MasterPanel m, int panel)
	{
		this(m);
		
	}
	
 	public void actionPerformed(ActionEvent e)
 	{
 		JComboBox source = (JComboBox) e.getSource();
 		master.sidePanel = source.getSelectedIndex()+1;
 		
 		if(master.sidePanel==1) source.setSelectedIndex(1);
 		else if(master.sidePanel==2) source.setSelectedIndex(0);
 		
 		master.redraw();
 	}
 }
