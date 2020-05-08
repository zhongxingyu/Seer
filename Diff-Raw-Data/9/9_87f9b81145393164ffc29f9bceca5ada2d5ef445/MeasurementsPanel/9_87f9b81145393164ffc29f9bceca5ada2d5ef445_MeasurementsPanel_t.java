 package raisa.vis;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.text.DecimalFormat;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.TitledBorder;
 
 public class MeasurementsPanel extends JPanel implements Observer {
 	private static final long serialVersionUID = 1L;
 
 	private AccelerationPanel accelerationPanel; 
 	private GyroscopePanel gyroscopePanel; 
 	
 	public MeasurementsPanel(WorldModel worldModel) {
 		worldModel.addObserver(this);
 		TitledBorder border = new TitledBorder("Measurements");
 		setBorder(border);
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		accelerationPanel = new AccelerationPanel();
 		this.add(accelerationPanel);
 		gyroscopePanel = new GyroscopePanel();
 		this.add(gyroscopePanel);		
 	}
 	
 	public void update(Observable worldModel, Object sample) {
 		accelerationPanel.update((WorldModel)worldModel, (Sample)sample);
 		gyroscopePanel.update((WorldModel)worldModel, (Sample)sample);
 	}
 	
 	class AccelerationPanel extends JPanel {
 		private static final long serialVersionUID = 1L;
 
 		private JLabel accXField;
 		private JLabel accYField;
 		private JLabel accZField;
 		
 		public AccelerationPanel() {
 			this.setMinimumSize(new Dimension(200,80));
 			this.setPreferredSize(new Dimension(200,80));
 			this.setMaximumSize(new Dimension(Short.MAX_VALUE, 80));
			TitledBorder border = new TitledBorder("Acceleration (m/s^2)");
 			setBorder(border);
 			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 			accXField = new JLabel("X: -");
 			accYField = new JLabel("Y: -");
 			accZField = new JLabel("Z: -");
 			this.add(accXField);		
 			this.add(accYField);			
 			this.add(accZField);						
 		}
 			
 		public void update(WorldModel worldModel, Sample sample) {
 			float avgX = 0, avgY = 0, avgZ = 0;
 			List<Sample> samples = worldModel.getLastSamples(10);
 			DecimalFormat format = new DecimalFormat("0.00");			
 			
 			for (Sample s : samples) {
 				Vector3D a = s.getAcceleration();
 				avgX += a.getX();
 				avgY += a.getY();
 				avgZ += a.getZ();
 			}
 			avgX /= samples.size();
 			avgY /= samples.size();
 			avgZ /= samples.size();
 			
 			Vector3D acceleration = sample.getAcceleration();
 			accXField.setText("X: " + format.format(acceleration.getX()) + " (last), " + format.format(avgX) + " (avg 10)");
 			accYField.setText("Y: " + format.format(acceleration.getY()) + " (last), " + format.format(avgY) + " (avg 10)");
 			accZField.setText("Z: " + format.format(acceleration.getZ()) + " (last), " + format.format(avgZ) + " (avg 10)");
 			repaint();
 		}
 		
 	}
 	
 	class GyroscopePanel extends JPanel {
 		private static final long serialVersionUID = 1L;
 
 		private JLabel gyroXField;
 		private JLabel gyroYField;
 		private JLabel gyroZField;
 		
 		public GyroscopePanel() {
 			this.setMinimumSize(new Dimension(200,80));
 			this.setPreferredSize(new Dimension(200,80));
 			this.setMaximumSize(new Dimension(Short.MAX_VALUE, 80));
 			TitledBorder border = new TitledBorder("Gyroscope");
 			setBorder(border);
 			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 			gyroXField = new JLabel("X: -");
 			gyroYField = new JLabel("Y: -");
 			gyroZField = new JLabel("Z: -");
 			this.add(gyroXField);		
 			this.add(gyroYField);			
 			this.add(gyroZField);						
 		}
 			
 		public void update(WorldModel worldModel, Sample sample) {
 			Vector3D angularAcceleration = sample.getAngularAcceleration();
 			gyroXField.setText("X: " + angularAcceleration.getX());
 			gyroYField.setText("Y: " + angularAcceleration.getY());
 			gyroZField.setText("Z: " + angularAcceleration.getZ());	
 			repaint();
 		}
 		
 	}
 	
 	
 }
