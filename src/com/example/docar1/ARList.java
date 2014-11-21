package com.example.docar1;



import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.util.Log;


//should be combined with the ARMarker class.do it later
public class ARList extends Filable<ARList> {

	private String[] name;
	private String[] disec;
	private ArrayList<Bitmap> bm;
	private int[] bmIndex;
	private String[] imageName;
	private String url;//php file
	private boolean bDisplayAR=false;
	@Override
	public DataType onParse(Object... objs) {
		// TODO Auto-generated method stub
		if(objs[0].getClass().equals(String.class))
		{
			try {				  
				   JSONArray jArray = new JSONArray((String)objs[0]);
				   name=new String[jArray.length()];
				   disec=new String[jArray.length()];
				   imageName=new String[jArray.length()];
				   bmIndex=new int [jArray.length()];
				   for(int i=0; i<jArray.length();i++){				   
					   JSONObject json = jArray.getJSONObject(i);
					  name[i]=json.getString("name");	
					  if(json.isNull("disec")==false)
						  disec[i]=json.getString("disec");
					  if(json.isNull("image_name")==false)
						  imageName[i]=json.getString("image_name");
					  else imageName[i]="null";
				   }
			     
				   
			} catch (Exception e) {
			// TODO: handle exception
			   Log.e("log_tag", "Error Parsing Data "+e.toString());
			}			
			if(imageName!=null&&bDisplayAR==true)
				type=DataType.BITMAP;
		} 
		return type;
	}

	@Override
	public String[] sendRequest() {
		// TODO Auto-generated method stub
		String str="http://voyager.cs.bgsu.edu/zmaohua/";
		if(type==DataType.BITMAP)
		{	
			String[] urls=new String[getLenght(imageName)];//should determine if it is >0
			int index=0;
			int validIndex=0;
			for(int i=0;i<imageName.length;i++){
				if(imageName[i]!="null"){
					urls[validIndex]=str+imageName[i];
					bmIndex[index++]=validIndex;
					validIndex++;				
				}else{
					bmIndex[index++]=-1;
				}
			}
			return urls;
			
		}
		else if(type==DataType.OTHERS){			
				return new String[]{str+url};}
		
		else
			return null;
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean onSave(List<?> obj) {
		// TODO Auto-generated method stub
		ArrayList<Bitmap> bmObjs=(ArrayList<Bitmap>) obj;
		if(bmObjs!=null)
		{
			bm=bmObjs;
			synchronized(lock)
			{
				done=2;
			}
		}
		return true;
	}

	
	ARList(String str,boolean flag)
	{
		url=str; //will be the organID
		bDisplayAR=flag;
	}

	public Bitmap getBm(int index) {
		if(bm==null)return null;
		if(bmIndex[index]>=0)return bm.get(bmIndex[index]);
		else
			return null;
	}

	public String[] getName()
	{
		return name;
	}
	
	public String[] getDisec()
	{
		return disec;
	}
	
	private int getLenght(String[] strs)
	{
		int i=0;
		for(String str:strs)
		{
			if(str!="null")i++;
		}
		return i;
	}

}
