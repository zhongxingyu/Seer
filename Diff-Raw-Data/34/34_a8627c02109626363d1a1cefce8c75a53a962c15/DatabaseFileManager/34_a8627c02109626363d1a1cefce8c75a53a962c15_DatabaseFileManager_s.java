 package com.zotca.vbc.dbhistory.core;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.StreamCorruptedException;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.Locale;
 
 import com.zotca.vbc.dbhistory.ProgressDialogFragment;
 import com.zotca.vbc.dbhistory.R;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.support.v4.app.FragmentActivity;
 
 public class DatabaseFileManager {
 
 	public static class DialogModifier {
 		
 		protected ProgressDialogFragment mDialog;
 		protected Handler mHandler;
 		protected FragmentActivity mActivity;
 		
 		public DialogModifier(ProgressDialogFragment d, FragmentActivity activity) {
 			mDialog = d;
 			mHandler = new Handler();
 			mActivity = activity;
 		}
 		
 		public Handler getHandler() {
 			return mHandler;
 		}
 		
 		public void setTitle(final int resid) {
 			mHandler.post(new Runnable() {
 
 				@Override
 				public void run() {
 					mDialog.setTitle(resid);
 				}
 				
 			});
 		}
 		public void setTitle(final String title) {
 			mHandler.post(new Runnable() {
 
 				@Override
 				public void run() {
 					mDialog.setTitle(title);
 				}
 				
 			});
 		}
 		public void setMessage(final int resid) {
 			mHandler.post(new Runnable() {
 
 				@Override
 				public void run() {
 					mDialog.setMessage(mActivity.getResources().getString(resid));
 				}
 				
 			});
 		}
 		public void setMessage(final String message) {
 			mHandler.post(new Runnable() {
 
 				@Override
 				public void run() {
 					mDialog.setMessage(message);
 				}
 				
 			});
 		}
 	}
 
 	public static interface PostProcessHandler {
 		void onPostProcess(DatabaseFileManager manager);
 	}
 	
 	private static DatabaseFileManager mObject = null;
 	private static boolean mProcessing = false;
 	
 	public static DatabaseFileManager getManager(
 			final FragmentActivity activity,
 			final PostProcessHandler runAfter)
 	{
 		if (mProcessing == true)
 		{
 			return null;
 		}
 		if (mObject == null)
 		{
 			final ProgressDialogFragment df = new ProgressDialogFragment();
 			Bundle args = new Bundle();
 			df.setArguments(args);
 			final DialogModifier modifier = new DialogModifier(df, activity);
 			final Handler handler = modifier.getHandler();
 			Thread thread = new Thread(new Runnable() {
 				@Override
 				public void run() {
 					mObject = new DatabaseFileManager(activity.getFilesDir(), modifier);
 					handler.post(new Runnable() {
 
 						@Override
 						public void run() {
 							runAfter.onPostProcess(mObject);
 							df.dismissAllowingStateLoss();
 						}
 						
 					});
 				}
 			});
 			df.show(activity.getSupportFragmentManager(), "");
 			thread.start();
 			return null;
 		}
 		runAfter.onPostProcess(mObject);
 		return mObject;
 	}
 	
 	
 	private LinkedList<Long> mDeltaChain;
 	private Hashtable<Long, DatabaseDelta> mDeltaTable;
 	private CardDatabase mHead;
 	private DialogModifier mDF;
 	
 	public DatabaseFileManager(File f, DialogModifier df) {
 		mDF = df;
 		mDF.setTitle(R.string.progress_init_db);
 		mDF.setMessage(R.string.progress_db_datecheck);
 		File extstorage = Environment.getExternalStorageDirectory();
 		File db = new File(extstorage,
 				"Android/data/com.square_enix.million_kr/files/save/database/master_card");
 		
 		File head = new File(f, "HEAD");
 		long pHeadDelta;
 		boolean madeNewHead = true;
 		ObjectInputStream ois;
 		try {
 			ois = new ObjectInputStream(new FileInputStream(head));
 			pHeadDelta = ois.readLong();
 			if (pHeadDelta != db.lastModified())
 			{
 				mHead = (CardDatabase) ois.readObject();
 			}
 			else
 			{
 				ois.close();
 				generateChain(f, pHeadDelta);
 				return;
 			}
 			ois.close();
 			madeNewHead = false;
 		} catch (FileNotFoundException e) { // don't have HEAD, make new DB
 			pHeadDelta = makeNewHead(f, null, df);
 		} catch (StreamCorruptedException e) {
 			e.printStackTrace();
 			pHeadDelta = makeNewHead(f, null, df);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		} catch (ClassNotFoundException e) {
 			pHeadDelta = makeNewHead(f, null, df);
 		}
 		
 		if (!madeNewHead)
 		{
 			CardDatabase snapshot = makeSnapshot(df);
 			df.setTitle(R.string.progress_process_db);
 			df.setMessage(R.string.progress_db_diff);
 			DatabaseDelta delta = snapshot.makeDelta(mHead);
 			if (delta != null) // modified!
 			{
 				pHeadDelta = makeDelta(f, pHeadDelta, delta, snapshot, df);
 				mHead = snapshot;
 			}
 		}
 		// link all
 		generateChain(f, pHeadDelta);
 	}
 	
 	private void generateChain(File f, long pHead) {
 		mDF.setTitle(R.string.progress_loading_db);
 		mDF.setMessage(R.string.progress_db_chain);
 		mDeltaChain = new LinkedList<Long>();
 		mDeltaTable = new Hashtable<Long, DatabaseDelta>();
 		while (pHead != 0)
 		{
 			mDeltaChain.add(pHead);
 			File fDelta = new File(f,
 					String.format(Locale.getDefault(), "%d", pHead));
 			try {
 				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fDelta));
 				long oldHead = pHead;
 				pHead = ois.readLong();
 				Object o = ois.readObject();
 				ois.close();
 				mDeltaTable.put(oldHead, (DatabaseDelta) o);
 			} catch (StreamCorruptedException e) {
 				e.printStackTrace();
 				return;
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 				return;
 			} catch (IOException e) {
 				e.printStackTrace();
 				return;
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 				return;
 			}
 		}
 	}
 	
 	private static long makeNewHead(File f, CardDatabase snapshot, DialogModifier df) {
 		if (snapshot == null) snapshot = makeSnapshot(df);
 		df.setTitle(R.string.progress_process_db);
 		df.setMessage(R.string.progress_db_diff);
 		DatabaseDelta delta = snapshot.makeDelta(null);
 		return makeDelta(f, 0, delta, snapshot, df);
 	}
 	
	public LinkedList<Long> getChain() {
		return mDeltaChain;
	}
	public DatabaseDelta getDelta(long pDelta) {
		return mDeltaTable.get(pDelta);
	}
	
 	private static long makeDelta(File f, long pOldHead,
 			DatabaseDelta delta, CardDatabase snapshot, DialogModifier df) {
 		df.setTitle(R.string.progress_process_db);
 		df.setMessage(R.string.progress_db_savediff);
 		long deltaTimestamp = delta.getCreatedAt().getTime();
 		try {
 			File headDelta = new File(f,
 					String.format(Locale.getDefault(), "%d", deltaTimestamp));
 			ObjectOutputStream delta_oos = new ObjectOutputStream(new FileOutputStream(headDelta));
 			delta_oos.writeLong(pOldHead);
 			delta_oos.writeObject(delta);
 			delta_oos.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return 0;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return 0;
 		}
 		
 		df.setMessage(R.string.progress_db_savesnapshot);
 		try {
 			File head = new File(f, "HEAD");
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(head));
 			oos.writeLong(deltaTimestamp);
 			oos.writeObject(snapshot);
 			oos.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return 0;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return 0;
 		}
 		return deltaTimestamp;
 	}
 	
 	public static CardDatabase makeSnapshot() {
 		File extstorage = Environment.getExternalStorageDirectory();
 		File db = new File(extstorage,
 				"Android/data/com.square_enix.million_kr/files/save/database/master_card");
 		FileInputStream fis;
 		try {
 			fis = new FileInputStream(db);
 		} catch (FileNotFoundException e) { // DB not found; MA not installed
 			return null;
 		}
 		CardDatabase ret = null;
 		try {
 			ret = new CardDatabase(fis, new Date(db.lastModified())); // read DB and make snapshot
 			fis.close();
 		} catch (IOException e) { // why error?
 			e.printStackTrace();
 		}
 		return ret;
 	}
 	public static CardDatabase makeSnapshot(DialogModifier df) {
 		if (df != null)
 		{
 			df.setTitle(R.string.progress_process_db);
 			df.setMessage(R.string.progress_db_snapshot);
 		}
 		return makeSnapshot();
 	}
 }
