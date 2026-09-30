package games.mrlaki5.backgammon.GameView.themes;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.widget.ImageView;

/**
 * Decodes the large JPEG theme-preview images (400-700KB full-resolution photos) down-sampled
 * to the small thumbnail size they're actually shown at, instead of letting ImageView/BitmapFactory
 * decode them at full resolution for an 52-80dp box. Cached so repeated dialog opens are free.
 */
public final class ThemeThumbnailLoader {

    private static final LruCache<String, Bitmap> CACHE = new LruCache<>(16);

    private ThemeThumbnailLoader() {}

    public static void loadInto(ImageView imageView, int resId, int targetPx) {
        String key = resId + "_" + targetPx;
        Bitmap cached = CACHE.get(key);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }

        Resources res = imageView.getResources();
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, bounds);

        int sampleSize = 1;
        int halfWidth = bounds.outWidth / 2;
        int halfHeight = bounds.outHeight / 2;
        while (targetPx > 0 && (halfWidth / sampleSize) >= targetPx && (halfHeight / sampleSize) >= targetPx) {
            sampleSize *= 2;
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = sampleSize;
        Bitmap bitmap = BitmapFactory.decodeResource(res, resId, decodeOptions);
        if (bitmap != null) {
            CACHE.put(key, bitmap);
            imageView.setImageBitmap(bitmap);
        }
    }
}
