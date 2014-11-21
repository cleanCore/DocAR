/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.example.docar1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import android.util.Log;

import com.example.docar1.TextRenderHelper.WordDesc;
import com.qualcomm.QCAR.QCAR;
import com.qualcomm.vuforia.ImageTargetResult;

import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Obb2D;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vec3F;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.VideoMode;

import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;

import com.qualcomm.vuforia.Word;
import com.qualcomm.vuforia.WordResult;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationSession;

import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleUtils;
import com.qualcomm.vuforia.samples.SampleApplication.utils.Texture;


// The renderer class for the ImageTargets sample. 
public class TextRecoRenderer implements GLSurfaceView.Renderer
{
        
    public boolean mIsActive = false; 
    // Reference to main activity *
    public TextReco mActivity;
    private Renderer mRenderer;
    //video
    private VideoRender videoRender=null;
    private boolean bVideoTrack=false;
    private MediaPlayer mMediaPlayer = null;
    //
    private List<WordDesc> mWords = new ArrayList<WordDesc>();//did not use it any more
    private SampleApplicationSession vuforiaAppSession;
    private boolean bImageTracker=false;
    private static final String LOGTAG = "TextRecoRenderer";
    
    //
    private TextImageRenderHelper imageRender;
    private TextRenderHelper textRender;
    private Text2DImageHelper image2DRender;
	private HashMap<String,Bitmap> bmStorage=new HashMap<String,Bitmap>(); 
	private HashMap<String,String> textStorage=new HashMap<String,String>();
	private HashMap<String,String> videotStorage=new HashMap<String,String>();
	//private ARMarker arMarker=null;
	private Data dt=null;
	private IDataHelper idh=null;
	//private String currentTrackName=null;
	private String[] wordMarkerList=null;
	private int dMarkercount=0;
	private HashMap<String,ARMarker> mARMarkersMap=new HashMap<String,ARMarker>(); 
    //
    
    public TextRecoRenderer(TextReco activity, SampleApplicationSession session,MediaPlayer player,String[] words)
    {
        mActivity = activity;
        vuforiaAppSession = session;
        videoRender=new VideoRender(activity);
        textRender=new TextRenderHelper(session,mWords);
        imageRender=new TextImageRenderHelper(session); 
       // mMediaPlayer = new MediaPlayer();
        mMediaPlayer=player; 
        videoRender.setMediaPlayer(mMediaPlayer);
        image2DRender=new Text2DImageHelper();
        wordMarkerList=words;
        //wordMarkercount=new int[words.length];
        //
   
    }
    
    
    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");
        
        
        // Call function to initialize rendering:
        initRendering();
        
        //videoRender.onSurfaceCreated(gl, config);
        
        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }
    
    
    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
        {
            mWords.clear();
            mActivity.updateWordListUI("");
            return;
        } //clear all the words first. then it will be true forever
        
        // Call our function to render content
       renderFrame(gl);// paly video and draw image or prepare words
       
       /*if(bImageTracker==false&&bVideoTrack==false)
        { 
        
	        List<WordDesc> words;
	        synchronized (mWords)
	        {
	            words = new ArrayList<WordDesc>(mWords);
	        }
	        
	        Collections.sort(words);
	        
	        // update UI - we copy the list to avoid concurrent modifications
	        
	        mActivity.updateWordListUI(new ArrayList<WordDesc>(words)); //update words in UI
        }*/
        
    }
    
    
    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");
        
        mActivity.configureVideoBackgroundROI();
        
        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }
    
    
    // Function for initializing the renderer.
    private void initRendering()
    {
        
        
        mRenderer = Renderer.getInstance();
        textRender.initRendering();
        if(mActivity.IsHasImageTracker())
        {
        	imageRender.initRendering(mActivity);
        }
        
        
    }
    
    // The render function.
    public void renderFrame(GL10 gl)
    {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        State state = mRenderer.begin();
        mRenderer.drawVideoBackground();  //modify
        /*renderInit();
        // enable blending to support transparency
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,
            GLES20.GL_ONE_MINUS_CONSTANT_ALPHA);*/
        
        
        // clear words list
        dMarkercount=0;
        mWords.clear();
        bImageTracker=false;
        TrackableResult result=null;
        TrackableResult currentWordResult=null;
        if(state.getNumTrackableResults()<=0)
        	mActivity.setARVisable(false);
        else
        // did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {
            // get the trackable
        	result= state.getTrackableResult(tIdx);
            
        	
            //add for image
            if (result.isOfType(ImageTargetResult.getClassType()))
            {
            	dMarkercount++;
            	if(dMarkercount>2)break;
            	Trackable trackable = result.getTrackable();
                //printUserData(trackable);
            	Log.d("image mark","found");
                //call framework designed by maohua
                String trackName=trackable.getName();
                
                prepareAR(trackName,trackable,result,"getpicmarkinfo.php?");
                SampleUtils.checkGLError("Render Frame");
                
                
            

                
                bImageTracker=true;
                break;
            	
            	/*
            	if(!mMediaPlayer.isPlaying())
            	{
	            	//mActivity.updateUIVideoPlayer(mMediaPlayer); 
	            	/*try {
	        			final String path =
	        					 //"http://daily3gp.com/vids/747.3gp";
	        					AssetsManager.getAssetPath(mActivity.getApplicationContext(), "test/demo_movie.3g2");
	        			mMediaPlayer.setDataSource(path);
	        		} catch (Exception e) {
	        			Log.e("textRecoerror", e.getMessage(), e);
	        		}*/
            	/*
	            	bVideoTrack=true;
	            	//bImageTracker=true;
	            	Log.e("video", "vido capture");
            	}*/
            	
            }
            //add for image
            
            
            if (result.isOfType(WordResult.getClassType()))
            {
                WordResult wordResult = (WordResult) result;
                Word word = (Word) wordResult.getTrackable();
                Obb2D obb = wordResult.getObb();
                if(textRender.renderFrame(obb,word,result,wordMarkerList)==true)
                	currentWordResult=result;
            }          
           
        }//for
        
        if(!bImageTracker){
        	String textRecoList="";
        	for(int i=0;i<mWords.size();i++){
        		WordDesc str=mWords.get(i);
        		textRecoList=textRecoList+str.text;//+" ";
        	}
        	textRecoList=textRecoList.toLowerCase();
        	//Log.e("find text list",textRecoList);
            for(int i=0; i<wordMarkerList.length;i++){
            	if(textRecoList.contains(wordMarkerList[i].toLowerCase())){
            		Log.d("find one",wordMarkerList[i]);
            		prepareAR(wordMarkerList[i],null,currentWordResult,"getTextMarker.php?");
            		//prepareARNew(wordMarkerList[i],null,result,"getTextMarker.php?");
            	}
            		
            	
            }
            
        	
        }
        
        // Draw the region of interest
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        
        //textRender.drawRegionOfInterest();
        
        GLES20.glDisable(GLES20.GL_BLEND);
        
        
        mRenderer.end();
    }
    
    public void setROI(float center_x, float center_y, float width, float height)
    {
    	textRender.setROI(center_x, center_y, width, height);
    }
    
    public void setViewport(int vpX, int vpY, int vpSizeX, int vpSizeY)
    {
    	textRender.setViewport(vpX, vpY, vpSizeX, vpSizeY);
    }
    
    private void printUserData(Trackable trackable)
    {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }
    
    //for ImageTracker
    public void setTextures(Vector<Texture> textures)
    {
    	imageRender.setTextures(textures);
        
    }
    
    private void renderInit()
    {
    	GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        
        // handle face culling, we need to detect if we are using reflection
        // to determine the direction of the culling
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
        {
            GLES20.glFrontFace(GLES20.GL_CW);  // Front camera
        } else
        {
            GLES20.glFrontFace(GLES20.GL_CCW);   // Back camera
        }
        
    }
    
    static String fromShortArray(short[] str)
    {
        StringBuilder result = new StringBuilder();
        for (short c : str)
            result.appendCodePoint(c);
        return result.toString();
    }
    
    public TextRenderHelper getTextRender()
    {
    	return textRender;
    }
    
   
    private void prepareAR(String trackName,Trackable trackable,TrackableResult result,String url)
    {
    	if(mARMarkersMap.containsKey(trackName))
        	prepareARNew(trackName,trackable,result,url,mARMarkersMap.get(trackName));
        else
        	prepareARNew(trackName,trackable,result,url,null);
    }
    
    private void prepareARNew(String trackName,Trackable trackable,TrackableResult result,String url,ARMarker arMarker )
    {
    	if(arMarker!=null && arMarker.getDownloadFlag()==1)return; 
    	if(arMarker!=null &&arMarker.getDownloadFlag()==2) //dowanlaod finishing
    	{
    		
    		if(arMarker.getBm()!=null&&bmStorage.get(trackName)==null){
    			bmStorage.put(trackName, arMarker.getBm());
    			Log.e("getDownloadFlag","2 and bitmap");}
    		if(arMarker.getVideo()!=null&&videotStorage.get(trackName)==null){
				Log.e("getDownloadFlag","2 and video");
				videotStorage.put(trackName, arMarker.getVideo());
    		}
    		if(arMarker.getText()!=null&&textStorage.get(trackName)==null){
    			Log.e("getDownloadFlag","2 and text");
    			textStorage.put(trackName, arMarker.getText());}
    			
    		//if(arMarker.getText()!=null)
    			//textStorage.put(trackName, arMarker.getText());   
    	}
    	if(bmStorage.get(trackName)!=null||textStorage.get(trackName)!=null
        		||videotStorage.get(trackName)!=null)
        {
    		
    		Vec2F vec = cameraPointToScreenPoint(Tool.projectPoint(CameraDevice.getInstance()
                    .getCameraCalibration(), result.getPose(), new Vec3F(0,0,0)));
            Log.i(LOGTAG, "screen point");
            Log.i(LOGTAG, Arrays.toString(vec.getData()));
            
            mActivity.setUILayoutCenter((int)vec.getData()[0],(int)vec.getData()[1]);
    		
    		mActivity.setARVisable(true);
    		if(textStorage.get(trackName)!=null){
        		mActivity.updateWordListUI(textStorage.get(trackName));
        		Log.e("textStorage","start");}
    		if(videotStorage.get(trackName)!=null){
        		mActivity.updateUIVideoPlayer(videotStorage.get(trackName));
        		Log.e("videotStorage","start");}
        	if(bmStorage.get(trackName)!=null){
        		//image2DRender.renderFrame(bmStorage.get(trackName));
        		//imageRender.renderFrame(trackable, result, bmStorage.get(trackName));
        		mActivity.updateUIImage(bmStorage.get(trackName));
        		Log.e("bmStorage","start");}
        	
        	
        	return;
        }    
    	Log.d("getDownloadFlag ","0 and new start");
    	arMarker=new ARMarker(trackName,url); //dowanlaod finishing or a new dolading
	    dt=new ARDataFiles<ARMarker>(arMarker);
	    idh=new DataHelper(dt);
	    idh.sendTo(true);  
	    //if text
	   /* if(arMarker.getText()!=null)
	    {
	    	textStorage.put(trackName, arMarker.getText());
	    	Log.e("text",arMarker.getText());   	
	    }*/
	    dt.add(null);
	    idh.sendTo(false); 
	    mARMarkersMap.put(trackName, arMarker);
	   // currentTrackName=trackName;          
    }
    
    
    private Vec2F cameraPointToScreenPoint(Vec2F cameraPoint)
    {
        int screenWidth = mActivity.getScreenWidth();
        int screenHeight = mActivity.getScreenHeight();
        VideoMode videoMode = CameraDevice.getInstance().getVideoMode(CameraDevice.MODE.MODE_DEFAULT);
        VideoBackgroundConfig config = Renderer.getInstance().getVideoBackgroundConfig();
        int xOffset = (int) (((int) screenWidth - config.getSize().getData()[0]) / 2.0f + config.getPosition().getData()[0]);
        int yOffset = (int) (((int) screenHeight - config.getSize().getData()[1]) / 2.0f - config.getPosition().getData()[1]);
        if (mActivity.isPortrait())
        {
            // camera image is rotated 90 degrees
            int rotatedX = (int) (videoMode.getHeight() - cameraPoint.getData()[1]);
            int rotatedY = (int) cameraPoint.getData()[0];
            float f1 = rotatedX * config.getSize().getData()[0] / (float) videoMode.getHeight() + xOffset;
            float f2 = rotatedY * config.getSize().getData()[1] / (float) videoMode.getWidth() + yOffset;
            Vec2F vf = new Vec2F (f1, f2);
            return vf;
        }
        else
        {
            float f1 = cameraPoint.getData()[0] * config.getSize().getData()[0] / (float) videoMode.getWidth() + xOffset;
            float f2 =  cameraPoint.getData()[1] * config.getSize().getData()[1] / (float) videoMode.getHeight() + yOffset;
            Vec2F vf = new Vec2F(f1, f2);
            return vf;
        }
    }

    
    

 
    
    
    /*
    
    private void prepareAR(String trackName,Trackable trackable,TrackableResult result,String url )
    {
    	if(bmStorage.get(trackName)!=null||textStorage.get(trackName)!=null
        		||videotStorage.get(trackName)!=null)
        {
        	if(bmStorage.get(trackName)!=null)
        	{
        		//image2DRender.renderFrame(bmStorage.get(trackName));
        		imageRender.renderFrame(trackable, result, bmStorage.get(trackName));
        	}
        	else if(textStorage.get(trackName)!=null)
        	{
        		mActivity.updateWordListUI(textStorage.get(trackName));
        	}
        	else 
        		mActivity.updateUIVideoPlayer(videotStorage.get(trackName));
        	
        }else{
        	if(arMarker!=null && arMarker.getDownloadFlag()==1)return;             	
        	if(arMarker!=null &&arMarker.getDownloadFlag()==2) //dowanlaod finishing
        	{
        		Log.e("getDownloadFlag","2");
        		switch(arMarker.getType())
        		{
        			case BITMAP:
        				bmStorage.put(currentTrackName, arMarker.getBm());
        				break;
        			case FILES:
        				Log.e("capature","capture before setdata");
        				videotStorage.put(currentTrackName, arMarker.getVideo());
        				break;
        		}
        	
        	}    
        	
        	arMarker=new ARMarker(trackName,url); //dowanlaod finishing or a new dolading
    	    dt=new ARDataFiles<ARMarker>(arMarker);
    	    idh=new DataHelper(dt);
    	    idh.sendTo(true);  
    	    //if text
    	    if(arMarker.getText()!=null)
    	    {
    	    	textStorage.put(trackName, arMarker.getText());
    	    	//Log.e("text",arMarker.getText());
    	    	
    	    }else{
        	    dt.add(null);
        	    idh.sendTo(false);
        	}       	             	    
    	    currentTrackName=trackName;
        }          
    }*/
    
    
    //end
    
}
