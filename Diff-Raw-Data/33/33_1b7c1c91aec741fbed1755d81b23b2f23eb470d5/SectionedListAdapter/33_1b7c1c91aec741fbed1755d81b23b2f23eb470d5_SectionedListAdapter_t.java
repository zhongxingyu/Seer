 package com.appspot.manup.signup.ui;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DataSetObservable;
 import android.database.DataSetObserver;
 import android.provider.BaseColumns;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListAdapter;
 
 public abstract class SectionedListAdapter implements ListAdapter
 {
     @SuppressWarnings("unused")
     private static final String TAG = SectionedListAdapter.class.getSimpleName();
 
     private static final int VIEW_TYPE_HEADER = 0;
     private static final int VIEW_TYPE_ITEM = 1;
     private static final int VIEW_TYPE_COUNT = 2;
 
     private final class Section
     {
         private final Cursor mCursor;
         private final int mSectionId;
         private final int mHeaderPos;
         private final int mEndPos;
         private final int mItemCount;
 
         Section(final Cursor cursor, final int sectionId, final int headerPos)
         {
             super();
             mCursor = cursor;
             mSectionId = sectionId;
             mHeaderPos = headerPos;
             mItemCount = mCursor.getCount();
             mEndPos = headerPos + mItemCount;
         } // constructor()
 
         Section append(final Cursor cursor, final int sectionId)
         {
             return new Section(cursor, sectionId, mEndPos + 1);
         } // append(Cursor, int)
 
         Cursor getItem(final int listPos)
         {
             if (!mCursor.moveToPosition(toCursorPos(listPos)))
             {
                 throw new IllegalStateException("Couldn't move cursor to position " + listPos);
             } // if
             return mCursor;
         } // getItem(int)
 
         long getItemId(final int listPos)
         {
            if (isHeader(listPos))
             {
                return -mSectionId;
             } // if
             else
             {
                return getItem(listPos).getLong(mIdCol);
             } // else
         } // getItemId(int)
 
         int getItemViewType(final int listPos)
         {
             return isHeader(listPos) ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
         } // getViewItemType(int)
 
         boolean isEnabled(final int listPos)
         {
             return !isHeader(listPos);
         } // isEnabled(int)
 
         boolean isHeader(final int listPos)
         {
             return listPos == mHeaderPos;
         } // isHeader(int)
 
         boolean withinSection(final int listPos)
         {
             return listPos >= mHeaderPos && listPos <= mEndPos;
         } // withinSection(int)
 
         int toCursorPos(final int listPos)
         {
             if (!withinSection(listPos) || isHeader(listPos))
             {
                 throw new IllegalArgumentException("List position " + listPos
                         + " is not within this section body [" + (mHeaderPos + 1) + "," + mEndPos
                         + "].");
             } // if
             return listPos - mHeaderPos - 1;
         } // toCusorIndex(int)
 
     } // class Section
 
     private DataSetObserver mObserver = new DataSetObserver()
     {
         @Override
         public void onChanged()
         {
             mCursorsValid = true;
             notifyDataSetChanged();
         } // onchanged()
 
         @Override
         public void onInvalidated()
         {
             mCursorsValid = false;
             notifyDataSetInvalidated();
         } // onInvalidated()
     };
 
     private final ArrayList<Section> mSections = new ArrayList<Section>();
     private final DataSetObservable mDataSetObservable = new DataSetObservable();
 
     private final Context mContext;
     private final Cursor[] mCursors;
     private final int mSectionCount;
 
     private boolean mCursorsValid = false;
     private int mItemCount = 0;
 
     private int mIdCol = Integer.MIN_VALUE;
 
     public SectionedListAdapter(final Context context, final int sectionCount)
     {
         super();
         mContext = context;
         mCursors = new Cursor[sectionCount];
         mSectionCount = sectionCount;
     } // constructor(int)
 
     public void changeCursors(final Cursor[] cursors)
     {
         if (cursors == null)
         {
             throw new IllegalArgumentException("sections cannot be null.");
         } // if
 
         if (cursors.length != mSectionCount)
         {
             throw new IllegalArgumentException("Required " + mSectionCount + " sections, got "
                     + cursors.length + ".");
         } // if
 
         reset();
         System.arraycopy(cursors, 0, mCursors, 0, mSectionCount);
 
         Section section = null;
         for (int sectionId = 0; sectionId < mSectionCount; sectionId++)
         {
             final Cursor cursor = cursors[sectionId];
             if (cursor != null && cursor.getCount() > 0)
             {
                 cursor.registerDataSetObserver(mObserver);
                 if (section != null)
                 {
                     section = section.append(cursor, sectionId);
                 } // if
                 else
                 {
                     section = new Section(cursor, sectionId, 0);
                 } // else
                 mSections.add(section);
                 mItemCount += section.mItemCount;
             } // if
         } // for
 
         if (section != null)
         {
             final Cursor cursor = section.mCursor;
             mIdCol = cursor.getColumnIndexOrThrow(BaseColumns._ID);
         } // if
 
         mCursorsValid = true;
         notifyDataSetChanged();
     } // changeSectionData(Cursor[])
 
     @Override
     public View getView(final int listPos, final View convertView, final ViewGroup parent)
     {
         if (!mCursorsValid)
         {
             throw new IllegalStateException(
                     "This should only be called when the cursors are valid.");
         } // if
 
         final Section section = getSection(listPos);
 
         final boolean getHeader = section.isHeader(listPos);
 
         View view = convertView;
         if (view == null)
         {
             view = getHeader ? newHeader(mContext, parent) : newItem(mContext, parent);
         } // if
 
         if (getHeader)
         {
             bindHeader(mContext, view, section.mSectionId);
         } // if
         else
         {
             bindItem(mContext, view, section.getItem(listPos), section.mSectionId);
         } // else
 
         return view;
     } // getView(int, View, ViewGroup)
 
     protected abstract View newHeader(Context context, ViewGroup parent);
 
     protected void bindHeader(final Context context, final View headerView, final int sectionId)
     {
         // Do nothing.
     } // bindHeader(Context, View, int)
 
     protected abstract View newItem(Context context, ViewGroup parent);
 
     protected void bindItem(final Context context, final View itemView, final Cursor item,
             final int sectionId)
     {
         // Do nothing.
     } // bindItem(Context, View, Cursor, int)
 
     public void close()
     {
         reset();
         notifyDataSetInvalidated();
     } // close()
 
     private void reset()
     {
         for (int i = 0; i < mSectionCount; i++)
         {
             final Cursor cursor = mCursors[i];
 
             if (cursor != null && cursor.getCount() > 0)
             {
                 cursor.unregisterDataSetObserver(mObserver);
                 cursor.close();
             } // if
 
             mCursors[i] = null;
         } // for
         mSections.clear();
         mItemCount = 0;
         mIdCol = Integer.MIN_VALUE;
         mCursorsValid = false;
     } // reset()
 
     private Section getSection(final int listPos)
     {
         for (final Section section : mSections)
         {
             if (section.withinSection(listPos))
             {
                 return section;
             } // if
         } // for
         throw new IllegalArgumentException("List position " + listPos + " is invalid.");
     } // getSection(int)
 
     @Override
     public boolean areAllItemsEnabled()
     {
         return false;
     } // areAllItemsEnabled()
 
     @Override
     public int getCount()
     {
         // The size of mSections is the number of headers.
         return mItemCount + mSections.size();
     } // getCount()
 
     @Override
     public Object getItem(int listPos)
     {
         return getSection(listPos).getItem(listPos);
     } // getItem(int)
 
     @Override
     public long getItemId(int listPos)
     {
        if (mCursorsValid)
        {
            return getSection(listPos).getItemId(listPos);
        } // if
        else
        {
            /*
             * I'm not sure this is the right thing to do, I'm just coping
             * CursorAdapter.
             */
            return 0;
        } // else
     } // getItemId(int)
 
     @Override
     public boolean hasStableIds()
     {
         return true;
     } // hasStableIds()
 
     @Override
     public int getItemViewType(final int listPos)
     {
         return getSection(listPos).getItemViewType(listPos);
     } // getItemViewType(int)
 
     @Override
     public int getViewTypeCount()
     {
         return VIEW_TYPE_COUNT;
     } // getViewTypeCount()
 
     @Override
     public boolean isEmpty()
     {
         return mSections.isEmpty();
     } // isEmpty()
 
     @Override
     public boolean isEnabled(final int listPos)
     {
         return getSection(listPos).isEnabled(listPos);
     } // isEnabled(int)
 
     @Override
     public void registerDataSetObserver(final DataSetObserver observer)
     {
         mDataSetObservable.registerObserver(observer);
     } // registerDataSetObserver(DataSetObserver)
 
     @Override
     public void unregisterDataSetObserver(final DataSetObserver observer)
     {
         mDataSetObservable.unregisterObserver(observer);
     } // unregisterDataSetObserver(DataSetObserver)
 
     public void notifyDataSetChanged()
     {
         mDataSetObservable.notifyChanged();
     } // notifyDataSetChanged()
 
     public void notifyDataSetInvalidated()
     {
         mDataSetObservable.notifyInvalidated();
     } // notifyDataSetInvalidated()
 
 } // class SectionedListAdapter
