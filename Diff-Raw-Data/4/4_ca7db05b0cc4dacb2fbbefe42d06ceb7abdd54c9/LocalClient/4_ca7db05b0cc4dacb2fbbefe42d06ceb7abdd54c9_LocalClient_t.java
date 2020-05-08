 /*
 Copyright (C) 2004 Geoffrey Alan Washburn
     
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.
     
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
     
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 USA.
 */
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.awt.event.KeyListener;
 
 /**
  * An abstract class for {@link Client}s in a {@link Maze} that local to the
  * computer the game is running upon. You may choose to implement some of
  * your code for communicating with other implementations by overriding
  * methods in {@link Client} here to intercept upcalls by {@link GUIClient} and
  * {@link RobotClient} and generate the appropriate network events.
  *
  * @author Geoffrey Washburn &lt;<a href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
  * @version $Id: LocalClient.java 343 2004-01-24 03:43:45Z geoffw $
  */
 
 
 public abstract class LocalClient extends Client implements KeyListener {
     private static final Logger logger = LoggerFactory.getLogger(LocalClient.class);
 
    protected boolean pause = true;
 
     /**
      * Create a {@link Client} local to this machine.
      *
      * @param name The name of this {@link Client}.
      */
     public LocalClient(String name) {
         super(name);
         assert (name != null);
     }
 
     public void registerMaze(Maze maze) {
         super.registerMaze(maze);
     }
 
     protected void pause() {
         pause = true;
     }
 
     protected void resume() {
         pause = false;
     }
 
     protected void quit() {
         Mazewar.quit();
     }
 
     /**
      * Notify connected clients adding me.
      */
     protected void notifyAdd() {
         // Wait until all existing clients have reported their locations
         while (Mazewar.maze.getNumOfClients() < Mazewar.connectedOuts.size()) try {
             Thread.sleep(10);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
 
         // Clear queue
         Mazewar.actionQueue.clear();
 
         MazewarPacket outgoing = new MazewarPacket();
         outgoing.type = MazewarPacket.ADD;
         outgoing.directedPoint = Mazewar.maze.addLocalClient(this);
 
         multicastAction(outgoing);
        resume();
 
         logger.info("Notify addClient: " + getName() +
                 " at location " + outgoing.directedPoint.getX() + " " + outgoing.directedPoint.getY() + " " +
                 outgoing.directedPoint.getDirection() + "\n");
     }
 
     /**
      * Notify connected clients moving the client forward.
      *
      * @return <code>true</code> if move was successful, otherwise <code>false</code>.
      */
     protected boolean notifyForwardAction() {
         assert (maze != null);
 
         if (maze.canMoveForward(this)) {
             MazewarPacket outgoing = new MazewarPacket();
             outgoing.type = MazewarPacket.MOVE_FORWARD;
 
             multicastAction(outgoing);
 
             Point oldPoint = getPoint();
             Direction d = getOrientation();
             DirectedPoint newDp = new DirectedPoint(oldPoint.move(d), d);
             logger.info("Notify moveClient: " + getName() +
                     "\n\tfrom X: " + oldPoint.getX() +
                     "\n\t     Y: " + oldPoint.getY() +
                     "\n\tto   X: " + newDp.getX() +
                     "\n\t     Y: " + newDp.getY() +
                     "\n\t     orientation : " + newDp.getDirection() + "\n");
             return true;
         } else {
             logger.info(getName() + " Cannot move forward!\n");
             return false;
         }
     }
 
     /**
      * Notify connected clients moving the client backward.
      *
      * @return <code>true</code> if move was successful, otherwise <code>false</code>.
      */
     protected boolean notifyBackupAction() {
         assert (maze != null);
 
         if (maze.canMoveBackward(this)) {
             MazewarPacket outgoing = new MazewarPacket();
             outgoing.type = MazewarPacket.MOVE_BACKWARD;
 
             // Multicast the moving backward action
             multicastAction(outgoing);
 
             Point oldPoint = getPoint();
             Direction d = getOrientation();
             DirectedPoint newDp = new DirectedPoint(oldPoint.move(d.invert()), d);
             logger.info("Notify moveClient: " + getName() +
                     "\n\tfrom X: " + oldPoint.getX() +
                     "\n\t     Y: " + oldPoint.getY() +
                     "\n\tto   X: " + newDp.getX() +
                     "\n\t     Y: " + newDp.getY() +
                     "\n\t     orientation : " + newDp.getDirection() + "\n");
             return true;
         } else {
             logger.info(getName() + " Cannot move backward!\n");
             return false;
         }
     }
 
     /**
      * Notify connected clients turning the client left.
      */
     protected void notifyTurnLeftAction() {
         MazewarPacket outgoing = new MazewarPacket();
         outgoing.type = MazewarPacket.TURN_LEFT;
 
         // Multicast the turning left action
         multicastAction(outgoing);
 
         Point oldPoint = getPoint();
         Direction d = getOrientation();
         DirectedPoint newDp = new DirectedPoint(oldPoint, d.turnLeft());
         logger.info("Nofity rotateClient: " + getName() +
                 "\n\tfrom X: " + oldPoint.getX() +
                 "\n\t     Y: " + oldPoint.getY() +
                 "\n\t     orientation: " + d +
                 "\n\tto   X: " + newDp.getX() +
                 "\n\tto   Y: " + newDp.getY() +
                 "\n\t     orientation : " + newDp.getDirection() + "\n");
     }
 
     /**
      * Notify connected clients turning the client right.
      */
     protected void notifyTurnRightAction() {
         MazewarPacket outgoing = new MazewarPacket();
         outgoing.type = MazewarPacket.TURN_RIGHT;
 
         // Multicast the turning right action
         multicastAction(outgoing);
 
         Point oldPoint = getPoint();
         Direction d = getOrientation();
         DirectedPoint newDp = new DirectedPoint(oldPoint, d.turnRight());
         logger.info("Nofity rotateClient: " + getName() +
                 "\n\tfrom X: " + oldPoint.getX() +
                 "\n\t     Y: " + oldPoint.getY() +
                 "\n\t     orientation: " + d +
                 "\n\tto   X: " + newDp.getX() +
                 "\n\tto   Y: " + newDp.getY() +
                 "\n\t     orientation : " + newDp.getDirection() + "\n");
     }
 
     /**
      * Notify server the client fired.
      */
     protected boolean notifyFireAction() {
         assert (maze != null);
 
         if (maze.canFire(this)) {
             MazewarPacket outgoing = new MazewarPacket();
             outgoing.type = MazewarPacket.FIRE;
 
             // Multicast the fire action
             multicastAction(outgoing);
 
             logger.info("Notify client: " + getName() + " fired" +
                     "\n\t@ X: " + getPoint().getX() +
                     "\n\t  Y: " + getPoint().getY() +
                     "\n\t  orientation: " + getOrientation() + "\n");
             return true;
         } else {
             logger.info(getName() + " Cannot fire!\n");
             return false;
         }
     }
 
     /**
      * Notify connected clients a kill.
      */
     protected void notifyKill(String victim, DirectedPoint newDp, boolean isInstant) {
         assert (maze != null);
 
         MazewarPacket outgoing = new MazewarPacket();
         outgoing.type = isInstant ? MazewarPacket.INSTANT_KILL : MazewarPacket.KILL;
         outgoing.victim = victim;
         outgoing.directedPoint = newDp;
 
         // Multicast the kill action
         multicastAction(outgoing);
 
         logger.info("Notify client: " + getName() + " killed " + victim +
                 "\n\t reSpawning at location " + newDp.getX() + " " + newDp.getY() + " " +
                 newDp.getDirection() + "\n");
     }
 
     protected void notifyQuit() {
         assert (maze != null);
 
         MazewarPacket outgoing = new MazewarPacket();
         outgoing.type = MazewarPacket.QUIT;
 
         // Multicast the turning right action
         multicastAction(outgoing);
 
         logger.info("Nofity " + getName() + " quitting\n");
     }
 
     private void multicastAction(MazewarPacket outgoing) {
         // Multicast add action
         Mazewar.multicaster.multicastAction(outgoing);
 
         // Multicast ACK to all clients
         Mazewar.multicaster.multicastACK(outgoing);
     }
 }
