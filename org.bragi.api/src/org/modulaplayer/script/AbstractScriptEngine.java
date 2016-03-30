/**
 * 
 */
package org.modulaplayer.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author christoph
 *
 */
public abstract class AbstractScriptEngine {
	
	protected Map<String, String> loadedScripts;
	protected Map<String, Map<Object, List<EventHandler>>> eventHandlers;
	protected Map<String, Object> runningScripts;
		
	protected AbstractScriptEngine() {
		loadedScripts=new HashMap<>();
		eventHandlers=new HashMap<>();
		runningScripts=new HashMap<>();
	}
	
	public void loadScript(String name, String script) {
		loadedScripts.put(name, script);
	}

	public void unloadScript(String name) {
		loadedScripts.remove(name);
		stopScript(name);
	}
		
	public void registerEventHandler(String eventTopic, Object obj, EventHandler eventHandler) {
		if (null==obj)
			throw new IllegalArgumentException("obj must NOT be null");
		if (eventHandlers.containsKey(eventTopic)) {
			Map<Object, List<EventHandler>> eventHandlersForTopic=eventHandlers.get(eventTopic);
			if (eventHandlersForTopic.containsKey(obj))
				eventHandlersForTopic.get(obj).add(eventHandler);
			else {
				List<EventHandler> evtHandlers=new ArrayList<>();
				evtHandlers.add(eventHandler);
				eventHandlersForTopic.put(obj, evtHandlers);
			}
		}
		else {
			List<EventHandler> evtHandlers=new ArrayList<>();
			evtHandlers.add(eventHandler);
			Map<Object, List<EventHandler>> eventHandlersForTopic=new HashMap<>();
			eventHandlersForTopic.put(obj, evtHandlers);
			eventHandlers.put(eventTopic, eventHandlersForTopic);
		}
	}
	
	public void registerObject(String name, Object object) {
		registerObjectCore(name, object);
	}
	
	protected void registerObjectCore(String name, Object object) {
		
	}
	
	public abstract void runScript(String name) throws Exception;
	
	public abstract void stopScript(String name);
}
