package com.droidtools.rubiksolver;

import java.util.List;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ColorsAdapter extends BaseAdapter {

	List<Byte> mData;
	ColorDecoder mDecoder;
	int lastSize = -1;
	
	public ColorsAdapter(ColorDecoder decoder) {
		mData =  null; //new ArrayList<Integer>(decoder.getIds());
		mDecoder = decoder;
	}
	
	public ColorsAdapter(List<Byte> results, ColorDecoder decoder) {
		mData =  results; //new ArrayList<Integer>(decoder.getIds());
		mDecoder = decoder;
	}

	public void setData(List<Byte> data) {
		mData = data;
		notifyDataSetChanged();
	}
	@Override
	public int getCount() {
		if (mData == null) {
			return mDecoder.colorSize();
		} else {
			return mData.size();
		}
	}

	@Override
	public Object getItem(int position) {
		if (mData == null) {
			return mData.get(position);
		} else {
			return mDecoder.getSortedId(position);
		}
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
		if (lastSize != mDecoder.colorSize()) {
			// TODO(bbrown): A change in size is a bad way to track change.
			// We should be notified of a change in a thread safe way.
			lastSize = mDecoder.colorSize();
			notifyDataSetChanged();
		}
		View nv;
		byte colorPos;
		if (convertView == null) {
			nv = new FaceletView(parent.getContext());
		} else { // Reuse/Overwrite the View passed
			nv = convertView;
		}
		if (mData == null) {
			// TODO(bbrown): position can be out of bounds of idArray when a bad cube is passed to the solver.
			colorPos = mDecoder.getSortedId(position);
		} else {
			colorPos = mData.get(position);
		}
		
		// TODO(bbrown): We probably want to show a default image if the bitmap is null.
		Bitmap bitmap = mDecoder.getBitmap(colorPos);
		if (bitmap != null) {
			((FaceletView) nv).updateView(String.format("Color %02d", colorPos), bitmap);
		}
		return nv;
	}

}
