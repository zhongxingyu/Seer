 package com.fun.midworx;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.Spanned;
 import android.text.TextUtils;
 import android.util.Pair;
 import android.view.Display;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TableLayout;
 import android.widget.TextView;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 public class DictionaryActivity extends Activity {
     private static final Logger logger = Logger.getLogger(DictionaryActivity.class.getName());
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_dictionary);
 
         final String word = getIntent().getExtras().getString("word");
         new DefinitionGetter(word, this).execute();
     }
 
     static class HeadWord {
         final String text;
         final List<String> related = new ArrayList<String>();
         final String phonetic;
         final Uri sound;
         final String label;
         final List<Pair<String, List<String>>> meanings = new ArrayList<Pair<String, List<String>>>();
 
         public HeadWord(JSONObject headword, String word) throws JSONException {
             String text = word;
             String phonetic = "/" + word + "/";
             String sound = null;
             String label = null;
             JSONArray terms = headword.getJSONArray("terms");
             for (int i = 0; i < terms.length(); ++i) {
                 JSONObject term = terms.getJSONObject(i);
                 if (term.getString("type").equals("text")) {
                     text = term.getString("text");
                     if (term.has("labels")) {
                         JSONArray labels = term.getJSONArray("labels");
                         label = labels.getJSONObject(0).getString("text");
                     }
                 } else if (term.getString("type").equals("phonetic")) {
                     phonetic = term.getString("text");
                 } else if (term.getString("type").equals("sound")) {
                     sound = term.getString("text");
                 }
             }
             JSONArray entries = headword.getJSONArray("entries");
             for (int i = 0; i < entries.length(); ++i) {
                 JSONObject entry = entries.getJSONObject(i);
                 if (entry.getString("type").equals("related")) {
                     JSONArray termsArr = entry.getJSONArray("terms");
                     for (int j = 0; j < termsArr.length(); ++j) {
                         JSONObject term = termsArr.getJSONObject(j);
                         if (term.getString("text").equals(word)) {
                             related.add(term.getJSONArray("labels").getJSONObject(0).getString("text"));
                         }
                     }
                 } else if (entry.getString("type").equals("meaning")) {
                     JSONObject term = entry.getJSONArray("terms").getJSONObject(0);
                     List<String> examples = new ArrayList<String>();
                     if (entry.has("entries")) {
                         JSONArray exampleEntries = entry.getJSONArray("entries");
                         for (int j = 0; j < exampleEntries.length(); ++j) {
                             JSONObject example = exampleEntries.getJSONObject(j);
                             if (!example.getString("type").equals("example")) {
                                 continue;
                             }
                             examples.add(example.getJSONArray("terms").getJSONObject(0).getString("text"));
                         }
                     }
                     meanings.add(Pair.create(term.getString("text"), examples));
                 }
             }
 
             this.text = text;
             this.phonetic = phonetic;
             this.sound = Uri.parse(sound);
             this.label = label;
         }
     }
 
     Spanned meaningToSpanned(Pair<String, List<String>> meaning) {
         String text = meaning.first;
         if (meaning.second.size() > 0) {
             text += ": \"" + meaning.second.get(0) + "\"";
         }
         return Html.fromHtml(text.replaceAll("x3c", "<").replaceAll("x3e", ">")
                 .replaceAll("x27", "'"));
     }
 
     List<View> getTableRows(List<Pair<String, List<String>>> meanings) {
         List<View> retval = new ArrayList<View>();
         for (int i = 0; i < meanings.size(); ++i) {
             View tableRow = getLayoutInflater().inflate(R.layout.definition_meaning, null);
             TextView ordinal = (TextView) tableRow.findViewById(R.id.ordinal);
             if (meanings.size() == 1) {
                 ordinal.setVisibility(View.GONE);
             } else {
                 ordinal.setText((i + 1) + ".");
             }
             ((TextView) tableRow.findViewById(R.id.meaning)).setText(meaningToSpanned(meanings.get(i)));
             retval.add(tableRow);
         }
         return retval;
     }
 
     class DefinitionGetter extends AsyncTask<Void, Void, JSONObject> {
         private static final String GOOGLE_DICT_URL =
                 "http://www.google.com/dictionary/json?callback=x&q=%s&sl=en&tl=en&restrict=pr%2Cde";
 
         private final String word;
         private final Context context;
 
         private TextView wordText;
         private TextView relatedText;
         private TextView phoneticText;
         private ImageButton pronounceButton;
         private LinearLayout definitionsList;
 
         DefinitionGetter(String word, Context context) {
             this.word = word;
             this.context = context;
 
             this.wordText = (TextView) findViewById(R.id.wordText);
             this.relatedText = (TextView) findViewById(R.id.relatedText);
             this.phoneticText = (TextView) findViewById(R.id.phoneticText);
             this.pronounceButton = (ImageButton) findViewById(R.id.pronounceButton);
             this.definitionsList = (LinearLayout) findViewById(R.id.definitionsList);
         }
 
         @Override
         protected JSONObject doInBackground(Void... params) {
             try {
                 URL url = new URL(GOOGLE_DICT_URL.replace("%s", word));
                 BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                 StringBuilder jsonpBuilder = new StringBuilder();
                 String line;
                 while ((line = br.readLine()) != null) {
                     jsonpBuilder.append(line);
                 }
                 br.close();
                 String jsonp = jsonpBuilder.toString();
                 String json = jsonp.substring(2, jsonp.length()-10);
                 return new JSONObject(json);
             } catch (MalformedURLException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (JSONException e) {
                 e.printStackTrace();
             }
 
             return null;
         }
 
         @Override
         protected void onPostExecute(JSONObject json) {
             if (json == null) {
                 // TODO(misha): display no definition found.
                 return;
             }
 
             try {
                 JSONArray primaries = json.getJSONArray("primaries");
                 final Map<String, List<HeadWord>> headwords = new HashMap<String, List<HeadWord>>();
 
                 HeadWord firstHeadword = null;
                 for (int i = 0; i < primaries.length(); ++i) {
                     JSONObject object = primaries.getJSONObject(i);
                     if (object.getString("type").equals("headword")) {
                         HeadWord headword = new HeadWord(object, this.word);
                         if (!headwords.containsKey(headword.label)) {
                             headwords.put(headword.label, new ArrayList<HeadWord>());
                         }
                         headwords.get(headword.label).add(headword);
                         if (firstHeadword == null) {
                             firstHeadword = headword;
                         }
                     }
                 }
 
                 String titleText = this.word;
                 if (firstHeadword.related.isEmpty()) {
                     titleText = firstHeadword.text;
                 }
                 wordText.setText(titleText);
                 phoneticText.setText(firstHeadword.phonetic);
                 final Uri sound = firstHeadword.sound;
                 pronounceButton.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         MediaPlayer.create(context, sound).start();
                     }
                 });
 
                 Map<Pair<String, String>, List<String>> relatedDict = new HashMap<Pair<String, String>, List<String>>();
                 for (Map.Entry<String, List<HeadWord>> labelEntry : headwords.entrySet()) {
                     View definition = getLayoutInflater().inflate(R.layout.definition, null);
                     TextView labelText = (TextView) definition.findViewById(R.id.label);
                     labelText.setText(labelEntry.getKey());
                     if (labelEntry.getKey() == null) {
                         labelText.setVisibility(View.GONE);
                     }
                     List<Pair<String, List<String>>> meanings = new ArrayList<Pair<String, List<String>>>();
                     for (HeadWord headword : labelEntry.getValue()) {
                         Pair<String, String> relatedKey = Pair.create(headword.text, labelEntry.getKey());
                         if (!relatedDict.containsKey(relatedKey)) {
                             relatedDict.put(relatedKey, new ArrayList<String>());
                         }
                         relatedDict.get(relatedKey).addAll(headword.related);
                         meanings.add(headword.meanings.get(0));
                     }
                     if ((headwords.size() == 1) && (labelEntry.getValue().size() == 1) &&
                             (labelEntry.getValue().get(0).meanings.size() > 1)) {
                         meanings.add(labelEntry.getValue().get(0).meanings.get(1));
                     }
                     List<View> rows = getTableRows(meanings);
                     TableLayout table = (TableLayout) definition.findViewById(R.id.meaningsTable);
                     for (View row : rows) {
                         table.addView(row);
                     }
                     definitionsList.addView(definition);
                 }
 
                 StringBuilder relatedString = new StringBuilder();
                 Set<String> allRelated = new HashSet<String>();
                 for (Map.Entry<Pair<String, String>, List<String>> entry : relatedDict.entrySet()) {
                     allRelated.addAll(entry.getValue());
                     if (entry.getValue().isEmpty()) {
                         continue;
                     }
                     relatedString.append(String.format("%1$s of <b>%2$s</b> (%3$s)<br/>",
                             TextUtils.join(", ", entry.getValue()),
                             entry.getKey().first, entry.getKey().second));
                 }
                 new ImageGetter(this.word, allRelated).execute();
                 relatedText.setText(Html.fromHtml(relatedString.toString()));
             } catch (JSONException e) {
                 e.printStackTrace();
             };
         }
     }
 
     class ImageGetter extends AsyncTask<Void, Void, Bitmap> {
         private static final String FREEBASE_SEARCH_URL =
                 "https://www.googleapis.com/freebase/v1/search?query=%1$s";
         private static final String FREEBASE_IMAGE_URL =
                 "https://www.googleapis.com/freebase/v1/image%1$s?maxwidth=%2$d&maxheight=%3$d";
         private static final String WIKIPEDIA_REDIR_URL =
                 "http://en.wikipedia.org/w/api.php?format=json&action=query&titles=%1$s&redirects";
 
         private final String word;
         private final Set<String> related;
 
         private ImageView image;
 
         public ImageGetter(String word, Set<String> related) {
             this.word = word;
             this.related = related;
             this.image = (ImageView) findViewById(R.id.image);
         }
 
         Map<String, String> getIdToMid(JSONObject json) throws JSONException {
             Map<String, String> map = new HashMap<String, String>();
             JSONArray array = json.getJSONArray("result");
             for (int i = 0; i < array.length(); ++i) {
                 JSONObject item = array.getJSONObject(i);
                if (!item.has("id")) {
                     continue;
                 }
                String id = item.getString("id");
                if (!id.startsWith("/en/")) {
                    continue;
                }
                map.put(id.substring(4).toLowerCase(), item.getString("mid"));
             }
             return map;
         }
 
         Point getMaxImageSize() {
             Point size = new Point();
             Display display = getWindowManager().getDefaultDisplay();
             if (Build.VERSION.SDK_INT >= 13) {
                 display.getSize(size);
             } else {
                 size.set(display.getWidth(), 0);
             }
             size.set(size.x / 2, (int) (size.x / 2. / 4. * 3.));
             return size;
         }
 
         Bitmap midToBitmap(String mid) throws IOException {
             Point size = getMaxImageSize();
             URL url = new URL(String.format(FREEBASE_IMAGE_URL, mid, size.x, size.y));
             return BitmapFactory.decodeStream(url.openStream());
         }
 
         JSONObject readJson(String urlStr) throws IOException, JSONException {
             URL url = new URL(urlStr);
             BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
             StringBuilder jsonBuilder = new StringBuilder();
             String line;
             while ((line = br.readLine()) != null) {
                 jsonBuilder.append(line);
             }
             br.close();
             return new JSONObject(jsonBuilder.toString());
         }
 
         @Override
         protected Bitmap doInBackground(Void... voids) {
             try {
                 JSONObject json = readJson(String.format(FREEBASE_SEARCH_URL, this.word));
                 Map<String, String> idToMid = getIdToMid(json);
                 if (idToMid.containsKey(this.word)) {
                     return midToBitmap(idToMid.get(this.word));
                 }
                 for (String potentialWord : this.related) {
                     if (idToMid.containsKey(potentialWord)) {
                         return midToBitmap(idToMid.get(potentialWord));
                     }
                 }
                 JSONObject query = readJson(String.format(WIKIPEDIA_REDIR_URL, this.word)).getJSONObject("query");
                 if (query.has("redirects")) {
                     JSONArray redirects = query.getJSONArray("redirects");
                     for (int i = 0; i < redirects.length(); ++i) {
                         String redirect = redirects.getJSONObject(i).getString("to").toLowerCase();
                         if (idToMid.containsKey(redirect)) {
                             return midToBitmap(idToMid.get(redirect));
                         }
                     }
                 }
             } catch (MalformedURLException e) {
                 e.printStackTrace();
             } catch (JSONException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(Bitmap bm) {
             if (bm == null) {
                 // TODO(misha): Say we don't have an image :(.
             }
             image.setImageBitmap(bm);
         }
     }
 }
