 package net.skycraftmc.SkyQuest;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.skycraftmc.SkyQuest.action.ActionType;
 import net.skycraftmc.SkyQuest.objective.ObjectiveType;
 import net.skycraftmc.SkyQuest.util.nbt.TagBase;
 import net.skycraftmc.SkyQuest.util.nbt.TagBase.TagType;
 import net.skycraftmc.SkyQuest.util.nbt.TagByte;
 import net.skycraftmc.SkyQuest.util.nbt.TagCompound;
 import net.skycraftmc.SkyQuest.util.nbt.TagInt;
 import net.skycraftmc.SkyQuest.util.nbt.TagList;
 import net.skycraftmc.SkyQuest.util.nbt.TagString;
 
 public class FileManager 
 {
 	public void saveQuest(File file, Quest q) throws IOException
 	{
 		TagCompound tag = new TagCompound();
 		tag.setTag("name", new TagString("name", q.getName()));
 		Stage fs = q.getFirstStage();
 		TagCompound fstage = saveStage(fs);
 		fstage.setName("firststage");
 		fstage.setTag("id", new TagString("id", fs.getID()));
 		tag.setTag("firststage", fstage);
 		TagCompound stages = new TagCompound("stages");
 		for(Stage s:q.getStages())stages.setTag(s.getID(), saveStage(s));
 		tag.setTag("stages", stages);
 		TagCompound objs = new TagCompound("objectives");
 		for(Objective o:q.getObjectives())objs.setTag(o.getID(), saveObjective(o));
 		tag.setTag("objectives", objs);
 		TagList desc = new TagList("description");
 		for(String s:q.getDescription())desc.add(new TagString("", s));
 		tag.setTag("description", desc);
 		tag.setTag("iconid", new TagInt("iconid", q.getItemIconId()));
 		tag.setTag("firstassigned", new TagByte("firstassigned", (byte)(q.isFirstAssigned() ? 1 : 0)));
 		File f = new File(file, q.getID() + ".dat");
 		DataOutputStream dos = new DataOutputStream(new FileOutputStream(f));
 		tag.saveTag(dos);
 		dos.flush();
 		dos.close();
 	}
 	public Quest loadQuest(File f, String id) throws IOException
 	{
 		if(!f.getName().endsWith(".dat"))throw new IllegalArgumentException("File must have a .dat extension");
 		DataInputStream dis = new DataInputStream(new FileInputStream(f));
 		TagBase btag = TagBase.loadTag(dis);
 		dis.close();
 		if(!(btag instanceof TagCompound))return null;
 		TagCompound tag = (TagCompound)btag;
 		TagBase tbname = tag.getTag("name");
 		if(!(tbname instanceof TagString))return null;
 		String name = ((TagString)tbname).data;
 		TagBase tbfstage = tag.getTag("firststage");
 		if(!(tbfstage instanceof TagCompound))return null;
 		TagCompound tfstage = (TagCompound)tbfstage;
 		TagBase tbfstageid = tfstage.getTag("id");
 		if(!(tbfstageid instanceof TagString))return null;
 		Stage s = loadStage(((TagString)tbfstageid).data, tfstage);
 		if(s == null)return null;
 		Quest q = new Quest(id, name, s);
 		TagBase tbstages = tag.getTag("stages");
 		if(tbstages instanceof TagCompound)
 		{
 			TagCompound stages = (TagCompound)tbstages;
 			for(TagBase tb:stages.getTags())
 			{
 				if(tb instanceof TagCompound)
 				{
 					Stage a = loadStage((TagCompound)tb);
					if(a != null)
					{
						if(!a.getID().equals(s.getID()))q.addStage(a);
					}
 				}
 			}
 		}
 		TagBase tbobj = tag.getTag("objectives");
 		if(tbobj instanceof TagCompound)
 		{
 			TagCompound obj = (TagCompound)tbobj;
 			for(TagBase b: obj.getTags())
 			{
 				if(!(b instanceof TagCompound))continue;
 				TagCompound c = (TagCompound)b;
 				Objective o = loadObjective(c);
 				if(o != null)q.addObjective(o);
 			}
 		}
 		TagBase tbitemico = tag.getTag("iconid");
 		if(tbitemico instanceof TagInt)q.setItemIconId(((TagInt)tbitemico).data);
 		TagBase tbdesc = tag.getTag("description");
 		ArrayList<String>desc = new ArrayList<String>();
 		if(tbdesc instanceof TagList)
 		{
 			TagList tdesc = (TagList)tbdesc;
 			for(TagBase b:tdesc.get())
 			{
 				if(b instanceof TagString)desc.add(((TagString)b).data);
 			}
 		}
 		q.setDescription(desc);
 		TagBase tbfirstassigned = tag.getTag("firstassigned");
 		if(tbfirstassigned instanceof TagByte)
 		{
 			TagByte tfa = (TagByte)tbfirstassigned;
 			q.setFirstAssigned(tfa.data == 1 ? true : false);
 		}
 		return q;
 	}
 	public void savePlayerData(File f, PlayerQuestLog log)throws IOException
 	{
 		TagCompound tag = new TagCompound();
 		TagCompound assigned = new TagCompound("assigned");
 		for(Quest q:log.getAssigned())
 		{
 			QuestData qd = log.getProgress(q);
 			TagCompound qt = new TagCompound(q.getID());
 			qt.setTag("stage", new TagString("stage", qd.getStage()));
 			TagList unassigned = new TagList("unassigned", TagType.STRING);
 			for(String s:qd.unassigned)unassigned.add(new TagString("", s));
 			qt.setTag("unassigned", unassigned);
 			TagCompound progress = new TagCompound("progress");
 			for(Map.Entry<String, String>l:qd.objprog.entrySet())
 				progress.setTag(l.getKey(), new TagString(l.getKey(), l.getValue()));
 			qt.setTag("progress", progress);
 			assigned.setTag(q.getID(), qt);
 		}
 		TagCompound compl = new TagCompound("completed");
 		for(CompletedQuestData cqd:log.getCompleted())
 		{
 			Quest q = cqd.getQuest();
 			TagCompound qt = new TagCompound(q.getID());
 			TagList unassigned = new TagList("unassigned", TagType.STRING);
 			for(String s:cqd.unassigned)unassigned.add(new TagString("", s));
 			qt.setTag("unassigned", unassigned);
 			TagCompound progress = new TagCompound("progress");
 			for(Map.Entry<String, String>l:cqd.objprog.entrySet())
 				progress.setTag(l.getKey(), new TagString(l.getKey(), l.getValue()));
 			qt.setTag("progress", progress);
 			compl.setTag(q.getID(), qt);
 		}
 		tag.setTag("assigned", assigned);
 		tag.setTag("completed", compl);
 		DataOutputStream dos = new DataOutputStream(new FileOutputStream(f));
 		tag.saveTag(dos);
 		dos.flush();
 		dos.close();
 	}
 	public PlayerQuestLog loadPlayerData(File file) throws IOException
 	{
 		String fname = file.getName();
 		if(!fname.endsWith(".dat"))throw new IllegalArgumentException("File must end with .dat");
 		String pname = fname.substring(0, fname.length() - 4);
 		DataInputStream dis = new DataInputStream(new FileInputStream(file));
 		TagBase btag = TagBase.loadTag(dis);
 		dis.close();
 		if(!(btag instanceof TagCompound))return null;
 		TagCompound tag = (TagCompound)btag;
 		TagBase bassigned = tag.getTag("assigned");
 		if(!(bassigned instanceof TagCompound))return null;
 		TagCompound assigned = (TagCompound)bassigned;
 		TagBase bcompl = tag.getTag("completed");
 		if(!(bcompl instanceof TagCompound))return null;
 		TagCompound compl = (TagCompound)bcompl;
 		PlayerQuestLog pql = new PlayerQuestLog(pname);
 		for(TagBase b:assigned.getTags())
 		{
 			if(!(b instanceof TagCompound))continue;
 			TagCompound c = (TagCompound)b;
 			Quest q = QuestManager.getInstance().getQuest(c.getName());
 			if(q == null)continue;
 			TagBase bstage = c.getTag("stage");
 			if(!(bstage instanceof TagString))continue;
 			String stage = ((TagString)bstage).data;
 			if(q.getStage(stage) == null)continue;
 			TagBase buoa = c.getTag("unassigned");
 			if(!(buoa instanceof TagList))continue;
 			ArrayList<String>unassigned = new ArrayList<String>();
 			TagList uoa = (TagList)buoa;
 			for(TagBase b2:uoa.get())
 			{
 				if(!(b2 instanceof TagString))continue;
 				String oid = ((TagString)b2).data;
 				if(q.getObjective(oid) != null)unassigned.add(oid);
 			}
 			TagBase bprog = c.getTag("progress");
 			if(!(bprog instanceof TagCompound))continue;
 			HashMap<String, String>progress = new HashMap<String, String>();
 			TagCompound prog = (TagCompound)bprog;
 			for(TagBase b2:prog.getTags())
 			{
 				if(!(b2 instanceof TagString))continue;
 				String oid = b2.getName();
 				if(q.getObjective(oid) != null)progress.put(oid, ((TagString)b2).data);
 			}
 			QuestData qd = new QuestData(pql, q);
 			qd.stage = stage;
 			qd.unassigned = unassigned;
 			qd.objprog = progress;
 			pql.assigned.add(qd);
 		}
 		for(TagBase b:compl.getTags())
 		{
 			if(!(b instanceof TagCompound))continue;
 			TagCompound c = (TagCompound)b;
 			Quest q = QuestManager.getInstance().getQuest(c.getName());
 			if(q == null)continue;
 			TagBase buoa = c.getTag("unassigned");
 			if(!(buoa instanceof TagList))continue;
 			ArrayList<String>unassigned = new ArrayList<String>();
 			TagList uoa = (TagList)buoa;
 			for(TagBase b2:uoa.get())
 			{
 				if(!(b2 instanceof TagString))continue;
 				String oid = ((TagString)b2).data;
 				if(q.getObjective(oid) != null)unassigned.add(oid);
 			}
 			TagBase bprog = c.getTag("progress");
 			if(!(bprog instanceof TagCompound))continue;
 			HashMap<String, String>progress = new HashMap<String, String>();
 			TagCompound prog = (TagCompound)bprog;
 			for(TagBase b2:prog.getTags())
 			{
 				if(!(b2 instanceof TagString))continue;
 				String oid = b2.getName();
 				if(q.getObjective(oid) != null)progress.put(oid, ((TagString)b2).data);
 			}
 			int times = 1;
 			TagBase btimes = c.getTag("times");
 			if(btimes instanceof TagInt)times = ((TagInt)btimes).data;
 			if(times < 1)times = 1;
 			CompletedQuestData cqd = new CompletedQuestData(q, unassigned, progress);
 			pql.completed.put(cqd, times);
 		}
 		return pql;
 	}
 	public void loadData(File file, QuestManager qm)
 	{
 		if(!file.exists())return;
 		File players = new File(file, "Players");
 		if(!players.exists())players.mkdir();
 		File quests = new File(file, "Quests");
 		if(!quests.exists())quests.mkdir();
 		for(File f:quests.listFiles())
 		{
 			if(!f.getName().endsWith(".dat"))continue;
 			String fname = f.getName();
 			String id = fname.substring(0, fname.length() - 4);
 			try
 			{
 				Quest q = loadQuest(f, id);
 				if(q != null)QuestManager.getInstance().addQuest(q);
 			}
 			catch(EOFException eofe)
 			{
				eofe.printStackTrace();
 			}
 			catch(IOException ioe)
 			{
 				ioe.printStackTrace();
 			}
 		}
 		for(File f:players.listFiles())
 		{
 			if(!f.getName().endsWith(".dat"))continue;
 			try
 			{
 				PlayerQuestLog log = loadPlayerData(f);
 				if(log != null)QuestManager.getInstance().addQuestLog(log);
 			}
 			catch(EOFException eofe)
 			{
 				
 			}
 			catch(IOException ioe)
 			{
 				ioe.printStackTrace();
 			}
 		}
 	}
 	public void saveData(File file, QuestManager qm) throws IOException
 	{
 		if(!file.exists())file.mkdir();
 		File players = new File(file, "Players");
 		if(!players.exists())players.mkdir();
 		File quests = new File(file, "Quests");
 		if(!quests.exists())quests.mkdir();
 		for(Quest q:QuestManager.getInstance().getQuests())
 		{
 			try
 			{
 				saveQuest(quests, q);
 			}
 			catch(IOException ioe)
 			{
 				ioe.printStackTrace();
 			}
 		}
 		savePlayerData(players);
 	}
 	public void savePlayerData(File file) throws IOException
 	{
 		for(PlayerQuestLog l:QuestManager.getInstance().getQuestLogs())
 		{
 			File f = new File(file, l.getPlayer() + ".dat");
 			savePlayerData(f, l);
 		}
 	}
 	private Stage loadStage(TagCompound tag)
 	{
 		return loadStage(tag.getName(), tag);
 	}
 	private Stage loadStage(String id, TagCompound tag)
 	{
 		Stage st = new Stage(id);
 		TagBase tl = tag.getTag("actions");
 		if(tl instanceof TagList)
 		{
 			for(TagBase tb : ((TagList)tl).get())
 			{
 				if(tb instanceof TagCompound)
 				{
 					QuestAction qa = loadAction((TagCompound)tb);
 					if(qa != null)st.addAction(qa);
 				}
 			}
 		}
 		return st;
 	}
 	private Objective loadObjective(TagCompound tag)
 	{
 		try
 		{
 			TagBase tbname = tag.getTag("name");
 			if(!(tbname instanceof TagString))return null;
 			TagBase tbtarget = tag.getTag("target");
 			if(!(tbtarget instanceof TagString))return null;
 			TagBase tbtypes = tag.getTag("type");
 			if(!(tbtypes instanceof TagString))return null;
 			ObjectiveType t = QuestManager.getInstance().getRegisteredObjectiveType(((TagString)tbtypes).data);
 			if(t == null)return null;
 			Objective o = new Objective(tag.getName(), ((TagString)tbname).data, t, ((TagString)tbtarget).data);
 			TagBase tbicoid = tag.getTag("iconid");
 			if(tbicoid instanceof TagInt)o.setItemIconId(((TagInt)tbicoid).data);
 			TagBase tbopt = tag.getTag("optional");
 			if(tbopt instanceof TagByte)o.setOptional(((TagByte)tbopt).data == 1);
 			TagBase tbvis = tag.getTag("visible");
 			if(tbvis instanceof TagByte)o.setVisible(((TagByte)tbvis).data == 1);
 			TagBase rewards = tag.getTag("rewards");
 			if(rewards instanceof TagList)
 			{
 				for(TagBase b:((TagList)rewards).get())
 				{
 					if(b instanceof TagCompound)
 					{
 						QuestAction a = loadAction((TagCompound)b);
 						if(a != null)o.addReward(a);
 					}
 				}
 			}
 			return o;
 		}
 		catch(IllegalArgumentException iae)
 		{
 			return null;
 		}
 	}
 	private TagCompound saveObjective(Objective o)
 	{
 		TagCompound c = new TagCompound(o.getID());
 		c.setTag("name", new TagString("name", o.getName()));
 		c.setTag("target", new TagString("target", o.getTarget()));
 		c.setTag("type", new TagString("type", o.getType().getClass().getName()));
 		c.setTag("iconid", new TagInt("iconid", o.getItemIconId()));
 		c.setTag("optional", new TagByte("optional", (byte)(o.isOptional() ? 1 : 0)));
 		c.setTag("visible", new TagByte("visible", (byte)(o.isVisible() ? 1 : 0)));
 		TagList rewards = new TagList("rewards", TagType.COMPOUND);
 		for(QuestAction a:o.getRewards())rewards.add(saveAction(a));
 		c.setTag("rewards", rewards);
 		return c;
 	}
 	private QuestAction loadAction(TagCompound tag)
 	{
 		try
 		{
 			TagBase ttype = tag.getTag("type");
 			if(!(ttype instanceof TagString))return null;
 			ActionType t = QuestManager.getInstance().getRegisteredActionType(((TagString)ttype).data);
 			if(t == null)return null;
 			TagBase tdata = tag.getTag("data");
 			if(!(tdata instanceof TagString))return null;
 			return new QuestAction(t, ((TagString)tdata).data);
 		}
 		catch(IllegalArgumentException iae)
 		{
 			return null;
 		}
 	}
 	private TagCompound saveStage(Stage s)
 	{
 		TagCompound c = new TagCompound(s.getID());
 		TagList act = new TagList("actions", TagType.COMPOUND);
 		for(QuestAction a:s.getActions())act.add(saveAction(a));
 		c.setTag("actions", act);
 		return c;
 	}
 	private TagCompound saveAction(QuestAction a)
 	{
 		TagCompound c = new TagCompound();
 		c.setTag("type", new TagString("type", a.getType().getClass().getName()));
 		c.setTag("data", new TagString("data", a.getAction()));
 		return c;
 	}
 }
