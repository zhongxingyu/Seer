 package com.williamjoy.wall.english;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apmem.tools.layouts.FlowLayout;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.ViewGroup;
 import android.widget.Space;
 import android.widget.TextView;
 
 import com.williamjoy.wall.english.view.VocabularyInput;
 
 public class LearnSentenceActivity extends Activity {
     String learn_content = "Dedicated to my little Mary. :)";
     public static final String LEARN_CONTENT = "LEARN_CONTENT";
     private boolean isAutoComplete = false;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_learn_sentence);
         TextView t = (TextView) this.findViewById(R.id.given_content);
 
         Intent intent = getIntent();
         String input = intent.getStringExtra(LEARN_CONTENT);
         isAutoComplete = intent.getBooleanExtra("ENABLE_AUTO_COMPLETE", false);
         if (input != null && !input.isEmpty()) {
             learn_content = input;
         }
         t.setText(learn_content);
         Pattern p = Pattern.compile(
                 "([a-z0-9']+(-[a-z0-9]+)*)|[,.()#@\\$!?\\\"]+|\\n+",
                 Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
         Matcher m = p.matcher(learn_content);
         ViewGroup stubViewGroup = (ViewGroup) this
                 .findViewById(R.id.passage_layout);
         FlowLayout flowlayout = new FlowLayout(this.getApplicationContext());
 
         stubViewGroup.addView(flowlayout);
         VocabularyInput prev = null;
         while (m.find()) {
             String token = m.group(0);
             if (token.matches("\\n+")) {
                 flowlayout = new FlowLayout(this.getApplicationContext());
                 Space divider = new Space(getApplicationContext());
                 divider.setMinimumHeight(40);
                 stubViewGroup.addView(divider);
                 stubViewGroup.addView(flowlayout);
                 continue;
             }
             VocabularyInput current = new VocabularyInput(
                     getApplicationContext(), token);
             flowlayout.addView(current);
             if (prev != null && current.isEditable()) {
                 prev.setNextFocus(current);
                 current.setPrevFocus(prev);
 
             }
             if (current.isEditable()) {
                 prev = current;
                 current.setAutoComplete(isAutoComplete);
             }
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_learn_sentence, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case (R.id.menu_settings):
                 startActivity(new Intent(this.getApplicationContext(),
                         SettingsActivity.class));
                 break;
             case (R.id.menu_login):
                startActivity(new Intent(this.getApplicationContext(),
                        LoginActivity.class));
                 break;
             case (R.id.menu_input):
                 startActivity(new Intent(this.getApplicationContext(),
                         MaterialEditorActivity.class));
                 break;
             default:
                 return super.onOptionsItemSelected(item);
         }
         return true;
     }
 
 }
