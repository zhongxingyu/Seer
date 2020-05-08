 
 /**
  * This class is the main class for the program. From this class the game starts and all the other objects are initialized.
  * 
  * @author Jurgen Franse <fran0060@hz.nl>
  * @version 0.1
  */
 public class GameController
 {
     private static String username;
     /**
      * Constructor for objects of class GameController
      */
     public GameController()
     {
         //output the start screen
         Terminal.print("******************************************************");
         Terminal.print("*   _______ _____ _   _ _    _                       *");
         Terminal.print("*   |__   __/ ____| \\  | |  | |                      *");
         Terminal.print("*     | | | |  __|  \\| | |  | |                      *");
         Terminal.print("*     | | | | |_ | . ` | |  | |                      *");
         Terminal.print("*     | | | |__| | |\\  | |__| |                      *");
         Terminal.print("*     |_|  \\_____|_| \\_|\\____/                       *");
         Terminal.print("*        This Game is Not Unix version 0.1           *");
         Terminal.print("*****************************************************");
         Terminal.print("");
         Terminal.print("Please enter your username: ");
         username = Terminal.askUsername();
         this.initTheGame();
     }
 
     /**
      * Initialize the game!
      *
      */
     public void initTheGame()
     {
         Terminal.setPrompt(username+"@h4ckst4ti0n> ");
         // !!!! DO NOT SWITCH TO ACTIVATE THIS IN FINAL !!!!
         //Terminal.print("!!! hacking scene skipped, please activate this in final !!!");
         //this.hackCinematic();
         
         Terminal.print("Welcome to TGNU, the game now starts!");
        Terminal.print("You have broken into the fileserver of Area51, the files are mounted in the folder '/mnt/mainframe/'");
         Terminal.print("Your task is to seek out and copy classified data about aliens to your local harddrive");
         Terminal.print("Put the collected data in your home folder (/home/"+username+"/)");
         Terminal.print("Type 'help' for more info about the commands you can use.");
        
         //create the filesystem
         Filesystem.createFilesystem();
         
         //load in the commands that can be used.
         Command.initCommandList();
         
         // this boolean stays false as long as the user doen not type 'exit'
         boolean exit = false;
         while(!exit)
         {
             //looks strange but getInput returns false when exit is recieved and true when tekst is recieved (see getInput documentation)
             exit = Terminal.getInput();
         }
         
     }
     
     /**
      * Play a cinematic where the user sees itself hacking the Area51 mainframe
      *
      */
     public void hackCinematic()
     {
         String prompt = Terminal.getPrompt();
         Terminal.printInline(prompt);
         Terminal.printAsTyped("ssh serv1425.area51.gov");
         Terminal.print("Welcome to the Area51 fileserver");
         this.sleep(1000);
         Terminal.print("Please enter username and password");
         Terminal.printInline("username: ");
         this.sleep(1000);
         Terminal.printAsTyped("root");
         this.sleep(1000);
         Terminal.printInline("password: ");
         this.sleep(4000);
         Terminal.printAsTyped("Alien1");
         this.sleep(3000);
         Terminal.print("Welcome root you now have full system access.");
         this.sleep(2000);
         Terminal.printInline("root@serv1425# ");
         this.sleep(3000);
         Terminal.printAsTyped("exit");
         this.sleep(1000);
         Terminal.printInline(prompt);
         this.sleep(1000);
         Terminal.printAsTyped("sshfs root@serv1425.area51.gov:/ /mainframe");
         Terminal.printInline("password: ");
         this.sleep(800);
         Terminal.printAsTyped("Alien1");
         this.sleep(500);
     } 
     
     
     /**
      * Return the players username
      * 
      * @return the players username
      */    
     public static String getUsername()
     {
         return username;
     }
     
      /**
      * sleep the program
      * used to simulate the feeling of the mainframe/computer processing the input
      * 
      * @param   int the time to sleep in miliseconds
      */    
     public static void sleep(int sleepTime) 
     {
         try {
             Thread.sleep(sleepTime);
         }
         catch(Exception e) {
              System.out.println("Fatal error in the game: "+e);
         }
     }
 
 }
