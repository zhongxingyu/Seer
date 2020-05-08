 /*
  * Copyright (c) 2013 by Adam Hellberg.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package com.f16gaming.pathofexilestatistics;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.*;
 import android.widget.*;
 import net.simonvt.numberpicker.NumberPicker;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.json.JSONException;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Gamer
  * Date: 2013-02-28
  * Time: 02:27
  * To change this template use File | Settings | File Templates.
  */
 public class MainActivity extends Activity {
     private final String statsUrl = "http://api.pathofexile.com/leagues/%s?ladder=1&ladderOffset=%d&ladderLimit=%d";
     private final String normalLeague = "Default";
     private final String hardcoreLeague = "Hardcore";
     private final int limit = 200;
     private final int max = 15000;
 
     private ProgressDialog progressDialog;
     private AlertDialog rankGoDialog;
     private CharSequence progressDialogText;
 
     private ListView statsView;
     private View listFooter;
 
     private int offset = 0; // Num entries to load = limit + limit * offset
     private boolean showHardcore = false;
     private int refreshOffset = 0; // Current refresh offset
 
     private ArrayList<PoeEntry> poeEntries;
     private EntryAdapter adapter;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.main);
 
         LayoutInflater inflater = getLayoutInflater();
 
         poeEntries = new ArrayList<PoeEntry>();
 
         statsView = (ListView) findViewById(R.id.statsView);
 
         listFooter = inflater.inflate(R.layout.list_footer, null);
 
         statsView.addFooterView(listFooter);
 
         statsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 onStatsViewClick(position);
             }
         });
 
         AlertDialog.Builder rankGoBuilder = new AlertDialog.Builder(this);
         View view = inflater.inflate(R.layout.rank_go_dialog, null);
 
         net.simonvt.numberpicker.NumberPicker numberPicker = (net.simonvt.numberpicker.NumberPicker) view.findViewById(R.id.number_picker);
         numberPicker.setMinValue(1);
         numberPicker.setMaxValue(max);
 
         rankGoBuilder.setTitle(R.string.rank_go_dialog_title)
                      .setView(view)
                      .setCancelable(true)
                      .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialogInterface, int i) {
                              // Do nothing
                          }
                      })
                      .setPositiveButton(R.string.go, new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialogInterface, int i) {
                              int rawOffset = ((net.simonvt.numberpicker.NumberPicker) rankGoDialog.findViewById(R.id.number_picker)).getValue() - 1;
                              if (rawOffset >= limit * offset && rawOffset <= limit + limit * offset)
                                  showToast(String.format("Rank #%d is already showing", rawOffset + 1));
                              else
                                  updateList(showHardcore, false, rawOffset);
                          }
                      });
 
         rankGoDialog = rankGoBuilder.create();
 
         resetList();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         super.onPrepareOptionsMenu(menu);
         MenuItem toggleItem = menu.findItem(R.id.menu_toggle);
         toggleItem.setTitle(showHardcore ? R.string.menu_normal : R.string.menu_hardcore);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_refresh:
                 refreshList();
                 return true;
             case R.id.menu_search:
                 rankGoDialog.show();
                 return true;
             case R.id.menu_reset:
                 resetList();
             case R.id.menu_toggle:
                 toggleList();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void showToast(CharSequence text) {
         Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
     }
 
     private void showProgress(CharSequence text) {
         showProgress("", text);
     }
 
     private void showProgress(CharSequence title, CharSequence text) {
         if (progressDialog != null) {
             progressDialog.setTitle(title);
             progressDialog.setMessage(text);
             return;
         }
 
         progressDialogText = text;
         progressDialog = ProgressDialog.show(this, title, progressDialogText, true, false);
     }
 
     private void showProgress(CharSequence title, String format, Object... args) {
         showProgress(title, String.format(format, args));
     }
 
     private void hideProgress() {
         if (progressDialog == null)
             return;
         progressDialog.dismiss();
         progressDialog = null;
     }
 
     private void resetList() {
         offset = -1;
         updateList(showHardcore);
     }
 
     private void toggleList() {
         updateList(!showHardcore);
     }
 
     private void refreshList() {
         refreshOffset = 0;
         updateList(showHardcore, true);
     }
 
     private void updateList(boolean hardcore) {
         updateList(hardcore, false);
     }
 
     private void updateList(boolean hardcore, boolean refresh) {
         updateList(hardcore, refresh, 0);
     }
 
     private void updateList(boolean hardcore, boolean refresh, int rawOffset) {
         if (hardcore != showHardcore) // Switching mode
             offset = 0;
         else if (!refresh && rawOffset == 0) // Not refreshing or jumping, load more entries
             offset++;
         else if (rawOffset > 0) // Calculate actual offset
             offset = (int) Math.floor((double) rawOffset / (double) limit);
 
         if (refresh)
             showProgress("Refreshing", "Refreshing data (%d/%d)...", refreshOffset + 1, offset + 1);
         else
             showProgress("Retrieving stats data...");
 
         // Get the league we want to load from
         String league = hardcore ? hardcoreLeague : normalLeague;
 
         // Construct proper URL
         // Refreshes start at 0 then build up to offset var, hence the separate refreshOffset var
         String url = refresh ?
                 String.format(statsUrl, league, limit * refreshOffset, limit)
                 : String.format(statsUrl, league, limit * offset, limit);
 
         // Create the task object to handle our request
         new RetrieveStatsTask(new RetrieveStatsListener() {
             @Override
             public void handleResponse(StatsResponse response, boolean hardcore, boolean refresh, boolean jump) {
                 updateList(response, hardcore, refresh, jump);
             }
         }, hardcore, refresh, rawOffset > 0).execute(url); // And execute it!
     }
 
     private void updateList(StatsResponse response, boolean hardcore, boolean refresh, boolean jump) {
         if (response == null) {
             // Something went wrong and we didn't even get a response body
             showToast("Failed to retrieve PoE stats data!");
             hideProgress();
             return;
         }
 
         try {
             StatusLine status = response.getStatus();
             // Should probably make this handle status codes listed on PoE API
             if (status.getStatusCode() == HttpStatus.SC_OK) {
                 String responseString = response.getResponseString();
                 PoeEntry[] newEntries = PoeEntry.getEntriesFromJSONString(responseString);
 
                 // This was a mode switch, refresh or jump; clear the previous entries
                if (hardcore != showHardcore || (refresh && refreshOffset == 0) || jump)
                     poeEntries.clear();
 
                 // Add the newly loaded entries
                 Collections.addAll(poeEntries, newEntries);
 
                 // Continue loading entries if this is a refresh and it's not done yet
                 if (refresh && refreshOffset < offset) {
                     refreshOffset++;
                     updateList(hardcore, true);
                     return;
                 }
 
                 // If adapter var is null, this is the first updateList execution
                 if (adapter == null) {
                     adapter = new EntryAdapter(this, poeEntries, this, getResources());
                     statsView.setAdapter(adapter);
                 } else { // Otherwise just notify it that data has changed
                     adapter.notifyDataSetChanged();
                 }
 
                 // Show a toast to the user telling them the data is updated
                 showToast(String.format("%s stats updated", hardcore ? "Hardcore" : "Normal"));
 
                 // Remove the "Load more entries" at bottom of list if we reached the maximum
                 if (hardcore == showHardcore && limit + limit * offset >= max)
                     statsView.removeFooterView(listFooter);
                 else if (hardcore != showHardcore)
                 {
                     // Otherwise add it back if it's not there
                     if (statsView.getFooterViewsCount() == 0)
                         statsView.addFooterView(listFooter);
                     statsView.setSelection(0);
                 }
 
                 // Update the showHardcore var to reflect new value
                 showHardcore = hardcore;
 
                 // Update title to new mode
                 setTitle(showHardcore ? R.string.hardcore : R.string.normal);
 
                 // Invalidate the options menu to update the text properly
                 // (This is only needed and supported on SDK level >= 11, since older versions
                 // always update the menu every time it's shown
                 if (Build.VERSION.SDK_INT >= 11)
                     invalidateOptionsMenu();
             } else {
                 showToast("Failed to retrieve PoE stats data!");
             }
         } catch (JSONException e) { // Invalid JSON returned
             showToast("Error while parsing JSON data");
         } catch (ClassCastException e) { // Usually indicative of a JSON parsing error
             showToast("Failed to retrieve PoE stats data!");
         }
 
         hideProgress();
     }
 
     private void onStatsViewClick(int index) {
         if (poeEntries == null || index < 0)
             return;
 
         if (index >= poeEntries.size() && index == statsView.getCount() - 1) {
             updateList(showHardcore);
         } else {
             PoeEntry entry = poeEntries.get(index);
             AlertDialog dialog = entry.getInfoDialog(this, getResources());
             dialog.show();
         }
     }
 }
