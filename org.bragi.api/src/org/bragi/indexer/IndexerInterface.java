/**
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU
 * Lesser General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>
 */
package org.bragi.indexer;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.bragi.metadata.MetaDataEnum;
import org.bragi.metadata.MetaDataProviderInterface;

/**
 * 
 * @author christoph
 *
 */
public interface IndexerInterface {

	/**
	 * Add the given URI to the index	
	 * @param uri the URI as String
	 * @return true if indexing of the given URI was successful an false otherwise
	 */
	public boolean indexUri(String uri);

	/**
	 * Try to remove the given URI from the index
	 * @param uri, the URI as String
	 * @throws Exception thrown if something goes wrong
	 */
	public void removeUri(String uri) throws Exception;

	/**
	 * Try to close the Indexer
	 * @throws IOException thrown if something goes wrong
	 */
	public void closeIndexWriter() throws Exception;

	/**
	 * Filter the index with the given Query.
	 * @param query the Query used to filter
	 * @param metaData the metaData we are interested in
	 * @return a List<Dictionary<MetaDataEnum,String>> containing a List of Dictionary-objects that match the filter-expression
	 * @throws Exception
	 */
	public Map<URI,Map<MetaDataEnum,String>> filter(String query, MetaDataEnum... metaData) throws Exception;
	
	/**
	 * 
	 * @param pMetaDataProvider
	 */
	public void unsetMetaDataProvider(MetaDataProviderInterface pMetaDataProvider);

	/**
	 * 
	 * @param pMetaDataProvider
	 */
	public void setMetaDataProvider(MetaDataProviderInterface pMetaDataProvider);

}