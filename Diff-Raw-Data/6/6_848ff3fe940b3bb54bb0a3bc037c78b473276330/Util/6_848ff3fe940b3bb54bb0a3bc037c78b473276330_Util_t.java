 package com.megginson.sloopsql;
 
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.content.Context;
 import android.widget.Toast;
 
 /**
  * Static utility methods for the application.
  */
 public final class Util
 {
 
 	/**
 	 * Inflate a layout.
 	 *
 	 * @param context The context (e.g. activity) from which to grab an
 	 * inflater.
 	 * @param id The identifier of the XML layout to inflate.
 	 * @return The inflated view.
 	 */
 	public static View inflate(Context context, int id)
 	{
 		LayoutInflater inflater = LayoutInflater.from(context);
 		return inflater.inflate(id, null);
 	}
 	
 	/**
 	 * Show a long toast pop-up message.
 	 *
 	 * @param context The context (e.g. activity) in which to show the toast.
 	 * @param message The string message to show.
 	 */
 	public static void toast(Context context, String message)
 	{
 		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
 	}
 	
 	/**
 	 * Construct a stacktrace string from a throwable.
 	 *
 	 * Combine all the {@link StackTrace} objects in a throwable into a
 	 * single string, for debugging.
 	 *
 	 * @param t The throwable from which to extract the stacktrace.
 	 * @return A representation of the stacktrace as a single string.
 	 */
 	public static String makeStackTrace(Throwable t)
 	{
		StringBuffer s = new StringBuffer(t.getMessage());
 		for (StackTraceElement e : t.getStackTrace())
 		{
 			s.append('\n');
			s.append(e.toString());
 		}
 		return s.toString();
 	}
 
 }
