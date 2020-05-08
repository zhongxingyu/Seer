 package jp.mixi.androidstudy01.test.diary;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import jp.mixi.androidstudy01.R;
 import jp.mixi.androidstudy01.diary.DiaryComposeActivity;
 import jp.mixi.androidstudy01.diary.entity.DiaryEntity;
 
 import java.lang.reflect.Field;
 
 /**
  * DiaryComposeActivityの機能テストです。
  *
  * A functionality test for {@link DiaryComposeActivity}.
  * @author keishin.yokomaku
  */
 public class DiaryComposeActivityTest extends ActivityInstrumentationTestCase2<DiaryComposeActivity> {
     public DiaryComposeActivityTest() {
         super(DiaryComposeActivity.class);
     }
 
     /**
      * AndroidのテストフレームワークはJUnit3がベースになっているので、
      * テストケースとなるメソッド名は必ず"test"ではじめなければなりません。
      * Test method name should start with "test".
      * This is because android test framework is based on JUnit3.
      */
     public void testInputTitle() {
         final Activity activity = getActivity();
         activity.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 getInstrumentation().callActivityOnStart(activity);
             }
         });
         getInstrumentation().waitForIdleSync();
 
         final EditText title = (EditText) activity.findViewById(R.id.ComposeDiaryTitleInput);
         final TextView length = (TextView) activity.findViewById(R.id.ComposeDiaryTitleLength);
         assertEquals("", title.getText().toString());
         assertEquals("0 / 20", length.getText().toString());
 
         activity.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 title.setText("test");
             }
         });
         getInstrumentation().waitForIdleSync();
 
         assertEquals("test", title.getText().toString());
         assertEquals("4 / 20", length.getText().toString());
     }
 
     public void testInputBody() {
         final Activity activity = getActivity();
         activity.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 getInstrumentation().callActivityOnStart(activity);
             }
         });
         getInstrumentation().waitForIdleSync();
 
         final EditText body = (EditText) activity.findViewById(R.id.ComposeDiaryBodyInput);
         final TextView length = (TextView) activity.findViewById(R.id.ComposeDiaryBodyLength);
         assertEquals("", body.getText().toString());
         assertEquals("0 / 10000", length.getText().toString());
 
         activity.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 body.setText("test");
             }
         });
         getInstrumentation().waitForIdleSync();
 
         assertEquals("test", body.getText().toString());
         assertEquals("4 / 10000", length.getText().toString());
     }
 
     /*public void testPassIntent() {
         Intent intent = new Intent();
         DiaryEntity entity = DiaryEntity.getBuilder()
                 .setTitle("test")
                 .setBody("testtest")
                 .create();
         intent.putExtra(DiaryComposeActivity.REQUEST_EXTRA_DIARY_ENTITY, entity);
         setActivityIntent(intent);
 
         final Activity activity = getActivity();
         activity.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 getInstrumentation().callActivityOnStart(activity);
             }
         });
         getInstrumentation().waitForIdleSync();
 
         final EditText body = (EditText) activity.findViewById(R.id.ComposeDiaryBodyInput);
         final TextView bodyLength = (TextView) activity.findViewById(R.id.ComposeDiaryBodyLength);
         final EditText title = (EditText) activity.findViewById(R.id.ComposeDiaryTitleInput);
         final TextView titleLength = (TextView) activity.findViewById(R.id.ComposeDiaryTitleLength);
         assertEquals("test", title.getText().toString());
         assertEquals("testtest", body.getText().toString());
         assertEquals("4 / 20", titleLength.getText().toString());
         assertEquals("8 / 10000", bodyLength.getText().toString());
     }*/
 
     public void testSaveClick() throws Exception {
         final Activity activity = getActivity();
         activity.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 getInstrumentation().callActivityOnStart(activity);
             }
         });
         getInstrumentation().waitForIdleSync();
 
         final Button save = (Button) activity.findViewById(R.id.ComposeDiarySave);
         // Controlling ui is not allowed from outside ui thread,
         // so pass the runnable object and run it on ui thread.
         activity.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 // Click the button on ui thread
                 assertTrue(save.performClick());
             }
         });
         getInstrumentation().waitForIdleSync();
 
         // Activity has been finished.
         assertTrue(activity.isFinishing());
 
         try {
             // Obtain activity result data with reflection! Too complexed!
             Field f = Activity.class.getDeclaredField("mResultCode");
             f.setAccessible(true);
             int actualResultCode = (Integer)f.get(getActivity());
             // OK result should be returned
             assertEquals(Activity.RESULT_OK, actualResultCode);
             f = Activity.class.getDeclaredField("mResultData");
             f.setAccessible(true);
             Intent intent = (Intent) f.get(getActivity());
             DiaryEntity entity = intent.<DiaryEntity>getParcelableExtra(DiaryComposeActivity.RESULT_EXTRA_DIARY_ENTITY);
             assertNotNull(entity);
         } catch (NoSuchFieldException e) {
             throw new RuntimeException("Looks like the Android Activity class has changed it's   private fields for mResultCode or mResultData.  Time to update the reflection code.", e);
         }
     }
 
     public void testCancelClick() throws Exception {
         final Activity activity = getActivity();
         activity.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 getInstrumentation().callActivityOnStart(activity);
             }
         });
         getInstrumentation().waitForIdleSync();
 
         final Button cancel = (Button) activity.findViewById(R.id.ComposeDiaryCancel);
         // Controlling ui is not allowed from outside ui thread,
         // so pass the runnable object and run it on ui thread.
         activity.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 // Click the button on ui thread
                 assertTrue(cancel.performClick());
             }
         });
         getInstrumentation().waitForIdleSync();
 
         assertTrue(activity.isFinishing());
 
         try {
             // Obtain activity result data with reflection! Too complexed!
             Field f = Activity.class.getDeclaredField("mResultCode");
             f.setAccessible(true);
             int actualResultCode = (Integer)f.get(getActivity());
             // Canceled result should be returned
             assertEquals(Activity.RESULT_CANCELED, actualResultCode);
         } catch (NoSuchFieldException e) {
             throw new RuntimeException("Looks like the Android Activity class has changed it's   private fields for mResultCode or mResultData.  Time to update the reflection code.", e);
           }
     }
 
     /**
      * Test all functionality(Start activity, input some text, save them, and finish the activity with result).
      * @throws Exception
      */
     public void testComposeDiary() throws Exception {
         final Activity activity = getActivity();
         activity.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 getInstrumentation().callActivityOnStart(activity);
             }
         });
         getInstrumentation().waitForIdleSync();
 
         final EditText body = (EditText) activity.findViewById(R.id.ComposeDiaryBodyInput);
         final TextView bodyLength = (TextView) activity.findViewById(R.id.ComposeDiaryBodyLength);
         final EditText title = (EditText) activity.findViewById(R.id.ComposeDiaryTitleInput);
         final TextView titleLength = (TextView) activity.findViewById(R.id.ComposeDiaryTitleLength);
 
         activity.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 title.setText("test");
                 body.setText("testtest");
             }
         });
         getInstrumentation().waitForIdleSync();
 
         assertEquals("test", title.getText().toString());
         assertEquals("testtest", body.getText().toString());
         assertEquals("4 / 20", titleLength.getText().toString());
         assertEquals("8 / 10000", bodyLength.getText().toString());
 
         final Button save = (Button) activity.findViewById(R.id.ComposeDiarySave);
         // Controlling ui is not allowed from outside ui thread,
         // so pass the runnable object and run it on ui thread.
         activity.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 // Click the button on ui thread
                 assertTrue(save.performClick());
             }
         });
         getInstrumentation().waitForIdleSync();
 
         // Activity has been finished.
         assertTrue(activity.isFinishing());
 
         try {
             // Obtain activity result data with reflection! Too complexed!
             Field f = Activity.class.getDeclaredField("mResultCode");
             f.setAccessible(true);
             int actualResultCode = (Integer)f.get(getActivity());
             // OK result should be returned
             assertEquals(Activity.RESULT_OK, actualResultCode);
             f = Activity.class.getDeclaredField("mResultData");
             f.setAccessible(true);
             Intent intent = (Intent) f.get(getActivity());
             DiaryEntity entity = intent.<DiaryEntity>getParcelableExtra(DiaryComposeActivity.RESULT_EXTRA_DIARY_ENTITY);
             assertNotNull(entity);
             assertEquals("test", entity.getTitle());
             assertEquals("testtest", entity.getBody());
         } catch (NoSuchFieldException e) {
             throw new RuntimeException("Looks like the Android Activity class has changed it's   private fields for mResultCode or mResultData.  Time to update the reflection code.", e);
         }
     }
 }
