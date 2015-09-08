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
package org.bragi.playlist.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bragi.metadata.MetaDataEnum;
import org.bragi.metadata.MetaDataProviderInterface;
import org.bragi.playlist.PlaylistEntry;
import org.bragi.playlist.PlaylistInterface;
import org.bragi.query.QueryParserInterface;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import christophedelory.content.Content;
import christophedelory.playlist.Media;
import christophedelory.playlist.Parallel;
import christophedelory.playlist.Sequence;
import christophedelory.playlist.SpecificPlaylist;
import christophedelory.playlist.SpecificPlaylistFactory;
import christophedelory.playlist.SpecificPlaylistProvider;

/**
 * @author christoph
 *
 */
@Component(name="org.bragi.playlist.impl.Playlist")
public class Playlist implements PlaylistInterface {
	
	private static final String COMPONENT_NAME = "org.bragi.playlist.impl.Playlist";

	/**
	 * String-Constants for the configuration value "repeat"
	 */
	private static final String REPEAT = "repeat";
	
	/**
	 * String-Constants for the configuration value "random"
	 */
	private static final String RANDOM = "random";

	/**
	 * Private class which implements a PlaylistVisitor, which is used when loading a playlist to fill the playlist
	 * with the files the playlist contains
	 * 
	 * @author christoph
	 *
	 */
	private final class PlaylistVisitor implements christophedelory.playlist.PlaylistVisitor {
		@Override
		public void endVisitSequence(Sequence arg0) throws Exception {
			
		}

		@Override
		public void endVisitPlaylist(christophedelory.playlist.Playlist arg0) throws Exception {
			
		}

		@Override
		public void endVisitParallel(Parallel arg0) throws Exception {
			
		}

		@Override
		public void endVisitMedia(Media arg0) throws Exception {
			
		}

		@Override
		public void beginVisitSequence(Sequence arg0) throws Exception {
			
		}

		@Override
		public void beginVisitPlaylist(christophedelory.playlist.Playlist arg0) throws Exception {
			
		}

		@Override
		public void beginVisitParallel(Parallel arg0) throws Exception {
			
		}

		@Override
		public void beginVisitMedia(Media media) throws Exception {
			Content content=media.getSource();
			addMedia(content.getURI().toString());
		}
	}

	private List<PlaylistEntry> playlist;
	private boolean isRepeated;
	private boolean isRandomized;
	private EventAdmin eventAdmin;
	private MetaDataProviderInterface metaDataProvider;
	private QueryParserInterface queryParser;
	private ConfigurationAdmin configAdmin;
	
	@Reference
	public void setEventAdmin(EventAdmin pEventAdmin) {
		eventAdmin=pEventAdmin;
	}
	public void unsetEventAdmin(EventAdmin pEventAdmin) {
		eventAdmin=null;
	}
	
	@Reference
	public void setConfigAdmin(ConfigurationAdmin pConfigAdmin) {
		configAdmin=pConfigAdmin;
	}
	public void unsetConfigAdmin(ConfigurationAdmin pConfigAdmin) {
		configAdmin=null;
	}
	
	@Reference
	public void setMetaDataProvider(MetaDataProviderInterface pMetaDataProvider) {
		metaDataProvider=pMetaDataProvider;
	}
	public void unsetIndexer(MetaDataProviderInterface pMetaDataProvider) {
		metaDataProvider=null;
	}
	
	@Reference
	public void setQueryParser(QueryParserInterface pQueryParser) {
		queryParser=pQueryParser;
	}
	public void unsetQueryParser(QueryParserInterface pQueryParser) {
		queryParser=null;
	}
	
	@Activate
	void activate(Map<String,Object> map) {
		playlist=new ArrayList<>();
		isRepeated=false;
		isRandomized=false;
		modified(map);
	}

	@Modified
	void modified(Map<String,Object> map) {
		if (map.containsKey(RANDOM))
			isRandomized = (boolean) map.get(RANDOM);
		if (map.containsKey(REPEAT))
			isRepeated = (boolean) map.get(REPEAT);
		postBooleanEvent(isRandomized, PlaylistInterface.RANDOM_CHANGED_EVENT);
		postBooleanEvent(isRepeated, PlaylistInterface.REPEAT_CHANGED_EVENT);
	}
	
	/* (non-Javadoc)
	 * @see org.bragi.playlist.PlaylistInterface#addMedia(java.lang.String)
	 */
	@Override
	public void addMedia(String uri) throws URISyntaxException {
		if (uri!=null && !uri.isEmpty() && metaDataProvider!=null) { //only do something if uri is not null and not empty
			URI uriObject = new URI(uri);
			PlaylistEntry entry=new PlaylistEntry();
			entry.setUri(uriObject);
			if (playlist.add(entry)) { //only post even if operation was successful
				entry.setMetaData(createMetaData(uri));
				postEvent(uriObject,PlaylistInterface.ADD_EVENT);
			}
		}
	}
	/**
	 * @param uri
	 * @return
	 * @throws URISyntaxException
	 */
	private Map<MetaDataEnum, String> createMetaData(String uri) throws URISyntaxException {
		EnumSet<MetaDataEnum> metaData=EnumSet.allOf(MetaDataEnum.class);
		String[] metaDataValues=metaDataProvider.getMetaData(uri, metaData);
		final AtomicInteger index=new AtomicInteger(0);
		Map<MetaDataEnum,String> metaDataValuesMap=metaData.stream().collect(Collectors.toMap(m->m, m->metaDataValues[index.getAndIncrement()]));
		return metaDataValuesMap;
	}
	

	/* (non-Javadoc)
	 * @see org.bragi.playlist.PlaylistInterface#removeMedia(java.lang.String)
	 */
	@Override
	public void removeMedia(int index) {
		if (index>=0 && index<playlist.size()) { //only do something if uri is not null and not empty
			URI uriObject=playlist.remove(index).getUri();
			postEvent(uriObject,PlaylistInterface.REMOVE_EVENT);
		}
	}

	/* (non-Javadoc)
	 * @see org.bragi.playlist.PlaylistInterface#insertMedia(int, java.lang.String)
	 */
	@Override
	public void insertMedia(int index, String uri) throws URISyntaxException {
		if (uri!=null && !uri.isEmpty() && metaDataProvider!=null) { //only do something if uri is not null and not empty
			URI uriObject = URI.create(uri);
			PlaylistEntry entry=new PlaylistEntry();
			entry.setUri(uriObject);
			int oldSize=playlist.size();
			playlist.add(index, entry);
			if (oldSize!=playlist.size()) {
				entry.setMetaData(createMetaData(uri));
				HashMap<String,Object> eventData=new HashMap<>();
				eventData.put(URI_EVENTDATA, uriObject);
				eventData.put(INDEX_EVENTDATA, index);
				eventAdmin.postEvent(new Event(PlaylistInterface.INSERT_EVENT,eventData));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.bragi.playlist.PlaylistInterface#shuffle()
	 */
	@Override
	public void shuffle() {
		Collections.shuffle(playlist);
	}

	/* (non-Javadoc)
	 * @see org.bragi.playlist.PlaylistInterface#save(java.lang.String)
	 */
	@Override
	public void save(String url) {
		try {
			//create generic playlist
			christophedelory.playlist.Playlist genericPlaylist=new christophedelory.playlist.Playlist();
			Sequence sequence=genericPlaylist.getRootSequence();
			for (PlaylistEntry entry : playlist) {
				Media media=new Media();
				Content content=new Content(entry.getUri());
				media.setSource(content);
				sequence.addComponent(media);
			}
			URI uri=URI.create(url);
			SpecificPlaylistProvider provider = getPlaylistProvider(uri);
			//"wrap" generic playlist in specific playlist
			SpecificPlaylist specificPlaylist = provider.toSpecificPlaylist(genericPlaylist);
			//save the playlist
			OutputStream outputStream;
			if (url.contains("file")) { //special handling for file://-uris
				outputStream=new FileOutputStream(uri.getPath());
			}
			else {
				URL urlObject=uri.toURL();
				URLConnection connection = urlObject.openConnection();
				connection.setDoOutput(true);
				outputStream=connection.getOutputStream();
			}
			specificPlaylist.writeTo(outputStream, null);
			outputStream.flush();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/* (non-Javadoc)
	 * @see org.bragi.playlist.PlaylistInterface#load(java.lang.String)
	 */
	@Override
	public void load(String url) {
		try {
			//initialize logger
			Log log=LogFactory.getLog(getClass());
			//create URI-object for given URL
			URI uri=URI.create(url);
			SpecificPlaylistProvider provider = getPlaylistProvider(uri);
			//load the playlist
			try (FileInputStream fileInputStream = new FileInputStream(new File(uri))) {
				SpecificPlaylist specificPlaylist=provider.readFrom(fileInputStream, null, log);
				playlist.clear();
				//traverse the playlist and visit it's entries using an instance of LucenePlaylistVisitor
				christophedelory.playlist.Playlist genericPlaylist=specificPlaylist.toPlaylist();
				genericPlaylist.acceptDown(new PlaylistVisitor());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * @param uri
	 * @return
	 */
	private SpecificPlaylistProvider getPlaylistProvider(URI uri) {
		//get extension of URL
		String extension=uri.getPath();
		extension=extension.substring(extension.lastIndexOf("."));
		//get playlistprovider for the extension
		SpecificPlaylistProvider provider = SpecificPlaylistFactory.getInstance().findProviderByExtension(extension);
		return provider;
	}

	/**
	 * Post a new event of type eventType to the EventAdmin-service
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
	
	/**
	 * 
	 */
	private void postBooleanEvent(boolean value, String eventType) {
		if (eventAdmin!=null) {
			HashMap<String,Boolean> eventData=new HashMap<>();
			eventData.put(PlaylistInterface.BOOLEAN_EVENTDATA, value);
			eventAdmin.postEvent(new Event(eventType, eventData));
		}
	}
	
	@Override
	public List<PlaylistEntry> filter(String query) {
		List<PlaylistEntry> retValue = new ArrayList<>();
		if (query!=null && queryParser!=null) {
			try {
				Map<URI, Map<MetaDataEnum, String>> metaData=playlist.stream().distinct().collect(Collectors.toMap(entry->entry.getUri(),entry->entry.getMetaData()));
				Map<URI, Map<MetaDataEnum, String>> filteredMetaData=queryParser.execute(query, metaData);
				return playlist.stream().filter(entry->filteredMetaData.containsKey(entry.getUri())).collect(Collectors.toList());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return retValue;
	}
	@Override
	public boolean getRepeat() {
		return isRepeated;
	}
	@Override
	public void setRepeat(boolean repeat) {
		isRepeated=repeat;
		createOrUpdateConfiguration();
	}
	@Override
	public boolean getRandom() {
		return isRandomized;
	}
	@Override
	public void setRandom(boolean random) {
		isRandomized=random;
		createOrUpdateConfiguration();
	}
	
	/**
	 * This method is used to either create or update the configuration of the component
	 */
	private void createOrUpdateConfiguration() {
		try {
			Configuration configuration = configAdmin.getConfiguration(COMPONENT_NAME, "?");
			Hashtable<String, Object> map = new Hashtable<>();
			map.put(REPEAT, isRepeated);
			map.put(RANDOM, isRandomized); 
			configuration.update(map);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
