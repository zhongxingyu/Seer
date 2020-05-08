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
 package edu.pdx.cawhite.coordinates;
 
 import edu.pdx.cawhite.math.LengthException;
 import edu.pdx.cawhite.math.MatrixSizeException;
 import edu.pdx.cawhite.math.Vector;
 import edu.pdx.cawhite.coordinates.CoordinateMatrix;
 import edu.pdx.cawhite.iterators.IteratorException;
 
 /**
  * @author Cameron Brandon White
  */
 public class Functions2D {
      
     /**
      * @param coordinateMatrix  The matrix to translate.
      * @param x                 The amount to translate in the x direction.
      * @param y                 The amount to translate in the y direction.
      * @return                  The new matrix translated. 
      */
     public static CoordinateMatrix translate(
             CoordinateMatrix columnMatrix, Double x, Double y) 
             throws MatrixSizeException {
 
         TransformationMatrix translationMatrix = TransformationMatrices2D.translationMatrix(x, y);
         return translationMatrix.mul(columnMatrix);
     }
 
     /**
      * @param vector    The vector to translate.
      * @param x         The amount to translate in the x direction.
      * @param y         The amount to translate in the y direction.
      * @return          The new matrix translated. 
      */
     public static Coordinate translate(Coordinate vector, Double x, Double y) 
             throws LengthException {
         return new Coordinate(new double[] {vector.get(0)+x, vector.get(1)+y});
     }
 
     /**
      * @param vector    The matrix to translate.
      * @param t         The amount to rotate.
      * @return          The new matrix translated. 
      */
     public static Vector rotate(Vector vector, Double t) 
             throws LengthException {
     
         TransformationMatrix rotationMatrix = TransformationMatrices2D.rotationMatrix(t);        
         return rotationMatrix.mul(vector);
     }
 
     /**
      * @param coordinateMatrix  The matrix to translate.
      * @param t                 The amount to rotate.
      * @return                  The new matrix translated. 
      */
     public static CoordinateMatrix rotate(
             CoordinateMatrix columnMatrix, Double t) 
             throws MatrixSizeException {
     
         TransformationMatrix rotationMatrix = TransformationMatrices2D.rotationMatrix(t);        
         return rotationMatrix.mul(columnMatrix);
     }
 
     /**
      * @param vector    The matrix to translate.
      * @param t         The amount to rotate.
      * @param x         The x-component of the point to rotate around.
      * @param y         The y-component of the point to rotate around.
      * @return          The new matrix translated. 
      */
     public static Vector rotate(
             Vector vector, Double t, Double x, Double y) 
             throws LengthException {
         
         TransformationMatrix rotationMatrix = TransformationMatrices2D.rotationMatrix(t, x, y);
         return rotationMatrix.mul(vector);
     }
 
     /**
      * @param coordinateMatrix  The matrix to translate.
      * @param t                 The amount to rotate.
      * @param x                 The x-component of the point to rotate around.
      * @param y                 The y-component of the point to rotate around.
      * @return                  The new matrix translated. 
      */
     public static CoordinateMatrix rotate(
             CoordinateMatrix columnMatrix, Double t, Double x, Double y) 
             throws MatrixSizeException {
         
         TransformationMatrix rotationMatrix = TransformationMatrices2D.rotationMatrix(t, x, y);
         return rotationMatrix.mul(columnMatrix);
     }
     
     public static CoordinateMatrix line(
             Coordinate p, Coordinate d,
             double tStart, double tEnd, double tStep)
             throws LengthException {
 
         assert tEnd > tStart;
         assert tStep > 0.0;
 
         int numberOfRows = 3;
         int numberOfColumns = (int) ((tEnd - tStart) / tStep);
 
         if ((p.getLength() != numberOfRows) || 
             (d.getLength() != numberOfRows)) 
             throw new LengthException("Coordinate must be of length 3");
         
         CoordinateMatrix newMatrix = new CoordinateMatrix(numberOfRows,
                                                           numberOfColumns);
 
         CoordinateMatrix.Iterator matrixIterator = newMatrix.new Iterator();
 
         for (Double i = tStart; i <= tEnd; i += tStep) {
             try {
                 matrixIterator.set(p.add(d.mul(i)));
                 matrixIterator.next();
             } catch (IteratorException e) {
                 assert false : "Fatal programming error";
             } catch (LengthException e) {
                 assert false : "Fatal programming error";
             }
         }
 
         return newMatrix;
     }
 
     public static CoordinateMatrix plane(
             Coordinate p, 
             Coordinate d1, Double uStart, Double uEnd, Double uStep,
             Coordinate d2, Double vStart, Double vEnd, Double vStep) 
             throws LengthException {
 
         assert uEnd > uStart;
         assert vEnd > vStart;
         assert uStep > 0.0;
         assert vStep > 0.0;
 
         int numberOfRows = 3;
        int numberOfColumns = (int) Math.ceil(((uEnd - uStart) / uStep) * ((vEnd - vStart) / vStep));
 
         if ((p.getLength() != numberOfRows) || 
             (d1.getLength() != numberOfRows) ||
             (d2.getLength() != numberOfRows)) 
             throw new LengthException("Coordinate must be of length 4");
 
         CoordinateMatrix newMatrix = new CoordinateMatrix(numberOfRows,
                                                           numberOfColumns);
 
         CoordinateMatrix.Iterator matrixIterator = newMatrix.new Iterator();
 
        for (Double i = uStart; i < uEnd; i += uStep) {
            for (Double j = vStart; j < vEnd; j += vStep) {
                 try {
                     matrixIterator.set(p.add(d1.mul(i).add(d2.mul(j))));
                     matrixIterator.next();
                 } catch (IteratorException e) {
                     assert false : "Fatal programming error";
                 } catch (LengthException e) {
                     assert false : "Fatal programming error";
                 }
             }
         }
 
         return newMatrix;
     }
 
     public static CoordinateMatrix square(Coordinate point, double size) 
             throws LengthException {
 
         Coordinate d1 = new Coordinate(new double[] {1.0, 0.0}); 
         Coordinate d2 = new Coordinate(new double[] {0.0, 1.0}); 
 
         return plane(point, d1, 0.0, size, 1.0, d2, 0.0, size, 1.0);
     }
 
 }
