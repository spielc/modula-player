/**
 * 
 */
package org.bragi.LuceneIndexer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.lucene.store.AlreadyClosedException;
import org.junit.Before;
import org.junit.Test;

/**
 * @author christoph
 *
 */
public class RAMBasedLuceneIndexerTest extends BaseLuceneIndexerTest {
	

	private RAMBasedLuceneIndexer indexerComponent;
	
	public RAMBasedLuceneIndexerTest() throws URISyntaxException {
		super();
	}

	@Before
	public void initTest() throws URISyntaxException, IOException {
		indexerComponent=new RAMBasedLuceneIndexer();
		indexerComponent.setMetaDataProvider(metaDataProvider);
		indexerComponent.activate(new HashMap<String, Object>());
	}
		
	@Test
	public void indexUriTest() {
		indexUriTest(indexerComponent);
	}

	
	
	@Test
	public void removeUriTest() throws Exception {
		removeUriTest(indexerComponent);
	}

	
	
	@Test(expected=AlreadyClosedException.class)
	public void closeIndexWriterTest() throws Exception {
		closeIndexWriterTest(indexerComponent);
	}

	@Test
	public void filterTest() throws Exception {
		filterTest(indexerComponent);
	}
}
