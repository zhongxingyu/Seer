 package com.zrd.zr.letuwb;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import weibo4android.User;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.view.animation.AlphaAnimation;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.sonyericsson.zoom.DynamicZoomControl;
 import com.sonyericsson.zoom.ImageZoomView;
 import com.sonyericsson.zoom.LongPressZoomListener;
 import com.zrd.zr.pnj.SecureURL;
 import com.zrd.zr.pnj.ThreadPNJDealer;
 import com.zrd.zr.protos.WeibousersProtos.UCMappings;
 import com.zrd.zr.weiboes.Sina;
 import com.zrd.zr.weiboes.ThreadSinaDealer;
 
 public class BrowPage {
 	private EntranceActivity parent;
 	
 	private FrameLayout mFrameBackground;
 	private RelativeLayout mLayoutCtrl;
 	private TextView tvNums;
 	private TextView mTextScreenName;
 	private TextView mTextCounts_brow;
 	private ImageButton mBtnDescriptionMore;
 	private ImageView mImgVerified;
 	private ImageZoomView mBrow;
 	private Button btnSave;
 	private Button btnUpload;
 	private Button btnPlay;
 	private Button btnPause;
 	private Button mBtnShare;
 	private Button mBtnWeiboShow;
 	private Button mBtnWeiboFriend;
 	private Button mBtnPossess;
 	private Button mBtnAtSomeone;
 	private static Boolean mIsLoading = false;
 	private Boolean mWasPlaying = false;
 	private Boolean mIsDooming = false;
 	Long mId = (long)0;
 	Bitmap bdPicFailed;
 	private GestureDetector mGestureDetector = null;
 	//private Vibrator mVibrator;
 	AlphaAnimation fadeinAnim = new AlphaAnimation(0.1f, 1.0f);
 	AlphaAnimation fadeoutAnim = new AlphaAnimation(1.0f, 0.1f);
 	
 	Timer mTimer = null;
 	Handler mHandler = null;
 	
     /** Zoom control */
     private DynamicZoomControl mZoomControl;
     /** On touch listener for zoom view */
     private LongPressZoomListener mZoomListener;
     
 	private File mSaveFile = null;
 	
 	private int mReferer = -1;
 	
 	BrowPage(EntranceActivity activity) {
 		this.parent = activity;
 		
 		mFrameBackground = (FrameLayout) activity.findViewById(R.id.flBackground);
 		mLayoutCtrl = (RelativeLayout) activity.findViewById(R.id.rlControl);
 		tvNums = (TextView) activity.findViewById(R.id.textViewNums);
 		mTextScreenName = (TextView) activity.findViewById(R.id.tvScreenNameAbovePic);
 		mTextCounts_brow = (TextView) activity.findViewById(R.id.tvCounts_brow);
 		mBtnDescriptionMore = (ImageButton) activity.findViewById(R.id.btnDescriptionMore);
 		mImgVerified = (ImageView) activity.findViewById(R.id.imgVerified_brow);
 		mBrow = (ImageZoomView) activity.findViewById(R.id.imgBrow);
 		btnSave = (Button) activity.findViewById(R.id.btnSave);
 		btnPlay = (Button) activity.findViewById(R.id.btnPlay);
 		btnPause = (Button) activity.findViewById(R.id.btnPause);
 		btnUpload = (Button) activity.findViewById(R.id.btnUpload);
 		mBtnShare = (Button) activity.findViewById(R.id.btnShare);
 		mBtnWeiboShow = (Button) activity.findViewById(R.id.btnWeiboShow);
 		mBtnWeiboFriend = (Button) activity.findViewById(R.id.btnMakeFriendsFromBrow);
 		mBtnPossess = (Button) activity.findViewById(R.id.btnPossess);
 		mBtnAtSomeone = (Button) activity.findViewById(R.id.btnAtSomeoneBrow);
 		bdPicFailed = BitmapFactory.decodeResource(activity.getResources(), R.drawable.broken);
 		mGestureDetector = new GestureDetector(activity, new PicbrowGestureListener());
 		//mVibrator = ( Vibrator )getApplication().getSystemService(Service.VIBRATOR_SERVICE);
 		mHandler = new Handler() {
 
 			public void handleMessage(Message msg) {
 				switch (msg.what) {
 					case 1:
 						ArrayList<WeibouserInfo> usrs = parent.getMainPage().getUsrs();
 						if (usrs.size() == 0) {
 							Toast.makeText(parent, parent.getString(R.string.tips_nopictures), Toast.LENGTH_SHORT).show();
 						} else if (getBtnPause().getVisibility() == ImageButton.VISIBLE && !isLoading()) {
 	                		Toast.makeText(parent, parent.getString(R.string.tips_playing), Toast.LENGTH_SHORT).show();
 	                		if (parent.getMainPage().getUsrIndexFromId(mId, usrs) < usrs.size() - 1) {
 	                			zrAsyncShowPic(mId, 2);
 	                		} else {
 	                			zrAsyncShowPic(usrs.get(0).id, 0);
 	                		}
 	                	}
 						break;
 					case ThreadSinaDealer.CREATE_FRIENDSHIP:
 						User user = (User)msg.getData().getSerializable(ThreadSinaDealer.KEY_DATA);
 						if (user != null) {
 							if (!user.equals(WeiboPage.getSina().getLoggedInUser())) {
 								Toast.makeText(
 									parent,
 									R.string.tips_friendsmade,
 									Toast.LENGTH_LONG
 								).show();
 							} else {
 								Toast.makeText(
 									parent,
 									R.string.tips_friendsalready,
 									Toast.LENGTH_LONG
 								).show();
 							}
 						} else {
 							//deal with failing to make friends
 						}
 						break;
 					case ThreadPNJDealer.GET_POSSESSIONS:
 						UCMappings mappings = (UCMappings)msg.getData().getSerializable(ThreadPNJDealer.KEY_DATA);
 						if (mappings != null) {
 							if (mappings.getFlag() == 1) {
 								Toast.makeText(
 									parent,
 									R.string.tips_alreadypossessed,
 									Toast.LENGTH_LONG
 								).show();
 							} else if (mappings.getFlag() == 2) {
 								Toast.makeText(
 									parent,
 									R.string.tips_possessed,
 									Toast.LENGTH_LONG
 								).show();
 							} else {
 								Toast.makeText(
 									parent,
 									R.string.tips_failedtopossess,
 									Toast.LENGTH_LONG
 								).show();
 							}
 						} else {
 							//deal with failing to possess
 						}
 						break;
 				}    
 				super.handleMessage(msg);
 			}
 			
 		};
 		
 		getLayoutCtrl().setOnTouchListener(parent);
 		mZoomControl = new DynamicZoomControl();
 		//mZoomListener = new LongPressZoomListener(getApplicationContext());
 		mZoomListener = new LongPressZoomListener(parent);
         mZoomListener.setZoomControl(mZoomControl);
         mBrow.setZoomState(mZoomControl.getZoomState());
         mZoomControl.setAspectQuotient(mBrow.getAspectQuotient());
 		mBrow.setOnTouchListener(parent);
         //mBrow.setOnTouchListener(mZoomListener);
 		
 		mTextScreenName.setVisibility(View.GONE);
 		mImgVerified.setVisibility(View.GONE);
 		mBtnDescriptionMore.setVisibility(View.GONE);
 		
 		zrAsyncShowPic(mId, 0);
 		
 		tvNums.setText("0/" + parent.getMainPage().getUsrs().size());
 		
 		mBtnAtSomeone.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				parent.getWeiboPage().getTextAtSomeone().performClick();
 			}
 			
 		});
 				
 		mBtnShare.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                     // TODO Auto-generated method stub
             	WeibouserInfo wi = parent.getMainPage().getPicFromId(mId, parent.getMainPage().getUsrs());
             	if (wi != null) {
             		String sCacheFile = 
             			AsyncSaver.getSdcardDir() + EntranceActivity.PATH_CACHE 
             			+ wi.uid + ".jg";
             		String sFile =
             			AsyncSaver.getSdcardDir() + EntranceActivity.PATH_CACHE 
             			+ wi.uid + ".jpg";
             		File file = new File(sCacheFile);
             		file.renameTo(new File(sFile));
             		Uri uri = Uri.parse("file:///" + sFile);
             		Intent intent = new Intent(Intent.ACTION_SEND);
                 	intent.setType("image/*");
                     intent.putExtra(Intent.EXTRA_STREAM, uri);
                     
                     intent.putExtra(Intent.EXTRA_SUBJECT, parent.getString(R.string.sharing_title));
                     intent.putExtra(Intent.EXTRA_TEXT, parent.getString(R.string.sharing_content));
                     
                     parent.startActivity(Intent.createChooser(intent, parent.getTitle()));
             	}
             }
         });
 		
 		mBtnWeiboShow.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				//get current weibo user's information
             	WeibouserInfo wi = parent.getMainPage().getPicFromId(mId, parent.getMainPage().getUsrs());
             	
             	/*
                 Intent intent = new Intent();
                 
                 intent.putExtra("uid", wi.uid);
                 intent.putExtra("id", wi.id);
 				
 				intent.setClass(parent, WeiboShowActivity.class);
 				parent.startActivity(intent);
 				*/
 				
 				parent.getWeiboPage().setReferer(R.layout.brow);
 				parent.switchPage(R.layout.weibo_show, wi.uid, wi.id);
 			}
 			
 		});
 		
 		mBtnWeiboFriend.setOnClickListener(new OnClickListener () {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Sina sina = WeiboPage.getSina();
 				if (sina != null && sina.isLoggedIn()) {
 					new Thread(
 						new ThreadSinaDealer(
 							sina,
 							ThreadSinaDealer.CREATE_FRIENDSHIP,
 							new String[] {"" + parent.getMainPage().getPicFromId(mId, parent.getMainPage().getUsrs()).uid},
 							mHandler
 						)
 					).start();
 				} else {
 					RegLoginActivity.shallWeLogin(R.string.title_loginfirst, parent);
 				}
 			}
 			
 		});
 		
 		mBtnPossess.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				WeibouserInfo wi = parent.getMainPage().getPicFromId(mId, parent.getMainPage().getUsrs());
 				
 				new Thread(
 					new ThreadPNJDealer(
 						ThreadPNJDealer.GET_POSSESSIONS,
 						EntranceActivity.URL_SITE 
 							+ "updpzs.php?"
 							+ "clientkey=" + EntranceActivity.getClientKey()
 							+ "&channelid=0"
 							+ "&uid=" + wi.uid,
 						mHandler
 					)
 				).start();
 			}
 			
 		});
 				
 		btnUpload.setOnClickListener(new OnClickListener () {
 
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				AlertDialog dlg = new AlertDialog.Builder(parent)
 					.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface arg0, int arg1) {
 							// TODO Auto-generated method stub
 							AsyncUploader.upload(EntranceActivity.getPrivilege(), parent);
 						}
 						
 					})
 					.setNegativeButton(R.string.label_cancel, null)
 					.setTitle(R.string.tips_noadultstuff)
 					.create();
 				dlg.show();
 			}
 			
 		});
 		
 		btnSave.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Context context = parent.getApplicationContext();
 				// TODO Auto-generated method stub
 				if(!AsyncSaver.getSdcardDir().equals("")) {
 					String path = AsyncSaver.getSdcardDir() + EntranceActivity.PATH_COLLECTION;
 					File file = new File(path);
 					Boolean couldSave = false;
 					if (!file.exists()) {
 						if (file.mkdirs()) {
 							couldSave = true;
 						} else {
 							Toast.makeText(context,
 								String.format(parent.getString(R.string.err_nopath), path),
 								Toast.LENGTH_LONG
 							).show();
 							return;
 						}
 					} else couldSave = true;
 					if (couldSave) {
 						//OK, now we could actually save the file, finally.
 						String fn = getSaveFileName(parent.getMainPage().getPicFromId(mId, parent.getMainPage().getUsrs()).uid + ".xxx");
 						mSaveFile = new File(file, fn);
 						if (mSaveFile.exists()) {
 							//if there is already a file exists with same file name
 							AlertDialog alertDlg = new AlertDialog.Builder(parent).create();
 							alertDlg.setTitle(R.string.title_warning);
 							alertDlg.setMessage(String.format(parent.getString(R.string.err_filealreadyexists), fn));
 							alertDlg.setIcon(android.R.drawable.ic_dialog_alert);
 							alertDlg.setButton(
 								DialogInterface.BUTTON_POSITIVE,
 								parent.getString(R.string.label_ok),
 								new DialogInterface.OnClickListener () {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										// TODO Auto-generated method stub
 										Toast.makeText(
 											parent,
 											R.string.tips_saving,
 											Toast.LENGTH_SHORT
 										).show();
 										AsyncSaver asyncSaver = new AsyncSaver(parent, getBrow().getImage());
 										asyncSaver.execute(mSaveFile);
 									}
 									
 								}
 							);
 							alertDlg.setButton(
 								DialogInterface.BUTTON_NEGATIVE, 
 								parent.getString(R.string.label_cancel), 
 								new DialogInterface.OnClickListener () {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										// TODO Auto-generated method stub
 									}
 									
 								}
 							);
 							alertDlg.show();
 						} else {
 							Toast.makeText(
 								parent,
 								R.string.tips_saving,
 								Toast.LENGTH_SHORT
 							).show();
 							AsyncSaver asyncSaver = new AsyncSaver(parent, getBrow().getImage());
 							asyncSaver.execute(mSaveFile);
 						}
 					}
 				} else {
 					Toast.makeText(context,
 						parent.getString(R.string.err_sdcardnotmounted),
 						Toast.LENGTH_LONG
 					).show();
 				}
 			}
 			
 		});
 		
 		btnPlay.setOnClickListener(new OnClickListener () {
 
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				getBtnPause().setVisibility(ImageButton.VISIBLE);
 				getBtnPlay().setVisibility(ImageButton.GONE);
 				
 				mTimer  = new Timer();
 				
 				mTimer.schedule(new TimerTask(){
 
 					public void run() {
 						Message message = new Message();    
 						message.what = 1;    
 						mHandler.sendMessage(message);  
 					}
 					
 				} , 0, 9000);
 			}
 			
 		});
 		
 		btnPause.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if (mTimer != null) {
 					mTimer.cancel();
 				}
 				getBtnPlay().setVisibility(ImageButton.VISIBLE);
 				getBtnPause().setVisibility(ImageButton.GONE);
 			}
 			
 		});
 		
 		mBtnDescriptionMore.setOnClickListener (new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				parent.popupDescription((String) mBtnDescriptionMore.getTag());
 			}
 			
 		});
 	}
 	
 	/**
      * Reset zoom state and notify observers
      */
     public void resetZoomState() {
     	mZoomControl.getZoomState().setPanX(0.5f);
         mZoomControl.getZoomState().setPanY(0.5f);
         mZoomControl.getZoomState().setZoom(1f);
         mZoomControl.getZoomState().notifyObservers();
     }
 		
 	/**
 	 * we save all file with JPEG format
 	 */
 	public String getSaveFileName(String sOriginalFileName) {
 		String fn = sOriginalFileName;
 		int i = fn.lastIndexOf(".");
 		if (i > 0 && i < (fn.length() - 1)) {
 			fn = fn.substring(0, i);
 		}
 		fn += ".jpg";
 		return fn;
 	}
 	
 	/*
 	 * set current file information on mTextScreenName
 	 */
 	public void zrRenewCurFileInfo() {
 		WeibouserInfo wi = parent.getMainPage().getPicFromId(mId, parent.getMainPage().getUsrs());
 		if (wi == null) return;
 		parent.getWeiboPage().reloadLastUser(wi.uid);
 		tvNums.setText(
 			(parent.getMainPage().getUsrIndexFromId(mId, parent.getMainPage().getUsrs()) + 1)
 			+ "/"
 			+ parent.getMainPage().getTotalPics()
 		);
 		if (wi.screen_name.trim().equals("")) {
 			mTextScreenName.setVisibility(TextView.GONE);
 		} else {
 			mTextScreenName.setVisibility(TextView.VISIBLE);
 			String s = wi.description;
 			mBtnDescriptionMore.setTag(wi.description);
			int overLoadLen = 16;
			if (s.length() >= overLoadLen) {
				s = s.substring(0, overLoadLen - 1) + "...";
 				mBtnDescriptionMore.setVisibility(View.VISIBLE);
 			} else {
 				mBtnDescriptionMore.setVisibility(View.GONE);
 			}
 			mTextScreenName.setText(
 				String.format(
 					parent.getString(R.string.info_picture), 
 					wi.screen_name,
 					wi.location,
 					s
 				)
 			);
 		}
 		mImgVerified.setVisibility((wi.verified == 1 ? View.VISIBLE : View.GONE));
 		parent.getTextUpup().setText(wi.likes.toString());
 		parent.getTextDwdw().setText(wi.dislikes.toString());
 		int iTotalVotes = wi.likes + wi.dislikes;
 		int iPercentage = iTotalVotes <= 0 ? 0 : (wi.likes * 100 / iTotalVotes);
 		if (iTotalVotes <= 0) {
 			parent.getProgressVote().setSecondaryProgress(0);
 			parent.getProgressVote().setProgress(0);
 		} else {
 			parent.getProgressVote().setProgress(iPercentage);
 			parent.getProgressVote().setSecondaryProgress(100);
 		}
 		parent.getTextVoteRating().setText(
 			String.format(
 				parent.getString(R.string.tips_voterating), 
 				iPercentage, 
 				iTotalVotes
 			)
 		);
 		if (wi.mLastVote != 0) {
 			parent.getLayoutVoteInfo().setVisibility(LinearLayout.VISIBLE);
 			parent.getTextNoVoteTips().setVisibility(View.GONE);
 		} else {
 			parent.getLayoutVoteInfo().setVisibility(LinearLayout.GONE);
 			parent.getTextNoVoteTips().setVisibility(View.VISIBLE);
 		}
 		parent.getLayoutVote().setVisibility(RelativeLayout.VISIBLE);
 	}
 	
 	/*
 	 * show the next, previous or current picture following 2 parameters:
 	 * id, direction
 	 */
 	public void zrAsyncShowPic(long id, int direction) {
 		ArrayList<WeibouserInfo> usrs = parent.getMainPage().getUsrs();
 		if (usrs == null) return;
 		if (usrs.size() == 0) return;
 		AsyncPicLoader asyncPicLoader = new AsyncPicLoader(parent);
 		asyncPicLoader.execute(id, direction);
 	}
 	
 	public Boolean getWasPlaying() {
 		return wasPlaying();
 	}
 
 	public void setWasPlaying(Boolean mWasPlaying) {
 		this.setPlaying(mWasPlaying);
 	}
 
 	public GestureDetector getGestureDetector() {
 		return mGestureDetector;
 	}
 
 	public void setGestureDetector(GestureDetector mGestureDetector) {
 		this.mGestureDetector = mGestureDetector;
 	}
 
 	public FrameLayout getFrameBackground() {
 		return mFrameBackground;
 	}
 
 	public void setFrameBackground(FrameLayout mFrameBackground) {
 		this.mFrameBackground = mFrameBackground;
 	}
 
 	public ImageZoomView getBrow() {
 		return mBrow;
 	}
 
 	public void setBrow(ImageZoomView mBrow) {
 		this.mBrow = mBrow;
 	}
 
 	public RelativeLayout getLayoutCtrl() {
 		return mLayoutCtrl;
 	}
 
 	public void setLayoutCtrl(RelativeLayout mLayoutCtrl) {
 		this.mLayoutCtrl = mLayoutCtrl;
 	}
 
 	public int getReferer() {
 		return mReferer;
 	}
 
 	public void setReferer(int mReferer) {
 		this.mReferer = mReferer;
 	}
 
 	public Button getBtnPause() {
 		return btnPause;
 	}
 
 	public void setBtnPause(Button btnPause) {
 		this.btnPause = btnPause;
 	}
 	
 	public Button getBtnAtSomeone() {
 		return this.mBtnAtSomeone;
 	}
 	
 	public TextView getTextCounts_brow() {
 		return this.mTextCounts_brow;
 	}
 
 	public Boolean wasPlaying() {
 		return mWasPlaying;
 	}
 
 	public void setPlaying(Boolean mWasPlaying) {
 		this.mWasPlaying = mWasPlaying;
 	}
 
 	public static Boolean isLoading() {
 		return mIsLoading;
 	}
 
 	public void setLoading(Boolean mIsLoading) {
 		BrowPage.mIsLoading = mIsLoading;
 	}
 
 	public Button getBtnPlay() {
 		return btnPlay;
 	}
 
 	public void setBtnPlay(Button btnPlay) {
 		this.btnPlay = btnPlay;
 	}
 
 	public Boolean isDooming() {
 		return mIsDooming;
 	}
 
 	public void setDooming(Boolean mIsDooming) {
 		this.mIsDooming = mIsDooming;
 	}
 	
 	public void switchBrowseZoom(MenuItem item) {
 		if (parent.getString(R.string.label_zoom).equals(item.getTitle())) {
 			item.setTitle(parent.getString(R.string.label_browse));
 			mBrow.setOnTouchListener(mZoomListener);
 			mLayoutCtrl.setOnTouchListener(mZoomListener);
 			mIsDooming = true;
 		} else {
 			item.setTitle(parent.getString(R.string.label_zoom));
 			mBrow.setOnTouchListener(parent);
 			mLayoutCtrl.setOnTouchListener(parent);
 			mIsDooming = false;
 		}
 	}
 
 	/*
 	 * classes
 	 */
 	/*
 	 * implement asynchronized picture loading by using AsyncTask
 	 */
 	private class AsyncPicLoader extends AsyncTask<Object, Object, Bitmap> {
 		Context mContext;
 		Dialog mPrgDialog;
 		
 		public AsyncPicLoader(Context c) {
 			super();
 			mContext = c;
 			mPrgDialog = new Dialog(mContext, R.style.Dialog_CleanWithDim);
 			mPrgDialog.setContentView(R.layout.custom_dialog_loading);
 			((TextView) mPrgDialog.findViewById(R.id.tvCustomDialogTitle)).setText(parent.getString(R.string.msg_loading));
 			mPrgDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
 				public void onCancel(DialogInterface dialog) {
 					if (mTimer != null) {
 						mTimer.cancel();
 					}
 					getBtnPlay().setVisibility(ImageButton.VISIBLE);
 					getBtnPause().setVisibility(ImageButton.GONE);
 					setLoading(false);
 					dialog.cancel();
 				}
         	});
 			mPrgDialog.setCancelable(true);
 			WindowManager.LayoutParams lp = mPrgDialog.getWindow().getAttributes();
 	        lp.alpha = 1.0f;
 	        mPrgDialog.getWindow().setAttributes(lp);
 	        mPrgDialog.show();
 		}
 		
 		@Override
 		protected Bitmap doInBackground(Object... params) {
 			System.gc();
 			System.runFinalization();
 			System.gc();
 			
 			// TODO Auto-generated method stub
 			setLoading(true);
 			
 			if (params.length != 2) return bdPicFailed;
 			
 			Long id = (Long) params[0];
 			Integer direction = (Integer) params[1];
 			int idx = parent.getMainPage().getUsrIndexFromId(id, parent.getMainPage().getUsrs());
 			if (idx == -1) return bdPicFailed;
 			if (parent.getMainPage().getUsrs().size() == 0) return bdPicFailed;
 			switch (direction) {
 			default:
 			case 0:// means refresh
 				mId = id;
 				break;
 			case 1:// means previous
 				//zrTODO
 				break;
 			case 2:// means next
 				//zrTODO
 				break;
 			}
 			
 			WeibouserInfo wi = parent.getMainPage().getPicFromId(mId, parent.getMainPage().getUsrs());
 			String sPath, sFname;
 			sPath = AsyncSaver.getSdcardDir() + EntranceActivity.PATH_CACHE;
 			sFname = wi.uid + ".jg";
 
 			/*
 			 * we +1 for this click, and get back all the "likes, dislikes..."
 			 * kinda stuff from the sever with the function "vote" here.
 			 */
 			parent.vote(
 				"weibouserid", 
 				mId.toString(), 
 				"clientkey", 
 				EntranceActivity.getClientKey(), 
 				"vote", 
 				"2" //means a click
 			);
 			if (AsyncSaver.probeFile(sPath, sFname) == -2) {
 				Bitmap bmp = BitmapFactory.decodeFile(sPath + "/" + sFname);
 		    	return bmp == null ? bdPicFailed : bmp;
 			} else {
 				SecureURL su = new SecureURL();
 				URLConnection conn = su.getConnection(
 					wi.getBigger_profile_image_url()
 				);
 				InputStream is;
 				
 				if (conn == null) return bdPicFailed;
 				try {
 					is = conn.getInputStream();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					Log.e("DEBUGTAG", "Remtoe Image Exception", e);
 					e.printStackTrace();
 					return bdPicFailed;
 				}
 				
 				if (is == null) return bdPicFailed;
 				BufferedInputStream bis = new BufferedInputStream(is, 8192);
 				Bitmap bmp = BitmapFactory.decodeStream(bis);
 				if (bmp == null) return bdPicFailed;
 				try {
 					bis.close();
 					is.close();
 					bis = null;
 					is = null;
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					Log.e("DEBUGTAG", "Remtoe Image Exception", e);
 					e.printStackTrace();
 					bis = null;
 					is = null;
 					return bdPicFailed;
 				}
 				File file = AsyncSaver.getSilentFile(sPath, sFname);
 				if (file != null) {
 					AsyncSaver saver = new AsyncSaver(parent, bmp);
 					saver.saveImage(file);
 					saver = null;
 				}
 				/*
 				 * the marked block below was trying to get the "likes, dislikes..."
 				 * kinda info from some php script on the server which could provide
 				 * the info in the header of the http respondings.
 				String sLastVote = conn.getHeaderField("lastvote");
 				String sLastVoteTime = conn.getHeaderField("lastvotetime");
 				String sClicks = conn.getHeaderField("clicks");
 				String sLikes = conn.getHeaderField("likes");
 				String sDislikes = conn.getHeaderField("dislikes");
 				if (sLastVote != null && sLastVoteTime != null
 					&& sClicks != null && sLikes != null && sDislikes != null) {
 					wi.mLastVote = Integer.parseInt(sLastVote);
 					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 					ParsePosition pp = new ParsePosition(0);
 					wi.mLastVoteTime = sdf.parse(sLastVoteTime, pp);
 					wi.clicks = Integer.parseInt(sClicks);
 					wi.likes = Integer.parseInt(sLikes);
 					wi.dislikes = Integer.parseInt(sDislikes);
 				}
 				*/
 				return bmp;
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Bitmap result) {
 			// TODO Auto-generated method stub
 			mBrow.setImage(result);
 			resetZoomState();
 			fadeinAnim.setDuration(300);
 			mBrow.startAnimation(fadeinAnim);
 			//mBrow.setTag(result);
 			mPrgDialog.dismiss();
 			setLoading(false);
 			zrRenewCurFileInfo();
 			super.onPostExecute(result);
 		}
 
 		@Override
 		protected void onProgressUpdate(Object... values) {
 			// TODO Auto-generated method stub
 			super.onProgressUpdate(values);
 		}
 		
 		@Override
         protected void onCancelled() {
 			if (mTimer != null) {
 				mTimer.cancel();
 			}
 			getBtnPause().setVisibility(ImageButton.GONE);
 			getBtnPlay().setVisibility(ImageButton.VISIBLE);
 			setLoading(false);
             //super.onCancelled();
         }
 	}
 	
 	class PicbrowGestureListener extends GestureDetector.SimpleOnGestureListener {
 
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 			// TODO Auto-generated method stub
 			if (parent.getMainPage().getUsrs().size() == 0) {
 				Toast.makeText(parent, R.string.tips_nopictures, Toast.LENGTH_SHORT).show();
 			} else {
 				if (!isLoading()) {
 					if(e1.getX() > e2.getX()) {//move to left
 						/*
 						if (LetuseeActivity.getPicIndexFromId(mId, mUsrs) == mUsrs.size() - 1) {
 							Toast.makeText(PicbrowActivity.this, getString(R.string.tips_lastone), Toast.LENGTH_SHORT).show();
 						}
 						*/
 						zrAsyncShowPic(mId, 2);
 					} else if (e1.getX() < e2.getX()) {
 						/*
 						if (LetuseeActivity.getPicIndexFromId(mId, mUsrs) == 0) {
 							Toast.makeText(PicbrowActivity.this, getString(R.string.tips_firstone), Toast.LENGTH_SHORT).show();
 						}
 						*/
 						zrAsyncShowPic(mId, 1);
 					}
 				}
 			}
 			return super.onFling(e1, e2, velocityX, velocityY);
 		}
 
 		@Override
 		public boolean onDoubleTap(MotionEvent e) {
 			// TODO Auto-generated method stub
 			if (!isDooming()) {
 				Toast.makeText(
 					parent,
 					R.string.tips_howtobacktobrowse,
 					Toast.LENGTH_SHORT
 				).show();
 				
 				/*
 				ZoomState state = mZoomControl.getZoomState();
 				state.setZoom(state.getZoom() * (float)1.8);
 				state.notifyObservers();
 				mBrow.setOnTouchListener(mZoomListener);
 				mLayoutCtrl.setOnTouchListener(mZoomListener);
 				*/
 				
 				mZoomControl.zoom((float)Math.pow(20, 0.1), 0, 0);
 				getBrow().setOnTouchListener(mZoomListener);
 				getLayoutCtrl().setOnTouchListener(mZoomListener);
 				
 				setDooming(true);
 			}
 			
 			//return super.onDoubleTap(e);
 			return true;
 		}
 
 		@Override
 		public boolean onSingleTapConfirmed(MotionEvent e) {
 			// TODO Auto-generated method stub
 			//return super.onSingleTapConfirmed(e);
 			mBtnWeiboShow.performClick();
 			return true;
 		}
 
 		@Override
 		public boolean onSingleTapUp(MotionEvent e) {
 			// TODO Auto-generated method stub
 			
 			//return super.onSingleTapUp(e);
 			return false;
 		}
 		
 	}
 }
