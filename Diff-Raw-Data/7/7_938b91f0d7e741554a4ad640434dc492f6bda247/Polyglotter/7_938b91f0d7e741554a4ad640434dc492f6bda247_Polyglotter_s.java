 /*
  * Polyglotter (http://polyglotter.org)
  * See the COPYRIGHT.txt file distributed with this work for information
  * regarding copyright ownership.  Some portions may be licensed
  * to Red Hat, Inc. under one or more contributor license agreements.
  * See the AUTHORS.txt file in the distribution for a full listing of 
  * individual contributors.
  *
  * Polyglotter is free software. Unless otherwise indicated, all code in Polyglotter
  * is licensed to you under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * Polyglotter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.polyglotter;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 
 import org.modeshape.modeler.ModeShapeModeler;
 import org.modeshape.modeler.Model;
 import org.modeshape.modeler.ModelObject;
 import org.modeshape.modeler.ModelType;
 import org.modeshape.modeler.ModelTypeManager;
 import org.modeshape.modeler.Modeler;
 import org.modeshape.modeler.ModelerException;
 import org.polyglotter.common.CheckArg;
 import org.polyglotter.common.PolyglotterException;
 import org.polyglotter.internal.CloneOperation;
 import org.polyglotter.internal.ModelTransformImpl;
 import org.polyglotter.internal.TransformImpl;
 
 /**
  * 
  */
 public final class Polyglotter implements Modeler {
 
     private final Modeler modeler;
 
     /**
      * Uses a default ModeShape configuration.
      * 
      * @param repositoryStoreParentPath
      *        the file path to the folder that should contain the ModeShape repository store
      */
     public Polyglotter( final String repositoryStoreParentPath ) {
         modeler = new ModeShapeModeler( repositoryStoreParentPath );
     }
 
     /**
      * @param repositoryStoreParentPath
      *        the file path to the folder that should contain the ModeShape repository store
      * @param modeShapeConfigurationPath
      *        the file path to a ModeShape configuration file
      */
     public Polyglotter( final String repositoryStoreParentPath,
                         final String modeShapeConfigurationPath ) {
         modeler = new ModeShapeModeler( repositoryStoreParentPath, modeShapeConfigurationPath );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see AutoCloseable#close()
      */
     @Override
     public void close() throws Exception {
         modeler.close();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.modeshape.modeler.Modeler#export(org.modeshape.modeler.Model, java.io.File)
      */
     @Override
     public void export( final Model model,
                         final File file ) throws ModelerException {
         modeler.export( model, file );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.modeshape.modeler.Modeler#export(org.modeshape.modeler.Model, java.io.OutputStream)
      */
     @Override
     public void export( final Model model,
                         final OutputStream stream ) throws ModelerException {
         modeler.export( model, stream );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.modeshape.modeler.Modeler#export(org.modeshape.modeler.Model, java.net.URL)
      */
     @Override
     public void export( final Model model,
                         final URL url ) throws ModelerException {
         modeler.export( model, url );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see Modeler#generateDefaultModel(String, String)
      */
     @Override
     public Model generateDefaultModel( final String artifactPath,
                                        final String modelPath ) throws ModelerException {
         return modeler.generateDefaultModel( artifactPath, modelPath );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see Modeler#generateModel(File, String, ModelType)
      */
     @Override
     public Model generateModel( final File file,
                                 final String modelFolder,
                                 final ModelType modelType ) throws ModelerException {
         return modeler.generateModel( file, modelFolder, modelType );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see Modeler#generateModel(File, String, String, ModelType)
      */
     @Override
     public Model generateModel( final File file,
                                 final String modelFolder,
                                 final String modelName,
                                 final ModelType modelType ) throws ModelerException {
         return modeler.generateModel( file, modelFolder, modelName, modelType );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see Modeler#generateModel(InputStream, String, ModelType)
      */
     @Override
     public Model generateModel( final InputStream stream,
                                 final String modelPath,
                                 final ModelType modelType ) throws ModelerException {
         return modeler.generateModel( stream, modelPath, modelType );
     }
 
     /**
      * {@inheritDoc}
      * 
     * @see Modeler#generateModel(java.lang.String, java.lang.String, org.modeshape.modeler.ModelType, boolean)
      */
     @Override
     public Model generateModel( final String artifactPath,
                                 final String modelPath,
                                 final ModelType modelType,
                                final boolean persistArtifacts ) throws ModelerException {
        return modeler.generateModel( artifactPath, modelPath, modelType, persistArtifacts );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see Modeler#generateModel(URL, String, ModelType)
      */
     @Override
     public Model generateModel( final URL artifactUrl,
                                 final String modelFolder,
                                 final ModelType modelType ) throws ModelerException {
         return modeler.generateModel( artifactUrl, modelFolder, modelType );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see Modeler#generateModel(URL, String, String, ModelType)
      */
     @Override
     public Model generateModel( final URL artifactUrl,
                                 final String modelFolder,
                                 final String modelName,
                                 final ModelType modelType ) throws ModelerException {
         return modeler.generateModel( artifactUrl, modelFolder, modelName, modelType );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see Modeler#importArtifact(InputStream, String)
      */
     @Override
     public String importArtifact( final InputStream stream,
                                   final String workspacePath ) throws ModelerException {
         return modeler.importArtifact( stream, workspacePath );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see Modeler#importArtifact(URL, String)
      */
     @Override
     public String importArtifact( final URL url,
                                   final String workspaceFolder ) throws ModelerException {
         return modeler.importArtifact( url, workspaceFolder );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see Modeler#importArtifact(URL, String, String)
      */
     @Override
     public String importArtifact( final URL url,
                                   final String workspaceFolder,
                                   final String workspaceName ) throws ModelerException {
         return modeler.importArtifact( url, workspaceFolder, workspaceName );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see Modeler#importFile(File, String)
      */
     @Override
     public String importFile( final File file,
                               final String workspaceFolder ) throws ModelerException {
         return modeler.importFile( file, workspaceFolder );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see Modeler#importFile(File, String, String)
      */
     @Override
     public String importFile( final File file,
                               final String workspaceFolder,
                               final String workspaceName ) throws ModelerException {
         return modeler.importFile( file, workspaceFolder, workspaceName );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see Modeler#model(String)
      */
     @Override
     public Model model( final String path ) throws ModelerException {
         return modeler.model( path );
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see Modeler#modelTypeManager()
      */
     @Override
     public ModelTypeManager modelTypeManager() throws ModelerException {
         return modeler.modelTypeManager();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see Modeler#modeShapeConfigurationPath()
      */
     @Override
     public String modeShapeConfigurationPath() {
         return modeler.modeShapeConfigurationPath();
     }
 
     /**
      * @param sourceModel
      *        the model that is the source of the transforms
      * @param targetModelPath
      *        the workspace path to a new model that will be the target of the transforms
      * @return a new model transform with default one-to-one transforms between the supplied models
      * @throws PolyglotterException
      *         if any error occurs
      */
     public ModelTransform newCloneModelTransform( final Model sourceModel,
                                                   final String targetModelPath ) throws PolyglotterException {
         CheckArg.notNull( sourceModel, "sourceModel" );
         CheckArg.notEmpty( targetModelPath, "targetModelPath" );
         final ModelTransformImpl modelXform = new ModelTransformImpl();
         newCopyModelTransform( modelXform, sourceModel, sourceModel, targetModelPath );
         return modelXform;
     }
 
     private void newCopyModelTransform( final ModelTransformImpl modelXform,
                                         final Model sourceModel,
                                         final ModelObject sourceObject,
                                         final String targetModelPath ) throws PolyglotterException {
         try {
             TransformImpl xform = new TransformImpl();
             xform.add( new CloneOperation( sourceModel, sourceObject.modelRelativePath(), targetModelPath ) );
             modelXform.add( xform );
             for ( final String name : sourceObject.propertyNames() ) {
                 xform = new TransformImpl();
                 xform.add( new CloneOperation( sourceModel, sourceObject.modelRelativePath() + "/@" + name, targetModelPath ) );
                 modelXform.add( xform );
             }
             for ( final ModelObject sourceChild : sourceObject.children() )
                 newCopyModelTransform( modelXform, sourceModel, sourceChild, targetModelPath );
         } catch ( final ModelerException e ) {
             throw new PolyglotterException( e );
         }
     }
 
     /**
      * @return a new model transform
      */
     public ModelTransform newModelTransform() {
         return new ModelTransformImpl();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see Modeler#repositoryStoreParentPath()
      */
     @Override
     public String repositoryStoreParentPath() {
         return modeler.repositoryStoreParentPath();
     }
 }
