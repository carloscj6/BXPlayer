package com.revosleap.bxplayer.AppUtils.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;

public class GaussianBlur {
    private static final float BITMAP_SCALE= 0.4f;
    private static final float BLUR_RADIUS= 7.5f;
    private static int colorLight;


    public static Bitmap blurred(Context context, Bitmap bitmap,int radius){
        try {
            bitmap=colorEncodingChange(bitmap);

            }
            catch (Exception e){
            e.printStackTrace();
            }
        Bitmap blurry=Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        RenderScript renderScript= RenderScript.create(context);
        Allocation blurInput= Allocation.createFromBitmap(renderScript,bitmap);
        Allocation blurOutput= Allocation.createFromBitmap(renderScript,blurry);
        ScriptIntrinsicBlur blur= ScriptIntrinsicBlur.create(renderScript,
                Element.U8_4(renderScript));
        blur.setInput(blurInput);
        blur.setRadius(radius);
        blur.forEach(blurOutput);
        blurOutput.copyTo(bitmap);
        renderScript.destroy();
        return bitmap;
    }
    private static Bitmap colorEncodingChange(Bitmap img)throws Exception{
        int pixelNum= img.getWidth()/4*img.getHeight()/4;
        int[] pixels= new int[pixelNum];
        img.getPixels(pixels,0,img.getWidth(),0,0,img.getWidth(),img.getHeight());
        Bitmap result=Bitmap.createBitmap(img.getWidth(),img.getHeight(),Bitmap.Config.ARGB_8888);
        result.setPixels(pixels,0,result.getWidth()/4,0,0,result.getWidth()/4,result.getHeight());
        return result;
    }
    public final static int lightColor(Bitmap bitmap){
         final int[] color = new int[1];
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch= palette.getLightVibrantSwatch();
                    if (swatch!= null){
                        color[0] =swatch.getRgb();
                        Log.v("color","color "+ color[0]);
                        colorLight=color[0];

                    }
                }
            });
            return colorLight;
    }
}
