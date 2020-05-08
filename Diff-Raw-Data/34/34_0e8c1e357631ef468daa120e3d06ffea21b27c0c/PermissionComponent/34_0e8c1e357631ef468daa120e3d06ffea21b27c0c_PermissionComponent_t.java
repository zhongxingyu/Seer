 package net.croxis.plugins.civilmineation.components;
 
 import javax.persistence.Entity;
import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 @Entity()
 @Table(name = "civ_permissions")
 public class PermissionComponent{
 	
 	@Id
 	private int id;
	@OneToOne(fetch = FetchType.LAZY)
 	private Ent entityID;
 	private String name;
 	public boolean residentBuild, residentDestroy, residentSwitch, residentItemUse,
 	outsiderBuild, outsiderDestroy, outsiderSwitch, outsiderItemUse,
 	allyBuild, allyDestroy, allySwitch, allyItemUse;
 public boolean pvp, fire, explosion, mobs;
 private boolean enforceChildren;
 
 public void reset() {
 	setAll(false);
 }
 
 public void setAll(boolean b) {
 	residentBuild = b;
 	residentDestroy = b;
 	residentSwitch = b;
 	residentItemUse = b;
 	outsiderBuild = b;
 	outsiderDestroy = b;
 	outsiderSwitch = b;
 	outsiderItemUse = b;
 	allyBuild = b;
 	allyDestroy = b;
 	allySwitch = b;
 	allyItemUse = b;
 	
 	pvp = b;
 	fire = b;
 	explosion = b;
 	mobs = b;
 }
 
 public void set(String s, boolean b) {
 	
 	if (s.equalsIgnoreCase("denyAll"))
 		reset();
 	else if (s.equalsIgnoreCase("residentBuild"))
 		residentBuild = b;
 	else if (s.equalsIgnoreCase("residentDestroy"))
 		residentDestroy = b;
 	else if (s.equalsIgnoreCase("residentSwitch"))
 		residentSwitch = b;
 	else if (s.equalsIgnoreCase("residentItemUse"))
 		residentItemUse = b;
 	else if (s.equalsIgnoreCase("outsiderBuild"))
 		outsiderBuild = b;
 	else if (s.equalsIgnoreCase("outsiderDestroy"))
 		outsiderDestroy = b;
 	else if (s.equalsIgnoreCase("outsiderSwitch"))
 		outsiderSwitch = b;
 	else if (s.equalsIgnoreCase("outsiderItemUse"))
 		outsiderItemUse = b;
 	else if (s.equalsIgnoreCase("allyBuild"))
 		allyBuild = b;
 	else if (s.equalsIgnoreCase("allyDestroy"))
 		allyDestroy = b;
 	else if (s.equalsIgnoreCase("allySwitch"))
 		allySwitch = b;
 	else if (s.equalsIgnoreCase("allyItemUse"))
 		allyItemUse = b;
 	else if (s.equalsIgnoreCase("pvp"))
 		pvp = b;
 	else if (s.equalsIgnoreCase("fire"))
 		fire = b;
 	else if (s.equalsIgnoreCase("explosion"))
 		explosion = b;
 	else if (s.equalsIgnoreCase("mobs"))
 		mobs = b;
 }
 
 
 @Override
 public String toString() {
 	String out = "";
 	if (residentBuild)
 		out += "residentBuild";
 	if (residentDestroy)
 		out += (out.length() > 0 ? "," : "") + "residentDestroy";
 	if (residentSwitch)
 		out += (out.length() > 0 ? "," : "") + "residentSwitch";
 	if (residentItemUse)
 		out += (out.length() > 0 ? "," : "") + "residentItemUse";
 	if (outsiderBuild)
 		out += (out.length() > 0 ? "," : "") + "outsiderBuild";
 	if (outsiderDestroy)
 		out += (out.length() > 0 ? "," : "") + "outsiderDestroy";
 	if (outsiderSwitch)
 		out += (out.length() > 0 ? "," : "") + "outsiderSwitch";
 	if (outsiderItemUse)
 		out += (out.length() > 0 ? "," : "") + "outsiderItemUse";
 	if (allyBuild)
 		out += (out.length() > 0 ? "," : "") + "allyBuild";
 	if (allyDestroy)
 		out += (out.length() > 0 ? "," : "") + "allyDestroy";
 	if (allySwitch)
 		out += (out.length() > 0 ? "," : "") + "allySwitch";
 	if (allyItemUse)
 		out += (out.length() > 0 ? "," : "") + "allyItemUse";
 	if (pvp)
 		out += (out.length() > 0 ? "," : "") + "pvp";
 	if (fire)
 		out += (out.length() > 0 ? "," : "") + "fire";
 	if (explosion)
 		out += (out.length() > 0 ? "," : "") + "explosion";
 	if (mobs)
 		out += (out.length() > 0 ? "," : "") + "mobs";
 	if (out.length() == 0)
 		out += "denyAll"; // Make the token not empty
 	return out;
 }
 
 public enum ActionType {
 	BUILD,
 	DESTROY,
 	SWITCH,
 	ITEM_USE;
 	
 	@Override
 	public String toString() {
 		return super.toString().toLowerCase();
 	}
 };
 
 public enum PermLevel {
 	RESIDENT,
 	ALLY,
 	OUTSIDER;
 	
 	@Override
 	public String toString() {
 		return super.toString().toLowerCase();
 	}
 };
 
 public boolean getResidentPerm(ActionType type) {
 	switch (type) {
 		case BUILD: return residentBuild;
 		case DESTROY: return residentDestroy;
 		case SWITCH: return residentSwitch;
 		case ITEM_USE: return residentItemUse;
 		default: throw new UnsupportedOperationException();
 	}
 }
 
 public boolean getOutsiderPerm(ActionType type) {
 	switch (type) {
 		case BUILD: return outsiderBuild;
 		case DESTROY: return outsiderDestroy;
 		case SWITCH: return outsiderSwitch;
 		case ITEM_USE: return outsiderItemUse;
 		default: throw new UnsupportedOperationException();
 	}
 }
 
 public boolean getAllyPerm(ActionType type) {
 	switch (type) {
 		case BUILD: return allyBuild;
 		case DESTROY: return allyDestroy;
 		case SWITCH: return allySwitch;
 		case ITEM_USE: return allyItemUse;
 		default: throw new UnsupportedOperationException();
 	}
 }
 
 public boolean getPerm(String s){
 	if (s.equalsIgnoreCase("residentBuild"))
 	return residentBuild;
 	else if (s.equalsIgnoreCase("residentDestroy"))
 	return residentDestroy;
 	else if (s.equalsIgnoreCase("residentSwitch"))
 	return residentSwitch;
 	else if (s.equalsIgnoreCase("residentItemUse"))
 	return residentItemUse;
 
 	else if (s.equalsIgnoreCase("outsiderBuild"))
 	return outsiderBuild;
 	else if (s.equalsIgnoreCase("outsiderDestroy"))
 	return outsiderDestroy;
 	else if (s.equalsIgnoreCase("outsiderSwitch"))
 	return outsiderSwitch;
 	else if (s.equalsIgnoreCase("outsiderItemUse"))
 	return outsiderItemUse;
 
 	else if (s.equalsIgnoreCase("allyBuild"))
 	return allyBuild;
 	else if (s.equalsIgnoreCase("allyDestroy"))
 	return allyDestroy;
 	else if (s.equalsIgnoreCase("allySwitch"))
 	return allySwitch;
 	else if (s.equalsIgnoreCase("allyItemUse"))
 	return allyItemUse;
 	return false;
 	}
 
 public boolean isResidentBuild() {
 	return residentBuild;
 }
 
 public void setResidentBuild(boolean residentBuild) {
 	this.residentBuild = residentBuild;
 }
 
 public boolean isResidentDestroy() {
 	return residentDestroy;
 }
 
 public void setResidentDestroy(boolean residentDestroy) {
 	this.residentDestroy = residentDestroy;
 }
 
 public boolean isResidentSwitch() {
 	return residentSwitch;
 }
 
 public void setResidentSwitch(boolean residentSwitch) {
 	this.residentSwitch = residentSwitch;
 }
 
 public boolean isResidentItemUse() {
 	return residentItemUse;
 }
 
 public void setResidentItemUse(boolean residentItemUse) {
 	this.residentItemUse = residentItemUse;
 }
 
 public boolean isOutsiderBuild() {
 	return outsiderBuild;
 }
 
 public void setOutsiderBuild(boolean outsiderBuild) {
 	this.outsiderBuild = outsiderBuild;
 }
 
 public boolean isOutsiderDestroy() {
 	return outsiderDestroy;
 }
 
 public void setOutsiderDestroy(boolean outsiderDestroy) {
 	this.outsiderDestroy = outsiderDestroy;
 }
 
 public boolean isOutsiderSwitch() {
 	return outsiderSwitch;
 }
 
 public void setOutsiderSwitch(boolean outsiderSwitch) {
 	this.outsiderSwitch = outsiderSwitch;
 }
 
 public boolean isOutsiderItemUse() {
 	return outsiderItemUse;
 }
 
 public void setOutsiderItemUse(boolean outsiderItemUse) {
 	this.outsiderItemUse = outsiderItemUse;
 }
 
 public boolean isAllyBuild() {
 	return allyBuild;
 }
 
 public void setAllyBuild(boolean allyBuild) {
 	this.allyBuild = allyBuild;
 }
 
 public boolean isAllyDestroy() {
 	return allyDestroy;
 }
 
 public void setAllyDestroy(boolean allyDestroy) {
 	this.allyDestroy = allyDestroy;
 }
 
 public boolean isAllySwitch() {
 	return allySwitch;
 }
 
 public void setAllySwitch(boolean allySwitch) {
 	this.allySwitch = allySwitch;
 }
 
 public boolean isAllyItemUse() {
 	return allyItemUse;
 }
 
 public void setAllyItemUse(boolean allyItemUse) {
 	this.allyItemUse = allyItemUse;
 }
 
 public boolean isPvp() {
 	return pvp;
 }
 
 public void setPvp(boolean pvp) {
 	this.pvp = pvp;
 }
 
 public boolean isFire() {
 	return fire;
 }
 
 public void setFire(boolean fire) {
 	this.fire = fire;
 }
 
 public boolean isExplosion() {
 	return explosion;
 }
 
 public void setExplosion(boolean explosion) {
 	this.explosion = explosion;
 }
 
 public boolean isMobs() {
 	return mobs;
 }
 
 public void setMobs(boolean mobs) {
 	this.mobs = mobs;
 }
 
 public boolean isEnforceChildren() {
 	return enforceChildren;
 }
 
 public void setEnforceChildren(boolean enforceChildren) {
 	this.enforceChildren = enforceChildren;
 }
 
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public Ent getEntityID() {
 		return entityID;
 	}
 	public void setEntityID(Ent entityID) {
 		this.entityID = entityID;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 
 }
