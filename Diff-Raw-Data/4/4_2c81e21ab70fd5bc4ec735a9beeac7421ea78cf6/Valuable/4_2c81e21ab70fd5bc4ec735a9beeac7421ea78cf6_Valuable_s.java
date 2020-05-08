 package game.things;
 
 import game.*;
 
 import serialization.*;
 
 public class Valuable extends PickupGameThing {
 	public static void makeSerializer(SerializerUnion<GameThing> union, final GameWorld world){
 		union.addIdentifier(new SerializerUnion.Identifier<GameThing>(){
 			public String type(GameThing g){
 				return g instanceof Valuable? "valuable" : null;
 			}
 		});
 
 		union.addSerializer("valuable", new Serializer<GameThing>(){
 			public Tree write(GameThing o){
 				Valuable in = (Valuable)o;
 				Tree out = new Tree();
 				out.add(new Tree.Entry("value", Serializers.Serializer_Integer.write(in.value)));
				out.add(new Tree.Entry("name", Serializers.Serializer_Integer.write(in.value)));
				out.add(new Tree.Entry("renderer", Serializers.Serializer_Integer.write(in.value)));
 				return out;
 			}
 
 			public GameThing read(Tree in) throws ParseException {
 				return new Valuable(world, in.find("name").value(), in.find("renderer").value(), Serializers.Serializer_Integer.read(in.find("value")));
 			}
 			
 		});
 	}
 	
 	private int value;
 	private String name, renderer;
 
 	public Valuable(GameWorld w, String n, String r, int v){
 		super(w);
 		value = v;
 		name = n;
 		renderer = r;
 		update();
 	}
 
 	public String name(){
 		return name;
 	}
 
 	public String renderer(){
 		return renderer;
 	}
 
 	public int value(){
 		return value;
 	}
 }
