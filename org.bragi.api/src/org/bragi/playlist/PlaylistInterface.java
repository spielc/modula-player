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
package org.bragi.playlist;

import java.net.URISyntaxException;
import java.util.List;

/**
 * @author christoph
 *
 */
public interface PlaylistInterface {
	//event-IDs for playlist-events
	public static final String ADD_EVENT = "org/bragi/playlist/event/ADDED";
	public static final String REMOVE_EVENT = "org/bragi/playlist/event/REMOVED";
	public static final String INSERT_EVENT = "org/bragi/playlist/event/INSERTED";
	public static final String RANDOM_CHANGED_EVENT = "org/bragi/playlist/event/RANDOM_CHANGED";
	public static final String REPEAT_CHANGED_EVENT = "org/bragi/playlist/event/REPEAT_CHANGED";
	//various other constants
	public static final String URI_EVENTDATA = "org/bragi/playlist/eventData/URI";
	public static final String INDEX_EVENTDATA = "org/bragi/playlist/eventData/INDEX";
	public static final String BOOLEAN_EVENTDATA = "org/bragi/playlist/eventData/BOOLEAN";
	//methods for working with playlists
	public void addMedia(String uri) throws URISyntaxException;
	public void removeMedia(int index);
	public void insertMedia(int index, String uri) throws URISyntaxException;
	public void shuffle();
	public void save(String uri);
	public void load(String uri);
	// TODO Because a playlist may contain a certain URI multiple times
	public List<PlaylistEntry> filter(String query);
	
	//methods for getting and setting repeat and random-state of the playlist
	public boolean getRepeat();
	public void setRepeat(boolean repeat);
	public boolean getRandom();
	public void setRandom(boolean random);
}
