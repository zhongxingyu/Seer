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
 package org.jagatoo.loaders.models.tds.chunks.processors;
 
 import java.io.IOException;
 
 import org.openmali.vecmath2.Point3f;
 import org.openmali.vecmath2.TexCoord2f;
 import org.openmali.vecmath2.Vector3f;
 import org.jagatoo.loaders.models._util.AnimationFactory;
 import org.jagatoo.loaders.models._util.AppearanceFactory;
 import org.jagatoo.loaders.models._util.GeometryFactory;
 import org.jagatoo.loaders.models._util.NodeFactory;
 import org.jagatoo.loaders.models._util.SpecialItemsHandler;
 import org.jagatoo.loaders.models.tds.TDSFile;
 import org.jagatoo.loaders.models.tds.internal.Face;
 import org.jagatoo.loaders.models.tds.internal.ModelContext;
 import org.jagatoo.loaders.models.tds.internal.Surface;
 
 /**
  * A processor to handle smoothing groups
  * 
  * @author Kevin Glass
  * @author Marvin Froehlich (aka Qudus)
  */
 public class SmoothGroupProcessor extends ChunkProcessor
 {
     public static final int MAX_SURFACES = 33;
     private Vector3f[] normalCache;
     
     private void calculateSurfaces( ModelContext context )
     {
         for ( int surface = 0; surface < MAX_SURFACES; surface++ )
         {
             calculateSurfaceNormals( context.surfaces[ surface ] );
         }
         
         for ( int i = 0; i < normalCache.length; i++ )
         {
             normalCache[ i ].normalize();
         }
     }
     
     private void addNormal( int index, Vector3f normal )
     {
         if ( normalCache[ index ] == null )
         {
             normalCache[ index ] = new Vector3f();
         }
         
         normalCache[ index ].add( normal );
     }
     
     private void calculateSurfaceNormals( Surface surface )
     {
         for ( Face face: surface.getFaces() )
         {
             addNormal( face.getCoordAIndex(), face.getNormal() );
             addNormal( face.getCoordBIndex(), face.getNormal() );
             addNormal( face.getCoordCIndex(), face.getNormal() );
         }
     }
     
     private boolean sharesVertex( Face face, int vertex )
     {
         return face.getCoordAIndex() == vertex || face.getCoordBIndex() == vertex || face.getCoordCIndex() == vertex;
     }
     
     private Vector3f calculateVertexNormal( int vertex, Face thisFace, Surface surface )
     {
         if ( normalCache[ vertex ] != null )
         {
             return normalCache[ vertex ];
         }
         
         Vector3f normal = new Vector3f( thisFace.getNormal() );
         
         for ( Face otherFace: surface.getFaces() )
         {
             if ( otherFace != thisFace )
             {
                 if ( sharesVertex( otherFace, vertex ) )
                 {
                     normal.add( otherFace.getNormal() );
                 }
             }
         }
         
         normal.normalize();
         
         normalCache[ vertex ] = normal;
         return normal;
     }
     
     @Override
     public void process( TDSFile file, AppearanceFactory appFactory, GeometryFactory geomFactory, NodeFactory nodeFactory, AnimationFactory animFactory, SpecialItemsHandler siHandler, ModelContext context, int length ) throws IOException
     {
         int i;
         
         normalCache = new Vector3f[ context.vertexCoords.length ];
         
         context.surfaces = new Surface[ MAX_SURFACES ];
         
         for ( i = 0; i < MAX_SURFACES; i++ )
         {
             context.surfaces[ i ] = new Surface();
         }
         
         for ( i = 0; i < context.numberOfFaces; i++ )
         {
             // int     group   = file.readUnsignedShort();
             int group = file.readInt();
             long b = 0x1;
             int index;
             
             for ( index = 0; index < 32; index++ )
             {
                 if ( ( group & b ) > 0 )
                 {
                     break;
                 }
             }
             
             context.faces[i].group = group;
             context.surfaces[index].addFace( context.faces[i] );
         }
         
         int surface;
         
         i = 0;
         
         calculateSurfaces( context );
         
         if ( FaceArrayProcessor.CREATE_INDEXED_GEOMETRY )
         {
             Vector3f normals[] = new Vector3f[ context.vertexCoords.length ];
             int indices[] = new int[ context.numberOfFaces * 3 ];
             
             for ( surface = 0; surface < MAX_SURFACES; surface++ )
             {
                 for ( Face f: context.surfaces[ surface ].getFaces() )
                 {
                     normals[ f.getCoordAIndex() ] = calculateVertexNormal( f.getCoordAIndex(), f, context.surfaces[ surface ] );
                     normals[ f.getCoordBIndex() ] = calculateVertexNormal( f.getCoordBIndex(), f, context.surfaces[ surface ] );
                     normals[ f.getCoordCIndex() ] = calculateVertexNormal( f.getCoordCIndex(), f, context.surfaces[ surface ] );
                     
                     indices[ i * 3 ] = f.getCoordAIndex();
                     indices[ ( i * 3 ) + 1 ] = f.getCoordBIndex();
                     indices[ ( i * 3 ) + 2 ] = f.getCoordCIndex();
                     
                     i++;
                 }
                 
             }
             
             GeometryFactory.GeometryType geomType = GeometryFactory.GeometryType.INDEXED_TRIANGLE_ARRAY;
             geomFactory.setCoordinates( context.geometry, geomType, 0, context.vertexCoords, 0, context.vertexCoords.length );
             geomFactory.setNormals( context.geometry, geomType, 0, normals, 0, normals.length );
             geomFactory.setIndex( context.geometry, geomType, 0, indices, 0, indices.length );
             
             if ( context.textureCoords != null )
             {
                System.out.println( context.vertexCoords.length + ", " + normals.length + ", " + context.textureCoords.length );
                 geomFactory.setTexCoords( context.geometry, geomType, 0, 2, 0, context.textureCoords, 0, context.textureCoords.length );
             }
             //context.indexedGeometry.calculateFaceNormals();
             
             geomFactory.finalizeGeometry( context.geometry, geomType, 0, context.vertexCoords.length, 0, indices.length );
         }
         else
         {
             Point3f[] coords = new Point3f[ context.numberOfFaces * 3 ];
             Vector3f[] normals = new Vector3f[ context.numberOfFaces * 3 ];
             TexCoord2f[] texCoords = new TexCoord2f[ context.numberOfFaces * 3 ];
             
             for ( surface = 0; surface < MAX_SURFACES; surface++ )
             {
                 for ( Face f: context.surfaces[ surface ].getFaces() )
                 {
                     Vector3f normalA = calculateVertexNormal( f.getCoordAIndex(), f, context.surfaces[ surface ] );
                     Vector3f normalB = calculateVertexNormal( f.getCoordBIndex(), f, context.surfaces[ surface ] );
                     Vector3f normalC = calculateVertexNormal( f.getCoordCIndex(), f, context.surfaces[ surface ] );
                     
                     coords[ i * 3 ] = f.getCoordA();
                     coords[ ( i * 3 ) + 1 ] = f.getCoordB();
                     coords[ ( i * 3 ) + 2 ] = f.getCoordC();
                     
                     //context.geometry.setCoordinate( i * 3, f.getPointA() );
                     //context.geometry.setCoordinate( (i * 3) + 1, f.getPointB() );
                     //context.geometry.setCoordinate( (i * 3) + 2, f.getPointC() );
                     
                     normals[ i * 3 ] = normalA;
                     normals[ ( i * 3 ) + 1 ] = normalB;
                     normals[ ( i * 3 ) + 2 ] = normalC;
                     
                     //context.geometry.setNormal( i * 3, normalA );
                     //context.geometry.setNormal( (i * 3) + 1, normalB );
                     //context.geometry.setNormal( (i * 3) + 2, normalC );
                     
                     if ( context.textureCoords != null )
                     {
                         texCoords[ i * 3 ] = context.textureCoords[ f.getCoordAIndex() ];
                         texCoords[ ( i * 3 ) + 1 ] = context.textureCoords[ f.getCoordBIndex() ];
                         texCoords[ ( i * 3 ) + 2 ] = context.textureCoords[ f.getCoordCIndex() ];
                         
                         //context.geometry.setTextureCoordinate( i * 3,0, context.textureCoords[f.getPointAIndex()] );
                         //context.geometry.setTextureCoordinate( (i * 3) + 1,0, context.textureCoords[f.getPointBIndex()] );
                         //context.geometry.setTextureCoordinate( (i * 3) + 2,0, context.textureCoords[f.getPointCIndex()] );
                     }
                     
                     i++;
                 }
             }
             
             GeometryFactory.GeometryType geomType = GeometryFactory.GeometryType.TRIANGLE_ARRAY;
             geomFactory.setCoordinates( context.geometry, geomType, 0, coords, 0, coords.length );
             geomFactory.setNormals( context.geometry, geomType, 0, normals, 0, normals.length );
             
             if ( context.textureCoords != null )
             {
                 geomFactory.setTexCoords( context.geometry, geomType, 0, 2, 0, texCoords, 0, texCoords.length );
             }
             
             geomFactory.finalizeGeometry( context.geometry, geomType, 0, context.vertexCoords.length, 0, 0 );
         }
         
         //context.shape.updateBounds( false );
         context.facesCreated = true;
     }
     
     public SmoothGroupProcessor()
     {
     }
 }
