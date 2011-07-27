package com.droidtools.rubiksolver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.markupartist.android.widget.ActionBar;

public class Home extends Activity {
	Button mHistory;
	Button mStart;
	Button mAbout;
	Button mInstruc;
	private static final int DIALOG_HISTORY = 1;
	private static final int DIALOG_ABOUT = 2;
	java.util.ArrayList<ByteBuffer> historyDataSolution;
	java.util.ArrayList<ByteBuffer> historyDataCubeState;
	java.util.ArrayList<ByteBuffer> historyDataColors;
	java.util.ArrayList<Integer> historyId;
	DashboardLayout dbl;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboardlayout);
		makeActionBar(this);

		dbl = (DashboardLayout) findViewById(R.id.dashboard);
		
		/*
		dbl.addView(new DashboardIcon(this, "Instructions", R.drawable.dashinstructions));
		dbl.addView(new DashboardIcon(this, "Capture", R.drawable.dashcapture));
		dbl.addView(new DashboardIcon(this, "History", R.drawable.dashhistory));
		dbl.addView(new DashboardIcon(this, "About", R.drawable.dashabout));
		*/
		mHistory = (Button) findViewById(R.id.home_btn_history);
		mStart = (Button) findViewById(R.id.home_btn_capture);
		mAbout = (Button) findViewById(R.id.home_btn_about);
		mInstruc = (Button) findViewById(R.id.home_btn_instructions);

		mHistory.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_HISTORY);
			}
		});

		mStart.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent inten = new Intent(v.getContext(), LoadCube.class);
				inten.putExtra("face", "FRONT");
				startActivity(inten);
			}
		});
		
		mAbout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_ABOUT);
			}
		});
		
		mInstruc.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent inten = new Intent(v.getContext(), Instructions.class);
				startActivity(inten);
			}
		});
		
	}
	
	public static void makeActionBar(Activity act) {
		ActionBar actionBar = (ActionBar) act.findViewById(R.id.actionbar);
		actionBar.setTitle(R.string.actionTitle);
		Intent inten = new Intent(act, Home.class);
		inten.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		actionBar.setHomeAction(new ActionBar.IntentAction(act, inten, R.drawable.ic_title_home_default));
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.info, menu);
		return true;
	}*/

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.about:
			showDialog(DIALOG_ABOUT);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public static AlertDialog aboutCreate(Context context)
			throws NameNotFoundException {
		// Try to load the a package matching the name of our own package
		PackageInfo pInfo = context.getPackageManager().getPackageInfo(
				context.getPackageName(), PackageManager.GET_META_DATA);
		String versionInfo = pInfo.versionName;

		String aboutTitle = String.format("About %s",
				context.getString(R.string.app_name));
		String versionString = String.format("Version: %s", versionInfo);
		String aboutText = context.getString(R.string.aboutText);
		
		// Set up the TextView
		final TextView message = new TextView(context);
		// We'll use a spannablestring to be able to make links clickable
		final SpannableString s = new SpannableString(aboutText);

		// Set some padding
		message.setPadding(5, 5, 5, 5);
		// Set up the final string
		message.setText(versionString + "\n" + s);
		// Now linkify the text
		Linkify.addLinks(message, Linkify.ALL);

		return new AlertDialog.Builder(context)
				.setTitle(aboutTitle)
				.setCancelable(true)
				.setIcon(R.drawable.icon)
				.setPositiveButton(context.getString(android.R.string.ok), null)
				.setView(message).create();
	}

	private Dialog makeHistoryDialog() {
		/*
		 * //Context mContext = getApplicationContext(); Dialog dialog = new
		 * Dialog(this);
		 * 
		 * dialog.setContentView(R.layout.listdialog);
		 * dialog.getWindow().setLayout(LayoutParams.FILL_PARENT,
		 * LayoutParams.FILL_PARENT); dialog.setTitle("History");
		 * 
		 * ListView historyList = (ListView)
		 * dialog.findViewById(R.id.historyList);
		 * 
		 * historyList.setAdapter(new HistoryAdapter(a, null));
		 */
		// final CharSequence[] items = {"Red", "Green", "Blue", "Orange"};
		java.util.ArrayList<String> histories = new java.util.ArrayList<String>();
		historyDataSolution = new java.util.ArrayList<ByteBuffer>();
		historyDataCubeState = new java.util.ArrayList<ByteBuffer>();
		historyDataColors = new java.util.ArrayList<ByteBuffer>();
		historyId = new java.util.ArrayList<Integer>();	

		Cursor cursor = getContentResolver().query(HistoryProvider.CONTENT_URI,
				HistoryProvider.PROJECTION, null, null,
				HistoryProvider.DEFAULT_SORT_ORDER);

		if (cursor != null && cursor.getCount() != 0) {
			cursor.moveToFirst();
			do {
				historyId.add(cursor.getInt(cursor
						.getColumnIndexOrThrow(HistoryProvider.ID)));
				histories.add(cursor.getString(cursor
						.getColumnIndexOrThrow(HistoryProvider.NAME)));
				historyDataSolution.add(ByteBuffer.wrap(cursor.getBlob(cursor
						.getColumnIndexOrThrow(HistoryProvider.MOVES))));
				historyDataCubeState.add(ByteBuffer.wrap(cursor.getBlob(cursor
						.getColumnIndexOrThrow(HistoryProvider.STATE))));
				historyDataColors.add(ByteBuffer.wrap(cursor.getBlob(cursor
						.getColumnIndexOrThrow(HistoryProvider.COLORS))));
			} while (cursor.moveToNext());

		}
		String[] items = new String[histories.size()];
		histories.toArray(items);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("History");
		cursor.close();
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@SuppressWarnings("unchecked")
			public void onClick(DialogInterface dialog, int item) {
				ArrayList<RubikMove> result = null;
				Bundle b = new Bundle();
				byte[] data = historyDataSolution.get(item).array();
				ByteArrayInputStream bais = new ByteArrayInputStream(data);

				try {
					ObjectInputStream ois = new ObjectInputStream(bais);
					result = (ArrayList<RubikMove>) ois.readObject();
					ois.close();
					bais.close();
				} catch (OptionalDataException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				b.putParcelableArrayList("SOLUTION", result);
				//b.putParcelableArrayList("SOLUTION", RubikMove.fromNative(RubikCube.nativeSolve(RubikCube.getCubeState(historyDataCubeState.get(item).array()).nativeStringState())));
				b.putByteArray("CUBE_STATE", historyDataCubeState.get(item).array());
				b.putByteArray("COLORS", historyDataColors.get(item).array());
				b.putString("HID", Integer.toString(historyId.get(item)));
				Intent inten = new Intent(Home.this, Solution.class);
				inten.putExtras(b);
				startActivity(inten);
			}
		});
		return builder.create();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case DIALOG_HISTORY:
			dialog = makeHistoryDialog();
			break;
		case DIALOG_ABOUT:
			try {
				dialog = aboutCreate(this);
			} catch (NameNotFoundException e) {
			}
			break;
		default:
			dialog = null;
		}
		return dialog;
	}
}