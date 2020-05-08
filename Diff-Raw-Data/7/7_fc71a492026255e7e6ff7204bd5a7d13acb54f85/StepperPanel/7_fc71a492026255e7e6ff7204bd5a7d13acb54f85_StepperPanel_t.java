 package org.reprap.steppertestgui;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.reprap.comms.Communicator;
 import org.reprap.comms.snap.SNAPAddress;
 import org.reprap.devices.GenericStepperMotor;
 
 /// TODO There is a bug in this app that can cause the stepper to skip some steps
 /// and lose its place.  This can occur if you slowly drag the position slider.  Multiple
 /// updates will be rapidly sent to the stepper telling it to change position. This
 /// will actually cause it to perform a step immediately on each request so it
 /// can cause steps to occur faster than they normally would.  If this happens
 /// too quickly, it will be beyond the safe torque speed and it will skip.  This
 /// should be resolved with a timer or something in the gui.  If an update just
 /// occurred the new one should not be sent immedidately.  Instead the event
 /// should be queued up and only send after a safe amount of time has elapsed.
 /// This would also allow multiple movement events to be coallesced into a single
 /// request if they occur very quickly.  It still has to be sent eventually
 /// or else the motor will not go to the correct location represented in the gui.
 /// One way to avoid this is for the short term to use keys instead of the mouse
 /// (eg pgup, pgdown) and don't press too quickly.
 
 public class StepperPanel extends JPanel implements ChangeListener {
 
 	private static final long serialVersionUID = 6262697694879478425L;
 
 	private JSlider externalSpeedSlider;  // Externally maintained speed slider
 	private JSlider positionRequest;      // Slider to control position
 	private JSlider positionActual;       // Slider to indicate position
 	
 	private JCheckBox torque;            // If motor is driving or not
 	
 	private JLabel rangeLabel;
 	
 	private GenericStepperMotor motor; 
 	private boolean moving = false;   // True if (as far as we know) the motor is seeking
 	private boolean waiting = false;  // True if already waiting for a timer to complete (so we don't start another one)
 
 	private Timer updateTimer;
 	
 	private int minValue = 0;
 	private int maxValue = 1000;
 	
 	private boolean monitoring = false;
 	
 	public StepperPanel(String name, int address, JSlider externalSpeedSlider, Communicator communicator) {
 		super();
 		
 		updateTimer = new Timer();
 
         motor = new GenericStepperMotor(communicator, new SNAPAddress(address));
 		
 		this.externalSpeedSlider = externalSpeedSlider;
 	
         setLayout(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
 
         c.insets.bottom = c.insets.top = 0;
         c.ipady = 0;
         c.gridx = 0;
         c.gridy = 0;
                 
         add(new JLabel("Set " + name + " axis position"), c);
         positionRequest = new JSlider(JSlider.HORIZONTAL, minValue, maxValue, 0);
         
         positionRequest.addChangeListener(this);
         c.gridy = 1;
         add(positionRequest, c);
 
         c.gridy = 2;
         add(new JLabel("Actual " + name + " axis position"), c);
         positionActual = new JSlider(JSlider.HORIZONTAL, minValue, maxValue, 0);
         positionActual.setEnabled(false);
         c.gridy = 3;
         add(positionActual, c);
         
         c.gridx = 1;
         c.gridy = 0;
         rangeLabel = new JLabel();
         updateRange();
         add(rangeLabel, c);
         
         c.gridy = 1;
         JButton calibrate = new JButton("Calibrate");
         calibrate.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent evt) {
         		onCalibrate();
         	}
         });
         add(calibrate, c);
         
         c.gridy = 2;
         torque = new JCheckBox("Torque");
         torque.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent evt) {
         		onTorqueUpdate();
         	}
         });
         add(torque, c);
 	}
 
 	/**
 	 * Utility function to update the range display and 
 	 * slider end points
 	 */
 	private void updateRange() {
 		rangeLabel.setText(minValue + " to " + maxValue);
 		
 		positionRequest.setMinimum(minValue);
 		positionRequest.setMaximum(maxValue);
 		positionActual.setMinimum(minValue);
 		positionActual.setMaximum(maxValue);
 		
         //int range = maxValue - minValue;
         positionRequest.setMajorTickSpacing(400);  // A full circle
         positionRequest.setMinorTickSpacing(50);   // 8ths of a circle
         positionRequest.setPaintTicks(true);
 	}
 
 	/**
 	 * Calibrate button handler
 	 */
 	protected void onCalibrate() {
 		try {
			GenericStepperMotor.Range range = motor.getRange(externalSpeedSlider.getValue());
 			minValue = range.minimum;
 			maxValue = range.maximum;
 			// We could re-request current position, but for now we know
 			// that we're at the max position, so update to reflect this
			moving = false;
 			updateRange();
			positionRequest.setValue(maxValue);
 		} catch (Exception ex) {
 			JOptionPane.showMessageDialog(null, "Problem during calibration: " + ex);
 		}
 	}
 
 	/**
 	 * Callback when the torque checkbox is clicked
 	 */
 	protected void onTorqueUpdate()
 	{
 		try {
 			if (torque.isSelected()) {
 				// You can't currently do this
 				torque.setSelected(false);
 			} else {
 				motor.setIdle();
 				moving = false;
 			}
 		} catch (Exception ex) {
 			JOptionPane.showMessageDialog(null, "Could not idle motor: " + ex);
 		}
 			
 	}
 
 	/**
 	 * Callback when a slider is changed
 	 */
 	public void stateChanged(ChangeEvent evt) {
 		try {
 			Object srcObj = evt.getSource();
 			
 			if (srcObj instanceof JSlider) {
 				JSlider src = (JSlider)srcObj;
 				if (src == positionRequest)
 					seekToSelectedPosition();
 			}
 		} catch (Exception ex) {
     		JOptionPane.showMessageDialog(null, "Update exception: " + ex);
 		} 
 	}
 
 	/**
 	 * Queue a timer event for the near future
 	 */
 	private void startUpdates() {
 		if (!waiting && (moving || monitoring)) {    // If there is already one, don't create another
 			waiting = true;
 			TimerTask task = new TimerTask() {
 				public void run() {
 					waiting = false;
 					updatePosition();
 				}			
 			};
 			updateTimer.schedule(task, 200);
 		}
 	}
 
 	/**
 	 * Request the current position and display it
 	 */
 	private void setDisplayPosition() throws IOException {
 		int position = motor.getPosition();
 		if (monitoring)
 			positionRequest.setValue(position);
 		positionActual.setValue(position);
 		if (position == positionRequest.getValue())
 			moving = false;
 	}	
 
 	/**
 	 * Called when the slider is moved to seek the motor
 	 * @throws IOException
 	 */
 	private void seekToSelectedPosition() throws IOException {
 		motor.seek(externalSpeedSlider.getValue(), positionRequest.getValue());
 		torque.setSelected(true);
 		moving = true;
 		startUpdates();
 	}
 
 	/**
 	 * Called when the speed slider is changed
 	 * @throws IOException
 	 */
 	public void updateSpeed() throws IOException {
 		// If we're not moving, changing the speed does nothing.
 		// Otherwise, we just re-seek to the current position
 		// with the new speed for it to take effect.
 		if (moving)
 			seekToSelectedPosition();
 	}
 
 	/**
 	 * This method is called on the timer event to get the current
 	 * position and display it.
 	 */
 	protected void updatePosition()
 	{
 		try {
 			setDisplayPosition();
 			if (moving || monitoring)  // If we're moving, start another timer
 				startUpdates();
 		} catch (IOException ex) {
 			// Ignore these if they happen
 			System.out.println("Ignored IO exception in position update: " + ex);
 		}
 	}
 
 	/**
 	 * @return Returns the motor.
 	 */
 	public GenericStepperMotor getMotor() {
 		return motor;
 	}
 
 	public void monitor(boolean enable) {
 		monitoring = enable;
 		if (monitoring)
 			startUpdates();
 			
 	}
 	
 	public void setMoved() {
 		torque.setSelected(true);
 	}
 }
