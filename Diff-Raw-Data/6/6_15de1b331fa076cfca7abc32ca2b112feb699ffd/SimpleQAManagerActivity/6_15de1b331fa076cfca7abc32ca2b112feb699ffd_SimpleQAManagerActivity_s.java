 package com.t2.mtbi.activity.qa;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AbsListView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.t2.mtbi.R;
 import com.t2.mtbi.activity.WebViewActivity;
 
 public abstract class SimpleQAManagerActivity extends XMLQAManager implements OnClickListener {
 	public static final String EXTRA_SHOW_TOTAL_SCORE = "showTotalScore";
 	private boolean showTotalScore = true;
 	private ListView listView;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		this.showTotalScore = getIntent().getBooleanExtra(EXTRA_SHOW_TOTAL_SCORE, true);
 
 		this.setContentView(R.layout.simple_qa_manager_activity);
 		this.setTitle(this.questionare.title);
 
 		if(this.questionare.content != null && this.questionare.content.length() > 0) {
 			View v = this.findViewById(R.id.detailsButton);
 			v.setOnClickListener(this);
 			v.setVisibility(View.VISIBLE);
 		}
 
 		if(this.questionare.desc != null && this.questionare.desc.length() > 0) {
 			TextView tv = (TextView)this.findViewById(R.id.text1);
 			tv.setText(this.questionare.desc);
 			tv.setVisibility(View.VISIBLE);
 		}
 
 		this.findViewById(R.id.score_wrapper).setVisibility(View.GONE);
 		this.findViewById(R.id.startButton).setOnClickListener(this);
 
 		this.findViewById(R.id.sendResultsButton).setOnClickListener(this);
 
 		listView = (ListView)this.findViewById(R.id.list);
 		listView.setEmptyView(this.findViewById(R.id.emptyListView));
 
 		if(!showTotalScore) {
 			this.findViewById(R.id.score_wrapper).setVisibility(View.GONE);
 		}
 	}
 
 	@Override
 	protected void onAllQuestionsAnswered() {
 
 		if(showTotalScore) {
 			double total = getTotalScore();
 			this.findViewById(R.id.score_wrapper).setVisibility(View.VISIBLE);
 			((TextView)this.findViewById(R.id.total_score)).setText(total+"");
 		}
 
 		View listHeader = this.findViewById(R.id.listHeader);
 		((ViewGroup)listHeader.getParent()).removeView(listHeader);
 		listHeader.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 		listView.addHeaderView(listHeader);
 		listView.setAdapter(
 				new QAAdapter(
 						this,
 						R.layout.simple_qa_manager_list_item,
 						this.questions,
 						this.selectdAnswers
 				)
 		);
 	}
 
 	@Override
 	protected Intent getQuestionIntent(Question question, Answer[] answers,
 			int totalQuestions, int questionIndex) {
 		return new Intent(this, SingleChoiceQuestionActivity.class);
 	}
 
 	@Override
 	public void onClick(View v) {
 		Intent intent;
 		switch(v.getId()) {
 		case R.id.startButton:
 			this.startQuestionare();
 			break;
 
 		case R.id.detailsButton:
 			intent = new Intent(this, WebViewActivity.class);
 			intent.putExtra(WebViewActivity.EXTRA_CONTENT, this.questionare.content);
 			this.startActivity(intent);
 			break;
 
 		case R.id.sendResultsButton:
 			sendResults();
 		}
 	}
 
 	private void sendResults() {
 		try {
 			File outputFile = new File(Environment.getExternalStorageDirectory(), "results.csv");
 			outputFile.deleteOnExit();
			if(!(outputFile.exists() && outputFile.delete())) {
				return;
 			}
 
 			BufferedWriter fw = new BufferedWriter(new FileWriter(outputFile));
 
 			if(this.showTotalScore) {
 				fw.write(getString(R.string.total_score));
 				fw.write(this.getTotalScore()+"");
 				fw.write("\n");
 			}
 
 			Answer[] ans;
 			Question q;
 			fw.write("question,answer_value,answer,\n");
 			for(String questionId: this.selectdAnswers.keySet()) {
 				ans = this.selectdAnswers.get(questionId);
 				q = this.questions.get(questionId);
 				fw.write(q.title+",");
 				fw.write(ans[0].value+",");
 				fw.write(ans[0].title+",");
 				fw.write("\n");
 			}
 
 			fw.close();
 
 
 			String subject = getString(R.string.send_results_subject);
 			subject = subject.replace("{0}", this.questionare.title);
 
 			ArrayList<Uri> attachementUris = new ArrayList<Uri>();
 			attachementUris.add(Uri.fromFile(outputFile));
 
 			Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
 			shareIntent.setType("text/csv");
 			shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachementUris);
 			shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
 
 			startActivity(Intent.createChooser(shareIntent, getString(R.string.send_results)));
 
 		} catch (IOException e) {
 
 		}
 	}
 
 }
