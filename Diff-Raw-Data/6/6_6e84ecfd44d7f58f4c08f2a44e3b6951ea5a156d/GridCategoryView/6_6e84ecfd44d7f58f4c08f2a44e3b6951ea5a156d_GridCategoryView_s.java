 package com.quanleimu.view;
 
 import java.util.ArrayList;
 import java.util.List;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import com.quanleimu.view.BaseView;
 import com.quanleimu.view.BaseView.ETAB_TYPE;
 import com.quanleimu.view.BaseView.TabDef;
 import com.quanleimu.view.BaseView.TitleDef;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.adapter.GridAdapter;
 import com.quanleimu.adapter.GridAdapter.GridInfo;
 import com.quanleimu.entity.FirstStepCate;
 
 public class GridCategoryView extends BaseView implements OnItemClickListener {
 	private Bundle bundle;
 	private int msgBack = 0xFFFFFFFF;
 
 	public GridCategoryView(Context content, Bundle bundle) {
 		super(content, bundle);
 		this.bundle = bundle;
 		init();
 	}
 	
 	public GridCategoryView(Context content, Bundle bundle, int msgBack){
 		super(content, bundle);
 		this.bundle = bundle;
 		this.msgBack = msgBack;
 		init();
 	}
 
 	private void init() {
 		LayoutInflater inflater = LayoutInflater.from(this.getContext());
 		View v = inflater.inflate(R.layout.gridcategory, null);
 		this.addView(v);
 
 		List<GridInfo> gitems = new ArrayList<GridInfo>();
 		GridInfo gi = new GridInfo();
 		gi.imgResourceId = R.drawable.ershou;
 		gi.text = "物品交易";
 		gitems.add(gi);
 		gi = new GridInfo();
 		gi.imgResourceId = R.drawable.cheliang;
 		gi.text = "车辆买卖";
 		gitems.add(gi);
 		gi = new GridInfo();
 		gi.imgResourceId = R.drawable.fang;
 		gi.text = "房屋租售";
 		gitems.add(gi);
 		gi = new GridInfo();
 		gi.imgResourceId = R.drawable.gongzuo;
 		gi.text = "全职招聘";
 		gitems.add(gi);
 		gi = new GridInfo();
 		gi.imgResourceId = R.drawable.jianzhi;
 		gi.text = "兼职招聘";
 		gitems.add(gi);
 		gi = new GridInfo();
 		gi.imgResourceId = R.drawable.qiuzhi;
 		gi.text = "求职简历";
 		gitems.add(gi);
 		gi = new GridInfo();
 		gi.imgResourceId = R.drawable.huodong;
 		gi.text = "交友活动";
 		gitems.add(gi);
 		gi = new GridInfo();
 		gi.imgResourceId = R.drawable.chongwuleimu;
 		gi.text = "宠物";
 		gitems.add(gi);
 		gi = new GridInfo();
 		gi.imgResourceId = R.drawable.fuwu;
 		gi.text = "生活服务";
 		gitems.add(gi);
 		gi = new GridInfo();
 		gi.imgResourceId = R.drawable.jiaoyupeixun;
 		gi.text = "教育培训";
 		gitems.add(gi);
 
 		GridAdapter adapter = new GridAdapter(this.getContext());
 		adapter.setList(gitems);
 		((GridView) v.findViewById(R.id.gridcategory)).setAdapter(adapter);
 		((GridView) v.findViewById(R.id.gridcategory)).setOnItemClickListener(this);
 	}
 	
 	@Override
 	public void onPreviousViewBack(int message, Object obj){
 		m_viewInfoListener.onBack(message, obj);
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		List<FirstStepCate> allCates = QuanleimuApplication.getApplication()
 				.getListFirst();
 		if (allCates == null)
 			return;
 		if (arg1.getTag() == null)
 			return;
 		for (int i = 0; i < allCates.size(); ++i) {
 			String selText = ((GridAdapter.GridHolder) arg1.getTag()).text.getText().toString();
 			if (allCates.get(i).name.equals(selText)){
 				SecondCategoryView secView = msgBack == 0xFFFFFFFF ?
 						new SecondCategoryView(this.getContext(), bundle, allCates.get(i), true) : 
 							new SecondCategoryView(this.getContext(), bundle, allCates.get(i), true, msgBack);
 				m_viewInfoListener.onNewView(secView);
 			}
 		}
 	}
 	
 	@Override
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 		title.m_visible = true;
 		title.m_title = "选择类目";
 		title.m_leftActionHint = "返回";
 		return title;
 	}
 	
 	@Override
 	public TabDef getTabDef(){
 		TabDef tab = new TabDef();
 		tab.m_visible = true;
 		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_PUBLISH;
 		return tab;
 	}
 }
