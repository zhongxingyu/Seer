 package net.gumbercules.loot;
 
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.database.*;
 import android.database.sqlite.*;
 
 public class RepeatSchedule
 implements Cloneable
 {
 	public static final String KEY_ID		= "rs_id";
 	public static final String KEY_ITER		= "key_iter";
 	public static final String KEY_FREQ		= "key_freq";
 	public static final String KEY_CUSTOM	= "key_custom";
 	public static final String KEY_DATE		= "key_date";
 	
 	// repetition iterator type
 	public static final int NO_REPEAT	= 0;
 	public static final int DAILY		= 1;
 	public static final int WEEKLY		= 2;
 	public static final int MONTHLY		= 3;
 	public static final int YEARLY		= 4;
 	
 	// custom weekly repetition
 	public static final int SUNDAY		= 1 << 0;
 	public static final int MONDAY		= 1 << 1;
 	public static final int TUESDAY		= 1 << 2;
 	public static final int WEDNESDAY	= 1 << 3;
 	public static final int THURSDAY	= 1 << 4;
 	public static final int FRIDAY		= 1 << 5;
 	public static final int SATURDAY	= 1 << 6;
 	
 	// custom monthly repetition
 	public static final int DAY			= 0;
 	public static final int DATE		= 1;
 	
 	int iter;			// repetition iterator type
 	int freq;			// number between repetitions
 	int custom;			// used only for custom types
 	Date start;			// start date
 	Date end;			// end date
 	private Date due;	// date of the next repetition
 	private int id;		// id of the database repeat_pattern, if available
 	
 	public RepeatSchedule()
 	{
 		this.id = -1;
 		this.due = null;
 		this.start = null;
 		this.end = null;
 		this.iter = NO_REPEAT;
 		this.freq = -1;
 		this.custom = -1;
 	}
 	
 	public RepeatSchedule( int it, int fr, int cu, Date st, Date en )
 	{
 		this.id = -1;
 		this.due = null;
 		this.iter = it;
 		this.freq = fr;
 		this.custom = cu;
 		this.start = st;
 		this.end = en;
 	}
 	
 	public int id()
 	{
 		return id;
 	}
 	
 	protected Object clone()
 	throws CloneNotSupportedException
 	{
 		return super.clone();
 	}
 	
 	public int write(int trans_id)
 	{
 		this.due = this.calculateDueDate();
 		if (this.due == null || this.start == null)
 		{
 			if (this.id > 0)
 				this.erase(true);
 			return -1;
 		}
 		
 		if (this.id == -1)
 			return newRepeat(trans_id);
 		else
 			return updateRepeat(trans_id);
 	}
 	
 	private int newRepeat(int trans_id)
 	{
 		// check the database to make sure that we don't get stuck in an infinite loop
 		// of constantly writing a new repeat schedule for the transfer links
 		int repeat_id = RepeatSchedule.getRepeatId(trans_id);
 		if (repeat_id != -1)
 			return repeat_id;
 		
 		long start_time = this.start.getTime(), end_time = 0;
 		try
 		{
 			end_time = this.end.getTime();
 		}
 		catch (Exception e) { }
 		
 		String insert = "insert into repeat_pattern (start_date,end_date,iterator,frequency," +
 						"custom,due) values (" + start_time + "," + end_time + "," + this.iter +
 						"," + this.freq + "," + this.custom + "," + this.due.getTime() + ")";
 		
 		SQLiteDatabase lootDB = Database.getDatabase();
 		lootDB.beginTransaction();
 		
 		try
 		{
 			lootDB.execSQL(insert);
 		}
 		catch (SQLException e)
 		{
 			lootDB.endTransaction();
 			return -1;
 		}
 		
 		String[] columns = {"max(id)"};
 		Cursor cur = lootDB.query("repeat_pattern", columns, null, null, null, null, null);
 		if (!cur.moveToFirst())
 		{
 			cur.close();
 			lootDB.endTransaction();
 			return -1;
 		}
 		this.id = cur.getInt(0);
 		cur.close();
 		
 		if (!this.writeTransactionToRepeatTable(trans_id))
 		{
 			lootDB.endTransaction();
 			return -1;
 		}
 		
 		Transaction trans = Transaction.getTransactionById(trans_id, true);
 		int trans_id2 = trans.getTransferId();
 		if (trans_id2 > 0)
 		{
 			try
 			{
 				RepeatSchedule repeat2 = (RepeatSchedule) this.clone();
 				repeat2.id = -1;
 				if (repeat2.write(trans_id2) == -1)
 				{
 					lootDB.endTransaction();
 					return -1;
 				}
 			}
 			catch (CloneNotSupportedException e)
 			{
 				lootDB.endTransaction();
 				return -1;
 			}
 		}
 
 		lootDB.setTransactionSuccessful();
 		lootDB.endTransaction();
 		
 		return this.id;
 	}
 	
 	private int updateRepeat(int trans_id)
 	{
 		if (this.iter == NO_REPEAT)
 		{
 			this.erase(true);
 			return -1;
 		}
 
 		long end_time = 0;
 		try
 		{
 			end_time = this.end.getTime();
 		}
 		catch (Exception e) { }
 
 		Transaction trans = Transaction.getTransactionById(trans_id, true);
 		int trans_id2 = trans.getTransferId();
 		int repeat_id2 = -1;
 		if (trans_id2 > 0)
 			repeat_id2 = RepeatSchedule.getRepeatId(trans_id2);
 		
 		String update = "update repeat_pattern set start_date = " + this.start.getTime() +
 						", end_date = " + end_time + ", iterator = " + this.iter +
 						", frequency = " + this.freq + ", custom = " + this.custom +
 						", due = " + this.due.getTime() + " where id = " + this.id;
 		
 		if (repeat_id2 != -1)
 			update += " or id = " + repeat_id2;
 		
 		SQLiteDatabase lootDB = Database.getDatabase();
 		lootDB.beginTransaction();
 		
 		try
 		{
 			lootDB.execSQL(update);
 		}
 		catch (SQLException e)
 		{
 			lootDB.endTransaction();
 			return -1;
 		}
 		
 		// instead of trying to update the row, delete it and copy over the updated transaction
 		if (!this.eraseTransactionFromRepeatTable(repeat_id2))
 		{
 			lootDB.endTransaction();
 		}
 		
 		if (trans_id2 != -1)
 		{
 			RepeatSchedule repeat2 = RepeatSchedule.getSchedule(repeat_id2);
 			if (!this.writeTransactionToRepeatTable(trans_id) ||
 				!repeat2.writeTransactionToRepeatTable(trans_id2))
 			{
 				lootDB.endTransaction();
 				return -1;
 			}
 		}
 		else
 		{
 			if (!this.writeTransactionToRepeatTable(trans_id))
 			{
 				lootDB.endTransaction();
 				return -1;
 			}
 		}
 		
 		lootDB.setTransactionSuccessful();
 		lootDB.endTransaction();
 		return this.id;
 	}
 	
 	public boolean erase(boolean eraseTransfers)
 	{
 		Transaction trans = Transaction.getTransactionById(this.getTransactionId(), true);
 		int repeat_id2 = -1;
 		if (eraseTransfers)
 		{
 			int trans_id2 = trans.getTransferId();
 			if (trans_id2 != -1)
 				repeat_id2 = RepeatSchedule.getRepeatId(trans_id2);
 		}
 		
 		SQLiteDatabase lootDB = Database.getDatabase();
 		lootDB.beginTransaction();
 		
 		boolean ret = false;
 		if (this.erasePattern(repeat_id2) && this.eraseTransactionFromRepeatTable(repeat_id2))
 		{
 			ret = true;
 			lootDB.setTransactionSuccessful();
 		}
 		
 		lootDB.endTransaction();
 
 		return ret;
 	}
 	
 	private boolean erasePattern(int repeat_id2)
 	{
 		String del = "delete from repeat_pattern where id = " + this.id;
 		if (repeat_id2 != -1)
 			del += " or id = " + repeat_id2;
 		
 		SQLiteDatabase lootDB = Database.getDatabase();
 		lootDB.beginTransaction();
 		
 		try
 		{
 			lootDB.execSQL(del);
 			lootDB.setTransactionSuccessful();
 		}
 		catch (SQLException e)
 		{
 			return false;
 		}
 		finally
 		{
 			lootDB.endTransaction();
 		}
 		
 		return true;
 	}
 	
 	public boolean load(int repeat_id)
 	{
 		String[] columns = {"start_date", "end_date", "iterator", "frequency", "custom", "due"};
 		
 		SQLiteDatabase lootDB = Database.getDatabase();
 		Cursor cur = lootDB.query("repeat_pattern", columns, "id = " + repeat_id, null, null, null, null);
 		
 		boolean ret = false;
 		if (cur.moveToFirst())
 		{
 			double end = cur.getLong(1);
 			this.start = new Date(cur.getLong(0));
 			
 			if (end > 0)
 				this.end = new Date(cur.getLong(1));
 			else
 				this.end = null;
 			
 			this.iter = cur.getInt(2);
 			this.freq = cur.getInt(3);
 			this.custom = cur.getInt(4);
 			this.due = new Date(cur.getLong(5));
 			this.id = repeat_id;
 			
 			ret = true;
 		}
 		
 		cur.close();
 		return ret;
 	}
 	
 	private static int getId(String column, String where, String[] wArgs)
 	{
 		SQLiteDatabase lootDB = Database.getDatabase();
 		String[] columns = {column};
 		
 		Cursor cur = lootDB.query("repeat_transactions", columns, where, wArgs, null, null, null);
 		
 		int ret = -1;
 		if (cur.moveToFirst())
 			ret = cur.getInt(0);
 
 		cur.close();
 		return ret;
 	}
 	
 	public static int getRepeatId(int trans_id)
 	{
 		return getId("repeat_id", "trans_id = " + trans_id, null);
 	}
 	
 	public static RepeatSchedule getSchedule(int repeat_id)
 	{
 		RepeatSchedule rs = new RepeatSchedule();
 		if (rs.load(repeat_id))
 			return rs;
 		
 		return null;
 	}
 	
 	public int getTransactionId()
 	{
 		return getId("trans_id", "repeat_id = " + this.id, null);
 	}
 	
 	public int getTransferId()
 	{
 		return getId("transfer_id", "repeat_id = " + this.id, null);
 	}
 	
 	public Transaction getTransaction()
 	{
 		String[] columns = {"account", "date", "party", "amount", "check_num", "budget", "tags"};
 		SQLiteDatabase lootDB = Database.getDatabase();
 		Cursor cur = lootDB.query("repeat_transactions", columns, "repeat_id = " + this.id,
 				null, null, null, null);
 		
 		Transaction trans = null;
 		if (cur.moveToFirst())
 		{
 			int type, check_num = cur.getInt(4);
 			double amount = cur.getInt(3);
 			boolean budget = Database.getBoolean(cur.getInt(5));
 			Date date = new Date(cur.getLong(1));
 			
 			if (check_num > 0)
 			{
 				amount = -amount;
 				type = Transaction.CHECK;
 			}
 			else if (amount < 0.0)
 			{
 				amount = -amount;
 				type = Transaction.WITHDRAW;
 			}
 			else
 				type = Transaction.DEPOSIT;
 			
 			trans = new Transaction(false, budget, date, type, cur.getString(2), amount, check_num);
 			trans.account = cur.getInt(0);
 			
 			trans.addTags(cur.getString(6));
 		}
 		
 		cur.close();
 		return trans;
 	}
 	
 	public String[] getTags()
 	{
 		SQLiteDatabase lootDB = Database.getDatabase();
 		String[] columns = {"tags"};
 		Cursor cur = lootDB.query("repeat_transactions", columns, "repeat_id = " + this.id,
 				null, null, null, null);
 		if (!cur.moveToFirst())
 		{
 			cur.close();
 			return null;
 		}
 		
 		String tag_str = cur.getString(0);
 		cur.close();
 		
 		String[] tags = null;
 		if (tag_str != null)
 			tags = tag_str.split(" ");
 		if (tags == null || tags.length == 0)
 			return null;
 		
 		return tags;
 	}
 	
 	private static int[] getIds(String where, String[] wArgs)
 	{
 		SQLiteDatabase lootDB = Database.getDatabase();
 		
 		String[] columns = {"id"};
 		Cursor cur = lootDB.query("repeat_pattern", columns, where, wArgs, null, null, null);
 		if (!cur.moveToFirst())
 		{
 			cur.close();
 			return null;
 		}
 		
 		int[] ids = new int[cur.getCount()];
 		int i = 0;
 		do
 		{
 			// get the id and increment i when done
 			ids[i++] = cur.getInt(0);
 		} while (cur.moveToNext());
 		cur.close();
 		
 		// if there are no ids, return null instead of a zero-length list
 		if (ids.length == 0)
 			ids = null;
 
 		return ids;		
 	}
 	
 	public static int[] getRepeatIds()
 	{
 		return getIds(null, null);
 	}
 	
 	public static int[] getDueRepeatIds(Date date)
 	{
 		String[] wArgs = {Double.toString(date.getTime())};
 		return getIds("due <= ?", wArgs);
 	}
 	
 	public static int[] processDueRepetitions(Date date)
 	{
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 		
 		// if we post repetitions early, set the date passed in the future
 		int early_days = (int)Database.getOptionInt("post_repeats_early");
 		if (early_days >= 0)
 			cal.add(Calendar.DAY_OF_YEAR, early_days);
 		
 		int[] ids = getDueRepeatIds(cal.getTime());
 		if (ids == null)
 			return null;
 		
 		ArrayList<Integer> new_trans_ids = new ArrayList<Integer>();
 		ArrayList<Integer> ret_list = null;
 		
 		RepeatSchedule pattern = new RepeatSchedule();
 		ArrayList<Integer> new_ids = new ArrayList<Integer>();
 		
 		int i, id;
 		for (i = 0; i < ids.length; ++i)
 		{
 			id = ids[i];
 			pattern.load(id);
 			
 			// write the transaction
 			int trans_id = pattern.writeTransaction(pattern.due);
 			if (trans_id != -1)
 			{
 				new_trans_ids.add(trans_id);
 				
 				int transfer_id = pattern.getTransferId();
 				if (transfer_id != -1)
 				{
 					Transaction transfer = Transaction.getTransactionById(transfer_id, true);
 					if (transfer == null)
 					{
 						transfer = Transaction.getTransactionById(trans_id);
 						transfer.linkTransfer(trans_id, transfer_id);
 					}
 					else if (transfer.getTransferId() == transfer_id)
 						transfer.linkTransfer(trans_id, transfer_id);
 				}
 			}
 
 			try
 			{
 				// clone this schedule, then delete it
 				RepeatSchedule new_repeat = (RepeatSchedule) pattern.clone();
 				pattern.erase(false);
 
 				// change the start date and write a new repeat pattern
 				new_repeat.start = (Date)new_repeat.due.clone();
 				new_repeat.id = -1;
 				int repeat_id = new_repeat.write(trans_id);
 
 				// if the write was successful, add it to the list of newly created ids
 				if (repeat_id != -1)
 					new_ids.add(repeat_id);
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 		
 		// if there were new ids in this run, add them to the list of ids to return
 		if (new_ids.size() != 0)
 		{
 			int[] more_trans_ids = processDueRepetitions(date);
 			if (more_trans_ids != null)
 			{
 				// make a new list out of new_trans_ids and more_trans_ids
 				ret_list = new ArrayList<Integer>();
 				ret_list.addAll(new_trans_ids);
 				for ( Integer val : more_trans_ids )
 					ret_list.add(val);
 			}
 			else
 				// if there were no new ids while processing more repetitions,
 				// set the ret_list to the new ids gained from this run
 				ret_list = new_trans_ids;
 		}
 		else
 		{
 			ret_list = new_trans_ids;
 		}
 		
 		if (ret_list == null)
 			return null;
 		
 		// convert ret_list to int[]
 		int[] arr = new int[ret_list.size()];
 		i = 0;
 		for ( int val : ret_list )
 			arr[i++] = val;
 		Arrays.sort(arr);
 		
 		return arr;
 	}
 	
 	public int writeTransaction(Date date)
 	{
 		if (date == null)
 			return -1;
 		
 		int trans_id = this.getTransactionId();
 		
 		// have to retrieve the next check num first because android sqlite api
 		// doesn't allow for the creation of sqlite functions
 		Transaction trans = this.getTransaction();
 		int next_check_num = 0;
 		if (trans.check_num > 0)
 		{
 			Account acct = Account.getAccountById(trans.account);
 			next_check_num = acct.getNextCheckNum();
 		}
 		
 		// insert the transaction into the transactions table
 		String insert = "insert into transactions (account,date,party,amount,check_num,budget,timestamp) " +
 						"select account," + date.getTime() + ",party,amount," + next_check_num +
 						",budget,strftime('%%s','now') from repeat_transactions where repeat_id = " + 
 						this.id;
 		
 		SQLiteDatabase lootDB = Database.getDatabase();
 		lootDB.beginTransaction();
 		
 		try
 		{
 			lootDB.execSQL(insert);
 		}
 		catch (SQLException e)
 		{
 			lootDB.endTransaction();
 			return -1;
 		}
 
 		// retrieve the new id for this row
 		String[] columns = {"max(id)"};
 		Cursor cur = lootDB.query("transactions", columns, null, null, null, null, null);
 		if (!cur.moveToFirst())
 		{
 			cur.close();
 			return -1;
 		}
 		int max = cur.getInt(0);
 		cur.close();
 		
 		// update any repeat_transactions rows that refer to this transaction as the transfer_id
 		ContentValues cv = new ContentValues();
 		cv.put("transfer_id", max);
 		
 		int updated = 0;
 		try
 		{
 			updated = lootDB.update("repeat_transactions", cv, "transfer_id = " + trans_id, null);
 		}
 		catch (SQLException e)
 		{
 			lootDB.endTransaction();
 			return -1;
 		}
 		
 		// link transaction to itself if rows are updated
 		if (updated > 0)
 		{
 			Transaction tmp = Transaction.getTransactionById(max, true);
 			tmp.linkTransfer(max, max);
 		}
 		
 		// write the tags to the tags table for the new transaction
 		trans = Transaction.getTransactionById(max, true);
 		trans.addTags(this.getTags());
 		
 		int ret = trans.write(trans.account);
 		if (ret != -1)
 			lootDB.setTransactionSuccessful();
 		lootDB.endTransaction();
 		
 		return ret;
 	}
 	
 	public boolean writeTransactionToRepeatTable(int trans_id)
 	{
 		Transaction trans = Transaction.getTransactionById(trans_id, true);
 		if (trans == null)
 			return false;
 		
 		int transfer_id = trans.getTransferId();
 		
 		// verify that both the transaction and repeat pattern exist
 		String query = "select t.id, r.id from transactions as t, repeat_pattern as r where " +
 					   "t.id = " + trans_id + " and r.id = " + this.id;
 		SQLiteDatabase lootDB = Database.getDatabase();
 		Cursor cur = lootDB.rawQuery(query, null);
 		
 		// no rows exist with those IDs
 		if (!cur.moveToFirst())
 		{
 			cur.close();
 			return false;
 		}
 		cur.close();
 		
 		String insert = "insert into repeat_transactions (trans_id,repeat_id,account,date,party,amount," +
 						"check_num,budget,transfer_id) select id," + this.id + ",account,date,party," +
 						"amount,check_num,budget," + transfer_id + " from transactions where id = " + 
 						trans_id;
 		lootDB.beginTransaction();
 		
 		try
 		{
 			lootDB.execSQL(insert);
 		}
 		catch (SQLException e)
 		{
 			lootDB.endTransaction();
 			return false;
 		}
 		
 		// copy the tags over to the new row
 		insert = "update repeat_transactions set tags = ? where trans_id = " + trans_id +
 				 " and repeat_id = " + this.id;
 		Object[] bindArgs = {trans.tagListToString()};
 		
 		try
 		{
 			lootDB.execSQL(insert, bindArgs);
 			lootDB.setTransactionSuccessful();
 		}
 		catch (SQLException e)
 		{
 			return false;
 		}
 		finally
 		{
 			lootDB.endTransaction();
 		}
 		
 		return true;
 	}
 	
 	public boolean eraseTransactionFromRepeatTable(int repeat_id2)
 	{
 		String del = "delete from repeat_transactions where repeat_id = " + this.id;
 		if (repeat_id2 != -1)
 			del += " or repeat_id = " + repeat_id2;
 		
 		SQLiteDatabase lootDB = Database.getDatabase();
 		lootDB.beginTransaction();
 		
 		try
 		{
 			lootDB.execSQL(del);
 			lootDB.setTransactionSuccessful();
 		}
 		catch (SQLException e)
 		{
 			return false;
 		}
 		finally
 		{
 			lootDB.endTransaction();
 		}
 		
 		return true;
 	}
 
 	public Date calculateDueDate()
 	{
 		if (this.freq == 0 || this.iter == NO_REPEAT ||
 				((this.end != null && this.end.getTime() > 0) && this.start.after(this.end)))
 			return null;
 		
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(this.start);
 
 		if ( this.iter == DAILY )
 		{
 			cal.add(Calendar.DAY_OF_MONTH, this.freq);
 		}
 		else if ( this.iter == WEEKLY )
 		{
 			if (this.custom == -1)
 			{
 				// standard weekly repetition
 				cal.add(Calendar.DAY_OF_MONTH, this.freq * 7);
 			}
 			else
 			{
 				// go to next day in the pattern
 				int start_day = cal.get(Calendar.DAY_OF_WEEK);
 				int first_day = cal.getFirstDayOfWeek();
 				int weekday;
 				boolean keep_going = true;
 				
 				while (keep_going)
 				{
 					cal.add(Calendar.DAY_OF_MONTH, 1);
 					weekday = cal.get(Calendar.DAY_OF_WEEK);
 					
 					if (weekday == start_day)
 					{
 						keep_going = false;
 						break;
 					}
 					
 					// if we reached the end of the week, add this.freq - 1 weeks
 					if (weekday == first_day)
 						cal.add(Calendar.DAY_OF_MONTH, 7 * (this.freq - 1));
 					
 					switch (weekday)
 					{
 						case Calendar.MONDAY:
 							if ((this.custom & RepeatSchedule.MONDAY) > 0)
 								keep_going = false;
 							break;
 							
 						case Calendar.TUESDAY:
 							if ((this.custom & RepeatSchedule.TUESDAY) > 0)
 								keep_going = false;
 							break;
 							
 						case Calendar.WEDNESDAY:
 							if ((this.custom & RepeatSchedule.WEDNESDAY) > 0)
 								keep_going = false;
 							break;
 							
 						case Calendar.THURSDAY:
 							if ((this.custom & RepeatSchedule.THURSDAY) > 0)
 								keep_going = false;
 							break;
 							
 						case Calendar.FRIDAY:
 							if ((this.custom & RepeatSchedule.FRIDAY) > 0)
 								keep_going = false;
 							break;
 							
 						case Calendar.SATURDAY:
 							if ((this.custom & RepeatSchedule.SATURDAY) > 0)
 								keep_going = false;
 							break;
 							
 						case Calendar.SUNDAY:
 							if ((this.custom & RepeatSchedule.SUNDAY) > 0)
 								keep_going = false;
 							break;
 						
 						default:
 							keep_going = false;
 							break;
 					}
 				}
 			}
 		}
 		else if (this.iter == MONTHLY)
 		{
 			if (this.custom == DATE)
 			{
 				cal.add(Calendar.MONTH, this.freq);
 			}
 			else if (this.custom == DAY)
 			{
 				int day_of_week = cal.get(Calendar.DAY_OF_WEEK);
 				int day_of_week_in_month = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
 				cal.add(Calendar.MONTH, this.freq);
 				cal.set(Calendar.DAY_OF_WEEK, day_of_week);
 				cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, day_of_week_in_month);
 			}
 		}
 		else if (this.iter == YEARLY)
 		{
 			cal.add(Calendar.YEAR, this.freq);
 		}
 		
 		Date due = cal.getTime();
		if (due.before(this.start) || (this.end != null && due.after(this.end)))
 			return null;
 		else
 			return due;
 	}
 }
