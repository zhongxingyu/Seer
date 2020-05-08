 package com.aragaer.reminder;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class DrawDialogLayout extends ViewGroup {
 	boolean horizontal;
	int padding_x = 0, padding_y = 0, cs_s = 0;
 
 	public DrawDialogLayout(Context context) {
 		this(context, null);
 	}
 
 	public DrawDialogLayout(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		final int w = MeasureSpec.getSize(widthMeasureSpec);
 		final int h = MeasureSpec.getSize(heightMeasureSpec);
 
 		// We have exactly 4 children right now
 		// - colorswitch,
 		// - variable size drawing area,
 		// - checkbox and
 		// - "OK" button
 		// Start with figuring out how much space button and checkbox will take
 		final View cs = getChildAt(0), grid = getChildAt(1), chk = getChildAt(2), btn = getChildAt(3);
 		chk.measure(widthMeasureSpec, heightMeasureSpec);
 		btn.measure(widthMeasureSpec, heightMeasureSpec);
 
 		final int extra_w = Math.max(chk.getMeasuredWidth(),
 				btn.getMeasuredWidth());
 		final int extra_h = chk.getMeasuredHeight() + btn.getMeasuredHeight();
 
 		int space_w = w - extra_w; // how much we can take for grid if we use horizontal layout
 		int space_h = h - extra_h; // same for vertical
 
 		// There's no point in having more space in one dimension than the whole
 		// other dimension
 		space_w = Math.min(space_w, h);
 		space_h = Math.min(space_h, w);
 
 		// We've got all the sizes now
 		// Still we'll take all the space provided, so it's time to find out how
 		// much spacing we have
 		horizontal = space_h < space_w;
 		int grid_spec, cs_w_spec, cs_h_spec;
 		int my_w, my_h;
 		if (horizontal) {
 			grid_spec = MeasureSpec.makeMeasureSpec(space_w, MeasureSpec.AT_MOST);
 			my_w = space_w + extra_w;
 			my_h = Math.max(space_w, extra_h);
 		} else {
 			grid_spec = MeasureSpec.makeMeasureSpec(space_h, MeasureSpec.AT_MOST);
 			my_w = Math.max(space_h, extra_w);
 			my_h = space_h + extra_h;
 		}
 		padding_x = w - my_w;
 		padding_y = h - my_h;
 		if (horizontal) {
 			cs_s = Math.min(padding_x, my_h / Bitmaps.N_COLORS);
 			padding_x -= cs_s;
 			cs_h_spec = MeasureSpec.makeMeasureSpec(my_h, MeasureSpec.EXACTLY);
 			cs_w_spec = MeasureSpec.makeMeasureSpec(cs_s, MeasureSpec.EXACTLY);
 		} else {
 			cs_s = Math.min(padding_y, my_w / Bitmaps.N_COLORS);
 			padding_y -= cs_s;
 			cs_h_spec = MeasureSpec.makeMeasureSpec(cs_s, MeasureSpec.EXACTLY);
 			cs_w_spec = MeasureSpec.makeMeasureSpec(my_w, MeasureSpec.EXACTLY);
 		}
 		cs.measure(cs_w_spec, cs_h_spec);
 		grid.measure(grid_spec, grid_spec);
 		setMeasuredDimension(w, h);
 	}
 
 	protected void onLayout(boolean changed, int left, int top, int right,
 			int bottom) {
 		if (!changed)
 			return;
 		final View cs = getChildAt(0), grid = getChildAt(1), chk = getChildAt(2), btn = getChildAt(3);
 		int w = right - left, h = bottom - top;
 		int chk_h = chk.getMeasuredHeight();
		int grid_s = grid.getMeasuredHeight();
 
 		// I'm really expecting top to be 0!
 		bottom -= top;
 		top = 0;
 
 		if (horizontal) {
 			int px = padding_x / 4, py = padding_y / 2;
 			left += px;
 			right -= px;
 			top += py;
 			bottom -= py;
 			h -= padding_y;
 			cs.layout(left, top, left + cs_s, bottom);
 			left += cs_s + px;
 			int grid_pad = (h - grid_s) / 2;
			grid.layout(left, top + grid_pad, left + grid_s, top + grid_pad + grid_s);
 			left += px;
 			chk.layout(left + grid_s, top + py, right, top + chk_h);
 			btn.layout(left + grid_s, top + chk_h, right,
 					top + chk_h + btn.getMeasuredHeight());
 		} else {
 			int px = padding_x / 2, py = padding_y / 3;
 			left += px;
 			right -= px;
 			top += py;
 			w -= padding_x;
 			cs.layout(left, top, right, top + cs_s);
 			top += cs_s + py;
 			int grid_pad = (w - grid_s) / 2;
			grid.layout(left + grid_pad, top, left + grid_pad + grid_s, top + grid_s);
 			top += py;
 			chk.layout(left, top + grid_s, right, top + grid_s + chk_h);
 			btn.layout(left, top + grid_s + chk_h, right, bottom);
 		}
 	}
 }
