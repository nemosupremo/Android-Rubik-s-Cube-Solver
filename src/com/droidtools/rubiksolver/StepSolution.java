package com.droidtools.rubiksolver;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class StepSolution extends Activity implements Runnable {
	ArrayList<RubikMove> sol;
	String hid;
	Bundle bundle;
	byte[] cubeState;
	byte[] colorArray;
	CubeSurface cubeSurface;
	MoveNavigator moveNav;
	ImageButton play;
	ImageButton pause;
	ImageButton next;
	ImageButton prev;
	int position;
	boolean playing = false;
	boolean nexting = false;
	boolean preving = false;
	Thread playThread;
	TextView counterText;

	private PowerManager.WakeLock wl;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stepbystep);
		
		Home.makeActionBar(this);
		
		if (savedInstanceState != null)
			bundle = savedInstanceState;
		else
			bundle = this.getIntent().getExtras();
		sol = bundle.getParcelableArrayList("SOLUTION");
		cubeState = bundle.getByteArray("CUBE_STATE");
		colorArray = bundle.getByteArray("COLORS");
		hid = bundle.getString("HID");
		
		play = (ImageButton) findViewById(R.id.play);
		pause = (ImageButton) findViewById(R.id.stop);
		next = (ImageButton) findViewById(R.id.stepForward);
		prev = (ImageButton) findViewById(R.id.stepBack);
		counterText = (TextView) findViewById(R.id.counterText);
		
		play.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				play();
			}
		});
		
		pause.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				pause();
			}
		});
		
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
		
		
		cubeSurface = (CubeSurface) findViewById(R.id.cubesurface);
		moveNav = (MoveNavigator) findViewById(R.id.movenav);
		/*RubikCube.CubeState rc = new RubikCube.CubeState(new byte[]{
				0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  1,  1,  1,  1,  1,  1,  1,  1,  2,  2,
				2,  2,  2,  2,  2,  2,  2,  3,  3,  3,  3,  3,  3,  3,  3,  3,  4,  4,  4,  4,
				4,  4,  4,  4,  4,  5,  5,  5,  5,  5,  5,  5,  5,  5
		});
		Map<Integer, Integer> mp = new HashMap<Integer, Integer>();
		mp.put(0,0xFF0000);
		mp.put(1,0xFF8000);
		mp.put(2,0xFFFF00);
		mp.put(3,0x0000FF);
		mp.put(4,0xFFFFFF);
		mp.put(5,0x00FF00);*/
		
		/*sol = new ArrayList<RubikMove>();
		for (int i=0; i<30; i++) {
			sol.add(new RubikMove('F', false));
		}*/
		
		position = bundle.getInt("ANIM_POSITION", 0);
		if (bundle.containsKey("ANIM_STATE"))
			cubeSurface.init( bundle.getByteArray("ANIM_STATE"), makeColors(colorArray));
		else
			cubeSurface.init( new RubikCube.CubeState(cubeState), makeColors(colorArray));
		playing = false;
		moveNav.init(sol, position);
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
        counterText.setText(String.format("%d/%d", position+1, sol.size()));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		wl.release();
		pause();
	}

	@Override
	public void onResume() {
		super.onResume();
		wl.acquire();
	}

	
	protected void incrPosition() {
		position++;
		moveNav.setPosition(position);
		moveNav.postInvalidate();
		counterText.post(new Runnable() {
	        public void run() {
	        	int step = position + 1;
	        	int size = sol.size();
	        	if (step > size) {
	        		counterText.setText(getString(R.string.solved));
	        	} else {
	        		counterText.setText(String.format(
	        				getString(R.string.simpleCounter), position+1, sol.size()));	
	        	}
	        }
	      });
	}
	
	protected void decrPosition() {
		position--;
		moveNav.setPosition(position);
		moveNav.postInvalidate();
		counterText.post(new Runnable() {
	        public void run() {
	        	counterText.setText(String.format(
	        			getString(R.string.simpleCounter), position+1, sol.size()));
	        }
	      });
	}
	
	
	protected void play() {
		if (!playing) {
			playing = true;
			playThread = new Thread(this, "Play Thread");
			playThread.start();
		}
	}
	
	protected void next() {
		//Log.d("HELLO", "I AM HERE1");
		if (position >= sol.size() || nexting || preving) return;
		//Log.d("HELLO", "I AM HERE2");
		playing = false;
		nexting = true;
		new Thread(new Runnable() {

			@Override
			public void run() {
				cubeSurface.move(sol.get(position).getAnimRep(), 1, true, true);
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
		//Log.d("HELLO", "I AM PREV");
		playing = false;
		preving = true;
		new Thread(new Runnable() {

			@Override
			public void run() {
				cubeSurface.move(sol.get(position-1).getAnimRep(), -1, true, true);
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
	
	protected void pause() {
		playing = false;
	}
	
	@Override
	public void run() {
		while (playing) {
			if (position >= sol.size()) {
				playing = false;
				break;
			}
			cubeSurface.move(sol.get(position).getAnimRep(), 1, false, true);
			
			try {
				synchronized (this) {
					do {
						this.wait(100);
					} while (cubeSurface.animating);
					this.wait(100);
				}
			} catch (InterruptedException e) {
				
			} finally {
				incrPosition();
				
			}
		}
	}
	
	protected SparseIntArray makeColors(byte[] colors) {
		SparseIntArray ret = new SparseIntArray();
		for (int i=0; i<colors.length; i+=5) {
			byte key = colors[i];
			int col = (colors[i+1] << 24)
        	+ ((colors[i+2] & 0xFF) << 16)
        	+ ((colors[i+3] & 0xFF) << 8)
        	+ (colors[i+4] & 0xFF);
			ret.put((int)key, col);
		}
		return ret;
	}
	
	@Override
	public void onSaveInstanceState(Bundle out) {
		out.putParcelableArrayList("SOLUTION", sol);
		out.putByteArray("CUBE_STATE", cubeState);
		out.putByteArray("COLORS", colorArray);
		out.putString("HID", hid);
		out.putInt("ANIM_POSITION", position);
		out.putByteArray("ANIM_STATE", cubeSurface.cubeState());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.solution, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.goHome:
			LoadCube.goHome(this);
			return true;
		case R.id.deleteSol:
			String where = String.format("%s='%s'", HistoryProvider.ID, hid);
			getContentResolver().delete(HistoryProvider.CONTENT_URI, where, null);
			LoadCube.goHome(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
}
