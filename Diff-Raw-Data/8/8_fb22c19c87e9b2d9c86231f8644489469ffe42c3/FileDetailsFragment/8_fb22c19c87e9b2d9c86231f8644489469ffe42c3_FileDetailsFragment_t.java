 package com.quanturium.androcloud.fragments;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.content.ClipData;
 import android.content.ClipboardManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.MimeTypeMap;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 import com.cloudapp.api.CloudApp;
 import com.cloudapp.api.CloudAppException;
 import com.cloudapp.api.model.CloudAppItem;
 import com.cloudapp.api.model.CloudAppItem.Type;
 import com.cloudapp.impl.CloudAppImpl;
 import com.cloudapp.impl.model.CloudAppItemImpl;
 import com.quanturium.androcloud.R;
 import com.quanturium.androcloud.activities.FilesActivity;
 import com.quanturium.androcloud.listener.FilesDetailsFragmentListener;
 import com.quanturium.androcloud.tools.Cache;
 import com.quanturium.androcloud.tools.Constant;
 import com.quanturium.androcloud.tools.Prefs;
 import com.quanturium.androcloud.tools.Tools;
 import com.quanturium.androcloud.transfert.TransfertTask;
 import com.quanturium.androcloud.transfert.download.DownloadTask;
 
 public class FileDetailsFragment extends Fragment
 {
 	private Handler							handler				= new Handler();
 	private Activity						activity;
 	private CloudAppItem					file;
 	private FilesDetailsFragmentListener	listener;
 	public Menu								menu;
 	public boolean							currentlyLoading	= false;
 	public int								currentlyAction		= -1;
 	private ImageView						imageView;
 	private ProgressBar						imageLoader;
 	private Bitmap							imageBitmap			= null;
 
 	private final static String				TAG					= "FileDetailsFragment";
 
 	@Override
 	public void onAttach(Activity activity)
 	{
 		Log.i(TAG, "Attached : " + this.toString() + " to " + activity.toString());
 
 		super.onAttach(activity);
 		try
 		{
 			listener = (FilesDetailsFragmentListener) activity;
 		} catch (ClassCastException e)
 		{
 			Log.e(TAG, "error");
 			throw new ClassCastException(activity.toString() + " must implement FileDetailsFragmentListener");
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		Log.i(TAG, "Created : " + this.toString());
 
 		super.onCreate(savedInstanceState);
 
 		setHasOptionsMenu(true);
 		setRetainInstance(false);
 
 		setHandler();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		Log.i(TAG, "View created : " + this.toString());
 
 		if (container == null)
 			return null;
 
 		View mainView = inflater.inflate(R.layout.file_details, container, false);
 		return mainView;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState)
 	{
 		Log.i(TAG, "Activity created : " + this.toString());
 
 		super.onActivityCreated(savedInstanceState);
 
 		this.activity = getActivity();
 
 		Bundle bundle = this.getArguments();
 
 		if (bundle != null)
 		{
 			if (getArguments().getString("json", null) != null)
 			{
 				if (getArguments().getString("json") != null && getView() != null)
 				{
 					setUI();
 					loadItem(getArguments().getString("json"));
 					fillUI();
 				}
 			}
 		}
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case android.R.id.home:
 
 				if (!listener.isDualView())
 				{
 					Intent intent = new Intent(getActivity(), FilesActivity.class);
 					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 					startActivity(intent);
 				}
 
 				break;
 
 			case R.id.menuItemView:
 
 				openFile();
 
 				break;
 
 			case R.id.menuItemSave:
 
 				File saveFile = null;
 				try
 				{
 					saveFile = new File(getSavingFolder(), file.getName());
 
 					if (saveFile.exists())
 					{
 						AlertDialog.Builder alert = new AlertDialog.Builder(this.activity);
 
 						alert.setTitle("Open file");
 						alert.setMessage("A file with the same name already exists. Do you want to overwrite it ?");
 						alert.setNegativeButton("Cancel", null);
 						alert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
 						{
 							public void onClick(DialogInterface dialog, int whichButton)
 							{
 								saveFile(false);
 							}
 						});
 
 						alert.show();
 					}
 					else
 					{
 						saveFile(false);
 					}
 				} catch (CloudAppException e1)
 				{
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 
 				break;
 
 			case R.id.menuItemShare:
 
 				shareFile();
 
 				break;
 
 			case R.id.menuItemRename:
 
 				AlertDialog.Builder alert = new AlertDialog.Builder(this.activity);
 
 				alert.setTitle("New name");
 
 				// Set an EditText view to get user input
 				final EditText input = new EditText(this.activity);
 				input.setText(getNameWithoutExtension());
 				input.setSelectAllOnFocus(true);
 				alert.setView(input);
 				alert.setNegativeButton("Cancel", null);
 				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
 				{
 					public void onClick(DialogInterface dialog, int whichButton)
 					{
 						String value = input.getText().toString().trim();
 
 						if (!value.equals("") && !value.equals(getNameWithoutExtension()))
 						{
 							String ext = getExtention();
 
 							if (ext != null)
 								renameFile(value + '.' + getExtention());
 							else
 								renameFile(value);
 						}
 
 					}
 				});
 
 				alert.show();
 
 				break;
 
 			case R.id.menuItemDelete:
 
 				deleteFile();
 
 				break;
 
 			default:
 
 				return false;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
 	{
		if(activity instanceof FilesActivity)
		{
			if(!((FilesActivity)activity).isDualView2())
			{
				return;
			}
		}
			
 		inflater.inflate(R.menu.file_activity, menu);
 
 		if (currentlyLoading)
 		{
 			int order = menu.findItem(currentlyAction).getOrder();
 			Log.i(TAG, "Order " + order + "");
 			menu.add(Menu.NONE, Menu.NONE, order, "Loading").setActionView(R.layout.progress).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 			menu.removeItem(currentlyAction);
 		}
 
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 	private void displayView(View view)
 	{
 		this.imageView.setVisibility(View.GONE);
 		this.imageLoader.setVisibility(View.GONE);
 
 		view.setVisibility(View.VISIBLE);
 	}
 
 	private void setUI()
 	{
 		this.imageView = (ImageView) activity.findViewById(R.id.imageView);
 		this.imageLoader = (ProgressBar) activity.findViewById(R.id.imageLoader);
 	}
 
 	private void fillUI()
 	{
 		ActionBar actionBar = activity.getActionBar();
 
 		if (!listener.isDualView())
 		{
 			actionBar.setDisplayHomeAsUpEnabled(true);
 
 			int ressource = 0;
 
 			try
 			{
 				switch (file.getItemType())
 				{
 
 					case AUDIO:
 						ressource = R.drawable.ic_itemtype_audio;
 						break;
 
 					case BOOKMARK:
 						ressource = R.drawable.ic_itemtype_bookmark;
 						break;
 
 					case IMAGE:
 						ressource = R.drawable.ic_itemtype_image;
 						break;
 
 					case VIDEO:
 						ressource = R.drawable.ic_itemtype_video;
 						break;
 
 					case TEXT:
 						ressource = R.drawable.ic_itemtype_text;
 						break;
 
 					case ARCHIVE:
 						ressource = R.drawable.ic_itemtype_archive;
 						break;
 
 					case UNKNOWN:
 					default:
 						ressource = R.drawable.ic_itemtype_unknown;
 						break;
 				}
 			} catch (CloudAppException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			if (android.os.Build.VERSION.SDK_INT >= 14)
 			{
 				actionBar.setIcon(ressource);
 			}
 
 			try
 			{
 				actionBar.setSubtitle(file.getName());
 			} catch (CloudAppException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		try
 		{
 			Integer drawableRessource = null;
 
 			switch (file.getItemType())
 			{
 
 				case AUDIO:
 					drawableRessource = R.drawable.file_audio;
 					break;
 
 				case BOOKMARK:
 					drawableRessource = R.drawable.file_bookmark;
 					break;
 
 				case IMAGE:
 
 					break;
 
 				case VIDEO:
 					drawableRessource = R.drawable.file_video;
 					break;
 
 				case TEXT:
 					drawableRessource = R.drawable.file_text;
 					break;
 
 				case ARCHIVE:
 					drawableRessource = R.drawable.file_archive;
 					break;
 
 				case UNKNOWN:
 				default:
 					drawableRessource = R.drawable.file_unknown;
 					break;
 			}
 
 			if (drawableRessource == null)
 			{
 				String fileName = "bitmap." + Tools.md5(file.getHref()) + ".png";
 				if ((imageBitmap = Cache.getCachedBitmap(activity, fileName, Cache.CACHE_TIME_BITMAP)) != null)
 				{
 					displayView(imageView);
 					imageView.setImageBitmap(imageBitmap);
 				}
 				else
 				{
 					displayView(imageLoader);
 					loadPicture();
 				}
 			}
 			else
 			{
 				Display display = activity.getWindowManager().getDefaultDisplay();
 
 				imageView.setAdjustViewBounds(true);
 				imageView.setMaxHeight((int) (display.getHeight() * 0.7));
 				imageView.setMaxWidth((int) (display.getWidth() * 0.7));
 				imageView.setImageDrawable(getResources().getDrawable(drawableRessource));
 
 				displayView(imageView);
 			}
 		} catch (CloudAppException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private String getMIME()
 	{
 		MimeTypeMap mime = MimeTypeMap.getSingleton();
 		return mime.getMimeTypeFromExtension(getExtention());
 	}
 
 	public String getSavingFolder()
 	{
 		String folder = null;
 
 		try
 		{
 			Type type = file.getItemType();
 
 			switch (type)
 			{
 				case IMAGE:
 
 					folder = Prefs.getPreferences(activity).getString(Prefs.DIRECTORY_PICTURES, null);
 
 					break;
 
 				case AUDIO:
 
 					folder = Prefs.getPreferences(activity).getString(Prefs.DIRECTORY_MUSICS, null);
 
 					break;
 
 				case VIDEO:
 
 					folder = Prefs.getPreferences(activity).getString(Prefs.DIRECTORY_MOVIES, null);
 
 					break;
 
 				case TEXT:
 
 					folder = Prefs.getPreferences(activity).getString(Prefs.DIRECTORY_TEXTS, null);
 
 					break;
 
 				case ARCHIVE:
 				case BOOKMARK:
 				case UNKNOWN:
 				default:
 
 					folder = Prefs.getPreferences(activity).getString(Prefs.DIRECTORY_UNKOWN, null);
 
 					break;
 			}
 		} catch (CloudAppException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return folder;
 	}
 
 	private String getExtention()
 	{
 		try
 		{
 			int index = file.getRemoteUrl().lastIndexOf(".") + 1;
 
 			if (index == 0) // no file extention
 				throw new Exception();
 
 			return file.getRemoteUrl().substring(index);
 		} catch (CloudAppException e)
 		{
 			return null;
 		} catch (Exception e)
 		{
 			return null;
 		}
 	}
 
 	private String getNameWithoutExtension()
 	{
 		try
 		{
 			int index = file.getName().lastIndexOf(".") + 1;
 
 			if (index == 0) // no file extention
 			{
 				return file.getName();
 			}
 			else
 			{
 				return file.getName().substring(0, index - 1);
 			}
 		} catch (CloudAppException e)
 		{
 			return null;
 		}
 	}
 
 	private void loadItem(String jsonString)
 	{
 		if (jsonString != null)
 		{
 			try
 			{
 				this.file = new CloudAppItemImpl(new JSONObject(jsonString));
 			} catch (JSONException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void loadPicture()
 	{
 		new Thread(new Runnable()
 		{
 
 			@Override
 			public void run()
 			{
 				URL urlImage;
 
 				try
 				{
 					urlImage = new URL(file.getRemoteUrl());
 					HttpURLConnection connection = (HttpURLConnection) urlImage.openConnection();
 					connection.setInstanceFollowRedirects(true);
 					InputStream inputStream = connection.getInputStream();
 					imageBitmap = BitmapFactory.decodeStream(inputStream);
 					int new_width = 0, new_height = 0;
 					float ratio = (float) imageBitmap.getHeight() / imageBitmap.getWidth();
 
 					if (imageBitmap.getWidth() > Constant.MAX_IMAGE_SIZE_PX && imageBitmap.getHeight() > Constant.MAX_IMAGE_SIZE_PX)
 					{
 
 						if (imageBitmap.getWidth() > imageBitmap.getHeight())
 						{
 							new_width = Constant.MAX_IMAGE_SIZE_PX;
 							new_height = (int) (Constant.MAX_IMAGE_SIZE_PX * ratio);
 						}
 						else
 						{
 							new_width = (int) (Constant.MAX_IMAGE_SIZE_PX / ratio);
 							new_height = Constant.MAX_IMAGE_SIZE_PX;
 						}
 
 						imageBitmap = Bitmap.createScaledBitmap(imageBitmap, new_width, new_height, false);
 					}
 					else
 						if (imageBitmap.getWidth() > Constant.MAX_IMAGE_SIZE_PX)
 						{
 							new_width = Constant.MAX_IMAGE_SIZE_PX;
 							new_height = (int) (Constant.MAX_IMAGE_SIZE_PX * ratio);
 
 							imageBitmap = Bitmap.createScaledBitmap(imageBitmap, Constant.MAX_IMAGE_SIZE_PX, new_height, false);
 						}
 						else
 							if (imageBitmap.getHeight() > Constant.MAX_IMAGE_SIZE_PX)
 							{
 								new_width = (int) (Constant.MAX_IMAGE_SIZE_PX / ratio);
 								new_height = Constant.MAX_IMAGE_SIZE_PX;
 
 								imageBitmap = Bitmap.createScaledBitmap(imageBitmap, new_width, Constant.MAX_IMAGE_SIZE_PX, false);
 							}
 
 					Cache.setCachedBitmap(activity, "bitmap." + Tools.md5(file.getHref()) + ".png", imageBitmap);
 
 				} catch (CloudAppException e)
 				{
 					e.printStackTrace();
 				} catch (MalformedURLException e)
 				{
 					e.printStackTrace();
 				} catch (IOException e)
 				{
 					e.printStackTrace();
 				} catch (NullPointerException e)
 				{
 					e.printStackTrace();
 				} finally
 				{
 					handler.sendEmptyMessage(Constant.HANDLER_ACTION_PICTURE_LOADED);
 				}
 
 			}
 		}).start();
 	}
 
 	private void deleteFile()
 	{
 		if (!currentlyLoading)
 		{
 			listener.onStartLoading(R.id.menuItemDelete);
 
 			new Thread(new Runnable()
 			{
 
 				@Override
 				public void run()
 				{
 					Message message = new Message();
 					message.what = Constant.HANDLER_ACTION_DELETE;
 
 					CloudApp api = new CloudAppImpl(Prefs.getPreferences(activity).getString(Prefs.EMAIL, ""), Prefs.getPreferences(activity).getString(Prefs.PASSWORD, ""));
 
 					try
 					{
 						api.delete(file);
 						// TODO Delete the local file if it exists
 						message.arg1 = 1; // success
 						message.obj = file.getHref();
 					} catch (CloudAppException e)
 					{
 						message.arg1 = 0; // fail
 						e.printStackTrace();
 					} finally
 					{
 						handler.sendMessage(message);
 					}
 				}
 			}).start();
 
 		}
 	}
 
 	private void renameFile(final String newName)
 	{
 		if (!currentlyLoading)
 		{
 			listener.onStartLoading(R.id.menuItemRename);
 
 			new Thread(new Runnable()
 			{
 
 				@Override
 				public void run()
 				{
 					Message message = new Message();
 					message.what = Constant.HANDLER_ACTION_RENAME;
 
 					CloudApp api = new CloudAppImpl(Prefs.getPreferences(activity).getString(Prefs.EMAIL, ""), Prefs.getPreferences(activity).getString(Prefs.PASSWORD, ""));
 
 					try
 					{
 						api.rename(file, newName);
 						// TODO Rename the local file if it exists
 						message.arg1 = 1; // success
 						String[] data =
 						{ file.getHref(), newName };
 						message.obj = data;
 					} catch (CloudAppException e)
 					{
 						message.arg1 = 0; // fail
 						e.printStackTrace();
 					} finally
 					{
 						handler.sendMessage(message);
 					}
 				}
 			}).start();
 
 		}
 	}
 
 	private void saveFile(boolean openWhenSaved)
 	{
 		if (!currentlyLoading)
 		{
 			listener.onStartLoading(R.id.menuItemSave);
 
 			try
 			{
 				TransfertTask task = new DownloadTask(activity, file, file.getName(), this, openWhenSaved);
 				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "test");
 			} catch (CloudAppException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void shareFile()
 	{
 		// TODO Auto-generated method stub
 
 		Message message = new Message();
 		message.what = Constant.HANDLER_ACTION_SHARE;
 
 		try
 		{
 			ClipData clip = ClipData.newPlainText(file.getName() + " URL", file.getUrl());
 
 			ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
 			clipboard.setPrimaryClip(clip);
 
 			message.arg1 = 1; // success
 			message.obj = file.getHref();
 		} catch (CloudAppException e)
 		{
 			message.arg1 = 0; // fail
 			e.printStackTrace();
 		} finally
 		{
 			handler.sendMessage(message);
 		}
 	}
 
 	private void openFile()
 	{
 		if (!currentlyLoading)
 		{
 			try
 			{
 				File saveFile = new File(getSavingFolder(), file.getName());
 
 				if (saveFile.exists())
 				{
 					Intent i = new Intent();
 					i.setAction(android.content.Intent.ACTION_VIEW);
 					i.setDataAndType(Uri.fromFile(saveFile), getMIME());
 					startActivity(i);
 				}
 				else
 				// We propose to save it before opening it
 				{
 					AlertDialog.Builder alert = new AlertDialog.Builder(this.activity);
 
 					alert.setTitle("Open file");
 					alert.setMessage("The file has not been found on your device. You have to save it before being able to open it. Would you like to download and save the file ?");
 					alert.setNegativeButton("Cancel", null);
 					alert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
 					{
 						public void onClick(DialogInterface dialog, int whichButton)
 						{
 							saveFile(true);
 						}
 					});
 
 					alert.show();
 				}
 			} catch (CloudAppException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void setHandler()
 	{
 		this.handler = new Handler()
 		{
 
 			@Override
 			public void handleMessage(Message msg)
 			{
 				switch (msg.what)
 				{
 					case Constant.HANDLER_ACTION_DELETE:
 
 						listener.onStopLoading();
 
 						try
 						{
 							if (msg.arg1 == 1)
 							{
 								Toast.makeText(activity, "Deleting " + file.getName() + " : success", Toast.LENGTH_SHORT).show();
 								listener.onFileDeleted(msg);
 							}
 							else
 								Toast.makeText(activity, "Deleting " + file.getName() + " : error", Toast.LENGTH_SHORT).show();
 						} catch (CloudAppException e)
 						{
 							e.printStackTrace();
 						}
 
 						break;
 
 					case Constant.HANDLER_ACTION_OPEN:
 
 						break;
 
 					case Constant.HANDLER_ACTION_RENAME:
 
 						listener.onStopLoading();
 
 						try
 						{
 							if (msg.arg1 == 1)
 							{
 								String[] data = (String[]) msg.obj;
 
 								Toast.makeText(activity, "Renaming " + data[1] + " : success", Toast.LENGTH_SHORT).show();
 
 								activity.getActionBar().setSubtitle(data[1]);
 
 								listener.onFileRenamed(msg);
 								file.setName(data[1]);
 							}
 							else
 								Toast.makeText(activity, "Renaming " + file.getName() + " : error", Toast.LENGTH_SHORT).show();
 						} catch (CloudAppException e)
 						{
 							e.printStackTrace();
 						}
 
 						break;
 
 					case Constant.HANDLER_ACTION_SAVE:
 
 						listener.onStopLoading();
 
 						try
 						{
 							if (msg.arg1 == 1)
 							{
 								Toast.makeText(activity, "Downloading " + file.getName() + " : success", Toast.LENGTH_SHORT).show();
 
 								if (msg.arg2 == 1)
 									openFile();
 							}
 							else
 								if (msg.arg1 == 0)
 								{
 									Toast.makeText(activity, "Downloading " + file.getName() + " : canceled", Toast.LENGTH_SHORT).show();
 								}
 
 						} catch (CloudAppException e)
 						{
 							e.printStackTrace();
 						}
 
 						break;
 
 					case Constant.HANDLER_ACTION_SHARE:
 
 						if (msg.arg1 == 1)
 						{
 							Toast.makeText(activity, "Copying link to clipboard : success", Toast.LENGTH_SHORT).show();
 						}
 						else
 							Toast.makeText(activity, "Copying link to clipboard : error", Toast.LENGTH_SHORT).show();
 
 						break;
 
 					case Constant.HANDLER_ACTION_PICTURE_LOADED:
 
 						imageLoader.setVisibility(View.GONE);
 
 						if (imageBitmap != null)
 						{
 							displayView(imageView);
 
 							imageView.setImageBitmap(imageBitmap);
 						}
 						else
 						{
 							displayView(imageView);
 							Display display = activity.getWindowManager().getDefaultDisplay();
 
 							imageView.setAdjustViewBounds(true);
 							imageView.setMaxHeight((int) (display.getHeight() * 0.5));
 							imageView.setMaxWidth((int) (display.getWidth() * 0.5));
 							imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_nopreview3));
 						}
 
 						break;
 				}
 			}
 		};
 	}
 
 	public Handler getHandler()
 	{
 		return this.handler;
 	}
 
 	@Override
 	public void onDestroy()
 	{
 		super.onDestroy();
 
 		if (imageBitmap != null)
 		{
 			imageBitmap.recycle();
 			imageBitmap = null;
 		}
 	}
 }
