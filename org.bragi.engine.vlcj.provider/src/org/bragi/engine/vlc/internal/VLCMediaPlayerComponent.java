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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bragi.engine.EngineInterface;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import uk.co.caprica.vlcj.component.AudioMediaListPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;

public class VLCMediaPlayerComponent extends AudioMediaListPlayerComponent {

	private EventAdmin eventAdmin;
	private int currentIndex;
	private UnlimitedListIterator songOrderIterator;
	private boolean isRandomPlayback;
	private boolean isRepeated;
		
	public VLCMediaPlayerComponent() {
		super();
		currentIndex=-1;
		isRandomPlayback=false;
		isRepeated=false;
	}
	
	public void setEventAdmin(EventAdmin pEventAdmin) {
		eventAdmin=pEventAdmin;
	}
	public void unsetEventAdmin(EventAdmin pEventAdmin) {
		eventAdmin=null;
	}
	
	@Override
	public void finished(MediaPlayer mediaPlayer) {
		changeSong(songOrderIterator::next);
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
	
	public void backward() {
		changeSong(songOrderIterator::previous);
	}
	
	public void forward() {
		changeSong(songOrderIterator::next);
	}
	
	private void changeSong(Supplier<Integer> newIndexSupplier) {
		int newIndex=newIndexSupplier.get();
		getMediaListPlayer().playItem(newIndex);
		postNavigateEvents(currentIndex, newIndex);
		currentIndex=newIndex;
	}
	
	/**
	 * Post the event to the evantad
	 */
	public void postEvent(Event event) {
		if (eventAdmin!=null)
			eventAdmin.postEvent(event);
	}
	
	public void play(int pCurrentIndex) {
		songOrderIterator.setCurrentIndex(pCurrentIndex);
		currentIndex=pCurrentIndex;
	}
	
	public void playlistChanged() {
		List<Integer> tmpList = IntStream.range(0, getMediaList().size()).boxed().collect(Collectors.toList());
		if (isRandomPlayback)
			Collections.shuffle(tmpList);
		songOrderIterator=new UnlimitedListIterator(tmpList);
		songOrderIterator.setRepeated(isRepeated);
	}
	
	/**
	 * 
	 * @param lastIndex
	 * @param currentIndex
	 */
	private void postNavigateEvents(int lastIndex, int currentIndex) {
		Map<String,Object> eventProperties=new HashMap<>();
		eventProperties.put(EngineInterface.CURRENT_INDEX, currentIndex);
		Event event=null;
		if (Math.abs(lastIndex-currentIndex)>1)
			event=new Event(EngineInterface.JUMP_EVENT,eventProperties);
		else if (lastIndex<currentIndex)
			event=new Event(EngineInterface.FORWARD_EVENT,eventProperties);
		else if (lastIndex>currentIndex)
			event=new Event(EngineInterface.BACKWARD_EVENT,eventProperties);
		if (event!=null)			
			postEvent(event);
	}
	
	public void setRepeat(boolean repeat) {
		isRepeated=repeat;
		if (songOrderIterator!=null)
			songOrderIterator.setRepeated(repeat);
	}
	
	public void setRandom(boolean random) {
		isRandomPlayback=random;
		playlistChanged();
	}
}
