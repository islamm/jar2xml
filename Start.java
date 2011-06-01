package jar2xml;

import java.io.File;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Start {

	public static void main (String[] args)
	{
		String docs = null;
		String path = null;
		String usage = "Usage: jar2xml <jarfile> [--docpath=<javadocs>]";

		for (String arg : args) {
			if (arg.startsWith ("--docpath=")) {
				docs = arg.substring (10);
			} else if (arg.endsWith (".jar")) {
				path = arg;
			} else {
				System.err.println (usage);
				System.exit (1);
			}
		}

		if (path == null) {
			System.err.println (usage);
			System.exit (1);
		}

		JavaArchive jar = null;
		try {
			jar = new JavaArchive (path);
		} catch (Exception e) {
			System.err.println ("Couldn't open java archive at specified path " + path);
			System.exit (1);
		}

		try {
			if (docs != null)
				JavaClass.addDocScraper (new AndroidDocScraper (new File (docs)));
		} catch (Exception e) {
			System.err.println ("Couldn't access javadocs at specified docpath.  Continuing without it...");
		}

		Document doc = null;
		try {
			DocumentBuilderFactory builder_factory = DocumentBuilderFactory.newInstance ();
			DocumentBuilder builder = builder_factory.newDocumentBuilder ();
			doc = builder.newDocument ();
		} catch (Exception e) {
			System.err.println ("Couldn't create xml document - exception occurred:" + e.getMessage ());
		}

		Element root = doc.createElement ("api");
		doc.appendChild (root);
		for (JavaPackage pkg : jar.getPackages ())
			pkg.appendToDocument (doc, root);

		try {
			// Boilerplate much?
			TransformerFactory transformer_factory = TransformerFactory.newInstance ();
			Transformer transformer = transformer_factory.newTransformer ();
			transformer.setOutputProperty (OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty (OutputKeys.INDENT, "yes");
			StringWriter writer = new StringWriter ();
			StreamResult result = new StreamResult (writer);
			DOMSource source = new DOMSource (doc);
			transformer.transform (source, result);
			System.out.println (writer.toString ());
		} catch (Exception e) {
			System.err.println ("Couldn't format xml file - exception occurred:" + e.getMessage ());
		}
	}
}

