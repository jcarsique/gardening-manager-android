package org.gots.seed.providers.prestashop;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "language", strict = false)
public class LanguageString {
	@Element
	String language;
	
	public String getTranstaledString(){
		return language;
	}
}
