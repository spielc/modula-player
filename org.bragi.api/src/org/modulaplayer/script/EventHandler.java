/**
 * 
 */
package org.modulaplayer.script;

import org.osgi.service.event.Event;

/**
 * @author christoph
 *
 */
@FunctionalInterface
public interface EventHandler {
	void handleEvent(Event event);
}
