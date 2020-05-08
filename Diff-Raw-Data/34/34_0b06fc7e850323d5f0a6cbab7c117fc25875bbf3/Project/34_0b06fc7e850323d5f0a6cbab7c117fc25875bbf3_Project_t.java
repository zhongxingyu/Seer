 package cyberlink.dogtim.horizontalview;
 
 import android.util.Log;
 import android.util.SparseArray;
 
 /**
  * Project class record all editing items here,<p>
  * For production and providing get function make client could query requirement resource<p>
  * 
  */
 public class Project {
 
     private static final String TAG = Project.class.getSimpleName();
     private SparseArray<Item> mEditingItem = null;
     private static Project mInstance = null;
     private static int referenceCount = 0;
     
     private final int DEFAULT_PROJECT_DURATION = 60;
     private final int DEFAULT_ITEM_DURATION = 5;
     
     private Project() {
         Log.v(TAG, "get instance");
         mEditingItem = new SparseArray<Item>();
     }
 
     public synchronized static Project get() {
         if (mInstance == null) {
         	mInstance = new Project();
         }
 
         referenceCount += 1;
 
         return mInstance;
     }
 
     public synchronized void release() {
         referenceCount -= 1;
 
         if (referenceCount > 0) return;
         if (referenceCount < 0) {
             Log.e(TAG, "Error, plz check reference count");
         }
         Log.v(TAG, "Destroy instance");
         referenceCount = 0; // reset count to avoid mess up counter.
         mInstance = null;
         
         mEditingItem.clear();
         mEditingItem = null;
         
     }
     
     public void addEditingItem(int id, Item item){
         mEditingItem.put(id, item);
     }
 
    public void removeEditingItem(int id){
        mEditingItem.remove(id);
    }
    
     public int getDuration(){
         if(mEditingItem == null){ // default give 60s
             return DEFAULT_PROJECT_DURATION;
         }
         int duration = mEditingItem.size() * DEFAULT_ITEM_DURATION;
         
         if (duration < DEFAULT_PROJECT_DURATION){
             return DEFAULT_PROJECT_DURATION;
         }
         
         return duration;
     }
 }
