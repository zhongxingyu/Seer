 package eu.europeana.uim.command;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.PrintStream;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.felix.gogo.commands.Action;
 import org.apache.felix.gogo.commands.Argument;
 import org.apache.felix.gogo.commands.Command;
 import org.apache.felix.gogo.commands.Option;
 import org.apache.felix.service.command.CommandSession;
 
 import eu.europeana.uim.api.IngestionPlugin;
 import eu.europeana.uim.api.Registry;
 import eu.europeana.uim.api.ResourceEngine;
 import eu.europeana.uim.api.StorageEngine;
 import eu.europeana.uim.api.StorageEngineException;
 import eu.europeana.uim.store.Collection;
 import eu.europeana.uim.store.Provider;
 import eu.europeana.uim.util.SampleProperties;
 import eu.europeana.uim.workflow.Workflow;
 
 /**
  * Store for the UIM process.
  * 
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @since Mar 22, 2011
  */
 @SuppressWarnings({ "rawtypes", "unchecked" })
 @Command(name = "uim", scope = "store")
 public class UIMStore implements Action {
     private static final Logger log = Logger.getLogger(UIMStore.class.getName());
 
     protected enum Operation {
         createProvider("<mnemonic> <name> [true|false] the mnemonic, name and aggregator flag"),
 
         updateProvider(
                        "<mnemonic> <field> <value> set the appropriate field value (field=oaiBaseUrl|oaiMetadataPrefix"),
 
         listProvider("lists the providers"),
 
         createCollection(
                          "p provider <mnemonic> <name> the provider as well as the mnemonic and name values"),
 
         updateCollection(
                          "<mnemonic> <field> <value> set the appropriate field value (field=oaiBaseUrl|oaiMetadataPrefix|language"),
 
         setMacsIndex("Sets the given path as base directory for the MACS index!"),
 
         addBlacklistWorkflow(
                              "Puts the given workflow onto the blacklist (stored in resource engine)"),
 
         removeBlacklistWorkflow(
                                 "Remove the given workflow from the blacklist (stored in resource engine)"),
 
         listCollection("lists the collections"),
 
         listGlobalResources("lists the global resources"),
 
         listProviderResources("lists the provider resources"),
 
         listCollectionResources("lists the collection resources"),
 
         checkpoint("creates a checkpoint (a data synchronization)"),
 
         loadConfigData("loads a set of provider/collections"),
 
         loadSampleData("loads a set of sample provider/collections");
 
         private String desc;
 
         private Operation(String desc) {
             this.desc = desc;
         }
 
         public String getDescription() {
             return desc;
         }
     }
 
     private Registry    registry;
 
     @Option(name = "-o", aliases = { "--operation" }, required = false)
     protected Operation operation;
 
     @Option(name = "-p", aliases = { "--parent" })
     protected String    parent;
 
     @Argument(index = 0)
     protected String    argument0;
 
     @Argument(index = 1)
     protected String    argument1;
 
     @Argument(index = 2)
     protected String    argument2;
 
     /**
      * Creates a new instance of this class.
      * 
      * @param registry
      */
     public UIMStore(Registry registry) {
         this.registry = registry;
     }
 
     @Override
     public Object execute(CommandSession session) throws Exception {
         PrintStream out = session.getConsole();
 
         if (operation == null) {
             out.println("Please specify an operation with the '-o' option. Possible values are:");
             for (Operation o : Operation.values()) {
                 out.println(o.toString() + "\t" + o.getDescription());
             }
             return null;
         }
 
         try {
             StorageEngine storage = registry.getStorageEngine();
             ResourceEngine resource = registry.getResourceEngine();
 
             switch (operation) {
             case createProvider:
                 createProvider(storage, out);
                 break;
             case updateProvider:
                 updateProvider(storage, out);
                 break;
             case listProvider:
                 listProvider(storage, out);
                 break;
             case createCollection:
                 createCollection(storage, out);
                 break;
             case updateCollection:
                 updateCollection(storage, out);
                 break;
             case setMacsIndex:
                 setMacsIndex(resource, argument0);
                 break;
             case addBlacklistWorkflow:
                 addBlacklistWorkflow(resource, argument0);
                 break;
             case removeBlacklistWorkflow:
                 removeBlacklistWorkflow(resource, argument0);
                 break;
             case listCollection:
                 listCollection(storage, out);
                 break;
             case listGlobalResources:
                 listGlobalResources(storage, resource, out);
                 break;
             case listProviderResources:
                 listProviderResources(storage, resource, out);
                 break;
             case listCollectionResources:
                 listCollectionResources(storage, resource, out);
                 break;
             case checkpoint:
                 checkpoint(storage, out);
                 break;
             case loadConfigData:
                 new SampleProperties().loadConfigData(storage, new FileInputStream(new File(
                         argument0)));
                 break;
             case loadSampleData:
                 new SampleProperties().loadSampleData(storage);
                 break;
             }
         } catch (Throwable t) {
             log.log(Level.SEVERE, "Failed to start storage command:", t);
         }
 
         return null;
     }
 
     /**
      * key for macs index path in resource engine
      */
     public static List<String> macsKey = new ArrayList<String>() {
                                            {
                                                add("MACS Indexpath");
                                            }
                                        };
 
     private void setMacsIndex(ResourceEngine resource, String macsIndexPath) {
        LinkedHashMap<String, List<String>> resources = resource.getGlobalResources(macsKey);
        List<String> macsList = resources.get(macsKey.get(0));
        macsList.clear();
         if (macsIndexPath != null &&
             macsIndexPath.length() > 0 &&
             new File(macsIndexPath.startsWith("file://") ? macsIndexPath.substring(7)
                     : macsIndexPath).isDirectory()) {
             macsList.add(macsIndexPath);
         }
         resource.setGlobalResources(resources);
     }
 
     /**
      * key for blacklisted workflows in resource engine
      */
     public static List<String> blackListKey = new ArrayList<String>() {
                                                 {
                                                     add("Workflow Blacklist");
                                                 }
                                             };
 
     private void removeBlacklistWorkflow(ResourceEngine resource, String blacklistWorkflow) {
         LinkedHashMap<String, List<String>> resources = resource.getGlobalResources(blackListKey);
         List<String> blackList = resources.get(blackListKey.get(0));
         if (blackList == null) { return; }
         boolean remove = true;
         while (remove) {
             remove = blackList.remove(blacklistWorkflow);
         }
         resource.setGlobalResources(resources);
     }
 
     private void addBlacklistWorkflow(ResourceEngine resource, String blacklistWorkflow) {
         LinkedHashMap<String, List<String>> resources = resource.getGlobalResources(blackListKey);
         List<String> blackList = resources.get(blackListKey.get(0));
         if (blackList == null) {
             blackList = new ArrayList<String>();
             resources.put(blackListKey.get(0), blackList);
         }
         blackList.add(blacklistWorkflow);
         resource.setGlobalResources(resources);
     }
 
     /**
      * @param resource
      * @param out
      */
     private <I> void listGlobalResources(StorageEngine<I> storage, ResourceEngine resource,
             PrintStream out) {
         List<Workflow> workflows = new ArrayList<Workflow>();
         Workflow workflow = registry.getWorkflow(argument0);
         if (workflow == null) {
             workflows = registry.getWorkflows();
         } else {
             workflows.add(workflow);
         }
 
         for (Workflow current : workflows) {
             List<String> keys = getParameters(current);
 
             LinkedHashMap<String, List<String>> resources = resource.getGlobalResources(keys);
             out.println("Global Resources for <" + current.getIdentifier() + ">:" +
                         resources.toString() + "\n");
         }
 
         LinkedHashMap<String, List<String>> blackWorkflow = resource.getGlobalResources(blackListKey);
         out.println("Blacklisted Workflows are:" + blackWorkflow.toString() + "\n");
 
         LinkedHashMap<String, List<String>> macsIndex = resource.getGlobalResources(macsKey);
         out.println("MACS Index:" + macsIndex.toString() + "\n");
     }
 
     /**
      * @param resource
      * @param out
      * @throws StorageEngineException
      */
     private <I> void listProviderResources(StorageEngine<I> storage, ResourceEngine resource,
             PrintStream out) throws StorageEngineException {
         List<Workflow> workflows = new ArrayList<Workflow>();
         Workflow workflow = registry.getWorkflow(argument0);
         if (workflow == null) {
             workflows = registry.getWorkflows();
         } else {
             workflows.add(workflow);
         }
 
         for (Workflow current : workflows) {
             List<String> keys = getParameters(current);
 
             List<Provider<I>> providers = storage.getAllProviders();
             for (Provider provider : providers) {
                 LinkedHashMap<String, List<String>> resources = resource.getProviderResources(
                         provider, keys);
                 out.println("Provider " + provider.getMnemonic() + " Resources for <" +
                             current.getIdentifier() + ">:" + resources.toString());
 
             }
         }
     }
 
     private List<String> getParameters(Workflow current) {
         List<String> keys = new ArrayList<String>();
         keys.addAll(current.getStart().getParameters());
         for (IngestionPlugin plugin : current.getSteps()) {
             keys.addAll(plugin.getParameters());
         }
         return keys;
     }
 
     /**
      * @param resource
      * @param out
      * @throws StorageEngineException
      */
     private <I> void listCollectionResources(StorageEngine<I> storage, ResourceEngine resource,
             PrintStream out) throws StorageEngineException {
         List<Workflow> workflows = new ArrayList<Workflow>();
         Workflow workflow = registry.getWorkflow(argument0);
         if (workflow == null) {
             workflows = registry.getWorkflows();
         } else {
             workflows.add(workflow);
         }
 
         for (Workflow current : workflows) {
             List<String> keys = getParameters(current);
 
             List<Provider<I>> providers = storage.getAllProviders();
             for (Provider provider : providers) {
                 List<Collection<I>> collections = storage.getCollections(provider);
                 if (collections != null && !collections.isEmpty()) {
                     out.println(provider.getMnemonic());
                     for (Collection collection : collections) {
                         LinkedHashMap<String, List<String>> resources = resource.getCollectionResources(
                                 collection, keys);
                         out.println("Collection " + collection.getMnemonic() + " Resources for <" +
                                     current.getIdentifier() + ">:" + resources.toString() + "\n");
                     }
                 }
             }
         }
     }
 
     private Provider createProvider(StorageEngine<?> storage, PrintStream out)
             throws StorageEngineException {
         if (argument0 == null || argument1 == null) {
             out.println("Failed to create provider. No arguments specified, should be <mnemonic> <name> [<true|false>]");
             return null;
         }
 
         Provider provider = storage.createProvider();
         provider.setMnemonic(argument0);
         provider.setName(argument1);
         if (argument2 != null) {
             provider.setAggregator(Boolean.parseBoolean(argument2));
         }
 
         if (parent != null) {
             Provider pParent = storage.findProvider(parent);
 
             if (pParent != null) {
                 provider.getRelatedIn().add(pParent);
                 pParent.getRelatedOut().add(provider);
 
                 storage.updateProvider(provider);
                 storage.updateProvider(pParent);
             } else {
                 out.println("Failed to create provider. Parent <" + parent + "> not found.");
 
             }
         } else {
             storage.updateProvider(provider);
         }
 
         storage.checkpoint();
         return provider;
     }
 
     private void updateProvider(StorageEngine storage, PrintStream out)
             throws StorageEngineException {
         if (argument0 == null || argument1 == null) {
             out.println("Failed to update provider. No arguments specified, should be <mnemonic> <field> <value>");
             return;
         }
 
         Provider provider = storage.findProvider(argument0);
 
         String method = "set" + StringUtils.capitalize(argument1);
         try {
             Method setter = provider.getClass().getMethod(method, String.class);
             setter.invoke(provider, argument2);
 
             storage.updateProvider(provider);
             storage.checkpoint();
 
             out.println("Successfully executed " + method + "(" + argument2 + ")");
         } catch (Throwable e) {
             out.println("Failed to update provider. Failed to update using method <" + method +
                         "(" + argument2 + ") reason:" + e.getLocalizedMessage());
             e.printStackTrace(out);
         }
     }
 
     private void listProvider(StorageEngine storage, PrintStream out) throws StorageEngineException {
         Set<Provider> mainprovs = new HashSet<Provider>();
         List<Provider> providers = storage.getAllProviders();
         for (Provider provider : providers) {
             if (provider.getRelatedIn() == null || provider.getRelatedIn().isEmpty()) {
                 mainprovs.add(provider);
             }
         }
 
         Set<Provider> processed = new HashSet<Provider>();
         printTree(mainprovs, processed, "+-", out);
     }
 
     private void printTree(Set<Provider> providers, Set<Provider> processed, String indent,
             PrintStream out) {
         for (Provider provider : providers) {
 
             String p = "(" + provider.getId() + ") " + provider.toString();
             out.println(indent + p);
 
             if (!processed.contains(provider)) {
                 processed.add(provider);
                 if (provider.getRelatedOut() != null && !provider.getRelatedOut().isEmpty()) {
                     String in = indent.substring(0, indent.length() - 2);
                     printTree(provider.getRelatedOut(), processed, in + "|  +-", out);
                 }
             }
         }
     }
 
     private Collection createCollection(StorageEngine storage, PrintStream out)
             throws StorageEngineException {
         if (argument0 == null || argument1 == null || parent == null) {
             out.println("Failed to create collection. No arguments specified, should be -p provider <mnemonic> <name>");
             return null;
         }
 
         Provider provider = storage.findProvider(parent);
         if (provider == null) {
             out.println("Failed to create collection. Provider \"" + parent + "\" not found.");
             return null;
         }
 
         Collection collection = storage.createCollection(provider);
         collection.setMnemonic(argument0);
         collection.setName(argument1);
         storage.updateCollection(collection);
         storage.checkpoint();
 
         return collection;
     }
 
     private void updateCollection(StorageEngine storage, PrintStream out)
             throws StorageEngineException {
         if (argument0 == null || argument1 == null) {
             out.println("Failed to update collection. No arguments specified, should be <mnemonic> <field> <value>");
             return;
         }
 
         Collection collection = storage.findCollection(argument0);
 
         String method = "set" + StringUtils.capitalize(argument1);
         try {
             Method setter = collection.getClass().getMethod(method, String.class);
             if ("null".equalsIgnoreCase(argument2)) {
                 argument2 = null;
             }
             setter.invoke(collection, argument2);
 
             storage.updateCollection(collection);
             storage.checkpoint();
 
             out.println("Successfully executed " + method + "(" + argument2 + ")");
         } catch (Throwable e) {
             out.println("Failed to update collection. Failed to update using method <" + method +
                         "(\"" + argument2 + "\") reason:" + e.getMessage());
             e.printStackTrace(out);
         }
     }
 
     private void listCollection(StorageEngine storage, PrintStream out)
             throws StorageEngineException {
         if (parent == null) {
             List<Provider> providers = storage.getAllProviders();
             for (Provider provider : providers) {
                 List<Collection> collections = storage.getCollections(provider);
                 if (collections != null && !collections.isEmpty()) {
                     out.println(provider.getMnemonic());
                     for (Collection collection : collections) {
                         String p = "|  -+ (" + collection.getId() + ") " + collection.toString();
                         out.println(p);
                     }
                 }
             }
         } else {
             Provider provider = storage.findProvider(parent);
             out.println(provider.getMnemonic());
 
             List<Collection> collections = storage.getCollections(provider);
             for (Collection collection : collections) {
                 String p = "|  -+ (" + collection.getId() + ") " + collection.toString();
                 out.println(p);
             }
         }
     }
 
     private void checkpoint(StorageEngine storage, PrintStream out) throws StorageEngineException {
         storage.checkpoint();
     }
 
     // @SuppressWarnings("unused")
     private void setFieldValues(String[] split) {
         String[] arguments = split[1].split("\\|");
         this.argument0 = null;
         this.argument1 = null;
         this.argument2 = null;
         this.parent = null;
 
         this.argument0 = arguments[0];
         if (arguments.length > 1) {
             this.argument1 = arguments[1];
         }
         if (arguments.length > 2) {
             this.argument2 = arguments[2];
         }
         if (arguments.length > 3) {
             this.parent = arguments[3];
         }
     }
 
     /**
      * @return registry
      */
     public Registry getRegistry() {
         return registry;
     }
 }
