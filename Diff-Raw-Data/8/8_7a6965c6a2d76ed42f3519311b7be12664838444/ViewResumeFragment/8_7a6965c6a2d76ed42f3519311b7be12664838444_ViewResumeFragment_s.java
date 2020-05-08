 package ru.hh.school.android.ui.fragment;
 
 import ru.hh.school.R;
 import ru.hh.school.android.Resume;
 import android.app.Activity;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ViewResumeFragment extends Fragment implements OnClickListener {
 
     private Activity activity;
     private View currentView;
 
     private TextView resumeContent;
     private TextView resumePhone;
     private TextView resumeEmail;
     private EditText answer;
     private Button btnSendAnswer;
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         this.activity = activity;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.view_resume_fragment, container, false);
         this.currentView = view;
         return view;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         initUi();
     }
 
     private void initUi() {
         resumeContent = (TextView) currentView.findViewById(R.id.tv_resume_content);
         resumePhone = (TextView) currentView.findViewById(R.id.tv_resume_phone);
         resumeEmail = (TextView) currentView.findViewById(R.id.tv_resume_email);
         answer = (EditText) currentView.findViewById(R.id.et_answer);
         btnSendAnswer = (Button) currentView.findViewById(R.id.btn_send_answer);
         btnSendAnswer.setOnClickListener(this);
 
         setResumeContent();
     }
 
     private void setResumeContent() {
         Resume resume = (Resume) activity.getIntent().getParcelableExtra(Resume.class.getCanonicalName());
 
         Resources res = getResources();
         String lastFirstNameString = res.getString(R.string.last_first_name);
         String birthdayString = res.getString(R.string.birthday);
         String years = res.getString(R.string.years);
         String genderString = res.getString(R.string.gender);
         String desiredJobTitleString = res.getString(R.string.desired_job_title);
         String salaryString = res.getString(R.string.salary);
         String phoneString = res.getString(R.string.phone);
         String emailString = res.getString(R.string.email);
 
         // set resume content
         String temp = "";
         temp += "<b>" + lastFirstNameString + ":</b> " + resume.getLastFirstName() + "<br/>";
         temp += "<b>" + birthdayString + ":</b> " + resume.getFormattedBirthday(activity) + " (" + resume.getAgeYears() + " " + years + ")" + "<br/>";
         temp += "<b>" + genderString + ":</b> " + resume.getGender() + "<br/>";
         temp += "<b>" + desiredJobTitleString + ":</b> " + resume.getDesiredJobTitle()+ "<br/>";
        temp += "<b>" + salaryString + ":</b> " + resume.getSalary() + "<br/>";
         resumeContent.setText(Html.fromHtml(temp));
 
         // set resume phone
         temp = "";
        temp += "<b>" + phoneString + ":</b> " + resume.getPhone() + "<br/>";
         resumePhone.setText(Html.fromHtml(temp));
 
         // set resume email
         temp = "";
        temp += "<b>" + emailString + ":</b> " + resume.getEmail() + "<br/>";
         resumeEmail.setText(Html.fromHtml(temp));
     }
 
     @Override
     public void onClick(View v) {
         String message = answer.getText().toString();
         message = message.trim();
         // TODO: send message to...
         Toast.makeText(activity, R.string.on_send_answer_message, Toast.LENGTH_LONG).show();
     }
 }
