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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.bragi.LuceneCollection.internal.CollectionChangeHandlerThread;
import org.bragi.LuceneCollection.internal.IndexAction;
import org.bragi.collection.CollectionEntry;
import org.bragi.collection.CollectionInterface;
import org.bragi.indexer.IndexerInterface;
import org.bragi.metadata.MetaDataEnum;
import org.bragi.query.QueryParserInterface;
import org.bragi.query.QueryResult;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * @author christoph
 *
 */
@Component(name="org.bragi.LuceneCollection.LuceneCollection")
public class LuceneCollection implements CollectionInterface {
	
	private static final String COLLECTION_ROOTS = "collectionRoots";
	private static final String COMPONENT_NAME = "org.bragi.LuceneCollection.LuceneCollection";
	private EventAdmin eventAdmin;
	private IndexerInterface indexer;
	private CollectionChangeHandlerThread collectionChangeHandler;
	private QueryParserInterface queryParser;
	private ConfigurationAdmin configurationAdmin;
	private List<String> collectionRoots;
	
	@Activate
	public void activate(Map<String,Object> map) throws IOException {
		collectionChangeHandler=new CollectionChangeHandlerThread();
		collectionChangeHandler.setIndexer(indexer);
		collectionChangeHandler.setEventAdmin(eventAdmin);
		collectionRoots=new ArrayList<>();
		modified(map);
		try {
			Map<URI,Map<MetaDataEnum,String>> collectionEntries=indexer.filter("*", MetaDataEnum.TITLE);
			List<Path> files=collectionEntries.entrySet().parallelStream()
					.map(entry->entry.getKey())
					.map(Paths::get)
					.collect(Collectors.toList());
			final List<URI> filesToIndex=new ArrayList<>();
			for (String collectionRoot : collectionRoots) {
				Files.walkFileTree(Paths.get(URI.create(collectionRoot)), EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
				         new SimpleFileVisitor<Path>() {		

		                    @Override
							public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
		                    	if (!files.contains(path))
		                    		filesToIndex.add(path.toUri());
		                    	return FileVisitResult.CONTINUE;
							}

							@Override
							public FileVisitResult preVisitDirectory(Path dir,BasicFileAttributes attrs) throws IOException {
								collectionChangeHandler.register(dir);
								return FileVisitResult.CONTINUE;
							}
		                    		                    	
		         });
			}
			executeIndexAction(()->filesToIndex);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		collectionChangeHandler.setActive(true);
		collectionChangeHandler.start();
	}
	
	@Modified
	void modified(Map<String,Object> map) {
		if (map.containsKey(COLLECTION_ROOTS))
			collectionRoots = (List<String>) map.get(COLLECTION_ROOTS);
	}
	
	@Reference
	public void setQueryParser(QueryParserInterface pQueryParser) {
		queryParser=pQueryParser;
	}
	public void unsetQueryParser(QueryParserInterface pQueryParser) {
		queryParser=null;
	}
	
	@Reference
	public void setEventAdmin(EventAdmin pEventAdmin) {
		eventAdmin=pEventAdmin;
		if (collectionChangeHandler!=null)
			collectionChangeHandler.setEventAdmin(pEventAdmin);
		
	}
	public void unsetEventAdmin(EventAdmin pEventAdmin) {
		setEventAdmin(null);
	}
	
	@Reference
	public void setConfigurationAdmin(ConfigurationAdmin pConfigurationAdmin) {
		configurationAdmin=pConfigurationAdmin;
	}
	public void unsetConfigurationAdmin(ConfigurationAdmin pConfigurationAdmin) {
		configurationAdmin=null;
	}
	
	@Reference(target="(type=CollectionIndexer)")
	public void setIndexer(IndexerInterface pIndexer) {
		indexer=pIndexer;
		if (collectionChangeHandler!=null)
			collectionChangeHandler.setIndexer(pIndexer);
		if (indexer!=null && collectionRoots!=null) {
			try {
				
//				Map<URI,Map<MetaDataEnum,String>> collectionEntries=indexer.filter("*", MetaDataEnum.TITLE);
//				System.out.println();
//				// (re-)register directories of collection in collectionChangeHandler
//				String[] bla=collectionEntries.entrySet().parallelStream()
//											.map(entry->entry.getKey())
//											.map(Paths::get)
//											.map(path->path.getParent())
//											.filter(path->path!=null)
//											.distinct()
//											.map(Object::toString)
//											.toArray(String[]::new);
//				Path rootPath=Paths.get(commonPath(bla));
				Path rootPath=Paths.get(collectionRoots.get(0));
				Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS).filter(path->path.toFile().isDirectory())
											.forEach(path->{
												try {
													collectionChangeHandler.register(path);
												} catch (Exception e) {
													System.out.println("collectionChangeHandler.register failed for: "+path.toString());
												}
											});
////				// TODO iterate over all files in all parent directories of collectionEntries and index the ones that are not yet contained in the index
//				//directories.map
////				collectionEntries.entrySet().parallelStream()
////											.map(entry->entry.getKey())
////											.map(Paths::get)
////											.map(path->path.getParent())
////											.filter(path->path!=null)
////											.distinct()
////											.forEach(path->{
////												try {
////													Files.walk(path,FileVisitOption.FOLLOW_LINKS)
////														 .filter(path1->!collectionEntries.containsKey(path1.toUri()))
////														 .map(path1->path1.toUri().toString())
////														 //.collect(Collectors.toList())
////														 .forEach(path1->indexer.indexUri(path1));
////												} catch (Exception e) {
////													System.out.println("indexer.indexUri failed for: "+path.toString());
////												}
////											});
//				collectionChangeHandler.setActive(true);
//				collectionChangeHandler.start();
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
	
	static String commonPath(String...  paths){
		String commonPath = "";
		String[][] folders = new String[paths.length][];
 
		for(int i=0; i<paths.length; i++){
			folders[i] = paths[i].split("/");
		}
 
		for(int j = 0; j< folders[0].length; j++){
			String s = folders[0][j];
			for(int i=1; i<paths.length; i++){
				if(!s.equals(folders[i][j]))
					return commonPath;
			}
			commonPath += s + "/";
		}
		return commonPath;		
	}
	
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
	        executeIndexAction(()->files);
	        if (!collectionChangeHandler.isActive()) {
		        collectionChangeHandler.setActive(true);
		        collectionChangeHandler.start();
	        }
			collectionRoots.add(uri);
			createOrUpdateConfiguration();
//	        IndexAction task=new IndexAction(files); 
//	        IndexAction.setIndexer(indexer);
//	        IndexAction.setAdmin(eventAdmin);
//	        ForkJoinPool pool = new ForkJoinPool();
//	        pool.invoke(task);
//	        collectionChangeHandler.setActive(true);
//			collectionChangeHandler.start();
//			collectionRoots.add(uri);
//			createOrUpdateConfiguration();
		}
	}
	
	private void executeIndexAction(Supplier<List<URI>> files) {
		IndexAction task=new IndexAction(files.get());
        IndexAction.setIndexer(indexer);
        IndexAction.setAdmin(eventAdmin);
        ForkJoinPool pool = new ForkJoinPool();
        //pool.invoke(task);
        pool.execute(task);
	}
	
	/**
	 * This method is used to either create or update the configuration of the component
	 */
	private void createOrUpdateConfiguration() {
		try {
			Configuration configuration = configurationAdmin.getConfiguration(COMPONENT_NAME, "?");
			Hashtable<String, Object> map = new Hashtable<>();
			map.put(COLLECTION_ROOTS, collectionRoots);
			configuration.update(map);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static CollectionEntry createCollectionEntry(QueryResult result) {
		CollectionEntry collectionEntry=new CollectionEntry();
		collectionEntry.setUri(result.getUri());
		collectionEntry.setMetaData(result.getMetaData());
		return collectionEntry;
	}

	@Override
	public List<CollectionEntry> filter(String query) {
		List<CollectionEntry> returnValue=new ArrayList<>();
		if ((query!=null) && (queryParser!=null) && (indexer!=null)) {
			MetaDataEnum[] metaDataValues=MetaDataEnum.values();
			try {
				Map<URI,Map<MetaDataEnum,String>> collectionMetaData=indexer.filter("*", metaDataValues);
				returnValue=queryParser.execute(query, collectionMetaData).stream().map(LuceneCollection::createCollectionEntry).collect(Collectors.toList());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return returnValue;
	}
}
