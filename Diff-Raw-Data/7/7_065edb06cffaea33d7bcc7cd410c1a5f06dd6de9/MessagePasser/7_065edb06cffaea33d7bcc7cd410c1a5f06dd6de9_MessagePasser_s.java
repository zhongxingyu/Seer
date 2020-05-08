 import java.io.*;
 import java.security.AccessControlException;
 import java.security.AccessController;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.net.*;
 import java.util.concurrent.atomic.*;
 import java.util.concurrent.locks.*;
 import org.yaml.snakeyaml.*;
 
 public class MessagePasser {
 	
 	/* Fields */
 	int max_vals = 7; //max number of fields in config file for a rule
 	int max_config_items = 100; //this is an arbitrary number to take place of hard-coded values
 	String[][] conf = new String[max_vals][max_config_items]; //holds the raw configuration portion of the YAML file
 	String[][] send_rules = new String[max_vals][max_config_items]; //holds the raw send rules from the YAML file
 	String[][] recv_rules = new String[max_vals][max_config_items]; //holds the raw receive rules from the YAML file
 	String[] conf_headers = {"name", "ip", "port"};
 	String[] send_recv_headers = {"action", "src", "dest", "kind", "id", "nth", "everynth"};
 	String config_file;
 	String local_name;
 	
 	private static MessagePasser uniqInstance = null;
 	static ReentrantLock globalLock = new ReentrantLock(); // may be used to synchronize 
 	AtomicInteger message_id = new AtomicInteger(0); // atomic message id counter
 	AtomicInteger mcast_msg_id = new AtomicInteger(0); //atomic multicast message ID counter
 	Hashtable<String, ConnState> connections = new Hashtable<String, ConnState>(); // maintain all connection state information
 	TreeMap<String, Integer> names_index = new TreeMap<String, Integer>(); //stores the name-index mapping
 	MessageBuffer send_buf;
 	MessageBuffer rcv_buf;
 	MessageBuffer rcv_delayed_buf;
 	AtomicInteger rcv_delay_buf_ready = new AtomicInteger(0);
 	
     /* new fields for MC */
 	int num_nodes;
 	ArrayList<String> name_list;
 	ArrayList<Integer> mc_seqs = new ArrayList<Integer>();  // organize sequence # according to alphabetical order
 	ArrayList<HBItem> hbq = new ArrayList<HBItem>();
 	
 	
 	/* Constructor */
 	private MessagePasser() {		
 		this.send_buf = new MessageBuffer(1000); //setup fixed-size buffers
 		this.rcv_buf = new MessageBuffer(1000);
 		this.rcv_delayed_buf = new MessageBuffer(1000);
 	}
 	
 	
 	/* Accessors */
 	
 	public static synchronized MessagePasser getInstance() {
 		/* Provides a way for other classes to get the
 		 * singleton instance of this class. */
 		
 		if (uniqInstance == null) {
 			uniqInstance = new MessagePasser();
 		}
 		return uniqInstance;
 	}
 	
 	
 	public int getVectorSize()
 	{
 		/* Returns how large the vector clock should be made. */
 		
 		int numNames = 0;
 		String[] names = this.getField("name");
 		return names.length-2; //because we store the heading names as the first name and logger as last name
 	}
 	
 	
 	String[] getField(String field){
 		/* Accessor to return any field desired by the user
 		 * program. */
 		
 		String[] tmp = null;
 		int i, j;
 		
 		for(i=0; i<send_rules.length; i++)
 		{
 			for(j=0; j<send_rules[i].length; j++)
 			{
 				if(send_rules[i][j].equals("*"))
 					break;
 			}
 			if(send_rules[i][0].equalsIgnoreCase(field))
 			{
 				tmp = Arrays.copyOfRange(send_rules[i], 0, j);
 				return tmp;
 			}
 		}
 		
 		for(i=0; i<recv_rules.length; i++)
 		{
 			for(j=0; j<recv_rules[i].length; j++)
 			{
 				if(recv_rules[i][j].equals("*"))
 					break;
 			}
 			if(recv_rules[i][0].equalsIgnoreCase(field))
 			{
 				tmp = Arrays.copyOfRange(recv_rules[i], 0, j);
 				return tmp;
 			}
 		}
 		
 		for(i=0; i<conf.length; i++)
 		{
 			for(j=0; j<conf[i].length; j++)
 			{
 				if(conf[i][j].equals("*"))
 					break;
 			}
 			if(conf[i][0].equalsIgnoreCase(field))
 			{
 				tmp = Arrays.copyOfRange(conf[i], 0, j);
 				return tmp;
 			}
 		}
 		System.out.println("Could not match "+field+" to any field.");
 		return tmp;
 	}
 	
 	
 		
 	/* Mutators */
 	
 	public void setConfigAndName(String configuration_filename, String local_name) {
 		/* Assigns user input to specify configuration file and local application name. */
 		
 		this.config_file = configuration_filename;
 		this.local_name = local_name;		
 	}
 		
 	
 	public void clearCounters()
 	{
 		/* Resets all of the counters associated with
 		 * the application. */
 		
 		Set<String> keys = this.connections.keySet();
 		Iterator<String> itr = keys.iterator();
 	
 		if(itr.hasNext()) {
 			String key = itr.next();
 			ConnState conn = this.connections.get(key);
 			conn.in_messsage_counter.set(0);
 			conn.out_message_counter.set(0);
 			conn.special_rules.clear(); //empty all rule mappings when a config file changes 
 		}
 	}
 	
 	
 	public void listNodes()
 	  {
 		/* Creates an index of name-index pairs */
 		
 	    String[] names = getField("name");
 	    TreeMap<String, Integer> tmp = new TreeMap<String, Integer>();
 
 	    for (int i = 0; i < names.length; i++)
 	    {
 	      if ((!names[i].equalsIgnoreCase("logger")) && (!names[i].equalsIgnoreCase("name")))
 	      {
 	        tmp.put(names[i], Integer.valueOf(i));
 	      }
 	    }
 	    int j = 0;
 
 	    for (Map.Entry entry : tmp.entrySet())
 	    {
 	      String name = (String)entry.getKey();
 	      this.names_index.put(name, Integer.valueOf(j));
 	      j++;
 	    }
 	    
 	    /* for multi-casting messages */
 	    this.num_nodes = this.names_index.size();
 	    for(int i = 0; i < this.num_nodes; i++) {
 	    	this.mc_seqs.add(0);
 	    }
 	    this.name_list = new ArrayList<String>();
 	    for(int i = 0; i < this.num_nodes; i++) {
 	    	Iterator<String> itr = this.names_index.keySet().iterator();
 	    	while(itr.hasNext()) {
 	    		String name = itr.next();
 	    		if(this.names_index.get(name).intValue() == i) {
 	    			this.name_list.add(name);
 	    			break;
 	    		}
 	    	}
 	    }
 	    
 	  }
 	
 	/*
 	 * Insert msg to the HBQ, in Vector timestamp order
 	 */
 	public void insertToHBQ(HBItem hbi) {
 	
 		/* find the right position in HBQ */
 		System.out.println("HBQ size: "+ this.hbq.size());
 		int i = 0; 
 		for(; i < this.hbq.size(); i++) {
 			if(hbi.compareOrder(this.hbq.get(i)) <= 0)
 				break;
 		}
 		System.out.println("Before add");
 		this.hbq.add(i, hbi);
 		System.out.println("After add");
 	}
 
 	
 	/*
 	 * Return the first ready message in Vector timestamp order
 	 * Which means the first message in HBQ
 	 */
 	public TimeStampedMessage getReadyMessage() {
 		
 		TimeStampedMessage ready_msg = null;
 		
 		if(!this.hbq.isEmpty()) {
 			HBItem first_item = this.hbq.get(0);
 			if(first_item.isReady()) {
 					this.mc_seqs.set(this.names_index.get(first_item.src), first_item.mc_id);
 					ready_msg = this.hbq.remove(0).message;
 					System.out.println("Ready message: ");
 					ready_msg.print();
 			}
 		}
 		return ready_msg;
 		
 	}
 
     	
 	/*
 	 * Determine whether msg is in HBQ, based on src + mc_id
 	 */
 	public boolean isInHBQ(TimeStampedMessage msg) {
 		
 		boolean is_in = false;
 
 		/* get a multicast message */
 		if(msg.type.equals("multicast") && !msg.kind.equals("ack")) {
 			for(int i = 0; i < this.hbq.size(); i++) {
 				if(this.hbq.get(i).src.equals(msg.src) && this.hbq.get(i).mc_id == msg.mc_id) {
 					is_in = true;
 					break;
 				}	
 			}
 		}
 			
 		/* get a ACK message */
 		else if(msg.type.equals("multicast") && msg.kind.equals("ack")) {							
 			String[] payload = ((String)msg.payload).split("\t");
 			String src = payload[0];
 			int mc_id = Integer.parseInt(payload[1]);
 			
 			for(int i = 0; i < this.hbq.size(); i++) {
 				if(this.hbq.get(i).src.equals(src) && this.hbq.get(i).mc_id == mc_id) {
 					is_in = true;
 					break;
 				}
 			}
 		}
 			
 		/* get a retransmit kind message */
 		else if(msg.kind.equals("retransmit")) {
 			TimeStampedMessage message = (TimeStampedMessage)msg.payload;
 		
 			for(int i = 0; i < this.hbq.size(); i++) {
 				if(this.hbq.get(i).src.equals(message.src) && this.hbq.get(i).mc_id == message.mc_id) {
 					is_in = true;
 					break;
 				}
 			}
 		}
 			
 		else
 			;
 		
 //		
 //		for(int i = 0; i < this.hbq.size(); i++) {
 //			if(this.hbq.get(i).src.equals(msg.src) && this.hbq.get(i).mc_id == msg.mc_id) {
 //				is_in = true;
 //				break;
 //			}
 //		}
 		
 		return is_in;
 		
 	}
 	
 
 	
 	/*
 	 * Determine whether this message is out-of-date
 	 */
 	public boolean isUsefulMessage(TimeStampedMessage msg) {
 		
 		boolean is_useful = false;
 		
 		/* Check if this mc message is out-of-date */
 		
 		/* get a multicast message */
 		if(msg.type.equals("multicast") && !msg.kind.equals("ack")) {
 			int index = this.names_index.get(msg.src);
 			if(msg.mc_id > this.mc_seqs.get(index))
 				is_useful = true;
 		}
 		
 		/* get a ACK message */
 		else if(msg.type.equals("multicast") && msg.kind.equals("ack")) {			
 			String[] payload = ((String)msg.payload).split("\t");
 			int index = this.names_index.get(payload[0]);
 			if(Integer.parseInt(payload[1]) > this.mc_seqs.get(index))
 				is_useful = true;		
 		}
 		
 		/* get a retransmit kind message */
 		else if(msg.kind.equals("retransmit")) {
 			TimeStampedMessage message = (TimeStampedMessage)msg.payload;
 			int index = this.names_index.get(message.src);
 			if(message.mc_id > this.mc_seqs.get(index))
 				is_useful = true;
 		}
 		
 		else
 			;
 		
 		return is_useful;
 	}
 	
 	
 	public void tryAcceptAck(TimeStampedMessage msg) {
 		for(int i = 0; i < this.hbq.size(); i++) {
 			this.hbq.get(i).tryAcceptAck(msg);
 		}
 	}
 	
     	
 	public HBItem getHBItem(String src, int mc_id) {
 		
 		HBItem hbi = null;
 		
 		for(int i = 0; i < this.hbq.size(); i++) {
 			if(this.hbq.get(i).src.equals(src) && this.hbq.get(i).mc_id == mc_id) {
 				hbi = this.hbq.get(i);
 				break;
 			}
 		}
 		
 		return hbi;
 	}
 	
 	
 	public String getName(int index) {
 		
 		String name = ";";
 		Iterator<String> itr = this.names_index.keySet().iterator();
 		
 		while(itr.hasNext()) {
 			String cur_name = itr.next();
 			if(this.names_index.get(cur_name) == index) {
 				name = cur_name;
 				break;
 			}
 		}
 		
 		return name;
 	}
 
 	
 	/* Initialization Methods */
 		
 	public void runServer() {
 		
 		this.listNodes();
 		
 		// Init the local server which waits for incoming connection
 		Runnable runnableServer = new ServerThread();
 		Thread threadServer = new Thread(runnableServer);
 		threadServer.start();
 		
 		// Init HBQ thread
 		Runnable runnableHBQ = new HBQThread();
 		Thread threadHBQ = new Thread(runnableHBQ);
 		threadHBQ.start();
 	}
 	
 	
 	void initHeaders()
 	{
 		/* Statically defines the headers for each of the arrays. */
 		
 		for(int i=0; i<conf_headers.length; i++)
 			conf[i][0] = conf_headers[i];
 		
 		for(int j=0; j<send_recv_headers.length; j++)
 		{
 			send_rules[j][0] = send_recv_headers[j];
 			recv_rules[j][0] = send_recv_headers[j];
 		}
 	}
 	
 	
 	void parseConfig(String fname) {
 		/* Parses the configuration file and stores all of the sections into
 		 * their own 2D arrays. Any field not present is stored as "*" to 
 		 * denote a wildcard functionality. */
 		
 		InputStream yamlInput = null;
 		Yaml yaml = new Yaml();
 		String config = "";
 		
 		try {
 			yamlInput = new FileInputStream(new File(fname));			
 		} catch (FileNotFoundException e) { 
 			//error out if not found
 			System.out.println("File "+fname+" does not exist.\n");
 			System.exit(-1); 
 		}
 		
 		Object data = yaml.load(yamlInput);
 		LinkedHashMap<String,String> file_map = (LinkedHashMap<String,String>) data;
 		
 		String whole = "";
 		String[] elements = new String[10]; //figure out appropriate size for these
 		String[] pairs = new String[10];
 		String file_part = "";	
 		
 		for(int ctr=0; ctr<max_vals; ctr++) //quickly initialize all elements
 		{
 			for(int j=1; j<10; j++) //j=1 because the headers are already initialized by initHeaders
 			{
 				conf[ctr][j] = "*";
 				send_rules[ctr][j] = "*";
 				recv_rules[ctr][j] = "*";
 			}
 		}
 		
 		Set set = file_map.entrySet();
 		Iterator i = set.iterator();
 		int j = 0;
 		
 		while(i.hasNext())
 		{
 			Map.Entry me = (Map.Entry)i.next();
 			file_part = me.getKey().toString().toLowerCase();
 			ArrayList<String> inner = new ArrayList<String>();
 			inner.add(me.getValue().toString());
 			whole = inner.toString();
 			whole = whole.replaceAll("[\\[\\]\\{]", "");
 			elements = whole.split("\\},?");
 			
 			for(j=0; j<elements.length; j++) //the number of rules in this section of the config
 			{				
 				pairs = elements[j].split(", "); //the number of elements in a particular rule
 				
 				if(file_part.equals("sendrules"))
 					fillLoop(send_rules, pairs, j+1); //pass in 2D array, all of rule's elements in key-val form, and rule number
 				else if(file_part.equals("receiverules"))
 					fillLoop(recv_rules, pairs, j+1);
 				else if(file_part.equals("configuration")) //handle config third because it happens only once
 					fillLoop(conf, pairs, j+1);
 				else
 				{
 					System.out.println("Error parsing configuration file");
 					break;
 				}
 			}
 		}	
 				
 		try {
 			yamlInput.close();
 		} catch (IOException e) {
 			System.out.println("Could not close configuration file\n");
 		}
 	}
 	
 	
 	void fillLoop(String[][] arr, String[] pairs, int rule_num)
 	{
 		/* Populates all of the configuration file options into a 2D array.
 		 * This must be called for each major section in the config file
 		 * (such as configuration, send_rules, and receive_rules). */
 		
 		int key=0;
 		int val=1;
 		
 		for(int i=0; i<pairs.length; i++)
 		{
 			String[] choices = pairs[i].split("=");
 			choices[key] = choices[key].trim().toString().toLowerCase(); //grab heading
 
 			for(int j=0; j<arr.length; j++) //loop through 2D array's headers and put the values where they belong
 			{
 				if(arr[j][key].equals(choices[key])) //we found the correct heading
 				{
 					arr[j][rule_num] = choices[val]; //only set the ones with real values; all the rest stay as a wildcard
 					break; //go to next pair instead of continuing through loop needlessly
 				}
 			}
 		}
 	}
 	
 	
 	public String[][] populateOptions(MessagePasser mp, String user_input, int max_fields, int max_options)
 	{
 		/*Stores all of the possible options for each field of a message from
 		 *the lab information (such as for the message action) and from the 
 		 *configuration file (in the case of names, for example) into a 
 		 *two-dimensional array.
 		 */
 
 		String[][] all_fields = new String[max_fields][max_options]; 
 		int ctr;
 		
 		for(ctr=0; ctr<max_fields; ctr++) //quickly initialize all elements
 		{
 			for(int j=0; j<max_options; j++)
 				all_fields[ctr][j] = "";
 		}
 		
 		for (ctr = 0; ctr<max_fields; ctr++) 
 		{
 			switch(ctr)
 			{
 				case 0: //type of message
 					all_fields[ctr][1] = "send";
 					all_fields[ctr][2] = "receive";
 					break;
 				case 1:
 					String[] names = mp.getField("name");
 					int i;
 					for(i=0; i<names.length; i++)
 					{
 						if(!names[i].equals("*"))
 							all_fields[ctr][i] = names[i];
 					}
 					all_fields[ctr][i] = "*";
 					break;
 				case 2: //what kind of message
 					String[] kind = mp.getField("kind");
 					for(i=0; i<kind.length; i++)
 					{
 						if(!kind[i].equals("*"))
 							all_fields[ctr][i] = kind[i];
 					}
 					all_fields[ctr][i] = "*"; //since it's possible to have a message with only an action specified
 					break;
 				default:
 					break;
 			}
 		}
 		return all_fields;
 	}
 	
 	
 	public void buildRule(HashMap rule, int ctr, String type)
 	{
 		/* Builds the rule in an easy to use format for user
 		 * examination. Fields not specified in a rule are 
 		 * stored as "*". */
 		
 		int header = 0;
 		
 		if(type.equals("send"))
 		{
 			for(int i=0; i<send_rules.length; i++)
 				rule.put(send_rules[i][header], send_rules[i][ctr]);			
 			rule.put("rule_num", ctr); //add a field stating which rule number a rule is
 		}
 		else if(type.equals("receive"))
 		{
 			for(int i=0; i<recv_rules.length; i++)
 				rule.put(recv_rules[i][header], recv_rules[i][ctr]);
 			rule.put("rule_num", ctr+send_rules.length); //add a field stating which rule number a rule is
 			/* This last "put" is needed to take into account the send rules so as to not overwrite
 			 * the value of a send rule in the HashMap. */
 		}
 	}
 	
 	
 	/* Communication Functions */
 	
 	
 	public void multicastAck(TimeStampedMessage message)
 	{
 		/* Wrapper to provide the ability to transmit acknowledgment of
 		 * a message in the system. This is used to provide the foundation
 		 * of reliability. The incoming message is the original message
 		 * that is being acknowledged. */
 		
 		String payload = message.src+"\t"+message.mc_id+"\t"+message.ts.toString();
 		ClockService clock = ClockService.getInstance("vector", getVectorSize());
 		TimeStamp ts = null;
 		
 		for (Map.Entry entry : this.names_index.entrySet()) 
 	    {
 			String dest = (String)entry.getKey();
 			if(dest.equals(this.local_name))
 				continue; //don't send yourself an ack
 			TimeStampedMessage newMsg = new TimeStampedMessage(ts, local_name, dest, "ack", message.type, payload);
 			System.out.println("Sending a multicast ACK to acknowledge "+message.toString());
 			this.send(newMsg, clock);
 	    }
 	}
 	
 	
 	public void resend(TimeStampedMessage message, String dest)
 	{
 		/* This offers a destination (who has received a multicast message) 
 		 * to resend this message to a node that it has detected as not 
 		 * having received the original message. The original message is
 		 * sent as the payload of this method's message. */
 		
 		ClockService clock = ClockService.getInstance("vector", getVectorSize());
 		TimeStamp ts = null;
 		TimeStampedMessage newMsg = new TimeStampedMessage(ts, local_name, dest, "retransmit", "unicast", message);
 		this.send(newMsg, clock);
 		
 	}
 	
 	
 	public void send(Message message, ClockService clock) {
 		/* Sends a message to the specified destination. If there are messages
 		 * in the delay buffer, they will be sent if all conditions match for
 		 * sending. */
 		
 		// return if message is null
 		if(message == null) 
 			return;
 		
 		// get the output stream
 		ObjectOutputStream oos = null;
 		
 		try{	
 			// get the connection state information
 			ConnState conn = this.connections.get(message.dest);
 			
 			// if connection has not been established yet, set it up
 			if(conn == null) {			
 				// get the meta information of the remote host
 				String remote_addr = "";
 				int port = 0;
 				String remote_name = message.dest;
 
 				for(int i = 1; i < 10; i++) {
 					if(this.conf[0][i].equals(remote_name)) {
 						remote_addr = conf[1][i];
 						port = Integer.parseInt(conf[2][i]);
 						break;
 					}
 				}
 				
 				// remote host not found 
 				if(remote_addr.equals(""))
 					return;
 				
 				// prepare remote ip_addr and port number
 				String[] addr = remote_addr.split("\\.");	
 				byte[] iaddr = {(byte)Integer.parseInt(addr[0]), (byte)Integer.parseInt(addr[1]), 
 						(byte)Integer.parseInt(addr[2]), (byte)Integer.parseInt(addr[3])};
 				InetAddress ia = InetAddress.getByAddress(iaddr);
 				
 				Socket s = new Socket(ia, port); //create the socket, and connect to the other side	
 				
 				// after connected init the connection state information				
 				conn = new ConnState(message.dest, s);
 				conn.setObjectOutputStream(new ObjectOutputStream(s.getOutputStream()));
 				conn.setObjectInputStream(new ObjectInputStream(s.getInputStream()));
 				this.connections.put(remote_name, conn);
 				
 				// send a LOGIN message immediately to notify the other side who am I
 				oos = this.connections.get(remote_name).getObjectOutputStream();
 				
 				oos.writeObject(new Message(this.local_name, "", "LOGIN", "", null));
 				
 				oos.flush();
 				
 				// also setup the receiveThread on this connection
 				Runnable receiveR = new ReceiveThread(remote_name);
 				Thread receiveThread = new Thread(receiveR);
 				receiveThread.start();
 				System.out.println("Connection to "+remote_name+" has been established");
 			}
 			else //the connection is already set up
 				oos = this.connections.get(message.dest).getObjectOutputStream();
 
 			// get the first rule matched against this outgoing message
 			HashMap rule = this.matchRules("send", message);
 			
 			if(rule != null && (rule.containsKey("nth") || rule.containsKey("everynth"))) //figure out if it has an Nth/EveryNth field in it
 			{
 				int rule_num = (Integer)rule.get("rule_num");
 				int times=0;
 				times = this.connections.get(message.getVal("dest", message)).getTimesRuleSeen(rule_num); //get value, since it's already initialized if we're here
 
 				String everynth = rule.get("everynth").toString();
 				String nth = rule.get("nth").toString();
 
 				/* Have to be careful here...if both Nth/Ev are *, keep the rule. */
 				try{ //everyNth is a number
 					int eNth = Integer.parseInt(everynth);
 					if((!(nth).equals("*") || !(everynth).equals("*")) && (!(nth).equals(times+"") && !((times % eNth) == 0))) //if neither is a * and none match
 						rule = null;
 				} catch (NumberFormatException e) { //everyNth is not a number
 					if((!(nth).equals("*") || !(everynth).equals("*")) && (!(nth).equals(times+"") && !everynth.equals(times+"")))
 						rule = null;
 				}
 			}
 			
 			message = clock.affixTimestamp((TimeStampedMessage)message); //get a timestamp for the message
 			
 			if(message.type.equals("multicast")) //set the multicast ID
 			{
 				if(message.dest.equals(this.names_index.firstKey()))
 					message.set_mcast_id(this.mcast_msg_id.incrementAndGet());//increment multicast ID for first message in a multicast series
 				else
 					message.set_mcast_id(this.mcast_msg_id.get());//add the same multicast ID all subsequent messages in a multicast series
 			}
 			
 			// check against the send rules, and follow the first rule matched
 			if(rule != null) {
 				// 3 actions: duplicate, drop, and delay
 				String action = rule.get("action").toString();
 				
 				if(action.equals("drop")) { // action: drop -- simply return
 					
 					message.set_id(this.message_id.getAndIncrement());
 					((TimeStampedMessage)message).ts = clock.getTimestamp(); //give the current timestamp value to the message's ts
 					
 					System.out.println("**************************************************************************");
 					System.out.println("Main Thread $$ send: src: " + message.src + " dest: " + message.dest);
 					if(message.type.equals("multicast")) { System.out.println("MID: "+ message.mc_id); } //only print field if multicast message
 					System.out.println("ID: " + message.id + " kind: " + message.kind + " type: " + message.type + 
 									   " timestamp: " + ((TimeStampedMessage)message).ts.toString());
 					System.out.println("rule: drop");
 					System.out.println("**************************************************************************");
 					
 					return;
 				}
 				else if(action.equals("duplicate")) { // action: duplicate -- send two identical messages, but with different message id
 					// step 1: send two identical messages, with same message_id
 					message.set_id(this.message_id.get());
 					((TimeStampedMessage)message).ts = clock.getTimestamp();
 
 					if(handleSelf(message))
 						return;
 					oos.writeObject((TimeStampedMessage)message);
 					oos.flush();
 					conn.getAndIncrementOutMessageCounter();
 					
 					System.out.println("**************************************************************************");
 					System.out.println("Main Thread $$ send: src: " + message.src + " dest: " + message.dest);
 					if(message.type.equals("multicast")) { System.out.println("MID: "+ message.mc_id); }
 					System.out.println("ID: " + message.id + " kind: " + message.kind + " type: " + message.type + 
 							   " timestamp: " + ((TimeStampedMessage)message).ts.toString());
 					System.out.println("rule: duplicate");
 					System.out.println("**************************************************************************");
 					
 					//because duplicated messages should have different timestamps
 					message = new TimeStampedMessage(null, message.src, message.dest, message.kind, message.type, message.payload);
 			        message.set_id(this.message_id.getAndIncrement());
 			        if(message.type.equals("multicast"))
 			        {
 			        	message.set_mcast_id(this.mcast_msg_id.get()); //ID should be the same
 			        	if(!message.dest.equals(this.names_index.firstKey()))
 			        			clock.incrementTimeStamp();
 			        	/*multicast dup timestamp should be advanced and then "rolled back" (for the next 
 			        	 * message in the non-duplicate multicast series) to match the timestamp behavior 
 			        	 * of unicast. */
 			        }
 			        
 			        message = clock.affixTimestamp((TimeStampedMessage)message);
 			        
 			        if(handleSelf(message))
 						return;
 					oos.writeObject((TimeStampedMessage)message);
 					oos.flush();
 					conn.getAndIncrementOutMessageCounter();
 
 					System.out.println("**************************************************************************");
 					System.out.println("Main Thread $$ send: src: " + message.src + " dest: " + message.dest);
 					if(message.type.equals("multicast")) { System.out.println("MID: "+ message.mc_id); }
 					System.out.println("ID: " + message.id + " kind: " + message.kind + " type: " + message.type + 
 							   " timestamp: " + ((TimeStampedMessage)message).ts.toString());
 					System.out.println("rule: duplicated message");
 					System.out.println("**************************************************************************");
 					
 					if(message.type.equals("multicast") && !message.dest.equals(this.names_index.lastKey()))
 						clock.decrementTimeStamp(); //"roll back" to proper value (NOTE: this will look wrong if the last name isn't running in the system)
 					
 					if(!dispatch_delayed(message)) //conditionally release the delay buffer
 						return;
 					
 					// step 2: flush send buffer
 					ArrayList<Message> delayed_messages = this.send_buf.nonblockingTakeAll();
 					while(!delayed_messages.isEmpty()) {
 						
 						TimeStampedMessage dl_message = (TimeStampedMessage)delayed_messages.remove(0);
 						
 						if(handleSelf(dl_message))
							return;
 						oos.writeObject(dl_message);
 						oos.flush();
 						conn.getAndIncrementOutMessageCounter();
 
 						System.out.println("******************************************************************");
 						System.out.println("Main Thread $$ send: src: " + dl_message.src + " dest: " + dl_message.dest);
 						if(dl_message.type.equals("multicast")) { System.out.println("MID: "+ dl_message.mc_id); }
 						System.out.println("ID: " + dl_message.id + " kind: " + dl_message.kind + " type: " + dl_message.type + 
 								   " timestamp: " + dl_message.ts.toString());
 						System.out.println("rule: delayed message released");
 						System.out.println("******************************************************************");
 					}
 				}
 				else { // action: delay -- put the message in the send_buf
 					message.set_id(this.message_id.getAndIncrement());
 					((TimeStampedMessage)message).ts = clock.getTimestamp(); //because we are being rules agnostic on timestamp values
 					
 					this.send_buf.nonblockingOffer((TimeStampedMessage)message);
 					
 					System.out.println("******************************************************************");
 					System.out.println("Main Thread $$ send: src: " + message.src + " dest: " + message.dest);
 					if(message.type.equals("multicast")) { System.out.println("MID: "+ message.mc_id); }
 					System.out.println("ID: " + message.id + " kind: " + message.kind + " type: " + message.type + 
 							   " timestamp: " + ((TimeStampedMessage)message).ts.toString());
 					System.out.println("rule: delay");
 					System.out.println("******************************************************************");
 				}
 			}
 			else { //no rule matched
 				try {
 					
 					// step 1: write this object to the socket
 					message.set_id(this.message_id.getAndIncrement());
 					((TimeStampedMessage)message).ts = clock.getTimestamp();
 
 					if(handleSelf(message))
 						return;
 					oos.writeObject((TimeStampedMessage)message);
 					oos.flush();
 
 					conn.getAndIncrementOutMessageCounter();
 					
 					System.out.println("**************************************************************************");
 					System.out.println("Main Thread $$ send: src: " + message.src + " dest: " + message.dest);
 					if(message.type.equals("multicast")) { System.out.println("MID: "+ message.mc_id); }
 					System.out.println("ID: " + message.id + " kind: " + message.kind + " type: " + message.type + 
 							   " timestamp: " + ((TimeStampedMessage)message).ts.toString());
 					System.out.println("rule: n/a");
 					System.out.println("**************************************************************************");
 										
 					if(!dispatch_delayed(message))
 					{
 						//System.out.println("Not releasing delay buffer");
 						return;
 					}
 					
 					// step 2: send all delayed messages
 					ArrayList<Message> delayed_messages = this.send_buf.nonblockingTakeAll();
 					while(!delayed_messages.isEmpty()) {
 						
 						// send single delayed message at once
 						TimeStampedMessage dl_message = (TimeStampedMessage)delayed_messages.remove(0);
 
 						if(handleSelf(dl_message))
							return;
 						oos.writeObject(dl_message);
 						oos.flush();
 						conn.getAndIncrementOutMessageCounter();
 						
 						System.out.println("**************************************************************************");
 						System.out.println("Main Thread $$ send: src: " + dl_message.src + " dest: " + dl_message.dest);
 						if(dl_message.type.equals("multicast")) { System.out.println("MID: "+ dl_message.mc_id); }
 						System.out.println("ID: " + dl_message.id + " kind: " + dl_message.kind + " type: " + dl_message.type + 
 								   " timestamp: " + dl_message.ts.toString());
 						System.out.println("rule: delayed message released");
 						System.out.println("**************************************************************************");
 					}
 				} catch(Exception e) {
 					e.printStackTrace();
 				}
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	
 	public Message receive(ClockService clock) {
 		/* Get single message once called. Receiving any non-delayed message 
 		 * will append all delayed message in the rcv_delay_buf to the tail
 		 * of rcv_buf.	It performs in blocking mode. If it takes a message
 		 * with "drop", it will return null. */		
 		
 		
 		// retrieve from the rcv_delay_buf if it is ready
 		if(this.rcv_delay_buf_ready.get() != 0) {
 			this.rcv_delay_buf_ready.decrementAndGet();
 
 			Message message = this.rcv_delayed_buf.blockingTake();	
 			ConnState conn = this.connections.get(message.src);
 			conn.getAndIncrementInMessageCounter();
 			
 			clock.updateTimestamp((TimeStampedMessage) message);
 			
 			System.out.println("******************************************************************");
 			System.out.println("Main Thread: in receive(): src: " + message.src + " dest: " + message.dest);
 			if(message.type.equals("multicast")) { System.out.println("MID: "+ message.mc_id); }
 			System.out.println(" ID: " + message.id + " kind: " + message.kind + " type: " + message.type + 
 							   " timestamp: " + ((TimeStampedMessage)message).ts.toString());
 			System.out.println("rule: n/a");
 			System.out.println("******************************************************************");
 
 			
 			return message;
 			
 		}
 		// get blocked here until one message comes
 		TimeStampedMessage message = (TimeStampedMessage)this.rcv_buf.blockingTake();
 
 		clock.updateTimestamp((TimeStampedMessage) message);
 		
 		// check against receive rules
 		message.print();
 		HashMap rule = this.matchRules("receive", (Message)message);
 		
 		if(rule != null && (rule.containsKey("nth") || rule.containsKey("everynth"))) //figure out if it has an Nth/EveryNth field in it
 		{
 			int rule_num = (Integer)rule.get("rule_num");
 			int times=0;
 			times = this.connections.get(message.getVal("src", message)).getTimesRuleSeen(rule_num); //get value, since it's already initialized if we're here
 
 			String everynth = rule.get("everynth").toString();
 			String nth = rule.get("nth").toString();
 
 			//have to be careful here...if both Nth/Ev are *, keep the rule.
 			try{ //everyNth is a number
 				int eNth = Integer.parseInt(everynth);
 				if((!(nth).equals("*") || !(everynth).equals("*")) && (!(nth).equals(times+"") && !((times % eNth) == 0))) //if neither is a * and none match
 					rule = null;
 			} catch (NumberFormatException e) { //everyNth is not a number
 				if((!(nth).equals("*") || !(everynth).equals("*")) && (!(nth).equals(times+"") && !everynth.equals(times+"")))
 					rule = null;
 			}
 		}
 		
 		
 		try {
 			
 			// get the right action
 			String action = "";
 			if(rule != null)
 				action = (String)rule.get("action");
 
 			
 			// get the connection state information
 			ConnState conn = this.connections.get(message.src);
 
 			// single rule matched
 			if(!action.equals(""))
 			{
 				/* 3 actions: duplicate, drop, and delay */
 				
 				// 1: drop -- drop the message and return null
 				if(action.equals("drop")) {
 					
 					System.out.println("******************************************************************");
 					System.out.println("Main Thread: in receive(): src: " + message.src + " dest: " + message.dest);
 					if(message.type.equals("multicast")) { System.out.println("MID: "+ message.mc_id); }
 					System.out.println(" ID: " + message.id + " kind: " + message.kind + " type: " + message.type + 
 									   " timestamp: " + ((TimeStampedMessage)message).ts.toString());
 					System.out.println("rule: drop");
 					System.out.println("******************************************************************");
 					
 					// it will not increment the incoming message counter
 					return null;
 				}
 				
 				// 2: duplicate 
 				else if(action.equals("duplicate")) {
 					
 					// step 1: duplicate another message, and append it to the tail of the rcv_delayed_buf
 					this.rcv_delayed_buf.nonblockingOfferAtHead(message);
 					
 					System.out.println("******************************************************************");
 					System.out.println("Main Thread: in receive(): src: " + message.src + " dest: " + message.dest);
 					if(message.type.equals("multicast")) { System.out.println("MID: "+ message.mc_id); }
 					System.out.println(" ID: " + message.id + " kind: " + message.kind + " type: " + message.type + 
 									   " timestamp: " + ((TimeStampedMessage)message).ts.toString());
 					System.out.println("rule: duplicate");
 					System.out.println("******************************************************************");
 
 					// set the delay buffer as ready
 					System.out.println("size of delay buf: "+this.rcv_delayed_buf.size());
 				
 					this.rcv_delay_buf_ready.set(this.rcv_delayed_buf.size());
 					
 					conn.getAndIncrementInMessageCounter();					
 					return message;
 				}
 				// 3: delay -- put it to the rcv_delay_buf and return null
 				else {
 					
 					this.rcv_delayed_buf.nonblockingOffer(message);
 					
 					System.out.println("******************************************************************");
 					System.out.println("Main Thread: in receive(): src: " + message.src + " dest: " + message.dest);
 					if(message.type.equals("multicast")) { System.out.println("MID: "+ message.mc_id); }
 					System.out.println(" ID: " + message.id + " kind: " + message.kind + " type: " + message.type + 
 									   " timestamp: " + ((TimeStampedMessage)message).ts.toString());
 					System.out.println("rule: delay");
 					System.out.println("******************************************************************");
 					
 					return null;
 				}
 			}
 			
 			// no rule matched
 			else {
 			
 				// step 2: return message
 				System.out.println("******************************************************************");
 				System.out.println("Main Thread: in receive(): src: " + message.src + " dest: " + message.dest);
 				if(message.type.equals("multicast")) { System.out.println("MID: "+ message.mc_id); }
 				System.out.println(" ID: " + message.id + " kind: " + message.kind + " type: " + message.type + 
 								   " timestamp: " + ((TimeStampedMessage)message).ts.toString());
 				System.out.println("rule: n/a");
 				System.out.println("******************************************************************");
 				
 				// set delay buffer as ready
 				this.rcv_delay_buf_ready.set(this.rcv_delayed_buf.size());
 				
 				conn.getAndIncrementInMessageCounter();
 				return message;
 			}
 						
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		return message;
 	}
 	
 	
 	public boolean dispatch_delayed(Message message)
 {
 	/* This function allows delayed messages to be released at the
 	 * appropriate time. For example, if a multicast message is sent
 	 * to all members, and there is a send rule delaying delivery of
 	 * the message to a subset of the members, then the message should
 	 * NOT be released until after the next message series NOT in this
 	 * multicast is sent (whether it be a unicast or another multicast).
 	 * This will preserve the ability to "mix up" messages to a destination. */
 	
 	Message tmp_msg = this.send_buf.buf.peek();
 	if(message.type.equals("unicast"))
 		return true; //this rule only applies to multicast messages in the same series
 	
 	if(tmp_msg != null && tmp_msg.mc_id != message.mc_id)
 	{
 		if(!message.dest.equals(names_index.lastKey()))
 			return false; //only dispatch after the last message in the NEXT multicast series has been sent
 		return true;
 	}
 	else
 		return false; //don't dispatch a delayed message that was only delayed in this multicast series
 }	
 
 
 	/* Miscellaneous Methods */
 	
 	public int validOption(String user_input)
 	{
 		/* Verifies user has entered a valid option
 		 * for an action. Returns a correct action
 		 * if one has been input. */
 		
 		int user_action = -1;
 		
 		if(user_input.length() > 1)
 		{
 			System.out.println("Unrecognized option "+user_input+". Choices are 1, 2, and 3.");
 			return -1;
 		}
 		
 		try {
 			user_action = Integer.parseInt(user_input);
 		} catch(NumberFormatException e) {
 			System.out.println(user_input+" is not an integer.");
 			return -1;
 		}
 		return user_action;
 	}	
 	
 	
 	public HashMap matchRules(String type, Message message)
 	{
 		/* Returns the first rule matched so appropriate actions
 		 * can be taken. */
 		
 		int header = 0;
 		HashMap rule = new HashMap();
 		
 		String[][] tmp = null;
 		
 		if(type.equals("send"))
 		{
 			tmp = new String[send_rules.length][];
 			for (int i = 0; i < send_rules.length; i++) 
 				tmp[i] = Arrays.copyOf(send_rules[i], send_rules[i].length);
 	    }
 		else if(type.equals("receive"))
 		{
 			tmp = new String[recv_rules.length][];
 			for (int i = 0; i < recv_rules.length; i++) 
 				tmp[i] = Arrays.copyOf(recv_rules[i], recv_rules[i].length);
 	    }
 		
 		for(int i=0; i<tmp.length; i++)
 		{
 			rule = new HashMap();
 			buildRule(rule, i, type); //builds the current rule
 			
 			/* Grab each rule that we build and check its fields against those in the message */
 			for(int j=0; j<send_recv_headers.length; j++)
 			{
 				//iterate through each of the fields and check if the rule value and the message vals match.
 				int ctr;
 				String field_name = send_recv_headers[j]; //the current field we're trying to check
 				int rule_tmp = (Integer)rule.get("rule_num"); //save a copy
 				rule.put("rule_num", "*"); //overwrite it for the check
 				
 				String vals = rule.values().toString().replaceAll("[\\[,\\] ]", "");
 
 				if(vals.matches("^\\**$")) //and all fields were wildcards, including action (thus not a real rule)
 				{					
 					rule = null;
 					break; //it didn't match
 				}
 				else
 					rule.put("rule_num", rule_tmp); //re-add the rule number, as it is a real rule
 				
 				if(field_name.equals("rule_num")) //not for use in here
 					continue;
 				else if(field_name.equals("action")) //we don't act on this here
 					continue;
 				else if(field_name.equals("nth") || field_name.equals("everynth")) //this value set will get checked right before send time
 					continue;
 				else if(field_name.equals("id")) //we have to access this specially
 				{
 					if(type.equals("receive"))
 						ctr = message.get_id();
 					else
 						ctr = this.message_id.get();
 					
 					if(!rule.get(field_name).equals(ctr+"") && !rule.get(field_name).equals("*"))
 					{
 						rule = null;
 						break; //it didn't match
 					}
 				}
 				else
 				{
 					if(!rule.get(field_name).toString().equalsIgnoreCase(message.getVal(field_name, message)) && !rule.get(field_name).equals("*"))
 					{
 						rule = null;
 						break; //it didn't match
 					}
 				}
 			}
 			if(rule != null) //we've successfully matched a rule
 			{
 				int times = 0;
 				int tmp_i= i; //save a copy of i in case we need it again
 				String direction = "dest"; //for the send side rules
 				if(type.equals("receive"))
 				{
 					i = i+send_rules.length; //so we don't erroneously overwrite vals of the send rules
 					direction = "src"; //for the receive side rules
 				}
 				if(this.connections.get(message.getVal(direction, message)).special_rules.containsKey(i)) //it has already been initialized
 						times = this.connections.get(message.getVal(direction, message)).getTimesRuleSeen(i); //get value 
 				this.connections.get(message.getVal(direction, message)).setTimesRuleSeen(i, times+1); //update it OR initialize it
 				i = tmp_i; //reset just in case
 			
 				return rule;
 			}
 		}
 		return rule; //at this point, we can safely return based on the above declarations of rule
 	}
 
 	
 	public boolean isNewestConfig(SFTPConnection svr_conn)
 	{
 		/* Determines if the current configuration file is up to date.
 		 * If not, it downloads the newest version. */
 
 		if(!svr_conn.isConnected())
 			svr_conn.connect(CONSTANTS.HOST, CONSTANTS.USER);
 		
     	int global_modification_time = svr_conn.getLastModificationTime(CONSTANTS.CONFIGFILE); // record the timestamp of YAML file
     	
     	if(global_modification_time != TestSuite.local_modification_time)
     	{
     		svr_conn.downloadFile(CONSTANTS.CONFIGFILE, CONSTANTS.LOCALPATH); // download the YAML file
     		TestSuite.local_modification_time = global_modification_time;
     		clearCounters(); //get rid of the Nth and EveryNth counters upon new config file
     		return false;
     	}
     	return true;
 	}
 	
 	
 	public boolean validateUserRequests(String user_input, MessagePasser mp, String local_name)
 	{
 		/* Determines whether the user has followed the usage
 		 * guidelines. A send rule can have three elements:
 		 * send <dest> <kind>
 		 *			OR
 		 * send multicast <kind>
 		 * A receive rule can have one element:
 		 * receive
 		 * 
 		 * Return: false (no) or true (yes)
 		 *  */
 		
 		String[] kind = mp.getField("kind");
 		String[] names = mp.getField("name");
 		int ctr = 0;
 		int max_fields = 3;
 		int max_options =  (names.length >= kind.length) ? names.length+1 : kind.length+1; 
 		int dest = 1;
 		String[] user_options = new String[max_options];
 		String[][] all_fields = new String[max_fields][max_options]; /*2D array; outside is all possible fields,
 																	   inner is all possible options*/
 		user_options = user_input.trim().split("\\s");
 		if(user_options.length == 1)
 		{
 			if(user_input.trim().equalsIgnoreCase("receive"))
 				return true;
 			else
 				return false;
 		}
 		else if(user_options.length != max_fields)
 		{	
 			System.out.println("Usage error - must be receive OR send <dest> <kind> OR send multicast <kind>");
 			return false;
 		}	
 		
 		/*if(user_options[dest].equalsIgnoreCase(local_name)) //same src and dest
 		{
 			System.out.println("Error - same src and dest "+local_name+". No loopback functionality offered.");
 			return false;
 		}*/
 		
 		all_fields = populateOptions(mp, user_input, max_fields, max_options);	
 		
 		for (ctr = 0; ctr<max_fields; ctr++) //verify user entered valid options 
 		{
 			for(int j=1; j<max_options; j++)
 			{
 				if((all_fields[ctr][j].equalsIgnoreCase((user_options[ctr].toString()))) || user_options[ctr].toString().equalsIgnoreCase("multicast"))
 					break; //found a match for that field
 				else if(all_fields[ctr][0].equalsIgnoreCase("kind") && all_fields[ctr][j].equals("*")) //because it can be anything really
 				{
 					break;
 				}
 				else
 				{
 					if(j == max_options-1)
 					{
 						if(all_fields[ctr][j].equals(""))
 							return false;
 					}
 					else
 						continue;
 				}
 				return false; //item not in list
 			}
 		}
 		return true;
 	}
 
 	
 	public void closeConnections() {
 		/* Closed all socket streams as well as the sockets. */		
 		
 		try {	
 			Iterator<String> itr = this.connections.keySet().iterator();
 			while(itr.hasNext()) {
 				String node = itr.next();
 				ConnState conn = this.connections.get(node);
 				conn.ois.close();
 				conn.oos.close();
 				conn.local_socket.close();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 
 	public boolean handleSelf(Message msg)
 	{
 		/* Determines if a message is to be sent to the sender */
 		
 		if(msg.src.equals(msg.dest))
 		{
 			this.insertToHBQ(new HBItem((TimeStampedMessage) msg));
 			this.tryAcceptAck((TimeStampedMessage)msg); //set my bits for having received the message
 			this.printHBQ();
 			return true;
 		}
 		return false;
 	}
 	
 	
 	/* Print Methods */
 	
 	public void printConnections() {
 		/* Print all connections. */
 		
 		System.out.println("#############################  MessagePasser Status  ###############################");
 		String conns = "--> We have connections: ";
 		Iterator<String> itr = this.connections.keySet().iterator();
 		while(itr.hasNext()) {
 			String next_conn = itr.next();
 			ConnState conn = connections.get(next_conn);
 			conns += "\t" + next_conn + ": in-message-counter = " + conn.in_messsage_counter
 					+ ", out-message-counter = " + conn.out_message_counter + "\n";
 		}
 		System.out.println(conns);
 	}
 	
 	public void printHBQ() {
 		for(int i = 0; i < this.hbq.size(); i++) {
 			System.out.println(this.hbq.get(i).toString());
 		}
 		System.out.println();
 	}
 	
 
 	public void print() {
 		
 		// print all connections
 		this.printConnections();
 		
 		// print all buffers
 		System.out.print("send_buf: ");
 		this.send_buf.print();
 		System.out.print("rcv_buf: ");
 		this.rcv_buf.print();
 		System.out.print("rcv_delayed_buf: ");
 		this.rcv_delayed_buf.print();
 		System.out.print("HBQ: ");
 
 	}
 	
 	
 	//	public void printVectorOrder() {
 	//
 	//		/* lock the logger at first */
 	//		//this.globalLock.lock();
 	//		
 	//		/* copy the queue at first */
 	//		ArrayList<TimeStampedMessage> queue = new ArrayList<TimeStampedMessage>();
 	//		for(int i = 0 ; i < this.rcv_buf.size(); i++)
 	//			queue.add((TimeStampedMessage)this.rcv_buf.get(i).clone());
 	//
 	//		/* unlock the logger */
 	//		//this.globalLock.unlock();
 	//		
 	//		/* use bubble sorting to figure out the order */
 	//	    boolean swapped = true;
 	//	    int j = 0;
 	//	    int tmp;
 	//	    while (swapped) {
 	//	    	
 	//	    	swapped = false;
 	//	        j++;
 	//	        
 	//	        for (int i = 0; i < queue.size() - j; i++) {
 	//	        	
 	//	        	boolean swap_needed = false;
 	//	        	
 	//	        	/* determine whether should swap the neighbor */
 	//	        	int order = queue.get(i).compareOrder(queue.get(i + 1));
 	//	        	if(order == 1)
 	//	        		swap_needed = true;
 	//	        	else if(order == 0) {
 	//	        		if(queue.get(i).src.compareTo(queue.get(i + 1).src) > 0)
 	//	        				swap_needed  = true;
 	//	        	}
 	//	        	else
 	//	        		;	// do nothing
 	//	        	
 	//	        	if (swap_needed) { 
 	//	        		Collections.swap(queue, i, i + 1);
 	//		            swapped = true;
 	//	        	}
 	//	        	
 	//	         }
 	//	    }		
 	//		
 	//		/* print out the ordered messages */
 	//	    System.out.println("#####################  The Ordering  #####################");
 	//	    for(int i = 0; i < queue.size(); i++) {
 	//	    	System.out.println(queue.get(i).toString());
 	//	    }
 	//	    System.out.println("##########################################################");
 	//
 	//	}		
 }
