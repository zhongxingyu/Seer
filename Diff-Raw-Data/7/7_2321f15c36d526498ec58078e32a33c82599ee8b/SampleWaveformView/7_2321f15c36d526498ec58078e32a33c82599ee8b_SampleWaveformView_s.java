 package com.kh.beatbot.view;
 
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 
 import com.kh.beatbot.R;
 import com.kh.beatbot.global.GlobalVars;
 import com.kh.beatbot.manager.Managers;
 import com.kh.beatbot.view.bean.MidiViewBean;
 import com.kh.beatbot.view.helper.WaveformHelper;
 
 public class SampleWaveformView extends SurfaceViewBase {
 
 	private static final float[] ADSR_COLOR = MidiViewBean.PITCH_COLOR.clone();
 	private static final float[] ADSR_SELECTED_COLOR = {ADSR_COLOR[0], ADSR_COLOR[1], ADSR_COLOR[2], .6f};
 	private static final float[] LOOP_HIGHLIGHT_COLOR = { 1, .64706f, 0, .4f };
 	private static final float[] LOOP_SELECTION_LINE_COLOR = { 1, 1, 1, 1 };
 	private static final float[] LOOP_SELECTION_RECT_COLOR = { .9f, .9f, 1, .5f };
 	private static final float[] LOOP_SELECTION_RECT_SELECT_COLOR = { MidiViewBean.VOLUME_COLOR[0],
 																	  MidiViewBean.VOLUME_COLOR[1], 
 																	  MidiViewBean.VOLUME_COLOR[2], .6f };
 	private static final float[] BG_COLOR = {LOOP_HIGHLIGHT_COLOR[0] * .5f, LOOP_HIGHLIGHT_COLOR[1] * .5f,
 			LOOP_HIGHLIGHT_COLOR[2] * .5f, 1};
 
 	private static final float ADSR_POINT_RADIUS = 5;
 	// min distance for pointer to select loop markers
 	private static final int SNAP_DIST = 32;
 	private static final int SNAP_DIST_SQUARED = 1024;
 	private FloatBuffer waveformVb = null;
 	private FloatBuffer backgroundOutlineVb = null;
 	private FloatBuffer previewButtonSquareVb = null;
 	private FloatBuffer loopSelectionLineVb = null;
 	private FloatBuffer loopSelectionRectVbs[] = new FloatBuffer[2];
 	private FloatBuffer adsrPointVb = null;
 	private FloatBuffer[] adsrCurveVb = new FloatBuffer[4];
 
 	// keep track of which pointer ids are selecting which ADSR points
 	// init to -1 to indicate no pointer is selecting
 	private int[] adsrSelected = new int[] { -1, -1, -1, -1, -1 };
 
 	private boolean showAdsr = false; // show ADSR points?
 
 	private final int MIN_LOOP_WINDOW = 32;
 	// the left of this view is a preview button
 	private float previewButtonWidth, waveformWidth;
 
 	private int trackNum;
 
 	private int numSamples = 0;
 
 	// which pointer id is touching which marker (-1 means no pointer)
 	private int beginLoopMarkerTouched = -1;
 	private int endLoopMarkerTouched = -1;
 
 	// keep track of which finger is touching the preview button,
 	// so we can handle pointer-up/ move events outside the button
 	private int previewPointerId = -1;
 
 	private float zoomLeftAnchorSample = -1;
 	private float zoomRightAnchorSample = -1;
 
 	private float scrollAnchorSample = -1;
 
 	private float[] samples = null;
 	// zooming/scrolling will change the view window of the samples
 	// keep track of that with offset and width
 	private float sampleOffset = 0;
 	private float sampleWidth;
 
 	public SampleWaveformView(Context c, AttributeSet as) {
 		super(c, as);
 	}
 
 	public void setTrackNum(int trackNum) {
 		this.trackNum = trackNum;
 	}
 
 	public void setSamples(float[] samples) {
 		this.samples = samples;
 		numSamples = samples.length;
 		if (GlobalVars.sampleLoopEnd[trackNum] == 0)
 			GlobalVars.sampleLoopEnd[trackNum] = numSamples;
 		sampleWidth = numSamples;
 		if (height != 0) {
 			waveformVb = WaveformHelper.floatsToFloatBuffer(samples, height, 0);
 		}
 	}
 
 	public void setShowAdsr(boolean on) {
 		showAdsr = on;
 	}
 
 	private void initAdsrVb() {
 		float[] pointVertices = new float[10];
 		for (int i = 0; i < 5; i++) {
 			for (int j = 0; j < 2; j++) {
 				pointVertices[i * 2 + j] = j == 1 ? adsrToY(GlobalVars.adsrPoints[trackNum][i][j]) :
 					adsrToX(GlobalVars.adsrPoints[trackNum][i][j]);
 			}
 		}
 		adsrPointVb = makeFloatBuffer(pointVertices);
 		for (int i = 0; i < 4; i++) {
 			ArrayList<Float> curveVertices = new ArrayList<Float>();
 			curveVertices
 					.addAll(makeExponentialCurveVertices(pointVertices[i * 2],
 							pointVertices[i * 2 + 1],
 							pointVertices[(i + 1) * 2],
 							pointVertices[(i + 1) * 2 + 1]));
 			float[] converted = new float[curveVertices.size()];
 			for (int j = 0; j < curveVertices.size(); j++) {
 				converted[j] = curveVertices.get(j);
 			}
 			adsrCurveVb[i] = makeFloatBuffer(converted);
 		}
 	}
 
 	private void initBackgroundOutlineVb() {
 		backgroundOutlineVb = makeRectOutlineFloatBuffer(previewButtonWidth, 0, width - 2, height);
 	}
 
 	private void initPreviewButtonSquareVb() {
 		previewButtonSquareVb = makeRectFloatBuffer(0, 0, previewButtonWidth,
 				previewButtonWidth);
 	}
 	
 	private void initLoopMarkerVb() {
 		float xLoopBegin = sampleToX(GlobalVars.sampleLoopBegin[trackNum]);
 		float xLoopEnd = sampleToX(GlobalVars.sampleLoopEnd[trackNum]);
 		float[] loopSelectionVertices = { xLoopBegin, 0, xLoopBegin, height,
 				xLoopEnd, height, xLoopEnd, 0 };
 
 		loopSelectionLineVb = makeFloatBuffer(loopSelectionVertices);
 		loopSelectionRectVbs[0] = makeRectFloatBuffer(xLoopBegin - SNAP_DIST
 				/ 2, height, xLoopBegin + SNAP_DIST / 2, 0);
 		loopSelectionRectVbs[1] = makeRectFloatBuffer(xLoopEnd - SNAP_DIST / 2,
 				height, xLoopEnd + SNAP_DIST / 2, 0);
 	}
 
 	private ArrayList<Float> makeExponentialCurveVertices(float x1, float y1,
 			float x2, float y2) {
 		ArrayList<Float> vertices = new ArrayList<Float>();
 		// fake it w/ Bezier curve
 		for (float t = 0; t <= 1; t += 0.05) {
 			float bezierX = x1;
 			float bezierY = y2;
 			vertices.add((1 - t) * (1 - t) * x1 + 2 * (1 - t) * t * bezierX + t
 					* t * x2);
 			vertices.add((1 - t) * (1 - t) * y1 + 2 * (1 - t) * t * bezierY + t
 					* t * y2);
 		}
 		vertices.add(x2);
 		vertices.add(y2);
 		return vertices;
 	}
 
 	private void drawWaveform() {
 		if (waveformVb == null)
 			return;
 		float scale = (waveformWidth)
 				/ ((float) sampleWidth);
 		float translate = -sampleOffset;
 		float[] color = MidiViewBean.VOLUME_COLOR;
 		gl.glLineWidth(10);
 		gl.glEnable(GL10.GL_LINE_SMOOTH);
 		gl.glColor4f(color[0], color[1], color[2], .9f);
 		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, waveformVb);
 		gl.glPushMatrix();
 		// scale drawing so the entire waveform exactly fits in the view
 		gl.glTranslatef(previewButtonWidth + SNAP_DIST / 2, 0, 0);
 		gl.glScalef(scale, 1, 1);
 		gl.glTranslatef(translate, 0, 0);
 		gl.glDrawArrays(GL10.GL_LINE_STRIP, (int) (sampleOffset),
 				(int) (sampleWidth));
 		gl.glDisable(GL10.GL_LINE_SMOOTH);
 		gl.glPopMatrix();
 	}
 
 	private void drawLoopSelectionMarkers() {
 		if (loopSelectionLineVb == null)
 			return;
 		drawLines(loopSelectionLineVb, LOOP_SELECTION_LINE_COLOR, 2,
 				GL10.GL_LINES);
 		drawTriangleFan(loopSelectionLineVb, LOOP_HIGHLIGHT_COLOR);
 		drawTriangleStrip(loopSelectionRectVbs[0], beginLoopMarkerTouched == -1 ? LOOP_SELECTION_RECT_COLOR : LOOP_SELECTION_RECT_SELECT_COLOR);
 		drawTriangleStrip(loopSelectionRectVbs[1], endLoopMarkerTouched == -1 ? LOOP_SELECTION_RECT_COLOR : LOOP_SELECTION_RECT_SELECT_COLOR);
 	}
 
 	private void drawAdsr() {
 		if (!showAdsr)
 			return;
 		setColor(ADSR_COLOR);
 		gl.glPointSize(ADSR_POINT_RADIUS * 2);
 		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, adsrPointVb);
 		gl.glDrawArrays(GL10.GL_POINTS, 0, 5);
 		for (int i = 0; i < 5; i++) {
 			if (adsrSelected[i] != -1) {
 				gl.glPointSize(ADSR_POINT_RADIUS * 4);
 				setColor(ADSR_SELECTED_COLOR);
 				gl.glDrawArrays(GL10.GL_POINTS, i, 1);
 			}
 		}
 		for (int i = 0; i < adsrCurveVb.length; i++) {
 			drawLines(adsrCurveVb[i], ADSR_COLOR, 3, GL10.GL_LINE_STRIP);
 		}
 	}
 
 	@Override
 	protected void init() {
 		// preview button is 80dpX80dp, but that is not the same as a height of
 		// '80'. 80dp will be the height
 		previewButtonWidth = height;
 		waveformWidth = width - previewButtonWidth - SNAP_DIST;
 		while (samples == null)
 			; // wait until we're sure the sample bytes have been set
 		waveformVb = WaveformHelper.floatsToFloatBuffer(samples, height, 0);
 		loadTexture(R.drawable.preview_icon4, 0);
 		loadTexture(R.drawable.preview_icon_selected4, 1);
 		initBackgroundOutlineVb();
 		initPreviewButtonSquareVb();
 		initLoopMarkerVb();
 		initAdsrVb();
 	}
 
 	private float sampleToX(float sample) {
 		return (sample - sampleOffset) * waveformWidth / sampleWidth
 				+ previewButtonWidth + SNAP_DIST / 2;
 	}
 
 	private float xToSample(float x) {
 		return (x - previewButtonWidth - SNAP_DIST / 2) * sampleWidth
 				/ waveformWidth + sampleOffset;
 	}
 
 	private float adsrToX(float adsr) {
 		return sampleToX(GlobalVars.sampleLoopBegin[trackNum]) * (1 - adsr) + adsr
 				* sampleToX(GlobalVars.sampleLoopEnd[trackNum]);
 	}
 
 	private float adsrToY(float adsr) {
 		return -(adsr - 1)*(height - 2 * ADSR_POINT_RADIUS) + ADSR_POINT_RADIUS;
 	}
 	
 	private float xToAdsr(float x) {
 		return (x - sampleToX(GlobalVars.sampleLoopBegin[trackNum]))
 				/ (sampleToX(GlobalVars.sampleLoopEnd[trackNum]) - sampleToX(GlobalVars.sampleLoopBegin[trackNum]));
 	}
 
 	private float yToAdsr(float y) {
 		// clip y to half an adsr circle above 0 and half a circle below height
 		y = y > ADSR_POINT_RADIUS ? (y < height - ADSR_POINT_RADIUS ? y
 				: height - ADSR_POINT_RADIUS) : ADSR_POINT_RADIUS;
 		return 1 - (y - ADSR_POINT_RADIUS) / (height - 2 * ADSR_POINT_RADIUS);
 	}
 
 	private void updateSampleOffset(float scrollX) {
 		// set sampleOffset such that the scroll anchor sample stays under
 		// scrollX
 		float newSampleOffset = scrollAnchorSample - xToSample(scrollX)
 				+ sampleOffset;
 		sampleOffset = newSampleOffset < 0 ? 0
 				: (newSampleOffset + sampleWidth > numSamples ? numSamples
 						- sampleWidth : newSampleOffset);
 	}
 
 	private void updateZoom(float x1, float x2) {
 		// set sampleOffset and sampleWidth such that the zoom
 		// anchor samples stay under x1 and x2
 		float newSampleWidth = waveformWidth
 				* (zoomRightAnchorSample - zoomLeftAnchorSample) / (x2 - x1);
 		float newSampleOffset = zoomRightAnchorSample - newSampleWidth
 				* (x2 - previewButtonWidth - SNAP_DIST / 2) / waveformWidth;
 		if (newSampleOffset < 0
 				&& newSampleOffset + newSampleWidth > numSamples
 				|| newSampleWidth < MIN_LOOP_WINDOW) {
 			return;
 		}
 		if (newSampleOffset >= 0
 				&& newSampleOffset + newSampleWidth <= numSamples) {
 			sampleOffset = newSampleOffset;
 			sampleWidth = newSampleWidth;
 		} else if (newSampleOffset < 0) {
 			sampleOffset = 0;
 			sampleWidth = zoomRightAnchorSample * waveformWidth
 					/ (x2 - previewButtonWidth);
 		} else if (newSampleOffset + newSampleWidth > numSamples) {
 			sampleWidth = waveformWidth * (zoomLeftAnchorSample - numSamples)
 					/ (x1 - previewButtonWidth - waveformWidth);
 			sampleOffset = numSamples - sampleWidth;
 		}
 	}
 
 	private void updateLoopMarkers() {
 		float diff = 0;
 		if (GlobalVars.sampleLoopBegin[trackNum] < sampleOffset
 				&& GlobalVars.sampleLoopEnd[trackNum] > sampleOffset + sampleWidth) {
 			clipLoopToWindow();
 		} else if (GlobalVars.sampleLoopBegin[trackNum] < sampleOffset)
 			diff = sampleOffset - GlobalVars.sampleLoopBegin[trackNum];
 		else if (GlobalVars.sampleLoopEnd[trackNum] >= sampleOffset + sampleWidth)
 			diff = sampleOffset + sampleWidth - GlobalVars.sampleLoopEnd[trackNum];
 		if (diff != 0) {
 			GlobalVars.sampleLoopBegin[trackNum] += diff;
 			GlobalVars.sampleLoopEnd[trackNum] += diff;
 			clipLoopToWindow();
 		}
 		Managers.playbackManager.setLoopWindow(trackNum, (int) GlobalVars.sampleLoopBegin[trackNum],
 				(int) GlobalVars.sampleLoopEnd[trackNum]);
 		// update the display location of the loop markers
 		initLoopMarkerVb();
 		initAdsrVb();
 	}
 
 	@Override
	protected void fillBackground() {
		setBackgroundColor(BG_COLOR);
	}
	
	@Override
 	protected void drawFrame() {
 		drawTriangleStrip(previewButtonSquareVb, GlobalVars.BG_COLOR);
 		drawTexture(height, height);
 		drawWaveform();
 		drawLines(backgroundOutlineVb, GlobalVars.WHITE, 4, GL10.GL_LINE_LOOP);
 		drawLoopSelectionMarkers();
 		if (showAdsr)
 			drawAdsr();
 	}
 
 	private boolean selectAdsrPoint(int id, float x, float y) {
 		if (!showAdsr)
 			return false;
 		for (int i = 0; i < 5; i++) {
 			if (distanceFromPointSquared(adsrToX(GlobalVars.adsrPoints[trackNum][i][0]),
 					adsrToY(GlobalVars.adsrPoints[trackNum][i][1]), x, y) < SNAP_DIST_SQUARED) {
 				adsrSelected[i] = id;
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private float distanceFromPointSquared(float pointX, float pointY, float x, float y) {
 		return (x - pointX)*(x - pointX) + (y - pointY)*(y - pointY);
 	}
 	
 	private boolean selectLoopMarker(int id, float x) {
 		if (Math.abs(x - sampleToX(GlobalVars.sampleLoopBegin[trackNum])) < SNAP_DIST) {
 			// begin loop marker touched
 			beginLoopMarkerTouched = id;
 			return true;
 		} else if (Math.abs(x - sampleToX(GlobalVars.sampleLoopEnd[trackNum])) < SNAP_DIST) {
 			// end loop marker touched
 			endLoopMarkerTouched = id;
 			return true;
 		}
 		return false;
 	}
 
 	private void deselectLoopMarker(int id) {
 		if (beginLoopMarkerTouched == id) {
 			beginLoopMarkerTouched = -1;
 		} else if (endLoopMarkerTouched == id) {
 			endLoopMarkerTouched = -1;
 		}
 	}
 
 	private void deselectAdsrPoint(int id) {
 		for (int i = 0; i < adsrSelected.length; i++) {
 			if (adsrSelected[i] == id)
 				adsrSelected[i] = -1;
 		}
 	}
 
 	private void clearAdsrSelected() {
 		for (int i = 0; i < adsrSelected.length; i++) {
 			adsrSelected[i] = -1;
 		}
 	}
 
 	private boolean moveAdsrPoint(int id, float x, float y) {
 		for (int i = 0; i < adsrSelected.length; i++) {
 			if (adsrSelected[i] == id) {
 				float adsrX = xToAdsr(x);
 				float adsrY = yToAdsr(y);
 				float prevX = i >= 2 ? GlobalVars.adsrPoints[trackNum][i - 1][0] : 0;
 				float nextX = i <= 3 ? GlobalVars.adsrPoints[trackNum][i + 1][0] : 1;
 				if (i == 0)
 					adsrX = 0;
 				// ADSR samples cannot go past the next ADSR sample or before
 				// the previous sample
 				GlobalVars.adsrPoints[trackNum][i][0] = adsrX > prevX ? (adsrX < nextX ? adsrX
 						: nextX) : prevX;
 				if (i != 3) // can only change the x coord of the cutoff point
 					GlobalVars.adsrPoints[trackNum][i][1] = adsrY > 0 ? (adsrY < 1 ? adsrY : 1) : 0;
 				// points 2 and 3 must have the same y value, since these are
 				// the two ends
 				// of the sustain level, which must be linear.
 				// ie. adjusting either 2 or 3 will adjust both points' y values
 				if (i == 2)
 					GlobalVars.adsrPoints[trackNum][3][1] = GlobalVars.adsrPoints[trackNum][2][1];
 				initAdsrVb();
 				setAdsrPoint(trackNum, i, GlobalVars.adsrPoints[trackNum][i][0],
 						GlobalVars.adsrPoints[trackNum][i][1]);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean moveLoopMarker(int id, float x) {
 		if (beginLoopMarkerTouched == id) {
 			float newLoopBegin = xToSample(x);
 			GlobalVars.sampleLoopBegin[trackNum] = newLoopBegin < 0 ? 0
 					: (newLoopBegin >= GlobalVars.sampleLoopEnd[trackNum] - MIN_LOOP_WINDOW ? GlobalVars.sampleLoopEnd[trackNum]
 							- MIN_LOOP_WINDOW
 							: newLoopBegin);
 		} else if (endLoopMarkerTouched == id) {
 			float newLoopEnd = xToSample(x);
 			GlobalVars.sampleLoopEnd[trackNum] = newLoopEnd >= numSamples ? numSamples - 1
 					: (newLoopEnd <= GlobalVars.sampleLoopBegin[trackNum] + MIN_LOOP_WINDOW ? GlobalVars.sampleLoopBegin[trackNum]
 							+ MIN_LOOP_WINDOW
 							: newLoopEnd);
 		} else {
 			return false;
 		}
 		if (GlobalVars.sampleLoopBegin[trackNum] < sampleOffset) {
 			sampleOffset = GlobalVars.sampleLoopBegin[trackNum];
 			if (GlobalVars.sampleLoopEnd[trackNum] > sampleOffset + sampleWidth)
 				sampleWidth = GlobalVars.sampleLoopEnd[trackNum] - GlobalVars.sampleLoopBegin[trackNum];
 		} else if (GlobalVars.sampleLoopEnd[trackNum] > sampleOffset + sampleWidth) {
 			sampleWidth = GlobalVars.sampleLoopEnd[trackNum] - sampleOffset;
 			if (sampleWidth + sampleOffset > numSamples)
 				sampleWidth = numSamples - sampleOffset;
 		}
 		// update UI
 		initLoopMarkerVb();
 		initAdsrVb();
 		// update sample playback.
 		Managers.playbackManager.setLoopWindow(trackNum, (int) GlobalVars.sampleLoopBegin[trackNum],
 				(int) GlobalVars.sampleLoopEnd[trackNum]);
 		return true;
 	}
 
 	private boolean setZoomAnchor(float x) {
 		if (scrollAnchorSample != -1) {
 			// one pointer is already scrolling.
 			zoomLeftAnchorSample = Math.min(scrollAnchorSample, xToSample(x));
 			zoomRightAnchorSample = Math.max(scrollAnchorSample, xToSample(x));
 			scrollAnchorSample = -1; // not scrolling anymore
 			return true;
 		}
 		return false;
 	}
 
 	private void setScrollAnchor(float x) {
 		scrollAnchorSample = xToSample(x);
 	}
 
 	private boolean scroll(float scrollX) {
 		if (scrollAnchorSample == -1)
 			return false; // not scrolling
 		updateSampleOffset(scrollX);
 		return true;
 	}
 
 	private boolean zoom(float x1, float x2) {
 		if (zoomLeftAnchorSample == -1 || zoomRightAnchorSample == -1)
 			return false; // not zooming
 		float leftX = Math.min(x1, x2);
 		float rightX = Math.max(x1, x2);
 		updateZoom(leftX, rightX);
 		return true;
 	}
 
 	private void clipLoopToWindow() {
 		if (GlobalVars.sampleLoopBegin[trackNum] < sampleOffset)
 			GlobalVars.sampleLoopBegin[trackNum] = sampleOffset;
 		if (GlobalVars.sampleLoopEnd[trackNum] > sampleOffset + sampleWidth)
 			GlobalVars.sampleLoopEnd[trackNum] = sampleOffset + sampleWidth;
 	}
 
 	private void handleWaveActionDown(int id, float x, float y) {
 		if (!selectAdsrPoint(id, x, y) && !selectLoopMarker(id, x)) {
 			// no ADSR points are close enough, and
 			// loop marker not close enough to select. start scroll
 			// (we know it's the first pointer down, so we're not zooming)
 			setScrollAnchor(x);
 		}
 	}
 
 	private void handleWaveActionPointerDown(int id, float x, float y) {
 		if (!selectAdsrPoint(id, x, y) && !selectLoopMarker(id, x)
 				&& !setZoomAnchor(x)) {
 			// loop marker not close enough to select, and first pointer down.
 			// start scrolling
 			setScrollAnchor(x);
 		}
 	}
 
 	private void handleWaveActionPointerUp(MotionEvent e, int id) {
 		deselectLoopMarker(id);
 		deselectAdsrPoint(id);
 		scrollAnchorSample = -1;
 		// stop zooming
 		if (zoomLeftAnchorSample != -1 && zoomRightAnchorSample != -1) {
 			int otherId = (id + 1) % 2;
 			if (otherId != previewPointerId)
 				setScrollAnchor(e.getX(otherId));
 		}
 		zoomLeftAnchorSample = zoomRightAnchorSample = -1;
 	}
 
 	public void handlePreviewActionDown(int id) {
 		currentTexture = 1;
 		Managers.playbackManager.playTrack(trackNum);
 		previewPointerId = id;
 	}
 
 	public void handlePreviewActionPointerDown(int id) {
 		handlePreviewActionDown(id);
 	}
 
 	@Override
 	protected void handleActionDown(int id, float x, float y) {
 		if (x > previewButtonWidth)
 			handleWaveActionDown(id, x, y);
 		else
 			handlePreviewActionDown(id);
 	}
 
 	@Override
 	protected void handleActionPointerDown(MotionEvent e, int id, float x,
 			float y) {
 		if (x > previewButtonWidth)
 			handleWaveActionPointerDown(id, x, y);
 		else
 			handlePreviewActionPointerDown(id);
 	}
 
 	@Override
 	protected void handleActionMove(MotionEvent e) {
 		if (zoomLeftAnchorSample != -1 && zoomRightAnchorSample != -1
 				&& previewPointerId == -1)
 			zoom(e.getX(0), e.getX(1));
 		for (int i = 0; i < e.getPointerCount(); i++) {
 			int id = e.getPointerId(i);
 			if (id == previewPointerId) {
 				if (e.getX(i) > previewButtonWidth) {
 					// if a click goes down on the preview button,
 					// then moves out of it into wave, stop previewing
 					previewPointerId = -1;
 					Managers.playbackManager.stopTrack(trackNum);
 				}
 			} else if (!moveAdsrPoint(id, e.getX(i), e.getY(i))
 					&& !moveLoopMarker(id, e.getX(i)))
 				scroll(e.getX(i));
 		}
 		updateLoopMarkers();
 	}
 
 	@Override
 	protected void handleActionPointerUp(MotionEvent e, int id, float x, float y) {
 		if (x > previewButtonWidth)
 			handleWaveActionPointerUp(e, id);
 		else {
 			currentTexture = 0;
 			Managers.playbackManager.stopTrack(trackNum);
 		}
 	}
 
 	@Override
 	protected void handleActionUp(int id, float x, float y) {
 		currentTexture = 0;
 		beginLoopMarkerTouched = endLoopMarkerTouched = -1;
 		Managers.playbackManager.stopTrack(trackNum);
 		scrollAnchorSample = zoomLeftAnchorSample = zoomRightAnchorSample = -1;
 		clearAdsrSelected();
 	}
 
 	// set the native adsr point. x and y range from 0 to 1
 	public native void setAdsrPoint(int trackNum, int adsrPointNum, float x,
 			float y);
 }
