package org.freedesktop.icons;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GConf {

	private final static File DIR = new File(new File(
			System.getProperty("user.home")), ".gconf");

	public class Entry<T> {
		private Class<T> type;
		private long mtime;
		private String name;
		private List<T> values = new ArrayList<T>();

		private Entry(Class<T> type, long mtime, String name) {
			this.type = type;
			this.mtime = mtime;
			this.name = name;
		}

		@SuppressWarnings("unchecked")
		private Entry(Node n) {
			name = n.getAttributes().getNamedItem("name").getTextContent();
			mtime = Long.parseLong(n.getAttributes().getNamedItem("mtime")
					.getTextContent());
			String typeName = n.getAttributes().getNamedItem("type")
					.getTextContent();
			if (typeName.equals("string")) {
				type = (Class<T>) String.class;
				NodeList cn = n.getChildNodes();
				for (int i = 0; i < cn.getLength(); i++) {
					Node cc = cn.item(i);
					if (cc.getNodeName().equals("stringvalue")) {
						values.add((T) cc.getTextContent());
					} else {
						// TODO more
					}
				}
			} else {
				// TODO more
			}
		}

		public List<T> getValues() {
			return values;
		}

		public Class<?> getType() {
			return type;
		}

		public long getMtime() {
			return mtime;
		}

		public String getName() {
			return name;
		}

		public T getValue() {
			return values.size() > 0 ? values.get(0) : null;
		}
	}

	public final static GConf $ = new GConf(DIR, "/");

	private File dir;
	private Map<String, GConf> children = new HashMap<String, GConf>();
	private String path;
	private Map<String, Entry<?>> values = new HashMap<String, Entry<?>>();

	private GConf(File dir, String path) {
		this.dir = dir;
		this.path = path;

		if (dir.exists()) {
			File f = new File(dir, "%gconf.xml");
			if (f.exists() && f.length() > 0) {
				try {
					DocumentBuilder db = DocumentBuilderFactory.newInstance()
							.newDocumentBuilder();
					Document s = db.parse(f);
					NodeList nl = s.getElementsByTagName("entry");
					for (int i = 0; i < nl.getLength(); i++) {
						Node n = nl.item(i);
						Entry<Object> en = new Entry<Object>(n);
						values.put(en.name, en);
					}
				} catch (ParserConfigurationException e) {
					System.err.println("Failed to read " + f);
					e.printStackTrace();
				} catch (SAXException e) {
					System.err.println("Failed to read " + f);
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					System.err.println("Failed to read " + f);
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public Entry<?> getEntry(String name) {
		return values.get(name);
	}

	public GConf node(String name) {
		GConf n = children.get(name);
		if (n == null) {
			n = new GConf(new File(dir, name), path
					+ (path.equals("/") ? "" : "/") + name);
			children.put(name, n);
		}
		return n;
	}

	@SuppressWarnings("unchecked")
	public Entry<String> getStringEntry(String name) {
		return (Entry<String>) values.get(name);
	}
}
