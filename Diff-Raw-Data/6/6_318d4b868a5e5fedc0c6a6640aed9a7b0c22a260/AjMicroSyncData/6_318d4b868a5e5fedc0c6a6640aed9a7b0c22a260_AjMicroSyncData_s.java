 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package aj.apps.microsync.internal;
 
 import one.core.domain.OneClient;
 import one.core.dsl.CoreDsl;
 import one.core.dsl.callbacks.WhenCommitted;
 import one.core.dsl.callbacks.WhenLoaded;
 import one.core.dsl.callbacks.WhenResponseFromServerReceived;
 import one.core.dsl.callbacks.results.*;
 import one.core.nodes.OneNode;
 import one.core.nodes.OneTypedReference;
 import one.core.nodes.OneValue;
 
 /**
  *
  * @author Max
  */
 public class AjMicroSyncData implements DataService {
 
     private final OneClient client;
     private final WithUserRegisteredResult loginDetails;
 
     public AjMicroSyncData(OneClient client, WithUserRegisteredResult loginDetails) {
         this.client = client;
         this.loginDetails = loginDetails;
     }
 
     public void uploadChanges(final String newValue, String nodeUri, final WhenChangesUploaded callback) {
 
         final CoreDsl dsl = client.one();
 
         dsl.load(nodeUri).in(client).and(new WhenLoaded() {
 
             @Override
             public void thenDo(WithLoadResult<Object> wlr) {
 
                 Object resolvedNode = dsl.dereference(wlr.loadedNode()).in(client);
 
                 if (!(resolvedNode instanceof OneValue<?>)) {
                     callback.onFailure(new Exception("Node was not of type OneValue: " + wlr.loadedNode()));
                     return;
                 }
 
                 OneValue<?> valueNode = (OneValue<?>) resolvedNode;
 
                 if (!(valueNode.getValue() instanceof String)) {
                     callback.onFailure(new Exception("Value nodes value was not of type String: " + valueNode));
                     return;
                 }
 
                 String oldValue = (String) valueNode.getValue();
 
                 if (oldValue.equals(newValue)) {
                     callback.thenDo();
                     return;
                 }
 
                 OneValue<String> newValueNode = dsl.newNode(newValue).at(valueNode.getId());
 
                 dsl.replaceSafe(valueNode).with(newValueNode).in(client).and(new WhenResponseFromServerReceived<OneValue<String>>() {
 
                     @Override
                     public void thenDo(WithOperationResult<OneValue<String>> wor) {
                         callback.thenDo();
                     }
 
                     @Override
                     public void onFailure(Throwable t) {
                         callback.onFailure(t);
                     }
                 });
 
 
             }
 
             @Override
             public void onFailure(Throwable t) {
                 callback.onFailure(t);
             }
         });
 
 
     }
 
     @Override
     public void createNewNode(final String value, final String title, final String extension, final boolean isPublic,  final WhenNewNodeCreated callback) {
 
 
         assertAjFileSyncDataNode(new WhenSyncDataNodeAsserted() {
 
             public void thenDo(OneNode syncDataNode) {
                 final CoreDsl dsl = client.one();

                 dsl.appendSafe(value).to(syncDataNode).atClosestAddress("./" + title).in(client).and(new WhenResponseFromServerReceived<OneValue<String>>() {
 
                     @Override
                     public void thenDo(final WithOperationResult<OneValue<String>> wor) {
 
                         dsl.append(dsl.reference("https://u1.linnk.it/6wbnoq/Types/aTextValue")).to(wor.node()).in(client);
 
                         dsl.append(dsl.reference("https://u1.linnk.it/6wbnoq/Types/aTemplate")).to(wor.node()).in(client);
 
                         if (extension.equals("md")) {
                             dsl.append(dsl.reference("https://u1.linnk.it/6wbnoq/Types/isMarkdown")).to(wor.node()).in(client);
                         }
 
                         if (extension.equals("html")) {
                             dsl.append(dsl.reference("https://admin1.linnk.it/types/v01/isHtmlValue")).to(wor.node()).in(client);
                         }
 
                         if (isPublic) {
                             dsl.append(dsl.newNode().asPublicReadToken()).to(wor.node()).in(client);
                         }
                         
                         dsl.commit(client).and(new WhenCommitted() {
 
                             @Override
                             public void thenDo(WithCommittedResult wcr) {
                                callback.thenDo(wor.node());
                             }
                         });
                         
                         
                     }
 
                     @Override
                     public void onFailure(Throwable t) {
                         callback.onFailure(t);
                     }
                 });
 
 
             }
 
             public void onFailure(Throwable t) {
                 callback.onFailure(t);
             }
         });
 
     }
 
     public void downloadChanges(final String localValue, String nodeUri, final WhenChangesDownloaded callback) {
        final CoreDsl dsl = client.one();
 
         dsl.load(nodeUri).in(client).and(new WhenLoaded() {
 
             @Override
             public void thenDo(WithLoadResult<Object> wlr) {
                 Object resolvedNode = dsl.dereference(wlr.loadedNode()).in(client);
 
                 if (!(resolvedNode instanceof OneValue<?>)) {
                     callback.onFailure(new Exception("Node was not of type OneValue: " + wlr.loadedNode()));
                     return;
                 }
 
                 OneValue<?> valueNode = (OneValue<?>) resolvedNode;
 
                 if (!(valueNode.getValue() instanceof String)) {
                     callback.onFailure(new Exception("Value nodes value was not of type String: " + valueNode));
                     return;
                 }
 
                 String remoteValue = (String) valueNode.getValue();
 
                 if (remoteValue.equals(localValue)) {
                     callback.onUnchanged();
                     return;
                 }
                 
                 callback.onChanged(remoteValue);
             }
 
             @Override
             public void onFailure(Throwable t) {
                 callback.onFailure(t);
             }
             
             
         });
         
         
     }
 
     private interface WhenSyncDataNodeAsserted {
 
         public void thenDo(OneNode syncDataNode);
 
         public void onFailure(Throwable t);
     }
 
     private void assertAjFileSyncDataNode(final WhenSyncDataNodeAsserted callback) {
 
         CoreDsl dsl = client.one();
 
         dsl.load(loginDetails.userNodeUri()).withSecret(loginDetails.userNodeSecret()).in(client).and(new WhenLoaded() {
 
             @Override
             public void thenDo(WithLoadResult<Object> wlr) {
 
                 assertChild(wlr.loadedNode(), "apps", new WhenLoaded() {
 
                     @Override
                     public void thenDo(WithLoadResult<Object> wlr) {
 
                         assertChild(wlr.loadedNode(), "ajFileSync", new WhenLoaded() {
 
                             @Override
                             public void thenDo(WithLoadResult<Object> wlr) {
 
                                 assertChild(wlr.loadedNode(), "docs", new WhenLoaded() {
 
                                     @Override
                                     public void thenDo(WithLoadResult<Object> wlr) {
                                         callback.thenDo(wlr.loadedNode());
                                     }
 
                                     @Override
                                     public void onFailure(Throwable t) {
                                         callback.onFailure(t);
                                     }
                                 });
 
 
 
 
                             }
 
                             @Override
                             public void onFailure(Throwable t) {
                                 callback.onFailure(t);
                             }
                         });
 
                     }
 
                     @Override
                     public void onFailure(Throwable t) {
                         callback.onFailure(t);
                     }
                 });
 
             }
 
             @Override
             public void onFailure(Throwable t) {
                 callback.onFailure(t);
             }
         });
 
 
 
     }
 
     private void assertChild(final OneNode forNode, final String childPath, final WhenLoaded callback) {
 
         final CoreDsl dsl = client.one();
 
         dsl.load(forNode.getId() + "/" + childPath).in(client).and(new WhenLoaded() {
 
             @Override
             public void thenDo(WithLoadResult<Object> wlr) {
                 callback.thenDo(wlr);
             }
 
             @Override
             public void onUndefined(WithUndefinedContext context) {
 
                 dsl.appendSafe(childPath).to(forNode).atAddress("./" + childPath).in(client).and(new WhenResponseFromServerReceived<OneValue<String>>() {
 
                     @Override
                     public void thenDo(WithOperationResult<OneValue<String>> wor) {
 
                         callback.thenDo(new WithLoadResult<Object>(client, (OneTypedReference<Object>) dsl.reference(wor.node().getId())));
 
                     }
 
                     @Override
                     public void onImpossible(WithImpossibleContext context) {
                         assertChild(forNode, childPath, callback);
                     }
 
                     @Override
                     public void onFailure(Throwable t) {
                         callback.onFailure(t);
                     }
                 });
 
             }
 
             @Override
             public void onFailure(Throwable t) {
                 callback.onFailure(t);
             }
         });
 
 
     }
 }
