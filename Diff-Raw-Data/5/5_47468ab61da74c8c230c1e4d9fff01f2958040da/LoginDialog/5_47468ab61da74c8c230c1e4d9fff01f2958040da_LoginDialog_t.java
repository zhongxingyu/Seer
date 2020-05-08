 package com.intalker.borrow.ui.login;
 
 import com.intalker.borrow.HomeActivity;
 import com.intalker.borrow.R;
 import com.intalker.borrow.cloud.CloudAPIAsyncTask.ICloudAPITaskListener;
 import com.intalker.borrow.cloud.CloudAPI;
 import com.intalker.borrow.data.UserInfo;
 import com.intalker.borrow.util.DensityAdaptor;
 import com.intalker.borrow.util.LayoutUtil;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.graphics.Color;
 import android.text.InputType;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.ImageView.ScaleType;
 import android.widget.Toast;
 
 public class LoginDialog extends Dialog {
 	private RelativeLayout mContent = null;
 	private RelativeLayout mMainLayout = null;
 	private EditText mEmailInput = null;
 	private EditText mPasswordInput = null;
 	private Button mLoginBtn = null;
 	private Button mCancelBtn = null;
 
 	public LoginDialog(Context context) {
 		super(context, R.style.Theme_TransparentDialog);
 
 		mContent = new RelativeLayout(context);
 		this.setContentView(mContent);
 
 		mMainLayout = new RelativeLayout(context);
 		mMainLayout.setBackgroundResource(R.drawable.detail_bk);
 		RelativeLayout.LayoutParams mainLayoutLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		mainLayoutLP.width = DensityAdaptor.getDensityIndependentValue(260);
 		mainLayoutLP.height = DensityAdaptor.getDensityIndependentValue(250);
 
 		mContent.addView(mMainLayout, mainLayoutLP);
 
 		int margin = LayoutUtil.getLoginDialogMargin();
 		int y = DensityAdaptor.getDensityIndependentValue(40);
 		mEmailInput = createElement(R.string.email, margin, y);
 		// mEmailInput.setInputType(InputType.TYPE_CLASS_TEXT);//??
 		mEmailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
 		// Test
		mEmailInput.setText("tom.dong@openlib.com");
 
 		y = DensityAdaptor.getDensityIndependentValue(100);
 		mPasswordInput = createElement(R.string.password, margin, y);
 		mPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT
 				| InputType.TYPE_TEXT_VARIATION_PASSWORD);
 		// Test
		mPasswordInput.setText("dong");
 
 		mMainLayout.addView(createSeparator(DensityAdaptor
 				.getDensityIndependentValue(160)));
 
 		mLoginBtn = new Button(context);
 		mLoginBtn.setText(R.string.login);
 		RelativeLayout.LayoutParams loginBtnLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		loginBtnLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		loginBtnLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 		loginBtnLP.leftMargin = margin;
 		loginBtnLP.bottomMargin = margin;
 		mMainLayout.addView(mLoginBtn, loginBtnLP);
 		mLoginBtn.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				login();
 			}
 
 		});
 
 		mCancelBtn = new Button(context);
 		mCancelBtn.setText(R.string.cancel);
 		RelativeLayout.LayoutParams cancelBtnLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		cancelBtnLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 		cancelBtnLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 		cancelBtnLP.rightMargin = margin;
 		cancelBtnLP.bottomMargin = margin;
 		mMainLayout.addView(mCancelBtn, cancelBtnLP);
 		mCancelBtn.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				cancelLogin();
 			}
 
 		});
 	}
 
 	private View createSeparator(int y) {
 		ImageView v = new ImageView(this.getContext());
 		v.setImageResource(R.drawable.hori_separator);
 		v.setScaleType(ScaleType.FIT_XY);
 		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.FILL_PARENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 
 		lp.topMargin = y;
 		lp.width = LayoutUtil.getDetailDialogWidth();
 
 		v.setLayoutParams(lp);
 		return v;
 	}
 
 	private EditText createElement(int labelTextResId, int leftMargin,
 			int topMargin) {
 		Context context = this.getContext();
 		TextView label = new TextView(context);
 		label.setTextColor(Color.BLACK);
 		label.setText(labelTextResId);
 		RelativeLayout.LayoutParams labelLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		labelLP.leftMargin = leftMargin;
 		labelLP.topMargin = topMargin;
 		mMainLayout.addView(label, labelLP);
 
 		EditText input = new EditText(context);
 		input.setTextSize(12.0f);
 		RelativeLayout.LayoutParams inputLP = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		inputLP.width = LayoutUtil.getLoginDialogInputWidth();
 		inputLP.leftMargin = leftMargin
 				+ DensityAdaptor.getDensityIndependentValue(70);
 		inputLP.topMargin = topMargin
 				- DensityAdaptor.getDensityIndependentValue(10);
 		mMainLayout.addView(input, inputLP);
 
 		return input;
 	}
 
 	private void doAfterLogin(int returnCode) {
 		switch (returnCode) {
 		case CloudAPI.Return_OK:
 			HomeActivity.getApp().getBookGallery().updateTopPanel();
 			this.dismiss();
 			break;
 		case CloudAPI.Return_WrongUserNameOrPassword:
 			Toast.makeText(this.getContext(), "Wrong username or pwd.",
 					Toast.LENGTH_SHORT).show();
 			break;
 		case CloudAPI.Return_NetworkError:
 			Toast.makeText(this.getContext(), "Network error.",
 					Toast.LENGTH_SHORT).show();
 			break;
 		default:
 			Toast.makeText(this.getContext(), "Unknown error.",
 					Toast.LENGTH_SHORT).show();
 			break;
 		}
 	}
 
 	private void login() {
 		CloudAPI.login(this.getContext(), mEmailInput.getText().toString(),
 				mPasswordInput.getText().toString(),
 				new ICloudAPITaskListener() {
 
 					@Override
 					public void onFinish(int returnCode) {
 						doAfterLogin(returnCode);
 					}
 
 				});
 	}
 
 	private void cancelLogin() {
 		this.dismiss();
 	}
 }
