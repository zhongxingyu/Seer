 package DD.MapTool;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 import DD.Character.*;
 import DD.CombatSystem.TargetingSystem.Coordinate;
 import DD.SlickTools.*;
 
 public class CharacterObjects extends Objects{
 
 	private static final long serialVersionUID = -942204818115561142L;
 	public DDCharacter ddchar;
 	
 	public CharacterObjects(String name, DDImage image, int x, int y, Map owner, DDCharacter ddchar) throws SlickException {
 		super(name, image, owner, x, y);
 		this.ddchar = ddchar;
 		super.priority =10;
 		ddchar.addComponent(new CharacterStats());
 	}
 	
 	public CharacterObjects(String name, DDImage image, Map owner, DDCharacter ddchar) throws SlickException {
 		this(name, image, 0, 0, owner, ddchar);
 
 	}
 	
 	public CharacterObjects() {
 		// TODO Auto-generated constructor stub
 	}
 
 	public String toString(){
 		return "DDcharToString:(~*-*)~\n";
 	}
 	
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) 
 	{
 		try 
 		{
 			ddchar.update(gc, sbg, delta);
 		} /* end try */
 		catch (SlickException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} /* end catch */
 	}
 	
 	public DDCharacter getDdchar(){
 		return ddchar;
 	}
 	
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics gr)
 	{
 		float x = position.x;
 		float y = position.y;
 		float xCorrection = 30.85f;
 		float yCorrection = 30;
 		if(image != null) image.draw(x*xCorrection, (y+1)*yCorrection);
 		((CharacterEntity)ddchar).render(gc, sbg, gr, x*xCorrection, (y+1)*yCorrection);
 	} /* end render method */
 	
 	public void setPosition(int x, int y) {
 		this.position = new Coordinate(x,y);
		this.ddchar.setCoordiante(new Coordinate(x,y));
 	}
 }
