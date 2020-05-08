 /*
  * Copyright (C) 2012 Felix Wiemuth
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package circles.backend;
 
 import circles.api.AbstractSimulator;
 import circles.api.Circle;
 import circles.api.Collision;
 import circles.api.CollisionHandler;
 
 /**
  *
  * @author Felix Wiemuth
  */
 public class Simulator extends AbstractSimulator {
 
     private int stepPrecision = 1; //number of calculations to perform per step (call of 'simulate()'
     private double stepFactor = 1 / stepPrecision; // 1/stepPrecision
     private int stepInterval = 100; //milliseconds between two simulation steps
     private long lastStep = System.currentTimeMillis(); //system time when last step was simulated
     boolean run = false;
 
     public Simulator(CollisionHandler collisionHandler) {
         super(collisionHandler);
     }
 
     @Override
     public void simulate() {
         if (!run) {
             return;
         }
         long time = System.currentTimeMillis(); //take time snapshot: avoid infinite loop if calculation is slower then time progression
         while (lastStep < time) {
             lastStep += stepInterval;
             calculateStep();
         }
     }
 
     private void calculateStep() {
         for (int i = 0; i < stepPrecision; i++) {
             for (Circle c : circles) {
                c.move(stepPrecision);
             }
             for (Collision c : getCollisions()) {
                 collisionHandler.handleCollision(c, circles);
             }
         }
     }
 
     @Override
     public Iterable<Circle> getCircles() {
         return circles;
     }
 
     @Override
     public void addCirlce(Circle circle) {
         circles.add(circle);
     }
 
     @Override
     public void play() {
         lastStep = System.currentTimeMillis();
         run = true;
     }
 
     @Override
     public void pause() {
         run = false;
     }
 
     public void setPrecision(int precision) {
         stepPrecision = precision;
        stepFactor = 1 / stepPrecision;
     }
 
     /**
      * Lower number is faster.
      * @param speed 
      */
     public void setSpeed(int speed) {
         this.stepInterval = speed;
     }
     
     
 }
