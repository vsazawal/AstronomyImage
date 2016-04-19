package edu.umd.cs.astronomyimage;


import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.lang3.StringEscapeUtils;


public class MainActivity extends AppCompatActivity {

    private String NASA_IMAGE = "nasaimage.jpg";
    private TextView mTextView;
    private ImageView mImageView;
    private ImageOfTheDay mImage;
    private String mDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ContextWrapper cs = new ContextWrapper(getApplicationContext());
        File path = cs.getFilesDir();
        try {
            mDirectory = path.getCanonicalPath();
            Log.d("oncreate", mDirectory);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        mImage = null;
        mTextView = (TextView) findViewById(R.id.astronomy_text);
        mImageView = (ImageView) findViewById(R.id.astronomy_image);
        new RSSItemReadTask().execute("http://www.nasa.gov/rss/dyn/lg_image_of_the_day.rss");


    }


    public class RSSItemReadTask extends AsyncTask<String, Void, ImageOfTheDay> {

        private String mXMLItem;

        public RSSItemReadTask() {
            mXMLItem = "";

        }

        @Override
        protected ImageOfTheDay doInBackground(String... params) {
            String urlString = params[0];
            downloadFirstItemFromFile(urlString);
            return parseXMLItem();
        }

        protected void downloadFirstItemFromFile(String rss_url) {
            try {
                URL url = new URL(rss_url);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                InputStream in = connection.getInputStream();
                byte[] buffer = new byte[1024];
                int bytesRead = in.read(buffer);
                mXMLItem = "";
                while (bytesRead != -1) {
                    String text = new String(buffer);
                    mXMLItem = mXMLItem + text;
                    if (!(text.contains("</item>"))){
                        bytesRead = in.read(buffer);
                    }
                    else {
                        break;
                    }

                }
                in.close();

            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

        protected ImageOfTheDay parseXMLItem() {
            int start = mXMLItem.indexOf("<item>");
            int end = mXMLItem.indexOf("</item>");
            mXMLItem = mXMLItem.substring(start, end);
            mXMLItem = StringEscapeUtils.unescapeHtml4(mXMLItem);
            Log.d("PARSEXML", Integer.toString(end));
            String description_start = "<description>";
            String description_end = "</description>";
            int start_index = mXMLItem.indexOf(description_start);
            int end_index = mXMLItem.indexOf(description_end);
            Log.d("PARSEXML", Integer.toString(end_index));

            String description = mXMLItem.substring(start_index + description_start.length(),
                                            end_index);

            start_index = mXMLItem.indexOf("url");
            end_index = mXMLItem.indexOf("jpg");
            String image_url = mXMLItem.substring(start_index + 5, end_index + 3);
            Bitmap bmp = getBitmap(image_url);
            return new ImageOfTheDay(description, bmp);
        }

        private Bitmap getBitmap(String url) {

            Bitmap bmp = null;
            try {
                URL real_url = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) real_url.openConnection();
                InputStream in = connection.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                FileOutputStream out = openFileOutput(NASA_IMAGE, Context.MODE_PRIVATE);
                byte[] buffer = new byte[1024];
                int bytesRead = in.read(buffer);
                while (bytesRead != -1) {
                    out.write(buffer, 0, bytesRead);
                    bytesRead = in.read(buffer);
                }

                in.close();
                out.close();
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
                bmp = BitmapFactory.decodeFile(mDirectory + "/" + NASA_IMAGE);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            return bmp;
        }

        protected void onPostExecute(ImageOfTheDay img) {
            mImageView.setImageBitmap(img.getImage());
            mTextView.setText(img.getDescription());

        }
    }
}
