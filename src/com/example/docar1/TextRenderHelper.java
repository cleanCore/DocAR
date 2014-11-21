package com.example.docar1;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Obb2D;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Word;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.qualcomm.vuforia.samples.SampleApplication.utils.LineShaders;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleUtils;

public class TextRenderHelper {
    private static final int MAX_NB_WORDS = 132;
    private static final float TEXTBOX_PADDING = 0.0f;
    
    private static final float ROIVertices[] = { -0.5f, -0.5f, 0.0f, 0.5f,
            -0.5f, 0.0f, 0.5f, 0.5f, 0.0f, -0.5f, 0.5f, 0.0f };
    
    private static final int NUM_QUAD_OBJECT_INDICES = 8;
    private static final short ROIIndices[] = { 0, 1, 1, 2, 2, 3, 3, 0 };
    
    private static final float quadVertices[] = { -0.5f, -0.5f, 0.0f, 0.5f,
            -0.5f, 0.0f, 0.5f, 0.5f, 0.0f, -0.5f, 0.5f, 0.0f, };
    
    private static final short quadIndices[] = { 0, 1, 1, 2, 2, 3, 3, 0 };
    private int shaderProgramID;
    
    private int vertexHandle;
    
    private int mvpMatrixHandle;
    private int lineOpacityHandle;
    
    private int lineColorHandle;
    
    private ByteBuffer mROIVerts = null;
    private ByteBuffer mROIIndices = null;
	public float ROICenterX;
    public float ROICenterY;
    public float ROIWidth;
    public float ROIHeight;
    private int viewportPosition_x;
    private int viewportPosition_y;
    private int viewportSize_x;
    private int viewportSize_y;
    private ByteBuffer mQuadVerts;
    private ByteBuffer mQuadIndices;
    private SampleApplicationSession vuforiaAppSession;
    private List<WordDesc> mWords;    
	 
    TextRenderHelper(SampleApplicationSession session,List<WordDesc> words)
	 {
		 vuforiaAppSession=session; 
		 mWords=words;
	 }
	    
	 public void initRendering(){
	// init the vert/inde buffers
	    mROIVerts = ByteBuffer.allocateDirect(4 * ROIVertices.length);
	    mROIVerts.order(ByteOrder.LITTLE_ENDIAN);
	    updateROIVertByteBuffer();
	    
	    mROIIndices = ByteBuffer.allocateDirect(2 * ROIIndices.length);
	    mROIIndices.order(ByteOrder.LITTLE_ENDIAN);
	    for (short s : ROIIndices)
	        mROIIndices.putShort(s);
	    mROIIndices.rewind();
	    
	    mQuadVerts = ByteBuffer.allocateDirect(4 * quadVertices.length);
	    mQuadVerts.order(ByteOrder.LITTLE_ENDIAN);
	    for (float f : quadVertices)
	        mQuadVerts.putFloat(f);
	    mQuadVerts.rewind();
	    
	    mQuadIndices = ByteBuffer.allocateDirect(2 * quadIndices.length);
	    mQuadIndices.order(ByteOrder.LITTLE_ENDIAN);
	    for (short s : quadIndices)
	        mQuadIndices.putShort(s);
	    mQuadIndices.rewind();
	    

       // GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
         //   : 1.0f);
        
        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
            LineShaders.LINE_VERTEX_SHADER, LineShaders.LINE_FRAGMENT_SHADER);
        
        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexPosition");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "modelViewProjectionMatrix");
        
        lineOpacityHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "opacity");
        lineColorHandle = GLES20.glGetUniformLocation(shaderProgramID, "color");
	 }
	 
	 private void updateROIVertByteBuffer()
	 {
	        mROIVerts.rewind();
	        for (float f : ROIVertices)
	            mROIVerts.putFloat(f);
	        mROIVerts.rewind();
	 }
	 
	 public boolean renderFrame(Obb2D obb,Word word,TrackableResult result,String[] wordMarkerList){
		 String wordU = word.getStringU();
		 Vec2F wordBoxSize = null;
	     if (wordU != null)
	     {
	         // in portrait, the obb coordinate is based on
	         // a 0,0 position being in the upper right corner
	         // with :
	         // X growing from top to bottom and
	         // Y growing from right to left
	         //
	         // we convert those coordinates to be more natural
	         // with our application:
	         // - 0,0 is the upper left corner
	         // - X grows from left to right
	         // - Y grows from top to bottom
	         float wordx = -obb.getCenter().getData()[1];
	         float wordy = obb.getCenter().getData()[0];
	         wordBoxSize = word.getSize();
	         if (mWords.size() < MAX_NB_WORDS)
	         {
	             mWords.add(new WordDesc(wordU,
	                 (int) (wordx - wordBoxSize.getData()[0] / 2),
	                 (int) (wordy - wordBoxSize.getData()[1] / 2),
	                 (int) (wordx + wordBoxSize.getData()[0] / 2),
	                 (int) (wordy + wordBoxSize.getData()[1] / 2)));
	         }
	         
	     }
	     
	    
	    	
	    	 for(int i=0; i<wordMarkerList.length;i++){
            	if(wordMarkerList[i].contains(wordU.toLowerCase()))
            	{
            	 
			     Matrix44F mvMat44f = Tool.convertPose2GLMatrix(result.getPose());
			     float[] mvMat = mvMat44f.getData();
			     float[] mvpMat = new float[16];
			     Matrix.translateM(mvMat, 0, 0, 0, 0);
			     Matrix.scaleM(mvMat, 0, wordBoxSize.getData()[0] - TEXTBOX_PADDING,
			         wordBoxSize.getData()[1] - TEXTBOX_PADDING, 1.0f);
			     Matrix.multiplyMM(mvpMat, 0, vuforiaAppSession
			         .getProjectionMatrix().getData(), 0, mvMat, 0);
			     
			     GLES20.glUseProgram(shaderProgramID);
			     GLES20.glLineWidth(3.0f);
			     GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
			         false, 0, mQuadVerts);
			     GLES20.glEnableVertexAttribArray(vertexHandle);
			     GLES20.glUniform1f(lineOpacityHandle, 1.0f);
			     GLES20.glUniform3f(lineColorHandle, 1.0f, 0.447f, 0.0f);
			     GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMat, 0);
			     GLES20.glDrawElements(GLES20.GL_LINES, NUM_QUAD_OBJECT_INDICES,
			         GLES20.GL_UNSIGNED_SHORT, mQuadIndices);
			     GLES20.glDisableVertexAttribArray(vertexHandle);
			     GLES20.glLineWidth(1.0f);
			     GLES20.glUseProgram(0);
			     return true;
            	}
	    	 }
	     return false;
	 }
	 //end class
	 
    public void setROI(float center_x, float center_y, float width, float height)
    {
        ROICenterX = center_x;
        ROICenterY = center_y;
        ROIWidth = width;
        ROIHeight = height;
    }
	    
	    
    class WordDesc implements Comparable<WordDesc>
    {
        public WordDesc(String text, int aX, int aY, int bX, int bY)
        {
            this.text = text;
            this.Ax = aX;
            this.Ay = aY;
            this.Bx = bX;
            this.By = bY;
        }
        
        String text;
        int Ax, Ay, Bx, By;
        
        
        @Override
        public int compareTo(WordDesc w2)
        {
            WordDesc w1 = this;
            int ret = 0;
            
            // we check first if both words are on the same line
            // both words are said to be on the same line if the
            // mid point (on Y axis) of the first point
            // is between the values of the second point
            int mid1Y = (w1.Ay + w1.By) / 2;
            
            if ((mid1Y < w2.By) && (mid1Y > w2.Ay))
            {
                // words are on the same line
                ret = w1.Ax - w2.Ax;
            } else
            {
                // words on different line
                ret = w1.Ay - w2.Ay;
            }
            //Log.e(LOGTAG, "Compare result> " + ret);
            return ret;
        }
    }
    
    private void setOrthoMatrix(float nLeft, float nRight, float nBottom,
            float nTop, float nNear, float nFar, float[] _ROIOrthoProjMatrix)
        {
            for (int i = 0; i < 16; i++)
                _ROIOrthoProjMatrix[i] = 0.0f;
            
            _ROIOrthoProjMatrix[0] = 2.0f / (nRight - nLeft);
            _ROIOrthoProjMatrix[5] = 2.0f / (nTop - nBottom);
            _ROIOrthoProjMatrix[10] = 2.0f / (nNear - nFar);
            _ROIOrthoProjMatrix[12] = -(nRight + nLeft) / (nRight - nLeft);
            _ROIOrthoProjMatrix[13] = -(nTop + nBottom) / (nTop - nBottom);
            _ROIOrthoProjMatrix[14] = (nFar + nNear) / (nFar - nNear);
            _ROIOrthoProjMatrix[15] = 1.0f;
            
        }
        
    public void setViewport(int vpX, int vpY, int vpSizeX, int vpSizeY)
    {
        viewportPosition_x = vpX;
        viewportPosition_y = vpY;
        viewportSize_x = vpSizeX;
        viewportSize_y = vpSizeY;
    }
    
    
    public void drawRegionOfInterest()
    {
    	drawRegionOfInterest(ROICenterX, ROICenterY, ROIWidth, ROIHeight);
    }
    
    private  void drawRegionOfInterest(float center_x, float center_y,
        float width, float height)
    {
        // assumption is that center_x, center_y, width and height are given
        // here in screen coordinates (screen pixels)
        float[] orthProj = new float[16];
        setOrthoMatrix(0.0f, (float) viewportSize_x, (float) viewportSize_y,
            0.0f, -1.0f, 1.0f, orthProj);
        
        // compute coordinates
        float minX = center_x - width / 2;
        float maxX = center_x + width / 2;
        float minY = center_y - height / 2;
        float maxY = center_y + height / 2;
        
        // Update vertex coordinates of ROI rectangle
        ROIVertices[0] = minX - viewportPosition_x;
        ROIVertices[1] = minY - viewportPosition_y;
        ROIVertices[2] = 0;
        
        ROIVertices[3] = maxX - viewportPosition_x;
        ROIVertices[4] = minY - viewportPosition_y;
        ROIVertices[5] = 0;
        
        ROIVertices[6] = maxX - viewportPosition_x;
        ROIVertices[7] = maxY - viewportPosition_y;
        ROIVertices[8] = 0;
        
        ROIVertices[9] = minX - viewportPosition_x;
        ROIVertices[10] = maxY - viewportPosition_y;
        ROIVertices[11] = 0;
        
        updateROIVertByteBuffer();
        
        GLES20.glUseProgram(shaderProgramID);
        GLES20.glLineWidth(3.0f);
        
        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false,
            0, mROIVerts);
        GLES20.glEnableVertexAttribArray(vertexHandle);
        
        GLES20.glUniform1f(lineOpacityHandle, 0.7f); // 0.35f);
        GLES20.glUniform3f(lineColorHandle, 0.0f, 0.0f, 1.0f);// R,G,B
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, orthProj, 0);
        
        // Then, we issue the render call
        GLES20.glDrawElements(GLES20.GL_LINES, NUM_QUAD_OBJECT_INDICES,
            GLES20.GL_UNSIGNED_SHORT, mROIIndices);
        
        // Disable the vertex array handle
        GLES20.glDisableVertexAttribArray(vertexHandle);
        
        // Restore default line width
        GLES20.glLineWidth(1.0f);
        
        // Unbind shader program
        GLES20.glUseProgram(0);
    }
    //end class
}
