 package model;
 
 public class AttackCaC implements IAttack {
 	
 	protected boolean canHit(int i)
 	{
 		return (int)(Math.random() * (101)) < i;
 	}
 	
 	protected boolean trinityAdvantage(Weapon a, Weapon b)
 	{
 		return (a == Weapon.SWORD && b ==  Weapon.LANCE) 
 				|| (a ==  Weapon.LANCE && b ==  Weapon.AXE) 
 				|| (a ==  Weapon.AXE && b ==  Weapon.SWORD);
 	}
 	
 	
 	@Override
 	public String attack(Unit att, Unit def) throws DeadUnitException {
 		int hit = att.getHit();
 		int dmg = att.getDmg();
 		int crit = att.getCrit();
 		
 		
 		if(trinityAdvantage(def.getWep(),att.getWep()))
 			hit = hit * 1/3;
 		if(trinityAdvantage(att.getWep(),def.getWep()))
 			crit+=5;
 		
 		if(!canHit(hit))
 			return  def.getName() + " a esquiv l'attaque !";
 		
 		System.out.println("dmg avant crit: " + dmg);
		dmg += dmg * (crit / 100);
		dmg -= dmg * (def.getDef() / 100);
 		System.out.println("dmg aprs def: " + dmg);
 		
 		def.receiveDmg(dmg);
 		
		return "L'attaque de " + att.getName() + " a inflig " + dmg + "  " + def.getName();
 	}
 	
 	@Override
 	public boolean canAttackFromRange(int i) {
 		return false;
 	}
 
 }
