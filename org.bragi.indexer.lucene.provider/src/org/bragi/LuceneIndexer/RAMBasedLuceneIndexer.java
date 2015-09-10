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

import java.util.Map;

import org.apache.lucene.store.RAMDirectory;
import org.bragi.LuceneIndexer.internal.LuceneIndexer;
import org.bragi.indexer.IndexerInterface;
import org.bragi.metadata.MetaDataProviderInterface;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;


/**
 * @author christoph
 *
 */
@Component(name="org.bragi.LuceneIndexer.RAMBasedLuceneIndexer", configurationPolicy=ConfigurationPolicy.REQUIRE, property="service.ranking=1")
public class RAMBasedLuceneIndexer extends AbstractLuceneIndexer implements IndexerInterface {
	
	public RAMBasedLuceneIndexer() {
		super();
	}
	
	@Override
	@Modified
	public void modified(Map<String,Object> map) {
		indexer=new LuceneIndexer(new RAMDirectory());
		indexer.setMetaDataProvider(metaDataProvider);
	}
	
	@Override
	@org.osgi.service.component.annotations.Reference
	public void setMetaDataProvider(MetaDataProviderInterface pMetaDataProvider) {
		if (indexer!=null)
			indexer.setMetaDataProvider(pMetaDataProvider);
		metaDataProvider=pMetaDataProvider;
		
	}

	@Override
	public void unsetMetaDataProvider(MetaDataProviderInterface pMetaDataProvider) {
		setMetaDataProvider(null);
	}
	
}
