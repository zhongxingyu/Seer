 package net.mms_projects.copyit.clipboard_backends;
 
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.os.Build;
 import net.mms_projects.copy_it.R;
 
 @TargetApi(Build.VERSION_CODES.HONEYCOMB)
 public class HoneycombBackend extends AbstractAndroidClipboardBackend {
 
 	public HoneycombBackend(Context context) {
 		super(context);
 	}
 
 	@Override
 	public String getText() {
 		android.content.ClipboardManager clipboard = (android.content.ClipboardManager) this.context.getSystemService(Context.CLIPBOARD_SERVICE);
 		android.content.ClipData clip = clipboard.getPrimaryClip();
		if (clip.getItemCount() != 0) {
			return clip.getItemAt(0).getText().toString();
		}
		return null;
 	}
 
 	@Override
 	public void setText(String text) {
 		android.content.ClipboardManager clipboard = (android.content.ClipboardManager) this.context.getSystemService(Context.CLIPBOARD_SERVICE);
 		android.content.ClipData clip = android.content.ClipData.newPlainText(
 				"text label", text);
 		clipboard.setPrimaryClip(clip);
 	}
 
 }
