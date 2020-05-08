 import Button2.NetworkRequest;
 import Button2.Sender;
 import Util.Messages;
 import Util.Popup;
 import Util.Strings;
 import java.util.Calendar;
 import java.util.Date;
 import javax.microedition.lcdui.Display;
 import javax.microedition.midlet.MIDlet;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.w3c.dom.Document;
 
 /*******************************************************************************
  * CLASS: Button2
  *
  * FUNCTIONS: void startApp()
  *            void pauseApp()
  *            void destroyApp(boolean unconditional)
  *            int extractTime()
  *
  * DATE: 2013-01-18
  *
  * REVISIONS: 2013-05-18
  *            Luke Tao
 *            Steve Lo
  *            John Payment
  *
  * DESIGNER: Team Cirno
  *
  * PROGRAMMER: Team Cirno
  *
  * NOTES: This is a Java Mobile App intended to connect to the validation server
  *        with pre-stored credentials. It then reports success or failure and
  *        the next check-in time.
  *******************************************************************************/
 public class Button2 extends MIDlet
 {
 	/**
 	 * Constant string representing the connection being OK.
 	 */
 	private static final String OK = "OK"; 
 
 	/*************************************************************************** 
 	 * FUNCTION: startApp
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * REVISIONS: 2013-05-15
 	 *            Luke Tao
 	 *            Steve Lo
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: void startApp()
 	 * 
 	 * NOTES: Called when the app is launched.
 	 ***************************************************************************/
 	public void startApp()
 	{
 		String response;
 		Popup p;
 		Display disp = Display.getDisplay(this);
 		Date d = new Date();
 		Calendar c = Calendar.getInstance();
 		int period;
 		c.setTime(d);
 		boolean ifOk = false;
 		String time;
 
 		try
 		{
 			String[] str = Strings.getAllMessages();
 
 			p = new Popup(this, str[Messages.SENDING], false, null);
 			disp.setCurrent(p);
 			response = Sender.send(null, NetworkRequest.OK_MESSAGE);
 			ifOk = response.equals((OK));
 			p = new Popup(this, (response.equals(OK)) ? str[Messages.RECEIVED] : response, true, null);
 			str = Strings.getAllMessages();
 			response = Sender.send(null, NetworkRequest.DATA_MESSAGE);
 			period = extractTime();
 			if(ifOk)
 			{
 				int tempMin = c.get(Calendar.MINUTE) + period;
 				int tempHour = c.get(Calendar.HOUR_OF_DAY);
 				while(tempMin > 59)
 				{
 					tempHour++;
 					if(tempMin == 60)
 					{
 						tempMin = 0;
 					} else
 					{
 						tempMin -= 60;
 					}
 				}
 				if(tempHour > 23)
 				{
 					tempHour -= 24;
 				}
 
 				time = "Check-in completed.\nPlease check-in again at " + tempHour + ((tempMin < 10) ? ":0" : ":") + tempMin;
 
 				p = new Popup(this, time, true, null);
 				disp.setCurrent(p);
 			}
 		} catch(Exception e)
 		{
 			//most likely to happen if there is an error in Strings.getAllMessages()
 			Popup pop = new Popup(this, "Please login using the Configurator first.", true, null); 
 			disp.setCurrent(pop);
 		}
 	}
 	
 	/*************************************************************************** 
 	 * FUNCTION: pauseApp
 	 * 
 	 * DESIGNER: N/A
 	 * 
 	 * PROGRAMMER: N/A
 	 * 
 	 * INTERFACE: void pauseApp()
 	 * 
 	 * NOTES: Default function inherited by the parent class.
 	 ***************************************************************************/
 	public void pauseApp()
 	{
 	}
 
 	/*************************************************************************** 
 	 * FUNCTION: destroyApp
 	 * 
 	 * DESIGNER: N/A
 	 * 
 	 * PROGRAMMER: N/A
 	 * 
 	 * INTERFACE: void destroyApp(boolean unconditional)
 	 * 
 	 * NOTES: Default function inherited by the parent class.
 	 ***************************************************************************/
 	public void destroyApp(boolean unconditional)
 	{
 	}
 
 	/*************************************************************************** 
 	 * FUNCTION: extractTime
 	 * 
 	 * DATE: 2013-05-15
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Luke Tao
 	 *             Steve Lo
 	 * 
 	 * INTERFACE: int extractTime()
 	 * 
 	 * RETURNS: The time in minutes before the next check=in should be made
 	 * 
 	 * NOTES: Extracts the time until the next checkup from an XML message
 	 *        received from the server and returns it.
 	 ***************************************************************************/
 	public int extractTime()
 	{
 		String xmlMsg = Sender.getMsg();
 		String open = "&lt;get_next&gt;&lt;command_result&gt;";
 		int length = open.length();
 		int cmdStart = xmlMsg.indexOf(open);
 		int timeStart = cmdStart + length;
 		String subStr = xmlMsg.substring(timeStart);
 		int timeEnd = subStr.indexOf("&");
 		String time = subStr.substring(0, timeEnd);
 
 		return (int) Math.ceil(Double.parseDouble(time) / 60); 
 	}
 }
