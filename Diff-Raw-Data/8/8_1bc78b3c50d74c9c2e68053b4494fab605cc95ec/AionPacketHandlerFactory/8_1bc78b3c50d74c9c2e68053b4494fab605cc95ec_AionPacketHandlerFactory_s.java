 /*
  * This file is part of aion-lightning <aion-lightning.com>.
  *
  *  aion-lightning is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  aion-lightning is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with aion-lightning.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.aionemu.gameserver.network.aion;
 
 import com.aionemu.commons.network.netty.State;
 import com.aionemu.commons.network.netty.handler.AbstractPacketHandlerFactory;
import com.aionemu.commons.network.netty.packet.AbstractClientPacket;
 import com.aionemu.gameserver.network.aion.clientpackets.*;
 import com.aionemu.gameserver.network.aion.serverpackets.*;
 
 /**
  * This factory is responsible for creating {@link AionPacketHandler} object. It also initializes created handler with a
  * set of packet prototypes.<br>
  * <br>
  * 
  * @author Luno
  * 
  */
 public class AionPacketHandlerFactory extends AbstractPacketHandlerFactory<AionChannelHandler>
 {
 	
 	/**
 	 * Creates new instance of <tt>AionPacketHandlerFactory</tt><br>
 	 */
 	public AionPacketHandlerFactory()
 	{
 		super(null, new AionClientPacketHandler<AionChannelHandler>());
 		
 		addPacket(new CM_L2AUTH_LOGIN_CHECK(0x07), State.CONNECTED);// 2.1
 		addPacket(new CM_MAC_ADDRESS(0x2F), State.CONNECTED, State.AUTHED, State.ENTERED);// 2.1
 		addPacket(new CM_TIME_CHECK(0x80), State.CONNECTED, State.AUTHED, State.ENTERED);// 2.1
 		addPacket(new CM_VERSION_CHECK(0xF2), State.CONNECTED); // 2.1
 		
 		//Client Packet's
 		addPacket(new CM_CRAFT(0x00), State.ENTERED);// 1.9
 		addPacket(new CM_CLIENT_COMMAND_LOC(0x01), State.ENTERED);// 1.9
 		addPacket(new CM_RESTORE_CHARACTER(0x04), State.AUTHED);// 1.9
 		addPacket(new CM_START_LOOT(0x05), State.ENTERED);// 1.9
 		addPacket(new CM_LOOT_ITEM(0x06), State.ENTERED);// 1.9
 		//addPacket(new CM_MOVE_ITEM(0x07), State.ENTERED);// 1.9
 		addPacket(new CM_CHARACTER_LIST(0x09), State.AUTHED);// 1.9
 		addPacket(new CM_CREATE_CHARACTER(0x0A), State.AUTHED);// 1.9
 		addPacket(new CM_DELETE_CHARACTER(0x0B), State.AUTHED);// 1.9
 		addPacket(new CM_LEGION_UPLOAD_EMBLEM(0x0C), State.ENTERED);// testing
 		addPacket(new CM_SPLIT_ITEM(0x10), State.ENTERED);// 1.9
 		addPacket(new CM_PLAYER_SEARCH(0x12), State.ENTERED);// 1.9
 		addPacket(new CM_LEGION_UPLOAD_INFO(0x13), State.ENTERED);// testing
 		addPacket(new CM_FRIEND_STATUS(0x15), State.ENTERED);// 1.9
 		addPacket(new CM_CHANGE_CHANNEL(0x17), State.ENTERED);// 1.9
 		addPacket(new CM_BLOCK_ADD(0x19), State.ENTERED);// 1.9
 		addPacket(new CM_BLOCK_DEL(0x1A), State.ENTERED);// 1.9
 		addPacket(new CM_SHOW_BLOCKLIST(0x1B), State.ENTERED);// 1.9
 		addPacket(new CM_CHECK_NICKNAME(0x1C), State.AUTHED);// 1.9
 		addPacket(new CM_REPLACE_ITEM(0x1D), State.ENTERED);// testing
 		addPacket(new CM_BLOCK_SET_REASON(0x1E), State.ENTERED);// 1.9
 		addPacket(new CM_MAC_ADDRESS2(0x21), State.ENTERED);// 1.9
 		addPacket(new CM_MACRO_CREATE(0x22), State.ENTERED);// 1.9
 		addPacket(new CM_MACRO_DELETE(0x23), State.ENTERED);// 1.9
 		addPacket(new CM_DISTRIBUTION_SETTINGS(0x24), State.ENTERED);// 1.9
 		addPacket(new CM_MAY_LOGIN_INTO_GAME(0x25), State.AUTHED);// 1.9
 		addPacket(new CM_SHOW_BRAND(0x28), State.ENTERED);// 1.9
 		addPacket(new CM_RECONNECT_AUTH(0x2A), State.AUTHED);// 1.9
 		addPacket(new CM_GROUP_LOOT(0x2B), State.ENTERED);	
 		//addPacket(new CM_SHOW_MAP(0x2F), State.ENTERED);// 1.9
 		addPacket(new CM_REPORT_PLAYER(0x32), State.ENTERED);// need to find wrong code
 		addPacket(new CM_GROUP_RESPONSE(0x33), State.ENTERED);// 1.9
 		addPacket(new CM_SUMMON_MOVE(0x34), State.ENTERED);// 1.9
 		addPacket(new CM_SUMMON_EMOTION(0x35), State.ENTERED);// 1.9
 		addPacket(new CM_SUMMON_ATTACK(0x36), State.ENTERED);// 1.9
 		addPacket(new CM_DELETE_QUEST(0x43), State.ENTERED);// 1.9
 		addPacket(new CM_ITEM_REMODEL(0x45), State.ENTERED);// 1.9			
 		addPacket(new CM_GODSTONE_SOCKET(0x46), State.ENTERED);// 1.9
 		addPacket(new CM_INVITE_TO_GROUP(0x4C), State.ENTERED);// 1.9
 		addPacket(new CM_ALLIANCE_GROUP_CHANGE(0x4D), State.ENTERED);// 1.9
 		addPacket(new CM_VIEW_PLAYER_DETAILS(0x4F), State.ENTERED);// 1.9
 		addPacket(new CM_PLAYER_STATUS_INFO(0x53), State.ENTERED);// 1.9
 		addPacket(new CM_CLIENT_COMMAND_ROLL(0x56), State.ENTERED);// 1.9			
 		addPacket(new CM_GROUP_DISTRIBUTION(0x57), State.ENTERED);// 1.9
 		addPacket(new CM_PING_REQUEST(0x5A), State.ENTERED);// 1.9
 		addPacket(new CM_DUEL_REQUEST(0x5D), State.ENTERED);// 1.9
 		addPacket(new CM_DELETE_ITEM(0x5F), State.ENTERED);// 1.9
 		addPacket(new CM_SHOW_FRIENDLIST(0x61), State.ENTERED);// 1.9
 		addPacket(new CM_FRIEND_ADD(0x62), State.ENTERED);// 1.9
 		addPacket(new CM_FRIEND_DEL(0x63), State.ENTERED);// 1.9
 		addPacket(new CM_SUMMON_COMMAND(0x64), State.ENTERED);// 1.9
 		addPacket(new CM_BROKER_LIST(0x66), State.ENTERED);// 1.9
 		addPacket(new CM_BROKER_SEARCH(0x67), State.ENTERED);// 2.0
 		addPacket(new CM_PRIVATE_STORE(0x6A), State.ENTERED);// 1.9
 		addPacket(new CM_PRIVATE_STORE_NAME(0x6B), State.ENTERED);// 1.9
 		addPacket(new CM_BROKER_SETTLE_LIST(0x6C), State.ENTERED);// 1.9
 		addPacket(new CM_BROKER_SETTLE_ACCOUNT(0x6D), State.ENTERED);// 1.9
 		addPacket(new CM_SEND_MAIL(0x6F), State.ENTERED);// 1.9
 		addPacket(new CM_BROKER_REGISTERED(0x70), State.ENTERED);// 1.9
 		addPacket(new CM_BUY_BROKER_ITEM(0x71), State.ENTERED);// 1.9
 		//addPacket(new CM_REGISTER_BROKER_ITEM(0x72), State.ENTERED);// 1.9
 		addPacket(new CM_BROKER_CANCEL_REGISTERED(0x73), State.ENTERED);// 1.9
 		addPacket(new CM_DELETE_MAIL(0x74),State.ENTERED);// 1.9
 		addPacket(new CM_TITLE_SET(0x76), State.ENTERED);// 1.9
 		addPacket(new CM_READ_MAIL(0x79), State.ENTERED);// 1.9
 		addPacket(new CM_GET_MAIL_ATTACHMENT(0x7B), State.ENTERED);// 1.9
 		addPacket(new CM_TELEPORT_SELECT(0x7F), State.ENTERED);// 1.9
 		addPacket(new CM_PETITION(0x85), State.ENTERED);// 1.9
 		addPacket(new CM_CHAT_MESSAGE_PUBLIC(0x86), State.ENTERED);// 1.9
 		addPacket(new CM_CHAT_MESSAGE_WHISPER(0x87), State.ENTERED);// 1.9
 		addPacket(new CM_PET_MOVE(0x88), State.ENTERED);// 2.0
 		addPacket(new CM_PET(0x89), State.ENTERED);// 2.0
 		addPacket(new CM_OPEN_STATICDOOR(0x8A), State.ENTERED);// 1.9
 		addPacket(new CM_CASTSPELL(0x8C), State.ENTERED);// 1.9
 		addPacket(new CM_SKILL_DEACTIVATE(0x8D), State.ENTERED);// 1.9
 		addPacket(new CM_REMOVE_ALTERED_STATE(0x8E), State.ENTERED);// 1.9
 		addPacket(new CM_TARGET_SELECT(0x92), State.ENTERED);// 1.9
 		addPacket(new CM_ATTACK(0x93), State.ENTERED);// 1.9
 		addPacket(new CM_EMOTION(0x96), State.ENTERED);// 1.9
 		addPacket(new CM_PING(0x97), State.AUTHED, State.ENTERED);// 1.9
 		addPacket(new CM_USE_ITEM(0x98), State.ENTERED);// 1.9
 		addPacket(new CM_EQUIP_ITEM(0x99), State.ENTERED);// 1.9
 		addPacket(new CM_FLIGHT_TELEPORT(0x9C), State.ENTERED);// 1.9
 		addPacket(new CM_QUESTION_RESPONSE(0x9D), State.ENTERED);// 1.9
 		addPacket(new CM_BUY_ITEM(0x9E), State.ENTERED);// 1.9
 		addPacket(new CM_SHOW_DIALOG(0x9F), State.ENTERED);// 1.9
 		addPacket(new CM_LEGION(0xA0), State.ENTERED);// 1.9
 		addPacket(new CM_MOVE(0xA3), State.ENTERED);// 1.9
 		addPacket(new CM_SET_NOTE(0xA5), State.ENTERED);// 1.9
 		addPacket(new CM_LEGION_MODIFY_EMBLEM(0xA6), State.ENTERED);// 1.9
 		addPacket(new CM_CLOSE_DIALOG(0xA8), State.ENTERED);// 1.9
 		addPacket(new CM_DIALOG_SELECT(0xA9), State.ENTERED);// 1.9
 		addPacket(new CM_LEGION_TABS(0xAA), State.ENTERED);// 1.9
 		addPacket(new CM_EXCHANGE_ADD_KINAH(0xAD), State.ENTERED);// 1.9
 		addPacket(new CM_EXCHANGE_LOCK(0xAE), State.ENTERED);// 1.9
 		addPacket(new CM_EXCHANGE_OK(0xAF), State.ENTERED);// 1.9
 		addPacket(new CM_EXCHANGE_REQUEST(0xB2), State.ENTERED);// 1.9
 		addPacket(new CM_EXCHANGE_ADD_ITEM(0xB3), State.ENTERED);// 1.9
 		addPacket(new CM_MANASTONE(0xB5), State.ENTERED);// 1.9
 		addPacket(new CM_EXCHANGE_CANCEL(0xB8), State.ENTERED);// 1.9
 		addPacket(new CM_PLAY_MOVIE_END(0xBC), State.ENTERED);// 1.9
 		addPacket(new CM_SUMMON_CASTSPELL(0xC0), State.ENTERED);// 1.9
 		addPacket(new CM_FUSION_WEAPONS(0xC1), State.ENTERED);// 1.9
 		addPacket(new CM_BREAK_WEAPONS(0xC2), State.ENTERED);// 1.9
 		addPacket(new CM_LEGION_SEND_EMBLEM(0xD3), State.ENTERED);// 1.9
 		addPacket(new CM_DISCONNECT(0xED), State.ENTERED);// seems wrong
 		addPacket(new CM_QUIT(0xEE), State.AUTHED, State.ENTERED);// 1.9
 		addPacket(new CM_MAY_QUIT(0xEF), State.AUTHED, State.ENTERED);// 1.9
 		addPacket(new CM_LEVEL_READY(0xF4), State.ENTERED);// 1.9
 		addPacket(new CM_UI_SETTINGS(0xF5), State.ENTERED);// 1.9
 		addPacket(new CM_OBJECT_SEARCH(0xF6), State.ENTERED);// 1.9
 		addPacket(new CM_CUSTOM_SETTINGS(0xF7), State.ENTERED);// 1.9
 		addPacket(new CM_REVIVE(0xF8), State.ENTERED);// 1.9
 		addPacket(new CM_CHARACTER_EDIT(0xFA), State.AUTHED);// 1.9
 		addPacket(new CM_ENTER_WORLD(0xFB), State.AUTHED); // 1.9
 		addPacket(new CM_TIME_CHECK(0xFD), State.CONNECTED, State.AUTHED, State.ENTERED);// 1.9
 		addPacket(new CM_GATHER(0xFE), State.ENTERED);// 1.9
 		addPacket(new CM_QUESTIONNAIRE(0x7c), State.ENTERED); // 1.9
 		// opcode 70 broker sell page
 		// opcode 6c broker sold items page
 		
 		//Server Packet's
 		addPacket(SM_VERSION_CHECK.class, 0x00);
 		addPacket(SM_STATS_INFO.class, 0x01);
 		addPacket(SM_STATUPDATE_HP.class, 0x03);
 		addPacket(SM_STATUPDATE_MP.class, 0x04);
 		addPacket(SM_ATTACK_STATUS.class, 0x05);
 		addPacket(SM_STATUPDATE_DP.class, 0x06);
 		addPacket(SM_DP_INFO.class, 0x07);
 		addPacket(SM_STATUPDATE_EXP.class, 0x08);
 		addPacket(SM_LEGION_UPDATE_NICKNAME.class, 0x0B);
 		addPacket(SM_ENTER_WORLD_CHECK.class, 0x0D);
 		addPacket(SM_LEGION_TABS.class, 0x0C);
 		addPacket(SM_NPC_INFO.class, 0x0E);
 		addPacket(SM_PLAYER_SPAWN.class, 0x0F);
 		addPacket(SM_GATHERABLE_INFO.class, 0x11);
 		addPacket(SM_TELEPORT_LOC.class, 0x14);
 		addPacket(SM_PLAYER_MOVE.class, 0x15);
 		addPacket(SM_DELETE.class, 0x16);
 		addPacket(SM_LOGIN_QUEUE.class, 0x17);
 		addPacket(SM_MESSAGE.class, 0x18);
 		addPacket(SM_SYSTEM_MESSAGE.class, 0x19);
 		addPacket(SM_INVENTORY_INFO.class, 0x1A);
 		addPacket(SM_ADD_ITEMS.class, 0x1B);
 		addPacket(SM_DELETE_ITEM.class, 0x1C);
 		addPacket(SM_UPDATE_ITEM.class, 0x1D);
 		addPacket(SM_UI_SETTINGS.class, 0x1E);
 		addPacket(SM_PLAYER_INFO.class, 0x20);
 		addPacket(SM_CASTSPELL.class, 0x21);
 		addPacket(SM_GATHER_STATUS.class, 0x22);
 		addPacket(SM_GATHER_UPDATE.class, 0x23);
 		addPacket(SM_UPDATE_PLAYER_APPEARANCE.class, 0x24);
 		addPacket(SM_EMOTION.class, 0x25);
 		addPacket(SM_GAME_TIME.class, 0x26);
 		addPacket(SM_TIME_CHECK.class, 0x27);
 		addPacket(SM_LOOKATOBJECT.class, 0x28);
 		addPacket(SM_TARGET_SELECTED.class, 0x29);
 		addPacket(SM_SKILL_CANCEL.class, 0x2A);
 		addPacket(SM_CASTSPELL_END.class, 0x2B);
 		addPacket(SM_SKILL_LIST.class, 0x2C);
 		addPacket(SM_SKILL_REMOVE.class, 0x2D);
 		addPacket(SM_SKILL_ACTIVATION.class, 0x2E);
 		addPacket(SM_ABNORMAL_STATE.class, 0x31);
 		addPacket(SM_ABNORMAL_EFFECT.class, 0x32);
 		addPacket(SM_SKILL_COOLDOWN.class, 0x33);
 		addPacket(SM_QUESTION_WINDOW.class, 0x34);
 		addPacket(SM_ATTACK.class, 0x36);
 		addPacket(SM_MOVE.class, 0x37);
 		addPacket(SM_TRANSFORM.class, 0x3A);
 		addPacket(SM_DIALOG_WINDOW.class, 0x3C);
 		addPacket(SM_SELL_ITEM.class, 0x3E);
 		addPacket(SM_VIEW_PLAYER_DETAILS.class, 0x40);
 		addPacket(SM_WEATHER.class, 0x43);
 		addPacket(SM_PLAYER_STATE.class, 0x44);
 		addPacket(SM_LEVEL_UPDATE.class, 0x46);
 		addPacket(SM_KEY.class, 0x48);
 		addPacket(SM_SUMMON_PANEL_REMOVE.class, 0x49);
 		addPacket(SM_EXCHANGE_REQUEST.class, 0x4A);
 		addPacket(SM_EXCHANGE_ADD_ITEM.class, 0x4B);
 		addPacket(SM_EXCHANGE_ADD_KINAH.class, 0x4D);
 		addPacket(SM_EXCHANGE_CONFIRMATION.class, 0x4E);
 		addPacket(SM_EMOTION_LIST.class, 0x4F);
 		addPacket(SM_TARGET_UPDATE.class, 0x51);
 		addPacket(SM_PLASTIC_SURGERY.class, 0x53);
 		addPacket(SM_INFLUENCE_RATIO.class, 0x55);
 		addPacket(SM_NAME_CHANGE.class, 0x58);
 		addPacket(SM_SHOW_NPC_ON_MAP.class, 0x59);
 		addPacket(SM_GROUP_INFO.class, 0x5A);
 		addPacket(SM_GROUP_MEMBER_INFO.class, 0x5B);
 		addPacket(SM_QUIT_RESPONSE.class, 0x62);
 		addPacket(SM_PET.class, 0x65);
 		addPacket(SM_ITEM_COOLDOWN.class, 0x67);
 		addPacket(SM_UPDATE_NOTE.class, 0x68);
 		addPacket(SM_PLAY_MOVIE.class, 0x69);
 		addPacket(SM_LEGION_INFO.class, 0x6E);
 		addPacket(SM_LEGION_ADD_MEMBER.class, 0x6F);
 		addPacket(SM_LEGION_LEAVE_MEMBER.class, 0x70);
 		addPacket(SM_LEGION_UPDATE_MEMBER.class, 0x71);
 		addPacket(SM_LEGION_UPDATE_TITLE.class, 0x72);
 		addPacket(SM_LEGION_UPDATE_SELF_INTRO.class, 0x77);
 		addPacket(SM_RIFT_STATUS.class, 0x7A);
 		addPacket(SM_QUEST_LIST.class, 0x7B);
 		addPacket(SM_QUEST_ACCEPTED.class, 0x7C);
 		addPacket(SM_NEARBY_QUESTS.class, 0x7F);
 		addPacket(SM_PING_RESPONSE.class, 0x80);
 		addPacket(SM_CUBE_UPDATE.class, 0x82);
 		addPacket(SM_FRIEND_LIST.class, 0x84);
 		addPacket(SM_PRIVATE_STORE.class, 0x86);
 		addPacket(SM_GROUP_LOOT.class, 0x87);
 		addPacket(SM_ABYSS_RANK_UPDATE.class, 0x88);
 		addPacket(SM_MAY_LOGIN_INTO_GAME.class, 0x89);
 		addPacket(SM_PLAYER_ID.class, 0x8D);
 		addPacket(SM_PONG.class, 0x8E);
 		addPacket(SM_KISK_UPDATE.class, 0x90);
 		addPacket(SM_PRIVATE_STORE_NAME.class, 0x91);
 		addPacket(SM_BROKER_SERVICE.class, 0x92);
 		addPacket(SM_SUMMON_PANEL.class, 0x99);
 		addPacket(SM_SUMMON_OWNER_REMOVE.class, 0x9A);
 		addPacket(SM_SUMMON_UPDATE.class, 0x9B);
 		addPacket(SM_LEGION_MEMBERLIST.class, 0x9D);
 		addPacket(SM_LEGION_EDIT.class, 0x9E);
 		addPacket(SM_MAIL_SERVICE.class, 0xA1);
 		addPacket(SM_SUMMON_USESKILL.class, 0xA2);
 		addPacket(SM_WAREHOUSE_INFO.class, 0xA8);
 		addPacket(SM_WAREHOUSE_UPDATE.class, 0xA9);
 		addPacket(SM_DELETE_WAREHOUSE_ITEM.class, 0xAA);
 		addPacket(SM_UPDATE_WAREHOUSE_ITEM.class, 0xAB);
 		addPacket(SM_TITLE_LIST.class, 0xB0);
 		addPacket(SM_TITLE_SET.class, 0xB1);
 		addPacket(SM_TITLE_UPDATE.class, 0xB3);
 		addPacket(SM_CRAFT_ANIMATION.class, 0xB4);
 		addPacket(SM_CRAFT_UPDATE.class, 0xB5);
 		addPacket(SM_ASCENSION_MORPH.class, 0xB6);
 		addPacket(SM_ITEM_USAGE_ANIMATION.class, 0xB7);
 		addPacket(SM_CUSTOM_SETTINGS.class, 0xB8);
 		addPacket(SM_DUEL.class, 0xB9);
 		addPacket(SM_PET_MOVE.class, 0xBB);
 		addPacket(SM_QUESTIONNAIRE.class, 0xBF);
 		addPacket(SM_DIE.class, 0xC1);
 		addPacket(SM_RESURRECT.class, 0xC2);
 		addPacket(SM_FORCED_MOVE.class, 0xC3);
 		addPacket(SM_TELEPORT_MAP.class, 0xC4);
 		addPacket(SM_USE_OBJECT.class, 0xC5);
 		addPacket(SM_L2AUTH_LOGIN_CHECK.class, 0xC7);
 		addPacket(SM_CHARACTER_LIST.class, 0xC8);
 		addPacket(SM_CREATE_CHARACTER.class, 0xC9);
 		addPacket(SM_DELETE_CHARACTER.class, 0xCA);
 		addPacket(SM_RESTORE_CHARACTER.class, 0xCB);
 		addPacket(SM_TARGET_IMMOBILIZE.class, 0xCC);
 		addPacket(SM_LOOT_STATUS.class, 0xCD);
 		addPacket(SM_LOOT_ITEMLIST.class, 0xCE);
 		addPacket(SM_RECIPE_LIST.class, 0xCF);
 		addPacket(SM_MANTRA_EFFECT.class, 0xD0);
 		addPacket(SM_SIEGE_LOCATION_INFO.class, 0xD1);
 		addPacket(SM_PLAYER_SEARCH.class, 0xD3);
 		addPacket(SM_LEGION_SEND_EMBLEM.class, 0xD5);
 		addPacket(SM_LEGION_UPDATE_EMBLEM.class, 0xD7);
 		addPacket(SM_FRIEND_RESPONSE.class, 0xDE);
 		addPacket(SM_BLOCK_RESPONSE.class, 0xDF);
 		addPacket(SM_BLOCK_LIST.class, 0xE0);
 		addPacket(SM_FRIEND_NOTIFY.class, 0xE1);
 		addPacket(SM_CHANNEL_INFO.class, 0xE5);	
 		addPacket(SM_CHAT_INIT.class, 0xE6);
 		addPacket(SM_MACRO_LIST.class, 0xE7);
 		addPacket(SM_MACRO_RESULT.class, 0xE8);
 		addPacket(SM_NICKNAME_CHECK_RESPONSE.class, 0xE9);
 		addPacket(SM_SET_BIND_POINT.class, 0xEB);
 		addPacket(SM_RIFT_ANNOUNCE.class, 0xEC);
 		addPacket(SM_ABYSS_RANK.class, 0xED);
 		addPacket(SM_PETITION.class, 0xEF);
 		addPacket(SM_FRIEND_UPDATE.class, 0xF0);
 		addPacket(SM_LEARN_RECIPE.class, 0xF1);
 		addPacket(SM_RECIPE_DELETE.class, 0xF2);
 		addPacket(SM_FLY_TIME.class, 0xF4);
 		addPacket(SM_ALLIANCE_INFO.class, 0xF5);
 		addPacket(SM_ALLIANCE_MEMBER_INFO.class, 0xF6);
 		addPacket(SM_LEAVE_GROUP_MEMBER.class, 0xF7);
 		addPacket(SM_SHOW_BRAND.class, 0xF9);
 		addPacket(SM_ALLIANCE_READY_CHECK.class, 0xFA);
 		addPacket(SM_PRICES.class, 0xFC);
 		addPacket(SM_TRADELIST.class, 0xFD);
 		addPacket(SM_RECONNECT_KEY.class, 0xFF);
 		
 		
 		addPacket(SM_CUSTOM_PACKET.class, 99999); // fake packet
 
 	}
	
	public void addPacket(AbstractClientPacket<AionChannelHandler> packetPrototype, State... states)
	{
		packetPrototype.setOpCode((byte)(packetPrototype.getOpCode()-1));
		super.addPacket(packetPrototype, states);
	}
 }
