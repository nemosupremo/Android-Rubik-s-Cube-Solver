package com.droidtools.rubiksolver;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ColorsAdapter extends BaseAdapter {

	List<Byte> mData;
	ColorDecoder mDecoder;
	List<Byte> idArray;
	
	public ColorsAdapter(ColorDecoder decoder) {
		mData =  null; //new ArrayList<Integer>(decoder.getIds());
		mDecoder = decoder;
		idArray = new ArrayList<Byte>(mDecoder.getIdArray());
	}
	
	public ColorsAdapter(List<Byte> results, ColorDecoder decoder) {
		mData =  results; //new ArrayList<Integer>(decoder.getIds());
		mDecoder = decoder;
		idArray = null;
	}

	public void setData(List<Byte> data) {
		mData = data;
		notifyDataSetChanged();
	}
	@Override
	public int getCount() {
		if (mData == null)
		{
			if (idArray.size() != mDecoder.getIdArray().size())
				idArray = new ArrayList<Byte>(mDecoder.getIdArray());
			return mDecoder.colorSize();
		}
		else
			return mData.size();
	}

	@Override
	public Object getItem(int position) {
		if (mData == null)
			return mData.get(position);
		else
			return idArray.get(position);
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
		if (idArray != null && idArray.size() != mDecoder.getIdArray().size())
		{
			idArray = new ArrayList<Byte>(mDecoder.getIdArray());
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
			colorPos = idArray.get(position);
		} else {
			colorPos = mData.get(position);
		}
		((FaceletView) nv).updateView(String.format("Color %02d", colorPos),
				mDecoder.getBitmap(colorPos));
		return nv;
	}

}
