 
 package org.inftel.ssa.mobile.ui.fragments;
 
 import static android.content.Intent.ACTION_EDIT;
 import static android.content.Intent.ACTION_VIEW;
 import static org.inftel.ssa.mobile.util.Util.formatDate;
 
 import org.inftel.ssa.mobile.R;
 import org.inftel.ssa.mobile.provider.SsaContract.Projects;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TabHost;
 import android.widget.TextView;
 
 public class ProjectDetailFragment extends Fragment implements LoaderCallbacks<Cursor> {
 
     protected final static String TAG = "ProjectDetailFragment";
 
     private static final String TAG_DESCRIPTION = "description";
     private static final String TAG_INFORMATION = "information";
     private static final String TAG_LINKS = "links";
 
     protected Handler mHandler = new Handler();
     protected Activity mActivity;
     private Uri mContentUri;
     private String mProjectId;
     private String name;
     private String summary;
     private String description;
     private String labels;
     private String opened;
     private String closed;
     private String started;
     private String license;
     private String company;
     private String links;
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         mActivity = getActivity();
 
         if (mContentUri != null) {
             getLoaderManager().initLoader(0, null, this);
         }
 
         setHasOptionsMenu(true);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
         View view = inflater.inflate(R.layout.ssa_project_details, container, false);
 
         Bundle arguments = getArguments();
         if (arguments != null && arguments.get("_uri") != null) {
             mContentUri = (Uri) arguments.get("_uri");
         }
 
         setHasOptionsMenu(true);
 
         // Handle sprints click
         view.findViewById(R.id.project_btn_sprints).setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 startActivity(new Intent(ACTION_VIEW, Projects.buildSprintsDirUri(mProjectId)));
             }
         });
         // Handle users click
         view.findViewById(R.id.project_btn_users).setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 startActivity(new Intent(ACTION_VIEW, Projects.buildUsersDirUri(mProjectId)));
             }
         });
         // Handle tasks click
         view.findViewById(R.id.project_btn_tasks).setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 startActivity(new Intent(ACTION_VIEW, Projects.buildTasksDirUri(mProjectId)));
             }
         });
 
         TabHost tabHost = (TabHost) view.findViewById(android.R.id.tabhost);
         tabHost.setup();
         setupDescriptionTab(view);
         setupLinksTab(view);
         setupInformationTab(view);
 
         return view;
     }
 
     @Override
     public void onResume() {
         super.onResume();
     }
 
     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         String[] projection = new String[] {
                 Projects._ID,
                 Projects.PROJECT_NAME,
                 Projects.PROJECT_SUMMARY,
                 Projects.PROJECT_DESCRIPTION,
                 Projects.PROJECT_OPENED,
                 Projects.PROJECT_STARTED,
                 Projects.PROJECT_CLOSE,
                 Projects.PROJECT_COMPANY,
                 Projects.PROJECT_LICENSE,
                 Projects.PROJECT_LABELS,
                 Projects.PROJECT_LINKS
         };
         return new CursorLoader(mActivity, mContentUri, projection, null, null, null);
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
         if (data.moveToFirst()) {
             mProjectId = data.getString(data.getColumnIndex(Projects._ID));
             name = data.getString(data.getColumnIndex(Projects.PROJECT_NAME));
             summary = data.getString(data.getColumnIndex(Projects.PROJECT_SUMMARY));
             description = data.getString(data.getColumnIndex(Projects.PROJECT_DESCRIPTION));
             opened = data.getString(data.getColumnIndex(Projects.PROJECT_OPENED));
             started = data.getString(data.getColumnIndex(Projects.PROJECT_STARTED));
             closed = data.getString(data.getColumnIndex(Projects.PROJECT_CLOSE));
             company = data.getString(data.getColumnIndex(Projects.PROJECT_COMPANY));
             license = data.getString(data.getColumnIndex(Projects.PROJECT_LICENSE));
             labels = data.getString(data.getColumnIndex(Projects.PROJECT_LABELS));
             links = data.getString(data.getColumnIndex(Projects.PROJECT_LINKS));
 
             // taskChart(mProjectId);
 
             // Update UI
             mHandler.post(new Runnable() {
                 public void run() {
 
                     // Header
                     ((TextView) getView().findViewById(R.id.detail_title)).setText(name);
                     ((TextView) getView().findViewById(R.id.detail_subtitle)).setText(summary);
 
                     // Tab description
                    if (description != null) {
                        ((TextView) getView().findViewById(R.id.project_detail_description))
                                .setText(Html.fromHtml(description));
                    }
 
                     // Tab links
                     // TODO Ver como llegan los links y aplicarle formato
                     String link = "<a href='http://www.masterinftel.uma.es/'>Master Inftel</a>";
                     TextView l = (TextView) getView().findViewById(R.id.empty_links);
                     l.setText(Html.fromHtml(link));
                     l.setMovementMethod(LinkMovementMethod.getInstance());
                     l.setLinksClickable(true);
 
                     // Tab Information
                     ((TextView) getView().findViewById(R.id.lblOpened)).setText(formatDate(opened));
                     ((TextView) getView().findViewById(R.id.lblStarted))
                             .setText(formatDate(started));
                     ((TextView) getView().findViewById(R.id.lblClosed)).setText(formatDate(closed));
                     ((TextView) getView().findViewById(R.id.lblCompany)).setText(company);
                     ((TextView) getView().findViewById(R.id.lblLicense)).setText(license);
                     ((TextView) getView().findViewById(R.id.lblLabels)).setText(labels);
 
                 }
             });
         }
 
     }
 
     private void setupLinksTab(View view) {
         TabHost mTabHost = (TabHost) view.findViewById(android.R.id.tabhost);
         mTabHost.addTab(mTabHost.newTabSpec(TAG_LINKS)
                 .setIndicator(buildIndicator(R.string.project_links, view))
                 .setContent(R.id.tab_project_links));
     }
 
     private void setupInformationTab(View view) {
         TabHost mTabHost = (TabHost) view.findViewById(android.R.id.tabhost);
         mTabHost.addTab(mTabHost.newTabSpec(TAG_INFORMATION)
                 .setIndicator(buildIndicator(R.string.project_information, view))
                 .setContent(R.id.tab_project_information));
     }
 
     private void setupDescriptionTab(View view) {
         TabHost mTabHost = (TabHost) view.findViewById(android.R.id.tabhost);
         mTabHost.addTab(mTabHost.newTabSpec(TAG_DESCRIPTION)
                 .setIndicator(buildIndicator(R.string.project_description, view))
                 .setContent(R.id.tab_detail_project_description));
     }
 
     private View buildIndicator(int textRes, View view) {
         final TextView indicator = (TextView) getActivity().getLayoutInflater()
                 .inflate(R.layout.tab_indicator,
                         (ViewGroup) view.findViewById(android.R.id.tabs), false);
         indicator.setText(textRes);
         return indicator;
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
         mHandler.post(new Runnable() {
             public void run() {
                 // ((TextView)
                 // getView().findViewById(R.id.sprint_title)).setText("");
                 // ((TextView)
                 // getView().findViewById(R.id.sprint_subtitle)).setText("");
             }
         });
 
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.details_menu_items, menu);
         super.onCreateOptionsMenu(menu, inflater);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_edit:
                 Log.d(TAG, "Editando proyecto");
                 startActivity(new Intent(ACTION_EDIT, Projects.buildProjectUri(mProjectId)));
                 return true;
         }
         return super.onOptionsItemSelected(item);
 
     }
 
     // public void taskChart(String projectId) {
     //
     // ContentResolver cr = getActivity().getContentResolver();
     // String[] projection = new String[] {
     // Tasks.TASK_ESTIMATED,
     // Tasks.TASK_REMAINING,
     // Tasks.TASK_BURNED
     // };
     // String selection = "project_id = ?";
     // String[] selectionArgs = new String[] {
     // projectId
     // };
     // Cursor cursor = cr.query(Tasks.CONTENT_URI, projection, selection,
     // selectionArgs, null);
     // int taskCount = cursor.getCount();
     // int i = 1;
     // ArrayList<GraphViewData> estimatedData = new ArrayList<GraphViewData>();
     // ArrayList<GraphViewData> realData = new ArrayList<GraphViewData>();
     //
     // if (taskCount > 0) {
     // while (cursor.moveToNext()) {
     // int estimated = Integer.parseInt(cursor.getString(cursor
     // .getColumnIndex(Tasks.TASK_ESTIMATED)));
     // String remaining =
     // cursor.getString(cursor.getColumnIndex(Tasks.TASK_REMAINING));
     // String burned =
     // cursor.getString(cursor.getColumnIndex(Tasks.TASK_BURNED));
     // int real = Integer.parseInt(remaining) + Integer.parseInt(burned);
     // System.out.println("estimated: " + estimated);
     // System.out.println("real " + real);
     //
     // estimatedData.add(new GraphViewData(i, estimated));
     // realData.add(new GraphViewData(i, real));
     // i++;
     // System.out.println("i: " + i);
     //
     // }
     //
     // GraphViewData[] estimatedDataArray = new GraphViewData[taskCount];
     // GraphViewData[] realDataArray = new GraphViewData[taskCount];
     //
     // estimatedData.toArray(estimatedDataArray);
     // realData.toArray(realDataArray);
     //
     // GraphViewSeries estimatedSeries = new GraphViewSeries("Stimated",
     // Color.BLUE,
     // estimatedDataArray);
     // GraphViewSeries realSeries = new GraphViewSeries("Stimated", Color.BLUE,
     // realDataArray);
     // GraphView graphView;
     // graphView = new LineGraphView(
     // getActivity() // context
     // , "Stress Chart" // heading
     // );
     // graphView.addSeries(estimatedSeries);
     // graphView.addSeries(realSeries);
     // graphView.setBackgroundColor(Color.BLACK);
     //
     // // set legend
     // graphView.setShowLegend(true);
     // graphView.setLegendAlign(LegendAlign.BOTTOM);
     // graphView.setLegendWidth(200);
     //
     // LinearLayout layout = (LinearLayout) getView().findViewById(R.id.graph1);
     // layout.addView(graphView);
     // }
     // ContentResolver cr = getActivity().getContentResolver();
     // String[] projection = new String[] {
     // Tasks.TASK_ESTIMATED,
     // Tasks.TASK_REMAINING,
     // Tasks.TASK_BURNED
     // };
     // String selection = "project = ?";
     // String[] selectionArgs = new String[] {
     // projectId
     // };
     // Cursor cursor = cr.query(Tasks.CONTENT_URI, projection, selection,
     // selectionArgs, null);
     // int taskCount = cursor.getCount();
     // int i = 1;
     // ArrayList<GraphViewData> estimatedData = new
     // ArrayList<GraphViewData>();
     // ArrayList<GraphViewData> realData = new ArrayList<GraphViewData>();
     //
     // if (taskCount > 0) {
     // while (cursor.moveToNext()) {
     // int estimated = Integer.parseInt(cursor.getString(cursor
     // .getColumnIndex(Tasks.TASK_ESTIMATED)));
     // String remaining =
     // cursor.getString(cursor.getColumnIndex(Tasks.TASK_REMAINING));
     // String burned =
     // cursor.getString(cursor.getColumnIndex(Tasks.TASK_BURNED));
     // int real = Integer.parseInt(remaining) + Integer.parseInt(burned);
     // System.out.println("estimated: " + estimated);
     // System.out.println("real " + real);
     //
     // estimatedData.add(new GraphViewData(i, estimated));
     // realData.add(new GraphViewData(i, real));
     // i++;
     // System.out.println("i: " + i);
     //
     // }
     //
     // GraphViewData[] estimatedDataArray = new GraphViewData[taskCount];
     // GraphViewData[] realDataArray = new GraphViewData[taskCount];
     //
     // estimatedData.toArray(estimatedDataArray);
     // realData.toArray(realDataArray);
     //
     // GraphViewSeries estimatedSeries = new GraphViewSeries("Stimated",
     // Color.BLUE,
     // estimatedDataArray);
     // GraphViewSeries realSeries = new GraphViewSeries("Stimated",
     // Color.BLUE, realDataArray);
     // GraphView graphView;
     // graphView = new LineGraphView(
     // getActivity() // context
     // , "Stress Chart" // heading
     // );
     // graphView.addSeries(estimatedSeries);
     // graphView.addSeries(realSeries);
     // graphView.setBackgroundColor(Color.BLACK);
     //
     // // set legend
     // graphView.setShowLegend(true);
     // graphView.setLegendAlign(LegendAlign.BOTTOM);
     // graphView.setLegendWidth(200);
     //
     // LinearLayout layout = (LinearLayout)
     // getView().findViewById(R.id.graph1);
     // layout.addView(graphView);
     // }
 
     // }
 
     // public void fillProjectDetailsFragment(View view, Uri projectUri) {
     //
     // // The text view for task data, identified by its ID in the XML file.
     // mTxtName = (TextView) view.findViewById(R.id.lblProjectName);
     // mTxtSummary = (TextView) view.findViewById(R.id.lblProjectSummary);
     // mTxtDescription = (TextView)
     // view.findViewById(R.id.lblProjectDescription);
     //
     // // Get the note!
     // mCursor = getActivity().managedQuery(projectUri, PROJECTION, null, null,
     // null);
     // if (mCursor != null) {
     // // Requery in case something changed while paused (such as the
     // // title)
     //
     // mCursor.requery();
     //
     // int colName = mCursor.getColumnIndex(Projects.PROJECT_NAME);
     // int colSummary = mCursor.getColumnIndex(Projects.PROJECT_SUMMARY);
     // int colDescription =
     // mCursor.getColumnIndex(Projects.PROJECT_DESCRIPTION);
     // String txt = "";
     // if (mCursor.moveToFirst()) {
     // txt = mCursor.getString(colName);
     // mTxtName.setText(txt);
     // txt = mCursor.getString(colSummary);
     // mTxtSummary.setText(txt);
     // txt = mCursor.getString(colDescription);
     // mTxtDescription.setText(txt);
     // }
     // mCursor.close();
     //
     // }
     // }
 }
