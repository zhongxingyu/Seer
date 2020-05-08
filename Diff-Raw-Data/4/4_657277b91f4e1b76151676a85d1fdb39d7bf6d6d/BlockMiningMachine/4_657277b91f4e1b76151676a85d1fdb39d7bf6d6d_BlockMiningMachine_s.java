 package me.furt.industrial.block;
 
 import me.furt.industrial.IndustrialInc;
 import me.furt.industrial.block.design.DesignGenericBlock;
 import org.getspout.spoutapi.material.block.GenericCubeCustomBlock;
 
 public class BlockMiningMachine extends GenericCubeCustomBlock {
 
 	public BlockMiningMachine(IndustrialInc plugin) {
 		super(plugin, "Mining Machine", new DesignGenericBlock(plugin,
				plugin.assets.getTexture("block_machine"), new int[] { 103, 40,
						40, 40, 40, 24 }));
 		this.setHardness(4.0F);
 	}
 }
