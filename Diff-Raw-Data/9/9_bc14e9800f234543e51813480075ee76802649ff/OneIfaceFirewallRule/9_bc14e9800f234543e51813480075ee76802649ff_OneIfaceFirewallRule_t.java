 package ru.spbstu.telematics.flowgen.openflow;
 
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;
 
 
 public class OneIfaceFirewallRule implements FirewallRule {
 
 	private static final String RULE_DPID = 			"switch";
 	private static final String RULE_FLOW_NAME = 		"name";
 	private static final String RULE_PRIORITY = 		"priority";
 	private static final String RULE_ACTIVITY =			"active";
 	private static final String RULE_IN_PORT = 			"ingress-port";
 	private static final String RULE_DST_MAC = 			"dst-mac";
 	private static final String RULE_ACTIONS = 			"actions";
 	private static final String RULE_OUT_PORTS_PREFIX =	"output=";
 
 	private static final String FLOW_NAME_IN_SUFFIX = "i";
 	private static final String FLOW_NAME_OUT_SUFFIX = "o";
 	public static final char FLOW_NAME_DELIMITER = '-';
 
 
 	private String mFlowName;
 	private String mDpid;
 	private boolean mActive;
 	private int mPriority;
 	private int mFirewallPort;
 	private int mHostPort;
 	private String mHostMac;
 
 
 	/**
 	 * Constructors
 	 */
 
 	public OneIfaceFirewallRule(String dpid, int firewallPort, int hostPort, String hostMac) {
 		this(dpid, true, OpenflowUtils.DEFAULT_RULE_PRIORITY, firewallPort, hostPort, hostMac);
 	}
 
 	public OneIfaceFirewallRule(String dpid, boolean active, int priority,
 								int firewallPort, int hostPort, String hostMac) {
 		setDpid(dpid);
 		setActive(active);
 		setPriority(priority);
 		setFirewallPort(firewallPort);
 		setHostPort(hostPort);
 		setHostMac(hostMac);
 
 		StringBuilder sb = new StringBuilder();
 		sb.append(mDpid);
 		sb.append(FLOW_NAME_DELIMITER);
 		sb.append(mHostMac);
 		mFlowName = sb.toString();
 	}
 
 
 	/**
 	 * Flow names
 	 */
 
 	public String getFlowName() {
 		return mFlowName;
 
 	}
 
 	public String getInFlowName() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(mFlowName);
 		sb.append(FLOW_NAME_DELIMITER);
 		sb.append(FLOW_NAME_IN_SUFFIX);
 		return sb.toString();
 	}
 
 	public String getOutFlowName() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(mFlowName);
 		sb.append(FLOW_NAME_DELIMITER);
 		sb.append(FLOW_NAME_OUT_SUFFIX);
 		return sb.toString();
 	}
 
 
 	/**
 	 * DPID
 	 */
 
 	public String getDpid() {
 		return mDpid;
 	}
 
 	public void setDpid(String dpid) {
 		if (!OpenflowUtils.validateDpid(dpid)) {
 			throw new IllegalArgumentException("Wrong DPID value");
 		}
 		mDpid = dpid;
 	}
 
 
 	/**
 	 * Rule activity
 	 */
 
 	public boolean isActive() {
 		return mActive;
 	}
 
 	public void setActive(boolean active) {
 		mActive = active;
 	}
 
 
 	/**
 	 * Rule priority
 	 */
 
 	public int getPriority() {
 		return mPriority;
 	}
 
 	public void setPriority(int priority) {
 		if (!OpenflowUtils.validatePriority(priority)) {
 			throw new IllegalArgumentException("Wrong priority value");
 		}
 		mPriority = priority;
 	}
 
 
 	/**
 	 * Firewall port
 	 */
 
 	public int getFirewallPort() {
 		return mFirewallPort;
 	}
 
 	public void setFirewallPort(int firewallPort) {
 		if (!OpenflowUtils.validatePortNumber(firewallPort)) {
 			throw new IllegalArgumentException("Wrong firewall port");
 		}
 		if (firewallPort == mHostPort) {
 			throw new IllegalArgumentException("New firewall port equals to current host port");
 		}
 		mFirewallPort = firewallPort;
 	}
 
 	/**
 	 * Host port
 	 */
 
 	public int getHostPort() {
 		return mHostPort;
 	}
 
 	public void setHostPort(int hostPort) {
 		if (!OpenflowUtils.validatePortNumber(hostPort)) {
 			throw new IllegalArgumentException("Wrong host port");
 		}
 		if (hostPort == mFirewallPort) {
 			throw new IllegalArgumentException("New host port equals to current firewall port");
 		}
 		mHostPort = hostPort;
 	}
 
 
 	/**
 	 * Host MAC
 	 */
 
 	public String getHostMac() {
 		return mHostMac;
 
 	}
 
 	public void setHostMac(String hostMac) {
 		if (!OpenflowUtils.validateMac(hostMac)) {
 			throw new IllegalArgumentException("Wrong host MAC");
 		}
 		mHostMac = hostMac;
 	}
 
 
 	/**
 	 * OvS Commands
 	 */
 
 	@Override
 	public JSONObject ovsInFlowAddCommand() {
 		JSONObject command = new JSONObject();
 
 		try {
 			command.put(RULE_DPID,		mDpid);
 			command.put(RULE_FLOW_NAME,	getInFlowName());
			command.put(RULE_PRIORITY,	mPriority);
			command.put(RULE_ACTIVITY,	mActive);
 			command.put(RULE_IN_PORT,	mHostPort);
 			command.put(RULE_ACTIONS,	RULE_OUT_PORTS_PREFIX + mFirewallPort);
 		} catch (JSONException e) {
 			// TODO
 			e.printStackTrace();
 		}
 
 		return command;
 	}
 
 	@Override
 	public JSONObject ovsOutFlowAddCommand() {
 		JSONObject command = new JSONObject();
 
 		try {
 			command.put(RULE_DPID,		mDpid);
 			command.put(RULE_FLOW_NAME,	getOutFlowName());
			command.put(RULE_PRIORITY,	mPriority);
			command.put(RULE_ACTIVITY,	mActive);
 			command.put(RULE_IN_PORT,	mFirewallPort);
 			command.put(RULE_DST_MAC,	mHostMac);
 			command.put(RULE_ACTIONS,	RULE_OUT_PORTS_PREFIX + mHostPort);
 		} catch (JSONException e) {
 			// TODO
 			e.printStackTrace();
 		}
 
 		return command;
 	}
 
 	@Override
 	public JSONObject ovsInFlowRemoveCommand() {
 		JSONObject command = new JSONObject();
 
 		try {
 			command.put(RULE_FLOW_NAME,	getInFlowName());
 		} catch (JSONException e) {
 			// TODO
 			e.printStackTrace();
 		}
 
 		return command;
 	}
 
 	@Override
 	public JSONObject ovsOutFlowRemoveCommand() {
 		JSONObject command = new JSONObject();
 
 		try {
 			command.put(RULE_FLOW_NAME,	getOutFlowName());
 		} catch (JSONException e) {
 			// TODO
 			e.printStackTrace();
 		}
 
 		return command;
 	}
 
 
 	/**
 	 * Other
 	 */
 
 	@Override
 	public int hashCode() {
 		return mFlowName.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 
 		if (obj == null) {
 			return false;
 		}
 
 		if (obj instanceof OneIfaceFirewallRule) {
 			OneIfaceFirewallRule rule = (OneIfaceFirewallRule) obj;
 			if (this == rule) {
 				return true;
 			} else {
 				return mFlowName.equals(rule.getFlowName());
 			}
 		}
 
 		return false;
 	}
 
 	@Override
 	public String toString() {
 		final String EQUALS = "=";
 		final String DELIMITER = ", ";
 
 		StringBuilder sb = new StringBuilder();
 
 		sb.append("DPID");
 		sb.append(EQUALS);
 		sb.append(mDpid);
 		sb.append(DELIMITER);
 
 		sb.append("ACTIVE");
 		sb.append(EQUALS);
 		sb.append(mActive);
 		sb.append(DELIMITER);
 
 		sb.append("PRIORITY");
 		sb.append(EQUALS);
 		sb.append(mPriority);
 		sb.append(DELIMITER);
 
 		sb.append("FIREWALL_PORT");
 		sb.append(EQUALS);
 		sb.append(mFirewallPort);
 		sb.append(DELIMITER);
 
 		sb.append("HOST_PORT");
 		sb.append(EQUALS);
 		sb.append(mHostPort);
 		sb.append(DELIMITER);
 
 		sb.append("HOST_MAC");
 		sb.append(EQUALS);
 		sb.append(mHostMac);
 
 		return sb.toString();
 	}
 }
