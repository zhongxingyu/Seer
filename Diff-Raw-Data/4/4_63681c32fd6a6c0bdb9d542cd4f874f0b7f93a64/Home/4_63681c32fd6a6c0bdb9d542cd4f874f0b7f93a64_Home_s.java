 package smartcampus.android.template.standalone.HomeBlock;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import smartcampus.android.template.universiadi.R;
 import smartcampus.android.template.standalone.Activity.EventiBlock.InfoEventi;
 import smartcampus.android.template.standalone.Activity.FacilitiesBlock.Booking;
 import smartcampus.android.template.standalone.Activity.Model.ManagerData;
 import smartcampus.android.template.standalone.Activity.ProfileBlock.Profile;
 import smartcampus.android.template.standalone.Activity.ProfileBlock.CalendarSubBlock.FilterCalendarioActivity;
 import smartcampus.android.template.standalone.Activity.ProfileBlock.FAQSubBlock.FAQ;
 import smartcampus.android.template.standalone.Activity.SportBlock.Sport;
 import android.annotation.TargetApi;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.database.DataSetObserver;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.smartcampus.template.standalone.Evento;
 import android.smartcampus.template.standalone.Meeting;
 import android.smartcampus.template.standalone.Utente;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.GestureDetector;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.viewpagerindicator.CirclePageIndicator;
 
 public class Home extends FragmentActivity /* implements EventoUpdateListener */{
 
 	private ViewPager mPager;
 	private PagerAdapter mAdapter;
 
 	private List<Fragment> fragmentEventi = new ArrayList<Fragment>();
 	private List<Fragment> fragmentMeeting = new ArrayList<Fragment>();
 	private ArrayList<Evento> mListaEventiDiOggi = new ArrayList<Evento>();
 	private ArrayList<Meeting> mListaMeetingDiOggi = new ArrayList<Meeting>();
 
 	private CirclePageIndicator titleIndicator;
 
 	@Override
 	protected void onCreate(Bundle arg0) {
 		// TODO Auto-generated method stub
 		super.onCreate(arg0);
 		setContentView(R.layout.activity_home);
 
 		setupButton(getIntent().getBooleanExtra("sessionLogin", false));
 
 		new AsyncTask<Void, Void, Void>() {
 			private Dialog dialog;
 			private Map<String, Object> mResult;
 
 			@Override
 			protected void onPreExecute() {
 				// TODO Auto-generated method stub
 				super.onPreExecute();
 
 				dialog = new Dialog(Home.this);
 				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 				dialog.setContentView(R.layout.dialog_wait);
 				dialog.getWindow().setBackgroundDrawableResource(
 						R.drawable.dialog_rounded_corner_light_black);
 				dialog.show();
 				dialog.setCancelable(true);
 				dialog.setOnCancelListener(new OnCancelListener() {
 
 					@Override
 					public void onCancel(DialogInterface dialog) {
 						// TODO Auto-generated method stub
 						cancel(true);
 						finish();
 					}
 				});
 
 			}
 
 			@Override
 			protected Void doInBackground(Void... params) {
 				// TODO Auto-generated method stub
 				mResult = ManagerData.getEventiForData(Calendar.getInstance(
 						Locale.getDefault()).getTimeInMillis());
 				if (!((Boolean) mResult.get("connectionError"))) {
 					mListaEventiDiOggi = (ArrayList<Evento>) mResult
 							.get("params");
 					for (Evento evento : mListaEventiDiOggi)
 						fragmentEventi.add(new PageEventiOggi(evento,
 								fragmentEventi.size() - 1));
 
 					mListaMeetingDiOggi = (ArrayList<Meeting>) ManagerData
 							.getMeetingPerData(
 									Calendar.getInstance(Locale.getDefault())
 											.getTimeInMillis()).get("params");
 					for (Meeting meeting : mListaMeetingDiOggi)
 						fragmentMeeting.add(new PageEventiOggi(meeting,
 								fragmentMeeting.size() - 1));
 				}
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(Void result) {
 				// TODO Auto-generated method stub
 				super.onPostExecute(result);
 
 				dialog.dismiss();
 
 				// START ONPOST
 				if (!((Boolean) mResult.get("connectionError"))) {
 					// creating adapter and linking to view pager
 					if (mListaEventiDiOggi.size() != 0) {
 						if (mAdapter == null)
 							mAdapter = new PagerAdapter(
 									getSupportFragmentManager(), fragmentEventi);
 						else
 							mAdapter.fragments = fragmentEventi;
 						if (mPager == null)
 							mPager = (ViewPager) findViewById(R.id.pager_info_eventi);
 						mAdapter.notifyDataSetChanged();
 						mPager.setAdapter(mAdapter);
 						mPager.invalidate();
 						mPager.setVisibility(View.VISIBLE);
 						// Bind the title indicator to the adapter
 						titleIndicator = (CirclePageIndicator) findViewById(R.id.page_indicator);
 						titleIndicator.setViewPager(mPager);
 
 						class TapGestureListener extends
 								GestureDetector.SimpleOnGestureListener {
 
 							@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 							@Override
 							public boolean onSingleTapConfirmed(MotionEvent e) {
 								Intent mCaller = new Intent(getApplication(),
 										InfoEventi.class);
 								android.smartcampus.template.standalone.Evento evento = mListaEventiDiOggi
 										.get(mPager.getCurrentItem());
 								mCaller.putExtra("evento",
 										(Serializable) evento);
 								startActivity(mCaller);
 								return true;
 							}
 						}
 
 						final GestureDetector tapGestureDetector = new GestureDetector(
 								Home.this, new TapGestureListener());
 
 						mPager.setOnTouchListener(new OnTouchListener() {
 							public boolean onTouch(View v, MotionEvent event) {
 								tapGestureDetector.onTouchEvent(event);
 								return false;
 							}
 						});
 					} else
 						((TextView) findViewById(R.id.text_nessun_evento))
 								.setVisibility(View.VISIBLE);
 				} else {
 					((TextView) findViewById(R.id.text_nessun_evento))
 							.setVisibility(View.VISIBLE);
 					Dialog noConnection = new Dialog(Home.this);
 					noConnection.requestWindowFeature(Window.FEATURE_NO_TITLE);
 					noConnection.setContentView(R.layout.dialog_no_connection);
 					noConnection.getWindow().setBackgroundDrawableResource(
 							R.drawable.dialog_rounded_corner_light_black);
 					noConnection.setCancelable(true);
 					noConnection.show();
 					noConnection.setOnCancelListener(new OnCancelListener() {
 
 						@Override
 						public void onCancel(DialogInterface dialog) {
 							// TODO Auto-generated method stub
 							finish();
 						}
 					});
 				}
 
 				((EditText) findViewById(R.id.text_search))
 						.setTypeface(Typeface.createFromAsset(
 								getApplicationContext().getAssets(),
 								"PatuaOne-Regular.otf"));
 				((EditText) findViewById(R.id.text_search))
 						.setOnEditorActionListener(new OnEditorActionListener() {
 
 							@Override
 							public boolean onEditorAction(TextView v,
 									int actionId, KeyEvent event) {
 								// TODO Auto-generated method stub
 								if (actionId == EditorInfo.IME_ACTION_DONE) {
 									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 									imm.hideSoftInputFromWindow(
 											((EditText) findViewById(R.id.text_search))
 													.getWindowToken(), 0);
 									// Dialog general info
 									startFilterDialog();
 									return true;
 								}
 								return false;
 							}
 						});
 
 				((RelativeLayout) findViewById(R.id.btn_avvia_ricerca))
 						.setOnTouchListener(new OnTouchListener() {
 
 							@Override
 							public boolean onTouch(View v, MotionEvent event) {
 								// TODO Auto-generated method stub
 								if (event.getAction() == MotionEvent.ACTION_DOWN) {
 									((ImageView) findViewById(R.id.image_search))
 											.setImageResource(R.drawable.btn_main_cerca_press);
 									return true;
 								}
 								if (event.getAction() == MotionEvent.ACTION_UP) {
 									((ImageView) findViewById(R.id.image_search))
 											.setImageResource(R.drawable.btn_main_cerca);
 									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 									imm.hideSoftInputFromWindow(
 											((EditText) findViewById(R.id.text_search))
 													.getWindowToken(), 0);
 									// Dialog general info
 									startFilterDialog();
 
 									return true;
 								}
 								return false;
 							}
 						});
 
 				((ImageView) findViewById(R.id.image_today_meeting))
 						.setTag("pressed");
 				((ImageView) findViewById(R.id.image_today_meeting))
 						.setOnClickListener(new OnClickListener() {
 
 							@Override
 							public void onClick(View v) {
 								// TODO Auto-generated method stub
 								ImageView btn = (ImageView) findViewById(R.id.image_today_meeting);
 								if (((String) (btn.getTag()))
 										.equalsIgnoreCase("normal")) {
 									btn.setImageResource(R.drawable.btn_scroll_press);
 									btn.setTag("pressed");
 									((ImageView) findViewById(R.id.image_my_meeting))
 											.setImageResource(R.drawable.btn_scroll);
 									((ImageView) findViewById(R.id.image_my_meeting))
 											.setTag("normal");
 
 									if (mListaEventiDiOggi.size() != 0) {
 										((TextView) findViewById(R.id.text_nessun_evento))
 												.setVisibility(View.GONE);
 										mAdapter.fragments = fragmentEventi;
 										mAdapter.notifyDataSetChanged();
 										mPager.setVisibility(View.VISIBLE);
 										titleIndicator
 												.setVisibility(View.VISIBLE);
 										// ((RelativeLayout)
 										// findViewById(R.id.container_pager_eventi_oggi))
 										// .setBackgroundResource(R.drawable.scroll_main);
 									} else {
 										((TextView) findViewById(R.id.text_nessun_evento))
 												.setVisibility(View.VISIBLE);
 										mPager.setVisibility(View.GONE);
 										titleIndicator.setVisibility(View.GONE);
 										((RelativeLayout) findViewById(R.id.container_pager_eventi_oggi))
 												.setBackgroundResource(R.drawable.scroll_main);
 									}
 								}
 							}
 						});
 
 				((ImageView) findViewById(R.id.image_my_meeting))
 						.setTag("normal");
 				((ImageView) findViewById(R.id.image_my_meeting))
 						.setOnClickListener(new OnClickListener() {
 
 							@Override
 							public void onClick(View v) {
 								// TODO Auto-generated method stub
 								ImageView btn = (ImageView) findViewById(R.id.image_my_meeting);
 								if (((String) (btn.getTag()))
 										.equalsIgnoreCase("normal")) {
 									btn.setImageResource(R.drawable.btn_scroll_press);
 									btn.setTag("pressed");
 									((ImageView) findViewById(R.id.image_today_meeting))
 											.setImageResource(R.drawable.btn_scroll);
 									((ImageView) findViewById(R.id.image_today_meeting))
 											.setTag("normal");
 
 									// Change datasource in my meeting
 									if (mListaMeetingDiOggi.size() != 0) {
 										((TextView) findViewById(R.id.text_nessun_evento))
 												.setVisibility(View.GONE);
 										mAdapter.fragments = fragmentMeeting;
 										mAdapter.notifyDataSetChanged();
 										mPager.setVisibility(View.VISIBLE);
 										titleIndicator
 												.setVisibility(View.VISIBLE);
 										// ((RelativeLayout)
 										// findViewById(R.id.container_pager_eventi_oggi))
 										// .setBackgroundResource(R.drawable.scroll_main);
 									} else {
 										((TextView) findViewById(R.id.text_nessun_evento))
 												.setVisibility(View.VISIBLE);
 										mPager.setVisibility(View.GONE);
 										titleIndicator.setVisibility(View.GONE);
 										((RelativeLayout) findViewById(R.id.container_pager_eventi_oggi))
 												.setBackgroundResource(R.drawable.scroll_main);
 										// ((RelativeLayout)
 										// findViewById(R.id.container_pager_eventi_oggi))
 										// .setBackgroundResource(0);
 									}
 								}
 							}
 						});
 
 				((ImageView) findViewById(R.id.btn_sticker_credit))
 						.setOnClickListener(new OnClickListener() {
 
 							@Override
 							public void onClick(View v) {
 								// TODO Auto-generated method stub
 								startActivity(new Intent(Home.this,
 										Credits.class));
 							}
 						});
 				// END ONPOST
 			}
 
 		}.execute();
 
 	}
 
 	@Override
 	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(arg0, arg1, arg2);
 		((EditText) findViewById(R.id.text_search)).setText("");
 	}
 
 	@Override
 	public void onBackPressed() {
 		// TODO Auto-generated method stub
 		if (getIntent().getBooleanExtra("sessionLogin", false)) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle(getString(R.string.LOGOUT_TITLE));
 			builder.setMessage(getString(R.string.LOGOUT_TEXT));
 			builder.setCancelable(true);
 			builder.setPositiveButton(getString(R.string.LOGOUT_LOGOUT),
 					new DialogInterface.OnClickListener() {
 
 						public void onClick(DialogInterface dialog, int id) {
 
 							// TO-DO Invalidate Token
 							// ManagerData.invalidateToken();
 							dialog.dismiss();
 							finish();
 						}
 					});
 			builder.setNegativeButton(getString(R.string.LOGOUT_CANCEL),
 					new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface arg0, int arg1) {
 							// TODO Auto-generated method stub
 							arg0.dismiss();
 						}
 
 					});
 			AlertDialog alert = builder.create();
 			alert.show();
 
 		} else {
 			finish();
 		}
 	}
 
 	private void setupButton(boolean superUser) {
 		// *******SUPERUSER*********
 		if (superUser) {
 			((ImageView) findViewById(R.id.image_btn_1))
 					.setImageResource(R.drawable.btn_main_general);
 			((ImageView) findViewById(R.id.image_btn_1))
 					.setOnTouchListener(new OnTouchListener() {
 
 						@Override
 						public boolean onTouch(View v, MotionEvent event) {
 							// TODO Auto-generated method stub
 							if (event.getAction() == MotionEvent.ACTION_DOWN) {
 								((ImageView) findViewById(R.id.image_btn_1))
 										.setImageResource(R.drawable.btn_main_general_press);
 								return true;
 							}
 							if (event.getAction() == MotionEvent.ACTION_UP) {
 								((ImageView) findViewById(R.id.image_btn_1))
 										.setImageResource(R.drawable.btn_main_general);
 
 								startGeneralInfoDialog();
 								return true;
 							}
 							return false;
 						}
 					});
 
 			((ImageView) findViewById(R.id.image_btn_2))
 					.setImageResource(R.drawable.btn_main_staff);
 			((ImageView) findViewById(R.id.image_btn_2))
 					.setOnTouchListener(new OnTouchListener() {
 
 						@Override
 						public boolean onTouch(View v, MotionEvent event) {
 							// TODO Auto-generated method stub
 							if (event.getAction() == MotionEvent.ACTION_DOWN) {
 								((ImageView) findViewById(R.id.image_btn_2))
 										.setImageResource(R.drawable.btn_main_staff_press);
 								return true;
 							}
 							if (event.getAction() == MotionEvent.ACTION_UP) {
 								((ImageView) findViewById(R.id.image_btn_2))
 										.setImageResource(R.drawable.btn_main_staff);
 
 								startStaffToolDialog();
 								return true;
 							}
 							return false;
 						}
 					});
 
 			((ImageView) findViewById(R.id.image_btn_3))
 					.setImageResource(R.drawable.btn_main_profile);
 			((ImageView) findViewById(R.id.image_btn_3))
 					.setOnTouchListener(new OnTouchListener() {
 
 						@Override
 						public boolean onTouch(View v, MotionEvent event) {
 							// TODO Auto-generated method stub
 							if (event.getAction() == MotionEvent.ACTION_DOWN) {
 								((ImageView) findViewById(R.id.image_btn_3))
 										.setImageResource(R.drawable.btn_main_profile_press);
 								return true;
 							}
 							if (event.getAction() == MotionEvent.ACTION_UP) {
 								((ImageView) findViewById(R.id.image_btn_3))
 										.setImageResource(R.drawable.btn_main_profile);
 								startActivity(new Intent(
 										getApplicationContext(), Profile.class));
 								return true;
 							}
 							return false;
 						}
 					});
 		}
 		// ***********NORMALUSER***********
 		else {
 			((RelativeLayout) findViewById(R.id.container_button_meetings))
 					.setVisibility(View.INVISIBLE);
 
 			((ImageView) findViewById(R.id.image_btn_1))
 					.setImageResource(R.drawable.btn_main_event);
 			((ImageView) findViewById(R.id.image_btn_1))
 					.setOnTouchListener(new OnTouchListener() {
 
 						@Override
 						public boolean onTouch(View v, MotionEvent event) {
 							// TODO Auto-generated method stub
 							if (event.getAction() == MotionEvent.ACTION_DOWN) {
 								((ImageView) findViewById(R.id.image_btn_1))
 										.setImageResource(R.drawable.btn_main_event_press);
 								return true;
 							}
 							if (event.getAction() == MotionEvent.ACTION_UP) {
 								((ImageView) findViewById(R.id.image_btn_1))
 										.setImageResource(R.drawable.btn_main_event);
 								startActivity(new Intent(
 										getApplicationContext(),
 										smartcampus.android.template.standalone.Activity.EventiBlock.Evento.class));
 								return true;
 							}
 							return false;
 						}
 					});
 
 			((ImageView) findViewById(R.id.image_btn_2))
 					.setImageResource(R.drawable.btn_main_sport);
 			((ImageView) findViewById(R.id.image_btn_2))
 					.setOnTouchListener(new OnTouchListener() {
 
 						@Override
 						public boolean onTouch(View v, MotionEvent event) {
 							// TODO Auto-generated method stub
 							if (event.getAction() == MotionEvent.ACTION_DOWN) {
 								((ImageView) findViewById(R.id.image_btn_2))
 										.setImageResource(R.drawable.btn_main_sport_press);
 								return true;
 							}
 							if (event.getAction() == MotionEvent.ACTION_UP) {
 								((ImageView) findViewById(R.id.image_btn_2))
 										.setImageResource(R.drawable.btn_main_sport);
 								startActivity(new Intent(
 										getApplicationContext(), Sport.class));
 								return true;
 							}
 							return false;
 						}
 					});
 
 			((ImageView) findViewById(R.id.image_btn_3))
 					.setImageResource(R.drawable.btn_main_poi);
 			((ImageView) findViewById(R.id.image_btn_3))
 					.setOnTouchListener(new OnTouchListener() {
 
 						@Override
 						public boolean onTouch(View v, MotionEvent event) {
 							// TODO Auto-generated method stub
 							if (event.getAction() == MotionEvent.ACTION_DOWN) {
 								((ImageView) findViewById(R.id.image_btn_3))
 										.setImageResource(R.drawable.btn_main_poi_press);
 								return true;
 							}
 							if (event.getAction() == MotionEvent.ACTION_UP) {
 								((ImageView) findViewById(R.id.image_btn_3))
 										.setImageResource(R.drawable.btn_main_poi);
 								startActivity(new Intent(
 										getApplicationContext(), Booking.class));
 								return true;
 							}
 							return false;
 						}
 					});
 		}
 	}
 
 	private void startStaffToolDialog() {
 		Dialog dialog = new Dialog(Home.this);
 		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		dialog.setContentView(R.layout.dialog_staff_tool);
 		dialog.getWindow().setBackgroundDrawableResource(
 				R.drawable.dialog_rounded_corner);
 
 		final ImageView mFilterEvent = (ImageView) dialog
 				.findViewById(R.id.image_tool_faq);
 		mFilterEvent.setOnTouchListener(new OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				// TODO Auto-generated method stub
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					mFilterEvent
 							.setImageResource(R.drawable.btn_tool_faq_press);
 					return true;
 				}
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 					mFilterEvent.setImageResource(R.drawable.btn_tool_faq);
 
 					startActivity(new Intent(getApplicationContext(), FAQ.class));
 					return true;
 				}
 				return false;
 			}
 		});
 
 		final ImageView mFilterPoi = (ImageView) dialog
 				.findViewById(R.id.image_tool_helper);
 		mFilterPoi.setOnTouchListener(new OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				// TODO Auto-generated method stub
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					mFilterPoi
 							.setImageResource(R.drawable.btn_tool_helper_press);
 					return true;
 				}
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 					mFilterPoi.setImageResource(R.drawable.btn_tool_helper);
 
 					String url = getString(R.string.URL_ICE_AND_FIRE);
					if (!url.startsWith("http://"))
						url = "http://" + url;
 					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
 
 					// startActivity(new Intent(getApplicationContext(),
 					// Problema.class));
 					return true;
 				}
 				return false;
 			}
 		});
 
 		final ImageView mFilterSport = (ImageView) dialog
 				.findViewById(R.id.image_tool_planner);
 		mFilterSport.setOnTouchListener(new OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				// TODO Auto-generated method stub
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					mFilterSport
 							.setImageResource(R.drawable.btn_tool_planner_press);
 					return true;
 				}
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 					mFilterSport.setImageResource(R.drawable.btn_tool_planner);
 
 					startActivity(new Intent(getApplicationContext(),
 							FilterCalendarioActivity.class));
 					return true;
 				}
 				return false;
 			}
 		});
 
 		dialog.show();
 	}
 
 	private void startGeneralInfoDialog() {
 		Dialog dialog = new Dialog(Home.this);
 		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		dialog.setContentView(R.layout.dialog_general_info);
 		dialog.getWindow().setBackgroundDrawableResource(
 				R.drawable.dialog_rounded_corner);
 
 		final ImageView mFilterEvent = (ImageView) dialog
 				.findViewById(R.id.image_general_info_event);
 		mFilterEvent.setOnTouchListener(new OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				// TODO Auto-generated method stub
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					mFilterEvent
 							.setImageResource(R.drawable.btn_filter_event_press);
 					return true;
 				}
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 					mFilterEvent.setImageResource(R.drawable.btn_filter_event);
 
 					startActivity(new Intent(
 							getApplicationContext(),
 							smartcampus.android.template.standalone.Activity.EventiBlock.Evento.class));
 					return true;
 				}
 				return false;
 			}
 		});
 
 		final ImageView mFilterPoi = (ImageView) dialog
 				.findViewById(R.id.image_general_info_poi);
 		mFilterPoi.setOnTouchListener(new OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				// TODO Auto-generated method stub
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					mFilterPoi
 							.setImageResource(R.drawable.btn_filter_poi_press);
 					return true;
 				}
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 					mFilterPoi.setImageResource(R.drawable.btn_filter_poi);
 
 					startActivity(new Intent(getApplicationContext(),
 							Booking.class));
 					return true;
 				}
 				return false;
 			}
 		});
 
 		final ImageView mFilterSport = (ImageView) dialog
 				.findViewById(R.id.image_general_info_sport);
 		mFilterSport.setOnTouchListener(new OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				// TODO Auto-generated method stub
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					mFilterSport
 							.setImageResource(R.drawable.btn_filter_sport_press);
 					return true;
 				}
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 					mFilterSport.setImageResource(R.drawable.btn_filter_sport);
 
 					startActivity(new Intent(getApplicationContext(),
 							Sport.class));
 					return true;
 				}
 				return false;
 			}
 		});
 
 		dialog.show();
 	}
 
 	private void startFilterDialog() {
 
 		Dialog dialog = new Dialog(Home.this);
 		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		dialog.setContentView(R.layout.dialog_search);
 		dialog.getWindow().setBackgroundDrawableResource(
 				R.drawable.dialog_rounded_corner);
 
 		final ImageView mFilterEvent = (ImageView) dialog
 				.findViewById(R.id.image_filter_event);
 		mFilterEvent.setOnTouchListener(new OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				// TODO Auto-generated method stub
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					mFilterEvent
 							.setImageResource(R.drawable.btn_filter_event_press);
 					return true;
 				}
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 
 					try {
 						mFilterEvent
 								.setImageResource(R.drawable.btn_filter_event);
 
 						Intent mCaller = new Intent(getApplicationContext(),
 								ResultSearch.class);
 						mCaller.putExtra("rest", "/evento/search");
 						JSONObject obj = new JSONObject();
 						obj.put("tipoFiltro", "SPORT");
 						obj.put("nome", "re");
 						// obj.put("nome",((EditText)
 						// findViewById(R.id.text_search))
 						// .getText().toString()));
 						mCaller.putExtra("search", obj.toString());
 						startActivity(mCaller);
 					} catch (JSONException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					return true;
 				}
 				return false;
 			}
 		});
 
 		final ImageView mFilterPoi = (ImageView) dialog
 				.findViewById(R.id.image_filter_poi);
 		mFilterPoi.setOnTouchListener(new OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				// TODO Auto-generated method stub
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					mFilterPoi
 							.setImageResource(R.drawable.btn_filter_poi_press);
 					return true;
 				}
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 
 					try {
 						mFilterPoi.setImageResource(R.drawable.btn_filter_poi);
 
 						Intent mCaller = new Intent(getApplicationContext(),
 								ResultSearch.class);
 						mCaller.putExtra("rest", "/evento/search");
 						JSONObject obj = new JSONObject();
 						obj.put("tipoFiltro", "SPORT");
 						obj.put("nome", "re");
 						// obj.put("nome",(((EditText)
 						// findViewById(R.id.text_search))
 						// .getText().toString());
 						mCaller.putExtra("search", obj.toString());
 						startActivity(mCaller);
 					} catch (JSONException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					return true;
 				}
 				return false;
 			}
 		});
 
 		final ImageView mFilterSport = (ImageView) dialog
 				.findViewById(R.id.image_filter_sport);
 		mFilterSport.setOnTouchListener(new OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				// TODO Auto-generated method stub
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					mFilterSport
 							.setImageResource(R.drawable.btn_filter_sport_press);
 					return true;
 				}
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 					try {
 						mFilterSport
 								.setImageResource(R.drawable.btn_filter_sport);
 
 						Intent mCaller = new Intent(getApplicationContext(),
 								ResultSearch.class);
 						mCaller.putExtra("rest", "/evento/search");
 						JSONObject obj = new JSONObject();
 						obj.put("tipoFiltro", "SPORT");
 						obj.put("nome", "re");
 						// obj.put("nome",(((EditText)
 						// findViewById(R.id.text_search))
 						// .getText().toString());
 						mCaller.putExtra("search", obj.toString());
 						startActivity(mCaller);
 					} catch (JSONException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					return true;
 				}
 				return false;
 			}
 		});
 
 		dialog.show();
 	}
 
 	private class PagerAdapter extends FragmentStatePagerAdapter {
 
 		// fragments to instantiate in the viewpager
 		private List<Fragment> fragments;
 
 		// constructor
 		public PagerAdapter(FragmentManager fm, List<Fragment> fragments) {
 			super(fm);
 			this.fragments = fragments;
 		}
 
 		@Override
 		public void notifyDataSetChanged() {
 			// TODO Auto-generated method stub
 			super.notifyDataSetChanged();
 		}
 
 		@Override
 		public void registerDataSetObserver(DataSetObserver observer) {
 			// TODO Auto-generated method stub
 			super.registerDataSetObserver(observer);
 		}
 
 		// return access to fragment from position, required override
 		@Override
 		public Fragment getItem(int position) {
 			return this.fragments.get(position);
 		}
 
 		// number of fragments in list, required override
 		@Override
 		public int getCount() {
 			return this.fragments.size();
 		}
 
 		@Override
 		public int getItemPosition(Object object) {
 			return POSITION_NONE;
 		}
 	}
 
 	// @Override
 	// public void onEventiUpdate(Evento evento) {
 	// // TODO Auto-generated method stub
 	// mAdapter.fragments
 	// .add(new PageEventiOggi(evento, fragments.size() - 1));
 	// mAdapter.notifyDataSetChanged();
 	// }
 	//
 	// @Override
 	// protected void onCreate(Bundle arg0) {
 	// // TODO Auto-generated method stub
 	//
 	// setupButton();
 	//
 	//
 	// Bind the title indicator to the adapter
 	// CirclePageIndicator circleIndicator = (CirclePageIndicator)
 	// findViewById(R.id.page_indicator);
 	// circleIndicator.setViewPager(mPager);
 	// circleIndicator.setCurrentItem(0);
 	// if (mAdapter.getCount() == 0 || mAdapter.getCount() == 1)
 	// circleIndicator.setVisibility(View.INVISIBLE);
 	// else
 	// circleIndicator.setVisibility(View.VISIBLE);
 	//
 	// if (Intro.needDWN) {
 	//
 	// } else {
 	// ArrayList<Evento> mLista = (ArrayList<Evento>) database
 	// .getEventoPerAmbitoERuolo("", -1);
 	// for (Evento evento : mLista)
 	// mAdapter.fragments.add(new PageEventiOggi(evento, fragments.size() -
 	// 1));
 	// mPager.setAdapter(mAdapter);
 	// }
 	//
 	// ArrayList<Evento> mListaEventi = (ArrayList<Evento>) database
 	// .getEventoPerAmbitoERuolo("", -1);
 	//
 	// try
 	// {
 	//
 	// }
 	// catch(JSONException e)
 	// {
 	// try {
 	// Log.i("Nome " + response.getString("id"),
 	// response.getString("nome"));
 	// Log.i("Descrizione " + response.getString("id"),
 	// response.getString("descrizione"));
 	// Log.i("Data " + response.getString("id"),
 	// response.getString("data"));
 	// } catch (JSONException e1) {
 	// // TODO Auto-generated catch block
 	// e1.printStackTrace();
 	// }
 	// }
 	//
 	//
 	// rotateTimer = new Timer();
 	// rotateTimer.schedule(new TimerTask(){
 	//
 	// @Override
 	// public void run() {
 	// // TODO Auto-generated method stub
 	// if (goUp)
 	// {
 	// countPage++;
 	// if (countPage != mPager.getChildCount())
 	// goUp = true;
 	// else
 	// goUp = false;
 	// }
 	// else
 	// {
 	// countPage--;
 	// if (countPage == 0)
 	// goUp = false;
 	// else
 	// goUp = true;
 	// }
 	// mPager.setCurrentItem(countPage, true);
 	// }
 	// }, 0, 5000);
 	//
 	// ((EditText) findViewById(R.id.text_search))
 	// .setOnEditorActionListener(new OnEditorActionListener() {
 	//
 	// @Override
 	// public boolean onEditorAction(TextView v, int actionId,
 	// KeyEvent event) {
 	// // TODO Auto-generated method stub
 	// if (actionId == EditorInfo.IME_ACTION_DONE) {
 	// AlertDialog.Builder builder;
 	// LayoutInflater inflater = (LayoutInflater) getApplicationContext()
 	// .getSystemService(LAYOUT_INFLATER_SERVICE);
 	// final View layout = inflater
 	// .inflate(
 	// R.layout.dialog_search,
 	// (ViewGroup) findViewById(R.id.container_home));
 	// builder = new AlertDialog.Builder(Home.this);
 	// builder.setView(layout);
 	// final AlertDialog alert = builder.create();
 	// alert.show();
 	//
 	// ((ImageView) layout
 	// .findViewById(R.id.image_filter_event))
 	// .setOnTouchListener(new OnTouchListener() {
 	//
 	// @Override
 	// public boolean onTouch(View v,
 	// MotionEvent event) {
 	// // TODO Auto-generated method stub
 	// if (event.getAction() == MotionEvent.ACTION_DOWN) {
 	// ((ImageView) layout
 	// .findViewById(R.id.image_filter_event))
 	// .setImageResource(R.drawable.btn_filter_event_press);
 	// return true;
 	// }
 	// if (event.getAction() == MotionEvent.ACTION_UP) {
 	// ((ImageView) layout
 	// .findViewById(R.id.image_filter_event))
 	// .setImageResource(R.drawable.btn_filter_event);
 	//
 	// Intent mCaller = new Intent(
 	// getApplicationContext(),
 	// ResultSearch.class);
 	// mCaller.putExtra("rest",
 	// "search/eventi");
 	// mCaller.putExtra(
 	// "filter",
 	// ((EditText) findViewById(R.id.text_search))
 	// .getText()
 	// .toString());
 	// startActivity(mCaller);
 	// return true;
 	// }
 	// return false;
 	// }
 	// });
 	//
 	// ((ImageView) layout
 	// .findViewById(R.id.image_filter_poi))
 	// .setOnTouchListener(new OnTouchListener() {
 	//
 	// @Override
 	// public boolean onTouch(View v,
 	// MotionEvent event) {
 	// // TODO Auto-generated method stub
 	// if (event.getAction() == MotionEvent.ACTION_DOWN) {
 	// ((ImageView) layout
 	// .findViewById(R.id.image_filter_event))
 	// .setImageResource(R.drawable.btn_filter_poi_press);
 	// return true;
 	// }
 	// if (event.getAction() == MotionEvent.ACTION_UP) {
 	// ((ImageView) layout
 	// .findViewById(R.id.image_filter_event))
 	// .setImageResource(R.drawable.btn_filter_poi);
 	//
 	// Intent mCaller = new Intent(
 	// getApplicationContext(),
 	// ResultSearch.class);
 	// mCaller.putExtra("rest",
 	// "search/poi");
 	// mCaller.putExtra(
 	// "filter",
 	// ((EditText) findViewById(R.id.text_search))
 	// .getText()
 	// .toString());
 	// startActivity(mCaller);
 	// return true;
 	// }
 	// return false;
 	// }
 	// });
 	//
 	// ((ImageView) layout
 	// .findViewById(R.id.image_filter_sport))
 	// .setOnTouchListener(new OnTouchListener() {
 	//
 	// @Override
 	// public boolean onTouch(View v,
 	// MotionEvent event) {
 	// // TODO Auto-generated method stub
 	// if (event.getAction() == MotionEvent.ACTION_DOWN) {
 	// ((ImageView) layout
 	// .findViewById(R.id.image_filter_sport))
 	// .setImageResource(R.drawable.btn_filter_sport_press);
 	// return true;
 	// }
 	// if (event.getAction() == MotionEvent.ACTION_UP) {
 	// ((ImageView) layout
 	// .findViewById(R.id.image_filter_sport))
 	// .setImageResource(R.drawable.btn_filter_sport);
 	//
 	// Intent mCaller = new Intent(
 	// getApplicationContext(),
 	// ResultSearch.class);
 	// mCaller.putExtra("rest",
 	// "search/sport");
 	// mCaller.putExtra(
 	// "filter",
 	// ((EditText) findViewById(R.id.text_search))
 	// .getText()
 	// .toString());
 	// startActivity(mCaller);
 	// return true;
 	// }
 	// return false;
 	// }
 	// });
 	// }
 	// }
 	// });
 	//
 	//
 	// }
 
 	// private void setupButton() {
 	// mEventi = (ImageView) findViewById(R.id.image_eventi);
 	// mEventiLogoUp = (ImageView) findViewById(R.id.image_eventi_up);
 	// mEventiLogoDown = (ImageView) findViewById(R.id.image_eventi_down);
 	// mEventi.setOnTouchListener(new OnTouchListener() {
 	//
 	// @Override
 	// public boolean onTouch(View arg0, MotionEvent arg1) {
 	// // TODO Auto-generated method stub
 	// if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
 	// mEventi.setImageResource(R.drawable.button_events_down);
 	//
 	// AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
 	// fadeOut.setDuration(300);
 	// fadeOut.setFillAfter(true);
 	// fadeOut.setAnimationListener(new AnimationListener() {
 	//
 	// @Override
 	// public void onAnimationEnd(Animation animation) {
 	// // TODO Auto-generated method stub
 	// }
 	//
 	// @Override
 	// public void onAnimationRepeat(Animation animation) {
 	// // TODO Auto-generated method stub
 	//
 	// }
 	//
 	// @Override
 	// public void onAnimationStart(Animation animation) {
 	// // TODO Auto-generated method stub
 	// mEventiLogoDown.setVisibility(View.VISIBLE);
 	// AlphaAnimation fadeIn = new AlphaAnimation(0.0f,
 	// 1.0f);
 	// fadeIn.setDuration(300);
 	// fadeIn.setFillAfter(true);
 	// mEventiLogoDown.startAnimation(fadeIn);
 	// }
 	//
 	// });
 	// mEventiLogoUp.startAnimation(fadeOut);
 	//
 	// return true;
 	// }
 	// if (arg1.getAction() == MotionEvent.ACTION_UP) {
 	// startActivity(new Intent(
 	// arg0.getContext(),
 	// smartcampus.android.template.standalone.Activity.EventiBlock.Evento.class));
 	// return true;
 	// }
 	// return false;
 	// }
 	// });
 	//
 	// mSport = (ImageView) findViewById(R.id.image_sport);
 	// mSportLogoUp = (ImageView) findViewById(R.id.image_sport_up);
 	// mSportLogoDown = (ImageView) findViewById(R.id.image_sport_down);
 	// mSport.setOnTouchListener(new OnTouchListener() {
 	//
 	// @Override
 	// public boolean onTouch(View arg0, MotionEvent arg1) {
 	// // TODO Auto-generated method stub
 	// if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
 	// mSport.setImageResource(R.drawable.button_sports_down);
 	//
 	// AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
 	// fadeOut.setDuration(300);
 	// fadeOut.setFillAfter(true);
 	// fadeOut.setAnimationListener(new AnimationListener() {
 	//
 	// @Override
 	// public void onAnimationEnd(Animation animation) {
 	// // TODO Auto-generated method stub
 	// }
 	//
 	// @Override
 	// public void onAnimationRepeat(Animation animation) {
 	// // TODO Auto-generated method stub
 	//
 	// }
 	//
 	// @Override
 	// public void onAnimationStart(Animation animation) {
 	// // TODO Auto-generated method stub
 	// mSportLogoDown.setVisibility(View.VISIBLE);
 	// AlphaAnimation fadeIn = new AlphaAnimation(0.0f,
 	// 1.0f);
 	// fadeIn.setDuration(300);
 	// fadeIn.setFillAfter(true);
 	// mSportLogoDown.startAnimation(fadeIn);
 	// }
 	//
 	// });
 	// mSportLogoUp.startAnimation(fadeOut);
 	//
 	// return true;
 	// }
 	// if (arg1.getAction() == MotionEvent.ACTION_UP) {
 	// startActivity(new Intent(arg0.getContext(), Sport.class));
 	// return true;
 	// }
 	// return false;
 	// }
 	// });
 	//
 	// mFacilities = (ImageView) findViewById(R.id.image_facilities);
 	// mFacilitiesLogoUp = (ImageView) findViewById(R.id.image_facilities_up);
 	// mFacilitiesLogoDown = (ImageView)
 	// findViewById(R.id.image_facilities_down);
 	// mFacilities.setOnTouchListener(new OnTouchListener() {
 	//
 	// @Override
 	// public boolean onTouch(View arg0, MotionEvent arg1) {
 	// // TODO Auto-generated method stub
 	// if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
 	// mFacilities
 	// .setImageResource(R.drawable.button_facilities_down);
 	//
 	// AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
 	// fadeOut.setDuration(300);
 	// fadeOut.setFillAfter(true);
 	// fadeOut.setAnimationListener(new AnimationListener() {
 	//
 	// @Override
 	// public void onAnimationEnd(Animation animation) {
 	// // TODO Auto-generated method stub
 	// }
 	//
 	// @Override
 	// public void onAnimationRepeat(Animation animation) {
 	// // TODO Auto-generated method stub
 	//
 	// }
 	//
 	// @Override
 	// public void onAnimationStart(Animation animation) {
 	// // TODO Auto-generated method stub
 	// mFacilitiesLogoDown.setVisibility(View.VISIBLE);
 	// AlphaAnimation fadeIn = new AlphaAnimation(0.0f,
 	// 1.0f);
 	// fadeIn.setDuration(300);
 	// fadeIn.setFillAfter(true);
 	// mFacilitiesLogoDown.startAnimation(fadeIn);
 	// }
 	//
 	// });
 	// mFacilitiesLogoUp.startAnimation(fadeOut);
 	//
 	// return true;
 	// }
 	// if (arg1.getAction() == MotionEvent.ACTION_UP) {
 	// // SetImage
 	// startActivity(new Intent(arg0.getContext(), Booking.class));
 	// return true;
 	// }
 	// return false;
 	// }
 	// });
 	//
 	// mProfile = (ImageView) findViewById(R.id.image_profilo_utente);
 	// mProfile.setOnTouchListener(new OnTouchListener() {
 	//
 	// @Override
 	// public boolean onTouch(View arg0, MotionEvent arg1) {
 	// // TODO Auto-generated method stub
 	// if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
 	// mProfile.setImageResource(R.drawable.button_profile_down);
 	// return true;
 	// }
 	// if (arg1.getAction() == MotionEvent.ACTION_UP) {
 	// // SetImage
 	// mProfile.setImageResource(R.drawable.button_profile_up);
 	// startActivity(new Intent(arg0.getContext(), Profile.class));
 	// return true;
 	// }
 	// return false;
 	// }
 	// });
 	// }
 
 }
