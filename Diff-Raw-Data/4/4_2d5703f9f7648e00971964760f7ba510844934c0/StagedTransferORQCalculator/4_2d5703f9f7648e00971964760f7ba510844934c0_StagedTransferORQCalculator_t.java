 package de.zib.gndms.GORFX.action;
 
 import de.zib.gndms.c3resource.jaxb.Workspace;
 import de.zib.gndms.infra.configlet.C3MDSConfiglet;
 import de.zib.gndms.infra.system.GNDMSystem;
 import de.zib.gndms.kit.network.NetworkAuxiliariesProvider;
 import de.zib.gndms.logic.model.gorfx.AbstractORQCalculator;
 import de.zib.gndms.logic.model.gorfx.c3grid.AbstractProviderStageInORQCalculator;
 import de.zib.gndms.model.common.types.FutureTime;
 import de.zib.gndms.model.common.types.TransientContract;
 import de.zib.gndms.model.gorfx.types.GORFXConstantURIs;
 import de.zib.gndms.model.gorfx.types.SliceStageInORQ;
 import org.apache.axis.types.URI;
 import org.globus.wsrf.container.ServiceHost;
 import org.joda.time.Duration;
 
 import java.util.Set;
 
 /**
  * @author: Maik Jorra <jorra@zib.de>
  * @version: $Id$
  * <p/>
  * User: mjorra, Date: 11.11.2008, Time: 14:57:06
  */
 public class StagedTransferORQCalculator extends
     AbstractORQCalculator<SliceStageInORQ, StagedTransferORQCalculator> {
 
     // todo find pretty solution for system hack
     private static GNDMSystem system;
 
 
     public StagedTransferORQCalculator( ) {
         super( SliceStageInORQ.class );
     }
 
 
     //
     // offertime = stageing time + transfer-time
     @Override
     public TransientContract createOffer() throws Exception {
 
         // create provider staging orq using this this offer type
        AbstractProviderStageInORQCalculator psi_calc = AbstractProviderStageInORQCalculator.class.cast(
            getSystem().getInstanceDir().newORQCalculator( getSystem().getEntityManagerFactory(), GORFXConstantURIs.PROVIDER_STAGE_IN_URI ));
         
         psi_calc.setKey( getKey() );
         psi_calc.setORQArguments( getORQArguments() );
 
         TransientContract c = psi_calc.createOffer();
 
         if( c.hasExpectedSize() ) {
             long s = c.getExpectedSize( );
 
             String src = ServiceHost.getBaseURL( ).getHost();
             URI dst_uri = destinationURI( getORQArguments().getGridSite() );
             String dst = dst_uri.getHost( );
             Float ebw = getNetAux().getBandWidthEstimater().estimateBandWidthFromTo( src, dst );
 
             if( ebw == null )
                 throw new RuntimeException( "No connection beween" + src +  " and " + dst );
 
             getORQArguments().setActGridSiteURI( dst );
 
             long ms = NetworkAuxiliariesProvider.calculateTransferTime( s, ebw );
 
             c.setDeadline( FutureTime.atOffset( new Duration( ms ) ) );
         }
 
         return c;
     }
 
 
     protected static GNDMSystem getSystem( ) {
         if( system == null )
             throw new IllegalStateException ( "GNDMS not present" );
 
         return system;
     }
 
     
     public static void setSystem( GNDMSystem sys ) {
 
         if( system != null )
             throw new IllegalStateException ( "GNDMS already present" );
 
         system = sys;
     }
 
 
     public URI destinationURI( String gs ) throws URI.MalformedURIException {
         C3MDSConfiglet cfg = getConfigletProvider().getConfiglet( C3MDSConfiglet.class, C3MDSConfiglet.class.getName( ) );
         Set<Workspace.Archive> a = cfg.getCatalog().getArchivesByOid().get( gs );
         Workspace w = cfg.getCatalog().getWorkspaceByArchive().get( (Workspace.Archive) a.toArray()[0] );
         return new URI( w.getBaseUrl() );
     }
 }
 
 
