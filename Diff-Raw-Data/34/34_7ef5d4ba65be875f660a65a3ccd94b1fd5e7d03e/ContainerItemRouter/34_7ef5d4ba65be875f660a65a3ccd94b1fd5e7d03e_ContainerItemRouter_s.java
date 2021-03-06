 package powercrystals.minefactoryreloaded.gui.container;
 
 import powercrystals.minefactoryreloaded.transport.TileEntityItemRouter;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 
 public class ContainerItemRouter extends ContainerFactoryInventory
 {
	private TileEntityItemRouter _router;

 	public ContainerItemRouter(TileEntityItemRouter router, InventoryPlayer inventoryPlayer)
 	{
 		super(router, inventoryPlayer);
		_router = router;
 	}
 	
 	@Override
 	protected void addSlots()
 	{
 		for(int i = 0; i < 5; i++)
 		{
 			for(int j = 0; j < 9; j++)
 			{
				addSlotToContainer(new Slot(_router, j + i * 9, 8 + j * 18, 20 + i * 18));
 			}
 		}
 	}
 	
 	@Override
 	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
 	{
 		return null;
 	}
 }
