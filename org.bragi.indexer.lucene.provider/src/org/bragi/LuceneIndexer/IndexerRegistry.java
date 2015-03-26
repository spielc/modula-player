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
package org.bragi.LuceneIndexer;

import java.util.HashMap;
import java.util.Set;

import org.bragi.indexer.IndexerInterface;
import org.bragi.indexer.IndexerRegistryInterface;

/**
 * @author christoph
 *
 */
@org.osgi.service.component.annotations.Component
public class IndexerRegistry implements IndexerRegistryInterface {
	
	private HashMap<String,IndexerInterface> registeredIndexers;
	
	@org.osgi.service.component.annotations.Reference
	public void addIndexer(IndexerInterface indexer) {
		if (indexer!=null)
			registeredIndexers.put(indexer.getId(), indexer);
	}
	public void removeIndexer(IndexerInterface indexer) {
		if (indexer!=null)
			registeredIndexers.remove(indexer.getId());
	}
	
	public IndexerRegistry() {
		registeredIndexers=new HashMap<>();
	}

	/* (non-Javadoc)
	 * @see org.bragi.indexer.IndexerSelectorInterface#GetRegisteredIndexerIds()
	 */
	@Override
	public Set<String> getRegisteredIndexerIds() {
		return registeredIndexers.keySet();
	}

	/* (non-Javadoc)
	 * @see org.bragi.indexer.IndexerSelectorInterface#GetIndexer(java.lang.String)
	 */
	@Override
	public IndexerInterface getIndexer(String indexerId) {
		return registeredIndexers.get(indexerId);
	}

}
