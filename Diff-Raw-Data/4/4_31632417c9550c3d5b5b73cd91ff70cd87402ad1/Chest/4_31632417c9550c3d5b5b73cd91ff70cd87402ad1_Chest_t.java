 package game.things;
 
 import serialization.Serializer;
 import serialization.SerializerUnion;
 import serialization.Tree;
 import util.Direction;
import game.*;
 
 public class Chest extends AbstractGameThing {
 	public static void makeSerializer(SerializerUnion<GameThing> union, final GameWorld world){
 		union.addIdentifier(new SerializerUnion.Identifier<GameThing>(){
 			public String type(GameThing g){
 				return g instanceof Chest? "chest" : null;
 			}
 		});
 		
 		union.addSerializer("chest", new Serializer<GameThing>(){
 			public Tree write(GameThing o){
 				Chest in = (Chest)o;
 				Tree out = new Tree();
 				out.add(new Tree.Entry("type", new Tree(in.renderer)));
 				return out;
 			}
 
 			public GameThing read(Tree in){
 				return new Chest(world, in.find("type").value());
 			}
 		});
 	}
 		
 	private final String renderer;
 	private final Container cont;
 
 	
 	//For reading in (serializer to be completed)
 	public Chest(GameWorld world, String name, Container cont){
 		super(world);
 		renderer = name;
 		update();
 		this.cont = cont;
 	}
 	
 	//For empty chest
 	public Chest(GameWorld world, String name){
 		super(world);
 		renderer = name;
 		update();
 		cont = new Container();
 	}
 	
 	//need the default renderer for a chest, at the moment looks like a wall...
 	public Chest(GameWorld world){
 		this(world, "wallcross");
 	}
 
 	public String renderer(){
 		return renderer;
 	}
 
 	public String name(){
 		return "Chest";
 	}
 	
 	@Override
 	public boolean canWalkInto(Direction d, Player p) {
 		return false;
 	}
 }
