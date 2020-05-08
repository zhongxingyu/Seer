 package org.drools.planner.examples.ras2012.model.original;
 
 import java.math.BigDecimal;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.concurrent.TimeUnit;
 
 import junit.framework.Assert;
 import org.drools.planner.examples.ras2012.model.original.Train.Type;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class TrainTest {
 
     @BeforeClass
     public static void setSpeeds() {
         Track.setSpeed(Track.MAIN_0, 100, 50);
         Track.setSpeed(Track.MAIN_1, 85, 75);
         Track.setSpeed(Track.MAIN_2, 110, 95);
         Track.setSpeed(Track.SIDING, 25);
         Track.setSpeed(Track.SWITCH, 35);
         Track.setSpeed(Track.CROSSOVER, 45);
     }
 
     private Arc[] getArcs(final Node n1, final Node n2, final BigDecimal length) {
         // prepare arcs, all of the same length
         final Arc mainArc0 = new Arc(Track.MAIN_0, length, n1, n2);
         final Arc mainArc1 = new Arc(Track.MAIN_0, length, n1, n2);
         final Arc mainArc2 = new Arc(Track.MAIN_0, length, n1, n2);
         final Arc sidingArc = new Arc(Track.SIDING, length, n1, n2);
         final Arc switchArc = new Arc(Track.SWITCH, length, n1, n2);
         final Arc crossoverArc = new Arc(Track.CROSSOVER, length, n1, n2);
         return new Arc[] { mainArc0, mainArc1, mainArc2, sidingArc, switchArc, crossoverArc };
     }
 
     private Train[] getTrains(final Node n1, final Node n2) {
         // now prepare various trains
         final Random rand = new Random(0); // speed multipliers will be random, but with a fixed seed, so repeatable
         final Train aTrainWest = new Train("A1", BigDecimal.ONE, BigDecimal.valueOf(rand
                 .nextDouble()), 90, n2, n1, 0, 0, 0, null, true, true);
         final Train bTrainWest = new Train("B1", BigDecimal.ONE, BigDecimal.valueOf(rand
                 .nextDouble()), 90, n2, n1, 0, 0, 0, null, true, true);
         final Train cTrainWest = new Train("C1", BigDecimal.ONE, BigDecimal.valueOf(rand
                 .nextDouble()), 90, n2, n1, 0, 0, 0, null, true, true);
         final Train dTrainWest = new Train("D1", BigDecimal.ONE, BigDecimal.valueOf(rand
                 .nextDouble()), 90, n2, n1, 0, 0, 0, null, true, true);
         final Train eTrainWest = new Train("E1", BigDecimal.ONE, BigDecimal.valueOf(rand
                 .nextDouble()), 90, n2, n1, 0, 0, 0, null, true, true);
         final Train fTrainWest = new Train("F1", BigDecimal.ONE, BigDecimal.valueOf(rand
                 .nextDouble()), 90, n2, n1, 0, 0, 0, null, true, true);
         final Train aTrainEast = new Train("A2", BigDecimal.ONE, BigDecimal.valueOf(rand
                 .nextDouble()), 90, n1, n2, 0, 0, 0, null, true, false);
         final Train bTrainEast = new Train("B2", BigDecimal.ONE, BigDecimal.valueOf(rand
                 .nextDouble()), 90, n1, n2, 0, 0, 0, null, true, false);
         final Train cTrainEast = new Train("C2", BigDecimal.ONE, BigDecimal.valueOf(rand
                 .nextDouble()), 90, n1, n2, 0, 0, 0, null, true, false);
         final Train dTrainEast = new Train("D2", BigDecimal.ONE, BigDecimal.valueOf(rand
                 .nextDouble()), 90, n1, n2, 0, 0, 0, null, true, false);
         final Train eTrainEast = new Train("E2", BigDecimal.ONE, BigDecimal.valueOf(rand
                 .nextDouble()), 90, n1, n2, 0, 0, 0, null, true, false);
         final Train fTrainEast = new Train("F2", BigDecimal.ONE, BigDecimal.ONE, 90, n1, n2, 0, 0,
                 0, null, true, false);
         return new Train[] { aTrainWest, bTrainWest, cTrainWest, dTrainWest, eTrainWest,
                 fTrainWest, aTrainEast, bTrainEast, cTrainEast, dTrainEast, eTrainEast, fTrainEast };
     }
 
     @Test
     public void testCompareTo() {
         final Node n1 = Node.getNode(0);
         final Node n2 = Node.getNode(1);
         final Train aTrain2 = new Train("A2", BigDecimal.ONE, BigDecimal.ONE, 90, n2, n1, 0, 0, 0,
                 null, true, true);
         final Train aTrain110 = new Train("A110", BigDecimal.TEN, BigDecimal.TEN, 100, n1, n2, 0,
                 0, 0, null, false, false);
         final Train bTrain1 = new Train("B1", BigDecimal.TEN, BigDecimal.TEN, 100, n1, n2, 0, 0, 0,
                 null, false, false);
         // test ordering by the first letter
         Assert.assertEquals("Trains should be ordered alphabetically.", 0,
                 aTrain2.compareTo(aTrain2));
         Assert.assertEquals("Trains should be ordered alphabetically.", -1,
                 aTrain110.compareTo(bTrain1));
         Assert.assertEquals("Trains should be ordered alphabetically.", 1,
                 bTrain1.compareTo(aTrain110));
         // test ordering by the number
         Assert.assertEquals(
                 "Trains should be ordered alphabetically by first letter, then numerically.", -1,
                 aTrain2.compareTo(aTrain110));
         Assert.assertEquals(
                 "Trains should be ordered alphabetically by first letter, then numerically.", 1,
                 aTrain110.compareTo(aTrain2));
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorEntryTime() {
         new Train("A1", BigDecimal.ONE, BigDecimal.ONE, 90, Node.getNode(0), Node.getNode(1), -1,
                 1, 0, null, true, false);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorLengthNull() {
         new Train("A1", null, BigDecimal.ONE, 90, Node.getNode(0), Node.getNode(1), 0, 1, 0, null,
                 true, false);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorLengthZero() {
         new Train("A1", BigDecimal.ZERO, BigDecimal.ONE, 90, Node.getNode(0), Node.getNode(1), 0,
                 1, 0, null, true, false);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorMultiplierNull() {
         new Train("A1", BigDecimal.ONE, null, 90, Node.getNode(0), Node.getNode(1), 0, 1, 0, null,
                 true, false);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorMultiplierZero() {
         new Train("A1", BigDecimal.ONE, BigDecimal.ZERO, 90, Node.getNode(0), Node.getNode(1), 0,
                 1, 0, null, true, false);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorNullNode1() {
         new Train("A1", BigDecimal.ONE, BigDecimal.ONE, 0, null, Node.getNode(0), 0, 1, 0, null,
                 true, false);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorNullNode2() {
         new Train("A1", BigDecimal.ONE, BigDecimal.ONE, 0, Node.getNode(0), null, 0, 1, 0, null,
                 true, false);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorSameNodes() {
         new Train("A1", BigDecimal.ONE, BigDecimal.ONE, 0, Node.getNode(0), Node.getNode(0), 0, 1,
                 0, null, true, false);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorTOB() {
         new Train("A1", BigDecimal.ONE, BigDecimal.ONE, 0, Node.getNode(0), Node.getNode(1), 0, 1,
                 0, null, true, false);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorTrainNameNegativeNumber() {
         new Train("C-1", BigDecimal.ONE, BigDecimal.ONE, 90, Node.getNode(0), Node.getNode(1), 0,
                 1, 0, null, true, false);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorTrainNameNotNumber() {
         new Train("BX", BigDecimal.ONE, BigDecimal.ONE, 90, Node.getNode(0), Node.getNode(1), 0, 1,
                 0, null, true, false);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorTrainNameNull() {
         new Train(null, BigDecimal.ONE, BigDecimal.ONE, 90, Node.getNode(0), Node.getNode(1), 0, 1,
                 0, null, true, false);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorTrainNameWrongLetter() {
         new Train("X1", BigDecimal.ONE, BigDecimal.ONE, 90, Node.getNode(0), Node.getNode(1), 0, 1,
                 0, null, true, false);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorWantTime() {
         new Train("A1", BigDecimal.ONE, BigDecimal.ONE, 90, Node.getNode(0), Node.getNode(1), 0,
                 -1, 0, null, true, false);
     }
 
     @Test
     public void testDirection() {
         final Train east = new Train("A1", BigDecimal.ONE, BigDecimal.ONE, 90, Node.getNode(0),
                 Node.getNode(1), 0, 1, 0, null, true, false);
         Assert.assertTrue(east.isEastbound());
         Assert.assertFalse(east.isWestbound());
         final Train west = new Train("A2", BigDecimal.ONE, BigDecimal.ONE, 90, Node.getNode(0),
                 Node.getNode(1), 0, 1, 0, null, true, true);
         Assert.assertTrue(west.isWestbound());
         Assert.assertFalse(west.isEastbound());
     }
 
     @Test
     public void testEqualsObject() {
         final Node n1 = Node.getNode(0);
         final Node n2 = Node.getNode(1);
         final Train aTrain = new Train("A1", BigDecimal.ONE, BigDecimal.ONE, 90, n2, n1, 0, 0, 0,
                 null, true, true);
         final Train aTrain2 = new Train("A1", BigDecimal.TEN, BigDecimal.TEN, 100, n1, n2, 0, 0, 0,
                 null, false, false);
         Assert.assertFalse("Train shouldn't equal non-train.", aTrain.equals(new String()));
         Assert.assertFalse("Train shouldn't equal null.", aTrain.equals(null));
         Assert.assertEquals("Train should equal itself.", aTrain, aTrain);
         Assert.assertEquals("Trains with same name should equal.", aTrain, aTrain2);
         Assert.assertEquals("Trains with same name should equal reflexively.", aTrain2, aTrain);
         final Train bTrain = new Train("B1", BigDecimal.ONE, BigDecimal.ONE, 90, n2, n1, 0, 0, 0,
                 null, true, true);
         Assert.assertFalse("Trains with different names shouldn't equal.", aTrain.equals(bTrain));
         Assert.assertFalse("Trains with different names shouldn't equal reflexively.",
                 bTrain.equals(aTrain));
     }
 
     @Test
     public void testGetArcTravellingTimeInMilliseconds() {
         final Node n1 = Node.getNode(0);
         final Node n2 = Node.getNode(1);
         final BigDecimal length = new BigDecimal("1.5");
         for (final Arc a : this.getArcs(n1, n2, length)) {
             for (final Train t : this.getTrains(n1, n2)) {
                 final BigDecimal distanceInMiles = a.getLengthInMiles();
                 final int trainSpeedInMph = t.getMaximumSpeed(a.getTrack());
                 final BigDecimal timeInHours = distanceInMiles.divide(
                        BigDecimal.valueOf(trainSpeedInMph), 7, BigDecimal.ROUND_HALF_EVEN);
                 final BigDecimal timeInMilliseconds = timeInHours.multiply(BigDecimal.valueOf(60))
                         .multiply(BigDecimal.valueOf(60)).multiply(BigDecimal.valueOf(1000));
                final long result = timeInMilliseconds.setScale(0, BigDecimal.ROUND_HALF_EVEN)
                        .longValue();
                 Assert.assertEquals(t + " didn't travel " + a + " in expected time.", result,
                         t.getArcTravellingTime(a, TimeUnit.MILLISECONDS));
             }
         }
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testGetArcTravellingTimeInMillisecondsWithNullArc() {
         final Train t = this.getTrains(Node.getNode(0), Node.getNode(1))[0];
         t.getArcTravellingTime(null, TimeUnit.MILLISECONDS);
     }
 
     @Test
     public void testGetMaximumSpeed() {
         final Node n1 = Node.getNode(0);
         final Node n2 = Node.getNode(1);
         final BigDecimal length = new BigDecimal("1.5");
         for (final Arc a : this.getArcs(n1, n2, length)) {
             for (final Train t : this.getTrains(n1, n2)) {
                 final Integer arcSpeed = t.isEastbound() ? a.getTrack().getSpeedEastbound() : a
                         .getTrack().getSpeedWestbound();
                 if (a.getTrack().isMainTrack()) {
                     final BigDecimal multiplier = t.getSpeedMultiplier();
                     final Integer expectedSpeed = multiplier.multiply(BigDecimal.valueOf(arcSpeed))
                            .setScale(0, BigDecimal.ROUND_HALF_EVEN).intValue();
                     Assert.assertEquals(
                             "Outside main tracks, the train max speed should equal (arc speed)x(train speed multiplier).",
                             expectedSpeed, t.getMaximumSpeed(a.getTrack()));
                 } else {
                     Assert.assertEquals(
                             "Outside main tracks, the train max speed should equal arc speed.",
                             arcSpeed, t.getMaximumSpeed(a.getTrack()));
                 }
                 Assert.assertEquals(
                         "Maximum speed with unspecified track should return the highest possible train speed.",
                         t.getMaximumSpeed(), t.getMaximumSpeed(Track.MAIN_0));
             }
         }
     }
 
     @Test
     public void testGetType() {
         final Map<String, Type> types = new HashMap<String, Type>();
         types.put("A", Type.A);
         types.put("B", Type.B);
         types.put("C", Type.C);
         types.put("D", Type.D);
         types.put("E", Type.E);
         types.put("F", Type.F);
         final Random rand = new Random();
         for (final Map.Entry<String, Type> e : types.entrySet()) {
             final String name = e.getKey() + Math.max(1, rand.nextInt());
             final Train t = new Train(name, BigDecimal.ONE, BigDecimal.ONE, 90, Node.getNode(0),
                     Node.getNode(1), 0, 1, 0,
                     Collections.<ScheduleAdherenceRequirement> emptyList(), true, false);
             Assert.assertEquals("Train doesn't have the proper type.", e.getValue(), t.getType());
             // schedule adherence by train type is specified by the problem definition
             final boolean adheresToSchedule = !(e.getKey().equals("E") || e.getKey().equals("F"));
             Assert.assertEquals("Train doesn't adhere to schedule as expected.", adheresToSchedule,
                     t.getType().adhereToSchedule());
         }
     }
 
     @Test
     public void testIsHeavy() {
         final Train heavy = new Train("A1", BigDecimal.ONE, BigDecimal.ONE, 110, Node.getNode(0),
                 Node.getNode(1), 0, 1, 0, null, true, false);
         Assert.assertTrue(heavy.isHeavy());
         final Train light = new Train("A2", BigDecimal.ONE, BigDecimal.ONE, 90, Node.getNode(0),
                 Node.getNode(1), 0, 1, 0, null, true, true);
         Assert.assertFalse(light.isHeavy());
     }
 
 }
