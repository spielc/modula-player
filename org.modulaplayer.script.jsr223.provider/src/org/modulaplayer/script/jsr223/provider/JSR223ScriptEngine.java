package org.modulaplayer.script.jsr223.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.script.ScriptEngineManager;

import org.modulaplayer.script.AbstractScriptEngine;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.event.Event;

/**
 * 
 */
@Component(name="org.modulaplayer.script.JSR223ScriptEngine", property={"event.topics=*"}, configurationPolicy=ConfigurationPolicy.REQUIRE, service={org.osgi.service.event.EventHandler.class, AbstractScriptEngine.class})
public class JSR223ScriptEngine extends AbstractScriptEngine implements org.osgi.service.event.EventHandler {
	
	private javax.script.ScriptEngine scriptEngine;
	
	@Activate
	public void activate(Map<String,Object> map) {
		ScriptEngineManager manager=new ScriptEngineManager();
		scriptEngine=manager.getEngineByName(map.get("name").toString());
		extensions=scriptEngine.getFactory().getExtensions();
	}

	@Override
	public void runScript(String name) throws Exception {
		scriptEngine.put("engine", this);
		String script=loadedScripts.get(name);
		if (null!=script)
			runningScripts.put(name, scriptEngine.eval(script));
	}

	@Override
	public void stopScript(String name) {
		Object scriptObject=runningScripts.remove(name);
		eventHandlers.values().removeIf(entry->entry.containsKey(scriptObject));
	}

	@Override
	public void handleEvent(Event event) {
		String topic=event.getTopic();
		List<Consumer<Event>> handlers=new ArrayList<>();
		handlers.addAll(eventHandlers.entrySet().stream().filter(entry->entry.getKey().equals(topic))
														 .map(entry->entry.getValue().values())
														 .flatMap(entry->entry.stream())
														 .flatMap(entry->entry.stream())
														 .collect(Collectors.toList()));
		handlers.addAll(eventHandlers.entrySet().stream().filter(entry->entry.getKey().contains("*"))
																	 .filter(entry->event.getTopic().contains(entry.getKey().substring(0, entry.getKey().indexOf("*"))))
																	 .map(entry->entry.getValue().values())
																	 .flatMap(entry->entry.stream())
																	 .flatMap(entry->entry.stream())
																	 .collect(Collectors.toList()));
		handlers.forEach(handler->handler.accept(event));
	}

	@Override
	protected void registerObjectCore(String name, Object object) {
		scriptEngine.put(name, object);
	}
	
	
}
