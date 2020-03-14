package com.probe.aki.testbedrain;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;


public class ShowDisplay extends View {

    private Paint paint;
    private ArrayList<byte[]> bitmap_array;
    private Bitmap scaled_bitmap[];
    private int index = 0;

    public ShowDisplay(Context context) {
        super(context);
        init();
    }
    public ShowDisplay(Context context, AttributeSet set) {
        super(context,set);
        init();
    }

    private void init() {
        paint = new Paint();
    }

    public void setBitmapArray(ArrayList<byte[]> bitmap_array){
        this.bitmap_array = bitmap_array;
    }

    public ArrayList<byte[]>getBitmapArray(){
        return bitmap_array;
    }

    public void setTime(int index){
        this.index = index;
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(bitmap_array != null) {
            if(scaled_bitmap == null) {
                scaled_bitmap = new Bitmap[bitmap_array.size()];
                byte b[] = (byte[])bitmap_array.get(0);
                Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
                int bmh = bitmap.getHeight();
                int bmw = bitmap.getWidth();
                int newx;
                int newy;
                if(this.getWidth() > this.getHeight()){
                    newx = (int)((double)this.getHeight()/(double)bmh*(double)bmw);
                    newy = this.getHeight();
                } else {
                    newx = this.getWidth();
                    newy = (int)((double)this.getWidth()/(double)bmw*(double)bmh);
                }
                scaled_bitmap[0] = Bitmap.createScaledBitmap(bitmap, newx, newy, false);
                for (int i = 1; i < bitmap_array.size(); i++) {
                    b = (byte[])bitmap_array.get(i);
                    bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
                    scaled_bitmap[i] = Bitmap.createScaledBitmap(bitmap, newx, newy, false);
                }
            }
            if(scaled_bitmap != null)
                canvas.drawBitmap(scaled_bitmap[index], 0, 0, paint);
        }
    }
}
