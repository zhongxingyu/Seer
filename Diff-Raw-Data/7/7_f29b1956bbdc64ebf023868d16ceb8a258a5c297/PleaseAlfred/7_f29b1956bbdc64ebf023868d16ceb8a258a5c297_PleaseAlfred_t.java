 package esir.dom12.nsoc.android.pc;
 
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 import org.kevoree.android.framework.helper.UIServiceHandler;
 import org.kevoree.android.framework.service.KevoreeAndroidService;
 
 import org.kevoree.annotation.*;
 import org.kevoree.framework.AbstractComponentType;
 import org.kevoree.framework.MessagePort;
 
 
 @Requires({
         @RequiredPort(name = "scenario", type = PortType.MESSAGE, needCheckDependency = false , optional = true)
 })
 
 /*@Provides({
         @ProvidedPort(name = "trombi", type = PortType.MESSAGE)
 }) */
 
 @ComponentType
 @Library(name = "Android")
 
 public class PleaseAlfred extends AbstractComponentType {
 
     private KevoreeAndroidService uiService = null;
     private ImageView view = null;
     private Boolean on;
     private Button buttonScenario;
 
     private LinearLayout layout = null;
 
     @Start
     public void start () {
         uiService = UIServiceHandler.getUIService();
 
         //uiService = UIServiceHandler.getUIService((Bundle) this.getDictionary().get("osgi.bundle"));
         layout = new LinearLayout(uiService.getRootActivity());
         layout.setOrientation(LinearLayout.VERTICAL);
 
         Button buttonScenario = new Button(uiService.getRootActivity());
         buttonScenario.setText("Scenario");
         buttonScenario.setOnClickListener(new View.OnClickListener() {
             public void onClick (View v) {
                 sdmsg("");
 
             }
         });
         layout.addView(buttonScenario)    ;
 
     }
 
     @Stop
     public void stop () {
         uiService.remove(layout);
     }
 
 
     @Update
     public void update() {
         //stop();
         //start();
     }
 
     /*@Port(name = "trombi")
     public void tromb (Object message) {
         uiService.getRootActivity().runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 Toast.makeText(uiService.getRootActivity(), "Light on!", Toast.LENGTH_SHORT).show();
             }
         });
     }  */
 
     public void sdmsg(String selecButton){
 
         if (isPortBinded("Scenario")){
 
            MessagePort sdmsgButton = getPortByName("Scenario", MessagePort.class) ;
            sdmsgButton.process(selecButton);
 
        }
     }
 }
 
 
 
