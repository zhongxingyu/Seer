 package org.gots.seed.providers.nuxeo;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.gots.preferences.GotsPreferences;
 import org.gots.seed.BaseSeedInterface;
 import org.gots.seed.providers.local.LocalSeedProvider;
 import org.nuxeo.android.repository.DocumentManager;
 import org.nuxeo.ecm.automation.client.jaxrs.Constants;
 import org.nuxeo.ecm.automation.client.jaxrs.Session;
 import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
 import org.nuxeo.ecm.automation.client.jaxrs.model.DocRef;
 import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
 import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;
 import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap;
 import org.nuxeo.ecm.automation.client.jaxrs.spi.auth.TokenRequestInterceptor;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class NuxeoSeedProvider extends LocalSeedProvider {
     protected static final String TAG = "NuxeoSeedProvider";
 
     private static final long TIMEOUT = 10;
 
     String myToken = GotsPreferences.getInstance(mContext).getToken();
 
     String myLogin = GotsPreferences.getInstance(mContext).getNuxeoLogin();
 
     String myDeviceId = GotsPreferences.getInstance(mContext).getDeviceId();
 
     protected String myApp = GotsPreferences.getInstance(mContext).getGardeningManagerAppname();
 
     public NuxeoSeedProvider(Context context) {
         super(context);
     }
 
     @Override
     public List<BaseSeedInterface> getVendorSeeds() {
 
         List<BaseSeedInterface> vendorSeeds = super.getVendorSeeds();
 
         AsyncTask<Object, Integer, List<BaseSeedInterface>> task = new AsyncTask<Object, Integer, List<BaseSeedInterface>>() {
 
             private HttpAutomationClient client;
 
             @Override
             protected List<BaseSeedInterface> doInBackground(Object... params) {
                 List<BaseSeedInterface> nuxeoSeeds = new ArrayList<BaseSeedInterface>();
 
                client = new HttpAutomationClient(GotsPreferences.getGardeningManagerServerURI()+"site/automation");
                 if (GotsPreferences.getInstance(mContext).isConnectedToServer())
                     client.setRequestInterceptor(new TokenRequestInterceptor(myApp, myToken, myLogin, myDeviceId));
 
                 try {
 
                     Session session = client.getSession();
 
                     Documents docs = (Documents) session.newRequest("Document.Query") //
                     .setHeader(Constants.HEADER_NX_SCHEMAS, "*") //
                     .set("query",
                             "SELECT * FROM VendorSeed WHERE ecm:currentLifeCycleState <> 'deleted' ORDER BY dc:modified DESC") //
                     .execute();
                     for (Iterator<Document> iterator = docs.iterator(); iterator.hasNext();) {
                         Document document = iterator.next();
                         BaseSeedInterface seed = NuxeoSeedConverter.convert(document);
                         nuxeoSeeds.add(seed);
                         Log.i(TAG, "Nuxeo Seed Specie " + seed.getSpecie());
                     }
                 } catch (Exception e) {
                     Log.e(TAG, "getAllSeeds " + e.getMessage(), e);
                 }
 
                 return nuxeoSeeds;
             }
         }.execute(new Object());
 
         List<BaseSeedInterface> remoteSeeds = new ArrayList<BaseSeedInterface>();
 
         try {
             remoteSeeds = task.get(TIMEOUT, TimeUnit.SECONDS);
         } catch (InterruptedException e) {
             Log.e(TAG, e.getMessage(), e);
         } catch (ExecutionException e) {
             Log.e(TAG, e.getMessage(), e);
         } catch (TimeoutException e) {
             Log.e(TAG, GotsPreferences.getGardeningManagerServerURI() + "\n" + e.getMessage(), e);
         }
 
         // TODO send as intent
         List<BaseSeedInterface> myLocalSeeds = super.getVendorSeeds();
         for (BaseSeedInterface remoteSeed : remoteSeeds) {
             boolean found = false;
             for (BaseSeedInterface localSeed : myLocalSeeds) {
                 if (remoteSeed.getUUID() != null && remoteSeed.getUUID().equals(localSeed.getUUID())) {
                     // local and remote
                     // 1: overwrite remote
                     // updateRemoteGarden(localSeed);
                     // 2: TODO sync with remote instead
                     // syncGardens(localGarden,remoteGarden);
                     found = true;
                     break;
                 }
             }
             if (!found) {
                 // remote only
                 vendorSeeds.add(super.createSeed(remoteSeed));
             }
         }
 
         for (BaseSeedInterface localSeed : myLocalSeeds) {
             if (localSeed.getUUID() == null) {
                 createRemoteSeed(localSeed);
             }
         }
 
         return vendorSeeds;
     }
 
     @Override
     public void getAllFamilies() {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void getFamilyById(int id) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public BaseSeedInterface getSeedById() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public BaseSeedInterface createSeed(BaseSeedInterface seed) {
         super.createSeed(seed);
         return createRemoteSeed(seed);
     }
 
     /*
      * Return new remote seed or null if error
      */
     protected BaseSeedInterface createRemoteSeed(BaseSeedInterface seed) {
         try {
             AsyncTask<BaseSeedInterface, Integer, Document> task = new AsyncTask<BaseSeedInterface, Integer, Document>() {
 
                 private Document documentVendorSeed;
 
                 @Override
                 protected Document doInBackground(BaseSeedInterface... params) {
                     BaseSeedInterface currentSeed = params[0];
                     Log.d(TAG, "doInBackground createSeed " + currentSeed);
 
                     HttpAutomationClient client = new HttpAutomationClient(
                            GotsPreferences.getGardeningManagerServerURI()+"site/automation");
 
                     client.setRequestInterceptor(new TokenRequestInterceptor(myApp, myToken, myLogin, myDeviceId));
 
                     PropertyMap props = new PropertyMap();
                     props.set("dc:title", currentSeed.getVariety());
                     props.set("dc:description", "test");
                     props.set("vendorseed:datesowingmin", String.valueOf(currentSeed.getDateSowingMin()));
                     props.set("vendorseed:datesowingmax", String.valueOf(currentSeed.getDateSowingMax()));
                     props.set("vendorseed:durationmin", String.valueOf(currentSeed.getDurationMin()));
                     props.set("vendorseed:durationmax", String.valueOf(currentSeed.getDurationMax()));
                     props.set("vendorseed:family", currentSeed.getFamily());
                     props.set("vendorseed:specie", currentSeed.getSpecie());
                     props.set("vendorseed:variety", currentSeed.getVariety());
                     props.set("vendorseed:barcode", currentSeed.getBareCode());
 
                     // DocumentManager documentManager =
                     // session.getAdapter(DocumentManager.class);
                     // Document catalog =
                     // documentManager.getDocument(wsRef);
                     Session session;
                     session = client.getSession();
                     DocRef wsRef = new DocRef("/default-domain/UserWorkspaces/"
                             + GotsPreferences.getInstance(mContext).getNuxeoLogin());
                     Document catalog = null;
                     try {
                         catalog = (Document) session.newRequest(DocumentManager.FetchDocument).set("value",
                                 wsRef + "/Catalog").execute();
 
                     } catch (Exception e) {
                         Log.e(TAG, "Fetching folder Catalog " + e.getMessage());
 
                         Document folder;
                         try {
                             Document root;
                             root = (Document) session.newRequest("Document.Fetch").set("value", wsRef).execute();
                             folder = (Document) session.newRequest("Document.Create").setInput(root).setHeader(
                                     Constants.HEADER_NX_SCHEMAS, "*").set("type", "Hut").set("name", "Catalog").set(
                                     "properties", "dc:title=" + "Catalog").execute();
                             catalog = folder;
 
                             Log.d(TAG, "doInBackground create folder catalog UUID " + folder.getId());
                         } catch (Exception e1) {
                             Log.e(TAG, "Creating folder Catalog" + e.getMessage());
                         }
 
                     }
 
                     if (catalog == null)
                         return null;
 
                     try {
                         // get the root
 
                         documentVendorSeed = (Document) session.newRequest("Document.Create").setInput(catalog).setHeader(
                                 Constants.HEADER_NX_SCHEMAS, "*").set("type", "VendorSeed").set("name",
                                 currentSeed.getVariety()).set("properties", props).execute();
 
                         Log.d(TAG, "doInBackground remoteSeed UUID " + documentVendorSeed.getId());
                     } catch (Exception e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     }
 
                     return documentVendorSeed;
 
                 }
 
             }.execute(seed);
             // TODO wait for task.getStatus() == Status.FINISHED; in a thread
             //
             // TODO send as intent
             // TODO get(timeout)
             Document remoteVendorSeed = task.get();
             if (remoteVendorSeed == null)
                 return null;
             seed.setUUID(remoteVendorSeed.getId());
 
             super.updateSeed(seed);
 
         } catch (InterruptedException e) {
             Log.e(TAG, e.getMessage());
         } catch (ExecutionException e) {
             Log.e(TAG, e.getMessage(), e);
         }
         return seed;
     }
 }
