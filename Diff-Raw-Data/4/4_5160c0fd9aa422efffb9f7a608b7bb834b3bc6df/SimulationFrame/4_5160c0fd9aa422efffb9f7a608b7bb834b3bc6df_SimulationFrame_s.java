 package smarthouse.simulation;
 
 import jade.gui.GuiEvent;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.HashMap;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.Timer;
 
 import Data.Constants;
 
 public class SimulationFrame extends JFrame {
 	private SimulationAgent myAgent;
 	private HashMap<String, Room> rooms = new HashMap<String, Room>();
 	private HashMap<String, ImageIcon> cachedIcons = new HashMap<String, ImageIcon>();
 	private int[] currentTime = {7, 0};
 	private int currentDay = 0;
 	private boolean dayTime = false;
 	private Timer timer;
 	private JTextField time;
 	private JLabel day;
 	private JTextArea logs;
 	private boolean running = false;
 
 	public SimulationFrame(SimulationAgent agent) {
 		super();
 		setSize(1024, 600);
 		setTitle("Smarthouse");
 		myAgent = agent;
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		getContentPane().setLayout(null);
 
 		/* Cached Images */
 		cachedIcons.put("play", new ImageIcon("icons/play.png"));
 		cachedIcons.put("stop", new ImageIcon("icons/stop.png"));
 		cachedIcons.put("fastforward", new ImageIcon("icons/fastforward.png"));
 
 		/* Rooms creation */
 		Room livingroom = new Room(320, 480, Color.GREEN);
 		Room kitchen = new Room(320, 240, Color.RED);
 		Room bedroom = new Room(320, 240, Color.BLUE);
 
 		rooms.put(Constants.PLACE_LIVINGROOM, livingroom);
 		rooms.put(Constants.PLACE_KITCHEN, kitchen);
 		rooms.put(Constants.PLACE_BEDROOM, bedroom);
 
 		livingroom.setLocation(10, 10);
 		kitchen.setLocation(330, 10);
 		bedroom.setLocation(330, 250);
 
 		getContentPane().add(livingroom);
 		getContentPane().add(kitchen);
 		getContentPane().add(bedroom);
 
 		/* Living room's furniture */
 		livingroom.addLight(6, 6, 10);
 		livingroom.addLight(155, 235, 10);
 		livingroom.addShutter(0, 100, 40, true);
 		livingroom.addShutter(0, 340, 40, true);
 		livingroom.addWindow(5, 100, 40, true);
 		livingroom.addWindow(5, 340, 40, true);
 		livingroom.addHeater(140, 10, 40, false);
 		livingroom.addHeater(140, 459, 40, false);
 
 		/* Kitchen's furniture */
 		kitchen.addLight(155, 115, 10);
 		kitchen.addShutter(315, 100, 40, true);
 		kitchen.addWindow(310, 100, 40, true);
 		kitchen.addHeater(140, 219, 40, false);
 
 		/* Bedroom's furniture */
 		bedroom.addLight(300, 85, 10);
 		bedroom.addLight(300, 135, 10);
 		bedroom.addShutter(140, 235, 40, false);
 		bedroom.addWindow(140, 230, 40, false);
 		bedroom.addHeater(140, 10, 40, false);
 
 		/* Time UI */
 		time = new JTextField("7:00",5);
 		JLabel timeLabel = new JLabel("time:");
 		day = new JLabel("Monday");
 		JButton pause = new JButton(cachedIcons.get("play"));
 		JButton forward = new JButton(cachedIcons.get("fastforward"));
 
 		time.setSize(40, 20);
 		timeLabel.setSize(40, 20);
 		day.setSize(70, 20);
 		pause.setSize(20, 20);
 		forward.setSize(20, 20);
 
 		time.setLocation(50, 500);
 		timeLabel.setLocation(10, 500);
 		day.setLocation(100, 500);
 		pause.setLocation(10, 530);
 		forward.setLocation(40, 530);
 
 		getContentPane().add(time);
 		getContentPane().add(timeLabel);
 		getContentPane().add(day);
 		getContentPane().add(pause);
 		getContentPane().add(forward);
 
 		/* Living room UI */
 		JPanel livingroomPanel = new JPanel();
 		livingroomPanel.setBorder(BorderFactory.createTitledBorder("Living room"));
 		livingroomPanel.setSize(145, 70);
 		livingroomPanel.setLocation(190, 495);
 		getContentPane().add(livingroomPanel);
 
 		JLabel livingroomTemp = new JLabel("18.0°C");
 		livingroomTemp.setSize(20, 20);
 		livingroomTemp.setLocation(0, 0);
 		livingroomPanel.add(livingroomTemp);
 		livingroom.addThermometer(livingroomTemp);
 
 		/* Kitchen UI */
 		JPanel kitchenPanel = new JPanel();
 		kitchenPanel.setBorder(BorderFactory.createTitledBorder("Kitchen"));
 		kitchenPanel.setSize(145, 70);
 		kitchenPanel.setLocation(345, 495);
 		getContentPane().add(kitchenPanel);
 
 		JLabel kitchenTemp = new JLabel("18.0°C");
 		kitchenTemp.setSize(20, 20);
 		kitchenTemp.setLocation(0, 0);
 		kitchenPanel.add(kitchenTemp);
 		kitchen.addThermometer(kitchenTemp);
 
 		/* Bedroom UI */
 		JPanel bedroomPanel = new JPanel();
 		bedroomPanel.setBorder(BorderFactory.createTitledBorder("Bedroom"));
 		bedroomPanel.setSize(145, 70);
 		bedroomPanel.setLocation(500, 495);
 		getContentPane().add(bedroomPanel);
 
 		JLabel bedroomTemp = new JLabel("18.0°C");
 		bedroomTemp.setSize(20, 20);
 		bedroomTemp.setLocation(0, 0);
 		bedroomPanel.add(bedroomTemp);
 		bedroom.addThermometer(bedroomTemp);
 
 		/* Logs */
 		logs = new JTextArea();
 		logs.setEditable(false);
 		logs.setLineWrap(true);
 		logs.setWrapStyleWord(true);
 		JScrollPane scrollLog = new JScrollPane(logs);
 		scrollLog.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
 		scrollLog.setSize(349, 547);
 		scrollLog.setLocation(660, 15);
 		getContentPane().add(scrollLog);
 
 		/* Timer */
 		timer = new Timer(1000, new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				addTime(0, 0, 1);
 			}
 		});
 		timer.setInitialDelay(0);
 		timer.setRepeats(true);
 
 		/* Actions */
 		time.addKeyListener(new KeyAdapter() {
 			public void keyReleased(KeyEvent e) {
 				if (e.getKeyCode() != KeyEvent.VK_ENTER) {
 					return;
 				}
 				String time = ((JTextField) e.getSource()).getText();
 				int hours = Integer.parseInt(time.split(":")[0]);
 				int minutes = Integer.parseInt(time.split(":")[1]);
 				setTime(0, hours, minutes);
 			}
 		});
 
 		pause.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (running) {
 					((JButton) e.getSource()).setIcon(cachedIcons.get("play"));
 					timer.stop();
 				} else {
 					((JButton) e.getSource()).setIcon(cachedIcons.get("stop"));
 					timer.start();
 				}
 				running = !running;
 			}
 		});
 
 		forward.addMouseListener(new MouseListener() {
 			public void mouseClicked(MouseEvent e) {}
 			public void mouseEntered(MouseEvent e) {}
 			public void mouseExited(MouseEvent e) {}
 			public void mousePressed(MouseEvent e) {
 				timer.stop();
 				timer.setDelay(50);
 				timer.start();
 			}
 			public void mouseReleased(MouseEvent e) {
 				timer.stop();
 				timer.setDelay(1000);
				timer.start();
 			}
 		});
 
 		/* Start the simulation */
 		setVisible(true);
 	}
 
 	public Room getRoom(String name) {
 		return rooms.get(name);
 	}
 
 	public int[] getTime() {
 		return new int[]{currentDay, currentTime[0], currentTime[1]};
 	}
 
 	public synchronized void setTime(int day, int hours, int minutes) {
 		// TODO: notify agents
 		currentDay = day;
 		currentTime[0] = hours;
 		currentTime[1] = minutes;
 		GuiEvent event = new GuiEvent(this, Constants.GUI_EVENT_TIME);
 		event.addParameter(day);
 		event.addParameter(hours);
 		event.addParameter(minutes);
 		myAgent.postGuiEvent(event);
 		if (hours >= 8 && hours < 22) {
 			setDay(true);
 		} else {
 			setDay(false);
 		}
 		time.setText(hours + ":" + String.format("%02d", minutes));
 		this.day.setText(Constants.dayOfWeek[currentDay]);
 	}
 
 	public void addTime(int days, int hours, int minutes) {
 		int m = currentTime[1] + minutes;
 		int h = currentTime[0] + hours;
 		int d = currentDay + days;
 		d = (d + h/24) % 7;
 		h = (h + m/60) % 24;
 		m = m % 60;
 		setTime(d, h, m);
 		int elapsed = ((days * 24) + hours) * 60 + minutes;
 		for (Room room : rooms.values()) {
 			for (int i = 0; i < elapsed; ++i) {
 				room.tick();
 			}
 		}
 	}
 
 	public boolean isDay() {
 		return dayTime;
 	}
 
 	public void setDay(boolean day) {
 		if (day == dayTime) {
 			return;
 		}
 		if (day) {
 			appendLog("Sun is rising.");
 		} else {
 		    appendLog("Sun is setting.");
 		}
 		for (Room room : rooms.values()) {
 			room.setDay(day);
 		}
 		dayTime = day;
 	}
 
 	public synchronized void appendLog(String txt) {
 		String time = currentTime[0] + ":" + String.format("%02d", currentTime[1]);
 		logs.setText(logs.getText() + time + ": " + txt + "\n");
 	}
 }
