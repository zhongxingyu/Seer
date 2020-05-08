 package jivko.brain.movement;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import static jivko.brain.movement.CommandsCenter.XML_DOM_ATTRIBUTE_MAX;
 import static jivko.brain.movement.CommandsCenter.XML_DOM_ATTRIBUTE_MIN;
 import static jivko.brain.movement.CommandsCenter.XML_DOM_ATTRIBUTE_NAME;
 import static jivko.brain.movement.CommandsCenter.XML_DOM_ATTRIBUTE_PORT;
 import static jivko.brain.movement.CommandsCenter.XML_DOM_ATTRIBUTE_VAL;
 import static jivko.brain.movement.CommandsCenter.XML_DOM_NODE_COMMAND;
 import jivko.util.ComPort;
 import jivko.util.HexUtils;
 import jivko.util.OsUtils;
 import jivko.util.Tree;
 
 
 /**
  *
  * @author Sergii Smehov (smehov.com)
  */
 public class Command extends jivko.util.Tree implements Cloneable {
   
  private static final String DEFAULT_PORT_NAME = "/dev/ttyUSB1";
   private static final int DEFAULT_PORT_SPEED = 9600;
   
   
   private static final String COMMAND_SPEED_PREFIX = "T";
   public static final int DEFAULT_COMMAND_SPEED = 200;
   public static final double DEFAULT_COMMAND_SPEED_KOEF = 1.2;
   
   
   
   private static final int DEFAULT_COMMAND_DURATION = 1500;
   
   private static Random rand = new Random();    
                 
   public Command() {
   }
   
   public Command(String name) {
     this.name = name;
   }
   
   public Command(String name, String value) {
     this(name);
     setValue(value);
   }
 
   @Override
   public Object clone() throws CloneNotSupportedException {
     return super.clone(); //To change body of generated methods, choose Tools | Templates.
   }  
   
   private String command = "";  
   private String name = "";
   private Integer min;
   private Integer max;  
   private Integer value;
   private String port = "";
   private Integer duration = DEFAULT_COMMAND_DURATION;
   private Integer speed = DEFAULT_COMMAND_SPEED;    
   
   private static Map<String, ComPort> openedPorts = new HashMap<>();
 
   public String getName() {
     return name;
   }
 
   public void setName(String newName) {
     if (newName != null && !"".equals(newName))
       this.name = newName;
   }    
 
   public String getCommand() {
     return command;
   }
 
   public void setCommand(String newCommand) {
     if (newCommand != null && !"".equals(newCommand))
       this.command = newCommand;
   }
 
   public Integer getMin() {
     return min;
   }
 
   public void setMin(String min) {
     if (min != null && !"".equals(min)) {
       this.min = Integer.parseInt(min);
     }
   }
 
   public Integer getMax() {
     return max;
   }
 
   public void setMax(String max) {    
     if (max != null && !"".equals(max)) {
       this.max = Integer.parseInt(max);
     }
   }
 
   public Integer getValue() {
     return value;
   }
 
   public void setValue(String value) {
     if (value != null && !"".equals(value)) {
       this.value = Integer.parseInt(value);
     }
   }
 
   public String getPort() {
     return port;
   }
 
   public void setPort(String newPort) {
     if (newPort != null && !"".equals(newPort))
     this.port = newPort;
   }
 
   public Integer getDuration() {
     return duration;
   }
 
   public void setDuration(String duration) {
     if (duration != null && !"".equals(duration)) {
       this.duration = Integer.parseInt(duration);
     }
   }
 
   public Integer getSpeed() {
     return speed;
   }
 
   public void setSpeed(String speed) {    
     if (speed != null && !"".equals(speed)) {
       this.speed = Integer.parseInt(speed);
     }
   }
   
   public void addPort(String portName) throws Exception {
     //this work only for unix
     if (OsUtils.isUnix()) {
       //if port is not opened yet
       if (openedPorts.get(port) == null) {
         ComPort newPort = new ComPort(portName, DEFAULT_PORT_SPEED);
         openedPorts.put(port, newPort);
       }
     }
   }
   
   public boolean isHardcoded() {
     return command.split("#").length > 2;
   }
   
   public boolean isUsbCommand() {
    return port.contains("USB0");
   }
       
   public void compile() throws Exception {
     //System.err.println("before:" + command);
     if (port == null || "".equals(port))
     port = DEFAULT_PORT_NAME;    
     addPort(port);
     
     if (!isHardcoded()) {      
          
       if (command == null || "".equals(command))
         return;
 
       if (!command.contains("xxx")) {
         throw new Exception("Wrong command format -no xxx");  
       }
 
       Integer val;
       if (value != null) {
         val = value;
 
         if (min == null && max == null) {
           if (isUsbCommand()) {
             //String bytes = HexUtils.hexStringToByteArrayString(val.toString());
             char ch = (char)(val.intValue());    
             String s = "" + ch;
             command = command.replaceAll("xxx", s);           
           } else {
             command = command.replaceAll("xxx", val.toString());
           }
         }
         
       } else {
         if (min == null || max == null)
           throw new Exception("Command: " + name + ": if now val preset at least min or max should be!");            
       } 
 
       if (!isUsbCommand()) {
         command += COMMAND_SPEED_PREFIX + speed.toString();
       }
     }
     
     command += "\r\n";    
     
     //System.err.println("after:" + command);
   }
   
   public void print() {
     print("");  
   }
       
   public void print(String identity) {
     System.out.println(identity + XML_DOM_ATTRIBUTE_NAME + ": "+ getName());
     System.out.println(identity + XML_DOM_ATTRIBUTE_MAX + ": "+ getMax());
     System.out.println(identity + XML_DOM_ATTRIBUTE_MIN + ": "+ getMin());
     System.out.println(identity + XML_DOM_ATTRIBUTE_PORT + ": "+ getPort());
     System.out.println(identity + XML_DOM_ATTRIBUTE_VAL + ": "+ getValue());
     System.out.println(identity + XML_DOM_NODE_COMMAND + ": "+ getCommand());
                 
     identity = identity + "  ";        
     List<Tree> chNodes = getNodes();
     for (Tree t : chNodes) {
       ((Command)t).print(identity);
     }  
   }
   
   public void execute() throws  Exception {
     System.out.println("Executing command: " + getName());    
     
     if (command != null && !"".equals(command)) {
       String commandSaved = command;
     
       if (!isUsbCommand()) {
         if (!isHardcoded()) {
           Integer newVal = value;
           if (min != null && max != null) {
               newVal = min + rand.nextInt(max - min); 
           }
           command = command.replaceAll("xxx", newVal.toString());
 
           int newSpeed = CommandSpeedDeterminator.getReccomendSpeed(this, newVal);
           int idx = command.indexOf('T');
           command = command.substring(0, idx+1);
           command += newSpeed;
         }
         command += "\r\n";
       }    
       //print();
             
       //this work only for unix
       if (OsUtils.isUnix()) {
         ComPort comPort = openedPorts.get(port);
         
         if (isUsbCommand()) {
           comPort.writeCharByCharUntilReceived(command, 50, 4);
         } else {
           comPort.write(command);
         }
       }
       
       command = commandSaved;
     }
             
     for (Tree t : getNodes()) {
       ((Command)t).execute();
     }
         
     System.out.println("duration = " + getDuration());
     Thread.sleep(getDuration());
   }          
 }
 
