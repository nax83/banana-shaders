package it.alessandro.banana.program;

import android.content.Context;
import android.opengl.GLES20;
import it.alessandro.banana.util.Util;

public class TextureProgram {

	private static final String U_MATRIX = "u_Matrix";
	private static final String U_TEXTURE_UNIT = "u_TextureUnit";
	private static final String U_SATURATION = "u_Saturation";
	private static final String A_POSITION = "a_Position";
	private static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";
	// Shader program
	private final int program;

	private final int uMatrixLocation;
	private final int uTextureUnitLocation;
	private final int uSaturationLocation;
	private final int aPositionLocation;
	private final int aTextureCoordinatesLocation;

	public TextureProgram(Context context, int vertexShaderResourceId,
			int fragmentShaderResourceId) {
		// Compile the shaders and link the program.
		program = Util.buildProgram(
				Util.readFile(context, vertexShaderResourceId),
				Util.readFile(context, fragmentShaderResourceId));

		// Retrieve uniform locations for the shader program
		uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX);
		uTextureUnitLocation = GLES20.glGetUniformLocation(program,
				U_TEXTURE_UNIT);
		uSaturationLocation = GLES20.glGetUniformLocation(program, U_SATURATION);
		
		// Retrieve attribute locations for the shader program
		aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);
		aTextureCoordinatesLocation = GLES20.glGetAttribLocation(program,
				A_TEXTURE_COORDINATES);
		
	}

	public void useProgram() {
		// Set the current OpenGL shader program to this program.
		GLES20.glUseProgram(program);
	}

	public void setUniforms(float[] matrix, int textureId) {
		// Pass the matrix into the shader program.
		GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

		// Set the active texture unit to texture unit 0.
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

		// Bind the texture to this unit.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

		// Tell the texture uniform sampler to use this texture in the shader by
		// telling it to read from texture unit 0.
		GLES20.glUniform1i(uTextureUnitLocation, 0);
	}
	
	public void setUniformSaturation(float saturation) {
		GLES20.glUniform1f(uSaturationLocation, saturation);
	}

	public int getPositionAttributeLocation() {
		return aPositionLocation;
	}

	public int getTextureCoordinatesAttributeLocation() {
		return aTextureCoordinatesLocation;
	}

}
