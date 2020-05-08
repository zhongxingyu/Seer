 package de.zib.gndms.infra.service;
 
 import de.zib.gndms.infra.system.GNDMSystem;
 import de.zib.gndms.model.dspace.DSpace;
 import de.zib.gndms.model.common.GridResource;
 import org.apache.axis.types.URI;
 import org.jetbrains.annotations.NotNull;
 import org.globus.wsrf.ResourceKey;
 
 import javax.persistence.EntityManagerFactory;
 import javax.xml.namespace.QName;
 
 
 /**
  * Mockup-home for testing ModelHandler
  *
  * @author Stefan Plantikow <plantikow@zib.de>
  * @version $Id$
  *
  *          User: stepn Date: 08.08.2008 Time: 13:30:08
  */
 public final class GNDMServiceHomeMockup implements GNDMServiceHome<DSpace> {
 	private final GNDMSystem sys;
 
 
 	public GNDMServiceHomeMockup(final GNDMSystem sysParam) {
 		sys = sysParam;
 	}
 
 
 	public URI getServiceAddress() {
 		throw new UnsupportedOperationException();
 	}
 
 
 	public QName getResourceKeyTypeName() {
 		throw new UnsupportedOperationException();
 	}
 
 
 	@NotNull
 	public EntityManagerFactory getEntityManagerFactory() {
 		return sys.getEntityManagerFactory();
 	}
 
 
 	@NotNull
 	public GNDMSystem getSystem() throws IllegalStateException {
 		return sys;
 	}
 
 
 	public void setSystem(final @NotNull GNDMSystem system) throws IllegalStateException {
 		throw new IllegalStateException("Cant't overwrite system");
 	}
 
 
     public void refresh(final @NotNull GridResource resource) {
     }
 
 
     @NotNull
     public String getNickName() {
         return "dspaceMockup";
     }
 
 
     @NotNull
     public Class<DSpace> getModelClass() {
         return DSpace.class;
     }
 
     public ResourceKey getKeyForId(final GridResource model) {
         throw new UnsupportedOperationException("Mockup");
     }
 }
