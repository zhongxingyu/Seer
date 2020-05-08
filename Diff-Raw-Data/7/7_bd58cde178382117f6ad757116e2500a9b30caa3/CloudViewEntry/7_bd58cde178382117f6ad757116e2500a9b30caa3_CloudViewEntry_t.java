 package com.cloudappstudio.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public class CloudViewEntry implements Parcelable{
 	private List<ColumnValue> columnValues = new ArrayList<ColumnValue>();
 	
 	public CloudViewEntry(Parcel parcel) {
 		readFromParcel(parcel);
 	}
 	
 	public CloudViewEntry(List<ColumnValue> values) {
 		this.columnValues = values;
 	}
 
 	public ColumnValue getValue(int index) {
 		return columnValues.get(index);
 	}
 
 	public String getValueByColumnName(String column) {
 		for (ColumnValue value : columnValues) {
 			if (value.getColumn().equals(column))
 				return value.getValue();
 		}
		
		String value = "";
		if (columnValues.size() > 0)
			value = columnValues.get(0).getValue();
		
		return value;
 	}
 	
     public List<ColumnValue> getColumnValues() {
 		return columnValues;
 	}
 
 	public void setColumnValues(List<ColumnValue> columnValues) {
 		this.columnValues = columnValues;
 	}
 	
 	// Parcelable methods
 
 	@SuppressWarnings("rawtypes")
 	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
         public CloudViewEntry createFromParcel(Parcel in) {
             return new CloudViewEntry(in);
         }
  
         public CloudViewEntry[] newArray(int size) {
             return new CloudViewEntry[size];
         }
     };
     
 	public int describeContents() {
 		return 0;
 	}
 
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeTypedList(columnValues);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void readFromParcel(Parcel in) {
         in.readTypedList(columnValues, ColumnValue.CREATOR);
     }
 	
 }
