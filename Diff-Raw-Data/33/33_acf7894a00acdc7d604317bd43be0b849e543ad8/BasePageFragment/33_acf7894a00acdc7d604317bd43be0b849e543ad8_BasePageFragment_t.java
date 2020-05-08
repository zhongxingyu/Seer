 package com.mick88.convoytrucking.base;
 
 import org.json.JSONException;
 
 import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ProgressBar;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.mick88.convoytrucking.ConvoyTruckingApp;
 import com.mick88.convoytrucking.R;
 import com.mick88.convoytrucking.activities.MainActivity;
 import com.mick88.convoytrucking.api.ApiConnection;
 import com.mick88.convoytrucking.api.ApiRequest;
 import com.mick88.convoytrucking.api.ApiRequest.ApiRequestListener;
 import com.mick88.convoytrucking.api.entities.ApiEntity;
 import com.mick88.convoytrucking.api.entities.ApiError;
 import com.mick88.convoytrucking.interfaces.OnDownloadListener;
 import com.mick88.convoytrucking.interfaces.RefreshListener;
 import com.mick88.util.FontApplicator;
 
 /**
  * Base page fragment. Displays information as cards.
  * 
  * @author Michal
  * 
  */
 public abstract class BasePageFragment<T extends ApiEntity> extends
 		SherlockFragment
 {
 	public static final String EXTRA_ENTITY = "entity";
 	protected T entity;
 	protected MainActivity activity = null;
 	protected ConvoyTruckingApp application;
 	protected ViewGroup rootView = null;
 	ApiConnection connection;
 	ApiRequest pendingRequest = null;
 	FontApplicator fontApplicator = null;
 
 	protected abstract int selectLayout();
 
 	protected abstract T createEntity(String json) throws JSONException;
 
 	/**
 	 * Allows fragment to introduce required settings to the actionbar. Super
 	 * implementation sets title to app name and navigation mode to default
 	 * 
 	 * @param actionBar
 	 */
 	public void setupActionBar(ActionBar actionBar)
 	{
 		actionBar.setLogo(R.drawable.convoy_trucking_logo);
 		actionBar.setDisplayUseLogoEnabled(true);
 		actionBar.setTitle(R.string.app_name);
 		actionBar.setSubtitle(null);
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
 	}
 
 	protected ActionBar getActionBar()
 	{
 		if (activity == null)
 			return null;
 		return activity.getSupportActionBar();
 	}
 
 	/**
 	 * Should return url to the website equivalent of this page or null if there
 	 * is none.
 	 */
 	public String getWebsiteUrl()
 	{
 		return null;
 	}
 
 	public boolean refresh(final RefreshListener listener)
 	{
 		if (pendingRequest != null)
 			return false;
 
 		downloadData(new OnDownloadListener()
 		{
 
 			@Override
 			public void onDownloadFinished()
 			{
 				if (listener != null)
 					listener.onRefreshFinished();
 			}
 		});
 		return true;
 	}
 
 	protected void downloadData(final OnDownloadListener listener)
 	{
 		pendingRequest = createRequest().SendAsync(new ApiRequestListener()
 		{
 			@Override
 			public void onRequestComplete(ApiRequest apiRequest)
 			{
				String errorMessage = null;
 				if (apiRequest.isEmpty() == false)
 				{
 					try
 					{
 						setEntity(createEntity(apiRequest.getResult()));
 					} catch (JSONException e)
 					{
 						ApiError error = apiRequest.getError();
 						if (error != null)
 						{
 							Log.e(getClass().getName(), error.getMessage());
							errorMessage = error.getMessage();
							
 						} else
						{
 							Log.e(getClass().getName(),
 									"Unknown error while parsing "
 											+ apiRequest.getResult());
							errorMessage = "Unknown error while parsing received data.";
						}
						
 					}
 				}
				else errorMessage = "No data received from server";
				
				if (errorMessage != null)
				{
					new AlertDialog.Builder(activity)
						.setTitle("Error")
						.setMessage(errorMessage).setPositiveButton("Retry", new DialogInterface.OnClickListener()
						{
							
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								downloadData(listener);								
							}
						})
						.setNegativeButton(android.R.string.cancel, null)
						.show();
				}
				
 				pendingRequest = null;
 				
 				if (listener != null)
 					listener.onDownloadFinished();
 			}
 		});
 	}
 
 	/**
 	 * checks if view is created and entity exists, then calls fillContents()
 	 * returns true if both conditions are true
 	 */
 	protected boolean fillViewContents()
 	{
 		if (rootView != null && entity != null)
 		{
 			return true;
 		} else
 			return false;
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState)
 	{
 		super.onSaveInstanceState(outState);
 		outState.putSerializable(EXTRA_ENTITY, entity);
 	}
 
 	@Override
 	public void onAttach(Activity activity)
 	{
 		super.onAttach(activity);
 		this.activity = (MainActivity) activity;
 		this.fontApplicator = new FontApplicator(activity.getAssets(),
 				ConvoyTruckingApp.FONT_ROBOTO_LIGHT);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onCreate(android.os.Bundle arg0)
 	{
 		super.onCreate(arg0);
 
 		this.activity = (MainActivity) getActivity();
 		this.application = (ConvoyTruckingApp) activity.getApplication();
 		this.connection = application.getApiConnection();
 		setHasOptionsMenu(true);
 
 		if (arg0 != null)
 		{
 			Object state = arg0.getSerializable(EXTRA_ENTITY);
 			if (state instanceof ApiEntity)
 			{
 				setEntity((T) state);
 			}
 		}
 
 		if (entity == null)
 		{
 			downloadData(null);
 		}
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
 	{
 		super.onCreateOptionsMenu(menu, inflater);
 
 		inflater.inflate(R.menu.page, menu);
 		if (getWebsiteUrl() == null)
 			menu.findItem(R.id.action_web).setVisible(false);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case R.id.action_web:
 				String url = getWebsiteUrl();
 				if (url == null) return true;
 				Intent intent = new Intent(Intent.ACTION_VIEW);
 				intent.setData(Uri.parse(url));
 				startActivity(intent);
 				return true;
 	
 			case R.id.action_page_refresh:
 				if (refresh(new RefreshListener()
 				{
 	
 					@Override
 					public void onRefreshFinished()
 					{
 						item.setActionView(null);
 	
 					}
 				}))
 				{
 					LayoutParams params = new LayoutParams(
 							LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 					ProgressBar progressBar = new ProgressBar(getActivity(), null,
 							android.R.attr.progressBarStyle);
 					progressBar.setLayoutParams(params);
 					item.setActionView(progressBar);
 				}
 				return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * Creates empty request
 	 */
 	protected ApiRequest createRequest()
 	{
 		return connection.createRequest();
 	}
 
 	/**
 	 * Sets entity and updates views
 	 * 
 	 * @param entity
 	 * @return
 	 */
 	public BasePageFragment<T> setEntity(T entity)
 	{
 		this.entity = entity;
 		fillViewContents();
 		return this;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState)
 	{
 		rootView = (ViewGroup) inflater.inflate(selectLayout(), null);
 		fontApplicator.applyFont(rootView);
 		fillViewContents();
 
 		setupActionBar(activity.getSupportActionBar());
 		return rootView;
 	}
 
 	@Override
 	public void onDestroyView()
 	{
 		rootView = null;
 		super.onDestroyView();
 	}
 
 	protected View findViewById(int id)
 	{
 		if (rootView == null)
 			return null;
 		else
 			return rootView.findViewById(id);
 	}
 
 }
