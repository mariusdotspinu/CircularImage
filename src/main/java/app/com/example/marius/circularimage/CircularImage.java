package app.com.example.marius.circularimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Marius on 9/14/2016.
 */
public class CircularImage {

    int color;
    int textSize;
    int textColor;

    boolean isDownload;

    String text;
    String urlString;
    Context mContext;

    public static Bitmap bmp;
    Bitmap placeHolderBmp;

    public static int width, height;

    ImageView imageViewReference;

    URL url;


    public CircularImage(Context context) {

        mContext = context;
        width = 100;
        height = 100;
        bmp = null;
        this.url = null;
        this.urlString = null;
        this.color = -1;
        this.text = "";
        this.textSize = 10;
        this.textColor = Color.BLACK;
        this.isDownload = false;
        this.placeHolderBmp = null;

    }

    /**
     * Copying the reference of the destination imageView
     *
     * @param imageView ImageView where the circular image will be put in.
     */
    public void setImageViewReference(ImageView imageView) {

        this.imageViewReference = imageView;

    }

    /**
     * Creating the final circular image result.
     *
     * @param imageView ImageView where the circular image will be put in.
     * @return CircularImage
     */
    public CircularImage buildInto(ImageView imageView) {

        setImageViewReference(imageView);

        if (isDownload) {

            if (url != null) {
                new GetImageFromUrl(url).execute();
            } else if (urlString != null) {
                new GetImageFromUrl(urlString).execute();
            }

        } else {

            bmp = drawText(text, getCircledBitmap(bmp), textSize);
            this.imageViewReference.setImageBitmap(bmp);
        }

        return this;
    }

    /**
     * Setting a place holder while the image downloading takes place.
     *
     * @param resource Local resource for the place holder image.
     * @return CircularImage
     */
    public CircularImage setPlaceHolder(int resource) {

        Bitmap placeHolder = BitmapFactory.decodeResource(mContext.getResources(), resource);
        this.placeHolderBmp = Bitmap.createScaledBitmap(placeHolder, width, height, false);

        return this;
    }

    /**
     * Method for creating a circular image using a custom background from a given url string.
     *
     * @param url Url string given
     * @return CircularImage
     */
    public CircularImage setContentImage(String url) {

        if (this.color != -1) {
            isDownload = false;
        } else {

            this.urlString = url;
            isDownload = true;
        }

        return this;
    }

    /**
     * Method for creating a circular image using a custom background from an external url source.
     *
     * @param url Url source
     * @return CircularImage
     */
    public CircularImage setContentImage(URL url) {

        if (this.color != -1) {
            isDownload = false;
        } else {

            this.url = url;
            isDownload = true;
        }
        return this;
    }

    /**
     * Method for creating a circular image using a custom background.
     *
     * @param res Background resource
     * @return CircularImage
     */
    public CircularImage setContentImage(int res) {

        isDownload = false;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;

        bmp = BitmapFactory.decodeResource(mContext.getResources(), res, options);
        bmp = Bitmap.createScaledBitmap(bmp, width, height, false);

        return this;
    }

    /**
     * Method for creating a single solid-color circular image.
     *
     * @param resource Resource of the color
     * @return CircularImage
     */
    public CircularImage setColorImage(int resource) {
        try {
            this.color = getColorIntFromResource(mContext, resource);
        } catch (RuntimeException e) {
            e.printStackTrace();
            Log.d("colorImage", "Not resource");

            this.color = resource;
        }

        return this;
    }

    /**
     * @param text
     * @return CircularImage
     */
    public CircularImage setText(String text) {
        this.text = text;

        return this;
    }

    /**
     * @param textSize
     * @return CircularImage
     */
    public CircularImage setTextSize(int textSize) {
        this.textSize = textSize;

        return this;
    }

    /**
     * @param textColor
     * @return CircularImage
     */
    public CircularImage setTextColor(int textColor) {

        try {
            this.textColor = getColorIntFromResource(mContext, textColor);
        } catch (RuntimeException e) {

            e.printStackTrace();
            Log.d("textColor", "Image is not resource");
            this.textColor = textColor;
        }
        return this;
    }

    /**
     * Setting the size of the image (in width,height).
     *
     * @param Width
     * @param Height
     * @return CircularImage
     */
    public CircularImage setSize(int Width, int Height) {
        width = Width;
        height = Height;

        return this;
    }

    /**
     * Returns an int competent for color creation.
     *
     * @param context
     * @param colorInt The resource which was passed for the method
     * @return integer , later to be used for setting a color.
     */
    public int getColorIntFromResource(Context context, int colorInt) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Color.parseColor("#" + Integer.toHexString(context.getResources().getColor(
                    colorInt, context.getTheme())));
        } else {
            return Color.parseColor("#" + Integer.toHexString(context.getResources().
                    getColor(colorInt)));
        }
    }

    /**
     * Returns the bitmap as a circular shape.
     *
     * @param bmp Bitmap image
     * @return Bitmap - circle shaped image
     */
    private Bitmap getCircledBitmap(Bitmap bmp) {

        if (bmp == null) {
            bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas;
        BitmapShader shader = new BitmapShader(bmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        Paint paint = new Paint();
        paint.setAntiAlias(true);


        if (this.color != -1 && !isDownload) {

            paint.setColor(this.color);

            Rect rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
            RectF rectFloat = new RectF(rect);

            canvas = new Canvas(bmp);
            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawOval(rectFloat, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            canvas.drawBitmap(bmp, rect, rect, paint);

            return bmp;

        } else {

            paint.setXfermode(null);
            paint.setShader(shader);

            Bitmap cBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);

            canvas = new Canvas(cBitmap);
            canvas.drawCircle(bmp.getWidth() / 2, bmp.getHeight() / 2, bmp.getWidth() / 2, paint);

            return cBitmap;
        }

    }

    /**
     * Adds a text sequence to a required bitmap.
     *
     * @param text     Text that will appear in the image (centered by default).
     * @param bmp      The bitmap in which the text will be drawn.
     * @param textSize Text's size (10 dp by default)
     * @return Bitmap - Returns the old bitmap with the newly added text.
     */
    private Bitmap drawText(String text, Bitmap bmp, int textSize) {

        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textSize);
        String hexColor = String.format("#%06X", (0xFFFFFF & this.textColor));
        textPaint.setColor(Color.parseColor(hexColor));
        textPaint.setTextAlign(Paint.Align.CENTER);

        StaticLayout mTextLayout = new StaticLayout(text, textPaint,
                bmp.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        Canvas canvas = new Canvas(bmp);
        canvas.save();
        canvas.translate((canvas.getWidth() / 2), (canvas.getHeight() / 2) -
                ((mTextLayout.getHeight() / 2)));


        mTextLayout.draw(canvas);
        canvas.restore();

        return bmp;
    }

    /**
     * AsyncTask for getting the image source from the internet.
     */
    private class GetImageFromUrl extends AsyncTask {

        URL url;
        String urlString;

        public GetImageFromUrl(URL url) {
            this.url = url;
            this.urlString = null;
        }

        public GetImageFromUrl(String urlString) {
            this.urlString = urlString;
            this.url = null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (placeHolderBmp != null) {
                imageViewReference.setImageBitmap(placeHolderBmp);
            }
        }

        @Override
        protected Object doInBackground(Object[] params) {

            if (this.urlString == null) {
                return getBitmapFromURL(this.url);
            } else {
                try {
                    return getBitmapFromURL(new URL(this.urlString));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            bmp = Bitmap.createScaledBitmap((Bitmap) o,
                    width, height, false);

            bmp = drawText(text, getCircledBitmap(bmp), textSize);

            imageViewReference.setImageBitmap(bmp);
        }
    }

    /**
     * Gets a Bitmap from a specified URL.
     *
     * @param url url from where the image can be downloaded.
     * @return The Bitmap created from the given source.
     */
    private Bitmap getBitmapFromURL(URL url) {

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            int code = connection.getResponseCode();

            if (code == 200) {

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;

                return BitmapFactory.decodeStream(connection.getInputStream(), null, options);
            } else {
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }


}
