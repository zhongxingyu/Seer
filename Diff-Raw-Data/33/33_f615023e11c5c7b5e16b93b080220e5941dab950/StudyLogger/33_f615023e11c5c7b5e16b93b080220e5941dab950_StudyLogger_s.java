 package usask.hci.fastdraw;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.os.Environment;
 import android.util.Log;
 
 public class StudyLogger {
 	private File mFile;
 	private Context mContext;
 	
 	public StudyLogger(Context c) {
 		mContext = c;
 		
 		File dir = new File(Environment.getExternalStorageDirectory() + "/Fast Draw");
 		dir.mkdirs();
 		mFile = new File(dir, "study.txt");
 	}
 	
 	public void touch(long startNs, long endNs, boolean multiTouch, boolean markedCanvas) {
 		long startMs = startNs / 1000000;
 		long endMs = endNs / 1000000;
 		long duration = endMs - startMs;
 		log("Touch", startMs + "," + endMs + "," + duration + "," + (multiTouch ? 1 : 0) + "," + (markedCanvas ? 1 : 0));
 	}
 	
	public void task(int taskNum, int numTargets, String targetString, long durationNs) {
 		long durationMs = durationNs / 1000000;
		log("Task", taskNum + "," + numTargets + "," + targetString + "," + durationMs);
 	}
 	
 	public void log(String message) {
 		log("", message);
 	}
 
 	public void log(String type, String message) {
     	Log.i("FastDraw" + type, message);
     	
 		Writer s;
 		
 		try {
 			s = new OutputStreamWriter(new FileOutputStream(mFile, true));
 		} catch (FileNotFoundException e) {
 			notifyError(e);
 			return;
 		}
 
 		try {
 			s.write(message + "\r\n");
 			s.flush();
 		} catch (IOException e) {
 			notifyError(e);
 		} finally {
 			try { s.close(); } catch (IOException e) {}
 		}
 	}
 	
 	private void notifyError(Exception e) {
 		StringWriter sw = new StringWriter();
 		e.printStackTrace(new PrintWriter(sw));
 		
         new AlertDialog.Builder(mContext)
         	.setTitle(R.string.dialog_logging_error)
         	.setMessage(sw.toString())
         	.setPositiveButton(android.R.string.ok, null)
         	.show();
 	}
 }
