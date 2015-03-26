/**
 * 
 */
package org.bragi.player.helpers;

import java.net.URI;
import java.util.Map;

import org.bragi.metadata.MetaDataEnum;

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
	public static String QueryResult2String(Map<URI, Map<MetaDataEnum, String>> filteredCollection) {
		StringBuffer buffer=new StringBuffer();
		for (Map.Entry<URI, Map<MetaDataEnum, String>> filteredCollectionEntry : filteredCollection.entrySet()) {
			buffer.append("URI='"+filteredCollectionEntry.getKey().toString()+"'");
			for (Map.Entry<MetaDataEnum, String> metaData : filteredCollectionEntry.getValue().entrySet()) {
				buffer.append(";;");
				buffer.append(metaData.getKey().name()+"='"+metaData.getValue()+"'");
			}
			buffer.append("\n");
		}
		return buffer.toString();
	}
}
