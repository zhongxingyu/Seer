 /*
    Copyright 2011 kanata3249
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package com.github.kanata3249.ffxieq;
 
 import java.io.Serializable;
 
 import com.github.kanata3249.ffxi.FFXIDAO;
 import com.github.kanata3249.ffxi.status.*;
 
 public class FFXICharacter implements IStatus, Serializable {
 	private static final long serialVersionUID = 1L;
 
 	JobLevelAndRace mLevel;
 
 	EquipmentSet mEquipment;
 	JobAndRace mJobAndRace;
 	MeritPoint mMerits;
 	boolean mInAbyssea;
 	AtmaSet mAtmaset;
 	// TODO food
 
 	transient boolean mModified;
 	transient boolean mStatusCacheValid;
 	transient private StatusValue mCachedValues[];
 
 	static FFXIDAO Dao;
 	
 	public FFXICharacter() {
 		mLevel = new JobLevelAndRace(JobLevelAndRace.Hum, JobLevelAndRace.WAR, JobLevelAndRace.THF, 1, 0);
 		mJobAndRace = new JobAndRace();
 		mEquipment = new EquipmentSet();
 		mMerits = new MeritPoint();
 		mAtmaset = new AtmaSet();
 		mInAbyssea = false;
 		mModified = false;
 		mStatusCacheValid = false;
 	}
 
 	public static FFXIDAO getDao() {
 		return Dao;
 	}
 	
 	public static void setDao(FFXIDAO dao) {
 		Dao = dao;
 	}
 
 	// IStatus
 	public StatusValue getStatus(JobLevelAndRace level, StatusType type) {
 		StatusValue total = new StatusValue(0, 0);
 		total.add(mJobAndRace.getStatus(level, type));
 		total.add(mMerits.getStatus(level, type));
 		total.add(mEquipment.getStatus(level, type));
 		if (mInAbyssea) {
 			total.add(mAtmaset.getStatus(level, type));
 		}
 		
 		return total;
 	}
 	
 	public int getRace() {
 		return mLevel.getRace();
 	}
 	public void setRace(int race) {
 		mModified = mModified || (mLevel.getRace() != race);
 		mStatusCacheValid = (mLevel.getRace() == race) && mStatusCacheValid;
 		mLevel.setRace(race);
 	}
 	public int getJob() {
 		return mLevel.getJob();
 	}
 	public void setJob(int job) {
 		mModified = mModified || (mLevel.getJob() != job);
 		mStatusCacheValid = (mLevel.getJob() == job) && mStatusCacheValid;
 		mLevel.setJob(job);
 	}
 	public int getJobLevel() {
 		return ((Integer)mLevel.getLevel());
 	}
 	public void setJobLevel(int level) {
 		mModified = mModified || (mLevel.getLevel() != level);
 		mStatusCacheValid = (mLevel.getLevel() == level) && mStatusCacheValid;
 		mLevel.setLevel(level);
 	}
 	public int getSubJob() {
 		return mLevel.getSubJob();
 	}
 	public void setSubJob(int subjob) {
 		mModified = mModified || (mLevel.getSubJob() != subjob);
 		mStatusCacheValid = (mLevel.getSubJob() == subjob) && mStatusCacheValid;
 		mLevel.setSubJob(subjob);
 	}
 	public int getSubJobLevel() {
 		return ((Integer)mLevel.getSubLevel());
 	}
 	public void setSubJobLevel(int sublevel) {
 		mModified = mModified || (mLevel.getSubLevel() != sublevel);
 		mStatusCacheValid = (mLevel.getSubLevel() == sublevel) && mStatusCacheValid;
 		mLevel.setSubLevel(sublevel);
 	}
 	public JobAndRace getJobAndRace() {
 		return mJobAndRace;
 	}
 	public void setJobAndRace(JobAndRace jobandrace) {
 		mModified = true;
 		mStatusCacheValid = false;
 		mJobAndRace = jobandrace;
 	}
 	public Equipment getEquipment(int part) {
 		return mEquipment.getEquipment(part);
 	}
 	public void setEquipment(int part, long id) {
 		mModified = true;
 		mStatusCacheValid = false;
 		mEquipment.setEquipment(part, id);
 	}
 	public MeritPoint getMeritPoint() {
 		return mMerits;
 	}
 	public void setMeritPoint(MeritPoint merits) {
 		mModified = true;
 		mStatusCacheValid = false;
 		mMerits = merits;
 	}
 	public boolean isInAbbysea() {
 		return mInAbyssea;
 	}
 	public void setInAbysea(boolean inAbbysea) {
 		mModified = mModified || (mInAbyssea != inAbbysea);
 		mStatusCacheValid = (mInAbyssea == inAbbysea) && mStatusCacheValid;
 		this.mInAbyssea = inAbbysea;
 	}
 	public void setAbyssiteOfFurtherance(int n) {
 		mModified = mModified || (mAtmaset.getAbyssiteOfFurtherance() != n);
 		mStatusCacheValid = (mAtmaset.getAbyssiteOfFurtherance() == n) && mStatusCacheValid;
 		mAtmaset.setAbyssiteOfFurtherance(n);
 	}
 	public void setAbyssiteOfMerit(int n) {
 		mModified = mModified || (mAtmaset.getAbyssiteOfMerit() != n);
 		mStatusCacheValid = (mAtmaset.getAbyssiteOfMerit() == n) && mStatusCacheValid;
 		mAtmaset.setAbyssiteOfMerit(n);
 	}
 	public int getAbyssiteOfFurtherance() {
 		return mAtmaset.getAbyssiteOfFurtherance();
 	}
 	public int getAbyssiteOfMerit() {
 		return mAtmaset.getAbyssiteOfMerit();
 	}
 	public Atma getAtma(int index) {
 		return mAtmaset.getAtma(index);
 	}
 	public void setAtma(int index, long id) {
 		mModified = true;
 		mStatusCacheValid = false;
 		mAtmaset.setAtma(index, id);
 	}
 	public boolean isModified() {
 		return mModified;
 	}
 	public void setNotModified() {
 		mModified = false;
 	}
 	public boolean isCacheValid() {
 		return mStatusCacheValid;
 	}
 
 	public void cacheStatusValues() {
 		if (mStatusCacheValid && mCachedValues != null) {
 			return;
 		}
 
 		mStatusCacheValid = false;
 		mCachedValues = new StatusValue[StatusType.MODIFIER_NUM.ordinal()];
 		
 		StatusType types[] = StatusType.values();
 
 		for (int i = 0; i < types.length; i++) {
 			StatusType type = types[i];
 
 			switch (type) {
 			case HP:
 			case MP:
 			case STR:
 			case DEX:
 			case VIT:
 			case AGI:
 			case INT:
 			case MND:
 			case CHR:
 			case DSub:
 			case DRange:
 			case DelayRange:
 			case Haste:
 			case Slow:
 			case AttackMagic:
 			case AccuracyMagic:
 			case DefenceMagic:
 			case Regist_Fire:
 			case Regist_Ice:
 			case Regist_Wind:
 			case Regist_Earth:
 			case Regist_Lightning:
 			case Regist_Water:
 			case Regist_Light:
 			case Regist_Dark:
 			case CriticalRate:
 			case CriticalDamage:
 			case CriticalRateDefence:
 			case CriticalDamageDefence:
 			case SubtleBlow:
 			case StoreTP:
 			case Enmity:
 			case MagicEvasion:
 			case HealingHP:
 			case HealingMP:
 			case DualWield:
 			case MartialArts:
 			case DoubleAttack:
 			case TrippleAttack:
 			case QuadAttack:
 			case DamageCut:
 			case Counter:
 			case SpellInterruptionRate:
 				mCachedValues[type.ordinal()] = getStatus(mLevel, type);
 				break;
 
 			case D:
 				mCachedValues[type.ordinal()] = getD();
 				break;
 			case Delay:
 				mCachedValues[type.ordinal()] = getDelay();
 				break;
 			case DelaySub:
 				mCachedValues[type.ordinal()] = getDelaySub();
 				break;
 			case Accuracy:
 				mCachedValues[type.ordinal()] = getAccuracy();
 				break;
 			case AccuracySub:
 				mCachedValues[type.ordinal()] = getAccuracySub();
 				break;
 			case Attack:
 				mCachedValues[type.ordinal()] = getAttack();
 				break;
 			case AttackSub:
 				mCachedValues[type.ordinal()] = getAttackSub();
 				break;
 			case AccuracyRange:
 				mCachedValues[type.ordinal()] = getAccuracyRange();
 				break;
 			case AttackRange:
 				mCachedValues[type.ordinal()] = getAttackRange();
 				break;
 			case Evasion:
 				mCachedValues[type.ordinal()] = getEvasion();
 				break;
 			case Defence:
 				mCachedValues[type.ordinal()] = getDefence();
 				break;
 			case DamageCutPhysical:
 				mCachedValues[type.ordinal()] = getDamageCutPhysical();
 				break;
 			case DamageCutMagic:
 				mCachedValues[type.ordinal()] = getDamageCutMagic();
 				break;
 			case DamageCutBreath:
 				mCachedValues[type.ordinal()] = getDamageCutBreath();
 				break;
 			case MODIFIER_NUM:
 				break;
 			default:
 				mCachedValues[type.ordinal()] = new StatusValue(0, 0, 0);
 			}
 		}
 		mStatusCacheValid = true;
 	}
 
 	public StatusValue getStatus(StatusType type) {
 		if (mStatusCacheValid && mCachedValues != null) {
 			return mCachedValues[type.ordinal()];
 		}
 		
 		switch (type) {
 		case HP:
 		case MP:
 		case STR:
 		case DEX:
 		case VIT:
 		case AGI:
 		case INT:
 		case MND:
 		case CHR:
 		case DSub:
 		case DRange:
 		case DelayRange:
 		case Haste:
 		case Slow:
 		case AttackMagic:
 		case AccuracyMagic:
 		case DefenceMagic:
 		case Regist_Fire:
 		case Regist_Ice:
 		case Regist_Wind:
 		case Regist_Earth:
 		case Regist_Lightning:
 		case Regist_Water:
 		case Regist_Light:
 		case Regist_Dark:
 		case CriticalRate:
 		case CriticalDamage:
 		case CriticalRateDefence:
 		case CriticalDamageDefence:
 		case SubtleBlow:
 		case StoreTP:
 		case Enmity:
 		case MagicEvasion:
 		case HealingHP:
 		case HealingMP:
 		case DualWield:
 		case MartialArts:
 		case DoubleAttack:
 		case TrippleAttack:
 		case QuadAttack:
 		case DamageCut:
 		case Counter:
 		case SpellInterruptionRate:
 			return getStatus(mLevel, type);
 
 		case D:
 			return getD();
 		case Delay:
 			return getDelay();
 		case DelaySub:
 			return getDelaySub();
 		case Accuracy:
 			return getAccuracy();
 		case AccuracySub:
 			return getAccuracySub();
 		case Attack:
 			return getAttack();
 		case AttackSub:
 			return getAttackSub();
 		case AccuracyRange:
 			return getAccuracyRange();
 		case AttackRange:
 			return getAttackRange();
 		case Evasion:
 			return getEvasion();
 		case Defence:
 			return getDefence();
 		case DamageCutPhysical:
 			return getDamageCutPhysical();
 		case DamageCutMagic:
 			return getDamageCutMagic();
 		case DamageCutBreath:
 			return getDamageCutBreath();
 		}
 		return null;
 	}
 
 	public StatusValue getD() {
 		StatusType type;
 		Equipment eq;
 
 		eq = mEquipment.getEquipment(EquipmentSet.MAINWEAPON);
 		if (eq != null) {
 			type = eq.getWeaponType();
 		} else {
 			type = StatusType.SKILL_HANDTOHAND;
 		}
 		if (type == StatusType.SKILL_HANDTOHAND) {
 			StatusValue value;
 			StatusValue skill;
 			int D;
 			
 			skill = getStatus(mLevel, type);
 			D = (skill.getValue() + skill.getAdditional()) * 11 / 100 + 3;
 			value = new StatusValue(D, 0, 0);
 			
 			value.add(getStatus(mLevel, StatusType.D));
 			return value;
 		} else {
 			return getStatus(mLevel, StatusType.D);
 		}
 	}
 
 	public StatusValue getDelay() {
 		StatusType type, subtype;
 		Equipment eq;
 
 		type = subtype = null;
 		eq = mEquipment.getEquipment(EquipmentSet.MAINWEAPON);
 		if (eq != null) {
 			type = eq.getWeaponType();
 		}
 		eq = mEquipment.getEquipment(EquipmentSet.SUBWEAPON);
 		if (eq != null) {
 			subtype = eq.getWeaponType();
 		}
 		if (type == null) {
 			type = StatusType.SKILL_HANDTOHAND;
 		}
 		switch (type) {
 		case SKILL_HANDTOHAND:
 			{	// Martial Arts
 				StatusValue base = getStatus(mLevel, StatusType.Delay);
 				StatusValue martialarts = getStatus(mLevel, StatusType.MartialArts);
 				int rank = martialarts.getValue();
 				
 				if (rank > 0) {
 					base.setValue(400 - 20 * (rank - 1));
 				} else {
 					base.setValue(480);
 				}
 				return base;
 			}
 
 		case SKILL_DAGGER:
 		case SKILL_SWORD:
 		case SKILL_AXE:
 		case SKILL_KATANA:
 		case SKILL_CLUB:
 			if (subtype != null) {
 				switch (subtype) {
 				case SKILL_DAGGER:
 				case SKILL_SWORD:
 				case SKILL_AXE:
 				case SKILL_KATANA:
 				case SKILL_CLUB:
 					{
 						// Dual Wield
 						StatusValue base = getStatus(mLevel, StatusType.Delay);
 						StatusValue dualwield = getStatus(mLevel, StatusType.DualWield);
 						base.setAdditionalPercent(-(dualwield.getAdditional() + dualwield.getAdditionalPercent()));
 						
 						return base;
 					}
 				}
 			}
 		}
 		return getStatus(mLevel, StatusType.Delay);
 	}
 	public StatusValue getDelaySub() {
 		StatusType type, subtype;
 		Equipment eq;
 
 		type = subtype = null;
 		eq = mEquipment.getEquipment(EquipmentSet.MAINWEAPON);
 		if (eq != null) {
 			type = eq.getWeaponType();
 		}
 		eq = mEquipment.getEquipment(EquipmentSet.SUBWEAPON);
 		if (eq != null) {
 			subtype = eq.getWeaponType();
 		}
 		if (type == null) {
 			type = StatusType.SKILL_HANDTOHAND;
 		}
 		switch (type) {
 		case SKILL_DAGGER:
 		case SKILL_SWORD:
 		case SKILL_AXE:
 		case SKILL_KATANA:
 		case SKILL_CLUB:
 			if (subtype != null) {
 				switch (subtype) {
 				case SKILL_DAGGER:
 				case SKILL_SWORD:
 				case SKILL_AXE:
 				case SKILL_KATANA:
 				case SKILL_CLUB:
 					{
 						// Dual Wield
 						StatusValue base = getStatus(mLevel, StatusType.DelaySub);
 						StatusValue dualwield = getStatus(mLevel, StatusType.DualWield);
 						base.setAdditionalPercent(-(dualwield.getAdditional() + dualwield.getAdditionalPercent()));
 
 						return base;
 					}
 				}
 			}
 		}
 		return new StatusValue(0, 0, 0);
 	}
 
 	int calcAccuracyByWeaponType(StatusType type) {
 		int value;
 		int skillvalue, modvalue;
 		StatusValue skill = getStatus(mLevel, type);
 		StatusValue mod;
 
 		value = 0;
 		skillvalue = skill.getValue() + skill.getAdditional();
 		switch (type) {
 		case SKILL_HANDTOHAND:
 		case SKILL_DAGGER:
 		case SKILL_SWORD:
 		case SKILL_AXE:
 		case SKILL_KATANA:
 		case SKILL_CLUB:
 			mod = getStatus(mLevel, StatusType.DEX);
 			modvalue = mod.getValue() + mod.getAdditional();
 			if (skillvalue < 200) {
 				value = modvalue * 50 / 100 + skillvalue;
 			} else {
 				value = modvalue * 50 / 100 + 200 + (skillvalue - 200) * 90 / 100;
 			}
 			break;
 
 		case SKILL_GREATSWORD:
 		case SKILL_GREATAXE:
 		case SKILL_SCYTH:
 		case SKILL_POLEARM:
 		case SKILL_GREATKATANA:
 		case SKILL_STAFF:
 			mod = getStatus(mLevel, StatusType.DEX);
 			modvalue = mod.getValue() + mod.getAdditional();
 			if (skillvalue < 200) {
 				value = modvalue * 75 / 100 + skillvalue;
 			} else {
 				value = modvalue * 75 / 100 + 200 + (skillvalue - 200) * 90 / 100;
 			}
 			break;
 		case SKILL_ARCHERY:
 		case SKILL_MARKSMANSHIP:
 		case SKILL_THROWING:
 			mod = getStatus(mLevel, StatusType.AGI);
 			modvalue = mod.getValue() + mod.getAdditional();
 			if (skillvalue < 200) {
 				value = modvalue * 50 / 100 + skillvalue;
 			} else {
 				value = modvalue * 50 / 100 + 200 + (skillvalue - 200) * 90 / 100;
 			}
 			break;
 		}
 		return value;
 	}
 	public StatusValue getAccuracy() {
 		StatusType type;
 		StatusValue mod = getStatus(mLevel, StatusType.Accuracy);
 		Equipment eq = mEquipment.getEquipment(EquipmentSet.MAINWEAPON);
 		if (eq != null) {
 			type = eq.getWeaponType();
 		} else {
 			type = StatusType.SKILL_HANDTOHAND;
 		}
 		if (type != null) {
 			mod.setValue(calcAccuracyByWeaponType(type) + mod.getValue());
 		}
 		return mod;
 	}
 	public StatusValue getAccuracySub() {
 		StatusType type;
		StatusValue mod = getStatus(mLevel, StatusType.Accuracy);
 		Equipment eq = mEquipment.getEquipment(EquipmentSet.SUBWEAPON);
 		if (eq != null) {
 			type = eq.getWeaponType();
 			if (type != null) {
 				mod.setValue(calcAccuracyByWeaponType(type) + mod.getValue());
 			}
 			return mod;
 		} else {
 			return new StatusValue(0, 0, 0);
 		}
 	}
 	int calcAttackByWeaponType(StatusType type) {
 		int value;
 		int skillvalue, strvalue;
 		StatusValue skill = getStatus(mLevel, type);
 		StatusValue str = getStatus(mLevel, StatusType.STR);
 
 		value = 0;
 		strvalue = str.getValue() + str.getAdditional();
 		skillvalue = skill.getValue() + skill.getAdditional();
 		switch (type) {
 		case SKILL_HANDTOHAND:
 		case SKILL_DAGGER:
 		case SKILL_SWORD:
 		case SKILL_AXE:
 		case SKILL_KATANA:
 		case SKILL_CLUB:
 		case SKILL_ARCHERY:
 		case SKILL_MARKSMANSHIP:
 		case SKILL_THROWING:
 			value = strvalue * 50 / 100 + skillvalue + 8;
 			break;
 
 		case SKILL_GREATSWORD:
 		case SKILL_GREATAXE:
 		case SKILL_SCYTH:
 		case SKILL_POLEARM:
 		case SKILL_GREATKATANA:
 		case SKILL_STAFF:
 			value = strvalue * 75 / 100 + skillvalue + 8;
 			break;
 		}
 		return value;
 	}
 	public StatusValue getAttack() {
 		StatusType type;
 		StatusValue mod = getStatus(mLevel, StatusType.Attack);
 		Equipment eq = mEquipment.getEquipment(EquipmentSet.MAINWEAPON);
 		if (eq != null) {
 			type = eq.getWeaponType();
 		} else {
 			type = StatusType.SKILL_HANDTOHAND;
 		}
 		if (type != null) {
 			mod.setValue(calcAttackByWeaponType(type) + mod.getValue());
 		}
 		return mod;
 	}
 	public StatusValue getAttackSub() {
 		StatusType type;
		StatusValue mod = getStatus(mLevel, StatusType.Attack);
 		Equipment eq = mEquipment.getEquipment(EquipmentSet.SUBWEAPON);
 		if (eq != null) {
 			type = eq.getWeaponType();
 			if (type != null) {
 				mod.setValue(calcAttackByWeaponType(type) + mod.getValue());
 			}
 			return mod;
 		} else {
 			return new StatusValue(0, 0, 0);
 		}
 	}
 	public StatusValue getAccuracyRange() {
 		StatusType type;
 		StatusValue mod = getStatus(mLevel, StatusType.AccuracyRange);
 		Equipment eq = mEquipment.getEquipment(EquipmentSet.RANGE);
 		if (eq == null) {
 			eq = mEquipment.getEquipment(EquipmentSet.ANMO);
 		}
 		if (eq != null) {
 			type = eq.getWeaponType();
 			if (type != null) {
 				mod.setValue(calcAccuracyByWeaponType(type) + mod.getValue());
 			}
 		}
 		return mod;
 	}
 	public StatusValue getAttackRange() {
 		StatusType type;
 		StatusValue mod = getStatus(mLevel, StatusType.AttackRange);
 		Equipment eq = mEquipment.getEquipment(EquipmentSet.RANGE);
 		if (eq == null) {
 			eq = mEquipment.getEquipment(EquipmentSet.ANMO);
 		}
 		if (eq != null) {
 			type = eq.getWeaponType();
 			if (type != null) {
 				mod.setValue(calcAttackByWeaponType(type) + mod.getValue());
 			}
 		}
 		return mod;
 	}
 
 	public StatusValue getEvasion() {
 		int value;
 		int skillvalue, stsvalue;
 		StatusValue skill = getStatus(mLevel, StatusType.SKILL_EVASION);
 		StatusValue sts = getStatus(mLevel, StatusType.AGI);
 		StatusValue mod = getStatus(mLevel, StatusType.Evasion);
 
 		value = 0;
 		stsvalue = sts.getValue() + sts.getAdditional();
 		skillvalue = skill.getValue() + skill.getAdditional();
 		if (skillvalue < 200) {
 			value = stsvalue * 50 / 100 + skillvalue;
 		} else {
 			value = stsvalue * 50 / 100 + 200 + (skillvalue - 200) * 90 / 100;
 		}
 		mod.setValue(value + mod.getValue());
 		return mod;
 	}
 	public StatusValue getDefence() {
 		int stsvalue, lvlvalue;
 		StatusValue mod;
 		StatusValue sts = getStatus(mLevel, StatusType.VIT);
 
 		stsvalue = sts.getValue() + sts.getAdditional();
 		lvlvalue = mLevel.getLevel();
 		if (lvlvalue > 50) {
 			if (lvlvalue > 60) {
 				lvlvalue += 10;
 			} else {
 				lvlvalue += lvlvalue - 50;
 			}
 		}
 		mod = getStatus(mLevel, StatusType.Defence);
 		mod.setValue(lvlvalue + 8 + stsvalue * 50 / 100 + mod.getValue());
 		return mod;
 	}
 	public StatusValue getDamageCutPhysical() {
 		StatusValue value;
 		
 		value = getStatus(mLevel, StatusType.DamageCut);
 		value.add(getStatus(mLevel, StatusType.DamageCutPhysical));
 		return value;
 	}
 	public StatusValue getDamageCutMagic() {
 		StatusValue value;
 		
 		value = getStatus(mLevel, StatusType.DamageCut);
 		value.add(getStatus(mLevel, StatusType.DamageCutMagic));
 		return value;
 	}
 	public StatusValue getDamageCutBreath() {
 		StatusValue value;
 		
 		value = getStatus(mLevel, StatusType.DamageCut);
 		value.add(getStatus(mLevel, StatusType.DamageCutBreath));
 		return value;
 	}
 
 	public SortedStringList getUnknownTokens() {
 		if (mInAbyssea) {
 			SortedStringList unknownTokens = new SortedStringList();
 			unknownTokens.mergeList(mEquipment.getUnknownTokens());
 			unknownTokens.mergeList(mAtmaset.getUnknownTokens());
 			
 			return unknownTokens;
 		} else {
 			return mEquipment.getUnknownTokens();
 		}
 	}
 	
 	public void reload() {
 		mEquipment.reloadEquipments();
 		mAtmaset.reloadAtmas();
 		mModified = true;
 	}
 }
