package com.devil.utils;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public final class XmlW3cUtil {

	public static Document getXmlDocFromFile(Reader reader) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		Document doc = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(new InputSource(reader));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return doc;
	}

	public static String toPrettyXml(Document doc) {
		StringWriter sw = null;
		try {
			sw = new StringWriter();
			writeXml(doc, sw);
			return sw.toString();
		} finally {
			CommUtil.close(sw);
		}
	}

	public static void writeXml(Document doc, Writer writer) {
		try {
			StreamResult strResult = new StreamResult(writer);
			TransformerFactory tfac = TransformerFactory.newInstance();
			javax.xml.transform.Transformer t = tfac.newTransformer();
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(OutputKeys.METHOD, "xml"); // xml, html,
			// text
			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			t.transform(new DOMSource(doc.getDocumentElement()), strResult);
		} catch (TransformerException e) {
			throw new IllegalStateException(e);
		}
	}
}
