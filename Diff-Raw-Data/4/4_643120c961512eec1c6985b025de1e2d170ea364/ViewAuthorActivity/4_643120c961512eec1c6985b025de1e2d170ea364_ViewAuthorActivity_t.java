 package eu.gounot.bnfdata;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.TextUtils;
 import android.text.method.LinkMovementMethod;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewStub;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import eu.gounot.bnfdata.adapter.WorkAdapter;
 import eu.gounot.bnfdata.data.Author;
 import eu.gounot.bnfdata.loadercallbacks.AuthorDataLoaderCallbacks;
 import eu.gounot.bnfdata.loadercallbacks.AuthorImageLoaderCallbacks;
 import eu.gounot.bnfdata.util.Constants;
 import eu.gounot.bnfdata.util.NetworkState;
 
 public class ViewAuthorActivity extends BnfDataBaseActivity implements OnItemClickListener,
         OnClickListener {
 
     private static final String TAG = "ViewAuthorActivity";
 
     // Loaders' IDs.
     public static final int DATA_LOADER = 0;
     public static final int IMAGE_LOADER = 1;
 
     // Views.
     private ProgressBar mProgressBar;
     private View mNetworkErrorView;
     private ListView mListView;
 
     private AuthorDataLoaderCallbacks mDataLoaderCallbacks;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         if (BuildConfig.DEBUG) {
             Log.d(TAG, "onCreate()");
         }
 
         setContentView(R.layout.activity_view_author);
 
         mListView = (ListView) findViewById(R.id.list);
         mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
 
         // Set up the GridLayout (in which all the TextViews are)
         // as the ListView's header.
         View header = View.inflate(this, R.layout.activity_view_author_header, null);
         mListView.addHeaderView(header, null, false);
 
         // Set up a transparent footer that serves as a padding between
         // the ListView's items and the bottom of the screen.
         View footer = View.inflate(this, R.layout.listview_footer, null);
         mListView.addFooterView(footer, null, false);
 
         mListView.setOnItemClickListener(this);
 
         // Get the ARK name from the intent.
         String arkName = getIntent().getExtras().getString(Constants.INTENT_ARK_NAME_KEY);
 
         // Initialize the data loader with its callbacks.
         mDataLoaderCallbacks = new AuthorDataLoaderCallbacks(this, arkName);
         getSupportLoaderManager().initLoader(DATA_LOADER, null, mDataLoaderCallbacks);
     }
 
     public void onNetworkError() {
         if (BuildConfig.DEBUG) {
             Log.d(TAG, "onNetworkError()");
         }
 
         // Load the network error ViewStub if it was not already loaded.
         if (mNetworkErrorView == null) {
             // Inflate the network error view and reference it for further use.
             ViewStub viewStub = (ViewStub) findViewById(R.id.network_error_stub);
             viewStub.setLayoutResource(R.layout.network_error);
             mNetworkErrorView = viewStub.inflate();
 
             // Set the OnClickListener to the Retry button.
             Button retryButton = (Button) mNetworkErrorView.findViewById(R.id.retry_button);
             retryButton.setOnClickListener(this);
         }
 
         int errMsgResId;
 
         // Select an appropriate error message.
         if (!NetworkState.isNetworkAvailable(getApplicationContext())) {
             errMsgResId = R.string.errmsg_no_network_connection;
         } else {
             errMsgResId = R.string.errmsg_data_retrieval_failed;
         }
 
         // Set the error message to the network error TextView.
         TextView errorMessageTextView = (TextView) mNetworkErrorView
                 .findViewById(R.id.error_message);
         errorMessageTextView.setText(errMsgResId);
 
         // Hide the progress bar.
         mProgressBar.setVisibility(View.GONE);
 
         // Show the network error view.
         mNetworkErrorView.setVisibility(View.VISIBLE);
     }
 
     public void onDataLoaded(Author author) {
         if (BuildConfig.DEBUG) {
             Log.d(TAG, "onDataLoaded()");
         }
 
         setName(author.getGivenName(), author.getFamilyName());
         setDates(author.getDates());
         setCountry(author.getCountry());
         setLanguage(author.getLanguageName());
         setGender(author.getGender());
         setDateOfBirth(author.getDateOfBirth(), author.getPlaceOfBirth());
         setDateOfDeath(author.getDateOfDeath(), author.getPlaceOfDeath());
         setBiographicalInfos(author.getBiographicalInfos());
         setFieldsOfActivity(author.getFieldsOfActivity());
         setAltForms(author.getAltForms());
         setEditorialNotes(author.getEditorialNotes());
         setExternalLinks(author.getCatalogueUrl(), author.getWikipediaUrl());
         setWorks(author.getWorks());
 
         String imageUrl = author.getImageUrl();
         if (imageUrl != null) {
             AuthorImageLoaderCallbacks callbacks = new AuthorImageLoaderCallbacks(this, imageUrl);
             getSupportLoaderManager().initLoader(IMAGE_LOADER, null, callbacks);
         } else {
             setImage(null);
         }
 
         // Hide the network error view if it has already been inflated.
         if (mNetworkErrorView != null) {
             mNetworkErrorView.setVisibility(View.GONE);
         }
 
         // Hide the progress bar.
         mProgressBar.setVisibility(View.GONE);
 
         // Show the ListView.
         mListView.setVisibility(View.VISIBLE);
     }
 
     public void onImageLoaded(Bitmap bitmap) {
         setImage(bitmap);
     }
 
     @Override
     public void onClick(View v) {
         // Following a network error, the user has clicked the Retry button,
         // so we restart the loader to try loading the data again.
         mNetworkErrorView.setVisibility(View.INVISIBLE);
         mProgressBar.setVisibility(View.VISIBLE);
         getSupportLoaderManager().restartLoader(DATA_LOADER, null, mDataLoaderCallbacks);
     }
 
     @Override
     public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
         if (BuildConfig.DEBUG) {
             Log.d(TAG, "onItemClick() position=" + position);
         }
 
         // Create an intent, put in the selected work's URL,
         // and start the ViewWorkActivity to view this work in detail.
         Intent intent = new Intent(this, ViewWorkActivity.class);
         Author.Work work = (Author.Work) mListView.getAdapter().getItem(position);
        intent.putExtra(Constants.INTENT_ARK_NAME_KEY, work.getArkName());
         startActivity(intent);
     }
 
     // These methods below are used by the onLoadFinished() callback
     // to update the UI with the loaded data.
 
     public void setImage(Bitmap image) {
         ImageView imageView = (ImageView) findViewById(R.id.image);
         if (image != null) {
             imageView.setImageBitmap(image);
         } else {
             imageView.setImageResource(R.drawable.no_author_or_organization_image);
         }
         findViewById(R.id.image_progress_bar).setVisibility(View.GONE);
     }
 
     public void setName(String givenName, String familyName) {
         TextView nameTextView = (TextView) findViewById(R.id.name);
         String name = "";
         if (givenName != null) {
             name = givenName + " ";
         }
         if (familyName != null) {
             name += familyName;
         }
         if (!name.equals("")) {
             nameTextView.setText(name);
         } else {
             nameTextView.setVisibility(View.GONE);
         }
     }
 
     public void setDates(String dates) {
         TextView datesTextView = (TextView) findViewById(R.id.dates);
         if (dates != null) {
             datesTextView.setText(dates);
         } else {
             datesTextView.setVisibility(View.GONE);
         }
     }
 
     public void setCountry(String nationalities) {
         TextView countryLabelTextView = (TextView) findViewById(R.id.country_label);
         TextView countryTextView = (TextView) findViewById(R.id.country);
         if (nationalities != null) {
             countryTextView.setText(nationalities);
         } else {
             countryLabelTextView.setVisibility(View.GONE);
             countryTextView.setVisibility(View.GONE);
         }
     }
 
     public void setLanguage(String language) {
         TextView languageLabelTextView = (TextView) findViewById(R.id.language_label);
         TextView languageTextView = (TextView) findViewById(R.id.language);
         if (language != null) {
             languageTextView.setText(language);
         } else {
             languageLabelTextView.setVisibility(View.GONE);
             languageTextView.setVisibility(View.GONE);
         }
     }
 
     public void setGender(String gender) {
         TextView genderLabelTextView = (TextView) findViewById(R.id.gender_label);
         TextView genderTextView = (TextView) findViewById(R.id.gender);
         if (gender != null) {
             genderTextView.setText(gender);
         } else {
             genderLabelTextView.setVisibility(View.GONE);
             genderTextView.setVisibility(View.GONE);
         }
     }
 
     public void setDateOfBirth(String dateOfBirth, String placeOfBirth) {
         TextView dateOfBirthLabelTextView = (TextView) findViewById(R.id.date_of_birth_label);
         TextView dateOfBirthTextView = (TextView) findViewById(R.id.date_of_birth);
         String birth = "";
         if (dateOfBirth != null) {
             birth = dateOfBirth;
         }
         if (placeOfBirth != null) {
             if (!birth.equals("")) {
                 birth += ", ";
             }
             birth += placeOfBirth;
         }
         if (!birth.equals("")) {
             dateOfBirthTextView.setText(birth);
         } else {
             dateOfBirthLabelTextView.setVisibility(View.GONE);
             dateOfBirthTextView.setVisibility(View.GONE);
         }
     }
 
     public void setDateOfDeath(String dateOfDeath, String placeOfDeath) {
         TextView dateOfDeathLabelTextView = (TextView) findViewById(R.id.date_of_death_label);
         TextView dateOfDeathTextView = (TextView) findViewById(R.id.date_of_death);
         String death = "";
         if (dateOfDeath != null) {
             death = dateOfDeath;
         }
         if (placeOfDeath != null) {
             if (!death.equals("")) {
                 death += ", ";
             }
             death += placeOfDeath;
         }
         if (!death.equals("")) {
             dateOfDeathTextView.setText(death);
         } else {
             dateOfDeathLabelTextView.setVisibility(View.GONE);
             dateOfDeathTextView.setVisibility(View.GONE);
         }
     }
 
     public void setBiographicalInfos(String[] biographicalInfos) {
         TextView biographicalInfosLabelTextView = (TextView) findViewById(R.id.notes_label);
         TextView biographicalInfosTextView = (TextView) findViewById(R.id.notes);
         if (biographicalInfos != null) {
             biographicalInfosTextView.setText(TextUtils.join("\n", biographicalInfos));
         } else {
             biographicalInfosLabelTextView.setVisibility(View.GONE);
             biographicalInfosTextView.setVisibility(View.GONE);
         }
     }
 
     public void setFieldsOfActivity(String[] fieldsOfActivity) {
         TextView fieldsOfActivityLabelTextView = (TextView) findViewById(R.id.fields_of_activity_label);
         TextView fieldsOfActivityTextView = (TextView) findViewById(R.id.fields_of_activity);
         if (fieldsOfActivity != null) {
             fieldsOfActivityTextView.setText(TextUtils.join("\n", fieldsOfActivity));
         } else {
             fieldsOfActivityLabelTextView.setVisibility(View.GONE);
             fieldsOfActivityTextView.setVisibility(View.GONE);
         }
     }
 
     public void setAltForms(String[] altForms) {
         TextView altFormsLabelTextView = (TextView) findViewById(R.id.alt_forms_label);
         TextView altFormsTextView = (TextView) findViewById(R.id.alt_forms);
         if (altForms != null) {
             altFormsTextView.setText(TextUtils.join("\n", altForms));
         } else {
             altFormsLabelTextView.setVisibility(View.GONE);
             altFormsTextView.setVisibility(View.GONE);
         }
     }
 
     public void setEditorialNotes(String[][] editorialNotes) {
         TextView editorialNotesLabelTextView = (TextView) findViewById(R.id.editorial_notes_label);
         TextView editorialNotesTextView = (TextView) findViewById(R.id.editorial_notes);
         if (editorialNotes != null) {
             String[] noteGroups = new String[editorialNotes.length];
             for (int i = 0; i < editorialNotes.length; i++) {
                 noteGroups[i] = TextUtils.join("\n", editorialNotes[i]);
             }
             editorialNotesTextView.setText(TextUtils.join("\n\n", noteGroups));
         } else {
             editorialNotesLabelTextView.setVisibility(View.GONE);
             editorialNotesTextView.setVisibility(View.GONE);
         }
     }
 
     public void setExternalLinks(String catalogueUrl, String wikipediaUrl) {
         TextView externalLinksLabelTextView = (TextView) findViewById(R.id.external_links_label);
         TextView externalLinksTextView = (TextView) findViewById(R.id.external_links);
         String urls = "";
         if (catalogueUrl != null) {
             urls += "<a href=\"" + catalogueUrl + "\">"
                     + getResources().getString(R.string.catalogue_link_text) + "</a>";
         }
         if (wikipediaUrl != null) {
             if (!urls.equals("")) {
                 urls += "<br />";
             }
             urls += "<a href=\"" + wikipediaUrl + "\">"
                     + getResources().getString(R.string.wikipedia_link_text) + "</a>";
         }
         if (!urls.equals("")) {
             externalLinksTextView.setText(Html.fromHtml(urls));
             externalLinksTextView.setMovementMethod(LinkMovementMethod.getInstance());
         } else {
             externalLinksLabelTextView.setVisibility(View.GONE);
             externalLinksTextView.setVisibility(View.GONE);
         }
     }
 
     public void setWorks(Author.Work[] works) {
         WorkAdapter adapter = new WorkAdapter(this, works);
         mListView.setAdapter(adapter);
     }
 
 }
