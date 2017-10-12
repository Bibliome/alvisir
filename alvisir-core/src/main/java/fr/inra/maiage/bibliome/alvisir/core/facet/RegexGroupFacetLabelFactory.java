package fr.inra.maiage.bibliome.alvisir.core.facet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexGroupFacetLabelFactory implements FacetLabelFactory {
	private final Pattern pattern;
	private final int labelGroup;
	
	public RegexGroupFacetLabelFactory(Pattern pattern, int labelGroup) {
		super();
		this.pattern = pattern;
		this.labelGroup = labelGroup;
	}

	@Override
	public String getFacetLabel(String text) throws Exception {
		Matcher m = pattern.matcher(text);
		if (m.find()) {
			return m.group(labelGroup);
		}
		return text;
	}

}
