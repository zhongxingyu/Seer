 /*	GenesisChess, an Android chess application
 	Copyright 2012, Justin Madru (justin.jdm64@gmail.com)
 
 	Licensed under the Apache License, Version 2.0 (the "License");
 	you may not use this file except in compliance with the License.
 	You may obtain a copy of the License at
 
 	http://apache.org/licenses/LICENSE-2.0
 
 	Unless required by applicable law or agreed to in writing, software
 	distributed under the License is distributed on an "AS IS" BASIS,
 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 	See the License for the specific language governing permissions and
 	limitations under the License.
 */
 
 package com.chess.genesis.data;
 
 import android.content.*;
 import android.database.sqlite.*;
 import android.graphics.*;
 import android.os.*;
 import android.preference.*;
 import android.text.Layout.Alignment;
 import android.text.*;
 import android.util.*;
 import android.view.*;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.*;
 import com.chess.genesis.*;
 import com.chess.genesis.util.*;
 import com.chess.genesis.view.*;
 
 public class MsgListAdapter extends BaseAdapter
 {
 	private final Context context;
 	private final String gameID;
 
 	private GameDataDB db;
 	private SQLiteCursor list;
 
 	public MsgListAdapter(final Context _context, final String GameID)
 	{
 		super();
 
 		context = _context;
 		gameID = GameID;
 		initCursor();
 
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
 		final String username = pref.getString(PrefKey.USERNAME, "");
 
 		MsgListItem.Cache.Init(context, username);
 	}
 
 	private void initCursor()
 	{
 		db = new GameDataDB(context);
 		list = db.getMsgList(gameID);
 	}
 
 	@Override
 	public int getCount()
 	{
 		return list.getCount();
 	}
 
 	public void update()
 	{
 		if (list.isClosed())
 			initCursor();
 		else
 			list.requery();
 		notifyDataSetChanged();
 	}
 
 	public void close()
 	{
 		list.close();
 		db.close();
 	}
 
 	@Override
 	public long getItemId(final int index)
 	{
 		return index;
 	}
 
 	@Override
 	public Object getItem(final int index)
 	{
 		return GameDataDB.rowToBundle(list, index);
 	}
 
 	@Override
 	public View getView(final int index, View cell, final ViewGroup parent)
 	{
 		final Bundle data = (Bundle) getItem(index);
 
		// BUG: must always create new instance
		// or the message list gets corrupt
		cell = new MsgListItem(parent.getContext());
 		((MsgListItem) cell).setData(data);
 
 		return cell;
 	}
 
 	public static View getEmptyView(final Context _context)
 	{
 		final View cell = View.inflate(_context, R.layout.msglist_cell_empty, null);
 
 		// Fix sizing issue
 		final LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 		cell.setLayoutParams(lp);
 
 		return cell;
 	}
 }
 
 class MsgListItem extends View
 {
 	private final TextPaint paint = new TextPaint();
 	private StaticLayout msgImg;
 	private DataItem data;
 	private int lastWidth;
 
 	final static class DataItem
 	{
 		public String username;
 		public String time;
 		public String msg;
 		public boolean isYourMsg;
 	}
 
 	final static class Cache
 	{
 		public static Typeface fontNormal;
 		public static Typeface fontItalic;
 		public static String username;
 		public static int dpi;
 
 		public static int padding;
 		public static int smallText;
 		public static int largeText;
 		public static int headerAlign;
 		public static int headerHeight;
 
 		private Cache()
 		{
 		}
 
 		public static void Init(final Context context, final String Username)
 		{
 			fontNormal = RobotoText.getRobotoFont(context.getAssets(), Typeface.NORMAL);
 			fontItalic = RobotoText.getRobotoFont(context.getAssets(), Typeface.ITALIC);
 			username = Username;
 
 			final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
 			dpi = (int) ((1 + Math.max(metrics.ydpi, metrics.xdpi)) / 160);
 
 			padding = 9 * dpi;
 			smallText = 20 * dpi;
 			largeText = 22 * dpi;
 			headerAlign = 22 * dpi;
 			headerHeight = 30 * dpi;
 		}
 	}
 
 	public MsgListItem(final Context context)
 	{
 		super(context);
 
 		paint.setAntiAlias(true);
 		paint.setTypeface(Cache.fontNormal);
 		paint.setTextSize(Cache.largeText);
 	}
 
 	@Override
 	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
 	{
 		final int width = MeasureSpec.getSize(widthMeasureSpec);
 
 		if (width != lastWidth) {
 			msgImg = new StaticLayout(data.msg, paint, width - 4 * Cache.padding, Alignment.ALIGN_NORMAL, 1, 0, true);
 			lastWidth = width;
 		}
 
 		setMeasuredDimension(width, msgImg.getHeight() + Cache.headerHeight + 2 * Cache.padding);
 	}
 
 	@Override
 	protected void onDraw(final Canvas canvas)
 	{
 		final int width = getMeasuredWidth();
 
 		// draw msg text
 		canvas.save();
 		canvas.translate(2 * Cache.padding, Cache.headerHeight);
 		msgImg.draw(canvas);
 		canvas.restore();
 
 		// draw msg header
 		if (data.isYourMsg)
 			paint.setColor(MColors.PURPLE_PASTEL);
 		else
 			paint.setColor(MColors.BLUE_PASTEL);
 		canvas.drawRect(0, 0, getWidth(), Cache.headerHeight, paint);
 
 		// draw username
 		paint.setColor(MColors.BLACK);
 		paint.setTextSize(Cache.smallText);
 		canvas.drawText(data.username, Cache.padding, Cache.headerAlign, paint);
 
 		// draw time
 		paint.setTextAlign(Paint.Align.RIGHT);
 		paint.setTypeface(Cache.fontItalic);
 		canvas.drawText(data.time, width - Cache.padding, Cache.headerAlign, paint);
 
 		// reset paint
 		paint.setTextAlign(Paint.Align.LEFT);
 		paint.setTypeface(Cache.fontNormal);
 		paint.setTextSize(Cache.largeText);
 	}
 
 	public void setData(final Bundle bundle)
 	{
 		data = new DataItem();
 
 		data.username = bundle.getString("username");
 		data.time = new PrettyDate(bundle.getString("time")).agoFormat();
 		data.msg = bundle.getString("msg");
 		data.isYourMsg = data.username.equals(Cache.username);
 	}
 }
