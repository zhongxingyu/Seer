 package jp.knct.di.c6t.ui.route;
 
 import jp.knct.di.c6t.IntentData;
 import jp.knct.di.c6t.R;
 import jp.knct.di.c6t.communication.DebugSharedPreferencesClient;
 import jp.knct.di.c6t.model.Route;
 import jp.knct.di.c6t.util.ActivityUtil;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class RouteCreationDetailFormActivity extends Activity implements OnClickListener {
 
 	public static final int REQUEST_CODE_EDIT_ROUTE_DETAIL = 0x1000;
 	private Route mRoute;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_route_creation_detail_form);
 
 		mRoute = getIntent().getParcelableExtra(IntentData.EXTRA_KEY_ROUTE);
 		ActivityUtil.setOnClickListener(this, this, new int[] {
 				R.id.route_creation_detail_form_cancel,
 				R.id.route_creation_detail_form_ok,
 		});
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.route_creation_detail_form_cancel:
 			setResult(RESULT_CANCELED);
 			finish();
 			break;
 
 		case R.id.route_creation_detail_form_ok:
 			setDetailsFromEditForms(mRoute);
			DebugSharedPreferencesClient client = new DebugSharedPreferencesClient(this);
			mRoute.setUser(client.getMyUserData());
 			if (mRoute.isValid()) {
				client.saveRoute(mRoute); // TODO use http client
 				setResult(RESULT_OK);
 				finish();
 			}
 			break;
 
 		default:
 			break;
 		}
 	}
 
 	private void setDetailsFromEditForms(Route route) {
 		ActivityUtil getter = new ActivityUtil(this);
 		String name = getter.getText(R.id.route_creation_detail_form_name);
 		String description = getter.getText(R.id.route_creation_detail_form_description);
 		route.setName(name);
 		route.setDescription(description);
 	}
 }
