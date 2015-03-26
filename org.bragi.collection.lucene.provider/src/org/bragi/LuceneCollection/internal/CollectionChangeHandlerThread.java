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
package org.bragi.LuceneCollection.internal;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import org.bragi.collection.CollectionInterface;
import org.bragi.indexer.IndexerInterface;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * @author christoph
 *
 */
//TODO generate unit test for this class
public class CollectionChangeHandlerThread extends Thread {
	
	private WatchService watcher;
	private Map<Path,WatchKey> keys;
	private IndexerInterface indexer;
	private EventAdmin eventAdmin;
	private boolean active;
	
	public CollectionChangeHandlerThread() throws IOException {
		super("CollectionChangeHandlerThread");
		watcher=FileSystems.getDefault().newWatchService();
		keys=new Hashtable<>();
		active=false;
	}
	
	/**
	 * Register the specified Path-object for watching through the WatchService
	 * @param dir the Path-object to watch
	 * @throws IOException thrown if something goes wrong
	 */
	public void register(Path dir) throws IOException {
		if (dir!=null) {
			WatchKey key=dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
			keys.put(dir, key);
		}
	}
	
	/**
	 * Unregisteres the specified Path-object from watching through the WatchService
	 * @param dir the Path-object to stop watching
	 */
	public void unregister(Path dir) {
		if (dir!=null) {
			WatchKey key=keys.get(dir);
			if (key!=null) {
				key.cancel();
				keys.remove(dir);
			}
		}
	}
	
	@Override
	public void run() {
		while (active) {
			// wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException ie) {
            	ie.printStackTrace();
                return;
            }
            
            Path dir=null;
            for (Entry<Path,WatchKey> entry : keys.entrySet()) {
				if (entry.getValue()==key) {
					dir=entry.getKey();
					break;
				}
			}
            
            if (dir!=null) {
            	for (WatchEvent<?> event: key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    
                    Path path=dir.resolve((Path)event.context());
                    
                    String uri = path.toUri().toString();
                    
                    // TBD - provide example of how OVERFLOW event is handled
                    if ((kind == StandardWatchEventKinds.OVERFLOW) || (indexer == null) || (eventAdmin == null)) {
                        continue;
                    }
                    else if (kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    	if (indexer.indexUri(uri)) {
	                    	if (kind == StandardWatchEventKinds.ENTRY_CREATE)
	                    		postEvent(URI.create(uri),CollectionInterface.ADD_EVENT);
	                    	else 
	                    	   	postEvent(URI.create(uri),CollectionInterface.MODIFY_EVENT);
                    	}
                    }
                    else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    	try {
							indexer.removeUri(uri);
							postEvent(URI.create(uri),CollectionInterface.REMOVE_EVENT);
						} catch (Exception e) {
							e.printStackTrace();
						}
                    }
            	}
            }
            
            key.reset();
		}
	}

	public IndexerInterface getIndexer() {
		return indexer;
	}

	public void setIndexer(IndexerInterface indexer) {
		this.indexer = indexer;
	}

	public EventAdmin getEventAdmin() {
		return eventAdmin;
	}

	public void setEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}
	
	
	
	public WatchService getWatcher() {
		return watcher;
	}

	public void setWatcher(WatchService watcher) throws IOException {
		if (this.watcher!=null)
			this.watcher.close();
		this.watcher = watcher;
	}
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean running) {
		this.active = running;
	}

	/**
	 * Post a new event to the EventAdmin-service
	 * @param uriObject, the URI-object of the object
	 * @param eventType, the type of the event
	 */
	private void postEvent(URI uriObject, String eventType) {
		if (eventAdmin!=null) {
			HashMap<String,URI> eventData=new HashMap<>();
			eventData.put(CollectionInterface.URI_EVENTDATA, uriObject);
			eventAdmin.postEvent(new Event(eventType,eventData));
		}
	}
	
}
