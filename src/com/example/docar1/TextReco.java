/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.example.docar1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;

import android.view.ViewGroup.LayoutParams;
import android.webkit.URLUtil;
import android.widget.CheckBox;

import android.widget.ImageView;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import android.widget.VideoView;

import com.example.docar1.R;


import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.RectangleInt;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.STORAGE_TYPE;
import com.qualcomm.vuforia.TextTracker;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.VideoMode;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.WordList;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationControl;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationException;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.qualcomm.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleApplicationGLView;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleUtils;


public class TextReco extends Activity implements SampleApplicationControl
{
    private static final String LOGTAG = "TextReco";
    
    SampleApplicationSession vuforiaAppSession;
    
    private final static int COLOR_OPAUE=Color.argb(200, 0, 0, 0);
    
    // Our OpenGL view:
    private SampleApplicationGLView mGlView;
    
    // Our renderer:
    private TextRecoRenderer mRenderer;
    
    ////zmh
    final private boolean isImageTarget=true;
    private ImageTargets imageTarget;
    private boolean bNewMovie=false;
    private VideoView videoView=null;
    private TextView textDisplay;
    private ImageView imageDisplay;
    private Button mPlay;
    private ZoomImageView zoomImageView;
    private ImageButton ARCloseButton;
    private ImageButton videoIcon;
    private TextView ARTextView;
    //zmh
    
    private RelativeLayout mUILayout;
    private RelativeLayout mARUILayout;
    
    private LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(
        this);
    private boolean mIsTablet = false;
    
    private boolean mIsVuforiaStarted = false;
    
    
    private boolean mFlash = false;
    
    private View mFlashOptionView;
    
    boolean mIsDroidDevice = false;
    
    //video
    private MediaPlayer mMediaPlayer = null;
    //video
    
    
    // Called when the activity first starts or the user navigates back to an
    // activity.
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);
       
        
        vuforiaAppSession = new SampleApplicationSession(this);
        if(isImageTarget==true)
        {
        	imageTarget=new ImageTargets(this);
        }
        //video
        mMediaPlayer = new MediaPlayer();
        //updateUIVideoPlayer(mMediaPlayer);
        //video
        
        startLoadingAnimation();
        
        if(isImageTarget==true)
        	imageTarget.onCreate(savedInstanceState);
        
        vuforiaAppSession
            .initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        
        
        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith(
            "droid");
    }
    
  
    
    // Called when the activity will start interacting with the user.
    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume");
        super.onResume();
        
        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        
        try
        {
            vuforiaAppSession.resumeAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
        
        if (mIsVuforiaStarted)
            postStartCamera();
        
        // Resume the GL view:
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
        
    }
    
    
    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config)
    {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);
        
        vuforiaAppSession.onConfigurationChanged();
        
        if(mIsVuforiaStarted)
            configureVideoBackgroundROI();
    }
    
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // Process the Gestures
        return true;
    }
    
    
    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause()
    {
        Log.d(LOGTAG, "onPause");
        super.onPause();
        
        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }
        
        // Turn off the flash
        if (mFlashOptionView != null && mFlash)
        {
            // OnCheckedChangeListener is called upon changing the checked state
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                ((Switch) mFlashOptionView).setChecked(false);
            } else
            {
                ((CheckBox) mFlashOptionView).setChecked(false);
            }
        }
        
        try
        {
            vuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
        
        stopCamera();
    }
    
    
    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy()
    {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();
        if(isImageTarget==true)
        	imageTarget.onDestroy();
        try
        {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
        
        System.gc();
    }
    
    
    private void startLoadingAnimation()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(
            R.layout.camera_overlay_textreco, null, false);
        
        mARUILayout=(RelativeLayout) inflater.inflate(
            R.layout.ar_content_view, null, false);
        
        
        mUILayout.setVisibility(View.VISIBLE);
        mARUILayout.setVisibility(View.VISIBLE);
        
        mUILayout.setBackgroundColor(Color.BLACK);
        
        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
            .findViewById(R.id.loading_indicator);
        
        // Shows the loading indicator at start
        loadingDialogHandler
            .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
        
        // Adds the inflated layout to the view
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT));
        
        addContentView(mARUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        
    }
    
    
    // Initializes AR application components.
    private void initApplicationAR()
    {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();
        
        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);
        
        
        ParcelData wordNames = (ParcelData)getIntent().getParcelableExtra(ParcelData.PAR_KEY);      
        mRenderer = new TextRecoRenderer(this, vuforiaAppSession,mMediaPlayer,wordNames.getName());
        mGlView.setRenderer(mRenderer);       
        if(isImageTarget==true)
        	imageTarget.setTexture(mRenderer);
        
      
        	
        
        showLoupe(false);
        
    }
    
    
    private void postStartCamera()
    {
        // Sets the layout background to transparent
        mUILayout.setBackgroundColor(Color.TRANSPARENT);
        mARUILayout.setBackgroundColor(Color.TRANSPARENT);
        
        // start the image tracker now that the camera is started
        Tracker t = TrackerManager.getInstance().getTracker(
            TextTracker.getClassType());
        if (t != null)
            t.start();
        
        configureVideoBackgroundROI();
    }
    
    
    void configureVideoBackgroundROI()
    {
        VideoMode vm = CameraDevice.getInstance().getVideoMode(
            CameraDevice.MODE.MODE_DEFAULT);
        VideoBackgroundConfig config = Renderer.getInstance()
            .getVideoBackgroundConfig();
        
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        
        {
            // calc ROI
            // width of margin is :
            // 5% of the width of the screen for a phone
            // 20% of the width of the screen for a tablet
            int marginWidth = mIsTablet ? (screenWidth * 20) / 100
                : (screenWidth * 5) / 100;
            
            // loupe height is :
            // 15% of the screen height for a phone
            // 10% of the screen height for a tablet
            int loupeHeight = mIsTablet ? (screenHeight * 10) / 100
                : (screenHeight * 15) / 100;
            
            // lupue width takes the width of the screen minus 2 margins
            int loupeWidth = screenWidth - (2 * marginWidth);
            
            // definition of the region of interest
            mRenderer.setROI(screenWidth / 2, marginWidth + (loupeHeight / 2),
                loupeWidth, loupeHeight);
        }
        
        // convert into camera coords
        int[] loupeCenterX = { 0 };
        int[] loupeCenterY = { 0 };
        int[] loupeWidth = { 0 };
        int[] loupeHeight = { 0 };
        SampleUtils.screenCoordToCameraCoord((int) mRenderer.getTextRender().ROICenterX,
            (int) mRenderer.getTextRender().ROICenterY, (int) mRenderer.getTextRender().ROIWidth,
            (int) mRenderer.getTextRender().ROIHeight, screenWidth, screenHeight,
            vm.getWidth(), vm.getHeight(), loupeCenterX, loupeCenterY,
            loupeWidth, loupeHeight);
        
       /* RectangleInt detROI = new RectangleInt(loupeCenterX[0]
            - (loupeWidth[0] / 2), loupeCenterY[0] - (loupeHeight[0] / 2),
            loupeCenterX[0] + (loupeWidth[0] / 2), loupeCenterY[0]
                + (loupeHeight[0] / 2));*/
        
        RectangleInt detROI = new RectangleInt(0, 0, screenWidth,screenHeight);
        
        TextTracker tt = (TextTracker) TrackerManager.getInstance().getTracker(
            TextTracker.getClassType());
        if (tt != null)
            tt.setRegionOfInterest(detROI, detROI,
                TextTracker.UP_DIRECTION.REGIONOFINTEREST_UP_IS_9_HRS);
        
        int[] size = config.getSize().getData();
        int[] pos = config.getPosition().getData();
        int offx = ((screenWidth - size[0]) / 2) + pos[0];
        int offy = ((screenHeight - size[1]) / 2) + pos[1];
        mRenderer.setViewport(offx, offy, size[0], size[1]);
    }
    
    
    private void stopCamera()
    {
        doStopTrackers();
        
        CameraDevice.getInstance().stop();
        CameraDevice.getInstance().deinit();
    }
    
    /*
    void updateWordListUI1(final List<WordDesc> words)
    {
        runOnUiThread(new Runnable()
        {
            
            public void run()
            {
                RelativeLayout wordListLayout = (RelativeLayout) mUILayout
                    .findViewById(R.id.wordList);
                wordListLayout.removeAllViews();
                
                if (words.size() > 0)
                {
                    LayoutParams params = wordListLayout.getLayoutParams();
                    // Changes the height and width to the specified *pixels*
                    int maxTextHeight = params.height - (2 * WORDLIST_MARGIN);
                    
                    int[] textInfo = fontSizeForTextHeight(maxTextHeight,
                        words.size(), params.width, 32, 12);
                    
                    int count = -1;
                    int nbWords = textInfo[2]; // number of words we can display
                    TextView previousView = null;
                    TextView tv;
                    for (WordDesc word : words)
                    {
                        count++;
                        if (count == nbWords)
                        {
                            break;
                        }
                        tv = new TextView(TextReco.this);
                        tv.setText(word.text);
                        RelativeLayout.LayoutParams txtParams = new RelativeLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT);
                        
                        if (previousView != null)
                            txtParams.addRule(RelativeLayout.BELOW,
                                previousView.getId());
                        
                        txtParams.setMargins(0, (count == 0) ? WORDLIST_MARGIN
                            : 0, 0, (count == (nbWords - 1)) ? WORDLIST_MARGIN
                            : 0);
                        tv.setLayoutParams(txtParams);
                        tv.setGravity(Gravity.CENTER_VERTICAL
                            | Gravity.CENTER_HORIZONTAL);
                        tv.setTextSize(textInfo[0]);
                        tv.setTextColor(Color.WHITE);
                        tv.setHeight(textInfo[1]);
                        tv.setId(count + 100);
                        
                        wordListLayout.addView(tv);
                        previousView = tv;
                    }
                }
            }
        });
    }
    */
    
    
    void updateUIVideoPlayer(final MediaPlayer play)
    {
	
    	//mediaplayer
		
		try {
			final String path =
					 "http://daily3gp.com/vids/747.3gp";
					//AssetsManager.getAssetPath(getApplicationContext(), "test/demo_movie.3g2");
			play.setDataSource( path);
		} catch (Exception e) {
			Log.e("textReco", e.getMessage(), e);
		}
		
		//mediaplayer
		
			/*
			RelativeLayout wordListLayout = (RelativeLayout) mUILayout
                    .findViewById(R.id.wordList);
			if(bNewMovie)
			{
				wordListLayout.removeAllViews();
				VideoView mVideoView=new VideoView(mGlView.getContext());             
				//ImageView mVideoView=new ImageView(TextReco.this);
				RelativeLayout.LayoutParams videoParams = new RelativeLayout.LayoutParams(
                    200,//LayoutParams.MATCH_PARENT,
                    200//LayoutParams.WRAP_CONTENT
                    );
				mVideoView.setLayoutParams(videoParams);
				bNewMovie=false;
				MediaController mediacontroller = new MediaController(mGlView.getContext());
		        mediacontroller.setAnchorView(mVideoView);

		        //Set controllers to video
		        mVideoView.setMediaController(mediacontroller);
		        
		        
				//mVideoView.setBackgroundResource(R.drawable.ic_launcher);
				wordListLayout.addView(mVideoView);
				playVideo(mVideoView);
				
			}
			*/
           /* ImageButton mPlay=new ImageButton(TextReco.this);
        	ImageButton mPause=new ImageButton(TextReco.this);
        	ImageButton mReset=new ImageButton(TextReco.this);
        	ImageButton mStop=new ImageButton(TextReco.this);
        	mPlay.setOnClickListener(new OnClickListener() {
    			public void onClick(View view) {
    				playVideo();
    			}
    		});
    		mPause.setOnClickListener(new OnClickListener() {
    			public void onClick(View view) {
    				if (mVideoView != null) {
    					mVideoView.pause();
    				}
    			}
    		});
    		mReset.setOnClickListener(new OnClickListener() {
    			public void onClick(View view) {
    				if (mVideoView != null) {
    					mVideoView.seekTo(0);
    				}
    			}
    		});
    		mStop.setOnClickListener(new OnClickListener() {
    			public void onClick(View view) {
    				if (mVideoView != null) {
    					current = null;
    					mVideoView.stopPlayback();
    				}
    			}
    		});*/        
    }
    
    void updateUIImage(final Bitmap bitmap)
    {
    	runOnUiThread(new Runnable()
    	{
    		public void run(){
    			imageDisplay.setImageBitmap(bitmap);
    			//AR image
    			imageDisplay.setOnClickListener(new OnClickListener() {  
    				@Override  
    				public void onClick(View v) {
    									
    					zoomImageView.setImageBitmap(bitmap);
    					mARUILayout.setBackgroundColor(Color.BLACK);
    					zoomImageView.setVisibility(View.VISIBLE);
    					ARCloseButton.setVisibility(View.VISIBLE);
    					ARCloseButton.setOnClickListener(new OnClickListener() {  
    	    				@Override  
    	    				public void onClick(View v){
    	    					zoomImageView.setVisibility(View.GONE);
    	    					mARUILayout.setBackgroundColor(Color.TRANSPARENT);
    	    					ARCloseButton.setVisibility(View.GONE);
    	    				}
    					});
    				}  
    		    }); 

    			
    			//AR image
    		}
    		
    	});
    	
    }
    
    void updateUIVideoPlayer(final String path)
    {
	
    	runOnUiThread(new Runnable()
    	{
    		public void run(){
    			if(bNewMovie==false){
    				//mUILayout.setVisibility(View.GONE);
    	    		//mGlView.setVisibility(View.GONE);
    	    		/*VideoView mVideoView=new VideoView(TextReco.this); 
    	    		RelativeLayout.LayoutParams videoParams = new RelativeLayout.LayoutParams(
    	                    200,//LayoutParams.MATCH_PARENT,
    	                    200//LayoutParams.WRAP_CONTENT
    	                    );
    	    		TextReco.this.addContentView(mVideoView, videoParams);*/
    				//Log.e("video play",path);
    				
    				videoIcon.setOnClickListener(new OnClickListener() {  
        				@Override  
        				public void onClick(View v) {
        					bNewMovie=true;
        					//mARUILayout.bringChildToFront(videoView);
        					//mARUILayout.setBackgroundColor(Color.BLACK);
        					//SurfaceHolder sfhTrackHolder = videoView.getHolder();
	    		            //sfhTrackHolder.setFormat(PixelFormat.OPAQUE);
        					mUILayout.setVisibility(View.GONE);
        					videoView.setVisibility(View.VISIBLE);
        					ARCloseButton.setVisibility(View.VISIBLE);
        					if(videoView.isPlaying())return;
	        				Uri uri=Uri.parse(DataHelper.STOREDIR+path);
	        				Log.e("video play",path);
	        		        videoView.setVideoURI(uri);
	        		        MediaController mediacontroller = new MediaController(videoView.getContext());
	        		        mediacontroller.setAnchorView(videoView);;
	        		        //Set controllers to video
	        		        videoView.setMediaController(mediacontroller);
	        				//videoView.setVideoPath(path);
	        	    		playVideo(videoView);
        					ARCloseButton.setOnClickListener(new OnClickListener() {  
        	    				@Override  
        	    				public void onClick(View v){
	    	    					videoView.stopPlayback();
	    	    					bNewMovie=false;
	    	    					SurfaceHolder sfhTrackHolder = videoView.getHolder();
	    	    		            sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
	    	    					videoView.setVisibility(View.GONE);
        	    					mARUILayout.setBackgroundColor(Color.TRANSPARENT);
        	    					ARCloseButton.setVisibility(View.GONE);
        	    					mUILayout.setVisibility(View.VISIBLE);
        	    	    		}
        	    			});
        				}  
        		    }); 
    				
    	    		
    			}
    		}
    	});
    }
    		
    		  
    
    private void playVideo(VideoView v) {
		try {
			 
					// "http://daily3gp.com/vids/747.3gp";
					//AssetsManager.getAssetPath(getApplicationContext(), "test/demo_movie.3g2");
			Log.v("videoPlay", "path: " + "");
			/*if (path == null || path.length() == 0) {
				//Toast.makeText(TextReco.this, "File URL/path is empty",
				//		Toast.LENGTH_LONG).show();

			} else {
				// If the path has not changed, just start the media player
				/*if (path.equals(current) && v != null) {
					v.start();
					v.requestFocus();
					return;
				}
				current = path;*/
				//getDataSource(path));
				v.seekTo(1);
				v.start();
				v.requestFocus();

			
		} catch (Exception e) {
			Log.e("vieodPlayer", "error: " + e.getMessage(), e);
			if (v != null) {
				v.stopPlayback();
			}
		}
	}

	private String getDataSource(String path) throws IOException {
		if (!URLUtil.isNetworkUrl(path)) {
			return path;
		} else {
			URL url = new URL(path);
			URLConnection cn = url.openConnection();
			cn.connect();
			InputStream stream = cn.getInputStream();
			if (stream == null)
				throw new RuntimeException("stream is null");
			File temp = File.createTempFile("mediaplayertmp", "dat");
			temp.deleteOnExit();
			String tempPath = temp.getAbsolutePath();
			FileOutputStream out = new FileOutputStream(temp);
			byte buf[] = new byte[128];
			do {
				int numread = stream.read(buf);
				if (numread <= 0)
					break;
				out.write(buf, 0, numread);
			} while (true);
			try {
				stream.close();
				out.close();
			} catch (IOException ex) {
				Log.e("viedoPlayer", "error: " + ex.getMessage(), ex);
			}
			return tempPath;
		}
	}

	void setARVisable(final boolean flag){
		runOnUiThread(new Runnable()
        {
            
            public void run()
            {
            	if(flag){
	            	videoIcon.setVisibility(View.VISIBLE);
	            	textDisplay.setVisibility(View.VISIBLE);
	                imageDisplay.setVisibility(View.VISIBLE);
	            	//mPlay.setVisibility(View.VISIBLE);
            	}else{
            		videoIcon.setVisibility(View.GONE);
	            	textDisplay.setVisibility(View.GONE);
	                imageDisplay.setVisibility(View.GONE);
	            	//mPlay.setVisibility(View.GONE);
	                }            		
            }
        });
	}
    
	void setUILayoutCenter(final int x,final int y)
	{
		runOnUiThread(new Runnable()
        {
            
            public void run()
            {
	
				RelativeLayout wordListLayout = (RelativeLayout) mUILayout.findViewById(R.id.wordList);
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)wordListLayout.getLayoutParams();
				int width=360;//params.width;
				int height=100;//params.height;
				Log.d("width height",Integer.toString(width)+" "+Integer.toString(height));
				params.leftMargin=x-3*width/2;
				if(y<0)
					params.topMargin=height/2-y;
				else
					params.topMargin=y-height/2;
				wordListLayout.setLayoutParams(params);
            }
        });
	}
	
    void updateWordListUI(final String text)//List<WordDesc> words)
    {
        runOnUiThread(new Runnable()
        {
            
            public void run()
            {
            	Log.d("text", text);
            	int slashIndex=text.lastIndexOf('.');
				String fileExtension=text.substring(slashIndex+1,text.length());
            	textDisplay.setText(fileExtension);
            	
            	textDisplay.setOnTouchListener(new View.OnTouchListener(){
            		@Override
            		public boolean onTouch(View arg0, MotionEvent arg1) {
            			mARUILayout.setBackgroundColor(COLOR_OPAUE);
            			ARTextView.setVisibility(View.VISIBLE);
    					ARCloseButton.setVisibility(View.VISIBLE);
    					mUILayout.setVisibility(View.GONE);
    					
                        // TODO Auto-generated method stub
            			File myFile=new File(DataHelper.STOREDIR+text);
            		    try {
            		        StringBuilder contentText = new StringBuilder();
            		        BufferedReader br = new BufferedReader(new FileReader(myFile));
            		        String line1;
            		        while(null!=(line1=br.readLine()))
            		        {
            		        	contentText.append(line1);
            		        	contentText.append("\n");
            		        }
            		        ARTextView.setText(contentText.toString());
            		        ARTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

            		    } catch (FileNotFoundException e1) {
            		        // TODO Auto-generated catch block
            		        e1.printStackTrace();
            		    } catch (IOException e) {
            		        // TODO Auto-generated catch block
            		        e.printStackTrace();
            		    }   
            			/*
                    	try {
        					DefaultIntent.openFile(TextReco.this, myFile);
        				} catch (IOException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}*/
            		    ARCloseButton.setOnClickListener(new OnClickListener() {  
    	    				@Override  
    	    				public void onClick(View v){
    	    					    					
    	    					ARTextView.setVisibility(View.GONE);
    	    					mARUILayout.setBackgroundColor(Color.TRANSPARENT);
    	    					ARCloseButton.setVisibility(View.GONE);
    	    					mUILayout.setVisibility(View.VISIBLE);
    	    	    		}
    	    			});
                        return false;
                    }

            	});	
            		
            	
               // RelativeLayout wordListLayout = (RelativeLayout) mUILayout
                    //.findViewById(R.id.wordList);
               // wordListLayout.removeAllViews();
                
                /*if (words.size() > 0)
                {
                   /* LayoutParams params = wordListLayout.getLayoutParams();
                    // Changes the height and width to the specified *pixels
                    int maxTextHeight = params.height - (2 * WORDLIST_MARGIN);
                    
                    int[] textInfo = fontSizeForTextHeight(maxTextHeight,
                        words.size(), params.width, 32, 12);
                    */
                   // int count = -1;
                   // int nbWords = textInfo[2]; // number of words we can display
                    
                    /*String str="";
                    for (WordDesc word : words)
                    {
                        count++;
                        /*if (count == nbWords)
                        {
                            break;
                        }*/
                      //  str=str+" "+word.text;
                   // }
                    
                   
                    //TextView tv;
                   // tv = new TextView(TextReco.this);
                   // tv.setText(str);
                    /*
                    RelativeLayout.LayoutParams txtParams = new RelativeLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT);
                    
                    txtParams.setMargins(0, (count == 0) ? WORDLIST_MARGIN
                        : 0, 0, (count == (nbWords - 1)) ? WORDLIST_MARGIN
                        : 0);
                    tv.setLayoutParams(txtParams);
                    tv.setGravity(Gravity.CENTER_VERTICAL
                        | Gravity.CENTER_HORIZONTAL);
                    tv.setTextSize(textInfo[0]);
                    tv.setTextColor(Color.RED);
                    tv.setHeight(textInfo[1]);
                    tv.setId(count + 100);
                    
                    wordListLayout.addView(tv);*/
                    //bNewMovie=true;
                    
                //}else{
                //	textDisplay.setText(" ");
                //}
            }
        });
    }
    
    
    private void showLoupe(boolean isActive)
    {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        
        // width of margin is :
        // 5% of the width of the screen for a phone
        // 20% of the width of the screen for a tablet
        int marginWidth = mIsTablet ? (width * 20) / 100 : (width * 5) / 100;
        
        // loupe height is :
        // 33% of the screen height for a phone
        // 20% of the screen height for a tablet
        int loupeHeight = mIsTablet ? (height * 10) / 100 : (height * 15) / 100;
        
        // lupue width takes the width of the screen minus 2 margins
        int loupeWidth = width - (2 * marginWidth);
        
        int wordListHeight = height - (loupeHeight + marginWidth);
        
        // definition of the region of interest
        mRenderer.setROI(width / 2, marginWidth + (loupeHeight / 2),
            loupeWidth, loupeHeight);
        
        // Gets a reference to the loading dialog
        View loadingIndicator = mUILayout.findViewById(R.id.loading_indicator);
        
        RelativeLayout loupeLayout = (RelativeLayout) mUILayout
            .findViewById(R.id.loupeLayout);
        
        ImageView topMargin = (ImageView) mUILayout
            .findViewById(R.id.topMargin);
        
        ImageView leftMargin = (ImageView) mUILayout
            .findViewById(R.id.leftMargin);
        
        ImageView rightMargin = (ImageView) mUILayout
            .findViewById(R.id.rightMargin);
        
        ImageView loupeArea = (ImageView) mUILayout.findViewById(R.id.loupe);
        
        RelativeLayout wordListLayout = (RelativeLayout) mUILayout
            .findViewById(R.id.wordList);
        
        wordListLayout.setBackgroundColor(Color.TRANSPARENT);//COLOR_OPAQUE);
        
        if (isActive)
        {
            topMargin.getLayoutParams().height = marginWidth;
            topMargin.getLayoutParams().width = width;
            
            leftMargin.getLayoutParams().width = marginWidth;
            leftMargin.getLayoutParams().height = loupeHeight;
            
            rightMargin.getLayoutParams().width = marginWidth;
            rightMargin.getLayoutParams().height = loupeHeight;
            
            RelativeLayout.LayoutParams params;
            
            params = (RelativeLayout.LayoutParams) loupeLayout
                .getLayoutParams();
            params.height = loupeHeight;
            loupeLayout.setLayoutParams(params);
            
            loupeArea.getLayoutParams().width = loupeWidth;
            loupeArea.getLayoutParams().height = loupeHeight;
            loupeArea.setVisibility(View.VISIBLE);
            
            /*
            params = (RelativeLayout.LayoutParams) wordListLayout
                .getLayoutParams();
            params.height = wordListHeight;
            params.width = width;
            wordListLayout.setLayoutParams(params);
            */
            
            loadingIndicator.setVisibility(View.GONE);
            loupeArea.setVisibility(View.VISIBLE);
            topMargin.setVisibility(View.VISIBLE);
            loupeLayout.setVisibility(View.VISIBLE);
           // wordListLayout.setVisibility(View.VISIBLE);
            
        } else
        {
            loadingIndicator.setVisibility(View.VISIBLE);
            loupeArea.setVisibility(View.GONE);
            topMargin.setVisibility(View.GONE);
            loupeLayout.setVisibility(View.GONE);
            wordListLayout.setVisibility(View.GONE);
        }
        //
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) wordListLayout
                .getLayoutParams();
            params.height = wordListHeight;
            params.width = width;
            wordListLayout.setLayoutParams(params);
        wordListLayout.setVisibility(View.VISIBLE);
        
        //
        
        
    }
    
    
    // the funtions returns 3 values in an array of ints
    // [0] : the text size
    // [1] : the text component height
    // [2] : the number of words we can display
    private int[] fontSizeForTextHeight(int totalTextHeight, int nbWords,
        int textWidth, int textSizeMax, int textSizeMin)
    {
        
        int[] result = new int[3];
        String text = "Agj";
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT));
        tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        // tv.setTextSize(30);
        // tv.setHeight(textHeight);
        int textSize = 0;
        int layoutHeight = 0;
        
        final float densityMultiplier = getResources().getDisplayMetrics().density;
        
        for (textSize = textSizeMax; textSize >= textSizeMin; textSize -= 2)
        {
            // Get the font size setting
            float fontScale = Settings.System.getFloat(getContentResolver(),
                Settings.System.FONT_SCALE, 1.0f);
            // Text view line spacing multiplier
            float spacingMult = 1.0f * fontScale;
            // Text view additional line spacing
            float spacingAdd = 0.0f;
            TextPaint paint = new TextPaint(tv.getPaint());
            paint.setTextSize(textSize * densityMultiplier);
            // Measure using a static layout
            StaticLayout layout = new StaticLayout(text, paint, textWidth,
                Alignment.ALIGN_NORMAL, spacingMult, spacingAdd, true);
            layoutHeight = layout.getHeight();
            if ((layoutHeight * nbWords) < totalTextHeight)
            {
                result[0] = textSize;
                result[1] = layoutHeight;
                result[2] = nbWords;
                return result;
            }
        }
        
        // we won't be able to display all the fonts
        result[0] = textSize;
        result[1] = layoutHeight;
        result[2] = totalTextHeight / layoutHeight;
        return result;
    }
    
    
    @Override
    public void onInitARDone(SampleApplicationException exception)
    {
        
        if (exception == null)
        {
            initApplicationAR();
            
            // Hint to the virtual machine that it would be a good time to
            // run the garbage collector:
            //
            // NOTE: This is only a hint. There is no guarantee that the
            // garbage collector will actually be run.
            System.gc();
            
            // Activate the renderer:
            mRenderer.mIsActive = true;
            
            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
            
          
            
            
            // Hides the Loading Dialog
            loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
            //showLoupe(true);
            
            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();
            
            mARUILayout.bringToFront();
           
            RelativeLayout wordListLayout = (RelativeLayout) mUILayout
                    .findViewById(R.id.wordList);
            wordListLayout.setBackgroundColor(Color.TRANSPARENT);
          //add for test
            //videoView=new VideoView(this);
           // addContentView(videoView, new LayoutParams(400, 400));
            //vidoe view
            /*
            videoView=(VideoView)wordListLayout.findViewById(R.id.VideoDisplay);
            
            videoView.setZOrderOnTop(true);    // necessary
            SurfaceHolder sfhTrackHolder = videoView.getHolder();
            sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);*/
            //
            
            //button
            
           // mPlay=(Button)wordListLayout.findViewById(R.id.VideoDisplayLayout.VideoPlay);
			/*mPlay.setOnClickListener(new OnClickListener() {
    			public void onClick(View view) {
    				playVideo(videoView);
    			}
    		});*/
            //
            //textview
            textDisplay=(TextView)wordListLayout.findViewById(R.id.TextDisplay);
            textDisplay.setBackgroundColor(COLOR_OPAUE);
            //
            
            imageDisplay=(ImageView)wordListLayout.findViewById(R.id.ImageDisplay);
            videoIcon=(ImageButton)wordListLayout.findViewById(R.id.VideoDisplay);
            
            
            //add for test
            
            //AR imageView
            zoomImageView=(ZoomImageView)mARUILayout.findViewById(R.id.ARImageDisplay);           
            ARCloseButton=(ImageButton)mARUILayout.findViewById(R.id.ARClose);
            videoView=(VideoView)mARUILayout.findViewById(R.id.ARVideoDisplay);
            videoView.setZOrderOnTop(true);    // necessary
            SurfaceHolder sfhTrackHolder = videoView.getHolder();
            sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
            
            ARTextView=(TextView)mARUILayout.findViewById(R.id.ARTextDisplay);
            
            //
            
            
            try
            {
                vuforiaAppSession.startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);
            } catch (SampleApplicationException e)
            {
                Log.e(LOGTAG, e.getString());
            }
            
            mIsVuforiaStarted = true;
            
            postStartCamera();
            
        } else
        {
            Log.e(LOGTAG, exception.getString());
            finish();
        }
    }
    
    
    // Functions to load and destroy tracking data.
    @Override
    public boolean doLoadTrackersData()
    {
        TrackerManager tm = TrackerManager.getInstance();
        TextTracker tt = (TextTracker) tm
            .getTracker(TextTracker.getClassType());
        WordList wl = tt.getWordList();
        
        if(isImageTarget==true)
        	imageTarget.doLoadTrackersData();
        
       wl.loadWordList("TextReco/Vuforia-English-word.vwl",
           STORAGE_TYPE.STORAGE_APPRESOURCE);
       int number=wl.addWordsFromFile("TextReco/AdditionalWords.lst", 
    		   STORAGE_TYPE.STORAGE_APPRESOURCE);
       Log.e("load data",Integer.toString(number) );
       return true;

    }
    
    
    @Override
    public boolean doUnloadTrackersData()
    {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;
        TrackerManager tm = TrackerManager.getInstance();
        TextTracker tt = (TextTracker) tm
            .getTracker(TextTracker.getClassType());
        WordList wl = tt.getWordList();
        wl.unloadAllLists();
        if(isImageTarget==true)
        	imageTarget.doUnloadTrackersData();
        
        return result;
    }
    
    
    @Override
    public void onQCARUpdate(State state)
    {
    	if(isImageTarget==true)
        	imageTarget.onQCARUpdate(state);
    }
    
    
    @Override
    public boolean doInitTrackers()
    {
        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;
        
        // Indicate if the trackers were initialized correctly
        boolean result = true;
        
        tracker = tManager.initTracker(TextTracker.getClassType());
        if (tracker == null)
        {
            Log.e(
                LOGTAG,
                "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else
        {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        
        if(isImageTarget==true)
        	imageTarget.doInitTrackers();
        
        return result;
    }
    
    
    @Override
    public boolean doStartTrackers()
    {
        // Indicate if the trackers were started correctly
        boolean result = true;
        
        Tracker textTracker = TrackerManager.getInstance().getTracker(
            TextTracker.getClassType());
        if (textTracker != null)
            textTracker.start();
        
        if(isImageTarget==true)
        	imageTarget.doStartTrackers();
        
        return result;
    }
    
    
    @Override
    public boolean doStopTrackers()
    {
        // Indicate if the trackers were stopped correctly
        boolean result = true;
        
        Tracker textTracker = TrackerManager.getInstance().getTracker(
            TextTracker.getClassType());
        if (textTracker != null)
            textTracker.stop();
        
        if(isImageTarget==true)
        	imageTarget.doStopTrackers();
        
        return result;
    }
    
    
    @Override
    public boolean doDeinitTrackers()
    {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;
        Log.e(LOGTAG, "UnloadTrackersData");
        
        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(TextTracker.getClassType());
        
        if(isImageTarget==true)
        	imageTarget.doDeinitTrackers();
        
        return result;
    }
    
    public boolean IsHasImageTracker()
    {
    	return isImageTarget;
    }
    
    
    public int  getScreenWidth(){
	    Display display = getWindowManager().getDefaultDisplay();
	    Point size = new Point();
	    display.getSize(size);
	    int width = size.x;
	    return width;
    }
    
    public int  getScreenHeight(){
	    Display display = getWindowManager().getDefaultDisplay();
	    Point size = new Point();
	    display.getSize(size);
	    int height = size.y;
	    return height;
    }
    
    public boolean isPortrait()
    {
    	if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
    	    //Do some stuff
    		return true;
    	}
    	return false;
    }
    
}
