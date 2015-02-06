package it.alessandro.banana.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

public class Util {

	private static final String TAG = "Util";

	/*
	 * It's used to read the shader files saved in the raw folder
	 * */
	public static String readFile(Context context, int resourceId) {
		StringBuilder body = new StringBuilder();
		try {
			InputStream inputStream = context.getResources().openRawResource(
					resourceId);
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream);
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			String nextLine;
			while ((nextLine = bufferedReader.readLine()) != null) {
				body.append(nextLine);
				body.append('\n');
			}
		} catch (IOException e) {
			throw new RuntimeException(
					"Could not open resource: " + resourceId, e);
		} catch (Resources.NotFoundException nfe) {
			throw new RuntimeException("Resource not found: " + resourceId, nfe);
		}
		return body.toString();
	}

	public static int compileShader(int type, String shaderCode) {

		final int shaderId = GLES20.glCreateShader(type);
		if (shaderId == 0)
			throw new RuntimeException("glCreateShader failed to create type: "
					+ type);
		GLES20.glShaderSource(shaderId, shaderCode);
		GLES20.glCompileShader(shaderId);
		final int[] compileStatus = new int[1];
		GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compileStatus,
				0);
		if (compileStatus[0] == 0) {
			String err = GLES20.glGetShaderInfoLog(shaderId);
			Log.d(TAG, "Results of compiling source:" + err);
			GLES20.glDeleteShader(shaderId);
			throw new RuntimeException(err);
		}
		return shaderId;

	}

	public static int linkProgram(int vertexShaderId, int fragmentShaderId) {

		final int programId = GLES20.glCreateProgram();
		if (programId == 0)
			throw new RuntimeException("Cannot link shaders");
		GLES20.glAttachShader(programId, vertexShaderId);
		GLES20.glAttachShader(programId, fragmentShaderId);
		GLES20.glLinkProgram(programId);
		final int[] linkStatus = new int[1];
		GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0);
		if (linkStatus[0] == 0) {
			String err = GLES20.glGetProgramInfoLog(programId);
			GLES20.glDeleteProgram(programId);
			throw new RuntimeException(err);
		}
		return programId;
	}

	public static int buildProgram(String vertexShaderSource,
			String fragmentShaderSource) {
		int program;
		int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER,
				vertexShaderSource);
		int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER,
				fragmentShaderSource);
		program = linkProgram(vertexShader, fragmentShader);
		return program;
	}

	/*
	 * It's used to calculate the perspection matrix. 
	 * I used the frustumM function (the perspectiveM() function it's available starting from ICS) 
	 * but I read that on some versions of Android is bugged 
	 * and its suggested to use this one
	 */
	public static void perspectiveM(float[] m, float yFovInDegrees,
			float aspect, float n, float f) {
		final float angleInRadians = (float) (yFovInDegrees * Math.PI / 180.0);
		final float a = (float) (1.0 / Math.tan(angleInRadians / 2.0));
		m[0] = a / aspect;
		m[1] = 0f;
		m[2] = 0f;
		m[3] = 0f;
		m[4] = 0f;
		m[5] = a;
		m[6] = 0f;
		m[7] = 0f;
		m[8] = 0f;
		m[9] = 0f;
		m[10] = -((f + n) / (f - n));
		m[11] = -1f;
		m[12] = 0f;
		m[13] = 0f;
		m[14] = -((2f * f * n) / (f - n));
		m[15] = 0f;
	}
	
	public static boolean isNPOTSupported() {
	    String extensions = GLES20.glGetString(GL10.GL_EXTENSIONS);
	    return extensions.indexOf("GL_OES_texture_npot") != -1;
	}

	public static int loadTexture(Context context, int resourceId, int w, int h) {
		final int[] textureObjectIds = new int[1];
		GLES20.glGenTextures(1, textureObjectIds, 0);
		if (textureObjectIds[0] == 0) {
			Log.d(TAG, "Could not generate a new OpenGL texture object.");
			return 0;
		}
		final Bitmap bitmap = decodeSampledBitmapFromResource(
				context.getResources(), resourceId, w, h);
		
		if (bitmap == null) {
			Log.d(TAG, "Resource ID " + resourceId + " could not be decoded.");
			GLES20.glDeleteTextures(1, textureObjectIds, 0);
			return 0;
		}
		//on devices which don't support non-POT I decided to simply scale the images to a fixed size
		//This solution takes care of the width scaling and may have problems on larger screen (tablet)
		if(!Util.isNPOTSupported()){
			Bitmap background = Bitmap.createBitmap((int)1024, (int)1024, Config.ARGB_8888);
			float originalWidth = bitmap.getWidth();
			Canvas canvas = new Canvas(background);
			float scale = 1024/originalWidth;
			Matrix transformation = new Matrix();
			transformation.preScale(scale, 1);
			Paint paint = new Paint();
			paint.setFilterBitmap(true);
			canvas.drawBitmap(bitmap, transformation, paint);
			bitmap.recycle();

			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0]);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, background, 0);
			background.recycle();
		}else{
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0]);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
			bitmap.recycle();			
		}
		GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
		// Unbind from the texture.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

		return textureObjectIds[0];
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		Log.d(TAG, "inSampleSize " + inSampleSize);
		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight) {
		Log.d(TAG, "req width " + reqWidth + "req height " + reqHeight);
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}
}
