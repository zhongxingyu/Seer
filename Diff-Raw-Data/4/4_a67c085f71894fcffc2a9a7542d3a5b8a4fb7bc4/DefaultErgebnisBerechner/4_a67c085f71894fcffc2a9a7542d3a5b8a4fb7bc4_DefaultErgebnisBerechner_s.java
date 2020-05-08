 /**
  * 
  */
 package org.hitzemann.mms.solver;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.hitzemann.mms.model.ErgebnisKombination;
 import org.hitzemann.mms.model.SpielKombination;
 
 /**
  * @author simon
  * 
  */
 public class DefaultErgebnisBerechner implements IErgebnisBerechnung {
 	/* (non-Javadoc)
 	 * @see org.hitzemann.mms.solver.IErgebnisBerechnung#berechneErgebnis(org.hitzemann.mms.model.SpielKombination, org.hitzemann.mms.model.SpielKombination)
 	 */
 	@Override
 	public ErgebnisKombination berechneErgebnis(SpielKombination geheim,
 			SpielKombination geraten) {
 		// Fallback, nix richtig
 		int korrekt = 0;
 		int position = 0;
 		// Trivial: beide gleich
		if (geheim.getSpielSteineCount() != geraten.getSpielSteineCount()) {
			throw new IllegalArgumentException("Spielsteine haben unterschiedliche Größen!");
 		}
 		if (geheim.equals(geraten)) {
 			korrekt = geheim.getSpielSteineCount();
 			position = 0;
 		} else {
 			// 2 Maps um zu tracken, welche Steine schon "benutzt" wurden
 			final Map<Integer, Boolean> geheimMap = new HashMap<Integer, Boolean>();
 			final Map<Integer, Boolean> geratenMap = new HashMap<Integer, Boolean>();
 			for (int n = 0; n < geheim.getSpielSteineCount(); n++) {
 				geheimMap.put(n, true);
 				geratenMap.put(n, true);
 			}
 			// Berechne korrekte Positionen
 			for (int n = 0; n < geheim.getSpielSteineCount(); n++) {
 				if (geheimMap.get(n)
 						&& geratenMap.get(n)
 						&& geheim.getSpielStein(n).equals(
 								geraten.getSpielStein(n))) {
 					geheimMap.put(n, false);
 					geratenMap.put(n, false);
 					korrekt++;
 				}
 			}
 			// Berechne korrekte Farben und falsche Positionen
 			for (int n = 0; n < geheim.getSpielSteineCount(); n++) {
 				for (int m = 0; m < geheim.getSpielSteineCount(); m++) {
 					if (m != n) {
 						if (geheimMap.get(n)
 								&& geratenMap.get(m)
 								&& geheim.getSpielStein(n).equals(
 										geraten.getSpielStein(m))) {
 							geheimMap.put(n, false);
 							geratenMap.put(m, false);
 							position++;
 						}
 					}
 				}
 			}
 		}
 		return new ErgebnisKombination(korrekt, position);
 	}
 }
