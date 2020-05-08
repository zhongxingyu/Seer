 package main;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 public class ConsoleControl {
 	private BufferedReader in;
 	private InputListener listener;
 	
 	
 	public ConsoleControl(InputStream in, InputListener listener) {
		InputStreamReader converter = new InputStreamReader(System.in);
 		this.in = new BufferedReader(converter);
 		this.listener = listener;
 	}
 
 
 
 	public void listenForInput() throws IOException {
		while(listener.lineRead(in.readLine()) == false);
 	}
 }
