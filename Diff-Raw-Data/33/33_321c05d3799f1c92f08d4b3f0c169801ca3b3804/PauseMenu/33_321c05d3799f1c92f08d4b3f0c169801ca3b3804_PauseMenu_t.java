 package game;
 
 import java.io.FileNotFoundException;
 
 import javax.xml.stream.XMLStreamException;
 
 import game.gameplayStates.GamePlayState;
 import game.io.SaveGame;
 
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.ShapeFill;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.fills.GradientFill;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class PauseMenu {
 	
 	/*
 	 * 0 represents selection on RESUME
	 * 1 represents selection on QUIT
 	 */
 	private int m_selection = 0;
 
 	private float m_x;
     private float m_y;
 	private float m_width;
 	private float m_height;
 	
 	private Rectangle m_rectangle;
 	private ShapeFill m_shapeFill;
 	
 	private int m_inputDelta = 0;
 	private GamePlayState m_game;
 	
 	public float getX() { return m_x; }
 	public float getY() { return m_x; }
 	public float getWidth() { return m_width; }
 	public float getHeight() { return m_height; }
 	
 	public PauseMenu(GamePlayState game, GameContainer container) {
 		m_game = game;
 		
		m_width = container.getWidth()/3; // pause menu width is half the screen width
		m_height = container.getHeight()/3; // pause menu height is half the screen height
		m_x = m_width/3+140;
		m_y = m_height/3+160;
 		
 		m_rectangle = new Rectangle(m_x, m_y, m_width, m_height);
 		m_shapeFill = new GradientFill(m_x, m_y, Color.white,
 									   m_x + m_width, m_y + m_height, Color.white);
 	}
 	
 	public void render(Graphics g) throws SlickException {
 		g.fill(m_rectangle, m_shapeFill);
 		g.setColor(Color.black);
		g.drawString("resume", m_x + 60, m_y + 60);
		g.drawString("quit", m_x + 60, m_y + 110);
 		
 		switch (m_selection) {
 		case 0:
			g.drawString("<", m_x + 130, m_y + 60);
 			break;
 		case 1:
			g.drawString("<", m_x + 130, m_y + 110);
 			break;
 		}
 	}
 	
 	public void update(GameContainer container, StateBasedGame stateManager, int delta) {
 		Input input = container.getInput();
 		m_inputDelta-=delta;
         if (m_inputDelta<0 && input.isKeyDown(Input.KEY_UP)) {
         	if (m_selection > 0)
         		m_selection--;
         	m_inputDelta=200;
         }
         else if (m_inputDelta<0 && input.isKeyDown(Input.KEY_DOWN)) {
        	if (m_selection < 1)
         		m_selection++;
         	m_inputDelta=200;
         }
         if(m_inputDelta<0 && input.isKeyDown(Input.KEY_SPACE)){
         	if (m_selection == 0)
         		m_game.setPauseState(false);
         	else if (m_selection == 1)
         		System.exit(0);
         	input.resetInputTransform();
         	m_inputDelta=200;
         }			
 	}
 	
 	public void getDrawData() {
 	}
 }
