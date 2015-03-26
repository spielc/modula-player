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

import java.io.IOException;
import java.net.URI;
import java.util.Hashtable;
import java.util.Map;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.bragi.LuceneIndexer.configuration.SimpleFSBasedLuceneIndexerConfiguration;
import org.bragi.LuceneIndexer.internal.LuceneIndexer;
import org.bragi.indexer.IndexerInterface;
import org.bragi.metadata.MetaDataEnum;
import org.bragi.metadata.MetaDataProviderInterface;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import aQute.bnd.annotation.metatype.Configurable;


/**
 * Implementation of IndexerInterface. 
 * 
 * This implementation uses Lucene as index. To be more exact this implementation uses a SimpleFSDirectory
 * as the storage area of the index.
 * @author christoph
 *
 */
@org.osgi.service.component.annotations.Component(configurationPolicy=ConfigurationPolicy.REQUIRE, factory="SimpleFSBasedLuceneIndexerConfiguration.class",property="service.ranking=2")
public class SimpleFSBasedLuceneIndexer implements IndexerInterface {
	
	private LuceneIndexer indexer;
	private String pid;
	private MetaDataProviderInterface metaDataProvider;
	
	/**
	 * Constructor
	 */
	public SimpleFSBasedLuceneIndexer() {
		pid="";
	}
	
	/**
	 * Activates a new instance of this class. Activation is done via 
	 * @param props
	 */
	@org.osgi.service.component.annotations.Activate
	public void activate(Map<String,Object> props) {
		SimpleFSBasedLuceneIndexerConfiguration config = Configurable.createConfigurable(SimpleFSBasedLuceneIndexerConfiguration.class, props);
		try {
			indexer=new LuceneIndexer(new SimpleFSDirectory(config.path()));
			indexer.setMetaDataProvider(metaDataProvider);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		if (indexer!=null)
			indexer.setMetaDataProvider(null);
		metaDataProvider=null;
	}
	
	/* (non-Javadoc)
	 * @see org.bragi.playlist.LucenePlaylist.internal.IndexerInterface#indexUri(java.lang.String)
	 */
	@Override
	public boolean indexUri(String uri) {
		return (indexer==null)?false:indexer.indexUri(uri);
	}
	
	/* (non-Javadoc)
	 * @see org.bragi.playlist.LucenePlaylist.internal.IndexerInterface#removeUri(java.lang.String)
	 */
	@Override
	public void removeUri(String uri) throws ParseException, IOException {
		if (indexer!=null)
			indexer.removeUri(uri);
	}
	
	/* (non-Javadoc)
	 * @see org.bragi.playlist.LucenePlaylist.internal.IndexerInterface#closeIndexWriter()
	 */
	@Override
	public void closeIndexWriter() throws IOException {
		if (indexer!=null)
			indexer.closeIndexWriter();
	}
	
	@Override
	public String getId() {
		return pid;
	}

	@Override
	public Map<URI, Map<MetaDataEnum, String>> filter(String query,
			MetaDataEnum... metaData) throws Exception {
		Map<URI, Map<MetaDataEnum, String>> retValue=new Hashtable<>();
		if (indexer!=null)
			retValue=indexer.filter(query, metaData);
		return retValue;
	}
	
	
}
