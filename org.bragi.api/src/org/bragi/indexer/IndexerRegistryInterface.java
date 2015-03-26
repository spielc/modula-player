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

import java.util.Set;

/**
 * 
 * @author christoph
 *
 */
public interface IndexerRegistryInterface {
	
	/**
	 * Get a List of id-Strings of all registered indexers
	 * @return a List of all registered indexers
	 */
	public Set<String> getRegisteredIndexerIds();
	
	/**
	 * Get the IndexerInterface-implementation with the specified id 
	 * @param indexerId the id of the indexer
	 * @return either null if there is no indexer with the given id registered or the correct instance
	 */
	public IndexerInterface getIndexer(String indexerId);

}