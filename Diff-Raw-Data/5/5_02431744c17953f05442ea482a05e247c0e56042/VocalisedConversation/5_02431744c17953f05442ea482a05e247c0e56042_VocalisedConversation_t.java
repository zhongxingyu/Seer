 package me.tehbeard.vocalise.parser;
 
 import java.util.Map;
 
 import org.bukkit.conversations.*;
 import org.bukkit.plugin.Plugin;
 
 public class VocalisedConversation extends Conversation{
 
     public VocalisedConversation(Plugin plugin, Conversable forWhom,
             Prompt firstPrompt) {
         super(plugin, forWhom, firstPrompt);
         // TODO Auto-generated constructor stub
     }
     public VocalisedConversation(Plugin plugin, Conversable forWhom, Prompt firstPrompt, Map<Object, Object> initialSessionData) {
         super(plugin,forWhom,firstPrompt,initialSessionData);
     }
 
 
     public void outputNextPrompt() {
         if (currentPrompt == null) {
             abandon(new ConversationAbandonedEvent(this));
         } else {
            String promptText = currentPrompt.getPromptText(context);
            if(promptText != null){
                context.getForWhom().sendRawMessage(prefix.getPrefix(context) + promptText);
             }
             if (!currentPrompt.blocksForInput(context)) {
                 currentPrompt = currentPrompt.acceptInput(context, null);
                 outputNextPrompt();
             }
         }
     }
 }
