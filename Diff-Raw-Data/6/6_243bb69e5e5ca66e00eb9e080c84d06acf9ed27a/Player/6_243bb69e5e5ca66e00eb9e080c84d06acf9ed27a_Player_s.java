package gunslinger.G4PlayerV1;
 
 import java.util.*;
 
 // Extends gunslinger.sim.Player to start with your player
 public class Player extends gunslinger.sim.Player
 {
     // total versions of the same player
     private static int versions = 0;
     // my version no
     private int version = versions++;
     
     private static String PLAYER_NAME="G4PlayerV1";
     
     final int seed = 123456;
     private Random gen;
 
     // A simple fixed shoot rate
     private static double ShootRate = 0.8;
 
     // probability(weight) to shoot each target
     // can be negative
     private double ProbNoShoot;
     private double ProbNutral;
     final double ProbFriend = 0;
     final double ProbEnemy = 1;
     final double ProbShotAtMe = 1.5;
     final double ProbShotAtFriend = 0.5;
 
     // bookkeeping for some numbers
     private int nplayers;
     private int enemy;
     private int friend;
     // use ArrayList because we don't care about performace for
     // <nplayer sized container
     private ArrayList<Integer> friends = new ArrayList<Integer>();
     private ArrayList<Integer> enemies = new ArrayList<Integer>();
 
     // highest possible score
     private int potential;
     // current score
     private int current;
 
     // matrix mapping shots by x at y
     private int[][] shotsFired;
     private int[] shotAt;
     private int round;
 
     // name of the team
     public String name()
     {
         return PLAYER_NAME + (versions > 1 ? " v" + version : "");
     }
  
     // Initialize the player
     public void init(int nplayers, int[] friends, int enemies[])
     {
         // Note:
         //  Seed your random generator carefully
         //  if you want to repeat the same random number sequence
         //  pick your favourate seed number as the seed
         //  Or you can simply use the clock time as your seed     
         //
         // we use same seed for consistency
         gen = new Random(seed);
         this.nplayers = nplayers;
         for (int i = 0; i != friends.length; i++)
             this.friends.add(friends[i]);
         for (int i = 0; i != enemies.length; i++)
             this.enemies.add(enemies[i]);
         enemy = enemies.length;
         friend = friends.length;
         potential = enemy+friend+1;
         current = friend+1;
         shotsFired = new int[nplayers][nplayers];
         shotAt = new int[nplayers];
         round = 0;
         //crazy for now
         ProbNoShoot = (enemy / ShootRate);
         ProbNutral = 0;
     }
 
     // Pick a target to shoot
     // Parameters:
     //  prevRound - an array of previous shoots, prevRound[i] is the player that player i shot
     //              -1 if player i did not shoot
     //  alive - an array of player's status, true if the player is still alive in this round
     // Return:
     //  int - the player id to shoot, return -1 if do not shoot anyone
     //
     public int shoot(int[] prevRound, boolean[] alive)
     {
         round++;
 
         for (int i = 0; i < nplayers; i++) {
           if (prevRound != null && prevRound[i] >= 0) {
             shotsFired[i][prevRound[i]]++;
             shotAt[prevRound[i]]++;
           }
         }
         ProbNutral = (1-1/round) * ProbEnemy;
         double[] targets = buildTargets(alive);        
         int target = roll(ProbNoShoot/round, targets);
         
         return target;
     }
     
     private double[] buildTargets(boolean[] alive) {
         double[] targets = new double[nplayers];
         for (int i = 0; i != nplayers; ++i) {
             // init, may not needed
             targets[i] = 0;
             // friend
             if (friends.contains(i))
                 targets[i] += ProbFriend;
             // enemy
             else if (enemies.contains(i))
                 targets[i] += ProbEnemy;
             // nutral
             else
                 targets[i] += ProbNutral;
            // player that shot at me
             if (shotsFired[i][id] > 0)
                 targets[i] += ProbShotAtMe;
             // player that shot at my still alive friends
             // more weight if he threaten more than one friends
             for (int j : friends) {
                 if (alive[j] && (shotsFired[i][j] > 0))
                     targets[i] += ProbShotAtFriend;
             }
             // wrap up
             if (targets[i] < 0 || i == id || !alive[i])
                 targets[i] = 0;
         }
         return targets;
     }
 
     private int roll(double ProbNoShoot, double[] targets) {
         double sum = ProbNoShoot;
         assert(sum >= 0);
         for (int i = 0; i != nplayers; ++i) {
             assert(targets[i] >= 0);
             sum += targets[i];
         }
         double roll = gen.nextDouble() * sum;
         for (int i = 0; i != nplayers; ++i) {
             roll -= targets[i];
             if (roll < 0)
                 return i;
         }
         return -1;
     }
}
