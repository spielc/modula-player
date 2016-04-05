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
package org.modulaplayer.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.osgi.service.event.Event;

/**
 * @author christoph
 *
 */
public abstract class AbstractScriptEngine {
	
	protected Map<String, String> loadedScripts;
	protected Map<String, Map<Object, List<Consumer<Event>>>> eventHandlers;
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
		
	public void registerEventHandler(String eventTopic, Object obj, Consumer<Event> eventHandler) {
		if (null==obj)
			throw new IllegalArgumentException("obj must NOT be null");
		if (eventHandlers.containsKey(eventTopic)) {
			Map<Object, List<Consumer<Event>>> eventHandlersForTopic=eventHandlers.get(eventTopic);
			if (eventHandlersForTopic.containsKey(obj))
				eventHandlersForTopic.get(obj).add(eventHandler);
			else {
				List<Consumer<Event>> evtHandlers=new ArrayList<>();
				evtHandlers.add(eventHandler);
				eventHandlersForTopic.put(obj, evtHandlers);
			}
		}
		else {
			List<Consumer<Event>> evtHandlers=new ArrayList<>();
			evtHandlers.add(eventHandler);
			Map<Object, List<Consumer<Event>>> eventHandlersForTopic=new HashMap<>();
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
