 package org.jingbling.ContextEngine;
 
 import android.app.IntentService;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.*;
 import android.util.Log;
 import android.widget.Toast;
 import libsvm.svm_model;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.concurrent.ArrayBlockingQueue;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Jing
  * Date: 12/27/12
  * Time: 11:44 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ContextService extends IntentService {
 
    // Some variables for defining request
     private String contextGroup = new String();
     private String classifierAlg = new String();
     private ArrayList<String> features =new ArrayList<String>();
     private ArrayList<String> contextLabels =new ArrayList<String>();
     private String JSONDataFile="DataStructure.JSON";
     private static JSONArray JSONLabels = new JSONArray();
     private static JSONArray JSONFeatures = new JSONArray();
     private static JSONArray JSONAlgorithms = new JSONArray();
 
 
     private String trainingFileName;
     private ArrayList<String> classifiedModelFile;
     private JSONArray existingJSONClassifiers = new JSONArray();
 
     // Expected input labels
     public static String ACTIONINPUT_KEY = "action";
     public static String LABELSINPUT_KEY = "contextLabels";
     public static String FEATURESINPUT_KEY = "features";
     public static String ALGORITHM_KEY = "algorithm";
 
     // Create a hash map for each group of values that need to be looked up
     private HashMap featuresHashMap = new HashMap();
     private HashMap classifierAlgorithmHashMap = new HashMap();
     private HashMap classModel = new HashMap();
     private HashMap contextLabelsHashMap = new HashMap();
 
     private Bundle outputBundle = new Bundle();
     public boolean activityRunning = false;
     private String action;
     private static ArrayBlockingQueue<String> dataBuffer;
     private boolean runClassify=false;
     private boolean onTick = false;
     private Intent dataCollectIntent;
 
     private Handler timingHandler = new Handler();
     private long elapsedTime = 0;
     MessageReceiver myReceiver = null;
     Boolean receiverRegistered = false;
     private svm_model currentSVMModel;
     private ArrayList<Integer> currentlyExecutingModels = new ArrayList<Integer>();
 
     static final int LAUNCH_DATACOLLECT = 1;
 
     public ContextService(){
         super("ContextService");
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
 
         // Initialize data arrays from assets file if there is no local file found
         try {
             InitJSONtoHashMap(JSONDataFile);
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         //After launching service, first parse JSON from intent
         Bundle inputExtras = intent.getExtras();
 
         // instead of JSON input, just parse strings
         action = inputExtras.getString(ACTIONINPUT_KEY);
         contextLabels = inputExtras.getStringArrayList(LABELSINPUT_KEY);
         features = inputExtras.getStringArrayList(FEATURESINPUT_KEY);
         classifierAlg = inputExtras.getString(ALGORITHM_KEY);
 
         // if action is to initialize with allowable values, do this and return message.
         if (action.toLowerCase().equals("init")) {
             Messenger messenger = (Messenger) inputExtras.get("MESSENGER");
             // return allowable labels and features
             outputBundle.putInt("return",2);
             // query allowable values and send to requesting app
             String[] tempArray;
             outputBundle.putStringArray("allowedFeatures",(String[]) featuresHashMap.keySet().toArray(new String[featuresHashMap.size()]));
             outputBundle.putStringArray("allowedLabels",(String[])contextLabelsHashMap.values().toArray(new String[contextLabelsHashMap.size()]));
             tempArray = (String[])classifierAlgorithmHashMap.keySet().toArray(new String[classifierAlgorithmHashMap.size()]);
             outputBundle.putStringArray("allowedAlgorithms", tempArray.clone());
             outputBundle.putString("message","initialization variables sent");
 
             // send message then return
             sendMessageToClient(messenger, outputBundle);
             return;
         }
 
 
         if (action.toLowerCase().equals("classify")) {
             // todo add check for classifier IDs currently running, and add save of which classifier IDs are running - need separate thread?
             // Check for existing model
             try {
                 classifiedModelFile = findClassifierMatch(features, classifierAlg, contextLabels, existingJSONClassifiers);
             } catch (JSONException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
 
             if (classifiedModelFile == null) {
 
                 // Do not automatically launch activity - instead, return a message telling application to request training activity
                 // define output bundle
                 outputBundle.putInt("return", 1);
                 outputBundle.putString("message","No classifier found, please run training");
 
 
             }
 
             if (classifiedModelFile != null) {
                 // also parse out frequency to return context
                 long period = inputExtras.getLong("period");
                 long duration = inputExtras.getLong("duration");
                 // initialize buffer for holding input data to classify
                 dataBuffer = new ArrayBlockingQueue<String>(1);
 
                 // Create new countdown clock to determine how long and often to get data
                 //todo will eventually need to calculate max frequency for multiple requestors
 
                 // start service for grabbing feature data for classifier input
                 Bundle extras = new Bundle();
                 extras.putInt("labelID", -99); // chose a bogus value as this is not needed for classifying
                 extras.putString("labelName", "blah");  // chose a bogus value as this is not needed for classifying
                 extras.putStringArrayList("features",features);
                 extras.putString("filename","notNeeded");
                 extras.putString("action", "classify");
 
                 // todo Use feature server to save desired features to specified file for training
 
                 dataCollectIntent = new Intent(this, FeatureCollectionService.class);
                 dataCollectIntent.putExtras(extras);
                 startService(dataCollectIntent);
 
                 // set flag to indicate classification should start
                 runClassify = true;
                 elapsedTime = 0;
 
                 // First set up model before running in a loop
                 LearningServer classifierServer = new LearningServer();
                 svm_model currentModel=null;
                 if (classifierAlg.toLowerCase().equals("libsvm")) {
                     // load model from file
 
                     try {
                         // todo for now only train first match
                         currentModel = classifierServer.loadSVMModel(classifiedModelFile.get(0));
                     } catch (IOException e) {
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                 }
                 HashMap labelHash = new HashMap();
                 for (int i=0;i<contextLabels.size();i++) {
                     labelHash.put(i-1,contextLabels.get(i));
                 }
                 while (runClassify) {
                     // while the duration to run the classifier has not completed, grab input data from feature collection service
                     // and classify and return depending on frequency.
                     // Run classifier model to determine label
                     String classifiedLabel = new String();
                     // first load model
 
                     if (classifierAlg.toLowerCase().equals("libsvm")) {
 
                         // Only run classifier once every desired period
 
                         try {
                             classifiedLabel = classifierServer.evaluateSVMModel(dataBuffer.take(), labelHash, currentModel);
                         } catch (IOException e) {
                             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                         } catch (InterruptedException e) {
                             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                         }
                     }
 
                         // define output bundle
                     outputBundle.putInt("return", 0);
                     outputBundle.putString("label",classifiedLabel);
                     if (inputExtras != null) {
                         Messenger messenger = (Messenger) inputExtras.get("MESSENGER");
                         Message msg = Message.obtain();
                         msg.setData(outputBundle);
                         try {
                             messenger.send(msg);
                         } catch (android.os.RemoteException e1) {
                             Log.w(getClass().getName(), "Exception sending message", e1);
                         }
 
                     }
 
                     // if we have not yet reached end time, wait a delay
                     if (elapsedTime<duration){
                         elapsedTime+=period;
                         try {
                             HandlerThread.sleep(period);
                         } catch (InterruptedException e) {
                             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                         }
                     } else {
                         // max duration reached, end classifying
                         runClassify = false;
                     }
                 }
 
 
                 stopService(dataCollectIntent);
 
                 outputBundle.putInt("return", 0);
                 outputBundle.putString("label","classification mode completed");
 
             }
         } else if(action.equals("train")){ //action = train
 
             //no classifier found, launch activity to train model
             saveTrainingData(features, contextGroup, contextLabels);
             // set activity running to pause service until return value given from data collection activity
             activityRunning = true;
             // While waiting for Training session to finish, suspend thread
             while(activityRunning) {
                 try {
                     HandlerThread.sleep(1000,1000);
                 } catch (InterruptedException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
 
             // if training success (model file not null), return message
             outputBundle.putInt("return", 1);
             outputBundle.putString("message","left training activity");
 
 //            //todo automatically train data based on training file created
 //            if (trainingFileName == null) {
 //                // todo training file was not created correctly, return error
 //                outputBundle.putInt("return", 1);
 //                outputBundle.putString("error", "No training file found");
 //                classifiedModelFile = null;
 //            } else {
 //                // train model and then save trained model
 //                // todo create more generic model training function
 //                LearningServer trainModel = new LearningServer();
 //                File sdCard = Environment.getExternalStorageDirectory();
 //                File dir = new File(sdCard.getAbsolutePath() + "/ContextServiceModels/LibSVM");
 //                dir.mkdirs();
 //                classifiedModelFile = dir.toString()+"/ContextServiceModels/"+classifier+"/Classifier"+lookupID+".model";
 //                Log.v("TrainModel", "writing model file: "+ classifiedModelFile);
 //
 //                try {
 //                    trainModel.runSVMTraining(trainingFileName,classifiedModelFile);
 //                } catch (IOException e) {
 //                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 //                }
 //
 //                // classifiedModelFile created, update hashmap
 //                classifierAlgorithmHashMap.put(lookupID,classifiedModelFile);
 //            }
 
         }
         // At end of call, pass classified context back to calling application
         if (inputExtras != null) {
             Messenger messenger = (Messenger) inputExtras.get("MESSENGER");
             Message msg = Message.obtain();
             msg.setData(outputBundle);
             try {
                 messenger.send(msg);
             } catch (android.os.RemoteException e1) {
                 Log.w(getClass().getName(), "Exception sending message", e1);
             }
 
 
         }
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         myReceiver = new MessageReceiver();
         registerReceiver(myReceiver, new IntentFilter("org.jingbling.ContextEngine.ContextService"));
 //        android.os.Debug.waitForDebugger(); //todo TO BE REMOVED
     }
 
     @Override
     public void onDestroy() {
         //todo Before exiting, write hashmap to file
         unregisterReceiver(myReceiver);
         super.onDestroy();
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         return super.onStartCommand(intent, flags, startId);
 //        return Service.BIND_AUTO_CREATE;
     }
 
 
     @Override
     public IBinder onBind(Intent intent) {
         // We don't provide binding, so return null
         return null;
     }
 
     public ArrayList<String> findClassifierMatch (ArrayList<String> features, String classifierAlgorithm, ArrayList<String> labels, JSONArray inputJSONArray) throws JSONException {
         // This function takes in requested inputs and finds a matching classifier to run
         // If a matching classfier(s) is(are) found, they will be returned in a string arraylist.  Otherwise a null ArrayList is returned
 
         // First, parse JSON Array for desired inputs
         String FEATURES_KEY = "features";
         String LABELS_KEY = "labels";
         String ALG_KEY = "classifierAlgorithm";
         String FILE_KEY = "filename";
         String ID_KEY = "id";
         int numMatches = 0;
         ArrayList<String> matchOutput =  new ArrayList<String>();
 
         // Cycle through each array entry and look for exact match of all inputs
         for (int i = 0; i<inputJSONArray.length(); i++) {
             if (classifierAlgorithm.toLowerCase().equals(inputJSONArray.getJSONObject(i).getString(ALG_KEY).toLowerCase())) {
                 // now check that the sorted labels match between input and classifier object
                 Collections.sort(labels);
                 // Parse JSONArray into an ArrayList
                 JSONArray jsonArrayLabel = inputJSONArray.getJSONObject(i).getJSONArray(LABELS_KEY);
                 ArrayList<String> labelArrayList = new ArrayList<String>();
                 // for the two arrays to be equal,
                 for (int j=0; j<jsonArrayLabel.length(); j++) {
                     labelArrayList.add(j, jsonArrayLabel.get(j).toString().toLowerCase());
                 }
                 Collections.sort(labelArrayList);
 
                 if (labels.equals(labelArrayList)) {
                     // Continue in same fashion to check the features array
                     // now check that the sorted labels match between input and classifier object
                     Collections.sort(features);
                     // Parse JSONArray into an ArrayList
                     JSONArray jsonArrayFeat = inputJSONArray.getJSONObject(i).getJSONArray(FEATURES_KEY);
                     ArrayList<String> featArrayList = new ArrayList<String>();
                     for (int count=0; count<jsonArrayFeat.length(); count++) {
                         featArrayList.add(count, jsonArrayFeat.get(count).toString().toLowerCase());
                     }
                     Collections.sort(featArrayList);
                     if (features.equals(featArrayList)) {
                         // all aspects match, add filename to output arraylist and increment counter
                         matchOutput.add(numMatches,inputJSONArray.getJSONObject(i).getString(FILE_KEY));
                         numMatches++;
                     }
                 }
             }
         }
        // check for any matches, otherwise need to return null
        if (numMatches > 0) {
            return matchOutput;
        } else {
            return null;
        }
     }
     public void InitJSONtoHashMap (String inputJSONFile) throws IOException {
 
         JSONObject inputJSON = null;
         try {
             inputJSON = parseJSONFromFile(inputJSONFile);
         } catch (JSONException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         // Parse JSON to establish data mappings for lookups
         try {
 
             JSONLabels = inputJSON.getJSONArray("labels");
             for (int i=0;i<JSONLabels.length();i++) {
                 contextLabelsHashMap.put(JSONLabels.getJSONObject(i).getInt("id"),JSONLabels.getJSONObject(i).getString("name"));
             }
 
             JSONAlgorithms = inputJSON.getJSONArray("classifierAlgorithm");
             for (int i=0;i<JSONAlgorithms.length();i++) {
                 // Reverse the order for classifier Models, as lookup will be usually be performed with id
                 classifierAlgorithmHashMap.put(JSONAlgorithms.getJSONObject(i).getString("name"), JSONAlgorithms.getJSONObject(i).getInt("id"));
             }
 
             JSONFeatures = inputJSON.getJSONArray("features");
             for (int i=0;i<JSONFeatures.length();i++) {
                 featuresHashMap.put(JSONFeatures.getJSONObject(i).getString("name"),JSONFeatures.getJSONObject(i).getInt("id"));
             }
 
             existingJSONClassifiers = inputJSON.getJSONArray("classifier");
 //
 //            tempObject = inputJSON.getJSONArray("classifier");
 //            for (int i=0;i<tempObject.length();i++) {
 //                // Reverse the order for classifier Models, as lookup will be usually be performed with id
 //                classModel.put(tempObject.getJSONObject(i).getInt("id"),tempObject.getJSONObject(i));
 //            }
 
         } catch (JSONException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     public JSONObject parseJSONFromFile (String inputJSONFile) throws JSONException, IOException {
 
         String jString = null;
         InputStream istream = null;
         // Parse expected elements from JSON file into a JSON Object
         try {
 //          // first try opening private file - if doesn't exist, open default assets file
             File checkfile = new File(getBaseContext().getFilesDir()+"/"+JSONDataFile);
             boolean exists = checkfile.exists();
             try {
                 istream = new BufferedInputStream(new FileInputStream(checkfile));
                 Log.d("JSON_Writer", "internal file found and being used");
 
             } catch (IOException e) {
                 Log.d("JSON_Writer", "No internal file found, opening read-only JSONDataFile");
                 istream = getResources().getAssets().open(inputJSONFile);
             }
 
             Writer writer = new StringWriter();
             char[] buffer = new char[2048];
 
             Reader reader = new BufferedReader(new InputStreamReader(istream,
                     "UTF-8"));
             int n;
             while ((n = reader.read(buffer)) != -1) {
                 writer.write(buffer, 0, n);
             }
 
             jString = writer.toString();
 
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             if (istream != null)
                 istream.close();
         }
 
         if (jString != null) {
 
             JSONObject outputJSONObj = new JSONObject(jString);
             Log.d("parseJSONFromFile","SUCCESS parsing JSON from file");
             return outputJSONObj;
         } else {
             Log.w("parseJSONFromFile","ERROR parsing JSONObject from file");
             return null;
         }
     }
 
     public void writeJSONtoFile () {
         //todo Utility function to save internal data in form of JSON Object into a file
         // Save current state of features to internally kept JSON file
         JSONObject newJSONData = new JSONObject();
         try {
             newJSONData.put("features", JSONFeatures);
             newJSONData.put("labels", JSONLabels);
             newJSONData.put("classifierAlgorithm", JSONAlgorithms);
             newJSONData.put("classifier", existingJSONClassifiers);
         } catch (JSONException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
 
         try {
             // Write new file locally and update JSONDataFile to match
             FileOutputStream newFile = openFileOutput(JSONDataFile ,MODE_PRIVATE);
             newFile.write(newJSONData.toString().getBytes());
             newFile.close();
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
     }
 //
     // commenting classifyContext out for now as it may be easier to run directly in loop above
 //    public String classifyContext(String inputString, String classifierModelFile, String classifierToUse, ArrayList<String> contextLabels) {
 //        String returnValue = "toBeImplemented using features and group:" +contextGroup;
 ////        String inputVectorFilename = "/ContextServiceFiles/input/testinput.txt";
 ////        File dir = Environment.getExternalStorageDirectory();
 ////        File inputVectorFile = new File(dir, inputVectorFilename);
 //
 //        // First use features to Use to save file of input vector
 //        // todo Hardcode for now by creating file from input stringbuffer
 //
 //        // Check on classifier to use, depending on type, run training locally or perform on remote machine
 //        if (classifierToUse.toLowerCase().equals("libsvm")) {
 //            //if libSVM chosen, run on machine
 //            //todo Also need input hashtable for decoding labels
 //            // Need to revisit if this is the best way - for now create from input label array list
 //            HashMap labelHash = new HashMap();
 //            for (int i=0;i<contextLabels.size();i++) {
 //                labelHash.put(i-1,contextLabels.get(i));
 //            }
 //            LearningServer classifierServer = new LearningServer();
 //            try {
 //                returnValue = classifierServer.evaluateSVMModel(inputString, classifierModelFile, labelHash, currentSVMModel);
 //
 //            } catch (IOException e) {
 //                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 //            }
 //        }
 //
 //        return returnValue;
 //    }
 
     public void saveTrainingData(ArrayList featuresToUse, String contextGroup, ArrayList contextLabels) {
         //launch activity that will guide user through recording labeled data
         Intent dialogIntent = new Intent(getBaseContext(), DataCollectionAct.class);
 //        dialogIntent.setAction(Intent.ACTION_VIEW);
 //        dialogIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
         dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         dialogIntent.putExtra("features", featuresToUse);
         dialogIntent.putExtra("context", contextGroup);
         dialogIntent.putExtra("contextLabels", contextLabels);
 
         // Create a new Messenger for the communication back
         Messenger messenger = new Messenger(serviceMsgHandler);
         dialogIntent.putExtra("SERVICE_MESSENGER", messenger);
 
 //        startActivity(dialogIntent);
         getApplication().startActivity(dialogIntent);
     }
 
     private void sendMessageToClient (Messenger messengerOut, Bundle outBundle) {
         // function to send message out
         Message msg = Message.obtain();
         msg.setData(outBundle);
         try {
             messengerOut.send(msg);
         } catch (android.os.RemoteException e1) {
             Log.w(getClass().getName(), "Exception sending message", e1);
         }
 
     }
 
     private Handler serviceMsgHandler = new Handler() {
         public void handleMessage(Message message) {
             Bundle output = message.getData();
             if (output != null) {
                 // Also allow for getting final model file from activity
                 String fileReceived = output.getString("fileType");
                 if (fileReceived.toLowerCase().equals("model")) {
                     String modelFile = output.getString("modelFileName");
                     String modelAlgorithm = output.getString("algorithm");
                     ArrayList<String> modelLabels = output.getStringArrayList("labels");
                     ArrayList<String> modelFeatures = output.getStringArrayList("features");
 
                     // first check if this is a unique entry
                     ArrayList<String> matchFile = null;
                     try {
                         matchFile = findClassifierMatch(modelFeatures,modelAlgorithm,modelLabels,existingJSONClassifiers);
                     } catch (JSONException e) {
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                     if (matchFile==null){ // no matches found, save model data to data file
     //                    classifiedModelFile.add(classifiedModelFile.size()+1, modelFile);
                         Log.d("MESSAGE_HANDLER_SERVICE", "current number of classifiers: "+existingJSONClassifiers.length());
                         //todo update JSON file to include new modelFile mapping
                         JSONObject newJSONClassifier = new JSONObject();
                         try {
                             newJSONClassifier.put("id",existingJSONClassifiers.length());
                             newJSONClassifier.put("filename",output.getString("modelFileName"));
                             newJSONClassifier.put("labels",new JSONArray(modelLabels));
                             newJSONClassifier.put("classifierAlgorithm",modelAlgorithm);
                             newJSONClassifier.put("features", new JSONArray(modelFeatures));
                             existingJSONClassifiers.put(newJSONClassifier);
                             Log.d("MESSAGE_HANDLER_SERVICE","JSONClassifiers: "+existingJSONClassifiers.toString());
                         } catch (JSONException e) {
                             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                         }
                         //todo write JSON file
                         writeJSONtoFile();
                     }
                     Log.v("FROM_DATACOLLECT","Model file received: " + classifiedModelFile);
                 } else if (fileReceived.toLowerCase().equals("trainingdata")) {
 
                     trainingFileName = output.getString("trainingFile");
                     Toast.makeText(getApplicationContext(),
                             "Training file received: " + trainingFileName, Toast.LENGTH_LONG)
                             .show();
                 } else {
 
                     Toast.makeText(getApplicationContext(),
                             "Unrecognized message received from data collection activity", Toast.LENGTH_LONG)
                             .show();
                 }
             } else {
                 Log.d("FROM_DATACOLLECT","Training failed");
             }
             // after return received from activity, set activity running to false
             activityRunning = false;
         };
     };
 
 
     private class MessageReceiver extends BroadcastReceiver {
         @Override
         public void onReceive(Context context, Intent intent) {
             Bundle output = intent.getExtras();
             if (output != null) {
                 String input = output.getString("fileType");
                 if (input.toLowerCase().equals("datatolabel")){
                     Log.d("CONTEXT_SERVICE","Data input to classify: "+output.getString("inputData"));
                     // put data into a buffer
                     String tempString = output.getString("inputData");
                     // there is new data available, clear existing buffer before adding to ensure classify is done on latest input
                     dataBuffer.clear();
                     dataBuffer.offer(tempString);
 
                 }  else if (input.toLowerCase().equals("error")) {
                    // error received
                 }
             }
         }
     }
 //
 //    public boolean isActivityRunning() {
 //        // function to check if another activity like the training activity is running
 //        boolean ActivityRunState = false;
 //        // Get list of running tasks
 //        ActivityManager activityManager = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
 //        List<ActivityManager.RunningAppProcessInfo> Activities = activityManager.getRunningAppProcesses();
 //        for (int i=0; i < Activities.size(); i++) {
 //            Log.v("isActivityRunning", Activities.get(i).toString());
 //        }
 //        return ActivityRunState;
 //    }
 
 
 //
 //    public static <T, E> T getKeyByValue(HashMap<T, E> map, E value) {
 //        // Used for reverse lookup of key to value
 //        for (Map.Entry<T, E> entry : map.entrySet()) {
 //            if (value.equals(entry.getValue())) {
 //                return entry.getKey();
 //            }
 //        }
 //        return null;
 //    }
 }
