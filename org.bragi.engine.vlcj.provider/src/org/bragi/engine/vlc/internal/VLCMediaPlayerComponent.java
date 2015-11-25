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
package org.bragi.engine.vlc.internal;

import java.util.HashMap;
import java.util.Map;

import org.bragi.engine.EngineInterface;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import uk.co.caprica.vlcj.component.AudioMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;

public class VLCMediaPlayerComponent extends AudioMediaPlayerComponent {

	private EventAdmin eventAdmin;
		
	public void setEventAdmin(EventAdmin pEventAdmin) {
		eventAdmin=pEventAdmin;
	}
	public void unsetEventAdmin(EventAdmin pEventAdmin) {
		eventAdmin=null;
	}
	
	@Override
	public void finished(MediaPlayer mediaPlayer) {
		postEvent(new Event(EngineInterface.FINISHED_EVENT,(Map<String,Object>)null));
	}

	@Override
	public void paused(MediaPlayer mediaPlayer) {
		postEvent(new Event(EngineInterface.PAUSE_EVENT,(Map<String,Object>)null));	
	}
	
	@Override
	public void playing(MediaPlayer mediaPlayer) {
		postEvent(new Event(EngineInterface.PLAY_EVENT,(Map<String,Object>)null));
		long length = mediaPlayer.getLength();
		Map<String,Object> eventProperties=new HashMap<>();
		eventProperties.put(EngineInterface.CURRENT_DURATION, (int)length);
		postEvent(new Event(EngineInterface.DURATION_CHANGED_EVENT,eventProperties));
	}
	

	@Override
	public void stopped(MediaPlayer mediaPlayer) {
		postEvent(new Event(EngineInterface.STOP_EVENT,(Map<String,Object>)null));	
	
	}
	
	@Override
	public void error(MediaPlayer mediaPlayer) {
		System.out.println("error occured");
	}
	
	/**
	 * Post the event to the evantad
	 */
	public void postEvent(Event event) {
		if (eventAdmin!=null)
			eventAdmin.postEvent(event);
	}
}
