 package net.vrallev.android.base;
 
 import android.os.Bundle;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import dagger.ObjectGraph;
 
 /**
  * A base class for all activities in this application. It provides several helper methods to store data over configuration changes,
  * to enable the foreground dispatch system and to show dialogs.
  * 
  * @author Ralf Wondratschek
  *
  */
 @SuppressWarnings({"UnusedDeclaration", "ConstantConditions"})
 public abstract class BaseActivitySupport extends FragmentActivity {
 	
 	private static final String KEY_MESSAGE_LIST = "messageList";
 	private static final String NO_TAG = "no_tag";
 	
 	private static final int DISMISS_DIALOG = 396;
 	private static final int SHOW_DIALOG = 634;
 	private static final int REPLACE_FRAGMENT = 964;
 	private static final int REMOVE_FRAGMENT = 684;
 	
 	protected boolean mVisible = false;
 	private ArrayList<Message> toRunWhenVisible;
 	
 	private RetainInstanceFragment mRetainFragment;
 
     protected ObjectGraph mActivityObjectGraph;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
         BaseApp app = (BaseApp) getApplication();
 
         List<Object> modules = new ArrayList<>();
         modules.add(new BaseActivityModule(this));
         addModules(modules);
 
         mActivityObjectGraph = app.getObjectGraph().plus(modules.toArray());
        mActivityObjectGraph.inject(this);
 
 		if (savedInstanceState != null) {
 			toRunWhenVisible = savedInstanceState.getParcelableArrayList(KEY_MESSAGE_LIST);
 		}
 		
 		if (toRunWhenVisible == null) {
 			toRunWhenVisible = new ArrayList<>();
 		}
     }
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		mVisible = true;
 		
 		for (Message message : toRunWhenVisible) {
 			switch (message.what) {
 				
 				case SHOW_DIALOG:
 					DialogHolder holder = (DialogHolder) message.obj;
 					internalShowDialog(holder.mFragment, holder.mTag);
 					break;
 				
 				case DISMISS_DIALOG:
 					internalDismissDialog((String) message.obj);
 					break;
 					
 				case REPLACE_FRAGMENT:
 					ReplaceFragmentHolder fragmentHolder = (ReplaceFragmentHolder) message.obj;
 					internalReplaceFragment(fragmentHolder.mContainerViewId, fragmentHolder.mFragment, fragmentHolder.mTag, fragmentHolder.mTransition, fragmentHolder.mAddToBackStack, fragmentHolder.mBackStackName);
 					break;
 					
 				case REMOVE_FRAGMENT:
 					fragmentHolder = (ReplaceFragmentHolder) message.obj;
 					internalRemoveFragment(fragmentHolder.mFragment, fragmentHolder.mTransition);
 					break;
 			}
 		}
 		
 		toRunWhenVisible.clear();
 	}
 
 	@Override
 	protected void onPause() {
 		mVisible = false;
 		super.onPause();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		
 		outState.putParcelableArrayList(KEY_MESSAGE_LIST, toRunWhenVisible);
 	}
 
     protected void addModules(List<Object> modules) {
 
     }
 
     public void inject(Object object) {
         mActivityObjectGraph.inject(object);
     }
 
 	/**
 	 * Helper method to display a {@link android.app.DialogFragment}. If the activity is not visible, the dialog gets stored. After
 	 * the activity is visible again, the dialog gets displayed.
 	 *
 	 * @param dialogFragment The {@link android.app.DialogFragment}, which should be shown.
 	 * @param tag The fragment's tag. If the value is {@code null}, a default tag gets used. 
 	 */
 	public void showDialog(DialogFragment dialogFragment, String tag) {
 		if (tag == null) {
 			tag = NO_TAG;
 		}
 		
 		if (mVisible) {
 			internalShowDialog(dialogFragment, tag);
 		} else {
 			Message message = new Message();
 			message.obj = new DialogHolder(dialogFragment, tag);
 			message.what = SHOW_DIALOG;
 			
 			toRunWhenVisible.add(message);
 		}
 	}
 	
 	/**
 	 * Helper method to dismiss a dialog. If the activity is not visible, the action gets stored. After the
 	 * activity is visible again, the dialog gets dismissed. 
 	 * 
 	 * @param tag The dialog's tag. If the value is {@code null}, the default tag gets used. 
 	 */
 	public void dismissDialog(String tag) {
 		if (tag == null) {
 			tag = NO_TAG;
 		}
 		
 		if (mVisible) {
 			internalDismissDialog(tag);
 		} else {
 			Message message = new Message();
 			message.obj = tag;
 			message.what = DISMISS_DIALOG;
 			
 			toRunWhenVisible.add(message);
 		}
 	}
 	
 	public void replaceFragment(int containerViewId, Fragment fragment) {
         replaceFragment(containerViewId, fragment, FragmentTransaction.TRANSIT_NONE);
     }
 	
 	public void replaceFragment(int containerViewId, Fragment fragment, int transition) {
         replaceFragment(containerViewId, fragment, null, transition);
     }
 	
 	public void replaceFragment(int containerViewId, Fragment fragment, String tag, int transition) {
         replaceFragment(containerViewId, fragment, tag, transition, false);
     }
 	
 	public void replaceFragment(int containerViewId, Fragment fragment, String tag, int transition, boolean addToBackStack) {
         replaceFragment(containerViewId, fragment, tag, transition, addToBackStack, null);
     }
     
 	public void replaceFragment(int containerViewId, Fragment fragment, String tag, int transition, boolean addToBackStack, String backStackName) {
 		if (mVisible) {
 			internalReplaceFragment(containerViewId, fragment, tag, transition, addToBackStack, backStackName);
 		} else {
 			Message message = new Message();
 			message.what = REPLACE_FRAGMENT;
 			message.obj = new ReplaceFragmentHolder(containerViewId, fragment, tag, transition, addToBackStack, backStackName);
 
 			toRunWhenVisible.add(message);
 		}
 	}
 	
 	public void removeFragment(Fragment fragment, int transition) {
 		if (mVisible) {
 			internalRemoveFragment(fragment, transition);
 		} else {
 			Message message = new Message();
 			message.what = REMOVE_FRAGMENT;
 			message.obj = new ReplaceFragmentHolder(-1, fragment, null, transition, false, null);
 			
 			toRunWhenVisible.add(message);
 		}
 	}
 	
 	public Object put(String key, Object object) {
         if (mRetainFragment == null) {
             mRetainFragment = RetainInstanceFragment.findOrCreateFragment(getSupportFragmentManager());
         }
         return mRetainFragment.put(key, object);
 	}
 	
 	public Object load(String key) {
         if (mRetainFragment == null) {
             mRetainFragment = RetainInstanceFragment.findFragment(getSupportFragmentManager());
         }
         if (mRetainFragment == null) {
             return null;
         }
 		return mRetainFragment.get(key);
 	}
 	
 	public Object remove(String key) {
         if (mRetainFragment == null) {
             mRetainFragment = RetainInstanceFragment.findFragment(getSupportFragmentManager());
         }
         if (mRetainFragment == null) {
             return null;
         }
 		return mRetainFragment.remove(key);
 	}
 	
 	private void internalShowDialog(DialogFragment dialogFragment, String tag) {
 		dialogFragment.show(getSupportFragmentManager(), tag);
 	}
 
 	private void internalDismissDialog(String tag) {
 		Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
 		if (fragment instanceof DialogFragment) {
 			((DialogFragment) fragment).dismiss();
 		}
 	}
 	
 	private void internalReplaceFragment(int containerViewId, Fragment fragment, String tag, int transition, boolean addToBackStack, String backStackName) {
         FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                 .setTransition(transition)
                 .replace(containerViewId, fragment, tag);
 
         if (addToBackStack) {
             transaction.addToBackStack(backStackName);
         }
         
         transaction.commit();
     }
 	
 	private void internalRemoveFragment(Fragment fragment, int transition) {
 		getSupportFragmentManager().beginTransaction()
 				.setTransition(transition)
 				.remove(fragment)
 				.commit();
 	}
 	
 	/**
 	 * Simple helper class.
 	 * 
 	 * @author Ralf Wondratschek
 	 *
 	 */
 	private static class DialogHolder {
 		public DialogFragment mFragment;
 		public String mTag;
 
 		public DialogHolder(DialogFragment fragment, String tag) {
 			mFragment = fragment;
 			mTag = tag;
 		}
 	}
 	
 	private static class ReplaceFragmentHolder {
 		public Fragment mFragment;
 		public int mContainerViewId;
 		public String mTag;
 		public int mTransition;
         public boolean mAddToBackStack;
         public String mBackStackName;
 		
 		public ReplaceFragmentHolder(int containerViewId, Fragment fragment, String tag, int transition, boolean addToBackStack, String backStackName) {
 			mContainerViewId = containerViewId;
 			mFragment = fragment;
 			mTag = tag;
 			mTransition = transition;
             mAddToBackStack = addToBackStack;
             mBackStackName = backStackName;
 		}
 	}
 	
 	/**
 	 * Non-UI fragment to store and load objects. The same instance is retained over configuration changes. 
 	 * 
 	 * @author Ralf Wondratschek
 	 *
 	 */
 	public static class RetainInstanceFragment extends Fragment {
 		
 		private static final String FRAGMENT_TAG = "myRetainTag";
 		
 		public static RetainInstanceFragment findFragment(FragmentManager manager) {
             Fragment fragment = manager.findFragmentByTag(FRAGMENT_TAG);
             if (fragment instanceof RetainInstanceFragment) {
                 return (RetainInstanceFragment) fragment;
             } else {
                 return null;
             }
         }
 
 		public static RetainInstanceFragment findOrCreateFragment(FragmentManager manager) {
 			Fragment fragment = manager.findFragmentByTag(FRAGMENT_TAG);
 			if (fragment instanceof RetainInstanceFragment) {
 				return (RetainInstanceFragment) fragment;
 			}
 			
 			RetainInstanceFragment retainInstanceFragment = new RetainInstanceFragment();
 			manager.beginTransaction().add(retainInstanceFragment, FRAGMENT_TAG).commit();
 			return retainInstanceFragment;
 		}
 		
 		private Map<String, Object> mStorage;
 
         public RetainInstanceFragment() {
             mStorage = new HashMap<>();
         }
 
         @Override
 		public void onCreate(Bundle savedInstanceState) {
 			super.onCreate(savedInstanceState);
 			setRetainInstance(true);
 		}
 		
 		public Object put(String key, Object object) {
 			return mStorage.put(key, object);
 		}
 		
 		public Object get(String key) {
 			return mStorage.get(key);
 		}
 		
 		public Object remove(String key) {
 			return mStorage.remove(key);
 		}
 	}
 }
