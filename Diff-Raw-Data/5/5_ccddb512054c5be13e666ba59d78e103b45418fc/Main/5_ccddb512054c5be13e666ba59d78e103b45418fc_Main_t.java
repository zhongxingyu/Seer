 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.image.BufferStrategy;
 import java.util.Random;
 
 public class Main extends JPanel implements KeyListener, WindowListener {
   public static final int NUM_STARS = 200;
   private static final double ACCEL = 0.000000001;
   private static final double MAX_SPEED = 0.10;
   private boolean running = true;
   private final JFrame frame;
   double x;
   double y;
   double angle;
   double speedX, speedY;
   private int rotateLeft;
   private int rotateRight;
   private int throttle;
 
   private Polygon[] walls = new Polygon[]{
           new Polygon()
   };
 
   public static void main(String[] args) {
     new Main().run();
   }
 
   private void run() {
     double[] starX = new double[NUM_STARS];
     double[] starY = new double[NUM_STARS];
     Random random = new Random();
     for (int i = 0; i < NUM_STARS; i++) {
       starX[i] = random.nextDouble();
       starY[i] = random.nextDouble();
     }
     long prevTime = System.nanoTime();
     while (running) {
       long t = System.nanoTime();
       long delta = t - prevTime;
       prevTime = t;
 
       BufferStrategy bufferStrategy = frame.getBufferStrategy();
       Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
 
       angle += (rotateLeft + rotateRight) * delta * 0.00000001;
       speedX += Math.sin(angle) * throttle * delta * ACCEL;
       speedY += Math.cos(angle) * throttle * delta * ACCEL;
       double foo = Math.sqrt(speedX * speedX + speedY * speedY);
       if (foo > MAX_SPEED) {
           speedX = speedX * MAX_SPEED / foo;
           speedY = speedY * MAX_SPEED / foo;
       }
       x += speedX;
       y += speedY;
       g.setColor(Color.BLACK);
       g.fillRect(0, 0, 800, 600);
 
       for (int i = 0; i < NUM_STARS; i++) {
         int level = (i / (NUM_STARS / 3));
         int scale = 10 + 2*level;
         int color = 60 + level * 60;
         g.setColor(new Color(color, color, color));
         int x2 = (int) (-x * scale + 800 * scale * starX[i]) % 800;
         int y2 = (int) (y * scale + 600 * scale * starY[i]) % 600;
         x2 = (800 + x2) % 800;
         y2 = (600 + y2) % 600;
         g.fillOval(x2, y2, 1 + level, 1 + level);
       }
 
       g.translate(400, 300);
 
       g.setPaint(new RadialGradientPaint(0, -10, 30, new float[]{0.2f, 0.7f, 0.8f}, new Color[]{Color.BLUE, Color.CYAN, Color.WHITE}));
       g.rotate(angle);
       Polygon ship = new Polygon(new int[]{0, 15, 0, -15, 0}, new int[]{-20, 20, 10, 20, -20}, 5);
       g.fillPolygon(ship);
       if (throttle > 0) {
         int color = (int) ((System.currentTimeMillis() / 50) % 3);
         Color[] throttleColors = new Color[]{Color.RED, Color.ORANGE, Color.YELLOW};
         g.setColor(throttleColors[color]);
         g.drawPolyline(new int[]{-5, 0, 5}, new int[]{15, 25, 15}, 3);
       }
       g.rotate(-angle);
 
 
       g.setColor(Color.LIGHT_GRAY);
       Polygon block = new Polygon(new int[]{-100, 100, 100, -100}, new int[]{-100, -100, -70, -70}, 4);
      g.translate(-x, y);
       g.fillPolygon(block);
      g.translate(x, -y);
 
       g.dispose();
       bufferStrategy.show();
       Toolkit.getDefaultToolkit().sync();
       try {
         Thread.sleep(10);
       } catch (InterruptedException e) {
       }
     }
     frame.dispose();
   }
 
   public Main() throws HeadlessException {
     frame = new JFrame("foo");
     frame.addWindowListener(this);
     frame.setSize(800, 600);
     frame.setVisible(true);
     frame.createBufferStrategy(2);
     frame.add(this);
     frame.addKeyListener(this);
   }
 
   @Override
   public void keyTyped(KeyEvent keyEvent) {
   }
 
   @Override
   public void keyPressed(KeyEvent keyEvent) {
     if (keyEvent.getKeyCode() == KeyEvent.VK_LEFT) {
       rotateLeft = -1;
     }
     if (keyEvent.getKeyCode() == KeyEvent.VK_RIGHT) {
       rotateRight = 1;
     }
     if (keyEvent.getKeyCode() == KeyEvent.VK_UP) {
       throttle = 1;
     }
     if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
       running = false;
     }
   }
 
   @Override
   public void keyReleased(KeyEvent keyEvent) {
     if (keyEvent.getKeyCode() == KeyEvent.VK_LEFT) {
       rotateLeft = 0;
     }
     if (keyEvent.getKeyCode() == KeyEvent.VK_RIGHT) {
       rotateRight = 0;
     }
     if (keyEvent.getKeyCode() == KeyEvent.VK_UP) {
       throttle = 0;
     }
   }
 
   @Override
   public void windowOpened(WindowEvent windowEvent) {
   }
 
   @Override
   public void windowClosing(WindowEvent windowEvent) {
     running = false;
   }
 
   @Override
   public void windowClosed(WindowEvent windowEvent) {
   }
 
   @Override
   public void windowIconified(WindowEvent windowEvent) {
   }
 
   @Override
   public void windowDeiconified(WindowEvent windowEvent) {
   }
 
   @Override
   public void windowActivated(WindowEvent windowEvent) {
   }
 
   @Override
   public void windowDeactivated(WindowEvent windowEvent) {
   }
 }
