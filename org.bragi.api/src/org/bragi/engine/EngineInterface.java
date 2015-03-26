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
package org.bragi.engine;

/**
 * @author christoph
 *
 */
public interface EngineInterface {
	//event-IDs for engine-events
	public static final String PLAY_EVENT = "org/bragi/engine/event/PLAYING";
	public static final String PAUSE_EVENT = "org/bragi/engine/event/PAUSING";
	public static final String STOP_EVENT = "org/bragi/engine/event/STOPPING";
	public static final String FORWARD_EVENT = "org/bragi/engine/event/FORWARD";
	public static final String BACKWARD_EVENT = "org/bragi/engine/event/BACKWARD";
	public static final String FINISHED_EVENT = "org/bragi/engine/event/FINISHED";
	public static final String JUMP_EVENT = "org/bragi/engine/event/JUMPED";
	public static final String DURATION_CHANGED_EVENT = "org/bragi/engine/event/DURATION_CHANGED";
	public static final String NOOP_EVENT = "org/bragi/engine/event/NOOP";
	//event-property ids for engine-events
	public static final String CURRENT_INDEX = "CurrentIndex";
	public static final String CURRENT_DURATION = "CurrentDuration";
	//Playback methods
	public void play(int itemIndex);
	public void pause();
	public void stop(boolean immediately);
	public void forward();
	public void backward();
	//Methods for volume adjustment
	public void toggleMute();
	public void setVolume(int newVolume);
	public int getVolume();
}
