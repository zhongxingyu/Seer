 package au.com.dius.resilience.ui.fragment;
 
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 import au.com.dius.resilience.R;
 import au.com.dius.resilience.model.Device;
 import au.com.dius.resilience.model.Feedback;
 import au.com.dius.resilience.model.FeedbackResult;
 import au.com.dius.resilience.persistence.async.SendFeedbackTask;
 import au.com.dius.resilience.persistence.async.SendFeedbackTask.SendFeedbackCallback;
 
 public class FeedbackFragment extends Fragment {
 
   private TextView feedbackText;
 
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     return inflater.inflate(R.layout.fragment_feedback, container);
   }
 
   @Override
   public void onActivityCreated(Bundle state) {
     feedbackText = (TextView) getActivity().findViewById(R.id.feedback_text);
     super.onActivityCreated(state);
   }
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
   }
 
   public void onFeedbackSubmitClick(View button) {
     String deviceId = Device.getDeviceId(getActivity());
     String text = feedbackText.getText().toString();
 
     if (text != null && text.trim().length() > 0) {
       button.setEnabled(false);
 
       Feedback feedbackArgs = new Feedback(text, deviceId);
 
       SendFeedbackTask feedbackTask = new SendFeedbackTask(getActivity(), new SendFeedbackCallback() {
 
         @Override
         public void onTaskDone(FeedbackResult result) {
           feedbackResult(result);
         }
 
       });
 
       feedbackTask.execute(feedbackArgs);
 
       button.setEnabled(true);
     }
   }
 
   protected void feedbackResult(FeedbackResult result) {
     Toast toast;
 
    if (result.getResult() == true) {
       toast = Toast.makeText(getActivity(), R.string.feedback_thanks, Toast.LENGTH_LONG);
      toast.show();
     } else {
       toast = Toast.makeText(getActivity(), R.string.feedback_failure + ": " + result.getException().getMessage(), Toast.LENGTH_LONG);
     }
   }
 }
