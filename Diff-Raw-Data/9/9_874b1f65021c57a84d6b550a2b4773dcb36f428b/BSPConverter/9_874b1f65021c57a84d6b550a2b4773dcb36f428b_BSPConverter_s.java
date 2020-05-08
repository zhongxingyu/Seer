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
 package org.jagatoo.loaders.models.bsp;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.BitSet;
 import java.util.HashMap;
 
 import org.jagatoo.datatypes.NamedObject;
 import org.jagatoo.loaders.models._util.AppearanceFactory;
 import org.jagatoo.loaders.models._util.GroupType;
 import org.jagatoo.loaders.models._util.NodeFactory;
 import org.jagatoo.loaders.models._util.SpecialItemsHandler;
 import org.jagatoo.loaders.models._util.SpecialItemsHandler.SpecialItemType;
 import org.jagatoo.loaders.models.bsp.BSPEntitiesParser.BSPEntity;
 import org.jagatoo.loaders.models.bsp.BSPEntitiesParser.BSPEntity_Location;
 import org.jagatoo.loaders.models.bsp.BSPEntitiesParser.BSPEntity_light;
 import org.jagatoo.loaders.models.bsp.BSPEntitiesParser.BSPEntity_misc_model;
 import org.jagatoo.loaders.models.bsp.lumps.BSPFace;
 import org.jagatoo.loaders.models.bsp.lumps.BSPModel;
 import org.jagatoo.loaders.textures.AbstractTexture;
 import org.jagatoo.opengl.enums.BlendFunction;
 import org.jagatoo.opengl.enums.BlendMode;
 import org.jagatoo.opengl.enums.DrawMode;
 import org.jagatoo.opengl.enums.FaceCullMode;
 import org.jagatoo.opengl.enums.ShadeModel;
 import org.jagatoo.opengl.enums.TextureCombineFunction;
 import org.jagatoo.opengl.enums.TextureCombineMode;
 import org.jagatoo.opengl.enums.TextureCombineSource;
 import org.jagatoo.opengl.enums.TextureMode;
 import org.openmali.FastMath;
 import org.openmali.spatial.PlaneIndicator;
 import org.openmali.spatial.bounds.BoundingBox;
 import org.openmali.spatial.bounds.BoundsType;
 import org.openmali.vecmath2.Matrix4f;
 import org.openmali.vecmath2.util.MatrixUtils;
 
 /**
  * Insert type comment here.
  * 
  * @author Marvin Froehlich (aka Qudus)
  */
 public class BSPConverter
 {
     private static NamedObject texAttrRepl = null;
     private static NamedObject texAttrModu = null;
     private static NamedObject texAttrComb = null;
     private static NamedObject transAttrSimple = null;
     private static NamedObject transAttrFlames = null;
     private static NamedObject mainMat = null;
     private static NamedObject mainPA = null;
     private static NamedObject mainCA = null;
     
     private static void initAppearanceComponents( AppearanceFactory appFactory )
     {
         if ( texAttrRepl != null )
         {
             return;
         }
         
         texAttrRepl = appFactory.createTextureAttributes( "" );
         appFactory.setTextureAttribsTextureMode( texAttrRepl, TextureMode.REPLACE );
         
         texAttrModu = appFactory.createTextureAttributes( "" );
         appFactory.setTextureAttribsTextureMode( texAttrModu, TextureMode.MODULATE );
         
         texAttrComb = appFactory.createTextureAttributes( "" );
         appFactory.setTextureAttribsTextureMode( texAttrComb, TextureMode.COMBINE );
         appFactory.setTextureAttribsCombineRGBMode( texAttrComb, TextureCombineMode.MODULATE );
         appFactory.setTextureAttribsCombineRGBSource( texAttrComb, 0, TextureCombineSource.PREVIOUS_TEXTURE_UNIT );
         appFactory.setTextureAttribsCombineRGBFunction( texAttrComb, 0, TextureCombineFunction.SRC_COLOR );
         appFactory.setTextureAttribsCombineRGBSource( texAttrComb, 1, TextureCombineSource.TEXTURE_COLOR );
         appFactory.setTextureAttribsCombineRGBFunction( texAttrComb, 1, TextureCombineFunction.SRC_COLOR );
         appFactory.setTextureAttribsCombineRGBScale( texAttrComb, 2 );
         
         transAttrSimple = appFactory.createTransparencyAttributes( "" );
         appFactory.setTransparencyAttribsBlendMode( transAttrSimple, BlendMode.BLENDED );
         appFactory.setTransparencyAttribsTransparency( transAttrSimple, 0f );
         
         transAttrFlames = appFactory.createTransparencyAttributes( "" );
         appFactory.setTransparencyAttribsBlendMode( transAttrFlames, BlendMode.BLENDED );
         appFactory.setTransparencyAttribsTransparency( transAttrFlames, 0f );
         //appFactory.setTransparencyAttribsSourceBlendFunc( transAttrFlames, BlendFunction.ONE );
         //appFactory.setTransparencyAttribsDestBlendFunc( transAttrFlames, BlendFunction.ONE );
         appFactory.setTransparencyAttribsSourceBlendFunc( transAttrFlames, BlendFunction.SRC_ALPHA );
         appFactory.setTransparencyAttribsDestBlendFunc( transAttrFlames, BlendFunction.ONE_MINUS_SRC_ALPHA );
         appFactory.setTransparencyAttribsSortingEnabled( transAttrFlames, true );
         
         mainMat = appFactory.createMaterial( "" );
         appFactory.setMaterialLightingEnabled( mainMat, false );
         
         mainPA = appFactory.createPolygonAttributes( "" );
         appFactory.setPolygonAttribsDrawMode( mainPA, DrawMode.FILL );
         appFactory.setPolygonAttribsFaceCullMode( mainPA, FaceCullMode.FRONT );
         
         mainCA = appFactory.createColoringAttributes( "" );
         appFactory.setColoringAttribsColor( mainCA, new float[] { 1f, 1f, 1f }, 0, 3 ); // WHITE
         appFactory.setColoringAttribsShadeModel( mainCA, ShadeModel.GOURAUD );
     }
     
     private static NamedObject convertFaceToShape( int sourceBSPVersion, int faceIndex, BSPFace face, NamedObject geometry, AbstractTexture[][] baseTextures, AbstractTexture[] lightMaps, NodeFactory nodeFactory, BoundsType boundsType, AppearanceFactory appFactory, HashMap<String, NamedObject> appCache )
     //private static NamedObject convertFaceToShape( BSPScenePrototype prototype, NodeFactory nodeFactory, AppearanceFactory appFactory, HashMap<String, Object> appCache )
     {
         if ( geometry == null )
         {
             return( null );
         }
         
         final String appKey;
         
         String baseTexName = baseTextures[ face.textureID ][0].getName();
         boolean isTranslucentTex = ( face.textureID >= 0 ) && ( baseTextures[ face.textureID ][0].getFormat().hasAlpha() || ( baseTexName.indexOf( "flame" ) >= 0 ) );
         
         if ( baseTexName.startsWith( "{" ) )
         {
             isTranslucentTex = true;
         }
         
         if ( face.lightmapID < 0 )
         {
             if ( isTranslucentTex )
             {
                 appKey = "appB-" + face.textureID;
             }
             else
             {
                 appKey = "appA-" + face.textureID;
             }
         }
         else
         {
             appKey = "appC-" + face.textureID + "-" + face.lightmapID;
         }
         
         NamedObject app = appCache.get( appKey );
         if ( app == null )
         {
             app = appFactory.createAppearance( appKey, AppearanceFactory.APP_FLAG_STATIC );
             
             if ( face.textureID >= 0 )
                 appFactory.applyTexture( baseTextures[ face.textureID ][0], 0, app );
             else
                 appFactory.applyTexture( appFactory.getFallbackTexture(), 0, app );
             
             if ( face.lightmapID < 0 )
             {
                 if ( isTranslucentTex )
                 {
                     appFactory.applyTextureAttributes( texAttrModu, 0, app );
                     if ( sourceBSPVersion == 30 )
                         appFactory.applyTransparancyAttributes( transAttrFlames, app );
                     else
                         appFactory.applyTransparancyAttributes( transAttrFlames, app );
                 }
                 else
                 {
                     appFactory.applyTextureAttributes( texAttrRepl, 0, app );
                 }
             }
             else
             {
                 appFactory.applyTexture( lightMaps[ face.lightmapID ], 1, app );
                 appFactory.applyTextureAttributes( texAttrComb, 1, app );
             }
             
             appFactory.applyPolygonAttributes( mainPA, app );
             appFactory.applyColoringAttributes( mainCA, app );
             //appFactory.applyMaterial( mainMat, app );
             
             appCache.put( appKey, app );
         }
         
         return( nodeFactory.createShape( "Shape" + faceIndex, geometry, app, boundsType ) );
     }
     
     private static Object[] convertFacesToShapes( int sourceBSPVersion, BSPModel[] models, BSPFace[] faces, NamedObject[][] geometries, AbstractTexture[][] baseTextures, AbstractTexture[] lightMaps, AppearanceFactory appFactory, NodeFactory nodeFactory, NamedObject sceneGroup, GroupType mainGroupType, float worldScale, SpecialItemsHandler siHandler )
     {
         initAppearanceComponents( appFactory );
         
         HashMap<String, NamedObject> appCache = new HashMap<String, NamedObject>();
         
         BitSet faceBitset = new BitSet( models[0].numOfFaces );
         faceBitset.set( 0, models[0].numOfFaces - 1 );
         
         BSPModel model0 = models[0];
         BoundingBox m0Bounds = new BoundingBox( model0.min[0] * worldScale, model0.min[2] * worldScale, model0.min[1] * worldScale,
                                                 model0.max[0] * worldScale, model0.max[2] * worldScale, model0.max[1] * worldScale
                                               );
         
         NamedObject bspTreeGroup;
         BoundsType nodeBoundsType;
         switch ( mainGroupType )
         {
             case SIMPLE:
                 bspTreeGroup = nodeFactory.createSimpleGroup( "BSPMainGroup", BoundsType.SPHERE );
                 nodeBoundsType = BoundsType.SPHERE;
                 break;
             case BSP_TREE:
                 bspTreeGroup = nodeFactory.createBSPTreeGroup( "BSPMainGroup", null, BoundsType.SPHERE );
                 nodeBoundsType = BoundsType.SPHERE;
                 break;
             case OC_TREE:
                 bspTreeGroup = nodeFactory.createOcTreeGroup( "BSPMainGroup", m0Bounds.getCenterX(), m0Bounds.getCenterY(), m0Bounds.getCenterZ(), m0Bounds.getSize().getX(), m0Bounds.getSize().getY(), m0Bounds.getSize().getZ() );
                 nodeBoundsType = BoundsType.AABB;
                 break;
             case QUAD_TREE:
                 bspTreeGroup = nodeFactory.createQuadTreeGroup( "BSPMainGroup", m0Bounds.getCenterX(), m0Bounds.getCenterY(), m0Bounds.getCenterZ(), PlaneIndicator.X_Z_PLANE, m0Bounds.getSize().getX(), m0Bounds.getSize().getZ(), m0Bounds.getSize().getY() );
                 nodeBoundsType = BoundsType.AABB;
                 break;
             default:
                 throw new Error( "Unsupported main GroupType " + mainGroupType );
         }
         
         siHandler.addSpecialItem( SpecialItemType.MAIN_GROUP, bspTreeGroup.getName(), bspTreeGroup );
         
         boolean skyboxPublished = false;
         
         // Convert faces into Shapes...
         for ( int m = 0; m < models.length; m++ )
         {
             BSPModel model = models[ m ];
             
             NamedObject modelGroup;
             if ( m == 0 )
                 modelGroup = bspTreeGroup;
             else
                 modelGroup = nodeFactory.createSimpleGroup( "BSPModel" + m, nodeBoundsType );
             
             if ( ( model == null ) || ( geometries.length <= m ) || ( geometries[ m ] == null ) )
             {
                 continue;
             }
             
             for ( int f = 0; f < model.numOfFaces; f++ )
             {
                 BSPFace face = faces[ model.faceIndex + f ];
                 
                 NamedObject shape;
                 
                 String baseTexName = baseTextures[ face.textureID ][0].getName();
                 if ( baseTexName.startsWith( "sky" ) )
                 {
                     shape = nodeFactory.createDummyNode();
                     
                     if ( !skyboxPublished )
                     {
                         AbstractTexture[] skyTextures = baseTextures[ face.textureID ];
                         Object skybox = nodeFactory.createSkyBox( skyTextures[1], skyTextures[2], skyTextures[3], skyTextures[4], skyTextures[5], skyTextures[6] );
                         siHandler.addSpecialItem( SpecialItemType.SKYBOX, null, skybox );
                         
                         skyboxPublished = true;
                     }
                 }
                 else if ( baseTexName.startsWith( "aaatrigger" ) )
                 {
                     shape = nodeFactory.createDummyNode();
                 }
                 else
                 {
                     shape = convertFaceToShape( sourceBSPVersion, f, face, geometries[ m ][ f ], baseTextures, lightMaps, nodeFactory, nodeBoundsType, appFactory, appCache );
                     
                     if ( shape == null )
                     {
                         shape = nodeFactory.createDummyNode();
                     }
                     else
                     {
                         siHandler.addSpecialItem( SpecialItemType.SHAPE, shape.getName(), shape );
                         
                         if ( baseTexName.startsWith( "+" ) )
                         {
                             NamedObject appearance = nodeFactory.getAppearanceFromShape( shape );
                             BSPTextureAnimator animator = new BSPTextureAnimator( baseTextures[ face.textureID ], appearance, 0, appFactory, 10f );
                             
                             shape = siHandler.createTextureAnimator( animator, shape );
                         }
                     }
                 }
                 
                 nodeFactory.addNodeToGroup( shape, modelGroup );
             }
             
             if ( ( m == 0 ) || ( model.numOfFaces > 0 ) )
             {
                 nodeFactory.addNodeToGroup( modelGroup, sceneGroup );
             }
         }
         
         return( new Object[] { bspTreeGroup, faceBitset } );
     }
     
     /*
     private void checkBrushes( BSPScenePrototype prototype, BSPScene scene )
     {
         Shape3D shape = scene.getShapeNodes().get( 0 );
         
         System.out.println( shape.getGeometry().getVertexCount() );
         
         BoundingBox bb = BoundingBox.newAABB( shape.getGeometry() );
         
         System.out.println( bb );
         
         //BSPBrush brush = prototype.brushes[0];
         
         //System.out.println( brush.brushSide + ", " + brush.numBrushes );
         
         BSP46Model model = (BSP46Model)prototype.models[0];
         //System.out.println( prototype.brushes.length + ", " + model.numOfFaces + ", " + model.numOfBrushes );
         
         for ( int i = 0; i < prototype.brushes.length; i++ )
         {
             BSPBrush brush = prototype.brushes[i];
             System.out.println( brush.numBrushSides );
         }
     }
     */
     
     private static final Matrix4f getTransformFromEntity( BSPEntity_Location entLoc, float worldScale )
     {
         float angleX = FastMath.toRad( entLoc.angles.getX() );
        float angleY = FastMath.toRad( entLoc.angles.getY() );
         float angleZ = FastMath.toRad( entLoc.angles.getZ() );
         
         Matrix4f trans = new Matrix4f();
         trans.setIdentity();
         MatrixUtils.eulerToMatrix4f( angleX, angleY, angleZ, trans );
         trans.m03( entLoc.origin.getX() * worldScale );
         trans.m13( entLoc.origin.getZ() * worldScale );
         trans.m23( -entLoc.origin.getY() * worldScale );
         
         return( trans );
     }
     
     private static void convertEntities( BSPEntity[] entities, float worldScale, URL baseURL, SpecialItemsHandler siHandler, NodeFactory nodeFactory, NamedObject sceneGroup )
     {
         if ( entities == null )
         {
             return;
         }
         
         for ( int i = 0; i < entities.length; i++ )
         {
             BSPEntity entity = entities[i];
             String className2 = entity.className2;
             
             if ( className2.startsWith( "info_player_start" ) || className2.startsWith( "info_player_deathmatch" ) )
             {
                 BSPEntity_Location entLoc = (BSPEntity_Location)entity;
                 
                 Matrix4f spawnTrans = getTransformFromEntity( entLoc, worldScale );
                 
                 siHandler.addSpecialItem( SpecialItemType.SPAWN_TRANSFORM, className2, spawnTrans );
             }
             else if ( className2.startsWith( "item_" ) || className2.startsWith( "weapon_" ) || className2.startsWith( "ammo_" ) )
             {
                 BSPEntity_Location entLoc = (BSPEntity_Location)entity;
                 
                 Matrix4f itemTrans = getTransformFromEntity( entLoc, worldScale );
                 
                 siHandler.addSpecialItem( SpecialItemType.ITEM, className2, itemTrans );
             }
             else if ( className2.startsWith( "misc_model" ) )
             {
                 BSPEntity_misc_model entLoc = (BSPEntity_misc_model)entity;
                 
                 try
                 {
                     URL modelURL = new URL( baseURL, entLoc.model );
                     Matrix4f modelTrans = getTransformFromEntity( entLoc, worldScale );
                     
                     siHandler.addSpecialItem( SpecialItemType.SUB_MODEL, modelURL.toExternalForm(), modelTrans );
                 }
                 catch ( MalformedURLException e )
                 {
                     e.printStackTrace();
                 }
             }
             else if ( className2.startsWith( "light" ) )
             {
                 BSPEntity_light entLight = (BSPEntity_light)entity;
                 
                 // TODO: Check, if the light-type is encoded in the "style" attribute!
                 NamedObject light = nodeFactory.createPointLightNode( "" );
                 
                 nodeFactory.setPointLightLocation( light, entLight.origin.getX(), entLight.origin.getY(), entLight.origin.getZ() );
                 nodeFactory.setPointLightColor( light, entLight.lightColor.getRed(), entLight.lightColor.getGreen(), entLight.lightColor.getBlue() );
                 nodeFactory.setLightRadius( light, entLight._light * worldScale );
                 
                 siHandler.addSpecialItem( SpecialItemType.LIGHT, entLight.className2, light );
                 
                 nodeFactory.addNodeToGroup( light, sceneGroup );
             }
         }
     }
     
     /**
      * Converts the information stored in the loader into a scenegraph objects.
      * 
      * @param prototype
      */
     public static void convert( BSPScenePrototype prototype, AppearanceFactory appFactory, NodeFactory nodeFactory, NamedObject sceneGroup, GroupType mainGroupType, float worldScale, URL baseURL, SpecialItemsHandler siHandler )
     {
         Object[] result = convertFacesToShapes( prototype.sourceBSPVersion, prototype.models, prototype.faces, prototype.geometries, prototype.baseTextures, prototype.lightMaps, appFactory, nodeFactory, sceneGroup, mainGroupType, worldScale, siHandler );
         
         NamedObject bspTreeGroup = (NamedObject)result[0];
         BitSet faceBitset = (BitSet)result[1];
         
         if ( mainGroupType == GroupType.BSP_TREE )
         {
             nodeFactory.setBSPGroupVisibilityUpdater( bspTreeGroup, BSPClusterManager.create( prototype, faceBitset ) );
         }
         
         //checkBrushes( prototype, scene );
         
         convertEntities( prototype.entities, worldScale, baseURL, siHandler, nodeFactory, sceneGroup );
     }
 }
