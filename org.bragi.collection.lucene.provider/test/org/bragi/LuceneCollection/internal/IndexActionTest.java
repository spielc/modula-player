/**
 * 
 */
package org.bragi.LuceneCollection.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import org.bragi.indexer.IndexerInterface;
import org.bragi.metadata.MetaDataEnum;
import org.bragi.metadata.MetaDataProviderInterface;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.EventAdmin;

/**
 * @author christoph
 *
 */
public class IndexActionTest {
	
	private static final String MP3_URL = "file:///tmp/test.mp3";
	private static final String MP3_1_URL = "file:///tmp/test1.mp3";
	
	private EventAdmin admin;
	private IndexerInterface indexer;
	private IndexAction action;
	private List<URI> uris;
		
	@Before
	public void initTest() throws URISyntaxException, IOException {
		admin = mock(EventAdmin.class);
		indexer = mock(IndexerInterface.class);
		uris = new ArrayList<>();
		uris.add(URI.create(MP3_URL));
		uris.add(URI.create(MP3_1_URL));
		for(int i=0;i<20;i++)
			uris.add(URI.create(String.format("file:///tmp/test%d.mp3", (i+2))));
		action = new IndexAction(uris);
		IndexAction.setIndexer(indexer);
		IndexAction.setAdmin(admin);
	}

	@Test
	public void computeTest() {
		ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(action);
        //check that indexUri-method was called exactly once for each URL
        for (URI uri : uris) {
        	verify(indexer,times(1)).indexUri(uri.toString());
		}
        //test corner-cases (should not throw exceptions)
        action = new IndexAction(null);
        pool.invoke(action);
        action = new IndexAction(uris);
		IndexAction.setIndexer(null);
		pool.invoke(action);
		action = new IndexAction(uris);
		IndexAction.setAdmin(null);
		pool.invoke(action);
	}
	
}
