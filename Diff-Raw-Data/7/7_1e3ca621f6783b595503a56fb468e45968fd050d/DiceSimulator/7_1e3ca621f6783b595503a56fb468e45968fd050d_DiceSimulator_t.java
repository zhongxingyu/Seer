 /** Copyright by Barry G. Becker, 2000-2011. Licensed under MIT License: http://www.opensource.org/licenses/MIT  */
 package com.barrybecker4.simulation.dice;
 
import com.barrybecker4.common.app.AppContext;
 import com.barrybecker4.common.format.IntegerFormatter;
 import com.barrybecker4.common.math.function.LinearFunction;
 import com.barrybecker4.simulation.common.ui.DistributionSimulator;
 import com.barrybecker4.simulation.common.ui.SimulatorOptionsDialog;
 import com.barrybecker4.ui.renderers.HistogramRenderer;
import com.barrybecker4.ui.util.Log;

import java.util.Arrays;
 
 /**
  * Simulates the rolling of N number of M sided dice lots of times
  * to see what kind of distribution of numbers you get.
  *
  * @author Barry Becker
  */
 public class DiceSimulator extends DistributionSimulator {
 
     private int numDice_ = 2;
     private int numSides_ = 6;
 
 
     public DiceSimulator() {
         super("Dice Histogram");
        AppContext.initialize("ENGLISH", Arrays.asList("com.barrybecker4.ui.message"), new Log());
         initHistogram();
     }
 
     public void setNumDice(int numDice) {
         numDice_ = numDice;
         initHistogram();
     }
 
     public void setNumSides(int numSides) {
         numSides_ = numSides;
         initHistogram();
     }
 
     @Override
     protected void initHistogram() {
         data_ = new int[numDice_ * (numSides_-1) + 1];
         histogram_ = new HistogramRenderer(data_, new LinearFunction(1.0, -numDice_));
         histogram_.setXFormatter(new IntegerFormatter());
     }
 
     @Override
     protected SimulatorOptionsDialog createOptionsDialog() {
          return new DiceOptionsDialog( frame_, this );
     }
 
     @Override
     protected double getXPositionToIncrement() {
         int total = 0;
         for (int i=0; i < numDice_; i++) {
            total += random_.nextInt(numSides_) + 1;
         }
         return total;
     }
 
     public static void main( String[] args ) {
         final DiceSimulator sim = new DiceSimulator();
         sim.setNumDice(3);
         sim.setNumSides(6);
         runSimulation(sim);
     }
 }
