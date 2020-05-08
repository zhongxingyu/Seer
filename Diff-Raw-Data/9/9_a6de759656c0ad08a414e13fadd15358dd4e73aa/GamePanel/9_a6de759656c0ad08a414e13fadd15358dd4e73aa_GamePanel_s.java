 package com.hack.balls.swing;
 
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 import com.hack.balls.engine.PhysicsEngine;
 import com.hack.balls.model.Ball;
 
 public class GamePanel extends JPanel implements KeyListener, ActionListener {
 
 	public static final String REPAINT_COMMAND = "REPAINT";
 	public static final String START_COMMAND = "START";
 	public static final int MILLIS_PER_FRAME = 10;
 	
 	private JLabel scoreLabel;
 	private JButton startButton;
 	private PhysicsEngine physicsEngine;
 	private Timer timer;
 
 	public GamePanel(MainFrame mainFrame) {
 
 		setFocusable(true);
 		addKeyListener(this);
 
 		timer = new Timer(MILLIS_PER_FRAME, this);
 		timer.setActionCommand(REPAINT_COMMAND);
 
 		startButton = new JButton("Begin!");
 		startButton.addKeyListener(this);
 		startButton.addActionListener(this);
 		startButton.setActionCommand(START_COMMAND);
 		scoreLabel = new JLabel();
 
 		add(scoreLabel);
 		add(startButton);
 	}
 
 	public void startGame() {
 
 		physicsEngine = new PhysicsEngine(MainFrame.X_SIZE, MainFrame.Y_SIZE);
 		Thread physicsThread = new Thread(physicsEngine);
 		physicsThread.start();
 
 		timer.start();
 
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 
 		g.setColor(getBackground());
 		g.fillRect(0, 0, getWidth(), getHeight());
 
 		g.setColor(getForeground());
 
 		if (physicsEngine != null) {
 			List<Ball> balls = physicsEngine.getBalls();
 			for (int i = 0; i < balls.size(); i++) {
 				Ball ball = balls.get(i);
 				g.fillOval(ball.getX() - ball.getRadius(),
 						ball.getY() - ball.getRadius(), ball.getRadius() * 2,
 						ball.getRadius() * 2);
 
 			}
 			scoreLabel.setText("Score: " + physicsEngine.getScore());
 
 		}
 
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 
 		if (e.getKeyCode() == KeyEvent.VK_UP) {
 			physicsEngine.getPlayer().moveUp();
 		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
 			physicsEngine.getPlayer().moveDown();
 		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
 			physicsEngine.getPlayer().moveLeft();
 		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
 			physicsEngine.getPlayer().moveRight();
 		}
 
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// do nothing
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// do nothing
 	}
 
 	public void actionPerformed(ActionEvent evt) {
 		if (REPAINT_COMMAND.equals(evt.getActionCommand())) {
 			repaint();
 		} else if (START_COMMAND.equals(evt.getActionCommand())) {
 			startGame();
 		}
 	}
 
 }
