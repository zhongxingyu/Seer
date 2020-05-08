 package ddybb.d3calc.src;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 public class Character implements Serializable {
 	
 	private static final long serialVersionUID = 110L;
 	public String name;
 	public CharacterClass characterClass;
 	
 	public int paragon = 0;
 	
 	public int baseDex = 67;
 	public int baseInt = 67;
 	public int baseStr = 67;
 	public int baseVit = 127;
 	public int baseHP = 276;
 	public int dexterity = 0;
 	public int intelligence = 0;
 	public int strength = 0;
 	public int vitality = 0;
 	public int hp = 0;
 	
 	private float baseDR = 0f;
 	public int baseCritDamage = 50;
 	public float baseCritChance = 5f;
 	public int critDamage = 0;
 	public float critChance = 0f;
 	public int atkSpeedBonus = 0;
 	
 	public float dps = 0f;
 	public float atkSpeed = 0f;
 	public int damageMin = 0;
 	public int damageMax = 0;
 	
 	public int allRes = 0;
 	public int physRes = 0;
 	public int coldRes = 0;
 	public int fireRes = 0;
 	public int litRes = 0;
 	public int poisonRes = 0;
 	public int arcRes = 0;
 	public int armor = 0;
 	public int life = 0;
 	public float reducM = 0;
 	public float reducR = 0;
 	
 	public float blockChance = 0f;
 	
 	public float dodge = 0f;
 	public int ehp = 0;
 	public int ehpM = 0;
 	public int ehpR = 0;
 	public int ehpD = 0;
 	public int ehpDM = 0;
 	public int ehpDR = 0;
 	public float armorPct = 0f;
 	public float allResPct = 0f;
 	public float physResPct = 0f;
 	public float coldResPct = 0f;
 	public float fireResPct = 0f;
 	public float litResPct = 0f;
 	public float poisonResPct = 0f;
 	public float arcResPct = 0f;
 	
 	public ArrayList<Skill> skills = new ArrayList<Skill>();
 	
 	public Item[] equip = new Item[13]; 
 	//0helm,1shoulder,2chest,3pants,4gloves,5bracers,6belt,7boots,8amulet,9ring,10ring,11weapon,12weapon
 
 	public Character(String name, CharacterClass characterClass) {
 		this.name = name;
 		this.characterClass = characterClass;
 		setBaseStats();
 		addSkills();
 	}
 	
 	private void setBaseStats() {
 		
 		switch (this.characterClass) {
 		case Barbarian: baseStr += 120; baseDR = 0.3f; break;
 		case DemonHunter: baseDex += 120; break;
 		case Monk: baseDex += 120; baseDR = 0.3f; break;
 		case WitchDoctor: baseInt += 120; break;
 		case Wizard: baseInt += 120; break;
 		default: break;
 		}
 		
 		update();
 		
 	}
 	
 	private void addParagonBonus() {
 		
 		dexterity += paragon;
 		intelligence += paragon;
 		strength += paragon;
 		vitality += paragon * 2;
 		
 		switch (this.characterClass) {
 		case Barbarian: strength += paragon * 2; break;
 		case DemonHunter: dexterity += paragon * 2; break;
 		case Monk: dexterity += paragon * 2; break;
 		case WitchDoctor: intelligence += paragon * 2; break;
 		case Wizard: intelligence += paragon * 2; break;
 		default: break;
 		}
 	}
 	
 	private void addSkills() {
 		
 		int i = -1;
 		switch (this.characterClass) {
 		case Barbarian: i = 0; break;
 		case DemonHunter: i = 1; break;
 		case Monk: i = 2; break;
 		case WitchDoctor: i = 3; break;
 		case Wizard: i = 4; break;
 		default: break;
 		}
 		
 		for (Skill skill: Skill.values()) {
 			skill.flag = false;
 			if (skill.cMask == i) {
 				skills.add(skill);
 			}
 		}
 		
 	}
 	
 	public void update() {
 		
 		//reset all stats
 		dexterity = baseDex;
 		intelligence = baseInt;
 		strength = baseStr;
 		vitality = baseVit;
 		critDamage = baseCritDamage;
 		critChance = baseCritChance;
 		atkSpeedBonus = 0;
 		allRes = 0;
 		physRes = 0;
 		coldRes = 0;
 		fireRes = 0;
 		litRes = 0;
 		poisonRes = 0;
 		arcRes = 0;
 		armor = 0;
 		life = 0;
 		blockChance = 0f;
 		damageMin = 0;
 		damageMax = 0;
 		reducM = 0f;
 		reducR = 0f;
 		
 		addParagonBonus();
 		
 		//sum equipment bonuses
 		for (int i = 0; i < equip.length; i++) {
 			if (equip[i] != null) {
 				dexterity += equip[i].dexterity;
 				intelligence += equip[i].intelligence;
 				strength += equip[i].strength;
 				vitality += equip[i].vitality;
 				critDamage += equip[i].critDamage;
 				critChance += equip[i].critChance;
 				atkSpeedBonus += equip[i].atkSpeed;
 				allRes += equip[i].allRes;
 				physRes += equip[i].physRes;
 				coldRes += equip[i].coldRes;
 				fireRes += equip[i].fireRes;
 				litRes += equip[i].litRes;
 				poisonRes += equip[i].poisonRes;
 				arcRes += equip[i].arcRes;
 				armor += equip[i].armor;
 				life += equip[i].life;
 				blockChance += equip[i].blockChanceBase;
 				blockChance += equip[i].blockChance;
 				damageMin += equip[i].damageMin;
 				damageMax += equip[i].damageMax;
 				reducM += ((100f - reducM) / 100f) * equip[i].reducM;
 				reducR += ((100f - reducR) / 100f) * equip[i].reducR;
 			}
 		}
 		
 		//compound values
 		armor += strength;
 		allRes += intelligence / 10;
 		physRes += allRes;
 		coldRes += allRes;
 		fireRes += allRes;
 		litRes += allRes;
 		poisonRes += allRes;
 		arcRes += allRes;
 		
 		hp = ((baseHP + 35 * vitality) * (100 + life)) / 100;
 		
 		if (dexterity < 100) { dodge = 0.1f * (float)dexterity; } 
 		else if (dexterity < 500) { dodge = 10f + 0.025f * (float)(dexterity - 100); }
 		else if (dexterity < 1000) { dodge = 20f + 0.02f * (float)(dexterity - 500); }
 		else { dodge = 30f + 0.01f * (float)(dexterity - 1000); }
 		
 		allRes = physRes;
 		if (coldRes < allRes) { allRes = coldRes; }
 		if (fireRes < allRes) { allRes = fireRes; }
 		if (litRes < allRes) { allRes = litRes; }
 		if (poisonRes < allRes) { allRes = poisonRes; }
 		if (arcRes < allRes) { allRes = arcRes; }
 		
 		addSkillBonus();
 		calculateDerivedStats();
 		
 	}
 	
 	private void calculateDerivedStats() {
 		
 		armorPct = armor / (3000f + armor);
 		allResPct = allRes / (300f + allRes);
 		physResPct = physRes / (300f + physRes);
 		coldResPct = coldRes / (300f + coldRes);
 		fireResPct = fireRes / (300f + fireRes);
 		litResPct = litRes / (300f + litRes);
 		poisonResPct = poisonRes / (300f + poisonRes);
 		arcResPct = arcRes / (300f + arcRes);
 		
 		ehp = (int)((float)hp / ((1f - armorPct) * (1f - allResPct) * (1f - baseDR)));
		
		calculateDPS();
		addFinalSkillBonus();
		
		ehpD = (int)(ehp  / (1f - dodge / 100f));
 		ehpM = (int)(ehp * 100f / (100f - reducM));
 		ehpR = (int)(ehp * 100f / (100f - reducR));
 		ehpDM = (int)(ehpM  / (1f - dodge / 100f));
 		ehpDR = (int)(ehpR  / (1f - dodge / 100f));
 		
 	}
 	
 	private void calculateDPS() {
 		
 		//set primary stat coefficient
 		float primary = 1f;
 		if (characterClass == CharacterClass.Barbarian) {
 			primary = 1f + (float)strength / 100f;
 		}
 		if (characterClass == CharacterClass.DemonHunter || characterClass == CharacterClass.Monk) {
 			primary = 1f + (float)dexterity / 100f;
 		}
 		if (characterClass == CharacterClass.WitchDoctor || characterClass == CharacterClass.Wizard) {
 			primary = 1f + (float)intelligence / 100f;
 		}
 		
 		//skip if null
 		if (equip[11] == null) { return; }
 		
 		//2hander
 		if (equip[11].type == ItemType.Weapon2H) {
 			
 			atkSpeed = equip[11].atkSpeedBase * (100f + (float)atkSpeedBonus) / 100f;
 			float bonusDPS = (float)(damageMin + damageMax) / 2f * equip[11].atkSpeedBase;
 			dps = (equip[11].dps + bonusDPS) / equip[11].atkSpeedBase * atkSpeed * primary * (1f + (critChance / 100f) * ((float)critDamage / 100f));
 			
 		}
 		
 		//1h or 1h+shield or Bow
 		if (equip[11].type == ItemType.Weapon1 || equip[11].type == ItemType.WeaponBow) {
 			
 			atkSpeed = equip[11].atkSpeedBase * (100f + (float)atkSpeedBonus) / 100f;
 			float bonusDPS = (float)(damageMin + damageMax) / 2f * equip[11].atkSpeedBase;
 			dps = (equip[11].dps + bonusDPS) / equip[11].atkSpeedBase * atkSpeed * primary * (1f + (critChance / 100f) * ((float)critDamage / 100f));
 			
 		}
 		
 		//skip doubleslot checks if offhand null
 		if(equip[12] == null) { return; }
 		
 		//dual wield
 		if (equip[11].type == ItemType.Weapon1 && equip[12].type == ItemType.Weapon2) {
 			
 			float avgAtkSpeed = (equip[11].atkSpeedBase + equip[12].atkSpeedBase) / 2f;
 			atkSpeed = avgAtkSpeed * (115f + (float)atkSpeedBonus) / 100f;
 			float avgDPS = (equip[11].dps + equip[12].dps) / 2f;
 			float bonusDPS = (float)(damageMin + damageMax) / 2f * avgAtkSpeed;
 			dps = (avgDPS + bonusDPS) / equip[11].atkSpeedBase * atkSpeed * primary * (1f + (critChance / 100f) * ((float)critDamage / 100f));
 			return;
 			
 		}
 		
 		//1h + source
 		if (equip[11].type == ItemType.Weapon1 && equip[12].type == ItemType.Source) {
 			
 			atkSpeed = equip[11].atkSpeedBase * (100f + (float)atkSpeedBonus) / 100f;
 			float bonusDPS = (float)(damageMin + damageMax) / 2f * equip[11].atkSpeedBase;
 			dps = (equip[11].dps + bonusDPS) / equip[11].atkSpeedBase * atkSpeed * primary * (1f + (critChance / 100f) * ((float)critDamage / 100f));
 			return;
 		}
 		
 		
 	}
 	
 	private void addSkillBonus() {
 		
 		//monk: eva 15d, eva/ht 15d 20a, moh/tn 20r, sti Adex, owe allr, gp 15d, resolve 25b, ss 30(c)d, 
 		//barb super 20o, nails 25a, impu 20 a50r, hard wrath 40a, vet warn 20a 15d nos Avit, threat 20b, threat/falter 20b 15ab; ruth 5c 50cd, weapon s15d m10c p10a m3f, 
 		//barb
 		if (Skill.nos.flag) { armor += vitality; }
 		
 		if (Skill.nails.flag) { armor = (int)(armor * 1.25f); }
 		
 		if (Skill.war.flag) { armor = (int)(armor * 1.2f); }
 		
 		if (Skill.warHW.flag) { armor = (int)(armor * 1.4f); }
 		
 		if (Skill.warVW.flag) {
 			armor = (int)(armor * 1.2f);
 			dodge = dodge + 15f * (100f - dodge) / 100f;
 		}
 		
 		if (Skill.warInv.flag) { hp = (int)(hp * 1.1f); }
 		
 		if (Skill.warImp.flag) {
 			armor = (int)(armor * 1.2f);
 			allRes = (int)(allRes * 1.5f);
 			physRes = (int)(physRes * 1.5f);
 			coldRes = (int)(coldRes * 1.5f);
 			fireRes = (int)(fireRes * 1.5f);
 			litRes = (int)(litRes * 1.5f);
 			poisonRes = (int)(poisonRes * 1.5f);
 			arcRes = (int)(arcRes * 1.5f);
 		}
 		
 		if (Skill.ruth.flag) {
 			critChance += 5f;
 			critDamage += 50;
 		}
 		
 		if (Skill.weapM.flag) { critChance += 10f; }
 		
 		if (Skill.weapP.flag) { atkSpeedBonus += 10f; }
 		
 		//DH
 		
 		if (Skill.arcX.flag) { critDamage += 50; }
 		
 		if (Skill.arcH.flag) { critChance += 10f; }
 		
 		//monk
 		if (Skill.owe.flag) {
 			allRes = (physRes > allRes) ? physRes: allRes;
 			allRes = (coldRes > allRes) ? coldRes: allRes;
 			allRes = (fireRes > allRes) ? fireRes: allRes;
 			allRes = (litRes > allRes) ? litRes: allRes;
 			allRes = (poisonRes > allRes) ? poisonRes: allRes;
 			allRes = (arcRes > allRes) ? arcRes: allRes;
 			physRes = allRes;
 			coldRes = allRes;
 			fireRes = allRes;
 			litRes = allRes;
 			poisonRes = allRes;
 			arcRes = allRes;
 		}
 		
 		if (Skill.sti.flag) { armor += dexterity; }
 		
 		if (Skill.moe.flag) { dodge = dodge + 15f * (100f - dodge) / 100f; }
 		
 		if (Skill.moeHT.flag) {
 			dodge = dodge + 15f * (100f - dodge) / 100f;
 			armor = (int)(armor * 1.2f);
 		}
 		
 		if (Skill.mohTN.flag) {
 			allRes = (int)(allRes * 1.2f);
 			physRes = (int)(physRes * 1.2f);
 			coldRes = (int)(coldRes * 1.2f);
 			fireRes = (int)(fireRes * 1.2f);
 			litRes = (int)(litRes * 1.2f);
 			poisonRes = (int)(poisonRes * 1.2f);
 			arcRes = (int)(arcRes * 1.2f);
 		}
 		
 		if (this.equip[11] != null && this.equip[12] != null) {
 			if (Skill.gp.flag && this.equip[11].type == ItemType.Weapon1 && this.equip[12].type == ItemType.Weapon2) {
 				dodge = dodge + 15f * (100f - dodge) / 100f;
 			}
 		}
 		
 		if (Skill.sixS.flag) { dodge = dodge + (0.3f * critChance) * (100f - dodge) / 100f; }
 		
 		//WD
 		
 		//Wiz
 		if (Skill.blur.flag) { reducM += ((100f - reducM) / 100f) * 20f; }
 		
 		if (Skill.energy.flag) { armor = (int)(armor * 1.65f); }
 		
 		if (Skill.energyP.flag) {
 			armor = (int)(armor * 1.65f);
 			allRes = (int)(allRes * 1.4f);
 			physRes = (int)(physRes * 1.4f);
 			coldRes = (int)(coldRes * 1.4f);
 			fireRes = (int)(fireRes * 1.4f);
 			litRes = (int)(litRes * 1.4f);
 			poisonRes = (int)(poisonRes * 1.4f);
 			arcRes = (int)(arcRes * 1.4f);
 		}
 		
 		if (Skill.energyB.flag) {
 			armor = (int)(armor * 1.65f);
 			critChance += 5f;
 		}
 		
 		if (Skill.glass.flag) {
 			armor = (int)(armor * 0.9f);
 			allRes = (int)(allRes * 0.9f);
 			physRes = (int)(physRes * 0.9f);
 			coldRes = (int)(coldRes * 0.9f);
 			fireRes = (int)(fireRes * 0.9f);
 			litRes = (int)(litRes * 0.9f);
 			poisonRes = (int)(poisonRes * 0.9f);
 			arcRes = (int)(arcRes * 0.9f);
 		}
 	}
 	
 	private void addFinalSkillBonus() {
 		
 		//barb
 		if(Skill.threat.flag) { ehp = (int)(ehp / 0.8f); }
 		
 		if (Skill.threatF.flag) { ehp = (int)(ehp / 0.8f / 0.85f); }
 		
 		if (Skill.weapS.flag) { dps = dps * 1.15f; }
 		
 		//DH
 		if (Skill.arcB.flag) { dps = dps * 1.15f; }
 		
 		if (Skill.steady.flag) { dps = dps * 1.2f; }
 		
 		//monk
 		if (Skill.resolve.flag) { ehp = (int)(ehp / 0.75f); }
 		
 		//WD
 		if (Skill.jFort.flag) { ehp = (int)( ehp / 0.8f); }
 		
 		if (Skill.bad.flag) { ehp = (int)( ehp / 0.8f); }
 		
 		if (Skill.pierce.flag) { dps = dps * 1.2f; }
 		
 		//Wiz
 		if (Skill.glass.flag) { dps = dps * 1.15f; }
 		
 		if (Skill.magic.flag) { dps = dps * 1.10f; }
 		
 		if (Skill.magicF.flag) { dps = dps * 1.15f; }
 		
 		if (Skill.fam.flag) { dps = dps * 1.12f; }
 		
 	}
 
 
 
 }
