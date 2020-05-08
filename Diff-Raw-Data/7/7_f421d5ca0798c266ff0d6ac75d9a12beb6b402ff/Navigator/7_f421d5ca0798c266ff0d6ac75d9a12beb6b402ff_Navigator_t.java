 package com.capsule.common;
 
 import android.content.Context;
 import android.content.Intent;
 
 import com.capsule.android.AgreementActivity;
 import com.capsule.android.BottomTabActivity;
 import com.capsule.android.FindWaysActivity;
 import com.capsule.android.FriendActivity;
 import com.capsule.android.LoginActivity;
 import com.capsule.android.MainActivity;
 import com.capsule.android.MessageActivity;
 import com.capsule.android.RegisterActivity;
 import com.capsule.android.SettingActivity;
 
 public class Navigator {
 
 	//Add your new Activity SEQ value there
 	public final static int MainActivitySEQ = 0;
 	public final static int LoginActivitySEQ = 1;
 	public final static int RegistActivitySEQ = 2;
 	public final static int ForgertPasswordSEQ = 3;
 	public final static int BottomTabActivitySEQ = 4;
 	public final static int FriendListActivitySEQ = 10;
	public final static int MessageActivitySEQ = 11;
 	public final static int SettingActivitySEQ = 15;
 	public final static int AgreementActivitySEQ = 16;
 	public final static int FindHimActivitySEQ = 17;
	
 	
 	private Context myContext = null;
 	
 	public Navigator(Context context)
 	{
 		myContext = context;
 	}
 	
 	/**
 	 * @param The component class that is to be used for the intent
 	 */
 	public void switchTo(Class<?> cls)
 	{
 		switchTo(cls,null);
 	}
 	
 	/**
 	 * @param cls The component class that is to be used for the intent
 	 * @param src Copy all extras in "src" to new intent
 	 */
 	public void switchTo(Class<?> cls,Intent src)
 	{
       Intent intent = new Intent(myContext,cls);
       if(src != null)
       {
     	  intent.putExtras(src);
       }
       myContext.startActivity(intent);
 	}
 	
 	/**
 	 * @param seq Activity SEQ number, you can use Navigator.MainActivtySEQ to switch
 	 */
 	public void switchTo(int seq)
 	{
 		switchTo(seq,null);
 	}
 	
 	/**
 	 * @param seq Activity SEQ number, you can use Navigator.MainActivtySEQ to switch
 	 * @param src Copy all extras in "src" to new intent
 	 */
 	public void switchTo(int seq, Intent src){
 		switch(seq) {
 		    case MainActivitySEQ:
 		        switchTo(MainActivity.class, src);
 		        break;
 		    case LoginActivitySEQ:
                 switchTo(LoginActivity.class, src);
                 break;
             case RegistActivitySEQ:
                 switchTo(RegisterActivity.class, src);
                 break;
             case FriendListActivitySEQ:
                 switchTo(FriendActivity.class, src);
                 break;
             case SettingActivitySEQ:
                 switchTo(SettingActivity.class, src);
                 break;
             case AgreementActivitySEQ:
                 switchTo(AgreementActivity.class, src);
                 break;
             case FindHimActivitySEQ:
                 switchTo(FindWaysActivity.class, src);
                break;
             case MessageActivitySEQ:
 				switchTo(MessageActivity.class, src);
 				break;
             case BottomTabActivitySEQ:
                 switchTo(BottomTabActivity.class, src);
                 break;
             default:
                 break;
         }
 	    return;
 	}
 	
 }
