/**
 * 
 */
package org.bragi.player.helpers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bragi.collection.CollectionEntry;

/**
 * @author christoph
 *
 */
public final class QueryHelpers {
	
	public static String extractValueFromLine(String header, String line) {
		Pattern pattern=Pattern.compile(header+"='([^;;]*)'");
		Matcher matcher=pattern.matcher(line);
		if (matcher.find()) 
			return matcher.group(1);
		else
			return "";
	}
	
	/**
	 * 
	 * @param filteredCollection
	 * @return
	 */
	public static String QueryResult2String(List<CollectionEntry> filteredCollection) {
		return filteredCollection.stream().map(entry->"URI='"+entry.getUri().toString()+"'"+entry.getMetaData().entrySet().stream().map(metaData->";;"+metaData.getKey().name()+"='"+metaData.getValue()+"'").collect(Collectors.joining())).collect(Collectors.joining("\n"));
	}
}
