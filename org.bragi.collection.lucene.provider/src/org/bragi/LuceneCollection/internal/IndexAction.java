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

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.RecursiveAction;

import org.bragi.collection.CollectionInterface;
import org.bragi.indexer.IndexerInterface;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * @author christoph
 *
 */
public class IndexAction extends RecursiveAction {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7967287603738840941L;
	private static final int LOCALWORK_THRESHHOLD = 20;
	
	private static IndexerInterface INDEXER;
	private static EventAdmin ADMIN;
	
	private List<URI> uris;
	
	public IndexAction(List<URI> pUris) {
		uris=pUris;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.RecursiveAction#compute()
	 */
	@Override
	protected void compute() {
		if(uris!=null && INDEXER!=null && ADMIN!=null) {
			if (uris.size()<=LOCALWORK_THRESHHOLD) {
	            for (URI uri : uris) {
	            	if (INDEXER.indexUri(uri.toString())) { //make sure that only stuff is propagated that could be indexed
	            		HashMap<String,URI> eventData=new HashMap<>();
	            		eventData.put(CollectionInterface.URI_EVENTDATA, uri);
	            		ADMIN.postEvent(new Event(CollectionInterface.ADD_EVENT,eventData));
	            	}
	            }
		    }
		    else {
		        int split = uris.size() / 2;
		        invokeAll(new IndexAction(uris.subList(0, split)),new IndexAction(uris.subList(split, uris.size())));
		    }
		}
	}

	public static IndexerInterface getIndexer() {
		return INDEXER;
	}

	public static void setIndexer(IndexerInterface indexer) {
		INDEXER = indexer;
	}

	public static EventAdmin getAdmin() {
		return ADMIN;
	}

	public static void setAdmin(EventAdmin admin) {
		ADMIN = admin;
	}
	
}
