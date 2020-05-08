 package mse.tsm.mobop.starshooter;
 
 
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.R.bool;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class StarshooterMain extends Activity
 {
 
   ListView list;
 
   MenuAdapter adapter;
   
   final int DIALOG_SLAVE_PROMPT4MASTER_IP = 1;
 
   private ArrayList<ListObject> listItems;
 
   private String[] mmenuItems;
   
   
   /** `prompt 4 master ip'-dialog is in ready state, prepared for user input **/
   private final short PROMPT4MASTERIP_STATE_READY = 0;
   
   /** `prompt 4 master ip'-dialog is in waiting state, connection to master is in progress **/
   private final short PROMPT4MASTERIP_STATE_WAITING = 1;
   
   /** current state of `prompt 4 master ip'-dialog **/
   private short prompt4masterIp_state = PROMPT4MASTERIP_STATE_READY;
   
   private Playground pg;
   
   // Connection settings
   /** defines if we are master **/
   private boolean con_master=true;
   
   /** masters ip address **/
   private Integer [] con_masterip;
   
     
 
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
     
     listItems = new ArrayList<ListObject>();
     mmenuItems = new String[3];
     // fill up menu
       // connect
     ListObject lo0 = new ListObject(getResources().getString(R.string.main_menu_connect), "");
     listItems.add(lo0);
       // settings
     listItems.add(new ListObject(getResources().getString(R.string.main_menu_settings), ""));
       // about
     StringBuilder sb = new StringBuilder();
     sb.append(getResources().getString(R.string.app_name));
     sb.append(" ");
     try
     {
       sb.append( getPackageManager().getPackageInfo(getPackageName(), 0).versionName );
     } catch (NameNotFoundException e)
     {
       sb.append("unknw");
     }
     
     listItems.add(new ListObject(getResources().getString(R.string.main_menu_about), sb.toString()));
 
     list = (ListView) findViewById(R.id.mainmenu);
     adapter = new MenuAdapter(this, mmenuItems, listItems);
     list.setAdapter(adapter);
     
     list.setOnItemClickListener(new ListView.OnItemClickListener() {
       public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         //Toast.makeText(getApplicationContext(),"("+Integer.toString(position)+") selected", Toast.LENGTH_LONG).show();
         
         switch(position)
         {
           // try to connect
           case 0 :
             dialog_selectMasterSlave();
             break;
           
           // settings
           case 1 :
             
             break;
             
           // About
           case 2 :
             
             break;
             
           // quit button is obsolet
         }
 
         
       }
     });
     
   }
 
   public void dialog_selectMasterSlave()
   {
     final CharSequence[] items = { 
       getResources().getString(R.string.select_masterSlave_master), 
       getResources().getString(R.string.select_masterSlave_slave)
     };
 
     AlertDialog.Builder builder = new AlertDialog.Builder(this);
     builder.setTitle(getResources().getString(R.string.select_masterSlave_title));
     
     builder.setItems(items, new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int selectedItem) {
           
           switch( selectedItem )
           {
             // master
             case 0 :
               /*Intent intent = new Intent();
               intent.setComponent(new ComponentName("Playground", "mse.tsm.mobop.starshooter"));
               startActivity(intent);/*
                pg = new Playground();
                pg.startActivity(getIntent());*/
                 Intent i = new Intent(StarshooterMain.this, Playground.class);
                 StarshooterMain.this.startActivity(i);
               break;
               
             // slave
             case 1 :
               showDialog(DIALOG_SLAVE_PROMPT4MASTER_IP);
               break;
           }
         }
     });
     AlertDialog alert = builder.create();
     alert.show();
   }
     
   protected Dialog onCreateDialog(int id) {
     Dialog dialog;
     switch(id)
     {
       case DIALOG_SLAVE_PROMPT4MASTER_IP:
         dialog = new Dialog(this);
   
         dialog.setContentView(R.layout.request_ip_prompt);
         dialog.setTitle( getResources().getString(R.string.prompt4ip_title) );
         
         final AutoCompleteTextView input = (AutoCompleteTextView) dialog.findViewById(R.id.ripp_input);
         final Button button = (Button) dialog.findViewById(R.id.ripp_button);
         final ProgressBar pb= (ProgressBar) dialog.findViewById(R.id.ripp_connectionProgress);
         
         dialog.setOnKeyListener(new DialogInterface.OnKeyListener()
         {
           
          @Override
           public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
           {
             if(event.getAction() == KeyEvent.ACTION_DOWN)
             {
               if(keyCode == KeyEvent.KEYCODE_BACK)
               {
                 pb.setVisibility(View.INVISIBLE);
                 input.setEnabled(true);
                 input.requestFocus();
                 button.setText(R.string.prompt4ip_continue);
                 prompt4masterIp_state = PROMPT4MASTERIP_STATE_READY;
                 return false;
               }
             }
             return false;
           }
         });
         
         input.setOnKeyListener(new OnKeyListener()
         {
           
          @Override
           public boolean onKey(View v, int keyCode, KeyEvent event)
           {
             if(event.getAction() == KeyEvent.ACTION_DOWN)
             {
               if(keyCode == KeyEvent.KEYCODE_ENTER)
               {
                 button.performClick();
                 return true;
               }/*else if(keyCode == KeyEvent.KEYCODE_BACK)
               {
                 Toast.makeText(getApplicationContext(),"here", Toast.LENGTH_SHORT).show();
                 
                 pb.setVisibility(View.INVISIBLE);
                 input.setEnabled(true);
                 input.requestFocus();
                 button.setText(R.string.prompt4ip_continue);
                 prompt4masterIp_state = PROMPT4MASTERIP_STATE_READY;
                 return false;
               }*/
             }
             return false;
           }
         });
         
         button.setOnClickListener(new OnClickListener()
         {
          
          @Override
           public void onClick(View arg0)
           {
             switch( prompt4masterIp_state )
             {
               case PROMPT4MASTERIP_STATE_READY:
                 IPchecker ipc = new IPchecker(input.getText().toString());
                 if( ipc.isValid() )
                 {
                   con_masterip=ipc.getDigits();
                   pb.setVisibility(View.VISIBLE);
                   input.setEnabled(false);
                   button.setText(R.string.prompt4ip_abort);
                   button.requestFocus();
                   prompt4masterIp_state = PROMPT4MASTERIP_STATE_WAITING;
                   /// TODO: Start trying to connect, then start finish(); and close dialog, make sure dialog is as inited
                   
                 }
                 else
                 {
                   Toast.makeText(getApplicationContext(), getResources().getString(R.string.prompt4ip_errInvalid).toString(), Toast.LENGTH_SHORT).show();
                 }
                 break;
               case PROMPT4MASTERIP_STATE_WAITING :
                 pb.setVisibility(View.INVISIBLE);
                 input.setEnabled(true);
                 input.requestFocus();
                 button.setText(R.string.prompt4ip_continue);
                 prompt4masterIp_state = PROMPT4MASTERIP_STATE_READY;
                 /// TODO: aborting `trying-to-connect-to-master'-thread
                 break;
             }
             
           }
         });
         
         showDialog(dialog.getVolumeControlStream());
         break;
       default:
           dialog = null;
     }
     return dialog;
   }
   
 
   @Override
   public void onDestroy()
   {
     list.setAdapter(null);
     super.onDestroy();
   }
 
 }
 
 class IPchecker{
 
   private final short VALID_NOT_EVALUATED = 0;
   private final short VALID_EVALUATED_INVALID = 1;
   private final short VALID_EVALUATED_VALID = 2;
   
   private Integer digits[]=new Integer[4];
   private short valid = VALID_NOT_EVALUATED;
   private String input;
   
   public IPchecker(String ip)
   {
     input = ip;
     digits[0]=127;
     digits[1]=0;
     digits[2]=0;
     digits[3]=1;
   }
   
   private void validate()
   {
     Pattern p = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");
     Matcher m = p.matcher(input);
     
     if( m.find() )
     {
       for(int i=1;i<=4;i++)
       {
         int dig=Integer.parseInt(m.group(i).toString());
         if( dig<0 || dig>255 )
         {
           valid=VALID_EVALUATED_INVALID;
           return;
         }
       }
 
       for(int i=1;i<=4;i++)
         digits[i-1]=Integer.parseInt(m.group(i).toString());
       valid=VALID_EVALUATED_VALID;
     }
   }
   
   public boolean isValid()
   {
     if( valid == VALID_NOT_EVALUATED )
       validate();
     
     if( valid == VALID_EVALUATED_VALID )
       return true;
     return false;
   }
   
   public Integer [] getDigits()
   {
     return digits;
   }
 }
