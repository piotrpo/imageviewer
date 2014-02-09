package pl.com.digita.imageviewer.utils;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Cache for storing bitmaps in memory
 */
public class BitmapLruCache extends LruCache<String, Bitmap> {
    private static final int DEFAULT_CACHE_SIZE = (int) (Runtime.getRuntime().maxMemory() / 1024) / 4;

    public BitmapLruCache() {
        this(DEFAULT_CACHE_SIZE);
    }

    public BitmapLruCache(int maxSize) {
        super(maxSize);
    }


    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value == null ? 0 : value.getRowBytes() * value.getHeight() / 1024;
    }


    public Bitmap getBitmap(String url) {
        return get(url);
    }


    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }

}
