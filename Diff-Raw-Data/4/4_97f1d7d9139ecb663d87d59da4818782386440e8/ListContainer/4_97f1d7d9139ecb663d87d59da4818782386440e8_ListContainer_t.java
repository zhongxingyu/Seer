 package com.kmware.hrm;
 
 import java.util.ArrayList;
 
 import model.BaseModel;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class ListContainer extends ZActivity implements OnClickListener {
 
 	public static String LOGTAG = ListContainer.class.getSimpleName();
 	private static final int RES_EDIT = 1;
 	private Button current;
 
 	Button iv_People;
 	Button iv_Project;
 	Button iv_Positions;
 	Button iv_Interviews;
 	ImageView iv_subTitle;
 	TextView tv_subTitle;
 	EditText edt_Search;
 	LinearLayout ll_NavigationButtons;
 	LinearLayout parent;
 	private String extra;
 	private int currentPos;
 	ArrayList<BaseModel> dataList = new ArrayList<BaseModel>();
 	CustomContainerAdapter listAdapter;
 
 	/** Called when the activity is first created. */
 	public void onCreate(Bundle savedInstanceState) {
 		setContentView(R.layout.list_container);
 		super.onCreate(savedInstanceState);
 		setTitle("list", R.drawable.cat_people);
 
 		addprefBarBtn(android.R.drawable.ic_menu_search, new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (findViewById(R.id.filter_menu).isShown())
 					((LinearLayout) findViewById(R.id.filter_menu)).setVisibility(View.GONE);
 				else
 					((LinearLayout) findViewById(R.id.filter_menu)).setVisibility(View.VISIBLE);
 
 			}
 		});
 		addprefBarBtn(android.R.drawable.ic_input_add, new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent();
 
 				if (extra.equals(getResources().getString(R.string.cat_people))) {
 					intent = new Intent(ListContainer.this, EditPeople.class);
 				}
 				if (extra.equals(getResources().getString(R.string.cat_projects))) {
 					intent = new Intent(ListContainer.this, EditProject.class);
 				}
 				if (extra.equals(getResources().getString(R.string.cat_positions))) {
 					intent = new Intent(ListContainer.this, EditPosition.class);
 				}
 				if (extra.equals(getResources().getString(R.string.cat_interviews))) {
 					intent = new Intent(ListContainer.this, EditInterview.class);
 				}
 
 				startActivityForResult(intent, RES_EDIT);
 			}
 		});
 		init();
 
 	}
 
 	private void createNavigationButtons(int id) {
 		//  LayoutParams c     
 		LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 
 	}
 
 	private void init() {
 
 		ll_NavigationButtons = (LinearLayout) findViewById(R.id.ll_NavigationButtons);
 
 		iv_People = (Button) findViewById(R.id.iv_People);
 		iv_People.setTag(0);
 		iv_People.setOnClickListener(this);
 
 		iv_Project = (Button) findViewById(R.id.iv_Projects);
 		iv_Project.setOnClickListener(this);
 		iv_Project.setTag(0);
 
 		iv_Positions = (Button) findViewById(R.id.iv_Positions);
 		iv_Positions.setOnClickListener(this);
 		iv_Positions.setTag(1);
 
 		iv_Interviews = (Button) findViewById(R.id.iv_Interviews);
 		iv_Interviews.setOnClickListener(this);
 		iv_Interviews.setTag(2);
 
		current = iv_People;
 		parent = (LinearLayout) current.getParent();
 		parent.removeView(current);
 		getExtra();
 
 		if (extra.equals(getResources().getString(R.string.cat_people)))
 			iv_People.performClick();
 		if (extra.equals(getResources().getString(R.string.cat_projects)))
 			iv_Project.performClick();
 		if (extra.equals(getResources().getString(R.string.cat_positions)))
 			iv_Positions.performClick();
 		if (extra.equals(getResources().getString(R.string.cat_interviews)))
 			iv_Interviews.performClick();
 
 		
 		createNavigationButtons(R.id.iv_People);
 		fillData();
 		listAdapter = new CustomContainerAdapter(this, dataList, R.layout.list_container_row);
 
 		//  
 		ListView lv_Conteiner = (ListView) findViewById(R.id.lv_Conteiner);
 		lv_Conteiner.setAdapter(listAdapter);
 
		edt_Search = (EditText) findViewById(R.id.edt_Search);
 		edt_Search.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
 				// When user changed the Text
 
 				listAdapter.getFilter().filter(cs);
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void afterTextChanged(Editable arg0) {
 				// TODO Auto-generated method stub
 			}
 		});
 
 	}
 
 	private void getExtra() {
 		Bundle extras = getIntent().getExtras();
 
 		if (extras != null) {
 			try {
 				extra = extras.getString(Extras.DASHBOARD_INTENT);
 			} catch (Exception e) {
 
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 	//    
 	void fillData() {
 		for (int i = 1; i <= 20; i++) {
 			dataList.add(new BaseModel(i, "" + i * 1000));
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 
 		// l.removeView(current);
 		parent.addView(current, (Integer) v.getTag());
 		bar.setTitle(((Button) v).getText());
 		current.setTag(v.getTag());
 		switch (v.getId()) {
 		case R.id.iv_People:
 			// iv_subTitle.setImageResource(R.drawable.cat_people);
 			bar.setTitleIco(R.drawable.cat_people);
 			extra = getResources().getString(R.string.cat_people);
 			setVisability(R.id.iv_People);
 			break;
 		case R.id.iv_Projects:
 			// iv_subTitle.setImageResource(R.drawable.cat_project);
 			bar.setTitleIco(R.drawable.cat_project);
 			extra = getResources().getString(R.string.cat_projects);
 			setVisability(R.id.iv_Projects);
 			break;
 		case R.id.iv_Positions:
 			// iv_subTitle.setImageResource(R.drawable.cat_position);
 			bar.setTitleIco(R.drawable.cat_position);
 			extra = getResources().getString(R.string.cat_positions);
 			// setVisability(R.id.iv_Positions);
 			break;
 		case R.id.iv_Interviews:
 			bar.setTitleIco(R.drawable.cat_intervies);
 			extra = getResources().getString(R.string.cat_interviews);
 			// iv_subTitle.setImageResource(R.drawable.cat_intervies);
 
 			setVisability(R.id.iv_Interviews);
 			break;
 		}
 		current = (Button) v;
 		parent.removeView(current);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (requestCode) {
 
 		case RES_EDIT:
 
 			if (resultCode == RESULT_OK) {
 				listAdapter.notifyDataSetChanged();
 			}
 			break;
 		}
 	}
 
 	protected void setVisability(int id) {
 		iv_People.setVisibility(View.VISIBLE);
 		iv_Project.setVisibility(View.VISIBLE);
 		iv_Positions.setVisibility(View.VISIBLE);
 		iv_Interviews.setVisibility(View.VISIBLE);
 		// findViewById(id).setVisibility(View.GONE);
 
 	}
 }
