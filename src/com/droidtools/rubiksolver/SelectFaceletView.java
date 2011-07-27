package com.droidtools.rubiksolver;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SelectFaceletView extends LinearLayout {
	TextView mName;
	ImageView mImage;

	public SelectFaceletView(Context context) {
		super(context);
		setOrientation(HORIZONTAL);
		setGravity(Gravity.LEFT);
		LayoutParams lp = new LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,   
				LinearLayout.LayoutParams.WRAP_CONTENT
        );
		lp.gravity = Gravity.CENTER_VERTICAL;
		/*setLayoutParams(new LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,   
				LinearLayout.LayoutParams.WRAP_CONTENT
        ));*/
		
		mName = new TextView(context);//((TextView) findViewById(R.id.colorName));
		mImage = new ImageView(context);//((ImageView) findViewById(R.id.faceletImg));
		
		Resources res = context.getResources();
		mName.setPadding((int)toPixel(res, 15), 0, (int)toPixel(res, 15), 0);
		mName.setGravity(android.view.Gravity.CENTER_VERTICAL);
           
        Theme th = context.getTheme();
        TypedValue tv = new TypedValue();
     
        if (th.resolveAttribute(android.R.attr.textAppearanceLargeInverse, tv, true)) {
        	mName.setTextAppearance(context, tv.resourceId);
        }
        mName.setMinHeight(65);
       // mName.setCompoundDrawablePadding((int)toPixel(res, 14));
        
		//setContentView(R.layout.faceletview);
		
		addView(mImage);
		addView(mName);
		mName.setLayoutParams(lp);
		mImage.setLayoutParams(lp);
		
		
	}
	
	 private float toPixel(Resources res, int dip) {
         float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, res.getDisplayMetrics());
         return px;
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
