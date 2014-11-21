package com.example.docar1;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;

//import com.astuetz.PagerSlidingTabStrip;


public class MainUIActivity extends FragmentActivity {

	
	private ResultFragment resultFragment;
	private ResultFragment testFragment;
	private ResultFragment test1Fragment;
	private PagerSlidingTabStrip tabs;
	private DisplayMetrics dm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ui_main);
		setOverflowShowingAlways();
		dm = getResources().getDisplayMetrics();
		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
		tabs.setViewPager(pager);
		setTabsValue();
	}

	private void setTabsValue() {
		
		tabs.setShouldExpand(true);
		
		tabs.setDividerColor(Color.TRANSPARENT);
		
		tabs.setUnderlineHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 1, dm));
		
		tabs.setIndicatorHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 4, dm));
		
		tabs.setTextSize((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, 16, dm));
		
		tabs.setIndicatorColor(Color.parseColor("#45c01a"));
		
		tabs.setSelectedTextColor(Color.parseColor("#45c01a"));
		
		tabs.setTabBackground(0);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {  
	
		     switch (item.getItemId()) {  
		       case R.id.action_scan:    
		            //Intent intent = new Intent(this, ARMainActivity.class);
		    	   ParcelData wordNames = (ParcelData)getIntent().getParcelableExtra(ParcelData.PAR_KEY); 
		    	   Log.e("word names ",wordNames.getName()[0]);
		    	   Intent intent = new Intent(this, TextReco.class);
		    	   intent.putExtra(ParcelData.PAR_KEY,wordNames);
		            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP  
		              //      | Intent.FLAG_ACTIVITY_NEW_TASK);  
		            startActivity(intent); 
		            break;  
		       default:  
		           break;  
		        }  
		        return super.onOptionsItemSelected(item);  
		   }  

	
	public class MyPagerAdapter extends FragmentPagerAdapter {

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		private final String[] titles = { "Result", "test", "test1" };

		@Override
		public CharSequence getPageTitle(int position) {
			return titles[position];
		}

		@Override
		public int getCount() {
			return titles.length;
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				if (resultFragment == null) {
					resultFragment = new ResultFragment();
				}
				return resultFragment;
			case 1:
				if (testFragment == null) {
					testFragment = new ResultFragment();
				}
				return testFragment;
			case 2:
				if (test1Fragment == null) {
					test1Fragment = new ResultFragment();
				}
				return test1Fragment;
			default:
				return null;
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
			if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
				try {
					Method m = menu.getClass().getDeclaredMethod(
							"setOptionalIconsVisible", Boolean.TYPE);
					m.setAccessible(true);
					m.invoke(menu, true);
				} catch (Exception e) {
				}
			}
		}
		return super.onMenuOpened(featureId, menu);
	}

	private void setOverflowShowingAlways() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			menuKeyField.setAccessible(true);
			menuKeyField.setBoolean(config, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}