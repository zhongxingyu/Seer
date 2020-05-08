 package org.dodgybits.shuffle.android.core.model.persistence;
 
 import static org.dodgybits.shuffle.android.core.util.Constants.cFlurryCountParam;
 import static org.dodgybits.shuffle.android.core.util.Constants.cFlurryCreateEntityEvent;
 import static org.dodgybits.shuffle.android.core.util.Constants.cFlurryDeleteEntityEvent;
 import static org.dodgybits.shuffle.android.core.util.Constants.cFlurryEntityTypeParam;
 import static org.dodgybits.shuffle.android.core.util.Constants.cFlurryUpdateEntityEvent;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.dodgybits.shuffle.android.core.activity.flurry.Analytics;
 import org.dodgybits.shuffle.android.core.model.Entity;
 import org.dodgybits.shuffle.android.core.model.Id;
 
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.BaseColumns;
 
 public abstract class AbstractEntityPersister<E extends Entity> implements EntityPersister<E> {
 
     protected Analytics mAnalytics;
     
     protected ContentResolver mResolver;
     protected Map<String, String> mFlurryParams;
     
     public AbstractEntityPersister(ContentResolver resolver, Analytics analytics) {
         mResolver = resolver;
         mAnalytics = analytics;
         
         Map<String, String> params = new HashMap<String,String>();
         params.put(cFlurryEntityTypeParam, getEntityName());
         mFlurryParams = Collections.unmodifiableMap(params);
     }
     
     @Override
     public E findById(Id localId) {
         E entity = null;
         
         if (localId.isInitialised()) {
             Cursor cursor = mResolver.query(
                     getContentUri(), 
                     getFullProjection(),
                     BaseColumns._ID + " = ?", 
                     new String[] {localId.toString()}, 
                     null);
             
             if (cursor.moveToFirst()) {
                 entity = read(cursor);
             }
             cursor.close();
         }
         
         return entity;
     }
     
     @Override
     public Uri insert(E e) {
         validate(e);
         Uri uri = mResolver.insert(getContentUri(), null);
         update(uri, e);
         mAnalytics.onEvent(cFlurryCreateEntityEvent, mFlurryParams);
         return uri;
     }
 
     @Override
     public void bulkInsert(Collection<E> entities) {
         int numEntities = entities.size();
         if (numEntities > 0) {
             ContentValues[] valuesArray = new ContentValues[numEntities];
             int i = 0;
             for(E entity : entities) {
                 validate(entity);
                 ContentValues values = new ContentValues();
                 writeContentValues(values, entity);
                 valuesArray[i++] = values;
             }
             int rowsCreated = mResolver.bulkInsert(getContentUri(), valuesArray);
 
             Map<String, String> params = new HashMap<String, String>(mFlurryParams);
             params.put(cFlurryCountParam, String.valueOf(rowsCreated));
             mAnalytics.onEvent(cFlurryCreateEntityEvent, params);
         }
     }
 
     @Override
     public void update(E e) {
         validate(e);
         Uri uri = getUri(e);
         update(uri, e);
         
         mAnalytics.onEvent(cFlurryUpdateEntityEvent, mFlurryParams);
     }
 
     @Override
     public boolean delete(Id id) {
         Uri uri = getUri(id);
         boolean success = (mResolver.delete(uri, null, null) == 1);
         if (success) {
             mAnalytics.onEvent(cFlurryDeleteEntityEvent, mFlurryParams);
         }
 
         return success;
     }
     
     abstract public Uri getContentUri();
     
     abstract protected void writeContentValues(ContentValues values, E e);
     
     abstract protected String getEntityName();
     
     private void validate(E e) {
         if (e == null || !e.isInitialized()) {
             throw new IllegalArgumentException("Cannot persist uninitialised entity " + e);
         }
     }
     
     private Uri getUri(E e) {
         return getUri(e.getLocalId());
     }
 
     private Uri getUri(Id localId) {
         return ContentUris.appendId(
                 getContentUri().buildUpon(), localId.getId()).build();
     }
     
     private void update(Uri uri, E e) {
         ContentValues values = new ContentValues();
         writeContentValues(values, e);
         mResolver.update(uri, values, null, null);
     }
     
     
     protected static Id readId(Cursor cursor, int index) {
         Id result = Id.NONE;
         if (!cursor.isNull(index)) {
             result = Id.create(cursor.getLong(index));
         }
         return result;
     }
     
     protected static String readString(Cursor cursor, int index) {
         return (cursor.isNull(index) ? null : cursor.getString(index));
     }
     
     protected static long readLong(Cursor cursor, int index) {
         return readLong(cursor, index, 0L);
     }
     
     protected static long readLong(Cursor cursor, int index, long defaultValue) {
         long result = defaultValue;
         if (!cursor.isNull(index)) {
             result = cursor.getLong(index);
         }
         return result;
     }
     
     protected static Boolean readBoolean(Cursor cursor, int index) {
         return (cursor.getInt(index) == 1);
     }
     
     protected static void writeId(ContentValues values, String key, Id id) {
         if (id.isInitialised()) {
             values.put(key, id.getId());
         }
     }
     
     protected static void writeBoolean(ContentValues values, String key, boolean value) {
         values.put(key, value ? 1 : 0);
     }
     
     protected static void writeString(ContentValues values, String key, String value) {
         if (value == null) {
             values.putNull(key);
         } else {
             values.put(key, value);
         }
         
     }
     
 }
