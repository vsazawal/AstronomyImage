package edu.umd.cs.astronomyimage;


import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.lang3.StringEscapeUtils;


public class MainActivity extends AppCompatActivity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.astronomy_text);
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

        @Override
        protected void onPostExecute(ImageOfTheDay result) {
            Log.d("onPostExecute", result.getDescription());
            mTextView.setText(result.getDescription());
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
            return new ImageOfTheDay(description, null);
        }

    }
}
