 package info.micdm.ftr.activities;
 
 import info.micdm.ftr.Forum;
 import info.micdm.ftr.Message;
 import info.micdm.ftr.R;
 import info.micdm.ftr.Theme;
 import info.micdm.ftr.ThemePage;
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 /**
  * Экран со списком сообщений.
  * @author Mic, 2011
  *
  * TODO сделать красивые элементы списка
  */
 public class ThemeActivity extends ListActivity {
 
 	protected ArrayAdapter<Message> _adapter;
 	
 	/**
 	 * Вызывается, когда загрузилась очередная страница.
 	 */
 	protected void _onPageLoaded(ThemePage page) {
 		for (Message message : page.getMessages()) {
 			_adapter.add(message);
 		}
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
		// TODO: добавить прогресс-диалог при загрузке + прогрессбар при подгрузке страниц (?)
		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.theme);
 		
 		_adapter = new ArrayAdapter<Message>(this, R.layout.list_item);
 		getListView().setAdapter(_adapter);
 
 		Bundle extras = getIntent().getExtras();
 		Integer groupId = extras.getInt("groupId");
 		Integer themeId = extras.getInt("themeId");
 		
 		Theme theme = Forum.INSTANCE.getGroup(groupId).getTheme(themeId);
 		
 		TextView title = (TextView)findViewById(R.id.themeTitle);
 		title.setText(theme.getTitle());
 		
 		theme.loadPage(0, new Theme.OnPageLoadedCommand() {
 			@Override
 			public void callback(ThemePage page) {
 				_onPageLoaded(page);
 			}
 		});
 	}
 }
