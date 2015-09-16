/**
 * 
 */
package org.bragi.LuceneCollection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.bragi.collection.CollectionEntry;
import org.bragi.indexer.IndexerInterface;
import org.bragi.metadata.MetaDataEnum;
import org.bragi.query.QueryParserInterface;
import org.bragi.query.QueryResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.event.EventAdmin;

/**
 * @author christoph
 *
 */
public class LuceneCollectionTest {
	
	private static String QUERY = " SELECT * WHERE "+MetaDataEnum.ALBUM.name()+"='Kataklysm'";
	
	private IndexerInterface indexer;
	private LuceneCollection collection;
	private EventAdmin eventAdmin;
	private ConfigurationAdmin configAdmin;
	private QueryParserInterface queryParser;

	@Before
	public void initTest() throws Exception {
		indexer = mock(IndexerInterface.class);
		eventAdmin = mock(EventAdmin.class);
		configAdmin = mock(ConfigurationAdmin.class);
		Configuration config = mock(Configuration.class);
		queryParser = mock(QueryParserInterface.class);
		collection=new LuceneCollection();
		collection.setIndexer(indexer);
		collection.setEventAdmin(eventAdmin);
		collection.setConfigurationAdmin(configAdmin);
		collection.setQueryParser(queryParser);
		when(configAdmin.getConfiguration("org.bragi.LuceneCollection.LuceneCollection", "?")).thenReturn(config);
	}
	
	@Test
	public void addCollectionRootTest() throws URISyntaxException, IOException {
		String root = LuceneCollectionTest.class.getClassLoader().getResource("a").toURI().toString();
		final List<URI> files=new ArrayList<>();
        try {
			Files.walkFileTree(Paths.get(URI.create(root)), EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
			         new SimpleFileVisitor<Path>() {

	                    @Override
	                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            files.add(file.toUri());
                            return FileVisitResult.CONTINUE;
	                    }


			         });
		} catch (IOException e) {
			e.printStackTrace();
		}
        //test regular case
        collection.activate(new Hashtable<>());
		collection.addCollectionRoot(root);
		for (URI file : files) {
			verify(indexer,times(1)).indexUri(file.toString());
		}
		//test corner cases
		collection.addCollectionRoot("");
		verify(indexer,times(0)).indexUri("");
		collection.addCollectionRoot(null);
		verify(indexer,times(0)).indexUri(null);
	}
	
	@Test
	public void addMediaTest() throws URISyntaxException {
		String file = LuceneCollectionTest.class.getClassLoader().getResource("a/b/test.mp3").toURI().toString();
		collection.addMedia(file);
		verify(indexer,times(1)).indexUri(file);
		//test corner cases
		collection.addMedia("");
		verify(indexer,times(0)).indexUri("");
		collection.addMedia(null);
		verify(indexer,times(0)).indexUri(null);
		collection.setIndexer(null);
		collection.addMedia(file);
		verify(indexer,times(1)).indexUri(file);
	}
	
	@Test
	public void removeMediaTest() throws Exception {
		String file = LuceneCollectionTest.class.getClassLoader().getResource("a/b/test.mp3").toURI().toString();
		collection.removeMedia(file);
		verify(indexer,times(1)).removeUri(file);
		//test corner cases
		collection.removeMedia("");
		verify(indexer,times(0)).removeUri("");
		collection.removeMedia(null);
		verify(indexer,times(0)).removeUri(null);
		collection.setIndexer(null);
		collection.removeMedia(file);
		verify(indexer,times(1)).removeUri(file);
	}
	
	@Test
	public void filterTest() throws Exception {
		String query = "SELECT * WHERE "+ MetaDataEnum.ARTIST.name()+"='Kataklysm'";
		Map<URI,Map<MetaDataEnum, String>> retValue = new Hashtable<>();
		retValue.put(URI.create("1"), new Hashtable<MetaDataEnum, String>());
		retValue.put(URI.create("2"), new Hashtable<MetaDataEnum, String>());
		when(indexer.filter("*",MetaDataEnum.values())).thenReturn(retValue);
		when(indexer.filter("*",new MetaDataEnum[]{})).thenReturn(new Hashtable<>());
		when(indexer.filter("")).thenReturn(new Hashtable<>());
		when(indexer.filter(null)).thenReturn(new Hashtable<>());
		List<QueryResult> result=new ArrayList<>();
		result.add(new QueryResult());
		result.add(new QueryResult());
		when(queryParser.execute(query, retValue)).thenReturn(result);
		List<CollectionEntry> filtered=collection.filter(query);
		Assert.assertEquals(2, filtered.size());
		filtered=collection.filter(QUERY);
		Assert.assertEquals(0, filtered.size());
		filtered=collection.filter("");
		Assert.assertEquals(0, filtered.size());
		filtered=collection.filter(null);
		Assert.assertEquals(0, filtered.size());
		collection.setIndexer(null);
		filtered=collection.filter(QUERY);
		Assert.assertEquals(0, filtered.size());
		collection.setIndexer(indexer);
		collection.setQueryParser(null);
		filtered=collection.filter(QUERY);
		Assert.assertEquals(0, filtered.size());
	}
}
