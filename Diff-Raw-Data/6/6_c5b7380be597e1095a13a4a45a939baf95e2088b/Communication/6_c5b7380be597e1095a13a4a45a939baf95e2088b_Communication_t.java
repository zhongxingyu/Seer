 package bobot;
 
 import battlecode.common.*;
 
 public class Communication
 {
     static RobotController rc;
 
 
     static final int KEY = 0x0A5A5A5A;
    static final int MSK = 0xF0000000;
    static final int IND = 0x50000000;
 
     static final int[] OFF = { 0x2E3D, 0xD3E2, 0x8B5C };
 
     public static void broadcast(int channel, int data)
         throws GameActionException
     {
         if (rc.getTeamPower() < 1) return;
 
         for (int i = OFF.length; --i >= 0;) {
             channel =
                 (OFF[i] * Clock.getRoundNum() + channel)
                 & GameConstants.BROADCAST_MAX_CHANNELS;

             rc.broadcast(channel, (data & ~MSK) | IND);
         }
     }
 
     public static int readBroadcast(int channel)
         throws GameActionException
     {
         if (rc.getTeamPower() < 1) return -1;
 
         for (int i = OFF.length; --i >= 0;) {
             channel =
                 (OFF[i] * Clock.getRoundNum() + channel)
                 & GameConstants.BROADCAST_MAX_CHANNELS;
 
             int val = rc.readBroadcast(channel);
             if ((val & MSK) != IND) continue;
             return val & ~MSK;
         }
 
         return -1;
     }
 
 
 
     private static void trySpam(int channel)
         throws GameActionException
     {
         channel &= GameConstants.BROADCAST_MAX_CHANNELS;
 
         int data = rc.readBroadcast(channel);
         if (data == 0 || (data & MSK) == IND) return;
 
         rc.broadcast(channel, (data ^ KEY) | IND);
         lastSeen[lastSeenIndex++ % lastSeen.length] = channel;
     }
 
 
     static int lastSeen[] = new int[20];
     static int lastSeenIndex;
 
     private static void spamLastSeen()
         throws GameActionException
     {
         for (int i = lastSeen.length; --i >= 0;) {
             if (lastSeen[i] < 0) continue;
             trySpam(lastSeen[i]);
         }
     }
 
 
     private static int spamIndex = 0;
     private static int spamPattern;
 
     public static void spam() throws GameActionException
     {
         if (rc.getTeamPower() < 10) return;
 
         spamLastSeen();
 
         // Leave some room for hats.
         int bcThreshold =
             rc.getType() == RobotType.SOLDIER && Hat.hatless ? 4500 : 7000;
 
         while(Clock.getBytecodeNum() < bcThreshold) {
             for (int i = 10; --i >= 0;) {
                 switch(spamPattern) {
 
                 // Linear scans.
                 case 0: trySpam(spamIndex++); break;
                 case 1: trySpam(--spamIndex); break;
 
                 // Spiral out from the center.
                 case 2:
                     spamIndex = (spamIndex * -1) + 1;
                     trySpam(GameConstants.BROADCAST_MAX_CHANNELS/2 + spamIndex);
                     break;
 
                 // Random scan.
                 case 3: trySpam((int)System.nanoTime()); break;
 
                  // Math.random() can be used as a clever to communicate.
                 case 4:
                     trySpam((int)(GameConstants.BROADCAST_MAX_CHANNELS
                                     * Math.random()));
                     break;
 
                 }
             }
         }
     }
 
 
     public static void setup(RobotController r)
     {
         rc = r;
 
         spamPattern = rc.getRobot().getID() % 5;
         for (int i = lastSeen.length; --i >= 0;)
             lastSeen[i] = -1;
 
     }
 
 }
