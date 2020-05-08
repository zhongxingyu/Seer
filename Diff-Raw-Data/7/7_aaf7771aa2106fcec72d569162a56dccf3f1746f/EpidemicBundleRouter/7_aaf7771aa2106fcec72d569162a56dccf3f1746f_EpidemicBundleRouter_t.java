 package se.kth.ssvl.tslab.wsn.general.servlib.routing.routers;
 
 import java.util.ArrayList;
 
 import se.kth.ssvl.tslab.wsn.general.bpf.BPF;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.ForwardingInfo;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.Bundle;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.Bundle.priority_values_t;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundleActions;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundleDaemon;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundleList;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundlePayload.location_t;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundleProtocol;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.custody.CustodyTimerSpec;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.event.BundleDeleteRequest;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.event.ContactUpEvent;
 import se.kth.ssvl.tslab.wsn.general.servlib.contacts.links.Link;
 import se.kth.ssvl.tslab.wsn.general.servlib.naming.endpoint.EndpointID;
 import se.kth.ssvl.tslab.wsn.general.servlib.reg.EpidemicRegistration;
 import se.kth.ssvl.tslab.wsn.general.servlib.reg.Registration;
 import se.kth.ssvl.tslab.wsn.general.servlib.storage.BundleStore;
 import se.kth.ssvl.tslab.wsn.general.systemlib.util.IByteBuffer;
 import se.kth.ssvl.tslab.wsn.general.systemlib.util.SerializableByteBuffer;
 
 public class EpidemicBundleRouter extends TableBasedRouter {
 
 	private final static String TAG = "EpidemicBundleRouter";
 
 	private EpidemicRegistration registration = new EpidemicRegistration(this);
 	
 	public EpidemicBundleRouter(BundleActions actions, BundleList pendingBundles, BundleList custodyBundles) {
 		super(actions, pendingBundles, custodyBundles);
 	}
 	
 	@Override
 	public Registration getRegistration() {
 		return registration;
 	}
 
 	protected void handle_contact_up(ContactUpEvent event) {
 		Link link = event.contact().link();
 		sendList(link);
 	}
 
 	public void sendList(Link link) {
 
 		String list[] = BundleStore.getInstance().getHashList();
 		if(list == null){
 			BPF.getInstance().getBPFLogger().warning(TAG, "List is empty, sending empty list");
			list = new String[0];
 		}
 		BPF.getInstance().getBPFLogger().debug(TAG, "size of hashlist: " + list.length);
 		
 		// "check if the link is not open
 		if (!link.isopen()) {
 			BPF.getInstance()
 					.getBPFLogger()
 					.warning(
 							TAG,
 							"Trying to send list but link " + link.name()
 									+ " is not open!");
 			return;
 		}
 
 		// check if the link queue doesn't have space
 		if (!link.queue_has_space()) {
 			BPF.getInstance()
 					.getBPFLogger()
 					.warning(
 							TAG,
 							"Trying to send list but link " + link.name()
 									+ " does not have space in queue!");
 			return;
 		}
 
 		// create bundle containing list
 		Bundle bundle = new Bundle(location_t.MEMORY); //TODO: verify that MEMORY type does not cause problems
 		bundle.set_dest(new EndpointID(link.remote_eid() + "/epidemic"));
 		bundle.set_source(new EndpointID(BundleDaemon.getInstance().local_eid().str() + "/epidemic"));
 		bundle.set_prevhop(BundleDaemon.getInstance().local_eid());
 		bundle.set_custodian(EndpointID.NULL_EID());
 		bundle.set_replyto(new EndpointID(BundleDaemon.getInstance().local_eid().str() + "/epidemic"));
 		bundle.set_singleton_dest(true);
 		bundle.set_expiration(60);
 		bundle.set_priority(priority_values_t.COS_EXPEDITED);
 
 		//format the payload -> [hash1]-[hash2]-[hash3]...
 		String payload = "";
 		for (int i = 0; i < list.length; i++) {
 			payload += list[i];
 			if (i != list.length - 1) {
 				payload += "-";
 			}
 		}
 		
 		bundle.payload().set_data(payload.getBytes());
 
 		
 		ForwardingInfo info = new ForwardingInfo(ForwardingInfo.state_t.NONE,
 				ForwardingInfo.action_t.FORWARD_ACTION, link.name_str(),
 				0xffffffff, link.remote_eid(),
 				CustodyTimerSpec.getDefaultInstance());
 
 		// send bundle
 		actions_.queue_bundle(bundle, link, info.action(), info.custody_spec());
 		
 		//send bundle
 		BPF.getInstance().getBPFLogger().debug(TAG, "Trying to send bundle with payload: " + payload);
 	}
 
 	public void deliver_bundle(Bundle bundle) {
 		IByteBuffer buf = new SerializableByteBuffer(1000);
 
 		if (!bundle.payload().read_data(0, bundle.payload().length(), buf)) {
 			BPF.getInstance().getBPFLogger()
 					.error(TAG, "Couldn't read the epidemic bundle");
 			return;
 		}
 		BPF.getInstance()
 				.getBPFLogger()
 				.debug(TAG,
 						"Epidemic bundle received. List: "
 								+ new String(buf.array()));
 		
 		// Split the list on dashes (hashes and dashes!)
 		String[] neighborList = new String(buf.array()).split("-");
 		
 		String remote_eid = bundle.source().str().split("/epidemic")[0];
 		if (remote_eid == null) {
 			BPF.getInstance().getBPFLogger()
 					.error("TAG", "Bundle recv : remote_eid == null");
 			return;
 		}
 
 		String [] localList = BundleStore.getInstance().getHashList();
 		String [] diff = diff(localList,neighborList);
 		for(int i = 0; i < diff.length; i++){
 			//get bundle given hash
 			Bundle b = BundleStore.getInstance().getBundle(diff[i]);
 			BPF.getInstance().getBPFLogger().debug(TAG, "Trying to send bundle with hash: " + diff[i]);
 			//queue bundle
 			route_bundle(b);
 		}
 		
 		// Delete the bundle since we are handling it
 		BundleDaemon.getInstance().post_at_head(new BundleDeleteRequest(bundle,
 				BundleProtocol.status_report_reason_t.REASON_NO_ADDTL_INFO));
 
 	}
 	
 	/**
 	 * Get the items that are not in the remote list compared to our local
 	 * @param local Our local list
 	 * @param remote The remote list to compare to
 	 * @return The diff list
 	 */
 	private String[] diff(String[] local, String[] remote) {
		if (local == null || remote == null) {
			return new String[0];
		}
		
 		ArrayList<String> res = new ArrayList<String>();
 		for (int i = 0; i < local.length; i++) {
 			for (int j = 0; j < remote.length; j++) {
 				if (local[i] != remote[j]) {
 					res.add(local[i]);
 				}
 			}
 		}
 		
 		return res.toArray(new String[res.size()]);
 	}
 	
 }
