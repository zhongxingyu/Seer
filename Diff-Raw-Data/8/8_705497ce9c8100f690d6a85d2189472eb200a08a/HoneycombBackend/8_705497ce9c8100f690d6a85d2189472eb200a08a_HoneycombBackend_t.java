 package net.mms_projects.copy_it.clipboard_backends;
 
 import android.annotation.TargetApi;
import android.content.ClipData;
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
 		if ((clip != null) && (clip.getItemCount() != 0)) {
            ClipData.Item item = clip.getItemAt(0);
            if ((item == null) || (item.getText() == null)) {
                return null;
            }
			return item.getText().toString();
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
