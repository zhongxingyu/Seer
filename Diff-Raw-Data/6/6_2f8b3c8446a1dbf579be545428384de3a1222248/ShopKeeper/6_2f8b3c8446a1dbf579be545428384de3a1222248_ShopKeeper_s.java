 package game.things;
 
 import game.*;
 
 import java.util.*;
 
 import serialization.*;
 
 public class ShopKeeper extends Character implements Containable, Namable {
 	public static void makeSerializer(final SerializerUnion<GameThing> union, final GameWorld world){
 		union.addIdentifier(new SerializerUnion.Identifier<GameThing>(){
 			public String type(GameThing g){
 				return g instanceof ShopKeeper? "shopkeeper" : null;
 			}
 		});
 
 		union.addSerializer("shopkeeper", new Serializer<GameThing>(){
 			public Tree write(GameThing o){
 				ShopKeeper in = (ShopKeeper)o;
 				Tree out = new Tree();
				out.add(new Tree.Entry("renderer", new Tree(in.renderer())));
 				out.add(new Tree.Entry("name", new Tree(in.name)));
 				out.add(new Tree.Entry("parts", Serializers.map(Serializers.Serializer_String, Container.serializer(union.serializer(), world)).write(in.parts)));
 				return out;
 			}
 
 			public GameThing read(Tree in) throws ParseException {
 				return new ShopKeeper(world,
 					in.find("renderer").value(),
 					in.find("name").value(),
 					Serializers.map(Serializers.Serializer_String, Container.serializer(union.serializer(), world)).read(in.find("parts")));
 			}
 		});
 	}
 
 	private final Map<String, Container> parts;
 	private String name;
 
 	private ShopKeeper(GameWorld world, String r, String n, Map<String, Container> p){
 		super(world, r);
 		name = n;
 		parts = p;
 		health(1000);
 		update();
 	}
 
 	public ShopKeeper(GameWorld w, String r, String n){
 		this(w, r, n, new HashMap<String, Container>());
 	}
 
 	public String name(){
 		return name;
 	}
 
 	public Container addPart(String name){
 		Container c = new Container(world());
 		parts.put(name, c);
 		update();
 		return c;
 	}
 
 	public List<String> interactions(){
 		List<String> out = new LinkedList<String>();
 		for(String n : parts.keySet())
 			out.add("buy " + n);
 		out.addAll(super.interactions());
 		return out;
 	}
 
 	public void interact(String name, Player who){
 		for(Map.Entry<String, Container> kv : parts.entrySet())
 			if(name.equals("buy " + kv.getKey()))
 				who.showContainer(kv.getValue(), "Buying " + kv.getKey());
 		super.interact(name, who);
 	}
 
 	public String name(String s){
 		return name = s;
 	}
 
 	public Map<String, Container> getContainers(){
 		return new HashMap<String, Container>(parts);
 	}
 }
