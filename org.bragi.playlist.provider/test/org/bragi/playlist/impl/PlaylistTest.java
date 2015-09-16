/**
 * 
 */
package org.bragi.playlist.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.bragi.metadata.MetaDataEnum;
import org.bragi.metadata.MetaDataProviderInterface;
import org.bragi.playlist.PlaylistEntry;
import org.bragi.playlist.PlaylistInterface;
import org.bragi.query.QueryParserInterface;
import org.bragi.query.QueryResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;


/**
 * @author christoph
 *
 */
public class PlaylistTest {
	
	private static final String MP3_URL = "file:///tmp/test.mp3";
	private static final String MP3_1_URL = "file:///tmp/test1.mp3";
	private static final String ALBUM = "Romulus";
	private static final String UNSUCCESSFUL_QUERY = "SELECT * WHERE ARTIST='"+ALBUM+"'";
	private static final String SUCCESSFUL_QUERY = "SELECT * WHERE ALBUM='"+ALBUM+"'";
	private static final String ARTIST = "Ex Deo";
	private static final String TRACK_NR = "9";
	private static final String AND_QUERY = MetaDataEnum.ARTIST.name()+":\""+ARTIST+"\" AND "+MetaDataEnum.TRACK_NUMBER+":\""+TRACK_NR+"\"";
	private static final String RATING_TRACK_2 = "***";
	private static final String RATING_TRACK_1 = "****";
	private static final String TRACKNAME_TRACK_1 = "34532";
	private static final String OR_QUERY = MetaDataEnum.TRACK_ID+":\""+TRACKNAME_TRACK_1+"\" OR "+MetaDataEnum.TRACK_NUMBER+":\""+TRACK_NR+"\"";
	
	private Playlist playlist;
	private EventAdmin eventAdmin;
	private MetaDataProviderInterface metaDataProvider;
	private QueryParserInterface queryParser;
	private ConfigurationAdmin configurationAdmin;
	private Configuration config;
	
	@Before
	public void initTest() throws Exception {
		playlist=new Playlist();
		eventAdmin = mock(EventAdmin.class);
		metaDataProvider = mock(MetaDataProviderInterface.class);
		queryParser = mock(QueryParserInterface.class);
		configurationAdmin = mock(ConfigurationAdmin.class);
		String[] metaDataValues = new String[]{ALBUM, ARTIST, "k3b", "Metal", "en", "", RATING_TRACK_1, ALBUM, TRACKNAME_TRACK_1, "4", "http://www.ex-deo.com"};
		Map<MetaDataEnum, String> metaDataValuesMap=new HashMap<>();
		metaDataValuesMap.put(MetaDataEnum.ALBUM, ALBUM);
		metaDataValuesMap.put(MetaDataEnum.ARTIST, ARTIST);
		metaDataValuesMap.put(MetaDataEnum.ENCODED_BY, "k3b");
		metaDataValuesMap.put(MetaDataEnum.GENRE, "Metal");
		metaDataValuesMap.put(MetaDataEnum.LANGUAGE, "en");
		metaDataValuesMap.put(MetaDataEnum.PUBLISHER, "");
		metaDataValuesMap.put(MetaDataEnum.RATING, RATING_TRACK_1);
		metaDataValuesMap.put(MetaDataEnum.TITLE, ALBUM);
		metaDataValuesMap.put(MetaDataEnum.TRACK_ID, TRACKNAME_TRACK_1);
		metaDataValuesMap.put(MetaDataEnum.TRACK_NUMBER, "4");
		metaDataValuesMap.put(MetaDataEnum.URL, "http://www.ex-deo.com");
		when(metaDataProvider.getMetaData(MP3_URL, EnumSet.allOf(MetaDataEnum.class))).thenReturn(metaDataValues);
		when(metaDataProvider.getMetaData(MP3_1_URL, EnumSet.allOf(MetaDataEnum.class))).thenReturn(new String[]{ALBUM, ARTIST, "k3b", "Metal", "en", "", RATING_TRACK_2, ALBUM, "34531", TRACK_NR, "http://www.ex-deo.com"});
		Map<URI, Map<MetaDataEnum, String>> retValue = new Hashtable<>();
		Hashtable<MetaDataEnum, String> metaDataEnumMap = new Hashtable<>();
		retValue.put(URI.create(MP3_URL), metaDataValuesMap);
		Map<MetaDataEnum, String> metaDataValuesMap2=new HashMap<>();
		metaDataValuesMap2.put(MetaDataEnum.ALBUM, ALBUM);
		metaDataValuesMap2.put(MetaDataEnum.ARTIST, ARTIST);
		metaDataValuesMap2.put(MetaDataEnum.ENCODED_BY, "k3b");
		metaDataValuesMap2.put(MetaDataEnum.GENRE, "Metal");
		metaDataValuesMap2.put(MetaDataEnum.LANGUAGE, "en");
		metaDataValuesMap2.put(MetaDataEnum.PUBLISHER, "");
		metaDataValuesMap2.put(MetaDataEnum.RATING, RATING_TRACK_2);
		metaDataValuesMap2.put(MetaDataEnum.TITLE, ALBUM);
		metaDataValuesMap2.put(MetaDataEnum.TRACK_ID, "34531");
		metaDataValuesMap2.put(MetaDataEnum.TRACK_NUMBER, TRACK_NR);
		metaDataValuesMap2.put(MetaDataEnum.URL, "http://www.ex-deo.com");
		retValue.put(URI.create(MP3_1_URL), metaDataValuesMap2);
		List<QueryResult> results=new ArrayList<>();
		QueryResult result=new QueryResult();
		result.setUri(URI.create(MP3_URL));
		result.setMetaData(metaDataValuesMap);
		results.add(result);
		result=new QueryResult();
		result.setUri(URI.create(MP3_1_URL));
		result.setMetaData(metaDataValuesMap2);
		results.add(result);
		when(queryParser.execute(SUCCESSFUL_QUERY,retValue)).thenReturn(results);
		Map<URI, Map<MetaDataEnum, String>> retValue2 = new Hashtable<>();
		retValue2.put(URI.create(MP3_URL), metaDataEnumMap);
//		when(indexer.filter(AND_QUERY,MetaDataEnum.values())).thenReturn(retValue2);
//		when(indexer.filter(OR_QUERY,MetaDataEnum.values())).thenReturn(retValue);
		when(queryParser.execute("",retValue)).thenReturn(new ArrayList<QueryResult>());
		when(queryParser.execute(null,retValue)).thenReturn(new ArrayList<QueryResult>());
		when(queryParser.execute(UNSUCCESSFUL_QUERY,retValue)).thenReturn(new ArrayList<QueryResult>());
		config=mock(Configuration.class);
		when(configurationAdmin.getConfiguration("org.bragi.playlist.impl.Playlist", "?")).thenReturn(config);
				
		playlist.setEventAdmin(eventAdmin);
		playlist.setMetaDataProvider(metaDataProvider);
		playlist.setQueryParser(queryParser);
		playlist.setConfigAdmin(configurationAdmin);
		playlist.activate(new Hashtable<>());
	}

	@Test
	public void addMediaTest() throws URISyntaxException {
		URI uri=URI.create(MP3_URL);
		playlist.addMedia(uri.toString());
		HashMap<String,URI> eventData=new HashMap<>();
		eventData.put(PlaylistInterface.URI_EVENTDATA, uri);
		verify(eventAdmin,times(1)).postEvent(new Event(PlaylistInterface.ADD_EVENT,eventData));
		playlist.addMedia(null);
		eventData.clear();
		eventData.put(PlaylistInterface.URI_EVENTDATA, null);
		verify(eventAdmin,times(0)).postEvent(new Event(PlaylistInterface.ADD_EVENT,eventData));
		playlist.addMedia("");
		eventData.clear();
		eventData.put(PlaylistInterface.URI_EVENTDATA, URI.create(""));
		verify(eventAdmin,times(0)).postEvent(new Event(PlaylistInterface.ADD_EVENT,eventData));
	}
	
	@Test
	public void removeMediaTest() throws URISyntaxException {
		URI uri=URI.create(MP3_URL);
		playlist.removeMedia(0);
		HashMap<String,URI> eventData=new HashMap<>();
		eventData.put(PlaylistInterface.URI_EVENTDATA, uri);
		verify(eventAdmin,times(0)).postEvent(new Event(PlaylistInterface.REMOVE_EVENT,eventData));
		playlist.removeMedia(-1);
		eventData.clear();
		eventData.put(PlaylistInterface.URI_EVENTDATA, null);
		verify(eventAdmin,times(0)).postEvent(new Event(PlaylistInterface.REMOVE_EVENT,eventData));
		playlist.removeMedia(Integer.MAX_VALUE);
		eventData.clear();
		eventData.put(PlaylistInterface.URI_EVENTDATA, URI.create(""));
		verify(eventAdmin,times(0)).postEvent(new Event(PlaylistInterface.REMOVE_EVENT,eventData));
		playlist.addMedia(uri.toString());
		URI tmpUri=URI.create("file:///tmp/bla.mp3");
		playlist.removeMedia(0);
		eventData.clear();
		eventData.put(PlaylistInterface.URI_EVENTDATA, tmpUri);
		verify(eventAdmin,times(0)).postEvent(new Event(PlaylistInterface.REMOVE_EVENT,eventData));
		playlist.removeMedia(0);
		eventData.clear();
		eventData.put(PlaylistInterface.URI_EVENTDATA, uri);
		verify(eventAdmin,times(1)).postEvent(new Event(PlaylistInterface.REMOVE_EVENT,eventData));
	}
	
	@Test
	public void insertMediaTest() throws URISyntaxException {
		URI uri=URI.create(MP3_URL);
		playlist.insertMedia(0,uri.toString());
		HashMap<String,Object> eventData=new HashMap<>();
		eventData.put(PlaylistInterface.URI_EVENTDATA, uri);
		eventData.put(PlaylistInterface.INDEX_EVENTDATA, 0);
		verify(eventAdmin,times(1)).postEvent(new Event(PlaylistInterface.INSERT_EVENT,eventData));
		playlist.insertMedia(0,null);
		eventData.clear();
		eventData.put(PlaylistInterface.URI_EVENTDATA, null);
		eventData.put(PlaylistInterface.INDEX_EVENTDATA, 0);
		verify(eventAdmin,times(0)).postEvent(new Event(PlaylistInterface.INSERT_EVENT,eventData));
		playlist.insertMedia(0,"");
		eventData.clear();
		eventData.put(PlaylistInterface.URI_EVENTDATA, URI.create(""));
		eventData.put(PlaylistInterface.INDEX_EVENTDATA, 0);
		verify(eventAdmin,times(0)).postEvent(new Event(PlaylistInterface.INSERT_EVENT,eventData));
	}
	
	@Test
	public void saveLoadTest() throws URISyntaxException, IOException {
		URI[] uris=new URI[] {
				URI.create(MP3_URL),
				URI.create(MP3_URL),
				URI.create(MP3_URL)
		};
		int[] indizes=new int[uris.length];
		for (int i=0;i<uris.length;i++) {
			playlist.addMedia(uris[i].toString());
			indizes[i]=i;
		}
		File playlistFile = File.createTempFile("temp", ".xspf");
		playlist.save(playlistFile.toURI().toString());
		Assert.assertTrue(playlistFile.exists());
		Assert.assertTrue(playlistFile.length()>0);
		playlist.load(playlistFile.toURI().toString());
		for (URI uri: uris) {
			HashMap<String,URI> eventData=new HashMap<>();
			eventData.put(PlaylistInterface.URI_EVENTDATA, uri);
			verify(eventAdmin,times(6)).postEvent(new Event(PlaylistInterface.ADD_EVENT,eventData)); //six times because we already called addMedia on all URIs twice (once before save and once during loading)
		}
		playlistFile = File.createTempFile("temp", ".m3u");
		playlist.save(playlistFile.toURI().toString());
		Assert.assertTrue(playlistFile.exists());
		Assert.assertTrue(playlistFile.length()>0);
		playlist.load(playlistFile.toURI().toString());
		for (URI uri: uris) {
			HashMap<String,URI> eventData=new HashMap<>();
			eventData.put(PlaylistInterface.URI_EVENTDATA, uri);
			verify(eventAdmin,times(9)).postEvent(new Event(PlaylistInterface.ADD_EVENT,eventData)); //nine times! reason see above
		}
	}
	
	@Test
	public void filterTest() throws URISyntaxException {
		URI[] uris=new URI[] {
				URI.create(MP3_URL),
				URI.create(MP3_1_URL)
		};
		playlist.addMedia(uris[0].toString());
		playlist.addMedia(uris[1].toString());
		List<PlaylistEntry> filteredURIs=playlist.filter(SUCCESSFUL_QUERY); //test regular, simple query
		Assert.assertEquals(2, filteredURIs.size());
		filteredURIs=playlist.filter(""); //test empty query
		Assert.assertEquals(0, filteredURIs.size());
		filteredURIs=playlist.filter(null); //test null query
		Assert.assertEquals(0, filteredURIs.size());
		filteredURIs=playlist.filter(UNSUCCESSFUL_QUERY); //test query of not existing data
		Assert.assertEquals(0, filteredURIs.size());
//		filteredURIs=playlist.filter(AND_QUERY); //test AND-query
//		Assert.assertEquals(1, filteredURIs.size());
//		filteredURIs=playlist.filter(OR_QUERY); //test OR-query
//		Assert.assertEquals(2, filteredURIs.size());
	}
	
	@Test
	public void repeatTest() throws IOException {
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("repeat", true);
		properties.put("random", false);
		playlist.setRepeat(true);
		verify(config,times(1)).update(properties);
		Assert.assertTrue(playlist.getRepeat());
		properties.put("repeat", false);
		playlist.setRepeat(false);
		verify(config,times(1)).update(properties);
		Assert.assertFalse(playlist.getRepeat());		
	}
	
	@Test
	public void randomTest() throws IOException {
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("repeat", false);
		properties.put("random", true);
		playlist.setRandom(true);
		verify(config,times(1)).update(properties);
		Assert.assertTrue(playlist.getRandom());
		properties.put("random", false);
		playlist.setRandom(false);
		verify(config,times(1)).update(properties);
		Assert.assertFalse(playlist.getRandom());		
	}
}