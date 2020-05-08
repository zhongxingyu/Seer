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
 
 import com.aionemu.gameserver.model.gameobjects.Npc;
 import com.aionemu.gameserver.model.gameobjects.player.Player;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
 import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
 import com.aionemu.gameserver.questEngine.model.QuestEnv;
 import com.aionemu.gameserver.questEngine.model.QuestState;
 import com.aionemu.gameserver.questEngine.model.QuestStatus;
 import com.aionemu.gameserver.services.InstanceService;
 import com.aionemu.gameserver.services.ItemService;
 import com.aionemu.gameserver.services.TeleportService;
 import com.aionemu.gameserver.services.ZoneService;
 import com.aionemu.gameserver.utils.PacketSendUtility;
 import com.aionemu.gameserver.utils.ThreadPoolManager;
 import com.aionemu.gameserver.world.World;
 import com.aionemu.gameserver.world.WorldMapInstance;
 import com.aionemu.gameserver.world.WorldMapType;
 import com.google.inject.Inject;
 
 /**
  * @author Atomics
  * 
  */
 public class _1020SealingTheAbyssGate extends QuestHandler
 {
 	@Inject
 	ItemService itemService;
 	@Inject
 	ZoneService zoneService;
 	@Inject
 	TeleportService teleportService;
 	@Inject
 	InstanceService instanceService;
 	@Inject
 	World world;
 	
 	private final static int	questId	= 1020;
 	private final static int[]	npcIds	= { 203098, 700141, 700142, 700551 };
 
 	
 	public _1020SealingTheAbyssGate()
 	{
 		super( questId );
 	}
 
 	@Override
 	public void register()
 	{
 		qe.addOnEnterWorld(questId);
 		qe.addQuestLvlUp(questId);
 		qe.addOnDie(questId);
 		for(int npcId : npcIds)
 			qe.setNpcQuestData( npcId ).addOnTalkEvent( questId );
 	}
 
 	@Override
 	public boolean onLvlUpEvent(QuestEnv env)
 	{
 		final Player player = env.getPlayer();
 		final QuestState qs = player.getQuestStateList().getQuestState( questId );
 		if( qs == null || qs.getStatus() != QuestStatus.LOCKED || player.getCommonData().getLevel() < 15 )
 			return false;
         int[] quests = { 1130, 1023, 1022, 1021, 1019, 1018, 1017, 1016, 1015, 1014, 1013, 1012, 1011};
         for (int id : quests)
         {
             QuestState qs2 = player.getQuestStateList().getQuestState(id);
             if (qs2 == null || qs2.getStatus() != QuestStatus.COMPLETE)
                 return false;
         }
 
 		qs.setStatus( QuestStatus.START );
 		updateQuestStatus( player, qs );
 		return true;
 	}
 	
 	@Override
 	public boolean onDialogEvent(QuestEnv env)
 	{
 		final Player player = env.getPlayer();
 		final QuestState qs = player.getQuestStateList().getQuestState(questId);
 		if(qs == null)
 			return false;
 		final int instanceId = player.getInstanceId();
 		final int var = qs.getQuestVarById( 0 );
 		int targetId = 0;
 		int itemCount;
 		if(env.getVisibleObject() instanceof Npc)
 			targetId = ((Npc) env.getVisibleObject()).getNpcId();
 
 		if( qs.getStatus() == QuestStatus.REWARD )
 		{
 			if( targetId == 203098 )
 					return defaultQuestEndDialog( env );
 		}
 		else if( qs.getStatus() != QuestStatus.START )
 			return false;
 		switch( targetId )
 		{
 			case 203098:
 				switch( env.getDialogId() )
 				{
 					case 25:
 						if(var == 0)
 							return sendQuestDialog( player, env.getVisibleObject().getObjectId(), 1011 );
 					case 10000:
 						if(var == 0)
 						{
 							qs.setQuestVarById( 0, var + 1 );
 							updateQuestStatus( player, qs );
 							return sendQuestDialog( player, env.getVisibleObject().getObjectId(), 0 );
 						}
 					default:
 						return false;
 				}
 			case 700141:
 				 if( var == 1 && player.getPlayerGroup() != null)
 				{					
 					final int targetObjectId = env.getVisibleObject().getObjectId();
 					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, 37, 0, targetObjectId), true);
 					ThreadPoolManager.getInstance().schedule(new Runnable()
 					{
 						@Override
 						public void run()
 						{
 							WorldMapInstance newInstance = instanceService.getNextAvailableInstance(310030000);
 							instanceService.registerPlayerWithInstance(newInstance, player);
 							teleportService.teleportTo(player, 310030000, newInstance.getInstanceId(),(float) 270.5,(float) 174.3,(float) 204.3, 0);
 							qs.setQuestVarById( 0, var + 1 );
 							updateQuestStatus( player, qs );
 						}
 					}, 3000);
 					return true;
 				}
 				else if( var == 3)
 				{					
 					itemCount = player.getInventory().getItemCountByItemId(182200024);
 					if (itemCount >= 1)
 					{
 						final int targetObjectId = env.getVisibleObject().getObjectId();
 						PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, 37, 0, targetObjectId), true);
 						ThreadPoolManager.getInstance().schedule(new Runnable()
 						{
 							@Override
 							public void run()
 							{
 								teleportService.teleportTo(player, WorldMapType.VERTERON.getId(), 2684.308f, 1068.7382f, 199.375f, 0);
 								qs.setQuestVarById( 0, var + 1 );
 								qs.setStatus(QuestStatus.REWARD);
 								updateQuestStatus( player, qs );
 							}
 						}, 3000);
 						return true;
 					}
 				}
 				return false;
 			case 700551:
 				if( var == 2 )
 				{
 					itemCount = player.getInventory().getItemCountByItemId(182200024);
 					if (itemCount >= 1)
 					{
 						final int targetObjectId = env.getVisibleObject().getObjectId();
 						PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, 37, 0, targetObjectId), true);
 						ThreadPoolManager.getInstance().schedule(new Runnable()
 						{
 							@Override
 							public void run()
 							{
 								PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 153));
 								qs.setQuestVarById( 0, var + 1 );
 								updateQuestStatus(player, qs);
 							}
 						}, 3000);
 						return true;
 					}
 				}
 			case 700142:
 				if( var == 2)
 				{				
 				
 				final int targetObjectId = env.getVisibleObject().getObjectId();
 				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, 37, 0, targetObjectId), true);
 				ThreadPoolManager.getInstance().schedule(new Runnable()
 				{
 					@Override
 					public void run()
 					{
 						PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, 38, 0, targetObjectId), true);
 						questService.addNewSpawn(310030000, instanceId, 210753, (float) 258.89917, (float) 237.20166, (float) 217.06035, (byte) 0, true);
 					}
 				}, 3000);
 				return true;
 					
 				}
 			default:
 				return false;
 		}
 	}
 
 	@Override
 	public boolean onDieEvent(QuestEnv env)
 	{
 		Player player = env.getPlayer();
 		QuestState qs = player.getQuestStateList().getQuestState(questId);
 		if(qs == null || qs.getStatus() != QuestStatus.START)
 			return false;
 		int var = qs.getQuestVars().getQuestVars();
 		if(var == 2 || var == 3)
 		{
 			qs.setQuestVar(1);
 			player.getInventory().removeFromBagByItemId(182200024, 1);
 			updateQuestStatus(player, qs);
 		}
 
 		return false;
 	}
 		
 	@Override
 	public boolean onEnterWorldEvent(QuestEnv env)
 	{
 		Player player = env.getPlayer();
 		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		
		if(qs.getStatus() == QuestStatus.START)
 		{
 			int var = qs.getQuestVars().getQuestVars();
 			if(var == 2 || var == 3)
 			{
 				if(player.getWorldId() != 310030000)
 				{
 					qs.setQuestVar(1);
 					player.getInventory().removeFromBagByItemId(182200024, 1);
 					updateQuestStatus(player, qs);
 				}
 			}
 		}
 		else if(qs.getStatus() == QuestStatus.LOCKED && player.getCommonData().getLevel() > 15 )
 		{
 			int[] quests = { 1130, 1023, 1022, 1021, 1019, 1018, 1017, 1016, 1015, 1014, 1013, 1012, 1011};
 			for (int id : quests)
 			{
 				QuestState qs2 = player.getQuestStateList().getQuestState(id);
 				if (qs2 == null || qs2.getStatus() != QuestStatus.COMPLETE)
 					return false;
 			}
 			qs.setStatus( QuestStatus.START );
 			updateQuestStatus( player, qs );
 			return true;
 		}
 		return false;
 	}
 }
