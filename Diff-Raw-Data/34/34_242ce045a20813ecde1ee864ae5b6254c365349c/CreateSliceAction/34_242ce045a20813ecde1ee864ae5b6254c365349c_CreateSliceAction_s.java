 package de.zib.gndms.logic.model.dspace;
 
import de.zib.gndms.logic.model.CreateTimedGridResourceAction;
 import de.zib.gndms.kit.util.DirectoryAux;
import de.zib.gndms.kit.util.LinuxDirectoryAux;
 import de.zib.gndms.model.common.ModelUUIDGen;
 import de.zib.gndms.model.dspace.Slice;
 import de.zib.gndms.model.dspace.SliceKind;
 import de.zib.gndms.model.dspace.Subspace;
import de.zib.gndms.model.dspace.types.SliceKindMode;
 import org.jetbrains.annotations.NotNull;
 
 import javax.persistence.EntityManager;
 import java.io.File;
 import java.util.Calendar;
 
 /**
  * An action which creates a new slice in a given workspace
  *
  * @author: Maik Jorra <jorra@zib.de>
  * @version: $Id$
  *
  * User: mjorra, Date: 13.08.2008, Time: 10:21:36
  */
 public class CreateSliceAction extends CreateTimedGridResourceAction<Subspace, Slice> {
 
     private SliceKind sliceKind;     // kind of the slice to create
     private long storageSize;
     private DirectoryAux directoryAux;
 
     
     public CreateSliceAction( ) {
          directoryAux = DirectoryAux.getDirectoryAux();
     }
 
 
     public CreateSliceAction( String uuid, Calendar ttm, ModelUUIDGen gen, SliceKind kind, long ssize ) {
 
         super( uuid, ttm );
         setUUIDGen(gen);
         this.sliceKind = kind;
         this.storageSize = ssize;
         directoryAux = DirectoryAux.getDirectoryAux();
 
     }
 
 
     public CreateSliceAction( String uuid, Calendar ttm, ModelUUIDGen gen, SliceKind kind, long ssize, DirectoryAux da ) {
         
         super( uuid, ttm );
         setUUIDGen(gen);
         this.sliceKind = kind;
         this.storageSize = ssize;
         this.directoryAux = da;
     }
 
 
     @Override
     public void initialize() {
 
         if( sliceKind == null )
             throw new IllegalThreadStateException( "No slice kind provided" );
 
         super.initialize();
     }
 
 
     @Override
     public Slice execute( @NotNull EntityManager em ) {
 
         Subspace sp = getModel( );
 
         if( ! sp.getMetaSubspace( ).getCreatableSliceKinds( ).contains( sliceKind ) )
             throw new IllegalStateException("SliceKind not assigned to Subspace");
 
         String lp = sp.getPath( ) +  File.separator + sliceKind.getSliceDirectory() + File.separator;
         File f = null;
         String did = new String( );
 
         while ( !( f != null && !f.exists() ) ) {
             did = nextUUID();
             f = new File( lp + did );
         }
 
         try {
             // this also creats the dir for the namespace if it
             // doesn't exist yet.
             f.mkdirs( );
         } catch ( SecurityException e ) {
             throw new RuntimeException(e);
         }
 
         // fix permissions
         directoryAux.setPermissions( sliceKind.getPermission(), f.getAbsolutePath( ) );
 
         Slice sl = new Slice( did, sliceKind, sp );
         sl.setId( getId( ) );
         sl.setTerminationTime( getTerminationTime( ) );
         sl.setTotalStorageSize( storageSize );
         sl.setOwner( sp );
         sp.addSlice( sl );
 
         // todo maybe persist slice first
 //        em.persist( sl );
 
 //        em.merge( sp );
 
         addChangedModel( sl );
 
         // maybe this isn't of interesst
         addChangedModel( sp );
         
         return  sl;
     }
 
 
     public SliceKind getSliceKind( ) {
 
         return sliceKind;
     }
 
     
     public void setSliceKind( SliceKind knd ) {
 
         sliceKind = knd;
     }
 
 
     public DirectoryAux getDirectoryAux( ) {
         return directoryAux;
     }
 
 
     public void setDirectoryAux( DirectoryAux da ) {
         directoryAux = da;
     }
     
 }
