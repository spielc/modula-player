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
package org.bragi.engine.vlc.internal.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.bragi.engine.EngineInterface;
import org.bragi.engine.vlc.internal.VLCMediaPlayerComponent;
import org.bragi.engine.vlc.test.helpers.LocalTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import uk.co.caprica.vlcj.medialist.MediaList;
import uk.co.caprica.vlcj.mrl.FileMrl;
import uk.co.caprica.vlcj.player.list.MediaListPlayer;

/**
 * @author christoph
 *
 */
@Category({LocalTest.class})
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
