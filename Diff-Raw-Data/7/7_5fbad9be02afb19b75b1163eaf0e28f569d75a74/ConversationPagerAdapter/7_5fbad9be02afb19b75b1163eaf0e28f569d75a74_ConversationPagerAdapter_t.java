 package org.touchirc.adapter;
 
 import java.util.ArrayList;
 
 import org.touchirc.fragments.ConversationFragment;
 import org.touchirc.model.Server;
 
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 
 /**
 * ConversationPagerAdapter
 * In charge to handle conversation fragment for swiping in ConversationActivity
 */
 public class ConversationPagerAdapter extends FragmentStatePagerAdapter {
 
 //	private ArrayList<Fragment> mFragments;
 	private ArrayList<ConversationFragment> mFragments;
 	private Server server;
 
 	public ConversationPagerAdapter(FragmentManager fm, Server s) {
 	    super(fm);
 	    server = s;
 	    mFragments = new ArrayList<ConversationFragment>();
 	    for(String c : server.getAllConversations()) {
 	        mFragments.add(new ConversationFragment(server.getConversation(c)));
 	    }
 	}
 
 	@Override
 	public int getCount() {
 	    return mFragments.size();
 	}
 
 	@Override
 	public Fragment getItem(int position) {
 		return new ConversationFragment(server.getConversation(server.getAllConversations().get(position)));
 	}
 	
 	@Override
 	public int getItemPosition(Object object){
 		int position = mFragments.indexOf(object);
 		if (position >= 0)
             return position;
 		return POSITION_NONE;
 	}
 	
 	@Override
 	public CharSequence getPageTitle(int position){
 		return server.getAllConversations().get(position);
 	}
 		
 	public void addFragment(ConversationFragment c){
 		mFragments.add(c);
 		notifyDataSetChanged();
 	}
 
 }
