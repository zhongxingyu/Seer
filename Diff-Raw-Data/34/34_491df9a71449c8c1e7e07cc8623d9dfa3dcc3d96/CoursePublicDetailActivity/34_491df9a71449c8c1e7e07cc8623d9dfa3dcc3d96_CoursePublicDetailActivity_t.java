 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.courses;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.TextView;
import com.google.gson.Gson;
 import no.hials.muldvarp.R;
 
 /**
  *
  * @author kristoffer
  */
 public class CoursePublicDetailActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.course_public_detail);
         
         // testdata
        Gson gson = new Gson();
         String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed sed accumsan mi. Phasellus porta semper nisi vel ultrices. Aenean ullamcorper orci vitae elit commodo ut imperdiet nibh pharetra. Etiam massa lorem, tristique vel ullamcorper sed, elementum et elit. Morbi sit amet urna at sapien consectetur tempor eget nec est. Proin rutrum mauris a turpis interdum at interdum augue mattis. Suspendisse ullamcorper pretium neque, et laoreet enim malesuada at. Pellentesque scelerisque, diam ac vehicula vestibulum, orci justo convallis quam, quis condimentum leo libero eget nisi. Morbi non sem arcu. Sed interdum sodales feugiat. Morbi cursus molestie eros, laoreet posuere sapien dictum sed. Maecenas eget volutpat leo. Fusce vel sapien risus, non placerat nisi. Praesent consectetur venenatis leo vel ultricies. Nullam eu dui lacus, sed vehicula dui. Sed pulvinar posuere fermentum. Praesent ullamcorper mollis malesuada.";
        Course c = new Course("Working with Gson", lorem);
        String json = gson.toJson(c);
        
        Course c2 = gson.fromJson(json, Course.class);
         
         TextView name=(TextView)findViewById(R.id.name);
        name.setText(c2.getName());
         
         TextView desc =(TextView)findViewById(R.id.description);
        desc.append(c2.getDetail());
         
         TextView t2 =(TextView)findViewById(R.id.andreting);
        t2.append(c2.getDetail());
     }
 }
