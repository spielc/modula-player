package org.bragi.LuceneIndexer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IndexerRegistryTest {
	
	private IndexerRegistry registry;
	private static final String SERVICE_PID_1 = "org.bragi.LuceneIndexer.RAMBasedLuceneIndexer-1"; 
	private static final String SERVICE_PID_2 = "org.bragi.LuceneIndexer.RAMBasedLuceneIndexer-2";
	private Map<String,Object> config1;
	private Map<String,Object> config2;
	
	@Before
	public void initTest() throws URISyntaxException, IOException {
		registry=new IndexerRegistry();
		config1=new HashMap<>();
		config1.put("service.pid", SERVICE_PID_1);
		config2=new HashMap<>();
		config2.put("service.pid", SERVICE_PID_2);
	}
	
	@Test
	public void addIndexerTest() {
		registry.addIndexer(null);
		Assert.assertTrue(registry.getRegisteredIndexerIds().isEmpty());
		RAMBasedLuceneIndexer indexer1=new RAMBasedLuceneIndexer();
		indexer1.activate(config1);
		registry.addIndexer(indexer1);
		Assert.assertEquals(1, registry.getRegisteredIndexerIds().size());
		RAMBasedLuceneIndexer indexer2=new RAMBasedLuceneIndexer();
		registry.addIndexer(indexer2);
		Assert.assertEquals(2, registry.getRegisteredIndexerIds().size());
	}
	
	@Test
	public void removeIndexerTest() {
		registry.removeIndexer(null);
		Assert.assertTrue(registry.getRegisteredIndexerIds().isEmpty());
		RAMBasedLuceneIndexer indexer1=new RAMBasedLuceneIndexer();
		indexer1.activate(config1);
		registry.removeIndexer(indexer1);
		Assert.assertTrue(registry.getRegisteredIndexerIds().isEmpty());
		RAMBasedLuceneIndexer indexer2=new RAMBasedLuceneIndexer();
		registry.removeIndexer(indexer2);
		Assert.assertTrue(registry.getRegisteredIndexerIds().isEmpty());
	}
	
	@Test
	public void getRegisteredIndexerIdsTest() {
		Assert.assertTrue(registry.getRegisteredIndexerIds().isEmpty());
		RAMBasedLuceneIndexer indexer1=new RAMBasedLuceneIndexer();
		indexer1.activate(config1);
		RAMBasedLuceneIndexer indexer2=new RAMBasedLuceneIndexer();
		indexer2.activate(config2);
		registry.addIndexer(indexer1);
		registry.addIndexer(indexer2);
		Assert.assertEquals(2, registry.getRegisteredIndexerIds().size());
		registry.removeIndexer(indexer2);
		config2.put("service.pid", SERVICE_PID_1);
		indexer2.activate(config2);
		registry.addIndexer(indexer2);
		Assert.assertEquals(1, registry.getRegisteredIndexerIds().size());
		registry.removeIndexer(indexer1);
		Assert.assertTrue(registry.getRegisteredIndexerIds().isEmpty());
	}
	
	@Test
	public void getIndexerTest() {
		Assert.assertNull(registry.getIndexer(null));
		Assert.assertNull(registry.getIndexer(""));
		RAMBasedLuceneIndexer indexer1=new RAMBasedLuceneIndexer();
		indexer1.activate(config1);
		registry.addIndexer(indexer1);
		Assert.assertEquals(indexer1, registry.getIndexer(SERVICE_PID_1));
		Assert.assertNull(registry.getIndexer(null));
		Assert.assertNull(registry.getIndexer(""));
	}
}


