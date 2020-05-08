 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Scanner;
 import java.util.TreeMap;
 
 public class Converter {
 	private Display display;
 	private File inputFile;
 	private File outputFile;
 	
 	private Map<String, MessageType> messageData;
 	private ArrayList<float[]> values;
 	private ArrayList<String> axis;
 	
 	private int convertProgress;
 	private int normalizeProgress;
 	private int printProgress;
 	
 	public Converter() {
 		display = new Display(this);
 		display.setVisible(true);
 		if(!initMessageInfo()) System.exit(0);
 	}
 	
 	public void begin() {
 		this.convertProgress = 0;
 		this.normalizeProgress = 0;
 		this.printProgress = 0;
 		
 		long time = System.currentTimeMillis();
 		this.axis = prepareAxis();
 		
 		if(!prepareFiles()) return;
 		if(!convert()) return;
 		normalize();
 		outputValues();
 		
 		display.showTimeElapsed((System.currentTimeMillis() - time) / 1000.0);
 	}
 	
 	public boolean initMessageInfo() {
 		File configFile = new File(new File("").getAbsolutePath() + "/config.txt");
 		if(configFile == null || !configFile.canRead() || !configFile.exists() || configFile.isDirectory() || !configFile.isFile()) {
 			display.error("Config File Error","The config file cannot be read or found.\n\nPlease place it in the same directory as the program and restart the program.");
 			return false;
 		}
 		
 		Scanner reader;
 		try {
 			reader = new Scanner(configFile);
 		} catch (FileNotFoundException e) {
 			display.error("Config File Error","The config file cannot be read or found.\n\nPlease place it in the same directory as the program and restart the program.");
 			return false;
 		}
 		
 		messageData = new TreeMap<String, MessageType>();
 		
 		while(reader.hasNextLine()) {
 			String temp = reader.nextLine();
 			if(!temp.substring(0, 2).equals("//")) {
 				String[] data = temp.split(",");
 				
 				String msgId = data[0];
 				int len = Integer.parseInt(data[1]);
 				SubMessage[] subMessages = new SubMessage[len/2];
 				for(int i=0;i<len/2;i++) {
					if(data.length != (2 + 5 * (len / 2))) {
 						display.error("Config Parsing Error", "Invalid length. Missing sub-messages or there is missing values from a sub-message.");
 						reader.close();
 						return false;
 					}
 					
					if(data[2 + 5 * i].length() > 7) {
 						display.error("Config Parsing Error", "The length of a sub-message title must be less than or equal to 7 characters.");
 						reader.close();
 						return false;
 					}
 					
					if(data[6 + 5 * i].length() > 4) {
 						display.error("Config Parsing Error", "The length of a sub-message unit must be less than or equal to 4 characters.");
 						reader.close();
 						return false;
 					}
 					
					if(!(data[3 + 5 * i].equals("false") || data[3 + 5 * i].equals("true")) || !(data[4 + 5 * i].equals("false") || data[4 + 5 * i].equals("true"))) {
 						display.error("Config Parsing Error", "Booleans must have a value of 'true' or 'false'");
 						reader.close();
 						return false;
 					}
 					
 					try {
						subMessages[i] = new SubMessage(data[2 + 5 * i], Boolean.parseBoolean(data[3 + 5 * i]), Boolean.parseBoolean(data[4 + 5 * i]), Float.parseFloat(data[5 + 5 * i]), data[6 + 5 * i]);
 					} catch(Exception e) {
 						display.error("Config Parsing Error", "Parsing Error: " + e.getMessage());
 						reader.close();
 						return false;
 					}
 				}
 				messageData.put(msgId, new MessageType(msgId, len, subMessages));
 			}
 		}
 		reader.close();
 		return true;
 	}
 	
 	private ArrayList<String> prepareAxis() {
 		ArrayList<String> axis = new ArrayList<String>();
 		
 		int c = 0;
 		axis.add("Time [s] ");
 		Iterator<Entry<String, MessageType>> it = messageData.entrySet().iterator();
 		while(it.hasNext()) {
 			Entry<String, MessageType> message = it.next();
 			SubMessage[] messages = message.getValue().getSubMessages();
 			for(int i=0;i<messages.length;i++) {
 				if(!(messages[i].getTitle().equals("Rsrvd") || messages[i].getTitle().equals("Unused"))) {
 					c++;
 					message.getValue().getSubMessages()[i].setColumnIndex(c);
 					axis.add(messages[i].getTitle() + " [" + messages[i].getUnits() + "] ");
 				}
 			}
 		}
 		return axis;
 	}
 	
 	private boolean prepareFiles() {
 		if(inputFile == null || !inputFile.canRead() || !inputFile.exists() || inputFile.isDirectory() || !inputFile.isFile()) {
 			display.error("Invalid File Error","Please choose a valid input file.");
 			return false;
 		}
 		
 		if(outputFile == null) {
 			display.error("Invalid File Error", "Please specify an output file.");
 			return false;
 		}
 		return true;
 	}
 	
 	private boolean convert() {		
 		byte[] data = null;
 		try {
 			data = Files.readAllBytes(inputFile.toPath());
 		} catch (IOException e) {
 			display.error("IO Error","Please try again or try a different file.");
 			return false;
 		}
 		
 		this.values = new ArrayList<float[]>();
 			
 		int i=0;
 		while(true) {
 			try {
 				if(i >= data.length) break;
 				
 				int temp = (int)(Math.floor(((i / (float)data.length) * 100.0f)));
 				if(temp > this.convertProgress) setProgress(0,temp);
 				
 				String msgId = hex(data[i+1]) + hex(data[i]);
 				if(messageData.containsKey(msgId)) {
 					int len = messageData.get(msgId).getLength();
 					byte[] msgBytes = new byte[len];
 					byte[] timeBytes = new byte[] {data[i + len + 2], data[i + len + 3], data[i + len + 4], data[i + len + 5]};
 					for(int j=0; j<len;j++) {
 						msgBytes[j] = data[j+i+2];
 					}
 					values.add(messageData.get(msgId).translateData(msgBytes, timeBytes, this.axis.size()));						
 					i = i + len + 6;
 				} 
 				else {
 					System.out.println("Error: Message Code Not Found - " + msgId);
 					break;
 				}
 			} catch(ArrayIndexOutOfBoundsException e) {
 				break;
 			}
 		}
 		setProgress(0, 100);
 		return true;
 	}
 	
 	private void normalize() {
 		for(int i=1;i<values.get(0).length;i++) {
 			for(int j=0;j<values.size();j++) {
 				if(values.get(j)[i] != Float.MAX_VALUE) {
 					for(int k=0;k<j;k++) {
 						values.get(k)[i] = values.get(j)[i];
 					}
 					break;
 				}
 			}
 		}			
 		
 		for(int i=1;i<values.get(0).length;i++) {
 			int j=0;
 			float val = values.get(0)[i];
 			while(j < values.size()) {
 				if(values.get(j)[i] == Float.MAX_VALUE) values.get(j)[i] = val;					 
 				else val = values.get(j)[i];
 				j++;
 			}
 			int temp = (int)(Math.floor(((i / (float)values.get(0).length) * 100.0f)));
 			if(temp > this.normalizeProgress) setProgress(1, temp);
 		}
 				
 		for(int i=0;i<values.get(0).length;i++) {
 			for(int j=0;j<values.size();j++) {
 				if(values.get(j)[i] == Float.MAX_VALUE) {
 					values.get(j)[i] = 0.0f;
 				}
 			}
 		}
 		setProgress(1, 100);
 	}
 	
 	public void outputValues() {
 		try {
 			FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
 			BufferedWriter bw = new BufferedWriter(fw, 32768);
 			
 			for(String e: axis) {
 				bw.write(e + " ");
 			}
 			bw.write("\n");
 			
 			for(int i=0;i<values.size();i++) {
 				bw.write(values.get(i)[0] + " ");
 				for(int j=1;j<values.get(i).length;j++) {
 					bw.write(round(values.get(i)[j]) + " ");
 				}
 				
 				int temp = (int)(Math.floor(((i / (float)values.size()) * 100.0f)));
 				if(temp > this.printProgress) setProgress(2, temp);
 				
 				bw.write("\n");
 			}
 			setProgress(2, 100);
 			
 			bw.flush();
 			fw.flush();
 			bw.close();
 			fw.close();
 		} catch (IOException e) {
 			display.error("IO Error","Please try again or try a different file.");
 			return;
 		}
 	}
 	
 	public void setInputFile(File file) {
 		this.inputFile = file;
 	}
 	
 	public void setOutputFile(File file) {
 		this.outputFile = file;
 	}
 	
 	public String round(double num) {
 		final int SIG_FIGS = 6;
 	    if(num == 0) {
 	        return "0.0";
 	    }
 
 	    final double d = Math.ceil(Math.log10(num < 0 ? -num: num));
 	    final int power = SIG_FIGS - (int) d;
 
 	    final double magnitude = Math.pow(10, power);
 	    final long shifted = Math.round(num*magnitude);
 	    return "" + shifted/magnitude;
 	}
 	
 	private String hex(byte num) {
 		return String.format("%02x", num);
 	}
 	
 	public void setProgress(int which, int progress) {
 		display.setProgress(which, progress);
 		switch(which) {
 			case 0:
 				this.convertProgress = progress;
 				return;
 			case 1:
 				this.normalizeProgress = progress;
 				return;
 			case 2:
 				this.printProgress = progress;
 				return;
 		}
 	}
 }
