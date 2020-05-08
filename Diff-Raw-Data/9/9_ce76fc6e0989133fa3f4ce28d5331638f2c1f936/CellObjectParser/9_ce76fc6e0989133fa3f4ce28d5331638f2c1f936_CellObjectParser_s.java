 package map;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Scanner;
 
 import notify.Notification;
 
 import org.newdawn.slick.tiled.GroupObject;
 
 import utils.triggers.TriggerSource;
 import utils.triggers.VoidAugmentedTriggerEffect;
 import entities.DestructibleEntity;
 import entities.Entity;
 import entities.enemies.Enemy;
 import entities.npcs.NPC;
 import entities.objects.Cage;
 import entities.objects.Door;
 import entities.objects.DoorProjectileTrigger;
 import entities.objects.DoorTrigger;
 import entities.objects.JumpPlatform;
 import entities.objects.LeafTest;
 import entities.objects.TeleportReciever;
 import entities.objects.TeleportSender;
 import entities.objects.TextField;
 import entities.objects.items.StickItem;
 import entities.objects.watereffects.WaterSurfaceEffect;
 import game.config.Config;
 
 /**
  * A class responsible for decoding all the entities represented on the map.<br />
  * Should be initialised with all cell's contents prior to parsing to ensure any dependencies
  * traversing multiple cells is correctly interpreted.<br />
  * Note this object will set itself to null after parsing is complete. Please do not keep 
  * references to any object of this class.
  */
 public class CellObjectParser {
 	
 	private static CellObjectParser inst = null;
 	
 	/**
 	 * Returns the current instance of the parser. If one does not currently exist (e.g. it has
 	 * been destroyed by a call to {@code parse()}), a new object is created and returned.
 	 */
 	static CellObjectParser getInst(){
 		return inst == null ? inst = new CellObjectParser() : inst;
 	}
 	
 	private final PriorityQueue<IndexedGroupObject> parseQueue = new PriorityQueue<IndexedGroupObject>();
 	
 	private final Map<String,Entity> everything = new HashMap<String,Entity>();
 	
 	private final Map<String,TeleportReciever> teleportRecievers = new HashMap<String,TeleportReciever>();
 	private final Map<String,TriggerSource> triggers = new HashMap<String,TriggerSource>();
 	private final Map<String,TextField<?>> textFields = new HashMap<String,TextField<?>>();
 	
 	/**
 	 * Adds a new object to the queue to be parsed. Will not parse this object until a call to {@code parse()} is made.
 	 */
 	void addParseObject(Cell parent, GroupObject go){
 		parseQueue.add(new IndexedGroupObject(parent, go));
 	}
 	
 	/**
 	 * Consumes this parser and it's contents, populating the cells references with the objects added previously.
 	 */
 	void parse(){
 		while(!parseQueue.isEmpty()){
 			IndexedGroupObject igo = parseQueue.poll();
 			parseObject(igo.getCell(), igo.unwrap());
 		}
 		parseTrigger("none 01_npc_bob textField Save meeeeeeeeee!");
 		parseTrigger("01_cage1 death 01_npc_bob textField Thanks :)");
 		inst = null;
 	}
 	
 	private void parseTrigger(String trigger) {
 		//TODO everything...
 		final Scanner in = new Scanner(trigger);
 		final String src = in.next();
 		
 		if(src.equalsIgnoreCase("none")){
 			getTriggerEffect(trigger, in).triggered();
 			return;
 		}
 		
 		final String cond = in.next();
 		VoidAugmentedTriggerEffect<? super Entity> triggerEffect = getTriggerEffect(trigger, in);
 		
 		if(triggerEffect != null){
 			final Entity srcE = everything.get(src);
 			if(srcE == null){
 				System.out.println("Warning: Failed to parse '" + trigger + "': No such id: '" + src + "'.");
 				return;
 			}else if(cond.equalsIgnoreCase("death")){
 				if(!(srcE instanceof DestructibleEntity)){
 					System.out.println("Warning: Failed to parse '" + trigger + "': Entity: '" + src + "' may not hold a death trigger.");
 					return;
 				}
 				((DestructibleEntity) srcE).addDeathTrigger(triggerEffect);
 			}
 		}
 	}
 
 	private VoidAugmentedTriggerEffect<? super Entity> getTriggerEffect(final String trigger, final Scanner in) {
 		final String dst = in.next();
 		if(dst.equalsIgnoreCase("notify")){
 			return new VoidAugmentedTriggerEffect<Entity>() {
 				@Override
 				public void triggered() {
					Notification.addNotification(in.nextLine());
 				}
 			};
 		}else{
 			final Entity dstE = everything.get(dst);
 			if(dstE == null){
 				System.out.println("Warning: Failed to parse '" + trigger + "': No such id: '" + dst + "'.");
 				return null;
 			}
 			final String res = in.next();
 			if(res.equalsIgnoreCase("textField")){
 				final TextField<?> tf;
 				if(dstE instanceof NPC){
 					tf = ((NPC) dstE).getTextField();
 				}else if(dstE instanceof TextField<?>){
 					tf = (TextField<?>) dstE;
 				}else{
 					System.out.println("Warning: Failed to parse '" + trigger + "': Entity: '" + dst + "' may not set text field contents.");
 					return null;
 				}
 				return new VoidAugmentedTriggerEffect<Entity>() {
 					@Override
 					public void triggered() {
						tf.setText(in.nextLine());
 					}
 				};
 			}
 		}
 		return null;
 	}
 
 	private void parseObject(Cell cell, GroupObject go) {
 		String id = go.name;
 		int x = go.x / Config.getTileSize();
 		int y = go.y / Config.getTileSize();
 		int width  = go.width  / Config.getTileSize();
 		int height = go.height / Config.getTileSize();
 		String subtype = go.props == null ? null : go.props.getProperty("type");
 		
 		if(id == null || id.isEmpty()){
 			System.out.println("Warning: " + go.type + " object at " + x + "," + y + " found with no id.");
 		}
 		
 		if(go.type.equalsIgnoreCase("enemy")){
 			Enemy e = Enemy.getNew(cell, subtype, x,y);
 			cell.addDefaultMovingEntity(e);
 			everything.put(id, e);
 		}else if(go.type.equalsIgnoreCase("npc")){
 			NPC npc = NPC.getNew(cell, subtype, x,y);
 			String tfstr = go.props.getProperty("text_id");
 			if(tfstr != null){
 				TextField<?> tf = textFields.get(tfstr);
 				if(tf != null){
 					npc.setTextField(tf);
 				}
 			}
 			cell.addDefaultMovingEntity(npc);
 			everything.put(id, npc);
 		}else if(go.type.equalsIgnoreCase("door")){
 			Door d = new Door(cell,x,y);
 			String ts = go.props.getProperty("triggers");
 			if(ts != null){
 				for(String tid : ts.split("\\s+")) {
 					TriggerSource trig = triggers.get(tid);
 					trig.addTriggerEffect(d);
 					d.addTriggerSource(trig);
 				}
 			}
 			cell.addStaticEntity(d);
 			everything.put(id, d);
 		}else if(go.type.equalsIgnoreCase("doorTrigger")){
 			DoorTrigger dt = new DoorTrigger(x,y);
 			if(id != null){
 				triggers.put(id, dt);
 			}
 			cell.addStaticEntity(dt);
 			everything.put(id, dt);
 		}else if(go.type.equalsIgnoreCase("doorProjectileTrigger")){
 			DoorProjectileTrigger dpt = new DoorProjectileTrigger(x,y);
 			if(id != null){
 				triggers.put(id, dpt);
 			}
 			cell.addStaticEntity(dpt);
 			everything.put(id, dpt);
 		}else if(go.type.equalsIgnoreCase("leafTest")){
 			LeafTest lt = new LeafTest(x,y);
 			cell.addStaticEntity(lt);
 			everything.put(id, lt);
 		}else if(go.type.equalsIgnoreCase("jumpPlatform")){
 			JumpPlatform jp = new JumpPlatform(x,y,width);
 			cell.addStaticEntity(jp);
 			everything.put(id, jp);
 		}else if(go.type.equalsIgnoreCase("cage")){
 			Cage c = new Cage(cell, x,y,width,height);
 			cell.addDefaultDestructibleEntity(c);
 			everything.put(id, c);
 		}else if(go.type.equalsIgnoreCase("textField")){
 			TextField<?> tf = TextField.newTextField(x,y,width,height,go.props);
 			if(id != null){
 				textFields.put(id, tf);
 			}
 			cell.addStaticEntity(tf);
 			everything.put(id, tf);
 		}else if(go.type.equalsIgnoreCase("teleport_recieve")){
 			TeleportReciever tr = new TeleportReciever(cell, x, y, width, height);
 			teleportRecievers.put(id,tr);
 			cell.addStaticEntity(tr);
 			everything.put(id, tr);
 		}else if(go.type.equalsIgnoreCase("teleport_send")){
 			String dest = go.props.getProperty("dest");
 			TeleportSender ts = new TeleportSender(teleportRecievers.get(dest), x, y, width, height);
 			cell.addStaticEntity(ts);
 			everything.put(id, ts);
 		}else if(go.type.equalsIgnoreCase("waterSurfaceEffect")){
 			WaterSurfaceEffect wse = new WaterSurfaceEffect(x, y, width);
 			cell.addStaticEntity(wse);
 			everything.put(id, wse);
 		}else if(go.type.equalsIgnoreCase("weapon_item_stick")){
 			StickItem si = new StickItem(x, y, width, height);
 			cell.addDefaultDestructibleEntity(si);
 			everything.put(id, si);
 		}
 	}
 	
 	/**
 	 * A utility class for holding triples of parent cell, parse object and queue priority.
 	 */
 	private static class IndexedGroupObject implements Comparable<IndexedGroupObject> {
 		
 		private final Cell c;
 		private final int index;
 		private final GroupObject go;
 
 		public IndexedGroupObject(Cell c, GroupObject go, int index) {
 			this.c = c;
 			this.go = go;
 			this.index = index;
 		}
 		
 		public IndexedGroupObject(Cell c, GroupObject go){
 			this(c,go,ParseOrder.getIndex(go.type));
 		}
 		
 		public GroupObject unwrap(){
 			return go;
 		}
 		
 		public Cell getCell(){
 			return c;
 		}
 		
 		@Override
 		public int compareTo(IndexedGroupObject o) {
 			return index - o.index;
 		}
 		
 	}
 	
 	/**
 	 * A utility enum for determining parse ordering.<br />
 	 * Objects with no dependencies should be located at a lower ordinal than those 
 	 * with a high number of dependencies.
 	 */
 	private static enum ParseOrder {
 		WEAPON_ITEM_STICK,
 		LEAFTEST,
 		JUMPPLATFORM,
 		ENEMY,
 		DOORTRIGGER,
 		WATERSURFACEEFFECT,
 		DOORPROJECTILETRIGGER,
 		CAGE,
 		TEXTFIELD,
 		TELEPORT_RECIEVE,
 		TELEPORT_SEND,
 		DOOR,
 		NPC;
 		
 		/**
 		 * Returns the priority index of the type specified.
 		 * @throws IllegalArgumentException if no such type exists.
 		 */
 		public static int getIndex(String type) {
 			return valueOf(type.toUpperCase()).ordinal();
 		}
 	}
 }
