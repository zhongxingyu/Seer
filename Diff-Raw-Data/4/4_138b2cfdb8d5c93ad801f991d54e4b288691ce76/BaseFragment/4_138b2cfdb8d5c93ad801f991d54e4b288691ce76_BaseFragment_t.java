 package com.dbstar.settings.base;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.Fragment;
 import android.content.ContentResolver;
 import android.content.DialogInterface;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.util.Log;
 
 public class BaseFragment extends Fragment implements DialogCreatable {
 	private static final String TAG = "BaseFragment";
 
 	private BaseDialogFragment mDialogFragment;
 	protected Activity mActivity;
 	protected boolean mBound = false;
 	protected PageManager mManager = null;
 
	public BaseFragment() {

	}

 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		mActivity = getActivity();
 		if (mActivity instanceof PageManager) {
 			mManager = (PageManager) mActivity;
 		}
 	}
 
 	public void onStart() {
 		super.onStart();
 	}
 
 	public void onStop() {
 		super.onStop();
 	}
 
 	/*
 	 * The name is intentionally made different from Activity#finish(), so that
 	 * users won't misunderstand its meaning.
 	 */
 	public final void finishFragment() {
 		getActivity().onBackPressed();
 	}
 
 	// Some helpers for functions used by the settings fragments when they were
 	// activities
 
 	/**
 	 * Returns the ContentResolver from the owning Activity.
 	 */
 	protected ContentResolver getContentResolver() {
 		return getActivity().getContentResolver();
 	}
 
 	/**
 	 * Returns the specified system service from the owning Activity.
 	 */
 	protected Object getSystemService(final String name) {
 		return getActivity().getSystemService(name);
 	}
 
 	/**
 	 * Returns the PackageManager from the owning Activity.
 	 */
 	protected PackageManager getPackageManager() {
 		return getActivity().getPackageManager();
 	}
 
 	@Override
 	public void onDetach() {
 		if (isRemoving()) {
 			if (mDialogFragment != null) {
 				mDialogFragment.dismiss();
 				mDialogFragment = null;
 			}
 		}
 		super.onDetach();
 	}
 
 	// Dialog management
 
 	protected void showDialog(int dialogId) {
 		if (mDialogFragment != null) {
 			Log.e(TAG, "Old dialog fragment not null!");
 		}
 		mDialogFragment = new BaseDialogFragment(this, dialogId);
 		mDialogFragment.show(getActivity().getFragmentManager(),
 				Integer.toString(dialogId));
 	}
 
 	public Dialog onCreateDialog(int dialogId) {
 		return null;
 	}
 
 	protected void removeDialog(int dialogId) {
 		// mDialogFragment may not be visible yet in parent fragment's
 		// onResume().
 		// To be able to dismiss dialog at that time, don't check
 		// mDialogFragment.isVisible().
 		if (mDialogFragment != null
 				&& mDialogFragment.getDialogId() == dialogId) {
 			mDialogFragment.dismiss();
 		}
 		mDialogFragment = null;
 	}
 
 	/**
 	 * Sets the OnCancelListener of the dialog shown. This method can only be
 	 * called after showDialog(int) and before removeDialog(int). The method
 	 * does nothing otherwise.
 	 */
 	protected void setOnCancelListener(DialogInterface.OnCancelListener listener) {
 		if (mDialogFragment != null) {
 			mDialogFragment.mOnCancelListener = listener;
 		}
 	}
 
 	/**
 	 * Sets the OnDismissListener of the dialog shown. This method can only be
 	 * called after showDialog(int) and before removeDialog(int). The method
 	 * does nothing otherwise.
 	 */
 	protected void setOnDismissListener(
 			DialogInterface.OnDismissListener listener) {
 		if (mDialogFragment != null) {
 			mDialogFragment.mOnDismissListener = listener;
 		}
 	}
 
 	public static class BaseDialogFragment extends DialogFragment {
 		private static final String KEY_DIALOG_ID = "key_dialog_id";
 		private static final String KEY_PARENT_FRAGMENT_ID = "key_parent_fragment_id";
 
 		private int mDialogId;
 
 		private Fragment mParentFragment;
 
 		private DialogInterface.OnCancelListener mOnCancelListener;
 		private DialogInterface.OnDismissListener mOnDismissListener;
 
 		public BaseDialogFragment() {
 			/* do nothing */
 		}
 
 		public BaseDialogFragment(DialogCreatable fragment, int dialogId) {
 			mDialogId = dialogId;
 			if (!(fragment instanceof Fragment)) {
 				throw new IllegalArgumentException(
 						"fragment argument must be an instance of "
 								+ Fragment.class.getName());
 			}
 			mParentFragment = (Fragment) fragment;
 		}
 
 		@Override
 		public void onSaveInstanceState(Bundle outState) {
 			super.onSaveInstanceState(outState);
 			if (mParentFragment != null) {
 				outState.putInt(KEY_DIALOG_ID, mDialogId);
 				outState.putInt(KEY_PARENT_FRAGMENT_ID, mParentFragment.getId());
 			}
 		}
 
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			if (savedInstanceState != null) {
 				mDialogId = savedInstanceState.getInt(KEY_DIALOG_ID, 0);
 				int mParentFragmentId = savedInstanceState.getInt(
 						KEY_PARENT_FRAGMENT_ID, -1);
 				if (mParentFragmentId > -1) {
 					mParentFragment = getFragmentManager().findFragmentById(
 							mParentFragmentId);
 					if (!(mParentFragment instanceof DialogCreatable)) {
 						throw new IllegalArgumentException(
 								KEY_PARENT_FRAGMENT_ID + " must implement "
 										+ DialogCreatable.class.getName());
 					}
 				}
 				// This dialog fragment could be created from
 				// non-SettingsPreferenceFragment
 				if (mParentFragment instanceof BaseFragment) {
 					// restore mDialogFragment in mParentFragment
 					((BaseFragment) mParentFragment).mDialogFragment = this;
 				}
 			}
 			return ((DialogCreatable) mParentFragment)
 					.onCreateDialog(mDialogId);
 		}
 
 		@Override
 		public void onCancel(DialogInterface dialog) {
 			super.onCancel(dialog);
 			if (mOnCancelListener != null) {
 				mOnCancelListener.onCancel(dialog);
 			}
 		}
 
 		@Override
 		public void onDismiss(DialogInterface dialog) {
 			super.onDismiss(dialog);
 			if (mOnDismissListener != null) {
 				mOnDismissListener.onDismiss(dialog);
 			}
 		}
 
 		public int getDialogId() {
 			return mDialogId;
 		}
 
 		@Override
 		public void onDetach() {
 			super.onDetach();
 
 			// This dialog fragment could be created from
 			// non-SettingsPreferenceFragment
 			if (mParentFragment instanceof BaseFragment) {
 				// in case the dialog is not explicitly removed by
 				// removeDialog()
 				if (((BaseFragment) mParentFragment).mDialogFragment == this) {
 					((BaseFragment) mParentFragment).mDialogFragment = null;
 				}
 			}
 		}
 	}
 
 }
