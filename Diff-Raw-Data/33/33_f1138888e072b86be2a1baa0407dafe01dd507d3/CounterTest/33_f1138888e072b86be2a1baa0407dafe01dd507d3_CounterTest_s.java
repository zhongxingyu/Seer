 package no.bouvet.kpro.signal;
 
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
import no.bouvet.kpro.gui.lyric.LyricWidget;
import no.bouvet.kpro.gui.lyric.ButtonWidget;
import com.trolltech.extensions.signalhandler.QSignalHandler1;
import com.trolltech.qt.gui.QMainWindow;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.QSignalEmitter;

 
 public class CounterTest {
 
     @Test
     public void testCounter() {
         Counter a, b;
         a = new Counter();
         b = new Counter();
         a.valueChanged.connect(b, "setValue(int)");
        a.setValue(12);     // a.value() == 12, b.value() == 12
         assertEquals(12, a.getValue());
         assertEquals(12, b.getValue());
        b.setValue(48);     // a.value() == 12, b.value() == 48
         assertEquals(12, a.getValue());
         assertEquals(48, b.getValue());
     }

    @Test
    public void testLyricWidget() {
        String[] args = new String[0];
        QApplication.initialize(args);
        LyricWidget lyricWidget = new LyricWidget();
        ButtonWidget buttonWidget  = new ButtonWidget();


        
        //buttonWidget.text.connect(lyricWidget, "setText(String)");
        /*
        buttonWidget.text.connect(lyricWidget, "setText(String)");
        buttonWidget.fire();
        buttonWidget.fire();
        buttonWidget.fire();
        buttonWidget.fire();
          */
    }
 }
 
