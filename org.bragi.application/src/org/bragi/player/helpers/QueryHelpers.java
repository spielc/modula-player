/**
 * 
 */
package org.bragi.player.helpers;

import java.util.List;
import java.util.stream.Collectors;

import org.bragi.collection.CollectionEntry;

/**
 * @author christoph
 *
 */
public final class QueryHelpers {
	
	/**
	 * 
	 * @param filteredCollection
	 * @return
	 */
	public static String QueryResult2String(List<CollectionEntry> filteredCollection) {
		return filteredCollection.stream().map(entry->"URI='"+entry.getUri().toString()+"'"+entry.getMetaData().entrySet().stream().map(metaData->";;"+metaData.getKey().name()+"='"+metaData.getValue()+"'").collect(Collectors.joining())).collect(Collectors.joining("\n"));
	}
}
