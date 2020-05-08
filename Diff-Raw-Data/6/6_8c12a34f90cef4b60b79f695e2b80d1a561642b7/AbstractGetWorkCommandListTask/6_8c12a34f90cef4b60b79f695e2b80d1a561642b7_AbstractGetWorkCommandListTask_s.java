 package com.jinheyu.lite_mms;
 
 import android.os.AsyncTask;
 import com.jinheyu.lite_mms.data_structures.WorkCommand;
 import com.jinheyu.lite_mms.netutils.BadRequest;
 import org.json.JSONException;
 
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Created by abc549825@163.com
  * 2013-09-12
  */
 public abstract class AbstractGetWorkCommandListTask extends AsyncTask<Void, Void, List<WorkCommand>> {
     Exception ex;
     WorkCommandListFragment mFragment;
 
     public AbstractGetWorkCommandListTask(WorkCommandListFragment listFragment) {
         mFragment = listFragment;
     }
 
     /**
      * Override this method to perform a computation on a background thread. The
      * specified parameters are the parameters passed to {@link #execute}
      * by the caller of this task.
      * <p/>
      * This method can call {@link #publishProgress} to publish updates
      * on the UI thread.
      *
      * @param params The parameters of the task.
      * @return A result, defined by the subclass of this task.
      * @see #onPreExecute()
      * @see #onPostExecute
      * @see #publishProgress
      */
     @Override
     protected List<WorkCommand> doInBackground(Void... params) {
         try {
             return getWorkCommandList();
         } catch (IOException e) {
             e.printStackTrace();
             ex = e;
         } catch (JSONException e) {
             e.printStackTrace();
             ex = e;
         } catch (BadRequest badRequest) {
             badRequest.printStackTrace();
             ex = badRequest;
         }
         return null;
     }
 
     protected abstract List<WorkCommand> getWorkCommandList() throws IOException, JSONException, BadRequest;
 
     /**
      * <p>Runs on the UI thread after {@link #doInBackground}. The
      * specified result is the value returned by {@link #doInBackground}.</p>
      * <p/>
      * <p>This method won't be invoked if the task was cancelled.</p>
      *
      * @param workCommandList The result of the operation computed by {@link #doInBackground}.
      * @see #onPreExecute
      * @see #doInBackground
      * @see #onCancelled(Object)
      */
     @Override
     protected void onPostExecute(List<WorkCommand> workCommandList) {
         if (ex != null) {
             Utils.displayError(mFragment.getActivity(), ex);
             return;
         }
         doUpdateView(workCommandList);
     }
 
     private void doUpdateView(List<WorkCommand> workCommandList) {
         mFragment.setListAdapter(new WorkCommandListAdapter(mFragment, workCommandList));
         mFragment.dismissProcessDialog();
         mFragment.setRefreshComplete();
     }
 }
