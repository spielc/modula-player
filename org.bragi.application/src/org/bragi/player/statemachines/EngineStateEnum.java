/**
 * 
 */
package org.bragi.player.statemachines;

/**
 * This enum defines the different states a engine can be in
 * @author christoph
 *
 */
public enum EngineStateEnum {
	
	/**
	 * A engine plugin is loaded
	 */
	LOADED,
	
	/**
	 * The engine is playing
	 */
	PLAYING,
	
	/**
	 * The engine is paused
	 */
	PAUSED,
	
	/**
	 * No engine plugin is loaded
	 */
	UNLOADED,
	
	/**
	 * Just a marker for an invalid transition
	 */
	INVALID
}
