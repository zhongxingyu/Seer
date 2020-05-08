 package net.gumbercules.loot.transaction;
 
 import java.text.DateFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Currency;
 
 import net.gumbercules.loot.R;
 import net.gumbercules.loot.account.Account;
 import net.gumbercules.loot.backend.Database;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.preference.PreferenceManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.Filter;
 import android.widget.Filterable;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class TransactionAdapter extends ArrayAdapter<Transaction> implements Filterable
 {
 	private ArrayList<Transaction> mTransList;
 	private ArrayList<Transaction> mOriginalList;
 	private ArrayList<Double> mRunningBalances;
 	private int mRowResId;
 	private Context mContext;
 	private LayoutInflater mInflater;
 	private static CharSequence mConstraint;
 	private int mAcctId;
 	private DateFormat mDf;
 	private static NumberFormat mNf = null;
 	
 	// preferences
 	private int mColorCheck;
 	private int mColorCheckBudget;
 	private int mColorWithdraw;
 	private int mColorWithdrawBudget;
 	private int mColorDeposit;
 	private int mColorDepositBudget;
 	private boolean mShowColors;
 	private boolean mColorBackgrounds;
 	private boolean mShowRunningBalance;
 	
 	public TransactionAdapter(Context con, int row, ArrayList<Transaction> tr, int acct_id)
 	{
 		super(con, 0);
 		this.mTransList = tr;
 		this.mOriginalList = tr;
 		this.mRunningBalances = new ArrayList<Double>();
 		this.mRowResId = row;
 		this.mContext = con;
 		this.mAcctId = acct_id;
 		this.mDf = DateFormat.getDateInstance();
 		mNf = NumberFormat.getCurrencyInstance();
 		String new_currency = Database.getOptionString("override_locale");
 		if (new_currency != null && !new_currency.equals("") &&
 				!new_currency.equals(mNf.getCurrency().getCurrencyCode()))
 			mNf.setCurrency(Currency.getInstance(new_currency));
 		
 		mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 	
 	public void updatePreferenceValues()
 	{
 		final int Color_LTGREEN = Color.rgb(185, 255, 185);
 		final int Color_LTYELLOW = Color.rgb(255, 255, 185);
 		final int Color_LTCYAN = Color.rgb(185, 255, 255);
 
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
 		mShowColors = prefs.getBoolean("color", false);
 		mColorBackgrounds = prefs.getBoolean("color_background", false);
 		mShowRunningBalance = prefs.getBoolean("running_balance", false);
 		
 		mColorCheckBudget = prefs.getInt("color_budget_check", prefs.getInt("bc_color", Color_LTCYAN));
 		mColorCheck = prefs.getInt("color_check", prefs.getInt("ac_color", Color.CYAN));
 		mColorWithdrawBudget = prefs.getInt("color_budget_withdraw", prefs.getInt("bw_color", Color_LTYELLOW));
 		mColorWithdraw = prefs.getInt("color_withdraw", prefs.getInt("aw_color", Color.YELLOW));
 		mColorDepositBudget = prefs.getInt("color_budget_deposit", prefs.getInt("bd_color", Color_LTGREEN));
 		mColorDeposit = prefs.getInt("color_deposit", prefs.getInt("ad_color", Color.GREEN));
 	}
 	
 	public void calculateRunningBalances()
 	{
 		calculateRunningBalances(0);
 	}
 	
 	public void calculateRunningBalances(int pos)
 	{
 		// don't waste time with the calculations if it's not being shown
 		if (!mShowRunningBalance)
 		{
 			return;
 		}
 		
 		if (!mRunningBalances.isEmpty())
 		{
 			int sz = mRunningBalances.size();
 			mRunningBalances.subList(pos, sz).clear(); 
 		}
 		
 		int len = mOriginalList.size();
 		double cur_balance, prev_balance, amount;
 		Transaction trans;
 		
		if (pos == 0 || mRunningBalances.isEmpty())
 		{
 			Account acct = Account.getAccountById(mAcctId);
 			prev_balance = acct.initialBalance;
 		}
 		else
 		{
			prev_balance = mRunningBalances.get(pos - 1);
 		}
 		
 		for (int i = pos; i < len; ++i)
 		{
 			trans = mOriginalList.get(i);
 			if (trans.type == Transaction.DEPOSIT)
 			{
 				amount = trans.amount;
 			}
 			else
 			{
 				amount = -trans.amount;
 			}
 			
 			cur_balance = prev_balance + amount;
 			mRunningBalances.add(cur_balance);
 			
 			prev_balance = cur_balance;
 		}
 		
 		notifyDataSetChanged();
 	}
 
 	public void setContext(Context con)
 	{
 		this.mContext = con;
 	}
 	
 	@Override
 	public int getCount()
 	{
 		return mTransList.size();
 	}
 
 	@Override
 	public Transaction getItem(int position)
 	{
 		if (position == -1)
 			return null;
 		return mTransList.get(position);
 	}
 
 	@Override
 	public long getItemId(int position)
 	{
 		return mTransList.get(position).id();
 	}
 	
 	public int findItemNoFilter(int id)
 	{
 		for (int i = 0; i < mOriginalList.size(); ++i)
 		{
 			if (mOriginalList.get(i).id() == id)
 				return i;
 		}
 		
 		return -1;
 	}
 	
 	public int findItemById(int id)
 	{
 		for (int i = 0; i < mTransList.size(); ++i)
 		{
 			if (mTransList.get(i).id() == id)
 				return i;
 		}
 		
 		return -1;
 	}
 
 	public void setResource(int row)
 	{
 		this.mRowResId = row;
 	}
 	
 	public void setList(ArrayList<Transaction> trans)
 	{
 		mOriginalList = trans;
 		new TransactionFilter()._filter(mConstraint);
 		notifyDataSetChanged();
 	}
 	
 	public ArrayList<Transaction> getList()
 	{
 		return mTransList;
 	}
 	
 	public void sort()
 	{
 		Collections.sort(mOriginalList);
 		new TransactionFilter()._filter(mConstraint);
 		notifyDataSetChanged();
 	}
 
 	@Override
 	public void add(Transaction object)
 	{
 		if (object.account == mAcctId)
 		{
 			mOriginalList.add(object);
 			notifyDataSetChanged();
 		}
 	}
 	
 	public void add(int[] ids)
 	{
 		if (ids == null)
 		{
 			return;
 		}
 		
 		int last = -1;
 		Transaction trans;
 		for (int id : ids)
 		{
 			if (id == -1 || id == last)
 				continue;
 			
 			trans = Transaction.getTransactionById(id);
 			if (trans != null && trans.account == mAcctId)
 			{
 				mOriginalList.add(trans);
 			}
 			
 			last = id;
 		}
 		notifyDataSetChanged();
 	}
 
 	public void add(Transaction[] trans_list)
 	{
 		if (trans_list == null)
 		{
 			return;
 		}
 		
 		Transaction trans;
 		for (int i = trans_list.length - 1; i >= 0; --i)
 		{
 			trans = trans_list[i];
 			if (trans != null && trans.account == mAcctId)
 			{
 				mOriginalList.add(trans);
 			}
 		}
 		notifyDataSetChanged();
 	}
 
 	@Override
 	public void insert(Transaction object, int index)
 	{
 		if (object.account == mAcctId)
 		{
 			mOriginalList.add(index, object);
 			notifyDataSetChanged();
 		}
 	}
 
 	@Override
 	public void remove(Transaction object)
 	{
 		mOriginalList.remove(object);
 		new TransactionFilter()._filter(mConstraint);
 		notifyDataSetChanged();
 	}
 	
 	public void remove(int[] ids)
 	{
 		for (int id : ids)
 		{
 			mOriginalList.remove(getItem(findItemById(id)));
 		}
 		new TransactionFilter()._filter(mConstraint);
 		notifyDataSetChanged();
 	}
 	
 	@Override
 	public void clear()
 	{
 		mOriginalList.clear();
 		new TransactionFilter()._filter(mConstraint);
 		notifyDataSetChanged();
 	}
 	
 	@Override
 	public Filter getFilter()
 	{
 		TransactionFilter filter = new TransactionFilter();
 		return (Filter)filter;
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent)
 	{
 		ViewHolder holder;
 		
 		if (convertView == null)
 		{
 			convertView = mInflater.inflate(mRowResId, parent, false);
 			
 			holder = new ViewHolder();
 			holder.check = (CheckBox)convertView.findViewById(R.id.PostedCheckBox);
 			holder.date = (TextView)convertView.findViewById(R.id.DateText);
 			holder.party = (TextView)convertView.findViewById(R.id.PartyText);
 			holder.amount = (TextView)convertView.findViewById(R.id.AmountText);
 			holder.running_balance = (TextView)convertView.findViewById(R.id.RunningBalanceText);
 			holder.image = (ImageView)convertView.findViewById(R.id.image_view);
 			holder.top = (LinearLayout)convertView.findViewById(R.id.LinearLayout01);
 			
 			convertView.setTag(holder);
 		}
 		else
 		{
 			holder = (ViewHolder)convertView.getTag();
 		}
 
 		// bail early if the transaction doesn't exist, isn't for this account, or is not currently visible
 		final Transaction trans = mTransList.get(position);
 		if (trans == null || trans.account == 0)
 		{
 			return convertView;
 		}
 		
 		final int pos = position;
 
 		// find and retrieve the widgets
 		CheckBox postedCheck = holder.check;
 		
 		if (postedCheck == null)
 			return convertView;
 		
 		postedCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
 		{
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
 			{
 				if (trans.isPosted() != isChecked)
 				{
 					boolean budget = trans.budget;
 					trans.post(isChecked);
 					TransactionActivity ta = (TransactionActivity) mContext;
 					ta.setBalances();
 
 					Intent broadcast = new Intent("net.gumbercules.loot.intent.ACCOUNT_UPDATED", null);
 					broadcast.putExtra("account_id", trans.account);
 					TransactionAdapter.this.getContext().sendBroadcast(broadcast);
 
 					// only need to update the view if it changed from budget to posted
 					if (budget)
 					{
 						ListView lv = ta.getListView();
 						View v = lv.getChildAt(pos - lv.getFirstVisiblePosition());
 						if (v != null)
 						{
 							ViewHolder holder = (ViewHolder)v.getTag();
 							setViewData(trans, holder, null, null, null);
 						}
 					}
 				}
 			}
 		});
 		
 		// change the date to the locale date format
 		DateFormat df = mDf;
 		String dateStr = df.format(trans.date);
 		
 		// change the numbers to the locale currency format
 		NumberFormat nf = mNf;
 		String amountStr = nf.format(Math.abs(trans.amount));
 		String balStr = null;
 		if (mShowRunningBalance)
 		{
 			try
 			{
 				balStr = nf.format(mRunningBalances.get(pos));
 			}
 			catch (IndexOutOfBoundsException e)
 			{
 				calculateRunningBalances();
 				balStr = nf.format(mRunningBalances.get(pos));
 			}
 		}
 		
 		if (postedCheck != null)
 		{
 			postedCheck.setChecked(trans.isPosted());
 		}
 
 		// populate the widgets with data
 		setViewData(trans, holder, amountStr, dateStr, balStr);
 		
 		return convertView;
 	}
 	
 	private void setViewData(Transaction trans, ViewHolder v, String amountStr, String dateStr, String balStr)
 	{
 		String partyStr = "";
 		int color = Color.LTGRAY;
 		
 		if (trans.budget)
 		{
 			if (trans.type == Transaction.DEPOSIT)
 			{
 				partyStr += "+";
 			}
 			else
 			{
 				partyStr += "-";
 			}
 		}
 		
 		if (trans.type == Transaction.CHECK)
 		{
 			partyStr += trans.check_num;
 			if (trans.budget)
 			{
 				color = mColorCheckBudget;
 			}
 			else
 			{
 				color = mColorCheck;
 			}
 		}
 		else if (trans.type == Transaction.WITHDRAW)
 		{
 			partyStr += "W";
 			if (trans.budget)
 			{
 				color = mColorWithdrawBudget;
 			}
 			else
 			{
 				color = mColorWithdraw;
 			}
 		}
 		else
 		{
 			partyStr += "D";
 			if (trans.budget)
 			{
 				color = mColorDepositBudget;
 			}
 			else
 			{
 				color = mColorDeposit;
 			}
 		}
 		partyStr += ":" + trans.party;
 		
 		TextView dateText = v.date;
 		TextView partyText = v.party;
 		TextView amountText = v.amount;
 		TextView runningBalanceText = v.running_balance;
 		
 		if (mShowColors && mColorBackgrounds)
 		{
 			v.top.setBackgroundColor(color);
 		}
 		else
 		{
 			v.top.setBackgroundColor(v.top.getRootView().getDrawingCacheBackgroundColor());
 		}
 		
 		if (dateStr == null)
 		{
 			dateStr = v.date.getText().toString();
 		}
 		if (amountStr == null)
 		{
 			amountStr = v.amount.getText().toString();
 		}
 		
 		setText(dateText, dateStr, color, mShowColors, mColorBackgrounds);
 		setText(partyText, partyStr, color, mShowColors, mColorBackgrounds);
 		setText(amountText, amountStr, color, mShowColors, mColorBackgrounds);
 		
 		if (mShowRunningBalance)
 		{
 			if (balStr == null)
 			{
 				balStr = v.running_balance.getText().toString();
 			}
 			setText(runningBalanceText, balStr, color, mShowColors, mColorBackgrounds);
 			runningBalanceText.setVisibility(View.VISIBLE);
 		}
 		else
 		{
 			runningBalanceText.setVisibility(View.GONE);
 		}
 		
 		if (trans.images != null && trans.images.size() > 0)
 		{
 			v.image.setVisibility(View.VISIBLE);
 		}
 		else
 		{
 			v.image.setVisibility(View.GONE);
 		}
 	}
 	
 	private void setText(TextView text, String str, int color, boolean colors, boolean bg)
 	{
 		if (text != null)
 		{
 			text.setText(str);
 		}
 
 		if (colors)
 		{
 			if (bg)
 			{
 				int red = Color.red(color);
 				int green = Color.green(color);
 				int blue = Color.blue(color);
 				
 				int max = Math.max(red, Math.max(green, blue));
 				int min = Math.min(red, Math.min(green, blue));
 				
 				float lightness = 0.5f * (float)(max + min);
 				
 				if (lightness < 100.0f)
 					color = Color.LTGRAY;
 				else
 					color = Color.DKGRAY;
 			}
 			text.setTextColor(color);
 		}
 	}
 	
 	static class ViewHolder
 	{
 		CheckBox check;
 		TextView date;
 		TextView party;
 		TextView amount;
 		TextView running_balance;
 		ImageView image;
 		LinearLayout top;
 	}
 
 	public class TransactionFilter extends Filter
 	{
 		private boolean mShowPosted;
 		private boolean mShowNonPosted;
 		
 		public TransactionFilter()
 		{
 			super();
 			mShowPosted = true;
 			mShowNonPosted = true;
 		}
 		
 		public FilterResults filtering(CharSequence constraint)
 		{
 			return performFiltering(constraint);
 		}
 		
 		public void setShowPosted(boolean b)
 		{
 			mShowPosted = b;
 		}
 		
 		public void setShowNonPosted(boolean b)
 		{
 			mShowNonPosted = b;
 		}
 		
 		@Override
 		protected FilterResults performFiltering(CharSequence constraint)
 		{
 			mConstraint = constraint;
 			
 			// do the filtering to decide which items we show
 			if (constraint == null || constraint.length() == 0)
 			{
 				return filterEmptyString();
 			}
 			else
 			{
 				return filterString(constraint);
 			}
 		}
 		
 		private FilterResults filterEmptyString()
 		{
 			FilterResults results = new FilterResults();
 			ArrayList<Transaction> tList = new ArrayList<Transaction>(mOriginalList);
 			ArrayList<Transaction> values = new ArrayList<Transaction>();
 			results.count = 0;
 			results.values = values;
 			
 			for (Transaction trans : tList)
 			{
 				if ((!mShowPosted && trans.isPosted()) || (!mShowNonPosted && !trans.isPosted()))
 				{
 					continue;
 				}
 				
 				values.add(trans);
 			}
 
 			results.values = (Object)values;
 			results.count = values.size();
 			
 			return results;
 		}
 		
 		private FilterResults filterString(CharSequence constraint)
 		{
 			FilterResults results = new FilterResults();
 
 			boolean matches = false;
 			ArrayList<Transaction> tList = new ArrayList<Transaction>(mOriginalList);
 			ArrayList<Transaction> values = new ArrayList<Transaction>();
 			results.count = 0;
 			results.values = values;
 			String[] filters = constraint.toString().split(" ");
 			
 			for (Transaction trans : tList)
 			{
 				// if it doesn't match the posted/non-posted options, exit
 				if ((!mShowPosted && trans.isPosted()) || (!mShowNonPosted && !trans.isPosted()))
 				{
 					continue;
 				}
 				
 				// check for at least one match
 				for (String filter : filters)
 				{
 					filter = "(?i).*" + filter + ".*";
 					if (trans.party.matches(filter))
 					{
 						matches = true;
 						break;
 					}
 					for (String tag : trans.tags)
 					{
 						if (tag.matches(filter))
 						{
 							matches = true;
 							break;
 						}
 					}
 					if (matches)
 						break;
 				}
 				
 				if (matches)
 				{
 					values.add(trans);
 				}
 				
 				matches = false;
 			}
 			
 			results.values = (Object)values;
 			results.count = values.size();
 
 			return results;
 		}
 
 		public void publish(CharSequence constraint, FilterResults results)
 		{
 			publishResults(constraint, results);
 		}
 		
 		@SuppressWarnings("unchecked")
 		public void _filter(CharSequence cs)
 		{
 			filter(cs);
 			
 			if (cs == null || cs.equals(""))
 			{
 				mTransList = (ArrayList<Transaction>) mOriginalList.clone();
 			}
 		}
 		
 		@SuppressWarnings("unchecked")
 		@Override
 		protected void publishResults(CharSequence constraint, FilterResults results)
 		{
 			mTransList = (ArrayList<Transaction>)results.values;
 			notifyDataSetChanged();
 		}
 	}
 }
