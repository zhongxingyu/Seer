 package lv.jug.javaday.androidapp.application;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import dagger.Module;
 import lv.jug.javaday.androidapp.BaseRobolectricTest;
 import lv.jug.javaday.androidapp.R;
 import lv.jug.javaday.androidapp.infrastructure.dagger.DaggerModule;
 import org.junit.Test;
 
 import javax.inject.Inject;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.junit.Assert.assertThat;
 
 public class DrawableServiceRoboTest extends BaseRobolectricTest {
 
     @Inject
     Context context;
 
     @Inject
     DrawableService service;
 
     @Test
     public void shouldFindPortraitDrawable() {
        Drawable expected = context.getResources().getDrawable(R.drawable.ch);
        Drawable actual = service.loadDrawable("ch");
 
         assertThat(actual.toString(), equalTo(expected.toString()));
     }
 
     @Override
     public DaggerModule getModule() {
         return new TestModule();
     }
 
     @Module(includes = BaseTestModule.class, injects = DrawableServiceRoboTest.class)
     static class TestModule implements DaggerModule {}
 }
