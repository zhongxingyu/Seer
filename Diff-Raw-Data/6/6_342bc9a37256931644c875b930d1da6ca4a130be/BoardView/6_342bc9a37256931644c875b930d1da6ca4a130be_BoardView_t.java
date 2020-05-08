 package com.charlesmadere.android.classygames.views;
 
 
 import android.content.Context;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.content.res.TypedArray;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.ViewGroup;
 import com.charlesmadere.android.classygames.R;
 import com.charlesmadere.android.classygames.utilities.Utilities;
 
 
 public class BoardView extends ViewGroup
 {
 
 
 	private final static String LOG_TAG = Utilities.LOG_TAG + " - BoardView";
 	private final static int COLUMNS_DEFAULT = 2;
 	private final static int ROWS_DEFAULT = 2;
 
 	private Drawable darkBackground;
 	private Drawable brightBackground;
 	private int columns;
 	private int rows;
 	private PositionView positionViews[][];
 
 
 	public BoardView(final Context context, final AttributeSet attrs)
 	{
 		super(context, attrs);
 		parseAttributes(attrs);
 		createPositions();
 	}
 
 
 	@Override
 	protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b)
 	{
 		final int width = positionViews[0][0].getMeasuredWidth();
 		final int height = positionViews[0][0].getMeasuredHeight();
 
 		for (int x = 0; x < columns; ++x)
 		{
 			final int left = width * x;
 			final int right = left + width;
 
 			for (int y = 0; y < rows; ++y)
 			{
 				final int top = height * (rows - (y + 1));
 				final int bottom = top + height;
 
 				final PositionView positionView = positionViews[x][y];
 				positionView.layout(left, top, right, bottom);
 			}
 		}
 	}
 
 
 	@Override
 	@SuppressWarnings("SuspiciousNameCombination")
 	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
 	{
 		final int height;
 		final int width;
 
 		if (isOrientationLandscape())
 		{
 			height = MeasureSpec.getSize(heightMeasureSpec);
 			width = height;
 		}
 		else
 		{
 			width = MeasureSpec.getSize(widthMeasureSpec);
 			height = width;
 		}
 
 		setMeasuredDimension(width, height);
 
		final int widthSize = (int) Math.ceil((double) width / (double) columns);
		final int heightSize = (int) Math.ceil((double) height / (double) rows);
		final int widthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
		final int heightSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
 
 		for (int x = 0; x < columns; ++x)
 		{
 			for (int y = 0; y < rows; ++y)
 			{
 				final PositionView positionView = positionViews[x][y];
 				positionView.measure(widthSpec, heightSpec);
 			}
 		}
 	}
 
 
 	@Override
 	public boolean shouldDelayChildPressedState()
 	{
 		return false;
 	}
 
 
 	/**
 	 * Applies the given OnClickListener to every one of the PositionViews that
 	 * belong to this BoardView.
 	 */
 	public void setPositionsOnClickListener(final OnClickListener onClickListener)
 	{
 		for (int x = 0; x < columns; ++x)
 		{
 			for (int y = 0; y < rows; ++y)
 			{
 				final PositionView positionView = positionViews[x][y];
 				positionView.setOnClickListener(onClickListener);
 			}
 		}
 	}
 
 
 	private void createPositions()
 	{
 		positionViews = new PositionView[columns][rows];
 		final Context context = getContext();
 
 		for (int x = 0; x < columns; ++x)
 		{
 			for (int y = 0; y < rows; ++y)
 			{
 				final PositionView positionView = new PositionView(context, x, y, brightBackground, darkBackground);
 				positionViews[x][y] = positionView;
 				addView(positionView);
 			}
 		}
 	}
 
 
 	private boolean isOrientationLandscape()
 	{
 		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
 	}
 
 
 	private void parseAttributes(final AttributeSet attrs)
 	{
 		final Resources.Theme theme = getContext().getTheme();
 		final TypedArray attributes = theme.obtainStyledAttributes(attrs, R.styleable.BoardView, 0, 0);
 
 		try
 		{
 			brightBackground = attributes.getDrawable(R.styleable.BoardView_bright_background);
 			darkBackground = attributes.getDrawable(R.styleable.BoardView_dark_background);
 			columns = attributes.getInt(R.styleable.BoardView_columns, COLUMNS_DEFAULT);
 			rows = attributes.getInt(R.styleable.BoardView_rows, ROWS_DEFAULT);
 		}
 		catch (final Exception e)
 		{
 			Log.e(LOG_TAG, "Exception when reading attributes!", e);
 			brightBackground = null;
 			darkBackground = null;
 			columns = COLUMNS_DEFAULT;
 			rows = ROWS_DEFAULT;
 		}
 		finally
 		{
 			attributes.recycle();
 		}
 	}
 
 
 }
