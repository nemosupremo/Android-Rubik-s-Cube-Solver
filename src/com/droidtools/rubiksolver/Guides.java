package com.droidtools.rubiksolver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Display;
import android.view.View;

public class Guides extends View {
	Paint mPaint = new Paint();
	int sideLength;
	int deviceWidth;
	int deviceHeight;
	String mFace;

	public Guides(Context context, String face) {
		super(context);
		
		mFace = face;
		mPaint.setColor(Color.RED);
		Display display = ((LoadCube) context).getWindowManager().getDefaultDisplay(); 
		deviceWidth = display.getWidth();
		deviceHeight = display.getHeight();
		sideLength = (int) (Math.min(deviceWidth, deviceHeight) * .9);
	}
	
	public String getSide() {
		return mFace;
	}

	@Override
	public void onDraw(Canvas canvas) {
		int x0,y0,x1,y1,margin;
		margin = (int) (Math.min(deviceWidth, deviceHeight) * .1);
		for (int i=0; i<4; i++) {
			x0 = ((deviceWidth - sideLength) / 2) + (sideLength / 3) * i;
			y0 = margin/2; y1 = y0+sideLength;
			canvas.drawLine(x0,y0,x0,y1,mPaint);
			
			x0 = ((deviceWidth - sideLength) / 2);
			x1 = ((deviceWidth - sideLength) / 2) + sideLength;
			y0 = margin/2 + (sideLength / 3) * i;
			canvas.drawLine(x0,y0,x1,y0,mPaint);
		}
		
		mPaint.setTextSize(60);
		canvas.rotate(-90);
		x0 = -deviceHeight + margin/2 + 15;
		y0 = (deviceWidth - sideLength)/2 + 62;
		canvas.drawText("TL", x0,y0, mPaint);
		
		x0 = -deviceHeight + margin/2 + 15;
		y0 = 62;
		canvas.drawText(getSide(), x0,y0, mPaint);
		
		//canvas.drawLine(0, 0, 20, 20, paint);
		//canvas.drawLine(20, 0, 0, 20, paint);
	}

}
