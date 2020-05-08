 package dungeonCrawler.GameElements;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 import dungeonCrawler.GameElement;
 import dungeonCrawler.GameEvent;
 import dungeonCrawler.LevelLoader;
 import dungeonCrawler.Vector2d;
 
 public class NetworkPlayer extends GameElement {
 
 	private static NetworkPlayer element;
 
 	public NetworkPlayer(Vector2d position, Vector2d size) {
 		super(position, size);
 		// TODO Auto-generated constructor stub
 	}
 
 	public NetworkPlayer(Vector2d position, Vector2d size, int id) {
 		super(position, size, id);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		g.setColor(Color.BLUE);
 		g.fillRect(0, 0, size.getX(), size.getY());
 
 	}
 
 	@Override
 	public void GameEventPerformed(GameEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
	
 
 	@Override
 	public String getName() {
 		return "NETWORKPLAYER";
 	}
 
 	public static NetworkPlayer createElement(String[] param, int id) {
			if (param.length > 5) {
				element = new NetworkPlayer(new Vector2d(), new Vector2d(), Integer.parseInt(param[1]));
			}
			else {
				element = new NetworkPlayer(new Vector2d(), new Vector2d(), id);
			}
		modify(param);
 		return element;
 	}
 
 	/**Modifies parameters.
 	 * @param param as {@link String[]}
 	 */
	public static void modify(String[] param) {
 		Vector2d position = new Vector2d();
 		Vector2d size = new Vector2d();
 		try {
 			int i = (param.length > 5 ? 1 : 0);
 			position.setX(Integer.parseInt(param[i+1]));
 			position.setY(Integer.parseInt(param[i+2]));
 			size.setX(Integer.parseInt(param[i+3]));
 			size.setY(Integer.parseInt(param[i+4]));
 			element.setPosition(position);
 			element.setSize(size);
//			element.gei.setSize(size);
 		} catch (NumberFormatException e) {
 			System.out.println("Kann NetworkPlayer-Parameter nicht interpretieren.");
 			element = null;
 		}
 	}
	
 	@Override
 	public boolean collision(GameElement element) {
 		if(!(element instanceof Player))
 			return super.collision(element);
 		return false;
 	}
 
 }
