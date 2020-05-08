 /**
  * Copyright (c) 2003-2008, Xith3D Project Group all rights reserved.
  * 
  * Portions based on the Java3D interface, Copyright by Sun Microsystems.
  * Many thanks to the developers of Java3D and Sun Microsystems for their
  * innovation and design.
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
 package org.jagatoo.loaders.models.tds.internal;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.jagatoo.datatypes.NamedObject;
 import org.jagatoo.loaders.models._util.AppearanceFactory;
 import org.jagatoo.loaders.models._util.GeometryFactory;
 import org.jagatoo.loaders.models._util.NodeFactory;
 import org.jagatoo.loaders.models.tds.chunks.processors.FaceArrayProcessor;
 import org.jagatoo.opengl.enums.FaceCullMode;
 import org.openmali.vecmath2.Point3f;
 import org.openmali.vecmath2.TexCoord2f;
 import org.openmali.vecmath2.Vector3f;
 
 /**
  * A set of data pertaining to the model being built. This is all made public
  * for convienience. Since it represents no real world object and is literally
  * a data store this seems feasible. Not very convincing huh?
  * 
  * @author Kevin Glass
  */
 public class ModelContext
 {
     // file data
     public int totalVerts;
     public int totalFaces;
     public float transparency;
     
     public String objectName;
     public NamedObject shape;
     public Point3f[] vertexCoords;
     public ArrayList< Face >[] sharedFaces;
     public Surface[] surfaces;
     public TexCoord2f[] textureCoords;
     public int numberOfVerts;
     public HashMap< String, NamedObject > objectTable = new HashMap< String, NamedObject >();
     public HashMap< Integer, NamedObject > nodeIDMap = new HashMap< Integer, NamedObject >();
     public HashSet< NamedObject > unanimatedNodes = new HashSet< NamedObject >();
     public ArrayList< NamedObject > rootNodes = new ArrayList< NamedObject >();
     public ArrayList< NamedObject > nestedNodes = new ArrayList< NamedObject >();
     public ArrayList< Object > animControllers = new ArrayList< Object >();
     public HashMap< String, ModelContext > instanceTable = new HashMap< String, ModelContext >();
     public Face[] faces;
     public int numberOfFaces;
     public NamedObject geometry;
     
     public RotTransform orientation;
     public ScaleTransform scale;
     public PosTransform translation;
     
     public String instanceName;
     public String nodeName;
     public int nodeID;
     public int father;
     
     // material settings
     public HashMap< String, NamedObject > appearanceMap = new HashMap< String, NamedObject >();
     public HashMap< FaceCullMode, NamedObject > faceCullPolyAttribsCache = new HashMap< FaceCullMode, NamedObject >();
     
     public NamedObject textureAttributes = null;
     public NamedObject coloringAttributes = null;
     public FaceCullMode faceCullMode = null;
     public NamedObject material = null;
     public float shininess;
     public String appName = null;
     public NamedObject appearance;
     
     public boolean meshDataBegun = false;
     
     public boolean facesCreated = false;
     public boolean animationFound = false;
     public Point3f pivot;
     public int framesCount;
     
     public void composeAppearance( AppearanceFactory appFactory )
     {
         if ( ( appearance == null ) || meshDataBegun )
         {
             return;
         }
         
         appFactory.applyMaterial( material, appearance );
         if ( textureAttributes != null )
             appFactory.applyTextureAttributes( textureAttributes, 0, appearance );
         if ( coloringAttributes != null )
             appFactory.applyColoringAttributes( coloringAttributes, appearance );
         
         if ( faceCullMode != null )
         {
             NamedObject polyAttribs = faceCullPolyAttribsCache.get( faceCullMode );
             if ( polyAttribs == null )
             {
                 polyAttribs = appFactory.createPolygonAttributes( "" );
                 appFactory.setPolygonAttribsFaceCullMode( polyAttribs, faceCullMode );
                 faceCullPolyAttribsCache.put( faceCullMode, polyAttribs );
             }
             appFactory.applyPolygonAttributes( polyAttribs, appearance );
         }
         
         textureAttributes = null;
         coloringAttributes = null;
         faceCullMode = null;
         material = null;
         appName = null;
         appearance = null;
     }
     
     public void applyAppearanceAttributes( NodeFactory nodeFactory )
     {
         if ( ( appearance == null ) || ( shape == null ) )
         {
             return;
         }
         
         nodeFactory.applyAppearanceToShape( appearance, shape );
         appearance = null;
     }
     
     private void prepareForNewObject()
     {
         geometry = null;
         numberOfVerts = 0;
         vertexCoords = null;
        textureCoords = null;
         sharedFaces = null;
         numberOfFaces = 0;
         faces = null;
         nodeName = null;
         nodeID = -1;
         father = -1;
         instanceName = null;
         translation = null;
         orientation = null;
         scale = null;
         facesCreated = false;
         
         pivot = new Point3f( 0f, 0f, 0f );
         orientation = null;
         scale = null;
         translation = null;
         
         textureAttributes = null;
         coloringAttributes = null;
         material = null;
         faceCullMode = null;
         appearance = null;
     }
     
     public void createUnsmoothedFaces( GeometryFactory geomFactory, NodeFactory nodeFactory )
     {
         if ( !facesCreated )
         {
             if ( numberOfFaces > 0 )
             {
                 Point3f[] coords = new Point3f[ numberOfVerts ];
                 Vector3f[] normals = new Vector3f[ numberOfVerts ];
                 TexCoord2f[] texCoords = new TexCoord2f[ numberOfVerts ];
                 
                 for ( int i = 0; i < numberOfFaces; i++ )
                 {
                     Face f = faces[i];
                     
                     /*
                     coords[ ( i * 3 ) + 0 ] = f.getCoordA();
                     coords[ ( i * 3 ) + 1 ] = f.getCoordB();
                     coords[ ( i * 3 ) + 2 ] = f.getCoordC();
                     
                     normals[ ( i * 3 ) + 0 ] = f.getNormal();
                     normals[ ( i * 3 ) + 1 ] = f.getNormal();
                     normals[ ( i * 3 ) + 2 ] = f.getNormal();
                     */
                     
                     coords[ f.getCoordAIndex() ] = f.getCoordA();
                     coords[ f.getCoordBIndex() ] = f.getCoordB();
                     coords[ f.getCoordCIndex() ] = f.getCoordC();
                     
                     normals[ f.getCoordAIndex() ] = f.getNormal();
                     normals[ f.getCoordBIndex() ] = f.getNormal();
                     normals[ f.getCoordCIndex() ] = f.getNormal();
                     
                     if ( textureCoords != null )
                     {
                         /*
                         texCoords[ ( i * 3 ) + 0 ] = textureCoords[f.getCoordAIndex()];
                         texCoords[ ( i * 3 ) + 1 ] = textureCoords[f.getCoordBIndex()];
                         texCoords[ ( i * 3 ) + 2 ] = textureCoords[f.getCoordCIndex()];
                         */
                         
                         texCoords[ f.getCoordAIndex() ] = textureCoords[f.getCoordAIndex()];
                         texCoords[ f.getCoordBIndex() ] = textureCoords[f.getCoordBIndex()];
                         texCoords[ f.getCoordCIndex() ] = textureCoords[f.getCoordCIndex()];
                     }
                 }
                 
                 GeometryFactory.GeometryType geomType = FaceArrayProcessor.CREATE_INDEXED_GEOMETRY ? GeometryFactory.GeometryType.INDEXED_TRIANGLE_ARRAY : GeometryFactory.GeometryType.TRIANGLE_ARRAY;
                 for ( int i = 0; i < coords.length; i++ )
                 {
                     geomFactory.setCoordinate( geometry, geomType, i, coords[i].getX(), coords[i].getY(), coords[i].getZ() );
                     geomFactory.setNormal( geometry, geomType, i, normals[i].getX(), normals[i].getY(), normals[i].getZ() );
                     
                     if ( textureCoords != null )
                     {
                         geomFactory.setTexCoord( geometry, geomType, 0, i, texCoords[i].getS(), texCoords[i].getT() );
                     }
                 }
             }
         }
         
         if ( shape != null )
         {
             //shape.updateBounds( true );
             
             applyAppearanceAttributes( nodeFactory );
         }
         
         prepareForNewObject();
     }
     
     public ModelContext()
     {
     }
 }
