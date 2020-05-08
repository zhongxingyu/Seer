 package natrem.tool.filereader;
 
 import natrem.tool.filereader.input.FileContentInput;
 import natrem.tool.filereader.input.FileContentInputBuilder;
 import natrem.tool.filereader.input.InputBuilder;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.HierarchicalConfiguration;
 import org.apache.commons.configuration.XMLConfiguration;
 
 import com.google.common.base.Function;
 
 public class FileReader {
 
     private InputBuilder inputBuilder = new FileContentInputBuilder();
     private FileContentInput input;
 
     public void init(String configFilePath) throws ConfigurationException {
         init(new XMLConfiguration(configFilePath));
     }
 
     public void init(HierarchicalConfiguration configuration) {
         HierarchicalConfiguration inputConfiguration = getInputsConfiguration(configuration);
         input = inputBuilder.buildFromConfig(inputConfiguration);
     }
 
     private HierarchicalConfiguration getInputsConfiguration(HierarchicalConfiguration configuration) {
         // cast allowed because implementation always return a
         // HierarchicalConfiguration
         return (HierarchicalConfiguration) configuration.subset("inputs");
     }
     
     public void setInputBuilder(InputBuilder inputBuilder) {
         this.inputBuilder = inputBuilder;
     }
 
     public void apply(Function<String, Void> output) {
         if (input != null) {
             for (String line : input) {
                 output.apply(line);
             }
         }
     }
     
 }
