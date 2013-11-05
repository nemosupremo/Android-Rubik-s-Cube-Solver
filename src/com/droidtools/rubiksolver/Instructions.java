package com.droidtools.rubiksolver;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class Instructions extends Activity {
	
	private PowerManager.WakeLock wl;
	int position;
	boolean preving = false;
	boolean nexting = false;
	CubeSurface cubeSurface;
	TextView counterText;
	TextView infoText;
	ImageButton next;
	ImageButton prev;
	String[] moves = {"Z", "Z", "Z", "ZX'", "XX"};
	static Map<Integer, Integer> colorMap = new HashMap<Integer, Integer>();
	static {
		colorMap.put(0, 0xFFFFFFFF);
		colorMap.put(1, 0xFFFF0000);
		colorMap.put(2, 0xFF00FF00);
		colorMap.put(3, 0xFF0000FF);
		colorMap.put(4, 0xFFFFFF00);
		colorMap.put(5, 0xFFFF6020);
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.instruc);
		
		Home.makeActionBar(this);
		cubeSurface = (CubeSurface) findViewById(R.id.cubesurface);
		counterText = (TextView) findViewById(R.id.counterText);
		infoText = (TextView) findViewById(R.id.infoText);
		next = (ImageButton) findViewById(R.id.stepForward);
		prev = (ImageButton) findViewById(R.id.stepBack);
		
		next.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				next();
			}
		});
		
		prev.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				prev();
			}
		});
		
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
		position = 0;
		updateInfoText(position);
		counterText.setText(String.format("%d/%d", position+1, 7));
		
		cubeSurface.init( (new RubikCube()).getCubeState(), colorMap);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		wl.release();
	}

	@Override
	public void onResume() {
		super.onResume();
		wl.acquire();
	}
	
	protected void updateInfoText(int position) {
		switch (position) {
    	case 1:
    		infoText.setText(R.string.insStep2);
    		break;
    	case 2:
    		infoText.setText(R.string.insStep3);
    		break;
    	case 3:
    		infoText.setText(R.string.insStep4);
    		break;
    	case 4:
    		infoText.setText(R.string.insStep5);
    		break;
    	case 5:
    		infoText.setText(R.string.insStep6);
    		break;
    	case 6:
    		infoText.setText(R.string.insStep7);
    		break;
    	default:
    		infoText.setText(R.string.insStep1);
    		break;
    	}
	}
	
	protected void incrPosition() {
		position++;
	}
	
	protected void decrPosition() {
		position--;
	}
	
	
	protected void next() {
		//Log.d("HELLO", "I AM HERE1");
		if (position >= 6 || nexting || preving) return;
		updateInfoText(position+1);
		counterText.setText(String.format("%d/%d", position+2, 7));
		
		// Only animate for the first 6 steps.
		if (position >= 5) {
			incrPosition();
			return;
		}
		//Log.d("HELLO", "I AM HERE2");
		nexting = true;
		new Thread(new Runnable() {

			@Override
			public void run() {
				cubeSurface.move(RubikMove.getAnimRep(moves[position]), 1, false, true);
				try {
					synchronized (this) {
						do {
							this.wait(100);
						} while (cubeSurface.animating);
					}
				} catch (InterruptedException e) {
					
				} finally {
					nexting = false;
					incrPosition();
				}
			}
			
		
		}).start();
	}
	
	protected void prev() {
		if (position <= 0 || nexting || preving) return;
		updateInfoText(position-1);
		counterText.setText(String.format("%d/%d", position, 7));
		
		// Only animate for the first 6 steps.
		if (position >= 6) {
			decrPosition();
			return;
		}
		preving = true;
		new Thread(new Runnable() {

			@Override
			public void run() {
				cubeSurface.move(RubikMove.getAnimRep(moves[position-1]), -1, false, true);
				try {
					synchronized (this) {
						do {
							this.wait(100);
						} while (cubeSurface.animating);
					}
				} catch (InterruptedException e) {
					
				} finally {
					preving = false;
					decrPosition();
				}
			}
			
		
		}).start();
	}
	
}
