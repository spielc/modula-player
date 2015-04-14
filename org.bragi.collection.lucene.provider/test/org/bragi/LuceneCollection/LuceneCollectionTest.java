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

import junit.framework.Assert;

import org.bragi.collection.CollectionEntry;
import org.bragi.indexer.IndexEntry;
import org.bragi.indexer.IndexerInterface;
import org.bragi.metadata.MetaDataEnum;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.EventAdmin;

/**
 * @author christoph
 *
 */
public class LuceneCollectionTest {
	
	private IndexerInterface indexer;
	private LuceneCollection collection;
	private EventAdmin admin;

	@Before
	public void initTest() throws URISyntaxException, IOException {
		indexer = mock(IndexerInterface.class);
		admin = mock(EventAdmin.class);
		collection=new LuceneCollection();
		collection.setIndexer(indexer);
		collection.setEventAdmin(admin);
	}
	
	@Test
	public void addCollectionRootTest() throws URISyntaxException {
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
		String file1 = LuceneCollectionTest.class.getClassLoader().getResource("a/b/test.mp3").toURI().toString();
		String file2 = LuceneCollectionTest.class.getClassLoader().getResource("a/b/c/test.ogg").toURI().toString();
		String query = MetaDataEnum.ARTIST.name()+":\"Kataklysm\"";
		List<IndexEntry> retValue = new ArrayList<>();
		IndexEntry entry1=new IndexEntry();
		entry1.setUri(URI.create("1"));
		entry1.setMetaData(new Hashtable<MetaDataEnum, String>());
		retValue.add(entry1);
		IndexEntry entry2=new IndexEntry();
		entry2.setUri(URI.create("2"));
		entry2.setMetaData(new Hashtable<MetaDataEnum, String>());
		retValue.add(entry2);
		when(indexer.filter(query,MetaDataEnum.values())).thenReturn(retValue);
		when(indexer.filter(query,new MetaDataEnum[]{})).thenReturn(new ArrayList<>());
		when(indexer.filter("")).thenReturn(new ArrayList<>());
		when(indexer.filter(null)).thenReturn(new ArrayList<>());
		List<CollectionEntry> filtered=collection.filter(query,MetaDataEnum.values());
		Assert.assertEquals(2, filtered.size());
		filtered=collection.filter(query, new MetaDataEnum[]{});
		Assert.assertEquals(0, filtered.size());
		filtered=collection.filter("");
		Assert.assertEquals(0, filtered.size());
		filtered=collection.filter(null);
		Assert.assertEquals(0, filtered.size());
		collection.setIndexer(null);
		filtered=collection.filter(query);
		Assert.assertEquals(0, filtered.size());
	}
}
