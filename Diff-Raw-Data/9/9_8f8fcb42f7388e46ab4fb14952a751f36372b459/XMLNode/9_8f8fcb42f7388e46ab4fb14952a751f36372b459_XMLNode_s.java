 /**
  * Copyright (c) 2007-2008, JAGaToo Project Group all rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * 
  * Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * 
  * Neither the name of the 'Xith3D Project Group' nor the names of its
  * contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) A
  * RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE
  */
 package org.jagatoo.loaders.models.collada.jibx;
 
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import org.openmali.vecmath2.Matrix4f;
 import org.openmali.vecmath2.Point3f;
 import org.openmali.vecmath2.util.MatrixUtils;
 
 /**
  * A Node can have a Transform and can instance
  * geometries/controllers (for skeletal animation).
  * Child of VisualScene or Node.
  * 
  * @author Amos Wenger (aka BlueSky)
  */
 public class XMLNode {
     
     public ArrayList<String> layers = null;
     public String sid = null;
     public static enum Type {
         NODE,
         JOINT
     }
     public Type type = null;
     public String id = null;
     public String name = null;
     
     public XMLMatrix4x4 matrix = new XMLMatrix4x4();
     public ArrayList<XMLInstanceGeometry> instanceGeometries = null;
     public ArrayList<XMLInstanceController> instanceControllers = null;
     
     public ArrayList<XMLNode> childrenList = null;
     
     //private String translateString;
     //private String rotateString;
     //private String scaleString;
     
     public static ArrayList<String> parseLayerList(String str) {
         ArrayList<String> layers = new ArrayList<String>();
         StringTokenizer tknz = new StringTokenizer(str);
         while(tknz.hasMoreTokens()) {
             layers.add(tknz.nextToken());
         }
         return layers;
     }
     
     public void applyTranslate(String str) {
         StringTokenizer tknz = new StringTokenizer(str);
         Point3f translate = new Point3f(
                 Float.parseFloat(tknz.nextToken()),
                 Float.parseFloat(tknz.nextToken()),
                 Float.parseFloat(tknz.nextToken())
         );
         Matrix4f mat = new Matrix4f();
         mat.setIdentity();
         mat.setTranslation(translate);
         // System.out.println("Mat before translate : \n"+matrix.matrix4f);
         matrix.matrix4f.mul(mat);
         // System.out.println("Mat after translate of "+translate+" : \n"+matrix.matrix4f);
     }
     
     public void applyRotate(String str) {
         StringTokenizer tknz = new StringTokenizer(str);
        Point3f rotate = new Point3f(
                 Float.parseFloat(tknz.nextToken()),
                 Float.parseFloat(tknz.nextToken()),
                 Float.parseFloat(tknz.nextToken())
         );
        float angle = Float.parseFloat(tknz.nextToken());
         rotate.mul( angle );
         Matrix4f mat = MatrixUtils.eulerToMatrix4f(rotate);
         // System.out.println("Mat before rotate : \n"+matrix.matrix4f);
         matrix.matrix4f.mul(mat);
         // System.out.println("Mat after rotate of "+rotate+" : \n"+matrix.matrix4f);
     }
     
     public void applyScale(String str) {
         StringTokenizer tknz = new StringTokenizer(str);
         
         Point3f scale = new Point3f(
                 Float.parseFloat(tknz.nextToken()),
                 Float.parseFloat(tknz.nextToken()),
                 Float.parseFloat(tknz.nextToken())
         );
         
         // System.out.println("Mat before scale : \n"+matrix.matrix4f);
         
         matrix.matrix4f.mul( 0, 0, scale.getX() );
         matrix.matrix4f.mul( 0, 1, scale.getX() );
         matrix.matrix4f.mul( 0, 2, scale.getX() );
         matrix.matrix4f.mul( 0, 3, scale.getX() );
         
         matrix.matrix4f.mul( 1, 0, scale.getY() );
         matrix.matrix4f.mul( 1, 1, scale.getY() );
         matrix.matrix4f.mul( 1, 2, scale.getY() );
         matrix.matrix4f.mul( 1, 3, scale.getY() );
         
         matrix.matrix4f.mul( 2, 0, scale.getZ() );
         matrix.matrix4f.mul( 2, 1, scale.getZ() );
         matrix.matrix4f.mul( 2, 2, scale.getZ() );
         matrix.matrix4f.mul( 2, 3, scale.getZ() );
         
         // System.out.println("Mat after scale of "+scale+" : \n"+matrix.matrix4f);
     }
     
 }
