package com.haikuwind.feed.fetch;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.util.Log;

import com.haikuwind.feed.Haiku;
import com.haikuwind.feed.HaikuWindData;
import com.haikuwind.feed.UserInfo;
import com.haikuwind.feed.parser.HaikuHandler;
import com.haikuwind.feed.parser.ResultHandler;
import com.haikuwind.feed.parser.XmlCorrectorStream;

public class HttpRequest {
    private final static String TAG = HttpRequest.class.getSimpleName();
    private final static String HW_ADDR = "http://192.168.4.134:8888/haiku";
//    private final static String HW_ADDR = "http://www.haikuwind.com/haiku";

    public static void newUser(String userId) throws FeedException {
        // http://localhost:8080/haiku?command=new_user&id=ABCD
        String url = String
                .format("%s?command=new_user&id=%s", HW_ADDR, userId);
        parseResult(url);
    }

    public static void newHaiku(CharSequence haiku, String userId) throws FeedException {
        // http://localhost:8080/haiku?command=new_text&user=1&haiku=.......
        String url = String.format("%s?command=new_text&user=%s&haiku=%s",
                HW_ADDR, userId, URLEncoder.encode(haiku.toString()));
        parseResult(url);
    }

    public static List<Haiku> getTimeline(long from, String userId) throws FeedException {
        // http://localhost:8080/haiku?command=refresh&user=1&from=1
        String url = String.format("%s?command=refresh&user=%s&from=%d",
                HW_ADDR, userId, from);
        return parseHaikuList(url, true);
    }

    public static List<Haiku> getTop(int limit, String userId) throws FeedException {
        // http://localhost:8080/haiku?command=top&user=1&limit=25
        String url = String.format("%s?command=top&user=%s&limit=%d", HW_ADDR,
                userId, limit);
        //sort by votes, not by time
        return parseHaikuList(url, false);
    }

    public static List<Haiku> getHallOfFame(String userId) throws FeedException {
        // http://localhost:8080/haiku?command=hall_of_fame&user=1
        String url = String.format("%s?command=hall_of_fame&user=%s", HW_ADDR,
                userId);
        return parseHaikuList(url, true);
    }

    public static List<Haiku> getFavorite(String userId) throws FeedException {
        // http://localhost:8080/haiku?command=my_favorite&user=1
        String url = String.format("%s?command=my_favorite&user=%s", HW_ADDR,
                userId);
        return parseHaikuList(url, true);
    }

    public static List<Haiku> getMy(String userId) throws FeedException {
        // http://localhost:8080/haiku?command=my&user=1
        String url = String.format("%s?command=my&user=%s", HW_ADDR, userId);
        return parseHaikuList(url, true);
    }
    
    public static void vote(String textId, boolean isGood, String userId) throws FeedException {
//  http://localhost:8080/haiku?command=vote&user=1&text=1&vote=yes / or 'no'
        String url = String.format("%s?command=vote&user=%s&text=%s&vote=%s", 
                HW_ADDR, userId, textId, isGood? "yes" : "no");
        parseResult(url);
    }
    
    public static void favorite(String textId, String userId) throws FeedException {
//    http://localhost:8080/haiku?command=favorite&user=1&text=1
        String url = String.format("%s?command=favorite&user=%s&text=%s", HW_ADDR, userId, textId);
        parseResult(url);
    }

    /**
     * Side effect is: if user info received, it updates {@link UserInfoHolder}
     * @throws FeedException 
     */
    private static List<Haiku> parseHaikuList(String url, boolean sort) throws FeedException {
        HaikuHandler handler = new HaikuHandler();
        parse(url, handler);

        //TODO ask user info directly
        UserInfo user = handler.getUseInfo();
        if (user != null) {
            HaikuWindData.getInstance().setUserInfo(user);
        }
        List<Haiku> result = handler.getHaikuList();
        if(sort) {
            //newer first
            Collections.sort(result, new NewerFirstComparator());
        }
        return result;
    }

    private static void parseResult(String url) throws FeedException {
        ResultHandler handler = new ResultHandler();
        parse(url, handler);
        
        if(!handler.getResult()) {
            throw new FeedException("Received error from server");
        }

    }

    /**
     * Central point to process http request.
     * @throws FeedException 
     */
    private static void parse(String url, ContentHandler handler) throws FeedException {
        InputStream xmlStream = null;
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
    
            xr.setContentHandler(handler);
    
            Log.d(TAG, url);
            URLConnection connection = new URL(url).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
//            used for old version of server with invalid xml
            xmlStream = new XmlCorrectorStream(connection.getInputStream());
//            xmlStream = connection.getInputStream();
            xr.parse(new InputSource(xmlStream));
        } catch(Exception e) {
            Log.e(TAG, "Error occured while treating the request", e);
            throw new FeedException(e);
        } finally {
            try {
                if(xmlStream!=null) {
                    xmlStream.close();
                }
            } catch(IOException e) {
                throw new FeedException(e);
            }
        }
    }


}
