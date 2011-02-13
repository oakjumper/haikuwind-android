package com.haikuwind.feed;

import static com.haikuwind.feed.XmlTags.FAVORITED_BY_ME;
import static com.haikuwind.feed.XmlTags.POINTS;
import static com.haikuwind.feed.XmlTags.TEXT;
import static com.haikuwind.feed.XmlTags.TIME;
import static com.haikuwind.feed.XmlTags.TIMES_VOTED_BY_ME;
import static com.haikuwind.feed.XmlTags.USER;
import static com.haikuwind.feed.XmlTags.USER_RANK;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class HaikuHandler extends DefaultHandler {
	private static String TAG = HaikuHandler.class.getName();
	
	private List<Haiku> haikuList;
	private Haiku currentHaiku;
	
	public List<Haiku> getHaikuList() {
		return haikuList;
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (XmlTags.HAIKU.equalsIgnoreCase(localName)) {
			haikuList.add(currentHaiku);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		Log.d(TAG, "start: "+localName);
		
		if (XmlTags.ANSWER.equalsIgnoreCase(localName)) {
			haikuList = new ArrayList<Haiku>();
		} else if (XmlTags.HAIKU.equalsIgnoreCase(localName)) {
			currentHaiku = new Haiku();
			
			try {
				currentHaiku.setText(attributes.getValue(TEXT));
				currentHaiku.setFavoritedByMe(Boolean.parseBoolean(attributes.getValue(FAVORITED_BY_ME)));
				currentHaiku.setPoints(Integer.parseInt(attributes.getValue(POINTS)));
				currentHaiku.setTime(new Date(Long.parseLong(attributes.getValue(TIME))));
				currentHaiku.setTimesVotedByMe(Integer.parseInt(attributes.getValue(TIMES_VOTED_BY_ME)));
				currentHaiku.setUser(attributes.getValue(USER));
				currentHaiku.setUserRank(Integer.parseInt(attributes.getValue(USER_RANK)));
				
				Log.d(TAG, currentHaiku.toString());
			} catch (NumberFormatException e) {
				Log.e(TAG, "incorrect value in XML", e);
			}
		}
	}
	
	
}