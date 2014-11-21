package com.example.docar1;

import java.io.IOException;
import java.util.Vector;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.qualcomm.vuforia.samples.SampleApplication.utils.CubeShaders;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleApplication3DModel;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleUtils;
import com.qualcomm.vuforia.samples.SampleApplication.utils.Teapot;
import com.qualcomm.vuforia.samples.SampleApplication.utils.Texture;



public class TextImageRenderHelper {
	//add image
    private SampleApplication3DModel mBuildingsModel;
    private int normalHandleImage;
    private int textureCoordHandleImage;
    private int texSampler2DHandleImage;
    private int vertexHandleImage;
    private int mvpMatrixHandleImage;
    private int shaderProgramIDImage;
    private Vector<Texture> mTextures;
    //private Texture mTexture;
    private Teapot mTeapot;
    private static final float OBJECT_SCALE_FLOAT = 3.0f;
    private final String LOGTAG="ImageRender";
    private SampleApplicationSession vuforiaAppSession;
    //
    
    TextImageRenderHelper(SampleApplicationSession session)
    {
    	vuforiaAppSession=session;
    }
    
    public void setTextures(Vector<Texture> textures)
    {
        mTextures = textures;
        
        
    }
    
    public void initRendering(TextReco activity)
    {
	    // add for image
	    //////////////////////////////////////////////
	////////////////////////////////
		
		mTeapot = new Teapot();
		/*for (Texture t : mTextures)
		{
			GLES20.glGenTextures(1, t.mTextureID, 0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
			GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
			GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
			t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
			GLES20.GL_UNSIGNED_BYTE, t.mData);
		}*/
		
		   shaderProgramIDImage = SampleUtils.createProgramFromShaderSrc(
			            CubeShaders.CUBE_MESH_VERTEX_SHADER,
			            CubeShaders.CUBE_MESH_FRAGMENT_SHADER);
			        
	        vertexHandleImage = GLES20.glGetAttribLocation(shaderProgramIDImage,
	            "vertexPosition");
	        normalHandleImage = GLES20.glGetAttribLocation(shaderProgramIDImage,
	            "vertexNormal");
	        textureCoordHandleImage = GLES20.glGetAttribLocation(shaderProgramIDImage,
	            "vertexTexCoord");
	        mvpMatrixHandleImage = GLES20.glGetUniformLocation(shaderProgramIDImage,
	            "modelViewProjectionMatrix");
	        texSampler2DHandleImage = GLES20.glGetUniformLocation(shaderProgramIDImage,
	            "texSampler2D");
		 try
	        {
	            mBuildingsModel = new SampleApplication3DModel();
	            mBuildingsModel.loadModel(activity.getResources().getAssets(),
	                "Buildings.txt");
	        } catch (IOException e)
	        {
	            Log.e("imageRenderhelper", "Unable to load buildings");
	        }
	}
    ////////////////////////////////////////////////////////
    public void renderFrame(Trackable trackable, TrackableResult result,Bitmap bitmap){
    	
    	
    	Texture t=Texture.loadTextureFromBitmap(bitmap);
		{
			GLES20.glGenTextures(1, t.mTextureID, 0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
			GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
			GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
			t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
			GLES20.GL_UNSIGNED_BYTE, t.mData);
		}
    	
    	
    	
		Matrix44F modelViewMatrix_Vuforia = Tool
            .convertPose2GLMatrix(result.getPose());
        float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();
        
        /*
        int textureIndex = trackable.getName().equalsIgnoreCase("test") ? 0
            : 1;
        textureIndex = trackable.getName().equalsIgnoreCase("tarmac") ? 2
            : textureIndex;*/
        
        // deal with the modelview and projection matrices
        float[] modelViewProjection = new float[16];
        
        Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f,
            OBJECT_SCALE_FLOAT);
        Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT,
            OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);
         
        Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
            .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);
        
        // activate the shader program and bind the vertex/normal/tex coords
        GLES20.glUseProgram(shaderProgramIDImage);
        
    	Log.d(LOGTAG, "a lot of images : " + result.getType());
    	GLES20.glVertexAttribPointer(vertexHandleImage, 3, GLES20.GL_FLOAT,
                false, 0, mTeapot.getVertices());
            GLES20.glVertexAttribPointer(normalHandleImage, 3, GLES20.GL_FLOAT,
                false, 0, mTeapot.getNormals());
            GLES20.glVertexAttribPointer(textureCoordHandleImage, 2,
                GLES20.GL_FLOAT, false, 0, mTeapot.getTexCoords());
            
            GLES20.glEnableVertexAttribArray(vertexHandleImage);
            GLES20.glEnableVertexAttribArray(normalHandleImage);
            GLES20.glEnableVertexAttribArray(textureCoordHandleImage);
            
            // activate texture 0, bind it, and pass to shader
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                //mTextures.get(textureIndex).mTextureID[0]);
            		t.mTextureID[0]);
            GLES20.glUniform1i(texSampler2DHandleImage, 0);
            
            // pass the model view matrix to the shader
            GLES20.glUniformMatrix4fv(mvpMatrixHandleImage, 1, false,
                modelViewProjection, 0);
            
            // finally draw the teapot
            GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                mTeapot.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
                mTeapot.getIndices());
            
            // disable the enabled arrays
            GLES20.glDisableVertexAttribArray(vertexHandleImage);
            GLES20.glDisableVertexAttribArray(normalHandleImage);
            GLES20.glDisableVertexAttribArray(textureCoordHandleImage);
    }
    //
    

}
