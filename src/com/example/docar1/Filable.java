package com.example.docar1;

import java.util.List;

public abstract class Filable<T> {

	public abstract DataType onParse(Object... objs); //save according the order of request.because ohp return string
	public abstract String[] sendRequest();//request based on php
	public  abstract boolean onSave(List<?> obj);//did nothing
	protected DataType type=DataType.OTHERS;
	public DataType getType()
	{
		return type; 
	}
	protected Object lock=new Object();
	protected int done=0;
	public int getDownloadFlag()
	{
		return done;
	}
	public void setDownloadFlag(int flag)
	{
		synchronized(lock)
		{
			done=flag;
		}
	}
}
