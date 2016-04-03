/**
 * 
 */
package org.modulaplayer.script.jsr223.provider.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

import org.bragi.engine.EngineInterface;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.modulaplayer.script.jsr223.provider.JSR223ScriptEngine;
import org.modulaplayer.script.jsr223.provider.ScriptEngineTest;
import org.modulaplayer.script.jsr223.provider.util.LocalTest;
import org.osgi.service.event.Event;

/**
 * @author christoph
 *
 */
@Category({LocalTest.class})
public class LocalScriptEngineTest {
	
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
	public void eventHandlingTest2() throws Exception {
		scriptEngine.loadScript("registerEventHandler", "function test(evt) { if (ENGINE!=null) { ENGINE.toggleMute(); } }; engine.registerEventHandler(\"org/bragi/playlist/event/INSERTED\", this, test);");
		scriptEngine.runScript("registerEventHandler");
		Event evt=new Event("org/bragi/playlist/event/ADDED", new HashMap<>());
		Random random=new Random(System.currentTimeMillis());
		int count=random.nextInt(10);
		int i;
		for (i=0;i<=count;i++)
			scriptEngine.handleEvent(evt);
		verify(engine,times(0)).toggleMute();
	}

	@Test
	public void wildcardEventHandlingTest() throws Exception {
		scriptEngine.loadScript("registerEventHandler", "function test(evt) { if (ENGINE!=null) { ENGINE.toggleMute(); } }; engine.registerEventHandler(\"org/bragi/engine/event/*\", this, test);");
		scriptEngine.runScript("registerEventHandler");
		Event evt=new Event("org/bragi/engine/event/PAUSING", new HashMap<>());
		Random random=new Random(System.currentTimeMillis());
		int count=random.nextInt(10);
		int i;
		for (i=0;i<=count;i++)
			scriptEngine.handleEvent(evt);
		verify(engine,times(i)).toggleMute();
	}
	
	@Test
	public void wildcardEventHandlingTest2() throws Exception {
		scriptEngine.loadScript("registerEventHandler", "function test(evt) { if (ENGINE!=null) { ENGINE.toggleMute(); } }; engine.registerEventHandler(\"org/bragi/engine/event/*\", this, test); this;");
		scriptEngine.runScript("registerEventHandler");
		Event evt=new Event("org/bragi/playlist/event/ADDED", new HashMap<>());
		Random random=new Random(System.currentTimeMillis());
		int count=random.nextInt(10);
		int i;
		for (i=0;i<=count;i++)
			scriptEngine.handleEvent(evt);
		verify(engine,times(0)).toggleMute();
	}
	
	@Test
	public void stopScriptTest2() throws Exception {
		try (InputStream stream = ScriptEngineTest.class.getClassLoader().getResource("class.js").openStream()) {
			StringBuffer script=new StringBuffer();
			int count=stream.available();
			while(count>0) {
				byte[] data=new byte[count];
				stream.read(data, 0, count);
				script.append(new String(data));
				count=stream.available();
			}
			scriptEngine.loadScript("registerEventHandler", script.toString());
			scriptEngine.runScript("registerEventHandler");
			Event evt=new Event("org/bragi/engine/event/PAUSING", new HashMap<>());
			Random random=new Random(System.currentTimeMillis());
			count=random.nextInt(10);
			int i;
			for (i=0;i<=count;i++)
				scriptEngine.handleEvent(evt);
			verify(engine,times(i)).toggleMute();
			scriptEngine.stopScript("registerEventHandler");
			for (int j=0;j<=count;j++)
				scriptEngine.handleEvent(evt);
			verify(engine,times(i)).toggleMute();
		}
	}
	
	@Test
	public void runAdvancedScriptTest() throws Exception {
		scriptEngine.loadScript("registerEventHandler", "function test(evt) { if (ENGINE!=null) { ENGINE.toggleMute(); } }; engine.registerEventHandler(\"test\", this, test); this;");
		scriptEngine.runScript("registerEventHandler");
	}
	
	@Test
	public void eventHandlingTest() throws Exception {
		scriptEngine.loadScript("registerEventHandler", "function test(evt) { if (ENGINE!=null) ENGINE.toggleMute(); }; engine.registerEventHandler(\"test\", this, test);");
		scriptEngine.runScript("registerEventHandler");
		Event evt=new Event("test", new HashMap<>());
		Random random=new Random(System.currentTimeMillis());
		int count=random.nextInt(10);
		int i;
		for (i=0;i<=count;i++)
			scriptEngine.handleEvent(evt);
		verify(engine,times(i)).toggleMute();
	}
	
	@Test
	public void stopScriptTest() throws Exception {
		scriptEngine.loadScript("registerEventHandler", "function test(evt) { if (ENGINE!=null) { ENGINE.toggleMute(); } }; engine.registerEventHandler(\"org/bragi/engine/event/*\", this, test); this;");
		scriptEngine.runScript("registerEventHandler");
		Event evt=new Event("org/bragi/engine/event/PAUSING", new HashMap<>());
		Random random=new Random(System.currentTimeMillis());
		int count=random.nextInt(10);
		int i;
		for (i=0;i<=count;i++)
			scriptEngine.handleEvent(evt);
		verify(engine,times(i)).toggleMute();
		scriptEngine.stopScript("registerEventHandler");
		for (int j=0;j<=count;j++)
			scriptEngine.handleEvent(evt);
		verify(engine,times(i)).toggleMute();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void registerNullObjectEventHandlerTest() throws Exception {
		scriptEngine.loadScript("registerEventHandler", "function test(evt) { if (ENGINE!=null) { ENGINE.toggleMute(); } }; engine.registerEventHandler(\"test\", null, test);");
		scriptEngine.runScript("registerEventHandler");
	}
}
