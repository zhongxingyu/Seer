 package org.geometerplus.fbreader.fbreader;
 
 import org.geometerplus.fbreader.fbreader.FBAction;
 import org.geometerplus.fbreader.fbreader.FBReaderApp;
 import org.geometerplus.fbreader.fbreader.ScrollingPreferences.AutoBrowseInterval;
 import org.geometerplus.fbreader.fbreader.ScrollingPreferences.AutoBrowseUnit;
 import org.geometerplus.zlibrary.ui.android.view.AnimationProvider;
 
 public class AutoBrowseAction extends FBAction {
 	private boolean actionOn = false;
 
 	AutoBrowseAction(FBReaderApp fbreader, boolean actionOn) {
 		super(fbreader);
 		this.actionOn = actionOn;
 		Instance = this;
 	}
 
 	public static AutoBrowseAction Instance;
 
 	@Override
 	public boolean isVisible() {
 		if (actionOn) {
 			if (thread == null) {
 				return true;
 			}
 		} else {
 			if (thread != null) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private static AutoBrowseThread thread;
 
 	public boolean isAutoBrowsing() {
 		return !(thread == null);
 	}
 
 	public boolean isSmoothing() {
 		return ScrollingPreferences.Instance().AutoBrowseUnitOption.getValue() == AutoBrowseUnit.Smooth
 				&& thread != null;
 	}
 
 	public int Speed;
 
 	@Override
 	protected void run(Object... params) {
 		if (thread == null) {
 			thread = new AutoBrowseThread();
 			AutoBrowseInterval intervalEnum = ScrollingPreferences.Instance().AutoBrowseIntervalOption
 					.getValue();
 			int interval = Integer.parseInt(intervalEnum.name()
 					.replace("S", "")) * 1000;
 			if (interval == 1000) {
 				Speed = -4;
 			} else if (interval == 2000) {
 				Speed = -3;
 			} else if (interval == 3000) {
 				Speed = -2;
 			} else if (interval == 4000) {
 				Speed = -1;
 			} else {
 				Speed = interval / 120;
 			}
 			FBReaderApp.Instance().addTimerTask(
 					thread,
 					interval <= 10000 ? interval / 2 : 5000,
 					ScrollingPreferences.Instance().AutoBrowseUnitOption
							.getValue() == AutoBrowseUnit.Smooth ? 500
 							: interval);
 		} else {
 			FBReaderApp.Instance().removeTimerTask(thread);
 			if (AnimationProvider.Instance != null) {
 				AnimationProvider.Instance.terminate();
 			}
 			thread = null;
 		}
 	}
 
 	static class AutoBrowseThread implements Runnable {
 		public void run() {
 			boolean result = FBReaderApp
 					.Instance()
 					.StartAutoBrowse(
 							ScrollingPreferences.Instance().AutoBrowseUnitOption
 									.getValue() == AutoBrowseUnit.Row ? FBView.ScrollingMode.SCROLL_LINES
 									: FBView.ScrollingMode.SCROLL_PERCENTAGE);
 			if (!result) {
 				FBReaderApp.Instance().StopAutoBrowse();
 			}
 		}
 	}
 }
