 package org.apache.felix.ipojo.everest.casa;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Instantiate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.everest.casa.device.GenericDeviceManager;
 import org.apache.felix.ipojo.everest.casa.person.PersonManager;
 import org.apache.felix.ipojo.everest.casa.zone.ZoneManager;
 import org.apache.felix.ipojo.everest.impl.ImmutableResourceMetadata;
 import org.apache.felix.ipojo.everest.services.Path;
 import org.apache.felix.ipojo.everest.services.Resource;
 import org.apache.felix.ipojo.everest.services.ResourceMetadata;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: root
  * Date: 09/07/13
  * Time: 14:00
  * To change this template use File | Settings | File Templates.
  */
 @Component(name = "CasaRootRessource")
 @Provides(specifications = Resource.class)
 @Instantiate
 public class CasaRootResource extends AbstractResourceCollection {
 
     /*
     * Name ressource
      */
    public static final String m_casaRoot = "org.apache.felix.ipojo.everest.everestApi.casa";
 
     /*
      * Path of the ressource
     */
     public static final Path m_casaRootPath = Path.from(Path.SEPARATOR + m_casaRoot);
 
     /*
      * Description of the ressource
     */
    private static final String m_casaDescription = "org.apache.felix.ipojo.everest.everestApi.casa resources";
 
     /*
      * List of the subRessource
     */
     private final List<Resource> m_casaResources = new ArrayList<Resource>();
 
     /*
      * Constructor
     */
     public CasaRootResource() {
         super(m_casaRootPath);
         m_casaResources.add(GenericDeviceManager.getInstance());
         m_casaResources.add(PersonManager.getInstance());
         m_casaResources.add(ZoneManager.getInstance());
     }
 
 
     @Override
     public List<Resource> getResources() {
         return m_casaResources;
     }
 
     @Override
     public ResourceMetadata getMetadata() {
         ImmutableResourceMetadata.Builder metadataBuilder = new ImmutableResourceMetadata.Builder();
         metadataBuilder.set("Name", m_casaRoot);
         metadataBuilder.set("Path", m_casaRootPath);
         return metadataBuilder.build();
     }
 }
