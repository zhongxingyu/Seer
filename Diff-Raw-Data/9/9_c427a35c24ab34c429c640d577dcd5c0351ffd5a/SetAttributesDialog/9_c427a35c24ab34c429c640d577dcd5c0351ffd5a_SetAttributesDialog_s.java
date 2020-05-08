 /**
  * 
  */
 package cz.kalcik.vojta.terraingis.dialogs;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import cz.kalcik.vojta.terraingis.MainActivity;
 import cz.kalcik.vojta.terraingis.R;
 import cz.kalcik.vojta.terraingis.layer.AttributeHeader;
 import cz.kalcik.vojta.terraingis.layer.AttributeHeader.Column;
 import cz.kalcik.vojta.terraingis.layer.AttributeRecord;
 import cz.kalcik.vojta.terraingis.layer.AttributeType;
 import cz.kalcik.vojta.terraingis.layer.VectorLayer;
 import cz.kalcik.vojta.terraingis.view.AttributeValueLayout;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.widget.LinearLayout;
 
 /**
  * @author jules
  *
  */
 public class SetAttributesDialog extends DialogFragment
 {
     // constants =====================================================================================
     private String DATETIME_NAME = "datetime";
     private AttributeType DATETIME_TYPE = AttributeType.TEXT;
     // attributes ====================================================================================
     private MainActivity mMainActivity;
     private LinearLayout mMainLayout;
     private ArrayList<AttributeValueLayout> mAttributes = new ArrayList<AttributeValueLayout>();
     private VectorLayer mLayer;
     
     // public methods ================================================================================
     
     // on methods ====================================================================================
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState)
     {
          mMainActivity = (MainActivity)getActivity();
         
          Builder dialogBuilder = new AlertDialog.Builder(mMainActivity);
          
          dialogBuilder.setTitle(R.string.set_attributes_message);
          
          mMainLayout = new LinearLayout(mMainActivity);
          mMainLayout.setOrientation(LinearLayout.VERTICAL);
          
         dialogBuilder.setView(mMainLayout);
          loadAttributes();
          
          dialogBuilder.setPositiveButton(R.string.positive_button, positiveHandler);
          dialogBuilder.setNegativeButton(R.string.negative_button, null);
          
          return dialogBuilder.create();
     }
     
     // getter setter ==================================================================================
     /**
      * @param mLayer the mLayer to set
      */
     public void setLayer(VectorLayer layer)
     {
         this.mLayer = layer;
     }
 
     // private methods ================================================================================
     /**
      * attributes of selected layer
      */
     private void loadAttributes()
     {
         AttributeHeader attributeHeader = mLayer.getAttributeHeader();
         LayoutInflater inflater = mMainActivity.getLayoutInflater();
         
         for(Column column: attributeHeader.getColumns())
         {
             if(!column.isPK)
             {
                 AttributeValueLayout item = (AttributeValueLayout)inflater.inflate(R.layout.set_attribute_value_item, null);
                 mAttributes.add(item);
                 item.setName(column.name);
                 item.setInputType(column.type);
                 // datetime
                 if(column.name.equals(DATETIME_NAME) && column.type == DATETIME_TYPE)
                 {
                     DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                     item.setValue(df.format(new Date()));
                 }
                 mMainLayout.addView(item);
             }
         }
     }
     
     // handlers =======================================================================================
     
     /**
      * positive button
      */
     DialogInterface.OnClickListener positiveHandler = new DialogInterface.OnClickListener()
     {
         @Override
         public void onClick(DialogInterface dialog, int id)
         {
             AttributeHeader attributeHeader = mLayer.getAttributeHeader();
             String[] values = new String[attributeHeader.getCountColumns()];
             
             int indexAllColumns = 0;
             int indexNoPKColumns = 0;
             
             for(Column column: attributeHeader.getColumns())
             {
                 if(column.isPK)
                 {
                     values[indexAllColumns] = null;
                 }
                 else
                 {
                     values[indexAllColumns] = mAttributes.get(indexNoPKColumns).getValue();
                     indexNoPKColumns++;
                 }
                 indexAllColumns++;
             }
             
             mLayer.endObject(new AttributeRecord(attributeHeader, values));
         }        
     };
 }
