 package edu.berkeley.cs160.smartnature;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.AlertDialog;
 import android.app.Application;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.Rect;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 class GardenGnome extends Application {
 	private static ArrayList<Garden> gardens = new ArrayList<Garden>();
 	private static DatabaseHelper dh;
 
 	public static void tmpinit(Context context) {
 		dh = new DatabaseHelper(context);
 		initAll();
 	}
 
 	public static void initAll() {
 		gardens.clear();
 		List<Integer> existing_garden = dh.select_all_garden_pk();
 		for(int i = 0; i < existing_garden.size(); i++) {
 			Garden temp1 = dh.select_garden(existing_garden.get(i));
 			temp1.setGardenNum(existing_garden.get(i));
 			System.out.println("garden = " + existing_garden.get(i));
 			gardens.add(temp1);
 			List<Integer> existing_photo = dh.select_map_gp2_ph(existing_garden.get(i));
 			for(int m = 0; m < existing_photo.size(); m++) {
 				Photo temp2 = dh.select_photo(existing_photo.get(m));
 				temp2.setPhotoNum(existing_photo.get(m));
 				gardens.get(i).addImage(temp2);
 				System.out.println("photo = " + existing_photo.get(m));
 			}
 			List<Integer> existing_plot = dh.select_map_gp_po(existing_garden.get(i));
 			for(int j = 0; j < existing_plot.size(); j++) {
 				Plot temp2 = dh.select_plot(existing_plot.get(j));
 				temp2.setPlotNum(existing_plot.get(j));
 				gardens.get(i).addPlot(temp2);
 				System.out.println("plot = " + existing_plot.get(j));
 				List<Integer> existing_plant = dh.select_map_pp_pa(existing_plot.get(j));
 				for(int k = 0; k < existing_plant.size(); k++) {
 					Plant temp3 = dh.select_plant(existing_plant.get(k));
 					temp3.setPlantNum(existing_plant.get(k));
 					gardens.get(i).getPlot(j).addPlant(temp3);
 					System.out.println("plant = " + existing_plant.get(k));
 					List<Integer> existing_entry = dh.select_map_pe_e(existing_plant.get(k));
 					for(int l = 0; l < existing_entry.size(); l++) {
 						Entry temp4 = dh.select_entry(existing_entry.get(l));
 						temp4.setEntryNum(existing_entry.get(l));
 						gardens.get(i).getPlot(j).getPlant(k).addEntry(temp4);
 						System.out.println("entry = " + existing_entry.get(l));
 					}
 				}
 			}
 		}
 	}
 
 	public void tmpinitMockData() {
 		Log.w("debug", "initMockData called");
 		dh.insert_garden("Berkeley Youth Alternatives", R.drawable.preview, "0,0,800,480", "Berkeley", "California", 0, -1, "");
 		dh.insert_garden("Karl Linn", R.drawable.preview, "0,0,800,480", "Berkeley", "California", 0, -1, "");
 
 		dh.insert_plot("Jerry Plot", "40,60,90,200," + Color.BLACK, Plot.RECT, Color.BLACK, "", 10, 0);
 		dh.insert_plot("Amy Plot", "140,120,210,190," + Color.BLACK, Plot.OVAL, Color.BLACK, "", 0, 0);
 		dh.insert_plot("Shared Plot", "270,120,360,220," + Color.BLACK, Plot.POLY, Color.BLACK, "0,0,50,10,90,100", 0, 0);
 		dh.insert_plot("Cyndi Plot", "40,200,90,300," + Color.BLACK, Plot.RECT, Color.BLACK, "", 0, 0);
 		dh.insert_plot("Alex Plot", "140,50,210,190," + Color.BLACK, Plot.OVAL, Color.BLACK, "", 10, 0);
 		dh.insert_plot("Flowers", "270,120,360,260," + Color.BLACK, Plot.POLY, Color.BLACK, " 0,0,50,10,90,100,70,140,60,120", 0, 0);
 
 		Garden g1 = dh.select_garden(1);
 		Garden g2 = dh.select_garden(2);
 
 		for(int i = 1; i < 4; i++) {
 			g1.addPlot(dh.select_plot(i));
 			dh.insert_map_gp(1, i);
 		}
 
 		for(int i = 4; i < 7; i++) {
 			g2.addPlot(dh.select_plot(i));
 			dh.insert_map_gp(2, i);
 		}
 
 		gardens.add(g1);
 		gardens.add(g2);
 	}
 
 	public static ArrayList<Garden> getGardens() {
 		return gardens;
 	}
 
 	public static int getGardenPk(int garden_id) {
 		return dh.select_garden_pk(getGarden(garden_id).getName());
 	}
 
 	public static Garden getGarden(int garden_id) {
 		return gardens.get(garden_id);
 	}
 
 	/** TODO: should manually set the id of the garden */
 	public static void addGarden(Garden garden) {
 		dh.insert_garden(garden.getName(), R.drawable.preview, "0,0,800,480", "", "", 0, -1, "");
 		garden.setGardenNum(dh.count_garden());
 		gardens.add(garden);
 		System.out.println("addGarden = " + dh.count_garden());
 	}
 	
 	public static void addGardenServer(Garden garden) {
 		String bounds_s = "" + garden.getBounds().left + "," + garden.getBounds().top + "," + garden.getBounds().right + "," + garden.getBounds().bottom;
 		int is_public = -1;
 		if(garden.isPublic())
 			is_public = 1;
 		else
 			is_public = 0;
 		dh.insert_garden(garden.getName(), R.drawable.preview, bounds_s, garden.getCity(), garden.getState(), garden.getServerId(), is_public, "");
 		garden.setGardenNum(dh.count_garden());
 		gardens.add(garden);
 		System.out.println("addGarden = " + dh.count_garden());
 	}
 
 	public static void removeGarden(int garden_id) {
 		dh.delete_map_gp_g(getGardenPk(garden_id));
 		dh.delete_garden(getGardenPk(garden_id));
 		gardens.remove(garden_id);
 	}
 
 	public static void tmpupdateGarden(int garden_id, String bounds) {
 		dh.update_garden(getGardenPk(garden_id), bounds);
 	}
 	
 	public static void setGardenId(int g_pk, int serverId) {
 		dh.update_garden(g_pk, serverId);
 	}
 	
 	public static int getPlotPk(int garden_id, Plot plot) {
 		List<Integer> temp = dh.select_map_gp_po(getGardenPk(garden_id));
 		int po_pk = -1;
 		for(int i = 0; i < temp.size(); i++) {
 			if(po_pk != -1) 
 				break;
 			if(plot.getName().equalsIgnoreCase(dh.select_plot_name(temp.get(i).intValue())))
 				po_pk = temp.get(i);
 		}
 		return po_pk;
 	}
 
 	public static Plot getPlot(int garden_id, int plot_id) {
 		return getGarden(garden_id).getPlot(plot_id);
 	}
 
 	/** FIXME */
 	public static void addPlot(Garden garden, Plot plot) {
 		Rect bounds = plot.getBounds();
 		String shape_s = "" + bounds.left + "," + bounds.top + "," + bounds.right + "," + bounds.bottom + "," + Color.BLACK;
 		String points = plot.getType() == Plot.POLY ? "0,0" : ""; 
		dh.insert_plot(plot.getName(), shape_s, plot.getType(), plot.getPaint().getColor(), points, plot.getAngle(), plot.getServerId());
 		dh.insert_map_gp(garden.getGardenNum(), dh.count_plot());
 		plot.setPlotNum(dh.count_plot());
 		garden.addPlot(plot);
 		System.out.println("addPlot = " + dh.count_plot());
 	}
 
 	public static void addPlot(int garden_id, Plot plot) {
 		Rect bounds = plot.getBounds();
 		String shape_s = "" + bounds.left + "," + bounds.top + "," + bounds.right + "," + bounds.bottom + "," + Color.BLACK;
 		String points = plot.getType() == Plot.POLY ? "0,0" : ""; 
 		dh.insert_plot(plot.getName(), shape_s, plot.getType(), Color.BLACK, points, 0, 0);
 		dh.insert_map_gp(getGardenPk(garden_id), dh.count_plot());
 		getGarden(garden_id).addPlot(plot);
 		plot.setPlotNum(dh.count_plot());
 		System.out.println("addPlot = " + dh.count_plot());
 	}
 
 	public static void removePlot(int garden_id, int po_pk, Plot plot) {
 		getGarden(garden_id).remove(plot);
 		dh.delete_plot(po_pk);
 		dh.delete_map_gp_p(po_pk);
 	}
 
 	public static void updatePlot(int garden_id, Plot plot) {
 		String shape_s = plot.getBounds().left + "," + plot.getBounds().top + "," + plot.getBounds().right + "," + plot.getBounds().bottom + "," + plot.getPaint().getColor();
 		float[] polyPoints_f = plot.getPoints();
 		String polyPoints_s = "";
 		if(plot.getPoints().length >= 2) {
 			for(int i = 0; i < polyPoints_f.length; i++)
 				polyPoints_s = polyPoints_s + polyPoints_f[i] + ",";
 			polyPoints_s.substring(0, polyPoints_s.length() - 2);
 		}
 
 		List<Integer> temp = dh.select_map_gp_po(getGardenPk(garden_id));
 		int po_pk = -1;
 		for(int i = 0; i < temp.size(); i++) {
 			if(po_pk != -1) 
 				break;
 			if(plot.getName().equalsIgnoreCase(dh.select_plot_name(temp.get(i).intValue())))
 				po_pk = temp.get(i);
 		}				
 		dh.update_plot(po_pk, shape_s, plot.getColor(), polyPoints_s, plot.getAngle());
 	}
 	
 	public static void setPlotId(int po_pk, int id) {
 		dh.update_plot(po_pk, id);
 	}
 	
 	public static void initPlant(int po_pk, Plot plot) {
 		List<Integer> existing_plant = dh.select_map_pp_pa(po_pk);
 		if(plot.getPlants().size() != existing_plant.size()) {
 			for(int i = 0; i < existing_plant.size(); i++)
 				plot.addPlant(dh.select_plant(existing_plant.get(i)));
 		}
 	}
 
 	public static int getPlantPk(int po_pk, Plant plant) {
 		List<Integer> temp2 = dh.select_map_pp_pa(po_pk);
 		int pa_pk = -1;
 		for(int i = 0; i < temp2.size(); i++) {
 			if(pa_pk != -1) 
 				break;
 			if(plant.getName().equalsIgnoreCase(dh.select_plant_name(temp2.get(i).intValue())))
 				pa_pk = temp2.get(i);
 		}
 		return pa_pk;
 	}
 
 	/** FIXME */
 	public static void addPlant(Plot plot, Plant plant) {
		dh.insert_plant(plant.getName(), plant.getServerId());
 		dh.insert_map_pp(plot.getPlotNum(), dh.count_plant());
 		plant.setPlantNum(dh.count_plant());
 		plot.addPlant(plant);
 		System.out.println("addPlant = " + dh.count_plant());
 	}
 
 	public static void addPlant(int po_pk, String name, Plot plot) {
 		dh.insert_plant(name, 0);
 		dh.insert_map_pp(po_pk, dh.count_plant());
 		Plant temp = new Plant(name);
 		temp.setPlantNum(dh.count_plant());
 		plot.addPlant(temp);
 		System.out.println("addPlant = " + dh.count_plant());
 	}
 
 	public static void removePlant(int plant_id, int pa_pk, Plot plot) {
 		plot.getPlants().remove(plant_id);
 		dh.delete_plant(pa_pk);
 		dh.delete_map_pp(pa_pk);
 	}
 	
 	public static void setPlantId(int pa_pk, int id) {
 		dh.update_plant(pa_pk, id);
 	}
 	
 	public static void initEntry(int pa_pk, Plant plant) {
 		List<Integer> existing_entry = dh.select_map_pe_e(pa_pk);
 		if(plant.getEntries().size() != existing_entry.size()) {
 			for(int i = 0; i < existing_entry.size(); i++) {
 				plant.addEntry(dh.select_entry(existing_entry.get(i)));
 			}
 		}
 	}
 
 	public static int getEntryPk(int pa_pk, Entry entry) {
 		int e_pk = -1;
 		List<Integer> temp = dh.select_map_pe_e(pa_pk);
 		for(int i = 0; i < temp.size(); i++) {
 			if(e_pk != -1) 
 				break;
 			if(entry.getName().equalsIgnoreCase(dh.select_entry_name(temp.get(i).intValue())))
 				e_pk = temp.get(i);
 		}
 		return e_pk;
 	}
 
 	/** FIXME */
 	public static void addEntry(Plant plant, Entry entry) {
 		dh.insert_entry(entry.getName(), entry.getDate() + "", entry.getServerId());
 		dh.insert_map_pe(plant.getPlantNum(), dh.count_entry());
 		entry.setEntryNum(dh.count_entry());
 		plant.addEntry(entry);
 		System.out.println("addEntry = " + dh.count_entry());
 	}
 
 	public static void addEntry(int pa_pk, Plant plant, Entry entry) {
 		dh.insert_entry(entry.getName(), entry.getDate() + "", entry.getServerId());
 		dh.insert_map_pe(pa_pk, dh.count_entry());
 		entry.setEntryNum(dh.count_entry());
 		plant.addEntry(entry);
 		System.out.println("addEntry = " + dh.count_entry());
 	}
 
 	public static void updateEntry(int e_pk, String name, Entry entry) {
 		entry.setName(name);
 		dh.update_entry(e_pk, name);
 	}
 	
 	public static void setEntryId(int e_pk, int id) {
 		dh.update_entry(e_pk, id);
 	}
 	
 	public static void removeEntry(int entry_id, int e_pk, Plant plant) {
 		plant.getEntries().remove(entry_id);
 		dh.delete_map_pe(e_pk);
 		dh.delete_entry(e_pk);
 	}
 
 	public static Photo getPhoto(int garden_id, int image_index) {
 		return gardens.get(garden_id).getImage(image_index);
 	}
 
 	/** FIXME */
 	public static void addPhoto(Garden garden, Photo photo) {
 		dh.insert_photo(photo.getServerId(), photo.getUri().toString(), photo.getTitle());
 		dh.insert_map_gp2(garden.getGardenNum(), dh.count_photo());
 		garden.addImage(photo);
 		photo.setPhotoNum(dh.count_photo());
 		System.out.println("addPhoto = " + dh.count_photo());
 	}
 	
 	public static void setPhotoId(int ph_pk, int serverId) {
 		dh.update_photo(ph_pk, serverId);
 	}
 	
 	// unused? unimplemented
 	public static void addPhoto(int garden_id, Uri uri) {
 		Photo temp = new Photo(uri);
 		gardens.get(garden_id).addImage(temp);
 		temp.setPhotoNum(dh.count_photo());
 		System.out.println("addPhoto = " + dh.count_photo());
 		dh.insert_photo(0, uri.toString(), "");
 		dh.insert_map_gp2(GardenGnome.getGardenPk(garden_id), dh.count_photo());
 	}
 
 }
 
 public class StartScreen extends ListActivity implements DialogInterface.OnClickListener, View.OnClickListener, AdapterView.OnItemClickListener {
 
 	static GardenAdapter adapter;
 	ArrayList<Garden> gardens = GardenGnome.getGardens();
 	AlertDialog dialog;
 	View textEntryView;
 
 	LocationManager lm;
 	Geocoder geocoder;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		geocoder = new Geocoder(this, Locale.getDefault());
 		GardenGnome.tmpinit(this);
 
 		adapter = new GardenAdapter(this, R.layout.garden_list_item, gardens);
 		setListAdapter(adapter);
 		registerForContextMenu(getListView());
 		getListView().setOnItemClickListener(this);
 		findViewById(R.id.new_garden).setOnClickListener(this);
 		findViewById(R.id.search_encyclopedia).setOnClickListener(this);
 		findViewById(R.id.find_garden).setOnClickListener(this);
 	}
 
 	@Override
 	public void onClick(View view) {
 		switch (view.getId()) {
 		case R.id.new_garden:
 			showDialog(0);
 			break;
 		case R.id.search_encyclopedia:
 			startActivityForResult(new Intent(this, Encyclopedia.class), 0);
 			break;
 		case R.id.find_garden:
 			startActivityForResult(new Intent(this, FindGarden.class), 0);
 			break;
 		}
 	}
 
 	@Override
 	public Dialog onCreateDialog(int id) {
 		textEntryView = LayoutInflater.from(this).inflate(R.layout.text_entry_dialog, null);
 		AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(textEntryView);
 
 		dialog = builder.setTitle(R.string.new_garden_prompt)
 		.setPositiveButton(R.string.alert_dialog_ok, this)
 		.setNegativeButton(R.string.alert_dialog_cancel, null) // this means cancel was pressed
 		.create();
 
 		// automatically show soft keyboard
 		EditText input = (EditText) textEntryView.findViewById(R.id.dialog_text_entry);
 		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 				if (hasFocus)
 					dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
 			}
 		});
 
 		input.setOnKeyListener(new View.OnKeyListener() {
 			@Override public boolean onKey(View view, int keyCode, KeyEvent event) {
 				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
 					InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
 					mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
 					return true;
 				}
 				return false;
 			}
 		});
 
 		return dialog;
 	}
 
 	public void onClick(DialogInterface dialog, int whichButton) {
 		EditText textEntry = ((EditText) textEntryView.findViewById(R.id.dialog_text_entry));
 		String gardenName = textEntry.getText().toString().trim();
 		if (gardenName.length() == 0)
 			gardenName = "Untitled garden";
 		Intent intent = new Intent(this, GardenScreen.class);
 		intent.putExtra("garden_id", gardens.size());
 		Garden garden = new Garden(gardenName);
 		GardenGnome.addGardenServer(garden);
 		adapter.notifyDataSetChanged();
 		startActivityForResult(intent, 0);
 		new Thread(setLocation).start();
 		removeDialog(0);
 	}
 
 	/** sets garden location using user's physical location */
 	Runnable setLocation = new Runnable() {
 		@Override
 		public void run() {
 			Garden garden = gardens.get(gardens.size() - 1);
 			List<String> providers = lm.getProviders(false);
 			if (!providers.isEmpty()) {
 				Location loc = lm.getLastKnownLocation(providers.get(0));
 				List<Address> addresses = new ArrayList<Address>();
 				try {
 					addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
 				} catch (Exception e) { e.printStackTrace(); }
 				if (!addresses.isEmpty()) {
 					Address addr = addresses.get(0);
 					System.out.println(addr.getLocality() + "," + addr.getAdminArea() + "," + addr.getCountryCode());
 					garden.setCity(addr.getLocality());
 					if (addr.getAdminArea() != null)
 						garden.setState(addr.getAdminArea());
 					else
 						garden.setState(addr.getCountryCode());
 				}
 			}
 		}
 	};
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		adapter.notifyDataSetChanged();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		return true;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		Intent intent = new Intent(this, GardenScreen.class);
 		intent.putExtra("garden_id", gardens.get(position).getId());
 		startActivityForResult(intent, 0);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.m_contact:
 			Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
 			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
 			intent.setData(Uri.parse("mailto:" + getString(R.string.dev_email)));
 			intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "GardenGnome feedback");
 			startActivity(intent); //startActivity(Intent.createChooser(intent, "Send mail..."));
 			break;
 		case R.id.m_globaloptions:
 			startActivity(new Intent(this, GlobalSettings.class));
 			break;
 		case R.id.m_help:
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, view, menuInfo);
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 		menu.setHeaderTitle(gardens.get(info.position).getName());
 		menu.add(Menu.NONE, 0, Menu.NONE, "Edit info"); //menu.add(Menu.NONE).setNumericShortcut(1);
 		menu.add(Menu.NONE, 1, Menu.NONE, "Delete"); //menu.add("Delete").setNumericShortcut(2);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		switch (item.getItemId()) {
 		case 0:
 			Intent intent = new Intent(this, GardenAttr.class).putExtra("garden_id", info.position);
 			startActivityForResult(intent, 0);
 			break;
 		case 1:
 			GardenGnome.removeGarden(info.position);
 			adapter.notifyDataSetChanged();
 			break;
 		}
 
 		return super.onContextItemSelected(item);
 	}
 
 	class GardenAdapter extends ArrayAdapter<Garden> {
 		private ArrayList<Garden> gardens;
 		private LayoutInflater li;
 
 		public GardenAdapter(Context context, int textViewResourceId, ArrayList<Garden> items) {
 			super(context, textViewResourceId, items);
 			li = ((ListActivity) context).getLayoutInflater();
 			gardens = items;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View view = convertView;
 			if (view == null)
 				view = li.inflate(R.layout.garden_list_item, null);
 			Garden garden = gardens.get(position);
 			((TextView) view.findViewById(R.id.garden_name)).setText(garden.getName());
 			ImageView image = (ImageView) view.findViewById(R.id.preview_img); 
 			if (garden.getImages().isEmpty())
 				image.setImageResource(R.drawable.preview);
 			else {
 				Uri preview = garden.getPreview();
 				if (preview.getScheme().equals(ContentResolver.SCHEME_CONTENT))
 					image.setImageURI(preview);
 				else {
 					System.out.println("BITMAP FACTORY");
 					System.out.println(preview.getPath());
 					BitmapFactory.Options options = new BitmapFactory.Options();
 					//options.inSampleSize = 2;
 					DisplayMetrics metrics = new DisplayMetrics();
 					getWindowManager().getDefaultDisplay().getMetrics(metrics);
 					options.inTargetDensity = metrics.densityDpi;
 					options.outWidth = 75;
 					options.outHeight = 50;
 
 					image.setImageBitmap(BitmapFactory.decodeFile(preview.toString().replace("file://", ""), options));
 				}
 			}
 			return view;
 		}
 	}
 }
