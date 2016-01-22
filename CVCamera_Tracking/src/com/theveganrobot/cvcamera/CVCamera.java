package com.theveganrobot.cvcamera;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.opencv.camera.CameraConfig;
import com.opencv.camera.NativePreviewer;
import com.opencv.camera.NativeProcessor;
import com.opencv.camera.NativeProcessor.PoolCallback;
import com.opencv.jni.image_pool;
import com.opencv.opengl.GL2CameraViewer;
import com.theveganrobot.cvcamera.jni.Processor;
import com.theveganrobot.cvcamera.jni.cvcamera;

public class CVCamera extends Activity {

	private static final int DIALOG_OPENING_TUTORIAL = 0;
	private static final int DIALOG_TUTORIAL_TRACKING = 1;

	/**
	 * Display specific toasts.
	 */
	void toasts(int id) {
		switch (id) {
		case DIALOG_OPENING_TUTORIAL:
			Toast.makeText(this, "Try clicking the menu for CV options.",
					Toast.LENGTH_LONG).show();
			break;
		case DIALOG_TUTORIAL_TRACKING:
			Toast.makeText(this, "Tracking motion in viewfinder frames",
					Toast.LENGTH_LONG).show();
			break;
		default:
			break;
		}
	}


	/**
	 * Avoid that the screen get's turned off by the system.
	 */
	public void disableScreenTurnOff() {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	/**
	 * Set's the orientation to landscape, as this is needed for a nice AR experience.
	 */
	public void setOrientation() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	/**
	 * Maximize the application.
	 */
	public void setFullscreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	/**
	 * Remove the title bar.
	 */
	public void setNoTitle() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	/**
	 * Set options menu.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Tracking");
		menu.add("Settings");
		return true;
	}

	// Two view objects
	private NativePreviewer mPreview;
	private GL2CameraViewer glview;

	/**
	 * Respond to options menu selection.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		LinkedList<PoolCallback> defaultcallbackstack = new LinkedList<PoolCallback>();
		
		// Add gl callback for frame drawing
		defaultcallbackstack.addFirst(glview.getDrawCallback());
		
		// Add callback for feature detection, depending on selected menu option
		// These callbacks modify the frame before it is drawn by gl callback
		if (item.getTitle().equals("Tracking")) {
			defaultcallbackstack.addFirst(new TrackingProcessor());
			toasts(DIALOG_TUTORIAL_TRACKING);
		}
		else if (item.getTitle().equals("Settings")) {
			Intent intent = new Intent(this,CameraConfig.class);
			startActivity(intent);
		}

		// Register all callbacks with preview
		mPreview.addCallbackStack(defaultcallbackstack);
		
		return true;
	}

	/**
	 * Set option menu close behavior.
	 */
	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// TODO Auto-generated method stub
		super.onOptionsMenuClosed(menu);
	}

	/**
	 * Set start-up behavior.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Make application full screen without title bar
		setFullscreen();
		disableScreenTurnOff();

		// Create frame layout
		FrameLayout frame = new FrameLayout(this);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.height = getWindowManager().getDefaultDisplay().getHeight();
		params.width = (int) (params.height * 4.0 / 2.88);

		// Create our Preview view and set it as the content of our activity.
		mPreview = new NativePreviewer(getApplication(), 640, 480);
		
		// Create video preview layout
		LinearLayout vidlay = new LinearLayout(getApplication());
		vidlay.setGravity(Gravity.CENTER);
		vidlay.addView(mPreview, params);
		frame.addView(vidlay);

		// Make the glview overlay on top of video preview
		mPreview.setZOrderMediaOverlay(false);

		// Create glview
		glview = new GL2CameraViewer(getApplication(), false, 0, 0);
		glview.setZOrderMediaOverlay(true);

		// Create gl layout
		LinearLayout gllay = new LinearLayout(getApplication());
		gllay.setGravity(Gravity.CENTER);
		gllay.addView(glview, params);
		frame.addView(gllay);

		// Make frame visible
		setContentView(frame);
		
		// Set opening toast
		toasts(DIALOG_OPENING_TUTORIAL);
	}

	/**
	 * Set on-pause behavior.
	 */
	@Override
	protected void onPause() {
		super.onPause();

		// clears the callback stack
		mPreview.onPause();
		glview.onPause();
	}

	/**
	 * Set on-resume behavior.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		glview.onResume();
		mPreview.setParamsFromPrefs(getApplicationContext());
		
		// add an initial callback stack to the preview on resume...
		// this one will just draw the frames to opengl
		LinkedList<NativeProcessor.PoolCallback> cbstack = new LinkedList<PoolCallback>();
		cbstack.add(glview.getDrawCallback());
		mPreview.addCallbackStack(cbstack);
		mPreview.onResume();

	}

	// final processor so that these processor callbacks can access it
	final Processor processor = new Processor();

	/**
	 * Processor for tracking motion.
	 */
	class TrackingProcessor implements NativeProcessor.PoolCallback {
		@Override
		public void process(int idx, image_pool pool, long timestamp,
				NativeProcessor nativeProcessor) {
			processor.processImage(idx, pool, cvcamera.PERFORM_TRACKING);
		}
	}
}
