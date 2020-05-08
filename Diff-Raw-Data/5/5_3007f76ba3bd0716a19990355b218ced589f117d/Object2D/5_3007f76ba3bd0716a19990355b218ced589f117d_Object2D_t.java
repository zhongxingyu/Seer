 /* Copyright (C) 2013, Cameron White
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its contributors
  *    may be used to endorse or promote products derived from this software
  *    without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE PROJECT AND CONTRIBUTORS ``AS IS'' AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL THE PROJECT OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  */
 package edu.pdx.cawhite.simulator;
 
 import java.awt.Graphics;
 
 import edu.pdx.cawhite.coordinates.Coordinate;
 import edu.pdx.cawhite.coordinates.Functions2D;
 import edu.pdx.cawhite.math.LengthException;
 import edu.pdx.cawhite.math.MatrixSizeException;
 import edu.pdx.cawhite.iterators.IteratorException;
 
 /**
  * @author Cameron Brandon White
  */
 public class Object2D extends Object<Enviroment2D> {
 
     /**
      * Create a new 2D object.
      *
      * @param enviroment    The environment to add the object to.
      * @param items         The items.
      */
     public Object2D(Enviroment2D enviroment, Coordinate center, Double size) 
     		throws LengthException {
         this(center, size);
         this.enviroment = enviroment;
         this.enviroment.addObject(this);
     }
 
     protected Object2D(Coordinate center, Double size) 
     		throws LengthException {
         super(Functions2D.square(center, size));
         this.center = new Coordinate(new double[] 
             {center.get(0) + (size/2), center.get(1) + (size/2)});
     }
 
     /**
      * Paint the object onto the screen.
      *
      * @param g     
      */
     public void paint(Graphics g) {
 
         Iterator iterator = this.new Iterator();
 
        while (true) {
             try {
                 Coordinate coordinate = iterator.get();
                 int x = (int) Math.round(coordinate.get(0));
                 int y = (int) Math.round(coordinate.get(1));
                 g.drawRect(x, y, 1, 1);
                 iterator.next();
             } catch (IteratorException e) {
                 break;
             }
        }
 
 
     }
 
     /**
      * @param x     The amount to translate on the x-axis.
      * @param y     The amount to translate on the y-axis. 
      */
     public synchronized void translate(Double x, Double y) {
         try {
             this.set(Functions2D.translate(this, x, y));
             this.center = Functions2D.translate(this.center, x, y);
         } catch (MatrixSizeException e) {
             assert false;
         } catch (LengthException e) {
         	assert false;
         }
     }
 
     /**
      * @param theta     The amount to rotate about (0,0).
      */
     public void rotate(Double theta) {
         try {
             double x = this.center.get(0);
             double y = this.center.get(1);
             this.set(Functions2D.rotate(this, theta, x, y));
         } catch (MatrixSizeException e) {
             assert false;
         }
     }
 
     /**
      * @param theta     The amount to rotate about the given point.
      * @param x         The x component of the point to rotate around.
      * @param y         The y component of the point to rotate around.
      */
     public void rotate(Double theta, Double x, Double y) {
     }
 }
