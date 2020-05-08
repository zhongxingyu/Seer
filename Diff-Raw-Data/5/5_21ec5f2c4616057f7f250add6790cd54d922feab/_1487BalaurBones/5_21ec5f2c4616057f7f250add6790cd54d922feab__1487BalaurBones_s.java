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
 package quest.eltnen;
 
 import com.aionemu.gameserver.model.gameobjects.Npc;
 import com.aionemu.gameserver.model.gameobjects.player.Player;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
 import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
 import com.aionemu.gameserver.questEngine.model.QuestEnv;
 import com.aionemu.gameserver.questEngine.model.QuestState;
 import com.aionemu.gameserver.questEngine.model.QuestStatus;
 import com.aionemu.gameserver.utils.PacketSendUtility;
 import com.aionemu.gameserver.utils.ThreadPoolManager;
 
 /**
 * @author Atomics
 */
 
 public class _1487BalaurBones extends QuestHandler
 {
 
    private final static int   questId   = 1487;
 
    public _1487BalaurBones()
    {
       super(questId);
    }
 
    @Override
    public void register()
    {
 
       qe.setNpcQuestData(798126).addOnQuestStart(questId);
       qe.setNpcQuestData(798126).addOnTalkEvent(questId);
       qe.setNpcQuestData(700313).addOnTalkEvent(questId);
       qe.setNpcQuestData(700314).addOnTalkEvent(questId);
       qe.setNpcQuestData(700315).addOnTalkEvent(questId);
    }
 
    @Override
    public boolean onDialogEvent(QuestEnv env)
    {
       final Player player = env.getPlayer();
       int targetId = 0;
       if(env.getVisibleObject() instanceof Npc)
          targetId = ((Npc) env.getVisibleObject()).getNpcId();
       QuestState qs = player.getQuestStateList().getQuestState(questId);
       if(targetId == 798126)
       {
          if(qs == null || qs.getStatus() == QuestStatus.NONE)
          {
             if(env.getDialogId() == 25)
                return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
             else
                return defaultQuestStartDialog(env);
          }
       
          else if (qs.getStatus() == QuestStatus.START)
          {
         	 	long itemCount;
         	 	long itemCount1;
         	 	long itemCount2;
 				if(env.getDialogId() == 25 && qs.getQuestVarById(0) == 0)
 				{
 					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
 				}
 				else if(env.getDialogId() == 33 && qs.getQuestVarById(0) == 0)
 				{
 					itemCount = player.getInventory().getItemCountByItemId(182201407);
 					itemCount1 = player.getInventory().getItemCountByItemId(182201408);
 					itemCount2 = player.getInventory().getItemCountByItemId(182201409);
 					if(itemCount > 0 && itemCount1 > 2 && itemCount2 > 1)
 					{
 						player.getInventory().removeFromBagByItemId(182201407, 1);
 						player.getInventory().removeFromBagByItemId(182201408, 3);
 						player.getInventory().removeFromBagByItemId(182201409, 2);
 						qs.setStatus(QuestStatus.REWARD);
 						updateQuestStatus(player, qs);
 						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
 					}
 					else
 					{
 						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2716);
 					}
 				}
 				else
 					return defaultQuestEndDialog(env);
          }
 
          else if( qs != null && qs.getStatus() == QuestStatus.REWARD)
          {
             return defaultQuestEndDialog(env);
          }
       }
       else if(targetId == 700313)
 		{
     	    long itemCount;
 			itemCount = player.getInventory().getItemCountByItemId(182201407);
			if(qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0 && itemCount < 1)
 			{
 				final int targetObjectId = env.getVisibleObject().getObjectId();
 				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, 37, 0, targetObjectId), true);
 				ThreadPoolManager.getInstance().schedule(new Runnable()
 				{
 					@Override
 					public void run()
 					{
 						PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, 38, 0, targetObjectId), true);
 					}
 		}, 3000);
 				return true;
 			}
 			else
 			{
 				return defaultQuestEndDialog(env);
 			}
 		}
 		else if(targetId == 700314)
 		{
 			long itemCount1;
 			itemCount1 = player.getInventory().getItemCountByItemId(182201408);
 			if(qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0 && itemCount1 < 3)
 			{
 				final int targetObjectId = env.getVisibleObject().getObjectId();
 				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, 37, 0, targetObjectId), true);
 				ThreadPoolManager.getInstance().schedule(new Runnable()
 				{
 					@Override
 					public void run()
 					{
 						PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, 38, 0, targetObjectId), true);
 					}
 				}, 3000);
 				return true;
 			}
 			else
 			{
 				return defaultQuestEndDialog(env);
 			}
 		}
 		else if(targetId == 700315)
 		{
 			long itemCount2;
 			itemCount2 = player.getInventory().getItemCountByItemId(182201409);
			if(qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0 && itemCount2 < 2)
 			{
 				final int targetObjectId = env.getVisibleObject().getObjectId();
 				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, 37, 0, targetObjectId), true);
 				ThreadPoolManager.getInstance().schedule(new Runnable()
 				{
 					@Override
 					public void run()
 					{
 						PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, 38, 0, targetObjectId), true);
 					}
 				}, 3000);
 				return true;
 			}
 			else
 			{
 				return defaultQuestEndDialog(env);
 			}
 		}
 		else
 		{
 		return defaultQuestEndDialog(env);
 		}
 		return false;
    }
 }
