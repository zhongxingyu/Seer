 
 package com.openfeint.qa.ggp;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import util.RawFileUtil;
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.openfeint.qa.core.caze.TestCase;
 import com.openfeint.qa.core.caze.builder.CaseBuilder;
 import com.openfeint.qa.core.caze.builder.CaseBuilderFactory;
 import com.openfeint.qa.core.exception.CaseBuildFailedException;
 import com.openfeint.qa.core.exception.TCMIsnotReachableException;
 import com.openfeint.qa.core.net.PlainHttpCommunicator;
 import com.openfeint.qa.core.net.TCMCommunicator;
 import com.openfeint.qa.core.runner.TestRunner;
 import com.openfeint.qa.core.util.JsonUtil;
 
 public class DailyRunActivity extends Activity {
 
     private static final String TAG = "DailyRunActivity";
 
     private TestRunner runner;
 
     private RawFileUtil rfu;
 
     private TestCase[] case_list;
 
     private boolean is_create_run;
 
     private String suite_id;
 
     private String run_id;
 
     private boolean need_reload = false;
 
     private String coffeeServer = "";
 
     // TODO for debug
     private Button start_button;
 
     private void loadCase() {
         Log.d(TAG, "==================== Load Cases From TCMS ====================");
         CaseBuilder builder = CaseBuilderFactory.makeBuilder(CaseBuilderFactory.TCM_BUILDER,
                 rfu.getTextFromRawResource(R.raw.tcm),
                 rfu.getTextFromRawResource(R.raw.step_def, "step_path"), DailyRunActivity.this);
 
         try {
             runner.emptyCases();
             runner.addCases(builder.buildCases(suite_id));
             need_reload = false;
         } catch (CaseBuildFailedException cbfe) {
             Log.e(TAG, "Load test case failed!");
             need_reload = true;
         }
     }
 
     private Runnable run_case_thread = new Runnable() {
         public void run() {
             Log.d(TAG, "==================== Begin to Run ====================");
             case_list = runner.getAllCases();
             // TODO debug message
             Log.d(TAG, "All test case loaded are below:");
             for (TestCase tc : case_list) {
                 Log.i(TAG, "id: " + tc.getId() + ", name: " + tc.getTitle());
             }
             runner.runAllCases();
             Log.i(TAG, "---------- Running done ---------");
             submitResult();
             genTestReport(run_id);
         }
     };
 
     private void genEmptyTestReport() {
         // submit an invalid test run id to create a empty report
         genTestReport("99999");
     }
 
     private void genTestReport(String runId) {
         HttpPost httpPost = new HttpPost("http://" + coffeeServer + "/report");
 
         List<NameValuePair> params = new ArrayList<NameValuePair>();
         String filePath = "";
         params.add(new BasicNameValuePair("key", "adfqet87983hiu783flkad09806g98adgk"));
         params.add(new BasicNameValuePair("runId", runId));
         params.add(new BasicNameValuePair("reportDir", filePath));
 
         try {
             httpPost.setEntity(new UrlEncodedFormEntity(params));
             HttpResponse httpResponse = new DefaultHttpClient().execute(httpPost);
             Log.d(TAG, "HttpResponse of genTestReport: " + httpResponse.getStatusLine().toString());
         } catch (UnsupportedEncodingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (ClientProtocolException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     private void runAndSubmitCase() {
         new Thread(run_case_thread).start();
     }
 
     private void submitResult() {
         Log.i(TAG, "---------- Submitting result to TCM ---------");
         TCMCommunicator tcm = new TCMCommunicator(rfu.getTextFromRawResource(R.raw.tcm), "");
         tcm.setTestCasesResult(run_id, Arrays.asList(case_list));
         Log.i(TAG, "---------- result submitted ----------");
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         runner = TestRunner.getInstance(DailyRunActivity.this);
         rfu = RawFileUtil.getInstance(DailyRunActivity.this);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         // initDebugButton();
 
         int times = 0;
         do {
            if (times++ > 5) {
                 Log.d(TAG, "loading config failed!");
                 genEmptyTestReport();
                 System.exit(0);
             }
             getConfig(); // Get Configuration for this run
         } while (need_reload);
 
         times = 0;
         do {
            if (times++ > 5) {
                 Log.d(TAG, "loading test case failed!");
                 genEmptyTestReport();
                 System.exit(0);
             }
             loadCase(); // Load test case from TCMS
         } while (need_reload);
 
         // run_case_thread.run();
         runAndSubmitCase(); // Run test cases loaded and submit result
     }
 
     private void initDebugButton() {
         start_button = (Button) findViewById(R.id.start_selected);
         start_button.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 Log.d(TAG, "Click to debug running...");
                 loadCase();
                 runAndSubmitCase();
             }
         });
     }
 
     private void getConfig() {
         PlainHttpCommunicator http = new PlainHttpCommunicator(null, null);
         try {
             Log.d(TAG, "==================== Load Configuration ====================");
             BufferedReader br = http.getJsonResponse("http://" + coffeeServer
                     + "/android/config?key=adfqet87983hiu783flkad09806g98adgk");
             if (br != null) {
 
                 String mark = JsonUtil.getAutoConfigJsonValueByKey("is_create_run", br);
                 Log.d(TAG, "is_create_run: " + mark);
                 if ("true".equals(mark))
                     is_create_run = true;
                 else
                     is_create_run = false;
 
                 suite_id = JsonUtil.getAutoConfigJsonValueByKey("suite_id", br);
                 Log.d(TAG, "suite_id: " + suite_id);
 
                 run_id = JsonUtil.getAutoConfigJsonValueByKey("run_id", br);
                 Log.d(TAG, "run_id: " + run_id);
 
                 need_reload = false;
             }
         } catch (TCMIsnotReachableException e) {
             need_reload = true;
             e.printStackTrace();
         } finally {
         }
     }
 }
