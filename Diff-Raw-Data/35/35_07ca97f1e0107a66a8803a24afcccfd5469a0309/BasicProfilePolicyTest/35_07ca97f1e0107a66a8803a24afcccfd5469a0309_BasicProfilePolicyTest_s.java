 package org.mule.galaxy.policy.wsdl;
 
 import java.io.InputStream;
 import java.util.Collection;
 
 import org.mule.galaxy.ArtifactPolicyException;
 import org.mule.galaxy.ArtifactResult;
 import org.mule.galaxy.Workspace;
 import org.mule.galaxy.policy.Approval;
 import org.mule.galaxy.policy.ArtifactPolicy;
 import org.mule.galaxy.test.AbstractGalaxyTest;
 
 public class BasicProfilePolicyTest extends AbstractGalaxyTest {
 
     public void testVersioning() throws Exception {
         InputStream helloWsdl = getResourceAsStream("/wsdl/hello-invalid.wsdl");
 
         Collection<Workspace> workspaces = registry.getWorkspaces();
         assertEquals(1, workspaces.size());
         Workspace workspace = workspaces.iterator().next();
 
         ArtifactPolicy p = policyManager.getPolicy(BasicProfilePolicy.WSI_BP_1_1_WSDL);
 
         policyManager.activatePolicy(p, lifecycleManager.getDefaultLifecycle());
 
         try {
             registry.createArtifact(workspace, "application/xml", "hello-invalid.wsdl", "0.1", helloWsdl,
                                     getAdmin());
             fail("Expected ArtifactPolicyException");
         } catch (ArtifactPolicyException e) {
             Collection<Approval> approvals = e.getApprovals();
             for (Approval a : approvals) {
                 
                 for (String m : a.getMessages()) {
                     System.out.println(m);
                 }
                 
             }
             assertEquals(1, approvals.size());
 
             Approval a = approvals.iterator().next();
             assertFalse(a.isApproved());
 
             assertEquals(1, a.getMessages().size());
         }
         
         try {
             registry.createArtifact(workspace, "application/xml", "hello.wsdl", "0.1", 
                                     getResourceAsStream("/wsdl/wsi/soapbinding/r2710.wsdl"),
                                     getAdmin());
             fail("Expected ArtifactPolicyException");
         } catch (ArtifactPolicyException e) {
             Collection<Approval> approvals = e.getApprovals();
             for (Approval a : approvals) {
                 
                 for (String m : a.getMessages()) {
                     System.out.println(m);
                 }
                 
             }
             assertEquals(1, approvals.size());
 
             Approval a = approvals.iterator().next();
             assertFalse(a.isApproved());
 
             assertEquals(1, a.getMessages().size());
         }
     }
 
     @Override
     protected String[] getConfigLocations() {
         return new String[] {"/META-INF/applicationContext-core.xml"};
     }
 }
