 package com.theminequest.common.impl.v1parser;
 
 import java.util.List;
 import java.util.Map;
 
 import com.theminequest.api.Managers;
 import com.theminequest.api.quest.QuestDetails;
 import com.theminequest.api.targeted.QuestTarget;
 import com.theminequest.common.quest.v1.V1Parser;
 
 public class TargetHandler implements V1Parser {
 	
 	@Override
 	public void handle(QuestDetails details, List<String> arguments) {
 		int number = Integer.parseInt(arguments.remove(0));
		String name = arguments.remove(1);
 		String parameters = "";
 		for (String s : arguments)
 			parameters += s + ":";
 		if (parameters.length() != 0)
 			parameters = parameters.substring(0, parameters.length() - 1);
 		Map<Integer, QuestTarget> targets = details.getProperty(QuestDetails.QUEST_TARGET);
 		QuestTarget target = Managers.getTargetManager().construct(name, number, parameters.split(":"));
 		if (target != null)
 			targets.put(number, target);
 		else
 			Managers.logf("[Quest] Retrieving target %s for detail %s failed!", number, details.getName());
 	}
 	
 }
