 package au.com.dius.resilience.test.ui.activity;
 
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import au.com.dius.resilience.R;
 import au.com.dius.resilience.model.Impact;
 import au.com.dius.resilience.persistence.repository.impl.AbstractSqlLiteRepository;
 import au.com.dius.resilience.test.AbstractResilienceActivityTestCase;
 import au.com.dius.resilience.ui.activity.EditIncidentActivity;
 
 public class EditIncidentActivityTest extends AbstractResilienceActivityTestCase<EditIncidentActivity> {
 
   public EditIncidentActivityTest() {
     super(EditIncidentActivity.class);
   }
 
   @Override
   protected void beforeTest() {
     getActivity().getApplication().deleteDatabase(AbstractSqlLiteRepository.DB_NAME);
   }
 
   @Override
   protected void afterTest() {
     getActivity().getApplication().deleteDatabase(AbstractSqlLiteRepository.DB_NAME);
   }
 
   public void testCreateAndSaveIncident() {
     
     solo.pressSpinnerItem(0, 2);
     solo.pressSpinnerItem(1, 3);
 
     EditText notes = (EditText) solo.getView(R.id.notes);
     Button createButton = (Button) solo.getView(R.id.submit_incident);
     
     solo.typeText(notes, "fire");
     solo.clickOnView(createButton);
   }
   
   public void testImpactLabelChange() {
     // Test that the initial displayed impact is LOW
     TextView impactRatingLbl = (TextView) solo.getView(R.id.impact_scale_desc);
     assertEquals(Impact.LOW.name(), impactRatingLbl.getText().toString());
     
     final SeekBar impactRating = (SeekBar) solo.getView(R.id.impact_scale);
    solo.setProgressBar(impactRating, 50);
     assertEquals(Impact.MEDIUM.name(), impactRatingLbl.getText().toString());
     
    solo.setProgressBar(impactRating, 100);
     assertEquals(Impact.HIGH.name(), impactRatingLbl.getText().toString());
   }
 
 }
