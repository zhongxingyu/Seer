package sample;
 
 import java.util.List;
 
 import flex2.compiler.Transcoder;
 import flex2.compiler.as3.As3Configuration;
 import flex2.compiler.as3.Extension;
 import flex2.compiler.extensions.IMxmlCompilerExtension;
 import flex2.compiler.mxml.MxmlConfiguration;
 import flex2.compiler.util.NameMappings;
 import flex2.compiler.util.TraceExtension;
 
 /**
  * This compiler extension processes metadata and generates the proper Reflex actionscript code from it.
 * @author Andrew Westberg <andrew@swiftmako.com>
  */
 public class SampleMxmlCompilerExtension
     implements IMxmlCompilerExtension {
 
     public void run(List<Extension> mxmlCompilerExtension, String gendir, MxmlConfiguration mxmlConfiguration,
                     As3Configuration ascConfiguration, NameMappings mappings, Transcoder[] transcoders,
                     boolean processComments) {
         mxmlCompilerExtension.add(new TraceExtension());
     }
 
 }
