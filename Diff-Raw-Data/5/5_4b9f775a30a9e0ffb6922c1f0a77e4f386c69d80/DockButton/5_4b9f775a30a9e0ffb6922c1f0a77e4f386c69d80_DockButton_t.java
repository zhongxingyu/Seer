 package com.fruit.launcher;
 
 import com.fruit.launcher.LauncherSettings.Applications;
 import com.fruit.launcher.LauncherSettings.BaseLauncherColumns;
 import com.fruit.launcher.LauncherSettings.Favorites;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.Typeface;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 
 import java.net.URISyntaxException;
 
 public class DockButton extends ImageView implements DropTarget, DragSource,
 		LauncherMonitor.InfoCallback {
 
 	private static final String TAG = "DockButton";
 
 	public boolean mIsEmpty;
 	public boolean mIsHold;
 	public boolean mIsHome;
 
 	private Launcher mLauncher;
 
 	private ShortcutInfo mBackupDockButtonInfo;
 	private Paint mTrashPaint;
 
 	private Paint mPaint;
 
 	private Bitmap mCueBitmap;
 	private CueNumber mCueNumber;
 
 	public DockButton(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		// TODO Auto-generated constructor stub
 		mIsEmpty = true;
 		mIsHold = false;
 		mIsHome = false;
 		mLauncher = null;
 
 		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
 		mPaint.setColor(Color.WHITE);
 		mPaint.setTextSize(18);
 		mPaint.setTypeface(Typeface.SANS_SERIF);
 
 		mCueNumber = new CueNumber();
 		mCueNumber.mbNumber = false;
 		mCueNumber.mMonitorType = LauncherMonitor.MONITOR_NONE;
 	}
 
 	@Override
 	public void draw(Canvas canvas) {
 		super.draw(canvas);
 
 		if (mCueNumber.mbNumber) {
 			mCueNumber.drawCueNumber(canvas, mPaint, this.getWidth(), this.getHeight(),
 					mCueBitmap);
 		}
 	}
 
 	public void setDrawCueNumberState(boolean draw, int type) {
 		if (draw) {
 			if (mCueBitmap == null) {
 				BitmapDrawable drawable = (BitmapDrawable) getResources()
 						.getDrawable(R.drawable.ic_cue_bg);
 				mCueBitmap = drawable.getBitmap();
 			}
 			if (mLauncher != null) {
 				mLauncher.registerMonitor(type, this);
 			}
 		} else {
 			if (mLauncher != null) {
 				mLauncher.unregisterMonitor(type, this);
 			}
 		}
 
 		mCueNumber.mMonitorType = type;
 		mCueNumber.mbNumber = draw;
 	}
 
 	@Override
 	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
 			int yOffset, DragView dragView, Object dragInfo) {
 		// TODO Auto-generated method stub
 		final int itemType = ((ItemInfo) dragInfo).itemType;
 		// Only support ITEM_TYPE_APPLICATION and ITEM_TYPE_SHORTCUT
 		return (itemType == BaseLauncherColumns.ITEM_TYPE_APPLICATION
 				|| itemType == BaseLauncherColumns.ITEM_TYPE_SHORTCUT
 				|| itemType == Applications.APPS_TYPE_APP || itemType == Applications.APPS_TYPE_FOLDERAPP);
 	}
 
 	@Override
 	public void onDrop(DragSource source, int x, int y, int xOffset,
 			int yOffset, DragView dragView, Object dragInfo) {
 		Log.d(TAG, "drag sequence,dockbutton onDrop");
 
 		// TODO Auto-generated method stub
 		final int itemType = ((ItemInfo) dragInfo).itemType;
 		final int index = ((DockBar.LayoutParams) getLayoutParams()).index;
 
 		final Workspace workspace = mLauncher.getWorkspace();
 
 		CellLayout current = (CellLayout) workspace.getChildAt(workspace
 				.getCurrentScreen());
 
 		if (itemType == Applications.APPS_TYPE_APP
 				|| itemType == Applications.APPS_TYPE_FOLDERAPP) {
 			ApplicationInfoEx appInfo = (ApplicationInfoEx) dragInfo;
 
 			ShortcutInfo dockItemInfo = mLauncher.getLauncherModel()
 					.getShortcutInfo(mLauncher.getPackageManager(),
 							appInfo.intent, getContext());
 			dockItemInfo.setActivity(appInfo.intent.getComponent(),
 					Intent.FLAG_ACTIVITY_NEW_TASK
 							| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 			setImageBitmap(Utilities.createCompoundBitmapEx(
 					dockItemInfo.title.toString(),
 					dockItemInfo.getIcon(mLauncher.getIconCache())));
 
 			dockItemInfo.container = Favorites.CONTAINER_DOCKBAR;
 			dockItemInfo.screen = -1;
 			dockItemInfo.cellX = index;
 			dockItemInfo.cellY = -1;
 			if (mIsEmpty) {
 				// Insert a new record to db, and got a new item id
 				LauncherModel.addItemToDatabase(mLauncher, dockItemInfo,
 						Favorites.CONTAINER_DOCKBAR, dockItemInfo.screen,
 						dockItemInfo.cellX, dockItemInfo.cellY, false);
 				mIsEmpty = false;
 			} else {
 				// Use old dock item's id
 				dockItemInfo.id = ((ShortcutInfo) getTag()).id;
 				LauncherModel.updateItemInDatabase(mLauncher, dockItemInfo);
 			}
 			setTag(dockItemInfo);
 			return;
 		}
 
 		if (mIsEmpty) {
 			ShortcutInfo appInfo = (ShortcutInfo) dragInfo;
 
 			if (appInfo.container >= 0) {
 				if (source instanceof UserFolder
 						&& ((UserFolder) source).mInfo.id == appInfo.container) {
 					// Drag from user folder
 					mLauncher.removeItemFromFolder(appInfo);
 				}
 			}
 			setImageBitmap(Utilities.createCompoundBitmapEx(
 					appInfo.title.toString(),
 					appInfo.getIcon(mLauncher.getIconCache())));
 			mIsEmpty = false;
 			appInfo.cellX = index;
 			appInfo.cellY = -1;
 			appInfo.container = Favorites.CONTAINER_DOCKBAR;
 			appInfo.screen = -1;
 			setTag(appInfo);
 			LauncherModel.updateItemInDatabase(mLauncher, appInfo);
 			Log.d(TAG,
 					"dockbar, mIsEmpty,current has " + current.getChildCount()
 							+ " children");
 
 		} else {
 			ShortcutInfo deskItemInfo = (ShortcutInfo) dragInfo;
 			ShortcutInfo dockIteminfo = (ShortcutInfo) getTag();
 
 			if (source instanceof Workspace) {
 				deskItemInfo.cellX = workspace.getmDragInfo().cellX;
 				deskItemInfo.cellY = workspace.getmDragInfo().cellY;
 			}
 
 			if (deskItemInfo.container >= 0) {
 				if (source instanceof UserFolder
 						&& ((UserFolder) source).mInfo.id == deskItemInfo.container) {
 					// Drag from user folder
 					// Remove this code block to add userfolder icon can
 					// exchange with dockbar
 					/*
 					 * mLauncher.removeItemFromFolder(deskItemInfo);
 					 * LauncherModel.deleteItemFromDatabase(mContext,
 					 * dockIteminfo);
 					 * 
 					 * deskItemInfo.cellX = index; deskItemInfo.cellY = -1;
 					 * deskItemInfo.container = Favorites.CONTAINER_DOCKBAR;
 					 * deskItemInfo.screen = -1;
 					 * LauncherModel.updateItemInDatabase(mLauncher,
 					 * deskItemInfo);
 					 * setImageBitmap(Utilities.createCompoundBitmapEx
 					 * (deskItemInfo.title.toString(),
 					 * deskItemInfo.getIcon(mLauncher.getIconCache())));
 					 * setTag(deskItemInfo); return;
 					 */
 				}
 			}
 
 			// Switch position of two items
 			/*
 			 * if ((source instanceof Workspace) && current.isFull()) { int
 			 * newCell[] = new int[2]; int scrn = workspace.getCurrentScreen();
 			 * int number = current.findLastVacantCell(); if (number < 0) { scrn
 			 * = deskItemInfo.screen; CellLayout temp = (CellLayout)
 			 * workspace.getChildAt(scrn); number = temp.findLastVacantCell(); }
 			 * 
 			 * current.numberToCell(number, newCell);
 			 * 
 			 * dockIteminfo.cellX = newCell[0];//deskItemInfo.cellX;
 			 * dockIteminfo.cellY = newCell[1];//deskItemInfo.cellY;
 			 * dockIteminfo.screen = scrn;//deskItemInfo.screen; } else
 			 */{
 				dockIteminfo.cellX = deskItemInfo.cellX;
 				dockIteminfo.cellY = deskItemInfo.cellY;
                if(source instanceof DockButton)
                	dockIteminfo.screen = -1;
                else
                	dockIteminfo.screen = workspace.getOriLayout().getPageIndex();// deskItemInfo.screen;
 			}
 
 			dockIteminfo.container = deskItemInfo.container;
 			dockIteminfo.orderId = deskItemInfo.orderId;
 			LauncherModel.updateItemInDatabase(mLauncher, dockIteminfo);
 
 			deskItemInfo.cellX = index;
 			deskItemInfo.cellY = -1;
 			deskItemInfo.container = Favorites.CONTAINER_DOCKBAR;
 			deskItemInfo.screen = -1;
 			LauncherModel.updateItemInDatabase(mLauncher, deskItemInfo);
 			setImageBitmap(Utilities.createCompoundBitmapEx(
 					deskItemInfo.title.toString(),
 					deskItemInfo.getIcon(mLauncher.getIconCache())));
 			setTag(deskItemInfo);
 
 			if (dockIteminfo.screen == -1) {
 				// Switch dock button
 				final DockBar dockBar = (DockBar) getParent();
 				DockButton view = (DockButton) dockBar
 						.getChildAt(dockIteminfo.cellX);
 				// view.setImageBitmap(dockIteminfo.getIcon(mLauncher.getIconCache()));
 				view.setImageBitmap(Utilities.createCompoundBitmapEx(
 						dockIteminfo.title.toString(),
 						dockIteminfo.getIcon(mLauncher.getIconCache())));
 				view.setTag(dockIteminfo);
 				view.mIsEmpty = false;
 			} else if (dockIteminfo.container != Favorites.CONTAINER_DESKTOP) {
 				// Drag from Folder to Dock bar
 				FolderInfo folderInfo = ((Folder) source).mInfo;
 				CellLayout cellLayout = (CellLayout) workspace
 						.getChildAt(folderInfo.screen);
 				for (int i = 0; i < cellLayout.getChildCount(); i++) {
 					ItemInfo itemInfo = (ItemInfo) cellLayout.getChildAt(i)
 							.getTag();
 					if (itemInfo != null && folderInfo.id == itemInfo.id) {
 						((FolderIcon) cellLayout.getChildAt(i))
 								.addItemInfo(dockIteminfo);
 						// ((FolderIcon)
 						// cellLayout.getChildAt(i)).refreshFolderIcon();
 					}
 				}
 				// if (source instanceof UserFolder) {
 				// UserFolder uf = (UserFolder)source;
 				// uf.
 				// }
 
 			} else {
 				// Drag from workspace to Dock bar
 				final View shortcut = mLauncher
 						.createShortcut(
 								R.layout.application,
 								(ViewGroup) workspace.getChildAt(workspace
 										.getChildIndexByPageIndex(workspace
 												.getOriLayout().getPageIndex())/*
 																				 * workspace
 																				 * .
 																				 * getCurrentScreen
 																				 * (
 																				 * )
 																				 *//*
 																					 * dockIteminfo
 																					 * .
 																					 * screen
 																					 */),
 								dockIteminfo);
 				// Log.d(TAG,"dockbar, before addInScreen,current has "+
 				// ((CellLayout)workspace.getChildAt(dockIteminfo.screen)).getChildCount()+" children");
 				workspace.addInScreen(shortcut,
 						workspace.getChildIndexByPageIndex(workspace
 								.getOriLayout().getPageIndex())/*
 																 * workspace.
 																 * getCurrentScreen
 																 * ()
 																 *//*
 																	 * dockIteminfo.
 																	 * screen
 																	 */,
 						dockIteminfo.cellX, dockIteminfo.cellY, 1, 1, false);
 				// Log.d(TAG,"dockbar, after addInScreen,current has "+
 				// ((CellLayout)workspace.getChildAt(dockIteminfo.screen)).getChildCount()+" children");
 			}
 
 		}
 
 	}
 
 	@Override
 	public void onDropCompleted(View target, boolean success) {
 		Log.d(TAG, "drag sequence,dockbutton onDropCompleted");
 
 		// TODO Auto-generated method stub
 		if (!success) {
 			setImageBitmap(mBackupDockButtonInfo.getIcon(mLauncher
 					.getIconCache()));
 			mIsEmpty = false;
 			setTag(mBackupDockButtonInfo);
 			LauncherModel
 					.updateItemInDatabase(mLauncher, mBackupDockButtonInfo);
 		}
 		mBackupDockButtonInfo = null;
 	}
 
 	@Override
 	public void onDragEnter(DragSource source, int x, int y, int xOffset,
 			int yOffset, DragView dragView, Object dragInfo) {
 		Log.d(TAG, "drag sequence,dockbutton onDragEnter");
 
 		// TODO Auto-generated method stub
 		if (!acceptDrop(source, x, y, xOffset, yOffset, dragView, dragInfo)) {
 			return;
 		}
 		if (mIsHold && mLauncher != null && !mLauncher.isAllAppsVisible()) {
 			return;
 		}
 		dragView.setPaint(mTrashPaint);
 	}
 
 	@Override
 	public void onDragOver(DragSource source, int x, int y, int xOffset,
 			int yOffset, DragView dragView, Object dragInfo) {
 		Log.d(TAG, "drag sequence,dockbutton onDragOver");
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void onDragExit(DragSource source, int x, int y, int xOffset,
 			int yOffset, DragView dragView, Object dragInfo) {
 		Log.d(TAG, "drag sequence,dockbutton onDragExit");
 		// TODO Auto-generated method stub
 		Log.d(TAG, "dragview exit,width=" + dragView.getWidth() + ",height="
 				+ dragView.getHeight());
 		dragView.setPaint(null);
 	}
 
 	@Override
 	public Rect estimateDropLocation(DragSource source, int x, int y,
 			int xOffset, int yOffset, DragView dragView, Object dragInfo,
 			Rect recycle) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void setLauncher(Launcher launcher) {
 		mLauncher = launcher;
 	}
 
 	@Override
 	public void setDragController(DragController dragger) {
 
 	}
 
 	public void setPaint(Paint paint) {
 		mTrashPaint = paint;
 	}
 
 	public void setDockButtonInfo(ShortcutInfo info) {
 		mBackupDockButtonInfo = info;
 	}
 
 	public void setTag(ShortcutInfo info) {
 		// cancel previous state;
 		if (mTag != null && mTag instanceof ShortcutInfo) {
 			ShortcutInfo preInfo = (ShortcutInfo) mTag;
 			if (preInfo.intent != null
 					&& mCueNumber.getMonitorType(preInfo.intent) != LauncherMonitor.MONITOR_NONE) {
 				setDrawCueNumberState(false, mCueNumber.mMonitorType);
 			}
 		}
 
 		super.setTag(info);
 		// set new state
 		if (info != null && info.intent != null) {
 			int type = mCueNumber.getMonitorType(info.intent);
 			if (type != LauncherMonitor.MONITOR_NONE) {
 				setDrawCueNumberState(true, type);
 			}
 		}
 	}
 
 	@Override
 	public void onInfoCountChanged(int number) {
 		// TODO Auto-generated method stub
 		if (number <= 0) {
 			mCueNumber.mCueNum = null;
 			return;
 		} else if (number >= 100) {
 			mCueNumber.mCueNum = new String(CueNumber.CUE_MAX);
 		} else {
 			mCueNumber.mCueNum = String.valueOf(number);
 		}
 		invalidate();
 	}
 
 	// /* (non-Javadoc)
 	// * @see android.widget.ImageView#setImageBitmap(android.graphics.Bitmap)
 	// */
 	// @Override
 	// public void setImageBitmap(Bitmap bm) {
 	// // TODO Auto-generated method stub
 	// //Bitmap icon = Utilities.changeBitmap4Launcher(bm);
 	// super.setImageBitmap(bm);
 	// }
 
 }
