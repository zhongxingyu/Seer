 package woodsie.avalanche.info;
 
 import static woodsie.avalanche.section.CollapsibleSection.OPEN_SECTION;
 import static woodsie.avalanche.section.CollapsibleSection.State.OPEN;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import lombok.Getter;
 import woodsie.avalanche.R;
 import woodsie.avalanche.section.CollapsibleSection;
 import woodsie.avalanche.section.CollapsibleSectionParent;
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.ScrollView;
 
 public class InfoActivity extends Activity implements CollapsibleSectionParent {
 
 	@Getter
 	private List<CollapsibleSection> sectionList;
 
 	@Getter
 	private ScrollView scroller;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.info);
 
 		sectionList = new ArrayList<CollapsibleSection>();
 		scroller = (ScrollView) findViewById(R.id.infoScroller);
 
 		sectionList.add(new CollapsibleSection(this, R.id.hazardHeading, R.id.hazardArrow, R.id.hazardLayout));
 		sectionList.add(new CollapsibleSection(this, R.id.reductionHeading, R.id.reductionArrow, R.id.reductionLayout));
 		sectionList.add(new CollapsibleSection(this, R.id.threex3Heading, R.id.threex3Arrow, R.id.threex3Layout));
 		sectionList.add(new CollapsibleSection(this, R.id.booksHeading, R.id.booksArrow, R.id.booksLayout));
 		sectionList.add(new CollapsibleSection(this, R.id.linksHeading, R.id.linksArrow, R.id.linksLayout));
 
 		if (savedInstanceState != null && savedInstanceState.containsKey(OPEN_SECTION)) {
 			sectionList.get(savedInstanceState.getInt(OPEN_SECTION)).open();
 		}
 
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 
 		for (int i = 0; i < sectionList.size(); i++) {
 			if (sectionList.get(i).getState() == OPEN)
 				outState.putInt(OPEN_SECTION, i);
 		}
 	}
 
 	@Override
 	public void startActivity(Intent intent) {
 		try {
			/* First attempt at fixing an HTC broken by evil Apple patents. */
			if (intent.getComponent() != null && ".HtcLinkifyDispatcherActivity".equals(intent.getComponent().getShortClassName()))
				intent.setComponent(null);
 			super.startActivity(intent);
 		} catch (ActivityNotFoundException e) {
 			e.printStackTrace(System.err);
 			/*
			 * Probably an HTC broken by evil Apple patents. This is not perfect, but better than
 			 * crashing the whole application.
 			 */
 			super.startActivity(Intent.createChooser(intent, null));
 		}
 	}
 }
