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
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bragi.indexer.IndexerInterface;
import org.bragi.metadata.MetaDataEnum;
import org.bragi.playlist.PlaylistInterface;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import christophedelory.content.Content;
import christophedelory.playlist.Media;
import christophedelory.playlist.Parallel;
import christophedelory.playlist.PlaylistVisitor;
import christophedelory.playlist.Sequence;
import christophedelory.playlist.SpecificPlaylist;
import christophedelory.playlist.SpecificPlaylistFactory;
import christophedelory.playlist.SpecificPlaylistProvider;

/**
 * @author christoph
 *
 */
@org.osgi.service.component.annotations.Component
public class Playlist implements PlaylistInterface {
	
	/**
	 * Private class which implements a Comparator for URI-objects.
	 * This is used for sorting the playlist correctly
	 * @author christoph
	 *
	 */
	private final class LucenePlaylistComparator implements Comparator<URI> {
		
		private Map<URI, Map<MetaDataEnum, String>> uriMap;
		
		private LucenePlaylistComparator(Map<URI, Map<MetaDataEnum, String>> pUriMap ) {
			uriMap = pUriMap;
		}

		@Override
		public int compare(URI o1, URI o2) {
			Integer index1=playlist.indexOf(o1);
			Integer index2=playlist.indexOf(o2);
			return index1.compareTo(index2);
			//Long insertionTime1=Long.parseLong(uriMap.get(o1).get(MetaDataEnum.INSERTION_TIME));
			//Long insertionTime2=Long.parseLong(uriMap.get(o2).get(MetaDataEnum.INSERTION_TIME));;
			//return insertionTime1.compareTo(insertionTime2);
		}
		
	}
	
	/**
	 * 
	 * @author christoph
	 *
	 */
	private final class LucenePlaylistVisitor implements PlaylistVisitor {
		@Override
		public void endVisitSequence(Sequence arg0) throws Exception {
			// TODO Auto-generated method stub
		}

		@Override
		public void endVisitPlaylist(christophedelory.playlist.Playlist arg0) throws Exception {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void endVisitParallel(Parallel arg0) throws Exception {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void endVisitMedia(Media arg0) throws Exception {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void beginVisitSequence(Sequence arg0) throws Exception {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void beginVisitPlaylist(christophedelory.playlist.Playlist arg0) throws Exception {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void beginVisitParallel(Parallel arg0) throws Exception {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void beginVisitMedia(Media media) throws Exception {
			Content content=media.getSource();
			addMedia(content.getURI().toString());
		}
	}

	private List<URI> playlist;
	private boolean isRepeated;
	private boolean isRandomized;
	private EventAdmin eventAdmin;
	private IndexerInterface indexer;
	
	@org.osgi.service.component.annotations.Reference
	public void setEventAdmin(EventAdmin pEventAdmin) {
		eventAdmin=pEventAdmin;
	}
	public void unsetEventAdmin(EventAdmin pEventAdmin) {
		eventAdmin=null;
	}
	
	@org.osgi.service.component.annotations.Reference(target="(type=PlaylistIndexer)")
	public void setIndexer(IndexerInterface pIndexer) {
		indexer=pIndexer;
		for (URI uri : playlist) {
			indexer.indexUri(uri.toString());
		}
	}
	public void unsetIndexer(IndexerInterface pIndexer) {
		try {
			pIndexer.closeIndexWriter();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		indexer=null;
	}
	
	/**
	 * Constructor
	 */
	public Playlist() {
		playlist=new ArrayList<>();
		isRepeated=false;
		isRandomized=false;
	}

	/* (non-Javadoc)
	 * @see org.bragi.playlist.PlaylistInterface#addMedia(java.lang.String)
	 */
	@Override
	public void addMedia(String uri) throws URISyntaxException {
		if (uri!=null && !uri.isEmpty() && indexer!=null) { //only do something if uri is not null and not empty
			URI uriObject = new URI(uri);
			if (playlist.add(uriObject)) { //only post even if operation was successful
				indexer.indexUri(uri);
				postEvent(uriObject,PlaylistInterface.ADD_EVENT);
			}
		}
	}
	

	/* (non-Javadoc)
	 * @see org.bragi.playlist.PlaylistInterface#removeMedia(java.lang.String)
	 */
	@Override
	public void removeMedia(String uri) {
		if (uri!=null && !uri.isEmpty() && indexer!=null) { //only do something if uri is not null and not empty
			URI uriObject = URI.create(uri);
			if (playlist.remove(uriObject)) {
				try {
					indexer.removeUri(uri);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				postEvent(uriObject,PlaylistInterface.REMOVE_EVENT);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.bragi.playlist.PlaylistInterface#insertMedia(int, java.lang.String)
	 */
	@Override
	public void insertMedia(int index, String uri) {
		if (uri!=null && !uri.isEmpty() && indexer!=null) { //only do something if uri is not null and not empty
			URI uriObject = URI.create(uri);
			int oldSize=playlist.size();
			playlist.add(index, uriObject);
			if (oldSize!=playlist.size()) {
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
	 * @see org.bragi.playlist.PlaylistInterface#toggleRepeat()
	 */
	@Override
	public void toggleRepeat() {
		isRepeated=!isRepeated;
	}

	/* (non-Javadoc)
	 * @see org.bragi.playlist.PlaylistInterface#toggleRandom()
	 */
	@Override
	public void toggleRandom() {
		isRandomized=!isRandomized;
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
			for (URI uri : playlist) {
				Media media=new Media();
				Content content=new Content(uri);
				media.setSource(content);
				sequence.addComponent(media);
			}
			URI uri=URI.create(url);
			String extension=uri.getPath();
			extension=extension.substring(extension.lastIndexOf("."));
			//create specific playlist
			SpecificPlaylistProvider provider = SpecificPlaylistFactory.getInstance().findProviderByExtension(extension);
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
			//get extension of URL
			String extension=uri.getPath();
			extension=extension.substring(extension.lastIndexOf("."));
			//get playlistprovider for the extension
			SpecificPlaylistProvider provider = SpecificPlaylistFactory.getInstance().findProviderByExtension(extension);
			//load the playlist
			SpecificPlaylist specificPlaylist=provider.readFrom(new FileInputStream(new File(uri)), null, log);
			playlist.clear();
			//traverse the playlist and visit it's entries using an instance of LucenePlaylistVisitor
			christophedelory.playlist.Playlist genericPlaylist=specificPlaylist.toPlaylist();
			genericPlaylist.acceptDown(new LucenePlaylistVisitor());
		} catch (Exception e) {
			e.printStackTrace();
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
	public Map<URI, Map<MetaDataEnum, String>> filter(String query, MetaDataEnum... metaData) {
		Map<URI, Map<MetaDataEnum, String>> retValue = new Hashtable<>();
		if (query!=null && indexer!=null) {
			try {
				retValue=indexer.filter(query, metaData);
				//TODO currently necessary as sorting is not possible. And won't be included in 1.0! In 2.0 a DSL for queries will be included
				SortedMap<URI, Map<MetaDataEnum, String>> sortedRetValue=new TreeMap<>(new LucenePlaylistComparator(retValue));
				for (Entry<URI, Map<MetaDataEnum, String>>  entry: retValue.entrySet()) {
					sortedRetValue.put(entry.getKey(), entry.getValue());
				}
				return sortedRetValue;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return retValue;
	}
}
