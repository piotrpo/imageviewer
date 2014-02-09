package pl.com.digita.imageviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Simple worker extending AsyncTask that can decode bitmap adjusted to current screen size
 */
public class AsyncBitmapDecoder extends AsyncTask<File, Void, Bitmap> {
    private Context mContext;
    private IBitmapDecoderCallback mObserver;

    public AsyncBitmapDecoder(Context pContext, IBitmapDecoderCallback pObserver) {
        mContext = pContext;
        mObserver = pObserver;
    }

    @Override
    protected Bitmap doInBackground(File... params) {
        Point screenDimension = measureScreen();

        File pFile = params[0];


        Bitmap bitmap = null;
        try {
            bitmap = getScaledBitmap(pFile, getRatio(screenDimension, pFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        return bitmap;
    }

    private Point measureScreen() {
        Point screenDimension = new Point();

        screenDimension.x = mContext.getResources().getDisplayMetrics().widthPixels;
        screenDimension.y = mContext.getResources().getDisplayMetrics().heightPixels;
        return screenDimension;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        claimBitmapDecoded(result);
    }

    private int getRatio(Point pTargetDimension, File pFile) {

        Point bitmapSize = getBitmapSize(pFile);
        int longerTarget = Math.max(pTargetDimension.x, pTargetDimension.y);
        int longerSource = Math.max(bitmapSize.x, bitmapSize.y);

        int ratio = 1;

        while (ratio * longerTarget < longerSource) {
            ratio *= 2;
        }
        return ratio;

    }

    private Point getBitmapSize(File pFile) {
        Point result = new Point();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(pFile);
            BitmapFactory.decodeStream(fileInputStream, null, options);
            result.set(options.outWidth, options.outHeight);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }


    private void claimBitmapDecoded(Bitmap pScaledBitmap) {

        mObserver.onBitmapDecoded(pScaledBitmap);
    }

    private Bitmap getScaledBitmap(File bitmapFile, int ratio) throws FileNotFoundException {
        BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
        bitmapFactoryOptions.inSampleSize = ratio;
        FileInputStream bitmapInputStream = new FileInputStream(bitmapFile);
        Bitmap bitmap = BitmapFactory.decodeStream(bitmapInputStream, null, bitmapFactoryOptions);
        return bitmap;
    }

    public interface IBitmapDecoderCallback{
        public void onBitmapDecoded(Bitmap pBitmap);
    }
}
