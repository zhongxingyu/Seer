 /**
  * Copyright 2012 Jami Couch
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * This project uses:
  * 
  * LibGDX
  * Copyright 2011 see LibGDX AUTHORS file
  * Licensed under Apache License, Version 2.0 (see above).
  * 
  */
 package com.ahsgaming.valleyofbones.units;
 
 import java.util.ArrayList;
 
 import com.ahsgaming.valleyofbones.*;
 import com.ahsgaming.valleyofbones.network.Command;
 import com.ahsgaming.valleyofbones.screens.LevelScreen;
 import com.ahsgaming.valleyofbones.units.Prototypes.JsonProto;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.JsonReader;
 import com.badlogic.gdx.utils.JsonValue;
 import com.badlogic.gdx.utils.ObjectMap;
 
 /**
  * @author jami
  *
  */
 public class Unit extends GameObject implements Selectable, Targetable {
 	public String LOG = "Unit";
 
 	boolean selectable = true, targetable = true;
 	
 	int attackDamage = 0, attackRange = 0, sightRange = 0;
 	float attackSpeed = 0;
 	int armor = 0, cost = 0, curHP = 0, maxHP = 0, food = 0;
 	float moveSpeed = 0;
 	int upkeep = 0, buildTime = 0;
     float splashDamage = 0;
 
     String subtype = "";
     ObjectMap<String, Float> bonus = new ObjectMap<String, Float>();
 
     String ability = "";
 
     // stealth
     int lastStealthToggleTurn = 0;
     boolean stealthActive = false;
 
     boolean capturable = false;
     Player uncontested = null;
     int capUnitCount = 0;
     boolean building = true;
     int buildTimeLeft = 0;
 	
 	int upgradeAttackDamage = 0, upgradeAttackRange = 0;
 	float upgradeAttackSpeed = 0;
 	int upgradeArmor = 0, upgradeMaxHP = 0;
     float upgradeMoveSpeed = 0;
 
     float movesLeft = 0, attacksLeft = 0;
 
     ProgressBar healthBar;
 
 	Array<String> requires = new Array<String>();
 	
 	String protoId = "";
 	String type = "";
 	String sImage = "";
 	JsonValue properties;
 	
 	ArrayList<Command> commandQueue = new ArrayList<Command>();
 	GameObject commandTarget;
 	
 	final JsonProto proto;
 	
 	Array<JsonProto> upgrades = new Array<JsonProto>();
 
     TextureRegion overlay;
 
     boolean isTurn = false;
 	
 	/**
 	 * Constructors
 	 */
 	
 	public Unit(int id, JsonProto proto) {
 		this(id, null, proto);
 	}
 	
 	public Unit(int id, Player owner, JsonProto proto) {
 		// TODO load from atlas
 		super(id, owner, VOBGame.instance.getTextureManager().getSpriteFromAtlas("assets", proto.image));
 		
 		this.proto = proto;
 		this.protoId = proto.id;
 		type = proto.type;
         sImage = proto.image;
         properties = new JsonReader().parse(proto.properties.toString());
 		parseProperties();
 
         if (getBuildTime() > 0) {
             building = true;
             buildTimeLeft = getBuildTime();
             image = VOBGame.instance.getTextureManager().getSpriteFromAtlas("assets", "unit-builder");
             overlay = VOBGame.instance.getTextureManager().getSpriteFromAtlas("assets", "unit-builder-overlay");
         } else {
             overlay = VOBGame.instance.getTextureManager().getSpriteFromAtlas("assets", proto.image + "-overlay");
             building = false;
         }
 
         healthBar = new ProgressBar();
         healthBar.setSize(getWidth(), 4f);
 	}
 	
 	public void parseProperties() {
         ability = properties.getString("ability", "");
         attackDamage = properties.getInt("attackdamage", 0);
         attackRange = properties.getInt("attackrange", 0);
         attackSpeed = properties.getFloat("attackspeed", 0);
         armor = properties.getInt("armor", 0);
         bonus.clear();
         if (properties.get("bonus") != null)
             for (JsonValue v: properties.get("bonus"))
                 bonus.put(v.name(), v.asFloat());
         buildTime = properties.getInt("buildtime", 0);
         capturable = properties.getBoolean("capturable", false);
         cost = properties.getInt("cost", 0);
         curHP = properties.getInt("curhp", 0);
         Gdx.app.log(LOG, Integer.toString(curHP));
         food = properties.getInt("food", 0);
         maxHP = properties.getInt("maxhp", 0);
         moveSpeed = properties.getFloat("movespeed", 0);
         requires.clear();
         if (properties.get("requires") != null)
             for (JsonValue v: properties.get("requires"))
                 requires.add(v.asString());
         sightRange = properties.getInt("sightrange", attackRange); // sight range defaults to attack range
         splashDamage = properties.getFloat("splashdamage", 0);
         subtype = properties.getString("subtype", "");
         upkeep = properties.getInt("upkeep", 0);
 	}
 
     @Override
     public void draw(SpriteBatch batch, float parentAlpha) {
         super.draw(batch, parentAlpha);
 
         if (overlay != null) {
             Color color = getColor();
             if (owner != null)
                 batch.setColor(color.r * owner.getPlayerColor().r, color.g * owner.getPlayerColor().g, color.b * owner.getPlayerColor().b, color.a * parentAlpha * owner.getPlayerColor().a);
             else
                 batch.setColor(color);
 
             batch.draw(overlay, getX(), getY(), getWidth() * 0.5f, getHeight() * 0.5f, getWidth(), getHeight(), 1, 1, getRotation());
         }
 
         if (healthBar != null) {
             batch.setColor(getColor());
             healthBar.setCurrent((float)curHP / maxHP);
             healthBar.draw(batch, getX(), getY() + 8, parentAlpha);
         }
 
         if (isTurn) {
             int x = 0;
             batch.setColor(getColor());
             if (getMovesLeft() > 0) {
                 TextureRegion tex = VOBGame.instance.getTextureManager().getSpriteFromAtlas("assets", "walking-boot");
 
                 batch.draw(tex, getX() + x, getY() + healthBar.getHeight() + 8, 0, 0,  tex.getRegionWidth(), tex.getRegionHeight(), 0.5f, 0.5f, getRotation());
                 x += tex.getRegionWidth() * 0.5f;
             }
 
             if (getAttacksLeft() > 0) {
                 TextureRegion tex = VOBGame.instance.getTextureManager().getSpriteFromAtlas("assets", "rune-sword");
 
                 batch.draw(tex, getX() + x, getY() + healthBar.getHeight() + 8, 0, 0,  tex.getRegionWidth(), tex.getRegionHeight(), 0.5f, 0.5f, getRotation());
                 x += tex.getRegionWidth() * 0.5f;
             }
         }
     }
 
     @Override
     public void draw(SpriteBatch batch, float offsetX, float offsetY, float parentAlpha) {
         super.draw(batch, offsetX, offsetY, parentAlpha);
 
 
         if (overlay != null) {
             Color color = getColor();
             if (owner != null)
                 batch.setColor(color.r * owner.getPlayerColor().r, color.g * owner.getPlayerColor().g, color.b * owner.getPlayerColor().b, color.a * parentAlpha * owner.getPlayerColor().a);
             else
                 batch.setColor(color);
 
             batch.draw(overlay, offsetX + getX(), offsetY + getY(), getWidth() * 0.5f, getHeight() * 0.5f, getWidth(), getHeight(), 1, 1, getRotation());
         }
 
         if (healthBar != null) {
             batch.setColor(getColor());
             healthBar.setCurrent((float)curHP / maxHP);
             healthBar.draw(batch, offsetX + getX(), offsetY + getY() + 8, parentAlpha);
 //            batch.draw(new TextureRegion(healthBar.img), offsetX + getX(), offsetY + getY() + 8, getWidth(), 8);
         }
 //
         if (isTurn) {
             int x = 0;
             batch.setColor(getColor());
             if (getMovesLeft() > 0) {
                 TextureRegion tex = VOBGame.instance.getTextureManager().getSpriteFromAtlas("assets", "walking-boot");
 
                 batch.draw(tex, offsetX + getX() + x, offsetY + getY() + healthBar.getHeight() + 8, 0, 0,  tex.getRegionWidth(), tex.getRegionHeight(), 0.5f, 0.5f, getRotation());
                 x += tex.getRegionWidth() * 0.5f;
             }
 
             if (getAttacksLeft() > 0) {
                 TextureRegion tex = VOBGame.instance.getTextureManager().getSpriteFromAtlas("assets", "rune-sword");
 
                 batch.draw(tex, offsetX + getX() + x, offsetY + getY() + healthBar.getHeight() + 8, 0, 0,  tex.getRegionWidth(), tex.getRegionHeight(), 0.5f, 0.5f, getRotation());
                 x += tex.getRegionWidth() * 0.5f;
             }
         }
     }
 
     public void updateProperties() {
         // TODO reimplement this with the new JSON API
 //		properties.put("attackdamage", attackDamage);
 //		properties.put("attackrange", attackRange);
 //		properties.put("attackspeed", attackSpeed);
 //		properties.put("armor", armor);
 //        properties.put("bonus", bonus);
 //		properties.put("cost", cost);
 //        properties.put("capturable", capturable);
 //		properties.put("curhp", curHP);
 //		properties.put("food", food);
 //		properties.put("maxhp", maxHP);
 //		properties.put("movespeed", moveSpeed);
 //		properties.put("requires", requires);
 //        properties.put("subtype", subtype);
 //		properties.put("upkeep", upkeep);
 //        properties.put("ability", ability);
 	}
 	
 	public void parseUpgrades() {
 		upgradeAttackDamage = 0;
 		upgradeAttackRange = 0;
 		upgradeAttackSpeed = 0;
 		upgradeArmor = 0;
 		upgradeMaxHP = 0;
 		upgradeMoveSpeed = 0;
 		
 		for (JsonProto up: upgrades) {
 			if (up.hasProperty("attackdamage"))
 				upgradeAttackDamage += (int)Float.parseFloat(up.getProperty("attackdamage").toString());
 			
 			if (up.hasProperty("attackrange"))
 				upgradeAttackRange += (int)Float.parseFloat(up.getProperty("attackrange").toString());
 			
 			if (up.hasProperty("attackspeed"))
 				upgradeAttackSpeed += Float.parseFloat(up.getProperty("attackspeed").toString());
 			
 			if (up.hasProperty("armor"))
 				upgradeArmor += (int)Float.parseFloat(up.getProperty("armor").toString());
 			
 			if (up.hasProperty("maxhp"))
 				upgradeMaxHP += (int)Float.parseFloat(up.getProperty("maxhp").toString());
 			
 			if (up.hasProperty("movespeed"))
 				upgradeMoveSpeed += (int)Float.parseFloat(up.getProperty("movespeed").toString());
 		}
 	}
 	
 	@Override
 	public float takeDamage(float amount) {
 		// TODO take into account damage type here
 		float damage = amount - getArmor();
 		if (damage > 0) {
 			curHP -= damage;
 
             if (LevelScreen.getInstance() != null)
                 LevelScreen.getInstance().addFloatingLabel(String.format("-%d", (int)damage), getX() + getWidth() * 0.5f, getY() + getHeight() * 0.5f);
 
 			return damage;
 		}
 		// TODO add a hit effect
 		return 0;
 	}
 	
 	public int getAttackDamage() {
 		return attackDamage + upgradeAttackDamage;
 	}
 
 	public void setAttackDamage(int attackDamage) {
 		this.attackDamage = attackDamage;
 	}
 	
 	public int getAttackRange() {
 		return attackRange + upgradeAttackRange;
 	}
 
 	public void setAttackRange(int attackRange) {
 		this.attackRange = attackRange;
 	}
 
 	public float getAttackSpeed() {
 		return attackSpeed + upgradeAttackSpeed;
 	}
 
 	public void setAttackSpeed(float attackSpeed) {
 		this.attackSpeed = attackSpeed;
 	}
 
 	public int getArmor() {
 		return armor + upgradeArmor;
 	}
 
 	public void setArmor(int armor) {
 		this.armor = armor;
 	}
 
     public float getBonus(String subtype) {
         if (bonus.containsKey(subtype))
             return bonus.get(subtype);
         return 1;
     }
 
     public void setBonus(String subtype, float bonus) {
         this.bonus.put(subtype, bonus);
     }
 
     public int getBuildTime() {
         return buildTime;
     }
 
     public void setBuildTime(int buildTime) {
         this.buildTime = buildTime;
     }
 
     public int getCost() {
 		return cost;
 	}
 	
 	public void setCost(int cost) {
 		this.cost = cost;
 	}
 	
 	public int getCurHP() {
 		return curHP;
 	}
 
 	public void setCurHP(int curHP) {
 		this.curHP = curHP;
         if (this.curHP > getMaxHP()) this.curHP = getMaxHP();
 	}
 	
 	public int getFood() {
 		return food;
 	}
 	
 	public void setFood(int food) {
 		this.food = food;
 	}
 
 	public int getMaxHP() {
 		return maxHP + upgradeMaxHP;
 	}
 
 	public void setMaxHP(int maxHP) {
 		this.maxHP = maxHP;
 	}
 
 	public float getMoveSpeed() {
 		return moveSpeed + upgradeMoveSpeed;
 	}
 
 	public void setMoveSpeed(float moveSpeed) {
 		this.moveSpeed = moveSpeed;
 	}
 
 	public Array<String> getRequires() {
 		return requires;
 	}
 
 	public void setRequires(Array<String> requires) {
 		this.requires = requires;
 	}
 
     public int getSightRange() {
         return sightRange;
     }
 
     public void setSightRange(int sightRange) {
         this.sightRange = sightRange;
     }
 
     public float getSplashDamage() {
         return splashDamage;
     }
 
     public void setSplashDamage(float splashDamage) {
         this.splashDamage = splashDamage;
     }
 
     public String getSubtype() {
         return subtype;
     }
 
     public void setSubtype(String subtype) {
         this.subtype = subtype;
     }
 
     public String getTitle() {
         return proto.title;
     }
 
     public void setTitle(String title) {
         proto.title = title;
     }
 
     public String getType() {
         return type;
     }
 
     public void getType(String type) {
         this.type = type;
     }
 
     public int getUpkeep() {
         return upkeep;
     }
 
     public void setUpkeep(int upkeep) {
         this.upkeep = upkeep;
     }
 
 	public JsonValue getProperties() {
 		return properties;
 	}
 
 	public void setProperties(JsonValue properties) {
 		this.properties = properties;
 	}
 	
 	public void applyUpgrade(JsonProto upgrade) {
 		if (!hasUpgrade(upgrade.id)) {
 			upgrades.add(upgrade);
 			parseUpgrades();
 		}
 	}
 	
 	public boolean hasUpgrade(String upgradeId) {
 		for (JsonProto up: upgrades) {
 			if (up.id.equals(upgradeId)) return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.ahsgaming.spacetactics.GameObject#update(com.ahsgaming.spacetactics.GameController, float)
 	 */
 	@Override
 	public void update(GameController controller) {
 		if (capturable)
             findNewOwner(controller);
 
         if (getCurHP() <= 0) {
 			if (capturable) {
                 setCurHP(0);
                 if (owner != uncontested)
                     controller.getMap().setMapDirty(true);
                 setOwner(uncontested);
                 attacksLeft = 0; // hopefully this fixes the bug where capturing a tower that had an attack allows you to attack with it
                 movesLeft = 0;
             } else {
                 // remove self
                 remove = true;
                 // TODO add explosion anim or something
                 return;
             }
 		}
 
         isTurn = (getOwner() != null && controller.getCurrentPlayer().getPlayerId() == getOwner().getPlayerId());
 	}
 
     public void findNewOwner(GameController controller) {
         Player p = null;
         capUnitCount = 0;
         for (Unit unit: controller.getUnitsInArea(boardPos, 1)) {
             if (unit != this && unit.getOwner() != null && !unit.isBuilding()) {
                 if (p == null) {
                     p = unit.getOwner();
                     capUnitCount ++;
                 } else if (p != unit.getOwner()) {
                     uncontested = null;
                     capUnitCount = 0;
                     return;
                 } else {
                     capUnitCount ++;
                 }
             }
         }
         uncontested = p;
     }
 
 	public ArrayList<Command> getCommandQueue() {
 		return commandQueue;
 	}
 	
 	public void doCommand(Command cmd, boolean add) {
 		if (!add) commandQueue.clear();
 		commandQueue.add(cmd);
 	}
 	
 	public GameObject getTarget() {
 		if (commandTarget != null && (commandTarget.getOwner() != null && commandTarget.getOwner().getPlayerId() == getOwner().getPlayerId())) {
 			return null;
 		}
 		return commandTarget;
 	}
 	
 	public String getProtoId() {
 		return protoId;
 	}
 	
 	public boolean isAlive() {
 		return (!this.isRemove() && this.curHP > 0);
 	}
 
     public void startTurn() {
         if (!building) {
             movesLeft = (movesLeft % 1) + (getMoveSpeed() * (stealthActive ? 0.5f : 1f));
             attacksLeft = (attacksLeft % 1) + getAttackSpeed();
         }
 
         if (capturable && uncontested == getOwner()) {
             setCurHP(getCurHP() + 5 * capUnitCount);
         }
     }
 
     public void endTurn() {
         if (building) {
             buildTimeLeft -= 1;
 
             if (buildTimeLeft <= 0) {
                 building = false;
                 image = VOBGame.instance.getTextureManager().getSpriteFromAtlas("assets", proto.image);
                 overlay = VOBGame.instance.getTextureManager().getSpriteFromAtlas("assets", proto.image + "-overlay");
             }
         }
     }
 
     public int getMovesLeft() {
         return (int)movesLeft;
     }
 
     public boolean canMove(Vector2 location, GameController controller) {
         return (controller.getMap().getMapDist(getBoardPosition(), location) <= movesLeft);
     }
 
     public void move(Vector2 location, GameController controller) {
         int dist = controller.getMap().getMapDist(getBoardPosition(), location);
         if (canMove(location, controller)) {
             movesLeft -= dist;
 
             setBoardPosition(location);
             setPosition(controller.getMap().boardToMapCoords(location.x, location.y));
         }
     }
 
     public int getAttacksLeft() {
         return (int)attacksLeft;
     }
 
     public boolean canAttack(Unit other, GameController controller) {
         return (attacksLeft >= 1
                 && controller.getMap().getMapDist(getBoardPosition(), other.getBoardPosition()) <= getAttackRange()
                 && controller.getMap().isBoardPositionVisible(other.getBoardPosition()));
     }
 
    public boolean attack(Unit other, GameController controller) {
         if (canAttack(other, controller)) {
             attacksLeft--;
             Gdx.app.log(LOG + String.format(" (%d)", this.getObjId()), String.format("Attacking (%d) for %d", other.getObjId(), getAttackDamage()));
             float damage = other.takeDamage(getAttackDamage() * getBonus(other.getSubtype()));
 
             if (stealthActive) activateAbility(controller);
            return true;
         }
        return false;
     }
 
     public int getRefund() {
         return (int)(cost * 0.5f * ((float)getCurHP() / (float)getMaxHP()));
     }
 
     public void activateAbility(GameController controller) {
         if (ability.equals("stealth")) {
             if (lastStealthToggleTurn == controller.getGameTurn() && !stealthActive) return; // cannot toggle again on the same turn
 
             stealthActive = !stealthActive;
 
             lastStealthToggleTurn = controller.getGameTurn();
 
             if (stealthActive) {
                 int totalMoves = (int)(getMoveSpeed() / 2);
                 int usedMoves = (int)(getMoveSpeed() - getMovesLeft());
                 movesLeft = totalMoves - usedMoves;
                 if (movesLeft < 0) movesLeft = 0;
             } else {
                 int usedMoves = (int)(getMoveSpeed() / 2) - getMovesLeft();
                 movesLeft = (int)(getMoveSpeed() - usedMoves);
             }
         }
     }
 
     public boolean getInvisible() {
         return stealthActive;
     }
 
     public void setInvisible(boolean invisible) {
         this.stealthActive = invisible;
     }
 
     public boolean isDetector() {
         return ability.equals("detect");
     }
 
     public boolean isBuilding() {
         return building;
     }
 
     public void setBuilding(boolean building) {
         this.building = building;
         if (building) this.buildTimeLeft = getBuildTime();
     }
 	
 	//-------------------------------------------------------------------------
 	// Implemented methods
 	//-------------------------------------------------------------------------
 
 	@Override
 	public boolean isTargetable() {
 		return this.targetable;
 	}
 
 	@Override
 	public void setTargetable(boolean targetable) {
 		this.targetable = targetable;
 		
 	}
 
 	@Override
 	public boolean isSelectable() {
 		return selectable;
 	}
 
 	@Override
 	public void setSelectable(boolean selectable) {
 		this.selectable = selectable;
 	}
 }
