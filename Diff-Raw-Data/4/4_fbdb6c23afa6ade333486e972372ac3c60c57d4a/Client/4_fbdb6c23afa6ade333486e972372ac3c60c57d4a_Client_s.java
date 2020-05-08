 import maze_game.*;
 import java.lang.String;
 
 import java.util.Scanner;
 
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 
 
 public class Client implements ClientRemote{
 
     static boolean finished = false;
     static ServerRemote server;
     static int id;
     static int size;
 
     public void start(Status status){
         size = status.size;
 
         System.out.println("Game start!");
 
         printStatus(status);
 
         this.notify();
     }
 
     public void stop(Status status){
         finished = true;
 
         System.out.println("Game Finished.\nFinal Status: \n\n");
 
         printStatus(status);
     }
 
     static void printStatus(Status status){
         int n = status.size;
 
         for (int i=0; i < n*n; i++){
 
             boolean hasPlayer = false;
             for (int j=0; j<status.numPlayers; j++){
                 Player p = status.players[j];
                 if (p.position == i){
                     hasPlayer = true;
                     System.out.print(String.format("P%-3s", p.id));
                 }
             }
             if (!hasPlayer){
                 if (status.board[i] > 0)
                     System.out.print(String.format("%4d", status.board[i]));
                 else
                     System.out.print("    ");
             }
 
             if (i % n == 0) System.out.print("\n");
         }
     }
 
     public static void main(String[] args) {
         System.out.println("Maze Game.");
 
         String host = (args.length < 1) ? null : args[0];
 
         try{
             System.out.println("Connecting to server: " + host);
             Registry registry = LocateRegistry.getRegistry(host);
             server = (ServerRemote)registry.lookup("server");
 
             System.out.println("Joining game.");
             Client clientObj = new Client();
             id = server.join(clientObj);
 
             if (id < 0){
                 System.out.println("Cannot join game.");
                 return;
             } else{
                 System.out.println("I am P" + id + ". Waiting for other players...");
             }
 
            clientObj.wait();
 
             while (!finished){
                 System.out.println("Input command: (w=up/s=down/a=left/d=right/x=no move/q=quit)");
 
                 //Scanner scan = new Scanner (System.in);
                 char x = (char)System.in.read();//scan.nextChar();
                 int delta;
                 Status s;
 
                 switch (x){
                     case 'w':
                         delta = -size;
                     case 's':
                         delta = size;
                     case 'a':
                         delta = -1;
                     case 'd':
                         delta = 1;
                     case 'x':
                         delta = 0;
                         s= server.move(id, delta);
                         printStatus(s);
                         break;
                     case 'q':
                         server.quit(id);
                         System.out.println("Quited game.");
                         return;
                     default:break;
                 }
             }
 
         } catch (Exception e){
             System.err.println("Client exception: " + e.toString());
             e.printStackTrace();
         }
     }
 }
