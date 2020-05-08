 package yuuki.ui.screen;
 
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import yuuki.GameOptions;
 
 /**
  * The screen containing the options for the game.
  */
 @SuppressWarnings("serial")
 public class OptionsScreen extends Screen implements ChangeListener {
 	
 	/**
 	 * Listens for enter presses.
 	 */
 	private KeyListener enterListener = new KeyAdapter() {
 		@Override
 		public void keyPressed(KeyEvent e) {
 			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 				fireEnterClicked();
 			}
 		}
 	};
 	
 	/**
 	 * The slider for background music volume.
 	 */
 	private JSlider bgmVolumeSlider;
 	
 	/**
 	 * The slider for sound effects volume.
 	 */
 	private JSlider sfxVolumeSlider;
 	
 	/**
 	 * The form submission button.
 	 */
 	private JButton submitButton;
 	
 	/**
 	 * The listeners registered to this OptionsScreen.
 	 */
 	private ArrayList<OptionsScreenListener> listeners;
 	
 	/**
 	 * Creates a new OptionsScreen. The child components are created and added.
 	 * 
 	 * @param width The width of the screen.
 	 * @param height the height of the screen.
 	 */
 	public OptionsScreen(int width, int height) {
 		super(width, height);
 		listeners = new ArrayList<OptionsScreenListener>();
 		MouseListener ma = new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				fireEnterClicked();
 			}
 		};
 		Box form = new Box(BoxLayout.Y_AXIS);
 		bgmVolumeSlider = createPercentSlider();
 		sfxVolumeSlider = createPercentSlider();
 		submitButton = new JButton("OK");
 		submitButton.addMouseListener(ma);
 		submitButton.addKeyListener(enterListener);
 		form.add(createLabeledComponent("BGM: ", bgmVolumeSlider));
 		form.add(createLabeledComponent("SFX: ", sfxVolumeSlider));
 		form.add(submitButton);
 		add(form);
 	}
 	
 	/**
 	 * Sets the initial focus to the primary element in the options screen.
 	 */
 	public void setInitialFocus() {
		submitButton.requestFocus();
 	}
 	
 	/**
 	 * Registers a listener for option screen events.
 	 * 
 	 * @param l The listener to register.
 	 */
 	public void addListener(OptionsScreenListener l) {
 		listeners.add(l);
 	}
 	
 	/**
 	 * Removes a listener from the list.
 	 * 
 	 * @param l The listener to remove.
 	 */
 	public void removeListener(OptionsScreenListener l) {
 		listeners.remove(l);
 	}
 	
 	/**
 	 * Sets all fields to their current values.
 	 * 
 	 * @param options The options object.
 	 */
 	public void setValues(GameOptions options) {
 		bgmVolumeSlider.setValue(options.bgmVolume);
 		sfxVolumeSlider.setValue(options.sfxVolume);
 	}
 	
 	/**
 	 * Called when a slider is moved.
 	 */
 	public void stateChanged(ChangeEvent e) {
 		JSlider source = (JSlider) e.getSource();
 		if (!source.getValueIsAdjusting()) {
 			if (source == bgmVolumeSlider) {
 				fireBgmLevelChanged();
 			} else if (source == sfxVolumeSlider) {
 				fireSfxLevelChanged();
 			}
 		}
 	}
 	
 	/**
 	 * Calls bgmVolumeChanged() on all listeners.
 	 */
 	private void fireBgmLevelChanged() {
 		OptionsScreenListener[] listenersList = getListenersArray();
 		int volume = bgmVolumeSlider.getValue();
 		for (OptionsScreenListener l: listenersList) {
 			l.bgmVolumeChanged(volume);
 		}
 	}
 	
 	/**
 	 * Calls sfxVolumeChanged() on all listeners.
 	 */
 	private void fireSfxLevelChanged() {
 		OptionsScreenListener[] listenersList = getListenersArray();
 		int volume = sfxVolumeSlider.getValue();
 		for (OptionsScreenListener l: listenersList) {
 			l.sfxVolumeChanged(volume);
 		}
 	}
 	
 	/**
 	 * Calls optionsSubmitted() on all listeners.
 	 */
 	private void fireEnterClicked() {
 		OptionsScreenListener[] listenersList = getListenersArray();
 		for (OptionsScreenListener l: listenersList) {
 			l.optionsSubmitted();
 		}
 	}
 	
 	/**
 	 * Gets an array version of the list of listeners.
 	 * 
 	 * @return The array version of listeners.
 	 */
 	private OptionsScreenListener[] getListenersArray() {
 		OptionsScreenListener[] ls = new OptionsScreenListener[0];
 		OptionsScreenListener[] listenersList = listeners.toArray(ls);
 		return listenersList;
 	}
 	
 	/**
 	 * Creates a slider for discrete values in the range [0, 100].
 	 * 
 	 * @return The slider.
 	 */
 	private JSlider createPercentSlider() {
 		JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
 		slider.addChangeListener(this);
 		slider.addKeyListener(enterListener);
 		return slider;
 	}
 	
 	/**
 	 * Creates a panel containing a label and a component.
 	 * 
 	 * @param label The String that the label should contain.
 	 * @param component The component that is to be labeled.
 	 * 
 	 * @return The panel.
 	 */
 	private JPanel createLabeledComponent(String label, JComponent component) {
 		JPanel panel = new JPanel();
 		panel.add(new JLabel(label));
 		panel.add(component);
 		return panel;
 	}
 	
 }
