 package board;
 
 
 /** 
  * Tile information. 
  * Among others, contains whether it's walkable, the displayed character, the type (Player, Box, etc).
  */
 public enum Symbol {	
 	Mark(        'X', ' ', 'X', false, Type.Mark), /* To mark tiles to be ignored */
 	Empty(       ' ', ' ', ' ', true, Type.None),
 	Wall(        '#', '#', ' ', false, Type.None),
 	Goal(        '.', '.', ' ', true, Type.None),
	Player(      '@', ' ', '@', true, Type.Player),
 	Box(         '$', ' ', '$', false, Type.Box),
	PlayerOnGoal('+', '.', '@', true, Type.Player),
 	BoxOnGoal(   '*', '.', '$', false, Type.Box);
 	
 	public static enum Type {Player, Box, Mark, None};
 
 	public final char value;
 	public final char staticValue;
 	public final char dynamicValue;
 	public final boolean isWalkable;
 	public final Type type;
 	
 	
 	private Symbol(char value, char staticValue, char dynamicValue, boolean walkable, Type type) {
 		this.value = value;
 		this.staticValue = staticValue;
 		this.dynamicValue = dynamicValue;
 		this.isWalkable = walkable;
 		this.type = type;
 	}
 
 	public static Symbol get(char c) {
 		for (Symbol s : values()) {
 			if (s.value == c) return s;
 		}
 		throw new IllegalArgumentException("The value '" + c + "' is not one of the accepted symbols.");
 	}
 	
 	public String toString() {
 		return Character.toString(value);
 	}
 }
