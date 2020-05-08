 package org.fiteagle.interactors.sfa.rspec;
 
 import java.lang.annotation.Annotation;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.fiteagle.adapter.common.Named;
 import org.fiteagle.adapter.common.Publish;
 import org.fiteagle.adapter.common.ResourceAdapter;
 import org.fiteagle.adapter.common.SSHAccessable;
 import org.fiteagle.core.ResourceAdapterManager;
import org.fiteagle.core.config.InterfaceConfiguration;
 import org.fiteagle.interactors.sfa.common.Geni_RSpec_Version;
 
 public class SFAv3RspecTranslator {
 
	private static final String COMPONENT_ID_PREFIX = "urn:publicid:IDN+"+InterfaceConfiguration.getInstance().getDomain()+"+";
	private static final String COMPONENT_MANAGER_ID = InterfaceConfiguration.getInstance().getAM_URN();
 	private final Geni_RSpec_Version geni_rspec_version;
 	private final String adRspecNamespace = "http://www.geni.net/resources/rspec/3";
 	private final String adRspecSchema = "http://www.geni.net/resources/rspec/3/ad.xsd";
 	private final ArrayList<String> adRspecExtensions = new ArrayList<String>();
 
 	private final String requestRspecNamespace = "http://www.geni.net/resources/rspec/3";
 	private final String requestRspecSchema = "http://www.geni.net/resources/rspec/3/request.xsd";
 	private final ArrayList<String> requestRspecExtensions = new ArrayList<String>();
 
 	private final String RSPEC_EXTENSION = "http://fiteagle.org/rspec/ext/1";
 
 	public SFAv3RspecTranslator() {
 		geni_rspec_version = new Geni_RSpec_Version();
 		geni_rspec_version.setType("GENI");
 		geni_rspec_version.setVersion("3");
 		addAdRspecExtension(this.RSPEC_EXTENSION);
 		addRequestRspecExtension(RSPEC_EXTENSION);
		
 	}
 
 	public String getAdRspecNamespace() {
 		return adRspecNamespace;
 	}
 
 	public String getAdRspecSchema() {
 		return adRspecSchema;
 	}
 
 	public String[] getAdRspecExtensions() {
 		return adRspecExtensions.toArray(new String[adRspecExtensions.size()]);
 	}
 
 	public String getRequestRspecNamespace() {
 		return requestRspecNamespace;
 	}
 
 	public String getRequestRspecSchema() {
 		return requestRspecSchema;
 	}
 
 	public String[] getRequestRspecExtensions() {
 		return requestRspecExtensions.toArray(new String[requestRspecExtensions
 				.size()]);
 	}
 
 	public String getType() {
 		return this.geni_rspec_version.getType();
 	}
 
 	public String getVersion() {
 		return this.geni_rspec_version.getVersion();
 	}
 
 	private void addRequestRspecExtension(String extension) {
 		requestRspecExtensions.add(extension);
 	}
 
 	private void addAdRspecExtension(String extension) {
 		adRspecExtensions.add(extension);
 	}
 
 	public Object translateToFITeagleResource(ResourceAdapter resourceAdapter) {
 
 		Resource fiteagleSFAResource = new Resource();
 
 		fiteagleSFAResource.getMethod().addAll(
 				this.getResourceAdapterRspecMethods(resourceAdapter));
 
 		fiteagleSFAResource.setName(resourceAdapter.getType());
 
 		HashMap<String, Object> resourceAdapterProperties = resourceAdapter
 				.getProperties();
 
 		if (resourceAdapterProperties != null) {
 
 			Set<String> propKeys = resourceAdapterProperties.keySet();
 
 			for (Iterator<String> iterator = propKeys.iterator(); iterator
 					.hasNext();) {
 				Property tmpProperty = new Property();
 				String key = iterator.next();
 				tmpProperty.setName(key);
 				// The resource adaptor properties must have a string
 				// representation.
 				tmpProperty.setValue(resourceAdapterProperties.get(key)
 						.toString());
 				fiteagleSFAResource.getProperty().add(tmpProperty);
 			}
 
 		}
 
 		Property typeProperty = new Property();
 		typeProperty.setName("type");
 		// idProperty.setType("string");
 		typeProperty.setValue(resourceAdapter.getType());
 		fiteagleSFAResource.getProperty().add(typeProperty);
 
 		Property idProperty = new Property();
 		idProperty.setName("id");
 		// idProperty.setType("string");
 		idProperty.setValue(resourceAdapter.getId());
 		fiteagleSFAResource.getProperty().add(idProperty);
 
 		return new ObjectFactory().createResource(fiteagleSFAResource);
 	}
 
 	private List<Method> getResourceAdapterRspecMethods(
 			ResourceAdapter resourceAdapter) {
 		ArrayList<Method> result = new ArrayList<Method>();
 		java.lang.reflect.Method[] resourceAdapterMethods = resourceAdapter
 				.getClass().getDeclaredMethods();
 		for (int i = 0; i < resourceAdapterMethods.length; i++) {
 			java.lang.reflect.Method method = resourceAdapterMethods[i];
 			if (method.isAnnotationPresent(Publish.class)) {
 				Method tmpRspecMethod = new Method();
 
 				tmpRspecMethod.setName(method.getName());
 				tmpRspecMethod.setReturnType(method.getReturnType().getName());
 				if (!getRspecParametersFromResourceAdapterMethod(method)
 						.isEmpty()) {
 					tmpRspecMethod
 							.getParameter()
 							.addAll(getRspecParametersFromResourceAdapterMethod(method));
 				}
 				result.add(tmpRspecMethod);
 			}
 		}
 		return result;
 
 	}
 
 	private ArrayList<Parameter> getRspecParametersFromResourceAdapterMethod(
 			java.lang.reflect.Method method) {
 
 		ArrayList<Parameter> result = new ArrayList<Parameter>();
 		Class<?>[] paramTypes = method.getParameterTypes();
 		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
 		for (int i = 0; i < paramTypes.length; i++) {
 			Parameter rspecParameter = new Parameter();
 			rspecParameter.setType(paramTypes[i].getName());
 			if (parameterAnnotations[i][0] != null
 					&& parameterAnnotations[i][0].annotationType().equals(
 							Named.class)) {
 				rspecParameter.setName(((Named)parameterAnnotations[i][0]).name());
 			} else {
 
 				rspecParameter.setName("arg0" + paramTypes[i].getName());
 
 			}
 			result.add(rspecParameter);
 		}
 		return result;
 	}
 
 	public ResourceAdapter translateResourceToResourceAdapter(Resource object) {
 		List<Property> properties = object.getProperty();
 
 		ResourceAdapter resource;
 		HashMap<String, Object> resourceProperties = new HashMap<String, Object>();
 		String id = "";
 		for (Iterator iterator = properties.iterator(); iterator.hasNext();) {
 			Property property = (Property) iterator.next();
 			if (property.getName().compareToIgnoreCase("id") == 0) {
 				id = property.getValue();
 
 				break;
 			}
 		}
 		resource = ResourceAdapterManager.getInstance()
 				.getResourceAdapterInstance(id);
 		return resource;
 	}
 
 	public String translateResourceIdToSliverUrn(String id, String sliceUrn) {
 		String[] str = sliceUrn.split("\\+slice\\+");
 		String response = str[0] + "+sliver+" + id;
 		return response;
 	}
 
 	public String getIdFromSliverUrn(String urn) {
 		String[] str = urn.split("\\+sliver\\+");
 		return str[1];
 	}
 
 	// TODO quick and dirty for demo, new concept of resource description and
 	// management urgently needed!!!!!
 	public Object translateToNode(ResourceAdapter resourceAdapter) {
 
 		NodeContents node = new NodeContents();
 		HashMap<String, Object> resourceAdapterProperties = resourceAdapter
 				.getProperties();
 
 		if (resourceAdapterProperties != null) {
 			ObjectFactory factory = new ObjectFactory();
 			Set<String> propKeys = resourceAdapterProperties.keySet();
 			node.setComponentId(COMPONENT_ID_PREFIX + resourceAdapter.getId());
 			node.setComponentManagerId(COMPONENT_MANAGER_ID);
 			node.setExclusive(resourceAdapter.isExclusive());
 			List<Object> nodeContent = node.getAnyOrRelationOrLocation();
 			AvailableContents available = new AvailableContents();
 			available.setNow(resourceAdapter.isAvailable());
 			nodeContent.add(factory.createAvailable(available));
 
 			LocationContents location = new LocationContents();
 			location.setCountry((String) resourceAdapterProperties
 					.get("country"));
 			location.setLatitude((String) resourceAdapterProperties
 					.get("latitude"));
 			location.setLongitude((String) resourceAdapterProperties
 					.get("longitude"));
 			nodeContent.add(factory.createLocation(location));
 
 			if (resourceAdapter instanceof SSHAccessable) {
 				SSHAccessable sshAccessableResource = (SSHAccessable) resourceAdapter;
 
 				HardwareTypeContents hardwareType = new HardwareTypeContents();
 				hardwareType.setName(sshAccessableResource.getHardwareType());
 				nodeContent.add(factory.createHardwareType(hardwareType));
 
 				List<Object> services = node.getAnyOrRelationOrLocation();
 				ServiceContents service = new ServiceContents();
 				List<Object> logins = service.getAnyOrLoginOrInstall();
 				LoginServiceContents login = new LoginServiceContents();
 
 				login.setAuthentication("ssh-keys");
 				login.setHostname(sshAccessableResource.getIp());
 				login.setPort(sshAccessableResource.getPort());
 
 				logins.add(new ObjectFactory().createLogin(login));
 
 				services.add(new ObjectFactory().createServices(service));
 				// TODO: add node properties
 			}
 		}
 
 		return new ObjectFactory().createNode(node);
 	}
 
 	public Object translateSSHAccesableToAdvertisementNode(
 			ResourceAdapter resourceAdapter) {
 		NodeContents node = new NodeContents();
 
 		HashMap<String, Object> resourceAdapterProperties = resourceAdapter
 				.getProperties();
 
 		if (resourceAdapterProperties != null) {
 
 			ObjectFactory factory = new ObjectFactory();
 
 			HashMap<String, Object> resourceProperties = resourceAdapter
 					.getProperties();
 			node.setComponentId(COMPONENT_ID_PREFIX + resourceAdapter.getId());
 			node.setComponentManagerId(COMPONENT_MANAGER_ID);
 			node.setExclusive(resourceAdapter.isExclusive());
 
 			List<Object> nodeContent = node.getAnyOrRelationOrLocation();
 
 			AvailableContents available = new AvailableContents();
 			// available.setNow((boolean)resourceAdapterProperties.get("available"));
 			available.setNow(resourceAdapter.isAvailable());
 			nodeContent.add(factory.createAvailable(available));
 
 			LocationContents location = new LocationContents();
 			location.setCountry((String) resourceAdapterProperties
 					.get("country"));
 			location.setLatitude((String) resourceAdapterProperties
 					.get("latitude"));
 			location.setLongitude((String) resourceAdapterProperties
 					.get("longitude"));
 			nodeContent.add(factory.createLocation(location));
 
 			SSHAccessable sshAccesableResource = (SSHAccessable) resourceAdapter;
 
 			HardwareTypeContents hardwareType = new HardwareTypeContents();
 			hardwareType.setName(sshAccesableResource.getHardwareType());
 			nodeContent.add(factory.createHardwareType(hardwareType));
 
 		}
 
 		return new ObjectFactory().createNode(node);
 	}
 }
