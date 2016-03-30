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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.bragi.engine.EngineInterface;
import org.bragi.engine.vlc.internal.VLCMediaPlayerComponent;
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

import uk.co.caprica.vlcj.binding.LibVlcConst;
import uk.co.caprica.vlcj.player.MediaPlayer;

/**
 * @author christoph
 *
 */
@Component(configurationPolicy=ConfigurationPolicy.OPTIONAL)
public class VLCEngine implements EngineInterface {
	
	private static final String COMPONENT_NAME = "org.bragi.engine.vlc.VLCEngine";
	private static final String VOLUME ="VOLUME";
	
	private VLCMediaPlayerComponent playerComponent;
	private MediaPlayer player;
	private EventAdmin eventAdmin;
	private ConfigurationAdmin configAdmin;
	private int volume;
	
	@Activate
	public void activate(Map<String,Object> map) {
		playerComponent=new VLCMediaPlayerComponent();
		player=playerComponent.getMediaPlayer();
		playerComponent.setEventAdmin(eventAdmin);
		modified(map);
	}

	@Modified
	public void modified(Map<String,Object> map) {
		if (map.containsKey(VOLUME))
			setVolume((Integer)map.get(VOLUME));
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
	 * @see org.bragi.engine.EngineInterface#pause()
	 */
	@Override
	public void pause() {
		if (player!=null) {
			player.pause();
		}
	}

	@Override
	public void forward() {
		if (eventAdmin!=null) {
			Map<String,Object> eventProperties=new HashMap<>();
			Event event=new Event(EngineInterface.FORWARD_EVENT,eventProperties);
			eventAdmin.postEvent(event);
		}
	}

	@Override
	public void backward() {
		if (eventAdmin!=null) {
			Map<String,Object> eventProperties=new HashMap<>();
			Event event=new Event(EngineInterface.BACKWARD_EVENT,eventProperties);
			eventAdmin.postEvent(event);
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
		volume=LibVlcConst.MIN_VOLUME;
		volume=(newVolume<LibVlcConst.MAX_VOLUME)?newVolume:LibVlcConst.MAX_VOLUME;
		if (player!=null) 
			player.setVolume(newVolume);
		if (eventAdmin!=null) {
			Map<String,Object> eventProperties=new HashMap<>();
			eventProperties.put(CURRENT_VOLUME, volume);
			Event event=new Event(VOLUME_CHANGED_EVENT, eventProperties);
			eventAdmin.postEvent(event);
		}
	}

	@Override
	public void seek(long milliseconds) {
		if (null!=player)
			player.setTime(milliseconds);
	}

	@Override
	public void play(String url) {
		if (player!=null)
			player.playMedia(url);
	}
	
	@Override
	public void play() {
		if (player!=null)
			player.play();
	}

	@Override
	public void stop() {
		if (player!=null)
			player.stop();
	}
}
