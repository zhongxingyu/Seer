 package com.aragaer.reminder;
 
 import java.util.List;
 
 import android.database.Cursor;
 import android.database.CursorWrapper;
 
 public class ReorderedCursor extends CursorWrapper {
 	int position;
 	private final int count, position_map[];
 	public ReorderedCursor(final Cursor cursor) {
 		super(cursor);
 		count = cursor.getCount();
 		position_map = new int[count];
 	}
 
 	public int getPosition() {
 		return position;
 	}
 
 	public boolean isAfterLast() {
 		return position >= count;
 	}
 
 	public boolean isBeforeFirst() {
 		return position < 0;
 	}
 
 	public boolean isFirst() {
 		return position == 0;
 	}
 
 	public boolean isLast() {
 		return position == count - 1;
 	}
 
 	final boolean try_move(final int to) {
 		position = to;
 		try {
 			return super.moveToPosition(position_map[to]);
 		} catch (ArrayIndexOutOfBoundsException e) {
 			return false;
 		}
 	}
 	public boolean moveToFirst() {
 		return try_move(0);
 	}
 
 	public boolean moveToLast() {
 		return try_move(count - 1);
 	}
 
 	public boolean moveToNext() {
 		return try_move(position + 1);
 	}
 	
 	public boolean moveToPrevious() {
 		return try_move(position - 1);
 	}
 
 	public boolean moveToPosition(final int position) {
 		return try_move(position);
 	}
 
 	public ReorderedCursor setOrder(final List<Long> ids) {
 		final int id_col = getColumnIndex("_id");
 		int position = 0;
		if (super.moveToFirst())
			do {
				position_map[ids.indexOf(Long.valueOf(super.getLong(id_col)))] = position++;
			} while (super.moveToNext());
 		return this;
 	}
 }
