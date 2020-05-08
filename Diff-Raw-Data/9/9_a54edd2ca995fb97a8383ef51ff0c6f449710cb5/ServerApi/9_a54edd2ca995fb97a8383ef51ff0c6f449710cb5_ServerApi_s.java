 package com.charlesmadere.android.classygames.server;
 
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.AsyncTask;
 import android.os.Build;
 
 import com.charlesmadere.android.classygames.R;
 import com.charlesmadere.android.classygames.models.Game;
 import com.charlesmadere.android.classygames.models.Person;
 import com.charlesmadere.android.classygames.utilities.Utilities;
 
 
 /**
  * A class that will hit an end point of the Classy Games server with some
  * given data.
  */
 public abstract class ServerApi
 {
 
 
 	protected final static String LOG_TAG = Utilities.LOG_TAG + " - ServerApi";
 
 
 
 
 	/**
 	 * A reference to this class's AsyncTask (if it's currently running).
 	 */
 	private ServerApiTask serverApiTask;
 
 
 	/**
 	 * The Context of the class that this ServerApi class is being called from.
 	 */
 	private Context context;
 
 
 	/**
 	 * The Game object that this API call has to deal with.
 	 */
 	protected Game game;
 
 
 	/**
 	 * Object that allows us to run any of the methods that are defined in the
 	 * ServerApiListeners interface.
 	 */
 	private ServerApiListeners listeners;
 
 
 	/**
 	 * An interface that will be used once we're done running code here.
 	 */
 	public interface ServerApiListeners
 	{
 
 
 		/**
 		 * If this class's ServerApiTask AsyncTask gets cancelled this then
 		 * this method will be run.
 		 */
 		public void onCancel();
 
 
 		/**
 		 * Once this ServerApi class has finished its duty this method will
 		 * run. If the ServerApiTask AsyncTask was cancelled, or if the user
 		 * dismissed or selected "No" on the AlertDialog that this class
 		 * prompts with, then this method will never be run.
 		 */
 		public void onComplete();
 
 
 		/**
 		 * If, on the AlertDialog that this class prompts with, the user
 		 * selects either "No" or dismisses it, then this method will be run.
 		 */
 		public void onDismiss();
 
 
 	}
 
 
 
 
 	/**
 	 * Creates a ServerApi object. This should be used to hit certain server
 	 * end points.
 	 * 
 	 * @param context
 	 * The Context of the class that you're creating this object from.
 	 * 
 	 * @param game
 	 * The Game object that this API call has to deal with.
 	 * 
 	 * @param onCompleteListener
 	 * A listener to call once we're done running code here.
 	 */
 	protected ServerApi(final Context context, final Game game, final ServerApiListeners onCompleteListener)
 	{
 		this.context = context;
 		this.game = game;
 		this.listeners = onCompleteListener;
 	}
 
 
 	/**
 	 * Cancels the currently running AsyncTask (if it is currently running).
 	 */
 	public void cancel()
 	{
 		if (serverApiTask != null)
 		{
 			serverApiTask.cancel(true);
 		}
 
 		listeners.onCancel();
 	}
 
 
 	/**
 	 * Begins the execution of this ServerApi code.
 	 * 
 	 * @param askUserToExecute
 	 * True if you want to ask the user to confirm the execution of this
 	 * ServerApi code. False if you want the code without asking the user
 	 * anything; this will immediately begin the ServerApi code.
 	 */
 	public void execute(final boolean askUserToExecute)
 	{
 		if (askUserToExecute)
 		{
 			final AlertDialog.Builder builder = new AlertDialog.Builder(context)
 				.setMessage(getDialogMessage())
 				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
 				{
 					@Override
 					public void onClick(final DialogInterface dialog, final int which)
 					{
 						dialog.dismiss();
 						listeners.onCancel();
 					}
 				})
 				.setOnCancelListener(new DialogInterface.OnCancelListener()
 				{
 					@Override
 					public void onCancel(final DialogInterface dialog)
 					{
 						dialog.dismiss();
 						listeners.onDismiss();
 					}
 				})
 				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
 				{
 					@Override
 					public void onClick(final DialogInterface dialog, final int which)
 					{
 						dialog.dismiss();
 						executeTask();
 					}
 				})
 				.setTitle(getDialogTitle());
 
 			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
 			{
 				builder.setOnDismissListener(new DialogInterface.OnDismissListener()
 				{
 					@Override
 					public void onDismiss(final DialogInterface dialog)
 					{
 						dialog.dismiss();
 						listeners.onDismiss();
 					}
 				});
 			}
 
 			builder.show();
 		}
 		else
 		{
 			executeTask();
 		}
 	}
 
 
 	/**
 	 * Begins the execution of this ServerApi code. Will first ask the user if
	 * they want do in fact want to perform this server call.
 	 */
 	public void execute()
 	{
 		execute(true);
 	}
 
 
 	/**
 	 * Starts the execution of the ServerApiTask AsyncTask.
 	 */
 	private void executeTask()
 	{
 		serverApiTask = new ServerApiTask(context);
 		serverApiTask.execute();
 	}
 
 
 
 
 	/**
 	 * An AsyncTask that will query the Classy Games server.
 	 */
 	private final class ServerApiTask extends AsyncTask<Void, Void, String>
 	{
 
 
 		private Context context;
 		private ProgressDialog progressDialog;
 
 
 		private ServerApiTask(final Context context)
 		{
 			this.context = context;
 		}
 
 
 		@Override
 		protected String doInBackground(final Void... params)
 		{
 			String serverResponse = null;
 
 			if (!isCancelled())
 			{
 				final Person whoAmI = Utilities.getWhoAmI(context);
 				serverResponse = ServerApi.this.doInBackground(whoAmI);
 			}
 
 			return serverResponse;
 		}
 
 
 		private void cancelled()
 		{
 			serverApiTask = null;
 			progressDialog.dismiss();
 			listeners.onCancel();
 		}
 
 
 		@Override
 		protected void onCancelled()
 		{
 			cancelled();
 		}
 
 
 		@Override
 		protected void onCancelled(final String serverResponse)
 		{
 			cancelled();
 		}
 
 
 		@Override
 		protected void onPostExecute(final String serverResponse)
 		{
 			serverApiTask = null;
 			progressDialog.dismiss();
 			listeners.onComplete();
 		}
 
 
 		@Override
 		protected void onPreExecute()
 		{
 			progressDialog = new ProgressDialog(context);
 			progressDialog.setCancelable(true);
 			progressDialog.setCanceledOnTouchOutside(true);
 			progressDialog.setMessage(context.getString(getProgressDialogMessage()));
 
 			progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
 			{
 				@Override
 				public void onCancel(final DialogInterface dialog)
 				{
 					ServerApiTask.this.cancel(true);
 				}
 			});
 
 			progressDialog.setTitle(getDialogTitle());
 			progressDialog.show();
 		}
 
 
 	}
 
 
 
 
 	/**
 	 * The heart of the AsyncTask's code.
 	 * 
 	 * @param whoAmI
 	 * A Person object that represents the user of this Android device.
 	 * 
 	 * @return
 	 * A String that contains the responding result from the server or null if
 	 * a problem happened.
 	 */
 	protected abstract String doInBackground(final Person whoAmI);
 
 
 	/**
 	 * @return
 	 * The R.string.* value for the message to show in the dialog box.
 	 */
 	protected abstract int getDialogMessage();
 
 
 	/**
 	 * @return
 	 * The R.string.* value for the title to show in the dialog box.
 	 */
 	protected abstract int getDialogTitle();
 
 
 	/**
 	 * @return
 	 * The R.string.* value for the message to show in the progress dialog box.
 	 */
 	protected abstract int getProgressDialogMessage();
 
 
 }
