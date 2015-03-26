/**
 * 
 */
package org.bragi.player.statemachines;

import java.util.EventObject;

/**
 * @author christoph
 *
 */
public class EngineStateChangeEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3556950211793152293L;
	
	private EngineStateEnum currentState;
	
	private String engineEvent;
	
	private EngineStateEnum newState;

	public EngineStateChangeEvent(Object source, EngineStateEnum pCurrentState, String pEngineEvent, EngineStateEnum pNewState) {
		super(source);
		currentState=pCurrentState;
		engineEvent=pEngineEvent;
		newState=pNewState;
	}

	public EngineStateEnum getCurrentState() {
		return currentState;
	}

	public String getEngineEvent() {
		return engineEvent;
	}

	public EngineStateEnum getNewState() {
		return newState;
	}
	
	

}
