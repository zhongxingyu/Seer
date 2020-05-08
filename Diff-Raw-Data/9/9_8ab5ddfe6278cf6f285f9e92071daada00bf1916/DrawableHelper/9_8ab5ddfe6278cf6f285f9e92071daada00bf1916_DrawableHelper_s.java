 package com.gmerin.descentmonsters.utils;
 
 import com.gmerin.descentmonsters.R;
 
 public class DrawableHelper {
 
 	public static int getDrawableIDAttackType(String attack) {
 		// Cuerpo a cuerpo
 		if (attack.compareToIgnoreCase("cc")==0) return R.drawable.attack_type_melee;
 		if (attack.compareToIgnoreCase("ad")==0) return R.drawable.attack_type_distance;
 		return -1;
 	}
 	
 	public static int getDrawableIDTrait(int traitID) {
 		switch(traitID) {
 			case 1: return R.drawable.trait_wilderness; // Bosque
 			case 2:	return R.drawable.trait_water; // Agua
 			case 3: return R.drawable.trait_hot; // Calor
 			case 4: return R.drawable.trait_civilized; // Civilizado
 			case 5: return R.drawable.trait_cave; // Cueva
 			case 6: return R.drawable.trait_building; // Edificio
 			case 7: return R.drawable.trait_cold; // Fro
 			case 8: return R.drawable.trait_cursed; // Maldito
 			case 9: return R.drawable.trait_mountain; // Montaa
 			case 10: return R.drawable.trait_dark; // Oscuridad
 		}
 		return -1;
 	}
 	
 	public static int getDrawableIDDice(char c) {
 		switch(c) {
 			case 'a': return R.drawable.dice_a; // Amarillo
 			case 'm': return R.drawable.dice_m; // Marrn
 			case 'n': return R.drawable.dice_n; // Negro
 			case 'p': return R.drawable.dice_p; // Plata
 			case 'r': return R.drawable.dice_r; // Rojo
 			case 'z': return R.drawable.dice_z; // Azul
 			case 'v': return R.drawable.dice_v; // Verde
 		}
 		return -1;
 	}
 	
 	public static int getDrawableIDAbilityCost(String cod) {
 		if (cod == null) return -1;
 		if (cod.compareTo("ac") == 0) return R.drawable.icon_action; // Accin
 		if (cod.compareTo("in") == 0) return R.drawable.icon_surge;	// Incremento
 		if (cod.compareTo("ii") == 0) return R.drawable.icon_2surge; // Doble incremento
 		return -1;
 	}
 	
 	/**
 	 * Transforma los cdigos utilizados en la BD para representar el coste de las abilidades
 	 * en tokens utilizados el fichero desc_abilities.xml, que son los mismos que estn en el
 	 * mtodo de abajo.
 	 * @param cod Cdigo del coste de la accin usado en la BD
 	 * @return token Token usado por el mtodo de abajo
 	 */
 	public static String transformBDActionCostToToken(String cod) {
 		if (cod == null) return "";
 		if (cod.compareTo("ac") == 0) return ":act:"; // Accin
 		if (cod.compareTo("in") == 0) return ":sur:";	// Incremento
 		if (cod.compareTo("ii") == 0) return ":su2:"; // Doble incremento
 		return "";
 	}
 	
 	public static int getDrawableIDAbilityDesc(String token) {
 		if (token == null) return -1;
 		if (token.compareTo(":dam:") == 0) return R.drawable.icon_damage; // Dao
 		if (token.compareTo(":fat:") == 0) return R.drawable.icon_fatigue; // Fatiga
		if (token.compareTo(":awa:") == 0) return R.drawable.icon_awarenes; // Percepcin
 		if (token.compareTo(":mig:") == 0) return R.drawable.icon_might; // Fuerza
 		if (token.compareTo(":kno:") == 0) return R.drawable.icon_knowledge; // Conocimiento
 		if (token.compareTo(":def:") == 0) return R.drawable.icon_defense; // Defensa
 		if (token.compareTo(":sur:") == 0) return R.drawable.icon_surge; // Incremento
 		if (token.compareTo(":act:") == 0) return R.drawable.icon_action; // Accin
 		if (token.compareTo(":wil:") == 0) return R.drawable.icon_willpower; // Voluntad
 		if (token.compareTo(":su2:") == 0) return R.drawable.icon_2surge;
 		return -1;
 	}
 	
 }
