package org.freedesktop.icons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Properties;

/**
 * Extension of {@link DefaultIconService} that could be used on a Linux system.
 * Adds <i>/usr/share/icons</i> and <i>${HOME}/.icons</i> as default base
 * directories.
 * <p>
 * Tested on Ubuntu 8.04 LTS.
 */
public class LinuxIconService extends DefaultIconService {

	public LinuxIconService() throws IOException, ParseException {
		super();
		checkAndAddBase(new File(System.getProperty("user.home")
				+ File.separator + ".local"+ File.separator + "share"+ File.separator + "icons"));
		checkAndAddBase(new File(System.getProperty("user.home")
				+ File.separator + ".icons"));
		checkAndAddBase(new File("/usr/share/icons"));
	}

	protected IconTheme getDefaultTheme() {
		IconTheme theme = null;
		// String selectedThemeName =
		// getGConfValue("/desktop/gnome/interface/icon_theme", null);
		String selectedThemeName = null;

		// Cinnamon
		String cinnamon = System.getenv("CINNAMON_VERSION");
		if (cinnamon != null) {
			DConf node = DConf.$.node("org/cinnamon/desktop/interface/");
			DConf.Entry<String> se = node.getStringEntry("icon-theme");
			if (se.isExists()) {
				selectedThemeName = se.getValue();
			}
		}

		// GTK3 can store icon theme here
		if (selectedThemeName == null) {
			File gtk3 = new File(new File(new File(new File(
					System.getProperty("user.home")), ".config"), "gtk-3.0"),
					"settings.ini");
			if (gtk3.exists()) {
				try {
					FileInputStream fin = new FileInputStream(gtk3);
					try {
						Properties p = new Properties();
						p.load(fin);
						selectedThemeName = p
								.getProperty("gtk-icon-theme-name");
					} finally {
						fin.close();
					}
				} catch (IOException ioe) {
				}
			}
		}

		if (selectedThemeName == null) {
			// Use the pretty dumb GConf class
			GConf node = GConf.$.node("desktop").node("gnome")
					.node("interface");
			GConf.Entry<String> en = node.getStringEntry("icon_theme");
			selectedThemeName = en == null ? null : en.getValue();

			if (selectedThemeName == null) {
				// Maybe gconf-tool is installed
				selectedThemeName = getGConfValue(
						"/desktop/gnome/interface/icon_theme", null);
			}
		}

		// Load the theme, or fallback to a default that may be installed
		if (selectedThemeName != null) {
			theme = getEntity(selectedThemeName);
			if (theme == null) {

				if (defaultThemeName != null) {
					theme = getEntity(defaultThemeName);
					if (theme != null) {
						return theme;
					}
				}

				for (String name : new String[] { "Tango", "Human", "Humanity" }) {
					theme = getEntity(name);
					if (theme != null) {
						break;
					}
				}
			}
		}

		// Finally use the default supplied tango theme
		if (theme == null) {
			theme = super.getDefaultTheme();
		}
		return theme;
	}

	private String getGConfValue(String key, String defaultValue) {
		ProcessBuilder bui = new ProcessBuilder("gconftool", "--get", key);
		try {
			Process p = bui.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			try {
				String val = reader.readLine();
				p.waitFor();
				if (val != null && !val.equals("")) {
					return val;
				}
			} finally {
				reader.close();
			}
		} catch (IOException ioe) {
			System.out
					.println("WARNING: Failed to get gconf value. Using default of "
							+ defaultValue);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	public static void main(String[] args) throws Exception {
		new LinuxIconService().postInit();;
	}
}
