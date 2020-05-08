 package fr.eyal.datalib.sample.netflix.fragment;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.widget.ImageView;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import fr.eyal.datalib.sample.netflix.MovieActivity;
 import fr.eyal.datalib.sample.netflix.R;
 import fr.eyal.datalib.sample.netflix.data.model.cast.Cast;
 import fr.eyal.datalib.sample.netflix.data.model.cast.CastPerson;
 import fr.eyal.datalib.sample.netflix.data.model.movie.Movie;
 import fr.eyal.datalib.sample.netflix.data.model.movie.MovieCategory;
 import fr.eyal.datalib.sample.netflix.data.model.movieimage.MovieImage;
 import fr.eyal.datalib.sample.netflix.data.model.synopsis.Synopsis;
 import fr.eyal.datalib.sample.netflix.data.service.NetflixService;
 import fr.eyal.datalib.sample.netflix.fragment.model.MovieItem;
 import fr.eyal.datalib.sample.netflix.util.Util;
 import fr.eyal.lib.data.model.ResponseBusinessObject;
 import fr.eyal.lib.data.service.DataManager;
 import fr.eyal.lib.data.service.model.BusinessResponse;
 import fr.eyal.lib.data.service.model.DataLibRequest;
 
 public class MovieFragment extends NetflixFragment {
 
 	MovieItem mMovieItem;
 	Movie mMovie;
 	Synopsis mSynopsis;
 	Cast mCast;
 	
 	TextView mTxtTitle;
 	TextView mTxtCategory;
 	TextView mTxtYear;
 	TextView mTxtTime;
 	TextView mTxtSynopsis;
 	TextView mTxtCast1;
 	TextView mTxtCast2;
 	ImageView mImage;
 	String mDataType;
 	Bitmap mBitmap;
 	
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Bundle extras = getActivity().getIntent().getExtras();
 		mMovieItem = (MovieItem) extras.get(MovieActivity.EXTRA_MOVIE);
 		
 		
 		super.onCreate(savedInstanceState);
 		setRetainInstance(true);
 		
 		if(mMovieItem.getLabel(-1).contains("Season"))
 			mDataType = "series";
 		else
 			mDataType = "movies";
 		
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		
 		ScrollView scrollView = (ScrollView) inflater.inflate(R.layout.fgmt_movie, null, false);
 				
 		mTxtCast1 = (TextView) scrollView.findViewById(R.id.cast1);
 		mTxtCast2 = (TextView) scrollView.findViewById(R.id.cast2);
 		mTxtCategory = (TextView) scrollView.findViewById(R.id.category);
 		mTxtSynopsis = (TextView) scrollView.findViewById(R.id.synopsis_content);
 		mTxtTime = (TextView) scrollView.findViewById(R.id.time);
 		mTxtTitle = (TextView) scrollView.findViewById(R.id.title);
 		mTxtYear = (TextView) scrollView.findViewById(R.id.year);
 		mImage = (ImageView) scrollView.findViewById(R.id.image);
 
 
 		Bitmap bmp = null;
 		if(mBitmap != null) {
 			mImage.setImageDrawable(new BitmapDrawable(getResources(), mBitmap));
 			bmp = mBitmap;
 			
 		} else {
 			
 			if(mMovieItem != null) {
 				mTxtTitle.setText(mMovieItem.getLabel(-1));
				bmp = mMovieItem.getPoster(false);			
 			}
 			
 			if(bmp != null) {
 				mBitmap = bmp;
 				
 				// Init RS variables
 //				mInBitmap = bmp;
 //		        mOutBitmap = bmp.copy(bmp.getConfig(), true);
 				
 				float width = getResources().getDimension(R.dimen.movie_image_width);
 				float height = width * bmp.getHeight() / bmp.getWidth();
 				Util.scaleBitmap(bmp, width, height, Util.ScalingLogic.FIT);
 				
 				mImage.setImageDrawable(new BitmapDrawable(getResources(), bmp));
 			}
 			
 		}
 		
		if(bmp != null && bmp.getWidth() < mImage.getHeight()){
 			
 			try {
 				int id = mDataManager.getMovieImage(DataManager.TYPE_CACHE, this, mMovieItem.getImageUrl(), DataLibRequest.OPTION_NO_OPTION, null, null);
 				synchronized (mRequestIds){
 					mRequestIds.add(id);
 				}
 				
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		int movieId = Integer.parseInt(mMovieItem.getId());
 
 		if(mMovie != null){
 			updateBasics(mMovie);
 		} else {
 			try {
 				int id = mDataManager.getMovie(DataManager.TYPE_CACHE, this, mDataType, movieId, DataLibRequest.OPTION_NO_OPTION, null, null);
 				synchronized (mRequestIds){
 					mRequestIds.add(id);
 				}
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 		}
 
 		if(mSynopsis != null) {
 			updateSynopsis(mSynopsis);
 		} else {
 			try {
 				int id = mDataManager.getSynopsis(DataManager.TYPE_CACHE, this, movieId, DataLibRequest.OPTION_NO_OPTION, null, null);
 				synchronized (mRequestIds){
 					mRequestIds.add(id);
 				}
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 		}
 
 		if(mCast != null) {
 			updateCast(mCast);
 		} else {
 			try {
 				int id = mDataManager.getCast(DataManager.TYPE_CACHE, this, movieId, DataLibRequest.OPTION_NO_OPTION, null, null);
 				synchronized (mRequestIds){
 					mRequestIds.add(id);
 				}
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return scrollView;
 	}
 
 	@Override
 	public void onCacheRequestFinished(int requestId, ResponseBusinessObject response) {
 
 		synchronized (mRequestIds) {
 
 			mRequestIds.remove(Integer.valueOf(requestId));
 
 			if(response instanceof MovieImage){
 				MovieImage movieImage = (MovieImage) response;
 
 				if(movieImage.image != null) {
 					Bitmap bmp = movieImage.image.get();
 					if(bmp != null)
 						getActivity().runOnUiThread(new UpdatePoster(bmp, mImage));
 				} else {
 					try {
 						int id = mDataManager.getMovieImage(DataManager.TYPE_NETWORK, this, mMovieItem.getImageUrl(), DataLibRequest.OPTION_NO_OPTION, null, null);
 						mRequestIds.add(id);
 
 					} catch (NumberFormatException e) {
 						e.printStackTrace();
 					} catch (UnsupportedEncodingException e) {
 						e.printStackTrace();
 					}
 
 
 				}
 
 			} else if(response instanceof Movie){
 
 				Movie movie = (Movie) response;
 
 				if(!movie.isInvalidID()){
 					
 					mMovie = movie;
 					getActivity().runOnUiThread(new UpdateContent(mMovie));
 
 				} else {
 					try {
 						int id = mDataManager.getMovie(DataManager.TYPE_NETWORK, this, mDataType, Integer.parseInt(mMovieItem.getId()), DataLibRequest.OPTION_NO_OPTION, null, null);
 						mRequestIds.add(id);
 
 					} catch (NumberFormatException e) {
 						e.printStackTrace();
 					} catch (UnsupportedEncodingException e) {
 						e.printStackTrace();
 					}
 				}
 
 			} else if(response instanceof Synopsis) {
 				
 				Synopsis synopsis = (Synopsis) response;
 				
 				if(!synopsis.isInvalidID()){
 					
 					mSynopsis = synopsis;
 					getActivity().runOnUiThread(new UpdateContent(mSynopsis));
 
 
 				} else {
 					try {
 						int id = mDataManager.getSynopsis(DataManager.TYPE_NETWORK, this, Integer.parseInt(mMovieItem.getId()), DataLibRequest.OPTION_NO_OPTION, null, null);
 						mRequestIds.add(id);
 
 					} catch (NumberFormatException e) {
 						e.printStackTrace();
 					} catch (UnsupportedEncodingException e) {
 						e.printStackTrace();
 					}
 				}
 
 			} else if(response instanceof Cast) {
 				
 				Cast cast = (Cast) response;
 
 				if(!cast.isInvalidID()){
 					mCast = cast;
 					getActivity().runOnUiThread(new UpdateContent(mCast));
 
 
 				} else {
 					try {
 						int id = mDataManager.getCast(DataManager.TYPE_NETWORK, this, Integer.parseInt(mMovieItem.getId()), DataLibRequest.OPTION_NO_OPTION, null, null);
 						mRequestIds.add(id);
 
 					} catch (NumberFormatException e) {
 						e.printStackTrace();
 					} catch (UnsupportedEncodingException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 
 		}
 	}
 
 	@Override
 	public void onDataFromDatabase(int code, ArrayList<?> data) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void onRequestFinished(int requestId, boolean suceed, BusinessResponse response) {
 
 		synchronized (mRequestIds) {
 			mRequestIds.remove(Integer.valueOf(requestId));
 		}
 		
 		if(!suceed){
 //			if(response.response instanceof ResponseBusinessObjectDAO){
 //				try {
 //					int id = mDataManager.getMovie(DataManager.TYPE_NETWORK, this, mDataType, Integer.parseInt(mMovieItem.getId()), DataLibRequest.OPTION_NO_OPTION, null, null);
 //					mRequestIds.add(id);
 //					
 //				} catch (NumberFormatException e) {
 //					e.printStackTrace();
 //				} catch (UnsupportedEncodingException e) {
 //					e.printStackTrace();
 //				}
 //			}
 //			return;
 		}
 		
 		switch (response.webserviceType) {
 		
 		case NetflixService.WEBSERVICE_MOVIE:
 
 			Movie movie = (Movie) response.response;
 			getActivity().runOnUiThread(new UpdateContent(movie));
 			
 			break;
 
 		case NetflixService.WEBSERVICE_SYNOPSIS:
 
 			Synopsis synopsis = (Synopsis) response.response;
 			getActivity().runOnUiThread(new UpdateContent(synopsis));
 			
 			break;
 			
 		case NetflixService.WEBSERVICE_CAST:
 
 			Cast cast = (Cast) response.response;
 			getActivity().runOnUiThread(new UpdateContent(cast));
 			
 			break;
 
 		default:
 			break;
 		}
 		
 	}
 	
 	public class UpdatePoster implements Runnable {
 
 		Bitmap mImage;
 		ImageView mView;
 		Animation mFadeIn;
 		
 		public UpdatePoster(Bitmap image, ImageView view){
 			mImage = image;
 			mView = view;
 			mFadeIn = new AlphaAnimation(0, 1);
 			mFadeIn.setDuration(300);
 		}
 		
 		@Override
 		public void run() {
 			Animation anim = mView.getAnimation();
 			if(anim != null){
 				anim.cancel();
 				anim.reset();
 				mView.setImageBitmap(mImage);
 				anim.startNow();
 			} else {
 				mView.startAnimation(mFadeIn);
 			}
 		}
 	}
 
 	public class UpdateContent implements Runnable {
 
 		Movie mMovie;
 		Synopsis mSynopsis;
 		Cast mCast;
 		
 		public UpdateContent(Movie movie){
 			mMovie = movie;
 		}
 
 		public UpdateContent(Synopsis synopsis){
 			mSynopsis = synopsis;
 		}
 		
 		public UpdateContent(Cast cast){
 			mCast = cast;
 		}
 
 		@Override
 		public void run() {
 			
 			
 			if(mMovie != null) {
 				updateBasics(mMovie);				
 			}
 			
 			if(mSynopsis != null) {
 				updateSynopsis(mSynopsis);
 			}
 
 			if(mCast != null) {
 				updateCast(mCast);
 			}
 		}
 	}
 	
 	private void updateSynopsis(Synopsis synopsis) {
 		if(synopsis != null && synopsis.synopsis != null)
 			mTxtSynopsis.setText(Html.fromHtml(synopsis.synopsis));
 	}
 	
 
 	private void updateCast(Cast cast) {
 		if(cast != null && cast.castPerson != null){
 			ArrayList<CastPerson> persons = cast.castPerson;
 			int size = persons.size();
 			if(size > 10)
 				size = 10;
 			
 			StringBuilder builderLeft = new StringBuilder();
 			StringBuilder builderRight = new StringBuilder();
 			StringBuilder builder = builderLeft;
 			
 			for (int i = 0; i < size; i++) {
 				CastPerson person = persons.get(i);
 				
 				if(i%2 == 0)
 					builder = builderLeft;
 				else
 					builder = builderRight;
 
 				if(i > 1)
 					builder.append('\n');
 
 				builder.append(person.name);
 				
 			}
 			
 			if(size%2 == 1)
 				builderRight.append('\n');
 			
 			mTxtCast1.setText(builderLeft);
 			mTxtCast2.setText(builderRight);
 		}
 		
 	}
 	
 	private void updateBasics(Movie movie) {
 		if(mTxtTitle.getText().length() < movie.attrTitleRegular.length())
 			mTxtTitle.setText(movie.attrTitleRegular);
 		
 		if(movie.release_year > 0)
 			mTxtYear.setText(""+movie.release_year);
 		else
 			mTxtYear.setText("N.C.");
 		
 		int runtime = movie.runtime;
 //		int hours = runtime/3600;
 		int minutes = runtime/60; //(runtime%3600)/60;
 		int seconds = runtime%60;
 		
 		StringBuilder builder = new StringBuilder();
 //				if(hours > 0){
 //					builder.append(hours);
 //					builder.append("h ");
 //				}
 //				if(hours > 0 || minutes > 0) {
 //					builder.append(minutes);
 //					builder.append("m ");
 //				}
 //				if(hours <= 0) {
 //					builder.append(seconds);
 //					builder.append("s");
 //				}
 		if(minutes > 0) {
 			builder.append(minutes);
 			builder.append(" minutes");
 		} else if(seconds > 0) {
 			builder.append(seconds);
 			builder.append(" seconds");
 		} else {
 			builder.append("N.C.");
 		}
 		
 		mTxtTime.setText(builder.toString());
 		
 		ArrayList<MovieCategory> categories = movie.movieCategory;
 		
 		if(categories != null) {
 			if(categories.size() > 1)
 				mTxtCategory.setText(categories.get(1).attrLabel);
 			else
 				mTxtCategory.setText("");			
 		}
 	}
 }
