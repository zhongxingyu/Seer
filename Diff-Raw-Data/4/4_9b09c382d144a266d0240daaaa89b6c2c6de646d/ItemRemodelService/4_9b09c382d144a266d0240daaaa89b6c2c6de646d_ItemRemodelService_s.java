 /*
  * This file is part of aion-unique <aion-unique.org>.
  *
  *  aion-unique is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  aion-unique is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.aionemu.gameserver.services;
 
 import com.aionemu.gameserver.model.DescriptionId;
 import com.aionemu.gameserver.model.gameobjects.Item;
 import com.aionemu.gameserver.model.gameobjects.player.Player;
 import com.aionemu.gameserver.model.gameobjects.player.Storage;
 import com.aionemu.gameserver.model.templates.item.ArmorType;
 import com.aionemu.gameserver.model.templates.item.ItemQuality;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_UPDATE_ITEM;
 import com.aionemu.gameserver.utils.PacketSendUtility;
 
 /**
  * @author Sarynth
  *
  */
 public class ItemRemodelService
 {
 	/**
 	 * 
 	 * @param player
 	 * @param keepItemObjId
 	 * @param extractItemObjId
 	 */
 	public void remodelItem (Player player, int keepItemObjId, int extractItemObjId)
 	{
 		Storage inventory = player.getInventory();
 		Item keepItem = inventory.getItemByObjId(keepItemObjId);
 		Item extractItem = inventory.getItemByObjId(extractItemObjId);
 		
 		
 		// Check Player Level
 		if (player.getLevel() < 30)
 		{
 			
 			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_PC_LEVEL_LIMIT);
 			return;
 		}
 		
 		// Check Kinah
 		if (player.getInventory().getKinahItem().getItemCount() < 1000)
 		{
 			PacketSendUtility.sendPacket(player,
 				SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_NOT_ENOUGH_GOLD(new DescriptionId(keepItem.getItemTemplate().getNameId())));
 			return;
 		}
 		
 		// Check for using "Pattern Reshaper" (168100000)
 		if (extractItem.getItemTemplate().getTemplateId() == 168100000)
 		{
 			if (keepItem.getItemTemplate() == keepItem.getItemSkinTemplate())
 			{
 				PacketSendUtility.sendMessage(player, "That item does not have a remodeled skin to remove.");
 				return;
 			}
 			// Remove Money
 			player.getInventory().decreaseKinah(1000);
 			
 			// Remove Pattern Reshaper
 			player.getInventory().decreaseItemCount(extractItem, 1);
 			
 			// Revert item to ORIGINAL SKIN
 			keepItem.setItemSkinTemplate(keepItem.getItemTemplate());
 			
 			// Notify Player
 			PacketSendUtility.sendPacket(player, new SM_UPDATE_ITEM(keepItem));
 			PacketSendUtility.sendPacket(player,
 				SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_SUCCEED(new DescriptionId(keepItem.getItemTemplate().getNameId())));
 			
 			return;
 		}
 		// Check that types match.
 		if(keepItem.getItemTemplate().getWeaponType() != extractItem.getItemSkinTemplate().getWeaponType()
 			|| (extractItem.getItemSkinTemplate().getArmorType() != ArmorType.CLOTHES
			&& keepItem.getItemTemplate().getArmorType() != extractItem.getItemSkinTemplate().getArmorType()))
 		{
 			PacketSendUtility.sendPacket(player,
 				SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_NOT_COMPATIBLE(
 					new DescriptionId(keepItem.getItemTemplate().getNameId()),
 					new DescriptionId(extractItem.getItemSkinTemplate().getNameId())
 			));
 			return;
 		}
 
 		// TODO: Find a consistent mask value to determine if item may be remodeled.
 		
 		// Temporary check... I *think* epic and mythic items can *never* be remodeled...
 		if (keepItem.getItemTemplate().getItemQuality() == ItemQuality.EPIC ||
 			keepItem.getItemTemplate().getItemQuality() == ItemQuality.MYTHIC)
 		{
 			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300478,
 				new DescriptionId(keepItem.getItemTemplate().getNameId())));
 			return;
 		}
 		
 		// Check for hacks... I *think* epic and mythic items can *never* be remodeled...
 		if (extractItem.getItemTemplate().getItemQuality() == ItemQuality.EPIC ||
 			extractItem.getItemTemplate().getItemQuality() == ItemQuality.MYTHIC)
 		{
 			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300482,
 				new DescriptionId(extractItem.getItemTemplate().getNameId())));
 			return;
 		}
 		
 		
 		// -- SUCCESS --
 		
 		// Remove Money
 		player.getInventory().decreaseKinah(1000);
 		
 		// Remove Item
 		player.getInventory().decreaseItemCount(extractItem, 1);
 		
 		// REMODEL ITEM
 		keepItem.setItemSkinTemplate(extractItem.getItemSkinTemplate());
 		
 		// Transfer Dye
 		keepItem.setItemColor(extractItem.getItemColor());
 		
 		// Notify Player
 		PacketSendUtility.sendPacket(player, new SM_UPDATE_ITEM(keepItem));
 		PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300483, new DescriptionId(keepItem.getItemTemplate().getNameId())));
 	}
 }
