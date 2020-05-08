 package org.blockout.network.reworked;
 
 import org.blockout.network.dht.IHash;
 
 import com.google.common.base.Preconditions;
 
public class IAmYourPredeccessor {
	private final IHash	nodeId;
 
 	public IAmYourPredeccessor(final IHash nodeId) {
 
 		Preconditions.checkNotNull( nodeId );
 
 		this.nodeId = nodeId;
 	}
 
 	public IHash getNodeId() {
 		return nodeId;
 	}
 
 }
