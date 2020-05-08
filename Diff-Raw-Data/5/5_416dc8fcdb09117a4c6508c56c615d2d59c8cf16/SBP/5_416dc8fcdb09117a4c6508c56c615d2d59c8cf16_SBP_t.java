 package meshmadness.domain;
 
 import meshmadness.actors.SalesPerson;
 import meshmadness.actors.User;
 import meshmadness.messaging.LocalPayload;
 import meshmadness.messaging.MeshPayload;
 import meshmadness.messaging.Payload;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import rx.Observable;
 import rx.Subscription;
 import rx.concurrency.Schedulers;
 import rx.subjects.*;
 import rx.util.functions.Action1;
 
 import java.util.*;
 
 public class SBP {
     final Logger logger = LoggerFactory.getLogger(SBP.class);
 
     final private String name;
     final private List<SBP> mesh = new ArrayList<>();
     final private List<User> users = new ArrayList<>();
     final private List<SalesPerson> dealers = new ArrayList<>();
 
     final private ReplaySubject<Payload> rfqQueue = ReplaySubject.create();
 
     final private Map<Integer, RFQStateManager> workingRFQs = new HashMap<>();
     private final Subscription rfqQueueSubscription;
 
     public class RFQSubjectHolder {
         public final int id;
         public final RFQStateManager.RFQState state;
         public final String fillerName;
 
         public RFQSubjectHolder(final int rfqId, final RFQStateManager.RFQState state, final String quoterName) {
             this.id = rfqId;
             this.state = state;
             this.fillerName = quoterName;
         }
 
         @Override
         public String toString() {
             return String.format("RFQ%d %s %s", id, state, fillerName);
         }
     }
 
     private ReplaySubject<RFQSubjectHolder> rfqStateManagerChanges = ReplaySubject.create();
     public Observable<RFQSubjectHolder> subscribe() {
         return rfqStateManagerChanges;
     }
 
     public SBP(final String name) {
         this.name = name;
 
         rfqQueueSubscription = rfqQueue.observeOn(Schedulers.newThread()).subscribe(new Action1<Payload>() {
             @Override
             public void call(Payload rfq) {
                 // Find RFQ manager
                 final RFQStateManager rfqStateManager = workingRFQs.get(rfq.getRFQId());
                 if (rfqStateManager != null) {
                     rfqStateManager.NextState(rfq);
                     final RFQSubjectHolder data = new RFQSubjectHolder(rfq.getRFQId(), rfqStateManager.getCurrentState(), rfqStateManager.getQuoterName());
                     logger.debug(String.format("%d (%s) storing RFQManagerStateChange %s", System.nanoTime(), name, data.toString()));
                     rfqStateManagerChanges.onNext(data);
                 }
 
             }
         });
     }
 
     public void logon(final User user) {
        assert !users.contains(user);
         users.add(user);
     }
 
     public void join(final SBP sbp) {
         mesh.add(sbp);
     }
 
     public void logon(final SalesPerson salesPerson) {
        assert !dealers.contains(salesPerson);
         this.dealers.add(salesPerson);
     }
 
     public void submitRFQ(final RFQ rfq) {
         if (!workingRFQs.containsKey(rfq.getRFQId())) {
 
             final RFQStateManager rfqStateManager = new RFQStateManager(this, rfq);
             notifyAllMesh(rfq, rfqStateManager.getCurrentState(), rfqStateManager.getCurrentStateTime());
             workingRFQs.put(rfq.getRFQId(), rfqStateManager);
         }
 
         rfqQueue.onNext(rfq);
     }
 
     public void notifyLocalSales(final RFQ rfq, final RFQStateManager.RFQState state) {
         logger.debug(String.format("%d (%s) Notify all sales people RFQ%s", System.nanoTime(), name, rfq.getRFQId()));
         for (final SalesPerson salesPerson : getSalesPersons()) {
             salesPerson.SalesPersonCommunication(rfq, state);
         }
     }
 
     synchronized public void MeshCommunications(final MeshPayload meshPayload) {
         logger.debug(String.format("%d (%s) Received from Mesh RFQ%s (%s) from %s (%s)", System.nanoTime(), name, meshPayload.getRFQId(), meshPayload.getState(), meshPayload.getSource().getName(), meshPayload.getTime()));
 
         // If we have never seen the RFQ before, add it to the list to work on
         if (!workingRFQs.containsKey(meshPayload.getRFQ().getRFQId())) {
             final RFQStateManager rfqStateManager = new RFQStateManager(this, meshPayload.getRFQ());
             workingRFQs.put(meshPayload.getRFQ().getRFQId(), rfqStateManager);
             logger.debug(String.format("%d (%s) Creating NEW RFQStateManager (%s,StartRFQ,RFQ%s)", System.nanoTime(), name, meshPayload.getRFQ().getUsername(), meshPayload.getRFQ().getRFQId()));
 
             notifyAllMesh(meshPayload.getRFQ(), rfqStateManager.getCurrentState(), rfqStateManager.getCurrentStateTime());
         }
 
         // Process Mesh message
         rfqQueue.onNext(meshPayload);
     }
 
     public List<SalesPerson> getSalesPersons() {
         return dealers;
     }
 
     synchronized public void send(final LocalPayload salesPayload) {
         rfqQueue.onNext(salesPayload);
     }
 
     public String getName() {
         return name;
     }
 
     public void notifyAllMesh(final RFQ rfq, final RFQStateManager.RFQState state, final long time) {
         for (final SBP region : mesh) {
             try {
                 if (region != rfq.getOriginatingSBP()) {
                     region.MeshCommunications(new MeshPayload((RFQ) rfq.clone(), state, this, time));
                 }
             } catch (CloneNotSupportedException e) {
                 e.printStackTrace();
             }
         }
     }
 
     public void notifyOneRegion(final SBP source, final RFQ rfq, final RFQStateManager.RFQState state, long time) {
         source.MeshCommunications(new MeshPayload(rfq, state, this, time));
     }
 }
