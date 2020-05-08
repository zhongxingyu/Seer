 package museumassault;
 
 import java.util.Random;
 import museumassault.monitor.Corridor;
 import museumassault.monitor.Logger;
 import museumassault.monitor.Room;
 import museumassault.monitor.SharedSite;
 
 /**
  * MuseumAssault main class.
  *
  * @author Andre Cruz <andremiguelcruz@ua.pt>
  */
 public class MuseumAssault
 {
     /**
      * Program entry point.
      *
      * @param args the command line arguments
      */
     public static void main(String[] args)
     {
     	// Configurable params
         int nrChiefs = 1;
         int nrTeams = 1;
         int nrThievesPerTeam = 5;
         int nrRooms = 4;
         int maxDistanceBetweenThieves = 1;
         int totalThieves = nrTeams * nrThievesPerTeam;
         String logFileName = "log.txt";
 
 
         Random random = new Random();
         Logger logger = new Logger(logFileName);
 
         // Initializing the necessary entities
         Team[] teams = new Team[nrTeams];
         for (int x = 0; x < nrTeams; x++) {
             teams[x] = new Team(x + 1, nrThievesPerTeam);
         }
 
         Room[] rooms = new Room[nrRooms];
         for (int x = 0; x < nrRooms; x++) {
            rooms[x] = new Room(x + 1, 5, new Corridor(random.nextInt(nrRooms - 2) + 1, maxDistanceBetweenThieves, logger), logger);
         }
 
         SharedSite site = new SharedSite(rooms, teams, logger, (nrChiefs > 1));
 
         Thief[] thieves = new Thief[totalThieves];
         for (int x = 0; x < totalThieves; x++) {
            Thief thief = new Thief(x + 1, random.nextInt(totalThieves - 3) + 1, site);
             thieves[x] = thief;
         }
 
         Chief[] chiefs = new Chief[nrChiefs];
         for (int x = 0; x < nrChiefs; x++) {
             Chief chief = new Chief(x + 1, site);
             chiefs[x] = chief;
         }
 
         // Initialize the logger
         logger.initialize(chiefs, thieves, teams);
 
         // Start the threads
         for (int x = 0; x < totalThieves; x++) {
             thieves[x].start();
         }
 
         for (int x = 0; x < nrChiefs; x++) {
             chiefs[x].start();
         }
 
         // Wait for the chiefs to join
         for (int x = 0; x < nrChiefs; x++) {
             try {
                 chiefs[x].join();
             } catch (InterruptedException e) {}
         }
 
         logger.terminateLog(site.getNrCollectedCanvas());
 
         System.out.println("Program terminated successfully, please check the log file.");
         System.exit(0);
     }
 }
