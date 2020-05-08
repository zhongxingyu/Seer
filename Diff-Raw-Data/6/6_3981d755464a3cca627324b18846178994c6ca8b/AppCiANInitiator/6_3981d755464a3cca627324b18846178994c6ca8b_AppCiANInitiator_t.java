 package ext.jist.swans.app;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import jist.runtime.JistAPI;
 import jist.swans.Constants;
 import jist.swans.mac.MacAddress;
 import jist.swans.misc.Message;
 import jist.swans.net.NetAddress;
 import ducks.driver.SimParams;
 import ext.util.stats.DucksCompositionStats;
 
 public class AppCiANInitiator extends AppCiANBase
 {
     private static final int              STATE_SEARCHING      = 0;
     private static final int              STATE_AWAITING_TIMER = 1;
     private static final int              STATE_OBSERVING      = 2;
 
     private static final int              defaultTtl           = 64;
 
     private int                           state;
     private HashMap<String, CiANWorkflow> pendingWf;
     private List<Message>                 completedMessages;
     private int                           msgId;
     private int                           reqSize;
     private int                           waitTimeStart;
     private int                           waitTimeEnd;
     private int                           duration;
 
     private String                        compoRestrict;
     private int                           responsesReceived;
 
     public AppCiANInitiator(int nodeId, DucksCompositionStats stats, int reqSize, int reqRate, int waitTimeStart,
             int waitTimeEnd, int duration, String compoRestrict) {
         super(nodeId, stats);
         this.state = STATE_SEARCHING;
         this.pendingWf = new HashMap<String, CiANWorkflow>();
         this.completedMessages = new ArrayList<Message>();
         this.msgId = 1;
         this.reqSize = reqSize;
         this.waitTimeStart = waitTimeStart;
         this.waitTimeEnd = waitTimeEnd;
         this.duration = duration;
 
         // If set to true, we ask for different providers for each service
         this.compoRestrict = compoRestrict;
         this.responsesReceived = 0;
     }
 
     @Override
     public void run(String[] args) {
         // startup delay
         if (JistAPI.getTime() == 0) {
             JistAPI.sleep((this.waitTimeStart) * Constants.SECOND);
         }
 
         CiANWorkflow wf = createWorkflow();
         search(wf);
         compositionStats.incrementNumReq();
     }
 
     @Override
     public void receive(Message msg, NetAddress src, MacAddress lastHop, byte macId, NetAddress dst, byte priority,
             byte ttl) {
 
         // filter message duplicates
         if (completedMessages.contains(msg)) {
             return;
         } else {
             completedMessages.add(msg);
         }
 
         // ignore message if not a pending workflow
         if (msg instanceof CiANWorkflowMessage) {
             if (!pendingWf.containsKey(((CiANWorkflowMessage) msg).getId()))
                 return;
         }
 
         // handle workflow message
         if (msg instanceof CiANToken) {
             handleToken((CiANToken) msg, src);
         } else if (msg instanceof CiANDiscoveryResponse) {
             handleDiscoveryResponse((CiANDiscoveryResponse) msg, src);
         } else {
             return;
         }
     }
 
     protected void handleDiscoveryResponse(CiANDiscoveryResponse response, NetAddress src) {
         CiANWorkflow wf = pendingWf.get(response.getId());
         if (null == wf)
             return;
 
         switch (state) {
             case STATE_SEARCHING:
             case STATE_AWAITING_TIMER:
                 // We have a new potential provider
                 ++responsesReceived;
                 CiANProvider p = new CiANProvider(response.getSenderId(), response.getDistance(defaultTtl));
                 char[] services = wf.getServices();
                 for (char service : services) {
                     if (!wf.isPartOfProviderList(service, p.getNodeId())) {
                         wf.updateProviderFor(service, p);
                     }
                 }
 
                 // If all services have a provider we're good to go planning
                 if (wf.areAllServicesProvided()
                         && (!compoRestrict.equals(SimParams.COMPOSITION_RESTRICTION_NO_REPEAT) || responsesReceived >= reqSize)) {
                     state = STATE_AWAITING_TIMER;
                 }
                 break;
             default:
                 break;
         }
     }
 
     protected void handleToken(CiANToken t, NetAddress src) {
         CiANWorkflow wf = pendingWf.get(t.getId());
         switch (state) {
             case STATE_OBSERVING:
                 char service = t.getService();
                 int in = t.getInput();
                 wf.updateInputFor(service, in);
                 if (wf.isLastService(service)) {
                     compositionStats.incrementInvokeSuccess(CiANWorkflow.STRING_DESTINATION);
                     compositionStats.registerForwardToExecEndTime(CiANWorkflow.STRING_DESTINATION, wf.getId(),
                             JistAPI.getTime());
                     compositionStats.setLastServiceExecuted(CiANWorkflow.STRING_DESTINATION);
                     compositionStats.setiKnowsLastServiceExecuted(CiANWorkflow.STRING_DESTINATION);
                     compositionStats.setiKnowsLastServiceBound(CiANWorkflow.STRING_DESTINATION);
                 } else { // should not happen
                     compositionStats.setiKnowsLastServiceExecuted(String.valueOf((char) (service - 1)));
                     compositionStats.setiKnowsLastServiceBound(String.valueOf(service));
                 }
                 break;
             default:
                 break;
         }
     }
 
     protected void timeout(String wkId) {
         CiANWorkflow wf = pendingWf.get(wkId);
         if (wf == null)
             return;
 
         switch (state) {
             case STATE_SEARCHING:
                 if (JistAPI.getTime() <= ((this.duration - this.waitTimeEnd) * Constants.SECOND) - reqSize
                         * (TIMEOUT_SEND_SERVICE_AD + TIMEOUT_COOL_OFF)) {
                     JistAPI.sleep(SLEEP_BEFORE_RETRY);
                     search(wf);
                 }
                 break;
             case STATE_AWAITING_TIMER:
                 handOff(wf);
                 state = STATE_OBSERVING;
                 break;
             default:
                 break;
         }
     }
 
     public static void timeoutProxyMethod(String wfId, AppCiANInitiator provider) {
         provider.timeout(wfId);
     }
 
     private void scheduleTimeoutFor(String wfId, Long at) {
         try {
             Method m = getClass().getMethod("timeoutProxyMethod", new Class[] { String.class, getClass() });
             Object[] parameters = new Object[] { wfId, this };
             JistAPI.callStaticAt(m, parameters, at);
         } catch (SecurityException e) {
             e.printStackTrace();
         } catch (NoSuchMethodException e) {
             e.printStackTrace();
         }
     }
 
     private CiANWorkflow createWorkflow() {
         CiANWorkflow wf = new CiANWorkflow(nodeId, msgId, reqSize);
         pendingWf.put(wf.getId(), wf);
         msgId++;
         compositionStats.setLastService(Character.toString(wf.getLastRealService()));
         compositionStats.registerBindStartTime("A", wf.getId(), JistAPI.getTime());
 
         return wf;
     }
 
     private void search(CiANWorkflow wf) {
         CiANDiscoveryRequest req = new CiANDiscoveryRequest(wf.getId(), wf.getServices(), defaultTtl);
         send(req);
         scheduleTimeoutFor(wf.getId(), JistAPI.getTime() + TIMEOUT_SEND_SERVICE_AD + TIMEOUT_COOL_OFF);
     }
 
     private void handOff(CiANWorkflow wf) {
         // Allocation
         CiANProvider[] providers = new CiANProvider[wf.getServices().length];
        for (int i = 0; i < providers.length - 1; ++i) {
             // Little hack to have different providers for each service...
             // TODO improve this...
             List<CiANProvider> _providers = wf.getProvidersFor(i);
             int size = _providers.size();
             providers[i] = _providers.get(i % size);
         }
        
        // Last service must always get back to the initiator
        providers[providers.length - 1] = new CiANProvider(nodeId, 0);
 
         // Disbursement
         CiANWorkflowRequest wfReq = new CiANWorkflowRequest(wf.getId(), wf.getServices(), wf.getInputs(), providers);
         for (int i = 0; i < providers.length; ++i) {
             CiANProvider provider = providers[i];
             char service = wf.getServiceForIndex(i);
 
             compositionStats.addServiceProvider(wf.getId(), "" + provider.getNodeId());
             compositionStats.incrementSearchSuccess(String.valueOf(service));
             compositionStats.setLastServiceBound(String.valueOf(service));
             compositionStats.setiKnowsLastServiceBound(String.valueOf(service));
             send(wfReq, provider.getAddress());
         }
     }
 }
