 package gt.general.logic.response;
 
 import gt.general.character.ZombieManager;
 
import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.block.Block;
 
 public abstract class ZombieResponse extends Response {
 
 	private ZombieManager zm;
 
 	public ZombieResponse(String labelPrefix) {
 		super(labelPrefix);
 	}
 
 	@Override
 	public Set<Block> getBlocks() {
		return new HashSet<Block>();
 	}
 	
 	public void setZombieManager(ZombieManager zm) {
 		this.zm = zm;		
 	}
 	
 	public ZombieManager getZombieManager() {
 		return zm;
 	}
 
 }
