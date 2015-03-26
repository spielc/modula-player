/**
 * 
 */
package org.bragi.LuceneIndexer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.store.AlreadyClosedException;
import org.junit.Assert;
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

	@Test(expected=NullPointerException.class)
	public void activateTest() {
		Map<String,Object> configData=new HashMap<>();
		configData.put("service.pid", SERVICE_PID);
		indexerComponent.activate(configData);
		Assert.assertEquals(SERVICE_PID, indexerComponent.getId());
		configData.remove("service.pid");
		indexerComponent.activate(configData);
	}
	
	@Test
	public void getIdTest() {
		Assert.assertTrue(indexerComponent.getId().isEmpty());
		Map<String,Object> configData=new HashMap<>();
		configData.put("service.pid", SERVICE_PID);
		indexerComponent.activate(configData);
		Assert.assertEquals(SERVICE_PID, indexerComponent.getId());
	}
}
