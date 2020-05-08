 package se.purestyle.beatr.view.generic;
 
 import se.purestyle.beatr.model.generic.KnobModel;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.view.View;
 
 public class KnobView extends View {
 	
 	private KnobModel model;
 	
 	public KnobView( Context context ) {
 		
 		super( context );
 	}
 	
 	public void setModel( KnobModel model ) {
 		
 		this.model = model;
 	}
 	
 	@Override
 	public void onDraw( Canvas canvas ) {
 		
 		Paint p = new Paint();
 		p.setColor( Color.parseColor( "#434343" ) );
 		p.setAntiAlias( true );
 		
		canvas.drawCircle( getWidth() / 2, getHeight() / 2, getWidth() / 2, p ); //( 0, 0, getWidth(), getHeight(), p );
 		
 		Paint linePaint = new Paint();
 		linePaint.setColor( Color.parseColor( "#00ffe4" ) );
 		//linePaint.setAlpha( 200 );
 		linePaint.setAntiAlias( true );
 		
 		canvas.save();
 		
 		int[] apa = new int[ 2 ];
 		getLocationOnScreen( apa );
 		
 		Matrix m = new Matrix();
 		m.preTranslate( apa[0] * 1.5f + (getWidth() / 2) * 1.5f, apa[1] * 1.5f + (getHeight() / 2) * 1.5f );
 		m.preRotate( model.getCurrentAngle() + 45 );
 		
 		canvas.setMatrix( m );
 		canvas.drawCircle( 0, getHeight() / 2, 10, linePaint );
 		
 		canvas.restore();
 	}
 }
