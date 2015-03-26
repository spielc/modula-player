/**
 * 
 */
package org.bragi.LuceneIndexer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.store.AlreadyClosedException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author christoph
 *
 */
public class SimpleFSBasedLuceneIndexerTest extends BaseLuceneIndexerTest {
	
	private SimpleFSBasedLuceneIndexer indexerComponent;

	public SimpleFSBasedLuceneIndexerTest() throws URISyntaxException {
		super();
	}

	@Before
	public void initTest() throws URISyntaxException, IOException {
		indexerComponent=new SimpleFSBasedLuceneIndexer();
		Map<String, Object> props=new HashMap<>();
		props.put("test", "PlaylistIndexer");
		File path = new File("/tmp/SimpleFSBasedLuceneIndexerTest");
		if (path.exists())
			removeRecursive(Paths.get(path.toURI()));
		props.put("path", path);
		indexerComponent.activate(props);
		indexerComponent.setMetaDataProvider(metaDataProvider);
	}
	
	@After
	public void cleanupTest() throws IOException {
		indexerComponent.closeIndexWriter();
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
	
	/**
	 * 
	 * @param path
	 * @throws IOException
	 */
	private void removeRecursive(Path path) throws IOException 	{
	    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
	        @Override
	        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	            Files.delete(file);
	            return FileVisitResult.CONTINUE;
	        }

	        @Override
	        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
	            // try to delete the file anyway, even if its attributes
	            // could not be read, since delete-only access is
	            // theoretically possible
	            Files.delete(file);
	            return FileVisitResult.CONTINUE;
	        }

	        @Override
	        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
	            if (exc == null) {
	                Files.delete(dir);
	                return FileVisitResult.CONTINUE;
	            }
	            else 
	            	throw exc;
	        }
	    });
	}
}
