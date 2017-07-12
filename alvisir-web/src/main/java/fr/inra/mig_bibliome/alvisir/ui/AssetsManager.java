package fr.inra.mig_bibliome.alvisir.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

class AssetsManager /* "assets management" lol */ {
	private static final String LOGO_FILENAME = "alvis";
	private static final String LOGO_DEFAULT_RESOURCE = "/images/alvis.png";
	private static final String LOGO_DISPLAY_PARAM = "logo-png-path";
	
	private static final String BACKGROUND_FILENAME = "background";
	private static final String BACKGROUND_DEFAULT_RESOURCE = "/images/background.jpg";
	private static final String BACKGROUND_DISPLAY_PARAM = "background-jpg-path";
	
	private static final String BACKGROUND_LEFT_FILENAME = "background-left";
	private static final String BACKGROUND_LEFT_DEFAULT_RESOURCE = "/images/background-left.jpg";
	private static final String BACKGROUND_LEFT_DISPLAY_PARAM = "background-left-jpg-path";
	
	private static final String BACKGROUND_RIGHT_FILENAME = "background-right";
	private static final String BACKGROUND_RIGHT_DEFAULT_RESOURCE = "/images/background-right.jpg";
	private static final String BACKGROUND_RIGHT_DISPLAY_PARAM = "background-right-jpg-path";
	
	private static final String CHEATSHEET_FILENAME = "cheatsheet";
	private static final String CHEATSHEET_DEFAULT_RESOURCE = "/html/cheatsheet.html";
	private static final String CHEATSHEET_DISPLAY_PARAM = "cheatsheet-html-path";
	
	static InputStream getDataStream(ServletContext context, Configuration uiConfig, String filename) {
		switch (filename) {
			case LOGO_FILENAME: return getDataStream(context, uiConfig, LOGO_DISPLAY_PARAM, LOGO_DEFAULT_RESOURCE);
			case BACKGROUND_FILENAME: return getDataStream(context, uiConfig, BACKGROUND_DISPLAY_PARAM, BACKGROUND_DEFAULT_RESOURCE);
			case BACKGROUND_LEFT_FILENAME: return getDataStream(context, uiConfig, BACKGROUND_LEFT_DISPLAY_PARAM, BACKGROUND_LEFT_DEFAULT_RESOURCE);
			case BACKGROUND_RIGHT_FILENAME: return getDataStream(context, uiConfig, BACKGROUND_RIGHT_DISPLAY_PARAM, BACKGROUND_RIGHT_DEFAULT_RESOURCE);
			case CHEATSHEET_FILENAME: return getDataStream(context, uiConfig, CHEATSHEET_DISPLAY_PARAM, CHEATSHEET_DEFAULT_RESOURCE);
		}
		return null;
	}
	
	private static InputStream getDataStream(ServletContext context, Configuration uiConfig, String displayParam, String defaultResource) {
		Map<String,String> displayConfig = uiConfig.getDisplayParams();
		if (displayConfig.containsKey(displayParam)) {
			String dataPath = displayConfig.get(displayParam);
			File dataFile = new File(dataPath);
			if (!dataFile.isAbsolute()) {
				String base = uiConfig.getBasedir();
				dataFile = new File(base, dataPath);
			}
			try {
				return new FileInputStream(dataFile);
			}
			catch (FileNotFoundException e) {
	            Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, "could not open " + dataPath, e);
			}
		}
		return context.getResourceAsStream(defaultResource);
	}
}
