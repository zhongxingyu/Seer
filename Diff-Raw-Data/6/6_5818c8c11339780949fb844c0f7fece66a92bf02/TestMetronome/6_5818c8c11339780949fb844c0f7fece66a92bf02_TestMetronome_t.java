 package kiv.janecekz.ma.test;
 
 import kiv.janecekz.ma.MainActivity;
 import kiv.janecekz.ma.MetronomeFragment;
 import kiv.janecekz.ma.R;
 import kiv.janecekz.ma.metronome.Operator;
 import kiv.janecekz.ma.metronome.Peeper;
 import kiv.janecekz.ma.metronome.TempoControl;
 import kiv.janecekz.ma.prefs.SharedPref;
 import android.app.Activity;
 import android.app.Fragment;
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.ImageView;
 import android.widget.NumberPicker;
 
 public class TestMetronome extends
         ActivityInstrumentationTestCase2<MainActivity> {
     Activity mActivity;
 
     public TestMetronome() {
         super(MainActivity.class);
     }
 
     protected void setUp() throws Exception {
         super.setUp();
 
         mActivity = getActivity();
 
         MetronomeFragment mf = new MetronomeFragment();
         mActivity.getFragmentManager().beginTransaction()
                .replace(R.id.container, mf).commit();
     }
 
     public void testRunContainer() {
         Fragment f = mActivity.getFragmentManager().findFragmentById(
                 R.id.container);
 
         assertNotNull(f);
     }
 
     public void testMetronome() {
         MetronomeFragment mf = (MetronomeFragment) mActivity
                 .getFragmentManager().findFragmentById(R.id.container);
 
         NumberPicker bpm = (NumberPicker) mActivity.findViewById(R.id.bpmCount);
 
         Peeper peeper = new Peeper();
         peeper.setSun((ImageView) mActivity.findViewById(R.id.sun));
         peeper.setTime(SharedPref.getTime(getActivity()));
        Operator op = new Operator(peeper);
 
         TempoControl tc = new TempoControl();
         tc.addObserver(mf);
         tc.addObserver(op);
         tc.setBPM(SharedPref.getBPM(mActivity));
         tc.refreshObservers();
 
         assertEquals(tc.getBPM(), op.getBpm());
         assertEquals(tc.getBPM(), bpm.getValue());
         
         tc.setBPM(20);
         
         assertEquals(tc.getBPM(), op.getBpm());
         assertEquals(tc.getBPM(), bpm.getValue());
         
         getInstrumentation().callActivityOnPause(mActivity);
         getInstrumentation().callActivityOnResume(mActivity);
         
         int val = SharedPref.getBPM(mActivity);
         assertEquals(val, tc.getBPM());
         assertEquals(val, tc.getBPM());
     }
 }
