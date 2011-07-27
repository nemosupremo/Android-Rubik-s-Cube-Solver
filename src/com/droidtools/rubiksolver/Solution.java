package com.droidtools.rubiksolver;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

public class Solution extends Activity {
	GridView mSolution;
	Button stepSolutionButton;
	ArrayList<RubikMove> sol;
	String hid;
	Bundle bundle;
	byte[] cubeState;
	byte[] colorArray;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.solution);
		
		Home.makeActionBar(this);
		
		if (savedInstanceState != null)
			bundle = savedInstanceState;
		else
			bundle = this.getIntent().getExtras();
		sol = bundle.getParcelableArrayList("SOLUTION");
		cubeState = bundle.getByteArray("CUBE_STATE");
		colorArray = bundle.getByteArray("COLORS");
		hid = bundle.getString("HID");
		
		mSolution = (GridView)(findViewById(R.id.solutionGrid));
		mSolution.setAdapter(new SolutionAdapter(sol));
		mSolution
		.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				((SolutionAdapter) mSolution.getAdapter()).mData.get(position).setDone();
				((SolutionAdapter) mSolution.getAdapter()).notifyDataSetChanged();
			}

		});
		
		stepSolutionButton = (Button) findViewById(R.id.stepSolutionButton);
		stepSolutionButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent inten = new Intent(v.getContext(), StepSolution.class);
				Bundle b = new Bundle();
				b.putParcelableArrayList("SOLUTION", sol);
				b.putByteArray("CUBE_STATE", cubeState);
				b.putByteArray("COLORS", colorArray);
				b.putString("HID", hid);
				inten.putExtras(b);
				startActivity(inten);
			}
		});
		
		setTitle(String.format("Solution - %d Moves", sol.size()));
	}
	
	@Override
	public void onSaveInstanceState(Bundle out) {
		out.putParcelableArrayList("SOLUTION", sol);
		out.putByteArray("CUBE_STATE", cubeState);
		out.putByteArray("COLORS", colorArray);
		out.putString("HID", hid);
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
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	Intent inten = new Intent(this, Home.class);
	    	inten.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(inten);
	    }
	    return super.onKeyDown(keyCode, event);
	}

}
