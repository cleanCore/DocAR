package com.example.docar1;

import android.os.Parcel;
import android.os.Parcelable;
public class ParcelData implements Parcelable {
	public static final String PAR_KEY="https://voyager.cs.bgsu.edu/zmaohua/";//do not mean anything
	private String[] name;
	
	public String[] getName() {
		return name;
	}
	
	public void setName(String[] str) {
		name=str;//new String[]{"qeqew"};//str;
	}
	
	public ParcelData()
	{
		
	}
	
	private ParcelData(Parcel in) {
		name=in.createStringArray();
    }
	
	public static final Parcelable.Creator<ParcelData> CREATOR = new Creator<ParcelData>() {
		public ParcelData createFromParcel(Parcel in) {
			return new ParcelData(in);
		}
		
		public ParcelData[] newArray(int size) {
			return new ParcelData[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeStringArray(name);
	}
}


