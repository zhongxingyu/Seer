 package balle.world;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Graphics;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import balle.misc.Globals;
 import balle.world.processing.AbstractWorldProcessor;
 
 public class SimpleWorldGUI extends AbstractWorldProcessor {
 
     private JFrame frame;
     private JPanel panel;
 
     public SimpleWorldGUI(AbstractWorld world) {
         super(world);
         frame = new JFrame("WorldGUI");
         // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         panel = new Screen();
         frame.getContentPane().add(BorderLayout.CENTER, panel);
         frame.setSize(770, 500);
         frame.setVisible(true);
     }
 
     private class Screen extends JPanel {
 
         private float       scale;
         private final float XSHIFTM     = 0.4f;
         private final float YSHIFTM     = 0.39f;
         private final float VIEWHEIGHTM = 2;
 
         @Override
         public void paintComponent(Graphics g) {
             scale = getHeight() / VIEWHEIGHTM;
             g.setColor(new Color(72, 104, 22));
             g.fillRect(0, 0, getWidth(), getHeight());
             drawField(g);
             drawFieldObjects(g);
         }
 
         // public void
 
         private void drawField(Graphics g) {
             g.setColor(Color.BLACK);
             drawLineTransformMeters(g, 0f, 0f, Globals.PITCH_WIDTH, 0f);
             drawLineTransformMeters(g, 0f, Globals.PITCH_HEIGHT,
                     Globals.PITCH_WIDTH, Globals.PITCH_HEIGHT);
             drawLineTransformMeters(g, 0f, 0f, 0f, Globals.GOAL_POSITION);
             drawLineTransformMeters(g, Globals.PITCH_WIDTH, 0f,
                     Globals.PITCH_WIDTH, Globals.GOAL_POSITION);
             drawLineTransformMeters(g, 0f, Globals.PITCH_HEIGHT, 0f,
                     Globals.PITCH_HEIGHT - Globals.GOAL_POSITION);
             drawLineTransformMeters(g, Globals.PITCH_WIDTH,
                     Globals.PITCH_HEIGHT, Globals.PITCH_WIDTH,
                     Globals.PITCH_HEIGHT - Globals.GOAL_POSITION);
             // Left-hand goal area
             drawLineTransformMeters(g, 0f, Globals.GOAL_POSITION, -0.1f,
                     Globals.GOAL_POSITION);
             drawLineTransformMeters(g, -0.1f, Globals.GOAL_POSITION, -0.1f,
                     Globals.PITCH_HEIGHT - Globals.GOAL_POSITION);
             drawLineTransformMeters(g, Globals.PITCH_WIDTH,
                     Globals.GOAL_POSITION, Globals.PITCH_WIDTH + 0.1f,
                     Globals.GOAL_POSITION);
             // Right-hand goal area
             drawLineTransformMeters(g, Globals.PITCH_WIDTH,
                     Globals.PITCH_HEIGHT - Globals.GOAL_POSITION,
                     Globals.PITCH_WIDTH + 0.1f, Globals.PITCH_HEIGHT
                             - Globals.GOAL_POSITION);
             drawLineTransformMeters(g, 0f, Globals.PITCH_HEIGHT
                     - Globals.GOAL_POSITION, -0.1f, Globals.PITCH_HEIGHT
                     - Globals.GOAL_POSITION);
             drawLineTransformMeters(g, Globals.PITCH_WIDTH + 0.1f,
                     Globals.GOAL_POSITION, Globals.PITCH_WIDTH + 0.1f,
                     Globals.PITCH_HEIGHT - Globals.GOAL_POSITION);
         }
 
         private void drawFieldObjects(Graphics g) {
             Snapshot s = getSnapshot();
             if (s != null) {
                 drawRobot(g, Color.GREEN, s.getBalle());
                 drawRobot(g, Color.RED, s.getOpponent());
                 drawBall(g, Color.RED, s.getBall());
             }
         }
 
         private void drawBall(Graphics g, Color c, FieldObject ball) {
 
             if ((ball == null) || (ball.getPosition() == null)) {
                 return;
             }
             float radius = Globals.BALL_RADIUS;
             Coord pos = ball.getPosition();
 
             if (!pos.isEstimated())
                 g.setColor(c);
             else
                 g.setColor(Color.LIGHT_GRAY);
 
             g.fillOval(m2PX(pos.getX() - radius), m2PY(pos.getY() - radius),
                     (int) (radius * 2 * scale), (int) (radius * 2 * scale));
         }
 
         private void drawRobot(Graphics g, Color c, Robot robot) {
 
             // Fail early, fail often
             if ((robot == null) || (robot.getPosition() == null)) {
                 return;
             }
 
             // position of center of the robot
             float x = (float) robot.getPosition().getX();
             float y = (float) robot.getPosition().getY();
             boolean isEstimated = robot.getPosition().isEstimated();
 
             // half length and width of robot
             float hl = Globals.ROBOT_LENGTH / 2;
             float hw = Globals.ROBOT_WIDTH / 2;
 
             // list of (x,y) positions of the corners of the robot
             // with the center at (0,0)
             float[][] poly = new float[][] { { hl, hw }, { hl, -hw },
                     { -hl, -hw }, { -hl, hw } };
 
             // for each point
             double a = robot.getOrientation().radians();
             for (int i = 0; i < poly.length; i++) {
                 float px = poly[i][0];
                 float py = poly[i][1];
 
                 // a = (90 * Math.PI) / 180;
 
                 // System.out.println(robot.getOrientation().degrees());
                 // rotate by angle of orientation
                poly[i][0] = (float) ((px * Math.cos(a)) + (py * Math.sin(a)));
                poly[i][1] = (float) ((-px * Math.sin(a)) + (py * Math.cos(a)));
 
                 // transform to robot's position
                 poly[i][0] += x;
                 poly[i][1] += y;
             }
 
             // convert to pixel coordinates
             int n = poly.length;
             int[] xs = new int[n];
             int[] ys = new int[n];
             for (int i = 0; i < n; i++) {
                 xs[i] = m2PX(poly[i][0]);
                 ys[i] = m2PY(poly[i][1]);
             }
 
             // draw
             if (!isEstimated)
                 g.setColor(Color.LIGHT_GRAY);
             else
                 g.setColor(Color.DARK_GRAY);
 
             g.fillPolygon(xs, ys, n);
             g.setColor(c);
             g.fillPolygon(new int[] { xs[2], xs[3], m2PX(x) }, new int[] {
                     ys[2], ys[3], m2PY(y) }, 3);
         }
 
         // Convert meters into pixels and draws line
         private void drawLineTransformMeters(Graphics g, float x1, float y1,
                 float x2, float y2) {
 
             g.drawLine(m2PX(x1), m2PY(y1), m2PX(x2), m2PY(y2));
 
         }
 
         private int m2PX(double x) {
             return m2PX((float) x);
         }
 
         private int m2PX(float x) {
             return (int) ((x + XSHIFTM) * scale);
         }
 
         private int m2PY(double y) {
             return m2PY((float) y);
         }
 
         private int m2PY(float y) {
         	y = Globals.PITCH_HEIGHT-y;
             return (int) ((y + YSHIFTM) * scale);
         }
 
     }
 
     @Override
     protected void actionOnStep() {
 
     }
 
     @Override
     protected void actionOnChange() {
         panel.repaint();
     }
 
 }
