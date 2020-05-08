 package ui.isometric.builder;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import game.Level;
 
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 /**
  * A panel for inspecting one level
  * @author melby
  *
  */
 public class LevelPanel extends JPanel {
 	private static final long serialVersionUID = 1L;
 	private Level level;
 
 	/**
 	 * Create a LevelPanel with the given level
 	 * @param l
 	 */
 	public LevelPanel(final Level l) {
 		level = l;
 		refresh();
 	}
 
 	/**
 	 * Update the view
 	 */
 	private void refresh() {
 		this.removeAll();
 		this.add(new JLabel("level "+level.z()));
 		String radiusS = ((level.luminance()==-1)?"infinity":(level.luminance()+""));
 		final JLabel ill = new JLabel("luminance "+radiusS);
 		this.add(ill);
 		final LevelPanel t = this;
 		final JTextField r = new JTextField();
 		r.setPreferredSize(new Dimension(90, 20));
 		r.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					int radius = Integer.parseInt(r.getText());
 					String radiusS = ((radius==-1)?"infinity":(radius+""));
 					if(JOptionPane.showConfirmDialog(t, "Are you sure you wish to set the level "+level.z()+" to radius "+radiusS+"?") == JOptionPane.OK_OPTION) {
 						level.luminance(radius);
 						ill.setText("luminance "+radiusS);
 					}
 				}
 				catch (NumberFormatException e) {}
 			}
 		});
 		this.add(r);
 		this.validate();
 		this.repaint();
 	}
 	
 	/**
 	 * Get the level being inspected
 	 * @return
 	 */
 	public Level level() {
 		return level;
 	}
 	
 	/**
 	 * Set the level to inspect
 	 * @param l
 	 */
 	public void setLevel(Level l) {
 		level = l;
 		refresh();
 	}
 }
