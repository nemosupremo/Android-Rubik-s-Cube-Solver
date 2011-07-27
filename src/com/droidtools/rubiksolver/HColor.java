package com.droidtools.rubiksolver;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class HColor implements Comparable<HColor>, Parcelable {
	double h = 0;
	double s = 0;
	double l = 0;
	int r = 0;
	int g = 0;
	int b = 0;
	public double distance = 0;
	public int key = -1;

	public HColor(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
		double[] res = hue(r, g, b);
		this.h = res[0];
		this.l = res[1];
		this.s = res[2];
	}

	public HColor(int color) {
		r = (color >> 16) & 0xFF;
		g = (color >> 8) & 0xFF;
		b = color & 0xFF;
		double[] res = hue(r, g, b);
		this.h = res[0];
		this.l = res[1];
		this.s = res[2];
	}
	
	public HColor(byte[] color) {
		int col = (color[0] << 24)
        	+ ((color[1] & 0xFF) << 16)
        	+ ((color[2] & 0xFF) << 8)
        	+ (color[3] & 0xFF);
		r = (col >> 16) & 0xFF;
		g = (col >> 8) & 0xFF;
		b = col & 0xFF;
		double[] res = hue(r, g, b);
		this.h = res[0];
		this.l = res[1];
		this.s = res[2];
		
	}

	public HColor(double h, double l, double s, int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.h = h;
		this.l = l;
		this.s = s;
	}
	
	

	/*
	public double getH() {
		return h;
	}

	public double getL() {
		return l;
	}

	public int getR() {
		return r;
	}

	public int getG() {
		return g;
	}

	public int getB() {
		return b;
	}*/

	public int getColor() {
		return 0xFF << 24 | r << 16 | g << 8 | b;
	}

	public boolean isBlack() {
		return r < 15 && g < 15 && b < 15;
	}
	
	public byte[] asByteArray() {
		int value = getColor();
		return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
	}

	private static double[] hue(int ri, int gi, int bi) {
		double[] ret = new double[3];
		double r = ri / 255.0;
		double g = gi / 255.0;
		double b = bi / 255.0;

		double maxcol = Math.max(Math.max(r, g), b);
		double mincol = Math.min(Math.min(r, g), b);
		ret[1] = (maxcol + mincol) / 2;
		
		if( maxcol != 0 )
			ret[2] = (maxcol-mincol) / maxcol;		// s
		else {
			// r = g = b = 0		// s = 0, v is undefined
			ret[2] = 0;
		}

		if (maxcol == mincol)
			ret[0] = 0;
		if (r == maxcol)
			ret[0] = (g - b) / (maxcol - mincol);
		else if (g == maxcol)
			ret[0] = 2.0 + (b - r) / (maxcol - mincol);
		else if (b == maxcol)
			ret[0] = 4.0 + (r - g) / (maxcol - mincol);
		else {
			ret[0] = 0;
		}

		ret[0] = ret[0] / 6 * 255;
		ret[1] = ret[1] * 255;
		ret[2] = ret[2] * 255;

		return ret;
	}

	public boolean isSimilar(HColor other) {
		int rad = (h > 100) ? 25 : 5;
		if (Math.abs(h - other.h) < rad) {
			if (Math.abs(l - other.l) < 25) {
				return true;
			}
		}
		return false;
	}
	
	//private double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
	//	return Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1)+(z2-z1)*(z2-z1));
	//}
	
	protected double distance(HColor other) {
		double[] vals = {r-other.r, g-other.g, b-other.b};
		double sum = 0;
		for (int i = 0; i<vals.length; i++)
			sum += vals[i]*vals[i];
		return Math.sqrt(sum);
	}

	public Byte mostSimilar(List<Byte> ids, ColorDecoder decoder, int limit) {
		if (ids.size() == 0) return null;
		for (int i=0; i<ids.size(); i++)
			decoder.getColor(ids.get(i)).distance = distance(decoder.getColor(ids.get(i)));
		Collections.sort(ids, new DistanceComparable(decoder));
		HColor ret = decoder.getColor(ids.get(0));
		if (ret.distance > limit && limit != -1)
			return null;
		else
			return ids.get(0);
	}
	
	public void usurp(HColor other)
	{
		this.r = (r+other.r)/2;
		this.g = (g+other.g)/2;
		this.b = (b+other.b)/2;
		this.h = (h+other.h)/2;
		this.l = (l+other.l)/2;
		this.s = (s+other.r)/2;
	}
	
	@Override
	public String toString() {
		return String.format("0x%X", getColor());
		
	}
	
	@Override
	public int compareTo(HColor another) {
		return Double.compare(h, another.h);
	}

	private HColor(Parcel in) {
		int color = in.readInt();
		r = (color >> 16) & 0xFF;
		g = (color >> 8) & 0xFF;
		b = color & 0xFF;
		h = in.readDouble();
		l = in.readDouble();
		s = in.readDouble();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(getColor());
		out.writeDouble(h);
		out.writeDouble(l);
		out.writeDouble(s);
	}

	public static final Parcelable.Creator<HColor> CREATOR = new Parcelable.Creator<HColor>() {
		public HColor createFromParcel(Parcel in) {
			return new HColor(in);
		}

		public HColor[] newArray(int size) {
			return new HColor[size];
		}
	};

	private class DistanceComparable implements Comparator<Byte>{
		ColorDecoder mDecoder;
		
		public DistanceComparable(ColorDecoder decoder)
		{
			mDecoder = decoder;
		}
		
		@Override
		public int compare(Byte o1, Byte o2) {
			return (mDecoder.getColor(o1).distance>mDecoder.getColor(o2).distance ? 1 : (mDecoder.getColor(o1).distance==mDecoder.getColor(o2).distance ? 0 : -1));
		}
	}
}
