 package org.cloudifysource.quality.iTests.test.rest.component;
 
 import iTests.framework.utils.AssertUtils;
 
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.httpclient.HttpStatus;
 import org.cloudifysource.dsl.rest.response.Response;
 import org.cloudifysource.dsl.rest.response.ServiceDetails;
 import org.cloudifysource.dsl.rest.response.ServiceInstanceDetails;
 import org.cloudifysource.dsl.rest.response.ServiceInstanceMetricsResponse;
 import org.cloudifysource.dsl.rest.response.ServiceMetricsResponse;
 import org.cloudifysource.quality.iTests.framework.utils.ApplicationInstaller;
 import org.cloudifysource.quality.iTests.framework.utils.rest.CloudifyRestClient;
 import org.cloudifysource.quality.iTests.framework.utils.rest.RestClientException;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.AbstractLocalCloudTest;
 import org.codehaus.jackson.type.TypeReference;
 import org.openspaces.admin.application.Application;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import com.j_spaces.kernel.PlatformVersion;
 
 public class DeploymentsControllerTest extends AbstractLocalCloudTest {
 
     protected CloudifyRestClient client;
 
 
     @BeforeClass
     public void installTravel() throws IOException, InterruptedException {
         ApplicationInstaller installer = new ApplicationInstaller(restUrl,
                 "helloworld");
         installer.recipePath("helloworld");
         installer.install();
         client = new CloudifyRestClient(restUrl + "/"
                 + PlatformVersion.getVersion() + "/deployments");
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void testGoodGetServiceDetails() throws Exception {
         testValidateApplication();
         ServiceDetails details = client.responseGetMethod(
                 "/helloworld/service/tomcat/metadata",
                 new TypeReference<Response<ServiceDetails>>() {
                 });
         AssertUtils.assertEquals("helloworld", details.getApplicationName());
         AssertUtils.assertEquals("tomcat", details.getName());
         AssertUtils.assertEquals(1, details.getNumberOfInstances());
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void testGoodGetServiceInstanceDetails() throws Exception {
         testValidateApplication();
         ServiceInstanceDetails details = client.responseGetMethod(
                 "/helloworld/service/tomcat/instances/1/metadata",
                 new TypeReference<Response<ServiceInstanceDetails>>() {
                 });
         AssertUtils.assertEquals("helloworld", details.getApplicationName());
         AssertUtils.assertEquals("tomcat", details.getServiceName());
         AssertUtils.assertEquals(1, details.getInstanceId());
         AssertUtils.assertEquals("helloworld.tomcat",
                 details.getServiceInstanceName());
         AssertUtils.assertEquals("localcloud", details.getHardwareId());
         AssertUtils.assertEquals("localcloud", details.getImageId());
         AssertUtils.assertEquals("localcloud", details.getMachineId());
         AssertUtils.assertEquals("127.0.0.1", details.getPrivateIp());
         AssertUtils.assertEquals("127.0.0.1", details.getPublicIp());
         AssertUtils.assertEquals("localcloud", details.getTemplateName());
         AssertUtils.assertNotNull(
                 "missing data ,ProcessDetails object is null",
                 details.getProcessDetails());
         AssertUtils.assertEquals("127.0.0.1",
                 details.getProcessDetails().get("Cloud Private IP"));
         AssertUtils.assertEquals("127.0.0.1",
                 details.getProcessDetails().get("Cloud Public IP"));
         AssertUtils.assertEquals(1,
                 details.getProcessDetails().get("Instance ID"));
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void testBadGetServiceDetails() throws Exception {
         try {
 
             client.responseGetMethod("/helloworldBad/service/tomcat/metadata",
                     new TypeReference<Response<ServiceDetails>>() {
                     });
             AssertUtils
                     .assertFail("getServiceDetails request should have thrown an exception due to a wrong application name");
         } catch (RestClientException e) {
 //            AssertUtils.assertEquals(
 //                    CloudifyMessageKeys.MISSING_RESOURCE.getName(),
 //                    e.getMessageId());
        	// TODO - change to 404 when CLOUDIFY-1818 is resolved
            AssertUtils.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getStatus());
         }
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void testBadGetServiceInstanceDetails() throws Exception {
         try {
             client.responseGetMethod(
                     "/helloworld/service/tomcat/instances/100/metadata",
                     new TypeReference<Response<ServiceInstanceDetails>>() {
                     }); // Bad instance Id
             AssertUtils
                     .assertFail("getServiceInstanceDetails request should have thrown an exception due to a wrong instance name");
         } catch (RestClientException e) {
 //            AssertUtils.assertEquals(
 //                    CloudifyMessageKeys.MISSING_RESOURCE.getName(),
 //                    e.getMessageId());
            AssertUtils.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getStatus());
         }
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void testGoodServiceMetricsResponse() throws Exception {
         testValidateApplication();
         String url = "/helloworld/service/tomcat/metrics";
         ServiceMetricsResponse serviceMetricsResponse = client
                 .responseGetMethod(url,
                         new TypeReference<Response<ServiceMetricsResponse>>() {
                         });
         AssertUtils.assertEquals("helloworld",
                 serviceMetricsResponse.getAppName());
         AssertUtils.assertEquals("tomcat",
                 serviceMetricsResponse.getServiceName());
         AssertUtils.assertNotNull("service instance metrics data is null",
                 serviceMetricsResponse.getServiceInstaceMetricsData());
         AssertUtils.assertEquals(serviceMetricsResponse
                 .getServiceInstaceMetricsData().size(), 1);
         AssertUtils.assertEquals(1, serviceMetricsResponse
                 .getServiceInstaceMetricsData().get(0).getInstanceId());
 
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void testGoodServiceMetricsInstanceResponse() throws Exception {
         testValidateApplication();
         String url = "/helloworld/service/tomcat/instances/1/metrics";
         ServiceInstanceMetricsResponse serviceInstanceMetricsResponse = client
                 .responseGetMethod(
                         url,
                         new TypeReference<Response<ServiceInstanceMetricsResponse>>() {
                         });
         AssertUtils.assertEquals("helloworld",
                 serviceInstanceMetricsResponse.getAppName());
         AssertUtils.assertEquals("tomcat",
                 serviceInstanceMetricsResponse.getServiceName());
         AssertUtils.assertNotNull("service instance metrics data is null",
                 serviceInstanceMetricsResponse.getServiceInstanceMetricsData());
         AssertUtils.assertEquals(1, serviceInstanceMetricsResponse
                 .getServiceInstanceMetricsData().getInstanceId());
     }
 
     protected void testValidateApplication() {
         Application app = admin.getApplications().waitFor("helloworld",
                 OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
         AssertUtils.assertTrue("Failed to discover application helloworld",
                 app != null);
     }
 
     @Override
     @AfterMethod(alwaysRun = true)
     public void cleanup() throws Exception {
         // dont uninstall. we need it for other tests
     }
 
     @AfterClass(alwaysRun = true)
     public void uninstall() throws Exception {
         // here is where we cleanup
         super.cleanup();
     }
 }
