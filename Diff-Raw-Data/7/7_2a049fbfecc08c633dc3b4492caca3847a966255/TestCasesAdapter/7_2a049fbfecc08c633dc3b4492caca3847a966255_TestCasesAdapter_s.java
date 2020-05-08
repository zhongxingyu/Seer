 
 package com.openfeint.qa.ggp.adapter;
 
 import com.openfeint.qa.core.caze.TestCase;
 import com.openfeint.qa.ggp.R;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.CheckBox;
 import android.widget.TextView;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class TestCasesAdapter extends BaseAdapter {
 
     private class RecentViewHolder {
         CheckBox use;
 
         TextView result;
 
         TextView case_id;
 
         TextView case_text;
     }
 
     private LayoutInflater inflater;
 
     private List<CaseWrapper> testCases;
 
     public TestCasesAdapter(Context context, List<CaseWrapper> cases) {
         this.testCases = cases;
         inflater = LayoutInflater.from(context);
     }
 
     @Override
     public int getCount() {
 
         return testCases.size();
     }
 
     @Override
     public Object getItem(int arg0) {
 
         return testCases.get(arg0);
     }
 
     @Override
     public long getItemId(int arg0) {
         return arg0;
     }
 
     public String[] getSelectedCaseIds() {
         List<String> list = new ArrayList<String>();
         for (CaseWrapper wrapper : testCases) {
             if (wrapper.isSelected()) {
//                if (wrapper.getTheCase().getResult() == TestCase.RESULT.UNTEST) {
//                    wrapper.getTheCase().setExecuted(false);
//                }
                 list.add(wrapper.getTheCase().getId());
             }
         }
         String[] sl = (String[]) list.toArray(new String[list.size()]);
         return sl;
     }
 
     public List<TestCase> getSelectedCases() {
         List<TestCase> list = new ArrayList<TestCase>();
         for (CaseWrapper wrapper : testCases) {
             if (wrapper.isSelected()) {
                 list.add(wrapper.getTheCase());
             }
         }
         return list;
     }
 
     public void ToggleSelectAll(boolean isSelected) {
         for (CaseWrapper wrapper : testCases) {
             wrapper.setSelected(isSelected);
         }
         notifyDataSetChanged();
     }
     
     public void ToggleSelectFailed(boolean isSelected) {
         for (CaseWrapper wrapper : testCases) {
             if (wrapper.getTheCase().getResult() == TestCase.RESULT.FAILED) {
                 wrapper.setSelected(isSelected);
                 this.notifyDataSetChanged();
             }
         }
     }
 
     @Override
     public View getView(final int position, View convertView, ViewGroup parent) {
         RecentViewHolder holder;
         if (convertView == null) {
             convertView = inflater.inflate(R.layout.list_item, null);
             holder = new RecentViewHolder();
             holder.use = (CheckBox) convertView.findViewById(R.id.use);
             holder.result = (TextView) convertView.findViewById(R.id.case_result);
             holder.case_id = (TextView) convertView.findViewById(R.id.case_id);
             holder.case_text = (TextView) convertView.findViewById(R.id.case_title);
             convertView.setTag(holder);
         } else {
             holder = (RecentViewHolder) convertView.getTag();
         }
         CaseWrapper wrapper = testCases.get(position);
         holder.use.setChecked(wrapper.isSelected());
         int result = wrapper.getTheCase().getResult();
         String sResult = "U";
         if (result == TestCase.RESULT.FAILED) {
             sResult = "F";
         } else if (result == TestCase.RESULT.PASSED) {
             sResult = "T";
         } else if (result == TestCase.RESULT.RETESTED) {
             sResult = "R";
         }else if (result == TestCase.RESULT.BLOCKED) {
             sResult = "B";
         }
         holder.result.setText(sResult);
         holder.case_id.setText(wrapper.getTheCase().getId());
         holder.case_text.setText(wrapper.getTheCase().getTitle());
 
         holder.use.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 CheckBox me = (CheckBox) v;
                 testCases.get(position).setSelected(me.isChecked());
             }
         });
         return convertView;
     }
 }
