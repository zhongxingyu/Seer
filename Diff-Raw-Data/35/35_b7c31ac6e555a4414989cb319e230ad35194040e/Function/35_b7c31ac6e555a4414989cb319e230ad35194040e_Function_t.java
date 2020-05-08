 package game;
 
 public class Function {
 	public String name;
 	public String desc;
 	
 	public Function(String name, Game game) {
 		this(name, game, "");
 	}
 	
 	public Function(String name, Game game, String desc) {
 		this.name = name;
		this.desc = desc;
 		game.addFunction(this);
 	}
 	
 	public Object run(Object args) {
 		
 		return null;
 	}
 }
