 package com.jp.miaulavirtual;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ActivityNotFoundException;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Path;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.webkit.MimeTypeMap;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class FileManager extends Activity {
 	private ListView lstDocs2;
 	private ListManagerAdapter lstAdapter2;
 	private String homePath;
 	private Boolean isHomePath;
 	private File[] sdData;
 	private Activity mycontext;
 	private Boolean status = true;
 	private int i = 1;
 	
 	public Object onRetainNonConfigurationInstance() {
 		Object[] passData = new Object[2];
 		passData[0] = sdData;
 		passData[1] = isHomePath;
 		return passData;
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_file_manager);
 		
 		mycontext = this;
 		Log.d("Lifecycle4", "In the onCreate()");
 
 		Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
 		if(!isSDPresent) {
 			lstAdapter2 = null;
 	        lstDocs2 = null;
 	        
 			// No SD present
 			TextView tv = (TextView) findViewById(R.id.manager_error);
 	        tv.setText(getString(R.string.handle_1));
 	        tv.setVisibility(0);
 		} else {
 			String root = Environment.getExternalStorageDirectory().toString();
 			homePath = root + "/Android/data/com.jp.miaulavirtual/files";
 			
 			// Get the onRetainNonConfigurationInstance()
 	        final Object[] passData = (Object[]) getLastNonConfigurationInstance();
 	        if(passData != null) { 
 		        	sdData = (File[]) passData[0];
 		        	isHomePath = (Boolean) passData[1];
 		        	if(sdData!= null && sdData.length==0) sdData = null;
 		        if(sdData!=null && status) {	
 		        	// our subtitle
 		    		setSubTitle();
 		        	// our listView
 		        	setData(true);
 			        startListener();
 	        	} else {
 	        		lstAdapter2 = null;
 			        lstDocs2 = null;
 	        		// None
 					TextView tv = (TextView) findViewById(R.id.manager_error);
 			        tv.setText(getString(R.string.handle_2));
 			        tv.setVisibility(0);
 	        	}
 	        } else {
 				File myDir = new File(homePath);
 				sdData = listFiles(myDir, true);
 				if(sdData!= null && sdData.length==0) sdData = null;
 				if(sdData!=null && status) {
 					isHomePath = isHomePath(sdData[0].getParent());
 					
 					// our subtitle
 		    		setSubTitle();
 		            // our listView
 			        setData(true);
 			        startListener();
 				} else {
 					lstAdapter2 = null;
 			        lstDocs2 = null;
 					// None
 					TextView tv = (TextView) findViewById(R.id.manager_error);
 			        tv.setText(getString(R.string.handle_2));
 			        tv.setVisibility(0);
 				}
 	        }
 		}
 	}
 	
 	public File[] listFiles(File path, Boolean filter) {
 		if(filter) {
 			FileFilter filterDirectoriesOnly = new FileFilter() {
 			    public boolean accept(File file) {
 			        return file.isDirectory();
 			    }
 			};
 			return path.listFiles(filterDirectoriesOnly);
 		}
 		return path.listFiles();
 	}
 	
 	public Boolean isHomePath(String path) {
 		Log.d("Path", path);
 		Log.d("Path", homePath);
 		Log.d("Path", String.valueOf(path.equalsIgnoreCase(homePath)));
 		return path.equalsIgnoreCase(homePath);
 	}
 	
 	public void setData() {
         lstAdapter2.setIsHomePath(isHomePath);
     	lstAdapter2.setData(sdData);
         lstAdapter2.notifyDataSetChanged();
 	}
 	
 	public void setData(Boolean type) {
 		lstDocs2 = (ListView)findViewById(R.id.LstDocs2); // Declaramos la lista
         lstAdapter2 = new ListManagerAdapter(mycontext, sdData, isHomePath);
         lstDocs2.setAdapter(lstAdapter2); // Declaramos nuestra propia clase adaptador como adaptador
 	}
 	
 	public void setSubTitle() {
 		String s = "";
 		TextView headerTitle = (TextView) findViewById(R.id.LblSubTitulo2); // Título Header
 		headerTitle.setTextColor(getResources().getColor(R.color.list_title));
         headerTitle.setTypeface(null, 1);
         
         if(!isHomePath) s = " de "+sdData[0].getParentFile().getName();
         headerTitle.setText(getString(R.string.doc_of)+s);
 	}
 	
 	public void startListener() {
 		lstDocs2.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> a, View v, int position, long id) { //Al clicar X item de la lista
 	            if(position == 0 && !isHomePath(sdData[position].getParent())) { // back button
 	            	Log.d("Tipo", "Atrás");
 	            	// update Data
 	            	sdData = listFiles(sdData[0].getParentFile().getParentFile(), false);
 	            	isHomePath = isHomePath(sdData[0].getParent());
 	            	
 	            	// our subtitle
 		    		setSubTitle();
 	            	// re-construct listView
 	            	setData();
 	            } else {
 	            	if(isHomePath(sdData[0].getParent())) {
 	            		Log.d("Tipo", "Directorio");
 	            		// update Data
 		            	isHomePath = isHomePath(sdData[position].getAbsolutePath());
 		            	sdData = listFiles(sdData[position].getAbsoluteFile(), false);
 		            	
 		            	// our subtitle
 			    		setSubTitle();
 		            	// re-construct listView
 		            	setData();
 	            	} else {
 		            	if(sdData[position-1].isDirectory()){
 		            		Log.d("Tipo", "Directorio2");
 		            		// update data
 			            	isHomePath = isHomePath(sdData[position].getAbsolutePath());
 			            	sdData = listFiles(sdData[position].getAbsoluteFile(), false);
 			            	
 			            	// our subtitle
 				    		setSubTitle();
 			            	// re-construct listView
 				    		setData();
 		            	} else {
 		            		Log.d("Tipo", "Archivo");
 			              
 			                MimeTypeMap mime = MimeTypeMap.getSingleton();
 			                
 			                // Get extension file
 			                String file_s = sdData[position-1].toString();
 			                String extension = "";
 			
 			        		int i = file_s.lastIndexOf('.');
 			        		int p = Math.max(file_s.lastIndexOf('/'), file_s.lastIndexOf('\\'));
 			
 			        		if (i > p) {
 			        		    extension = file_s.substring(i+1);
 			        		}
 			        		
 			        		// Get extension reference
 			                String doc_type = mime.getMimeTypeFromExtension(extension);
 			                
 			                Log.d("Type", extension);
 			                
 			                // the Dialog
 			                AlertDialog dialog = (AlertDialog) createDialog(position, doc_type, extension);
 			                dialog.show();     
 		            	}
 	            	}
 	            }
             }
         });
 	}
 	
 	public Dialog createDialog(final int position, final String doc_type, final String ext) {
 	    AlertDialog.Builder builder = new AlertDialog.Builder(mycontext);
 	    builder.setTitle(sdData[position-1].getName().replaceFirst("[.][^.]+$", ""));
 	    builder.setItems(R.array.dialog_array, new DialogInterface.OnClickListener() {
 	        public void onClick(final DialogInterface dialog, int which) {
 	        	switch(which) {
 	        	case 0:
 	        		Intent intent = new Intent();
 	                intent.setAction(android.content.Intent.ACTION_VIEW);
 	                intent.setDataAndType(Uri.fromFile(sdData[position-1]),doc_type);
 	                try {
 	                	startActivity(intent);
 	            	} catch(ActivityNotFoundException e) {
 						Toast.makeText(mycontext, "Lo siento, ha ocurrido algún error abriendo el archivo.", Toast.LENGTH_SHORT).show();
 					}
 	                dialog.dismiss();
 	        		break;
 	        	case 1:
 	        		AlertDialog.Builder builder3 = new AlertDialog.Builder(mycontext);
 	        	    // Get the layout inflater
 	        	    LayoutInflater inflater2 = mycontext.getLayoutInflater();
 	        	    final View item2 = inflater2.inflate(R.layout.dialog_frename, null);
 
 	        	    // Inflate and set the layout for the dialog
 	        	    // Pass null as the parent view because its going in the dialog layout
 	        	    builder3.setView(item2);
 	        	    builder3.setTitle(sdData[position-1].getName().replaceFirst("[.][^.]+$", ""));
 	        	    builder3.setPositiveButton("Renombrar",
 	        	            new DialogInterface.OnClickListener() {
 	        	                public void onClick(DialogInterface dialog3, int id) {
 	        	                	EditText new_name = (EditText) item2.findViewById(R.id.file_rename);
 	        	                	String file_name = new_name.getText().toString();
 	        	                	if(file_name.length()==0) {
 	        	                		Toast.makeText(mycontext, "El archivo no puede ser renombrado con ese nombre!", Toast.LENGTH_SHORT).show();
 	        	                	} else {
 	        	                		File file = sdData[position-1];
 	        	                		try {
 	        	                		file.renameTo(new File(sdData[position-1].getParentFile(), file_name+"."+ext));
 	        	                		} catch(SecurityException e) {
 	        	                			Toast.makeText(mycontext, "No ha sido posible renombrar el archivo!", Toast.LENGTH_SHORT).show();
 	        	                		}
 	        	                		Toast.makeText(mycontext, "Archivo renombrado a: "+file_name+"."+ext, Toast.LENGTH_SHORT).show();
 	        	                	}
 	        	                	isHomePath = isHomePath(sdData[position-1].getParentFile().getAbsolutePath());
 	        	                	sdData = listFiles(sdData[position-1].getParentFile(), false);
 	        			    		lstAdapter2.setIsHomePath(isHomePath);
 	        			        	lstAdapter2.setData(sdData);
 	        			            lstAdapter2.notifyDataSetChanged();
 	        			    		// bye bye dialog
 	        	                	dialog3.dismiss();
 	        	                    dialog.dismiss();
 	        	                }
 	        	            });
 	        	    builder3.setNegativeButton("Cancelar",
 	        	            new DialogInterface.OnClickListener() {
 	        	                public void onClick(DialogInterface dialog3, int id) {
 	        	                	dialog3.cancel();
 	        	                    dialog.dismiss();
 	        	                }
 	        	    });
 	        	    // show dialog
 	        	    builder3.show();
 	        		break;
 	        	case 2:
 	        		// delete archive
 	        		sdData[position-1].delete();
 	            	
             		// get parent in case of...
 	            	File parent = sdData[position-1].getParentFile();
 	            	sdData = listFiles(sdData[position-1].getParentFile(), false);
 	            	//...there are any file inside directory
 	            	if(sdData!= null && sdData.length==0) sdData = null;
 	            	if(sdData==null) {
 	            		File home = parent.getParentFile();
 	            		// directory deletion
 	            		parent.delete(); 
 	            		// data update
 	            		isHomePath = true;
 	            		sdData = listFiles(home, true);
 	            	} else {
 	            		isHomePath = isHomePath(parent.getAbsolutePath());
 	            	}
 	        		
 	            	if(sdData!= null && sdData.length==0) sdData = null;
 	            	if(sdData!=null) {
 		            	// our subtitle
 			    		setSubTitle();
 		        		// re-construct listView
 			    		setData();
 	            	} else {
 	            		lstAdapter2.clear();
 	            		lstAdapter2.notifyDataSetChanged();
 				        lstAdapter2 = null;
 				        lstDocs2 = null;
 				        status = false;
 
 				        // None
 						TextView tv = (TextView) findViewById(R.id.manager_error);
 				        tv.setText(getString(R.string.handle_2));
 				        tv.setVisibility(0);
 	            	}
 			        // user notice
 	        		Toast.makeText(mycontext, "Archivo eliminado", Toast.LENGTH_SHORT).show();
 	        		// bye bye dialog
 	        		dialog.dismiss();
 	        		break;
	        	case 3:
 	        		AlertDialog.Builder builder2 = new AlertDialog.Builder(mycontext);
 	        	    // Get the layout inflater
 	        	    LayoutInflater inflater = mycontext.getLayoutInflater();
 	        	    View item = inflater.inflate(R.layout.dialog_finfo, null);
 
 	        	    // Inflate and set the layout for the dialog
 	        	    // Pass null as the parent view because its going in the dialog layout
 	        	    builder2.setView(item);
 	        	    builder2.setTitle(sdData[position-1].getName().replaceFirst("[.][^.]+$", ""));
 	        	    builder2.setPositiveButton("Atrás",
 	        	            new DialogInterface.OnClickListener() {
 	        	                public void onClick(DialogInterface dialog2, int id) {
 	        	                    dialog2.cancel();
 	        	                    dialog.dismiss();
 	        	                }
 	        	            });
 	        	    
 	        	    // complete name
 	        	    TextView textview = (TextView) item.findViewById(R.id.file_name_data);
 	        	    textview.setText(sdData[position-1].getName());
 	        	    // modified date
 	        	    textview = (TextView) item.findViewById(R.id.file_modified_data);
 	        	    Date date = new Date(sdData[position-1].lastModified());
 	        	    DateFormat formatter = new SimpleDateFormat("dd/MM/yy - HH:mm:ss");
 	        	    String dateFormatted = formatter.format(date);
 	        	    textview.setText(dateFormatted);
 	        	    // extension
 	        	    textview = (TextView) item.findViewById(R.id.file_extension_data);
 	        	    textview.setText(ext);
 	        	    // size
 	        	    textview = (TextView) item.findViewById(R.id.file_size_data);
 	        	    textview.setText(humanReadableByteCount(sdData[position-1].length(), true));
 	        	    // path
 	        	    textview = (TextView) item.findViewById(R.id.file_path_data);
 	        	    textview.setText(sdData[position-1].getPath());
 	        	    // show dialog
 	        	    builder2.show();
 	        		
 	        		break;
 	        	}
 	        }
 	    });
 	    return builder.create();
 	}
 	
 	public static String humanReadableByteCount(long bytes, boolean si) {
 	    int unit = si ? 1000 : 1024;
 	    if (bytes < unit) return bytes + " B";
 	    int exp = (int) (Math.log(bytes) / Math.log(unit));
 	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
 	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
 	}
     
     public void onResume()
     {
         super.onResume();
         Log.d("Lifecycle4", "In the onResume()");
         
         // re-construct listView
         if(i>1) {
 	        try {
 		        File parent = sdData[0].getParentFile();
 		        if(isHomePath) {
 		        	sdData = listFiles(parent, true);
 		        } else {
 		        	sdData = listFiles(parent, false);
 		        }
 		        if(sdData!= null && sdData.length==0) sdData = null;
 		        if(sdData!=null) {
 		        	if(lstDocs2==null) {
 		        		setData(true);
 		        		startListener();
 		        	} else {
 		        		setData();
 		        	}
 		        }
 	        } catch(NullPointerException e) {
 	        	status = true;
 	        	onCreate(null);
 	        } catch(ArrayIndexOutOfBoundsException e) {
 	        	status = true;
 	        	onCreate(null);
 	        }
         } else {
         	i++;
         }
     }
 }
