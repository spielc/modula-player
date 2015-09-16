package org.bragi.query;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.bragi.metadata.MetaDataEnum;

public interface QueryParserInterface {
	public List<QueryResult> execute(String query, Map<URI,Map<MetaDataEnum,String>> metaData) throws ParseException;
}