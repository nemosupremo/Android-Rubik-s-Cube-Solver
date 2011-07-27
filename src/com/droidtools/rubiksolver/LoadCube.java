package com.droidtools.rubiksolver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class LoadCube extends Activity {
	private static final String TAG = "LoadCube";
	public ColorDecoder decoder;
	Camera mCamera = null;
	Preview preview;
	ImageButton buttonClick;
	OrientationEventListener orientEL;
	Guides mGuides;
	int pictureState = 0;
	String face;

	byte[] frontFace;
	byte[] backFace;
	byte[] leftFace;
	byte[] rightFace;
	byte[] upFace;
	byte[] downFace;
	
	private PowerManager.WakeLock wl;

	private static final int DIALOG_NO_CAMERA =  0;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
         
		// Animation rotateAnim = AnimationUtils.loadAnimation(this,
		// R.anim.rotation);
		Bundle b = this.getIntent().getExtras();
		face = getIntent().getStringExtra("face");
		loadFaces(b);
		if (face.equals("FRONT")) {
			decoder = new ColorDecoder(getCacheDir().getAbsolutePath());
		} else {
			decoder = b.getParcelable("DECODER");
			if (decoder == null)
				goHome(this);
		}
		
		preview = new Preview(this);
		
		getCamera();
		
		((FrameLayout) findViewById(R.id.preview)).addView(preview);

		mGuides = new Guides(this, face);
		((RelativeLayout) findViewById(R.id.guides)).addView(mGuides);
		mGuides.getLayoutParams().width = LayoutParams.FILL_PARENT;
		mGuides.getLayoutParams().height = LayoutParams.FILL_PARENT;

		buttonClick = (ImageButton) findViewById(R.id.media);
		buttonClick.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (pictureState == 2) {
					pictureState = 0;
					mCamera.startPreview();
				} else if (pictureState == 0) {
					pictureState = 1;
					mCamera.takePicture(shutterCallback, rawCallback,
							jpegCallback);
					/*mCamera.autoFocus(new AutoFocusCallback() {

						@Override
						public void onAutoFocus(boolean success, Camera camera) {
							camera.takePicture(shutterCallback, rawCallback,
									jpegCallback);
						}
					});*/
				}
			}
		});

		
		
		Animation rotateAnim = AnimationUtils.loadAnimation(this,
				R.anim.rotation);
		LayoutAnimationController animController = new LayoutAnimationController(
				rotateAnim, 0);
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.content);
		layout.setLayoutAnimation(animController);

		/*
		 * orientEL = new OrientationEventListener(this,
		 * SensorManager.SENSOR_DELAY_NORMAL){
		 * 
		 * @Override public void onOrientationChanged(int arg0) {
		 * //textviewOrientation.setText("Orientation: " +
		 * String.valueOf(arg0)); rotateControls(); }};
		 * 
		 * if (orientEL.canDetectOrientation()) { orientEL.enable(); }
		 */
		Log.d(TAG, "onCreate'd");
	}

	private void loadFaces(Bundle b) {
		frontFace = b.getByteArray("FRONT");
		backFace = b.getByteArray("BACK");
		leftFace = b.getByteArray("LEFT");
		rightFace = b.getByteArray("RIGHT");
		upFace = b.getByteArray("UP");
		downFace = b.getByteArray("DOWN");
	}

	private Intent makeIntent(Class<?> cls, Bundle b) {
		Intent i = new Intent(this, cls);
		b.putParcelable("DECODER", decoder);
		b.putByteArray("FRONT", frontFace);
		b.putByteArray("BACK", backFace);
		b.putByteArray("LEFT", leftFace);
		b.putByteArray("RIGHT", rightFace);
		b.putByteArray("UP", upFace);
		b.putByteArray("DOWN", downFace);
		i.putExtras(b);
		return i;
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_NO_CAMERA:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Failed to connect to camera.")
			       .setCancelable(false)
			       .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                LoadCube.this.finish();
			           }
			       });
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	public static void goHome(Activity act) {
		Intent inten = new Intent(act, Home.class);
		inten.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		act.startActivity(inten);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.goHome:
			goHome(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	@SuppressWarnings("unused")
	private void rotateControls() {
		// Display display = ((WindowManager)
		// getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		/*
		 * int rotation = getResources().getConfiguration().orientation; int
		 * toDegrees; if (rotation == Surface.ROTATION_90) { toDegrees = 0; }
		 * else if (rotation == Surface.ROTATION_180) { toDegrees = 90; } else
		 * if (rotation == Surface.ROTATION_270) { toDegrees = 180; } else { /*
		 * Surface.ROTATION_0 toDegrees = -90; } Log.d(TAG,
		 * String.format("Rotating from %d to %d------ %d", 0, -90, rotation));
		 */
		RotateAnimation rotateAnim = new RotateAnimation(0, -90, .5f, .5f);
		LayoutAnimationController animController = new LayoutAnimationController(
				rotateAnim, 0);
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.content);
		layout.setLayoutAnimation(animController);
		// fromDegrees = toDegrees;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d(TAG, "Orient change------");
		// rotateControls();
	}

	@Override
	public void onPause() {
		super.onPause();
		releaseCamera();
		wl.release();
	}

	@Override
	public void onResume() {
		super.onResume();
		getCamera();
		wl.acquire();
	}

	private void getCamera() {
		try
		{
			if (mCamera == null) {
				mCamera = Camera.open();
				if (mCamera == null)
					showDialog(DIALOG_NO_CAMERA);
				else
					preview.setCamera(mCamera);
			}
		}
		catch (Exception ex)
		{
			showDialog(DIALOG_NO_CAMERA);
		}
		//
	}

	private void releaseCamera() {
		if (mCamera != null) {
			preview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
	}

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			Log.d(TAG, "onShutter'd");
		}
	};

	/** Handles data for raw picture */
	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "onPictureTaken - raw");
		}
	};

	/** Handles data for jpeg picture */
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Bitmap picture = BitmapFactory
					.decodeByteArray(data, 0, data.length);
			new DecodeImageTask().execute(picture);
			// picture.getHeight();
			/*
			 * FileOutputStream outStream = null; try { // write to local
			 * sandbox file system // outStream = //
			 * CameraDemo.this.openFileOutput(String.format("%d.jpg", //
			 * System.currentTimeMillis()), 0); // Or write to sdcard outStream
			 * = new FileOutputStream(String.format( "/sdcard/rubik/%d.jpg",
			 * System.currentTimeMillis())); outStream.write(data);
			 * outStream.close(); Log.d(TAG, "onPictureTaken - wrote bytes: " +
			 * data.length); } catch (FileNotFoundException e) {
			 * e.printStackTrace(); } catch (IOException e) {
			 * e.printStackTrace(); } finally { }
			 */
			Log.d(TAG,
					String.format("onPictureTaken - jpeg - %d",
							picture.getHeight()));
			pictureState = 2;
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_CAMERA)
				|| (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
			if (pictureState == 2) {
				pictureState = 0;
				mCamera.startPreview();
			} else if (pictureState == 0) {
				pictureState = 1;
				mCamera.autoFocus(new AutoFocusCallback() {

					@Override
					public void onAutoFocus(boolean success, Camera camera) {
						camera.takePicture(null, null, jpegCallback);
					}
				});
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private class DecodeImageTask extends
			AsyncTask<Bitmap, Void, byte[]> {
		ProgressDialog pd;

		@Override
		protected byte[] doInBackground(Bitmap... bitmap) {
			Log.d(TAG,
					String.format("Async - jpeg - %d", bitmap[0].getHeight()));
			Bitmap img = bitmap[0];
			if (img.getWidth() > 600) {
				int newWidth = 600;
				int newHeight = 360;
				float scaleWidth = ((float) newWidth) / img.getWidth();
				float scaleHeight = ((float) newHeight) / img.getHeight();
			    Matrix matrix = new Matrix();
		        matrix.postScale(scaleWidth, scaleHeight);
		        img = Bitmap.createBitmap(img, 0, 0,
		        		img.getWidth(), img.getHeight(), matrix, true);
			}
			return decoder.decode(img);
			//return new byte[]{-1,-1,-1,-1,-1,-1,-1,-1,-1};
		}

		@Override
		protected void onPreExecute() {
			pd = ProgressDialog
					.show(LoadCube.this, "", "Reading cube...", true);
		}

		@Override
		protected void onPostExecute(byte[] result) {
			pd.dismiss();
			/*
			 * String text =
			 * String.format("RESULTS : %d %d %d | %d %d %d | %d %d %d",
			 * result.get(0), result.get(1), result.get(2), result.get(3),
			 * result.get(4), result.get(5), result.get(6), result.get(5),
			 * result.get(8));
			 */
			Bundle b = new Bundle();
			b.putByteArray("SCAN_RESULTS", result);
			b.putString("SCAN_FACE", face);
			Intent i = makeIntent(CheckFace.class, b);
			i.putExtras(b);
			startActivity(i);
			// Toast.makeText(LoadCube.this, text, Toast.LENGTH_LONG);
			// Log.d(TAG, text);
		}
	}

}