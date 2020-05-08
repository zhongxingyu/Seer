 package ru.gang018.expandabletextview.ui;
 import ru.gang018.expandabletextview.R;
 import android.R.integer;
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Color;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class ExpandableTextView extends LinearLayout
 {	
 	/**
 	 * Default constants
 	 */
 	private static int MAX_LINES_MAX = 100000,
 					   DEFAULT_ANIMATION_DURATION = 500;
 	
 	public static int DEFAULT_MIN_LINES_COUNT = 3;
 	
 	/**
 	 * Min lines count when text is expanded
 	 */
 	private int mLinesCount = 0;
 	
 	/**
 	 * TextView which is showing the text
 	 */
 	private TextView mText;
 	
 	/**
 	 * Fade animation for textview
 	 */
 	private Animation mDefaultAnimation;
 	
 	/**
 	 * Implements logic
 	 */
 	private boolean isTextCollapsed = true;
 
 	public ExpandableTextView(final Context context) 
 	{
 		super(context);
 		initViews();
 	}
 
 	public ExpandableTextView(Context context, AttributeSet attrs) 
 	{
 		super(context, attrs);
 		initViews();
 		
 		final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
 		
 		try 
 		{
 			mLinesCount = ta.getInteger(R.styleable.ExpandableTextView_linesCountWhenExpand, DEFAULT_MIN_LINES_COUNT);
			mText.setMinLines(mLinesCount);
 			mText.setMaxLines(mLinesCount);
 			
			int param = ta.getColor(R.styleable.ExpandableTextView_textColor, Color.BLACK);
 			mText.setTextColor(param);
 			
			param = (int) ta.getDimension(R.styleable.ExpandableTextView_textSize, context.getResources().getDimension(R.dimen.expandable_text_size_default));
			mText.setTextSize(param);
 			
 			final String text = ta.getString(R.styleable.ExpandableTextView_textToExpand);
 			mText.setText(text);
 		} 
 		finally
 		{
 			ta.recycle();
 		}
 	}
 	
 	/**
 	 * Inits TextViews
 	 */
 	private void initViews()
 	{
 		LayoutInflater.from(getContext()).inflate(R.layout.expandable_textview, this, true);
 		
 		mText = (TextView) findViewById(R.id.expandable_textview_text);
 		
 		mText.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				if (isTextCollapsed)
 				{
 					mText.setMaxLines(MAX_LINES_MAX);
 				}
 				else
 				{
 					mText.setMaxLines(mLinesCount);
 				}
 				
 				isTextCollapsed = !isTextCollapsed;
 			}
 		});
 	} 
 	
 	/**
 	 * Gets default animation for TextView with fading effect
 	 * @param from - {@link Float}
 	 * @param to - {@link Float}
 	 * @return - {@link Animation}
 	 */
 	private Animation getDefaultAnimation()
 	{
 		if (mDefaultAnimation == null)
 		{
 			mDefaultAnimation = new AlphaAnimation(0, 1);
 			mDefaultAnimation.setDuration(DEFAULT_ANIMATION_DURATION);
 		}
 			
 		return mDefaultAnimation;
 	}
 	
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) 
 	{
 		// TODO Auto-generated method stub
 		super.onSizeChanged(w, h, oldw, oldh);
 		
 		if (mText.getAnimation() == null)
 		mText.setAnimation(getDefaultAnimation());
 
 		mText.getAnimation().start();
 	}
 	
 	/**
 	 * Sets the size of text
 	 * @param size - {@link integer}
 	 */
 	public void setTextSize(final float size)
 	{
 		if (size < 0)
 			throw new IllegalArgumentException("Text size must be more than 0");
 		
 		mText.setTextSize(size);
 	}
 	
 	/**
 	 * Sets the minimum count of visible lines of text
 	 * @param count - {@link integer}
 	 */
 	public void setMinLinesCount(final int count)
 	{
 		if (count < 1)
 			throw new IllegalArgumentException("Lines count must be more than 0");
 	
 		mText.setMinLines(count);
 		mText.setMaxLines(count);
 	}
 	
 	/**
 	 * Set the text color
 	 * @param color - {@link integer}
 	 */
 	public void setTextColor(final int color)
 	{
 		mText.setTextColor(color);
 	}
 	
 	/**
 	 * Sets the main text {@link TextView}
 	 * @param text - {@link String}
 	 */
 	public void setText(final String text)
 	{
 		mText.setText(text);
 	}
 	
 	/**
 	 * Sets the main text from resource file
 	 * @param resId - {@link integer}
 	 */
 	public void setText(final int resId)
 	{
 		mText.setText(resId);
 	}
 }
