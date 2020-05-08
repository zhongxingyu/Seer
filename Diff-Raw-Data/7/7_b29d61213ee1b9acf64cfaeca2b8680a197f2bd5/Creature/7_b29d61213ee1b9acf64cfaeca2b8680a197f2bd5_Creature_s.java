 package pl.edu.agh.megamud.base;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import pl.edu.agh.megamud.GameServer;
 import pl.edu.agh.megamud.dao.Attribute;
 import pl.edu.agh.megamud.dao.CreatureAttribute;
 import pl.edu.agh.megamud.dao.ItemAttribute;
 import pl.edu.agh.megamud.dao.PlayerCreature;
 import pl.edu.agh.megamud.dao.Profession;
 
 import com.j256.ormlite.dao.ForeignCollection;
 
 /**
  * A "creature", object/person that we can interact with.
  */
 public class Creature extends ItemHolder implements BehaviourHolderInterface{
 	/**
 	 * Its name.
 	 */
 	protected String name;
 	/**
 	 * Its brain.
 	 */
 	protected Controller controller=null;
 	/**
 	 * Its current location. MUST be one.
 	 */
 	protected Location location=null;
 	
 	/**
 	 * In-database representation.
 	 */
 	protected PlayerCreature dbCreature;
 	
 	private BehaviourHolder behaviourHolder = new BehaviourHolder();
 	
 	protected Profession profession=Profession.DEFAULT;
 	private int level;
 	private int hp;
 	private int expNeeded;
 	private int exp;
 	
 	public PlayerCreature getDbCreature() {
 		return dbCreature;
 	}
 	
 	public void setDbCreature(PlayerCreature dbCreature) {
 		this.dbCreature = dbCreature;
 		
 		this.profession=dbCreature.getProfession();
 		this.level=dbCreature.getLevel();
 		this.hp=dbCreature.getHp();
 		this.exp=dbCreature.getExp();
 		this.expNeeded=dbCreature.getExp_needed();
 		
 		List<Attribute> attrs;
 		try {
 			attrs = Attribute.createDao().queryForAll();
 			for(Iterator<Attribute> i=attrs.iterator();i.hasNext();){
 				Attribute a=i.next();
 				List<CreatureAttribute> found=CreatureAttribute.createDao().queryBuilder().where().eq("attribute_id",a).and().eq("creature_id", this.dbCreature).query();
 				
 				if(found.size()>0){
 					CreatureAttribute first=found.get(0);
 					this.attributes.put(a,first.getValue().longValue());
 				}else{
 					this.attributes.put(a,Long.valueOf(0L));
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	// @todo integrate with db
 	private Map<Attribute,Long> attributes=new HashMap<Attribute,Long>();
 	protected List<Modifier> modifiers=new LinkedList<Modifier>();
 	
 	public Creature setName(String name) {
 		this.name=name;
 		if(this.dbCreature!=null){
 			this.dbCreature.setName(name);
 			this.commit();
 		}
 		return this;
 	}
 	
 	public void commit(){
 		if(this.dbCreature==null)
 			return;
 		try{
 			PlayerCreature.createDao().update(this.dbCreature);
 		}catch(SQLException e){
 			e.printStackTrace();
 		}
 	}
 	public Creature setHp(int hp) {
 		this.hp=hp;
 		if(this.dbCreature!=null){
 			this.dbCreature.setHp(hp);
 			this.commit();
 		}
 		return this;
 	}
 	public Creature setLevel(int level) {
 		this.level=level;
 		if(this.dbCreature!=null){
 			this.dbCreature.setLevel(level);
 			this.commit();
 		}
 		return this;
 	}
 	public Creature setExp(int exp) {
 		this.exp=exp;
 		if(this.dbCreature!=null){
 			this.dbCreature.setHp(exp);
 			this.commit();
 		}
 		return this;
 	}
 	public Creature setExpNeeded(int expNeeded) {
 		this.expNeeded=expNeeded;
 		if(this.dbCreature!=null){
 			this.dbCreature.setExp_needed(expNeeded);
 			this.commit();
 		}
 		return this;
 	}
 	
 	public Creature setModifiers(List<Modifier> modifiers) {
 		this.modifiers = modifiers;
 		return this;
 	}
 	public List<Modifier> getModifiers() {
 		return this.modifiers;
 	}
 	
 	public Creature setProfession(Profession profession) {
 		this.profession=profession;
 		if(this.dbCreature!=null){
 			this.dbCreature.setProfession(profession);
 			this.commit();
 		}
 		return this;
 	}
 	
 	public int getHp() {
 		return this.hp;
 	}
 	
 	public boolean addDamage(int hpMinus){
 		hp-= hpMinus;
 		this.setHp(hp);
 		if(hp <=0 ){
 			//TODO Death
 //			getLocation().getCreatures
 			GameServer.getInstance().killCreature(this);
 			return true;
 		}
 		return false;
 	}
 	
 	public int getLevel() {
 		return this.level;
 	}
 	
 	public int getExp() {
 		return this.exp;
 	}
 	public int getExpNeeded() {
 		return this.expNeeded;
 	}
 	public Map<Attribute, Long> getAttributes() {
 		return this.attributes;
 	}
 	public void setAttribute(String x,Long val){
 		for(Iterator<Entry<Attribute,Long>> set=attributes.entrySet().iterator();set.hasNext();){
 			Entry<Attribute,Long> next=set.next();
 			Attribute a=next.getKey();
 			if(a.getName().equals(x)){
 				next.setValue(val);
 				
 				if(this.dbCreature!=null){
 					try{
 						CreatureAttribute ne=new CreatureAttribute();
 						ne.setAttribute(a);
 						ne.setCreature(this.dbCreature);
 						
 						CreatureAttribute.createDao().delete(ne);
 						//CreatureAttribute.createDao().deleteBuilder().where().eq("attribute_id",a).and().eq("creature_id", this.dbCreature).query();
 						
 						ne=new CreatureAttribute();
 						ne.setAttribute(a);
 						ne.setCreature(this.dbCreature);
 						ne.setValue(val.intValue());
 						CreatureAttribute.createDao().create(ne);
 					}catch(SQLException e){
 						e.printStackTrace();
 					}
 				}
 				return;
 			}
 		}
 		//@todo save to db
 	}
 	public final Controller getController(){
 		return this.controller;
 	}
 	public String getName(){
 		return this.name;
 	}
 	public Profession getProfession(){
 		return this.profession;
 	}
 	
 	public Creature(String name){
 		this.name=name;
 		
 		List<Attribute> attrs;
 		try {
 			attrs = Attribute.createDao().queryForAll();
 			for(Iterator<Attribute> i=attrs.iterator();i.hasNext();){
 				Attribute a=i.next();
 				this.attributes.put(a, 0L);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	/**
 	 * Executed upon "login" command or after creating a NPC (manually). Binds a creature to a controller and shares its commands.
 	 */
 	public void connect(Controller controller){
 		this.controller=controller;
 		controller.setCreature(this);
 		
 		for(Command cmd:getAllCommands()){
 			controller.addCommand(cmd);
 		}
 	}
 	/**
 	 * Disconnects this creature from the controller.
 	 */
 	public void disconnect(){
 		for(Command cmd:getAllCommands()){
 			controller.removeCommand(cmd);
 		}
 		this.controller=null;
		location.getCreatures().remove(name);
 		location = null;
 	}
 	
 	public final Location getLocation(){
 		return location;
 	}
 	
 	/**
 	 * Moves creature to other location. Optionally provide an exit name for notifications.
 	 * @todo Write this to database.
 	 */
 	public void setLocation(Location exit,String exitName){
 		if(location!=null){
 			for(Command cmd:location.getAllCommands()){
 				removeCommand(cmd);
 				controller.removeCommand(cmd);
 			}
 			
 			location.onRemoveCreature(this,exitName);
 		}
 		
 		location=exit;
 		
 		if(exit!=null){
 			for(Command cmd:location.getAllCommands()){
 				addCommand(cmd);
 				controller.addCommand(cmd);
 			}
 			location.onAddCreature(this);
 		}
 		//@todo write to db
 	}
 	
 	/**
 	 * Tries to move to a specified exit.
 	 * @return true, if succedeed. 
 	 */
 	public boolean moveTo(String exit){
 		Map<String,Location> exits=location.getExits();
 		if(exits.containsKey(exit)){
 			setLocation(exits.get(exit),exit);
 			return true;
 		}
 		return false;
 			
 	}
 	
 	/**
 	 * Use this to give an experience points to a creature. This can change creature's level (when after change exp>=expNeeded).
 	 * @todo change level, change stats, force class change upon some level/block change
 	 * @todo Write this to database
 	 */
 	public void giveExp(int num){
 		int exp=this.getExp();
 		int expn=this.getExpNeeded();
 		int lev=this.getLevel();
 		
 		exp+=num;
 		while(exp>=expn){
 			//TODO sensowne wartoci?
 			exp-=expn;
 			expn*=1.2;
 			lev++;
 		}
 		
 		this.setExp(exp);
 		this.setExpNeeded(expn);
 		this.setLevel(lev);
 		this.commit();
 	}
 	
 	/**
 	 * Adds a temporary attributes modifier.
 	 * @todo Write this to database
 	 */
 	public void addModifier(Modifier m){
 		this.modifiers.add(m);
 		m.onBegin();
 	}
 	
 	/**
 	 * Removes a temporary attributes modifier.
 	 * @todo Write this to database
 	 */
 	public void removeModifier(Modifier m){
 		this.modifiers.remove(m);
 		m.onStop();
 	}
 	
 	/**
 	 * Generates a temporary (at a moment of generation) attributes of a creature. Map attribute-id -> value.
 	 * Calculation:
 	 * - get base attributes of a creature;
 	 * - apply modifiers;
 	 * - return the result.
 	 */	
 	protected Map<Attribute,Long> generateAttributes(){
 		Map<Attribute,Long> cur=new HashMap<Attribute,Long>();
 		cur.putAll(getAttributes());
 		
 		for(ListIterator<Modifier> i=modifiers.listIterator();i.hasNext();){
 			Modifier m=i.next();
 			if(!m.modify(this, cur)){
 				m.onStop();
 				i.remove();
 			}
 		}
 		return cur;
 	}
 	
 	/**
 	 * Use this to send a message to a creature.
 	 */
 	public void write(String s){
 		controller.write(s);
 	}
 	
 	public void onItemAppear(Item i,ItemHolder from){
 		getController().onItemAppear(i, from);
 	}
 	public void onItemDisappear(Item i,ItemHolder to){
 		getController().onItemDisappear(i, to);
 	}
 
 	@Override
 	public List<Behaviour> getBehaviourList() {
 		return behaviourHolder.getBehaviourList();
 	}
 
 	@Override
 	public void setBehaviourList(List<Behaviour> list) {
 		behaviourHolder.setBehaviourList(list);
 	}
 
 	@Override
 	public void addBehaviour(Behaviour behaviour) {
 		behaviourHolder.addBehaviour(behaviour);
 	}
 
 	@Override
 	public void removeBehaviour(Behaviour behaviour) {
 		behaviourHolder.removeBehaviour(behaviour);
 		
 	}
 
 	@Override
 	public List<Behaviour> getBehaviourByType(Class clazz) {
 		return behaviourHolder.getBehaviourByType(clazz);
 	}
 
 	
 }
