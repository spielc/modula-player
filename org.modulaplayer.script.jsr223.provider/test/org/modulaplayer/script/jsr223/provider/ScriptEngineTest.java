/**
 * 
 */
package org.modulaplayer.script.jsr223.provider;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.bragi.engine.EngineInterface;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.Event;

/**
 * @author christoph
 *
 */
public class ScriptEngineTest {
	
	private static final String ENGINE = "ENGINE";
	private JSR223ScriptEngine scriptEngine;
	private EngineInterface engine;
	
	
	@Before
	public void initTest() {
		scriptEngine=new JSR223ScriptEngine();
		engine=mock(EngineInterface.class);
		//scriptEngine.setEngine(mock(EngineInterface.class));
		Map<String,Object> map=new Hashtable<>();
		map.put("name", "nashorn");
		scriptEngine.activate(map);
		scriptEngine.registerObject(ENGINE, engine);
	}
	
	@Test
	public void activateTest() {
		scriptEngine=new JSR223ScriptEngine();
		Map<String,Object> map=new Hashtable<>();
		map.put("name", "nashorn");
		scriptEngine.activate(map);
		map.put("name", "");
	}
	
	@Test
	public void loadScriptTest() {
		scriptEngine.loadScript("simpleCalc", "1+2");
		scriptEngine.loadScript("simpleCalc", "function test(evt) {}; engine.registerEventHandler(\"test\", this, test);");
		scriptEngine.loadScript("simpleCalc", "");
		scriptEngine.loadScript("simpleCalc", null);
	}
	
	@Test
	public void runSimpleScriptTest() throws Exception {
		scriptEngine.loadScript("simpleCalc", "1+2");
		scriptEngine.runScript("simpleCalc");
	}
	
	@Test
	public void runNotLoadedScriptTest() throws Exception {
		scriptEngine.runScript("simpleCalc");
	}
	
	@Test(expected=Exception.class)
	public void unregisteredObjectTest() throws Exception {
		scriptEngine=new JSR223ScriptEngine();
		Map<String,Object> map=new Hashtable<>();
		map.put("name", "nashorn");
		scriptEngine.activate(map);
		scriptEngine.loadScript("registerEventHandler", "function test(evt) { if (ENGINE!=null) ENGINE.toggleMute(); }; engine.registerEventHandler(\"test\", this, test);");
		scriptEngine.runScript("registerEventHandler");
		Event evt=new Event("test", new HashMap<>());
		scriptEngine.handleEvent(evt);
	}	
}
