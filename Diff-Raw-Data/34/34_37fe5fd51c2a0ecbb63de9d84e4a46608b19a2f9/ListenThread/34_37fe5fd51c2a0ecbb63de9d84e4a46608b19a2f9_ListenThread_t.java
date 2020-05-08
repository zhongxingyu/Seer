 package fh.itb.avalanchewarning;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import android.os.StrictMode;
 
 public class ListenThread {
 
 	private TempData data;
 	private Socket socket;
 
 	public ListenThread(TempData a) {
 		data = a;
 		try {
 			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
 			.permitAll().build();
 			StrictMode.setThreadPolicy(policy);
 			this.socket = new Socket("10.0.2.2", 4711);
 
 		} catch (UnknownHostException e) {
 			System.out.println("UNKNOWN HOST");
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void getForecast(){
 		String forecast = "";
 		PrintWriter out;
 		
 		try {
 			out = new PrintWriter(socket.getOutputStream(), true);
			out.println("Info");
 			out.flush();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					socket.getInputStream()));
 			
 			try{
 				forecast = reader.readLine();
 				if (!forecast.equals("")){
 					data.setData(forecast);
 					forecast = "";
 				}
 			} catch (IOException e){
 				e.printStackTrace();
 			}
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	}
 
 	public boolean sendPhoto(File photo){
 		
 		Long length = photo.getTotalSpace();
 		PrintWriter out;
 		try {
 			out = new PrintWriter(socket.getOutputStream(), true);
 			out.println("Bild");
 			out.flush();
 
 			//File myFile = new File(photo.getAbsolutePath());
 			byte[] mybytearray = new byte[(int) photo.length()];
 			FileInputStream fis = new FileInputStream(photo);
 			BufferedInputStream bis = new BufferedInputStream(fis);
 			bis.read(mybytearray, 0, mybytearray.length);
 			OutputStream os = socket.getOutputStream();
 			System.out.println("Sending...");
 			//wenns nicht geht ndere write in println!
 			os.write(mybytearray, 0, mybytearray.length);
 			os.flush();
 			os.close();
 			return true;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public boolean sendGPS(String GPSData){
 		PrintWriter out;
 		
 		try {
 			//write ->println
 			out = new PrintWriter(socket.getOutputStream(), true);
 			out.println("GPS");
 			out.flush();
 			out.println(GPSData);
 			out.flush();
 			out.close();
 			return true;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 }
