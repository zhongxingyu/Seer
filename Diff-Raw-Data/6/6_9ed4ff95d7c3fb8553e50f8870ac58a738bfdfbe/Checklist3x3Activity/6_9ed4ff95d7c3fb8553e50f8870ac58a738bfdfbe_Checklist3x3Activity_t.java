 package woodsie.avalanche.checklist;
 
 import woodsie.avalanche.R;
 import woodsie.avalanche.data.CollapsableSection;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
import android.widget.ScrollView;
 
 public class Checklist3x3Activity extends Activity implements OnClickListener {
 
 	private CollapsableSection regional;
 
 	private CollapsableSection local;
 
 	private CollapsableSection zonal;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.checklist_3x3);
 
 		View heading = findViewById(R.id.regionalHeading);
 		ImageView arrow = (ImageView) findViewById(R.id.regionalArrow);
 		View layout = findViewById(R.id.regionalLayout);
 		regional = new CollapsableSection(this, arrow, layout);
 		heading.setOnClickListener(this);
 		arrow.setOnClickListener(this);
 
 		heading = findViewById(R.id.localHeading);
 		arrow = (ImageView) findViewById(R.id.localArrow);
 		layout = findViewById(R.id.localLayout);
 		local = new CollapsableSection(this, arrow, layout);
 		heading.setOnClickListener(this);
 		arrow.setOnClickListener(this);
 
 		heading = findViewById(R.id.zonalHeading);
 		arrow = (ImageView) findViewById(R.id.zonalArrow);
 		layout = findViewById(R.id.zonalLayout);
 		zonal = new CollapsableSection(this, arrow, layout);
 		heading.setOnClickListener(this);
 		arrow.setOnClickListener(this);
 
 	}
 
 	public void onClick(View view) {
 		switch (view.getId()) {
 		case R.id.regionalHeading:
 		case R.id.regionalArrow:
 			regional.toggle();
 			local.close();
 			zonal.close();
 			break;
 
 		case R.id.localHeading:
 		case R.id.localArrow:
 			regional.close();
 			local.toggle();
 			zonal.close();
 			break;
 
 		case R.id.zonalHeading:
 		case R.id.zonalArrow:
 			regional.close();
 			local.close();
 			zonal.toggle();
 			break;
 
 		}
 
		((ScrollView) findViewById(R.id.checklistScroll)).scrollTo(0, 0);
	}
 }
