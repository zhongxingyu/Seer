 package org.glite.authz.pap.ui.wizard;
 
import org.glite.authz.pap.common.PAP;
 import org.glite.authz.pap.common.utils.xacml.PolicySetHelper;
 import org.opensaml.xacml.policy.PolicySetType;
 
 public class LocalPAPPolicySet {
 	
 	private LocalPAPPolicySet() {}
 	
 	public static PolicySetType build() {
		return PolicySetHelper.buildWithAnyTarget(PAP.localPAPId, PolicySetHelper.COMB_ALG_ORDERED_DENY_OVERRIDS);
 	}
 
 }
