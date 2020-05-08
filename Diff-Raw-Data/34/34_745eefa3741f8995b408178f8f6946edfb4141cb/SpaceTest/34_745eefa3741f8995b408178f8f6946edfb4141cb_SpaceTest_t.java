 package envJavaSpace;
 import java.awt.Point;
 import java.net.MalformedURLException;
 import java.rmi.*; 
 import java.util.Arrays;
 import java.util.concurrent.*; 
 
 import oopl.DistributedOOPL;
 import oopl.GUI.GUI;
  
 
 import apapl.Environment;
 import apapl.data.*;
 import aplprolog.prolog.Prolog;
 import aplprolog.prolog.builtins.ExternalActions;
 import aplprolog.prolog.builtins.ExternalTool;
 
 import net.jini.core.discovery.*;
 import net.jini.core.entry.*;
 import net.jini.core.lease.Lease;
 import net.jini.core.lookup.*;  
 import net.jini.space.*;
 
 /*
  * Extends Environment to be compatible with 2APL and implements ExternalTool to 
  * be compatible with my Prolog engine. 
  */
 public class SpaceTest  extends Environment implements ExternalTool{
 	public JavaSpace space; // shared data
 	public int clock = 0;
 	public DistributedOOPL oopl; // norm interpreter
 	public static String TYPE_STATUS="status",TYPE_OBJECT="object", TYPE_INVENTORY="inventory", NULL="null"; // for matching string with class type
 	public int[] ar_true, ar_null, ar_state_change; // precalculated IntProlog data 
 	public int INT_TUPLE=0, INT_POINT=0, INT_NULL=0;
 	public APAPLTermConverter converter; // Converts between IntProlog and 2APL
 	
 	/*
 	 * Just for testing.
 	 */
     public static void main(String[] args){ 
 		SpaceTest st = new SpaceTest();
     }
     
     /*
      * A kickoff function to begin the system.
      */
 	public void initialize() throws RemoteException, UnusableEntryException, TimeoutException, InterruptedException { 
 		// Jini stuff
 		System.setSecurityManager(new RMISecurityManager());
 		LookupLocator ll = null; 
 		try { 
			ll = new LookupLocator("jini://192.168.0.3:4160"); 
 		} catch (MalformedURLException e) { 
 			e.printStackTrace(); 
 		} 
 		ServiceRegistrar sr = null; 
 		try { 
 			sr = ll.getRegistrar(); 
 		} catch (Exception e) { 
 			e.printStackTrace(); 
 		} 
 		System.out.println("Service Registrar: "+sr.getServiceID()); 
 		ServiceTemplate template = new ServiceTemplate(null, new Class[] { JavaSpace.class }, null); 
 		ServiceMatches sms = null; 
 		try { 
 			sms = sr.lookup(template, 10); 
 		} catch (RemoteException e) { 
 			e.printStackTrace(); 
 		} 
 		if(0 < sms.items.length) { 
 			space = (JavaSpace) sms.items[0].service; 
 			System.out.println("Java Space found.");  
 			
 			// Starting the normative system:
 			oopl = new DistributedOOPL(); // Create interpreter object
 			GUI g = new GUI(oopl,"SpaceOrg.2opl","OOPL",null,6677); // Make a GUI for the interpreter
 			converter = new APAPLTermConverter(oopl.prolog); // Make a term converter (relies on Prolog engine for string storage)
 			INT_TUPLE =makeStringKnown("tuple"); // Because strings get integers, you need to add them to the engine, so you can precompute constructs.
 			INT_POINT =makeStringKnown("point");
 			INT_NULL =makeStringKnown("null"); 
 			makeStringKnown("notifyAgent"); 
 			makeStringKnown("clock"); 
 			makeStringKnown("obligation"); 
 			makeStringKnown("prohibition"); 
 			makeStringKnown("read"); 
 			makeStringKnown("readIfExists"); 
 			makeStringKnown("snapshot"); 
 			makeStringKnown("take"); 
 			makeStringKnown("takeIfExists"); 
 			makeStringKnown("write"); 
 			registerActions(oopl.prolog); // Register the possible actions on this ExternalTool (such as @external(space,theAction(arg1,arg2),Result).)
 			// Precompute some data: ('true.', 'null.', 'tuple_space_changed.')
 			ar_true = oopl.prolog.mp.parseFact("true.", oopl.prolog.strStorage, false); 
 			ar_null = oopl.prolog.mp.parseFact("null.", oopl.prolog.strStorage, false);
 			ar_state_change = oopl.prolog.mp.parseFact("tuple_space_changed.", oopl.prolog.strStorage, false);
 			// To create a IntProlog structure out of a string use the above lines (but replace the fact string such as "true.")
 			// Starting the clock 
 			Thread t = new Thread(new ClockTicker(this));
 			t.start(); 
 		} else { 
 			System.out.println("No Java Space found."); 
 		}
 	} 
 
 	/*
 	 * Both used for increasing or just reading the clock. 
 	 */
 	public synchronized int updateClock(int amount){
 		if(amount>0)  oopl.handleEvent(ar_state_change, false); // clock ticked so deadlines can be passed, handleEvent causes the interpreter to check the norms
 		clock += amount;
 		return clock;
 	}
 	
 	/*
 	 * Constructor immediately initializes the space. 
 	 */
 	public SpaceTest(){
 		super();
 		try { initialize(); } catch (Exception e) { e.printStackTrace(); }
 	}
 	
 	//////////////////////// 2OPL TO JAVASPACE AND 2APL
 	/*
 	 * Make sure String s has an index in the Prolog engine.
 	 */
 	public int makeStringKnown(String s){
 		if(oopl.prolog.strStorage.getInt(s)==null) oopl.prolog.strStorage.add(s);
 		return oopl.prolog.strStorage.getInt(s);
 	}
 	/*
 	 * Make the possible external actions known to the Prolog engine. These will be the actions that
 	 * the organization can do.
 	 */
 	public void registerActions(Prolog p) { 
 		oopl.prolog.builtin.external.registerAction("read", this, ExternalActions.INTAR, ExternalActions.INTAR);
 		oopl.prolog.builtin.external.registerAction("readIfExists", this, ExternalActions.INTAR, ExternalActions.INTAR);
 		oopl.prolog.builtin.external.registerAction("snapshot", this, ExternalActions.INTAR, ExternalActions.INTAR);
 		oopl.prolog.builtin.external.registerAction("take", this, ExternalActions.INTAR, ExternalActions.INTAR);
 		oopl.prolog.builtin.external.registerAction("takeIfExists", this, ExternalActions.INTAR, ExternalActions.INTAR);
 		oopl.prolog.builtin.external.registerAction("write", this, ExternalActions.INTAR, ExternalActions.INTAR);
 		oopl.prolog.builtin.external.registerAction("notifyAgent", this, ExternalActions.INTAR, ExternalActions.INTAR);
 		oopl.prolog.builtin.external.registerAction("clock", this, ExternalActions.INTAR, ExternalActions.INTAR);
 	}
 
 	/*
 	 * Handle a call from the organization (actually: from Prolog). These calls are in IntProlog datatypes (int arrays). 
 	 * ExternalActions ea is a part of the Prolog engine which reads returns ea.intResult after the
 	 * external call.
 	 */
 	public void handleCall(int[] call, ExternalActions ea, int returnType) {  
 		/*
 		 * For JavaSpace calls: the integer array is first transformed to an Entry object, then passed
 		 * to the JavaSpaced using the appropriate method call, and then the result is converted back
 		 * to an integer array.
 		 */
 		if(call[1] == oopl.prolog.strStorage.getInt("read")){
 			try {
 			ea.intResult = entryToArray(space.read(createEntry(call), null, get_number(call,oopl.prolog.harvester.scanElement(call, 3, false, false)+1)));
 			} catch (Exception e) {e.printStackTrace();}
 		} else if(call[1] == oopl.prolog.strStorage.getInt("readIfExists")){
 			try {
 				ea.intResult = entryToArray(space.readIfExists(createEntry(call), null, get_number(call,oopl.prolog.harvester.scanElement(call, 3, false, false)+1)));
 			} catch (Exception e) {e.printStackTrace();}
 		} else if(call[1] == oopl.prolog.strStorage.getInt("snapshot")){
 			ea.intResult = ar_true;
 		} else if(call[1] == oopl.prolog.strStorage.getInt("take")){
 			try {
 				ea.intResult = entryToArray(space.take(createEntry(call), null, get_number(call,oopl.prolog.harvester.scanElement(call, 3, false, false)+1)));
 			} catch (Exception e) {e.printStackTrace();}
 		} else if(call[1] == oopl.prolog.strStorage.getInt("takeIfExists")){
 			try {
 				ea.intResult = entryToArray(space.takeIfExists(createEntry(call), null, get_number(call,oopl.prolog.harvester.scanElement(call, 3, false, false)+1)));
 			} catch (Exception e) {e.printStackTrace();}
 		} else if(call[1] == oopl.prolog.strStorage.getInt("write")){
 			try {
 				long lease = get_number(call,oopl.prolog.harvester.scanElement(call, 3, false, false)+1);
 				if(lease <= 0) lease = Lease.FOREVER;
 				//System.out.println(createEntry(call)+"  "+lease+"   "+Lease.FOREVER);
 				space.write(createEntry(call), null, lease);
 				ea.intResult = ar_true;
 			} catch (Exception e) {e.printStackTrace();}
 	    /*
 	     * The next case throws towards the agent an event that its status is changed.
 	     */
 		} else if(call[1] == oopl.prolog.strStorage.getInt("notifyAgent")){ // notifyAgent(name,obligation(blabla)).
 			String recipient = oopl.prolog.strStorage.getString(call[4]);
 			APLFunction event = (APLFunction)converter.get2APLTerm(Arrays.copyOfRange(call, 6, call.length));
 			//System.out.println("Sending event to "+recipient+": "+event);
 			throwEvent(event, new String[]{recipient});
 			ea.intResult = ar_true;
 		} else if(call[1] == oopl.prolog.strStorage.getInt("clock")){ // Read the clock
 			int[] r = new int[3];
 			addNumber(r, 0, updateClock(0)); // Use updateClock because of synchronization
 			ea.intResult = r;
 		}
 	}
 	public void handleCall(Object[] call, ExternalActions p, int returnType) { }
 	
 	/*
 	 * Create an entry object form an integer array. Perhaps we want to replace this with
 	 * something like createEntry(oopl.prolog.toPrologString(call)).
 	 */
 	public Entry createEntry(int[] call){ // e.g.: read(tuple(name,point(2,4),20),0)
 		int type = call[4];
 		if(type == INT_TUPLE){ // I have to make this more accessable 
 			String name = null;
 			if(call[7]!=INT_NULL) name = oopl.prolog.strStorage.getString(call[7]);
 			Point p = null;
 			if(call[10]!=INT_NULL) p = new Point(get_number(call,13),get_number(call,16)); 
 			Integer i = null;
 			int c = oopl.prolog.harvester.scanElement(call, 9, false, false);
 			if(call[c+1]!=INT_NULL) i = get_number(call,c+1);
 			return new Tuple(name,p,i);
 		}
 		return null;
 	}
 	
 	/*
 	 * Convert an entry to an array. Can also be done by calling the prolog compiler and give
 	 * it e.toPrologString() as an argument.
 	 */
 	public int[] entryToArray(Entry e){
 		if(e == null){
 			int[] r = new int[3];
 			addPredicate(r, 0, oopl.prolog.strStorage.getInt("null"), 0);
 			return r;
 		} else if(e instanceof Tuple){
 			Tuple t = (Tuple)e;
 			int[] r = new int[t.point==null?12:18];
 			addPredicate(r,0,oopl.prolog.strStorage.getInt("tuple"),3); // tuple/3
 			addPredicate(r,3,makeStringKnown(t.str==null?"null":t.str),0); // the name
 			int c = 9;
 			if(t.point == null) addPredicate(r, 6, oopl.prolog.strStorage.getInt("null"), 0);
 			else {
 				addPredicate(r, 6, oopl.prolog.strStorage.getInt("point"), 2);
 				addNumber(r, 9, t.point.x);
 				addNumber(r, 12, t.point.y);
 				c = 15;
 			} 
 			addNumber(r, c,t.i);
 			return r;
 		}
 		return null;
 	}
 	/*
 	 * Gets the int value of a number out of an integer array.
 	 * Note that normally this is a double.
 	 */
 	public int get_number(int[] call, int cursor){
 		long l1 = ((long)call[cursor]<<32)>>>32;
 		long l2 = ((long)call[cursor+1]<<32)>>>32;
 		return (int)Double.longBitsToDouble((l1<<32)|l2);
 	} 
 	/*
 	 * Add predicate integers to an array.
 	 */
 	public void addPredicate(int[] array, int cursor, int name, int arity){
 		array[cursor] = oopl.prolog.harvester.PREDICATE;
 		array[cursor+1] = name;
 		array[cursor+2] = arity;
 	}
 	/*
 	 * Add a number to an array.
 	 */
 	public void addNumber(int[] array, int cursor, int number){
 		array[cursor] = oopl.prolog.harvester.NUMBER;
 		array[cursor+1] = getInt(number,true);
 		array[cursor+2] = getInt(number,false);
 	}
 	/*
 	 * Convert a regular integer to a Prolog store value. Each number is encoded with 
 	 * two integers (64 bit double format), so you can use getInt(i,true) and getInt(i,false) 
 	 * to get both i's number representation parts.
 	 */
 	public int getInt(int i, boolean a){
 		long l = Double.doubleToLongBits(i);
 		if(a) return (int)((l>>>32));
 		else return (int)((l<<32)>>>32);
 	}
 	//////////////////////// 2APL AND JAVASPACE
 
 	/**
 	 * Convert a Prolog predicate to a suitable JavaSpace datatype.
 	 * @param sAgent The agent that calls the method (important for the name in the status).
 	 * @param call The predicate from the call.
 	 * @return The entry representation of the predicate.
 	 */
 	public Entry createEntry(String sAgent, APLFunction call){ 
 		if(call.getName().equals(TYPE_STATUS)){ // Prolog format: status(position(1,4),30) 
 			Point p = null;
 			if(call.getParams().get(0) instanceof APLFunction){ // null is APLIdent  
 				APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
 				int pointX = ((APLNum)point.getParams().get(0)).toInt(); // Get the position
 				int pointY = ((APLNum)point.getParams().get(1)).toInt();
 				p = new Point(pointX,pointY);
 			}
 			Integer health = null; // if health is null (which is ident) it stays also in java null
 			if(call.getParams().get(1) instanceof APLNum) health = ((APLNum)call.getParams().get(1)).toInt(); // The health meter
 			return new Tuple(sAgent,p,health); // Create Tuple
 		} else if(call.getName().equals(TYPE_OBJECT)){ // Prolog format: object(truck,position(30,20))
 			String name = ((APLIdent)call.getParams().get(0)).getName();
 			Point p = null;
 			if(call.getParams().get(1) instanceof APLFunction){ // null is APLIdent so 
 				APLFunction point = (APLFunction) call.getParams().get(1); // Get the point coordinations TODO: type check the arguments
 				int pointX = ((APLNum)point.getParams().get(0)).toInt(); // Get the position
 				int pointY = ((APLNum)point.getParams().get(1)).toInt();
 				p = new Point(pointX,pointY);
 			}
 			return new Tuple(name,p,null); // Create Tuple
 		}  
 		return null;
 	}
 	
 	public Term entryToTerm(Entry entry){ 
 		if(entry instanceof Tuple){ // in case of tuples return tuple(name,position(2,4),48)
 			Tuple tuple = (Tuple) entry;   // cast to tuple
 			String name = tuple.str;
 			if(name==null)name="null"; 
 			Term posTerm = new APLIdent("null");
 			if(tuple.point!=null){
 				posTerm = new APLFunction("position", new Term[]{new APLNum(tuple.point.x),new APLNum(tuple.point.y)}); // get position
 			}
 			Term i = new APLIdent("null");
 			if(tuple.i!=null) i = new APLNum(tuple.i);
 			return new APLFunction("tuple", new Term[]{new APLIdent(name),posTerm,i}); // construct result
 		} else {
 			// TODO: other datatypes
 		}
 		return new APLIdent("null");
 	}
 
 	public Term read(String sAgent, APLFunction call, APLNum timeOut){
 		try{ 
 			return entryToTerm(space.read(createEntry(sAgent,call), null, timeOut.toInt())); 
 		} catch(Exception e){ e.printStackTrace(); return new APLIdent("null"); }
 	}
 	
 	public Term readIfExists(String sAgent, APLFunction call, APLNum timeOut){
 		try{ 
 			return entryToTerm(space.readIfExists(createEntry(sAgent,call), null, timeOut.toInt())); 
 		} catch(Exception e){ e.printStackTrace(); return new APLIdent("null"); }
 	}
 	
 	public Term snapshot(String sAgent, APLFunction call){
 		return new APLIdent("true");
 	}
 	
 	public Term take(String sAgent, APLFunction call, APLNum timeOut){
 		try{ 
 			Term e =  entryToTerm(space.take(createEntry(sAgent,call), null, timeOut.toInt())); 
 			oopl.handleEvent(ar_state_change, false); // check the norms
 			return e;
 		} catch(Exception e){ e.printStackTrace(); return new APLIdent("null"); }
 	}
 	
 	public Term takeIfExists(String sAgent, APLFunction call, APLNum timeOut){
 		try{ 
 			Term e =  entryToTerm(space.takeIfExists(createEntry(sAgent,call), null, timeOut.toInt())); 
 			if(e!=null)oopl.handleEvent(ar_state_change, false); // check the norms
 			return e;
 		} catch(Exception e){ e.printStackTrace(); return new APLIdent("null"); }
 	}
 	
 	public Term write(String sAgent, APLFunction call, APLNum lease){ 
 		try{
 			long leaseVal = lease.toInt();
 			if(leaseVal < 0) leaseVal = Lease.FOREVER; 
 			space.write(createEntry(sAgent,call), null, leaseVal);
 			oopl.handleEvent(ar_state_change, false); // check the norms
 			return new APLIdent("true");
 		}catch (Exception e){ e.printStackTrace(); return new APLIdent("null"); }
 	}
 	
 	public Term clock(String sAgent){
 		return new APLNum(updateClock(0));
 	}
 	
 	/*
 	 * ENVIRONMENT OVERRIDES
 	 */
 	public synchronized void addAgent(String sAgent) {
 		try {
 			Tuple data = (Tuple)space.readIfExists(new Tuple(sAgent,null,null), null, Lease.FOREVER);
 			if(data == null) // agent doesn't exist
 				space.write(new Tuple(sAgent,new Point(1,1),100), null, Lease.FOREVER); // so make it
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public synchronized void removeAgent(String sAgent){
 		try {
 			space.takeIfExists(new Tuple(sAgent,null,null), null, Lease.FOREVER); // remove the agent
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
