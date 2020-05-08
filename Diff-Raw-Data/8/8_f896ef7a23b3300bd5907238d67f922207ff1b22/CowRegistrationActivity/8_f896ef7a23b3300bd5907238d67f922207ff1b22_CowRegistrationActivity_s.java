 package org.cgiar.ilri.mistro.farmer;
 
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.ScrollView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 
 import org.cgiar.ilri.mistro.farmer.carrier.Cow;
 import org.cgiar.ilri.mistro.farmer.carrier.Dam;
 import org.cgiar.ilri.mistro.farmer.carrier.Farmer;
 import org.cgiar.ilri.mistro.farmer.carrier.Sire;
 
 import java.lang.reflect.Field;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 
 public class CowRegistrationActivity extends SherlockActivity implements View.OnClickListener,TextWatcher,AdapterView.OnItemSelectedListener,View.OnFocusChangeListener,DatePickerDialog.OnDateSetListener,ListView.OnItemClickListener
 {
     public static final String TAG="CowRegistrationActivity";
     public static final String KEY_MODE="mode";
     public static final String KEY_INDEX="index";
     public static final String KEY_NUMBER_OF_COWS="numberOfCows";
     public static final int MODE_COW=0;
     public static final int MODE_SIRE=1;
     public static final int MODE_DAM=2;
    private final String dateFormat="MM/yyyy";
 
     private int mode;
     private int index;
     private int numberOfCows;
     private String localeCode;
     private int selectedBreeds=0;
     private String maxSelectedBreedsWarning;
     private String deformityOSpecifyText;
     private Cow thisCow;
     private Farmer farmer;
 
     private TextView strawNumberTV;
     private EditText strawNumberET;
     private TextView embryoNumberTV;
     private EditText embryoNumberET;
     private TextView vetUsedTV;
     private EditText vetUsedET;
     private TextView nameTV;
     private EditText nameET;
     private TextView earTagNumberTV;
     private EditText earTagNumberET;
     private TextView ageTV;
     private Spinner ageS;
     private EditText ageET;
     private TextView dateOfBirthTV;
     private EditText dateOfBirthET;
     private TextView breedTV;
     private EditText breedET;
     private TextView sexTV;
     private Spinner sexS;
     private TextView deformityTV;
     private EditText deformityET;
     private TextView sireTV;
     private EditText sireET;
     private TextView damTV;
     private EditText damET;
     private Button previousButton;
     private Button nextButton;
     private boolean monitorAgeChange=true;
     private DatePickerDialog datePickerDialog;
     private Dialog breedDialog;
     private ScrollView breedDialogSV;
     private ListView breedLV;
     private Button dialogBreedOkayB;
     private String[] breeds;
     private Dialog deformityDialog;
     private ListView deformityLV;
     private EditText specifyET;
     private Button dialogDeformityOkayB;
     private String[] deformities;
     private TextView serviceTypeTV;
     private Spinner serviceTypeS;
     private TextView countryOfOriginTV;
     private AutoCompleteTextView countryOfOriginACTV;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_cow_registration);
 
         localeCode="en";//TODO:get locale from sharedPreferences
 
         Bundle bundle=this.getIntent().getExtras();
         if(bundle!=null)
         {
             mode=bundle.getInt(KEY_MODE);
             index=bundle.getInt(KEY_INDEX);
             numberOfCows=bundle.getInt(KEY_NUMBER_OF_COWS);
         }
         else
         {
             Toast.makeText(this,"Bundle is null, going rogue!",Toast.LENGTH_LONG).show();
         }
 
         //init child views
         strawNumberTV=(TextView)this.findViewById(R.id.straw_number_tv);
         strawNumberET=(EditText)this.findViewById(R.id.straw_number_et);
         embryoNumberTV=(TextView)this.findViewById(R.id.embryo_number_tv);
         embryoNumberET=(EditText)this.findViewById(R.id.embryo_number_et);
         vetUsedTV=(TextView)this.findViewById(R.id.vet_used_tv);
         vetUsedET=(EditText)this.findViewById(R.id.vet_used_et);
         nameTV=(TextView)this.findViewById(R.id.name_tv);
         nameET=(EditText)this.findViewById(R.id.name_et);
         earTagNumberTV=(TextView)this.findViewById(R.id.ear_tag_number_tv);
         earTagNumberET=(EditText)this.findViewById(R.id.ear_tag_number_et);
         ageTV=(TextView)this.findViewById(R.id.age_tv);
         ageS=(Spinner)this.findViewById(R.id.age_s);
         //ageS.setOnItemSelectedListener(this);
         ageET=(EditText)this.findViewById(R.id.age_et);
         //ageET.addTextChangedListener(this);
         dateOfBirthTV=(TextView)this.findViewById(R.id.date_of_birth_tv);
         dateOfBirthET=(EditText)this.findViewById(R.id.date_of_birth_et);
         dateOfBirthET.setOnFocusChangeListener(this);
         dateOfBirthET.setOnClickListener(this);
         breedTV=(TextView)this.findViewById(R.id.breed_tv);
         breedET=(EditText)this.findViewById(R.id.breed_et);
         breedET.setOnFocusChangeListener(this);
         breedET.setOnClickListener(this);
         sexTV=(TextView)this.findViewById(R.id.sex_tv);
         sexS=(Spinner)this.findViewById(R.id.sex_s);
         deformityTV=(TextView)this.findViewById(R.id.deformity_tv);
         deformityET=(EditText)this.findViewById(R.id.deformity_et);
         deformityET.setOnFocusChangeListener(this);
         deformityET.setOnClickListener(this);
         sireTV=(TextView)this.findViewById(R.id.sire_tv);
         sireET=(EditText)this.findViewById(R.id.sire_et);
         sireET.setOnFocusChangeListener(this);
         sireET.setOnClickListener(this);
         damTV=(TextView)this.findViewById(R.id.dam_tv);
         damET=(EditText)this.findViewById(R.id.dam_et);
         damET.setOnFocusChangeListener(this);
         damET.setOnClickListener(this);
         serviceTypeTV=(TextView)this.findViewById(R.id.service_type_tv);
         serviceTypeS=(Spinner)this.findViewById(R.id.service_type_s);
         serviceTypeS.setOnItemSelectedListener(this);
         countryOfOriginTV=(TextView)this.findViewById(R.id.country_of_origin_tv);
         countryOfOriginACTV =(AutoCompleteTextView)this.findViewById(R.id.country_of_origin_actv);
         previousButton=(Button)this.findViewById(R.id.previous_button);
         previousButton.setOnClickListener(this);
         /*if(mode==MODE_DAM||mode==MODE_SIRE||index==0)
         {
             previousButton.setVisibility(Button.INVISIBLE);
         }*/
         nextButton=(Button)this.findViewById(R.id.next_button);
         nextButton.setOnClickListener(this);
         breedDialog=new Dialog(this);
         breedDialog.setContentView(R.layout.dialog_breed);
         dialogBreedOkayB=(Button)breedDialog.findViewById(R.id.dialog_breed_okay_b);
         dialogBreedOkayB.setOnClickListener(this);
         /*WindowManager.LayoutParams dialogLayoutParams=new WindowManager.LayoutParams();
         dialogLayoutParams.copyFrom(breedDialog.getWindow().getAttributes());
         dialogLayoutParams.width=WindowManager.LayoutParams.MATCH_PARENT;
         dialogLayoutParams.height=WindowManager.LayoutParams.MATCH_PARENT;
         breedDialog.getWindow().setAttributes(dialogLayoutParams);*/
         breedDialogSV=(ScrollView)breedDialog.findViewById(R.id.dialog_breed_sv);
         breedLV=(ListView)breedDialog.findViewById(R.id.breed_lv);
         breedLV.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
         breedLV.setOnItemClickListener(this);
         deformityDialog =new Dialog(this);
         deformityDialog.setContentView(R.layout.dialog_deformity);
         deformityLV =(ListView) deformityDialog.findViewById(R.id.deformity_lv);
         deformityLV.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
         deformityLV.setOnItemClickListener(this);
         specifyET=(EditText)deformityDialog.findViewById(R.id.specify_et);
         dialogDeformityOkayB =(Button) deformityDialog.findViewById(R.id.dialog_deformity_okay_b);
 
         //init text in child views
         initTextInViews(localeCode);
         resetMode();
     }
 
     @Override
     protected void onResume()
     {
         super.onResume();
 
         Bundle bundle=this.getIntent().getExtras();
         if(bundle!=null)
         {
             farmer=bundle.getParcelable(Farmer.PARCELABLE_KEY);
             if(mode==MODE_COW)
             {
                 if(farmer!=null)
                     thisCow=farmer.getCow(index);
                 else
                     Log.d(TAG,"Farmer object is null");
             }
             else if(mode==MODE_DAM)
             {
                 if(farmer!=null)
                     thisCow=farmer.getCow(index).getDam();
                 else
                     Log.d(TAG,"Farmer object is null");
             }
             else if(mode==MODE_SIRE)
             {
                 if(farmer!=null)
                     thisCow=farmer.getCow(index).getSire();
                 else
                     Log.d(TAG,"Farmer object is null");
             }
         }
         if(thisCow!=null)
         {
             nameET.setText(thisCow.getName());
             earTagNumberET.setText(thisCow.getEarTagNumber());
             dateOfBirthET.setText(thisCow.getDateOfBirth());
             ageET.setText((thisCow.getAge()==-1) ? "":String.valueOf(thisCow.getAge()));
             if(thisCow.getAgeType()==Cow.AGE_TYPE_DAY)
             {
                 ageS.setSelection(0);
             }
             else if(thisCow.getAgeType()==Cow.AGE_TYPE_WEEK)
             {
                 ageS.setSelection(1);
             }
             else if(thisCow.getAgeType()==Cow.AGE_TYPE_YEAR)
             {
                 ageS.setSelection(2);
             }
             List<String> savedBreeds=thisCow.getBreeds();
             String breed="";
             for (int i=0;i<savedBreeds.size();i++)
             {
                 if(i==0)
                 {
                     breed=savedBreeds.get(i);
                 }
                 else
                 {
                     breed=breed+", "+savedBreeds.get(i);
                 }
             }
             breedET.setText(breed);
             if(thisCow.getSex()==Cow.SEX_FEMALE)
             {
                 sexS.setSelection(0);
             }
             else
             {
                 sexS.setSelection(1);
             }
             List<String> savedDeformities=thisCow.getDeformities();
             String deformity="";
             for (int i=0;i<savedDeformities.size();i++)
             {
                 if(i==0)
                 {
                     deformity=savedDeformities.get(i);
                 }
                 else
                 {
                     deformity=deformity+", "+savedDeformities.get(i);
                 }
             }
             deformityET.setText(deformity);
             Sire sire=thisCow.getSire();
             if(sire!=null)
             {
                 if(sire.getName()!=null||sire.getName().length()>0)
                     sireET.setText(sire.getName());
                 else if(sire.getEarTagNumber()!=null||sire.getEarTagNumber().length()>0)
                     damET.setText(sire.getEarTagNumber());
                 else if(sire.getStrawNumber()!=null||sire.getStrawNumber().length()>0)
                     damET.setText(sire.getStrawNumber());
             }
             Dam dam=thisCow.getDam();
             if(dam!=null)
             {
                 if(dam.getName()!=null||dam.getName().length()>0)
                     damET.setText(dam.getName());
                 else if(dam.getEarTagNumber()!=null||dam.getEarTagNumber().length()>0)
                     damET.setText(dam.getEarTagNumber());
                 else if(dam.getEmbryoNumber()!=null||dam.getEmbryoNumber().length()>0)
                     damET.setText(dam.getEmbryoNumber());
             }
             countryOfOriginACTV.setText(thisCow.getCountryOfOrigin());
             if(mode==MODE_DAM)
             {
                 if(((Dam) thisCow).getServiceType()==Dam.SERVICE_TYPE_COW)
                 {
                     serviceTypeS.setSelection(0);
                 }
                 else
                 {
                     serviceTypeS.setSelection(1);
                 }
                 embryoNumberET.setText(((Dam) thisCow).getEmbryoNumber());
                 vetUsedET.setText(((Dam) thisCow).getVetUsed());
             }
             else if(mode==MODE_SIRE)
             {
                 if(((Sire)thisCow).getServiceType()==Sire.SERVICE_TYPE_BULL)
                 {
                     serviceTypeS.setSelection(0);
                 }
                 else
                 {
                     serviceTypeS.setSelection(1);
                 }
                 strawNumberET.setText(((Sire)thisCow).getStrawNumber());
                 vetUsedET.setText(((Sire)thisCow).getVetUsed());
             }
         }
     }
 
     private void resetMode()
     {
         if(mode==MODE_DAM)
         {
             sexTV.setVisibility(TextView.GONE);
             sexS.setVisibility(Spinner.GONE);
             damTV.setVisibility(TextView.GONE);
             damET.setVisibility(EditText.GONE);
             sireTV.setVisibility(TextView.GONE);
             sireET.setVisibility(EditText.GONE);
             deformityTV.setVisibility(TextView.GONE);
             deformityET.setVisibility(EditText.GONE);
             countryOfOriginACTV.setVisibility(EditText.VISIBLE);
             countryOfOriginTV.setVisibility(TextView.VISIBLE);
 
             serviceTypeTV.setVisibility(TextView.VISIBLE);
             serviceTypeS.setVisibility(Spinner.VISIBLE);
 
             previousButton.setVisibility(Button.INVISIBLE);
 
             embryoNumberTV.setVisibility(TextView.VISIBLE);
             embryoNumberET.setVisibility(EditText.VISIBLE);
             vetUsedTV.setVisibility(TextView.VISIBLE);
             vetUsedET.setVisibility(EditText.VISIBLE);
             serviceTypeModeHandler(serviceTypeS.getSelectedItemPosition());
         }
         else if(mode==MODE_SIRE)
         {
             sexTV.setVisibility(TextView.GONE);
             sexS.setVisibility(Spinner.GONE);
             damTV.setVisibility(TextView.GONE);
             damET.setVisibility(EditText.GONE);
             sireTV.setVisibility(TextView.GONE);
             sireET.setVisibility(EditText.GONE);
             deformityTV.setVisibility(TextView.GONE);
             deformityET.setVisibility(EditText.GONE);
             countryOfOriginACTV.setVisibility(EditText.VISIBLE);
             countryOfOriginTV.setVisibility(TextView.VISIBLE);
 
             serviceTypeTV.setVisibility(TextView.VISIBLE);
             serviceTypeS.setVisibility(Spinner.VISIBLE);
 
             previousButton.setVisibility(Button.INVISIBLE);
 
             strawNumberTV.setVisibility(TextView.VISIBLE);
             strawNumberET.setVisibility(EditText.VISIBLE);
             vetUsedTV.setVisibility(TextView.VISIBLE);
             vetUsedET.setVisibility(EditText.VISIBLE);
             serviceTypeModeHandler(serviceTypeS.getSelectedItemPosition());
         }
     }
 
     private void serviceTypeModeHandler(int serviceTypeIndex)
     {
        if(mode==MODE_SIRE)
        {
            if(serviceTypeIndex==0)//bull
            {
                strawNumberTV.setVisibility(TextView.GONE);
                strawNumberET.setVisibility(EditText.GONE);
                vetUsedTV.setVisibility(TextView.GONE);
                vetUsedET.setVisibility(EditText.GONE);
                nameTV.setVisibility(TextView.VISIBLE);
                nameET.setVisibility(EditText.VISIBLE);
                earTagNumberTV.setVisibility(TextView.VISIBLE);
                earTagNumberET.setVisibility(EditText.VISIBLE);
                dateOfBirthTV.setVisibility(TextView.VISIBLE);
                dateOfBirthET.setVisibility(EditText.VISIBLE);
                ageTV.setVisibility(TextView.VISIBLE);
                ageET.setVisibility(EditText.VISIBLE);
                ageS.setVisibility(Spinner.VISIBLE);
                breedTV.setVisibility(TextView.VISIBLE);
                breedET.setVisibility(EditText.VISIBLE);
                countryOfOriginTV.setVisibility(TextView.VISIBLE);
                countryOfOriginACTV.setVisibility(AutoCompleteTextView.VISIBLE);
 
            }
            else if(serviceTypeIndex==1)//artificial insemination
            {
                strawNumberTV.setVisibility(TextView.VISIBLE);
                strawNumberET.setVisibility(EditText.VISIBLE);
                vetUsedTV.setVisibility(TextView.VISIBLE);
                vetUsedET.setVisibility(EditText.VISIBLE);
                nameTV.setVisibility(TextView.GONE);
                nameET.setVisibility(EditText.GONE);
                earTagNumberTV.setVisibility(TextView.GONE);
                earTagNumberET.setVisibility(EditText.GONE);
                dateOfBirthTV.setVisibility(TextView.GONE);
                dateOfBirthET.setVisibility(EditText.GONE);
                ageTV.setVisibility(TextView.GONE);
                ageET.setVisibility(EditText.GONE);
                ageS.setVisibility(Spinner.GONE);
                breedTV.setVisibility(TextView.GONE);
                breedET.setVisibility(EditText.GONE);
                countryOfOriginTV.setVisibility(TextView.GONE);
                countryOfOriginACTV.setVisibility(AutoCompleteTextView.GONE);
            }
        }
        else if(mode==MODE_DAM)
        {
            if(serviceTypeIndex==0)//cow
            {
                embryoNumberTV.setVisibility(TextView.GONE);
                embryoNumberET.setVisibility(EditText.GONE);
                vetUsedTV.setVisibility(TextView.GONE);
                vetUsedET.setVisibility(EditText.GONE);
                nameTV.setVisibility(TextView.VISIBLE);
                nameET.setVisibility(EditText.VISIBLE);
                earTagNumberTV.setVisibility(TextView.VISIBLE);
                earTagNumberET.setVisibility(EditText.VISIBLE);
                dateOfBirthTV.setVisibility(TextView.VISIBLE);
                dateOfBirthET.setVisibility(EditText.VISIBLE);
                ageS.setVisibility(Spinner.VISIBLE);
                ageTV.setVisibility(TextView.VISIBLE);
                ageET.setVisibility(EditText.VISIBLE);
                breedTV.setVisibility(TextView.VISIBLE);
                breedET.setVisibility(EditText.VISIBLE);
                countryOfOriginTV.setVisibility(TextView.VISIBLE);
                countryOfOriginACTV.setVisibility(AutoCompleteTextView.VISIBLE);
            }
            else if(serviceTypeIndex==1)//embryo transfer
            {
                embryoNumberTV.setVisibility(TextView.VISIBLE);
                embryoNumberET.setVisibility(EditText.VISIBLE);
                vetUsedTV.setVisibility(TextView.VISIBLE);
                vetUsedET.setVisibility(EditText.VISIBLE);
                nameTV.setVisibility(TextView.GONE);
                nameET.setVisibility(EditText.GONE);
                earTagNumberTV.setVisibility(TextView.GONE);
                earTagNumberET.setVisibility(EditText.GONE);
                dateOfBirthTV.setVisibility(TextView.GONE);
                dateOfBirthET.setVisibility(EditText.GONE);
                ageTV.setVisibility(TextView.GONE);
                ageET.setVisibility(EditText.GONE);
                ageS.setVisibility(Spinner.GONE);
                breedTV.setVisibility(TextView.GONE);
                breedET.setVisibility(EditText.GONE);
                countryOfOriginTV.setVisibility(TextView.GONE);
                countryOfOriginACTV.setVisibility(AutoCompleteTextView.GONE);
            }
        }
     }
 
     private void initTextInViews(String localeCode)
     {
         if(localeCode.equals("en"))
         {
             if(mode==MODE_COW)
             {
                 String title=getResources().getString(R.string.cow_registration_en)+" "+String.valueOf(index+1);
                 setTitle(title);
             }
             else if(mode==MODE_SIRE)
             {
                 setTitle(R.string.sire_registration_en);
             }
             else if(mode==MODE_DAM)
             {
                 setTitle(R.string.dam_registration_en);
             }
             strawNumberTV.setText(R.string.straw_number_en);
             embryoNumberTV.setText(R.string.embryo_number_en);
             vetUsedTV.setText(R.string.vet_used_en);
             nameTV.setText(R.string.name_en);
             earTagNumberTV.setText(R.string.ear_tag_number_en);
             ageTV.setText(R.string.age_en);
             ArrayAdapter<CharSequence> arrayAdapter2=ArrayAdapter.createFromResource(this, R.array.age_type_array_en, android.R.layout.simple_spinner_item);
             arrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             ageS.setAdapter(arrayAdapter2);
             dateOfBirthTV.setText(R.string.date_of_birth_en);
             breedTV.setText(R.string.breed_en);
             sexTV.setText(R.string.sex_en);
             ArrayAdapter<CharSequence> arrayAdapter=ArrayAdapter.createFromResource(this, R.array.sex_array_en, android.R.layout.simple_spinner_item);
             arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             sexS.setAdapter(arrayAdapter);
             deformityTV.setText(R.string.deformity_en);
             sireTV.setText(R.string.sire_en);
             damTV.setText(R.string.dam_en);
             serviceTypeTV.setText(R.string.service_type_used_en);
             ArrayAdapter<CharSequence> serviceTypesAdapter=null;
             if(mode==MODE_SIRE)
             {
                 serviceTypesAdapter=ArrayAdapter.createFromResource(this,R.array.service_types_sire_array_en,android.R.layout.simple_spinner_item);
                 serviceTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                 serviceTypeS.setAdapter(serviceTypesAdapter);
             }
             else if(mode==MODE_DAM)
             {
                 serviceTypesAdapter=ArrayAdapter.createFromResource(this,R.array.service_types_dam_array_en,android.R.layout.simple_spinner_item);
                 serviceTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                 serviceTypeS.setAdapter(serviceTypesAdapter);
             }
             countryOfOriginTV.setText(R.string.country_of_origin_en);
             previousButton.setText(R.string.previous_en);
             if(mode==MODE_SIRE||mode==MODE_DAM)
             {
                 nextButton.setText(R.string.okay_en);
             }
             else
             {
                 if(index==numberOfCows-1)//last cow
                 {
                     nextButton.setText(R.string.finish_en);
                 }
                 else
                 {
                     nextButton.setText(R.string.next_en);
                 }
             }
             breedDialog.setTitle(R.string.breed_en);
             breeds=getResources().getStringArray(R.array.breeds_array_en);
             ArrayAdapter<String> breedArrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,breeds);
             breedLV.setAdapter(breedArrayAdapter);
             dialogBreedOkayB.setText(R.string.okay_en);
             deformityDialog.setTitle(R.string.deformity_en);
             deformities=getResources().getStringArray(R.array.deformities_array_en);
             ArrayAdapter<String> deformityArrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,deformities);
             deformityLV.setAdapter(deformityArrayAdapter);
             specifyET.setHint(R.string.specify_en);
             dialogDeformityOkayB.setText(R.string.okay_en);
             dialogDeformityOkayB.setOnClickListener(this);
             maxSelectedBreedsWarning=this.getResources().getString(R.string.maximum_of_four_breeds_en);
         }
     }
 
     @Override
     public void onClick(View view)
     {
         if(view==previousButton)
         {
             cacheThisCow();
             Intent intent=new Intent(CowRegistrationActivity.this, CowRegistrationActivity.class);
             intent.putExtra(KEY_MODE,MODE_COW);
             intent.putExtra(KEY_INDEX,index-1);
             intent.putExtra(KEY_NUMBER_OF_COWS,numberOfCows);
             Bundle bundle=new Bundle();
             bundle.putParcelable(Farmer.PARCELABLE_KEY,farmer);
             intent.putExtras(bundle);
             startActivity(intent);
         }
         else if(view==nextButton)
         {
             cacheThisCow();
             Bundle bundle=new Bundle();
             bundle.putParcelable(Farmer.PARCELABLE_KEY,farmer);
             if(mode==MODE_COW)
             {
                 if(index==numberOfCows-1)//last cow
                 {
                     Log.d(TAG, farmer.getJsonObject().toString());
                     //TODO: send to server
                     Intent intent=new Intent(CowRegistrationActivity.this, LandingActivity.class);
                     intent.putExtras(bundle);
                     startActivity(intent);
                 }
                 else
                 {
                     Intent intent=new Intent(CowRegistrationActivity.this, CowRegistrationActivity.class);
                     intent.putExtra(KEY_MODE,MODE_COW);
                     intent.putExtra(KEY_INDEX,index+1);
                     intent.putExtra(KEY_NUMBER_OF_COWS,numberOfCows);
                     intent.putExtras(bundle);
                     startActivity(intent);
                 }
             }
             else//go back to cow
             {
                 Intent intent=new Intent(CowRegistrationActivity.this, CowRegistrationActivity.class);
                 intent.putExtra(KEY_MODE,MODE_COW);
                 intent.putExtra(KEY_INDEX,index);
                 intent.putExtra(KEY_NUMBER_OF_COWS, numberOfCows);
                 intent.putExtras(bundle);
                 startActivity(intent);
             }
         }
         else if(view==dialogBreedOkayB)
         {
             String selectedBreeds="";
             SparseBooleanArray checkedBreeds=breedLV.getCheckedItemPositions();
             for (int i=0; i<breedLV.getCount();i++)
             {
                 if(checkedBreeds.get(i))
                 {
                     if(!selectedBreeds.equals(""))
                     {
                         selectedBreeds=selectedBreeds+", "+breeds[i];
                     }
                     else
                     {
                         selectedBreeds=breeds[i];
                     }
                 }
             }
             breedET.setText(selectedBreeds);
             breedDialog.dismiss();
 
         }
         else if(view==dialogDeformityOkayB)
         {
             String selectedDeformities="";
             SparseBooleanArray checkedDeformities=deformityLV.getCheckedItemPositions();
             for (int i=0; i<deformityLV.getCount();i++)
             {
                 if(checkedDeformities.get(i))
                 {
                     if(!selectedDeformities.equals(""))
                     {
                         selectedDeformities=selectedDeformities+", "+deformities[i];
                     }
                     else
                     {
                         selectedDeformities=deformities[i];
                     }
                 }
             }
             deformityET.setText(selectedDeformities);
             deformityDialog.dismiss();
             deformityOSpecifyText=specifyET.getText().toString();
         }
         else if(view==dateOfBirthET)
         {
             dateOfBirthETClicked();
         }
         else if(view==breedET)
         {
             breedETClicked();
         }
         else if(view==deformityET)
         {
             deformityETClicked();
         }
         else if(view==sireET)
         {
             sireETClicked();
         }
         else if(view==damET)
         {
             damETClicked();
         }
     }
 
     private void dateOfBirthETClicked()
     {
         Date date=null;
         if(dateOfBirthET.getText().toString().length()>0)
         {
             try
             {
                 date=new SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(dateOfBirthET.getText().toString());
             }
             catch (ParseException e)
             {
                 e.printStackTrace();
             }
         }
         if(date==null)
         {
             date=new Date();
         }
         Calendar calendar=new GregorianCalendar();
         calendar.setTime(date);
         datePickerDialog=new DatePickerDialog(this,this,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
         //datePickerDialog=createDialogWithoutDateField(this,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
         datePickerDialog.show();
     }
 
     private void breedETClicked()
     {
         //uncheck everything in listview
         for (int i=0; i<breedLV.getCount();i++)
         {
             breedLV.setItemChecked(i,false);
         }
         String breedETString=breedET.getText().toString();
         if(!breedETString.equals(null)||!breedETString.equals(""))
         {
             String[] selectedBreeds=breedETString.split(", ");
             //for all of the breeds check if breed is in selected breeds
             for(int i=0; i<breeds.length;i++)
             {
                 String currentBreed=breeds[i];
                 for(int j=0; j<selectedBreeds.length;j++)
                 {
 
                     if(currentBreed.equals(selectedBreeds[j]))
                     {
                         breedLV.setItemChecked(i,true);
                         break;
                     }
                 }
             }
         }
         breedDialog.show();
     }
 
     private void deformityETClicked()
     {
         //uncheck everything in listview
         for (int i=0;i<deformityLV.getCount();i++)
         {
             deformityLV.setItemChecked(i,false);
         }
         String deformityETString=deformityET.getText().toString();
         if(!deformityETString.equals(null)||!deformityETString.equals(""))
         {
             String[] selectedDeformities=deformityETString.split(", ");
             for (int i=0;i<deformities.length;i++)
             {
                 String currentDeformity=deformities[i];
                 for (int j=0;j<selectedDeformities.length;j++)
                 {
                     if(currentDeformity.equals(selectedDeformities[j]))
                     {
                         deformityLV.setItemChecked(i,true);
                         if (i==deformities.length-1)
                         {
                             specifyET.setVisibility(EditText.VISIBLE);
                             specifyET.setText(deformityOSpecifyText);
                         }
                         break;
                     }
                 }
             }
         }
         deformityDialog.show();
     }
 
     private void sireETClicked()
     {
         cacheThisCow();
         Intent intent=new Intent(CowRegistrationActivity.this, CowRegistrationActivity.class);
         intent.putExtra(KEY_MODE,MODE_SIRE);
         intent.putExtra(KEY_INDEX,index);
         intent.putExtra(KEY_NUMBER_OF_COWS,numberOfCows);
         Bundle bundle=new Bundle();
         bundle.putParcelable(Farmer.PARCELABLE_KEY,farmer);
         intent.putExtras(bundle);
         startActivity(intent);
     }
 
     private void damETClicked()
     {
         cacheThisCow();
         Intent intent=new Intent(CowRegistrationActivity.this, CowRegistrationActivity.class);
         intent.putExtra(KEY_MODE,MODE_DAM);
         intent.putExtra(KEY_INDEX,index);
         intent.putExtra(KEY_NUMBER_OF_COWS,numberOfCows);
         Bundle bundle=new Bundle();
         bundle.putParcelable(Farmer.PARCELABLE_KEY,farmer);
         intent.putExtras(bundle);
         startActivity(intent);
     }
 
 
     private String getDateFromAge()
     {
         if(ageET.getText().toString().length()>0)
         {
             int age=Integer.valueOf(ageET.getText().toString());
             long milliseconds=0;
             if(ageS.getSelectedItemPosition()==0)//days
             {
                 milliseconds=age*86400000L;
             }
             else if(ageS.getSelectedItemPosition()==1)//weeks
             {
                 milliseconds=age*604800000L;
             }
             else if(ageS.getSelectedItemPosition()==2)//years
             {
                 milliseconds=age*31557600000L;
             }
             long nowMilliseconds=new Date().getTime();
             long pastDateMilliseconds=nowMilliseconds-milliseconds;
             Date pastDate=new Date(pastDateMilliseconds);
             SimpleDateFormat simpleDateFormat=new SimpleDateFormat(dateFormat);
             return simpleDateFormat.format(pastDate);
         }
         return null;
     }
 
     private void setAgeFromDate(String dateString)
     {
         monitorAgeChange=false;
         SimpleDateFormat simpleDateFormat=new SimpleDateFormat(dateFormat);
         Date enteredDate=new Date();
         try
         {
             enteredDate=simpleDateFormat.parse(dateString);
         }
         catch (ParseException e)
         {
             e.printStackTrace();
         }
         Date today=new Date();
         long millisecondDifference=today.getTime()-enteredDate.getTime();
         if(millisecondDifference>0&&millisecondDifference<604800000L)//less than one week
         {
             int days=(int)(millisecondDifference/86400000L);
             ageET.setText(String.valueOf(days));
             ageS.setSelection(0);
         }
         else if(millisecondDifference>=604800000L&&millisecondDifference<31557600000L)//less than a year
         {
             int weeks=(int)(millisecondDifference/604800000L);
             ageET.setText(String.valueOf(weeks));
             ageS.setSelection(1);
         }
         else if(millisecondDifference>=31557600000L)//a year or greater
         {
             int years=(int)(millisecondDifference/31557600000L);
             ageET.setText(String.valueOf(years));
             ageS.setSelection(2);
         }
         monitorAgeChange=true;
     }
 
     @Override
     public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 
     }
 
     @Override
     public void onTextChanged(CharSequence s, int start, int before, int count) {
 
     }
 
     @Override
     public void afterTextChanged(Editable editable)
     {
         if(monitorAgeChange)
         {
             dateOfBirthET.setText(getDateFromAge());
         }
     }
     private DatePickerDialog createDialogWithoutDateField(DatePickerDialog.OnDateSetListener dateSetListener, int cyear, int cmonth, int cday){
 
     DatePickerDialog dpd = new DatePickerDialog(this, dateSetListener,cyear,cmonth, cday);
     try
     {
         Field[] datePickerDialogFields = dpd.getClass().getDeclaredFields();
         for (Field datePickerDialogField : datePickerDialogFields)
         {
             if (datePickerDialogField.getName().equals("mDatePicker"))
             {
                 datePickerDialogField.setAccessible(true);
                 DatePicker datePicker = (DatePicker) datePickerDialogField.get(dpd);
                 Field datePickerFields[] = datePickerDialogField.getType().getDeclaredFields();
                 for (Field datePickerField : datePickerFields)
                 {
                     if ("mDayPicker".equals(datePickerField.getName()))
                     {
                         datePickerField.setAccessible(true);
                         Object dayPicker = new Object();
                         dayPicker = datePickerField.get(datePicker);
                         ((View) dayPicker).setVisibility(View.GONE);
                     }
                 }
             }
 
         }
     }catch(Exception ex){
     }
     return dpd;
 
 }
 
     @Override
     public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
     {
         if(parent==ageS)
         {
             if(monitorAgeChange)
             {
                 dateOfBirthET.setText(getDateFromAge());
             }
 
         }
         else if(parent==serviceTypeS)
         {
             serviceTypeModeHandler(serviceTypeS.getSelectedItemPosition());
         }
 
     }
 
     @Override
     public void onNothingSelected(AdapterView<?> parent)
     {
 
     }
 
     @Override
     public void onFocusChange(View view, boolean hasFocus)
     {
         if(view==dateOfBirthET&&hasFocus)
         {
             dateOfBirthETClicked();
         }
         else if(view==breedET&&hasFocus)
         {
             breedETClicked();
         }
         else if(view==deformityET&&hasFocus)
         {
             deformityETClicked();
         }
         else if(view==sireET)
         {
             sireETClicked();
         }
         else if(view==damET)
         {
             damETClicked();
         }
     }
 
     @Override
     public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
     {
         String dateString=String.valueOf(dayOfMonth)+"/"+String.valueOf(monthOfYear+1)+"/"+String.valueOf(year);//TODO: this might be a bug
         dateOfBirthET.setText(dateString);
         setAgeFromDate(dateString);
     }
 
 
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id)
     {
         if(parent==breedLV)
         {
             if(breedLV.isItemChecked(position))
             {
                 selectedBreeds++;
             }
             else
             {
                 selectedBreeds--;
             }
             if(selectedBreeds>4)
             {
                 breedLV.setItemChecked(position,false);
                 selectedBreeds--;
                 Toast.makeText(this,maxSelectedBreedsWarning,Toast.LENGTH_LONG).show();
             }
         }
         else if(parent==deformityLV)
         {
             if(position==deformities.length-1)//last deformity. should be other
             {
                 if(deformityLV.isItemChecked(position))
                 {
                     specifyET.setVisibility(EditText.VISIBLE);
                 }
                 else
                 {
                     specifyET.setVisibility(EditText.GONE);
                     specifyET.setText("");
                 }
             }
         }
 
 
     }
 
     private void cacheThisCow()
     {
         if(thisCow==null)
         {
             if(mode==MODE_COW)
             {
                 thisCow=new Cow(true);
             }
             else if(mode==MODE_SIRE)
             {
                 thisCow=new Sire();
             }
             else if(mode==MODE_DAM)
             {
                 thisCow=new Dam();
             }
         }
         thisCow.setName(nameET.getText().toString());
         thisCow.setEarTagNumber(earTagNumberET.getText().toString());
         thisCow.setDateOfBirth(dateOfBirthET.getText().toString());
         thisCow.setAge((ageET.getText().toString()==null||ageET.getText().toString().length()==0) ? -1:Integer.parseInt(ageET.getText().toString()));
         if(ageS.getSelectedItemPosition()==0)
         {
             thisCow.setAgeType(Cow.AGE_TYPE_DAY);
         }
         else if(ageS.getSelectedItemPosition()==1)
         {
             thisCow.setAgeType(Cow.AGE_TYPE_WEEK);
         }
         else if(ageS.getSelectedItemPosition()==2)
         {
             thisCow.setAgeType(Cow.AGE_TYPE_YEAR);
         }
         thisCow.setBreeds(breedET.getText().toString().split(", "));
         if(sexS.getSelectedItemPosition()==0)
         {
             thisCow.setSex(Cow.SEX_FEMALE);
         }
         else
         {
             thisCow.setSex(Cow.SEX_MALE);
         }
         thisCow.setDeformities(deformityET.getText().toString().split(", "));
         thisCow.setCountryOfOrigin(countryOfOriginACTV.getText().toString());
         if(mode==MODE_DAM)
         {
             if(serviceTypeS.getSelectedItemPosition()==0)
             {
                 ((Dam)thisCow).setServiceType(Dam.SERVICE_TYPE_COW);
             }
             else
             {
                 ((Dam)thisCow).setServiceType(Dam.SERVICE_TYPE_ET);
             }
             ((Dam)thisCow).setEmbryoNumber(embryoNumberET.getText().toString());
             ((Dam)thisCow).setVetUsed(vetUsedET.getText().toString());
             farmer.getCow(index).setDam((Dam)thisCow);
         }
         else if(mode==MODE_SIRE)
         {
             if(serviceTypeS.getSelectedItemPosition()==0)
             {
                 ((Sire)thisCow).setServiceType(Sire.SERVICE_TYPE_BULL);
             }
             else
             {
                 ((Sire)thisCow).setServiceType(Sire.SERVICE_TYPE_AI);
             }
             ((Sire)thisCow).setStrawNumber(strawNumberET.getText().toString());
             ((Sire)thisCow).setVetUsed(vetUsedET.getText().toString());
             farmer.getCow(index).setSire((Sire)thisCow);
         }
         else if(mode==MODE_COW)
         {
             farmer.setCow(thisCow,index);
         }
     }
 }
