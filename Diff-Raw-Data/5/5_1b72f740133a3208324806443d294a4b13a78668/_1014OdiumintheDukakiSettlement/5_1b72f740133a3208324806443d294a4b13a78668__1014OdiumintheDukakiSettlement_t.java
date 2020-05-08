 /*
  * This file is part of aion-unique <aion-unique.org>.
  *
  * aion-unique is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * aion-unique is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
  */
 package quest.verteron;
 
 import com.aionemu.gameserver.model.gameobjects.Item;
 import com.aionemu.gameserver.model.gameobjects.Npc;
 import com.aionemu.gameserver.model.gameobjects.player.Player;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
 import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
 import com.aionemu.gameserver.questEngine.model.QuestEnv;
 import com.aionemu.gameserver.questEngine.model.QuestState;
 import com.aionemu.gameserver.questEngine.model.QuestStatus;
 import com.aionemu.gameserver.services.ZoneService;
 import com.aionemu.gameserver.utils.PacketSendUtility;
 import com.aionemu.gameserver.utils.ThreadPoolManager;
 import com.aionemu.gameserver.world.zone.ZoneName;
 import com.google.inject.Inject;
 
 /**
  * @author Rhys2002
  * 
  */
 public class _1014OdiumintheDukakiSettlement extends QuestHandler
 {
 	@Inject
 	ZoneService zoneService;
 	
 	private final static int	questId	= 1014;
 	private final static int[]	npc_ids	= { 203129, 730020, 203098, 700090 };
 	private final static int[]	mob_ids	= { 210145, 210174, 210739 };
 	
 	public _1014OdiumintheDukakiSettlement()
 	{
 		super(questId);
 	}
 
 	@Override
 	public void register()
 	{
 		qe.addQuestLvlUp(questId);
 		qe.setQuestItemIds(182200012).add(questId);			
 		for(int mob_id : mob_ids)
 			qe.setNpcQuestData(mob_id).addOnKillEvent(questId);			
 		for(int npc_id : npc_ids)
 			qe.setNpcQuestData(npc_id).addOnTalkEvent(questId);	
 	}
 
 	@Override
 	public boolean onLvlUpEvent(QuestEnv env)
 	{
 		final Player player = env.getPlayer();
 		final QuestState qs = player.getQuestStateList().getQuestState(questId);
 		if(qs == null || player.getCommonData().getLevel() < 14 || qs.getStatus() != QuestStatus.LOCKED)
 			return false;
 		qs.setStatus(QuestStatus.START);
 		updateQuestStatus(player, qs);
 		return true;
 	}
 
 	@Override
 	public boolean onDialogEvent(QuestEnv env)
 	{
 		final Player player = env.getPlayer();
 		final QuestState qs = player.getQuestStateList().getQuestState(questId);
 		if(qs == null)
 			return false;
 
 		int var = qs.getQuestVarById(0);
 		int targetId = 0;
 		if(env.getVisibleObject() instanceof Npc)
 			targetId = ((Npc) env.getVisibleObject()).getNpcId();
 
 		if(qs.getStatus() == QuestStatus.REWARD)
 		{
 			if(targetId == 203098)
 					return defaultQuestEndDialog(env);
 		}
 		else if(qs.getStatus() != QuestStatus.START)
 		{
 			return false;
 		}
 		if(targetId == 203129)
 		{
 			switch(env.getDialogId())
 			{
 				case 25:
 					if(var == 0)
 						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
 					else if(var == 10)
 						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
 					else if(var == 14)
 						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);						
 					return false;
 
 				case 1013:
 					if(var == 0)
 					{		
 						PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 26));
 						return false;
 					}
 					
 				case 10000:
 					if(var == 0)
 					{
 						qs.setQuestVarById(0, var + 1);
 						updateQuestStatus(player, qs);
 						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
 						return true;
 					}
 					
 				case 10001:
 					if(var == 10)
 					{
 						qs.setQuestVarById(0, var + 1);
 						updateQuestStatus(player, qs);
 						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
 						return true;
 					}
 
 				case 10002:
 					if(var == 14)
 					{
 						qs.setStatus(QuestStatus.REWARD);
 						updateQuestStatus(player, qs);
 						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
 						return true;
 					}
 					
 					return false;
 			}
 		}
 		else if(targetId == 730020)
 		{
 			switch(env.getDialogId())
 			{
 				case 25:
 					if(var == 1)
 						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
 					return false;
 
 				case 10001:
 					if(var == 1)
 					{
 						qs.setQuestVarById(0, var + 1);
 						updateQuestStatus(player, qs);
 						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
 						return true;
 					}
 					return false;
 			}
 		}
 		else if(targetId == 700090)
 		{
 			if (var == 11 && env.getDialogId() == -1)
 			{
 				final int targetObjectId = env.getVisibleObject().getObjectId();
 				
 				PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 1));
 				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, 37, 0, targetObjectId), true);
 				ThreadPoolManager.getInstance().schedule(new Runnable(){
 					@Override
 					public void run()
 					{
 						if(!player.isTargeting(targetObjectId))
 							return;
 						if(player.getInventory().getItemCountByItemId(182200011) == 0)
 							return;
 						PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 0));
 						PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, 38, 0, targetObjectId), true);
 						questService.addNewSpawn(210030000, 1, 210739, (float) 757.7, (float) 2477.2, (float) 217.4, (byte) 0, true);					
 					}
 				}, 3000);
 			}
 		}		
 			return false;
 	}
 	
 
 	@Override
 	public boolean onKillEvent(QuestEnv env)
 	{
 		Player player = env.getPlayer();
 		QuestState qs = player.getQuestStateList().getQuestState(questId);
 		if(qs == null)
 			return false;
 
 		int targetId = 0;
 		if(env.getVisibleObject() instanceof Npc)
 			targetId = ((Npc) env.getVisibleObject()).getNpcId();
 
 		if(qs.getStatus() != QuestStatus.START)
 			return false;
 		if(targetId == 210145 && qs.getQuestVarById(0) < 10)
 		{
 				qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
 				updateQuestStatus(player, qs);
 		}
 		return false;
 	}		
 
 	@Override
 	public boolean onItemUseEvent(QuestEnv env, Item item)
 	{
 		final Player player = env.getPlayer();
 		final int id = item.getItemTemplate().getTemplateId();
 		final int itemObjId = item.getObjectId();
 		final QuestState qs = player.getQuestStateList().getQuestState(questId);
 		
 		if(id != 182200012 || qs.getQuestVarById(0) != 11)
 			return false;
 		if(!zoneService.isInsideZone(player, ZoneName.ODIUM_REFINING_CAULDRON))
 			return false;
 
 		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 3000, 0, 0), true);
 		ThreadPoolManager.getInstance().schedule(new Runnable(){
 			@Override
 			public void run()
 			{
				PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 172));
 				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);
 			    player.getInventory().removeFromBagByItemId(182200012, 1);
 				player.getInventory().removeFromBagByItemId(182200011, 1);
 				qs.setQuestVarById(0, 14);
 				updateQuestStatus(player, qs);
 			}
		}, 3000);	
 		return true;
 	}
 }
