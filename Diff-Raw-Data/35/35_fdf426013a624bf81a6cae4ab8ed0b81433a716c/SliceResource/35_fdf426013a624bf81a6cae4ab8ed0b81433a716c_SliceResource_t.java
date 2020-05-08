 package de.zib.gndms.dspace.slice.service.globus.resource;
 
 import org.globus.wsrf.InvalidResourceKeyException;
 import org.globus.wsrf.NoSuchResourceException;
 import org.globus.wsrf.ResourceException;
 import org.globus.wsrf.ResourceKey;
 import org.jetbrains.annotations.NotNull;
 import org.apache.axis.types.URI;
 import org.apache.axis.types.PositiveInteger;
 import de.zib.gndms.model.dspace.Slice;
 import de.zib.gndms.model.dspace.Subspace;
 import de.zib.gndms.infra.wsrf.ReloadablePersistentResource;
 import de.zib.gndms.infra.model.GridEntityModelHandler;
 import de.zib.gndms.infra.model.GridResourceModelHandler;
 import de.zib.gndms.infra.system.GNDMSystem;
 import de.zib.gndms.dspace.common.DSpaceTools;
 import de.zib.gndms.dspace.slice.stubs.SliceResourceProperties;
 import de.zib.gndms.dspace.slice.common.SliceConstants;
 import de.zib.gndms.dspace.subspace.service.globus.resource.ExtSubspaceResourceHome;
 import de.zib.gndms.dspace.subspace.stubs.types.SubspaceReference;
 
 import java.rmi.RemoteException;
 
 
 /** 
  * The implementation of this SliceResource type.
  * 
  * @created by Introduce Toolkit version 1.2
  * 
  */
 public class SliceResource extends SliceResourceBase
         implements ReloadablePersistentResource<Slice, ExtSliceResourceHome> {
 
 
     private ExtSliceResourceHome resourceHome;
     private GridResourceModelHandler<Slice, ExtSliceResourceHome, SliceResource> mH;
 
     // override generated setter method to pass changes on the resource directly to the model
     public void setTotalStorageSize( PositiveInteger totalStorageSize ) throws ResourceException {
         Slice sl = loadModelById( getID( ) );
         sl.setTotalStorageSize( totalStorageSize.longValue( ) );
         mH.mergeModel( null, sl );
     }
 
     public void setSliceKind(javax.xml.namespace.QName sliceKind ) throws ResourceException {
         throw new UnsupportedOperationException( "Setting the sliceKind directly is not allowed (use SliceImpl.transformSliceTo instead)" );
     }
 
     public void setSliceLocation(org.apache.axis.types.URI sliceLocation ) throws ResourceException {
         throw new UnsupportedOperationException( "Changing the slice location is not supported" );
     }
 
     public void setSubspaceReference(de.zib.gndms.dspace.subspace.stubs.types.SubspaceReference subspaceReference ) throws ResourceException {
         throw new UnsupportedOperationException( "Changing the slices subspace is not supported (use SliceImpl.transformSliceTo instead)" );
     }
 
 
     @NotNull
     public Slice loadModelById( @NotNull String id ) throws ResourceException {
         return (Slice) mH.loadModelById( null, id );
     }
 
     public void loadViaModelId( @NotNull String id ) throws ResourceException {
         loadFromModel(loadModelById( id ) );
     }
 
     public void loadFromModel( @NotNull Slice model ) throws ResourceException {
 
         // todo uncomment after xsd change of slice kind uri
    //     super.setSliceKind( model.getKind( ).getURI( ) );
 
         try {
             super.setSliceLocation( new URI( model.getOwner().getMetaSubspace().getScopedName().getNameScope() ) );
         } catch ( URI.MalformedURIException e ) {
             throw new ResourceException( "Following exception occured: " + e.getMessage( ) );
         }
 
         super.setTerminationTime( model.getTerminationTime() );
        super.setTotalStorageSize( DSpaceTools.positiveIntegerFromLong( model.getTotalStorageSize( ) ) );
 
         GNDMSystem sys = resourceHome.getSystem( );
         ExtSubspaceResourceHome srh = (ExtSubspaceResourceHome) sys.getHome( Subspace.class );
         SubspaceReference subref;
         try {
             subref = srh.getReferenceForSubspace( model.getOwner( ) );
             super.setSubspaceReference( subref );
         } catch ( Exception e ) {
             throw new ResourceException( "Can't obtain reference for subspace: " + e.getMessage( ) );
         }
     }
 
     @NotNull
     public ExtSliceResourceHome getResourceHome() {
         if( resourceHome == null )
             throw new IllegalStateException( "now slice resource home set" );
         return resourceHome;
     }
 
     public void setResourceHome( @NotNull ExtSliceResourceHome resourceHomeParam ) {
         if (resourceHome == null) {
 			mH = new GridResourceModelHandler<Slice, ExtSliceResourceHome, SliceResource>
 				  (Slice.class, resourceHomeParam);
 			resourceHome = resourceHomeParam;
 		}
 		else
 			throw new IllegalStateException("resourceHome already set");
     }
 
     public void load( ResourceKey resourceKey ) throws ResourceException, NoSuchResourceException, InvalidResourceKeyException {
         
         if ( getResourceHome().getKeyTypeName().equals( resourceKey.getName() ) ) {
             String id = ( String ) resourceKey.getValue();
             Slice sl = loadModelById(id);
             setResourceKey( resourceKey );
             loadFromModel(sl);
             initialize(new SliceResourceProperties(),
                     SliceConstants.RESOURCE_PROPERTY_SET, id);
         }
         else
             throw new InvalidResourceKeyException("Invalid resourceKey name");
     }
 
     public void store() throws ResourceException {
         // done one model layer
     }
 
     public String getID( ) {
         return (String) getID( );
     }
 
 }
