 package com.github.soniex2.endermoney.trading.tileentity;
 
 import java.math.BigInteger;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 
 import com.github.soniex2.endermoney.core.EnderCoin;
 import com.github.soniex2.endermoney.core.EnderMoney;
 import com.github.soniex2.endermoney.trading.TradeError;
 import com.github.soniex2.endermoney.trading.base.AbstractTraderTileEntity;
 import com.github.soniex2.endermoney.trading.helper.item.ItemStackMapKey;
 
 public class TileEntityCreativeItemTrader extends AbstractTraderTileEntity {
 
 	public TileEntityCreativeItemTrader() {
 		super(18);
 	}
 
 	public ItemStack[] getTradeInputs() {
 		ItemStack[] tradeInputs = new ItemStack[9];
 		for (int i = 0; i < 9; i++) {
 			tradeInputs[i] = ItemStack.copyItemStack(inv[i]);
 		}
 		return tradeInputs;
 	}
 
 	public ItemStack[] getTradeOutputs() {
 		ItemStack[] tradeOutputs = new ItemStack[9];
 		for (int i = 0; i < 9; i++) {
 			tradeOutputs[i] = ItemStack.copyItemStack(inv[i + 9]);
 		}
 		return tradeOutputs;
 	}
 
 	public boolean doTrade(IInventory fakeInv, int inputMinSlot, int inputMaxSlot,
 			int outputMinSlot, int outputMaxSlot) throws TradeError {
 		if (fakeInv == null) { throw new TradeError(1, "Invalid inventory",
 				new NullPointerException()); }
 		HashMap<ItemStackMapKey, Integer> tradeInputs = new HashMap<ItemStackMapKey, Integer>();
 		BigInteger moneyRequired = BigInteger.ZERO;
 		for (ItemStack i : getTradeInputs()) {
 			if (i == null) {
 				continue;
 			}
 			if (i.getItem() == EnderMoney.coin) {
 				moneyRequired = moneyRequired.add(BigInteger.valueOf(
 						EnderCoin.getValueFromItemStack(i)).multiply(
 						BigInteger.valueOf(i.stackSize)));
 				continue;
 			}
 			ItemStackMapKey index = new ItemStackMapKey(i);
 			if (tradeInputs.containsKey(index)) {
 				tradeInputs.put(index, i.stackSize + tradeInputs.get(index));
 			} else {
 				tradeInputs.put(index, i.stackSize);
 			}
 		}
 
 		HashMap<ItemStackMapKey, Integer> tradeInput = new HashMap<ItemStackMapKey, Integer>();
 		BigInteger money = BigInteger.ZERO;
 		for (int i = inputMinSlot; i <= inputMaxSlot; i++) {
 			ItemStack is = fakeInv.getStackInSlot(i);
 			if (is == null) {
 				continue;
 			}
 			if (is.getItem() == EnderMoney.coin) {
 				money = money.add(BigInteger.valueOf(EnderCoin.getValueFromItemStack(is)).multiply(
 						BigInteger.valueOf(is.stackSize)));
 				continue;
 			}
 			ItemStackMapKey index = new ItemStackMapKey(is);
 			if (tradeInput.containsKey(index)) {
 				tradeInput.put(index, is.stackSize + tradeInput.get(index));
 			} else {
 				tradeInput.put(index, is.stackSize);
 			}
 		}
 
 		if (money.compareTo(moneyRequired) < 0) { return false; }
 		BigInteger newMoney = money.subtract(moneyRequired);
 
 		Set<Entry<ItemStackMapKey, Integer>> itemsRequired = tradeInputs.entrySet();
 		Iterator<Entry<ItemStackMapKey, Integer>> i = itemsRequired.iterator();
 		HashMap<ItemStackMapKey, Integer> newInput = new HashMap<ItemStackMapKey, Integer>();
 		while (i.hasNext()) {
 			Entry<ItemStackMapKey, Integer> entry = i.next();
 			ItemStackMapKey item = entry.getKey();
 			Integer amount = entry.getValue();
 			Integer available = tradeInput.get(item);
 			if (available == null) { return false; }
 			if (available < amount) { return false; }
 			if (available - amount == 0) {
 				continue;
 			}
 			newInput.put(item, available - amount);
 		}
 		if (newMoney.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
 			BigInteger[] coinCount = newMoney
 					.divideAndRemainder(BigInteger.valueOf(Long.MAX_VALUE));
 			int a = coinCount[0].intValue();
 			long b = coinCount[1].longValue();
 			ItemStack is1 = ((EnderCoin) EnderMoney.coin).getItemStack(Long.MAX_VALUE, 1);
 			ItemStack is2 = ((EnderCoin) EnderMoney.coin).getItemStack(b, 1);
 			ItemStackMapKey index1 = new ItemStackMapKey(is1);
 			ItemStackMapKey index2 = new ItemStackMapKey(is2);
 			newInput.put(index1, a);
 			newInput.put(index2, 1);
		} else if (!newMoney.equals(BigInteger.ZERO)) {
			ItemStack is = ((EnderCoin) EnderMoney.coin).getItemStack(newMoney.longValue(), 1);
			ItemStackMapKey index = new ItemStackMapKey(is);
			newInput.put(index, 1);
 		}
 		ItemStack[] tradeOutputs = getTradeOutputs();
 		// TODO put commented out code below somewhere else
 		/*
 		 * int[] something = new int[tradeOutputs.length];
 		 * int[][] lookAt = new int[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 },
 		 * { -1, 0, 0 },
 		 * { 0, -1, 0 }, { 0, 0, -1 } };
 		 * for (int a = 0; a < lookAt.length; a++) {
 		 * TileEntity tileEntity = this.worldObj.getBlockTileEntity(this.xCoord
 		 * + lookAt[a][0],
 		 * this.yCoord + lookAt[a][1], this.zCoord + lookAt[a][2]);
 		 * if (tileEntity == null) continue;
 		 * if (tileEntity instanceof IInventory) {
 		 * IInventory iinv = (IInventory) tileEntity;
 		 * for (int b = 0; b < iinv.getSizeInventory(); b++) {
 		 * ItemStack is = iinv.getStackInSlot(b);
 		 * if (is == null) continue;
 		 * for (int c = 0; c < tradeOutputs.length; c++) {
 		 * if (tradeOutputs[c] == null) continue;
 		 * if (tradeOutputs[c].isItemEqual(is) &&
 		 * ItemStack.areItemStackTagsEqual(tradeOutputs[c], is)) {
 		 * something[c] += is.stackSize;
 		 * }
 		 * }
 		 * }
 		 * }
 		 * }
 		 */
 		ItemStack[] oldOutInv = new ItemStack[outputMaxSlot - outputMinSlot + 1];
 		for (int a = outputMinSlot; a <= outputMaxSlot; a++) {
 			oldOutInv[a - outputMinSlot] = ItemStack.copyItemStack(fakeInv.getStackInSlot(a));
 		}
 		for (int a = outputMinSlot; a <= outputMaxSlot; a++) {
 			ItemStack is = fakeInv.getStackInSlot(a);
 			for (int b = 0; b < tradeOutputs.length; b++) {
 				if (is != null && tradeOutputs[b] != null && is.isItemEqual(tradeOutputs[b])
 						&& ItemStack.areItemStackTagsEqual(is, tradeOutputs[b])) {
 					if (is.isStackable()) {
 						if (is.stackSize < is.getMaxStackSize()) {
 							if (is.stackSize + tradeOutputs[b].stackSize > is.getMaxStackSize()) {
 								int newStackSize = tradeOutputs[b].stackSize + is.stackSize;
 								if (newStackSize > is.getMaxStackSize()) {
 									newStackSize = newStackSize - is.getMaxStackSize();
 								}
 								tradeOutputs[b].stackSize = newStackSize;
 								is.stackSize = is.getMaxStackSize();
 							} else {
 								is.stackSize = is.stackSize + tradeOutputs[b].stackSize;
 								tradeOutputs[b] = null;
 							}
 						}
 					}
 				} else if (is == null && tradeOutputs[b] != null) {
 					fakeInv.setInventorySlotContents(a, tradeOutputs[b]);
 					is = fakeInv.getStackInSlot(a);
 					tradeOutputs[b] = null;
 				}
 				if (tradeOutputs[b] != null && tradeOutputs[b].stackSize <= 0) {
 					tradeOutputs[b] = null;
 				}
 			}
 		}
 		for (int a = 0; a < tradeOutputs.length; a++) {
 			if (tradeOutputs[a] != null) {
 				for (int b = 0; b < oldOutInv.length; b++) {
 					fakeInv.setInventorySlotContents(b + outputMinSlot, oldOutInv[b]);
 				}
 				throw new TradeError(0, "Couldn't complete trade: Out of inventory space");
 			}
 		}
 		for (int _i = inputMinSlot; _i < inputMaxSlot; _i++) {
 			fakeInv.setInventorySlotContents(_i, null);
 		}
 		Set<Entry<ItemStackMapKey, Integer>> input = newInput.entrySet();
 		Iterator<Entry<ItemStackMapKey, Integer>> it = input.iterator();
 		int slot = inputMinSlot;
 		while (it.hasNext()) {
 			if (slot >= inputMaxSlot) { throw new TradeError(0,
 					"Couldn't complete trade: Out of inventory space"); }
 			if (fakeInv.getStackInSlot(slot) != null) {
 				slot++;
 				continue;
 			}
 			Entry<ItemStackMapKey, Integer> entry = it.next();
 			ItemStackMapKey itemData = entry.getKey();
 			ItemStack item = new ItemStack(itemData.itemID, 1, itemData.damage);
 			item.stackTagCompound = (NBTTagCompound) itemData.getTag();
 			Integer amount = entry.getValue();
 			if (amount == 0) { // shouldn't happen but who knows...
 				continue;
 			}
 			int stacks = amount / item.getMaxStackSize();
 			int extra = amount % item.getMaxStackSize();
 			ItemStack newItem = item.copy();
 			newItem.stackSize = item.getMaxStackSize();
 			for (int n = slot; n < slot + stacks; n++) {
 				fakeInv.setInventorySlotContents(n, newItem);
 			}
 			slot += stacks;
 			newItem = item.copy();
 			newItem.stackSize = extra;
 			fakeInv.setInventorySlotContents(slot, newItem);
 			slot++;
 		}
 		return true;
 	}
 
 	@Override
 	public String getInvName() {
 		return "endermoney.traders.item";
 	}
 
 	@Override
 	public boolean isInvNameLocalized() {
 		return false;
 	}
 
 	@Override
 	public void openChest() {
 	}
 
 	@Override
 	public void closeChest() {
 	}
 
 }
