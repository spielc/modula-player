/**
 * 
 */
package org.bragi.player.statemachines;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;

import org.bragi.engine.EngineInterface;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * @author christoph
 *
 */
@org.osgi.service.component.annotations.Component(property={"event.topics=org/bragi/engine/event/*"},immediate=true)
public class EngineStateMachine implements EventHandler {
	
	private EventListenerList listeners;
	private EngineStateEnum currentState;
	private boolean currentSongFinished;
	
	public EngineStateMachine() {
		currentSongFinished=true;
		currentState=EngineStateEnum.LOADED;
		listeners=new EventListenerList();
	}
	
	@org.osgi.service.component.annotations.Activate 
	public void activate(Map<String,Object> props) {
		System.out.println("Hi from EngineStateMachine");
	}

    @org.osgi.service.component.annotations.Deactivate 
    public void deactivate() {
    	System.out.println("Bye from EngineStateMachine");
    }
	
	@Override
	public void handleEvent(Event event) {
		System.out.print(currentState.name()+"->"+event.getTopic()+"->");
		List<Object> eventData=new ArrayList<>();
		EngineStateEnum newState=currentState;
		switch(event.getTopic()) {
		case EngineInterface.PLAY_EVENT:
			if (currentState==EngineStateEnum.LOADED || currentState==EngineStateEnum.PAUSED || currentState==EngineStateEnum.PLAYING)
				newState=EngineStateEnum.PLAYING;
			else
				newState=EngineStateEnum.INVALID;
			break;
		case EngineInterface.PAUSE_EVENT:
			if (currentState==EngineStateEnum.PLAYING)
				newState=EngineStateEnum.PAUSED;
			else
				newState=EngineStateEnum.INVALID;
			break;
		case EngineInterface.STOP_EVENT:
			if (currentState==EngineStateEnum.PLAYING || currentState==EngineStateEnum.PAUSED)
				newState=EngineStateEnum.LOADED;
			else
				newState=EngineStateEnum.INVALID;
			currentSongFinished=true;
			break;
		case EngineInterface.FORWARD_EVENT:
		case EngineInterface.BACKWARD_EVENT:
		case EngineInterface.JUMP_EVENT:
			if (currentState==EngineStateEnum.PLAYING)
				newState=EngineStateEnum.PLAYING;
			eventData.add(event.getProperty(EngineInterface.CURRENT_INDEX));
			currentSongFinished=true;
			break;
		case EngineInterface.DURATION_CHANGED_EVENT:
			if (currentState==EngineStateEnum.PLAYING)
				newState=EngineStateEnum.PLAYING;
			if (currentState!=EngineStateEnum.PLAYING || !currentSongFinished)
				return;
			currentSongFinished=false;
			eventData.add(event.getProperty(EngineInterface.CURRENT_DURATION));
			break;
		case EngineInterface.FINISHED_EVENT:
			currentSongFinished=true;
			break;
		default: 
			System.out.println("Unknown eventtype '"+event.getTopic()+"'!");
		}
		for (EngineStateChangeListener listener : listeners.getListeners(EngineStateChangeListener.class)) {
			listener.stateChange(currentState, event.getTopic(), newState, eventData.toArray());
		}
		currentState=newState;
		System.out.println(currentState.name());
	}
	
	/**
	 * Add a new EngineStateChangeListener to listeners
	 * @param listener the EngineStateChangeListener-object to add
	 */
	@org.osgi.service.component.annotations.Reference(cardinality=ReferenceCardinality.MULTIPLE, policy=ReferencePolicy.DYNAMIC)
	public void addListener(EngineStateChangeListener listener) {
		listeners.add(EngineStateChangeListener.class, listener);
		listener.stateChange(currentState, EngineInterface.NOOP_EVENT, currentState);
	}

	/**
	 * Remove the specified EngineStateChangeListener-object from listeners
	 * @param listener the EngineStateChangeListener-object to remove
	 */
	public void removeListener(EngineStateChangeListener listener) {
		listeners.remove(EngineStateChangeListener.class, listener);
	}
}
