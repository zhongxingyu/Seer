 package ip.industrialProcessing.machines.plants.oilRefinary.asphaltBlower;
 
import buildcraft.core.gui.slots.SlotOutput;
 import ip.industrialProcessing.machines.containers.ContainerPoweredFluidWorkerMachine;
 import ip.industrialProcessing.machines.plants.oilRefinary.sourWaterStripper.TileEntitySourWaterStripper;
 import ip.industrialProcessing.slots.SlotLiquid;
 import ip.industrialProcessing.slots.SlotLiquidOutput;
 import ip.industrialProcessing.utils.containers.ContainerUtils;
 import net.minecraft.entity.player.InventoryPlayer;
 
 public class ContainerAsphaltBlower  extends ContainerPoweredFluidWorkerMachine{
 	private SlotLiquid inputWaterFullInput;
 	private SlotLiquidOutput inputWaterEmptyOutput;
 	private SlotOutput slot;
 	
 	public ContainerAsphaltBlower(InventoryPlayer inventoryPlayer, TileEntityAsphaltBlower tileEntity) {
 		super(inventoryPlayer, tileEntity);
 
 		inputWaterFullInput = new SlotLiquid(tileEntity, 0, 8, 19);
 		inputWaterEmptyOutput = new SlotLiquidOutput(tileEntity, 1, 8, 53);
 		slot = new SlotOutput(tileEntity, 2, 134, 33);
 		
 		addSlotToContainer(inputWaterFullInput);
 		addSlotToContainer(inputWaterEmptyOutput);
 		addSlotToContainer(slot);
 		
 		ContainerUtils.BindPlayerInventory(inventoryPlayer, this, 0);
 		
 		addTankToContainer(0);
 		addTankToContainer(1);
 
 		addWorkerToContainer(tileEntity.getWorker());
         addPowerToContainer(tileEntity.getMainPowerStorage());
 	}
 	
 	@Override
 	public int getSizeInventory() {
 		return 3;
 	}
 
 }
