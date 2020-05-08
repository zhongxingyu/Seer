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
	private int mNumErrors;
	private StringBuilder mErrors;
 
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
				long now = System.nanoTime();
				
 				int numTargets = mTasks[mCurrTaskIdx].length;
 				StringBuilder targetString = new StringBuilder();
 				for (int i = 0; i < numTargets; i++) {
 					if (i != 0)
 						targetString.append(" ");
 					targetString.append(mTasks[mCurrTaskIdx][i]);
 				}

				mLog.task(mCurrTaskIdx + 1, mTasks[mCurrTaskIdx].length, targetString.toString(), mNumErrors, mErrors.toString(), now - mTaskStart);

 				selectTask((mCurrTaskIdx + 1) % mTasks.length);
 			}
 		} else {
			if (mNumErrors != 0)
				mErrors.append(" ");
			
			mErrors.append(selection);
			mNumErrors++;
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
		mNumErrors = 0;
		mErrors = new StringBuilder();
 		mCurrTaskIdx = taskIndex;
 		mCurrTask = new LinkedHashSet<String>(Arrays.asList(mTasks[mCurrTaskIdx]));
 	}
 }
