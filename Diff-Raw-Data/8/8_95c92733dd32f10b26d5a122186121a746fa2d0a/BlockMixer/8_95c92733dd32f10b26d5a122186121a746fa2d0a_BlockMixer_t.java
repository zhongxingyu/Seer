 package ip.industrialProcessing.machines.mixer;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.StepSound;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import ip.industrialProcessing.IndustrialProcessing;
 import ip.industrialProcessing.config.ConfigMachineBlocks;
import ip.industrialProcessing.config.ConfigRenderers;
 import ip.industrialProcessing.machines.BlockMachine;
 import ip.industrialProcessing.machines.BlockMachineRendered;
 
 public class BlockMixer extends BlockMachineRendered {
 
 	public BlockMixer() {
 		super(ConfigMachineBlocks.getMixerBlockID(), Material.iron, 1F, Block.soundMetalFootstep, "Mixer", IndustrialProcessing.tabMachines);
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		 
 		return new TileEntityMixer();
 	}
	
    @Override
    public int getRenderType()
    {
        return ConfigRenderers.getRendererMixerId();
    }
 
 }
