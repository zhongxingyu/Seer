 package se.chalmers.kangaroo.view;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 
 /**
  * A view to show when the game is paused.
  * 
  * 
  * @author arvidk
  * 
  */
 public class PauseView extends JPanelWithBackground implements MouseListener {
 	private Menuebutton resume, highScore, options, exitGame;
 	private String viewName = "gameview";
	
	//TOTO make sure that the thread that runs the game is asleep when the pause view is showed.
	//Check with the rest of the group how to connect the view and also graphics.
	
	
 	private ChangeView cv;
 
 	/**
 	 * The constructor for the GameView.
 	 * 
 	 * @param bgpath
 	 */
 	public PauseView(String bgpath, ChangeView cv) {
 		super(bgpath);
 		this.cv = cv;
 		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
 		this.setLayout(layout);
 		this.setSize(1024, 576);
		resume = new Menuebutton("resources/images/resuem.png");
 		highScore = new Menuebutton("resources/images/highscore.png");
 		options = new Menuebutton("resources/images/options.png");
 		exitGame = new Menuebutton("resources/images/exitgame.png");
 
 		this.add(Box.createVerticalGlue());
 		this.add(new Menuebutton("resources/images/menu_logo.gif"));
 		this.add(Box.createVerticalGlue());
 		this.add(Box.createVerticalGlue());
 		this.add(Box.createVerticalGlue());
 		this.add(resume);
 		this.add(Box.createVerticalGlue());
 		this.add(highScore);
 		this.add(Box.createVerticalGlue());
 		this.add(options);
 		this.add(Box.createVerticalGlue());
 		this.add(exitGame);
 		this.add(Box.createVerticalGlue());
 		this.add(Box.createVerticalGlue());
 		this.add(Box.createVerticalGlue());
 		
 		
 		resume.addMouseListener(this);
 		highScore.addMouseListener(this);
 		options.addMouseListener(this);
 		exitGame.addMouseListener(this);
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		if (e.getSource() == resume)
 			resume.setIcon(new ImageIcon(
 					"resources/images/resuem_onHover.png"));
 		if (e.getSource() == highScore)
 			highScore.setIcon(new ImageIcon(
 					"resources/images/highscore_onHover.png"));
 		if (e.getSource() == options)
 			options.setIcon(new ImageIcon(
 					"resources/images/options_onHover.png"));
 		if (e.getSource() == exitGame)
 			exitGame.setIcon(new ImageIcon(
 					"resources/images/exitgame_onHover.png"));
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		if (e.getSource() == resume)
 			resume.setIcon(new ImageIcon("resources/images/resume.png"));
 		if (e.getSource() == highScore)
 			highScore.setIcon(new ImageIcon("resources/images/highscore.png"));
 		if (e.getSource() == options)
 			options.setIcon(new ImageIcon("resources/images/options.png"));
 		if (e.getSource() == exitGame)
 			exitGame.setIcon(new ImageIcon("resources/images/exitgame.png"));
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		if (e.getSource() == resume)
 			resume.setIcon(new ImageIcon(
 					"resources/images/resuem_onSelect.png"));
 		if (e.getSource() == highScore)
 			highScore.setIcon(new ImageIcon(
 					"resources/images/highscore_onSelect.png"));
 		if (e.getSource() == options)
 			options.setIcon(new ImageIcon(
 					"resources/images/options_onSelect.png"));
 		if (e.getSource() == exitGame)
 			exitGame.setIcon(new ImageIcon(
 					"resources/images/exitgame_onSelect.png"));
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		if (e.getSource() == resume) {
 			resume.setIcon(new ImageIcon("resources/images/resume.png"));
 			//TODO wake the thread that the game is running in.
 			
 			cv.gameView();
 
 		}
 		if (e.getSource() == highScore) {
 			highScore.setIcon(new ImageIcon("resources/images/highscore.png"));
 			cv.highscoreView(viewName);
 		}
 
 		if (e.getSource() == options) {
 			options.setIcon(new ImageIcon("resources/images/options.png"));
 			cv.optionView(viewName);
 
 		}
 		if (e.getSource() == exitGame) {
 			exitGame.setIcon(new ImageIcon("resources/images/exitGame.png"));
 			System.exit(0);
 		}
 	}
 
 }
