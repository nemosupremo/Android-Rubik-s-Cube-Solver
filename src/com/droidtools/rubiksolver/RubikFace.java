package com.droidtools.rubiksolver;

import java.util.Map;

public class RubikFace {
	byte[] struc;
	String name;
	Map<String, RubikFace> cube;
	static final int[] map1 = {2,5,8,1,4,7,0,3,6};
	static final int[] map2 = {6,3,0,7,4,1,8,5,2};
	
	public RubikFace(String name, byte value, Map<String, RubikFace> cube) {
		this.name = name;
		struc = new byte[9];
		for (int i=0; i<9; i++) {
			struc[i] = value;
		}
		this.cube = cube;
	}
	
	public String getName() {
		return name;
	}
	
	public byte[] structure() {
		return struc;
	}
	
	public int getUpFacePos(RubikFace frontFace, int num) {
		if (frontFace.getName().equals("FRONT") || frontFace.getName().equals("UP") || frontFace.getName().equals("DOWN"))
			return num;
		else if (frontFace.getName().equals("BACK"))
			return 8 - num;
		else if (frontFace.getName().equals("LEFT"))
			return map1[num];
		else if (frontFace.getName().equals("RIGHT"))
			return map2[num];
		else
			return num;
	}
	
	public int getDownFacePos(RubikFace frontFace, int num) {
		if (frontFace.getName().equals("FRONT") || frontFace.getName().equals("UP") || frontFace.getName().equals("DOWN"))
			return num;
		else if (frontFace.getName().equals("BACK"))
			return 8 - num;
		else if (frontFace.getName().equals("LEFT"))
			return map2[num];
		else if (frontFace.getName().equals("RIGHT"))
			return map1[num];
		else
			return num;
	}
	
	public int getPosFromUp(int num) {
		if (name.equals("FRONT") || name.equals("UP") || name.equals("DOWN"))
			return num;
		else if (name.equals("BACK"))
			return 8 - num;
		else if (name.equals("LEFT"))
			return map2[num];
		else if (name.equals("RIGHT"))
			return map1[num];
		else
			return num;
	}
	
	public int getPosFromDown(int num) {
		if (name.equals("FRONT") || name.equals("UP") || name.equals("DOWN"))
			return num;
		else if (name.equals("BACK"))
			return 8 - num;
		else if (name.equals("LEFT"))
			return map1[num];
		else if (name.equals("RIGHT"))
			return map2[num];
		else
			return num;
	}
	
	private int getPos(RubikFace frontFace, int num) {
		if (frontFace.getName().equals("UP")) 
			return getPosFromUp(num);
		else if (frontFace.getName().equals("DOWN"))
			return getPosFromDown(num);
		else if (name.equals("UP"))
			return getUpFacePos(frontFace, num);
		else if (name.equals("DOWN"))
			return getDownFacePos(frontFace, num);
		else 
			return num;
	}
	
	public byte get(RubikFace frontFace, int num) {
		return struc[getPos(frontFace, num)];
	}
	
	public void set(RubikFace frontFace, int num, byte value) {
		 struc[getPos(frontFace, num)] = value;
	}
	
	public boolean isSolved() {
		int c = struc[0];
		for (int i=1; i<struc.length; i++) {
			if (c != struc[i])
				return false;
		}
		return true;
	}
	
	public byte[] listRep(RubikFace frontFace) {
		byte[] r = new byte[9];
		for (int i=0; i<9; i++) {
			r[i] = get(frontFace, i);
		}
		return r;
	}
	
	public void reOrder(RubikFace frontFace, int[] newOrder) {
		byte[] oldDat = listRep(frontFace);
		for (int i=0; i<9; i++) {
			set(frontFace, i, oldDat[newOrder[i]]);
		}
	}
	
	public void setValues(byte[] values) {
		for (int i=0; i<9; i++) {
			struc[i] = values[i];
		}
	}
	
	public char getNotName() {
		return name.charAt(0);
	}
}
