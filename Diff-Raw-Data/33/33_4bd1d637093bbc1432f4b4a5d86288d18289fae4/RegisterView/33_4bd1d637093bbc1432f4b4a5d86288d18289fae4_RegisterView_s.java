 package com.osastudio.newshub.widgets;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.huadi.azker_phone.R;
 import com.osastudio.newshub.data.NewsBaseObject;
 import com.osastudio.newshub.data.NewsObjectList;
 import com.osastudio.newshub.data.NewsResult;
 import com.osastudio.newshub.data.PairedStringFieldsObject;
 import com.osastudio.newshub.data.user.City;
 import com.osastudio.newshub.data.user.CityDistrict;
 import com.osastudio.newshub.data.user.CityDistrictList;
 import com.osastudio.newshub.data.user.CityList;
 import com.osastudio.newshub.data.user.Qualification;
 import com.osastudio.newshub.data.user.QualificationList;
 import com.osastudio.newshub.data.user.RegisterParameters;
 import com.osastudio.newshub.data.user.School;
 import com.osastudio.newshub.data.user.SchoolClass;
 import com.osastudio.newshub.data.user.SchoolClasslist;
 import com.osastudio.newshub.data.user.SchoolList;
 import com.osastudio.newshub.data.user.SchoolType;
 import com.osastudio.newshub.data.user.SchoolTypeList;
 import com.osastudio.newshub.data.user.SchoolYear;
 import com.osastudio.newshub.data.user.SchoolYearlist;
 import com.osastudio.newshub.net.UserApi;
 import com.osastudio.newshub.utils.Utils;
 
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class RegisterView extends Dialog {
 	// public RegisterView(Context context) {
 	// // this(context, null);
 	// mContext = context;
 	// }
 
 	// public RegisterView(Context context, AttributeSet attrs) {
 	// super(context, attrs);
 	// mContext = context;
 	// mInflater = LayoutInflater.from(mContext);
 	// mInflater.inflate(R.layout.register_view, this);
 	// findViews();
 	// }
 
 	public RegisterView(Context context, int theme, int width, int height) {
 		super(context, theme);
 		// TODO Auto-generated constructor stub
 		mContext = context;
 		mWidth = width;
 		mHeight = height;
 	}
 
 	enum LIST_TYPE {
 		CITY, DISTRICT, SCHOOLTYPE, SCHOOL, GRADE, CLASS, SEX, EDU
 	}
 
 	Context mContext = null;
 	int mWidth, mHeight;
 	ArrayList<Data> mDispList = null;
 	LIST_TYPE mCurrentType;
 	private LayoutInflater mInflater = null;
 
 	// @Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.register_view);
 		findViews();
 		setCancelable(false);
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (mListView.getVisibility() == View.VISIBLE) {
 			mListView.setVisibility(View.GONE);
 		}
 	}
 
 	private View mBase = null;
 	private View mSchool = null;
 	private View mGrade = null;
 	private View mClass = null;
 	private View mName = null;
 	private View mSex = null;
 	private View mBirth = null;
 	private View nEdu = null;
 	private View mConfirm = null;
 	private ListView mListView = null;
 
 	private String mCityId = null;
 
 	private void findViews() {
 		if (mWidth > 0 && mHeight > 0) {
 			View base = findViewById(R.id.base);
 			ViewGroup.LayoutParams lp = base.getLayoutParams();
 			lp.width = mWidth;
 			// lp.height = mHeight;
 			base.setLayoutParams(lp);
 		}
 		mSchool = findViewById(R.id.school);
 		// mSchool.setOnClickListener(this);
 		mSchool.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Utils.logd("pp", "mSchool onClick");
 				if (mLoadTask == null) {
 					mLoadTask = new LoadTask();
 					mLoadTask.execute(LIST_TYPE.CITY);
 				}
 			}
 		});
 		mGrade = findViewById(R.id.grade);
 		mGrade.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				if (mLoadTask == null) {
 					mLoadTask = new LoadTask();
 					mLoadTask.execute(LIST_TYPE.GRADE);
 				}
 			}
 		});
 		mClass = findViewById(R.id.clas);
 		mClass.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				if (mLoadTask == null) {
 					mLoadTask = new LoadTask();
 					mLoadTask.execute(LIST_TYPE.CLASS);
 				}
 			}
 		});
 		mName = findViewById(R.id.name);
 		mSex = findViewById(R.id.sex);
 		mSex.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				ArrayList<Data> sexList = new ArrayList<Data>();
				Data data = new Data("0", "��");
 				sexList.add(data);
				data = new Data("1", "Ů");
 				sexList.add(data);
 				setupList(LIST_TYPE.SEX, sexList);
 			}
 		});
 		mBirth = findViewById(R.id.birth);
 
 		mBirth.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				new DatePickerDialog(mContext,
 						new DatePickerDialog.OnDateSetListener() {
 							public void onDateSet(DatePicker view, int year,
 									int monthOfYear, int dayOfMonth) {
 								mYear = year;
 								mMonth = monthOfYear;
 								mDay = dayOfMonth;
 								if (year > 1900) {
 									mYear = year - 1900;
 								}
 								Date date = new Date(mYear, mMonth, mDay);
 								mDateStr = new SimpleDateFormat("yyyy-MM-dd")
 										.format(date);
 								TextView birth = (TextView) findViewById(R.id.birth_text);
 								birth.setText(mDateStr);
 							}
 						}, mYear, mMonth, mDay).show();
 
 			}
 
 		});
 
 		nEdu = findViewById(R.id.education);
 		nEdu.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				if (mLoadTask == null) {
 					mLoadTask = new LoadTask();
 					mLoadTask.execute(LIST_TYPE.EDU);
 				}
 			}
 		});
 		mListView = (ListView) findViewById(R.id.list);
 		mConfirm = findViewById(R.id.confirm_btn);
 		mConfirm.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				String userName = ((EditText) findViewById(R.id.name_text))
 						.getEditableText().toString();
 				RegisterParameters params = new RegisterParameters();
 				params.birthday = mDateStr;
 				params.classId = mClassId;
 				params.gender = mSexStr;
 				params.qualification = mEduStr;
 				params.schoolId = mSchoolId;
 				params.userName = userName;
 				params.yearId = mGradeId;
 				RegistTask task = new RegistTask();
 				task.execute(params);
 
 			}
 		});
 	}
 
 	private LoadTask mLoadTask = null;
 	private String mDistrictId = null;
 	private String mSchoolType = null;
 	private String mSchoolId = null;
 	private String mGradeId = "1";
 	private String mClassId = "1";
 	private String mSexStr = null;
 	private String mEduStr = null;
 	private String mDateStr = null;
 
 	private int mYear = 2000;
 	private int mMonth = 0;
 	private int mDay = 1;
 
 	// private List<PairedStringFieldsObject> mCityList = null;
 	private void setupList(LIST_TYPE type, ArrayList<Data> list) {
 		mDispList = list;
 		mCurrentType = type;
 
 		if (list != null) {
 
 			mListView.setVisibility(View.VISIBLE);
 			MyAdapter adapter = new MyAdapter(list);
 			mListView.setAdapter(adapter);
 			mListView.setOnItemClickListener(new OnItemClickListener() {
 
 				public void onItemClick(AdapterView<?> parent, View view,
 						int position, long id) {
 					Data data = mDispList.get(position);
 					if (mCurrentType.equals(LIST_TYPE.CITY)) {
 						if (mLoadTask == null) {
 							mLoadTask = new LoadTask();
 							mLoadTask.execute(LIST_TYPE.DISTRICT, data.mId);
 						}
 					} else if (mCurrentType.equals(LIST_TYPE.DISTRICT)) {
 						mDistrictId = data.mId;
 						if (mLoadTask == null) {
 							mLoadTask = new LoadTask();
 							mLoadTask.execute(LIST_TYPE.SCHOOLTYPE);
 						}
 					} else if (mCurrentType.equals(LIST_TYPE.SCHOOLTYPE)) {
 						mSchoolType = data.mName;
 						if (mLoadTask == null) {
 							mLoadTask = new LoadTask();
 							mLoadTask.execute(LIST_TYPE.SCHOOL);
 						}
 					} else if (mCurrentType.equals(LIST_TYPE.SCHOOL)) {
 						mSchoolId = data.mId;
 						String schoolName = data.mName;
 						TextView schoolNameText = (TextView) findViewById(R.id.school_text);
 						schoolNameText.setText(schoolName);
 						mGrade.setVisibility(View.VISIBLE);
 						mListView.setVisibility(View.GONE);
 
 					} else if (mCurrentType.equals(LIST_TYPE.GRADE)) {
 
 						mGradeId = data.mId;
 						String mGradeName = data.mName;
 						TextView gradeName = (TextView) findViewById(R.id.grade_text);
 						gradeName.setText(mGradeName);
 						mClass.setVisibility(View.VISIBLE);
 						mListView.setVisibility(View.GONE);
 					} else if (mCurrentType.equals(LIST_TYPE.CLASS)) {
 						mClassId = data.mId;
 						String name = data.mName;
 						TextView className = (TextView) findViewById(R.id.class_text);
 						className.setText(name);
 						mListView.setVisibility(View.GONE);
 
 					} else if (mCurrentType.equals(LIST_TYPE.SEX)) {
 						mSexStr = data.mName;
 						TextView sexName = (TextView) findViewById(R.id.sex_text);
 						sexName.setText(mSexStr);
 						mListView.setVisibility(View.GONE);
 
 					} else if (mCurrentType.equals(LIST_TYPE.EDU)) {
 						mEduStr = data.mName;
 						TextView eduName = (TextView) findViewById(R.id.education_text);
 						eduName.setText(mEduStr);
 						mListView.setVisibility(View.GONE);
 
 					}
 
 				}
 			});
 
 		}
 
 	}
 
 	private class Data {
 		public Data(String id, String name) {
 			mId = id;
 			mName = name;
 		}
 
 		String mId;
 		String mName;
 	}
 	
 	private class RegistTask extends AsyncTask<RegisterParameters, Void, Boolean> {
 
 		@Override
 		protected Boolean doInBackground(RegisterParameters... params) {
 			RegisterParameters param = params[0];
 			NewsResult rtn = UserApi.registerUser(mContext, param);
 			return rtn.isSuccess();
 		}
 		
 		@Override
 		protected void onPostExecute(Boolean result) {
 			RegisterView.this.dismiss();
 			super.onPostExecute(result);
 		}
 		
 	}
 
 	private class LoadTask extends AsyncTask<Object, Void, Object> {
 		ArrayList<Data> datalist = new ArrayList<Data>();
 
 		@Override
 		protected Object doInBackground(Object... params) {
 			LIST_TYPE type = (LIST_TYPE) params[0];
 
 			if (type.equals(LIST_TYPE.CITY)) {
 				CityList citylist = UserApi.getCityList(mContext);
 				List<City> citys = citylist.getList();
 
 				if (datalist.size() > 0) {
 					datalist.clear();
 				}
 				for (int i = 0; i < citys.size(); i++) {
 					City city = citys.get(i);
 					Data data = new Data(city.getId(), city.getName());
 					datalist.add(data);
 				}
 			} else if (type.equals(LIST_TYPE.DISTRICT)) {
 				String cityId = (String) params[1];
 				CityDistrictList district = UserApi.getCityDistrictList(
 						mContext, cityId);
 				List<CityDistrict> districts = district.getList();
 				if (datalist.size() > 0) {
 					datalist.clear();
 				}
 				for (int i = 0; i < districts.size(); i++) {
 					CityDistrict item = districts.get(i);
 					Data data = new Data(item.getId(), item.getName());
 					datalist.add(data);
 				}
 			} else if (type.equals(LIST_TYPE.SCHOOLTYPE)) {
 				SchoolTypeList schoolType = UserApi.getSchoolTypeList(mContext);
 				List<SchoolType> typelist = schoolType.getList();
 				if (datalist.size() > 0) {
 					datalist.clear();
 				}
 				for (int i = 0; i < typelist.size(); i++) {
 					SchoolType item = typelist.get(i);
 					Data data = new Data(null, item.getValue());
 					datalist.add(data);
 				}
 			} else if (type.equals(LIST_TYPE.SCHOOL)) {
 				SchoolList schoollist = UserApi.getSchoolList(mContext,
 						mDistrictId, mSchoolType);
 				List<School> schools = schoollist.getList();
 				if (datalist.size() > 0) {
 					datalist.clear();
 				}
 				for (int i = 0; i < schools.size(); i++) {
 					School item = schools.get(i);
 					Data data = new Data(item.getId(), item.getName());
 					datalist.add(data);
 				}
 			} else if (type.equals(LIST_TYPE.GRADE)) {
 				SchoolYearlist gradelist = UserApi.getSchoolYearList(mContext,
 						mSchoolId);
 				List<SchoolYear> grades = gradelist.getList();
 				if (datalist.size() > 0) {
 					datalist.clear();
 				}
 				for (int i = 0; i < grades.size(); i++) {
 					SchoolYear item = grades.get(i);
 					Data data = new Data(item.getId(), item.getName());
 					datalist.add(data);
 				}
 			} else if (type.equals(LIST_TYPE.CLASS)) {
 				SchoolClasslist classlist = UserApi.getSchoolClassList(
 						mContext, mGradeId);
 				List<SchoolClass> classes = classlist.getList();
 				if (datalist.size() > 0) {
 					datalist.clear();
 				}
 				for (int i = 0; i < classes.size(); i++) {
 					SchoolClass item = classes.get(i);
 					Data data = new Data(item.getId(), item.getName());
 					datalist.add(data);
 				}
 			} else if (type.equals(LIST_TYPE.EDU)) {
 				QualificationList edulist = UserApi
 						.getQualificationList(mContext);
 				List<Qualification> edu_list = edulist.getList();
 				if (datalist.size() > 0) {
 					datalist.clear();
 				}
 				for (int i = 0; i < edu_list.size(); i++) {
 					Qualification item = edu_list.get(i);
 					Data data = new Data("-1", item.getValue());
 					datalist.add(data);
 				}
 			}
 			return type;
 		}
 
 		@Override
 		protected void onPostExecute(Object result) {
 			mLoadTask = null;
 			LIST_TYPE type = (LIST_TYPE) result;
 			setupList(type, datalist);
 			super.onPostExecute(result);
 		}
 
 	}
 
 	private class MyAdapter extends BaseAdapter {
 		protected ArrayList<Data> mList = null;
 
 		public MyAdapter(ArrayList<Data> list) {
 			mList = list;
 		}
 
 		@Override
 		public int getCount() {
 			// TODO Auto-generated method stub
 			return mList.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			TextView tv = (TextView) convertView;
 			if (tv == null) {
 				tv = new TextView(mContext);
 				tv.setTextSize(18);
 				tv.setPadding(10, 10, 10, 0);
 				tv.setTextColor(Color.BLACK);
 			}
 			tv.setText(mList.get(position).mName);
 			return tv;
 		}
 
 	}
 
 	// @Override
 	// public boolean onTouchEvent(MotionEvent event) {
 	// // TODO Auto-generated method stub
 	//
 	// Utils.logd("RegisterView", "onTouchEvent");
 	// super.onTouchEvent(event);
 	// return true;
 	// }
 	//
 	// @Override
 	// public boolean dispatchTouchEvent(MotionEvent ev) {
 	// // TODO Auto-generated method stub
 	// Utils.logd("RegisterView", "dispatchTouchEvent");
 	// onTouchEvent(ev);
 	// return true;
 	// }
 
 	public void onClick(View v) {
 		Utils.logd("RegisterView", "onClick");
 		int id = v.getId();
 		switch (id) {
 		case R.id.school:
 			LoadTask task = new LoadTask();
 			task.execute(LIST_TYPE.CITY);
 			break;
 		}
 
 	}
 
 }
