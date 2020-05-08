 package client;
 
 import java.util.Random;
 
 public class ClientConfig {
 
     public final static int minPort = 10011;
     public final static int maxPort = 10111;
     public static String pseudo = "Ornicare-"+new Random().nextInt();
//    public static String pseudo = "caca";
 
 }
