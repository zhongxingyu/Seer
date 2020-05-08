 package model;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 import java.util.regex.*;
 import java.util.StringTokenizer;
 
 import javax.swing.event.EventListenerList;
 
 import model.TaskTriplet.State;
 
 import view.Utils;
 import controller.TaskListener;
 
 public class TaskSpec {
 	private ArrayList<BntTask> bntTaskList;
 	private ArrayList<BmtTask> bmtTaskList;
 	private ArrayList<BttTask> bttTaskList;
 	private ArrayList<CttTask> cttTaskList;
 	private EventListenerList listOfTaskListeners = new EventListenerList();
 	private Logging logg;
 	private String taskListName = "TaskList";
 
 	private String removeSpaces(String str) {
 		StringTokenizer tokens = new StringTokenizer(str, " ", false);
 		String newStr = "";
 		while (tokens.hasMoreElements()) {
 			newStr += tokens.nextElement();
 		}
 		return newStr;
 	}
 
 	public TaskSpec(int num) {
 		bntTaskList = new ArrayList<BntTask>();
 		bmtTaskList = new ArrayList<BmtTask>();
 		bttTaskList = new ArrayList<BttTask>();
 		cttTaskList = new ArrayList<CttTask>();
 		logg = Logging.getInstance();
 	}
 
 	public String getTaskSpecString(CompetitionIdentifier compIdent) {
 		String s = new String();
 		s = s.concat(compIdent.name());
 		s = s.concat("<");
 		switch (compIdent) {
 		case BNT:
 			if (bntTaskList.size() > 0) {
 				Iterator<BntTask> itBnt = bntTaskList.iterator();
 				while (itBnt.hasNext()) {
 					s = s.concat(((Task) itBnt.next()).getString());
 				}
 			}
 			break;
 		case BMT:
 			if (bmtTaskList.size() > 0) {
 				Iterator<BmtTask> itBmt = bmtTaskList.iterator();
 				BmtTask first = new BmtTask();
 				if (itBmt.hasNext()) {
 					first = itBmt.next();
 					s = s.concat(BmtTask.getPlaceInitial());
 					s = s.concat(",");
 					s = s.concat(BmtTask.getPlaceSource());
 					s = s.concat(",");
 					s = s.concat(BmtTask.getPlaceDestination());
 					s = s.concat(",");
 					s = s.concat(first.getConfiguration());
 					s = s.concat("(");
 					s = s.concat(first.getObject());
 				}
 				BmtTask last = first;
 				while (itBmt.hasNext()) {
 					last = itBmt.next();
 					s = s.concat(",");
 					s = s.concat(last.getObject());
 				}
 				s = s.concat(")");
 				if (last.getPlaceInitial().equals("")) {
 					s = s.concat(",");
 					s = s.concat(BmtTask.getPlaceFinal());
 				}
 			}
 			break;
 		case BTT:
 			/*
 			 * Iterator<BttTask> itBtt = bttTaskList.iterator();
 			 * ArrayList<BttTask> initialList = new ArrayList<BttTask>();
 			 * ArrayList<BttTask> goalList = new ArrayList<BttTask>(); while
 			 * (itBtt.hasNext()) { BttTask btt = itBtt.next(); if
 			 * (btt.getSituation().equals("initial")) initialList.add(btt); else
 			 * goalList.add(btt); } Iterator<BttTask> itInitial =
 			 * initialList.iterator(); while (itInitial.hasNext()) { ; }
 			 */
 			break;
 		default:
 		}
 		s = s.concat(">");
 		return s;
 	}
 
 	public void addTask(CompetitionIdentifier compIdent, Object task) {
 		switch (compIdent) {
 		case BNT:
 			BntTask bntTask = (BntTask) task;
 			bntTaskList.add(bntTask);
 			logg.globalLogging(taskListName, bntTask.getString() + " no. "
 					+ bntTaskList.indexOf(bntTask) + " added");
 			notifyBntTaskSpecChanged(bntTask, bntTaskList.indexOf(bntTask),
 					bntTaskList);
 			break;
 		case BMT:
 			BmtTask bmtTask = (BmtTask) task;
 			bmtTaskList.add(bmtTask);
 			logg.globalLogging(taskListName, bmtTask.getString() + " no. "
 					+ bmtTaskList.indexOf(bmtTask) + " added");
 			notifyBmtTaskSpecChanged(bmtTask, bmtTaskList.indexOf(bmtTask),
 					bmtTaskList);
 			break;
 		case BTT:
 			BttTask bttTask = (BttTask) task;
 			bttTaskList.add(bttTask);
 			logg.globalLogging(taskListName, bttTask.getString() + " no. "
 					+ bttTaskList.indexOf(bttTask) + " added");
 			notifyBttTaskSpecChanged(bttTask, bttTaskList.indexOf(bttTask),
 					bttTaskList);
 			break;
 		default:
 			return;
 		}
 	}
 
 	public Task deleteTask(int pos, CompetitionIdentifier compIdent) {
 		switch (compIdent) {
 		case BNT:
 			BntTask bntTask = bntTaskList.remove(pos);
 			logg.globalLogging(taskListName, bntTask.getString() + " no. "
 					+ bntTaskList.indexOf(bntTask) + " deleted");
 			notifyBntTaskSpecChanged(bntTask, pos, bntTaskList);
 			return bntTask;
 		case BMT:
 			BmtTask bmtTask = bmtTaskList.remove(pos);
 			logg.globalLogging(taskListName, bmtTask.getString() + " no. "
 					+ bmtTaskList.indexOf(bmtTask) + " deleted");
 			notifyBmtTaskSpecChanged(bmtTask, pos, bmtTaskList);
 			return bmtTask;
 		case BTT:
 			BttTask bttTask = bttTaskList.remove(pos);
 			logg.globalLogging(taskListName, bttTask.getString() + " no. "
 					+ bttTaskList.indexOf(bttTask) + " deleted");
 			notifyBttTaskSpecChanged(bttTask, pos, bttTaskList);
 			return bttTask;
 		default:
 			return null;
 		}
 	}
 
 	public Task moveUp(int pos, CompetitionIdentifier compIdent) {
 
 		if (pos == 0) {
 			return null;
 		} else {
 			switch (compIdent) {
 			case BNT:
 				BntTask bntTask = bntTaskList.remove(pos);
 				bntTaskList.add(pos - 1, bntTask);
 				logg.globalLogging(taskListName, bntTask.getString() + " no. "
 						+ bntTaskList.indexOf(bntTask) + " moved up");
 				notifyBntTaskSpecChanged(bntTask, pos, bntTaskList);
 				return bntTask;
 			case BMT:
 				BmtTask bmtTask = bmtTaskList.remove(pos);
 				bmtTaskList.add(pos - 1, bmtTask);
 				logg.globalLogging(taskListName, bmtTask.getString() + " no. "
 						+ bmtTaskList.indexOf(bmtTask) + " moved up");
 				notifyBmtTaskSpecChanged(bmtTask, pos, bmtTaskList);
 				return bmtTask;
 			case BTT:
 				BttTask bttTask = bttTaskList.remove(pos);
 				bttTaskList.add(pos - 1, bttTask);
 				logg.globalLogging(taskListName, bttTask.getString() + " no. "
 						+ bttTaskList.indexOf(bttTask) + " moved up");
 				notifyBttTaskSpecChanged(bttTask, pos, bttTaskList);
 				return bttTask;
 			default:
 				return null;
 			}
 		}
 	}
 
 	public Task moveDown(int pos, CompetitionIdentifier compIdent) {
 		switch (compIdent) {
 		case BNT:
 			if (bntTaskList.size() == pos + 1)
 				return null;
 			BntTask bntTask = bntTaskList.remove(pos);
 			bntTaskList.add(pos + 1, bntTask);
 			logg.globalLogging(taskListName, bntTask.getString() + " no. "
 					+ bntTaskList.indexOf(bntTask) + " moved down");
 			notifyBntTaskSpecChanged(bntTask, pos, bntTaskList);
 			return bntTask;
 		case BMT:
 			if (bmtTaskList.size() == pos + 1)
 				return null;
 			BmtTask bmtTask = bmtTaskList.remove(pos);
 			bmtTaskList.add(pos + 1, bmtTask);
 			logg.globalLogging(taskListName, bmtTask.getString() + " no. "
 					+ bmtTaskList.indexOf(bmtTask) + " moved down");
 			notifyBmtTaskSpecChanged(bmtTask, pos, bmtTaskList);
 			return bmtTask;
 		case BTT:
 			if (bttTaskList.size() == pos + 1)
 				return null;
 			BttTask bttTask = bttTaskList.remove(pos);
 			bttTaskList.add(pos + 1, bttTask);
 			logg.globalLogging(taskListName, bttTask.getString() + " no. "
 					+ bttTaskList.indexOf(bttTask) + " moved down");
 			notifyBttTaskSpecChanged(bttTask, pos, bttTaskList);
 			return bttTask;
 		default:
 			return null;
 		}
 	}
 
 	public Task updateTask(int pos, Task task, CompetitionIdentifier compIdent) {
 		switch (compIdent) {
 		case BNT:
 			BntTask bntTask = bntTaskList.set(pos, (BntTask) task);
 			logg.globalLogging(taskListName,
 					bntTask.getString() + " no. " + bntTaskList.indexOf(task)
 							+ " updated to " + bntTask.getString());
 			notifyBntTaskSpecChanged(bntTask, bntTaskList.indexOf(bntTask),
 					bntTaskList);
 			return bntTask;
 		case BMT:
 			BmtTask bmtTask = bmtTaskList.set(pos, (BmtTask) task);
 			logg.globalLogging(taskListName,
 					bmtTask.getString() + " no. " + bmtTaskList.indexOf(task)
 							+ " updated to " + bmtTask.getString());
 			notifyBmtTaskSpecChanged(bmtTask, bmtTaskList.indexOf(bmtTask),
 					bmtTaskList);
 			return bmtTask;
 		case BTT:
 			BttTask bttTask = bttTaskList.set(pos, (BttTask) task);
 			logg.globalLogging(taskListName,
 					bttTask.getString() + " no. " + bttTaskList.indexOf(task)
 							+ " updated to " + bttTask.getString());
 			notifyBttTaskSpecChanged(bttTask, bttTaskList.indexOf(bttTask),
 					bttTaskList);
 			return bttTask;
 		default:
 			return null;
 		}
 	}
 
 	/*
 	 * public List<TaskTriplet> getTaskTripletList() { return null; //
 	 * taskTripletList; }
 	 */
 
 	public Task getTaskAtIndex(int index, CompetitionIdentifier compIdent) {
 		switch (compIdent) {
 		case BNT:
 			return bntTaskList.get(index);
 		case BMT:
 			return bmtTaskList.get(index);
 		case BTT:
 			return bttTaskList.get(index);
 		default:
 			return null;
 		}
 	}
 
 	public void addTripletListener(TaskListener tL) {
 		listOfTaskListeners.add(TaskListener.class, tL);
 	}
 
 	public void removeTripletListener(TaskListener tL) {
 		listOfTaskListeners.remove(TaskListener.class, tL);
 	}
 
 	private void notifyBntTaskSpecChanged(BntTask bntTask, int pos,
 			ArrayList<BntTask> bntTaskList2) {
 		Object[] listeners = listOfTaskListeners.getListenerList();
 		// Each listener occupies two elements - the first is the listener class
 		// and the second is the listener instance
 		for (int i = 0; i < listeners.length; i += 2) {
 			if (listeners[i] == TaskListener.class) {
 				((TaskListener) listeners[i + 1]).bntTaskSpecChanged(bntTask,
 						pos, bntTaskList);
 			}
 		}
 	}
 
 	private void notifyBmtTaskSpecChanged(BmtTask bmtTask, int pos,
 			ArrayList<BmtTask> bntTaskList2) {
 		Object[] listeners = listOfTaskListeners.getListenerList();
 		// Each listener occupies two elements - the first is the listener class
 		// and the second is the listener instance
 		for (int i = 0; i < listeners.length; i += 2) {
 			if (listeners[i] == TaskListener.class) {
 				((TaskListener) listeners[i + 1]).bmtTaskSpecChanged(bmtTask,
 						pos, bmtTaskList);
 			}
 		}
 	}
 
 	private void notifyBttTaskSpecChanged(BttTask bttTask, int pos,
 			ArrayList<BttTask> bttTaskList) {
 		Object[] listeners = listOfTaskListeners.getListenerList();
 		// Each listener occupies two elements - the first is the listener class
 		// and the second is the listener instance
 		for (int i = 0; i < listeners.length; i += 2) {
 			if (listeners[i] == TaskListener.class) {
 				((TaskListener) listeners[i + 1]).bttTaskSpecChanged(bttTask,
 						pos, bttTaskList);
 			}
 		}
 	}
 
 	public boolean saveTaskSpec(File file) {
 
 		file = Utils.correctFile(file);
 		try {
 			FileWriter fstream = new FileWriter(file);
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write(getTaskSpecString(CompetitionIdentifier.BNT));
 			out.write("\n");
 			out.write(getTaskSpecString(CompetitionIdentifier.BMT));
 			out.write("\n");
 			out.write(getTaskSpecString(CompetitionIdentifier.BTT));
 			out.write("\n");
 			out.close();
 			logg.globalLogging("TODO",
 					"saved actual task specification in >" + file.getName()
 							+ "<");
 		} catch (Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			logg.globalLogging("TODO", "saving failed! >");
 			return false;
 		}
 
 		return true;
 	}
 
 	public boolean openTaskSpec(File file) {
 
 		try {
 			FileInputStream fstream = new FileInputStream(file);
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String strLine;
 			while ((strLine = br.readLine()) != null) {
 				if (!parseTaskSpecString(strLine))
 					return false;
 			}
 			in.close();
 		} catch (Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			return false;
 		}
 
 		return true;
 	}
 
 	public boolean parseTaskSpecString(String tSpecStr) {
 		tSpecStr = removeSpaces(tSpecStr);
 		// System.out.println("After removing spaces " + tSpecStr);
 		String competition = tSpecStr.substring(0, 2);
		//if (competition.equals(CompetitionIdentifier.BNT.name()))
		bntTaskList = new ArrayList<BntTask>();
 		try {
 			Pattern pat = Pattern.compile(TaskTriplet.getValidTripletPattern());
 			Matcher m = pat.matcher(tSpecStr);
 			do {
 				BntTask nextTask = new BntTask();
 				if (m.find()) {
 					nextTask.setPlace(m.group(1));
 					nextTask.setOrientation(m.group(2));
 					nextTask.setPause(m.group(3));
 					bntTaskList.add(nextTask);
 					logg.globalLogging(taskListName, nextTask.getString()
 							+ " no. " + bntTaskList.indexOf(nextTask)
 							+ " added");
 					notifyBntTaskSpecChanged(nextTask,
 							bntTaskList.indexOf(nextTask), bntTaskList);
 				}
 			} while (!m.hitEnd());
 		} catch (Exception e) {
 			System.out.println("Caught exception in parseTaskSpec. Error: "
 					+ e.getMessage());
 			return false;
 		}
 		return true;
 	}
 
 	public TaskTriplet setTripletState(int tripletIndex, int column) {
 		/*
 		 * TaskTriplet tT = taskTripletList.get(tripletIndex); State newState;
 		 * if (column == 1) newState = State.PASSED; else newState =
 		 * State.FAILED; if (tT.getState() == newState) tT.setState(State.INIT);
 		 * else tT.setState(newState); logg.LoggingFile(taskListName,
 		 * tT.getTaskTripletString() + " no. " + taskTripletList.indexOf(tT) +
 		 * " new state: " + tT.getState()); notifyTaskSpecChanged(new
 		 * TripletEvent(tT, taskTripletList.indexOf(tT), taskTripletList));
 		 */
 		return null; // tT;
 	}
 
 	public void resetStates() {
 		/*
 		 * for (TaskTriplet tT : taskTripletList) { tT.setState(State.INIT);
 		 * logg.LoggingFile(taskListName, tT.getTaskTripletString() + " no. " +
 		 * taskTripletList.indexOf(tT) + " new state: INIT"); }
 		 */
 	}
 
 	public ArrayList<BntTask> getBntTaskList() {
 		return bntTaskList;
 	}
 
 	public ArrayList<BmtTask> getBmtTaskList() {
 		return bmtTaskList;
 	}
 
 	public ArrayList<BttTask> getBttTaskList() {
 		return bttTaskList;
 	}
 }
