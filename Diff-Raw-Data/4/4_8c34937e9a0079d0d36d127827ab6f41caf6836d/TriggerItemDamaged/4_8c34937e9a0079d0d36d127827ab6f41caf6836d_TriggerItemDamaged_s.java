 package berryh.moltencore.common.trigger;
 
 import net.minecraft.tileentity.TileEntity;
 import net.minecraftforge.common.ForgeDirection;
 import berryh.moltencore.common.MoltenCoreCommonProxy;
 import berryh.moltencore.common.TileEntityMoltenCoreRepairer;
 import buildcraft.api.gates.ITriggerDirectional;
 import buildcraft.api.gates.ITriggerParameter;
 
 public class TriggerItemDamaged extends TriggerMCM {
 
 	public State state;
 
 	public TriggerItemDamaged(int id, State state) {
 		this.id = id;
 		this.state = state;
 	}
 
 	@Override
 	public int getIndexInTexture() {
 		switch (state) {
 		case DAMAGED:
 			return 1;
 		default:
 			return 2;
 		}
 	}
 
 	@Override
 	public boolean hasParameter() {
 		return false;
 	}
 
 	@Override
 	public String getDescription() {
 		switch (state) {
 		case DAMAGED:
 			return "Item Damaged";
 		}
		return "Item Repaired";
 	}
 
 	@Override
 	public boolean isTriggerActive(TileEntity tile, ITriggerParameter parameter) {
 		if ((tile instanceof TileEntityMoltenCoreRepairer)) {
 			TileEntityMoltenCoreRepairer tmcr = (TileEntityMoltenCoreRepairer) tile;
 			if (tmcr.inv != null) {
 				if (state == State.DAMAGED) {
 					return tmcr.inv.isItemDamaged();
 				}
 				if (state == State.REPAIRED) {
 					return tmcr.inv.getItemDamage() == 0;
 				}
 			}
 
 		}
 		return false;
 	}
 
 	public String getTextureFile() {
 		return MoltenCoreCommonProxy.BLOCK;
 	}
 
 	public static enum State {
 		DAMAGED, REPAIRED;
 	}
 }
