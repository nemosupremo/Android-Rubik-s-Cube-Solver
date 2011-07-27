package com.droidtools.rubiksolver;

import java.util.ArrayList;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class SolutionAdapter extends BaseAdapter {

	ArrayList<RubikMove> mData;
	
	public SolutionAdapter(ArrayList<RubikMove> data) {
		mData = data;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
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
		((FaceletView) nv).updateView(mData.get(position).getMoveRep(),
				mData.get(position).getImage(), (mData.get(position).isDone()) ? android.R.color.darker_gray : android.R.color.background_dark);
		return nv;
	}

}
