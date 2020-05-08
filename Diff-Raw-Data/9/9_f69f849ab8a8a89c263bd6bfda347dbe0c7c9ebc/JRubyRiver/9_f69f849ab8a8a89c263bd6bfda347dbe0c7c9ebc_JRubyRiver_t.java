 package com.asquera.elasticsearch.river.jruby;
 
 import java.util.Map;
 import java.util.List;
 
 import java.net.URL;
 import java.net.MalformedURLException;
 
 import java.io.File;
 
 import org.elasticsearch.ExceptionsHelper;
 import org.elasticsearch.action.ActionListener;
 import org.elasticsearch.action.bulk.BulkResponse;
 import org.elasticsearch.client.Client;
 import org.elasticsearch.client.Requests;
 import org.elasticsearch.client.action.bulk.BulkRequestBuilder;
 import org.elasticsearch.cluster.block.ClusterBlockException;
 import org.elasticsearch.common.Strings;
 import org.elasticsearch.common.inject.Inject;
 import org.elasticsearch.common.unit.TimeValue;
 import org.elasticsearch.common.xcontent.XContentBuilder;
 import org.elasticsearch.common.xcontent.XContentFactory;
 import org.elasticsearch.common.xcontent.support.XContentMapValues;
 import org.elasticsearch.indices.IndexAlreadyExistsException;
 import org.elasticsearch.river.AbstractRiverComponent;
 import org.elasticsearch.river.River;
 import org.elasticsearch.river.RiverName;
 import org.elasticsearch.river.RiverSettings;
 import org.elasticsearch.threadpool.ThreadPool;
 
 import org.jruby.Ruby;
 import org.jruby.RubyModule;
 import org.jruby.RubyObject;
 import org.jruby.javasupport.JavaUtil;
 import org.jruby.RubyClass;
 import org.jruby.embed.ScriptingContainer;
 import org.jruby.embed.PathType;
 import org.jruby.embed.AttributeName;
 import org.jruby.javasupport.util.RuntimeHelpers;
 import org.jruby.runtime.builtin.IRubyObject;
 import org.jruby.runtime.ThreadContext;
 import org.jruby.runtime.GlobalVariable;
 import org.jruby.internal.runtime.GlobalVariables;
 import org.jruby.anno.JRubyMethod;
 import static org.jruby.runtime.Visibility.*;
 import static org.jruby.CompatVersion.*;
 import org.jruby.embed.LocalContextScope;
 
 /**
  * @author Florian Gilcher (florian.gilcher@asquera.de)
  */
 public class JRubyRiver extends AbstractRiverComponent implements River {
     private ScriptingContainer container;
     private IRubyObject river;
     private Client client;
     private ThreadPool threadPool;
     
     @Inject public JRubyRiver(RiverName riverName, RiverSettings settings, Client client, ThreadPool threadPool) throws Exception {
         super(riverName, settings);
         this.client = client;
         this.threadPool = threadPool;
         
         this.container = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
         
        Map <String, Object> riverSettings = jrubySettings(settings);

         String scriptName = null;
         String scriptDirectory = null;
         
         if (null != riverSettings) {
             scriptName      = XContentMapValues.nodeStringValue(riverSettings.get("script_name"), "scripts/" +  riverName.name() + ".rb");
             scriptDirectory = XContentMapValues.nodeStringValue(riverSettings.get("script_directory"), null);
         } else {
             scriptName      = "scripts/" +  riverName.name() + ".rb";
         }
         
         if (null != scriptDirectory) {
             logger.info("changing to script directory [{}]", scriptDirectory);
             container.setAttribute(AttributeName.BASE_DIR, scriptDirectory);
             container.setCurrentDirectory(scriptDirectory);
         }
 
         Ruby runtime = setupRuntime();
 
         logger.info("running script [{}]", scriptName);
         container.runScriptlet(PathType.RELATIVE, scriptName);
         logger.info("found river class [{}]", river(runtime));
 
         IRubyObject klass = river(runtime);
         
         if (klass.isNil()) {
             this.river = toplevel();
         } else {
             this.river = RuntimeHelpers.invoke(runtime.getCurrentContext(), klass, "new");            
         }
     }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> jrubySettings(RiverSettings settings) {
         return (Map<String,Object>) settings.settings().get("jruby");
    }
 
     @Override public void start() {
         try {
             RuntimeHelpers.invoke(getRuntime().getCurrentContext(), this.river, "start");
         } catch (Exception e) {
             logger.error("error in River#start [{}]", e);
         }
     }
 
     @Override public void close() {
         try {
             RuntimeHelpers.invoke(getRuntime().getCurrentContext(), this.river, "close");
         } catch (Exception e) {
             logger.error("error in River#close [{}]", e);
         }
     }
     
     private RubyObject toplevel() {
         return (RubyObject)getRuntime().getTopSelf();
     }
     
     private Ruby setupRuntime() {
         Ruby runtime = getRuntime();
         
         RubyModule kernel = runtime.getKernel();
         kernel.defineAnnotatedMethods(KernelMethods.class);
         
         RubyModule riverSettings = runtime.defineModule("RiverSettings");
         riverSettings.defineAnnotatedMethods(RiverSettingsModule.class);
         
         GlobalVariables vars = runtime.getGlobalVariables();
         runtime.defineVariable(new GlobalVariable(runtime, "$river", runtime.getNil()));
         runtime.defineVariable(new GlobalVariable(runtime, "$client", JavaUtil.convertJavaToRuby(runtime, client)));
         runtime.defineVariable(new GlobalVariable(runtime, "$threadpool", JavaUtil.convertJavaToRuby(runtime, threadPool)));
         runtime.defineVariable(new GlobalVariable(runtime, "$settings", JavaUtil.convertJavaToRuby(runtime, settings)));
         runtime.defineVariable(new GlobalVariable(runtime, "$name", JavaUtil.convertJavaToRuby(runtime, riverName.name())));
         runtime.defineVariable(new GlobalVariable(runtime, "$logger", JavaUtil.convertJavaToRuby(runtime, logger)));
         
         toplevel().extend(new IRubyObject[]{riverSettings});
         
         return runtime;
     }
     
     private Ruby getRuntime() {
         return container.getProvider().getRuntime();
     }
     
     private IRubyObject river(Ruby runtime) {
         return runtime.getGlobalVariables().get("$river");
     }
 }
