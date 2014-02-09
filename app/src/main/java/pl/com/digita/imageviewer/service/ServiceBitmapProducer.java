package pl.com.digita.imageviewer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import pl.com.digita.imageviewer.AsyncBitmapDecoder;
import pl.com.digita.imageviewer.AsyncFileDownloader;
import pl.com.digita.imageviewer.utils.ApplicationUtils;
import pl.com.digita.imageviewer.utils.BitmapLruCache;

import java.io.File;


public class ServiceBitmapProducer extends Service {

    //set this value to keep service alive for a while
    private static final int INT_SERVICE_DEATH_DELAY = 20;


    private BinderServiceBitmapProducer mThisServiceBinder = new BinderServiceBitmapProducer();
    private Handler mHandler = new Handler();
    private boolean mIsBound = false;
    private BitmapLruCache mBitmapMemoryCache;
    private LocalBroadcastManager mLocalBroadcastManager;
    private AsyncFileDownloader mAsyncFileDownloader;


    @Override
    public void onCreate() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                intent.getStringExtra(ApplicationUtils.EXTRA_URL);
            }
        }, new IntentFilter(ApplicationUtils.ACTION_DOWNLOAD_BITMAP));

        //initialize with default size
        mBitmapMemoryCache = new BitmapLruCache();

    }

    @Override
    public IBinder onBind(Intent intent) {
        mIsBound = true;
        startService(new Intent(this, this.getClass()));

        return mThisServiceBinder;
    }



    @Override
    public boolean onUnbind(Intent intent) {
        // this method should be called only when all binders are released
        mIsBound = false;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                safeClose();
            }
        }, 1000 * INT_SERVICE_DEATH_DELAY);
        return true;
    }


    @Override
    public void onRebind(Intent intent) {
        mIsBound = true;
    }

    public void cancelCurrentTask(){
        if(mAsyncFileDownloader == null && mAsyncFileDownloader.getStatus().equals(AsyncTask.Status.RUNNING)){
            mAsyncFileDownloader.cancel(true);
        }
    }


    /**
     * stop this service is it's no longer needed
     */
    private void safeClose() {
        if (!mIsBound) {
            stopSelf();
        }
    }


    /**
     * This method will download bitmap and scale it down as close as possible to fit the target dimension.
     * Note - target dimension is only a suggestion
     *
     * @param pUrl url of bitmap to decode
     */
    public void deliverBitmap(final String pUrl) {

        Bitmap bitmap = mBitmapMemoryCache.get(pUrl);
        if(bitmap != null){
            broadcastBitmap(bitmap);
            return;
        }


        mAsyncFileDownloader = new AsyncFileDownloader(this, new AsyncFileDownloader.IDownloadTaskCallbacks() {
            @Override
            public void onProgressPublished(int progress, int downloadedBytes) {
                sendProgressStatus(false, progress, downloadedBytes);
            }

            @Override
            public void onFileAvailable(File pFile) {


                AsyncBitmapDecoder bitmapDecoder = new AsyncBitmapDecoder(ServiceBitmapProducer.this, new AsyncBitmapDecoder.IBitmapDecoderCallback() {
                    @Override
                    public void onBitmapDecoded(Bitmap pBitmap) {
                        broadcastBitmap(pBitmap);
                        mBitmapMemoryCache.put(pUrl, pBitmap);
                    }
                });
                bitmapDecoder.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pFile);
            }

            @Override
            public void onError() {
                Log.e(ApplicationUtils.TAG, "Error while downloading file");
            }
        });
        mAsyncFileDownloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pUrl);


    }

    private void broadcastBitmap(Bitmap pScaledBitmap) {
        Intent intent = new Intent(ApplicationUtils.ACTION_BITMAP_READY);
        intent.putExtra(ApplicationUtils.EXTRA_BITMAP, pScaledBitmap);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }



    private void sendProgressStatus(boolean hasError, int progress, int downloaded) {
        Intent intent = new Intent(ApplicationUtils.ACTION_DOWNLOAD_HEARTBEAT);

        intent.putExtra(ApplicationUtils.EXTRA_HAS_ERROR, hasError);
        intent.putExtra(ApplicationUtils.EXTRA_PROGRESS, progress);
        intent.putExtra(ApplicationUtils.EXTRA_DOWNLOADED, downloaded);

        mLocalBroadcastManager.sendBroadcast(intent);
    }

    public class BinderServiceBitmapProducer extends Binder {
        public ServiceBitmapProducer getService() {
            return ServiceBitmapProducer.this;
        }
    }
}
