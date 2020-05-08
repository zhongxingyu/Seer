 /**
  * A room subpanel is the parent of all the appointment blocks. If there is no practitioner set, 
  * this panel is blank. 
  */
 
 package gui.main;
 
 import gui.Constants;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.border.EmptyBorder;
 
 import backend.DataTransferObjects.AppointmentDto;
 import backend.DataTransferObjects.SchedulePractitionerDto;
 
 //import gui.Constants;
 
 @SuppressWarnings("serial")
 public class RoomSubpanel extends JPanel{
 	private SchedulePractitionerDto room;
 	private DayPanel dp;
 	
 	/** Constructs a room subpanel given the practitioner to be schedule for a given day. */
 	public RoomSubpanel(SchedulePractitionerDto room, DayPanel dp) {
 		this.dp = dp;
 		this.room = room;
 		
                 setAppointments();
 		setBorder(BorderFactory.createMatteBorder(0, 1, 1, 0, Color.BLACK));
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		setBackground(Color.LIGHT_GRAY);
 	}
 	
 	/**
 	 * Adds all the appointments to this object, and adds leftover if necessary
 	 */
 	public void setAppointments() {
 		removeAll();
 		repaint();
 		int dayStart = dp.getDay().getStart();
		add(new EmptyBlock(room.getStart() - dayStart));
 		for (AppointmentDto a : room.getAppointments()) {
 			AppointmentBlock app = new AppointmentBlock(a,dp);
 			add(app);
 		}
 		/*if (room.getLeftover() > 0) {
 			JPanel leftover = new JPanel();
 			leftover.setPreferredSize(new Dimension(0,room.getLeftover()*Constants.PIXELS_PER_MINUTE));
 			leftover.setBackground(Color.LIGHT_GRAY);
 			add(leftover);
 		}**/
 	}
 
 	/** Returns the day panel containing the rooms panel and subpanel. */
 	public DayPanel getDayPanel() {
 		return dp;
 	}
 
 	public class EmptyBlock extends JPanel {
 		
 		public EmptyBlock(int size) {
 			setPreferredSize(new Dimension(0, size*Constants.PIXELS_PER_MINUTE));
 			setMaximumSize(new Dimension(Integer.MAX_VALUE, size*Constants.PIXELS_PER_MINUTE));
 			setMinimumSize(new Dimension(0, size*Constants.PIXELS_PER_MINUTE));
 			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,Color.BLACK), new EmptyBorder(5,5,5,5)));
 			setLayout(new BorderLayout());
 			setBackground(Color.LIGHT_GRAY);
 
 			this.setFocusable(false);
 		}
 	}
 }
