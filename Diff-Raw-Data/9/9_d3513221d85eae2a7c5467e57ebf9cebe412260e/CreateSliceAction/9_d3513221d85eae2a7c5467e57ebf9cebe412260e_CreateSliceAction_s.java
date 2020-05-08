 package de.zib.gndms.logic.model.dspace;
 
 import de.zib.gndms.logic.model.BatchUpdateAction;
 import de.zib.gndms.logic.model.CreateTimedGridResourceAction;
 import de.zib.gndms.logic.model.ModelChangedAction;
 import de.zib.gndms.logic.model.aux.DirectoryAux;
 import de.zib.gndms.logic.model.aux.LinuxDirectoryAux;
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
 
     private ModelUUIDGen uuidgen;   // the uuid generator
     private SliceKind sliceKind;     // kind of the slice to create
     private DirectoryAux directoryAux;
 
     
     public CreateSliceAction( ) {
          directoryAux = new LinuxDirectoryAux( );
     }
 
 
     public CreateSliceAction( String uuid, Calendar ttm, ModelUUIDGen uuidgen, SliceKind kind ) {
 
         super( uuid, ttm );
         this.uuidgen = uuidgen;
         this.sliceKind = kind;
         this.directoryAux = new LinuxDirectoryAux( );
     }
 
 
     public CreateSliceAction( String uuid, Calendar ttm, ModelUUIDGen uuidgen, SliceKind kind, DirectoryAux da ) {
         
         super( uuid, ttm );
         this.uuidgen = uuidgen;
         this.sliceKind = kind;
         this.directoryAux = da;
     }
 
 
     @Override
     public void initialize() {
 
         if( uuidgen == null )
             throw new IllegalThreadStateException( "No UUId generator provided" );
 
         if( sliceKind == null )
             throw new IllegalThreadStateException( "No slice kind provided" );
 
         super.initialize();
     }
 
 
     @Override
     public Slice execute( @NotNull EntityManager em ) {
 
         Subspace sp = getModel( );
 
         if( ! sp.getMetaSubspace( ).getCreatableSliceKinds( ).contains( sliceKind ) )
             return null;
 
         String lp = sp.getPath( ) +  File.separator + sliceKind.getMode( ).toString( ) + File.separator;
         File f = null;
         String did = new String( );
 
        while ( f != null && f.exists() ) {
             did = uuidgen.nextUUID();
             f = new File( lp + did );
         }
 
         try {
             // this also creats the dir for the namespace if it
             // doesn't exist yet.
             f.mkdirs( );
         } catch ( SecurityException e ) {
             return null;
         }
 
         // fix permissions
         if( sliceKind.getMode( ).equals( SliceKindMode.RW ) )
             directoryAux.setDirectoryReadWrite( f.getAbsolutePath( ) );
         else
             directoryAux.setDirectoryReadOnly( f.getAbsolutePath( ) );
 
         Slice sl = new Slice( did, sliceKind, sp );
         sl.setId( getId( ) );
         sl.setTerminationTime( getTerminationTime( ) );
         sp.addSlice( sl );
 
         // todo maybe persist slice first
         // em.persist( sl );
 
         em.merge( sp );
 
         (( BatchUpdateAction<Object> )getPostponedActions( )).addAction( new ModelChangedAction( sl ) );
 
         // maybe this isn't of interesst
         ((BatchUpdateAction<Object>)getPostponedActions( )).addAction( new ModelChangedAction( sp ) );
         
         return  sl;
     }
 
 
     public ModelUUIDGen getUUIdGen( ) {
 
         return uuidgen;
     }
 
     
     public void setUUIdGen( ModelUUIDGen ug ) {
 
         uuidgen = ug;
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
