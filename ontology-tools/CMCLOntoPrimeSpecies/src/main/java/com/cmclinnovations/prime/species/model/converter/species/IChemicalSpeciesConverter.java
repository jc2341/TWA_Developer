package com.cmclinnovations.prime.species.model.converter.species;

import org.xml.sax.Attributes;

public interface IChemicalSpeciesConverter {
	public void parse(String qName, Attributes attributes);
}
