package com.droidtools.rubiksolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RubikCube {
	Map<String, RubikFace> cube;
	boolean recording;
	ArrayList<RubikMove> moveList;
	//ArrayList<RubikMove> OptMoveList;
	private static final boolean RECORD_CUBE = false;
	//private static final boolean OPTOMIZE = false;
	private static final int FRONT = 0;
	private static final int BACK = 1;
	private static final int LEFT = 2;
	private static final int RIGHT = 3;
	private static final int UP = 4;
	private static final int DOWN = 5;
	ArrayList<CubeState> cubeList;
	ArrayList<int[][]> cubeListDebug;
	
	static {
		System.loadLibrary("colordecoder");
    }

	public RubikCube() {
		cube = new HashMap<String, RubikFace>();
		moveList = new ArrayList<RubikMove>();
		cubeList = new ArrayList<CubeState>();

		createCube();
	}

	private void createCube() {
		String[] faces = { "UP", "FRONT", "LEFT", "BACK", "RIGHT", "DOWN" };
		for (byte i = 0; i < faces.length; i++) {
			cube.put(faces[i], new RubikFace(faces[i], i, cube));
		}
	}
	
	CubeState getCubeState() {
		return new CubeState(getFaces(cube.get("FRONT")));
	}
	
	static CubeState getCubeState(byte[] data) {
		return new CubeState(data);
	}

	private void startRecord() {
		moveList.clear();
		if (RECORD_CUBE)
			cubeList.clear();
		recording = true;
	}

	private void endRecord() {
		recording = false;
	}
	
	protected static native String nativeSolve(int[] state);

	public String nativeSolve() {
		return nativeSolve(getCubeState().nativeStringState());
		
		
	}
	
	/*private int[][] getCubeDebug(RubikFace unUsed, RubikMove move) {
		RubikFace frontFace = cube.get("FRONT");
		final RubikFace faces[] = getFaces(cube.get("FRONT"));
		int[][] ret = {
				new int[] { faces[UP].get(frontFace, 0),
						faces[UP].get(frontFace, 1),
						faces[UP].get(frontFace, 2) },
				new int[] { faces[UP].get(frontFace, 3),
						faces[UP].get(frontFace, 4),
						faces[UP].get(frontFace, 5) },
				new int[] { faces[UP].get(frontFace, 6),
						faces[UP].get(frontFace, 7),
						faces[UP].get(frontFace, 8) },
				new int[] { faces[LEFT].get(frontFace, 0),
						faces[LEFT].get(frontFace, 1),
						faces[LEFT].get(frontFace, 2),
						faces[FRONT].get(frontFace, 0),
						faces[FRONT].get(frontFace, 1),
						faces[FRONT].get(frontFace, 2),
						faces[RIGHT].get(frontFace, 0),
						faces[RIGHT].get(frontFace, 1),
						faces[RIGHT].get(frontFace, 2),
						faces[BACK].get(frontFace, 0),
						faces[BACK].get(frontFace, 1),
						faces[BACK].get(frontFace, 2) },
				new int[] { faces[LEFT].get(frontFace, 3),
						faces[LEFT].get(frontFace, 4),
						faces[LEFT].get(frontFace, 5),
						faces[FRONT].get(frontFace, 3),
						faces[FRONT].get(frontFace, 4),
						faces[FRONT].get(frontFace, 5),
						faces[RIGHT].get(frontFace, 3),
						faces[RIGHT].get(frontFace, 4),
						faces[RIGHT].get(frontFace, 5),
						faces[BACK].get(frontFace, 3),
						faces[BACK].get(frontFace, 4),
						faces[BACK].get(frontFace, 5) },
				new int[] { faces[LEFT].get(frontFace, 6),
						faces[LEFT].get(frontFace, 7),
						faces[LEFT].get(frontFace, 8),
						faces[FRONT].get(frontFace, 6),
						faces[FRONT].get(frontFace, 7),
						faces[FRONT].get(frontFace, 8),
						faces[RIGHT].get(frontFace, 6),
						faces[RIGHT].get(frontFace, 7),
						faces[RIGHT].get(frontFace, 8),
						faces[BACK].get(frontFace, 6),
						faces[BACK].get(frontFace, 7),
						faces[BACK].get(frontFace, 8) },
				new int[] { faces[DOWN].get(frontFace, 0),
						faces[DOWN].get(frontFace, 1),
						faces[DOWN].get(frontFace, 2) },
				new int[] { faces[DOWN].get(frontFace, 3),
						faces[DOWN].get(frontFace, 4),
						faces[DOWN].get(frontFace, 5) },
				new int[] { faces[DOWN].get(frontFace, 6),
						faces[DOWN].get(frontFace, 7),
						faces[DOWN].get(frontFace, 8) },
				((move == null) ? null : move.getMoveRepInt()) };
		return ret;
	}
*/
	/*public void writeCubeDebug(android.content.Context context) {
		try {
			// open myfilename.txt for writing
			
			File dump = new File(android.os.Environment.getExternalStorageDirectory(), "rubik_dump.txt");
	        FileWriter dumpWriter = new FileWriter(dump);
	        BufferedWriter out = new BufferedWriter(dumpWriter);

			//  write the contents on mySettings to the file
			final int across = 8;
			final int numCubes = cubeListDebug.size();
			int excl = cubeListDebug.get(0).length;
			for (int i = 0; i < numCubes; i += across) {
				for (int k = 0; k < excl; k++) {
					for (int j = i; j < numCubes && j < (i + across); j++) {
						int[] line = cubeListDebug.get(j)[k];
						if (line.length == 3) {
							out.write(String.format("   %d%d%d         ",
									line[0], line[1], line[2]));
						} else if (line.length == 2) {
							out.write(String.format("    %c%c         ",
									line[0] == 0 ? ' ' : (char) line[0],
									line[1] == 0 ? ' ' : (char) line[1]));
						} else if (line.length == 12) {
							out.write(String.format(
									"%d%d%d%d%d%d%d%d%d%d%d%d   ", line[0],
									line[1], line[2], line[3], line[4],
									line[5], line[6], line[7], line[8],
									line[9], line[10], line[11]));
						}
					}
					out.write("\r\n");
				}
				out.write("\r\n");
			}
			// close the file
			out.close();
		} catch (java.io.IOException e) {
			//
		}
	}
	*/
	private RubikFace[] getFaces(RubikFace frontFace) {
		if (frontFace.equals(cube.get("BACK"))) {
			RubikFace r[] = { cube.get("BACK"), cube.get("FRONT"),
					cube.get("RIGHT"), cube.get("LEFT"), cube.get("UP"),
					cube.get("DOWN") };
			return r;
		} else if (frontFace.equals(cube.get("LEFT"))) {
			RubikFace r[] = { cube.get("LEFT"), cube.get("RIGHT"),
					cube.get("BACK"), cube.get("FRONT"), cube.get("UP"),
					cube.get("DOWN") };
			return r;
		} else if (frontFace.equals(cube.get("RIGHT"))) {
			RubikFace r[] = { cube.get("RIGHT"), cube.get("LEFT"),
					cube.get("FRONT"), cube.get("BACK"), cube.get("UP"),
					cube.get("DOWN") };
			return r;
		} else if (frontFace.equals(cube.get("UP"))) {
			RubikFace r[] = { cube.get("UP"), cube.get("DOWN"),
					cube.get("LEFT"), cube.get("RIGHT"), cube.get("BACK"),
					cube.get("FRONT") };
			return r;
		} else if (frontFace.equals(cube.get("DOWN"))) {
			RubikFace r[] = { cube.get("DOWN"), cube.get("UP"),
					cube.get("LEFT"), cube.get("RIGHT"), cube.get("FRONT"),
					cube.get("BACK") };
			return r;
		} else {
			RubikFace r[] = { cube.get("FRONT"), cube.get("BACK"),
					cube.get("LEFT"), cube.get("RIGHT"), cube.get("UP"),
					cube.get("DOWN") };
			return r;
		}
	}

	public void turn(RubikFace frontFace, boolean clockwise) {
		RubikFace faces[] = getFaces(frontFace);
		RubikMove rm = new RubikMove(frontFace.getNotName(), clockwise);
		if (recording) {
			moveList.add(rm);
			cubeList.add(new CubeState(getFaces(cube.get("FRONT"))));
			/*boolean f = OPTOMIZE && true;
			while (f) {
				f = false;
				int mLSize = moveList.size();
				if (mLSize > 1) {
					if (moveList.get(mLSize - 1).equals(
							moveList.get(mLSize - 2).getInverse())) {
						moveList.remove(mLSize - 1);
						moveList.remove(mLSize - 2);
						f = true;
					}
				}
				mLSize = moveList.size();
				if (mLSize > 2) {
					if (moveList.get(mLSize - 1).equals(
							moveList.get(mLSize - 2))
							&& moveList.get(mLSize - 2).equals(
									moveList.get(mLSize - 3))) {
						RubikMove r = moveList.get(mLSize - 1).getInverse();
						moveList.remove(mLSize - 1);
						moveList.remove(mLSize - 2);
						moveList.remove(mLSize - 3);
						moveList.add(r);
					}
				}

			}*/
		}
		byte[] lRep = faces[LEFT].listRep(faces[FRONT]);
		byte[] rRep = faces[RIGHT].listRep(faces[FRONT]);
		byte[] uRep = faces[UP].listRep(faces[FRONT]);
		byte[] dRep = faces[DOWN].listRep(faces[FRONT]);
		if (clockwise) {
			faces[FRONT].reOrder(faces[FRONT], RubikFace.map2);

			faces[LEFT].set(faces[FRONT], 2, dRep[0]);
			faces[LEFT].set(faces[FRONT], 5, dRep[1]);
			faces[LEFT].set(faces[FRONT], 8, dRep[2]);

			faces[DOWN].set(faces[FRONT], 0, rRep[6]);
			faces[DOWN].set(faces[FRONT], 1, rRep[3]);
			faces[DOWN].set(faces[FRONT], 2, rRep[0]);

			faces[RIGHT].set(faces[FRONT], 0, uRep[6]);
			faces[RIGHT].set(faces[FRONT], 3, uRep[7]);
			faces[RIGHT].set(faces[FRONT], 6, uRep[8]);

			faces[UP].set(faces[FRONT], 6, lRep[8]);
			faces[UP].set(faces[FRONT], 7, lRep[5]);
			faces[UP].set(faces[FRONT], 8, lRep[2]);
		} else {

			faces[FRONT].reOrder(faces[FRONT], RubikFace.map1);

			faces[LEFT].set(faces[FRONT], 2, uRep[8]);
			faces[LEFT].set(faces[FRONT], 5, uRep[7]);
			faces[LEFT].set(faces[FRONT], 8, uRep[6]);

			faces[DOWN].set(faces[FRONT], 0, lRep[2]);
			faces[DOWN].set(faces[FRONT], 1, lRep[5]);
			faces[DOWN].set(faces[FRONT], 2, lRep[8]);

			faces[RIGHT].set(faces[FRONT], 0, dRep[2]);
			faces[RIGHT].set(faces[FRONT], 3, dRep[1]);
			faces[RIGHT].set(faces[FRONT], 6, dRep[0]);

			faces[UP].set(faces[FRONT], 6, rRep[0]);
			faces[UP].set(faces[FRONT], 7, rRep[3]);
			faces[UP].set(faces[FRONT], 8, rRep[6]);
		}/*
		if (RECORD_CUBE && recording) {
			cubeList.add(getCubeDebug(frontFace, rm));
			boolean f = OPTOMIZE && true;
			while (f) {
				f = false;
				int mLSize = cubeList.size();
				if (mLSize > 1) {
					if ((new RubikMove(cubeList.get(mLSize - 1)[9])).equals(
							(new RubikMove(cubeList.get(mLSize - 2)[9])).getInverse())) {
						cubeList.remove(mLSize - 1);
						cubeList.remove(mLSize - 2);
						f = true;
					}
				}
				mLSize = cubeList.size();
				if (mLSize > 2) {
					if ((new RubikMove(cubeList.get(mLSize - 1)[9])).equals(
							(new RubikMove(cubeList.get(mLSize - 2)[9])))
							&& (new RubikMove(cubeList.get(mLSize - 2)[9])).equals(
									(new RubikMove(cubeList.get(mLSize - 3)[9])))) {
						RubikMove r = (new RubikMove(cubeList.get(mLSize - 1)[9])).getInverse();
						int[][] finalpos = cubeList.get(mLSize - 1);
						cubeList.remove(mLSize - 1);
						cubeList.remove(mLSize - 2);
						cubeList.remove(mLSize - 3);
						finalpos[9] = r.getMoveRepInt();
						cubeList.add(finalpos);
					}
				}

			}
		}*/
	}

	public void turnMiddle(RubikFace frontFace) {
		boolean clockwise = frontFace.equals(cube.get("LEFT"))
				|| frontFace.equals(cube.get("DOWN"))
				|| frontFace.equals(cube.get("FRONT"));
		char let = '?';
		if (frontFace.getNotName() == 'L'
				|| frontFace.getNotName() == 'R') {
			let = 'M';
		} else if (frontFace.getNotName() == 'U'
				|| frontFace.getNotName() == 'D') {
			let = 'E';
		} else if (frontFace.getNotName() == 'F'
				|| frontFace.getNotName() == 'B') {
			let = 'S';
		}
		RubikMove rm = new RubikMove(let, clockwise);
		if (recording) {
			moveList.add(rm);
			cubeList.add(new CubeState(getFaces(cube.get("FRONT"))));
			/*boolean f = OPTOMIZE && true;
			while (f) {
				f = false;
				int mLSize = moveList.size();
				if (mLSize > 1) {
					if (moveList.get(mLSize - 1).equals(
							moveList.get(mLSize - 2).getInverse())) {
						moveList.remove(mLSize - 1);
						moveList.remove(mLSize - 2);
						f = true;
					}
				}
				mLSize = moveList.size();
				if (mLSize > 2) {
					if (moveList.get(mLSize - 1).equals(
							moveList.get(mLSize - 2))
							&& moveList.get(mLSize - 2).equals(
									moveList.get(mLSize - 3))) {
						RubikMove r = moveList.get(mLSize - 1).getInverse();
						moveList.remove(mLSize - 1);
						moveList.remove(mLSize - 2);
						moveList.remove(mLSize - 3);
						moveList.add(r);
					}
				}

			}*/
		}
		RubikFace faces[] = getFaces(frontFace);
		byte[] up = { faces[UP].get(faces[FRONT], 3),
				faces[UP].get(faces[FRONT], 4), faces[UP].get(faces[FRONT], 5) };

		byte[] left = { faces[LEFT].get(faces[FRONT], 1),
				faces[LEFT].get(faces[FRONT], 4),
				faces[LEFT].get(faces[FRONT], 7) };

		byte[] down = { faces[DOWN].get(faces[FRONT], 3),
				faces[DOWN].get(faces[FRONT], 4),
				faces[DOWN].get(faces[FRONT], 5) };

		byte[] right = { faces[RIGHT].get(faces[FRONT], 1),
				faces[RIGHT].get(faces[FRONT], 4),
				faces[RIGHT].get(faces[FRONT], 7) };

		faces[UP].set(faces[FRONT], 3, left[2]);
		faces[UP].set(faces[FRONT], 4, left[1]);
		faces[UP].set(faces[FRONT], 5, left[0]);

		faces[LEFT].set(faces[FRONT], 1, down[0]);
		faces[LEFT].set(faces[FRONT], 4, down[1]);
		faces[LEFT].set(faces[FRONT], 7, down[2]);

		faces[DOWN].set(faces[FRONT], 3, right[2]);
		faces[DOWN].set(faces[FRONT], 4, right[1]);
		faces[DOWN].set(faces[FRONT], 5, right[0]);

		faces[RIGHT].set(faces[FRONT], 1, up[0]);
		faces[RIGHT].set(faces[FRONT], 4, up[1]);
		faces[RIGHT].set(faces[FRONT], 7, up[2]);
		/*if (RECORD_CUBE && recording) {
			cubeList.add(getCubeDebug(frontFace, rm));
			boolean f = OPTOMIZE && true;
			while (f) {
				f = false;
				int mLSize = cubeList.size();
				if (mLSize > 1) {
					if ((new RubikMove(cubeList.get(mLSize - 1)[9])).equals(
							(new RubikMove(cubeList.get(mLSize - 2)[9])).getInverse())) {
						cubeList.remove(mLSize - 1);
						cubeList.remove(mLSize - 2);
						f = true;
					}
				}
				mLSize = cubeList.size();
				if (mLSize > 2) {
					if ((new RubikMove(cubeList.get(mLSize - 1)[9])).equals(
							(new RubikMove(cubeList.get(mLSize - 2)[9])))
							&& (new RubikMove(cubeList.get(mLSize - 2)[9])).equals(
									(new RubikMove(cubeList.get(mLSize - 3)[9])))) {
						RubikMove r = (new RubikMove(cubeList.get(mLSize - 1)[9])).getInverse();
						int[][] finalpos = cubeList.get(mLSize - 1);
						cubeList.remove(mLSize - 1);
						cubeList.remove(mLSize - 2);
						cubeList.remove(mLSize - 3);
						finalpos[9] = r.getMoveRepInt();
						cubeList.add(finalpos);
					}
				}

			}
		}*/
	}

	public void randomize() {
		randomize(1000);
	}

	public void randomize(int turns) {
		RubikFace faces[] = getFaces(cube.get("FRONT"));
		for (int i = 0; i < turns; i++) {
			Random rg = new Random(System.currentTimeMillis());
			int c = rg.nextInt(1000000);
			int x = 9;
			while (x == 9)
				x = rg.nextInt(faces.length - 2);
			if (c > 500000)
				turn(faces[x], false);
			else
				turnMiddle(faces[x]);
		}
	}

	public boolean isSolved() {
		RubikFace faces[] = getFaces(cube.get("FRONT"));
		for (int i = 0; i < faces.length; i++) {
			if (!faces[i].isSolved())
				return false;
		}
		return true;
	}

	private RubikFace primeCube(RubikFace frontFace) {
		if (frontFace == null)
			frontFace = cube.get("FRONT");
		RubikFace faces[] = getFaces(frontFace);
		int i = 0;
		while (faces[UP].get(faces[FRONT], 8) != faces[UP].get(faces[FRONT], 4)
				&& i < 4) {
			turnMiddle(faces[FRONT]);
			i++;
		}
		if (faces[UP].get(faces[FRONT], 8) != faces[UP].get(faces[FRONT], 4)) {
			i = 0;
			while (faces[UP].get(faces[FRONT], 8) != faces[UP].get(
					faces[FRONT], 4) && i < 4) {
				turnMiddle(faces[LEFT]);
				i++;
			}
		}
		if (faces[UP].get(faces[FRONT], 8) == faces[UP].get(faces[FRONT], 4)) {
			return faces[FRONT];
		} else {
			return null;
		}
	}

	private boolean cornerHasColsBR(RubikFace frontFace, int[] needColors) {
		RubikFace faces[] = getFaces(frontFace);
		int[] colors = { faces[FRONT].get(faces[FRONT], 8),
				faces[RIGHT].get(faces[FRONT], 6),
				faces[DOWN].get(faces[FRONT], 2) };
		int cnt = 0;
		for (int i = 0; i < needColors.length; i++) {
			boolean has = false;
			for (int j = 0; j < colors.length; j++) {
				if (needColors[i] == colors[j]) {
					has = true;
					break;
				}
			}
			if (has)
				cnt++;
		}
		return cnt >= 2;
	}

	private boolean cornerHasColsTR(RubikFace frontFace, int[] needColors) {
		RubikFace faces[] = getFaces(frontFace);
		int[] colors = { faces[FRONT].get(faces[FRONT], 2),
				faces[RIGHT].get(faces[FRONT], 0),
				faces[UP].get(faces[FRONT], 8) };
		int cnt = 0;
		for (int i = 0; i < needColors.length; i++) {
			boolean has = false;
			for (int j = 0; j < colors.length; j++) {
				if (needColors[i] == colors[j]) {
					has = true;
					break;
				}
			}
			if (has)
				cnt++;
		}
		return cnt >= 2;
	}

	/*
	 * private boolean cornerHasColsTL(RubikFace frontFace, int[] needColors) {
	 * RubikFace faces[] = getFaces(frontFace); int[] colors = {
	 * faces[FRONT].get(faces[FRONT], 0), faces[LEFT].get(faces[FRONT], 2),
	 * faces[UP].get(faces[FRONT], 6) }; int cnt = 0; for (int i = 0; i <
	 * needColors.length; i++) { boolean has = false; for (int j = 0; j <
	 * colors.length; j++) { if (needColors[i] == colors[j]) { has = true;
	 * break; } } if (has) cnt++; } return cnt >= 2; }
	 */
	private boolean stepOneCompleteFace(RubikFace frontFace, int upColor) {
		RubikFace faces[] = getFaces(frontFace);
		int f0 = faces[FRONT].get(faces[FRONT], 0);
		int f1 = faces[FRONT].get(faces[FRONT], 2);
		int u0 = faces[UP].get(faces[FRONT], 6);
		int u1 = faces[UP].get(faces[FRONT], 8);
		return (u0 == upColor) && (u0 == u1) && (f0 == f1);
	}

	private RubikFace stepOne(RubikFace frontFace) {
		return stepOne(frontFace, true);
	}

	public String wrongWay() {
		RubikFace faces[] = { cube.get("UP"), cube.get("FRONT"),
				cube.get("LEFT"), cube.get("BACK"), cube.get("RIGHT"),
				cube.get("DOWN") };
		String r = "";
		for (int i = 0; i < faces.length; i++) {
			for (int j = 0; j < 9; j++) {
				r = r + (faces[i].get(cube.get("FRONT"), j) + 1);
			}
		}
		return r;
	}

	private RubikFace stepOne(RubikFace frontFace, boolean check) {
		int counter = 0;
		ArrayList<RubikFace> completedFaces = new ArrayList<RubikFace>();
		if (frontFace == null)
			frontFace = cube.get("FRONT");
		RubikFace faces[] = getFaces(frontFace);
		faces = getFaces(faces[RIGHT]);
		int upCol = faces[UP].get(faces[FRONT], 4);
		int skipCount = 0;
		while (counter < 20 && completedFaces.size() < 4) {
			if (stepOneCompleteFace(faces[FRONT], upCol)) {
				if (!completedFaces.contains(faces[FRONT]))
					completedFaces.add(faces[FRONT]);
				faces = getFaces(faces[RIGHT]);
				continue;
			}
			int[] needCols = { upCol, faces[FRONT].get(faces[FRONT], 0) };
			if (faces[FRONT].get(faces[FRONT], 2) == upCol
					&& cornerHasColsTR(faces[FRONT], needCols)) {
				turn(faces[FRONT], true);
				turn(faces[DOWN], true);
				turn(faces[FRONT], false);
				turn(faces[DOWN], true);
				turn(faces[DOWN], true);
				turn(faces[RIGHT], false);
				turn(faces[DOWN], true);
				turn(faces[RIGHT], true);
				if (stepOneCompleteFace(faces[FRONT], upCol))
					completedFaces.add(faces[FRONT]);
				else
					faces = getFaces(faces[RIGHT]);
			} else if (faces[RIGHT].get(faces[FRONT], 0) == upCol
					&& cornerHasColsTR(faces[FRONT], needCols)) {
				turn(faces[RIGHT], false);
				turn(faces[DOWN], false);
				turn(faces[RIGHT], true);
				turn(faces[DOWN], true);
				turn(faces[RIGHT], false);
				turn(faces[DOWN], false);
				turn(faces[RIGHT], true);
				if (stepOneCompleteFace(faces[FRONT], upCol))
					completedFaces.add(faces[FRONT]);
				else
					faces = getFaces(faces[RIGHT]);
			} else {
				boolean foundCorner = true;
				int i = 0;
				while (!cornerHasColsBR(faces[FRONT], needCols) && i < 4) {
					turn(faces[DOWN], true);
					i++;
				}
				if (i == 4 && !cornerHasColsBR(faces[FRONT], needCols)) {
					foundCorner = false;
				}
				if (foundCorner && faces[RIGHT].get(faces[FRONT], 6) == upCol) {
					turn(faces[RIGHT], false);
					turn(faces[DOWN], false);
					turn(faces[RIGHT], true);
					if (stepOneCompleteFace(faces[FRONT], upCol))
						completedFaces.add(faces[FRONT]);
					else
						faces = getFaces(faces[RIGHT]);
				} else if (foundCorner
						&& faces[FRONT].get(faces[FRONT], 8) == upCol) {
					turn(faces[DOWN], false);
					turn(faces[RIGHT], false);
					turn(faces[DOWN], true);
					turn(faces[RIGHT], true);
					if (stepOneCompleteFace(faces[FRONT], upCol))
						completedFaces.add(faces[FRONT]);
					else
						faces = getFaces(faces[RIGHT]);
				} else if (foundCorner
						&& faces[DOWN].get(faces[FRONT], 2) == upCol) {
					turn(faces[RIGHT], false);
					turn(faces[DOWN], true);
					turn(faces[RIGHT], true);
					turn(faces[DOWN], true);
					turn(faces[DOWN], true);
					turn(faces[RIGHT], false);
					turn(faces[DOWN], false);
					turn(faces[RIGHT], true);
					if (stepOneCompleteFace(faces[FRONT], upCol))
						completedFaces.add(faces[FRONT]);
					else
						faces = getFaces(faces[RIGHT]);
				} else {
					skipCount++;
					if (skipCount == 3) {
						turn(faces[RIGHT], true);
						turn(faces[DOWN], true);
						skipCount = 0;
					}
					faces = getFaces(faces[RIGHT]);
				}
			}
			counter++;
		}
		if (stepOneComplete(faces[FRONT]))
			return faces[FRONT];
		else if (check)
			return stepOne(frontFace, false);
		else
			return null;
	}

	private boolean stepOneComplete(RubikFace frontFace) {
		RubikFace[] faces = getFaces(frontFace);
		int midCol = faces[UP].get(faces[UP], 4);
		if (faces[UP].get(faces[UP], 0) != midCol)
			return false;
		if (faces[UP].get(faces[UP], 2) != midCol)
			return false;
		if (faces[UP].get(faces[UP], 6) != midCol)
			return false;
		if (faces[UP].get(faces[UP], 8) != midCol)
			return false;
		for (int i = 0; i < 4; i++) {
			if (faces[FRONT].get(faces[FRONT], 0) != faces[FRONT].get(
					faces[FRONT], 2))
				return false;
			faces = getFaces(faces[RIGHT]);
		}
		return true;
	}

	private boolean optimizeStepTwo(RubikFace frontFace, int upColor) {
		RubikFace[] faces = getFaces(frontFace);
		boolean r = false;
		for (int i = 0; i < 4; i++) {
			int faceCol = faces[FRONT].get(faces[FRONT], 0);
			while (faces[UP].get(faces[FRONT], 7) == upColor
					&& faces[FRONT].get(faces[FRONT], 1) != faceCol) {
				r = true;
				turnMiddle(faces[LEFT]);
				turn(faces[DOWN], true);
				turnMiddle(faces[RIGHT]);
			}
			faces = getFaces(faces[RIGHT]);
		}
		return r;
	}

	private RubikFace stepTwo(RubikFace frontFace) {
		RubikFace[] faces = getFaces(frontFace);
		assert (stepOneComplete(faces[FRONT]));
		int upCol = faces[UP].get(faces[FRONT], 4);
		// optimizeStepTwo(faces[FRONT], upCol);
		int i = 0;
		while (!(allFirstLayer(faces[FRONT]) && faces[UP].isSolved())
				&& i < 128) {
			for (int k = 0; k < 9; k++) {
				int faceCol = faces[FRONT].get(faces[FRONT], 0);
				if (faces[DOWN].get(faces[FRONT], 1) == upCol
						&& faces[FRONT].get(faces[FRONT], 7) == faceCol) {
					turnMiddle(faces[LEFT]);
					turn(faces[DOWN], false);
					turn(faces[DOWN], false);
					turnMiddle(faces[RIGHT]);
					i -= i % 4;
				}
				if (faces[FRONT].get(faces[FRONT], 7) == upCol
						&& faces[DOWN].get(faces[FRONT], 1) == faceCol) {
					turn(faces[DOWN], false);
					turnMiddle(faces[LEFT]);
					turn(faces[DOWN], true);
					turnMiddle(faces[RIGHT]);
					i -= i % 4;
				}
				if (faces[RIGHT].get(faces[FRONT], 3) == upCol
						&& faces[FRONT].get(faces[FRONT], 5) == faceCol) {
					turnMiddle(faces[DOWN]);
					turn(faces[FRONT], true);
					turnMiddle(faces[UP]);
					turn(faces[FRONT], false);
					i -= i % 4;
				}
				if (faces[FRONT].get(faces[FRONT], 5) == upCol
						&& faces[RIGHT].get(faces[FRONT], 3) == faceCol) {
					turnMiddle(faces[DOWN]);
					turn(faces[FRONT], false);
					turnMiddle(faces[UP]);
					turnMiddle(faces[UP]);
					turn(faces[FRONT], true);
					i -= i % 4;
				}
				if (faces[FRONT].get(faces[FRONT], 1) == upCol
						&& faces[UP].get(faces[FRONT], 7) == faceCol) {
					turnMiddle(faces[LEFT]);
					turn(faces[DOWN], false);
					turn(faces[DOWN], false);
					turnMiddle(faces[RIGHT]);
					turn(faces[DOWN], false);
					turnMiddle(faces[LEFT]);
					turn(faces[DOWN], true);
					turnMiddle(faces[RIGHT]);
					i -= i % 4;
				}
				if (k < 4)
					turnMiddle(faces[UP]);
				else
					turn(faces[DOWN], true);
			}
			i += 1;
			if (i % 4 == 0)
				optimizeStepTwo(faces[FRONT], upCol);
			faces = getFaces(faces[RIGHT]);

		}
		if (allFirstLayer(faces[FRONT]) && faces[UP].isSolved())
			return faces[FRONT];
		else
			return null;
	}

	private boolean firstLayer(RubikFace face) {
		byte[] lr = face.listRep(face);
		for (int i = 1; i < 3; i++) {
			if (lr[0] != lr[i])
				return false;
		}
		return true;
	}

	private boolean firstTwoLayers(RubikFace face) {
		byte[] lr = face.listRep(face);
		for (int i = 1; i < 6; i++) {
			if (lr[0] != lr[i])
				return false;
		}
		return true;
	}

	private boolean allFirstLayer(RubikFace frontFace) {
		RubikFace[] faces = getFaces(frontFace);
		for (int i = 0; i < 4; i++) {
			if (!firstLayer(faces[0]))
				return false;
			faces = getFaces(faces[RIGHT]);
		}
		return true;
	}

	private boolean allFirstTwoLayers(RubikFace frontFace) {
		RubikFace[] faces = getFaces(frontFace);
		for (int i = 0; i < 4; i++) {
			if (!firstTwoLayers(faces[0]))
				return false;
			faces = getFaces(faces[RIGHT]);
		}
		return true;
	}

	private RubikFace stepThree(RubikFace frontFace) {
		RubikFace[] faces = getFaces(frontFace);
		assert (allFirstLayer(frontFace) && faces[UP].isSolved());
		while (faces[FRONT].get(faces[FRONT], 1) != faces[FRONT].get(
				faces[FRONT], 4)) {
			turnMiddle(faces[UP]);
		}

		int i = 0;
		int j = 0;
		while (!allFirstTwoLayers(faces[FRONT]) && j < 64) {
			int faceCol = faces[FRONT].get(faces[FRONT], 4);
			int leftCol = faces[LEFT].get(faces[FRONT], 4);
			int rightCol = faces[RIGHT].get(faces[FRONT], 4);
			int tCol = faces[FRONT].get(faces[FRONT], 7);
			int dCol = faces[DOWN].get(faces[FRONT], 1);

			if (tCol == faceCol) {
				if (dCol == leftCol) {
					turn(faces[DOWN], true);
					turn(faces[LEFT], true);
					turn(faces[DOWN], false);
					turn(faces[LEFT], false);
					turn(faces[DOWN], false);
					turn(faces[FRONT], false);
					turn(faces[DOWN], true);
					turn(faces[FRONT], true);
					i -= i % 4;
				} else if (dCol == rightCol) {
					turn(faces[DOWN], false);
					turn(faces[RIGHT], false);
					turn(faces[DOWN], true);
					turn(faces[RIGHT], true);
					turn(faces[DOWN], true);
					turn(faces[FRONT], true);
					turn(faces[DOWN], false);
					turn(faces[FRONT], false);
					i -= i % 4;
				}
			}
			faces = getFaces(faces[RIGHT]);
			i += 1;
			if (i % 4 == 0 && !allFirstTwoLayers(faces[FRONT])) {
				j += 1;
				if (j % 4 == 0) {
					RubikFace[] f = getFaces(faces[FRONT]);
					for (int k = 0; k < 4; k++) {
						if (f[FRONT].get(f[FRONT], 3) != f[FRONT].get(f[FRONT],
								4)
								|| f[LEFT].get(f[FRONT], 5) != f[LEFT].get(
										f[FRONT], 4)) {
							turn(f[DOWN], true);
							turn(f[LEFT], true);
							turn(f[DOWN], false);
							turn(f[LEFT], false);
							turn(f[DOWN], false);
							turn(f[FRONT], false);
							turn(f[DOWN], true);
							turn(f[FRONT], true);
							break;
						} else if (f[FRONT].get(f[FRONT], 5) != f[FRONT].get(
								f[FRONT], 4)
								|| f[LEFT].get(f[FRONT], 3) != f[RIGHT].get(
										f[FRONT], 4)) {
							turn(f[DOWN], false);
							turn(f[RIGHT], false);
							turn(f[DOWN], true);
							turn(f[RIGHT], true);
							turn(f[DOWN], true);
							turn(f[FRONT], true);
							turn(f[DOWN], false);
							turn(f[FRONT], false);
							break;
						}
						f = getFaces(f[RIGHT]);
					}
				} else
					turn(faces[DOWN], true);
			}
		}
		return allFirstTwoLayers(faces[FRONT]) ? faces[FRONT] : null;
	}

	private void switch1and2(RubikFace frontFace) {
		RubikFace[] f = getFaces(frontFace);
		turn(f[LEFT], false);
		turn(f[UP], false);
		turn(f[LEFT], true);
		turn(f[FRONT], true);
		turn(f[UP], true);
		turn(f[FRONT], false);
		turn(f[LEFT], false);
		turn(f[UP], true);
		turn(f[LEFT], true);
		turn(f[UP], true);
		turn(f[UP], true);
	}

	private void switch1and3(RubikFace frontFace) {
		RubikFace[] f = getFaces(frontFace);
		turn(f[UP], true);
		turn(f[LEFT], false);
		turn(f[UP], false);
		turn(f[LEFT], true);
		turn(f[FRONT], true);
		turn(f[UP], true);
		turn(f[FRONT], false);
		turn(f[LEFT], false);
		turn(f[UP], true);
		turn(f[LEFT], true);
		turn(f[UP], true);
	}

	private int[] colorPos1(RubikFace frontFace) {
		RubikFace[] f = getFaces(frontFace);
		int[] r = { f[FRONT].get(f[FRONT], 2), f[UP].get(f[FRONT], 8),
				f[RIGHT].get(f[FRONT], 0) };
		return r;
	}

	private int[] colorPos2(RubikFace frontFace) {
		RubikFace[] f = getFaces(frontFace);
		int[] r = { f[FRONT].get(f[FRONT], 0), f[UP].get(f[FRONT], 6),
				f[LEFT].get(f[FRONT], 2) };
		return r;
	}

	private int[] colorPos3(RubikFace frontFace) {
		RubikFace[] f = getFaces(frontFace);
		int[] r = { f[RIGHT].get(f[FRONT], 2), f[UP].get(f[FRONT], 2),
				f[BACK].get(f[FRONT], 0) };
		return r;
	}

	private int[] colorPos4(RubikFace frontFace) {
		RubikFace[] f = getFaces(frontFace);
		int[] r = { f[BACK].get(f[FRONT], 2), f[UP].get(f[FRONT], 0),
				f[LEFT].get(f[FRONT], 0) };
		return r;
	}

	private boolean sameColors(int[] one, int[] two) {
		boolean has;
		for (int i = 0; i < one.length; i++) {
			has = false;
			for (int j = 0; j < two.length; j++) {
				if (one[i] == two[j]) {
					has = true;
					break;
				}
			}
			if (!has)
				return false;
		}
		return true;
	}

	private RubikFace stepFour(RubikFace frontFace) {
		RubikFace[] faces = getFaces(frontFace);
		turn(faces[FRONT], true);
		turn(faces[FRONT], true);
		turnMiddle(faces[FRONT]);
		turnMiddle(faces[FRONT]);
		turn(faces[BACK], false);
		turn(faces[BACK], false);

		// int upCol = faces[UP].get(faces[FRONT], 4);
		// int[] needColors = {upCol, faces[FRONT].get(faces[FRONT], 4)};

		int[][] order = {
				{ faces[FRONT].get(faces[FRONT], 4),
						faces[RIGHT].get(faces[FRONT], 4),
						faces[UP].get(faces[FRONT], 4) },
				{ faces[FRONT].get(faces[FRONT], 4),
						faces[LEFT].get(faces[FRONT], 4),
						faces[UP].get(faces[FRONT], 4) },
				{ faces[BACK].get(faces[FRONT], 4),
						faces[RIGHT].get(faces[FRONT], 4),
						faces[UP].get(faces[FRONT], 4) },
				{ faces[BACK].get(faces[FRONT], 4),
						faces[LEFT].get(faces[FRONT], 4),
						faces[UP].get(faces[FRONT], 4) } };

		while (!sameColors(colorPos4(faces[FRONT]), order[3])) {
			turn(faces[UP], true);
		}

		int[] cube1 = colorPos1(faces[FRONT]);
		// int[] cube4 = colorPos4(faces[FRONT]);
		// int pos = 3;

		if (!sameColors(order[0], cube1)) {
			if (sameColors(colorPos2(faces[FRONT]), order[0])) {
				switch1and2(faces[FRONT]);
			} else if (sameColors(colorPos3(faces[FRONT]), order[0])) {
				switch1and3(faces[FRONT]);
			}
		}

		if (!sameColors(order[1], colorPos2(faces[FRONT]))) {
			switch1and3(faces[FRONT]);
			switch1and2(faces[FRONT]);
			switch1and3(faces[FRONT]);
		}

		while (!sameColors(colorPos1(faces[FRONT]), order[0])) {
			turn(faces[UP], true);
		}

		return faces[FRONT];
	}

	private boolean modelOne(RubikFace frontFace) {
		RubikFace[] faces = getFaces(frontFace);
		return faces[UP].get(faces[FRONT], 4) == faces[UP].get(faces[FRONT], 8)
				&& faces[UP].get(faces[FRONT], 4) == faces[FRONT].get(
						faces[FRONT], 0);
	}

	private boolean modelTwo(RubikFace frontFace) {
		RubikFace[] faces = getFaces(frontFace);
		return faces[UP].get(faces[FRONT], 4) == faces[RIGHT].get(faces[FRONT],
				0)
				&& faces[UP].get(faces[FRONT], 4) == faces[RIGHT].get(
						faces[FRONT], 2);
	}

	private boolean modelThree(RubikFace frontFace) {
		RubikFace[] faces = getFaces(frontFace);
		return faces[UP].get(faces[FRONT], 4) == faces[UP].get(faces[FRONT], 8)
				&& faces[UP].get(faces[FRONT], 4) == faces[RIGHT].get(
						faces[FRONT], 2);
	}

	private void stepFiveAlgo(RubikFace frontFace) {
		RubikFace[] faces = getFaces(frontFace);
		turn(faces[LEFT], false);
		turn(faces[UP], false);
		turn(faces[LEFT], true);
		turn(faces[UP], false);
		turn(faces[LEFT], false);
		turn(faces[UP], false);
		turn(faces[UP], false);
		turn(faces[LEFT], true);
		turn(faces[UP], false);
		turn(faces[UP], false);
	}

	private boolean upCornersDone(RubikFace frontFace) {
		RubikFace[] faces = getFaces(frontFace);
		int a = faces[UP].get(faces[FRONT], 0);
		int b = faces[UP].get(faces[FRONT], 2);
		int c = faces[UP].get(faces[FRONT], 4);
		int d = faces[UP].get(faces[FRONT], 6);
		int e = faces[UP].get(faces[FRONT], 8);
		return (a == b) && (a == c) && (a == d) && (a == e);
	}

	private RubikFace stepFive(RubikFace frontFace) {
		RubikFace[] faces = getFaces(frontFace);
		/*
		 * boolean f = true; while (true) { if (modelOne(faces[FRONT]) ||
		 * modelTwo(faces[FRONT]) || (modelThree(faces[FRONT]))) { break; } else
		 * { if (f) { stepFiveAlgo(faces[FRONT]); f = false; } faces =
		 * getFaces(faces[RIGHT]); } } stepFiveAlgo(faces[FRONT]);
		 */
		while (!upCornersDone(faces[FRONT])) {
			if (modelOne(faces[FRONT]) || modelTwo(faces[FRONT])
					|| (modelThree(faces[FRONT]))) {
				stepFiveAlgo(faces[FRONT]);
			} else {
				faces = getFaces(faces[RIGHT]);
			}
		}
		return faces[FRONT];
	}

	private void sixAlgo(RubikFace frontFace) {
		RubikFace[] faces = getFaces(frontFace);
		turnMiddle(faces[RIGHT]);
		turn(faces[UP], false);
		turnMiddle(faces[LEFT]);
		turn(faces[UP], false);
		turn(faces[UP], false);
		turnMiddle(faces[RIGHT]);
		turn(faces[UP], false);
		turnMiddle(faces[LEFT]);
	}

	private void dedmoreH(RubikFace frontFace) {
		RubikFace[] faces = getFaces(frontFace);
		turn(faces[RIGHT], false);
		turnMiddle(faces[UP]);
		turn(faces[RIGHT], false);
		turn(faces[RIGHT], false);
		turnMiddle(faces[UP]);
		turnMiddle(faces[UP]);
		turn(faces[RIGHT], false);
		turn(faces[UP], false);
		turn(faces[UP], false);
		turn(faces[RIGHT], true);
		turnMiddle(faces[DOWN]);
		turnMiddle(faces[DOWN]);
		turn(faces[RIGHT], false);
		turn(faces[RIGHT], false);
		turnMiddle(faces[DOWN]);
		turn(faces[RIGHT], true);
		turn(faces[UP], false);
		turn(faces[UP], false);

	}

	private void dedmoreFish(RubikFace frontFace) {
		RubikFace[] faces = getFaces(frontFace);
		turn(faces[FRONT], false);
		turn(faces[LEFT], false);
		turn(faces[RIGHT], false);
		turnMiddle(faces[UP]);
		turn(faces[RIGHT], false);
		turn(faces[RIGHT], false);
		turnMiddle(faces[UP]);
		turnMiddle(faces[UP]);
		turn(faces[RIGHT], false);
		turn(faces[UP], false);
		turn(faces[UP], false);
		turn(faces[RIGHT], true);
		turnMiddle(faces[DOWN]);
		turnMiddle(faces[DOWN]);
		turn(faces[RIGHT], false);
		turn(faces[RIGHT], false);

		turnMiddle(faces[DOWN]);
		turn(faces[RIGHT], true);
		turn(faces[UP], false);
		turn(faces[UP], false);
		turn(faces[LEFT], true);
		turn(faces[FRONT], true);
	}

	private boolean isFlipped(RubikFace frontFace) {
		RubikFace[] faces = getFaces(frontFace);
		return faces[FRONT].get(faces[FRONT], 1) == faces[UP].get(faces[FRONT],
				4)
				&& faces[FRONT].get(faces[FRONT], 4) == faces[UP].get(
						faces[FRONT], 7);
	}

	private boolean hasPattern(RubikFace frontFace) {
		RubikFace[] faces = getFaces(frontFace);
		if (isFlipped(faces[FRONT]) && isFlipped(faces[RIGHT])
				&& isFlipped(faces[BACK]) && isFlipped(faces[LEFT])) {
			return true;
		}
		for (int i = 0; i < 4; i++) {
			if (isFlipped(faces[LEFT]) && isFlipped(faces[RIGHT])) {
				return true;
			} else if (isFlipped(faces[FRONT]) && isFlipped(faces[RIGHT])) {
				return true;
			}
			faces = getFaces(faces[RIGHT]);
		}
		return false;
	}

	private boolean stepSix(RubikFace frontFace) {
		RubikFace[] faces = getFaces(frontFace);
		int j = 0;
		boolean p = true;
		if (!(isFlipped(faces[FRONT]) || isFlipped(faces[RIGHT])
				|| isFlipped(faces[BACK]) || isFlipped(faces[LEFT]))) {
			sixAlgo(faces[FRONT]);
		}
		while (!hasPattern(faces[FRONT])) {
			if ((faces[FRONT].get(faces[FRONT], 1) == faces[UP].get(
					faces[FRONT], 4) && faces[FRONT].get(faces[FRONT], 4) == faces[UP]
					.get(faces[FRONT], 7))
					|| !p) {
				sixAlgo(faces[FRONT]);
				p = false;
			} else if (!(isFlipped(faces[FRONT]) || isFlipped(faces[RIGHT])
					|| isFlipped(faces[BACK]) || isFlipped(faces[LEFT]))) {
				sixAlgo(faces[FRONT]);
			}
			if (p) {
				faces = getFaces(faces[RIGHT]);
				j += 1;
				if ((j % 4) == 0) {
					sixAlgo(faces[FRONT]);
					dedmoreH(faces[FRONT]);
				}
			}
		}

		if (isFlipped(faces[FRONT]) && isFlipped(faces[RIGHT])
				&& isFlipped(faces[BACK]) && isFlipped(faces[LEFT])) {
			dedmoreH(faces[FRONT]);
		}
		for (int i = 0; i < 4; i++) {
			if (isFlipped(faces[LEFT]) && isFlipped(faces[RIGHT])) {
				dedmoreH(faces[FRONT]);
			} else if (isFlipped(faces[FRONT]) && isFlipped(faces[RIGHT])) {
				dedmoreFish(faces[FRONT]);
			}
			faces = getFaces(faces[RIGHT]);
		}
		return isSolved();
	}

	public boolean solveCube() {
		startRecord();
		RubikFace[] faces = getFaces(cube.get("FRONT"));
		RubikFace res;
		for (int i = 0; i < 50; i++) {
			if ((i + 1) % 3 == 0)
				randomize(3);
			if (isSolved())
				break;
			res = primeCube(faces[i % faces.length]);
			if (res == null)
				continue;
			if (isSolved())
				break;
			res = stepOne(res);
			if (res == null)
				continue;
			if (isSolved())
				break;
			res = stepTwo(res);
			if (res == null)
				continue;
			if (isSolved())
				break;
			res = stepThree(res);
			if (res == null)
				continue;
			if (isSolved())
				break;
			res = stepFour(res);
			if (res == null)
				continue;
			if (isSolved())
				break;
			res = stepFive(res);
			if (res == null)
				continue;
			if (isSolved())
				break;
			if (stepSix(res))
				break;
		}
		endRecord();
		//android.util.Log.d("OPTIM", "Complex Dupes - "+cubeStateOptimization(cubeList, moveList));
		//OptMoveList = optomizeSolution(moveList);
		//if (RECORD_CUBE)
		//	writeCubeDebug(ctx);
		return isSolved();
	}
	
	/*private boolean executeMoves(ArrayList<RubikMove> moves) {
		RubikFace[] faces = getFaces(cube.get("FRONT"));
		RubikFace face = null;
		cubeListDebug = new ArrayList<int[][]>();
		for (RubikMove move : moves) {
			if (move.getMove() == 'M' || move.getMove() == 'E' || move.getMove() == 'S') {
				if (move.getClockwise()) {
					if (move.getMove() == 'M') {
						face = faces[LEFT];
					} else if (move.getMove() == 'E') {
						face = faces[DOWN];
					} else {
						face = faces[BACK];
					}
				} else {
					if (move.getMove() == 'M') {
						face = faces[RIGHT];
					} else if (move.getMove() == 'E') {
						face = faces[UP];
					} else {
						face = faces[FRONT];
					}
				}
				turnMiddle(face);
			}
			else {
				switch (move.getMove()) {
					case 'F':
						face = faces[FRONT];
						break;
					case 'B':
						face = faces[BACK];
						break;
					case 'L':
						face = faces[LEFT];
						break;
					case 'R':
						face = faces[RIGHT];
						break;
					case 'U':
						face = faces[UP];
						break;
					case 'D':
						face = faces[DOWN];
						break;
				}
				turn(face, move.getClockwise());
			}
			cubeListDebug.add(getCubeDebug(null, move));
		}
		return isSolved();
		
	}*/
	
	/*private static String get5(int i, ArrayList<RubikMove> moves) {
		String ret ="";
		for (int j=Math.max(0, i-5); j<Math.min(moves.size(), i+5); j++) {
			ret += moves.get(j).getMoveRep()+",";
		}
		return ret;
	}*/
	
	public static ArrayList<RubikMove> cubeStateOptimization(ArrayList<CubeState> cubeList, ArrayList<RubikMove> moves) {
		int cubeListSz = cubeList.size();
		ArrayList<CubeState> cubeListCopy = new ArrayList<CubeState>(cubeList);
		ArrayList<RubikMove> sol = new ArrayList<RubikMove>(moves);
		Collections.sort(cubeListCopy);
		Map<String, Integer> dupes = new HashMap<String, Integer>();
		/*for (int i=0; i<cubeListSz-1; i++) {
			android.util.Log.d("OPTIM", cubeListCopy.get(i).toString());
		}*/
		//android.util.Log.d("OPTIM", String.format("cubeList - %d | movesList = %d", cubeList.size(), sol.size()));
		for (int i=0; i<cubeListSz-1; i++) {
			if (cubeListCopy.get(i).equals(cubeListCopy.get(i+1))) {
				String key = cubeListCopy.get(i).toString();
				int newValue = (dupes.containsKey(key)) ? dupes.get(key)+1 : 1;
				dupes.put(key, newValue);
			}
		}
		//File dump = new File(android.os.Environment.getExternalStorageDirectory(), "rubik_dump.txt");
        //FileWriter dumpWriter = new FileWriter(dump);
        //BufferedWriter out = new BufferedWriter(dumpWriter);
		//ArrayList<int[]> remove = new ArrayList<int[]>();
		for (int i=cubeListSz-1; i>=1; i--) {
			String key = cubeList.get(i).toString();
			//out.write(String.format("%s -> %s\r\n", cubeList.get(i).toString(), sol.get(i).toString()));
			if (dupes.containsKey(key)) {
				for (int j=0; j<i; j++) {
					//int z = j/(j-i);
					String key2 = cubeList.get(j).toString();
					if (key.equals(key2)) {
						//ArrayList<CubeState> x = new ArrayList<CubeState>();
						//ArrayList<RubikMove> y = new ArrayList<RubikMove>();
						for (int s=i; s>j; s--) {
							cubeList.remove(s);
							//x.add(cubeList.get(s));
						}
						for (int s=i-1; s>=j; s--) {
							sol.remove(s);
							//y.add(sol.get(s));
						}
						//for (int k=0; k<x.size(); k++) 
						//	out.write(String.format("// %s -> %s\r\n", x.get(k).toString(), y.get(k).toString()));
						i = j;
						dupes.remove(key);
						break;
					}
				}
				
			}
		}
		//out.close();
		//android.util.Log.d("OPTIM", String.format("cubeList - %d | movesList = %d", cubeList.size(), sol.size()));
		return sol;
		/*for (int i=cubeListSz-1; i>=1; i--) {
			
			
		}*/
		
		//return numDupes;
	}
	
	public static ArrayList<RubikMove> optomizeSolution(ArrayList<RubikMove> moves) {
		return optomizeSolution(moves, 10, 0);
	}
	
	private static ArrayList<RubikMove> optomizeSolution(ArrayList<RubikMove> moves, int depth, int dupes) {
		ArrayList<RubikMove> sol = new ArrayList<RubikMove>(moves);
		ArrayList<Integer> remove = new ArrayList<Integer>();
		boolean found = true;
		boolean changesMade = false;
		while (found) {
			found = false;
			int mvSz = sol.size();
			remove.clear();
			for (int i=mvSz-1; i>=3; i--) {
				RubikMove move = sol.get(i);
				if (move.equals(sol.get(i-1)) &&
						move.equals(sol.get(i-2)) &&
						move.equals(sol.get(i-3))) {
					remove.add(i);
					i -= 4;
					found = true;
					dupes++;
				}
			}
			for (Integer i : remove) {
				changesMade = true;
				sol.remove((int)i);
				sol.remove(i-1);
				sol.remove(i-2);
				sol.remove(i-3);
			}
		}
		found = true;
		while (found) {
			
			found = false;
			remove.clear();
			int mvSz = sol.size();
			//android.util.Log.d("HELLO", "solSize = "+mvSz);
			for (int i=mvSz-1; i>=2; i--) {
				RubikMove move = sol.get(i);
				if (move.equals(sol.get(i-1)) &&
						move.equals(sol.get(i-2))) {
					//android.util.Log.d("HELLO", "Possible optomizaion");
					remove.add(i);
					i -= 3;
					found = true;
				}
			}
			//if ( (new RubikMove('F', false) ).equals(new RubikMove('F', false) ) ) {
			//android.util.Log.d("HELLO", "removes = "+remove.size());
			//}
			for (Integer i : remove) {
				changesMade = true;
				RubikMove inv = sol.get((int)i).getInverse();
				sol.remove((int)i);
				sol.remove(i-1);
				sol.remove(i-2);
				sol.add(i-2, inv);
			}
		}
		found = true;
		while (found) {
			found = false;
			remove.clear();
			int mvSz = sol.size();
			for (int i=mvSz-1; i>=1; i--) {
				RubikMove move = sol.get(i);
				if (move.getInverse().equals(sol.get(i-1))) {
					remove.add(i);
					i -= 2;
					found = true;
					dupes++;
				}
			}
			for (Integer i : remove) {
				changesMade = true;
				//android.util.Log.d("OPTIM", String.format("%d Removing %s and %s", sol.size(), sol.get(i), sol.get(i-1)));
				//android.util.Log.d("OPTIM", get5(i, sol));
				sol.remove((int)i);
				//android.util.Log.d("OPTIM", String.format("Removing %s and %s", sol.get(i), sol.get(i-1)));
				sol.remove(i-1);
				//sol.
				//android.util.Log.d("OPTIM",  sol.size()+" removed");
				//android.util.Log.d("OPTIM", get5(i, sol));
			}
		}
		if (changesMade && depth > 0)
			return optomizeSolution(sol, depth-1, dupes);
		else {
			//android.util.Log.d("OPTIM", "Simple Dupes - "+dupes);
			return sol;
		}
	}
	
	protected static class CubeState implements Comparable<CubeState> {
		private byte[] state;
		private String stringState;

		CubeState(RubikFace[] faces) {
			stringState = null;
			byte[] data = new byte[54];
			for (int i=0; i<faces.length; i++) {
				System.arraycopy(faces[i].structure(), 0, data, i*9, 9);
			}
			state = data;
			toString();
			//MessageDigest digest;
			//try {
			//	digest = MessageDigest.getInstance("SHA-1");
			//	digest.reset();
			//	state = digest.digest(data);
			//} catch (NoSuchAlgorithmException e) {
				
			//}
			
			//return input;

		}

		CubeState(byte[] data) {
			if (data.length != 54) {
				throw new IllegalArgumentException("Invalid cube state.");
			}
			state = data;
			toString();
		}
		
		public int[] nativeStringState()
		{
			Map<Byte, Integer> sm = new HashMap<Byte, Integer>();
			//StringBuilder ret = new StringBuilder();
			int ret[] = new int[54];
			// front, back, left, right, up,down
			int[] order = {4, 2, 0, 3, 1, 5};
			int q = 1;
			int numrep;
			int k = 0;
			for (int i=0; i<order.length; i++) {
				for (int j=0; j<9; j++) {
					byte col = state[9*order[i]+j];
					if (sm.containsKey(col)) {
						numrep = sm.get(col);
					}
					else {
						sm.put(col, q);
						numrep = q;
						q++;
					}
					ret[k] = numrep;
					k++;
				}
			}
			return ret;
		}
		
		public String toString() {
			if (stringState == null) {
				Formatter formatter = new Formatter();
		        for (byte b : state) {
		            formatter.format("%02x", b);
		        }
		        stringState = formatter.toString();
			}
			return stringState;
		}
		
		public byte[] toByteArray() {
			return state;
		}
		
		public byte[] toAnimCubeState() {
			byte[] ret = new byte[54];
			for (int i=0; i<ret.length; i++) {
				if (i < 9) {
					ret[i] = state[36+(i+(-6*(((i/3) % 3)-1)))];
				} else if (i < 18) {
					ret[i] = state[45+(i*3 % 9 + ((i-9)/3%3))];
				} else if (i < 27) {
					ret[i] = state[(i*3 % 9 + ((i-18)/3%3))];
				} else if (i < 36) {
					ret[i] = state[9+(i*3 % 9 + ((i-27)/3%3))];
				} else if (i < 45) {
					ret[i] = state[18+((9-(i-2)%9) % 3 + 3*((i/3)%6))];
				} else if (i < 54) {
					ret[i] = state[27+(i*3 % 9 + ((i-45)/3%3))];
				}
			} 
			return ret;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final CubeState other = (CubeState) obj;
			/*if (other.state.length != state.length) return false;
			for (int i=0; i<state.length; i++) {
				if (state[i] != other.state[i])
					return false;
			}
			return true;*/
			return toString().equals(other.toString());
		}

		@Override
		public int compareTo(CubeState another) {
			/*int minl = Math.min(state.length, another.state.length);
			for (int i=0; i<minl; i++) {
				if (state[i] != another.state[i]) {
					return state[i] - another.state[i];
				}
			}
			return state.length - another.state.length;*/
			return toString().compareTo(another.toString());
		}
	}
	
}
