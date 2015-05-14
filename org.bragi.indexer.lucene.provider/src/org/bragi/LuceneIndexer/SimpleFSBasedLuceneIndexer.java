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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.lucene.store.SimpleFSDirectory;
import org.bragi.LuceneIndexer.internal.LuceneIndexer;
import org.bragi.indexer.IndexerInterface;
import org.bragi.metadata.MetaDataProviderInterface;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;


/**
 * Implementation of IndexerInterface. 
 * 
 * This implementation uses Lucene as index. To be more exact this implementation uses a SimpleFSDirectory
 * as the storage area of the index.
 * @author christoph
 *
 */
@Component(name="org.bragi.LuceneIndexer.SimpleFSBasedLuceneIndexer", configurationPolicy=ConfigurationPolicy.REQUIRE, property="service.ranking=2")
public class SimpleFSBasedLuceneIndexer extends AbstractLuceneIndexer implements IndexerInterface {
	
	/**
	 * Constructor
	 */
	public SimpleFSBasedLuceneIndexer() {
		super();
	}
	
	@Modified
	public void modified(Map<String,Object> props) {
		try {
			File path=new File(props.get("path").toString());
			indexer=new LuceneIndexer(new SimpleFSDirectory(path));
			indexer.setMetaDataProvider(metaDataProvider);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@org.osgi.service.component.annotations.Reference
	public void setMetaDataProvider(MetaDataProviderInterface pMetaDataProvider) {
		if (indexer!=null)
			indexer.setMetaDataProvider(pMetaDataProvider);
		metaDataProvider=pMetaDataProvider;
		
	}

	public void unsetMetaDataProvider(MetaDataProviderInterface pMetaDataProvider) {
		setMetaDataProvider(null);
	}
	
}
