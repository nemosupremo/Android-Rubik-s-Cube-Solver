package com.droidtools.rubiksolver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.os.Parcel;
import android.os.Parcelable;

public class RubikMove implements Parcelable, Serializable {

	private static final long serialVersionUID = 2022509185913618271L;
	char move;
	private boolean done = false;
	boolean clockwise;

	public static Map<String, Integer> icons = new HashMap<String, Integer>();

	static {
		icons.put("F", R.drawable.f);
		icons.put("F'", R.drawable.fp);
		icons.put("L", R.drawable.l);
		icons.put("L'", R.drawable.lp);
		icons.put("R", R.drawable.r);
		icons.put("R'", R.drawable.rp);
		icons.put("B", R.drawable.b);
		icons.put("B'", R.drawable.bp);
		icons.put("U", R.drawable.u);
		icons.put("U'", R.drawable.up);
		icons.put("D", R.drawable.d);
		icons.put("D'", R.drawable.dp);
		icons.put("M", R.drawable.m);
		icons.put("M'", R.drawable.mp);
		icons.put("E", R.drawable.e);
		icons.put("E'", R.drawable.ep);
		icons.put("S", R.drawable.s);
		icons.put("S'", R.drawable.sp);
	}

	private static final int[] moveModes = { 0, 0, 0, 0, 0, 0, // UDFBLR
			1, 1, 1, // ESM
			3, 3, 3, 3, 3, 3, // XYZxyz
			2, 2, 2, 2, 2, 2 // udfblr
	};
	private static final int[] moveCodes = { 0, 1, 2, 3, 4, 5, // UDFBLR
			1, 2, 4, // ESM
			5, 2, 0, 5, 2, 0, // XYZxyz
			0, 1, 2, 3, 4, 5 // udfblr
	};

	private static final char[] modeChar = {'m', 't', 'c', 's', 'a'};
	
	public RubikMove(char move, boolean clockwise) {
		this.move = Character.toUpperCase(move);
		this.clockwise = clockwise;
	}

	public RubikMove(int[] move) {
		this.move = (char) move[0];
		this.clockwise = ((char) move[1] == '\'');
	}

	public static ArrayList<RubikMove> fromNative(String nativeString)
	{
		ArrayList<RubikMove> ret = new ArrayList<RubikMove>();
		for (int i=0; i<nativeString.length(); i += 3)
		{
			String a = nativeString.substring(i, i+3);
			if      (a.equals("UL.")) ret.add(new RubikMove('U', true));
		    else if (a.equals("UR.")) ret.add(new RubikMove('U', false));
		    else if (a.equals("DL.")) ret.add(new RubikMove('D', false));
		    else if (a.equals("DR.")) ret.add(new RubikMove('D', true));
		    else if (a.equals("LU.")) ret.add(new RubikMove('L', false));
		    else if (a.equals("LD.")) ret.add(new RubikMove('L', true));
		    else if (a.equals("RU.")) ret.add(new RubikMove('R', true));
		    else if (a.equals("RD.")) ret.add(new RubikMove('R', false));
		    else if (a.equals("FC.")) ret.add(new RubikMove('F', true));
		    else if (a.equals("FA.")) ret.add(new RubikMove('F', false));
		    else if (a.equals("BC.")) ret.add(new RubikMove('B', false));
		    else if (a.equals("BA.")) ret.add(new RubikMove('B', true));
		    else if (a.equals("ML.")) ret.add(new RubikMove('E', false));
		    else if (a.equals("MR.")) ret.add(new RubikMove('E', true));
		    else if (a.equals("MU.")) ret.add(new RubikMove('M', false));
		    else if (a.equals("MD.")) ret.add(new RubikMove('M', true));
		    else if (a.equals("MC.")) ret.add(new RubikMove('S', true));
		    else if (a.equals("MA.")) ret.add(new RubikMove('S', false));
		    else if (a.equals("CL.")) 
		    {
		    	ret.add(new RubikMove('U', true));
		    	ret.add(new RubikMove('E', false));
		    	ret.add(new RubikMove('D', false));
		    }
		    else if (a.equals("CR.")) 
		    {
		    	ret.add(new RubikMove('U', false));
		    	ret.add(new RubikMove('E', true));
		    	ret.add(new RubikMove('D', true));
		    }
		    else if (a.equals("CU.")) 
		    {
		    	ret.add(new RubikMove('L', false));
		    	ret.add(new RubikMove('M', false));
		    	ret.add(new RubikMove('R', true));
		    }
		    else if (a.equals("CD.")) 
		    {
		    	ret.add(new RubikMove('L', true));
		    	ret.add(new RubikMove('M', true));
		    	ret.add(new RubikMove('R', false));
		    }
		    else if (a.equals("CC.")) 
		    {
		    	ret.add(new RubikMove('F', true));
		    	ret.add(new RubikMove('S', true));
		    	ret.add(new RubikMove('B', false));
		    }
		    else if (a.equals("CA.")) 
		    {
		    	ret.add(new RubikMove('F', false));
		    	ret.add(new RubikMove('S', false));
		    	ret.add(new RubikMove('B', true));
		    }
		}
		return ret;
	}
	public int getImage() {
		return RubikMove.icons.get(getMoveRep());
	}

	public static int getImage(String key) {
		return RubikMove.icons.get(key);
	}

	public static Set<String> allMoves() {
		return RubikMove.icons.keySet();
	}

	public String getMoveRep() {
		return String.format("%c%s", move, (clockwise) ? "" : "'");
	}

	@Override
	public String toString() {
		return getMoveRep();
	}

	public int[] getMoveRepInt() {
		int ret[] = { 0, 0 };
		String s = getMoveRep();
		for (int i = 0; i < s.length(); i++) {
			ret[i] = (int) s.charAt(i);
		}
		return ret;
	}
	
	public int[] getAnimRep() {
		return getAnimRep(getMoveRep());
	}
	
	public static int[] getAnimRep(String sequence) {
		//String sequence = getMoveRep();
		int length = 0;
		int[] move = new int[sequence.length()]; // overdimmensioned
		for (int i = 0; i < sequence.length(); i++) {
			/*if (sequence.charAt(i) == '.') {
				move[length] = -1;
				length++;
			} else if (sequence.charAt(i) == '{') {
				i++;
				while (i < sequence.length()) {
					if (sequence.charAt(i) == '}')
						break;
					i++;
				}
			} else {*/
			for (int j = 0; j < 12; j++) {
				if (sequence.charAt(i) == "UDFBLRESMXYZ".charAt(j)) {
					i++;
					int mode = moveModes[j];
					move[length] = moveCodes[j] * 24;
					if (i < sequence.length()) {
						if (moveModes[j] == 0) { // modifiers for basic
													// characters UDFBLR
							for (int k = 0; k < modeChar.length; k++) {
								if (sequence.charAt(i) == modeChar[k]) {
									mode = k + 1;
									i++;
									break;
								}
							}
						}
					}
					move[length] += mode * 4;
					if (i < sequence.length()) {
						if (sequence.charAt(i) == '1')
							i++;
						else if (sequence.charAt(i) == '\''
								|| sequence.charAt(i) == '3') {
							move[length] += 2;
							i++;
						} else if (sequence.charAt(i) == '2') {
							i++;
							if (i < sequence.length()
									&& sequence.charAt(i) == '\'') {
								move[length] += 3;
								i++;
							} else
								move[length] += 1;
						}
					}
					length++;
					i--;
					break;
				}
			}
		}
		//}
		int[] returnMove = new int[length];
		for (int i = 0; i < length; i++)
			returnMove[i] = move[i];
		return returnMove;
	}

	public char getMove() {
		return move;
	}

	public boolean getClockwise() {
		return clockwise;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public void setDone() {
		done = !done;
	}

	public boolean isDone() {
		return done;
	}

	public RubikMove getInverse() {
		return new RubikMove(this.move, !this.clockwise);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final RubikMove other = (RubikMove) obj;
		return move == other.move && clockwise == other.clockwise;
	}

	private RubikMove(Parcel in) {
		move = (char) in.readInt();
		clockwise = in.readInt() != 0;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt((char) move);
		out.writeInt((clockwise) ? 1 : 0);
	}

	public static final Parcelable.Creator<RubikMove> CREATOR = new Parcelable.Creator<RubikMove>() {
		public RubikMove createFromParcel(Parcel in) {
			return new RubikMove(in);
		}

		public RubikMove[] newArray(int size) {
			return new RubikMove[size];
		}
	};

}
