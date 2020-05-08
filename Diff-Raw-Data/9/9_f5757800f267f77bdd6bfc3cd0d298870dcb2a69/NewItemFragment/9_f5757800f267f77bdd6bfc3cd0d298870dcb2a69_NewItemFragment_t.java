 package il.ac.huji.shoppit;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.app.ProgressDialog;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 import com.parse.GetDataCallback;
 import com.parse.ParseACL;
 import com.parse.ParseException;
 import com.parse.ParseFile;
 import com.parse.ParseGeoPoint;
 import com.parse.ParseImageView;
 import com.parse.ParseUser;
 import com.parse.SaveCallback;
 
 /**
  * @author Elie2
  * One of the two fragments used by NewItemActivity.
  * This fragment manages the data entry for a
  * new Item object. It lets the user input a 
  * item name and take a photo. If there is already a photo
  * associated with this item, it will be displayed in the 
  * preview at the bottom, which is a standalone
  * ParseImageView.
  */
 public class NewItemFragment extends Fragment {
 
 	private ImageView photoImageView;
 	private ParseImageView parseImageView;
 	private EditText nameEditText;
 	private EditText priceEditText;
 	private Spinner currencySpinner;
 	private Spinner categorySpinner;
 	private EditText keywordsEditText;
 	private Button doneButton;
 
 	private byte[] photoData;
 
 	private List<Item> itemList; //List of items with a similar barcode
 	int selectedItem = 0;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
 			Bundle SavedInstanceState) {
 		View v = inflater.inflate(R.layout.fragment_new_item, parent, false);
 
 		photoImageView = ((ImageView) v.findViewById(R.id.photoImageView));
 		parseImageView = ((ParseImageView) v.findViewById(R.id.parseImageView));
 		nameEditText = ((EditText) v.findViewById(R.id.nameEditText));
 		priceEditText = ((EditText) v.findViewById(R.id.priceEditText));
 		currencySpinner = ((Spinner) v.findViewById(R.id.currencySpinner));
 		categorySpinner = ((Spinner) v.findViewById(R.id.categorySpinner));
 		keywordsEditText = ((EditText) v.findViewById(R.id.keywordsEditText));
 		doneButton = ((Button) v.findViewById(R.id.doneButton));
 
 		ImageButton prev = (ImageButton) v.findViewById(R.id.prev_pic);
 		ImageButton next = (ImageButton) v.findViewById(R.id.next_pic);
 		ImageButton change = (ImageButton) v.findViewById(R.id.change_pic);
 
 		//If no barcode has been scanned, don't show the buttons over the image
 		itemList = ((NewItemActivity) getActivity()).getItemList();
 		if (itemList == null) {
 			prev.setVisibility(View.GONE);
 			next.setVisibility(View.GONE);
 			change.setVisibility(View.GONE);
 			parseImageView.setVisibility(View.INVISIBLE);
 		}
 		else {
 			photoImageView.setVisibility(View.INVISIBLE);
 		}
 
 
 		// set up currency spinner
 		ArrayAdapter<CharSequence> currencyAdapter = ArrayAdapter.createFromResource(getActivity(),
 				R.array.currencies_array, android.R.layout.simple_spinner_item);
 		currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		currencySpinner.setAdapter(currencyAdapter);
 
 		// set up category spinner
 		ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(getActivity(),
 				R.array.categories_array, android.R.layout.simple_spinner_item);
 		categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		categorySpinner.setAdapter(categoryAdapter);
 
 		// set up done button
 		doneButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				addItem();
 			}
 
 		});
 
 		//Listeners for the buttons that appear after scanning a barcode
 
 		prev.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				selectedItem = (--selectedItem < 0 ? itemList.size() - 1 : selectedItem);
 				Item item = itemList.get(selectedItem);
 				parseImageView.setParseFile(item.getPhotoFile());
 				parseImageView.loadInBackground(new GetDataCallback() {
 					public void done(byte[] data, ParseException e) {}
 				});
 				nameEditText.setText(item.getName());
 				selectCategory(item.getMainCategory());
 			}
 
 		});
 
 		next.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				selectedItem = (selectedItem + 1) % itemList.size();
 				Item item = itemList.get(selectedItem);
 				parseImageView.setParseFile(item.getPhotoFile());
 				parseImageView.loadInBackground(new GetDataCallback() {
 					public void done(byte[] data, ParseException e) {}
 				});
 				nameEditText.setText(item.getName());
 				selectCategory(item.getMainCategory());
 			}
 
 		});
 
 		change.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				//Save the info that was filled
 				NewItemActivity nia = (NewItemActivity) getActivity();
 				nia.savedData = true;
 				nia.name = nameEditText.getText().toString();
 				nia.price = priceEditText.getText().toString();
 				nia.currencySelection = currencySpinner.getSelectedItemPosition();
 				nia.categorySelection = categorySpinner.getSelectedItemPosition();
 				nia.categorySelection = categorySpinner.getSelectedItemPosition();
 				nia.keywords = keywordsEditText.getText().toString();
 				
 				//Start the camera again
 				getFragmentManager().popBackStack();
 			}
 
 		});
 
 
 
 		return v;
 	}
 
 	public void startCamera() {
 		//		Fragment cameraFragment = new CameraFragment();
 		//		FragmentTransaction transaction = getActivity().getFragmentManager()
 		//				.beginTransaction();
 		//		transaction.replace(R.id.fragmentContainer, cameraFragment);
 		//		transaction.addToBackStack("NewItemFragment");
 		//		transaction.commit();
 
 		FragmentManager fm = getActivity().getFragmentManager();
 		fm.popBackStack("NewCameraFragment",
 				FragmentManager.POP_BACK_STACK_INCLUSIVE);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		//Check if data has been saved previously, restore if so
 		
 		NewItemActivity nia = (NewItemActivity) getActivity();
 		if (nia.savedData) {
 			nameEditText.setText(nia.name);
 			priceEditText.setText(nia.price);
 			currencySpinner.setSelection(nia.currencySelection);
 			categorySpinner.setSelection(nia.categorySelection);
 			keywordsEditText.setText(nia.keywords);
 		}
 		else { //If not, select the "other" category.
 			categorySpinner.setSelection(categorySpinner.getCount() - 1);
 		}
 
 		
 		photoData = ((NewItemActivity) getActivity()).getCurrentPhotoData();
 
 		if (photoData != null) {
 			Bitmap itemImageBitmap = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
 			photoImageView.setImageBitmap(itemImageBitmap);
 		}
 
 		//If a barcode has been scanned, show the first image and text
 		if (itemList != null) {
 
 			Item item = itemList.get(selectedItem);
 			parseImageView.setParseFile(item.getPhotoFile());
 			parseImageView.loadInBackground(new GetDataCallback() {
 				public void done(byte[] data, ParseException e) {
 					if (e != null)
 						e.printStackTrace();
 				}
 			});
 			nameEditText.setText(item.getName());
 			selectCategory(item.getMainCategory());
 		}
 	}
 	
 	
 	/**
 	 * Select the category that has this text, or "other" if any error rises.
 	 * @param category
 	 */
 	private void selectCategory(String category) {
 		for (int i = 0; i < categorySpinner.getCount(); i++) {
 			String listCateg = categorySpinner.getItemAtPosition(i).toString();
 			if (listCateg.equals(category)) {
 				categorySpinner.setSelection(i);
 				return;
 			}
 		}
 		categorySpinner.setSelection(categorySpinner.getCount() - 1);
 	}
 	
 
 	private void addItem() {
 
 		final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", "Adding item...", true);
 
 		String name = nameEditText.getText().toString().trim().replaceAll("[ \t]+", " ");
 		String price = priceEditText.getText().toString();
 
 		// TODO - check everything is okay with category, currency and keywords
 		String currency = currencySpinner.getSelectedItem().toString();
 		String category = categorySpinner.getSelectedItem().toString();
 
 		String keywordsString = keywordsEditText.getText().toString().toLowerCase();
 		String [] keywords = keywordsString.split("\\s+");
 
 		//Perform sanity checks
 
 		if (name.length() == 0) {
 			Toast.makeText(getActivity(), "Please enter the item's name",
 					Toast.LENGTH_LONG).show();
 			return;
 		}
 
 		if (name.length() < 3) {
 			Toast.makeText(getActivity(), "Item name is too short",
 					Toast.LENGTH_LONG).show();
 			return;
 		}
 
 		if (name.length() > 30) {
 			Toast.makeText(getActivity(), "Item name is too long",
 					Toast.LENGTH_LONG).show();
 			return;
 		}
 
 		if (price.length() == 0) {
 			Toast.makeText(getActivity(), "Please enter the item's price",
 					Toast.LENGTH_LONG).show();
 			return;
 		}
 
 		if (price.length() > 6) {
 			Toast.makeText(getActivity(), "Item price is too long",
 					Toast.LENGTH_LONG).show();
 			return;
 		}
 
 		//Check there are no more than two digits after the decimal point.
 		int decimalPos = price.indexOf(".");
 		if (decimalPos >= 0 && price.substring(decimalPos + 1).length() > 2) {
 			Toast.makeText(getActivity(), "Item price is invalid",
 					Toast.LENGTH_LONG).show();
 			return;
 		}
 
 		//Check if the price is a valid number
 		float priceVal = 0;
 		try {
 			priceVal = Float.parseFloat(price);
 		} catch (Exception e) {
 			Toast.makeText(getActivity(), "Item price is invalid",
 					Toast.LENGTH_LONG).show();
 			return;
 		}
 
 		if (priceVal == 0) {
 			Toast.makeText(getActivity(), "Item price cannot be zero",
 					Toast.LENGTH_LONG).show();
 			return;
 		}
 
 		doneButton.setEnabled(false);
 
 		//Data is OK, upload the item to parse.
 
 		Item newItem = new Item();
 		newItem.setName(name);
 		newItem.setPrice(Double.parseDouble(price));
 		newItem.setCurrency(currency);
 		newItem.setAuthor(ParseUser.getCurrentUser());
 		newItem.setMainCategory(category);
 		newItem.setKeywords(keywords);
 
 		// location
 		// if location is 0, 0 then we have a problem. we could check for this, even though it's unlikely to happen.
 		ParseGeoPoint point = new ParseGeoPoint(
 				((NewItemActivity) getActivity()).getLatitude(),
 				((NewItemActivity) getActivity()).getLongitude());
 		newItem.setLocation(point);
 
 		// photo
		if (photoData != null) {
 			ParseFile photoFile = new ParseFile("photo.jpg", photoData);
 			newItem.setPhotoFile(photoFile);
 		}
 		else
 			newItem.setPhotoFile(itemList.get(selectedItem).getPhotoFile());
 
 		// Permissions
 		// everyone can read the item, only user that creates it can edit it.
 		ParseACL itemACL = new ParseACL(ParseUser.getCurrentUser());
 		itemACL.setPublicReadAccess(true);
 		newItem.setACL(itemACL);
 
 		//Barcode
 		String barcode = ((NewItemActivity) getActivity()).getBarcode();
 		if (barcode != null)
 			newItem.setBarcode(barcode);
 
 		newItem.saveInBackground(new SaveCallback() {
 			@Override
 			public void done(ParseException e) {
 				progressDialog.dismiss();
 				if (e == null) {
 					Toast.makeText(getActivity(), "Item added successfully",
 							Toast.LENGTH_LONG).show();
 					getActivity().setResult(Activity.RESULT_OK);
 					getActivity().finish();
 				} else {
 					Toast.makeText(getActivity(), "Error adding item " + e.getCode() + " " + e.getMessage(),
 							Toast.LENGTH_LONG).show();
 				}
 			}
 
 		});
 	}
 
 }
