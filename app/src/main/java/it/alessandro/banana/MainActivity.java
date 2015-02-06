package it.alessandro.banana;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.SeekBar;

public class MainActivity extends Activity {

	private GLSurfaceView glView;
	private MyRenderer renderer;

	protected void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.main);

		glView = (GLSurfaceView) findViewById(R.id.surfaceviewclass);
		configureOpenGL();
		//add event handler to seekbar
		SeekBar color_seeker = (SeekBar) findViewById(R.id.select_color);
		color_seeker
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						
						//value will change the saturation value in percentage
						final float value = (float)progress/100;
						glView.queueEvent(new Runnable() {
							
							@Override
							public void run() {
								renderer.blend(value);
							}
						});
					}

					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					public void onStopTrackingTouch(SeekBar seekBar) {
					}
				});
		
		SeekBar rotation_seeker = (SeekBar) findViewById(R.id.select_z_rotation);
		rotation_seeker
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					
					int offset = 180;
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						final int angle = progress-offset;
						glView.queueEvent(new Runnable() {
							@Override
							public void run() {
								renderer.rotate(angle);
							}
						});
					}

					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					public void onStopTrackingTouch(SeekBar seekBar) {

					}
				});
	}

	private void configureOpenGL() {
		glView.setEGLContextClientVersion(2);
		renderer = new MyRenderer(this);
		glView.setRenderer(renderer);
	}

	protected void onResume() {
		super.onResume();
		glView.onResume();
	}

	protected void onPause() {
		super.onPause();
		glView.onPause();
	}

}
