 package quest.beluslan;
 
 import com.aionemu.gameserver.model.gameobjects.Npc;
 import com.aionemu.gameserver.model.gameobjects.player.Player;
 import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
 import com.aionemu.gameserver.questEngine.model.QuestEnv;
 import com.aionemu.gameserver.questEngine.model.QuestState;
 import com.aionemu.gameserver.questEngine.model.QuestStatus;
 
 /**
  * @author gigi
  *
  */
 public class _2654SpyTheLastPersuasion extends QuestHandler
 {
    private final static int   questId = 2654;
    
    public _2654SpyTheLastPersuasion()
    {
       super(questId);
    }
    
    @Override
    public void register()
    {
       qe.setNpcQuestData(204775).addOnQuestStart(questId);
       qe.setNpcQuestData(204775).addOnTalkEvent(questId);
      qe.setNpcQuestData(204655).addOnTalkEvent(questId);
    }
    
    public boolean onLvlUpEvent(QuestEnv env)
    {
       final Player player = env.getPlayer();
       final QuestState qs = player.getQuestStateList().getQuestState(questId);
       final QuestState qs2 = player.getQuestStateList().getQuestState(2653);
       if(qs2 == null || qs2.getStatus() != QuestStatus.COMPLETE)
     	  return false;
       qs.setStatus(QuestStatus.START);
       updateQuestStatus(player, qs);
       return true;
    }
    
    @Override
    public boolean onDialogEvent(QuestEnv env)
    {
 	   final Player player = env.getPlayer();
 	   int targetId = 0;
 	   if(env.getVisibleObject() instanceof Npc)
 		   targetId = ((Npc) env.getVisibleObject()).getNpcId();
 	   final QuestState qs = player.getQuestStateList().getQuestState(questId);
 	   if(targetId == 204775)
 	   {
 		   if(qs == null)
 		   {
 			   if(env.getDialogId() == 25)
 				   return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
 			   else 
 				   return defaultQuestStartDialog(env);
 		   }
       }
      else if(targetId == 204655)
       {
           if(qs != null)
           {
              if(env.getDialogId() == 25 && qs.getStatus() == QuestStatus.START)
                 return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
              else if(env.getDialogId() == 1009)
              {
                 qs.setQuestVar(0);
                 qs.setStatus(QuestStatus.REWARD);
                 updateQuestStatus(player, qs);
                 return defaultQuestEndDialog(env);
              }
              else
                 return defaultQuestEndDialog(env);
           }
       }
 	   return false;
    }   
 }
