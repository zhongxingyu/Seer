 package com.github.soniex2.endermoney.trading.tileentity;
 
 import java.math.BigInteger;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 
 import com.github.soniex2.endermoney.core.EnderCoin;
 import com.github.soniex2.endermoney.core.EnderMoney;
 import com.github.soniex2.endermoney.trading.TradeError;
 import com.github.soniex2.endermoney.trading.base.AbstractTraderTileEntity;
 
 public class TileEntityCreativeItemTrader extends AbstractTraderTileEntity {
 
 	public TileEntityCreativeItemTrader() {
 		super(18);
 	}
 
 	public ItemStack[] getTradeInputs() {
 		ItemStack[] tradeInputs = new ItemStack[9];
 		for (int i = 0; i < 9; i++) {
			tradeInputs[i] = inv[i] != null ? inv[i].copy() : null;
 		}
 		return tradeInputs;
 	}
 
 	public ItemStack[] getTradeOutputs() {
 		ItemStack[] tradeOutputs = new ItemStack[9];
 		for (int i = 0; i < 9; i++) {
			tradeOutputs[i] = inv[i + 9] != null ? inv[i].copy() : null;
 		}
 		return tradeOutputs;
 	}
 
 	public boolean doTrade(IInventory fakeInv, int inputMinSlot, int inputMaxSlot,
 			int outputMinSlot, int outputMaxSlot) throws TradeError {
 		if (fakeInv == null) { throw new TradeError(1, "Invalid inventory",
 				new NullPointerException()); }
 		HashMap<ItemStack, Integer> tradeInputs = new HashMap<ItemStack, Integer>();
 		BigInteger moneyRequired = BigInteger.ZERO;
 		for (ItemStack i : getTradeInputs()) {
 			if (i == null) {
 				continue;
 			}
 			if (i.getItem() == EnderMoney.coin) {
 				moneyRequired = moneyRequired.add(BigInteger.valueOf(EnderCoin
 						.getValueFromItemStack(i)));
 				continue;
 			}
 			ItemStack index = i.copy();
 			index.stackSize = 1;
 			if (tradeInputs.containsKey(index)) {
 				tradeInputs.put(index, i.stackSize + tradeInputs.get(index));
 			} else {
 				tradeInputs.put(index, i.stackSize);
 			}
 		}
 
 		HashMap<ItemStack, Integer> tradeInput = new HashMap<ItemStack, Integer>();
 		BigInteger money = BigInteger.ZERO;
 		for (int i = inputMinSlot; i < inputMaxSlot; i++) {
 			ItemStack is = fakeInv.getStackInSlot(i);
 			if (is == null) {
 				continue;
 			}
 			if (is.getItem() == EnderMoney.coin) {
 				moneyRequired = moneyRequired.add(BigInteger.valueOf(EnderCoin
 						.getValueFromItemStack(is)));
 				continue;
 			}
 			ItemStack index = is.copy();
 			index.stackSize = 1;
 			if (tradeInput.containsKey(index)) {
 				tradeInput.put(index, is.stackSize + tradeInput.get(index));
 			} else {
 				tradeInput.put(index, is.stackSize);
 			}
 		}
 
 		if (money.compareTo(moneyRequired) < 0) { return false; }
 		BigInteger newMoney = money.subtract(moneyRequired);
 
 		Set<Entry<ItemStack, Integer>> itemsRequired = tradeInputs.entrySet();
 		Iterator<Entry<ItemStack, Integer>> i = itemsRequired.iterator();
 		HashMap<ItemStack, Integer> newInput = new HashMap<ItemStack, Integer>();
 		while (i.hasNext()) {
 			Entry<ItemStack, Integer> entry = i.next();
 			ItemStack item = entry.getKey();
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
 			newInput.put(((EnderCoin) EnderMoney.coin).getItemStack(Long.MAX_VALUE, 1), a);
 			newInput.put(((EnderCoin) EnderMoney.coin).getItemStack(b, 1), 1);
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
 		for (int a = 0; a < outputMaxSlot - outputMinSlot; a++) {
 			fakeInv.setInventorySlotContents(a + outputMinSlot, tradeOutputs[a]);
 		}
 		Set<Entry<ItemStack, Integer>> input = newInput.entrySet();
 		Iterator<Entry<ItemStack, Integer>> it = input.iterator();
 		int slot = inputMinSlot;
 		while (it.hasNext()) {
 			if (slot >= fakeInv.getSizeInventory()) { throw new TradeError(0,
 					"Couldn't complete trade: Out of inventory space"); }
 			if (fakeInv.getStackInSlot(slot) == null) {
 				slot++;
 				continue;
 			}
 			Entry<ItemStack, Integer> entry = it.next();
 			ItemStack item = entry.getKey();
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
