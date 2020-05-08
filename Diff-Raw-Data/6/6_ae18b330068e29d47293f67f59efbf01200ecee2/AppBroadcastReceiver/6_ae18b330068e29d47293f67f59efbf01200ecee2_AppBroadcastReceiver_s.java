 package com.sflab.common;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 
 public class AppBroadcastReceiver extends BroadcastReceiver {
 
 	final Action[] actions;
 
 	public AppBroadcastReceiver(Action... actions) {
 		this.actions = actions;
 	}
 
 	public void regist(Context context) {
 		IntentFilter filter = new IntentFilter();
 		for (Action action : actions) {
 			filter.addAction(action.name);
 		}
 		context.registerReceiver(this, filter);
 	}
 
 	public void unregist(Context context) {
		context.unregisterReceiver(this);
 	}
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		String target = intent.getAction();
 		for (Action action : actions) {
 			if (target.equals(action.name)) {
 				action.command.action(intent);
 				break;
 			}
 		}
 	}
 
 	public static class Action {
 		public Action(String name, ActionCommand command) {
 			this.name = name;
 			this.command = command;
 		}
 
 		final String name;
 		final ActionCommand command;
 	}
 
 	public static interface ActionCommand {
 		void action(Intent intent);
 	}
 }
