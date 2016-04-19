package edu.umd.cs.astronomyimage;

import android.graphics.Bitmap;
import java.text.DateFormat;

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Created by vibha on 4/18/16.
 */
public class ImageOfTheDay {

    private String description;
    private String URL;


    public ImageOfTheDay(String desc, String link) {
        description = desc;
        URL = link;
    }

    public String getDescription() {
        return description;
    }


    public Bitmap getImage() {

        //todo here
        return null;

    }

}
