 import java.lang.reflect.Method;
 import java.util.ArrayList;
 
 /*
  * Simple Java test Framework
  *
  *
  * Created by Michael Lyons <mdl0394@gmail.com>
  */
 
 
 public abstract class Test {
 
 	private ArrayList<String> errorMessages;
 	private int failCount; //How many tests have failed
 
 	private Boolean currentTest; //Whether the current test is passing or not, changed by asserts
 
 	public Test() {
 		errorMessages = new ArrayList<String>();
 		failCount = 0;
 	}
 
 	public void assertEqual(Object object1, Object object2, String errorMessage) {
 		if( object1.equals(object2) ) {
 
 		} else {
 			this.currentTest = false;
 			String new_message = "assertEqual failed\n";
 			new_message += "\t" + object1.toString() + " != " + object2.toString() + "\n";
 			new_message += "\tMessage:" + errorMessage;
 			addMessage(new_message);
 		}
 	}
 
 	public void assertTrue(Object object1, String errorMessage) {
 		if( object1 instanceof Boolean ) {
 			if( (Boolean)object1 ) {
 
 			} else {
 				this.currentTest = false;
 				String new_message = "assertTrue failed\n";
 				new_message += "\t" + object1.toString() + " != true\n";
 				new_message += "\tMessage:" + errorMessage;
 				addMessage(new_message);
 			}
 		} else {
 			this.currentTest = false;
 		}
 	}
 
 	private void addMessage( String message ) {
 		this.errorMessages.add( message );
 	}
 
 	private void printMessages() {
 		for( String message : this.errorMessages ) {
 			System.out.println("-------------------------------------------");
 			System.out.println( message );
 		}
 	}
 
 	public boolean run() {
 		try {
 			Method m[] = this.getClass().getDeclaredMethods();
 			for( Method i : m ) {
 				if( i.getName().startsWith("test") ) {
 					currentTest = true;
 
 					Boolean thrown = false;
 					try {
 						i.invoke(this);
 					} catch( Throwable t ) {
 						thrown = true;
 						System.out.print("E");
 						String new_message = i.getName() + " failed\n";
 						new_message += "\t" + t.toString() + "\n";
 						addMessage(new_message);
 					}
 
 					if( !this.currentTest ) {
 						System.out.print("F");
 						this.failCount++;
 					} else if(!thrown) {
 						System.out.print(".");
 					}
 
 				}
 			}
 			System.out.println("");
 			if( this.failCount > 0 ) {
 				printMessages();
 				System.out.println("");
 				System.out.println("[FAILED COUNT=" + this.failCount + "]");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return true;
 	}
 }
