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
package org.bragi.engine.vlc;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.bragi.engine.EngineInterface;
import org.bragi.engine.vlc.internal.URIParser;
import org.bragi.engine.vlc.internal.VLCMediaPlayerComponent;
import org.bragi.playlist.PlaylistInterface;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;

import uk.co.caprica.vlcj.binding.LibVlcConst;
import uk.co.caprica.vlcj.medialist.MediaListItem;
import uk.co.caprica.vlcj.player.list.MediaListPlayer;
import uk.co.caprica.vlcj.player.list.MediaListPlayerMode;

/**
 * @author christoph
 *
 */
@Component(property="event.topics=org/bragi/playlist/event/*")
public class VLCEngine implements EngineInterface, EventHandler {
	
	private VLCMediaPlayerComponent playerComponent;
	private MediaListPlayer player;
	private EventAdmin eventAdmin;
	private int volume;
	private int currentIndex;
	
	public VLCEngine() {
		playerComponent=new VLCMediaPlayerComponent();
		player=playerComponent.getMediaListPlayer();
		player.setMediaPlayer(playerComponent.getMediaPlayer());
		currentIndex=0;
	}
	
	@Reference
	public void setEventAdmin(EventAdmin pEventAdmin) {
		eventAdmin=pEventAdmin;
		playerComponent.setEventAdmin(eventAdmin);
	}
	public void unsetEventAdmin(EventAdmin pEventAdmin) {
		eventAdmin=null;
		playerComponent.setEventAdmin(null);
	}
	
	/* (non-Javadoc)
	 * @see org.bragi.engine.EngineInterface#play()
	 */
	@Override
	public void play(int itemIndex) {
		if (player!=null) {
			playerComponent.play(itemIndex);
			if (itemIndex!=currentIndex)
				player.playItem(itemIndex);
			else
				player.play();
//			postNavigateEvents(currentIndex, itemIndex);
			currentIndex=itemIndex;
			
			//playerComponent.postEvent(new Event(EngineInterface.PLAY_EVENT,(Map<String,Object>)null));
		}
	}

	/* (non-Javadoc)
	 * @see org.bragi.engine.EngineInterface#pause()
	 */
	@Override
	public void pause() {
		if (player!=null) {
			player.pause();
			//playerComponent.postEvent(new Event(EngineInterface.PAUSE_EVENT,(Map<String,Object>)null));
		}
	}

	/* (non-Javadoc)
	 * @see org.bragi.engine.EngineInterface#stop(boolean)
	 */
	@Override
	public void stop(boolean immediately) {
		//the parameter is currently not used; will be used for stop-after-track feature
		if (player!=null) {
			player.stop();
			//playerComponent.postEvent(new Event(EngineInterface.STOP_EVENT,(Map<String,Object>)null));
		}
	}

	@Override
	public void forward() {
		if (player!=null) {
			//currentIndex++;
			playerComponent.forward();
			player.playNext();
//			postNavigateEvents(currentIndex, ++currentIndex);
			//playerComponent.postEvent(new Event(EngineInterface.FORWARD_EVENT,(Map<String,Object>)null));
		}
	}

	@Override
	public void backward() {
		if (player!=null) {
			//currentIndex--;
			playerComponent.backward();
			player.playPrevious();
//			postNavigateEvents(currentIndex, --currentIndex);
			//playerComponent.postEvent(new Event(EngineInterface.BACKWARD_EVENT,(Map<String,Object>)null));
		}
	}

	//currently broken
	@Override
	public void toggleMute() {
		if (player!=null) {
			//temporary workaround
			int curVolume=getVolume();
			if (curVolume==0) 
				setVolume(volume);
			else {
				volume=curVolume;
				setVolume(0);
			}
		}
	}

	
	@Override
	public void setVolume(int newVolume) {
		if (player!=null) 
			playerComponent.getMediaPlayer().setVolume(newVolume);
	}

	
	@Override
	public int getVolume() {
		int volume=LibVlcConst.MIN_VOLUME;
		if (player!=null)
			volume = playerComponent.getMediaPlayer().getVolume();
		return (volume<LibVlcConst.MAX_VOLUME)?volume:LibVlcConst.MAX_VOLUME;
	}
	
	@Override
	public void handleEvent(Event event) {
		if (event.containsProperty(PlaylistInterface.URI_EVENTDATA)) 
			handleEventWithUriEventData(event);
		else if (event.containsProperty(PlaylistInterface.BOOLEAN_EVENTDATA))
			handleEventWithBooleanEventData(event);
//		if (event.containsProperty(PlaylistInterface.URI_EVENTDATA)) {
//			try {
//				URI uri=(URI)event.getProperty(PlaylistInterface.URI_EVENTDATA);
//				String uriString=URIParser.getMrl(uri.toString()).value();
//				int index=0;
//				switch (event.getTopic()) {
//				case PlaylistInterface.ADD_EVENT:
//					playerComponent.getMediaList().addMedia(uriString);
//					System.out.println(uriString+" added!!");
//					break;
//				case PlaylistInterface.INSERT_EVENT:
//					index=(int)event.getProperty(PlaylistInterface.INDEX_EVENTDATA);
//					playerComponent.getMediaList().insertMedia(index, uriString);
//					break;
//				case PlaylistInterface.REMOVE_EVENT:
//					List<MediaListItem> items = playerComponent.getMediaList().items();
//					for (MediaListItem item : items) {
//						if (item.mrl().equals(uriString)) 
//							break;
//						index++;
//					}
//					if (index!=items.size())
//						playerComponent.getMediaList().removeMedia(index);
//					break;
//				}
//			} catch (URISyntaxException e) {
//				e.printStackTrace();
//			}
//		}
//		player.setMediaList(playerComponent.getMediaList());
	}
	
	private void handleEventWithUriEventData(Event event) {
		try {
			URI uri=(URI)event.getProperty(PlaylistInterface.URI_EVENTDATA);
			String uriString=URIParser.getMrl(uri.toString()).value();
			int index=0;
			switch (event.getTopic()) {
			case PlaylistInterface.ADD_EVENT:
				playerComponent.getMediaList().addMedia(uriString);
				System.out.println(uriString+" added!!");
				break;
			case PlaylistInterface.INSERT_EVENT:
				index=(int)event.getProperty(PlaylistInterface.INDEX_EVENTDATA);
				playerComponent.getMediaList().insertMedia(index, uriString);
				break;
			case PlaylistInterface.REMOVE_EVENT:
				List<MediaListItem> items = playerComponent.getMediaList().items();
				for (MediaListItem item : items) {
					if (item.mrl().equals(uriString)) 
						break;
					index++;
				}
				if (index!=items.size())
					playerComponent.getMediaList().removeMedia(index);
				break;
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		player.setMediaList(playerComponent.getMediaList());
	}
	
	private void handleEventWithBooleanEventData(Event event) {
		boolean value=(boolean)event.getProperty(PlaylistInterface.BOOLEAN_EVENTDATA);
		switch(event.getTopic()) {
		case PlaylistInterface.RANDOM_CHANGED_EVENT:
			break;
		case PlaylistInterface.REPEAT_CHANGED_EVENT:
			if (value)
				playerComponent.getMediaListPlayer().setMode(MediaListPlayerMode.LOOP);
			else
				playerComponent.getMediaListPlayer().setMode(MediaListPlayerMode.DEFAULT);
			break;
		}
	}
}
