package com.droidtools.rubiksolver;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FaceletView extends LinearLayout {
	TextView mName;
	ImageView mImage;

	public FaceletView(Context context) {
		super(context);
		setOrientation(VERTICAL);
		setGravity(Gravity.CENTER);
		LayoutParams lp = new LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,   
				LinearLayout.LayoutParams.WRAP_CONTENT
        );
		lp.gravity = Gravity.CENTER;
		/*setLayoutParams(new LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,   
				LinearLayout.LayoutParams.WRAP_CONTENT
        ));*/
		//setContentView(R.layout.faceletview);
		mName = new TextView(context);//((TextView) findViewById(R.id.colorName));
		mImage = new ImageView(context);//((ImageView) findViewById(R.id.faceletImg));
		addView(mImage);
		addView(mName);
		mName.setLayoutParams(lp);
		mImage.setLayoutParams(lp);
	}
	
	public void updateView(String text, int image, int bg ) {
		mName.setText(text);
		mImage.setImageResource(image);
		setBackgroundResource(bg);
	}
	
	public void updateView(String text, Bitmap image) {
		mName.setText(text);
		mImage.setImageBitmap(image);
		//mImage.setAdjustViewBounds(true);
		//mImage.setMaxHeight(250);
		//mImage.setMaxWidth(250);
		//mImage.setImageResource(R.drawable.b);
	}
}
