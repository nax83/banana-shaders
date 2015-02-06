package it.alessandro.banana;

import it.alessandro.banana.program.TextureProgram;

import android.opengl.GLES20;

/*
 * This class represents a card which is made by 2 triangle.
 * It encapsulates the vertexes and all the attributes 
 * that will be passed to the shader and it's responsible to draw itself
 * */

public class Card {

	public static final int BYTES_PER_FLOAT = 4;
	
	//FRONT and BACK will be used to change the texture displayed on screen 
	public static final int FRONT = 0;
	public static final int BACK = 1;
	
	private static final int POSITION_COMPONENT_COUNT = 2;
	private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
	private static final int TEXTURE2_COORDINATES_COMPONENT_COUNT = 4;
	
	private static final int STRIDE = (POSITION_COMPONENT_COUNT
			+ TEXTURE_COORDINATES_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT)
			* BYTES_PER_FLOAT;

	private float[] cardVertices = {
			
			/* 
			 * Each row represents a point. 
			 * The first two elems are the x and y coords, 
			 * the other represents how texture will be bound
			 */
			
			// Triangle 1
			-0.5f, -1f, 1f, 1f, 0.3f, 1f,
			 0.5f,  1f, 0f, 0f, 0.7f, 0f,
			-0.5f,  1f, 1f, 0f, 0.3f, 0f,
			
			// Triangle 2
			-0.5f, -1f, 1f, 1f, 0.3f, 1f,
			 0.5f, -1f, 0f, 1f, 0.7f, 1f,
			 0.5f,  1f, 0f, 0f, 0.7f, 0f
	};

	private final VertexArray vertexArray;

	public Card() {
		vertexArray = new VertexArray(cardVertices);
	}

	public void bindData(TextureProgram textureProgram, int type) {
		vertexArray.setVertexAttribPointer(0,
				textureProgram.getPositionAttributeLocation(),
				POSITION_COMPONENT_COUNT, STRIDE);
		
		//changes the texture based on the rotation around the y-angle
		if (type == FRONT) {
			vertexArray.setVertexAttribPointer(POSITION_COMPONENT_COUNT,
					textureProgram.getTextureCoordinatesAttributeLocation(),
					TEXTURE_COORDINATES_COMPONENT_COUNT, STRIDE);
		} else {
			vertexArray.setVertexAttribPointer(TEXTURE2_COORDINATES_COMPONENT_COUNT,
					textureProgram.getTextureCoordinatesAttributeLocation(),
					TEXTURE_COORDINATES_COMPONENT_COUNT, STRIDE);
		}
	}

	public void draw() {
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
	}

}
