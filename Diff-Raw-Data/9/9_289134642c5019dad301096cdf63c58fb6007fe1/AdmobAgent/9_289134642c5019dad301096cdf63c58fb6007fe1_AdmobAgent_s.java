 package com.nemogames;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.location.Location;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewTreeObserver.OnGlobalLayoutListener;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout.LayoutParams;
 
 import com.google.ads.Ad;
 import com.google.ads.AdListener;
 import com.google.ads.AdRequest;
 import com.google.ads.AdRequest.ErrorCode;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 import com.google.ads.InterstitialAd;
 import com.unity3d.player.UnityPlayer;
 
 public class AdmobAgent implements AdListener
 {
 	public static int		FullscreenBannerID = -1000;
 	private String		ListenerGameObject = "";
 	private String		ListenerFunction = "";
 	private Activity	RootActivity;
 	private Hashtable<Integer, Ad>	banners;
 	private String		Birthdate_Year, Birthdate_Month, Birthdate_Day;
 	private String		Gender;
 	private String		Location;
 	private boolean		CustomRequst = false;
 	private boolean		inited = false;
 	private boolean		fullscreen_ready = false;
 	private InterstitialAd		fullScreenBanner;
 	private FrameLayout		RootLayout = null;
 	
 	public enum		AdmobBannerSize
 	{
 		SmartBanner(1),
 		MMAStandardBanner(2),
 		IABStandardBanner(3),
 		IABMRect(4),
 		WideSkyScraper(5),
 		IABLeaderboard(6);
 
 		int value;
 		AdmobBannerSize(int val) { value = val; }
 		public int	getValue() { return value; }
 	}
 	
 	public enum		AdmobEvent
 	{
 		OnDismissScreen(1),
 		OnFailedToReceiveAd(2),
 		OnLeaveApplication(3),
 		OnPresentScreen(4),
 		OnReceiveAd(5),
 		OnFullscreenAdReady(6);
 		
 		int value;
 		AdmobEvent(int val) { value = val; }
 		public int	getValue() { return value; }
 	}
 	
 	public void		init(String gameobject, String function)
 	{
 		this.RootActivity = UnityPlayer.currentActivity;
 		this.ListenerGameObject = gameobject;
 		this.ListenerFunction = function;
 		this.inited = true;
 		this.banners = new Hashtable<Integer, Ad>();
 	}
 	
 	public void		CreateBanner(final int iid, final int banner_size, final String uid)
 	{
 		if (banners.contains(iid))
 		{
 			Log.w("Nemo - AdmobAgent", "Banner with id: " + iid + " already exist");
 		} else
 		{
 			RootActivity.runOnUiThread(new Runnable()
 			{
 				@Override
 				public void run() 
 				{
 					Log.d("Nemo - AdmobAgent", "Creating banner with id: " + iid + "...");
 					AdView ad = new AdView(RootActivity, getAdSizeByEnumeration(banner_size), uid);
 					ad.setAdListener(AdmobAgent.this);
 					if (RootLayout == null)
 					{
 						RootLayout = new FrameLayout(RootActivity);
 						RootActivity.getWindow().addContentView(RootLayout, new 
 								LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 					}
 					RootLayout.addView(ad, new 
 							FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
 					ad.loadAd(AdmobAgent.this.getAdRequest());
 					AdmobAgent.this.banners.put(iid, ad);
 				}
 			});
 		}
 	}
 	
 	public void		SetBannerPosition(final int iid, final int left, final int top)
 	{
 		if (banners.contains(iid))
 		{
 			Log.w("Nemo - AdmobAgent", "Banner with id: " + iid + " already exist");
 		} else
 		{
 			RootActivity.runOnUiThread(new Runnable()
 			{
 				@Override
 				public void run() 
 				{
 					AdView ad = (AdView)banners.get(iid);
 					FrameLayout.LayoutParams params = new 
 							FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
 					params.leftMargin = left;
 					params.topMargin = top;
 					ad.setLayoutParams(params);
 				}
 			});
 		}
 	}
 	
 	public void		SetBannerVisiblity(final int iid, final boolean visible)
 	{
 		if (banners.contains(iid))
 		{
 			Log.w("Nemo - AdmobAgent", "Banner with id: " + iid + " already exist");
 		} else
 		{
 			RootActivity.runOnUiThread(new Runnable()
 			{
 				@Override
 				public void run() 
 				{
 					AdView ad = (AdView)banners.get(iid);
 					ad.setVisibility((visible? View.VISIBLE:View.INVISIBLE));
 				}
 			});
 		}
 	}
 	
 	public void		RequestFreshAd(final int iid)
 	{
 		if (banners.contains(iid))
 		{
 			Log.w("Nemo - AdmobAgent", "Banner with id: " + iid + " already exist");
 		} else
 		{
 			RootActivity.runOnUiThread(new Runnable()
 			{
 				@Override
 				public void run() 
 				{
 					AdView ad = (AdView)banners.get(iid);
 					ad.loadAd(getAdRequest());
 					ad.setVisibility(View.VISIBLE);
 				}
 			});
 		}
 	}
 	
 	public void		DestroyBanner(final int iid)
 	{
 		RootActivity.runOnUiThread(new Runnable()
 		{
 			@Override
 			public void run() 
 			{
 				AdView ad = (AdView)banners.get(iid);
 				RootLayout.removeView(ad);
 				ad.destroy();
 			}
 		});
 	}
 	
 	public void		RequestFullscreenAd(final String uid)
 	{
 		RootActivity.runOnUiThread(new Runnable()
 		{
 			@Override
 			public void run() 
 			{
 				if (AdmobAgent.this.fullScreenBanner == null)
 				{
 					AdmobAgent.this.fullScreenBanner = new InterstitialAd(AdmobAgent.this.RootActivity, uid);
 					AdmobAgent.this.fullScreenBanner.setAdListener(AdmobAgent.this);
 				}
 				AdmobAgent.this.fullscreen_ready = false;
 				AdmobAgent.this.fullScreenBanner.loadAd(AdmobAgent.this.getAdRequest());
 			}
 		});
 	}
 	
 	
 	public void		ShowFullscreenAd()
 	{
 		if (this.isFullscreenAdReady())
 			AdmobAgent.this.fullScreenBanner.show();
 	}
 
 	public boolean	isFullscreenAdReady() { return this.fullscreen_ready; }
 	public void		SetLocation(String location) { this.Location = location; }
 	public void		SetBirthdate(String year, String month, String day) 
 	{ this.Birthdate_Day = day; this.Birthdate_Month = month; this.Birthdate_Year = year; }
 	public void		SetGender(String gender) { this.Gender = gender; }
 	public void		SetCustomRequestStatus(boolean status) { this.CustomRequst = status; }
 	
 	//--------------------------------------------- private
 	private static AdSize	getAdSizeByEnumeration(int size)
 	{
 		switch (size)
 		{
 		case 1: return AdSize.SMART_BANNER;
 		case 2: return AdSize.BANNER;
 		case 3: return AdSize.IAB_BANNER;
 		case 4: return AdSize.IAB_MRECT;
 		case 5: return AdSize.IAB_WIDE_SKYSCRAPER;
 		case 6: return AdSize.IAB_LEADERBOARD;
 		}
 		return null;
 	}
 	private int		getBannerID(Ad banner)
 	{
 		Enumeration<Integer> keys = banners.keys();
 		while (keys.hasMoreElements())
 		{
 			int bid = keys.nextElement();
 			if (banners.get(bid) == banner) return bid;
 		}
 		return -1;
 	}
 	@SuppressWarnings("deprecation")
 	private AdRequest		getAdRequest()
 	{
 		if (this.CustomRequst)
 		{
 			AdRequest rq = new AdRequest();
 			rq.setLocation(new Location(this.Location));
 			rq.setGender(((this.Gender=="Female")? AdRequest.Gender.FEMALE:AdRequest.Gender.MALE));
 			rq.setBirthday(Birthdate_Year+Birthdate_Month+Birthdate_Day);
 			return rq;
 		} else return new AdRequest();
 	}
 	private void	SendListenerEvent(String json)
 	{
 		if (!inited) return;
 		UnityPlayer.UnitySendMessage(ListenerGameObject, ListenerFunction, json);
 	}
 	private void	SendAdmobEvent(AdmobEvent event, int iid)
 	{
 		JSONObject obj = new JSONObject();
 		try
 		{
 			obj.put("eid", event.getValue());
 			obj.put("iid", iid);
 		} catch (JSONException e) { e.printStackTrace(); } finally
 		{
 			SendListenerEvent(obj.toString());
 		}
 	}
 	private void	SendAdmobEvent(AdmobEvent event, int iid, String error)
 	{
 		JSONObject obj = new JSONObject();
 		try
 		{
 			obj.put("eid", event.getValue());
 			obj.put("iid", iid);
 			obj.put("error", error);
 		} catch (JSONException e) { e.printStackTrace(); } finally
 		{
 			SendListenerEvent(obj.toString());
 		}
 	}
 	private void	SendAdmobEvent(AdmobEvent event, int iid, int width, int height)
 	{
 		JSONObject obj = new JSONObject();
 		try
 		{
 			obj.put("eid", event.getValue());
 			obj.put("iid", iid);
 			obj.put("width", width);
 			obj.put("height", height);
 		} catch (JSONException e) { e.printStackTrace(); } finally
 		{
 			SendListenerEvent(obj.toString());
 		}
 	}
 	//--------------------------------------------- AdListener
 	@Override
 	public void onDismissScreen(Ad arg0) 
 	{
 		Log.d("Nemo - AdmobAgent", "onDismissScreen");
 		if (arg0 != this.fullScreenBanner)
 		{
 			SendAdmobEvent(AdmobEvent.OnDismissScreen, this.getBannerID(arg0));
 		} else
 		{
 			SendAdmobEvent(AdmobEvent.OnDismissScreen, FullscreenBannerID);
 		}
 	}
 
 	@Override
 	public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) 
 	{
 		Log.d("Nemo - AdmobAgent", "onFailedToReceiveAd: " + arg1.toString());
 		if (arg0 != this.fullScreenBanner)
 		{
 			SendAdmobEvent(AdmobEvent.OnFailedToReceiveAd, this.getBannerID(arg0), arg1.toString());
 		} else
 		{
 			SendAdmobEvent(AdmobEvent.OnFailedToReceiveAd, FullscreenBannerID, arg1.toString());
 		}
 	}
 
 	@Override
 	public void onLeaveApplication(Ad arg0) 
 	{
 		Log.d("Nemo - AdmobAgent", "onLeaveApplication");
 		if (arg0 != this.fullScreenBanner)
 		{
 			SendAdmobEvent(AdmobEvent.OnLeaveApplication, this.getBannerID(arg0));
 		} else
 		{
 			SendAdmobEvent(AdmobEvent.OnLeaveApplication, FullscreenBannerID);
 		}
 			
 	}
 
 	@Override
 	public void onPresentScreen(Ad arg0) 
 	{
 		Log.d("Nemo - AdmobAgent", "onPresentScreen");
 		if (arg0 != this.fullScreenBanner)
 		{
 			SendAdmobEvent(AdmobEvent.OnPresentScreen, this.getBannerID(arg0));
 		} else
 		{
 			SendAdmobEvent(AdmobEvent.OnPresentScreen, FullscreenBannerID);
 		}
 	}
 
 	@Override
 	public void onReceiveAd(Ad ad) 
 	{
 		Log.d("Nemo - AdmobAgent", "onReceiveAd");
 		if (ad != this.fullScreenBanner)
 		{
 			final AdView view = (AdView)ad;
 			if (view.getViewTreeObserver().isAlive())
 			{
 				view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() 
 				{
 					@Override
 					public void onGlobalLayout() 
 					{
						view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
 						SendAdmobEvent(AdmobEvent.OnReceiveAd,
 								AdmobAgent.this.getBannerID(view), (view).getWidth(), (view).getHeight());
 					}					
 				});
 			} else
 				SendAdmobEvent(AdmobEvent.OnReceiveAd,
 						this.getBannerID(ad), ((AdView)ad).getWidth(), ((AdView)ad).getHeight());
 		} else
 		{
 			this.fullscreen_ready = true;
 			SendAdmobEvent(AdmobEvent.OnReceiveAd, FullscreenBannerID);
 		}
 	}
 }
