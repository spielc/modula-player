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
package org.bragi.LuceneIndexer.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.bragi.indexer.IndexEntry;
import org.bragi.metadata.MetaDataEnum;
import org.bragi.metadata.MetaDataProviderInterface;

/**
 * @author christoph
 *
 */
public class LuceneIndexer {
	
	private static final String URICONSTANT = "URI";
	private static final String INSERTIONCOUNTCONSTANT = "INSERTIONCOUNT";
	private long insertionCount;
	private MetaDataProviderInterface metaDataProvider;
	private IndexWriter indexWriter;
	private static final EnumSet<MetaDataEnum> metaDataToIndexSet=EnumSet.range(MetaDataEnum.ALBUM, MetaDataEnum.URL);
	private QueryParser queryParser;
	private SearcherManager manager;	
	
	public LuceneIndexer(Directory directory) {
		try {
			insertionCount=0;
			StandardAnalyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LATEST, analyzer);
			indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			indexWriter = new IndexWriter(directory, indexWriterConfig);
			queryParser = new QueryParser(URICONSTANT, indexWriter.getAnalyzer());
			queryParser.setAllowLeadingWildcard(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public MetaDataProviderInterface getMetaDataProvider() {
		return metaDataProvider;
	}



	public void setMetaDataProvider(MetaDataProviderInterface metaDataProvider) {
		this.metaDataProvider = metaDataProvider;
	}



	/* (non-Javadoc)
	 * @see org.bragi.playlist.LucenePlaylist.internal.IndexerInterface#indexUri(java.lang.String)
	 */
	public boolean indexUri(String uri) {
		try {
			if (metaDataProvider==null || uri==null || uri.isEmpty())
				return false;
			String[] metaData = metaDataProvider.getMetaData(uri, metaDataToIndexSet);
			if (metaData.length==0)
				return false;
			Document doc = new Document();
			//doc.add(new LongField(INSERTIONCOUNTCONSTANT, insertionCount++, LongField.Store.YES));
			//doc.add(new Field (INSERTIONCOUNTCONSTANT, String.valueOf(insertionCount++), Field.Store.NO, Field.Index.NOT_ANALYZED));
			doc.add(new StoredField(INSERTIONCOUNTCONSTANT, insertionCount++));
			doc.add(new TextField(URICONSTANT, uri, TextField.Store.YES));
			int i=0;
			for (MetaDataEnum meta : metaDataToIndexSet) {
				doc.add(new TextField(meta.name(), metaData[i++], TextField.Store.YES));
			}
			indexWriter.addDocument(doc);
			indexWriter.commit();
			refreshSearchManager();
			return true;
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		} finally {
			
		}
		return false;
	}

	/**
	 * This method is used to refresh (and optionally initialize) the SearcherManager.
	 * @throws IOException if something goes wrong
	 */
	private void refreshSearchManager() throws IOException {
		if (manager==null)
			manager=new SearcherManager(indexWriter, true, new SearcherFactory());
		manager.maybeRefreshBlocking();
		
	}
	
	/* (non-Javadoc)
	 * @see org.bragi.playlist.LucenePlaylist.internal.IndexerInterface#removeUri(java.lang.String)
	 */
	public void removeUri(String uri) throws ParseException, IOException {
		if (metaDataProvider==null || uri==null || uri.isEmpty())
			return;
		Query q = queryParser.parse(URICONSTANT+":\""+uri+"\"");
		indexWriter.deleteDocuments(q);
		indexWriter.commit();
		refreshSearchManager();
	}
	
	/* (non-Javadoc)
	 * @see org.bragi.playlist.LucenePlaylist.internal.IndexerInterface#closeIndexWriter()
	 */
	public void closeIndexWriter() throws IOException {
		if (indexWriter!=null)
			indexWriter.close();
		if (manager!=null)
			manager.close();
	}
	
	/* (non-Javadoc)
	 * @see org.bragi.playlist.LucenePlaylist.internal.IndexerInterface#filter(java.lang.String)
	 */
	public List<IndexEntry> filter(String query, MetaDataEnum... metaData) throws ParseException, IOException {
		if (query==null || query.isEmpty() || metaData.length==0)
			return new ArrayList<>();
		List<IndexEntry> filteredMetaData = new ArrayList<>();
		//make sure SearcherManager is initialized
		if (manager==null)
			manager=new SearcherManager(indexWriter, true, new SearcherFactory());
		IndexSearcher searcher = manager.acquire();
		
		try {
			Query q = queryParser.parse(query);
			TopFieldDocs docs=searcher.search(q, null, Integer.MAX_VALUE, new Sort(new SortField(INSERTIONCOUNTCONSTANT, SortField.Type.LONG)));
			for (ScoreDoc hit : docs.scoreDocs) {
				int docId = hit.doc;
				Document hitDocument=searcher.doc(docId);
				Map<MetaDataEnum, String> metaDataDictionary=new Hashtable<>();
				for (MetaDataEnum metaDataEnum : metaData) {
					metaDataDictionary.put(metaDataEnum, hitDocument.getField(metaDataEnum.name()).stringValue());
				}
				IndexEntry entry=new IndexEntry();
				entry.setUri(URI.create(hitDocument.getField(URICONSTANT).stringValue()));
				entry.setMetaData(metaDataDictionary);
				filteredMetaData.add(entry);
				//filteredURIs.add();
			}
//			TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);
//			searcher.search(q, collector);
//			for (ScoreDoc hit : collector.topDocs().scoreDocs) {
//				int docId = hit.doc;
//				Document hitDocument=searcher.doc(docId);
//				Map<MetaDataEnum, String> metaDataDictionary=new Hashtable<>();
//				for (MetaDataEnum metaDataEnum : metaData) {
//					metaDataDictionary.put(metaDataEnum, hitDocument.getField(metaDataEnum.name()).stringValue());
//				}
//				filteredMetaData.put(URI.create(hitDocument.getField(URICONSTANT).stringValue()),metaDataDictionary);
//				//filteredURIs.add();
//			}
		} finally {
			manager.release(searcher);
		}
		return filteredMetaData;
		
	}
	
}
