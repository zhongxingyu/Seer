 package assignment1;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.auth.PropertiesCredentials;
 
 public class Simulator {
   public static void main(String[] args) {
     try {
       AWSCredentials credentials = new PropertiesCredentials(
           Simulator.class.getResourceAsStream("AwsCredentials.properties"));
       ArrayList<Employee> employees = createEmployees();
       HashMap<String, ArrayList<Integer>> policy = createPolicy();
       InstanceManager instManager = new InstanceManager(employees, policy,
           credentials);
       System.out.println("Day1 morning begins... Creating instances...");
       instManager.initialize();
       System.out.println("Doing some work...");
       int i = 0;
      // 10 mins for testing the elastic provisioning for super user employee2
       // and automatic termination of idle instance of user employee1.
       // This includes manually running commands in the autoscaling-commands
       // file and ssh into instances for both employees to stop the cpu 
       // intensive.
      while (i < 120) {
         System.out.println("Monitoring CPU usage of instances...");
         instManager.examAndTermIdleInsts();
         try {
           Thread.sleep(5000);
         } catch (InterruptedException e) {
           e.printStackTrace();
         }
         ++i;
       }
       System.out.println("Day1 5pm! Go home! Cleaning up after work...");
       instManager.afterWorkCleanup();
       System.out.println("Day2 morning begins... " +
       		"Creating instances from yesterday's snapshot...");
       instManager.createMorningInstances();
     } catch (IOException e) {
       e.printStackTrace();
     }
   }
 
   public static HashMap<String, ArrayList<Integer>> createPolicy() {
     HashMap<String, ArrayList<Integer>> policy =
         new HashMap<String, ArrayList<Integer>>();
     ArrayList<Integer> tcpPorts = new ArrayList<Integer>();
     tcpPorts.add(22);
     tcpPorts.add(80);
     tcpPorts.add(443);
     policy.put("tcp", tcpPorts);
     return policy;
   }
   
   public static ArrayList<Employee> createEmployees() {
     ArrayList<Employee> employees = new ArrayList<Employee>();
     Employee employee1 = new Employee("employee1", "user");
     employee1.setKeyPairName("employee1-keypair");
     employee1.setAmiId("ami-a2f04fcb");
     employee1.setBucketName("employee1-bucket");
     Employee employee2 = new Employee("employee2", "superuser");
     employee2.setKeyPairName("employee2-keypair");
     employee2.setAmiId("ami-a6f04fcf");
     employee2.setBucketName("employee2-bucket");
     employees.add(employee1);
     employees.add(employee2);
     return employees;
   }
 }
