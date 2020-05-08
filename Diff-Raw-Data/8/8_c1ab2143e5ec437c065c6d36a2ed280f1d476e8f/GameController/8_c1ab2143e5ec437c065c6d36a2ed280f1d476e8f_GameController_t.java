 
 /**
  * This class is the main class for the program. From this class the game starts and all the other objects are initialized.
  * 
  * @author Jurgen Franse <fran0060@hz.nl>
  * @version 0.1
  */
 public class GameController
 {
     private Terminal terminal;
     private String username;
     private Directory filesystem;
     
     //private Shell shell;
 
     /**
      * Constructor for objects of class GameController
      */
     public GameController()
     {
         //initialize the instance variables
         terminal = new Terminal(this);
         // shell = new Shell();
         
         //output the start screen
         terminal.print("******************************************************");
         terminal.print("*   _______ _____ _   _ _    _                       *");
         terminal.print("*   |__   __/ ____| \\  | |  | |                      *");
         terminal.print("*     | | | |  __|  \\| | |  | |                      *");
         terminal.print("*     | | | | |_ | . ` | |  | |                      *");
         terminal.print("*     | | | |__| | |\\  | |__| |                      *");
         terminal.print("*     |_|  \\_____|_| \\_|\\____/                       *");
         terminal.print("*        This Game is Not Unix version 0.1           *");
         terminal.print("*****************************************************");
         terminal.print("");
         terminal.print("Please enter your username:");
         username = terminal.getUsername();
         this.initTheGame();
     }
 
     /**
      * Initialize the game!
      *
      */
     public void initTheGame()
     {
         this.createFileSystem();
         terminal.setPrompt(username+"@h4ckst4ti0n> ");
         
         this.hackCinematic();
         terminal.print("Welcome to TGNU, the game now starts!");
         terminal.print("You have broken into the fileserver of Area51, the files are mounted in the folder 'mainframe'");
         terminal.print("Your task is to seek out and copy classified data about aliens to your local harddrive");
         terminal.print("Put the collected data in your home folder (/home/"+username+"/)");
         terminal.print("Type 'help' for more info about the commands you can use.");
         terminal.getInput();
     }
     
     /**
      * Play a cinematic where the user sees itself hacking the Area51 mainframe
      *
      */
     public void hackCinematic()
     {
         String prompt = terminal.getPrompt();
         terminal.printInline(prompt);
         terminal.printAsTyped("ssh serv1425.area51.gov");
         terminal.print("Welcome to the Area51 fileserver");
         this.sleep(1000);
         terminal.print("Please enter username and password");
         terminal.printInline("username: ");
         this.sleep(1000);
         terminal.printAsTyped("root");
         this.sleep(1000);
         terminal.printInline("password: ");
         this.sleep(4000);
         terminal.printAsTyped("Alien1");
         this.sleep(3000);
         terminal.print("Welcome root you now have full system access.");
         this.sleep(2000);
         terminal.printInline("root@serv1425# ");
         this.sleep(3000);
         terminal.printAsTyped("exit");
         this.sleep(1000);
         terminal.printInline(prompt);
         this.sleep(1000);
         terminal.printAsTyped("sshfs root@serv1425.area51.gov:/ /mainframe");
         terminal.printInline("password: ");
         this.sleep(800);
         terminal.printAsTyped("Alien1");
         this.sleep(500);
     } 
     
     
     /**
      * Return the players username
      *
      * @return  the players username 
      */
     public String getUsername()
     {
         return username;
     }
     
     /**
      * sleep the program
      * used to simulate the feeling of the mainframe/computer processing the input
      * 
      * @param   int the time to sleep in miliseconds
      */    
     public void sleep(int sleepTime) 
     {
         try {
             Thread.sleep(sleepTime);
         }
         catch(Exception e) {
              System.out.println("Fatal error in the game: "+e);
         }
     }
     
     /**
      * Creates the filesystem
      */
     public void createFileSystem()
     {
         filesystem = new Directory("/");
         Directory home = new Directory("home", filesystem);
         Directory userdir = new Directory(getUsername(), home);
         Directory mainframe = new Directory("mainframe", filesystem);
        Directory mainframeEtc = new Directory("etc", mainframe);
        Directory mainframeHome = new Directory("home", mainframe);
         
         home.addChild(userdir);
        
         filesystem.addChild(home);
         filesystem.addChild(mainframe);
        
        mainframe.addChild(mainframeEtc);
        mainframe.addChild(mainframeHome);
     }
                
 }
