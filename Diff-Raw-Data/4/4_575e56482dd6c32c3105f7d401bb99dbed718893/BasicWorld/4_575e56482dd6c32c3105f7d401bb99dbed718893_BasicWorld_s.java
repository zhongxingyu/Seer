 package balle.world;
 
 import balle.misc.Globals;
 
 public class BasicWorld extends AbstractWorld {
 
     private Snapshot prev        = null;
     private double   pitchWidth  = -1;
     private double   pitchHeight = -1;
 
     public BasicWorld(boolean balleIsBlue) {
         super(balleIsBlue);
     }
 
     @Override
     public synchronized Snapshot getSnapshot() {
         return prev;
     }
 
     private Coord subtractOrNull(Coord a, Coord b) {
         if ((a == null) || (b == null))
             return null;
         else
             return a.sub(b);
     }
 
     protected double scaleXToMeters(double x) {
         if (x < 0)
             return x;
 
         return (x / pitchWidth) * Globals.PITCH_WIDTH;
     }
 
     protected double scaleYToMeters(double y) {
         if (y < 0)
             return y;
 
         return (y / pitchHeight) * Globals.PITCH_HEIGHT;
     }
 
     /**
      * NOTE: DO ROBOTS ALWAYS MOVE FORWARD !? NO, treat angle of velocity
      * different from angle the robot is facing.
      * 
      */
     @Override
     public void update(double yPosX, double yPosY, double yRad, double bPosX,
             double bPosY, double bRad, double ballPosX, double ballPosY,
             long timestamp) {
 
         if ((pitchWidth < 0) || (pitchHeight < 0)) {
             System.err
                     .println("Cannot update locations as pitch size is not set properly. Restart vision");
             return;
         }
         // Scale the coordinates from vision to meters:
         yPosX = scaleXToMeters(yPosX);
         yPosY = scaleXToMeters(yPosY);
 
         bPosX = scaleXToMeters(bPosX);
         bPosY = scaleXToMeters(bPosY);
 
         ballPosX = scaleXToMeters(ballPosX);
         ballPosY = scaleXToMeters(ballPosY);
 
         Robot ours = null;
         Robot them = null;
         FieldObject ball = null;
 
         // Coordinates
         Coord ourPosition, theirsPosition;
         Orientation ourOrientation;
         // Orientations
         Orientation theirsOrientation;
 
         // Adjust based on our color.
         if (isBlue()) {
             if ((bPosX - UNKNOWN_VALUE < 0.00001)
                     || (bPosY - UNKNOWN_VALUE < 0.00001))
                 ourPosition = null;
             else
                 ourPosition = new Coord(bPosX, bPosY);
 
             ourOrientation = (bRad != UNKNOWN_VALUE) ? new Orientation(bRad,
                     false) : null;
 
             if ((yPosX - UNKNOWN_VALUE < 0.00001)
                     || (yPosY - UNKNOWN_VALUE < 0.00001))
                 theirsPosition = null;
             else
                 theirsPosition = new Coord(yPosX, yPosY);
 
             theirsOrientation = (yRad != UNKNOWN_VALUE) ? new Orientation(yRad,
                     false) : null;
         } else {
             if ((yPosX - UNKNOWN_VALUE < 0.00001)
                     || (yPosY - UNKNOWN_VALUE < 0.00001))
                 ourPosition = null;
             else
                 ourPosition = new Coord(yPosX, yPosY);
 
             ourOrientation = (yRad != UNKNOWN_VALUE) ? new Orientation(yRad,
                     false) : null;
 
             if ((bPosX - UNKNOWN_VALUE < 0.00001)
                     || (bPosY - UNKNOWN_VALUE < 0.00001))
                 theirsPosition = null;
             else
                 theirsPosition = new Coord(bPosX, bPosY);
 
             theirsOrientation = (bRad != UNKNOWN_VALUE) ? new Orientation(bRad,
                     false) : null;
         }
 
         Coord ballPosition;
         // Ball position
        if ((bPosX - UNKNOWN_VALUE < 0.00001)
                || (bPosY - UNKNOWN_VALUE < 0.00001))
             ballPosition = null;
         else
             ballPosition = new Coord(ballPosX, ballPosY);
 
         Snapshot prev = getSnapshot();
 
         // First case when there is no past snapshot (assume velocities are 0)
         if (prev == null) {
             if ((theirsPosition != null) && (theirsOrientation != null))
                 them = new Robot(theirsPosition, new Velocity(0, 0, 1),
                         theirsOrientation);
             if ((ourPosition != null) && (ourOrientation != null))
                 ours = new Robot(ourPosition, new Velocity(0, 0, 1),
                         ourOrientation);
             if (ballPosition != null)
                 ball = new FieldObject(ballPosition, new Velocity(0, 0, 1));
         } else {
             // change in time
             long deltaT = timestamp - prev.getTimestamp();
 
             // Special case when we get two inputs with the same timestamp:
             if (deltaT == 0) {
                 // This will just keep the prev world in the memory, not doing
                 // anything
                 return;
             }
 
             if (ourPosition == null)
                 ourPosition = estimatedPosition(prev.getBalle(), deltaT);
             if (theirsPosition == null)
                 theirsPosition = estimatedPosition(prev.getOpponent(), deltaT);
             if (ballPosition == null)
                 ballPosition = estimatedPosition(prev.getBall(), deltaT);
 
             // Change in position
             Coord oursDPos, themDPos, ballDPos;
             oursDPos = prev.getBalle() != null ? subtractOrNull(ourPosition,
                     prev.getBalle().getPosition()) : null;
             themDPos = prev.getOpponent() != null ? subtractOrNull(
                     theirsPosition, prev.getOpponent().getPosition()) : null;
             ballDPos = prev.getBall() != null ? subtractOrNull(ballPosition,
                     prev.getBall().getPosition()) : null;
 
             // velocities
             Velocity oursVel, themVel, ballVel;
             oursVel = oursDPos != null ? new Velocity(oursDPos, deltaT) : null;
             themVel = themDPos != null ? new Velocity(themDPos, deltaT) : null;
             ballVel = ballDPos != null ? new Velocity(ballDPos, deltaT) : null;
 
             // put it all together (almost)
             them = new Robot(theirsPosition, themVel, theirsOrientation);
             ours = new Robot(ourPosition, oursVel, ourOrientation);
             ball = new FieldObject(ballPosition, ballVel);
         }
 
         synchronized (this) {
             // pack into a snapshot
             this.prev = new Snapshot(them, ours, ball, timestamp);
         }
     }
 
     @Override
     public void updatePitchSize(double width, double height) {
         prev = null;
         pitchWidth = width;
         pitchHeight = height;
     }
 }
