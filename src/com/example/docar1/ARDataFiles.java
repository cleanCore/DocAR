package com.example.docar1;

import java.util.ArrayList;
import android.graphics.Bitmap;

//should implements from ARMArker class, for time and other reasons, I postpone it even I have already designed it.
public class ARDataFiles<T extends Filable<T>> extends Data {
	
	private T file=null;        //could be list to support bundle of bitmaps files

	ARDataFiles(String[] urlStr)
	{
		super(urlStr);
	}
	
	ARDataFiles(T t)
	{
		file=t;
	}
	
	public DataType onParse(Object... objs)
	{
		return file.onParse(objs); //first time jason object from php file
		
	}
	
	public T getFile()
	{
		if(null!=file)
			return file;
		return null;
	}
	
	
	public String[] getUrl(DataType dt) //local file path or internet php file path
	{
		return file.sendRequest();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean save(Object obj) {
		//for bitmap or file download to use
		if (obj instanceof ArrayList<?>)
		{
			if(file.getType()==DataType.BITMAP)
			{
				ArrayList<Bitmap> bms=(ArrayList<Bitmap>)obj; //download and give files list
				return file.onSave(bms);
			}
			else if(file.getType()==DataType.FILES)
			{
				ArrayList<String> strs=(ArrayList<String>)obj;
				return file.onSave(strs);
			}
			else if(file.getType()==DataType.TEXT)
			{
				ArrayList<String> strs=(ArrayList<String>)obj;
				return file.onSave(strs);
			}
		}
		return true;
		//files 
		// TODO Auto-generated method stub
		
	}

	@Override
	public DataType getTpye() {
		// TODO Auto-generated method stub
		if(file!=null)return file.getType();
		return null;
	}

	@Override
	public void setDownloadFlag(int flag) {
		// TODO Auto-generated method stub
		if(null!=file)file.setDownloadFlag(flag);
		
	}
	

	

}
