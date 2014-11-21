package com.example.docar1;

import java.util.ArrayList;
import java.util.List;

import android.widget.BaseAdapter;
//version 0.01 built by maohua

public abstract class Data
{
	public abstract DataType onParse(Object... objs);	
	public abstract String[] getUrl(DataType dt);//could be url or local path string  using list<?> to support multiple strings and bitmaps
	public abstract boolean save(Object obj);
	public abstract DataType getTpye();
	public abstract void setDownloadFlag(int flag);
	private String[] url;    //for the php url to get text info or return video or image path
	protected Data(String[] urlStr)
	{
		this();
		url=urlStr;
	}
	
	public String [] getUrls()
	{
		return url;
	}
	public void onNotify()
	{
		if(m_adapter.size()<=0)return;
		for(BaseAdapter adp:m_adapter)
		{
			adp.notifyDataSetChanged();
		}
		
	}
	
	private List<BaseAdapter> m_adapter;
	Data(){
		m_adapter=new ArrayList<BaseAdapter>();
	}
	public void add(BaseAdapter adp)
	{
		if(null!=m_adapter&&adp!=null)
			m_adapter.add(adp);
	}
	public void remove(BaseAdapter adp)
	{
		if(null!=m_adapter&&adp!=null)
			m_adapter.remove(adp);
	}
} 


