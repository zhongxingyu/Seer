package n3phele;
 
 import java.net.URI;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 
 import n3phele.service.model.Cloud;
 import n3phele.service.model.ParameterType;
 import n3phele.service.model.TypedParameter;
 import n3phele.service.model.core.BaseEntity;
 import n3phele.service.model.core.Collection;
 import n3phele.service.model.core.Entity;
 import n3phele.service.rest.impl.CloudResource;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.GenericType;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 import com.sun.jersey.api.representation.Form;
 
 public class CloudWebServiceTest  {
 	public CloudWebServiceTest() throws Exception {
 
 	}
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		client = Client.create();
 		client.addFilter(new HTTPBasicAuthFilter("test-user@gmail.com", "testit!"));
 		webResource = client.resource(UriBuilder.fromUri("http://127.0.0.1:8888/resources").path(CloudResource.class).build());
 	}
 
 	private Client client;
 	private WebResource webResource;
 
 	@Test
 	public void addEC2CloudTest() {
 		URI cloud = null;
 		Collection<BaseEntity> response = webResource.queryParam("summary", "false").get(new GenericType<Collection<BaseEntity>>() {});
 		if(response.getTotal() > 0) {
 			for(Entity c : response.getElements()) {
 				if(c.getName().equals("EC2")) {
 					cloud = c.getUri();
 				}
 			}
 		}
 
 		if(cloud == null) {
 
 
 			String myName = "EC2";
 			String description = "Amazon Elastic Compute Service";
 			URI location = URI.create("https://ec2.amazonaws.com");
 			URI factory =  URI.create("https://ec2factory.appspot.com/resources/virtualServer");
 			String factoryId = "fred";
 			String mySecret = "3hyebbehg56yeh5";
 			Form form = new Form();
 			form.add("name", myName);
 			form.add("description", description);
 			form.add("location", location);
 			form.add("factory", factory);
 			form.add("factoryId", factoryId);
 			form.add("secret", mySecret);
 			form.add("isPublic", true);
 
 			ClientResponse result = webResource.post(ClientResponse.class, form);
 			cloud = result.getLocation();
 
 
 			Assert.assertEquals(201, result.getStatus());  
 		}
 		
 
 
 	}
 	
 	static TypedParameter EC2Defaults[] = {
 		new TypedParameter("instanceType", "specifies virtual machine size. Valid Values: t1.micro | m1.small | m1.large | m1.xlarge | m2.xlarge | m2.2xlarge | m2.4xlarge | c1.medium | c1.xlarge", n3phele.service.model.ParameterType.String, "", "t1.micro"),
 		new TypedParameter("imageId", "Unique ID of a machine image, returned by a call to RegisterImage", ParameterType.String, "", "ami-54cf5c3d"),
 		new TypedParameter("securityGroups", "Name of the security group which controls the open TCP/IP ports for the VM.", ParameterType.String, "", "n3phele-default"),
 		new TypedParameter("userData", "Base64-encoded MIME user data made available to the instance(s). May be used to pass startup commands.", ParameterType.String, "", "#!/bin/bash\necho n3phele agent injection... \nset -x\n wget -q -O - https://n3phele-agent.s3.amazonaws.com/n3ph-install-tgz-basic | su - -c '/bin/bash -s ec2-user ~/agent ~/sandbox https://region-a.geo-1.objects.hpcloudsvc.com:443/v1/AUTH_dc700102-734c-4a97-afc8-50530e87a171/n3phele-agent/n3phele-agent.tgz' ec2-user\n")		
 	};
 	@Test
 	public void testInitCloudDefaults() throws Exception {
 		Cloud cloud = webResource.path("byName").queryParam("id","EC2").accept(MediaType.APPLICATION_JSON_TYPE).get(Cloud.class);
 		for(TypedParameter t : EC2Defaults) {
 			Form form = new Form();
 			form.add("key", t.getName());
 			form.add("defaultValue", t.getDefaultValue());
 			form.add("type", t.getType().toString());
 			
 	
 			ClientResponse result = webResource.uri(cloud.getUri()).path("inputParameter").post(ClientResponse.class, form);
 			Assert.assertEquals(200, result.getStatus());  
 		}
 
 	}
 	
 	 //@Test
 	  public void testCloudDelete() throws Exception {
 
 		Cloud cloud = webResource.path("byName").queryParam("id","EC2").accept(MediaType.APPLICATION_JSON_TYPE).get(Cloud.class);
 	  	ClientResponse response = webResource.uri(cloud.getUri()).delete(ClientResponse.class);
 
 	  	Assert.assertEquals(204, response.getStatus()); 
 	  	 
 	  }
 	  
 	  @Test
 		public void addHPCloudTest() {
 			URI cloud = null;
 			
 			String cloudName = "HPZone1";
 			
 			Collection<BaseEntity> response = webResource.queryParam("summary", "false").get(new GenericType<Collection<BaseEntity>>() {});
 			if(response.getTotal() > 0) {
 				for(Entity c : response.getElements()) {
 					if(c.getName().equals(cloudName)) {
 						cloud = c.getUri();
 					}
 				}
 			}
 
 			if(cloud == null) {
 
 				String myName = cloudName;
 				String description = "HP Cloud";
 				URI location = URI.create("https://az-1.region-a.geo-1.compute.hpcloudsvc.com/v1.1/12365734013392");
 				URI factory =  URI.create("https://nova-factory.appspot.com/resources/virtualServer");
 				//URI factory =  URI.create("http://127.0.0.1:8889/resources/virtualServer");
 				String factoryId = "user";
 				String mySecret = "password";
 				Form form = new Form();
 				form.add("name", myName);
 				form.add("description", description);
 				form.add("location", location);
 				form.add("factory", factory);
 				form.add("factoryId", factoryId);
 				form.add("secret", mySecret);
 				form.add("isPublic", true);
 
 				ClientResponse result = webResource.post(ClientResponse.class, form);
 				cloud = result.getLocation();
 
 				Assert.assertEquals(201, result.getStatus());  
 			}
 
 		}
 	  
 	  static TypedParameter HPCloudDefaults[] = {
 		  	new TypedParameter("instanceType", "Specifies the virtual machine size. Valid Values: 100 (standard.xsmall), 101 (standard.small), 102 (standard.medium), 103 (standard.large), 104 (standard.xlarge), 105 (standard.2xlarge)", ParameterType.String,"", "100"),
 		  	new TypedParameter("imageId", "Unique ID of a machine image, returned by a call to RegisterImage", ParameterType.String, "", "75845"),
 		  	new TypedParameter("locationId", "Unique ID of hpcloud zone. Valid Values: az-1.region-a.geo-1 | az-2.region-a.geo-1 | az-3.region-a.geo-1", ParameterType.String, null, "az-1.region-a.geo-1"),
 		  	new TypedParameter("securityGroup", "Name of the security group which controls the open TCP/IP ports for the VM.", ParameterType.String, "", "n3phele-default"),
 		  	new TypedParameter("userData", "Base64-encoded MIME user data made available to the instance(s). May be used to pass startup commands.", ParameterType.String, "", "#!/bin/bash\necho n3phele agent injection... \nset -x\n apt-get update;  apt-get install -y openjdk-6-jre-headless \n wget -q -O - https://n3phele-agent.s3.amazonaws.com/n3ph-install-tgz-basic | su - -c '/bin/bash -s ubuntu ~/agent ~/sandbox https://region-a.geo-1.objects.hpcloudsvc.com:443/v1/AUTH_dc700102-734c-4a97-afc8-50530e87a171/n3phele-agent/n3phele-agent.tgz' ubuntu\n")	,	
 	};
 	  		
 		@Test
 		public void testInitHPCloudDefaults() throws Exception {
 			Cloud cloud = webResource.path("byName").queryParam("id","HPZone1").accept(MediaType.APPLICATION_JSON_TYPE).get(Cloud.class);
 			for(TypedParameter t : HPCloudDefaults) {
 				Form form = new Form();
 			  	form.add("key", t.getName());
 			  	form.add("defaultValue", t.getDefaultValue());
 			  	form.add("type", t.getType().toString());
 			  		
 			  	ClientResponse result = webResource.uri(cloud.getUri()).path("inputParameter").post(ClientResponse.class, form);
 			  	Assert.assertEquals(200, result.getStatus());  
 			  	}
 			  		
 }
 	  			  
 	  
 	  //@Test
 	  public void testHPCloudDelete() throws Exception {
 
 		Cloud cloud = webResource.path("byName").queryParam("id","HPZone1").accept(MediaType.APPLICATION_JSON_TYPE).get(Cloud.class);
 	  	ClientResponse response = webResource.uri(cloud.getUri()).delete(ClientResponse.class);
 
 	  	Assert.assertEquals(204, response.getStatus()); 
 	  	 
 	  }
 
 }
