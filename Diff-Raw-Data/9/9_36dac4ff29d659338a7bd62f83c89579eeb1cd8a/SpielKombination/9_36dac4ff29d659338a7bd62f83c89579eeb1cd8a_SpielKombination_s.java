 package org.hitzemann.mms.model;
 
 import java.util.Arrays;
 
 /**
  * @author simon
  * 
  */
 public class SpielKombination implements Comparable<SpielKombination> {
 	/**
 	 * Internes Array um die SpielSteine aufzubewahren.
 	 */
 	private final SpielStein[] spielSteine;
 
 	/**
 	 * Konstruktor für beliebig viele Steine in der SpielKombination.
 	 * 
 	 * @param paramSpielSteine
 	 *            beliebige Anzahl an SpielSteinen
 	 */
 	public SpielKombination(final SpielStein... paramSpielSteine) {
 		this.spielSteine = paramSpielSteine;
 	}
 
 	/**
 	 * Konstruktor für beliebig viele Steine die durch einen int statt einer
 	 * Farbe referenziert werden.
 	 * 
 	 * @param paramSpielSteinWerte
 	 *            beliebige Anzahl an SpielSteinen durch ints referenziert
 	 */
 	public SpielKombination(final int... paramSpielSteinWerte) {
 		final SpielStein[] values = SpielStein.values();
 		this.spielSteine = new SpielStein[paramSpielSteinWerte.length];
 		for (int i = 0; i < paramSpielSteinWerte.length; i++) {
			spielSteine[i] = values[paramSpielSteinWerte[i]-1];
 		}
 	}
 
 	@Override
 	public final int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + Arrays.hashCode(spielSteine);
 		return result;
 	}
 
 	@Override
 	public final boolean equals(final Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		final SpielKombination other = (SpielKombination) obj;
 		if (!Arrays.equals(spielSteine, other.spielSteine)) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Gibt einen SpielStein an einer bestimmten Position zurück.
 	 * 
 	 * @param position
 	 *            Position des gewünschten SpielSteines
 	 * @return SpielStein an der gewünschen Position
 	 */
 	public final SpielStein getSpielStein(final int position) {
 		return spielSteine[position];
 	}
 
 	/**
 	 * Gibt die Anzahl der SpielSteine zurück, die aufbewahrt werden.
 	 * 
 	 * @return Anzahl der SpielSteine in der SpielKombination
 	 */
 	public final int getSpielSteineCount() {
 		return spielSteine.length;
 	}
 
 	@Override
 	public final String toString() {
 		return "SpielKombination [spielSteine=" + Arrays.toString(spielSteine)
 				+ "]";
 	}
 
 	@Override
 	public final int compareTo(final SpielKombination arg0) {
 
 		if (this.getSpielSteineCount() > arg0.getSpielSteineCount()) {
 			return 1;
 		} else if (this.getSpielSteineCount() < arg0.getSpielSteineCount()) {
 			return -1;
 		} else {
 			final int steine = this.getSpielSteineCount();
 			for (int i = 0; i < steine; i++) {
 				if (this.spielSteine[i].ordinal() > arg0.spielSteine[i]
 						.ordinal()) {
 					return 1;
 				} else if (this.spielSteine[i].ordinal() < arg0.spielSteine[i]
 						.ordinal()) {
 					return -1;
 				}
 			}
 		}
 		return 0;
 	}
 }
