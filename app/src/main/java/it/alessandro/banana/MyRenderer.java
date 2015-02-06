package it.alessandro.banana;

import it.alessandro.banana.program.TextureProgram;
import it.alessandro.banana.util.Util;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyRenderer implements GLSurfaceView.Renderer {

	private static final String TAG = "MyRenderer";
	
	private final float[] projectionMatrix = new float[16];
	private final float[] modelMatrix = new float[16];

	private float[] rotationMatrix = new float[16];

	private Card card;
	private Context ctx;

	private TextureProgram textureProgram;
	private int textureFront;
	private int textureBack;
	float angle = 0;
	float saturation = 1.f;

	public MyRenderer(Context ctx) {
		this.ctx = ctx;
	}

	public void onSurfaceCreated(GL10 ref, EGLConfig c) {
		
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		//checking support to non POT texture
		if(!Util.isNPOTSupported()){
			Log.d(TAG, "NPOT textures are not supported");
			}
		
		card = new Card();
		textureProgram = new TextureProgram(ctx, R.raw.vertex_shader,
				R.raw.fragment_shader);
	}

	public void onSurfaceChanged(GL10 ref, int w, int h) {
		
		GLES20.glViewport(0, 0, w, h);
		//I don't need to load the textures in background because the renderer class runs
		//on its own thread thus the UI will not be blocked during the process
		textureFront = Util.loadTexture(ctx, R.drawable.minion1, w, h);
		textureBack = Util.loadTexture(ctx, R.drawable.minion2, w, h);

		Util.perspectiveM(projectionMatrix, 45, (float) w / (float) h, 1f, 10f);
		Matrix.setIdentityM(modelMatrix, 0);
		Matrix.translateM(modelMatrix, 0, 0f, 0f, -2.5f);
		final float[] temp = new float[16];
		Matrix.multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
		System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
	}

	public void onDrawFrame(GL10 ref) {
		float[] scratch = new float[16];
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		
		long time = SystemClock.uptimeMillis() % 4000L;
		float y_angle = 0.090f * ((int) time);
		//adding the z rotation based on the user input
		Matrix.setRotateM(rotationMatrix, 0, angle, 0, 0, 1f);
		
		Matrix.rotateM(rotationMatrix, 0, y_angle, 0, 1f, 0f);
		Matrix.multiplyMM(scratch, 0, projectionMatrix, 0, rotationMatrix, 0);

		//pass all params needed by the shader program
		textureProgram.useProgram();
		textureProgram.setUniformSaturation(saturation);
		
		if (y_angle > 90 && y_angle < 270) {
			textureProgram.setUniforms(scratch, textureFront);	
			card.bindData(textureProgram, Card.FRONT);
		} else {
			textureProgram.setUniforms(scratch, textureBack);
			card.bindData(textureProgram, Card.BACK);
		}
		//let the elements on the scene draw themselves
		card.draw();
	}

	public void rotate(int angle) {
		this.angle = angle;

	}

	public void blend(float saturation) {
		this.saturation = saturation;
	}
}
