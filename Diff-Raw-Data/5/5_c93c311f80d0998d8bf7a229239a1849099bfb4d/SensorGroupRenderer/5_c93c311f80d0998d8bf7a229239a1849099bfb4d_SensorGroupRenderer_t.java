 package nl.sense_os.commonsense.client.sensors.library;
 
 import nl.sense_os.commonsense.shared.models.SensorModel;
 
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
 import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
 
 /**
  * Renderer for groups of sensors.
  */
 public class SensorGroupRenderer implements GridGroupRenderer {
 
    @SuppressWarnings("unused")
    private static final String TAG = "SensorGroupRenderer";
     private final ColumnModel cm;
 
     public SensorGroupRenderer(ColumnModel cm) {
         this.cm = cm;
     }
 
     @Override
     public String render(GroupColumnData data) {
 
         String field = this.cm.getColumnById(data.field).getHeader();
         String count = data.models.size() + ((data.models.size() == 1) ? " sensor" : " sensors");
 
         String group = data.group;
         if (data.field.equals(SensorModel.TYPE)) {
             int type = Integer.parseInt(data.group);
             switch (type) {
             case 0:
                 group = "Feeds";
                 break;
             case 1:
                 group = "Physical";
                 break;
             case 2:
                 group = "States";
                 break;
             case 3:
                 group = "Environment sensors";
                 break;
             case 4:
                 group = "Public sensors";
                 break;
             default:
                 group = "Unsorted";
             }
 
         } else if (group == null || group.equals("")) {
            return "No " + field + " (" + count + ")";
 
         }
 
         return field + ": " + group + " (" + count + ")";
     }
 }
