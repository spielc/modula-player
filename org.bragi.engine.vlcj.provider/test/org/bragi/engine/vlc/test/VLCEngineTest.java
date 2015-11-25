/**
 * 
 */
package org.bragi.engine.vlc.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.bragi.engine.EngineInterface;
import org.bragi.engine.vlc.VLCEngine;
import org.bragi.engine.vlc.test.helpers.LocalTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import uk.co.caprica.vlcj.mrl.FileMrl;

/**
 * @author christoph
 *
 */
@Category({LocalTest.class})
public class VLCEngineTest {
	
	public static final String MP3URL=new FileMrl().file(VLCEngineTest.class.getClassLoader().getResource("test.mp3").getPath()).value();
	
	private VLCEngine engine;
	private EventAdmin admin;
	
	@Before
	public void initTest() {
		engine=new VLCEngine();
		admin=mock(EventAdmin.class);
		engine.setEventAdmin(admin);
		engine.activate(new Hashtable<String, Object>());
		engine.setVolume(0);
	}
	
	@Test
	public void testPlay() {
		try {
			engine.play(MP3URL);
			Thread.sleep(1000);
			verify(admin,times(1)).postEvent(new Event(EngineInterface.PLAY_EVENT,(Map<String,Object>)null));
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
	
	@Test
	public void testPause() {
		try {
			engine.play(MP3URL);
			Thread.sleep(500);
			engine.pause();
			Thread.sleep(1000);
			verify(admin,times(1)).postEvent(new Event(EngineInterface.PAUSE_EVENT,(Map<String,Object>)null));
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
	
	@Test
	public void testStop() {
		try {
			engine.play(MP3URL);
			Thread.sleep(500);
			engine.stop();
			Thread.sleep(1000);
			verify(admin,times(1)).postEvent(new Event(EngineInterface.STOP_EVENT,(Map<String,Object>)null));
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
	
	@Test
	public void testForward() {
		try {
			engine.play(MP3URL);
			Thread.sleep(500);
			engine.forward();
			Thread.sleep(1000);
			Map<String,Object> eventProperties=new HashMap<>();
			verify(admin,times(1)).postEvent(new Event(EngineInterface.FORWARD_EVENT,eventProperties));
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
	
	@Test
	public void testBackward() {
		try {
			engine.play(MP3URL);
			Thread.sleep(500);
			engine.backward();
			Thread.sleep(500);
			HashMap<String, Object> eventProperties = new HashMap<>();
			verify(admin,times(1)).postEvent(new Event(EngineInterface.BACKWARD_EVENT,eventProperties));
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	@Test
	public void testSeek() {
		// TODO implement this test
	}
}
