 /**
  * 
  */
 package org.hitzemann.mms.solver;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.hitzemann.mms.model.ErgebnisKombination;
 import org.hitzemann.mms.model.SpielKombination;
 import org.hitzemann.mms.model.SpielStein;
 
 /**
  * @author simon
  * 
  */
 public final class KnuthSolver implements ISolver {
 
 	/**
 	 * Anzahl der Pins in diesem Spiel.
 	 */
 	private static final int PINS = 4;
 
 	/**
 	 * Maximumscore einer Lösung, sollte größer sein als Farben^Pins.
 	 */
 	private static final long MAXSCORE = Math.round(Math.pow(
 			SpielStein.values().length, PINS)) + 1;
 
 	/**
 	 * Statisches Set mit allen Möglichkeiten, sollte für mehr Geschwindigkeit
 	 * sorgen.
 	 */
 	private static SortedSet<SpielKombination> alleMoeglichkeiten;
 
 	/**
 	 * Cache für die gewählte Ratekombination in Abhängigkeit von der Menge der
 	 * verfügbaren Kandidaten für die Geheimkombination. 
 	 */
 	private static final Map<Set<SpielKombination>, SpielKombination> CACHE = new HashMap<Set<SpielKombination>, SpielKombination>();
 
 	/**
 	 * Objekt zur Ergebnisberechnung.
 	 */
 	private IErgebnisBerechnung ergebnisBerechner;
 
 	/**
 	 * Set mit noch allen möglichen Lösungen für das aktuelle Spiel.
 	 */
 	private SortedSet<SpielKombination> geheimMoeglichkeiten;
 
 	/**
 	 * Set mit allen gültigen ErgebnisMöglichkeiten (Modulo dem Ergebnis, dass
 	 * alle Pins richtig sind).
 	 */
 	private SortedSet<ErgebnisKombination> ergebnisMoeglichkeiten;
 
 	/**
 	 * Map aus SpielKombination und Score für den jeweiligen Zug. Die Score ist
 	 * die Anzahl an Lösungen, die mindestens eliminiert werden.
 	 */
 	private Map<SpielKombination, Long> scoreMap;
 
 	/**
 	 * Set mit allen noch gültigen SpielKombinationen, die noch geraten werden
 	 * können. Verhindert, dass Kombinationen mehrfach benutzt werden.
 	 */
 	private SortedSet<SpielKombination> rateSet;
 
 	static {
 		initialisiereAlleMoeglichkeiten();
 		
 		// Cache mit festem ersten Zug vorbelegen
 		CACHE.put(alleMoeglichkeiten, new SpielKombination(1, 1, 2, 2));
 	}
 
 	/**
 	 * Standardkonstruktor für den Solver.
 	 * 
 	 * @param berechner
 	 *            Objekt, welches IErgebnisBerechnung implementiert
 	 */
 	public KnuthSolver(final IErgebnisBerechnung berechner) {
 		this.ergebnisBerechner = berechner;
 		scoreMap = new TreeMap<SpielKombination, Long>();
 		initialisiereErgebnisMoeglichkeiten();
 		geheimMoeglichkeiten = new TreeSet<SpielKombination>(alleMoeglichkeiten);
 		rateSet = new TreeSet<SpielKombination>(alleMoeglichkeiten);
 	}
 
 	/**
 	 * Alle Farbmöglichkeiten der Spielsteine vorberechnen.
 	 */
 	private static void initialisiereAlleMoeglichkeiten() {
 		alleMoeglichkeiten = new TreeSet<SpielKombination>();
 		for (SpielStein i1 : SpielStein.values()) {
 			for (SpielStein i2 : SpielStein.values()) {
 				for (SpielStein i3 : SpielStein.values()) {
 					for (SpielStein i4 : SpielStein.values()) {
 						final SpielKombination kombi = new SpielKombination(i1,
 								i2, i3, i4);
 						alleMoeglichkeiten.add(kombi);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Für's Iterieren ein Set mit allen gültigen Ergebnissen für 4 Steine
 	 * anlegen.
 	 */
 	private void initialisiereErgebnisMoeglichkeiten() {
 		ergebnisMoeglichkeiten = new TreeSet<ErgebnisKombination>();
 		ergebnisMoeglichkeiten.add(new ErgebnisKombination(0, 0));
 		ergebnisMoeglichkeiten.add(new ErgebnisKombination(0, 1));
 		ergebnisMoeglichkeiten.add(new ErgebnisKombination(0, 2));
 		ergebnisMoeglichkeiten.add(new ErgebnisKombination(0, 3));
 		ergebnisMoeglichkeiten.add(new ErgebnisKombination(0, 4));
 		ergebnisMoeglichkeiten.add(new ErgebnisKombination(1, 0));
 		ergebnisMoeglichkeiten.add(new ErgebnisKombination(1, 1));
 		ergebnisMoeglichkeiten.add(new ErgebnisKombination(1, 2));
 		ergebnisMoeglichkeiten.add(new ErgebnisKombination(1, 3));
 		ergebnisMoeglichkeiten.add(new ErgebnisKombination(2, 0));
 		ergebnisMoeglichkeiten.add(new ErgebnisKombination(2, 1));
 		ergebnisMoeglichkeiten.add(new ErgebnisKombination(2, 2));
 		ergebnisMoeglichkeiten.add(new ErgebnisKombination(3, 0));
 	}
 
 	/**
 	 * Entferne alle möglichen Geheimkombinationen, die nicht durch die geratene
 	 * Kombination und das Ergebnis dargestellt werden können.
 	 * 
 	 * @param ratekombi
 	 *            Geratene SpielKombination
 	 * @param ergebnis
 	 *            Ergebnis für die geratene Kombination
 	 * @param moeglichkeitenSet
 	 *            Set aus SpielKombinationen über die iteriert wird
 	 */
 	private void eleminiereMoeglichkeiten(final SpielKombination ratekombi,
 			final ErgebnisKombination ergebnis,
 			final Set<SpielKombination> moeglichkeitenSet) {
 		if (ratekombi.getSpielSteineCount() != PINS) {
 			throw new IllegalArgumentException(
 					"Im Moment können nur 4 Pins gelöst werden");
 		}
 		for (final Iterator<SpielKombination> setIterator = moeglichkeitenSet
 				.iterator(); setIterator.hasNext();) {
 			final SpielKombination geheim = setIterator.next();
 			if (!ergebnis.equals(ergebnisBerechner.berechneErgebnis(geheim,
 					ratekombi))) {
 				setIterator.remove();
 			}
 		}
 	}
 
 	/**
 	 * Zähle wieviele Möglichkeiten ein Spiel- und ErgebnisKombinationstupel von
 	 * den noch übrigen geheimen Möglichkeiten entfernen würde.
 	 * 
 	 * @param ratekombi
 	 *            SpielKombination die geraten werden soll
 	 * @param ergebnis
 	 *            ErgebnisKombination welche herauskommen soll
 	 * @param geheimSet
 	 *            SpielKombination Set der noch möglichen Lösungen
 	 * @return Anzahl der SpielKombinationen die keine Lösung mehr sein können
 	 */
 	private int zaehleEleminierteMoeglichkeiten(
 			final SpielKombination ratekombi,
 			final ErgebnisKombination ergebnis,
 			final Set<SpielKombination> geheimSet) {
 		int anzahlEleminierterMoeglichkeiten = 0;
 		if (ratekombi.getSpielSteineCount() != PINS) {
 			throw new IllegalArgumentException(
 					"Im Moment können nur 4 Pins gelöst werden");
 		}
 		for (SpielKombination geheim : geheimSet) {
 			if (!ergebnis.equals(ergebnisBerechner.berechneErgebnis(geheim,
 					ratekombi))) {
 				anzahlEleminierterMoeglichkeiten++;
 			}
 		}
 		return anzahlEleminierterMoeglichkeiten;
 	}
 
 	/**
 	 * Zähle wieviele Möglichkeiten ein Spiel- und ErgebnisKombinationstupel von
 	 * den noch übrigen geheimen Möglichkeiten übrig lassen würde.
 	 * 
 	 * @param ratekombi
 	 *            SpielKombination die geraten werden soll
 	 * @param ergebnis
 	 *            ErgebnisKombination welche herauskommen soll
 	 * @param geheimSet
 	 *            SpielKombination Set der noch möglichen Lösungen
 	 * @return Anzahl der SpielKombinationen die noch eine Lösung sein können
 	 */
 	private int zaehleUebrigeMoeglichkeiten(
 			final SpielKombination ratekombi,
 			final ErgebnisKombination ergebnis,
 			final Set<SpielKombination> geheimSet) {
 		int anzahlUebrigerMoeglichkeiten = 0;
 		if (ratekombi.getSpielSteineCount() != PINS) {
 			throw new IllegalArgumentException(
 					"Im Moment können nur 4 Pins gelöst werden");
 		}
 		for (SpielKombination geheim : geheimSet) {
 			if (ergebnis.equals(ergebnisBerechner.berechneErgebnis(geheim,
 					ratekombi))) {
 				anzahlUebrigerMoeglichkeiten++;
 			}
 		}
 		return anzahlUebrigerMoeglichkeiten;
 	}
 
 
 	/**
 	 * Errechne die niedrigste Anzahl an geheimen Kombinationen, die jede
 	 * geratene Kombination von den noch übrigen geheimen Möglichkeiten
 	 * entfernen würde.
 	 * 
 	 * TODO: höchste Anzahl an geheimen Kombinationen errechnen, die jede
 	 * geratene Kombination übriglassen würde.
 	 * 
 	 * @param geheimSet
 	 *            SpielKombination Set der noch möglichen Lösungen
 	 */
 	private void errechneScoring(final Set<SpielKombination> geheimSet) {
 		long score;
 		long tempscore;
 		scoreMap.clear();
 		for (final Iterator<SpielKombination> rateIterator = rateSet.iterator(); rateIterator
 				.hasNext();) {
 			final SpielKombination rate = rateIterator.next();
 			/*
 			 * Für übriggelassene Kombinationen müssen wir bei 0 anfangen.
 			 */
 			// score = 0;
 			score = MAXSCORE;
 			for (ErgebnisKombination ergebnis : ergebnisMoeglichkeiten) {
 				tempscore = zaehleEleminierteMoeglichkeiten(rate, ergebnis,
 						geheimSet);
 				/*
 				 * Für übriggelassene Kombinationen muss tempscore größer als score sein.
 				 */
 				// if (tempscore > score) {
 				if (tempscore < score) {
 					score = tempscore;
 				}
 			}
 			/*
 			 * Für übriggelassene Kombinationen muss score > 0 sein.
 			 */
 			//if (score > 0) {
 			if (score < MAXSCORE) {
 				scoreMap.put(rate, score);
 			} else {
 				rateIterator.remove();
 			}
 		}
 		if (scoreMap.size() < 1) {
 			throw new RuntimeException(
 					"Score Map hat weniger als einen Eintrag. Da stimmt was nicht...");
 		}
 	}
 
 	/**
 	 * Suche die SpielKombination mit der höchsten WorstCase-Anzahl an
 	 * entfernten Möglichkeiten.
 	 * 
 	 * TODO: Suche die SpielKombination mit der niedrigsten WorstCase-Anzahl an übriggelassenen Möglichkeiten.
 	 * 
 	 * @param geheimSet
 	 *            SpielKombination Set der noch möglichen Lösungen
 	 * @return SpielKombination welche das beste Ergebnis hervorbringen sollte
 	 */
 	private SpielKombination errechneBesteKombination(
 			final Set<SpielKombination> geheimSet) {
 		SpielKombination tempkomb = null;
 		/*
 		 * Für übriggelassene Möglichkeiten müssen wir bei MAXSCORE anfangen
 		 */
 		// long tempscore = MAXSCORE;
 		long tempscore = 0;
 		errechneScoring(geheimSet);
 		for (Entry<SpielKombination, Long> tempentry : scoreMap.entrySet()) {
 			/*
 			 * Für übriggelassene Möglichkeiten muss tempentry < tempscore sein
 			 */
 			// if(tempentry.getValue() < tempscore) {
 			if (tempentry.getValue() > tempscore) {
 				tempscore = tempentry.getValue();
 				tempkomb = tempentry.getKey();
 			}
 		}
 		return tempkomb;
 	}
 
 	@Override
 	public SpielKombination getNeuerZug() {
 		// Kandidatenmenge ist veränderlich und deshalb als Cache-Schlüssel ungeeignet
 		// Kopie der Kandidatenmenge als Schlüssel verwenden!
 		final Set<SpielKombination> key = new HashSet<SpielKombination>(geheimMoeglichkeiten); 
 		SpielKombination result = CACHE.get(key);
 		if (result == null) {
 			result = errechneBesteKombination(geheimMoeglichkeiten);
 			CACHE.put(key, result);
 		}
 		return result;
 	}
 
 	@Override
 	public void setLetzterZug(final SpielKombination zug,
 			final ErgebnisKombination antwort) {
 		if (((antwort.getSchwarz() + antwort.getWeiss()) > PINS)
 				|| (antwort.getSchwarz() == (PINS - 1) && antwort.getWeiss() == 1)) {
 			throw new IllegalArgumentException();
 		}
 		eleminiereMoeglichkeiten(zug, antwort, geheimMoeglichkeiten);
 	}
 }
