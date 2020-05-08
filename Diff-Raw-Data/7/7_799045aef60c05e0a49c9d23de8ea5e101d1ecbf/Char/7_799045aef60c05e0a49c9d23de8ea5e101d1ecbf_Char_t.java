 package com.gdxtest02;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Random;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.ArrayMap;
 
 import static com.gdxtest02.GdxTest02.log;
 
 public class Char implements Cloneable {
 	private String name;
 	private String type;
 	private int hp;
 	protected int maxhp;
 	private int basehp;
 	private String description;
 	/**Damage to be taken this round. Skills will set this.
 	 * Final damage actually taken will only be decided at the end of the round.
 	 * 
 	 */
 	private int dmg;
 	private Rectangle box;
 	private Texture tex;
 	private String texname;
 	private int posX, posY;
 	/**Number of the current active action
 	 * 
 	 */
 	private int activeAction;
 	private Array<Action> actionBar;
 	private Array<Action> actionsInventory;
 	private ArrayMap<Integer, Action> actionUnlockedPerLevel;
 	private int maxActionBarSize = 4;
 	private Array<Buff> buffs;
 	/**List of buffs to be added this round, 
 	 * these will be moved to the main buffs list at the end of the round
 	 * so they don't make effect when first applied, only 1 round later
 	 */
 	private Array<Buff> newbuffs;
 	private Balance balance;
 	private Char target;
 	private boolean canoverheal;
 	
 	private float powerMultiplier;
 	private int level;
 	private int exp;
 	private Array<Integer> aiSkillList;
 
 	public Char(String name) {
 		this.name = name;
 		description = "This is a Char";
 		actionBar = new Array<Action>();
 		actionsInventory = new Array<Action>();
 		actionUnlockedPerLevel = new ArrayMap<Integer, Action>();
		aiSkillList = new Array<Integer>();
 		balance = new Balance(this);
 		this.maxhp = 1000;
 		this.posX = 0;
 		this.posY = 0;
 		basehp = maxhp;
 		canoverheal = true;
 		
 		level = 1;
 		powerMultiplier = 1;
 		
 		resetStats();
 //		resetAssets();
 	}
 
 	/**Reloads textures and assets
 	 * 
 	 */
 	private void resetAssets() {
 	}
 
 	/**Reset all stats to initial values
 	 * 
 	 */
 	public void resetStats() {
 		this.hp = maxhp;
 		this.activeAction = 0;
 		dmg = 0;
 		buffs = new Array<Buff>();
 		newbuffs = new Array<Buff>();
 		
 		updateAllActions();
 		//reset all actions
 		for (Action a : actionBar) a.reset();
 	}
 	
 	public void draw(SpriteBatch batch){
 		batch.draw(tex, posX, posY);
 	}
 	
 	/**Draw shapes, such as health bars
 	 * @param shapeRenderer
 	 */
 	public void drawShapes(ShapeRenderer shapeRenderer) {
 		int x = posX + 256/2 - 100; // bar start x position
 		int y = posY + 256 + 10; // bar start y position
 		int width = 200; // bar width
 		int height = 10; // bar height
 		// health normalized between 0 and 1
 		float health = (float)hp / (float)Math.max(maxhp, hp);
 		
 		shapeRenderer.setColor(0.1f, 0.1f, 0f, 1);
 		shapeRenderer.rect(x, y, width, height);
 		
 		shapeRenderer.setColor(1f, 1f * health, 0, 1);
 		shapeRenderer.rect(x, y, width * health, height);
 		
 //		Gdx.app.log("moo", "char: " + name + " hp: " + hp + " maxhp: " + maxhp
 //				+ " health: " + health);
 		
 	}
 	
 	public Balance getBalance() {
 		return balance;
 	}
 	
 	/**Finds the id of the action, returns zero if not found 
 	 * @param a
 	 * @return
 	 */
 	public int getIdOfAction(Action a) {
 		return actionBar.indexOf(a, true) + 1;
 	}
 	
 	/**Get the action of specific id, starting from 1
 	 * @param id
 	 */
 	public Action getAction(int id) {
 		if (id < 1 || id > actionBar.size) return null;
 		return actionBar.get(id - 1);
 	}
 	
 	/**Set the action of this id, starting from 1
 	 * @param id
 	 * @return
 	 */
 	public void setAction(int id, Action action) {
 		if (id < 1) return;
 		actionBar.set(id - 1, action);
 		action.setOwner(this);
 	}
 	
 	public void addAction(Action action) {
 		actionsInventory.add(action);
 		if (actionBar.size < maxActionBarSize) {
 			actionBar.add(action);
 		}
 		action.setOwner(this);
 		action.updatePower();
 	}
 	
 	/**Add buff to new buff list
 	 * @param buff
 	 */
 	public void addBuff(Buff buff) {
 		newbuffs.add(buff);
 	}
 	
 	/**Buffs do whatever they do when this is called.
 	 * Should be called by the game when it's time for buffs to take effect
 	 * such as, every turn
 	 */
 	public void applyBuffs() {
 		// list of buffs that will be removed
 		// this is done because you can't remove the items in the middle of a loop
 		Array<Buff> toremove = new Array<Buff>(); 
 		
 		// loop through all buffs and make their do their thing
 		for (Buff buff : buffs) {
 			buff.act(this);
 			buff.incDuration(-1);
 			if (buff.getDuration() == 0) toremove.add(buff); // this buff will be removed 
 		}
 		// remove buffs from the list of buffs to be removed
 		for (Buff buff : toremove) {
 			buffs.removeValue(buff, true);
 		}
 		
 		// move new buffs to main buffs list
 		buffs.addAll(newbuffs);
 		newbuffs.clear();
 	}
 	
 	/**Returns a string with a description of current buffs
 	 * @return
 	 */
 	public String printBuffs() {
 		String r = "(";
 		Buff b;
 		for (int i = 0; i < buffs.size; i++) {
 			b = buffs.get(i);
 			r += b.getName() + "(" + b.getDuration() + "s)";
 			if (i < buffs.size - 1) r += ", "; 
 		}
 		r += ")";
 		return r;
 	}
 	
 	public void dispose() {
 		tex.dispose();
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public int getPosX() {
 		return posX;
 	}
 	public int getPosY() {
 		return posY;
 	}
 	
 	public void setPos(int x, int y) {
 		this.posX = x;
 		this.posY = y;
 	}
 
 	public int getHp() {
 		return hp;
 	}
 
 	/**Set how much Hit Points I have
 	 * Private so others use incHp instead
 	 * Because settings the HP directly, without using dmg per round,
 	 * could desync the players
 	 * @param hp
 	 */
 	private void setHp(int hp) {
 		this.hp = hp;
 	}
 	
 	/**Incremeants hp by 'delta', returns final hp after change
 	 * @param inc how much to change
 	 * @return returns current total amount of dmg to be taken this round
 	 */
 	public float incHp(float delta) {
 		return dmg += delta;
 	}
 	
 	/**Applies the damage to be taken this round. After all the skills used this round are considered.
 	 * Called by the game logic
 	 * 
 	 * @return
 	 */
 	public int applyDmg() {
 		int delta = dmg; // sets current damage to a temporary value
 		dmg = 0; // then reset the current damage
 		if (delta < 0) {
 			if (-delta < hp) {
 				return hp += delta;
 			}
 			return hp = 0;
 		}
 		else if (delta > 0) {
 			hp += delta;
 			if (hp > maxhp && !canoverheal) {
 				return hp = maxhp;
 			}
 		}
 		return hp;
 	}
 	
 	/**Update cooldowns of all action
 	 * 
 	 */
 	public void updateCooldowns() {
 		for (Action a : actionBar) a.updateCooldown();
 	}
 	
 	/**Update all that needs to be updated every logic tick
 	 * use actions, update cooldowns
 	 */
 	public void updateAll() {
 		
 		Action a = getActiveAction();
 		if (a != null) a.act(this, getTarget());
 		applyBuffs(); 
 //		applyDmg();
 		updateCooldowns();
 	}
 
 	public int getMaxhp() {
 		return maxhp;
 	}
 
 	public void setMaxBasehp(int basehp) {
 		this.basehp = basehp;
 		updateHp();
 		// hp cannot be more than maxhp, fix that
 		if (hp < maxhp) {
 			hp = maxhp;
 		}
 	}
 
 	public Texture getTex() {
 		return tex;
 	}
 
 	public void setTex(String tex) {
 		this.tex = new Texture(Gdx.files.internal(tex));
 	}
 
 	public Action getActiveAction() {
 		return getAction(getActiveActionId());
 		
 	}
 	
 	public int getActiveActionId() {
 		return activeAction;
 	}
 
 	public void setActiveActionId(int activeAction) {
 		this.activeAction = activeAction;
 	}
 	
 	/**Gets how many actions I have
 	 * @return
 	 */
 	public int getNumOfActions() {
 		return actionBar.size;
 	}
 	
 	/**gets the list of actions
 	 * 
 	 * Avoid using this unless there's no other way
 	 * 
 	 * @return
 	 */
 	public Array<Action> getActionBar() {
 		return actionBar;
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @param description the description to set
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	
 	/**
 	 * @return the description
 	 */
 	public String getFullDescription() {
 //		return description + " avgdps: " + balance.getAvgDps();
 		return description + " level " + level  + " (k: " + powerMultiplier + ") " + getActionListString();
 	}
 
 	/**
 	 * @param description the description to set
 	 */
 	public void setFullDescription(String description) {
 		this.description = description;
 	}
 	
 	/**A string with the name of every skill
 	 * @return
 	 */
 	public String getActionListString() {
 		String list = "";
 		for (Action a : actionBar) {
 			list += a.getName() + ", ";
 		}
 		if (list.length()>0) list = list.substring(0, list.length()-2);
 		return list;
 	}
 
 	/**
 	 * @return the target
 	 */
 	public Char getTarget() {
 		return target;
 	}
 
 	/**
 	 * @param target the target to set
 	 */
 	public void setTarget(Char target) {
 		this.target = target;
 	}
 	
 	public int getAiSkill(int round){
 		log(aiSkillList.toString());
 		if (aiSkillList != null && aiSkillList.size >= round){
 			
 			 return aiSkillList.get(round - 1);
 			
 			
 		}
 		else {
 			int Min = 1;
 			int Max = 4;
 			int x = Min + (int)(Math.random() * ((Max - Min) + 1));
 			if (getAction(x) == null || getAction(x).isLegal() != true)
 			{
 				do{
 					x = Min + (int)(Math.random() * ((Max - Min) + 1));
 				} while (getAction(x) == null || getAction(x).isLegal() != true);
 			}
 			return x;
 		}
 	}
 	
 	public Char getClone() {
 		try {
 			Constructor<? extends Char> constructor = this.getClass().getConstructor(String.class);
 			Object clone = constructor.newInstance(this.getName());
 			return (Char)clone;
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * @return the powerMultiplier
 	 */
 	public float getPowerMultiplier() {
 		return powerMultiplier;
 	}
 
 	/**
 	 * @param powerMultiplier the powerMultiplier to set
 	 */
 	public void setPowerMultiplier(float powerMultiplier) {
 		this.powerMultiplier = powerMultiplier;
 		updateAllActions();
 		updateHp();
 	}
 
 	/**Updates max hp with multiplier
 	 */
 	private void updateHp() {
 		float pct = ((float) hp)/((float) maxhp); // get current heal %
 		maxhp = (int) (basehp * powerMultiplier);
 		hp = (int) Math.ceil(pct*hp); // keep health % the same
 		
 	}
 
 	/**Update power of all actions, remultiply base power by power
 	 * multipliers
 	 * 
 	 */
 	private void updateAllActions() {
 		for (Action a : actionBar) {
 			a.updatePower();
 			a.update(); // updates descriptions
 		}
 	}
 	
 	/**Increases current level by 1 and updates
 	 * everything that needs it
 	 * 
 	 */
 	public void levelUp() {
 		level++;
 		setPowerMultiplier(1f + (level - 1f)*0.1f);
 		unlockSkillsForLevel(level);
 		log(name + " leveled up, level: " + level + " pmult: " + powerMultiplier);
 	}
 	
 	private void unlockSkillsForLevel(int level) {
 		if (actionUnlockedPerLevel.containsKey(level)) {
 			Action a = actionUnlockedPerLevel.get(level);
 			if (a != null) {
 				addAction(a);
 				log(this.getName() + " unlocks a new skill: " + a.getName());
 			}
 		}
 	}
 
 	public String toString() {
 		return getFullDescription();
 	}
 
 	/**
 	 * @return the actionsInventory
 	 */
 	public Array<Action> getActionsInventory() {
 		return actionsInventory;
 	}
 
 	/**Remove the action with this id from the action bar
 	 * @param selected
 	 */
 	public void removeActionFromBar(int id) {
 		actionBar.removeIndex(id);
 	}
 
 	/**Adds this action from the inventory to the action bar
 	 * @param id
 	 */
 	public void addActionFromInv(int id) {
 		if (actionBar.size >= maxActionBarSize) return;
 		Action actionToAdd = actionsInventory.get(id);
 		if (getIdOfAction(actionToAdd) != 0) return;
 		actionBar.add(actionToAdd);
 	}
 
	public void setAiSkillList(Array<Integer> x) {
 		aiSkillList.addAll(x);
 	}
 	
 	/**adds an action to be unlocked this level
 	 * 
 	 * @param a
 	 */
 	public void addActionForLevel(int level, Action a) {
 		actionUnlockedPerLevel.put(level, a);
 	}
 	
 	/**Gets the action to be unlocked on this level
 	 * @param level
 	 * @return
 	 */
 	public Action getActionForLevel(int level) {
 		return actionUnlockedPerLevel.get(level);
 	}
 
 }
