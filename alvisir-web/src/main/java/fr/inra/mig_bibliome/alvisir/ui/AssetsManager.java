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
	static final String LOGO_DEFAULT_RESOURCE = "/images/alvis.png";
	static final String LOGO_DISPLAY_PARAM = "logo-png-path";
	
	static InputStream getLogo(ServletContext context, Configuration uiConfig) {
		return getDataStream(context, uiConfig, LOGO_DISPLAY_PARAM, LOGO_DEFAULT_RESOURCE);
	}
	
	static InputStream getDataStream(ServletContext context, Configuration uiConfig, String displayParam, String defaultResource) {
		Map<String,String> displayConfig = uiConfig.getDisplayParams();
		if (displayConfig.containsKey(displayParam)) {
			String dataPath = displayConfig.get(displayParam);
			File dataFile = new File(dataPath);
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
