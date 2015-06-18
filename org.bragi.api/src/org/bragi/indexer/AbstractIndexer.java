/**
 * 
 */
package org.bragi.indexer;

import java.io.IOException;
import java.net.URI;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.bragi.metadata.MetaDataEnum;
import org.bragi.metadata.MetaDataProviderInterface;

/**
 * @author christoph
 *
 */
public abstract class AbstractIndexer {
	/**
	 * Add the given URI to the index	
	 * @param uri the URI as String
	 * @return true if indexing of the given URI was successful an false otherwise
	 */
	public abstract boolean indexUri(String uri);

	/**
	 * Try to remove the given URI from the index
	 * @param uri, the URI as String
	 * @throws Exception thrown if something goes wrong
	 */
	public abstract void removeUri(String uri) throws Exception;

	/**
	 * Try to close the Indexer
	 * @throws IOException thrown if something goes wrong
	 */
	public abstract void closeIndexWriter() throws Exception;
	
	protected abstract Map<URI,Map<MetaDataEnum,String>> fetch();

	/**
	 * Filter the index with the given Query.
	 * @param query the Query used to filter
	 * @param metaData the metaData we are interested in
	 * @return a List<Dictionary<MetaDataEnum,String>> containing a List of Dictionary-objects that match the filter-expression
	 * @throws Exception
	 */
	public final Map<URI,Map<MetaDataEnum,String>> filter(String query) throws Exception {
		QueryParser parser=new QueryParser(this);
		return parser.execute(query);
	}
	
	/**
	 * 
	 * @param pMetaDataProvider
	 */
	public abstract void unsetMetaDataProvider(MetaDataProviderInterface pMetaDataProvider);

	/**
	 * 
	 * @param pMetaDataProvider
	 */
	public abstract void setMetaDataProvider(MetaDataProviderInterface pMetaDataProvider);
}
