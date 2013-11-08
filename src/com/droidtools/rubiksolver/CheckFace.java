package com.droidtools.rubiksolver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class CheckFace extends Activity {
	ListView mColorsList;
	GridView mColorsGrid;
	Button mNextFace;
	Button mTryAgain;
	String scanFace;
	int changeFacelet;
	ColorDecoder decoder;
	
	byte[] frontFace;
	byte[] backFace;
	byte[] leftFace;
	byte[] rightFace;
	byte[] upFace;
	byte[] downFace;
	
	/*List<Byte> color1;
	List<Byte> color2;
	List<Byte> color3;
	List<Byte> color4;
	List<Byte> color5;
	List<Byte> color6;*/
	List<List<Byte>> colorFix;
	List<Byte> ungroupedColors;
	ColorsAdapter ungroupAdapter;
	ColorsAdapter groupAdapter;
	Button nextSimilarButton;
	TextView similarText;
	
	int colorPage;

	private static final int DIALOG_CHECK = 0;
	private static final int DIALOG_INVALID_CUBE = 1;
	private static final int DIALOG_MORE_COLORS = 2;
	private static final int DIALOG_FIX_COLORS = 3;
	private static final int DIALOG_CHECK_COLORS = 4;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// if (LoadCube.decoder == null) {
		// LoadCube.goHome(this);
		// }
		setContentView(R.layout.checkpage);
		mColorsList = (ListView) findViewById(R.id.colorsList);
		mColorsGrid = (GridView) findViewById(R.id.colorsGrid);
		mNextFace = (Button) findViewById(R.id.nextFace);
		mTryAgain = (Button) findViewById(R.id.tryAgain);
		
		Home.makeActionBar(this);

		Bundle b = this.getIntent().getExtras();
		decoder = b.getParcelable("DECODER");
		if (decoder == null)
			LoadCube.goHome(this);
		loadFaces(b);

		scanFace = b.getString("SCAN_FACE");
		if (scanFace.equals("DOWN"))
			mNextFace.setText(R.string.solveCube);
		else
			mNextFace.setText(R.string.nextFace);

		mColorsList.setAdapter(new ColorsAdapter(decoder));
		mColorsGrid.setAdapter(new FaceAdapter(b.getByteArray("SCAN_RESULTS"), decoder));

		mColorsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				changeFacelet = position;
				showDialog(DIALOG_CHECK);
			}
		});

		mNextFace.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				nextFace();
			}
		});
		mTryAgain.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				tryAgain();
			}
		});
	}

	@Override
	protected  void onResume() {
		super.onResume();
		if (decoder == null || decoder.colorSize() == 0) {
			LoadCube.goHome(this);
		}
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

	Dialog editFaceDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		//builder.setTitle("Colors");
		//builder.setAdapter(arg0, arg1)
		SelectColorAdapter selectColorAdapter = new SelectColorAdapter(decoder);
		builder.setAdapter(selectColorAdapter, new SelectColorOnClickListener(selectColorAdapter) {
			
			@Override
			public void onClick(DialogInterface dialog, int item) {
				((FaceAdapter) mColorsGrid.getAdapter()).setItem(
						changeFacelet, (Byte) mAdapter.getItem(item));
				mColorsGrid.invalidate();
				dialog.dismiss();
				//mAdapter = null;
				//builder.
			}
		});
		AlertDialog alert = builder.create();
		return alert;
	}
	
	Dialog invalidCubeDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.invalidCube)
		       .setCancelable(false)
		       .setNeutralButton (R.string.goHome, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                LoadCube.goHome(CheckFace.this);
		           }
		       });
		AlertDialog alert = builder.create();
		return alert;
	}
	
	Dialog tooManyColorsDialog() {  
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.tooColors)
		       .setCancelable(false)
		       .setNeutralButton (R.string.goHome, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		return alert;
	}
	
	Dialog checkColorsDialog() {  
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.checkColors)
		       .setCancelable(false)
		       .setNeutralButton (R.string.goHome, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		return alert;
	}

	void tryAgain() {
		for (byte i = decoder.firstNewCol; decoder.hasId(i); i++) {
			decoder.removeColor(i);
		}
		decoder.nextId = (byte) (decoder.firstNewCol-1); 
		//decoder.clear();
		Intent inten = makeIntent(LoadCube.class, new Bundle());
		inten.putExtra("face", scanFace);
		startActivity(inten);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_CHECK:
			dialog = editFaceDialog();
			break;
		case DIALOG_INVALID_CUBE:
			dialog = invalidCubeDialog();
			break;
		case DIALOG_MORE_COLORS:
			dialog = tooManyColorsDialog();
			break;
		case DIALOG_CHECK_COLORS:
			dialog = checkColorsDialog();
			break;
		case DIALOG_FIX_COLORS:
			colorPage = 0;
			colorFix = new ArrayList<List<Byte>>();
			for (int i=0; i<6; i++) {
				colorFix.add(new ArrayList<Byte>());
			}
			ungroupedColors = new ArrayList<Byte>(decoder.getAllButFirstIds());
			colorFix.get(colorPage).add(decoder.getFirstId());
			
			groupAdapter = new ColorsAdapter(colorFix.get(colorPage), decoder);
			ungroupAdapter = new ColorsAdapter(ungroupedColors, decoder);
			
			AlertDialog.Builder builder;
			
			Context mContext = this;
			LayoutInflater inflater =
					(LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.similar, null);

			LinearLayout layoutRoot = (LinearLayout) layout.findViewById(R.id.layout_root);
			ListView ungroupedColorsLV = (ListView) layout.findViewById(R.id.ungroupedColors);
			ListView groupedColorsLV = (ListView) layout.findViewById(R.id.groupedColors);
			nextSimilarButton = (Button) layout.findViewById(R.id.nextSimilar);
			Button prevButton = (Button) layout.findViewById(R.id.prevSimilar);
			similarText = (TextView) layout.findViewById(R.id.similarFaceNo);
			
			ungroupedColorsLV.setAdapter(ungroupAdapter);
			groupedColorsLV.setAdapter(groupAdapter);
			
			ungroupedColorsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
					byte _id = ungroupedColors.get(position);
					ungroupedColors.remove(position);
					colorFix.get(colorPage).add(_id);
					ungroupAdapter.notifyDataSetChanged();
					groupAdapter.notifyDataSetChanged();
				}
			});
			groupedColorsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
					byte _id = colorFix.get(colorPage).get(position);
					colorFix.get(colorPage).remove(position);
					ungroupedColors.add(_id);
					ungroupAdapter.notifyDataSetChanged();
					groupAdapter.notifyDataSetChanged();
				}
			});
			
			prevButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (colorPage > 0) {
						if (colorPage == 5)
							nextSimilarButton.setText("Next");
						colorPage--;
						groupAdapter.setData(colorFix.get(colorPage));
						ungroupAdapter.notifyDataSetChanged();
						groupAdapter.notifyDataSetChanged();
						similarText.setText(String.format("Face %d/6", colorPage+1));
					}
				}
			});
			
			builder = new AlertDialog.Builder(mContext);
			builder.setView(layout);
			dialog = builder.create();
			
			//ViewGroup.LayoutParams lp = layoutRoot.getLayoutParams();
		    //lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
		    //lp.height = (int) (getWindowManager().getDefaultDisplay().getHeight() * .9);//WindowManager.LayoutParams.FILL_PARENT;
		    //layoutRoot.setLayoutParams(lp);
		    layoutRoot.setMinimumHeight((int) (getWindowManager().getDefaultDisplay().getHeight() * .98));
			
			nextSimilarButton.setOnClickListener(new DialogOnClickListener(dialog) {
				public void onClick(View v) {
					if (colorPage < 5) {
						colorPage++;
						if (colorFix.get(colorPage).size() == 0 && ungroupedColors.size() > 0) {
							byte _id = ungroupedColors.get(0);
							ungroupedColors.remove(0);
							colorFix.get(colorPage).add(_id);
						}
						groupAdapter.setData(colorFix.get(colorPage));
						ungroupAdapter.notifyDataSetChanged();
						groupAdapter.notifyDataSetChanged();
						similarText.setText(String.format(getResources().getString(R.string.colorPageFormat), colorPage+1));
						if (colorPage == 5)
							nextSimilarButton.setText("Done");
					}
					else {
						boolean allAssigned = true;
						for (int i=0; i<colorFix.size(); i++) {
							if (colorFix.get(i).size() == 0) {
								allAssigned = false;
								break;
							}
						}
						if (ungroupedColors.size() != 0 || !allAssigned) {
							showDialog(DIALOG_CHECK_COLORS);
						}
						else {
							byte[][] faces = {frontFace, backFace, leftFace, rightFace, upFace, downFace};
							for (int i=0; i<colorFix.size(); i++) {
								Collections.sort(colorFix.get(i));
							}
							for (int i=0; i<faces.length; i++) {
								byte[] face = faces[i];
								for (int j=0; j<face.length; j++) {
									for (int k=0; k<colorFix.size(); k++) {
										if (colorFix.get(k).contains(face[j])) {
											face[j] = colorFix.get(k).get(0);
											break;
										}
									}
								}
							}
							mDialog.dismiss();
							nextFace();
						}
					}
				}
			});
			break;
		default:
			dialog = null;
		}
		return dialog;
	}
	
	private List<Byte> toByteList(byte[] values)  {
	    //byte[] ret = new byte[list.size()];
	    List<Byte> ret = new ArrayList<Byte>();
		for (int i=0; i<values.length; i++) {
			ret.add(values[i]);
		}
	    return ret;
	}

	void nextFace() {
		/*if (true) {
			new GetSolution().execute(frontFace, backFace, leftFace, rightFace, upFace, downFace);
			return;
		}*/
		byte[] finalFace = ((FaceAdapter) mColorsGrid.getAdapter()).mData;
		
		// TODO(bbrown): This doesn't work because it makes a copy of the data, but I believe the
		// fix colors dialog relies on the reference to the data so it can manipulate it. Crazy!
		// byte[] finalFace = ((FaceAdapter) mColorsGrid.getAdapter()).getData();

		String face = null;
		Set<Byte> usedColors = new HashSet<Byte>();
		if (scanFace.equals("FRONT")) {
			frontFace = finalFace;
			face = "RIGHT";
		} else if (scanFace.equals("RIGHT")) {
			rightFace = finalFace;
			face = "BACK";
		} else if (scanFace.equals("BACK")) {
			backFace = finalFace;
			face = "LEFT";
		} else if (scanFace.equals("LEFT")) {
			leftFace = finalFace;
			face = "UP";
		} else if (scanFace.equals("UP")) {
			upFace = finalFace;
			face = "DOWN";
		} else if (scanFace.equals("DOWN")) {
			downFace = finalFace;
			face = null;
		}

		if (frontFace != null)
			usedColors.addAll(toByteList(frontFace));
		if (backFace != null)
			usedColors.addAll(toByteList(backFace));
		if (leftFace != null)
			usedColors.addAll(toByteList(leftFace));
		if (rightFace != null)
			usedColors.addAll(toByteList(rightFace));
		if (upFace != null)
			usedColors.addAll(toByteList(upFace));
		if (downFace != null)
			usedColors.addAll(toByteList(downFace));
		/*for (int i = decoder.colorSize() - 1; i >= 0; i--) {
			if (!usedColors.contains(i)) {
				decoder.removeColor(i);
			}
		}*/
		//for (Map.Entry<Integer, Parcelable[]> entry : decoder.entrySet()) {

		decoder.removeUnusedColors(usedColors);
		((ColorsAdapter) mColorsList.getAdapter()).notifyDataSetChanged();
		((FaceAdapter) mColorsGrid.getAdapter()).notifyDataSetChanged();
		//showDialog(DIALOG_FIX_COLORS);
		if (scanFace.equals("DOWN")) {
			if (decoder.colorSize() == 6) {
				// Disconnect the views from the adapters because they are backed by the decoder
				// which will be cleared during the solution calculation.
				mColorsList.setAdapter(null);
				mColorsGrid.setAdapter(null);
				new GetSolution().execute(frontFace, backFace, leftFace, rightFace, upFace, downFace);
			} else if (decoder.colorSize() > 6) { 
				showDialog(DIALOG_FIX_COLORS);
			} else {
				android.util.Log.d("SOLVER", String.format("Invalid colors size %d", decoder.colorSize()));
				showDialog(DIALOG_INVALID_CUBE);
			}
		} else {
			Intent inten = makeIntent(LoadCube.class, new Bundle());
			inten.putExtra("face", face);
			startActivity(inten);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		if (scanFace.equals("DOWN")) {
			inflater.inflate(R.menu.checkfacesolve, menu);
		} else {
			inflater.inflate(R.menu.checkface, menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.goHome:
			LoadCube.goHome(this);
			return true;
		case R.id.menuTryAgain:
			tryAgain();
			return true;
		case R.id.menuNextFace:
		case R.id.menuSolveCube:
			nextFace();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class GetSolution extends
			AsyncTask<byte[], Void, ArrayList<RubikMove>> {
		ProgressDialog pd;
		String historyId;
		byte[] cubeState;
		byte[] colorArray;

		@Override
		protected ArrayList<RubikMove> doInBackground(byte[]... faces) {
			
			
			RubikCube cube = new RubikCube();
			//RubikCube cube2 = new RubikCube();
			//RubikCube cube3 = new RubikCube();
			
			cube.cube.get("FRONT").setValues(faces[0]);
			cube.cube.get("BACK").setValues(faces[1]);
			cube.cube.get("LEFT").setValues(faces[2]);
			cube.cube.get("RIGHT").setValues(faces[3]);
			cube.cube.get("UP").setValues(faces[4]);
			cube.cube.get("DOWN").setValues(faces[5]);
			/*
			cube2.cube.get("FRONT").setValues(faces[0]);
			cube2.cube.get("BACK").setValues(faces[1]);
			cube2.cube.get("LEFT").setValues(faces[2]);
			cube2.cube.get("RIGHT").setValues(faces[3]);
			cube2.cube.get("UP").setValues(faces[4]);
			cube2.cube.get("DOWN").setValues(faces[5]);
			*/
			/*android.util.Log.d("CUBE", cube.cube.get("FRONT").toString());
			android.util.Log.d("CUBE", cube.cube.get("BACK").toString());
			android.util.Log.d("CUBE", cube.cube.get("LEFT").toString());
			android.util.Log.d("CUBE", cube.cube.get("RIGHT").toString());
			android.util.Log.d("CUBE", cube.cube.get("UP").toString());
			android.util.Log.d("CUBE", cube.cube.get("DOWN").toString());*/
			cubeState = cube.getCubeState().toByteArray();
			colorArray = decoder.colorArray();
			decoder.clear();
			/*
			
			if (!cube.solveCube(CheckFace.this)) {
				
				return null;
			}
			ArrayList<RubikMove> optMoveList = RubikCube.optomizeSolution(RubikCube.cubeStateOptimization(cube.cubeList, cube.moveList));
			*/
			
			String rc = cube.nativeSolve();
			if (rc.startsWith("Error:")) {
				return null;
			}
			ArrayList<RubikMove> optMoveList = RubikMove.fromNative(rc);
			
			/*if (cube2.executeMoves(optMoveList)) {
				cube2.writeCubeDebug(CheckFace.this);
				android.util.Log.d("SOLVE", "SOLVED CUBE SUPER OPT - " + optMoveList.size());
			} else {
				android.util.Log.d("SOLVE", "FAILED TO SOLVE CUBE SUPER OPT - " + optMoveList.size());
			}*/
			/*
			ArrayList<RubikMove> rms = RubikCube.optomizeSolution(cube.moveList);
			if (cube3.executeMoves(rms)) {
				android.util.Log.d("SOLVE", "SOLVED CUBE OPTIMIZED - " + rms.size());
			} else {
				android.util.Log.d("SOLVE", "FAILED TO SOLVE CUBE OPTIMIZED - " + rms.size());
			}*/
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//ArrayList<RubikMove> sol = new ArrayList<RubikMove>();
			try {
				ObjectOutputStream oos = new ObjectOutputStream(baos);

				//oos.writeObject(cube.moveList);
				oos.writeObject(optMoveList);
				oos.close();

				ContentValues cv = new ContentValues();
				cv.put(HistoryProvider.NAME, DateFormat.getDateTimeInstance().format(new Date(System
						.currentTimeMillis())));
				cv.put(HistoryProvider.MOVES, baos.toByteArray());
				cv.put(HistoryProvider.STATE, cubeState);
				cv.put(HistoryProvider.COLORS, colorArray);
				Uri result = CheckFace.this.getContentResolver().insert(HistoryProvider.CONTENT_URI, cv);
				historyId = result.getPathSegments().get(1);
				baos.close();
			} catch (IOException e) {
				android.util.Log.e("CheckFace", e.getMessage());
			} 

			Cursor cursor = getContentResolver().query(HistoryProvider.CONTENT_URI,
					HistoryProvider.PROJECTION, null, null,
					HistoryProvider.DEFAULT_SORT_ORDER);

			if (cursor.getCount() >= 6) {
				cursor.moveToFirst();
				String where = String.format("%s='%s'", HistoryProvider.ID, cursor.getInt(cursor
						.getColumnIndexOrThrow(HistoryProvider.ID)));
				getContentResolver().delete(HistoryProvider.CONTENT_URI, where, null);
			}
			cursor.close();
			return optMoveList;
		}

		@Override
		protected void onPreExecute() {
			pd = ProgressDialog.show(CheckFace.this, "", "Solving cube...",
					true);
		}

		@Override
		protected void onPostExecute(ArrayList<RubikMove> result) {
			pd.dismiss();
			if (result == null) {
				showDialog(DIALOG_INVALID_CUBE);
			}
			else {
				Bundle b = new Bundle();
				b.putParcelableArrayList("SOLUTION", result);
				b.putByteArray("CUBE_STATE", cubeState);
				b.putByteArray("COLORS", colorArray);
				b.putString("HID", historyId);
				Intent inten = new Intent(CheckFace.this, Solution.class);
				inten.putExtras(b);
				startActivity(inten);
			}
		}
	}
	
	private abstract class DialogOnClickListener implements OnClickListener
	{
		Dialog mDialog;
		DialogOnClickListener(Dialog d) {
			mDialog = d;
		}
	}
	
	private abstract class SelectColorOnClickListener implements DialogInterface.OnClickListener
	{
		SelectColorAdapter mAdapter;
		SelectColorOnClickListener(SelectColorAdapter sca) {
			mAdapter = sca;
		}
	}

}
