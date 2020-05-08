 package edu.ncsu.uhp.escape.engine.utilities;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Context;
 import edu.ncsu.uhp.escape.engine.utilities.math.Point;
 
 public class MultiRenderableSource extends RenderSource {
 	// private Resources res;
 	private Point offsets;
 	private RenderSource[] sources;
 
	public MultiRenderableSource(int id, RenderSource[] sources) {
 		super(id);
 		this.sources = sources;
 	}
 
 	@Override
 	public MultiRenderable loadData(Context context, GL10 gl) {
 		IRenderable[] renderables = new IRenderable[sources.length];
 		for (int i = 0; i < sources.length; i++) {
 			renderables[i] = sources[i].loadData(context, gl);
 		}
 		return new MultiRenderable(renderables, offsets);
 	}
 
 	public Point getOffsets() {
 		return offsets;
 	}
 
 }
