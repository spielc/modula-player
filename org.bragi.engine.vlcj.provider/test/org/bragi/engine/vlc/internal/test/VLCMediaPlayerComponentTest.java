/**
 * 
 */
package org.bragi.engine.vlc.internal.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.bragi.engine.EngineInterface;
import org.bragi.engine.vlc.internal.VLCMediaPlayerComponent;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import uk.co.caprica.vlcj.medialist.MediaList;
import uk.co.caprica.vlcj.mrl.FileMrl;
import uk.co.caprica.vlcj.player.list.MediaListPlayer;

/**
 * @author christoph
 *
 */
public class VLCMediaPlayerComponentTest {
	
	public static final String MP3URL=new FileMrl().file(VLCMediaPlayerComponentTest.class.getClassLoader().getResource("test.mp3").getPath()).value();
	
	private VLCMediaPlayerComponent component;
	private MediaList playList;
	private MediaListPlayer player;
	private EventAdmin admin;
	
	@Before
	public void initTest() {
		component=new VLCMediaPlayerComponent();
		player=component.getMediaListPlayer();
		playList=component.getMediaList();
		playList.addMedia(MP3URL);
		admin=mock(EventAdmin.class);
		component.setEventAdmin(admin);
	}
	
	@Test
	public void testFinished() {
		try {
			player.play();
			while (player.isPlaying())
				Thread.sleep(1000);
			verify(admin,times(1)).postEvent(new Event(EngineInterface.FINISHED_EVENT,(Map<String,Object>)null));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
