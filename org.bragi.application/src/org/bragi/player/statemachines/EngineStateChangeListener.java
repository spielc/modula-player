/**
 * 
 */
package org.bragi.player.statemachines;

import java.util.EventListener;

/**
 * @author christoph
 *
 */
public interface EngineStateChangeListener extends EventListener {
	
	public void stateChange(EngineStateEnum currentState, String engineEvent, EngineStateEnum newState, Object... eventData);
}
