 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2.fragments;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.content.LocalBroadcastManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.Toast;
 import java.util.ArrayList;
 import java.util.List;
 import no.hials.muldvarp.R;
 import no.hials.muldvarp.v2.MuldvarpService;
 import no.hials.muldvarp.v2.MuldvarpService.DataTypes;
 import no.hials.muldvarp.v2.TopActivity;
 import no.hials.muldvarp.v2.database.MuldvarpDataSource;
 import no.hials.muldvarp.v2.domain.Domain;
 import no.hials.muldvarp.v2.domain.Programme;
 import no.hials.muldvarp.v2.utility.DummyDataProvider;
 import no.hials.muldvarp.v2.utility.ListAdapter;
 
 /**
  * This class defines a fragment containing a list of specified ListItems.
  *
  * @author johan
  */
 public class ListFragment extends MuldvarpFragment {
 
     //Global variables
     ProgressDialog progressDialog;
     ListAdapter listAdapter;
     ListView listView;
     View fragmentView;
     List<Domain> items = new ArrayList<Domain>();
     Class destination;
     public enum ListType {COURSE, PROGRAMME, DOCUMENT, VIDEO, NEWS, QUIZ, TOPIC};
     ListType type;
 
     public ListFragment(String fragmentTitle, int iconResourceID, ListType type) {
         super.fragmentTitle = fragmentTitle;
         super.iconResourceID = iconResourceID;
         this.type = type;
     }
 
     public ListFragment(String fragmentTitle, int iconResourceID, List<Domain> items, ListType type) {
         super.fragmentTitle = fragmentTitle;
         super.iconResourceID = iconResourceID;
         this.items = items;
         this.type = type;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         if(fragmentView == null) {
             fragmentView = inflater.inflate(R.layout.layout_listview, container, false);
             listView = (ListView)fragmentView.findViewById(R.id.layoutlistview);
         }
         progressDialog = new ProgressDialog(owningActivity);
         itemsReady();
 
         // We use this to send broadcasts within our local process.
         mLocalBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
          // We are going to watch for interesting local broadcasts.
         IntentFilter filter = new IntentFilter();
         switch(type) {
             case PROGRAMME:
                 filter.addAction(MuldvarpService.ACTION_PROGRAMMES_UPDATE);
                 break;
             case COURSE:
                 filter.addAction(MuldvarpService.ACTION_COURSE_UPDATE);
                 break;
             case NEWS:
                 filter.addAction(MuldvarpService.ACTION_NEWS_UPDATE);
                 break;
             case DOCUMENT:
                 filter.addAction(MuldvarpService.ACTION_LIBRARY_UPDATE);
                 break;
             case VIDEO:
                 filter.addAction(MuldvarpService.ACTION_VIDEO_UPDATE);
                 break;
             default:
                 progressDialog.dismiss();
                 break;
         }
         mReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 System.out.println("Got onReceive in BroadcastReceiver " + intent.getAction());
                 updateItems();
             }
         };
         mLocalBroadcastManager.registerReceiver(mReceiver, filter);
 
         switch(type) {
             case COURSE:
                 owningActivity.mService.update(DataTypes.COURSES, owningActivity.domain.getId());
                 break;
             case PROGRAMME:
                 owningActivity.mService.update(DataTypes.PROGRAMS, 0);
                 break;
             case DOCUMENT:
                 owningActivity.mService.update(DataTypes.DOCUMENTS, 0);
                 break;
             case VIDEO:
                 owningActivity.mService.update(DataTypes.VIDEOS, 0);
                 break;
             case NEWS:
                 owningActivity.mService.update(DataTypes.NEWS, 0);
         }
 
         return fragmentView;
     }
 
     private void updateItems() {
         MuldvarpDataSource mds = new MuldvarpDataSource(getActivity());
         mds.open();
 
         Domain d = owningActivity.domain;
         System.out.println("ID " + d.getId());
         System.out.println("NAME " + d.getName());
 
         items.clear();
         switch(type) {
             case COURSE:
                 items.addAll(mds.getCoursesByProgramme((Programme)owningActivity.domain));
                 break;
             case PROGRAMME:
                 items.addAll(mds.getAllProgrammes());
                 break;
             case DOCUMENT:
                 items.addAll(mds.getAllDocuments());
                 break;
             case VIDEO:
                 items.addAll(mds.getAllVideos());
                 break;
             case NEWS:
                 items.addAll(mds.getArticlesByCategory("news"));
                 break;
             case QUIZ:
                 items.addAll(DummyDataProvider.getQuizList());
                 break;
         }
         if(listAdapter != null) {
             progressDialog.dismiss();
             listAdapter.notifyDataSetChanged();
         }
         //mds.close(); //crash
     }
 
     public void setDestinationClass(Class destinationClass) {
         this.destination = destinationClass;
     }
 
     public void itemsReady() {
 
         //If the items are empty, add temporary dummydata from database
 //        if(items.isEmpty()) {
 //            if(owningActivity.domain == null) {
 //                items.addAll(DummyDataProvider.getFromDatabase(owningActivity));
 //            }
 //        }
 
         showProgressDialog();
         updateItems();
 
         listView.setAdapter(new ListAdapter(
                     fragmentView.getContext(),
                     R.layout.layout_listitem,
                     R.id.text,
                     items,
                     false)
             );
         listAdapter = (ListAdapter) listView.getAdapter();
 
         listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
                 //The idea is that one click should start a corresponding activity.
                 Domain selectedItem = items.get(position);
 
                 if(selectedItem.getActivity() != null) {
                     destination = selectedItem.getActivity();
                     Intent myIntent = new Intent(view.getContext(), destination);
                     myIntent.putExtra("Domain", selectedItem);
                     myIntent.putExtra("type", type);
                     startActivityForResult(myIntent, 0);
                 } else {
                     Intent myIntent = new Intent(view.getContext(), TopActivity.class);
                     myIntent.putExtra("Domain", selectedItem);
                     startActivityForResult(myIntent, 0);
                     //Burde erstattes med en feilbeskjed fra en string i xml-fil
                    Toast show = Toast.makeText(owningActivity, "Muldvarp vet ikke hvordan det skal åpne dette innlegget.", Toast.LENGTH_SHORT);
                    show.show();
                 }
             }
         });
 
 
         listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
             @Override
             public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {   //triggers if a listitem is pressed and held.
                 Domain selectedItem = items.get(pos);                                       //Saves the clicked item in the selectedItem variable.
                 if(owningActivity.getLoggedIn() && selectedItem instanceof Domain){         //If the long-clicked item is a course, it is added to the users list of courses.
                     Domain domain = (Domain) selectedItem;
                     createDialog(domain);                                                   //Creates a new alertDialog asking whether the user wants to add the course to his/her favourites.
                     return true;                                                            //Tells the activity that the click has been "consumed", meaning that onItemClick should not be triggered.
                 }
                 else{                                                                       //If the clicked item isn't a course
                     return false;                                                           //Tells the activity that the click has not been "consumed", meaning that onItemClick will be triggered.
                 }
             }
         });
     }
 
     @Override
     public void queryText(String text){
         listAdapter.filter(text);
     }
 
     public void createDialog(final Domain d){
         AlertDialog.Builder builder = new AlertDialog.Builder(owningActivity);
         builder.setMessage("vil du legge til " + d.getName() + " i mine snarveier?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                         owningActivity.getService().getUser().addDomain(d);                          //Adds the domain to the list of personal shortcuts.
                         Toast toast = Toast.makeText(fragmentView.getContext(),                      //Shows a short toast to the user as feedback, telling him/her that the domain has been added to the user list.
                         d.getName() + " er lagt til i mine snarveier.", Toast.LENGTH_SHORT);
                         owningActivity.updateRBMMenu();
                         toast.show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                         dialog.cancel();                                                              //Dismisses the dialog without doing anything.
                    }
                });
         AlertDialog alert = builder.create();
         alert.show();
     }
 
 
     public void showProgressDialog(){
 
         if(!progressDialog.isShowing()){
 
             progressDialog.setMessage(getString(R.string.loading));
             progressDialog.setIndeterminate(true);
             progressDialog.setCancelable(false);
             progressDialog.show();
         }
     }
 }
