 package com.Andryyo.ArchPad.start;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import com.Andryyo.ArchPad.R;
 import com.Andryyo.ArchPad.database.CSQLiteOpenHelper;
 import com.Andryyo.ArchPad.target.CRing;
 import com.Andryyo.ArchPad.target.CTarget;
 import com.Andryyo.ArchPad.target.CTargetView;
 
 import java.io.IOException;
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Андрей
  * Date: 30.04.13
  * Time: 23:15
  * To change this template use File | Settings | File Templates.
  */
 public class CTargetSelectFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,View.OnClickListener {
     CTargetSelectAdapter adapter;
     Gallery gallery;
     LoaderManager loaderManager;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState)  {
         View view = inflater.inflate(R.layout.target_select_fragment, null);
         gallery = (Gallery) view.findViewById(R.id.gallery);
         adapter = new CTargetSelectAdapter(getActivity(), null);
         gallery.setAdapter(adapter);
         view.findViewById(R.id.addTarget).setOnClickListener(this);
         view.findViewById(R.id.deleteTarget).setOnClickListener(this);
         loaderManager = getLoaderManager();
         loaderManager.initLoader(0, null, this);
         return view;
     }
 
     public long getSelectedItemId() {
         return gallery.getSelectedItemId();
     }
 
     @Override
     public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
         return CSQLiteOpenHelper.getCursorLoader(getActivity(), CSQLiteOpenHelper.TABLE_TARGETS);
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> objectLoader, Cursor cursor) {
         adapter.changeCursor(cursor);
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> objectLoader) {
         adapter.changeCursor(null);
     }
 
     @Override
     public void onClick(View view) {
         switch (view.getId())   {
             case R.id.addTarget:
             {
                 CTargetCreateDialog.newInstance().show(getFragmentManager().beginTransaction(), "targetCreateDialog");
                 break;
             }
             case R.id.deleteTarget:
             {
                 CSQLiteOpenHelper.getHelper(getActivity()).deleteTarget(gallery.getSelectedItemId());
                 loaderManager.restartLoader(0, null, this);
                 break;
             }
         }
     }
 
     public void saveTarget(CTarget target) {
         if (!target.isEmpty())
         {
             target.addClosingRing();
             CSQLiteOpenHelper.getHelper(getActivity()).addTarget(target);
         }
         getLoaderManager().restartLoader(0, null, this);
     }
 
     private class CTargetSelectAdapter extends CursorAdapter {
 
 
         public CTargetSelectAdapter(Context context, Cursor c) {
             super(context, c);
         }
 
         @Override
         public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
             return new CTargetView(context);
         }
 
         @Override
         public void bindView(View view, Context context, Cursor cursor) {
             try {
                 ((CTargetView)view).setTarget(new CTarget(cursor));
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (ClassNotFoundException e) {
                 e.printStackTrace();
             };
         }
     }
 
     public static class CTargetCreateDialog extends DialogFragment implements View.OnClickListener {
 
         float prevDistanceFromCenter;
         float distanceFromCenter;
         CTarget target;
         CTargetView targetPreview;
 
         public static CTargetCreateDialog newInstance() {
             CTargetCreateDialog dialog = new CTargetCreateDialog();
             return dialog;
         }
 
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.target_create_dialog, null);
             if (savedInstanceState==null)
                 target = new CTarget("",new Vector<CRing>(),0);
             else
             {
                 distanceFromCenter = savedInstanceState.getFloat("distanceFromCenter");
                 target = (CTarget) savedInstanceState.getSerializable("target");
             }
             targetPreview = ((CTargetView)view.findViewById(R.id.targetPreview));
             targetPreview.setTarget(target);
             (view.findViewById(R.id.addRing)).setOnClickListener(this);
             (view.findViewById(R.id.deleteRing)).setOnClickListener(this);
            return view;
         }
 
         public void saveRing(float ringWidth, int points, int color)    {
             prevDistanceFromCenter = distanceFromCenter;
             distanceFromCenter+=ringWidth;
             target.addRing(new CRing(points,
                     distanceFromCenter,
                     color));
             targetPreview.invalidate();
         }
 
         @Override
        public void onDismiss(DialogInterface dialog) {
            ((StartActivity)getActivity()).saveTarget(target);
            super.onDismiss(dialog);
        }

        @Override
         public void onSaveInstanceState(Bundle state)   {
             state.putFloat("distanceFromCenter",distanceFromCenter);
             state.putSerializable("target",target);
             super.onSaveInstanceState(state);
         }
 
         @Override
         public void onClick(View view) {
             switch (view.getId())   {
                 case R.id.addRing:
                 {
                     RingAddDialog.newInstance().show(getFragmentManager().beginTransaction(), "ringAddDialog");
                     break;
                 }
                 case R.id.deleteRing:
                 {
                     distanceFromCenter = prevDistanceFromCenter;
                     target.removeRing();
                     targetPreview.invalidate();
                     break;
                 }
             }
         }
     }
 
     private static class RingAddDialog extends DialogFragment    {
         public static RingAddDialog newInstance()  {
             return new RingAddDialog();
         }
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState){
             return new AlertDialog.Builder(getActivity())
                 .setTitle("Новое кольцо:")
                 .setView(getActivity().getLayoutInflater().inflate(R.layout.ring_add_dialog, null))
                 .setPositiveButton("Создать", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         AlertDialog alertDialog = (AlertDialog) dialog;
                         float ringWidth = 0;
                         try {
                         ringWidth = Float.parseFloat(((EditText) alertDialog.findViewById(R.id.distanceFromCenter)).getText().toString());
                         }   catch (NumberFormatException e)   {}
                         int points = 0;
                         try {
                             points = Integer.parseInt(((EditText) alertDialog.findViewById(R.id.points)).getText().toString());
                         } catch (NumberFormatException e) {}
                         int color = ((CColorSelectView)((AlertDialog) dialog).findViewById(R.id.ringColor)).getSelectedColor();
                         ((StartActivity)getActivity()).saveRing(
                                                        ringWidth,
                                                        points,
                                                        color);
                 }
                 })
                 .setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         dismiss();
                     }
                 })
                 .create();
         }
     }
 }
