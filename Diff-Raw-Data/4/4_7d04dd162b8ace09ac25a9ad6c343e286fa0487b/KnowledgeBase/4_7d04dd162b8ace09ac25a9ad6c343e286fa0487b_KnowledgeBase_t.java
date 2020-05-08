 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 public class KnowledgeBase
 { 
 	//Borderline Isolated Systolic Hypertension
 	int BISH = 140;
 	//Isolated Systolic Hypertension
 	int ISH = 160;
 	//Severe Isolated Systolic Hypertension
 	int SISH = 200;
 	//Diastolic High Normal
 	int DHN = 85;
 	//Diastolic Mild Hypertension
 	int DMH = 90;
 	//Diastolic Moderate Hypertension
 	int DModH = 105;
 	//Diastolic Severe Hypertension
 	int DSH = 115;
 
 	//Low Systolic
 	int LS = 90;
 	//Low Low Systolic
 	int LLS = 60;
 	//High Low Systolic
 	int HLS = 50;
 	//Low Diastolic
 	int LD = 60;
 	//Low Low Diastolic
 	int LLD = 40;
 	//High Low Diastolic
 	int HLD = 33;
 	static int debug = 0;
 	
 	public static void main(String []args) throws Exception
 	{
 		if(args.length > 0)
 			for(String var : args)
 				if(var.equals("-debug"))
 					debug = 1;
 				
 		new KnowledgeBase();
 	}
 	
 	public KnowledgeBase()
 	{
 		String ip = "127.0.0.1";
 		int port = 7999;
 		Socket server;
 		PrintWriter out;
 		BufferedReader in;
 		String message;
 
 		Scanner S;
 		try
 		{
 			S = new Scanner(new FileInputStream("knowledgebase.txt"));
 			while(S.hasNextLine())
 			{
 			String temp = S.nextLine();
 				if(temp.indexOf("//") == -1)
 				{
 					String[] tempArr = temp.split(":");
 					if(tempArr[0] == "BISH")
 						BISH = Integer.parseInt(tempArr[2]);
 					if(tempArr[0] == "ISH")
 						ISH = Integer.parseInt(tempArr[2]);
 					if(tempArr[0] == "SISH")
 						SISH = Integer.parseInt(tempArr[2]);
 					if(tempArr[0] == "DHN")
 						DHN = Integer.parseInt(tempArr[2]);
 					if(tempArr[0] == "DMH")
 						DMH = Integer.parseInt(tempArr[2]);
 					if(tempArr[0] == "DModH")
 						DModH = Integer.parseInt(tempArr[2]);
 					if(tempArr[0] == "DSH")
 						DSH = Integer.parseInt(tempArr[2]);
 					if(tempArr[0] == "LS")
 						LS = Integer.parseInt(tempArr[2]);
 					if(tempArr[0] == "LLS")
 						LLS = Integer.parseInt(tempArr[2]);
 					if(tempArr[0] == "HLS")
 						HLS = Integer.parseInt(tempArr[2]);
 					if(tempArr[0] == "LD")
 						LD = Integer.parseInt(tempArr[2]);
 					if(tempArr[0] == "LLD")
 						LLD = Integer.parseInt(tempArr[2]);
 					if(tempArr[0] == "HLD")
 						HLD = Integer.parseInt(tempArr[2]);
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			System.exit(1);
 		}
 		
 		try
 		{
 			server = new Socket(ip, port);
 			
 			out = new PrintWriter(server.getOutputStream());
 			in = new BufferedReader(new InputStreamReader(server.getInputStream()));
 		
 			if(debug == 1)
 				System.out.println("Initializing...");
 			
 			String[][] outMessage = Parser.parseMessage(Parser.readMessage(23));
 			outMessage = Parser.setVal(outMessage, "Name", "BloodPressureMonitor_KnowledgeBase");
 			out.println(Parser.reparse(outMessage,"$$$"));
 			out.flush();
 			
 			while(true)
 			{
				if(debug == 1)
					System.out.println("Waiting...");
					
 				message = in.readLine();
 				
 				if(debug == 1)
 					System.out.println("Message Recieved:\n"+message);
 				
 				String[][] parsed = Parser.parseMessage(message, "[$][$][$]");
 				int msgid = Parser.getMessageID(parsed);
 				
 				out.println(getMessage(msgid, parsed));
 				out.flush();
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	private String getMessage(int msgID, String[][] message)
 	{
 		String[][] outMessage = new String[0][0];
 		int systolic = 0;
 		int diastolic = 0;
 		
 		if(msgID == 133 || msgID == 134)
 		{
 			outMessage = Parser.parseMessage(Parser.readMessage(132));
 			systolic = Integer.parseInt(Parser.getVal(message, "Systolic"));
 			diastolic = Integer.parseInt(Parser.getVal(message, "Diastolic"));
 		}
 		else
 		{
 			outMessage = Parser.parseMessage(Parser.readMessage(26));
 			outMessage = Parser.setVal(outMessage, "AckMsgID", Integer.toString(msgID));
 		}
 		
 		for(int i = 0; i < message[0].length; i++)
 			if(!message[0][i].equals("MsgID") && !message[0][i].equals("Description"))
 				Parser.setVal(outMessage, message[0][i], message[1][i]);
 		
 		if(systolic != 0)
 		{
 			String[] diagnosis = bloodPressureDiagnosis(systolic, diastolic).split(" : ");
 			outMessage = Parser.setVal(outMessage, "Diagnosis",  diagnosis[0]);
 			outMessage = Parser.setVal(outMessage, "Recommended Course of Action",  diagnosis[1]);
 		}
 		
 		String out = Parser.reparse(outMessage, "$$$");
 		if(debug == 1)
 			System.out.println("Message Sent:\n"+out);
 			
 		return out;
 	}
 	
 	public String bloodPressureDiagnosis(int Systolic, int Diastolic)
 	{
 		int conditionD = 0;
 		if(Diastolic >= DSH)
 		{
 			conditionD = 4;
 		}
 		else if(Diastolic >= DModH)
 		{
 			conditionD = 3;
 		}
 		else if(Diastolic >= DMH)
 		{
 			conditionD = 2;
 		}
 		else if(Diastolic >= DHN)
 		{
 			conditionD = 1;
 		}
 
 		int conditionS = 0;
 		if(Systolic >= SISH)
 		{
 			conditionS = 4;
 		}
 		else if(Systolic >= ISH)
 		{
 			conditionS = 3;
 		}
 		else if(Systolic >= BISH)
 		{
 			conditionS = 2;
 		}
 		
 		String ret = "Normal Blood Pressure : Recheck In 2 Years.";
 		if(conditionD != 0 || conditionS != 0)
 		{
 			if(conditionD < 2 && conditionS > 1)
 			{
 				if(conditionS == 4)
 				{
 					ret = "Isolated Systolic Hypertension : Medicated Therapy.";
 				}
 				else if(conditionS == 3)
 				{
 					ret = "Isolated Systolic Hypertension : Confirm Within 2 Months. Therapy If Confirmed.";
 				}
 				else
 				{
 					ret = "Borderline Isolated Systolic Hypertension : Confirm Within 2 Months.";
 				}
 			}
 			else
 			{
 				if(conditionS == 4)
 				{
 					ret = "Severe Hypertension : Medicated Therapy.";
 				}
 				else if(conditionS == 3)
 				{
 					ret = "Moderate Hypertension : Therapy.";
 				}
 				else if(conditionS == 2)
 				{
 					ret = "Mild Hypertension : Confirm within 2 months.";
 				}
 				else
 				{
 					ret = "High Normal : Recheck Within 1 Year.";
 				}
 				
 			}
 		}
 		else if(Diastolic <= HLD || Systolic <= HLS)
 		{
 			ret = "High Hypotension : Medicated Therapy.";
 		}
 		else if(Diastolic <= LLD || Systolic <= LLS)
 		{
 			ret = "Low Hypotension : Therapy.";
 		}
 		else if(Diastolic <= LD || Systolic <= LS)
 		{
 			ret = "Hypotension : Confirm Within 2 Months.";
 		}
 		
 		return ret;
 	}
 }
 
 	
