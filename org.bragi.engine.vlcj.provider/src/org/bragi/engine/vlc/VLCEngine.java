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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.bragi.engine.EngineInterface;
import org.bragi.engine.vlc.internal.URIParser;
import org.bragi.engine.vlc.internal.VLCMediaPlayerComponent;
import org.bragi.playlist.PlaylistInterface;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
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
@Component(property="event.topics=org/bragi/playlist/event/*",configurationPolicy=ConfigurationPolicy.OPTIONAL)
public class VLCEngine implements EngineInterface, EventHandler {
	
	private static final String COMPONENT_NAME = "org.bragi.engine.vlc.VLCEngine";
	private static final String VOLUME ="VOLUME";
	
	private VLCMediaPlayerComponent playerComponent;
	private MediaListPlayer player;
	private EventAdmin eventAdmin;
	private ConfigurationAdmin configAdmin;
	private int volume;
	private int currentIndex;
	
	@Activate
	public void activate(Map<String,Object> map) {
		playerComponent=new VLCMediaPlayerComponent();
		player=playerComponent.getMediaListPlayer();
		player.setMediaPlayer(playerComponent.getMediaPlayer());
		currentIndex=0;
		playerComponent.setEventAdmin(eventAdmin);
		modified(map);
	}

	@Modified
	public void modified(Map<String,Object> map) {
		if (map.containsKey(VOLUME))
			setVolume((int) map.get(VOLUME));
	}
	
	@Deactivate
	protected void deactivate(ComponentContext cContext, int reason) {
		if (configAdmin!=null) {
			try {
				Configuration configuration = configAdmin.getConfiguration(COMPONENT_NAME, "?");
				Hashtable<String, Object> map = new Hashtable<>();
				map.put(VOLUME, volume);
				configuration.update(map);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Reference
	public void setEventAdmin(EventAdmin pEventAdmin) {
		eventAdmin=pEventAdmin;
	}
	public void unsetEventAdmin(EventAdmin pEventAdmin) {
		eventAdmin=null;
		playerComponent.setEventAdmin(null);
	}
	
	@Reference
	public void setConfigAdmin(ConfigurationAdmin pConfigAdmin) {
		configAdmin=pConfigAdmin;
	}
	public void unsetConfigAdmin(ConfigurationAdmin pConfigAdmin) {
		configAdmin=null;
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
		}
	}

	@Override
	public void forward() {
		if (player!=null) {
			playerComponent.forward();
		}
	}

	@Override
	public void backward() {
		if (player!=null) {
			playerComponent.backward();
		}
	}

	//currently broken
	@Override
	public void toggleMute() {
		if (player!=null) {
			//temporary workaround
			int curVolume=playerComponent.getMediaPlayer().getVolume();
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
		if (eventAdmin!=null) {
			volume=LibVlcConst.MIN_VOLUME;
			volume=(newVolume<LibVlcConst.MAX_VOLUME)?newVolume:LibVlcConst.MAX_VOLUME;
			Map<String,Object> eventProperties=new HashMap<>();
			eventProperties.put(CURRENT_VOLUME, volume);
			Event event=new Event(VOLUME_CHANGED_EVENT, eventProperties);
			eventAdmin.postEvent(event);
		}
	}

	
	@Override
	public void handleEvent(Event event) {
		if (event.containsProperty(PlaylistInterface.URI_EVENTDATA)) 
			handleEventWithUriEventData(event);
		else if (event.containsProperty(PlaylistInterface.BOOLEAN_EVENTDATA))
			handleEventWithBooleanEventData(event);
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
		playerComponent.playlistChanged();
	}
	
	private void handleEventWithBooleanEventData(Event event) {
		boolean value=(boolean)event.getProperty(PlaylistInterface.BOOLEAN_EVENTDATA);
		switch(event.getTopic()) {
		case PlaylistInterface.RANDOM_CHANGED_EVENT:
			playerComponent.setRandom(value);
			break;
		case PlaylistInterface.REPEAT_CHANGED_EVENT:
			playerComponent.setRepeat(value);
			if (value)
				playerComponent.getMediaListPlayer().setMode(MediaListPlayerMode.LOOP);
			else
				playerComponent.getMediaListPlayer().setMode(MediaListPlayerMode.DEFAULT);
			break;
		}
	}

	@Override
	public void seek(long milliseconds) {
		if (null!=playerComponent)
			playerComponent.getMediaPlayer().setTime(milliseconds);
	}
}
