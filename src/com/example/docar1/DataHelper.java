package com.example.docar1;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import org.apache.http.client.HttpClient;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.http.util.EntityUtils;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

//version 0.01 built by maohua

public class DataHelper implements IDataHelper {  //global object control all the activities? maybe in the future
	
	public final static String STOREDIR="/data/data/com.example.docar1/files/";
	private Data m_data;
	private String result;
	
	DataHelper(Data d) {
		this.m_data=d;		
	}
	
	private String setConnect()
	{
		String result = "";		 
		try{
			
			HttpClient httpclient = new DefaultHttpClient();
	        HttpPost httppost = new HttpPost(m_data.getUrl(DataType.OTHERS)[0]); 
	       // ArrayList<NameValuePair> param = new ArrayList<NameValuePair>(1);	        
	       // param.add(new BasicNameValuePair("selected_game", "Computer and Technology"));//modify later
            //httppost.setEntity(new UrlEncodedFormEntity(param));
	       // httppost.setEntity(new UrlEncodedFormEntity(m_data.getParams());
	        HttpResponse response = httpclient.execute(httppost);
	        HttpEntity entity = response.getEntity();
	        
	         result =  EntityUtils.toString(entity);	       
	      }		
		
		catch(Exception e){
	        Log.e("log_tag", "Error  converting result "+e.toString());
	        return null;
		}		
		return result;
	}
	
	public void sendTo(boolean bFirst){
		if(bFirst==true)
		{
			result=setConnect();
			if(null==result)return;
			m_data.onParse(result);
		}else{
			switch(m_data.getTpye())
			{
				case BITMAP:
					Task<ArrayList<Bitmap>,Bitmap> imgTask = new Task<ArrayList<Bitmap>,Bitmap>(Bitmap.class);
					m_data.setDownloadFlag(1);
				    imgTask.execute(m_data.getUrl(DataType.BITMAP));				    
					break;
				case FILES:
					Task<ArrayList<String>,String> fileTask = new Task<ArrayList<String>,String>(String.class);
					m_data.setDownloadFlag(1);
					fileTask.execute(m_data.getUrl(DataType.FILES));				
					break;
				case TEXT:
					Task<ArrayList<String>,String> textTask = new Task<ArrayList<String>,String>(String.class);
					m_data.setDownloadFlag(1);
					textTask.execute(m_data.getUrl(DataType.TEXT));				
					break;
				default:
					break;
			}
		}
		
	};
	

	class Task<T,Type> extends AsyncTask<String[],Void,T>{
		
		private final Class<Type> type;
		public Task(Class<Type> type) {
	          this.type = type;
	     }
	     private Class<Type> getMyType() {
	         return this.type;
	     }

	     
	     
	     
		@SuppressWarnings("unchecked")
		@Override
		protected T doInBackground(String[]... arg0) {
			// TODO Auto-generated method stub
			Type bm = null ;
			ArrayList<Type> bmList=new ArrayList<Type>();
			try { 				
				for(String url:arg0[0])
				{
					HttpClient hc = new DefaultHttpClient(); 
					//String url=URLEncoder.encode(arg0[0],"UTF-8");  
					HttpGet hg = new HttpGet(url); 
					HttpResponse hr = hc.execute(hg);  
					if(Bitmap.class.equals(getMyType()))
					{
						//@SuppressWarnings("unchecked")
						bm = (Type)(BitmapFactory.decodeStream(hr.getEntity().getContent())); 
						bmList.add(bm);
					}
					else if(String.class.equals(getMyType()))
					{
						////////////////////downlaod files
						Log.e("log_tag", url);
						int slashIndex=url.lastIndexOf('/');
						String name=url.substring(slashIndex+1,url.length());
						downLoadFile(name,hr);
						Type viodeFile=(Type)(name);
						bmList.add(viodeFile);
					}
				}
			}catch (Exception e) { 
			   Log.e("log_tag", "task "+e.toString()); 
               return null;  
			}   
			return (T)bmList;  
		}
		
		@Override
		protected void onPostExecute(T result) {
			try{
				super.onPostExecute(result);
				if(m_data.save(result))
					m_data.onNotify();
				else
					DataHelper.this.sendTo(false);
			}catch(Exception e)
			{
				Log.e("log_tag", "post task "+e.toString()); 
			}
			
		}
		
		@SuppressWarnings("unused")
		private void downLoadFile(String name,HttpResponse response ){
		   try{	
			   File file = new File(DataHelper.STOREDIR,name);   
			   FileOutputStream outStream = new FileOutputStream(file);
			    HttpEntity entity = response.getEntity();					    			
			    InputStream is = entity.getContent();		
			    int count = 0;	
			    long length = entity.getContentLength();
			    if (is != null) {			
			        byte[] buf = new byte[1024];			
			        int ch = -1;
			        while ((ch = is.read(buf)) >0) {	
		                outStream.write(buf,0,ch);		
		                count += ch;
		
			        }
			    }

		        outStream.flush();
		
		        if (outStream != null) {
		
		            outStream.close();
		
		        }

		        if (is != null) {
		
		            is.close();
		
		        }}catch (IOException e) {

		        	e.printStackTrace();
		        }

		}
		
	//end class	
	}

   



	@Override
	public void receiveFrom(boolean b) {
		// TODO Auto-generated method stub
		
	}
	
}


