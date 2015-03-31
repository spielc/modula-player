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
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;

import org.bragi.collection.CollectionInterface;
import org.bragi.indexer.IndexerInterface;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * @author christoph
 *
 */
public class CollectionChangeHandlerThreadTest {
	
	private static final String MP3_URL = "file:///tmp/test.mp3";
	private static final String MP3_1_URL = "file:///tmp/test1.mp3";
	private CollectionChangeHandlerThread collectionChangeHandler;
	private EventAdmin eventAdmin;
	private IndexerInterface indexer;
	private Path path1;
	private WatchKey key1;
	private WatchService watcher;
	private WatchEvent<Path> event1,event2,event3;
	private WatchEvent<Object> event4;
	
	@Before
	public void initTest() throws Exception {
		collectionChangeHandler=new CollectionChangeHandlerThread();
		eventAdmin=mock(EventAdmin.class);
		indexer=mock(IndexerInterface.class);
		path1=mock(Path.class);
		watcher=mock(WatchService.class);
		key1=mock(WatchKey.class);
		event1=mock(WatchEvent.class);
		event2=mock(WatchEvent.class);
		event3=mock(WatchEvent.class);
		event4=mock(WatchEvent.class);
		when(indexer.indexUri(MP3_1_URL)).thenReturn(true);
		when(path1.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY)).thenReturn(key1);
		when(path1.toUri()).thenReturn(URI.create(MP3_1_URL));
		when(path1.resolve(event1.context())).thenReturn(path1);
		when(watcher.take()).thenReturn(key1);
		when(event1.kind()).thenReturn(StandardWatchEventKinds.ENTRY_CREATE);
		when(event2.kind()).thenReturn(StandardWatchEventKinds.ENTRY_MODIFY);
		when(event3.kind()).thenReturn(StandardWatchEventKinds.ENTRY_DELETE);
		when(event4.kind()).thenReturn(StandardWatchEventKinds.OVERFLOW);
		
		ArrayList<WatchEvent<?>> list = new ArrayList<WatchEvent<?>>();
		list.add(event1);
		list.add(event2);
		list.add(event3);
		list.add(event4);
		when(key1.pollEvents()).thenReturn(list).thenReturn(new ArrayList<WatchEvent<?>>());
		collectionChangeHandler.setEventAdmin(eventAdmin);
		collectionChangeHandler.setIndexer(indexer);
		collectionChangeHandler.setWatcher(watcher);
	}

	@Test
	public void registerTest() throws IOException {
		collectionChangeHandler.register(path1);
		verify(path1, times(1)).register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
		collectionChangeHandler.register(null);
	}
	
	@Test
	public void unregisterTest() throws IOException {
		collectionChangeHandler.register(path1);
		collectionChangeHandler.unregister(path1);
		verify(key1, times(1)).cancel();
		collectionChangeHandler.register(null);
		collectionChangeHandler.unregister(null);
	}
	
	@Test
	public void runTest() throws IOException, InterruptedException {
		collectionChangeHandler.register(path1);
		collectionChangeHandler.setActive(true);
		collectionChangeHandler.start();
		Thread.sleep(1000);
		collectionChangeHandler.setActive(false);
		collectionChangeHandler.join();
		postEventVerification(1,CollectionInterface.ADD_EVENT);
		postEventVerification(1,CollectionInterface.MODIFY_EVENT);
		postEventVerification(1,CollectionInterface.REMOVE_EVENT);
	}

	/**
	 * @param count 
	 * @param topic 
	 * 
	 */
	private void postEventVerification(int count, String topic) {
		HashMap<String,URI> eventData=new HashMap<>();
		eventData.put(CollectionInterface.URI_EVENTDATA, URI.create(MP3_1_URL));
		verify(eventAdmin,times(count)).postEvent(new Event(topic,eventData));
	}
	
}
