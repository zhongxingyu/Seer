 package de.zib.gndms.dspace.common;
 
 import de.zib.gndms.dspace.service.globus.resource.ExtDSpaceResourceHome;
 import de.zib.gndms.dspace.stubs.types.DSpaceReference;
 import de.zib.gndms.infra.GNDMSTools;
 import de.zib.gndms.infra.service.GNDMServiceHome;
 import de.zib.gndms.infra.system.GNDMSystem;
 import de.zib.gndms.infra.wsrf.ReloadablePersistentResource;
 import de.zib.gndms.model.common.GridResource;
 import de.zib.gndms.model.dspace.DSpaceRef;
 import org.apache.axis.types.PositiveInteger;
 import org.globus.wsrf.ResourceException;
 import org.globus.wsrf.ResourceHome;
 import org.globus.wsrf.ResourceKey;
 import org.jetbrains.annotations.NotNull;
 
 /**
  * ThingAMagic.
  *
  * @author Stefan Plantikow <plantikow@zib.de>
  * @version $Id$
  *
  *          User: stepn Date: 27.07.2008 Time: 19:18:59
  */
 public class DSpaceTools {
     /*
     public static StorageSizeT buildSizeT(StorageSize size) {
 		return new StorageSizeT(size.getUnit(), new PositiveInteger(Long.toString(size.getAmount())));
 	}
 
 	@NotNull
 	public static StorageSize buildSize(StorageSizeT sizeT) {
 		StorageSize value = new StorageSize();
 		value.setUnit(sizeT.getStorageSizeUnit());
 		value.setAmount(Long.parseLong(sizeT.getStorageSizeValue().toString()));
 		return value;
 	}
 	*/
 
 
	public static DSpaceReference dSpaceRefsAsReference(final DSpaceRef vep, GNDMSystem sysParam)
 		throws Exception {
 		if (vep.getGridSiteId() != null)
 			throw new IllegalStateException("Remote dspace reference");
 
 		final ResourceKey key = GNDMSTools.SRKVepRefAsKey(vep);
 		final ExtDSpaceResourceHome dspaceHome =
 			  sysParam.getInstance(ExtDSpaceResourceHome.class, "dspaceHome");
 		final String serviceURI = dspaceHome.getServiceAddress().toString();
 		return new DSpaceReference(org.globus.wsrf.utils.AddressingUtils.createEndpointReference(serviceURI, key));
 	}
 
 
     @SuppressWarnings({ "unchecked" })
     public static <M extends GridResource, H extends GNDMServiceHome<M> & ResourceHome>
          void refreshModelResource( M model, H home ) throws ResourceException {
 
         ResourceKey key = home.getKeyForResourceModel( model );
         ReloadablePersistentResource<M, H> rps =
                 ( ReloadablePersistentResource<M, H> ) home.find( key );
 
         rps.loadFromModel( model );
 
     }
 }
