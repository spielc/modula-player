package org.bragi.LuceneIndexer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Dictionary;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.bragi.indexer.IndexerInterface;
import org.bragi.metadata.MetaDataEnum;
import org.bragi.metadata.MetaDataProviderInterface;
import org.junit.Assert;

public class BaseLuceneIndexerTest {

	protected static final String SERVICE_PID = "org.bragi.LuceneIndexer.RAMBasedLuceneIndexer-1";
	protected static final String MP3_URL = "file:///tmp/test.mp3";
	protected static final String MP3_1_URL = "file:///tmp/test1.mp3";
	protected static final String ALBUM = "Romulus";
	protected static final String ARTIST = "Ex Deo";
	protected static final String TRACK_NR = "9";
	protected static final String RATING_TRACK_2 = "***";
	protected static final String RATING_TRACK_1 = "****";
	protected static final String TRACKNAME_TRACK_1 = "34532";
	protected MetaDataProviderInterface metaDataProvider;

	public BaseLuceneIndexerTest() throws URISyntaxException {
		super();
		metaDataProvider = mock(MetaDataProviderInterface.class);
		when(metaDataProvider.getMetaData(MP3_URL, EnumSet.range(MetaDataEnum.ALBUM, MetaDataEnum.URL))).thenReturn(new String[]{ALBUM, ARTIST, "k3b", "Metal", "en", "", RATING_TRACK_1, ALBUM, TRACKNAME_TRACK_1, "4", "http://www.ex-deo.com"});
		when(metaDataProvider.getMetaData(MP3_1_URL, EnumSet.range(MetaDataEnum.ALBUM, MetaDataEnum.URL))).thenReturn(new String[]{ALBUM, ARTIST, "k3b", "Metal", "en", "", RATING_TRACK_2, ALBUM, "34531", TRACK_NR, "http://www.ex-deo.com"});
	}

	/**
	 * @param indexerComponent 
	 * 
	 */
	protected void indexUriTest(IndexerInterface indexerComponent) {
		URI uri=URI.create(MP3_URL);
		indexerComponent.setMetaDataProvider(null);
		indexerComponent.indexUri(uri.toString());
		indexerComponent.setMetaDataProvider(metaDataProvider);
		indexerComponent.indexUri(uri.toString());
		indexerComponent.indexUri(null);
		indexerComponent.indexUri("");
		Assert.assertTrue(true);
	}
	
	/**
	 * @throws Exception 
	 */
	protected void removeUriTest(IndexerInterface indexerComponent) throws Exception {
		URI uri=URI.create(MP3_URL);
		indexerComponent.setMetaDataProvider(null);
		indexerComponent.removeUri(uri.toString());
		indexerComponent.setMetaDataProvider(metaDataProvider);
		indexerComponent.removeUri(uri.toString());
		indexerComponent.indexUri(uri.toString()); //actually add the correct uri and try to remove it afterwards
		indexerComponent.removeUri(uri.toString());
		indexerComponent.removeUri(null);
		indexerComponent.removeUri("");
		Assert.assertTrue(true);
	}
	
	/**
	 * @throws Exception 
	 */
	protected void filterTest(IndexerInterface indexerComponent) throws Exception {
		URI[] uris=new URI[] {
				URI.create(MP3_URL),
				URI.create(MP3_1_URL)
		};
		indexerComponent.indexUri(uris[0].toString());
		indexerComponent.indexUri(uris[1].toString());
		Map<URI, Map<MetaDataEnum, String>> result=indexerComponent.filter(MetaDataEnum.ALBUM.name()+":\""+ALBUM+"\"",MetaDataEnum.values()); //test regular, simple query
		Assert.assertEquals(2, result.size());
		result=indexerComponent.filter("",MetaDataEnum.values()); //test empty query
		Assert.assertEquals(0, result.size());
		result=indexerComponent.filter(null); //test null query
		Assert.assertEquals(0, result.size());
		result=indexerComponent.filter(MetaDataEnum.ARTIST.name()+":\""+ALBUM+"\"",MetaDataEnum.values()); //test query of not existing data
		Assert.assertEquals(0, result.size());
		result=indexerComponent.filter(MetaDataEnum.ARTIST.name()+":\""+ARTIST+"\" AND "+MetaDataEnum.TRACK_NUMBER+":\""+TRACK_NR+"\"",MetaDataEnum.values()); //test AND-query
		Assert.assertEquals(1, result.size());
		result=indexerComponent.filter(MetaDataEnum.TRACK_ID+":\""+TRACKNAME_TRACK_1+"\" OR "+MetaDataEnum.TRACK_NUMBER+":\""+TRACK_NR+"\"",MetaDataEnum.values()); //test OR-query
		Assert.assertEquals(2, result.size());
	}
	
	/**
	 * @throws Exception 
	 */
	protected void closeIndexWriterTest(IndexerInterface indexerComponent) throws Exception {
		URI uri=URI.create(MP3_URL);
		indexerComponent.closeIndexWriter();
		indexerComponent.indexUri(uri.toString());
	}
}