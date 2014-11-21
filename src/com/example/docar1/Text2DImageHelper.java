package com.example.docar1;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

public class Text2DImageHelper
{
	//Reference to Activity Context
	private final float[] mMVPMatrix = new float[16];
	private final float[] mProjMatrix = new float[16];
	private final float[] mVMatrix = new float[16];
	private float[] mRotationMatrix = new float[16];


	//Added for Textures
	private final FloatBuffer mCubeTextureCoordinates;
	private int mTextureUniformHandle;
	private int mTextureCoordinateHandle;
	private final int mTextureCoordinateDataSize = 2;
	private int mTextureDataHandle;
	
	private final String vertexShaderCode =
	//Test
	"attribute vec2 a_TexCoordinate;" +
	"varying vec2 v_TexCoordinate;" +
	//End Test
	"uniform mat4 uMVPMatrix;" +
	"attribute vec4 vPosition;" +
	"void main() {" +
	"  gl_Position = vPosition * uMVPMatrix;" +
	    //Test
	    "v_TexCoordinate = a_TexCoordinate;" +
	    //End Test
	"}";

	private final String fragmentShaderCode =
	"precision mediump float;" +
	"uniform vec4 vColor;" +
	//Test
	"uniform sampler2D u_Texture;" +
	"varying vec2 v_TexCoordinate;" +
	//End Test
	"void main() {" +
	//"gl_FragColor = vColor;" +
	"gl_FragColor = (v_Color * texture2D(u_Texture, v_TexCoordinate));" +
	"}";

	private final int shaderProgram;    
	private final FloatBuffer vertexBuffer;
	private final ShortBuffer drawListBuffer;
	private int mPositionHandle;
	private int mColorHandle;
	private int mMVPMatrixHandle;
	
	// number of coordinates per vertex in this array
	static final int COORDS_PER_VERTEX = 2;
	static float spriteCoords[] = { -0.5f,  0.5f,   // top left
	                                -0.5f, -0.5f,   // bottom left
	                                 0.5f, -0.5f,   // bottom right
	                                 0.5f,  0.5f }; //top right
	
	private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; //Order to draw vertices
	private final int vertexStride = COORDS_PER_VERTEX * 4; //Bytes per vertex
	
	// Set color with red, green, blue and alpha (opacity) values
	float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

	public Text2DImageHelper()
	{
	    //Initialize Vertex Byte Buffer for Shape Coordinates / # of coordinate values * 4 bytes per float
	    ByteBuffer bb = ByteBuffer.allocateDirect(spriteCoords.length * 4); 
	    //Use the Device's Native Byte Order
	    bb.order(ByteOrder.nativeOrder());
	    //Create a floating point buffer from the ByteBuffer
	    vertexBuffer = bb.asFloatBuffer();
	    //Add the coordinates to the FloatBuffer
	    vertexBuffer.put(spriteCoords);
	    //Set the Buffer to Read the first coordinate
	    vertexBuffer.position(0);
	
	    // S, T (or X, Y)
	    // Texture coordinate data.
	    // Because images have a Y axis pointing downward (values increase as you move down the image) while
	    // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
	    // What's more is that the texture coordinates are the same for every face.
	   /* final float[] cubeTextureCoordinateData =
	    {                                               
            //Front face
            /*0.0f, 0.0f,               
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f*/
/*
            -0.5f,  0.5f,
            -0.5f, -0.5f,
             0.5f, -0.5f,
             0.5f,  0.5f
	    };*/
	    
	    final float[] cubeTextureCoordinateData = {
	    		 (float) 0.5,(float) -0.5, (float) 0.5,(float) 0.5, (float) -0.5,(float) 0.5, (float) -0.5,(float) -0.5 };
	    
	    mCubeTextureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	    mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0);
	
	    //Initialize byte buffer for the draw list
	    ByteBuffer dlb = ByteBuffer.allocateDirect(spriteCoords.length * 2);
	    dlb.order(ByteOrder.nativeOrder());
	    drawListBuffer = dlb.asShortBuffer();
	    drawListBuffer.put(drawOrder);
	    drawListBuffer.position(0);
	
	    int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
	    int fragmentShader =loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
	
	    shaderProgram = GLES20.glCreateProgram();
	    GLES20.glAttachShader(shaderProgram, vertexShader);
	    GLES20.glAttachShader(shaderProgram, fragmentShader);
	
	    //Texture Code
	    GLES20.glBindAttribLocation(shaderProgram, 0, "a_TexCoordinate");
	
	    GLES20.glLinkProgram(shaderProgram);
	
	    //Load the texture
	   
	}

	public void renderFrame(Bitmap bitmap)
	{
		 mTextureDataHandle = loadTexture(bitmap);
	    //Add program to OpenGL ES Environment
	    GLES20.glUseProgram(shaderProgram);
	
	    //Get handle to vertex shader's vPosition member
	    mPositionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");
	
	    //Enable a handle to the triangle vertices
	    GLES20.glEnableVertexAttribArray(mPositionHandle);
	
	    //Prepare the triangle coordinate data
	    GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
	
	    //Get Handle to Fragment Shader's vColor member
	    mColorHandle = GLES20.glGetUniformLocation(shaderProgram, "vColor");
	
	    //Set the Color for drawing the triangle
	    GLES20.glUniform4fv(mColorHandle, 1, color, 0);
	
	    //Set Texture Handles and bind Texture
	    mTextureUniformHandle = GLES20.glGetAttribLocation(shaderProgram, "u_Texture");
	    mTextureCoordinateHandle = GLES20.glGetAttribLocation(shaderProgram, "a_TexCoordinate");
	
	    //Set the active texture unit to texture unit 0.
	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	
	    //Bind the texture to this unit.
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
	
	    //Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
	    GLES20.glUniform1i(mTextureUniformHandle, 0); 
	
	    //Pass in the texture coordinate information
	    mCubeTextureCoordinates.position(0);
	    GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mCubeTextureCoordinates);
	    GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
	
	    //Get Handle to Shape's Transformation Matrix
	    mMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");
	
	    //Apply the projection and view transformation
	    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
	
	    //Draw the triangle
	    GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
	
	    //Disable Vertex Array
	    GLES20.glDisableVertexAttribArray(mPositionHandle);
	}

	public static int loadTexture(Bitmap bitmap)
	{
	    final int[] textureHandle = new int[1];
	
	    GLES20.glGenTextures(1, textureHandle, 0);
	
	    if (textureHandle[0] != 0)
	    {
	       // Bind to the texture in OpenGL
	        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
	
	        // Set filtering
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
	
	        // Load the bitmap into the bound texture.
	        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
	
	        // Recycle the bitmap, since its data has been loaded into OpenGL.
	    }
	
	    if (textureHandle[0] == 0)
	    {
	        throw new RuntimeException("Error loading texture.");
	    }
	
	    return textureHandle[0];
	}
	
	public void matixAlgorithms()
	{
		Matrix.frustumM(mProjMatrix, 0, 1, 1, -1, 1, 3, 7);
		//Set the camera position (View Matrix)
	    Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
	
	    //Calculate the projection and view transformation
	    Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
	
	    //Create a rotation transformation for the triangle
	  //  Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);
	
	    //Combine the rotation matrix with the projection and camera view
	    Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);
	}

	public int loadShader(int type, String shaderCode)
	{
	    //Create a Vertex Shader Type Or a Fragment Shader Type (GLES20.GL_VERTEX_SHADER OR GLES20.GL_FRAGMENT_SHADER)
	    int shader = GLES20.glCreateShader(type);

	    //Add The Source Code and Compile it
	    GLES20.glShaderSource(shader, shaderCode);
	    GLES20.glCompileShader(shader);

	    return shader;
	}
	//end class
}













/*
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
	 
public class Text2DImageHelper  
{
	private FloatBuffer vertices;
	private FloatBuffer texture;
	private ShortBuffer indices;
	private int textureId;

	
    public Text2DImageHelper() {
	    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 2 * 4);
	    byteBuffer.order(ByteOrder.nativeOrder());
	    vertices = byteBuffer.asFloatBuffer();
	//	            vertices.put( new float[] {  -80f,   -120f,0,1f,
	//	                                         80f,  -120f, 1f,1f,
	//	                                         -80f, 120f, 0f,0f,
	//	                                         80f,120f,   1f,0f});
        vertices.put( new float[] {  -80f,   -120f,
                                         80f,  -120f,
                                         -80f, 120f,
                                         80f,  120f});
        ByteBuffer indicesBuffer = ByteBuffer.allocateDirect(6 * 2);
        indicesBuffer.order(ByteOrder.nativeOrder());
        indices = indicesBuffer.asShortBuffer();
        indices.put(new short[] { 0, 1, 2,1,2,3});
 
        ByteBuffer textureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4);
        textureBuffer.order(ByteOrder.nativeOrder());
        texture = textureBuffer.asFloatBuffer();
        texture.put( new float[] { 0,1f,
                                        1f,1f,
                                        0f,0f,
                                        1f,0f});
 
        indices.position(0);
        vertices.position(0);
        texture.position(0);
        
        
		
 
    }
        
	public void initRendering()
	{
		//did nothing
	}
    
    public void renderFrame(GL10 gl,Bitmap bm ) {
        textureId = loadTexture(gl,bm);
       // gl.glViewport(0, 0, glView.getWidth(), glView.getHeight());
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrthof(-160, 160, -240, 240, 1, -1);
 
        gl.glEnable(GL10.GL_TEXTURE_2D);
                        //bind textID
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
 
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 
        gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertices);
 
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texture);
            // gl.glRotatef(1, 0, 1, 0);
        gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 6,
                GL10.GL_UNSIGNED_SHORT, indices);
    }
 
    private int loadTexture(GL10 gl,Bitmap bitmap) {
        // Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open(
		 //   fileName));
		int textureIds[] = new int[1];
		gl.glGenTextures(1, textureIds, 0);
		int textureId = textureIds[0];
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D,
		        GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D,
		        GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
		return textureId;
    }
}*/

