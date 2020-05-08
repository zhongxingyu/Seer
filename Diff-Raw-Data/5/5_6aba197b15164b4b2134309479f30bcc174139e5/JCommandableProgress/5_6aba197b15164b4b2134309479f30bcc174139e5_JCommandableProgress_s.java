 package gui.components;
 
 import javax.swing.JProgressBar;
 
 import database.match.Commandable;
 
 @SuppressWarnings("serial")
 public class JCommandableProgress extends JProgressBar {
 	private Commandable commandable;
 
 	public JCommandableProgress(Commandable c) {
 		super(0, c.getTotalGamesCount());
 		setStringPainted(true);
 		commandable = c;
 	}
 
 	@Override
 	public String getString() {
 		return commandable.getFinishedGamesCount() + "/"
 				+ commandable.getTotalGamesCount() + " (" + super.getString()
 				+ ")";
 	}
 
 	@Override
 	public void repaint() {
		if (commandable != null)
 			setValue(commandable.getFinishedGamesCount());
 		super.repaint();
 	}
 
 	public void setCommandable(Commandable com) {
 		commandable = com;
 	}
 }
