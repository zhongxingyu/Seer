 package net.sf.springderby;
 
 import java.util.LinkedList;
 
 import org.apache.commons.logging.impl.SimpleLog;
 
 public class TestLog extends SimpleLog {
 	private final LinkedList messages = new LinkedList();
 	
 	public TestLog() {
 		super("TestLog");
 	}
 
 	protected void log(int type, Object message, Throwable t) {
 		messages.add(message);
 	}
 	
 	public Object pop() {
 		return messages.removeFirst();
 	}
 }
