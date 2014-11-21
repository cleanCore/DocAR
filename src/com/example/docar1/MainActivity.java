package com.example.docar1;

import java.util.ArrayList;
import java.util.List;


import com.mobeta.android.dslv.DragSortListView;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends ListActivity {

	 private DocAdapter adapter;

	    private ArrayList<DocItem> mDocs;
	    
	    private TypedArray imgs;
	    private ARDataFiles<ARList> dt;
	    private ARList ARdept;
	    private IDataHelper idh;
	  

	    private String[] mDocNames;
	    private String[] mDocDesces;

	    private DragSortListView.DropListener onDrop =
	        new DragSortListView.DropListener() {
	            @Override
	            public void drop(int from, int to) {
	            	DocItem item = adapter.getItem(from);

	                adapter.remove(item);
	                adapter.insert(item, to);
	            }
	        };

	    private DragSortListView.RemoveListener onRemove = 
	        new DragSortListView.RemoveListener() {
	            @Override
	            public void remove(int which) {
	                adapter.remove(adapter.getItem(which));
	            }
	        };

	    /** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        
	        StrictMode.ThreadPolicy policy = new StrictMode.
		    		ThreadPolicy.Builder().permitAll().build();
		    		StrictMode.setThreadPolicy(policy);
		    		
	        setContentView(R.layout.activity_main);

	        final DragSortListView lv = (DragSortListView) getListView(); 

	        lv.setDropListener(onDrop);
	        lv.setRemoveListener(onRemove);
	        lv.setOnItemClickListener(new OnItemClickListener() {  
	        	@Override  
	        	public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
	                //String selectedFromList =(String) (lv.getItemAtPosition(myItemInt));
	                Intent singleActivity = new Intent(getApplicationContext(), MainUIActivity.class);
	                //
	                ARList tempWordsARList=new ARList("getWords.php",false);
	                ARDataFiles<ARList> tempDt=new ARDataFiles<ARList>(tempWordsARList);
	    	        IDataHelper idhTemp=new DataHelper(tempDt);
	    	        idhTemp.sendTo(true);
	    	        
	    	        ParcelData wordNames=new ParcelData();
	    	        wordNames.setName(tempWordsARList.getName());	    	   
	    	        singleActivity.putExtra(ParcelData.PAR_KEY, wordNames);

	    	        //
		            //singleActivity.putExtra("id","qw");
					startActivity(singleActivity);
	        	}
	        });  

	        imgs= getResources().obtainTypedArray(R.array.select_imgs);
	        
	        
	        //call framework designed by maohua
	        ARdept=new ARList("getDept.php",true);
	        dt=new ARDataFiles<ARList>(ARdept);
	        idh=new DataHelper(dt);
	        idh.sendTo(true);
	        //

	        //
	        mDocNames = ARdept.getName();//getResources().getStringArray(R.array.jazz_artist_names);
	        mDocDesces =ARdept.getDisec();// getResources().getStringArray(R.array.jazz_artist_albums);

	        mDocs = new ArrayList<DocItem>();
	        DocItem ja;
	        for (int i = 0; i < mDocNames.length; ++i) {
	          ja = new DocItem();
	          ja.name = mDocNames[i];
	          if (i < mDocDesces.length) {
	            ja.desc = mDocDesces[i];
	          } else {
	            ja.desc = "No discription";
	          }
	          mDocs.add(ja);
	        }
	        //

	        adapter = new DocAdapter(mDocs);
	        setListAdapter(adapter);
	        
	        
	        //call framework designed by maohua
	        dt.add(adapter);
	        idh.sendTo(false);
	        //
	        
	        
	        

	    }
	    
	    @Override
	    public void onStop()
	    {
	    	super.onStop();
	    	imgs.recycle();
	    }

	    private class DocItem {
	      public String name;
	      public String desc;

	      @Override
	      public String toString() {
	        return name;
	      }
	    }

	    private class ViewHolder {
	      public TextView discriptView;
	    }

	    private class DocAdapter extends ArrayAdapter<DocItem> {
	      
	      public DocAdapter(List<DocItem> artists) {
	        super(MainActivity.this, R.layout.doc_select,
	          R.id.artist_name_textview, artists);
	      }

	      public View getView(int position, View convertView, ViewGroup parent) {
	        View v = super.getView(position, convertView, parent);

	        if (v != convertView && v != null) {
	          ViewHolder holder = new ViewHolder();

	          TextView tv = (TextView) v.findViewById(R.id.artist_albums_textview);
	          holder.discriptView = tv;

	          v.setTag(holder);
	        }

	        ViewHolder holder = (ViewHolder) v.getTag();
	        String desc = getItem(position).desc;

	        holder.discriptView.setText(desc);
	        
	        ImageView imgView=(ImageView)v.findViewById(R.id.doc_icon_identity);
		      //get resourceid by index
		  	  // or set you ImageView's resource to the id
	        int index=0;
	        if(imgs.length()>position)
	        	index=position;
	        imgView.setImageResource(imgs.getResourceId(index, -1));
	        
	        
	        /////////////////////
	        imgView.setImageBitmap(ARdept.getBm(position));
	        
	        /////////////////////////
	        return v;
	      }
	    }

	}