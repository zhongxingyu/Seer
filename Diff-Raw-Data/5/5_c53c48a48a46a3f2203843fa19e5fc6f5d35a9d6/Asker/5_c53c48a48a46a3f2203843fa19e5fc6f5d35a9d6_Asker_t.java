 package org.akquinet.audit;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 import org.akquinet.audit.ui.UserCommunicator;
 
 public class Asker
 {
 	private Set<YesNoQuestion> _questions;
 	private final UserCommunicator _uc = UserCommunicator.getDefault();
 	private ResourceBundle _labels;
 	
 	public Asker()
 	{
 		_labels = ResourceBundle.getBundle("global", _uc.getLocale());
 	}
 	
 	public Asker(Collection<YesNoQuestion> questions)
 	{
 		this();
 		_questions = new HashSet<YesNoQuestion>(questions);
 	}
 
 	public Asker(YesNoQuestion[] questions)
 	{
 		this();
 		_questions = new HashSet<YesNoQuestion>();
 		for (YesNoQuestion quest : questions)
 		{
 			_questions.add(quest);
 		}
 	}
 	
 	public boolean askQuestions()
 	{
 		List<YesNoQuestion> sorted = sortQuestions(_questions);
 		boolean overallAns = true;
 		
 		try
 		{
 			for (YesNoQuestion quest : sorted)
 			{
 				boolean askAgain = true;
 				boolean ans = false;
				_uc.markReport();
 				while(askAgain)
 				{
 					_uc.resetReport();
 					_uc.markReport();
 					ans = quest.answer();
 					askAgain = !ans && _uc.askYesNoQuestion( _labels.getString("S11") , false);
 					
 					if(!askAgain)
 					{
 						_uc.waitForUserToContinue();
 					}
 				}
				overallAns &= ans;
 				
 				if(quest.isCritical() && !ans)
 				{
 					_uc.println( _labels.getString("S12") );
 					_uc.waitForUserToContinue();
 					break;
 				}
 			}
 		}
 		catch (IOException e)
 		{
 			_uc.reportError(e.getMessage());
 			overallAns = false;
 		}
 		
 		return overallAns;
 	}
 	
 	private List<YesNoQuestion> sortQuestions(Set<YesNoQuestion> questions)
 	{
 		List<YesNoQuestion> ret = new LinkedList<YesNoQuestion>();
 		List<Set<YesNoQuestion>> blocks = new LinkedList<Set<YesNoQuestion>>();
 
 		splitIntoBlocks(questions, blocks);
 		
 		sortBlocks(ret, blocks);
 		
 		return ret;
 	}
 
 	private void sortBlocks(List<YesNoQuestion> ret,
 			List<Set<YesNoQuestion>> blocks)
 	{
 		for (Set<YesNoQuestion> block : blocks)
 		{
 			while(!block.isEmpty())
 			{
 				YesNoQuestion first = getNumberFirst(block);
 				
 				if(first.getRequirements().length > 0 && ! satisfied(first.getRequirements(), ret) )
 				{
 					List<YesNoQuestion> sList = satisfy(first, ret, block);
 					
 					block.removeAll(sList);
 					ret.addAll(sList);
 				}
 				else
 				{
 					block.remove(first);
 					ret.add(first);
 				}
 			}
 		}
 	}
 
 	private void splitIntoBlocks(Set<YesNoQuestion> questions,
 			List<Set<YesNoQuestion>> blocks)
 	{
 		List<YesNoQuestion> tmp = new LinkedList<YesNoQuestion>(questions);
 		sortByBlockNumber(tmp);
 		
 		blocks.add(new HashSet<YesNoQuestion>());
 		int blockNumber = tmp.get(0).getBlockNumber();
 		while(!tmp.isEmpty())
 		{
 			YesNoQuestion first = tmp.get(0);
 			int b = first.getBlockNumber();
 			if(b != blockNumber)
 			{
 				blockNumber = b;
 				blocks.add(new HashSet<YesNoQuestion>());
 			}
 
 			blocks.get(blocks.size()-1).add( first );
 			tmp.remove(0);
 		}
 	}
 
 	private void sortByBlockNumber(List<YesNoQuestion> tmp)
 	{
 		java.util.Collections.sort(tmp, new Comparator<YesNoQuestion>()
 		{
 			public int compare(YesNoQuestion q1, YesNoQuestion q2)
 			{
 				if(q1.getBlockNumber() < q2.getBlockNumber())
 				{
 					return -1;
 				}
 				else if(q1.getBlockNumber() > q2.getBlockNumber())
 				{
 					return 1;
 				}
 				else //if(q1.getBlockNumber() == q2.getBlockNumber())
 				{
 					return 0;
 				}
 			}
 		});
 	}
 
 	private List<YesNoQuestion> satisfy(YesNoQuestion first,
 										List<YesNoQuestion> alreadyHandled,
 										Set<YesNoQuestion> block)
 	{
 		List<YesNoQuestion> alreadyHandledHere = new LinkedList<YesNoQuestion>();
 		alreadyHandledHere.addAll(alreadyHandled);
 		
 		List<YesNoQuestion> ret = new LinkedList<YesNoQuestion>();
 
 		List<YesNoQuestion> requirements = generateRequirementsList(first.getRequirements(), alreadyHandled, block);
 		
 		for (YesNoQuestion quest : requirements)
 		{
 			if(!alreadyHandledHere.contains(quest))
 			{
 				List<YesNoQuestion> s = satisfy(quest, alreadyHandledHere, block);
 				alreadyHandledHere.addAll(s);
 				ret.addAll(s);
 			}
 		}
 		
 		ret.add(first);
 		return ret;
 	}
 
 	private List<YesNoQuestion> generateRequirementsList(String[] requirements,
 														 List<YesNoQuestion> alreadyHandled,
 														 Set<YesNoQuestion> block)
 	{
 		Arrays.sort(requirements);
 		
 		Set<YesNoQuestion> tmp = new HashSet<YesNoQuestion>(alreadyHandled);
 		tmp.addAll(block);
 		
 		List<YesNoQuestion> ret = new LinkedList<YesNoQuestion>();
 		
 		for (String questName : requirements)
 		{
 			for (YesNoQuestion quest : tmp)
 			{
 				if(quest.getID().equals(questName))
 				{
 					ret.add(quest);
 					break;
 				}
 			}
 		}
 		return ret;
 	}
 
 	private boolean satisfied(String[] requirements, List<YesNoQuestion> alreadyHandled)
 	{
 		boolean ret = true;
 		
 		for (String req : requirements)
 		{
 			boolean satisfied = false;
 			for (YesNoQuestion quest : alreadyHandled)
 			{
 				satisfied |= quest.getID().equals(req);
 				
 				if(satisfied == false)	//speed improvement
 				{
 					return false;
 				}
 			}
 			ret &= satisfied;
 		}
 		
 		return ret;
 	}
 
 	private YesNoQuestion getNumberFirst(Set<YesNoQuestion> block)
 	{
 		YesNoQuestion ret = java.util.Collections.min(block, new Comparator<YesNoQuestion>()
 		{
 			public int compare(YesNoQuestion q1, YesNoQuestion q2)
 			{
 				if(q1.getNumber() < q2.getNumber())
 				{
 					return -1;
 				}
 				else if(q1.getNumber() > q2.getNumber())
 				{
 					return 1;
 				}
 				else //if(q1.getNumber() == q2.getNumber())
 				{
 					return 0;
 				}
 			}
 		});
 		
 		return ret;
 	}
 }
