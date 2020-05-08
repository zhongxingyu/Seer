 import java.util.concurrent.*;
 import java.net.*;
 import java.io.*;
 
 public class Node {
     private static ConcurrentHashMap<String,String> SongDB;
     private static ConcurrentHashMap<String,String> NodeDB;
     private static ConcurrentHashMap<Integer,P2pRequest> ConsultDB;
 
     private static int node_port;
     private static String node_id;
     private static String music_library_filepath;
     private static String known_nodes_filepath;
     //public static Thread mainThread;
 
     public static void main(String[] args){
 	//mainThread = Thread.currentThread();
 
 	//Parseo de parmetros
 	set_params(args);
 	
 	try{
 	    ServerSocket node_socket = new ServerSocket(node_port);	    
 	    Socket client_socket = null;
 	    
 	    // CloseSockets cs = new CloseSockets(node_socket);
 	    // Runtime.getRuntime().addShutdownHook(cs);
 
 	    //Loop principal del servidor
 	    while(true){
 	    	client_socket = node_socket.accept();
 	    	new ClientRequestThread(client_socket,SongDB,NodeDB,ConsultDB).start();
 	    }
 	}
 	catch(IOException e){}
     }
 
     private static void set_params(String args[]){
 	char op = '\0';
 	int i = 0;
 
 	while( i < args.length ){
 	    op = args[i].charAt(1);
 	    switch(op){
 	    case 'p':
 		node_port = Integer.parseInt(args[i+1]);
 		break;
 	    case 'c':
 		known_nodes_filepath = args[i+1];
 		break;
 	    case 'b':
 		music_library_filepath = args[i+1];
 		break;
 	    case 'i':
 		node_id = args[i+1];
 		break;
 	    default:
 		System.out.println("Opcion incorrecta");
 		System.exit(1);
 		break;
 	    }
 	    i += 2;
 	}
     }
 }
 
 // class CloseSockets extends Thread{
 //     ServerSocket s_sockets[];
 //     Socket c_sockets[];
 //     public CloseSockets(ServerSocket ss[],Socket cs[]){
 // 	s_sockets = ss;
 // 	c_sockets = cs;
 //     }
 
 //     public CloseSockets(ServerSocket ss){
 // 	s_sockets = new ServerSocket[1];
 // 	s_sockets[0] = ss;
 // 	c_sockets = null;
 //     }
 
 //     public void run(){
 // 	//int i = 0;
 // 	Node.mainThread.interrupt();
 // 	System.out.println("tortuga");
 // 	// for (i; i < sockets.length; i++){
 // 	//     try{
 // 	// 	sockets[i].close();
 // 	//     }
 // 	//     catch(IOException){
 // 	// 	System.out.println("Error cerrando socket");
 // 	//     }
 // 	// }
 //     }
 // }
