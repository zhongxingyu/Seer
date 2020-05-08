 import lejos.nxt.*;
 import lejos.nxt.remote.*;
 import java.io.*;
 import java.net.*;
 import java.util.regex.*;
 import lejos.robotics.navigation.TachoPilot;
 
 public class BTExecutor
 {
 	private boolean isDebugging = true;
	private boolean throwErrorOnRoute = true;
 
     private LightSensor light;
     private TouchSensor touch;
     private UltrasonicSensor ultra;
     private TachoPilot tPilot;
     private RemoteMotor[] rd = new RemoteMotor[3];
     private ServerSocket server;
     private Socket socket;
     private BufferedReader reader;
     private PrintStream writer;
 
     private BTExecutor() throws Throwable
     {
     	System.out.println("--------- Hello! ---------");
         Init();
 
         while(true)
         {
             String cmd = reader.readLine();
             if(cmd == null || cmd.length() <= 1) {
                 System.out.println("Oops");
                 break;
             }
 
             System.out.println("\n" + cmd + "\n");
             System.out.println(cmd.charAt(0));
 
             String answer = cmd.charAt(0) == 'G'
             			? Get(cmd)
             			: Set(cmd);
 
             writer.print(answer + "\n");
             System.out.println(answer.startsWith("OK") ? answer.substring(0, 2) : answer);
             writer.flush();
         }
 
         Exit();
         System.out.println("--------- Bye! ---------");
     }
 
     public static void main(String[] args) {
         try { new BTExecutor(); }
         catch(Throwable ex) { ex.printStackTrace( System.out ); }
     }
 
     private void Init() throws Throwable {
         server = new ServerSocket(20042, 10);
         socket = server.accept();
         reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         writer = new PrintStream(socket.getOutputStream());
 
         if(isDebugging)
         	return;
 
         light = new LightSensor(SensorPort.S1);
         touch = new TouchSensor(SensorPort.S3);
         ultra = new UltrasonicSensor(SensorPort.S4);
 
         tPilot = new TachoPilot(56, 112, Motor.C, Motor.A, false);
 
         rd[0] = Motor.A;
         rd[1] = Motor.C;
         rd[2] = Motor.B;
 
         light.setFloodlight(false);
 
         rd[0].regulateSpeed(true);
         rd[1].regulateSpeed(true);
     }
 
     private String Get(String cmd) {
     	String sensorName = cmd.substring(1);
     	String answer = "";
 
     	if(isDebugging)
     		return "Cannot get value of " + sensorName + " in DEBUG MODE";
 
         if(sensorName.equals("ST"))
             answer = touch.isPressed() ? "1" : "0";
         else if(sensorName.equals("SL"))
             answer += (light.getLightValue() / 100.0);
         else if(sensorName.equals("SD"))
             answer += (ultra.getDistance() / 100.0);
         else if(sensorName.equals("SC"))
             answer += (Battery.getVoltage());
 
         return answer;
     }
 
     private String Set(String cmd) {
     	Pattern p = Pattern.compile("^.(\\w+)=(-?[\\d\\.]+)(.*)");
         Matcher m = p.matcher(cmd);
 
         String answer = "ERROR";
         if(!m.matches())
         	return answer;
 
         String name = m.group(1);
         String svalue = m.group(2);
         String info = m.group(3);
 
         System.out.println("[ " + name + " ] === [ " + svalue + " ]");
         float value = Float.parseFloat(svalue);
 
         if(name.equals("RD"))
         {
         	if(!isDebugging) {
 	            tPilot.travel(value, true);
 	            while(tPilot.isMoving())
 	            {
 	                if(touch.isPressed()) {
 	                    tPilot.stop();
 	                    tPilot.travel(-1 *((value < 50) ? value : 50));
 	                    return answer + info;
 	                }
 	            }
        	} else if(throwErrorOnRoute) {
        		return answer + info;
         	}
             answer = "OK";
         }
         else if(name.equals("RT"))
         {
         	if(!isDebugging)
         		tPilot.rotate(value);
             answer = "OK";
         }
         else if(name.equals("SL"))
         {
         	if(!isDebugging) {
 	            if(svalue.equals("1"))
 	                light.setFloodlight(true);
 	            else
 	                light.setFloodlight(false);
         	}
             answer = "OK";
         }
 
         return answer + info;
     }
 
     private void Exit() {
     	if(isDebugging)
     		return;
 
         rd[0].stop();
         rd[0].flt();
         ultra.off();
         light.setFloodlight(false);
     }
 }
 
 
