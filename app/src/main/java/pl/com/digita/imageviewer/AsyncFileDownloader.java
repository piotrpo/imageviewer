package pl.com.digita.imageviewer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import pl.com.digita.imageviewer.utils.ApplicationUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Implementation of AsyncTask for downloading files
 */
public class AsyncFileDownloader extends AsyncTask<String, Integer, File> {


    private Context mContext;
    private IDownloadTaskCallbacks mObserver;
    private File mFileResult;
    private String mUrl;

    public AsyncFileDownloader(Context pContext, IDownloadTaskCallbacks pObserver) {
        mContext = pContext;
        this.mObserver = pObserver;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    protected File doInBackground(String... sUrl) {
        mUrl = sUrl[0];
        //First check if file is not downloaded yet
        File cacheDir = mContext.getCacheDir();
        String fileName = ApplicationUtils.md5(sUrl[0]);
        mFileResult = new File(cacheDir, fileName);

        if (mFileResult.exists()) {
            return mFileResult;
        }


        //prevent system from sleeping
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        wl.acquire();


        //download the file
        try {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    claimDownloadError();
                    return null;
                }

                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(mFileResult);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling
                    if (isCancelled()) {

                        return null;
                    }
                    total += count;
                    // publishing the progress
                    if (fileLength > 0) { // only if total length is known
                        publishProgress((int) (total * 100 / fileLength), (int) total);
                    } else {
                        publishProgress(-1, (int) total);
                    }
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                claimDownloadError();
                return null;
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
        } finally {
            wl.release();
        }

        return mFileResult;
    }

    @Override
    protected void onPostExecute(File result) {
        if (mObserver != null) {

            mObserver.onFileAvailable(result);
        }

        //delete partially downloaded file
        if (result == null) {
            mFileResult.delete();
        }


    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (mObserver != null) {
            mObserver.onProgressPublished(values[0], values[1]);
        }
    }

    private void claimDownloadError() {
        if (mObserver != null) {


            mObserver.onError();

        }

    }


    /**
     * Interface for register callbacks to AsyncFileDownloader
     */
    public interface IDownloadTaskCallbacks {

        /**
         * Called on every progress update
         *
         * @param progress        percent of download (-1 if not available)
         * @param downloadedBytes downloaded bytes
         */
        public void onProgressPublished(int progress, int downloadedBytes);

        /**
         * Called when file is downloaded
         *
         * @param pFile just downloaded file
         */
        public void onFileAvailable(File pFile);

        /**
         * called on error
         */
        public void onError();
    }


}
