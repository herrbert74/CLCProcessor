package hu.herrbert74.osm.clcprocessor.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class InputFileChooserModel extends java.util.Observable {

	String villageBorderFile;
	String clcFile;

	public InputFileChooserModel() {
		super();
		String[] preferences;
		try {
			preferences = readPreferences();
			this.villageBorderFile = preferences[0];
			this.clcFile = preferences[1];
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public String[] readPreferences() throws ParserConfigurationException, SAXException, IOException {

		ArrayList<String> result = new ArrayList<String>();
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();

		PreferencesHandler preferencesHandler = new PreferencesHandler();
		parser.parse("preferences.xml", preferencesHandler);
		result = preferencesHandler.getPreferences();

		String[] stringResult = new String[result.size()];
		return result.toArray(stringResult);
	}

	private static class PreferencesHandler extends DefaultHandler {
		ArrayList<String> preferences = new ArrayList<String>();
		private final Stack<String> eleStack = new Stack<String>();
		StringBuffer buffer;

		public ArrayList<String> getPreferences() {
			return preferences;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
			if ("setting".equals(qName)) {
				buffer = new StringBuffer();
			}
			eleStack.push(qName);

		}

		public void characters(char buf[], int offset, int len) throws SAXException {
			String s = new String(buf, offset, len);
			if (buffer != null) {
				buffer.append(s);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			eleStack.pop();

			if ("setting".equals(qName)) {
				if (buffer != null) {
					preferences.add(buffer.toString());
					buffer = null;
				}
			}

		}
	}

}
