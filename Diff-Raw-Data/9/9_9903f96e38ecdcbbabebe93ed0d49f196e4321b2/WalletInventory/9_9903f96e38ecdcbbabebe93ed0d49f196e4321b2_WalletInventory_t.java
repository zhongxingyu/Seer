 /**
  * CCM Modding, ModJam
  */
 package ccm.trade_stuffs.inventory;
 
 import java.util.HashMap;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
import ccm.trade_stuffs.api.coins.CoinType;
import ccm.trade_stuffs.api.coins.CoinTypes;
 import ccm.trade_stuffs.items.ModItems;
 import ccm.trade_stuffs.items.WalletItem;
 import ccm.trade_stuffs.utils.helper.InventoryHelper;
 import ccm.trade_stuffs.utils.helper.NBTHelper;
 import ccm.trade_stuffs.utils.lib.Properties;
 
 /**
  * WalletInventory
  * <p>
  * 
  * @author Captain_Shadows
  */
 public class WalletInventory implements IInventory {
 	
 	public HashMap<CoinType, Integer> coins = new HashMap<CoinType, Integer>();
 	private ItemStack slotStack;
 	private ItemStack slotFakeCoins;
 	
 	private ItemStack wallet;
 	
 	private int coinBalance = 0;
 
 	public WalletInventory(ItemStack wallet) {
 		this.wallet = wallet;
 		readFromNBT(wallet);
 	}	
 
 	@Override
 	public int getSizeInventory() {
 		return 1;
 	}
 
 	@Override
 	public ItemStack decrStackSize(int slot, int amount) {
 		return InventoryHelper.decreaseStackSize(this, slot, amount);
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int slot) {
 		if(slot == 0) {
 			return slotStack;
 		} else if(slot == 1) {
 			return slotFakeCoins;
 		}
 		return null;
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int slot) {
 		if(slot == 0) {
 			ItemStack temp = slotStack;
 			slotStack = null;
 			return temp;
 		} else if(slot == 1) {
 			ItemStack temp = slotFakeCoins;
 			slotFakeCoins = null;
 			return temp;
 		}
 		return null;
 	}
 
 	@Override
 	public void setInventorySlotContents(int slot, ItemStack stack) {
 		if(slot == 0) {
 			if(stack != null) {
 				CoinType coinType = CoinTypes.getTypes().get(stack.getItemDamage());
 				int coinAmount = 0;
 				if(coins.containsKey(coinType)) {
 					coinAmount = coins.get(coinType);
 				}
 				if(coinAmount < getStacksPerCoin() * 64) {
 					int canAdd = (getStacksPerCoin() * 64) - coinAmount;
 					int added = 0;
 					if(stack.stackSize < canAdd) {
 						added = stack.stackSize;
 						stack.stackSize = 0;
 					} else {
 						added = canAdd;
 						stack.stackSize -= canAdd;
 					}
 					coins.put(coinType, coinAmount + added);
 				}			
 				if(stack.stackSize != 0) {
 					slotStack = stack;
 				} else {
 					slotStack = null;
 				}
 			}
 			countCoinBalance();
 			slotFakeCoins = new ItemStack(ModItems.coin, getCoinBalance(), 0);
 		}
 	}
 
 	@Override
 	public String getInvName() {
 		return wallet.hasDisplayName() ? wallet.getDisplayName() : "inventory.wallet";
 	}
 
 	@Override
 	public boolean isInvNameLocalized() {
 		return wallet.hasDisplayName();
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 64;
 	}
 
 	@Override
 	public void onInventoryChanged() {
 	}
 	
 	public void countCoinBalance() {
 		coinBalance = 0;
 		for(CoinType coinType : coins.keySet()) {
 			coinBalance += (coinType.getValue() * coins.get(coinType));
 		}
 		setHasMoney(coinBalance > 0);
 	}
 	
 	public int getCoinBalance() {
 		return coinBalance;
 	}
 	
 	public int getStacksPerCoin() {
 		return Properties.STACKS_PER_COIN;
 	}
 	
 	public void readFromNBT(ItemStack stack) {
 		NBTHelper.initCompound(stack);
 		NBTTagCompound nbt = stack.getTagCompound();
 		coins = new HashMap<CoinType, Integer>();
 		for(CoinType coinType : CoinTypes.getTypes()) {
 			if(nbt.hasKey("COIN-" + coinType.getName())) {
 				coins.put(coinType, nbt.getInteger("COIN-" + coinType.getName()));
 			}
 		}
 		countCoinBalance();
 	}
 	
 	public void writeToNBT(ItemStack stack) {
 		NBTHelper.initCompound(stack);
 		NBTTagCompound nbt = stack.getTagCompound();
 		for(CoinType coinType : coins.keySet()) {
 			nbt.setInteger("COIN-" + coinType.getName(), coins.get(coinType));
 		}
 	}
 
 	public void setHasMoney(boolean has) {
 		NBTHelper.setBoolean(wallet, WalletItem.fullWallet, has);
 	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
 		return true;
 	}
 
 	@Override
 	public void openChest() {
 	}
 
 	@Override
 	public void closeChest() {
 	}
 
 	@Override
 	public boolean isItemValidForSlot(int slot, ItemStack item) {
 		return false;
 	}
 }
