 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 
 public class LittleMensQuest implements KeyListener {
 	public static final int ticksPerSecond = 60;
 	private Renderer renderer = new Renderer(this);
	private PlayerEntity ownPlayer = new PlayerEntity(0, 0, 0, 99, 99, 99,
 			99, null, null, null, null, null, null);
 //	private PlayerEntity ownPlayer2 = new PlayerEntity(3.5, 0, 3.5, 99, 99, 99,
 //			99, null, null, null, null, null, null);
 	private Ball datBall = new Ball(1.5, 0, 1.5);
 
 	public static void main(String args[]) {
 		new LittleMensQuest();
 	}
 
 	public LittleMensQuest() {
 		long loopBegin;
 		long lastUpdate = System.currentTimeMillis();
 		while (true) {
 			loopBegin = System.currentTimeMillis();
 			update(System.currentTimeMillis() - lastUpdate);
 			lastUpdate = System.currentTimeMillis();
 			renderer.repaint();
 			try {
 				Thread.sleep(Math.max(1 / ticksPerSecond * 1000
 						- (loopBegin - System.currentTimeMillis()), 0));
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void update(long timeElapsed) {
 		ArrayList<Entity> entitys = Entity.getEntitys();
 		for (int i = 0; i < entitys.size(); i++) {
 			entitys.get(i).update(timeElapsed);
 		}
 	}
 
 	@Override
 	public void keyPressed(KeyEvent k) {
 		switch (k.getKeyCode()) {
 		case KeyEvent.VK_A:
 			ownPlayer.setxDirection(PlayerEntity.Direction.BACKWARDS);
 			break;
 		case KeyEvent.VK_D:
 			ownPlayer.setxDirection(PlayerEntity.Direction.FORWARDS);
 			break;
 		case KeyEvent.VK_W:
 			ownPlayer.setzDirection(PlayerEntity.Direction.FORWARDS);
 			break;
 		case KeyEvent.VK_S:
 			ownPlayer.setzDirection(PlayerEntity.Direction.BACKWARDS);
 			break;
 		case KeyEvent.VK_SHIFT:
 			ownPlayer.sprint();
 			break;
 		case KeyEvent.VK_SPACE:
 			ownPlayer.jump();
 			break;
 		}
 
 	}
 
 	@Override
 	public void keyReleased(KeyEvent k) {
 		switch (k.getKeyCode()) {
 		case KeyEvent.VK_A:
 			ownPlayer.setxDirection(PlayerEntity.Direction.STRAIGHT);
 			break;
 		case KeyEvent.VK_D:
 			ownPlayer.setxDirection(PlayerEntity.Direction.STRAIGHT);
 			break;
 		case KeyEvent.VK_W:
 			ownPlayer.setzDirection(PlayerEntity.Direction.STRAIGHT);
 			break;
 		case KeyEvent.VK_S:
 			ownPlayer.setzDirection(PlayerEntity.Direction.STRAIGHT);
 			break;
 		case KeyEvent.VK_SHIFT:
 			ownPlayer.run();
 			break;
 		}
 
 	}
 
 	@Override
 	public void keyTyped(KeyEvent k) {
 	}
 }
