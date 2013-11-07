package com.droidtools.rubiksolver;

import java.util.Arrays;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FaceAdapter extends BaseAdapter {

    // TODO(bbrown): Make this private. Problem now is this is referenced then
    // later modified.
	byte[] mData;
	private ColorDecoder mDecoder;
	
	public FaceAdapter(byte[] results, ColorDecoder decoder) {
		mData =  results;
		mDecoder = decoder;
	}

	@Override
	public int getCount() {
		if (mData[0] == -1) return 0;
		return mData.length;
	}

	@Override
	public Object getItem(int position) {
		return mData[position];
	}
	
	public void setItem(int position, byte item) {
		mData[position] = item;
		notifyDataSetChanged();
	}
	
	/**
	 * Returns a copy of the internal data.
	 */
	public byte[] getData() {
		return Arrays.copyOf(mData, mData.length);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View nv;
		byte colorPos;
		if (convertView == null) {
			nv = new FaceletView(parent.getContext());
		} else { // Reuse/Overwrite the View passed
			nv = convertView;
		}
		colorPos = mData[position];

		Bitmap bitmap = mDecoder.getBitmap(colorPos);
		((FaceletView) nv).updateView(String.format("Color %02d", colorPos), bitmap);
		
		return nv;
	}
}
