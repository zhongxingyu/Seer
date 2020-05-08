 package usask.hci.fastdraw;
 
 import java.util.Arrays;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 public class StudyController {
 	private final String[][] mTasks;
 	private int mCurrTaskIdx;
 	private Set<String> mCurrTask;
 	private StudyLogger mLog;
 	private long mTaskStart;
 
 	public StudyController(StudyLogger logger) {
 		mLog = logger;
 
 		mTasks = new String[][] {
 			{"Normal", "Red", "Line"},
 			{"Thin", "Yellow"},
 			{"Circle", "Save"}
 		};
 
 		selectTask(0);
 	}
 	
 	public void handleSelected(String selection) {
 		if (mCurrTask.contains(selection)) {
 			mCurrTask.remove(selection);
 
 			if (mCurrTask.isEmpty()) {
 				int numTargets = mTasks[mCurrTaskIdx].length;
 				StringBuilder targetString = new StringBuilder();
 				for (int i = 0; i < numTargets; i++) {
 					if (i != 0)
 						targetString.append(" ");
 					targetString.append(mTasks[mCurrTaskIdx][i]);
 				}
				
				long now = System.nanoTime();
				mLog.task(mCurrTaskIdx + 1, mTasks[mCurrTaskIdx].length, targetString.toString(), now - mTaskStart);
				
 				selectTask((mCurrTaskIdx + 1) % mTasks.length);
 			}
 		} else {
			mLog.log("Selection error: " + selection);
 		}
 	}
 	
 	public String getPrompt() {
 		String progress = "(" + (mCurrTaskIdx + 1) + "/" + mTasks.length + ")";
     	StringBuilder title = new StringBuilder(progress + " Please select: ");
     	
     	for (String toSelect : mCurrTask) {
     		title.append(toSelect);
     		title.append(", ");
     	}
     	
     	return title.substring(0, title.length() - 2);
 	}
 	
 	private void selectTask(int taskIndex) {
 		mTaskStart = System.nanoTime();
 		mCurrTaskIdx = taskIndex;
 		mCurrTask = new LinkedHashSet<String>(Arrays.asList(mTasks[mCurrTaskIdx]));
 	}
 }
