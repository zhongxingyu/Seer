 /*
  * FixedViewportDisplayTest2.java,  jJunit human display test 
  * for class RangePolicyFixedViewport. 
  * Copyright (c) 2007  Achim Westermann, Achim.Westermann@gmx.de
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Library General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Library General Public License for more details.
  *
  * You should have received a copy of the GNU Library General Public
  * License along with this library; if not, write to the Free
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *  If you modify or optimize the code in a useful way please let me know.
  *  Achim.Westermann@gmx.de
  */
 package info.monitorenter.gui.chart.demos;
 
import info.monitorenter.gui.chart.Chart2D;
 import info.monitorenter.gui.chart.IAxis;
 import info.monitorenter.gui.chart.ITrace2D;
 import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
 import info.monitorenter.gui.chart.traces.Trace2DSimple;
 import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;
 import info.monitorenter.gui.chart.traces.painters.TracePainterLine;
 import info.monitorenter.util.Range;
 
 import java.awt.Color;
 import java.io.IOException;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 /**
  * Test implementation that uses a chart with a
  * <code>{@link RangePolicyFixedViewport}</code> with a range from 0 to 100
  * for the x axis and a range from -40 to 40 for the y axis.
  * <p>
  * A {@link Trace2DSimple} that has a painter for discs and a painter for lines
  * (in that order) is used.
  * <p>
  * 
  * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
  * 
  * @version $Revision: 1.4 $
  * 
  */
 public class FixedViewportDisplayTest2
     extends ADisplayTestPropertyDataBased {
 
   /**
    * Main debug hook.
    * <p>
    * 
    * @param args
    *          ignored.
    * 
    * @throws IOException
    *           if something goes wrong reading data files.
    * 
    * @throws InterruptedException
    *           if sleeping fails.
    */
   public static void main(final String[] args) throws IOException, InterruptedException {
     FixedViewportDisplayTest2 test = new FixedViewportDisplayTest2(FixedViewportDisplayTest2.class
         .getName());
     try {
       test.testDisplay();
     } catch (Throwable f) {
       f.printStackTrace(System.err);
     }
   }
 
   /**
    * Test suite for this test class.
    * <p>
    * 
    * @return the test suite
    */
   public static Test suite() {
 
     TestSuite suite = new TestSuite();
     suite.setName(FixedViewportDisplayTest2.class.getName());
 
     suite.addTest(new FixedViewportDisplayTest2("testDisplay"));
 
     return suite;
   }
 
   /**
    * Creates a test case with the given name.
    * <p>
    * 
    * @param testName
    *          the name of the test case.
    */
   public FixedViewportDisplayTest2(final String testName) {
     super(testName);
   }
 
   /**
    * Sets up a {@link RangePolicyFixedViewport} with a range from 0 to 100 for
    * the x axis and a range from -40 to 40 for the y axis.
    * <p>
    * 
    * @see info.monitorenter.gui.chart.demos.ADisplayTest#configure(info.monitorenter.gui.chart.demos.StaticCollectorChart)
    */
   @Override
   protected void configure(final StaticCollectorChart chart) {
     IAxis axis = chart.getChart().getAxisX();
     axis.setRangePolicy(new RangePolicyFixedViewport());
     axis.setRange(new Range(0, 100));
     axis = chart.getChart().getAxisY();
     axis.setRangePolicy(new RangePolicyFixedViewport());
     axis.setRange(new Range(-40, 40));
   }
 
   /**
    * Returns a {@link Trace2DSimple} that has a painter for discs and a painter
    * for lines (in that order).
    * <p>
    * 
    * @return a {@link Trace2DSimple} that has a painter for discs and a painter
    *         for lines (in that order).
    * 
    * @see info.monitorenter.gui.chart.demos.ADisplayTest#createTrace()
    */
   @Override
   protected ITrace2D createTrace() {
	  Chart2D dummyChart = new Chart2D();
     ITrace2D result = new Trace2DSimple();
    dummyChart.addTrace(result);
     result.setTracePainter(new TracePainterDisc());
     result.addTracePainter(new TracePainterLine());
     result.setColor(Color.RED);
     return result;
   }
 
 }
