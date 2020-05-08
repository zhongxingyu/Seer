 package br.com.agendatech.ui;
 
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.widget.ImageView;
 import br.com.agendatech.BuildConfig;
 
 import com.actionbarsherlock.internal.nineoldandroids.animation.Animator;
 import com.actionbarsherlock.internal.nineoldandroids.animation.Animator.AnimatorListener;
 import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
 
 public class ImageLoader extends AsyncTask<Object, Void, Drawable> {
 	volatile ImageView logo;
 
 	public ImageLoader(ImageView logo) {
 		this.logo = logo;
 	}
 
 	protected Drawable doInBackground(Object... params) {
 		String fileName = (String) params[0];
 
 		HttpURLConnection connection = null;
 		try {
 			connection = (HttpURLConnection) new URL(fileName).openConnection();
 			return Drawable.createFromStream(connection.getInputStream(), fileName);
 		} catch (Exception e) {
 			if (BuildConfig.DEBUG)
 				e.printStackTrace();
 		} finally {
 			if (connection != null)
 				connection.disconnect();
 		}
 		return null;
 	}
 
 	protected void onPostExecute(final Drawable result) {
 		ObjectAnimator firstAnimation = ObjectAnimator.ofFloat(this.logo, "rotationY", 90);
 		firstAnimation.addListener(new AnimatorListener() {
 			public void onAnimationStart(Animator animation) {}
 
 			public void onAnimationRepeat(Animator animation) {}
 
 			public void onAnimationEnd(Animator animation) {
 				logo.setImageDrawable(result);
 				logo.setRotationY(-90);
 				ObjectAnimator.ofFloat(logo, "rotationY", 0).start();
 			}
 
 			public void onAnimationCancel(Animator animation) {}
 		});

		try {
			firstAnimation.start();
		} catch (Exception e) {
			logo.setImageDrawable(result);
		}
 	}
 }
