 package com.htb.cnk.data;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 import com.htb.cnk.lib.Http;
 import com.htb.constant.ErrorNum;
 import com.htb.constant.Server;
 
 /**
  * @author josh
  * 
  */
 public class Dishes {
 
 	final static int ID_COLUMN = 0;
 	final static int NAME_COLUMN = 1;
 	final static int PRICE_COLUMN = 2;
 	final static int PIC_COLUMN = 3;
 	final static int PRINTER_COLUMN = 4;
 	final static int UNIT_NAME = 5;
 	
 	private List<Dish> mDishes = new ArrayList<Dish>();
 	private List<Integer> mSoldOutItemsId = new ArrayList<Integer>();
 	private int mCategoryId;
 	//private String mTableName = "";
	private CnkDbHelper mCnkDbHelper;
	private SQLiteDatabase mDb;
 	private Context mContext;
 
 	public Dishes(Context context) {
 		mContext = context;
 	}
 
 	public int setCategory(int categoryId) {
 		if (mCategoryId == categoryId) {
 			return 0;
 		}
 		mCategoryId = categoryId;
 		mDishes.clear();
 		int ret = fillCategoriesData();
 		if (ret < 0) {
 			return ret;
 		}
 		// ret = removeSoldOutItems(mCategoryId);
 		return ret;
 	}
 
 	public int count() {
 		return mDishes.size();
 	}
 
 	public Dish getDish(int position) {
 		return mDishes.get(position);
 	}
 
 	public int getDishId(int position) {
 		return mDishes.get(position).getId();
 	}
 
 	public void addAll(List<Dish> list) {
 		
 		mDishes.clear();
 		mDishes.addAll(list);
 	}
 
 	public void clear() {
 		mDishes.clear();
 	}
 	
 	private int fillCategoriesData() {
 		int ret = connectDB();
 		if (ret < 0) {
 			return ret;
 		}
 		
 		try {
 			Cursor dishes = getDishesFromDataBase(mCategoryId);
 			while (dishes.moveToNext()) {
 				mDishes.add(new Dish(dishes.getInt(ID_COLUMN), dishes
 						.getString(NAME_COLUMN), dishes.getFloat(PRICE_COLUMN),
 						dishes.getString(PIC_COLUMN), dishes.getString(UNIT_NAME), dishes
 								.getInt(PRINTER_COLUMN)));
 			}
 			return 0;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return ErrorNum.DB_BROKEN;
 		}
 	}
 
 	private int connectDB() {
 		if (mCnkDbHelper == null) {
 			try {
 				mCnkDbHelper = new CnkDbHelper(mContext, CnkDbHelper.DB_MENU,
 						null, 1);
 				mDb = mCnkDbHelper.getReadableDatabase();
 			} catch (Exception e) {
 				e.printStackTrace();
 				return ErrorNum.DB_BROKEN;
 			}
		} else if (mDb == null) {
			mDb = mCnkDbHelper.getReadableDatabase();
 		}
 		
 		return 0;
 	}
 
 	private Cursor getDishesFromDataBase(int categoryId) {
 		String sql = String
 				.format("SELECT %s.%s, %s, %s, %s, %s, %s FROM %s,%s,%s Where %s.%s=%s.%s and %s.%s=%d and %s.%s=%s",
 						CnkDbHelper.TABLE_DISH_INFO, CnkDbHelper.DISH_ID,
 						CnkDbHelper.DISH_NAME,
 						CnkDbHelper.DISH_PRICE,
 						CnkDbHelper.DISH_PIC,
 						CnkDbHelper.DISH_PRINTER,
 						CnkDbHelper.UNIT_NAME,
 						CnkDbHelper.TABLE_DISH_INFO,
 						CnkDbHelper.TABLE_DISH_CATEGORY,
 						CnkDbHelper.TABLE_UNIT,
 						CnkDbHelper.TABLE_DISH_INFO, CnkDbHelper.DISH_ID,
 						CnkDbHelper.TABLE_DISH_CATEGORY,CnkDbHelper.DC_DISH_ID,
 						CnkDbHelper.TABLE_DISH_CATEGORY, CnkDbHelper.CATEGORY_ID, categoryId,
 						CnkDbHelper.TABLE_UNIT, "id", CnkDbHelper.UNIT_ID);
 		return mDb.rawQuery(sql, null);
 	}
 
 	private int removeSoldOutItems(int id) {
 		int ret;
 		ret = loadSoldOutItems(id);
 		if (ret < 0) {
 			return ret;
 		}
 
 		for (int i : mSoldOutItemsId) {
 			Iterator<Dish> iterator = mDishes.iterator();
 			while (iterator.hasNext()) {
 				Dish d = iterator.next();
 				if (d.getId() == i) {
 					iterator.remove();
 				}
 			}
 		}
 		return 0;
 	}
 
 	private int loadSoldOutItems(int id) {
 		String dishStatusPkg = Http.get(Server.GET_DISH_STATUS, "CID="
 				+ Integer.toString(id));
 
 		if (dishStatusPkg == null) {
 			return ErrorNum.GET_SOLDOUT_ITEM_FAILED;
 		}
 		int start = dishStatusPkg.indexOf("[");
 		int end = dishStatusPkg.indexOf("]");
 		if ((start < 0) || (end < 0)) {
 			return ErrorNum.GET_SOLDOUT_ITEM_FAILED;
 		}
 
 		mSoldOutItemsId.clear();
 
 		String dishStatus = dishStatusPkg.subSequence(start + 1, end)
 				.toString();
 		if (dishStatus.length() <= 0) {
 			return 0;
 		}
 
 		String dishStatusArray[] = dishStatus.split(",");
 		for (int i = dishStatusArray.length - 1; i >= 0; i--) {
 			mSoldOutItemsId.add(Integer.parseInt(dishStatusArray[i]));
 		}
 		return 0;
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		if (mDb != null) {
 			mDb.close();
			mDb = null;
 		}
 		super.finalize();
 	}
 
 }
