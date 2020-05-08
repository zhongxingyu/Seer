 package org.hitzemann.mms.model;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Random;
 
 import org.junit.Test;
 
 /**
  * Tests für {@link SpielKombination}.
  * 
  * @author schusterc
  */
 public final class SpielKombinationTest {
 
 	/**
 	 * Die maximale Länge der zufälligen Kombinationen.
 	 */
 	private static final int MAX_PINS = 30;
 
 	/**
 	 * Die größe der Stichprobe für die Tests.
 	 */
 	private static final int SAMPLES = 100000;
 
 	/**
 	 * Erzeugt eine zufällige {@link SpielKombination} mit zufälliger Länge
 	 * (maximal {@value #MAX_PINS}).
 	 * 
 	 * @param rng
 	 *            Der zu verwendende Zufallszahlengenerator
 	 * @return Die zufällige {@link SpielKombination}.
 	 */
 	private SpielKombination createRandomSpielKombination(final Random rng) {
 		final SpielStein[] alleFarben = SpielStein.values();
 
 		final int groesse = rng.nextInt(MAX_PINS);
 
 		final SpielStein[] steine = new SpielStein[groesse];
 		for (int j = 0; j < groesse; j++) {
 			steine[j] = alleFarben[rng.nextInt(alleFarben.length)];
 		}
 
 		return new SpielKombination(steine);
 	}
 
 	/**
 	 * Testet stichprobenartig, ob die Relation { (a,b) | a.compareTo(b) <= 0 }
 	 * antisymmetrisch ist.
 	 */
 	@Test
 	public void testCompareToAntisymmetric() {
 		final Random rng = new Random(1);
 
 		for (int i = 0; i < SAMPLES; i++) {
 			final SpielKombination a = createRandomSpielKombination(rng);
 			final SpielKombination b = createRandomSpielKombination(rng);
 			assertFalse("compareTo not antisymmetric for a = " + a + ", b = "
 					+ b,
 					a.compareTo(b) <= 0 && b.compareTo(a) <= 0 && !a.equals(b));
 		}
 	}
 
 	/**
 	 * Testet stichprobenartig, ob die Relation { (a,b) | a.compareTo(b) <= 0 }
 	 * transitiv ist.
 	 */
 	@Test
 	public void testCompareToTransitive() {
 		final Random rng = new Random(1);
 
 		for (int i = 0; i < SAMPLES; i++) {
 			final SpielKombination a = createRandomSpielKombination(rng);
 			final SpielKombination b = createRandomSpielKombination(rng);
 			final SpielKombination c = createRandomSpielKombination(rng);
 			assertFalse(
 					"compareTo not transitive for a = " + a + ", b = " + b,
 					a.compareTo(b) <= 0 && b.compareTo(c) <= 0
 							&& !(a.compareTo(c) <= 0));
 		}
 	}
 
 	/**
 	 * Testet stichprobenartig, ob die Relation { (a,b) | a.compareTo(b) <= 0 }
 	 * total ist.
 	 */
 	@Test
 	public void testCompareToTotal() {
 		final Random rng = new Random(1);
 
 		for (int i = 0; i < SAMPLES; i++) {
 			final SpielKombination a = createRandomSpielKombination(rng);
 			final SpielKombination b = createRandomSpielKombination(rng);
 			assertTrue("compareTo not total for a = " + a + ", b = " + b,
 					a.compareTo(b) <= 0 || b.compareTo(a) <= 0);
 		}
 	}
 
 	/**
 	 * Testet stichprobenartig, ob
 	 * {@link SpielKombination#compareTo(SpielKombination)} konsistent zu
 	 * {@link SpielKombination#equals(Object)} ist.
 	 */
 	@Test
 	public void testCompareToConsistentWithEquals() {
 		final Random rng = new Random(1);
 
 		for (int i = 0; i < SAMPLES; i++) {
 			final SpielKombination a = createRandomSpielKombination(rng);
 			final SpielKombination b = createRandomSpielKombination(rng);
 
 			final int cmp = a.compareTo(b);
 			final boolean eq = a.equals(b);
 
 			assertEquals("compareTo inconsistent with equals for a = " + a
 					+ ", b = " + b, cmp == 0, eq);
 		}
 	}
 
 	/**
 	 * Testet ob die beiden SpielKombination Konstruktoren die gleiche Objekte
 	 * erzeugen.
 	 */
 	@Test
 	public void testConstructorsDoTheSame() {
 		// TODO: Schleife für mehrere Kombinationen...
 		final SpielKombination a = new SpielKombination(SpielStein.ROT,
 				SpielStein.ROT, SpielStein.GRUEN, SpielStein.GRUEN);
 		final SpielKombination b = new SpielKombination(1, 1, 2, 2);
 
 		assertEquals("Constructors do not generate same objects a = " + a
 				+ ", b= " + b, a, b);
 	}
 }
