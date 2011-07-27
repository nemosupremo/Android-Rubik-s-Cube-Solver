package com.droidtools.rubiksolver;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class SelectColorAdapter extends BaseAdapter {
	
	//List<Byte> mData; 
	ColorDecoder mDecoder;
	
	public SelectColorAdapter(ColorDecoder decoder) {
		mDecoder = decoder;
		//mData = new ArrayList<Byte>(mDecoder.getIds());
	}

	@Override
	public int getCount() {
		return mDecoder.colorSize();
	}

	@Override
	public Object getItem(int position) {
		return mDecoder.getIdArray().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View nv;
		byte colorPos;
		if (convertView == null) {
			nv = new SelectFaceletView(parent.getContext());
		} else { // Reuse/Overwrite the View passed
			nv = convertView;
		}
		colorPos = mDecoder.getIdArray().get(position);
		((SelectFaceletView) nv).updateView(String.format("Color %02d", colorPos),
				mDecoder.getBitmap(colorPos));
		return nv;
	}

}
