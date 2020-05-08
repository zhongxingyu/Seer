 package def;
 
 import net.sourceforge.jpcap.net.TCPPacket;
 import java.util.regex.*;
 import java.util.*;
 
 
 public class TCPProtocolRuleChecker extends AbstractProtocolRuleChecker<TCPPacket, TCPProtocolRule> {
 
     final List<TCPPacket[]> store;
     final int ssize;
 
     public TCPProtocolRuleChecker(int numSubRules) {
 	store = new ArrayList<TCPPacket[]>();
 	ssize = numSubRules;
     }
 
     @Override
     public void add(TCPPacket candidate, TCPProtocolRule rule, String ruleName, String host) {
 	List<Integer> indices = matchingSubRuleIndices(candidate, rule, host);
 	List<TCPPacket[]> toRemove = new ArrayList<TCPPacket[]>();
 
 	for (Integer i : indices) {
 	    TCPPacket[] row = newRow();
     
 	    row[i] = candidate;
 	    store.add(row);
 	}
 
 	for (TCPPacket[] list : store)
 	    if (isFull(list)) {
 		System.out.println("ZOMG!!!!!");
 		toRemove.add(list);
 	    }
 	
 	for (TCPPacket[] list : toRemove)
 	    store.remove(list);
     }
 
     private boolean isFull(TCPPacket[] list) {
 	for (int x = 0; x < ssize; x++)
 	    if (list[x] == null)
 		return false;
 
 	return true;
     }
 
     private TCPPacket[] newRow() {
 	TCPPacket[] list = new TCPPacket[ssize];
 	
	//for (int x = 0; x < ssize; x++)
	//  list.add(null);
 
 	return list;
     }
 
     private List<Integer> matchingSubRuleIndices(TCPPacket candidate, TCPProtocolRule rule, String host) {
 	List<Integer> indices = new ArrayList<Integer>();
 
 	for (ProtocolSubrule subRule : rule.subrules)
 	    if (matchSubRule(candidate, subRule, rule, host))
 		indices.add( rule.subrules.indexOf(subRule) );	   
 	
 	return indices;
     }
 
     private boolean matchSubRule(TCPPacket candidate, ProtocolSubrule subRule, TCPProtocolRule rule, String host) {
 	if (checkDirection(candidate, subRule, rule, host))
 	    if (checkContents(candidate, subRule))
 		if (checkFlags(candidate, subRule))
 		    return true;
 
 	return false;
     }
 
     private boolean checkFlags(TCPPacket candidate, ProtocolSubrule subRule) {
 	List<Character> flags = subRule.flags;
 
 	if (flags == null) return true;
 
 	if (candidate.isFin() && !flags.contains('F')) return false;
 	if (candidate.isUrg() && !flags.contains('U')) return false;
 	if (candidate.isSyn() && !flags.contains('S')) return false;
 	if (candidate.isPsh() && !flags.contains('P')) return false;
 	if (candidate.isAck() && !flags.contains('A')) return false;
 	if (candidate.isRst() && !flags.contains('R')) return false;
 
 	return true;
     }
 
     private boolean checkDirection(TCPPacket candidate, ProtocolSubrule subRule, TCPProtocolRule rule, String host) {
 	if ((subRule.isSend && checkSend(candidate, rule, host)) || (!subRule.isSend && checkRecv(candidate, rule, host)))
 	    return true;
 
 	return false;
     }
 
     private boolean checkSend(TCPPacket candidate, TCPProtocolRule rule, String host) {
 	if (candidate.getSourceAddress().equals(host) &&
 	    (candidate.getSourcePort() == rule.srcPort || rule.srcPort == 0) &&
 	    (candidate.getDestinationAddress().equals(rule.ip) || rule.ip.equals("*.*.*.*")) &&
 	    (candidate.getDestinationPort() == rule.dstPort || rule.dstPort == 0))
 	    return true;
 
 	return false;
     }
 
     private boolean checkRecv(TCPPacket candidate, TCPProtocolRule rule, String host) {
 	if ((candidate.getSourceAddress().equals(rule.ip) || rule.ip.equals("*.*.*.*")) &&
 	    (candidate.getSourcePort() == rule.dstPort || rule.dstPort == 0) &&
 	    candidate.getDestinationAddress().equals(host) &&
 	    (candidate.getDestinationPort() == rule.srcPort || rule.srcPort == 0))
 	    return true;
 
 	return false;
     }
 
 }
