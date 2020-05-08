 package util;
 
 import java.awt.BorderLayout;
 import java.awt.Rectangle;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 /**
  * This class manages all output. You can print messages to the console here, 
  * and have an easy way to determine where the messages came from and how to turn them
  * off. There is also an option to have a secondary Java Swing window in which the output is printed,
  * instead of the need for a commandline tool.
  *
  * @author Roland van der Linden
  */
 public class Output
 {
 	// **********************************************
 	// Fields
 	// **********************************************
 	
 	/**
 	 *  This determines the current state of the output.
 	 *  If turned off, no output should be shown at all.
 	 */
 	public final static boolean ON = true;
 			
 	/**
 	 * This boolean turns on a secondary window in which the output is also shown,
 	 * so we don't need to use the commandline tool to view it.
 	 * If made false, will not use or make the seperate window.
 	 */
	private final static boolean showInSecondaryWindow = false;
 	/**
 	 * This boolean turns the class.method definition on in all output.
 	 */
 	private final static boolean showStackTraceCaller = true;
 	
 	private static boolean initializedSecondaryWindow = false;
 	private static JTextArea textArea;
 	private static JScrollPane scrollPane;
 	
 	
 	// *******************************
 	// Secondary window init.
 	// *******************************
 	
 	/**
 	 * This initializes the extra debugwindow in which debug output 
 	 * will be printed.
 	 */
 	private static void initializeSecondaryWindow()
 	{
 		//Create a textArea 
 		textArea = new JTextArea();
 		textArea.setEditable(false);
 		
 		//Put the textArea in a scrollpane, so we can fill it 'endlessly'.
 		scrollPane = new JScrollPane(textArea);
 		
 		//Put the scrollPane in a panel
 		JPanel newPanel = new JPanel();
 		newPanel.setLayout(new BorderLayout());
 		newPanel.add(scrollPane);
 		
 		//Put the panel in the frame in which we show
 		//the output.
 		JFrame frame = new JFrame("Secondary Debug Output");
 		frame.add(newPanel);
 		frame.setSize(450, 600);
 		frame.setVisible(true);
 		
 		initializedSecondaryWindow = true;
 	}
 	
 	
 	// **********************************************
 	// Show output
 	// **********************************************
 
 	/**
 	 * This method will show an empty message.
 	 */
 	public static void show()
 	{
 		show(null, "", false);
 	}
 	
 	
 	/**
 	 * This method will show the outputmessage.
 	 * @param message The specific message to show on the screen.
 	 */
 	public static void show(String message)
 	{
 		show(null, message, false);
 	}
 	
 	/**
 	 * This method will combine the pre-string and message, and then show it.
 	 * By default, it will be printed in the console, but if the secondary window is 
 	 * enabled, it will also be printed there.
 	 * @param pre Some pre string to highlight the specific outputmessage.
 	 * @param message The specific message to show on the screen.
 	 */
 	public static void show(String pre, String message)
 	{
 		show(pre, message, false);
 	}
 
 	/**
 	 * This method will combine the pre-string, class+method and message, and then show it.
 	 * By default, it will be printed in the console, but if the secondary window is 
 	 * enabled, it will also be printed there.
 	 * @param pre Some pre string to highlight the specific outputmessage.
 	 * @param message The specific message to show on the screen.
 	 * @param overrule This boolean determines if the ON field is overruled because the output may not be ignored.
 	 */
 	public static void show(String pre, String message, boolean overrule)
 	{
 		if(ON || overrule)
 		{
 			//Assemble output string.
 			String output = "";
 			
 			if(pre != null)
 				output += pre;
 			if(showStackTraceCaller)
 				output += getClassAndMethodFromStackTrace() + ": ";
 			if(message != null)
 				output += message;
 			
 			//Print output string.
 			print(output);
 		}
 	}
 	
 	// **********************************************
 	// Show exception output
 	// **********************************************
 	
 	/**
 	 * This method makes it easy to show an exception on the screen.
 	 * @param e The exception to be shown.
 	 */
 	public static void showException(Exception e)
 	{
 		showException(e, null);
 	}
 	
 	/**
 	 * This method makes it easy to show an exception on the screen, and
 	 * also incorporates an additional message.
 	 * @param e The exception to be shown.
 	 * @param message An extra message to be included in the total message.
 	 */
 	public static void showException(Exception e, String message)
 	{
 		String emessage = getExceptionMessage(e, message); 
 		
 		//We will use the common show method to print the exception.
 		//Note that we overrule the ON-field here, because exceptions
 		//always need to be shown.
 		show(null, emessage, false);
 	}
 	
 	/**
 	 * This method returns the important information from an exception
 	 * in a string.
 	 * @param e The exception to extract the information from.
 	 * @param additionalMessage An additional message provided by the user.
 	 */
 	private static String getExceptionMessage(Exception e, String additionalMessage)
 	{
 		String pre = " ";
 		String nl = "\n";
 		String info = "";
 		
 		info += "=====EXCEPTION=====" + nl;
 		info += pre + "-----CLASS-----" + nl;
 		if(e.getClass() != null) info += pre + e.getClass() + nl; else info += pre + "null" + nl;
 		if(showStackTraceCaller)
 		{
 			info += pre + "----CATCHER----" + nl;
 			info += pre + getClassAndMethodFromStackTrace() + nl;
 		}
 		info += pre + "----MESSAGE----" + nl;
 		if(e.getMessage() != null) info += pre + e.getMessage() + nl; else info += pre + "null" + nl;
 		info += pre + "--OWN MESSAGE--" + nl;
 		if(additionalMessage != null) info += pre + additionalMessage + nl; else info += pre + "null" + nl;
 		info += pre + "-----CAUSE-----" + nl;
 		if(e.getCause() != null) info += pre + e.getCause() + nl; else info += pre + "null" + nl;
 		info += pre + "--STACKTRACE---" + nl;
 		if(e.getStackTrace() != null) info += getExceptionStackTraceInfo(e.getStackTrace()) + nl; else info += pre + "null" + nl;
 		info += "======END======" + nl;
 		
 		return info;
 	}
 	
 	/**
 	 * This returns all the information from the stackTrace.
 	 * @param starray The stacktrace element array to extract the information from.
 	 * @return
 	 */
 	private static String getExceptionStackTraceInfo(StackTraceElement[] starray)
 	{
 		String result = "";
 		
 		String pre = "  ";
 		String nl = "\n";
 		
 		for(int i = 0; i < starray.length; i++)
 		{
 			StackTraceElement ste = starray[i];
 			
 			if(ste != null)
 			{
 				String className, methodName;
 				int lineNumber;
 				
 				if(ste.getClassName() != null) className = ste.getClassName(); else className = "null";
 				if(ste.getMethodName() != null) methodName = ste.getMethodName(); else methodName = "null";
 				lineNumber = ste.getLineNumber();
 				
 				result += pre + i + " -> " + className + "." + methodName + ":" + lineNumber + nl;
 			}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * This method returns the class and method information from which the Output class was called.
 	 * @return
 	 */
 	private static String getClassAndMethodFromStackTrace()
 	{
 		String result = "";
 		StackTraceElement[] STarray = Thread.currentThread().getStackTrace();
 		
 		//The first two elements are the getStackTrace method and this one. We don't want to show that.
 		for(int i = 2; i < STarray.length; i++)
 		{
 			StackTraceElement ste = STarray[i];
 			String steString = ste.toString();
 			
 			//We look for the first entry that has nothing to do with this class.
 			if(!steString.startsWith(Output.class.getName()))
 			{
 				//Then we split on the points in between packages, classes and methods.
 				String[] betweenPoints = steString.split("\\.");
 				int length = betweenPoints.length;
 				//We take the last element (method) and the one before (class).
 				String method = betweenPoints[length - 2];
 				String className = betweenPoints[length - 3];
 				//We remove the method hooks.
 				String[] splittedMethod = method.split("\\(");
 				String methodName = splittedMethod[0];
 				
 				//Append the classname and methodname to the result.
 				result += className + "." + methodName;
 				break;
 			}
 		}
 		
 		return result;
 	}
 	
 	// **********************************************
 	// Print output
 	// **********************************************
 	
 	/**
 	 * This will actually print the output to the screen.
 	 * The output will always be printed in the console, but also in 
 	 * a secondary window if specified.
 	 * @param output The output to be printed to the screen.
 	 */
 	private static void print(String output)
 	{
 		//Print output in the console.
 		System.out.println(output);
 		
 		//Print output in a secondary window if needed.
 		if(showInSecondaryWindow)
 		{
 			try
 			{
 				if(!initializedSecondaryWindow)
 					initializeSecondaryWindow();
 	
 				textArea.append(output + "\n");
 				textArea.scrollRectToVisible(new Rectangle(0, textArea.getHeight() - 2, 1, 1));
 			}
 			catch(Exception ex)
 			{
 				print("EXCEPTION in the Output.print(String output) method, while printing to the secondaryDebugWindow.");
 			}
 		}
 	}
 	
 }
