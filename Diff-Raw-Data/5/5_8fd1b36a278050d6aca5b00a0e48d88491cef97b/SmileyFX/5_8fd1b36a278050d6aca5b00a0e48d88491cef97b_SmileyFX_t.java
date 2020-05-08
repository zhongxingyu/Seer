 /*
  * Copyright 2012 Nagai Masato
  *
  * Licensed under the Apache License, Version 2.0 (the "License")
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package smileyfx;
 
 import javafx.scene.Group;
 import javafx.scene.paint.Color;
 import javafx.scene.shape.ArcBuilder;
 import javafx.scene.shape.Ellipse;
 import javafx.scene.shape.EllipseBuilder;
import javafx.scene.shape.StrokeType;
 
 public class SmileyFX {
 
     private static double STROKE_WIDTH = 2;
 
     /**
      * Creates a Smiley node.
      */
     public static javafx.scene.Node smiley(double size) {
         javafx.geometry.Point2D faceCp = javafx.geometry.Point2DBuilder
                 .create()
                 .x(size / 2)
                 .y(size / 2)
                 .build();
         return new Group(
                 outline(faceCp),
                 eye(faceCp, true),
                 eye(faceCp, false),
                 mouth(faceCp));
     }
 
     private static javafx.scene.Node outline(javafx.geometry.Point2D faceCp) {
         return EllipseBuilder
                 .create()
                 .stroke(Color.BLACK)
                 .strokeWidth(STROKE_WIDTH)
                .strokeType(StrokeType.INSIDE)
                 .fill(Color.GOLD)
                 .radiusX(faceCp.getX())
                 .radiusY(faceCp.getY())
                 .centerX(faceCp.getX())
                 .centerY(faceCp.getY())
                 .build();
     }
 
     private static javafx.scene.Node eye(javafx.geometry.Point2D faceCp,
             boolean left) {
         Ellipse eye = EllipseBuilder
                 .create()
                 .fill(Color.BLACK)
                 .radiusX(faceCp.getX() / 8)
                 .radiusY(faceCp.getY() / 6)
                 .build();
         double distanceFromMidline = (faceCp.getX() / 4 / 2 + eye.getRadiusX())
                 * (left ? -1 : 1);
         eye.setCenterX(faceCp.getX() + distanceFromMidline);
         eye.setCenterY(faceCp.getY() - (faceCp.getY() - eye.getRadiusY()) / 2);
         return eye;
     }
 
     private static javafx.scene.Node mouth(javafx.geometry.Point2D faceCp) {
         return ArcBuilder
                 .create()
                 .stroke(Color.BLACK)
                 .strokeWidth(STROKE_WIDTH)
                .strokeType(StrokeType.INSIDE)
                 .fill(Color.TRANSPARENT)
                 .centerX(faceCp.getX())
                 .centerY(faceCp.getY())
                 .radiusX(faceCp.getX() / 4 * 3)
                 .radiusY(faceCp.getY() / 4 * 3)
                 .startAngle(180)
                 .length(180)
                 .build();
     }
 
     private SmileyFX() {
     }
 
 }
