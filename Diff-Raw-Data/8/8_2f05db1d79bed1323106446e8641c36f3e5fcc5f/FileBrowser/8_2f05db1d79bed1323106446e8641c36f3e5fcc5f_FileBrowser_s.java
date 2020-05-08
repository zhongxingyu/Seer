 package com.chinaece.gaia.util;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.chinaece.gaia.R;
 import com.chinaece.gaia.constant.Gaia;
 
 public class FileBrowser extends ListView implements
 		android.widget.AdapterView.OnItemClickListener,android.widget.AdapterView.OnItemLongClickListener
 {
 	private String sdcardDirectory;
 	private List<File> fileList = new ArrayList<File>();
 	private Stack<String> dirStack = new Stack<String>();
 	private FileListAdapter fileListAdapter;
 	private OnFileBrowserListener onFileBrowserListener;
 	private int folderImageResId;
 	private int otherFileImageResId;
 	private Map<String, Integer> fileImageResIdMap = new HashMap<String, Integer>();
 	private boolean onlyFolder = false;
 
 	public FileBrowser(Context context, AttributeSet attrs) throws IOException
 	{
 		
 		super(context, attrs);
 		sdcardDirectory = android.os.Environment.getExternalStorageDirectory()
 				.toString()+"/ece";
 		File file = new File(sdcardDirectory);
		if(file.exists()){
			return;
 		}
		System.err.println(file.mkdirs());
		file.createNewFile(); 
 		setOnItemClickListener(this);
 		setOnItemLongClickListener(this);
 		setBackgroundColor(android.graphics.Color.BLACK);
 
 		folderImageResId = R.drawable.folder;
 		otherFileImageResId = R.drawable.other;
 		onlyFolder = false;
 		
 		dirStack.push(sdcardDirectory);
 		refreshFiles();
 		
 		fileListAdapter = new FileListAdapter(getContext(), 0);
 		setAdapter(fileListAdapter);
 
 	}
 
 	public void refreshFiles()
 	{
 		fileList.clear();
 		String currentPath = getCurrentPath();
 		File[] files = new File(currentPath).listFiles();
 		if (dirStack.size() > 1)
 			fileList.add(null);
 		for (File file : files)
 		{
 			if (onlyFolder)
 			{
 				if (file.isDirectory())
 					fileList.add(file);
 			}
 			else
 			{
 				fileList.add(file);
 			}
 		}
 	}
 
 	private String getCurrentPath()
 	{
 		String path = "";
 		for (String dir : dirStack)
 		{
 			path += dir + "/";
 		}
 		path = path.substring(0, path.length() - 1);
 		return path;
 	}
 
 	private String getExtName(String filename)
 	{
 
 		int position = filename.lastIndexOf(".");
 		if (position >= 0)
 			return filename.substring(position + 1);
 		else
 			return "";
 	}
 	
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 			long id)
 	{
 		if (fileList.get(position) == null)
 		{
 			dirStack.pop();
 			refreshFiles();
 			fileListAdapter.notifyDataSetChanged();
 			if (onFileBrowserListener != null)
 			{
 				onFileBrowserListener.onDirItemClick(getCurrentPath());
 			}
 		}
 		else if (fileList.get(position).isDirectory())
 		{
 			dirStack.push(fileList.get(position).getName());
 			refreshFiles();
 			fileListAdapter.notifyDataSetChanged();
 			if (onFileBrowserListener != null)
 			{
 				onFileBrowserListener.onDirItemClick(getCurrentPath());
 			}
 		}
 		else
 		{
 			if (onFileBrowserListener != null)
 			{
 				String filename = getCurrentPath() + "/"
 						+ fileList.get(position).getName();
 				onFileBrowserListener.onFileItemClick(filename);
 			}
 		}
 
 	}
 	
 	@Override
 	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position,
 			long arg3) {
 		if (fileList.get(position) == null)
 		{
 			dirStack.pop();
 			refreshFiles();
 			fileListAdapter.notifyDataSetChanged();
 			if (onFileBrowserListener != null)
 			{
 				onFileBrowserListener.onDirItemClick(getCurrentPath());
 			}
 		}
 		else if (fileList.get(position).isDirectory())
 		{
 			dirStack.push(fileList.get(position).getName());
 			refreshFiles();
 			fileListAdapter.notifyDataSetChanged();
 			if (onFileBrowserListener != null)
 			{
 				onFileBrowserListener.onDirItemClick(getCurrentPath());
 			}
 		}
 		else
 		{
 			if (onFileBrowserListener != null)
 			{
 				String filename = getCurrentPath() + "/"
 						+ fileList.get(position).getName();
 			onFileBrowserListener.onFlieLongItemClick(filename);
 			}
 		}
 		return true;
 	}
 
 	private class FileListAdapter extends ArrayAdapter<File>
 	{
 		private Context context;
 		
 		public FileListAdapter(Context context, int textViewResourceId) {
 			super(context, textViewResourceId);
 			this.context = context;
 		}
 		
 		@Override
 		public int getCount()
 		{
 			return fileList.size();
 		}
 
 		@Override
 		public File getItem(int position)
 		{
 			return fileList.get(position);
 		}
 
 		@Override
 		public long getItemId(int position)
 		{
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent)
 		{
 			LinearLayout fileLayout = new LinearLayout(context);
 			fileLayout.setLayoutParams(new LayoutParams(
 					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 			fileLayout.setOrientation(LinearLayout.HORIZONTAL);
 			fileLayout.setPadding(5, 10, 0, 10);
 			ImageView ivFile = new ImageView(context);
 			ivFile.setLayoutParams(new LayoutParams(48, 48));
 			TextView tvFile = new TextView(context);
 			tvFile.setTextColor(android.graphics.Color.WHITE);
 			tvFile.setTextAppearance(context,
 					android.R.style.TextAppearance_Large);
 
 			tvFile.setPadding(5, 5, 0, 0);
 			if (fileList.get(position) == null)
 			{
 
 				if (folderImageResId > 0)
 					ivFile.setImageResource(folderImageResId);
 				tvFile.setText(". .");
 			}
 			else if (fileList.get(position).isDirectory())
 			{
 				if (folderImageResId > 0)
 					ivFile.setImageResource(folderImageResId);
 				tvFile.setText(fileList.get(position).getName());
 			}
 			else
 			{
 				tvFile.setText(fileList.get(position).getName());
 				Integer resId = fileImageResIdMap.get(getExtName(fileList.get(
 						position).getName()).toLowerCase());
 				int fileImageResId = 0;
 				if (resId != null)
 				{
 					if (resId > 0)
 					{
 						fileImageResId = resId;
 					}
 
 				}
 				if (fileImageResId > 0)
 					ivFile.setImageResource(fileImageResId);
 				else if (otherFileImageResId > 0)
 					ivFile.setImageResource(otherFileImageResId);
 			}
 
 			tvFile.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
 					LayoutParams.WRAP_CONTENT));
 			fileLayout.addView(ivFile);
 			fileLayout.addView(tvFile);
 			return fileLayout;
 		}
 		
 		
 	}
 
 	public void setOnFileBrowserListener(OnFileBrowserListener listener)
 	{
 		this.onFileBrowserListener = listener;
 	}
 	
 	
 }
