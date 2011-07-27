package com.droidtools.rubiksolver;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FaceAdapter extends BaseAdapter {

	byte[] mData;
	ColorDecoder mDecoder;
	
	public FaceAdapter(byte[] results, ColorDecoder decoder) {
		mData =  results; //new ArrayList<Integer>(decoder.getIds());
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
		((FaceletView) nv).updateView(String.format("Color %02d", colorPos),
				mDecoder.getBitmap(colorPos));
		return nv;
	}

}
