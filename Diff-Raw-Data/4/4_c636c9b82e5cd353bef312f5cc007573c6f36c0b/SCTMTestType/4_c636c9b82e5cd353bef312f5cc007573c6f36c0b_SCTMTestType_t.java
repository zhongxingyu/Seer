 package hudson.plugins.sctmexecutor.publisher.xunit;
 
 import hudson.Extension;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
 import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
 
 public class SCTMTestType extends TestType {
   private static final long serialVersionUID = 1L;
  private static final SCTMTestType.DescriptorImpl DESCRIPTOR = new SCTMTestType.DescriptorImpl();
   
   @DataBoundConstructor
   public SCTMTestType(String pattern, boolean faildedIfNotNew, boolean deleteJUnitFiles) {
     super(pattern, faildedIfNotNew, deleteJUnitFiles);
   }
   
   @Override
   public TestTypeDescriptor<SCTMTestType> getDescriptor() {
    return DESCRIPTOR;
   }
 
   @Extension
   public static class DescriptorImpl extends TestTypeDescriptor<SCTMTestType> {
 
     public DescriptorImpl() {
       super(SCTMTestType.class, SCTMInputMetric.class);
     }
     
     @Override
     public String getId() {
       return SCTMTestType.class.getCanonicalName();
     }
     
   }
 }
