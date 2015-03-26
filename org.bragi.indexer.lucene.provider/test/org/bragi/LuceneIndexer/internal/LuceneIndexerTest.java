/**
 * 
 */
package org.bragi.LuceneIndexer.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Dictionary;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.RAMDirectory;
import org.bragi.metadata.MetaDataEnum;
import org.bragi.metadata.MetaDataProviderInterface;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author christoph
 *
 */
public class LuceneIndexerTest {
	
	private static final String MP3_URL = "file:///tmp/test.mp3";
	private static final String MP3_1_URL = "file:///tmp/test1.mp3";
	private static final String MP3_2_URL = "file:///home/christoph/music/Metalcore/The%20Black%20Dahlia%20Murder/The%20Black%20Dahlia%20Murder%20-%20Deflorate/02%20-%20Necropolis.mp3";
	private static final String ALBUM = "Romulus";
	private static final String ARTIST = "Ex Deo";
	private static final String TRACK_NR = "9";
	private static final String RATING_TRACK_2 = "***";
	private static final String RATING_TRACK_1 = "****";
	private static final String TRACKNAME_TRACK_1 = "34532";
	
	private LuceneIndexer indexer;
	private MetaDataProviderInterface metaDataProvider;

	@Before
	public void initTest() throws URISyntaxException, IOException {
		indexer = new LuceneIndexer(new RAMDirectory());
		metaDataProvider = mock(MetaDataProviderInterface.class);
		when(metaDataProvider.getMetaData(MP3_URL, EnumSet.range(MetaDataEnum.ALBUM, MetaDataEnum.URL))).thenReturn(new String[]{ALBUM, ARTIST, "k3b", "Metal", "en", "", RATING_TRACK_1, ALBUM, TRACKNAME_TRACK_1, "4", "http://www.ex-deo.com"});
		when(metaDataProvider.getMetaData(MP3_1_URL, EnumSet.range(MetaDataEnum.ALBUM, MetaDataEnum.URL))).thenReturn(new String[]{ALBUM, ARTIST, "k3b", "Metal", "en", "", RATING_TRACK_2, ALBUM, "34531", TRACK_NR, "http://www.ex-deo.com"});
		when(metaDataProvider.getMetaData(MP3_2_URL, EnumSet.range(MetaDataEnum.ALBUM, MetaDataEnum.URL))).thenReturn(new String[]{ALBUM, ARTIST, "k3b", "Metal", "en", "", RATING_TRACK_2, ALBUM, "34531", TRACK_NR, "http://www.ex-deo.com"});
		indexer.setMetaDataProvider(metaDataProvider);
	}
	
	@Test
	public void indexUriTest() throws URISyntaxException {
		URI uri=URI.create(MP3_URL);
		indexer.setMetaDataProvider(null);
		Assert.assertFalse(indexer.indexUri(uri.toString()));
		indexer.setMetaDataProvider(metaDataProvider);
		Assert.assertTrue(indexer.indexUri(uri.toString()));
		Assert.assertFalse(indexer.indexUri(null));
		Assert.assertFalse(indexer.indexUri(""));
	}
	
	@Test
	public void removeUriTest() throws ParseException, IOException {
		URI uri=URI.create(MP3_URL);
		indexer.setMetaDataProvider(null);
		indexer.removeUri(uri.toString());
		indexer.setMetaDataProvider(metaDataProvider);
		indexer.removeUri(uri.toString());
		indexer.indexUri(uri.toString()); //actually add the correct uri and try to remove it afterwards
		indexer.removeUri(uri.toString());
		uri=URI.create(MP3_2_URL);
		indexer.indexUri(uri.toString());
		indexer.removeUri(uri.toString());
		indexer.removeUri(null);
		indexer.removeUri("");
		Assert.assertTrue(true);
	}
	
	@Test(expected=AlreadyClosedException.class)
	public void closeIndexWriterTest() throws IOException {
		URI uri=URI.create(MP3_URL);
		indexer.closeIndexWriter();
		indexer.indexUri(uri.toString());
	}
	
	@Test
	public void filterTest() throws ParseException, IOException {
		URI[] uris=new URI[] {
				URI.create(MP3_URL),
				URI.create(MP3_1_URL)
		};
		indexer.indexUri(uris[0].toString());
		indexer.indexUri(uris[1].toString());
		Map<URI, Map<MetaDataEnum, String>> result=indexer.filter(MetaDataEnum.ALBUM.name()+":\""+ALBUM+"\"",MetaDataEnum.values()); //test regular, simple query
		Assert.assertEquals(2, result.size());
		result=indexer.filter(""); //test empty query
		Assert.assertEquals(0, result.size());
		result=indexer.filter(null); //test null query
		Assert.assertEquals(0, result.size());
		result=indexer.filter(MetaDataEnum.ALBUM.name()+":\""+ALBUM+"\"");
		Assert.assertEquals(0, result.size());
		result=indexer.filter(MetaDataEnum.ARTIST.name()+":\""+ALBUM+"\"",MetaDataEnum.values()); //test query of not existing data
		Assert.assertEquals(0, result.size());
		result=indexer.filter(MetaDataEnum.ARTIST.name()+":\""+ARTIST+"\" AND "+MetaDataEnum.TRACK_NUMBER+":\""+TRACK_NR+"\"",MetaDataEnum.values()); //test AND-query
		Assert.assertEquals(1, result.size());
		result=indexer.filter(MetaDataEnum.TRACK_ID+":\""+TRACKNAME_TRACK_1+"\" OR "+MetaDataEnum.TRACK_NUMBER+":\""+TRACK_NR+"\"",MetaDataEnum.values()); //test OR-query
		Assert.assertEquals(2, result.size());
	}
}
