 package mit.edu.stemplusplus;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import mit.edu.stemplusplus.helper.Comment;
 import mit.edu.stemplusplus.helper.ParseDatabase;
 import mit.edu.stemplusplus.helper.Project;
 import mit.edu.stemplusplus.helper.Step;
 import mit.edu.stemplusplus.helper.User;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.inputmethod.EditorInfo;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.RefreshCallback;
 
 public class ProjectDescription extends Activity {
 	Project selectedProject;
 	String projectDescription;
 	ArrayList<Step> projectSteps;
 	CustomizedStepAdapter stepAdapter;
 	ListView stepListView;
 	ListView commentListView;
 	CustomizedCommentAdapter commentAdapter;
 	EditText comment;
 	List<Comment> projectComments = new ArrayList<Comment>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_project_description);
 		ParseDatabase.initProject(this);
 
 		/** get the project selected earlier **/
 		Intent getProjectIntent = getIntent();
 		selectedProject = (Project) getProjectIntent
 				.getSerializableExtra(StemPlusPlus.PROJECT_INTENT);
 		ParseQuery query = new ParseQuery(StemPlusPlus.PROJECT_PARSE);
 		try {
 			final ParseObject projectObject = query.get(selectedProject.getId());
 			projectObject.refreshInBackground(new RefreshCallback() {
 				
 				@Override
 				public void done(ParseObject arg0, ParseException arg1) {
 					selectedProject = ParseDatabase.getBasicProjectFromParseObject(projectObject);
 					projectComments = selectedProject.getComments();
 					if (projectComments != null && projectComments.size() > 0){
 						for (Comment c: projectComments){
 							addCommenDynamically(c.getComment());
 						}
 						
 					}
 				}
 			});
 			
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		setUpView();
 		
 	}
 
 	private void setUpView() {
 				
 		TextView projectDescription = (TextView) findViewById(R.id.description_project_description);
 		projectDescription.setText(selectedProject.getDescription());
 
 		Button upVote = (Button) findViewById(R.id.upvote_project_description);
 		upVote.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				upVote(selectedProject);
 
 			}
 		});
 
 		Button downVote = (Button) findViewById(R.id.downvote_project_description);
 		downVote.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				downVote(selectedProject);
 
 			}
 		});
 		
 		
 		Button commentBtn = (Button) findViewById(R.id.comment_project_description);
 		commentBtn.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				comment = (EditText) findViewById(R.id.edittext_comment_project_description);
 				comment.setVisibility(View.VISIBLE);
 				comment.requestFocus();
 				if(comment.requestFocus()) {
 				    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
 				}
 				comment.setOnEditorActionListener(new OnEditorActionListener() {
 				    @Override
 				    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 				        boolean handled = false;
 				        if (actionId == EditorInfo.IME_ACTION_DONE) {
 				            handleComment(comment.getText().toString());
 				            handled = true;
 				        }
 				        comment.setVisibility(View.GONE);
 				        return handled;
 				    }
 
 					private void handleComment(String string) {
 						projectComments.add(new Comment(string));
 						ParseQuery query = new ParseQuery(StemPlusPlus.PROJECT_PARSE);
 						try {
 							ParseObject projectObject = query.get(selectedProject.getId());
 							projectObject.put(StemPlusPlus.COMMENT_PARSE, ParseDatabase.makeCommentArray(projectComments));
 							projectObject.saveInBackground();
 						} catch (ParseException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						addCommenDynamically(string);
 					}
 				});
 				
 			}
 		});
 		List<Step> step = selectedProject.getSteps();
 		stepAdapter = new CustomizedStepAdapter(this, step);
 		stepListView = (ListView) findViewById(R.id.step_listView_project_description);
 		stepListView.setAdapter(stepAdapter);
 		
 				
 		
 		
 		
 	}
 	
 	private void addCommenDynamically(String comment){
 		LinearLayout ll = (LinearLayout) findViewById(R.id.comment_list_view_project_description);
		
 		View v = getLayoutInflater().inflate(R.layout.comment_display, null);
 		TextView commentTitle = (TextView) v.findViewById(R.id.comment_tittle_comment_display);
 		TextView commentDetail = (TextView) v.findViewById(R.id.comment_detail_comment_display);
 		commentTitle.setText("User comment on "  + new SimpleDateFormat("MM-dd-yy HH:mm:ss")
 			.format(new Date()));
 		commentDetail.setText(comment);
 		ll.addView(v);
 	}
 
 	private void upVote(Project project) {
 		project.incrementRank();
 	}
 
 	private void downVote(Project project) {
 		project.decrementRank();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_project_description, menu);
 		return true;
 	}
 
 	public class CustomizedCommentAdapter extends BaseAdapter {
 		private Activity activity;
 		/** The collections of sellable objects */
 		private List<Comment> data;
 		/** To inflate the view from an xml file */
 		private LayoutInflater inflater = null;
 
 		public CustomizedCommentAdapter(Activity a, List<Comment> steps) {
 			activity = a;
 			data = steps;
 			inflater = (LayoutInflater) activity
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		}
 
 		@Override
 		public int getCount() {
 			// TODO Auto-generated method stub
 			return data.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			// TODO Auto-generated method stub
 			return data.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			final ViewHolder holder;
 			if (convertView == null) {
 				holder = new ViewHolder();
 				convertView = inflater.inflate(R.layout.comment_display, null);
 				holder.commentTitle = (TextView) convertView
 						.findViewById(R.id.comment_tittle_comment_display);
 
 				holder.commentDetail = (TextView) convertView
 						.findViewById(R.id.comment_detail_comment_display);
 				convertView.setTag(holder);
 			}
 			// picture is already there
 			else {
 				holder = (ViewHolder) convertView.getTag();
 
 			}
 
 			Comment comment = data.get(position);
 
 			Log.d("get project", "ok");
 			holder.commentDetail.setText(comment.getComment());
 			Log.d("set Text", "ok");
 			holder.commentTitle.setText(comment.getUser().getUsername()
 					+ "comments on "
 					+ new SimpleDateFormat("MM-DD-YY HH:mm:ss")
 							.format(new Date()));
 			return convertView;
 		}
 
 		public class ViewHolder {
 			private TextView commentTitle;
 			private TextView commentDetail;
 
 			private int id;
 
 			public void setId(int id) {
 				this.id = id;
 			}
 		}
 	}
 
 }
