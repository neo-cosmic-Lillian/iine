package jp.upset.horoscope;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.widget.ImageView;

public class TabView extends ImageView {

	private int count = 0;
	private Paint mPaint;
	private Bitmap mCircle;
	private Rect bounds;

	public TabView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub

		mPaint = new Paint();
		
		mPaint.setColor(Color.RED);
		mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
		mCircle = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.bg_num);
		bounds = new Rect();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if (count != 0) {
			int dx = getWidth() * 4 / 5;
			int dy = getWidth() * 2 / 5;
			int w = getWidth() / 5, h = getWidth() / 5;
			canvas.translate(dx, dy);
			bounds.set(0, 0, w, h);
			canvas.drawBitmap(mCircle, null, bounds, mPaint);
			mPaint.setTextSize(w/4*3);
			canvas.translate(w / 2, h * 5 / 12);
			String s = Integer.toString(count);
			mPaint.getTextBounds(s, 0, s.length(), bounds);
			mPaint.setTextAlign(Align.CENTER);
			canvas.drawText(s, 0, bounds.height() / 2, mPaint);
		}

	}

	public void setCount(int i) {
		// TODO Auto-generated method stub
		if(i>99) i=99;
		count = i;
	}

}
