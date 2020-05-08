 package ch.almana.android.stechkarte.utils;
 
 import android.app.Activity;
 
 public class ProgressWrapperActivity implements IProgressWrapper {
 
 	private Activity progressAct;
 	private int max;
 	private CharSequence origTitle;
 	private int inc;
 
 	public ProgressWrapperActivity(Activity act) throws Exception {
 		this.progressAct = act;
 		// progressAct.getWindow().re
 		this.origTitle = progressAct.getTitle();
 		//
 		// progressAct.setProgressBarVisibility(true);
 		// progressAct.setProgress(500);
 	}
 
 	@Override
 	public void dismiss() {
 		progressAct.setTitle(origTitle);
 		progressAct.setProgressBarVisibility(false);
 	}
 
 	@Override
 	public void incrementEvery(int i) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void setMax(int i) {
		this.inc = 10000 / i;
 	}
 
 	@Override
 	public void setProgress(int i) {
 		// TODO Auto-generated method stub
 		progressAct.setProgress(i * inc);
 	}
 
 	@Override
 	public void setTitle(String title) {
 		progressAct.setTitle(title);
 	}
 
 	@Override
 	public void show() {
 		progressAct.setProgressBarVisibility(true);
 	}
 
 }
