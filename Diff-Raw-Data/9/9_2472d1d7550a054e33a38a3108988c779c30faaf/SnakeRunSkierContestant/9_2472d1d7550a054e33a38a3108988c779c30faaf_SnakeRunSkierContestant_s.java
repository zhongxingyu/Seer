 /**
  *                This file is part of Laserinne.
  * 
  *  Laser projections in public space, inspiration and
  *  information, exploring the aesthetic and interactive possibilities of
  *  laser-based displays.
  * 
  *  http://www.laserinne.com/
  * 
  * Laserinne is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Laserinne is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Laserinne. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.laserinne.snakerun;
 
 import processing.core.PVector;
 
 import com.laserinne.util.Mover;
 import com.laserinne.util.SkierContestant;
 
 /**
  * SnakeRunSkierContestant implements SkierContestant for SnakeRun.
  * 
  * @author Jyrki Lilja
  */
 
 public class SnakeRunSkierContestant extends SkierContestant {
     private boolean inSnake;
     private long negativePointsStarted;
     
     private static final float POINT_WEIGHT = 0.1f;
     
     public SnakeRunSkierContestant() {
         super();
     }
     
     public boolean inSnake(Snake snake) {
         this.update();
         this.inSnake = false;
         int followerCount = snake.followerCount(),
             /**
              * Allow the skier to be between 1 / 2 and 2 / 3 of the snake. If
              * the skier is faster accelerate the snake and vice versa.
              */
             lastAllowedIndex = followerCount * 2 / 3,
             firstAllowedIndex = followerCount * 1 / 2;
         Mover m = snake.head();
         while (m.follower() != null) {
             if (m.closeTo(new PVector(this.position().x, this.position().y), 10)) {
                 if (m.index() > lastAllowedIndex) {
                     snake.changeTopSpeed(-0.02f);
                 }
                 if (m.index() < firstAllowedIndex) {
                     snake.changeTopSpeed(0.01f);
                 }
                 this.inSnake = true;
                 break;
             }
             m = m.follower();
         }
         if (!this.inSnake) {
             if (this.negativePointsStarted == 0) {
                 this.negativePointsStarted = System.currentTimeMillis();
             }
             snake.changeTopSpeed(-0.02f);
         }
         return this.inSnake;
     }
     
     public void draw(processing.core.PGraphics g) {
         int originalStroke = g.strokeColor;
         if (this.inSnake) {
             g.stroke(0xFF00FF00);
         }
         super.draw(g);
         g.stroke(originalStroke);
     }
     
     public void reset() {
         super.reset();
         this.negativePointsStarted = 0;
     }
 
     @Override
     public float scoreToSeconds() {
         return this.score * -0.25f * SnakeRunSkierContestant.POINT_WEIGHT;
     }
 
     @Override
     public void updateScore() {
        if (this.running && this.negativePointsStarted != 0 && System.currentTimeMillis() - this.negativePointsStarted > 100) {
             this.score--;
             System.out.println("Added minus point. Score now " + this.score);
             this.negativePointsStarted = 0;
         }
     }
 }
