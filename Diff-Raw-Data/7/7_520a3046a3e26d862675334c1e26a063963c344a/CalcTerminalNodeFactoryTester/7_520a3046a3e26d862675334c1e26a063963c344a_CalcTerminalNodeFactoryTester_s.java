 /**
  * CalcTerminalNodeFactoryTester.java
  * 
  * Copyright 2009 Jeffrey Finkelstein
  * 
  * This file is part of jmona.
  * 
  * jmona is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * jmona is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * jmona. If not, see <http://www.gnu.org/licenses/>.
  */
 package jmona.example.calc.factories;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Random;
 
 import jmona.MappingException;
import jmona.Function;
 import jmona.example.calc.nodes.NumberNode;
 import jmona.example.calc.nodes.VariableNode;
 import jmona.functional.Range;
 import jmona.gp.TerminalNode;
 import jmona.test.Util;
 
 import org.junit.Test;
 
 /**
  * Test class for the CalcTerminalNodeFactory class.
  * 
  * @author Jeffrey Finkelstein
  */
 public class CalcTerminalNodeFactoryTester {
 
   /**
    * The maximum value for a NumberNode created from the
    * CalcTerminalNodeFactory.
    */
   public static final int MAX_VALUE = 100;
   /** The number of times to repeat the test. */
  public static final int NUM_TESTS = 1000;
 
   /**
    * Test method for
    * {@link jmona.example.calc.factories.CalcTerminalNodeFactory#createObject()}
    * .
    */
   @Test
   public void testCreateNode() {
     final CalcTerminalNodeFactory factory = new CalcTerminalNodeFactory();
 
     int numNumberNodes = 0;
     int numVariableNodes = 0;
 
     for (final int i : new Range(NUM_TESTS)) {
       final TerminalNode node = factory.createObject();
 
       assertTrue(node instanceof VariableNode || node instanceof NumberNode);
       assertEquals(0, node.arity());
       assertNull(node.children());
 
       if (node instanceof VariableNode) {
         numVariableNodes += 1;
       } else {
         numNumberNodes += 1;
       }
     }
 
     assertEquals(NUM_TESTS, numVariableNodes + numNumberNodes);
 
     final double mean = (numVariableNodes + numNumberNodes) / 2.0;
 
     final double delta = mean * 0.1;
 
     assertEquals(mean, numVariableNodes, delta);
     assertEquals(mean, numNumberNodes, delta);
 
   }
 
   /**
    * Test for
    * {@link jmona.example.calc.factories.CalcTerminalNodeFactory#setMinValue(int)}
    * and
    * {@link jmona.example.calc.factories.CalcTerminalNodeFactory#setMaxValue(int)}
    * .
    * 
    */
   @Test
   public void testSetMaxAndMinValues() {
     final CalcTerminalNodeFactory factory = new CalcTerminalNodeFactory();
 
     final Random random = new Random();
     int newMinValue = random.nextInt(MAX_VALUE);
    int newMaxValue = newMinValue + random.nextInt(MAX_VALUE);
 
     factory.setMinValue(newMinValue);
     factory.setMaxValue(newMaxValue);
 
     try {
 
       TerminalNode node = null;
       double value = 0.0;
       for (final int i : new Range(NUM_TESTS)) {
 
         node = factory.createObject();
 
         if (node instanceof NumberNode) {
           value = ((NumberNode) node).evaluate().execute(null);
           assertTrue(value <= newMaxValue && value >= newMinValue);
         }
       }
     } catch (final MappingException exception) {
       Util.fail(exception);
     }
   }
 }
