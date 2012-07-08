package hu.herrbert74.osm.clcprocessor.utils;

import hu.herrbert74.osm.clcprocessor.osmentities.CustomNode;

import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

public class XMLFactory {
	public static void writePolygon(ArrayList<CustomNode> polygon, String filename) {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer;
		FileOutputStream stream;
		try {
			stream = new FileOutputStream("c:\\osm\\" + filename);
		} catch (Exception e) {
			System.out.append("nincs file");
			return;
		}
		try {
			writer = factory.createXMLStreamWriter(stream);

			writer.writeStartDocument("UTF-8", "1.0");

			writer.writeStartElement("osm");
			writer.writeAttribute("version", "0.6");
			writer.writeAttribute("upload", "true");
			writer.writeAttribute("generator", "JOSM");
			writer.writeCharacters("\r\n");

			for(int i = 0; i < polygon.size(); i++){
				writer.writeStartElement("node");
				writer.writeAttribute("id", "-" + Integer.toString(polygon.get(i).getNodeId()));
				writer.writeAttribute("timestamp", "2010-05-02T23:34:43Z");
				writer.writeAttribute("visible", "true");
				writer.writeAttribute("lat", Double.toString(polygon.get(i).getLat()));
				writer.writeAttribute("lon", Double.toString(polygon.get(i).getLon()));
				writer.writeEndElement();
				writer.writeCharacters("\r\n");
			}
			
			writer.writeStartElement("way");
			writer.writeAttribute("id", "-23456");
			writer.writeAttribute("timestamp", "2010-05-02T23:34:43Z");
			writer.writeAttribute("visible", "true");
			writer.writeCharacters("\r\n");

			for(int i = 0; i < polygon.size(); i++){
				writer.writeStartElement("nd");
				writer.writeAttribute("ref", "-" + Integer.toString(polygon.get(i).getNodeId()));
				writer.writeEndElement();
				writer.writeCharacters("\r\n");

			}
			writer.writeEndElement();
			writer.writeCharacters("\r\n");
			
			writer.writeEndElement();
			writer.writeEndDocument();

			writer.flush();
			writer.close();
		} catch (Exception e) {
			return;
		}
	}
}
