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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.bragi.indexer.IndexEntry;
import org.bragi.indexer.IndexerInterface;
import org.bragi.metadata.MetaDataEnum;
import org.bragi.metadata.MetaDataProviderInterface;
import org.bragi.playlist.PlaylistEntry;
import org.bragi.playlist.PlaylistInterface;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
	private static final String UNSUCCESSFUL_QUERY = MetaDataEnum.ARTIST.name()+":\""+ALBUM+"\"";
	private static final String SUCCESSFUL_QUERY = MetaDataEnum.ALBUM.name()+":\""+ALBUM+"\"";
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
	private IndexerInterface indexer;
	
	@Before
	public void initTest() throws Exception {
		playlist=new Playlist();
		eventAdmin = mock(EventAdmin.class);
		metaDataProvider = mock(MetaDataProviderInterface.class);
		indexer = mock(IndexerInterface.class);
		when(metaDataProvider.getMetaData(MP3_URL, EnumSet.allOf(MetaDataEnum.class))).thenReturn(new String[]{ALBUM, ARTIST, "k3b", "Metal", "en", "", RATING_TRACK_1, ALBUM, TRACKNAME_TRACK_1, "4", "http://www.ex-deo.com"});
		when(metaDataProvider.getMetaData(MP3_1_URL, EnumSet.allOf(MetaDataEnum.class))).thenReturn(new String[]{ALBUM, ARTIST, "k3b", "Metal", "en", "", RATING_TRACK_2, ALBUM, "34531", TRACK_NR, "http://www.ex-deo.com"});
		List<IndexEntry> retValue = new ArrayList<>();
		Hashtable<MetaDataEnum, String> metaDataEnumMap = new Hashtable<>();
		IndexEntry entry1=new IndexEntry();
		entry1.setUri(URI.create(MP3_URL));
		entry1.setMetaData(metaDataEnumMap);
		retValue.add(entry1);
		Hashtable<MetaDataEnum, String> metaDataEnumMap2 = new Hashtable<>();
		IndexEntry entry2=new IndexEntry();
		entry2.setUri(URI.create(MP3_URL));
		entry2.setMetaData(metaDataEnumMap2);
		retValue.add(entry2);
		retValue.add(entry1);
		when(indexer.filter(SUCCESSFUL_QUERY,MetaDataEnum.values())).thenReturn(retValue);
		List<IndexEntry> retValue2 = new ArrayList<>();
		IndexEntry entry3=new IndexEntry();
		entry3.setUri(URI.create("33"));
		entry3.setMetaData(metaDataEnumMap);
		retValue2.add(entry3);
		when(indexer.filter(AND_QUERY,MetaDataEnum.values())).thenReturn(retValue2);
		when(indexer.filter(OR_QUERY,MetaDataEnum.values())).thenReturn(retValue);
		when(indexer.filter("",MetaDataEnum.values())).thenReturn(new ArrayList<IndexEntry>());
		when(indexer.filter(null,MetaDataEnum.values())).thenReturn(new ArrayList<IndexEntry>());
		when(indexer.filter(UNSUCCESSFUL_QUERY,MetaDataEnum.values())).thenReturn(new ArrayList<IndexEntry>());
		
		playlist.setEventAdmin(eventAdmin);
		playlist.setIndexer(indexer);
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
		playlist.addMedia(uris[0].toString());
		List<PlaylistEntry> filteredURIs=playlist.filter(SUCCESSFUL_QUERY,MetaDataEnum.values()); //test regular, simple query
		Assert.assertEquals(3, filteredURIs.size());
		filteredURIs=playlist.filter("",MetaDataEnum.values()); //test empty query
		Assert.assertEquals(0, filteredURIs.size());
		filteredURIs=playlist.filter(null,MetaDataEnum.values()); //test null query
		Assert.assertEquals(0, filteredURIs.size());
		filteredURIs=playlist.filter(UNSUCCESSFUL_QUERY,MetaDataEnum.values()); //test query of not existing data
		Assert.assertEquals(0, filteredURIs.size());
		filteredURIs=playlist.filter(AND_QUERY,MetaDataEnum.values()); //test AND-query
		Assert.assertEquals(1, filteredURIs.size());
		filteredURIs=playlist.filter(OR_QUERY,MetaDataEnum.values()); //test OR-query
		Assert.assertEquals(3, filteredURIs.size());
	}
}