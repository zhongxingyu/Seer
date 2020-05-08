 package net.rdrei.android.wakimail.task;
 
 import net.rdrei.android.wakimail.R;
 import net.rdrei.android.wakimail.data.MailTable;
 import net.rdrei.android.wakimail.wak.MailLoader;
 import net.rdrei.android.wakimail.wak.MailLoaderFactory;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Handler;
 import android.widget.Toast;
 
 import com.google.inject.Inject;
 
 public class MailLoadTask extends RdreiAsyncTask<Void> {
 
 	@Inject
 	private MailLoaderFactory mailLoaderFactory;
 	private final Uri mUri;
 
 	public static final int LOAD_SUCCESS_MESSAGE = 0;
 	public static final int LOAD_ERROR_MESSAGE = -1;
 
 	public MailLoadTask(Context context, Handler handler, Uri uri) {
 		super(context, handler);
 		mUri = uri;
 	}
 
 	@Override
 	public Void call() throws Exception {
 		final ContentResolver resolver = getContext().getContentResolver();
 		final String externalId = getExternalId(resolver);
 
 		final MailLoader loader = mailLoaderFactory.create(externalId);
 		final String body = loader.load();
 		saveMailBody(resolver, body);
 
 		return null;
 	}
 
 	/**
 	 * Sends the result back to the main thread.
 	 * 
 	 * @see roboguice.util.SafeAsyncTask#onSuccess(java.lang.Object)
 	 */
 	@Override
 	protected void onSuccess(Void t) throws Exception {
 		super.onSuccess(t);
 
 		handler.sendEmptyMessage(LOAD_SUCCESS_MESSAGE);
 	}
 
 	@Override
 	protected void onException(Exception e) throws RuntimeException {
 		super.onException(e);
 
 		if (e instanceof InterruptedException) {
 			// Don't handle interruptions as usual exceptions.
 			return;
 		}
 		final String text = this.formatResourceString(R.string.login_error,
 				e.getMessage());
 		final Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
 		toast.show();
 
 		handler.sendEmptyMessage(LOAD_ERROR_MESSAGE);
 	}
 
 	private int saveMailBody(ContentResolver resolver, String body) {
 		final ContentValues values = new ContentValues(1);
 		values.put(MailTable.Columns.BODY, body);
 		return resolver.update(mUri, values, null, null);
 	}
 
 	/**
 	 * Blocking method to retrieve the external ID based on the URL provided.
 	 * 
 	 * @return External ID
 	 */
 	private String getExternalId(ContentResolver resolver) {
 		// Query the most recent version of the mail.
 		// We don't need transactional management, though. Even if there would
 		// be conflicting queries, the worst result would be to save the same
 		// result twice.
 		final Cursor cursor = resolver.query(mUri,
 				new String[] { MailTable.Columns.EXTERNAL_ID }, null, null,
 				null);
		try {
			cursor.moveToFirst();
			return cursor.getString(0);
		} finally {
			cursor.close();
		}
 	}
 }
