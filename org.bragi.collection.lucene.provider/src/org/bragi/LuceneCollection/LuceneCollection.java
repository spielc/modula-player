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
package org.bragi.LuceneCollection;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bragi.LuceneCollection.internal.CollectionChangeHandlerThread;
import org.bragi.LuceneCollection.internal.IndexAction;
import org.bragi.collection.CollectionEntry;
import org.bragi.collection.CollectionInterface;
import org.bragi.indexer.IndexEntry;
import org.bragi.indexer.IndexerInterface;
import org.bragi.metadata.MetaDataEnum;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * @author christoph
 *
 */
@org.osgi.service.component.annotations.Component()
public class LuceneCollection implements CollectionInterface {
	
	private EventAdmin eventAdmin;
	//private MetaDataProviderInterface metaDataProvider;
	private IndexerInterface indexer;
	private CollectionChangeHandlerThread collectionChangeHandler;
	
	public LuceneCollection() throws IOException {
		collectionChangeHandler=new CollectionChangeHandlerThread();
	}
	
	@org.osgi.service.component.annotations.Reference
	public void setEventAdmin(EventAdmin pEventAdmin) {
		eventAdmin=pEventAdmin;
		collectionChangeHandler.setEventAdmin(pEventAdmin);
	}
	public void unsetEventAdmin(EventAdmin pEventAdmin) {
		eventAdmin=null;
		collectionChangeHandler.setEventAdmin(null);
	}
	
	@org.osgi.service.component.annotations.Reference(target="(type=CollectionIndexer)")
	public void setIndexer(IndexerInterface pIndexer) {
		indexer=pIndexer;
		collectionChangeHandler.setIndexer(pIndexer);
		if (indexer!=null) {
			try {
//				Map<URI,Map<MetaDataEnum,String>> collectionEntries=indexer.filter("*", MetaDataEnum.TITLE);
//				// (re-)register directories of collection in collectionChangeHandler
//				collectionEntries.entrySet().parallelStream()
//											.map(entry->entry.getKey())
//											.map(Paths::get)
//											.map(path->path.getParent())
//											.filter(path->path!=null)
//											.distinct()
//											.forEach(path->{
//												try {
//													collectionChangeHandler.register(path);
//												} catch (Exception e) {
//													e.printStackTrace();
//												}
//											});
//				// TODO iterate over all files in all parent directories of collectionEntries and index the ones that are not yet contained in the index
//				//directories.map
//				collectionEntries.entrySet().parallelStream()
//											.map(entry->entry.getKey())
//											.map(Paths::get)
//											.map(path->path.getParent())
//											.filter(path->path!=null)
//											.distinct()
//											.forEach(path->{
//												try {
//													Files.walk(path,FileVisitOption.FOLLOW_LINKS)
//														 .filter(path1->!collectionEntries.containsKey(path1.toUri()))
//														 .map(path1->path1.toUri().toString())
//														 //.collect(Collectors.toList())
//														 .forEach(path1->indexer.indexUri(path1));
//												} catch (Exception e) {
//													e.printStackTrace();
//												}
//											});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		for (URI uri : playlist) {
//			indexer.indexUri(uri.toString());
//		}
	}
	public void unsetIndexer(IndexerInterface pIndexer) {
		try {
			pIndexer.closeIndexWriter();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setIndexer(null);
	}
		
//	@Reference
//	public void setMetaDataProvider(MetaDataProviderInterface pMetaDataProvider) {
//		metaDataProvider=pMetaDataProvider;
//	}
//	public void unsetMetaDataProvider(MetaDataProviderInterface pMetaDataProvider) {
//		metaDataProvider=null;
//	}

	/* (non-Javadoc)
	 * @see org.bragi.collection.CollectionInterface#addMedia(java.lang.String)
	 */
	@Override
	public void addMedia(String uri) throws URISyntaxException {
		if (uri!=null && !uri.isEmpty() && indexer!=null) {
			indexer.indexUri(uri);
			postEvent(URI.create(uri), ADD_EVENT);
		}
	}

	/* (non-Javadoc)
	 * @see org.bragi.collection.CollectionInterface#removeMedia(java.lang.String)
	 */
	@Override
	public void removeMedia(String uri) {
		if (uri!=null && !uri.isEmpty() && indexer!=null) {
			try {
				indexer.removeUri(uri);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			postEvent(URI.create(uri), REMOVE_EVENT);
		}
	}

	/**
	 * Post a new event to the EventAdmin-service
	 * @param uriObject, the URI-object of the object
	 * @param eventType, the type of the event
	 */
	private void postEvent(URI uriObject, String eventType) {
		if (eventAdmin!=null) {
			HashMap<String,URI> eventData=new HashMap<>();
			eventData.put(URI_EVENTDATA, uriObject);
			eventAdmin.postEvent(new Event(eventType,eventData));
		}
	}
	
	@Override
	public void addCollectionRoot(String uri) throws URISyntaxException {
		if (uri!=null && !uri.isEmpty()) {
			final List<URI> files=new ArrayList<>();
	        try {
				Files.walkFileTree(Paths.get(URI.create(uri)), EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
				         new SimpleFileVisitor<Path>() {
	
		                    @Override
		                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	                            files.add(file.toUri());
	                            return FileVisitResult.CONTINUE;
		                    }

							@Override
							public FileVisitResult preVisitDirectory(Path dir,BasicFileAttributes attrs) throws IOException {
								collectionChangeHandler.register(dir);
								return FileVisitResult.CONTINUE;
							}
		                    		                    	
		         });
			} catch (IOException e) {
				e.printStackTrace();
			}
	        IndexAction task=new IndexAction(files);
	        IndexAction.setIndexer(indexer);
	        IndexAction.setAdmin(eventAdmin);
	        ForkJoinPool pool = new ForkJoinPool();
	        pool.invoke(task);
	        collectionChangeHandler.setActive(true);
			collectionChangeHandler.start();
		}
	}

	@Override
	public List<CollectionEntry> filter(String query,
			MetaDataEnum... metaData) {
		List<CollectionEntry> result=new ArrayList<>();
		if (indexer!=null) {
			try {
				result=Arrays.asList(indexer.filter(query, metaData).stream().map(LuceneCollection::createCollectionEntry).toArray(CollectionEntry[]::new));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static CollectionEntry createCollectionEntry(IndexEntry entry) {
		CollectionEntry collectionEntry=new CollectionEntry();
		collectionEntry.setUri(entry.getUri());
		collectionEntry.setMetaData(entry.getMetaData());
		return collectionEntry;
	}
}
