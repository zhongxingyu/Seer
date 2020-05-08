/*
 * Copyright 2012 Jacques Fontignie
 *
 * This file is part of plyrenderer.
 *
 * plyrenderer is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * plyrenderer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with plyrenderer. If not, see http://www.gnu.org/licenses/.
 */

 package org.plyrenderer.client;
 
 /**
  * Author: Jacques Fontignie
  * Date: 5/19/12
  * Time: 9:49 PM
  */
 public class Camera {
 
 
 /*
  * Code translated from: http://mdkey.org/?p=406
  * Copyright: Mark Dwyer (markus.dwyer@gmail.com)
  */
 
     private Matrix3d viewRotationMatrix = new Matrix3d();
     Matrix3d worldTransform = new Matrix3d();
 
     private Vector3d position = new Vector3d();
     private Vector3d focalPoint = new Vector3d();
     private Vector3d directionOfProjection = new Vector3d();
     private double distance;
 
     private int viewportWidth;
     private int viewportHeight;
     double alpha;
     double beta;
     double aspect_ratio;
     double view_dist;
 
     private Vector3d WorldUp = new Vector3d(0.0, 1.0, 0.0);
 
     public void setWindow(int w, int h) {
         viewportWidth = w;
         viewportHeight = h;
         aspect_ratio = viewportWidth / viewportHeight;
         alpha = (0.5 * viewportWidth - 0.5);
         beta = (0.5 * viewportHeight - 0.5);
         view_dist = (0.5) * 2.0 * Math.tan(90.0 / 2 * Math.PI / 180.0);
     }
 
     public int getViewportWidth() {
         return viewportWidth;
     }
 
     public int getViewportHeight() {
         return viewportHeight;
     }
 
     /*
     * Build a transform as if you were at a point (x1,y1,z1), and
     * looking at a point (x2,y2,z2)
     */
     public int lookAt(Vector3d pos, Vector3d at) {
         // the View or "new Z" vector
         Vector3d ViewOut = new Vector3d();
 // the Up or "new Y" vector
         Vector3d viewUp = new Vector3d();
 // the Right or "new X" vector
         Vector3d ViewRight = new Vector3d();
 
 // for normalizing the View vector
         double ViewMagnitude;
 // for normalizing the Up vector
         double UpMagnitude;
 // magnitude of projection of View Vector3d on World UP
         double UpProjection;
 
         position.set(pos);
 
         focalPoint.set(at);
 
         directionOfProjection.sub(position, focalPoint);
 
         ViewOut.sub(at, pos);
         // first, calculate and normalize the view vector
         ViewMagnitude = ViewOut.normalize();
         distance = ViewMagnitude;
 
 // invalid points (not far enough apart)
         if (ViewMagnitude < .000001)
             return -1;
 
 // normalize. This is the unit vector in the "new Z" direction
         ViewOut.div(ViewMagnitude);
 
 
 // Now the hard part: The ViewUp or "new Y" vector
 
 // dot product of ViewOut vector and World Up vector gives projection of
 // of ViewOut on WorldUp
         UpProjection = ViewOut.dotProduct(WorldUp);
 
 // first try at making a View Up vector: use World Up
 
         //Equivalent to: WorldUp - UpProjection*ViewOut
         viewUp.set(ViewOut);
         viewUp.minus();
         viewUp.mul(UpProjection);
         viewUp.add(WorldUp);
 
 // Check for validity:
         UpMagnitude = viewUp.normalizeSquare();
 
         if (UpMagnitude < .0000001) {
             //Second try at making a View Up vector: Use Y axis default  (0,1,0)
 
             viewUp.setX(-ViewOut.getY() * ViewOut.getX());
             viewUp.setY(1 - ViewOut.getY() * ViewOut.getY());
             viewUp.setZ(-ViewOut.getY() * ViewOut.getZ());
 
 // Check for validity:
             UpMagnitude = viewUp.normalizeSquare();
 
             if (UpMagnitude < .0000001) {
                 //Final try at making a View Up vector: Use Z axis default  (0,0,1)
                 viewUp.setX(-ViewOut.getZ() * ViewOut.getX());
                 viewUp.setY(-ViewOut.getZ() * ViewOut.getY());
                 viewUp.setZ(1 - ViewOut.getZ() * ViewOut.getZ());
 
 // Check for validity:
                 UpMagnitude = viewUp.normalizeSquare();
 
                 if (UpMagnitude < .0000001)
                     return (-1);
             }
         }
 
         // normalize the Up Vector3d
         UpMagnitude = Math.sqrt(UpMagnitude);
         viewUp.div(UpMagnitude);
 
         UpVector(viewUp);
 
 // Calculate the Right Vector3d. Use cross product of Out and Up.
         ViewRight.crossProduct(viewUp, ViewOut);
 
 // Plug values into rotation matrix R
         viewRotationMatrix.set(0, 0, ViewRight.getX());
         viewRotationMatrix.set(0, 1, ViewRight.getY());
         viewRotationMatrix.set(0, 2, ViewRight.getZ());
         viewRotationMatrix.set(0, 3, 0);
 
         viewRotationMatrix.set(1, 0, viewUp.getX());
         viewRotationMatrix.set(1, 1, viewUp.getY());
         viewRotationMatrix.set(1, 2, viewUp.getZ());
         viewRotationMatrix.set(1, 3, 0);
 
         viewRotationMatrix.set(2, 0, ViewOut.getX());
         viewRotationMatrix.set(2, 1, ViewOut.getY());
         viewRotationMatrix.set(2, 2, ViewOut.getZ());
         viewRotationMatrix.set(2, 3, 0);
 
 
 // Plug values into translation matrix T
         Matrix3d viewMoveMatrix = Matrix3d.MoveFill(Vector3d.minus(pos));
 
 // build the World Transform
         worldTransform = Matrix3d.multiply(viewRotationMatrix, viewMoveMatrix);
         //worldTransform = MatrixMultiply(viewRotationMatrix, ViewMoveMatrix);
 
         return 0;
     }
 
     /*
     *  Change the World Up vector (the default is (0,1,0))
     *
     */
     public void UpVector(Vector3d v) {
         WorldUp.set(v);
     }
 
 
     /*
     *
     */
     public void rotateUp(double theta) {
         // Axis to rotate around - cross camera up and dir
         Vector3d axis = new Vector3d();
 
 
 // Cross product
         axis.crossProduct(directionOfProjection, WorldUp);
 // Translate point relative to origin
         position.sub(focalPoint);
 
         Vector3d newPosition = Vector3d.rotate(position, axis, theta);
 
 // Translate point back to world space
         position.add(newPosition, focalPoint);
         lookAt(position, focalPoint);
     }
 
 
     /*
     *
     */
     public void rotateRight(double theta) {
 
 
 // Axis to rotate around - up vector
         position.sub(focalPoint);
 
         Vector3d newPosition = Vector3d.rotate(position, WorldUp, theta);
 
         //newPosition = RotatePoint(position, WorldUp, theta);
 
         position.add(newPosition, focalPoint);
         lookAt(position, focalPoint);
     }
 
 
     /*
     *
     */
     public void moveForward(double dist) {
 
         double diff = 1 - dist;
         position.set(directionOfProjection);
         position.mul(diff);
         position.add(focalPoint);
 
         lookAt(position, focalPoint);
     }
 
 
     /*
     *
     */
     public void moveRight(double dist) {
         // the Right or "new X" vector
         Vector3d ViewRight = new Vector3d();
 
 // Calculate the Right Vector3d. Use cross product of Out and Up.
         ViewRight.crossProduct(WorldUp, directionOfProjection);
 
         position.setX(position.getX() + ViewRight.getX() * dist);
         position.setY(position.getY() + ViewRight.getY() * dist);
         position.setZ(position.getZ() + ViewRight.getZ() * dist);
 
         focalPoint.setX(focalPoint.getX() + ViewRight.getX() * dist);
         focalPoint.setY(focalPoint.getY() + ViewRight.getY() * dist);
         focalPoint.setZ(focalPoint.getZ() + ViewRight.getZ() * dist);
 
         lookAt(position, focalPoint);
     }
 
     /*
     *
     */
     public void moveUp(double dist) {
         double value = dist * distance;
 
         position.setX(position.getX() + WorldUp.getX() * value);
         position.setY(position.getY() + WorldUp.getY() * value);
         position.setZ(position.getZ() + WorldUp.getZ() * value);
 
         focalPoint.setX(focalPoint.getX() + WorldUp.getX() * value);
         focalPoint.setY(focalPoint.getY() + WorldUp.getY() * value);
         focalPoint.setZ(focalPoint.getZ() + WorldUp.getZ() * value);
 
         lookAt(position, focalPoint);
     }
 
 }
