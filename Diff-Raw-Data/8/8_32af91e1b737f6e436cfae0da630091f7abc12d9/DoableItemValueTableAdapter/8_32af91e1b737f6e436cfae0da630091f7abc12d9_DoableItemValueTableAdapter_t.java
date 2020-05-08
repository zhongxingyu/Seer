 package com.com.goldmanalpha.dailydo.db;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import com.goldmanalpha.dailydo.model.*;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 public class DoableItemValueTableAdapter
         extends TableAdapterBase<DoableValue> {
 
 
     public DoableItemValueTableAdapter() {
         super(DoableItemValueTable.TableName);
     }
 
     @Override
     public long save(DoableValue object) {
         long id = super.save(object);
         return id;
     }
 
 
     @Override
     protected ContentValues createContentValues(DoableValue object) {
         ContentValues values = super.createContentValues(object);
 
         values.put("appliesToDate", super.DateToTimeStamp(object.getAppliesToDate()));
         values.put("itemId", object.getDoableItemId());
 
         values.put("description", object.getDescription());
         values.put("potency", object.getPotency());
 
         if (object.getItem().getUnitType() != UnitType.time
                 && object.getItem().getUnitType() != UnitType.timeSpan) {
             values.putNull("fromTime");
             values.putNull("toTime");
 
             if (object.getAppliesToTime() == null)
                 values.putNull("appliesToTime");
             else
                 values.put("appliesToTime", TimeToInt(object.getAppliesToTime()));
 
             values.put("amount", object.getAmount());
         } else {
             values.putNull("amount");
             values.put("fromTime", TimeToInt(object.getFromTime()));
             values.put("toTime", TimeToInt(object.getToTime()));
 
             values.putNull("appliesToTime");
         }
 
         values.put("hasAnotherDayInstance", object.getHasAnotherDayInstance() ? 1 : 0);
         values.put("teaspoons", object.getTeaspoons().toString());
 
         //todo: handle case of item being added out of order :(
         //probably only if appliesToDate < current date
         //need to open the next item and save it...
 
         values.put("previousValueId", getPreviousId(object.getDoableItemId(), object.getAppliesToDate()));
 
         return values;
     }
 
 
     public int getPreviousId(int itemId, Date date) {
         Cursor c = db.rawQuery(
                 "select max(dateCreated) as maxDate from " + this.tableName
                         + " where itemId = ? "
                         + " and appliesToDate < ?"
                         + " order by dateCreated desc",
                 new String[]{"" + itemId, super.DateToTimeStamp(date)});
 
         if (c.moveToFirst()) {
 
             String lastDate = c.getString(c.getColumnIndex("maxDate"));
 
             c.close();
 
             if (lastDate != null) {
                 Cursor c2 = db.rawQuery(
                         "select id from " + this.tableName
                                 + " where dateCreated = ? "
                                 + " and itemId = ?",
                         new String[]{lastDate, "" + itemId});
 
                 c2.moveToFirst();
 
                 int id = c2.getInt(0);
 
                 c2.close();
 
                 return id;
             }
         }
 
         return 0;
     }
 
     public static final String ColId = "id";
     public static final String ColItemId = "items_id";
 
     public static final String ColItemName = "items_name";
     public static final String ColUnitType = "unitType";
     public static final String ColAmount = "amount";
     public static final String ColTeaspoons = "teaspoons";
     public static final String ColLastTeaspoons = "lastTeaspoons";
 
     public static final String ColPotency = "potency";
     public static final String ColLastPotency = "lastPotency";
 
     public static final String ColLastAmount = "lastAmount";
     public static final String ColLastAppliesToDate = "lastAppliesToDate";
     public static final String ColAppliesToDate = "appliesToDate";
 
     public static final String ColDescription = "description";
     public static final String ColPrivate = "private";
     public static final String ColDateCreated = "dateCreated";
     public static final String ColDateModified = "dateModified";
 
     public static final String ColFromTime = "fromTime";
 
     public static final String ColLastFromTime = "lastFromTime";
 
     public static final String ColToTime = "toTime";
     public static final String ColLastToTime = "lastToTime";
 
     public static final String ColAppliesToTime = "appliesToTime";
     public static final String ColShowAppliesToTimeCount = "showAppliesToTimeCount";
 
     //placeholders support using mapping infrastructure
     public static final String ColPlaceHolder1 = "placeHolder1";
     public static final String ColPlaceHolder2 = "placeHolder2";
     public static final String ColPlaceHolder3 = "placeHolder3";
 
     public void recalcDisplayOrder() {
         open();
 
         String sql = "select i.id, m.valueId, i.displayOrder "
                 + " from DoableItem i left outer join ViewItemValueMax m  "
                 + " on m.itemId = i.id order by nullif(i.id, m.itemId),  m.valueId desc ;";
 
         Cursor c = db.rawQuery(sql, new String[]{});
 
         int order = 0;
 
         List<DoableItem> items = new ArrayList<DoableItem>();
 
         if (c.moveToFirst()) {
             do {
                 if (c.getInt(2) != order) {
 
                     DoableItem item = new DoableItem(c.getInt(0));
                     item.setDisplayOrder(order);
                     items.add(item);
                 }
 
                 order++;
             } while (c.moveToNext());
         }
 
         c.close();
 
         if (!items.isEmpty()) {
             DoableItemTableAdapter adapter = new DoableItemTableAdapter();
 
             for (int i = 0; i < items.size(); i++) {
 
                 DoableItem item = items.get(i);
 
                 adapter.updateOrder(item.getId(), item.getDisplayOrder());
             }
         }
     }
 
     public Cursor getItems(Integer itemId, int limitToCategoryId) {
 
         DoableItemTableAdapter t = new DoableItemTableAdapter();
 
         //todo: add always show the the params here...
         String categoryWhere = "";
 
         String whereClauseToken = "{WhereClause}";
 
         String sql = "select vals.appliesToDate, vals.appliesToTime, "
                 + " vals.id as _id, vals.id, vals.description, "
                 + " vals.fromTime, vals.toTime, vals.amount, "
                 + " vals.teaspoons, potency, i.unitType, "
 
                 + " coalesce(vals.hasAnotherDayInstance, 0) showAppliesToTimeCount, "
 
                 + " vals.dateCreated, vals.dateModified, i.name items_name from "
                 + this.tableName + " vals join DoableItem i "
                 + " on vals.itemId = i.id "
                 + " " + whereClauseToken
                 + " order by appliesToDate desc, appliesToTime desc, i.id desc";
 
         boolean isMultipleItems = itemId == 0;
         boolean isSingleCategory = limitToCategoryId > 0;
 
         String[] params;
         String whereClause = "";
 
         if (isMultipleItems) {
 
             Calendar cal = Calendar.getInstance();
             cal.add(Calendar.DATE, -64);    //todo: allow scroll to add days past
             String timeStamp = super.DateToTimeStamp(cal.getTime());
 
             whereClause = " where appliesToDate > ? ";
 
             if (isSingleCategory) {
                 whereClause = whereClause + " and i.CategoryId = ?";
                 params = new String[]{timeStamp, Integer.toString(limitToCategoryId)};
             } else {
                 params = new String[]{timeStamp};
             }
 
         } else {
             whereClause = " where itemId = ? ";
             params = new String[]{Integer.toString(itemId)};
         }
 
         sql = sql.replace(whereClauseToken, whereClause);
         return db.rawQuery(sql, params);
     }
 
     //returns a cursor of doable items:
     public Cursor getItems(Date date, boolean showPrivate, int categoryId) {
 
         open();
 
         String categorySql = "";
 
         switch (categoryId) {
             case SimpleLookup.UNSET_ID:
                categorySql = " and (categoryId = 0 or categoryId is null)";
 
                 break;
 
             case SimpleLookup.ALL_ID:
                 //leave blank
                 break;
 
             case SimpleLookup.HAS_VALUE_ID:
                categorySql = " and vals.id is not null ";
                 break;
 
             default:
 
                 categorySql = " and categoryId = " + categoryId;
 
         }
 
         String sql = "select "
                 + " vals.id as _id, vals.id, vals.description, "
                 + " vals.fromTime, vals.toTime, vals.amount, "
                 + " vals.teaspoons, vals.potency,"
 
                 + " vals.dateCreated, vals.dateModified, "
 
                 + " items.id as items_id, items.name as items_name, items.unitType, items.private, "
                 + " coalesce(lastVal.teaspoons, vals.teaspoons, latestVal.teaspoons) lastTeaspoons, "
                 + " coalesce(lastVal.potency, vals.potency, latestVal.potency) lastPotency, "
                 + " coalesce(lastVal.amount, vals.amount, latestVal.amount) lastAmount, "
                 + " coalesce(lastVal.fromTime, vals.fromTime, latestVal.fromTime) lastFromTime, "
                 + " coalesce(lastVal.toTime, vals.toTime, latestVal.toTime) lastToTime, "
                 + " coalesce(lastVal.appliesToDate, vals.appliesToDate, latestVal.appliesToDate) lastAppliesToDate, "
 
                 //when 0 don't show, otherwise show
                 + " coalesce(items.showAppliesToTime, 0) + coalesce(vals.hasAnotherDayInstance, 0) showAppliesToTimeCount, "
 
 
                 //can be null
                 + " vals.appliesToTime appliesToTime, "
 
                 + " null placeHolder1, null placeHolder2, null placeHolder3 "
                 + " from " + DoableItemTable.TableName + " as items "
                 + " left outer join " + this.tableName + " as vals "
                 + " on vals.itemId = items.id "
                 + " and vals.appliesToDate = ?"
                 + " left outer join " + this.tableName + " as lastVal "
                 + " on vals.previousValueId = lastVal.id "
 
                 + " left outer join ViewItemValueMax as valueMaxJunction "
                 + " on items.id = valueMaxJunction.itemId "
 
                 + " left outer join " + this.tableName + " as latestVal "
                 + " on valueMaxJunction.valueId = latestVal.id "
 
                 + " where 1 = 1"
 
                 + (showPrivate ? "" : " and items.private = 0")
 
                 + categorySql
 
                 + " order by items.displayOrder";
 
         // + " order by valueMaxJunction.valueId desc, items.dateCreated desc";
 
         Cursor cursor = db.rawQuery(sql, new String[]{super.DateToTimeStamp(date)});
 
         return cursor;
     }
 
     @Override
     public DoableValue get(int id) throws ParseException {
         Cursor c = getSingle(id);
 
         DoableValue val = new DoableValue();
 
         if (c.moveToFirst()) {
 
             val = new DoableValue(c.getInt(c.getColumnIndex("id")));
 
             super.setCommonValues(val, c);
 
             val.setAmount(c.getFloat(c.getColumnIndex("amount")));
             val.setAppliesToDate(simpleDateFormat.parse(c.getString(c.getColumnIndex("appliesToDate"))));
 
             val.setFromTime(IntToTime(c.getInt(c.getColumnIndex("fromTime"))));
             val.setToTime(IntToTime(c.getInt(c.getColumnIndex("toTime"))));
 
             val.setDoableItemId(c.getInt(c.getColumnIndex("itemId")));
 
             val.setDescription(c.getString(c.getColumnIndex(ColDescription)));
 
             val.setTeaspoons(
                     TeaSpoons.valueOf(
                             c.getString(c.getColumnIndex("teaspoons"))));
 
             val.setPotency(c.getInt(c.getColumnIndex("potency")));
 
             val.setHasAnotherDayInstance(c.getInt(c.getColumnIndex("hasAnotherDayInstance")) > 0);
 
 
             int appliesToCol = c.getColumnIndex("appliesToTime");
             if (!c.isNull(appliesToCol)) {
                 val.setAppliesToTime(IntToTime(c.getInt(appliesToCol)));
             }
         }
 
         c.close();
 
         return val;
     }
 
     public void createDuplicate(int id) throws ParseException {
 
         DoableValue value = get(id);
 
         value.setHasAnotherDayInstance(true);
 
         DoableValue copy = new DoableValue(value);
         copy.setHasAnotherDayInstance(true);
 
         save(value);
         save(copy);
     }
 
     @Override
     public void delete(int id) throws ParseException {
 
         DoableValue deletingItem = get(id);
 
         super.delete(id);    //To change body of overridden methods use File | Settings | File Templates.
 
        //if this is a single item left, make sure its dup flag is off
         Cursor cursor = db.rawQuery("select id from " + this.tableName
                 + " where itemId = ? and appliesToDate = ? ",
                 new String[]{
                         Integer.toString(deletingItem.getItem().getId()),
                         super.DateToTimeStamp(deletingItem.getAppliesToDate())
                 });
 
         if (cursor.getCount() == 1 && cursor.moveToFirst()) {
             int remainingId = cursor.getInt(0);
             DoableValue remainder = get(remainingId);
             remainder.setHasAnotherDayInstance(false);
             save(remainder);
         }
     }
 
     public Date getAppliesToDate(Cursor cursor) throws ParseException {
 
         String appliesToDate =
                 cursor.getString(cursor.getColumnIndex(DoableItemValueTableAdapter.ColAppliesToDate));
 
         return TimeStampToDate(appliesToDate);
 
     }
 }
 
 
