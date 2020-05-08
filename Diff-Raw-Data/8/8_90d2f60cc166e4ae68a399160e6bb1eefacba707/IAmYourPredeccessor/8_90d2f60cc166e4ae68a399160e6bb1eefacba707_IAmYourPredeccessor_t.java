 package org.blockout.network.reworked;
 
import java.io.Serializable;

 import org.blockout.network.dht.IHash;
 
 import com.google.common.base.Preconditions;
 
public class IAmYourPredeccessor implements Serializable {
	private static final long	serialVersionUID	= -3266094817926621826L;
	private final IHash			nodeId;
 
 	public IAmYourPredeccessor(final IHash nodeId) {
 
 		Preconditions.checkNotNull( nodeId );
 
 		this.nodeId = nodeId;
 	}
 
 	public IHash getNodeId() {
 		return nodeId;
 	}
 
 }
