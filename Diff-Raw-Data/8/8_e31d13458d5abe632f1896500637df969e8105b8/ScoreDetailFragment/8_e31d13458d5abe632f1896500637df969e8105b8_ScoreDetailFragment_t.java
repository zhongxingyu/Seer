 package com.livejournal.karino2.guitarscorevisualizer;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.util.DisplayMetrics;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewTreeObserver;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.PopupWindow;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * A fragment representing a single Score detail screen.
  * This fragment is either contained in a {@link ScoreListActivity}
  * in two-pane mode (on tablets) or a {@link ScoreDetailActivity}
  * on handsets.
  */
 public class ScoreDetailFragment extends Fragment {
     /**
      * The fragment argument representing the item ID that this fragment
      * represents.
      */
     public static final String ARG_ITEM_ID = "item_id";
     public static final int ACTIVITY_ID_EDIT = 1;
 
     /**
      * The dummy content this fragment is presenting.
      */
     private Score mItem;
 
     /**
      * Mandatory empty constructor for the fragment manager to instantiate the
      * fragment (e.g. upon screen orientation changes).
      */
     public ScoreDetailFragment() {
         setHasOptionsMenu(true);
     }
 
     int CHORD_IMAGE_WIDTH = 64;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         if (getArguments().containsKey(ARG_ITEM_ID)) {
             long itemID = getArguments().getLong(ARG_ITEM_ID);
             if(itemID == -1) {
                 mItem = null;
             } else {
                 mItem = getDatabase().getScoreById(itemID);
             }
         }
 
         DisplayMetrics metrics = new DisplayMetrics();
         getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
 
         float dpi = (metrics.xdpi + metrics.ydpi) / 2;
         if (dpi < 10) dpi = 10;
 
         float imageWidthCm = 0.9f;
         CHORD_IMAGE_WIDTH =  (int)(dpi * imageWidthCm / 2.54f) + 1;
     }
 
     private Database getDatabase() {
         return Database.getInstance(getActivity());
     }
 
     public static void highQualityStretch( Bitmap src, Bitmap dest )
     {
         dest.eraseColor( 0 );
 
         Canvas canvas = new Canvas( dest );
         Paint paint = new Paint();
         paint.setFilterBitmap( true );
 
         Matrix m = new Matrix();
         m.postScale( (float)dest.getWidth()/src.getWidth(), (float)dest.getHeight()/src.getHeight() );
         canvas.drawBitmap( src, m, paint );
     }
 
     private Bitmap floatResource( int id, int w, int h )
     {
         Bitmap dest = Bitmap.createBitmap( w, h, Bitmap.Config.ARGB_8888 );
         Bitmap bmp = BitmapFactory.decodeResource(getActivity().getResources(), id);
         highQualityStretch( bmp, dest );
         return dest;
     }
 
 
     private View rootView;
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         rootView = inflater.inflate(R.layout.fragment_score_detail, container, false);
 
         // Show the dummy content as text in a TextView.
         if (mItem != null) {
             setTitleTextFromItem();
 
             rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                 @Override
                 public void onGlobalLayout() {
                     rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
 
                     tableLayoutReady = true;
                     formatTableIfReady();
                 }
             });
 
         }
 
 
         if(!chordsReady()) {
             startParseChord();
         }
 
         ((TextView)rootView.findViewById(R.id.score_detail_score)).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 dismissPopupIfShown();
             }
         });
         ((TextView)rootView.findViewById(R.id.score_detail_score)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
             @Override
             public void onFocusChange(View view, boolean hasFocus) {
                 dismissPopupIfShown();
             }
         });
 
 
         return rootView;
     }
 
     private void setTitleTextFromItem() {
         ((TextView) rootView.findViewById(R.id.score_detail_title)).setText(mItem.getTitle());
         ((TextView) rootView.findViewById(R.id.score_detail_score)).setText(mItem.getEncodedTexts());
     }
 
     private boolean chordsReady() {
         return mItem == null || ( mItem.getChords() != null);
     }
 
     Handler handler = new Handler();
     ScoreParser parser;
     ScoreParser getParser() {
         if(parser == null)
            parser = new ScoreParser();
         return parser;
     }
     private void startParseChord() {
         new Thread(){
             public void run() {
                 ScoreParser parser = getParser();
                 List<Chord> chords = parser.parseAll(mItem.getTexts());
                 final List<Integer> chordInts = toInts(chords);
                 handler.post(new Runnable(){
                     @Override
                     public void run() {
                         onChordsComming(chordInts);
                     }
                 });
             }
         }.start();
     }
 
     private List<Integer> toInts(List<Chord> chords) {
         ArrayList<Integer> res = new ArrayList<Integer>();
         for(Chord chord: chords) {
             res.add(chord.encodeToInt());
         }
         return res;
     }
 
     private void onChordsComming(List<Integer> chordInts) {
         mItem.setChords(chordInts);
         getDatabase().updateScore(mItem);
         formatTableIfReady();
     }
 
     public void reloadItem() {
         if(mItem != null) {
             mItem = getDatabase().getScoreById(mItem.getId());
             if(rootView != null) {
                 setTitleTextFromItem();
                 startParseChord();
             }
         }
     }
 
     public static class UniqueCodeWrapper implements Iterable<Chord> {
         List<Chord> chords;
         public UniqueCodeWrapper(List<Chord> chrds) {
             chords = chrds;
         }
 
         @Override
         public Iterator<Chord> iterator() {
             return new UniqueChordIterator(chords);
         }
     }
 
     public static class UniqueChordIterator implements Iterator<Chord> {
         List<Chord> chords;
         HashMap<Chord, Boolean> map;
         int pos;
         public UniqueChordIterator(List<Chord> chrds) {
             chords = chrds;
             map = new HashMap<Chord, Boolean>();
             pos =0;
         }
 
         @Override
         public boolean hasNext() {
             if(pos == chords.size())
                 return false;
             return true;
         }
 
         private boolean alreadyShown(Chord chord) {
             if(map.containsKey(chord))
                 return true;
             return false;
         }
 
         @Override
         public Chord next() {
             Chord chord = chords.get(pos);
             map.put(chord, true);
             for(pos = pos+1; pos < chords.size(); pos++) {
                 Chord tmp = chords.get(pos);
                 if(!alreadyShown(tmp))
                     break;
             }
             return chord;
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException();
         }
     }
 
     HashMap<Chord, List<Chord>> alternateChordMap;
     HashMap<Chord, Integer> chordResourceMap;
     int lookupResourceId(Chord chord) {
         if(chordResourceMap == null) {
             chordResourceMap = new HashMap<Chord, Integer>();
             chordResourceMap.put(new Chord(Chord.BASE_A, Chord.MODIFIER_MAJOR), R.drawable.chords_a_0);
             chordResourceMap.put(new Chord(Chord.BASE_A, Chord.MODIFIER_MAJOR, Chord.ALTERNATE_HICODE), R.drawable.chords_a_1);
             chordResourceMap.put(new Chord(Chord.BASE_A, Chord.MODIFIER_SEVENS), R.drawable.chords_a7_0);
             chordResourceMap.put(new Chord(Chord.BASE_A, Chord.MODIFIER_SIX), R.drawable.chords_a6_0);
             chordResourceMap.put(new Chord(Chord.BASE_A, Chord.MODIFIER_SUSFOUR), R.drawable.chords_asus4_0);
             chordResourceMap.put(new Chord(Chord.BASE_A, Chord.MODIFIER_ADDNINE), R.drawable.chords_aadd9_0);
             chordResourceMap.put(new Chord(Chord.BASE_A, Chord.MODIFIER_MINOR), R.drawable.chords_am_0);
             chordResourceMap.put(new Chord(Chord.BASE_A, Chord.MODIFIER_MINORSEVENS), R.drawable.chords_am7_0);
             chordResourceMap.put(new Chord(Chord.BASE_A, Chord.MODIFIER_MAJORSEVENS), R.drawable.chords_ama7_0);
             chordResourceMap.put(new Chord(Chord.BASE_A, Chord.MODIFIER_MINOR_MAJORSEVENS), R.drawable.chords_amma7_0);
             chordResourceMap.put(new Chord(Chord.BASE_A, Chord.MODIFIER_DIM), R.drawable.chords_adim_0);
             chordResourceMap.put(new Chord(Chord.BASE_A_PLUS_F, Chord.MODIFIER_MAJOR), R.drawable.chords_a_plus_f_0);
             chordResourceMap.put(new Chord(Chord.BASE_Am_ON_C, Chord.MODIFIER_MAJOR), R.drawable.chords_am_on_c_0);
             chordResourceMap.put(new Chord(Chord.BASE_A_SHARP, Chord.MODIFIER_MAJOR), R.drawable.chords_ash_0);
            chordResourceMap.put(new Chord(Chord.BASE_A_SHARP, Chord.MODIFIER_MAJOR, Chord.ALTERNATE_HICODE), R.drawable.chords_ash_1);
             chordResourceMap.put(new Chord(Chord.BASE_A_SHARP, Chord.MODIFIER_MINOR), R.drawable.chords_ashm_0);
             chordResourceMap.put(new Chord(Chord.BASE_A_SHARP, Chord.MODIFIER_MINORSEVENS), R.drawable.chords_ashm7_0);
             chordResourceMap.put(new Chord(Chord.BASE_B, Chord.MODIFIER_MAJOR), R.drawable.chords_b_0);
             chordResourceMap.put(new Chord(Chord.BASE_B, Chord.MODIFIER_MINOR), R.drawable.chords_bm_0);
             chordResourceMap.put(new Chord(Chord.BASE_B, Chord.MODIFIER_MINORSEVENS), R.drawable.chords_bm7_0);
             chordResourceMap.put(new Chord(Chord.BASE_B, Chord.MODIFIER_MAJORSEVENS), R.drawable.chords_bma7_0);
             chordResourceMap.put(new Chord(Chord.BASE_B, Chord.MODIFIER_SUSFOUR), R.drawable.chords_bsus4_0);
             chordResourceMap.put(new Chord(Chord.BASE_B, Chord.MODIFIER_SEVENS), R.drawable.chords_b7_0);
             chordResourceMap.put(new Chord(Chord.BASE_B, Chord.MODIFIER_MINOR_MAJORSEVENS), R.drawable.chords_bmma7_0);
             chordResourceMap.put(new Chord(Chord.BASE_C, Chord.MODIFIER_MAJOR), R.drawable.chords_c_0);
             chordResourceMap.put(new Chord(Chord.BASE_C, Chord.MODIFIER_MAJOR, Chord.ALTERNATE_HICODE), R.drawable.chords_c_1);
             chordResourceMap.put(new Chord(Chord.BASE_C, Chord.MODIFIER_MINOR), R.drawable.chords_cm_0);
             chordResourceMap.put(new Chord(Chord.BASE_C, Chord.MODIFIER_SEVENS), R.drawable.chords_c7_0);
             chordResourceMap.put(new Chord(Chord.BASE_C, Chord.MODIFIER_MINORSEVENS), R.drawable.chords_cm7_0);
             chordResourceMap.put(new Chord(Chord.BASE_C, Chord.MODIFIER_MAJORSEVENS), R.drawable.chords_cma7_0);
             chordResourceMap.put(new Chord(Chord.BASE_C_SHARP, Chord.MODIFIER_MAJOR), R.drawable.chords_csh_0);
             chordResourceMap.put(new Chord(Chord.BASE_C_SHARP, Chord.MODIFIER_MINOR), R.drawable.chords_cshm_0);
             chordResourceMap.put(new Chord(Chord.BASE_C_SHARP, Chord.MODIFIER_MINORSEVENS), R.drawable.chords_cshm7_0);
             chordResourceMap.put(new Chord(Chord.BASE_C_SHARP, Chord.MODIFIER_MINOR_MAJORSEVENS), R.drawable.chords_cshmma7_0);
             chordResourceMap.put(new Chord(Chord.BASE_C_SHARP, Chord.MODIFIER_SEVENS), R.drawable.chords_csh7_0);
             chordResourceMap.put(new Chord(Chord.BASE_C_SHARP, Chord.MODIFIER_MINORSEVEN_FLATFIVE), R.drawable.chords_cshm75_0);
             chordResourceMap.put(new Chord(Chord.BASE_C_SHARP, Chord.MODIFIER_SUSFOUR), R.drawable.chords_cshsus4_0);
             chordResourceMap.put(new Chord(Chord.BASE_C_ON_E, Chord.MODIFIER_MAJOR), R.drawable.chords_c_on_e_0);
             chordResourceMap.put(new Chord(Chord.BASE_Cm_ON_G, Chord.MODIFIER_MAJOR), R.drawable.chords_cm_on_g_0);
             chordResourceMap.put(new Chord(Chord.BASE_D, Chord.MODIFIER_MAJOR), R.drawable.chords_d_0);
             chordResourceMap.put(new Chord(Chord.BASE_D, Chord.MODIFIER_MINORSEVEN_FLATFIVE), R.drawable.chords_dm75_0);
             chordResourceMap.put(new Chord(Chord.BASE_D, Chord.MODIFIER_ADDNINE), R.drawable.chords_dadd9_0);
             chordResourceMap.put(new Chord(Chord.BASE_D, Chord.MODIFIER_MINOR), R.drawable.chords_dm_0);
             chordResourceMap.put(new Chord(Chord.BASE_D, Chord.MODIFIER_SEVENSUSFOUR), R.drawable.chords_d7sus4_0);
             chordResourceMap.put(new Chord(Chord.BASE_D, Chord.MODIFIER_MINORSEVENS), R.drawable.chords_dm7_0);
             chordResourceMap.put(new Chord(Chord.BASE_D, Chord.MODIFIER_MAJORSEVENS), R.drawable.chords_dma7_0);
             chordResourceMap.put(new Chord(Chord.BASE_D, Chord.MODIFIER_SEVENS), R.drawable.chords_d7_0);
             chordResourceMap.put(new Chord(Chord.BASE_D, Chord.MODIFIER_AUG), R.drawable.chords_daug_0);
             chordResourceMap.put(new Chord(Chord.BASE_D, Chord.MODIFIER_SUSFOUR), R.drawable.chords_dsus4_0);
             chordResourceMap.put(new Chord(Chord.BASE_D_SHARP, Chord.MODIFIER_MAJOR), R.drawable.chords_dsh_0);
             chordResourceMap.put(new Chord(Chord.BASE_D_SHARP, Chord.MODIFIER_SUSFOUR), R.drawable.chords_dshsus4_0);
             chordResourceMap.put(new Chord(Chord.BASE_E, Chord.MODIFIER_MAJOR), R.drawable.chords_e_0);
             chordResourceMap.put(new Chord(Chord.BASE_E, Chord.MODIFIER_SEVENS), R.drawable.chords_e7_0);
             chordResourceMap.put(new Chord(Chord.BASE_E, Chord.MODIFIER_SUSFOUR), R.drawable.chords_esus4_0);
             chordResourceMap.put(new Chord(Chord.BASE_E, Chord.MODIFIER_MINOR), R.drawable.chords_em_0);
             chordResourceMap.put(new Chord(Chord.BASE_E, Chord.MODIFIER_MINORSEVENS), R.drawable.chords_em7_0);
             chordResourceMap.put(new Chord(Chord.BASE_E, Chord.MODIFIER_MAJORSEVENS), R.drawable.chords_ema7_0);
             chordResourceMap.put(new Chord(Chord.BASE_E, Chord.MODIFIER_MINOR_MAJORSEVENS), R.drawable.chords_emma7_0);
             chordResourceMap.put(new Chord(Chord.BASE_Em7_ON_A, Chord.MODIFIER_MAJOR), R.drawable.chords_em7_on_a_0);
             chordResourceMap.put(new Chord(Chord.BASE_F, Chord.MODIFIER_MAJOR), R.drawable.chords_f_0);
             chordResourceMap.put(new Chord(Chord.BASE_F, Chord.MODIFIER_MAJOR, Chord.ALTERNATE_HICODE), R.drawable.chords_f_1);
             chordResourceMap.put(new Chord(Chord.BASE_F, Chord.MODIFIER_DIM), R.drawable.chords_fdim_0);
             chordResourceMap.put(new Chord(Chord.BASE_F, Chord.MODIFIER_AUG), R.drawable.chords_faug_0);
             chordResourceMap.put(new Chord(Chord.BASE_F, Chord.MODIFIER_MINORSEVENS), R.drawable.chords_fm7_0);
             chordResourceMap.put(new Chord(Chord.BASE_F, Chord.MODIFIER_MAJORSEVENS), R.drawable.chords_fma7_0);
             chordResourceMap.put(new Chord(Chord.BASE_F_SHARP, Chord.MODIFIER_MAJOR), R.drawable.chords_fsh_0);
             chordResourceMap.put(new Chord(Chord.BASE_F_SHARP, Chord.MODIFIER_SEVENS), R.drawable.chords_fsh7_0);
             chordResourceMap.put(new Chord(Chord.BASE_F_SHARP, Chord.MODIFIER_MINOR), R.drawable.chords_fshm_0);
             chordResourceMap.put(new Chord(Chord.BASE_F_SHARP, Chord.MODIFIER_MINORSEVENS), R.drawable.chords_fshm7_0);
             chordResourceMap.put(new Chord(Chord.BASE_FMASEVEN_ON_C, Chord.MODIFIER_MAJOR), R.drawable.chords_fma7_on_c_0);
             chordResourceMap.put(new Chord(Chord.BASE_FSHARPMINORSEVENS_ON_B, Chord.MODIFIER_MAJOR), R.drawable.chords_fshm7_on_b_0);
             chordResourceMap.put(new Chord(Chord.BASE_G, Chord.MODIFIER_MAJOR), R.drawable.chords_g_0);
             chordResourceMap.put(new Chord(Chord.BASE_G, Chord.MODIFIER_MINOR), R.drawable.chords_gm_0);
             chordResourceMap.put(new Chord(Chord.BASE_G, Chord.MODIFIER_SIX), R.drawable.chords_g6_0);
             chordResourceMap.put(new Chord(Chord.BASE_G, Chord.MODIFIER_SEVENS), R.drawable.chords_g7_0);
             chordResourceMap.put(new Chord(Chord.BASE_G, Chord.MODIFIER_SUSFOUR), R.drawable.chords_gsus4_0);
             chordResourceMap.put(new Chord(Chord.BASE_G, Chord.MODIFIER_MINORSEVENS), R.drawable.chords_gm7_0);
             chordResourceMap.put(new Chord(Chord.BASE_G_SHARP, Chord.MODIFIER_MAJOR), R.drawable.chords_gsh_0);
             chordResourceMap.put(new Chord(Chord.BASE_G_SHARP, Chord.MODIFIER_MINOR), R.drawable.chords_gshm_0);
             chordResourceMap.put(new Chord(Chord.BASE_G_SHARP, Chord.MODIFIER_MINORSEVENS), R.drawable.chords_gshm7_0);
             chordResourceMap.put(new Chord(Chord.BASE_G_SHARP, Chord.MODIFIER_DIM), R.drawable.chords_gshdim_0);
             chordResourceMap.put(new Chord(Chord.BASE_G, Chord.MODIFIER_MAJOR, Chord.ALTERNATE_HICODE), R.drawable.chords_g_1);
             chordResourceMap.put(new Chord(Chord.BASE_G_ON_D, Chord.MODIFIER_MAJOR), R.drawable.chords_g_on_d_0);
             chordResourceMap.put(new Chord(Chord.BASE_G_ON_B, Chord.MODIFIER_MAJOR), R.drawable.chords_g_on_b_0);
             chordResourceMap.put(new Chord(Chord.BASE_A_SHARP_ON_A, Chord.MODIFIER_MAJOR), R.drawable.chords_ash_on_a_0);
             chordResourceMap.put(new Chord(Chord.BASE_A_ON_B, Chord.MODIFIER_MAJOR), R.drawable.chords_a_on_b_0);
 
 
             alternateChordMap = new HashMap<Chord, List<Chord>>();
 
             ArrayList<Chord> aAltList = new ArrayList<Chord>();
             aAltList.add(new Chord(Chord.BASE_A, Chord.MODIFIER_MAJOR));
             aAltList.add(new Chord(Chord.BASE_A, Chord.MODIFIER_MAJOR, Chord.ALTERNATE_HICODE));
             alternateChordMap.put(new Chord(Chord.BASE_A, Chord.MODIFIER_MAJOR), aAltList);
             alternateChordMap.put(new Chord(Chord.BASE_A, Chord.MODIFIER_MAJOR, Chord.ALTERNATE_HICODE), aAltList);
 
 
 
             ArrayList<Chord> gAltList = new ArrayList<Chord>();
             gAltList.add(new Chord(Chord.BASE_G, Chord.MODIFIER_MAJOR));
             gAltList.add(new Chord(Chord.BASE_G, Chord.MODIFIER_MAJOR, Chord.ALTERNATE_HICODE));
             alternateChordMap.put(new Chord(Chord.BASE_G, Chord.MODIFIER_MAJOR), gAltList);
             alternateChordMap.put(new Chord(Chord.BASE_G, Chord.MODIFIER_MAJOR, Chord.ALTERNATE_HICODE), gAltList);
 
             ArrayList<Chord> fAltList = new ArrayList<Chord>();
             fAltList.add(new Chord(Chord.BASE_F, Chord.MODIFIER_MAJOR));
             fAltList.add(new Chord(Chord.BASE_F, Chord.MODIFIER_MAJOR, Chord.ALTERNATE_HICODE));
             alternateChordMap.put(new Chord(Chord.BASE_F, Chord.MODIFIER_MAJOR), fAltList);
             alternateChordMap.put(new Chord(Chord.BASE_F, Chord.MODIFIER_MAJOR, Chord.ALTERNATE_HICODE), fAltList);
 
 
             ArrayList<Chord> cAltList = new ArrayList<Chord>();
             cAltList.add(new Chord(Chord.BASE_C, Chord.MODIFIER_MAJOR));
             cAltList.add(new Chord(Chord.BASE_C, Chord.MODIFIER_MAJOR, Chord.ALTERNATE_HICODE));
             alternateChordMap.put(new Chord(Chord.BASE_C, Chord.MODIFIER_MAJOR), cAltList);
             alternateChordMap.put(new Chord(Chord.BASE_C, Chord.MODIFIER_MAJOR, Chord.ALTERNATE_HICODE), cAltList);
 
 
            ArrayList<Chord> ashAltList = new ArrayList<Chord>();
            ashAltList.add(new Chord(Chord.BASE_A_SHARP, Chord.MODIFIER_MAJOR));
            ashAltList.add(new Chord(Chord.BASE_A_SHARP, Chord.MODIFIER_MAJOR, Chord.ALTERNATE_HICODE));
            alternateChordMap.put(new Chord(Chord.BASE_A_SHARP, Chord.MODIFIER_MAJOR), ashAltList);
            alternateChordMap.put(new Chord(Chord.BASE_A_SHARP, Chord.MODIFIER_MAJOR, Chord.ALTERNATE_HICODE), ashAltList);

 
         }
         if(chordResourceMap.containsKey(chord))
             return chordResourceMap.get(chord);
         return R.drawable.chords_notready;
     }
 
     final int CELL_MARGIN = 20;
 
     boolean tableLayoutReady = false;
     private void formatTableIfReady() {
         if(!tableLayoutReady || !chordsReady())
             return;
         TableLayout tl = (TableLayout)rootView.findViewById(R.id.tableChords);
 
         tl.removeAllViews();
         int tableWidth = tl.getWidth();
 
         TableRow row = new TableRow(getActivity());
         tl.addView(row, new TableLayout.LayoutParams());
         UniqueCodeWrapper wrapper = new UniqueCodeWrapper(toChords(mItem.getChords()));
         int addedWidth = 0;
         for(Chord chord: wrapper) {
             ImageButton image = new ImageButton(getActivity());
             image.setPadding(10, 10, 10, 10);
             image.setTag(chord);
             image.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     showAlternateIfExist(view, (Chord)view.getTag());
                 }
             });
             setChordResource(image, lookupResourceId(chord));
             row.addView(image, new TableRow.LayoutParams(0));
             addedWidth += getButtonSize();
             if(addedWidth +CHORD_IMAGE_WIDTH+CELL_MARGIN > tableWidth) {
                 row = new TableRow(getActivity());
                 tl.addView(row, new TableLayout.LayoutParams());
                 addedWidth = 0;
             }
         }
 
 
     }
 
     Chord chordForDialog;
     PopupWindow popupForAlts;
 
     private void showAlternateIfExist(View parent, Chord selected) {
         if(alternateChordMap.containsKey(selected)) {
             chordForDialog = selected;
             ListView list = new ListView(getActivity(), null, android.R.attr.dropDownListViewStyle);
             // list.setLayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT)
             list.setBackgroundColor(Color.LTGRAY);
             final List<Chord> alts = alternateChordMap.get(selected);
             ArrayAdapter<Chord> adapter = new ArrayAdapter<Chord>(getActivity(), 0, alts) {
                 @Override
                 public View getView(int position, View convertView, ViewGroup parent) {
                     ImageButton button = new ImageButton(getActivity());
                     Chord altChord = alternateChordMap.get(chordForDialog).get(position);
                     setChordResource(button, chordResourceMap.get(altChord));
                     button.setTag(altChord);
                     button.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View view) {
                             replaceChord(chordForDialog, (Chord)view.getTag());
                             popupForAlts.dismiss();
                         }
                     });
                     return button;
                 }
             };
             list.setAdapter(adapter);
             popupForAlts = new PopupWindow(list,
                     getButtonSize(),
                     ViewGroup.LayoutParams.WRAP_CONTENT);
             // popup.setBackgroundDrawable()
             // popup.setOutsideTouchable(false);
             // popupForAlts.showAtLocation(parent, Gravity., 0, 0 );
             popupForAlts.showAsDropDown(parent, 0,  -(1+alts.size())* (getButtonSize()+10));
 
             // popup.showAsDropDown(parent);
         }
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.detail, menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()) {
             case R.id.action_reset:
                 Toast.makeText(getActivity(), "Reset selected chords", Toast.LENGTH_LONG).show();
                 mItem.setChords(null);
                 getDatabase().updateScore(mItem);
                 startParseChord();
                 return true;
             case R.id.action_edit:
                 Intent intent = new Intent(getActivity(), EditActivity.class);
                 intent.putExtra(EditActivity.ARG_ITEM_ID, mItem.getId());
                 startActivityForResult(intent, ACTIVITY_ID_EDIT);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
             case ACTIVITY_ID_EDIT:
                 reloadItem();
                 return;
         }
         super.onActivityResult(requestCode, resultCode, data);
     }
 
     private void replaceChord(Chord from, Chord to) {
         if(from.equals(to))
             return;
 
         List<Integer> chordIds = mItem.getChords();
         int index = chordIds.indexOf(from.encodeToInt());
         chordIds.set(index, to.encodeToInt());
         mItem.setChords(chordIds); // this code is unnecessary. but for sure.
         getDatabase().updateScore(mItem);
         formatTableIfReady();
     }
 
     private void dismissPopupIfShown() {
         if(isAltPopupShown()) {
             popupForAlts.dismiss();
             popupForAlts = null;
         }
     }
 
     private boolean isAltPopupShown() {
         return popupForAlts != null && popupForAlts.isShowing();
     }
 
     public boolean doBackProcess() {
         if(isAltPopupShown()) {
             dismissPopupIfShown();
             return true;
         }
         return false;
     }
 
     private int getButtonSize() {
         return CHORD_IMAGE_WIDTH+CELL_MARGIN;
     }
 
     private List<Chord> toChords(List<Integer> chords) {
         ArrayList<Chord> res = new ArrayList<Chord>();
         for(int chordInt : chords) {
             res.add(Chord.decodeInt(chordInt));
         }
         return res;
     }
 
 
     private void setChordResource(ImageButton image, int rid) {
         image.setImageBitmap(floatResource(rid, CHORD_IMAGE_WIDTH, CHORD_IMAGE_WIDTH));
     }
 }
