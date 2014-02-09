package pl.com.digita.imageviewer.ui.activity;

import android.app.Activity;
import android.content.*;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import pl.com.digita.imageviewer.R;
import pl.com.digita.imageviewer.service.ServiceBitmapProducer;
import pl.com.digita.imageviewer.utils.ApplicationUtils;

/**
 * Just old style Activity allows system to release more memory if needed.
 */
public class ActivityShowImage extends Activity {

    private ImageView mImageView;
    private ServiceConnection mServiceConnection;
    private ServiceBitmapProducer.BinderServiceBitmapProducer mBinderServiceBitmapProducer;
    private ProgressBar mProgressBar;
    private TextView mTextViewProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.image_view_main);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setMax(100);
        mProgressBar.setIndeterminate(true);

        mTextViewProgress = (TextView) findViewById(R.id.text_view_downloaded);
    }

    @Override
    protected void onResume() {
        registerReceivers();

        String url = getIntent().getStringExtra(ApplicationUtils.EXTRA_URL);
        connectService(url);

        super.onResume();
    }

    @Override
    protected void onPause() {
        unbindService(mServiceConnection);
        super.onPause();
    }

    private void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter(ApplicationUtils.ACTION_BITMAP_READY);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bitmap bitmap = intent.getParcelableExtra(ApplicationUtils.EXTRA_BITMAP);
                mImageView.setImageBitmap(bitmap);
            }
        }, intentFilter);


        intentFilter = new IntentFilter(ApplicationUtils.ACTION_DOWNLOAD_HEARTBEAT);
        localBroadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                showProgress(intent);
            }
        }, intentFilter);
    }

    private void showProgress(Intent intent) {
        int downloadedData = intent.getIntExtra(ApplicationUtils.EXTRA_DOWNLOADED, 0);
        int downloadProgress = intent.getIntExtra(ApplicationUtils.EXTRA_PROGRESS, 0);


        if (downloadProgress < 100 && downloadProgress >= 0) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(downloadProgress);

            mTextViewProgress.setText(getString(R.string.label_downloaded_bytes)+(downloadedData/1024) +"kB");
        } else {
            mProgressBar.setIndeterminate(true);
            mTextViewProgress.setText(getString(R.string.label_decoding_bitmap));
        }
    }

    private void connectService(final String pFileUrl) {
        Intent intentBindService = new Intent(this, ServiceBitmapProducer.class);
        //intentBindService.putExtra(ApplicationUtils.EXTRA_URL, pFileUrl);
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBinderServiceBitmapProducer = (ServiceBitmapProducer.BinderServiceBitmapProducer) service;
                mBinderServiceBitmapProducer.getService().deliverBitmap(pFileUrl);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBinderServiceBitmapProducer = null;
            }
        };
        bindService(intentBindService, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mBinderServiceBitmapProducer != null){
            mBinderServiceBitmapProducer.getService().cancelCurrentTask();
        }
    }
}


