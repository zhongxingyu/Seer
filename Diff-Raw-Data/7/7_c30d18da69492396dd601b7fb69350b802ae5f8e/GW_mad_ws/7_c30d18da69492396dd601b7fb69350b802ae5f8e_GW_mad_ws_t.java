 /* -------------------------------------------------------------------------- */
 /* Copyright 2002-2006 GridWay Team, Distributed Systems Architecture         */
 /* Group, Universidad Complutense de Madrid                                   */
 /*                                                                            */
 /* Licensed under the Apache License, Version 2.0 (the "License"); you may    */
 /* not use this file except in compliance with the License. You may obtain    */
 /* a copy of the License at                                                   */
 /*                                                                            */
 /* http://www.apache.org/licenses/LICENSE-2.0                                 */
 /*                                                                            */
 /* Unless required by applicable law or agreed to in writing, software        */
 /* distributed under the License is distributed on an "AS IS" BASIS,          */
 /* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   */
 /* See the License for the specific language governing permissions and        */
 /* limitations under the License.                                             */
 /* -------------------------------------------------------------------------- */
 
 import java.io.*;
 import java.util.*;
 import java.net.URL;
 
 import org.apache.axis.components.uuid.UUIDGen;
 import org.apache.axis.components.uuid.UUIDGenFactory;
 import org.apache.axis.message.addressing.EndpointReferenceType;
 
 import org.globus.wsrf.client.BaseClient;
 import org.globus.gsi.GlobusCredential;
 import java.security.cert.X509Certificate;
 import org.globus.delegation.DelegationUtil;
 import org.globus.exec.client.GramJobListener;
 import org.globus.exec.client.GramJob;
 import org.globus.exec.utils.client.ManagedJobFactoryClientHelper;
 import org.globus.exec.utils.ManagedJobFactoryConstants;
 import org.globus.exec.utils.rsl.RSLHelper;
 import org.globus.exec.generated.StateEnumeration;
 import org.gridforum.jgss.ExtendedGSSCredential;
 import org.gridforum.jgss.ExtendedGSSManager;
 import org.ietf.jgss.GSSCredential;
 
 class GW_mad_ws extends Thread implements GramJobListener {
 
     private Map job_pool = null; // Job pool
     private Map jid_pool = null; // JID pool
 //    private Long last_goodtill = 0L;
 
     public static void main(String args[]) {
         GW_mad_ws gw_mad_ws = new GW_mad_ws();
 
         //gw_mad_ws.start();
         gw_mad_ws.loop();
         //gw_mad_ws.interrupt();
     }
 
     // Constructor
     GW_mad_ws() {
         // Create the job and JID pool
         // Warning! With JDK1.5 should be "new HashMap<Integer,GramJob>()"
         job_pool = Collections.synchronizedMap(new HashMap());
         jid_pool = Collections.synchronizedMap(new HashMap());
     }
 
     void loop() {
         String str = null;
         String action = null;
         String jid_str = null;
         String contact;
         String rsl_file;
         boolean fin = false;
         Integer jid = null;
         GramJob job = null;
 
         BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 
         while (!fin) {
             // Read a line a parse it
             try
             {
                 str = in.readLine();
             }
             catch (IOException e)
             {
                 String info = e.getMessage().replace('\n', ' ');
 
                 synchronized (System.out)
                 {
                     System.out.println(action + " " + jid_str + " FAILURE " + info);
                 }
             }
 
             String str_split[] = str.split(" ", 4);
 
             if (str_split.length != 4)
             {
                 synchronized (System.out)
                 {
                     System.out.println(action + " " + jid_str + " FAILURE "
                             + "Error reading line");
                 }
                 fin = true;
             }
             else
             {
                 action = str_split[0].toUpperCase();
                 jid_str = str_split[1];
                 contact = str_split[2];
                 rsl_file = str_split[3];
 
 
                 // Perform the action
                 if (action.equals("INIT"))
                     init();
                 else if (action.equals("FINALIZE"))
                 {
                     finalize_mad();
                     fin = true;
                 }
                 else if (action.equals("SUBMIT") || action.equals("RECOVER"))
                 {
                     try
                     {
                         jid = new Integer(jid_str);
                     }
                     catch (NumberFormatException e)
                     {
                         String info = e.getMessage().replace('\n', ' ');
 
                         synchronized (System.out)
                         {
                             System.out.println(action + " " + jid_str + " FAILURE "
                                     + info);
                         }
                     }
 
                     // Create a job
                     try
                     {
                         if (action.equals("SUBMIT"))
                         {
                             
                             job = new GramJob(new java.io.File(rsl_file));
                         }
                         else if (action.equals("RECOVER"))
                         {
                             job = new GramJob();
                         }
 
                         // Add state listener
                         job.addListener(this);
 
                         Service s = new Service(action, jid, job, contact);
                         s.start();
 
                         // Add job and jid to the pools
                         job_pool.put(jid, job);
                         jid_pool.put(job, jid);
                         
                     }
                     catch (Exception e)
                     {
                         String info = e.getMessage().replace('\n', ' ');
 
                         synchronized (System.out)
                         {
                             System.out.println(action + " " + jid_str + " FAILURE "
                                     + info);
                         }
                     }
                 }
                 else if (action.equals("CANCEL") || action.equals("POLL"))
                 {
                     try
                     {
                         jid = new Integer(jid_str);
                     }
                     catch (NumberFormatException e)
                     {
                         String info = e.getMessage().replace('\n', ' ');
 
                         synchronized(System.out)
                         {
                             System.out.println(action + " " + jid_str + " FAILURE "
                                     + info);
                         }
                     }
 
                     job = (GramJob) job_pool.get(jid);
 
                     if (job == null)
                     {
                         synchronized(System.out)
                         {
                             System.out.println(action + " " + jid + " FAILURE Job does not exist jid");
                         }
                     }
                     else
                     {
                         Service s = new Service(action, jid, job, contact);
                         s.start();
                     }
                 }
                 else
                 {
                     synchronized(System.out)
                     {
                         System.out.println(action + " " + jid_str
                                 + " FAILURE Not valid action");
                     }
                 }
             }
         }
     }
 
     void init() {
         // Nothing to do here
         synchronized(System.out)
         {
             System.out.println("INIT - SUCCESS -");
         }
     }
 
     void finalize_mad() {
         // Nothing to do here
         synchronized(System.out)
         {
             System.out.println("FINALIZE - SUCCESS -");
         }
     }
 
     // Method from interface GramJobListener
     public void stateChanged(GramJob job) {
         int status;
         int exitCode;
         String info;
 
 
         // Get job state
         StateEnumeration jobState = job.getState();
         String jobState_str = jobState.getValue().toUpperCase();
         
 
         // Get the JID from the job
         Integer jid = (Integer) jid_pool.get(job);
 
 
         if (jid == null)
         {
             status = 1;
             info = "Job " + job.getHandle() + " does not exist";
         }
         else
         {
             if (jobState == StateEnumeration.Unsubmitted)
                 jobState_str = "PENDING";
 
             if (jobState == StateEnumeration.Failed)
             {
                 // Check if the reason is "User cancelled"
                 if (job.getFault() == null)
                 {
                     status = 0;
                     info = StateEnumeration._Done.toUpperCase();
                 }
                 else
                 {
                     status = 1;
                     info = job.getFault().getDescription(0).toString().replace('\n', ' ');
                 }
             }
             else
             {
                 status = 0;
                 info = jobState_str;
                 // Get job exit code                
                 exitCode = job.getExitCode();
                 
                 if(info.equals("DONE"))
                 	info = info + ":" + exitCode;              
                 
             }
         }
 
         synchronized (System.out)
         {
             if (status == 0)
                 System.out.println("CALLBACK " + jid + " SUCCESS " + info);
             else if (jid == null)
                 System.out.println("CALLBACK - FAILURE " + info);
             else
                 System.out.println("CALLBACK " + jid + " FAILURE " + info);
         }
         
         // If the job is done, remove it from the pool
         if (jobState == StateEnumeration.Done
                 || jobState == StateEnumeration.Failed)
         {
             job_pool.remove(jid);
             jid_pool.remove(job);
 
             try
             {
                 job.destroy();
             }
             catch (Exception e)
             {
                 //status = 1;
                 //info = e.getMessage().replace('\n', ' ');
             }
         }
     }
 
     // Not used nor needed     
     /*
     public void run() {
         Long goodtill;
         GlobusCredential proxy;
         ExtendedGSSManager manager;
         
         while (true)
         {
             try
             {
                 Thread.sleep(30000L);
             }
             catch (Exception e)
             {
             }
        
             try
             {
                 //manager = (ExtendedGSSManager)
                 //        ExtendedGSSManager.getInstance();
 
                 proxy = GlobusCredential.getDefaultCredential();
                 //        = manager.createCredential(GSSCredential.INITIATE_AND_ACCEPT);
 
                 long timeleft = proxy.getTimeLeft();
                 goodtill = new Long(System.currentTimeMillis() + timeleft*1000);
 
             
                 if (last_goodtill == 0)
                 {
                     last_goodtill = goodtill;
                 }
                 else if (goodtill > last_goodtill)
                 {
                     try
                     {
                         Iterator it = job_pool.values().iterator();
                         
                         while (it.hasNext())
                         {
                             GramJob job = (GramJob) it.next();
 
                             X509Certificate certToSign =
                                     DelegationUtil.getCertificateChainRP(null, null)[0];
                                 
                             DelegationUtil.refresh(proxy, certToSign, 12*60*60, false, null, null);
                             
                             synchronized (System.out)
                             {
                                 System.out.println("TIMER - SUCCESS Credentials refreshed until" + goodtill);
                             }
                         }
                     }
                     catch (Exception e)
                     {
                         synchronized (System.out)
                         {
                             System.out.println("TIMER - FAILURE Can't refresh credentials");
                         }
                     }
                 }
             }
             catch (Exception e)
             {
                 String info = e.getMessage().replace('\n', ' ');
                 synchronized (System.out)
                 {
                     System.out.println("TIMER - FAILURE Exception while obtaining user proxy: " + info);
                 }
             }
         }
     }*/
 }
 
 class Service extends Thread {
     String action, contact;
     Integer jid;
     GramJob job;
     int status = 0;
     String info;
 
     Service(String action, Integer jid, GramJob job, String contact) {
         this.action = action;
         this.jid = jid;
         this.job = job;
         this.contact = contact;
     }
 
     public void run() {
         // Perform the action
         if (action.equals("SUBMIT"))
             status = submit(jid, job, contact);
         else if (action.equals("RECOVER"))
             status = recover(jid, job, contact);
         else if (action.equals("CANCEL"))
             status = cancel(jid, job);
         else if (action.equals("POLL"))
             status = poll(jid, job);
         else
         {
             status = 1;
             info = "Not valid action";
         }
 
         synchronized (System.out)
         {
             if (status == 0)
                 System.out.println(action + " " + jid + " SUCCESS " + info);
             else
                 System.out.println(action + " " + jid + " FAILURE " + info);
         }
     }
 
     int submit (Integer jid, GramJob job, String contact) {
         int status = 0;
         int index = 0;
         EndpointReferenceType factoryEndpoint = null;
 
         info = "-";
 
         // Default factory type is Fork
         String factoryType = ManagedJobFactoryConstants.DEFAULT_FACTORY_TYPE;
 
         // Parse resource contact string
         String resource_split[] = contact.split("/");
         String host = resource_split[0];
 
         if (resource_split.length == 2)
             factoryType = resource_split[1];
 
         // Create Endpoint Reference to ManagedJobFactoryService with its
         //associated ManagedJobFactoryResource
         try
         {
            // Due to Bug 4919: Error in job submission (GRAM-WS) 
            //URL factoryURL =
            //        ManagedJobFactoryClientHelper.getServiceURL(host).getURL();
            URL factoryURL = new URL("https://" + host
                    + ":8443/wsrf/services/ManagedJobFactoryService");
             factoryEndpoint =
                     ManagedJobFactoryClientHelper.getFactoryEndpoint(factoryURL,
                     factoryType);
         }
         catch (Exception e)
         {
             info = e.getMessage().replace('\n', ' ');
             status = 1;
         }
 
         Calendar calendar = Calendar.getInstance();
         calendar.set(Calendar.WEEK_OF_YEAR , calendar.get(Calendar.WEEK_OF_YEAR) + 1);
         
         job.setTerminationTime(calendar.getTime()); // One week
 
         // Submit the job
         
         try
         {
             job.submit(factoryEndpoint, false, false, null);
             info = job.getHandle();
         }
         catch (Exception e)
         {
             info = e.getMessage().replace('\n', ' ');
             status = 1;
         }
 
         return status;
     }
     
     int recover (Integer jid, GramJob job, String handle) {
         // Bind to the job and getting its status
         try
         {
             job.setHandle(handle);
             job.bind();
 
             job.refreshStatus();
             StateEnumeration jobState = job.getState();
             info = jobState.getValue().toUpperCase();
 
             if (jobState == StateEnumeration.Unsubmitted)
                 info = "PENDING";
         }
         catch (Exception e)
         {
             info = e.getMessage();
 
             if (info != null)
                 info = info.replace('\n', ' ');
 
             status = 1;
         }
 
         // Get job status
         /*try
         {
             job.refreshStatus();
             StateEnumeration jobState = job.getState();
             info = jobState.getValue().toUpperCase();
 
             if (jobState == StateEnumeration.Unsubmitted)
                 info = "PENDING";
         }
         catch (Exception e)
         {
             info = e.getMessage();
             if (info != null)
                 info = info.replace('\n', ' ');
             status = 1;
         }*/
 
         return status;
     }
 
     int cancel (Integer jid, GramJob job) {
         int status = 0;
 
         info = "-";
 
         // Cancel the job
         try
         {
             job.cancel();
         }
         catch (Exception e)
         {
             info = e.getMessage().replace('\n', ' ');
             status = 1;
         }
 
         return status;
     }
 
     int poll(Integer jid, GramJob job) {
         int status = 0;
         int index = 0;
 
         // Get job status
         try
         {
             job.refreshStatus();
             StateEnumeration jobState = job.getState();
             info = jobState.getValue().toUpperCase();
 
             if (jobState == StateEnumeration.Unsubmitted)
                 info = "PENDING";
         }
         catch (Exception e)
         {
             info = e.getMessage().replace('\n', ' ');
             status = 1;
         }
 
         return status;
     }
 }
