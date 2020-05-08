 package com.jakaricare.app;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.Spanned;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import java.util.HashMap;
 
 public class PatientVerification extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // making activity fullscreen
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         setContentView( R.layout.languages );
         setPageTitle( "1. Front Desk Activation Welcome" );
 
         LinearLayout nextButton = (LinearLayout) findViewById( R.id.btn_next );
         nextButton.setOnClickListener( new View.OnClickListener() {
             @Override
             public void onClick( View view ) {
                 showVerification( view );
             }
         } );
 
 
         HashMap<String, String> footerParams = new HashMap<String, String>(){{
             put( "hide_prev", "true" );
             put( "hide_center", "true" );
         }};
         configureFooter( footerParams );
 
     }
 
     private void setPageTitle( String new_title ) {
 
         TextView pageTitle = ( TextView ) findViewById( R.id.page_title );
 
         if ( pageTitle != null ) {
             pageTitle.setText( new_title );
         }
 
     }
 
     // progress
     private void setProgress( String progress ) {
 
         TextView progressText = ( TextView ) findViewById( R.id.completion_rate );
 
         if ( progressText != null ) {
             progressText.setText( progress );
         }
     }
 
     /**
      * Hiding button
      * @param btnName
      * @param needToHide
      */
     private void hideFooterButton ( String btnName, Object needToHide ) {
 
         if ( needToHide != null && needToHide.toString().equals( "true" ) ) {
 
             int btnId = getResources().getIdentifier( btnName, "id",  "com.jakaricare.app" );
 
             LinearLayout btn = (LinearLayout) findViewById( btnId );
 
             if ( btn != null ) {
                 btn.setVisibility( View.INVISIBLE );
             }
         }
 
     }
 
     /**
      * Changing text on the button
      * @param btnName
      * @param label
      */
     private void changeFooterButtonLabel ( String btnName, Object label ) {
 
         if ( label != null ) {
 
             int btnId = getResources().getIdentifier( btnName, "id",  "com.jakaricare.app" );
 
             TextView btn = (TextView) findViewById( btnId );
             btn.setText( label.toString() );
         }
 
     }
 
 
     private void configureFooter( HashMap params ) {
 
         String[] buttons = { "prev", "center", "next" };
 
         for ( String btnName: buttons ) {
             hideFooterButton( "btn_" + btnName, params.get( "hide_" + btnName ) );
             changeFooterButtonLabel( "btn_" + btnName, params.get( "label_" + btnName ) );
 //            disableFooterButton( "btn_" + btnName, params.get( "label_" + btnName ) );
         }
     }
 
 
 
 
 
     public void showVerification(View view) {
         overridePendingTransition( R.anim.slide_in, R.anim.slide_out );
         setContentView(R.layout.patient_verification );
 
         setPageTitle( "2. Patient Verification" );
 
         LinearLayout nextButton = (LinearLayout) findViewById( R.id.btn_next );
         nextButton.setOnClickListener( new View.OnClickListener() {
             @Override
             public void onClick( View view ) {
                 //Log.d("LOL", "second click handler ");
                 showVerificationDone( view );
             }
         } );
 
         HashMap<String, String> footerParams = new HashMap<String, String>(){{
             put( "hide_prev", "true" );
             put( "hide_center", "true" );
         }};
         configureFooter( footerParams );
 
     }
 
     public void showVerificationDone(View view) {
 
         setContentView( R.layout.patient_verification_done );
 
         setPageTitle( "2. Forms — Introduction" );
         setProgress( "1%" );
 
         LinearLayout nextButton = (LinearLayout) findViewById( R.id.btn_next );
         nextButton.setOnClickListener( new View.OnClickListener() {
             @Override
             public void onClick( View view ) {
                 showTerms( view );
 
             }
         });
         HashMap<String, String> footerParams = new HashMap<String, String>(){{
             put( "hide_prev", "true" );
             put( "hide_center", "true" );
         }};
         configureFooter( footerParams );
 
     }
 
 
 
     public void showTerms(View view) {
         overridePendingTransition( R.anim.slide_in, R.anim.slide_out );
 
         setContentView(R.layout.terms );
 
 
         TextView termsText = (TextView) findViewById( R.id.terms );
         if ( termsText != null ) {
             Spanned formattedText = Html.fromHtml( getResources().getString( R.string.terms ) );
             termsText.setText( formattedText );
         }
 
         setPageTitle( "2.1 Forms — Terms of Use" );
         setProgress( "2%" );
 
         LinearLayout nextButton = (LinearLayout) findViewById( R.id.btn_next );
         nextButton.setOnClickListener( new View.OnClickListener() {
             @Override
             public void onClick( View view ) {
                 showSignature( view );
 
             }
         });
 
         HashMap<String, String> footerParams = new HashMap<String, String>(){{
             put( "hide_center", "true" );
         }};
         configureFooter( footerParams );
     }
 
     public void showSignature(View view) {
 
         setContentView(R.layout.signature );
 
         setPageTitle( "2.1 Forms — Patient Signature" );
         setProgress( "3%" );
 
         LinearLayout nextButton = (LinearLayout) findViewById( R.id.btn_next );
         nextButton.setOnClickListener( new View.OnClickListener() {
             @Override
             public void onClick( View view ) {
                 showPlainQuestion( view );
 
             }
         });
 
         HashMap<String, String> footerParams = new HashMap<String, String>(){{
             put( "hide_center", "true" );
         }};
         configureFooter( footerParams );
     }
 
 
     public void showPlainQuestion(View view) {
 
        setContentView(R.layout.question_single_variant );
 
         setPageTitle( "3. Plain question example" );
         setProgress( "4%" );
 
         LinearLayout nextButton = (LinearLayout) findViewById( R.id.btn_next );
         nextButton.setOnClickListener( new View.OnClickListener() {
             @Override
             public void onClick( View view ) {
                 showYesNoQuestion( view );
 
             }
         });
 
         HashMap<String, String> footerParams = new HashMap<String, String>(){{
             put( "hide_center", "true" );
         }};
         configureFooter( footerParams );
     }
 
 
     public void showYesNoQuestion(View view) {
 
         setContentView(R.layout.yesno_question );
 
         setPageTitle( "3. Yes/no question example" );
         setProgress( "5%" );
 
 //        LinearLayout nextButton = (LinearLayout) findViewById( R.id.btn_next );
 //        nextButton.setOnClickListener( new View.OnClickListener() {
 //            @Override
 //            public void onClick( View view ) {
 //                showYesNoQuestion( view );
 //
 //            }
 //        });
 
         HashMap<String, String> footerParams = new HashMap<String, String>(){{
             put( "hide_center", "true" );
         }};
         configureFooter( footerParams );
     }
 }
