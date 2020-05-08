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
 
 package com.laserinne.util;
 
 import processing.core.PConstants;
 import processing.core.PVector;
 
 /**
  * SkierContestant is an abstract class to support skier contestants.
  * 
  * Gives basic support to checking if contestant has crossed finish line,
  * timing the race and determining the winner.
  * 
  * Has method stubs for score tracking and weighting.
  * 
  * Usage:
  * 
  *  First, implement all abstract methods.
  *  
  *  Before using anything, call this.startLine(int) and this.finishLine(int)
  *  to set start and finish lines.
  *  
  *  Call constantly this.update() to get updated info on skier status. This
  *  method also starts the skier automatically.
  *  
  *  Constantly call this.finished() to figure out if the skier has finished.
  *  When both have finished, call this.winner() to get string telling the
  *  winner and its time.
  * 
  * @author Jyrki Lilja
  */
 
 public abstract class SkierContestant {
     protected boolean running;
     protected boolean finished;
     protected long startTime;
     protected long finishTime;
     protected int score;
     protected Skier skier;
     
     protected static int startLine;
     protected static int finishLine;
     
     /**
      * Constructor, sets fields and original position.
      * @param x original position
      * @param y - '' -
      */
     public SkierContestant() {
         this.reset();
     }
     
     /**
      * Setter for ``skier''.
      * Call this in every draw loop and provide the correct skier for the
      * contestant.
      * @param skier
      */
     public void skier(Skier skier) {
         this.skier = skier;
     }
     
     /**
      * @return position of the skier.
      */
     public PVector position() {
         if (skier == null) {
             return new PVector(-100, -100);
         }
     	return new PVector(skier.getX(), skier.getY());
     }
     
     /**
      * @return startLine
      */
     public static int startLine() {
         return SkierContestant.startLine;
     }
     
     /**
      * Use this to set the start line *before* any use of the class.
      * Timing starts when the skier has crossed start line.
      * @param startLine
      */
     public static void startLine(int startLine) {
         SkierContestant.startLine = startLine;
     }
     
     /**
      * Use this to set the finish line *before* any use of the class.
      * Timing ends when the skier has crossed finish line.
      * @param finishLine
      */
     public static void finishLine(int finishLine) {
         SkierContestant.finishLine = finishLine;
     }
     
     /**
      * Use this to check if the skier has finished. When both are finished
      * call SkierContestant.winner.
      * @return has the skier finished
      */
     public boolean finished() {
         if (this.finished == false && this.position().y >= SkierContestant.finishLine) {
             this.running = false;
             this.finished = true;
             this.finishTime = System.currentTimeMillis();
         }
         return this.finished;
     }
     
     /**
      * Getter for running.
      * @return is the skier running
      */
     public boolean running() {
         return this.running;
     }
     
     /**
      * Setter for running.
      * @param running boolean is the skier running?
      */
     public void running(boolean running) {
         this.running = running;
     }
     
     /**
      * Starts running the race. Has to be called when the skier starts the race.
      */
     public void start() {
         System.out.printf("Skier %s started.\n", this);
         this.running = true;
         this.startTime = System.currentTimeMillis();
     }
     
     /**
      * Times the race
      * @return time it took to run the race
      */
     public float timeInSeconds() {
         return (this.finishTime - this.startTime) / 1000.0f;
     }
     
     /**
      * Reset the skier fully.
      */
     public void reset() {
         this.running = false;
         this.finished = false;
         this.startTime = 0;
         this.score = 0;
     }
     
     /**
      * Draws the skier.
      * @param g PGraphics instance that's used for drawing
      */
     public void draw(processing.core.PGraphics g) {
         //g.ellipseMode(processing.core.PConstants.CENTER);
         //g.ellipse(this.getX(), this.getY(), 10, 10);
     	g.beginShape();
     		g.vertex(position().x, position().y);
     		g.vertex(position().x+5, position().y);
     		g.vertex(position().x+5, position().y+5);
     		g.vertex(position().x, position().y+5);
     	g.endShape(PConstants.CLOSE);
     }
     
     /**
      * Updates the skier:
      *  - score
      *  - running status (start & stop)
      */
     public void update() {
         this.updateScore();
        if (this.running == false && this.finished == false && this.position().y >= SkierContestant.startLine) {
             this.start();
         }
         this.finished();
     }
     
     /**
      * Score getter.
      * @return skier score
      */
     public int score() {
         return this.score;
     }
     
     /**
      * This method returns the skier contestant's score converted to seconds.
      * Negative score should return positive value (that is they should add
      * time to the run). +/-0.25 seconds/score point is recommended, depending
      * of course on the score calculation method.
      * 
      * @return score converted to seconds.
      */
     public abstract float scoreToSeconds();
     
     /**
      * This method updates the score. Use whatever method you wish to
      * determine whether point should be added or not.
      * 
      * NOTE: Please remember that this method gets called on every frame so you
      * might accidentally add/remove way too many score points! So use some way
      * to check if the score is being changed by the same event (like running
      * outside the track etc).
      */
     public abstract void updateScore();
     
     /**
      * Combines the time it took to run the slope and the score converted to
      * seconds.
      * @return time & score converted to seconds, precision is 0.001.
      */
     public float combinedTimeAndScore() {
         return (Math.round((this.timeInSeconds() + this.scoreToSeconds()) * 1000)) / 1000.0f;
     }
     
     /**
      * Determines winner and returns String telling winner.
      * @param leftSkier
      * @param rightSkier
      * @return String telling witch skier won (left or right) plus the time. 
      */
     public static String winner(SkierContestant leftSkier, SkierContestant rightSkier) {
         String finishNote;
         if (leftSkier.combinedTimeAndScore() < rightSkier.combinedTimeAndScore()) {
             // Left skier (Player 1) wins
             finishNote = "Player 1 wins (" + leftSkier.combinedTimeAndScore() + " s)";
         } else if (rightSkier.combinedTimeAndScore() < leftSkier.combinedTimeAndScore()) {
             // Right skier (Player 2) wins
             finishNote = "Player 2 wins (" + rightSkier.combinedTimeAndScore() + " s)";
         } else {
             // Draw
             finishNote = "Draw";
         }
         return finishNote;
     }
     
     /**
      * closeTo
      * 
      * Determines whether this is inside target radius.
      * 
      * @param target PVector telling the target
      * @param radius in pixels
      * @return boolean Close to target?
      */
     public boolean closeTo(PVector target, float radius) {
         PVector diff = PVector.sub(target, new PVector(this.position().x, this.position().y));
         if (diff.mag() < radius) {
             return true;
         } else {
             return false;
         }
     }
     
     /**
      * closeTo
      * 
      * Simplified version of closeTo(PVector target, float radius). Accepts only
      * target, radius defaults to 10.0f.
      * 
      * @param target
      * @return boolean Within 10.0f to target.
      */
     public boolean closeTo(PVector target) {
         return this.closeTo(target, 10);
     }
 }
