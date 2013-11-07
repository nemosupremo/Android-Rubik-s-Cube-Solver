package com.droidtools.rubiksolver;

import java.util.List;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ColorsAdapter extends BaseAdapter {

	List<Byte> mData;
	ColorDecoder mDecoder;
	
	public ColorsAdapter(ColorDecoder decoder) {
		mData =  null;
		mDecoder = decoder;
	}
	
	public ColorsAdapter(List<Byte> results, ColorDecoder decoder) {
		mData =  results;
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
		View nv;
		if (convertView == null) {
			nv = new FaceletView(parent.getContext());
		} else { // Reuse/Overwrite the View passed
			nv = convertView;
		}
		
		Byte colorId;
		if (mData == null) {
			colorId = mDecoder.getSortedId(position);
		} else {
			colorId = mData.get(position);
		}

		Bitmap bitmap = mDecoder.getBitmap(colorId);
        ((FaceletView) nv).updateView(String.format("Color %02d", colorId), bitmap);

		return nv;
	}

}
