 package pcc.ui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JOptionPane;
 
 public class PCCEventHandler implements ActionListener{
 	private PCCFrame parent;
 	
 	public PCCEventHandler(PCCFrame parent){
 		this.parent = parent;
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getActionCommand().equals("Add File"))
 			addFile();
 		else if(e.getActionCommand().equals("Commit"))
 			commit();
 		else if(e.getActionCommand().equals("New Project"))
 			newProject();
 		else if(e.getActionCommand().equals("Open Project"))
 			openProject();
 		else if(e.getActionCommand().equals("Short Report"))
 			shortReport();
 		else if(e.getActionCommand().equals("Long Report"))
 			longReport();
 		else if(e.getActionCommand().equals("Export Change Labels"))
 			exportChangeLabels();
 		else
 			JOptionPane.showMessageDialog(null,"Invalid Action Command");
 	}
 	
 	private void addFile(){
 		Main.addFile();
 		parent.update();
 	}
 	
 	private void commit(){
 		Main.commitNewVersion();
 		parent.update();
 	}
 	
 	private void newProject(){
 		Main.newProject();
 		parent.update();
 	}
 	
 	private void openProject(){
 		Main.openProject();
 		parent.update();
 	}
 	
 	private void shortReport(){
 		String report = Main.displayVersionChanges(true);
 		if(report!=null)
 			parent.setReport(report);
 		parent.update();
 	}
 
 	private void longReport(){
 		String report = Main.displayVersionChanges(false);
 		if(report!=null)
 			parent.setReport(report);
 		parent.update();
 	}
 	
 	private void exportChangeLabels(){
 		Main.exportChangeLables();
 		parent.update();
 	}
 }
