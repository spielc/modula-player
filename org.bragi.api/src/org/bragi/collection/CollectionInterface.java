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
package org.bragi.collection;

import java.net.URISyntaxException;
import java.util.List;

import org.bragi.metadata.MetaDataEnum;

/**
 * @author christoph
 *
 */
public interface CollectionInterface {
	//event-IDs for playlist-events
	public static final String ADD_EVENT = "org/bragi/collection/event/ADDED";
	public static final String REMOVE_EVENT = "org/bragi/collection/event/REMOVED";
	public static final String MODIFY_EVENT = "org/bragi/collection/event/MODIFY";
	//various other constants
	public static final String URI_EVENTDATA = "org/bragi/collection/eventData/URI";
	public static final String INDEX_EVENTDATA = "org/bragi/collection/eventData/INDEX";
	//methods for working with collections
	public void addMedia(String uri) throws URISyntaxException;
	public void addCollectionRoot(String uri) throws URISyntaxException;
	public void removeMedia(String uri);
	public List<CollectionEntry> filter(String query);
}
