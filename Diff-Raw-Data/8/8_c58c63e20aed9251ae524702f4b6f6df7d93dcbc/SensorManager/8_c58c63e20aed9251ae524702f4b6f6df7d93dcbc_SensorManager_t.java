 package at.fhooe.mcm441.client;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.slf4j.Logger;
 
 import at.fhooe.mcm441.commons.Configuration;
 import at.fhooe.mcm441.commons.network.Client;
 import at.fhooe.mcm441.commons.protocol.IAdminClientSideListener;
 import at.fhooe.mcm441.sensor.Sensor;
 
 public class SensorManager extends SensorViewer implements IAdminClientSideListener {
 	private static final Logger log = org.slf4j.LoggerFactory.getLogger(SensorManager.class.getName());
 
 	public static String HOST = "localhost";
 	public static int PORT = 4445;
 	
 	public AdminConnection m_admin_con;
 	private static final boolean HARDCORETEST = false; // if this is true, not one but MANY clients are started
 	private static final boolean LOGGING = !HARDCORETEST;
 	
 	private static final int MIN_STAY_CONNECTED_TIME = 120; // seconds
 	private static final int STARTED_CLIENTS_COUNT = 100;
 	private static final int MIN_STARTING_OFFSET = 100; // milliseconds
 	
 	private ArrayList<String> m_all_sensors;
 	public Vector<Configuration> configItems;
 	
 	//key = sensorID
 	private static Map<String, Configuration> m_configItems;
 	
 	public static void main(String[] args) throws Exception{
 		SensorManager sc = new SensorManager(true, 0);
 	
 	}
 	
 
 	//Gui elements
 	private Group m_group2;
 	private Group m_group3;
 	private Composite composite3;
 	
 	
 	public SensorManager(boolean autoRegister, int disconnectAfterSeconds) throws Exception {
 		super(autoRegister, disconnectAfterSeconds);
 	}
 	
 	@Override
 	public void newConnection() throws Exception
 	{
 		m_admin_con = new AdminConnection(HOST, PORT, this, this);
 		m_configItems = new HashMap<String, Configuration>();
 		m_all_sensors = new ArrayList<String>();
 
 	}
 	
 	@Override
 	public void setupGui()
 	{
 		super.setupGui();
 		
 		GridData gridData;
 		m_group2 = new Group(composite1, SWT.NULL);
 		m_group2.setText("connected clients");
 		gridData = new GridData(150, 200);
 		m_group2.setLayoutData(gridData);
 		m_group2.setLayout(new GridLayout(1, false));
 		
 	    m_group3 = new Group(composite2, SWT.NULL);
 	    m_group3.setText("settings");
 		gridData = new GridData(GridData.VERTICAL_ALIGN_FILL);
 		m_group3.setLayoutData(gridData);
 		m_group3.setLayout(new GridLayout(1, false));
 		
 		composite3 = new Composite(m_group3, SWT.NONE);
 		gridData = new GridData(GridData.END);
 		composite3.setLayoutData(gridData);
 		composite3.setLayout(new FillLayout(SWT.HORIZONTAL));
 			
 	    Button setButton = new Button(composite3, SWT.PUSH);
 	    setButton.setAlignment(SWT.BOTTOM);
 	    setButton.setText("set");
 	    setButton.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 //				System.out.println(text_freq.getText());
 			}
 	    });
 
 	}
 	
 	@Override
 	public void registerForSensor(String sensorID, boolean register)
 	{
 		m_admin_con.registerForSensor(sensorID, register);
 		
 		if((m_configItems != null) && (m_admin_con != null))
 		{
 			Configuration conf = m_configItems.get(sensorID);
 			m_admin_con.setSensorConfig(sensorID, conf.id, register +"");
 		}
 		
 	}
 	
 	public void setSettingElement()
 	{
 		new Thread( new Runnable() {
             public void run() {
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException e1) {
                     e1.printStackTrace();
                 }
                 m_shell.getDisplay().syncExec( new Runnable() {
                     public void run() {
 
                     	Configuration conf = configItems.firstElement();
                     	if(conf.type == Configuration.SettingType.number)
                 		{
                 			Label label_freq = new Label(composite3, SWT.LEFT);
                 			label_freq.setText(conf.displayName);
 
                 		    Text text_freq = new Text(composite3, SWT.BORDER);
                 		    text_freq.setText(conf.value);
                 		    text_freq.setLocation(100, 100);
                 		}
 
                     	composite3.pack();
                     	configItems.remove(conf);
                     }
                 }); 
             }
         } ).start();
 
 	}
 
 
 	@Override
 	public void onSensorActivated(Sensor s) {
 		if (LOGGING)
 			log.info("sensor activated: " + s);
 		if (m_autoRegister) {
 			if (LOGGING)
 //				log.info("automatically registering for sensor!");
 			if (!m_sensors.contains(s.ident)) {				
 				m_sensors.add(s.ident);
 			} else {
 				//log.info("already registered for that sensor");
 			}
 		}
 		m_sensors_map.put(s.ident, s);
 		msgsReceivedCount++;
 	}
 	
 	@Override
 	public void onSensorDeactivated(String sensorId) {
 		if (LOGGING)
 			log.info("sensor " + sensorId + " deactivated");
 		msgsReceivedCount++;
 	}
 
 	@Override
 	public void onSensorConfigurationItem(String sensorId, Configuration conf) {
 		if (LOGGING)
 			log.info("sensor conf item for sensor " + sensorId + " " + conf);
 		msgsReceivedCount++;
 
 		m_configItems.put(sensorId, conf);
 		
 		if(!m_all_sensors.contains(sensorId) && sensorId != null)
 		{
 			m_all_sensors.add(sensorId);
 			registerForSensor(sensorId, true);
 			super.addGuiElements(m_sensors_map.get(sensorId));
 		}
 		
 	}
 	
 
 	@Override
 	public void onServerConfigurationItem(Configuration conf) {
 		if (LOGGING)
 			log.info("server conf item " + conf);
 		msgsReceivedCount++;
 	}
 
 	@Override
 	public void onClientConnected(final Client client) {
 		if (LOGGING)
 			log.info("client connected " + client);
 		msgsReceivedCount++;
 		
 		new Thread( new Runnable() {
             public void run() {
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException e1) {
                     e1.printStackTrace();
                 }
                 m_shell.getDisplay().syncExec( new Runnable() {
                     public void run() {
                     	
                     	//show connect clients
                     	// Create a read-only text field
                 	    Text clients = new Text(m_group2, SWT.READ_ONLY | SWT.BORDER);
                 	    clients.setText(client.m_address);
                 	    clients.setData("client", client.m_id);
                 	    
                 	    m_group2.pack();
                     }
                 }); 
             }
         } ).start();
 	}
 
 	@Override
 	public void onClientDisconnected(final Client client) {
 		if (LOGGING)
 			log.info("client disconnected " + client);
 		msgsReceivedCount++;
 		
 		new Thread( new Runnable() {
             public void run() {
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException e1) {
                     e1.printStackTrace();
                 }
                 m_shell.getDisplay().syncExec( new Runnable() {
                     public void run() {
                     	
                     	//remove clients from gui
                    	Control[] checkboxchilds = m_group2.getChildren();
                    	System.out.println("blub" + checkboxchilds);
                 		for (Control child : checkboxchilds) {
                 			if(client.m_id.equals(child.getData("client")))
                 			{
                				System.out.println("blub");
                 				child.dispose();
                 				break;
                 			}
                 		}
                		m_group2.pack();
                     }
                 }); 
             }
         } ).start();
 	}	
 }
 
 
 
