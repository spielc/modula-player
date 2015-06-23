package org.bragi.query;

import java.net.URI;
import java.util.Map;

import org.bragi.metadata.MetaDataEnum;

public interface QueryParserInterface {
	public Map<URI,Map<MetaDataEnum,String>> execute(String query, Map<URI,Map<MetaDataEnum,String>> metaData) throws ParseException;
}