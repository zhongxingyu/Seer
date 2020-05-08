 package FRC_Score_Sys.InputWindow;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class OptRow extends JPanel implements ActionListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1;
 	private ScorePanel myParent;
 
 	private JComboBox<Integer> Climb;
 	private JCheckBox DQ;
 
 	private boolean OrigDQ;
 	private int OrigClimb;
 
 	public ActionListener al = new ActionListener() {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			myParent.ForwardRefresh();
 		}
 	};
 	final Logger logger = LoggerFactory.getLogger(OptRow.class);
 
	public OptRow(ScorePanel parent, String Title, int ClimbVal, boolean DqVal) {
 		myParent = parent;
 		setLayout(new GridLayout(0, 3, 0, 0));
 		JLabel lblTitle = new JLabel(Title);
 		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
 		this.add(lblTitle);
 		Climb = new JComboBox<Integer>();
 		Climb.addItem(0);
 		Climb.addItem(1);
 		Climb.addItem(2);
 		Climb.addItem(3);
 		Climb.addActionListener(al);
 		this.add(Climb);
 		DQ = new JCheckBox();
 		DQ.setHorizontalAlignment(SwingConstants.CENTER);
 		DQ.addActionListener(al);
 		this.add(DQ);
 		SetValues(DqVal, ClimbVal);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 
 	}
 
 	public int GetClimb() {
 		int a = 0;
 		try {
 			if (!DQ.isSelected()) {
 				a = (Integer) Climb.getSelectedItem();
 				a = a * 10;
 			}
 		} catch (NumberFormatException e) {
 			logger.debug("OptRow failed to parse Climb Integer! Using Zero.");
 			a = 0;
 		}
 		return a;
 	}
 
 	public int GetRawClimb() {
 		return (Integer) Climb.getSelectedItem();
 	}
 
 	public boolean isDQ() {
 		return DQ.isSelected();
 	}
 
 	public void RequestReset() {
 		SetValues(OrigDQ, OrigClimb);
 	}
 
 	private void SetValues(boolean DqVal, int ClimbVal) {
 		DQ.setSelected(DqVal);
 		Climb.setSelectedItem(ClimbVal);
 		OrigClimb = ClimbVal;
 		OrigDQ = DqVal;
 	}
 }
