 package com.sosobro.enchroma;
 
 // TODO: Refactor code, pull from PhotoView to CameraPhotoView
 // TODO: Implement SelectPhotoView
 
 import java.io.File;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.provider.MediaStore;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.ImageView;
 
 public class PhotoView extends ImageView
 	implements View.OnClickListener {
 
 	private Activity _activity;
 	private int _reqId;
 	private SavedState _ss = new SavedState();
 
 	public static class SavedState extends BaseSavedState {
 		public File photoFile;
 		public Bitmap thumb;
 
 		public SavedState( ) {
 			super( new Bundle() );
 		}
 		
 		public SavedState( Parcelable p, SavedState ss ) {
 			super( p );
 			photoFile = ss.photoFile;
 			thumb = ss.thumb;
 		}
 		
 		public SavedState( Parcel in ) {
 			super( in );
 			
 			{ // photoFile
 				String s = in.readString();
 				photoFile = s.isEmpty() ? null : new File( s ); 
 			}
 			
 			thumb = in.readParcelable( Bitmap.class.getClassLoader() );
 			
 			printState("Restored state:");
 		}
 
 		@Override
 		public void writeToParcel( Parcel out, int flags ) {
 			super.writeToParcel( out, flags );
 			out.writeString( (photoFile == null) ? "" : photoFile.toString() );
 			out.writeParcelable( thumb, 0 );
 			
 			printState("Saved state:");
 		}
 
 		private void printState( String header ) {
 			System.out.println( header );
 			System.out.printf( "photoFile=%s\n", photoFile==null ? "null" : photoFile.toString() );
 			System.out.printf( "thumb=%d\n", thumb==null ? 0 : thumb.getByteCount() );
 		}
 		
 		// required field that makes Parcelables from a Parcel
 		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
 			public SavedState createFromParcel( Parcel in ) { return new SavedState( in ); }
 			public SavedState[] newArray( int size ) { return new SavedState[size]; }
 		};
 	}
 	
 	public PhotoView( Context context, AttributeSet attrs ) {
 		super( context, attrs );
 		setOnClickListener( this );
 	}
 
 	public void init( Activity a, int reqId ) {
 		_activity = a;
 		_reqId = reqId;
 	}
 	
 	@Override
 	public Parcelable onSaveInstanceState( ) {
 		return new SavedState( super.onSaveInstanceState(), _ss );
 	}
 
 	@Override
 	public void onRestoreInstanceState( Parcelable state ) {
 		_ss = (SavedState) state;
 		super.onRestoreInstanceState( _ss.getSuperState() );
 
 		if (_ss.thumb != null) {
 			setImageBitmap( _ss.thumb );
 		}
 	}
 
 	@Override
 	public void onClick( View v ) {
 		_ss.photoFile = FileUtils.instance.createSubjectFilePath( _activity );
 		
 		Intent i = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
 		i.putExtra( MediaStore.EXTRA_OUTPUT, Uri.fromFile( _ss.photoFile ) );
 		_activity.startActivityForResult( i, _reqId );
 	}
 
 	public void onActivityResult( int resultCode, Intent data ) {
 		// Camera activity with filename provided provides no data.
 		if (resultCode == Activity.RESULT_OK) {
 			_ss.thumb = ThumbnailBuilder.instance.createThumbnail( _ss.photoFile );
 		}
 	}
 }
