package com.example.docar1;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.util.Log;

public class ARMarker extends Filable<ARMarker> {

	private Bitmap bm;
	private String imageName=null;
	private String videoName=null;
	private String videoPath=null;
	private String conditional;
	private String text=null;
	private String textFilename=null;
	private String str="http://voyager.cs.bgsu.edu/zmaohua/";
	private String url;
	//private Hashmap<Datatype,list<?>> arContents; //to provide more ar contens,could use data pool created by maohua to do this 
	//private HashMap<String,List<HashMap<DataType,Object>>> dataPool=new HashMap<String,List<HashMap<DataType,Object>>>(); 
	//if someone want to refine this project,could use the datapool object
	
	@Override
	public DataType onParse(Object... objs) {
		// TODO Auto-generated method stub
		if(objs[0].getClass().equals(String.class))
		{
			try {			  
				   JSONArray jArray = new JSONArray((String)objs[0]);	   
				   for(int i=0; i<jArray.length();i++){				   
					   JSONObject json = jArray.getJSONObject(i);
					   imageName=json.getString("image_name");	
					   videoName=json.getString("video_name");
					   text=json.getString("text");
				   }
			     
				   
			} catch (Exception e) {
			// TODO: handle exception
			   Log.e("log_tag", "Error Parsing Data "+e.toString());
			}			
			
		} 	
		if(videoName=="null"&&imageName!="null")
			type=DataType.BITMAP;
		else if(videoName!="null"&&imageName=="null")
			type=DataType.FILES;
		else if(text!="null"&&imageName=="null")
			type=DataType.TEXT;
		else if(videoName!="null"&&imageName!="null")
			type=DataType.BITMAP;
		else
			type=DataType.OTHERS;	
		return type;
	}

	@Override
	public String[] sendRequest() {
		// TODO Auto-generated method stub
		//String str="";//should put it in subclass
		//String str1="http://voyager.cs.bgsu.edu/zmaohua";
		switch(type)
		{
			case OTHERS:
				return new String[]{str+url+"name="+conditional};
			case BITMAP:
				return new String[]{str+imageName};
			case FILES:
				return new String[]{str+videoName};
			case TEXT:
				return new String[]{str+text};
			default:
				break;
		}
		return null;		
	}
	
	ARMarker(String str,String urlStr)
	{
		conditional=str; //will be maker name in target file. xml
		url=urlStr;
	}

	public Bitmap getBm() {//could be null
		return bm;
	}

	public String getText() {//could be null
		return textFilename;

	}
	
	public String getVideo() {//could be null
		return videoPath;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean onSave(List<?> obj) {
		// TODO Auto-generated method stub
		switch(type)
		{
			case BITMAP:
			{
				ArrayList<Bitmap> bmObjs=(ArrayList<Bitmap>) obj;
				Log.e("ARMarker","bitmap");
				if(bmObjs!=null)
				{//need a lock
					bm=bmObjs.get(0);
					if(videoName!="null"&&videoPath==null){			
						type=DataType.FILES;
						return false;
					}else if(text!="null"&&textFilename==null){
						type=DataType.TEXT;
						return false;
					}else{	
						synchronized(lock)
						{
							done=2;
						}
						return true;
					}
				}
				break;
			}
			case FILES:
			{
				ArrayList<String> strObjs=(ArrayList<String>) obj;
				
				if(strObjs!=null){
					videoPath=strObjs.get(0);
					Log.e("ARMarker",videoPath);
					if(text!="null"&&textFilename==null){
						type=DataType.TEXT;
						return false;
					}else{
						synchronized(lock)
						{
							done=2;
						}
						return true;
					}
				}
				break;
			}
			
			case TEXT:
			{
				ArrayList<String> strObjs=(ArrayList<String>) obj;
				Log.e("ARMarker","TEXT");
				if(strObjs!=null){
					textFilename=strObjs.get(0);
					if(videoName!="null"&&videoPath==null){
						type=DataType.FILES;
						return false;
					}else{
						synchronized(lock)
						{
							done=2;
						}
						return true;
					}
				}
				break;
			}
			
			
			
		default:
			break;
		}
	
		return true;
	}

	
	//end clcass
	

}
