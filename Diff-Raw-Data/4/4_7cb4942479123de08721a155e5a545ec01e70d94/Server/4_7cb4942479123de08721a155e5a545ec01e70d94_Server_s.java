 package iceepotpc.servergw;
 
 import iceepotpc.appication.Context;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 /**
  * @author tsantakismanos 
  * 		the class that models the server entity of the project
  *      and implements the interface between the present client and the
  *      arduino server. The time unit of measurements requested is a month
  * 
  */
 public class Server {
 
 	/**
 	 * @param date
 	 *            : a String representing the month for which the measurements
 	 *            are requested
 	 * @param pot
 	 *            : pot defined by the caller
 	 * @return: an arraylist of measurements (pot-moment-value)
 	 * @throws Exception 
 	 */
 	public static ArrayList<Meauserement> GetMeasurements(Calendar c, int pot) throws Exception {
 
 		Context cntx = Context.getInstance();
 		
 		ArrayList<Meauserement> measurements = new ArrayList<Meauserement>();
 		
 		String excMessage = "";
 		
 		//construct the request in a string
 		String request_str = Integer.toString(c.get(Calendar.MONTH)) + Integer.toString(c.get(Calendar.YEAR)) + ".txt";
 
 		InputStream is = null;
 		OutputStream os = null;
 		Socket s = null;
 		try {
 			
 			//byte[] request = { '2', '2', '0', '1', '3', '.', 't', 'x', 't','\0' };
 			request_str = request_str.concat("\0");
 			byte[] request = request_str.getBytes();
 			
 			int response = 0;
 			String response_str = "";
 			s = new Socket(cntx.getServerHost(), cntx.getServerPort());
 
 			os = s.getOutputStream();
 
 			// send request
 			os.write(request);
 			os.flush();
 
 			is = s.getInputStream();
 
 			// get & print response
 			response = is.read();
 
 			while (response != -1) {
 				if((char)response == '\n')
 				{
 					//a whole line has been read, parse, store and begin a new line
					measurements.add(ParseMeasurementRow(response_str));
 					
 					response_str = "";
 				}
 				else
 					response_str = response_str + (char) response;
 				
 				response = is.read();
 			}
 
 		} catch (UnknownHostException ex) {
 			excMessage = excMessage + "\n" + ex.getMessage(); 
 			throw new Exception(excMessage);
 		} catch (IOException ex) {
 			excMessage = excMessage + "\n" + ex.getMessage(); 
 		} finally {
 			try {
 				is.close();
 				s.close();
 				if(excMessage != "")
 					throw new Exception(excMessage);
 			} catch (Exception ex) {
 				excMessage = excMessage + "\n" + ex.getMessage(); 
 				throw new Exception(excMessage);
 			}
 
 		}
 		return measurements;
 	}
 	
 	/**
 	 * @param cFrom : the month from which the measurements will be fetched
 	 * @param cTo: the month to....
 	 * @param pot: the desired pot
 	 * @return the results in an arraylist form
 	 * @throws Exception 
 	 */
 	public static ArrayList<Meauserement> GetMeasurements(Calendar cFrom, Calendar cTo, int pot, Context cntx) throws Exception{
 		
 		ArrayList<Meauserement> measurements = new ArrayList<Meauserement>();
 		
 		//for all months in range
 		while(cFrom.get(Calendar.MONTH) != cTo.get(Calendar.MONTH)){
 			measurements.addAll(GetMeasurements(cFrom, pot));
 			cFrom.add(Calendar.MONTH, 1);
 		}
 		
 		measurements.addAll(GetMeasurements(cTo, pot));
 		
 		return measurements;
 		
 	}
 	
 	/**
 	 * @param s: a row of the form: seconds|pin|value
 	 * @return a measurement object with the above values after parsing that row
 	 */
 	private static Meauserement ParseMeasurementRow(String s){
 		
 		String[] parts = s.split("\\|");
 		
 		Meauserement m = new Meauserement(Integer.parseInt(parts[1]), Long.parseLong(parts[0])*1000, Double.parseDouble(parts[2]));
 		
 		return m;
 	}
 
 }
