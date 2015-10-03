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

import java.net.URISyntaxException;

import org.bragi.engine.vlc.internal.URIParser;
import org.junit.Assert;
import org.junit.Test;

import uk.co.caprica.vlcj.mrl.CdMrl;
import uk.co.caprica.vlcj.mrl.FileMrl;
import uk.co.caprica.vlcj.mrl.FtpMrl;
import uk.co.caprica.vlcj.mrl.HttpMrl;
import uk.co.caprica.vlcj.mrl.Mrl;


/**
 * @author christoph
 *
 */
public class URIParserTest {

	@Test(expected=NullPointerException.class)
	public void testParseFTPUrl() throws URISyntaxException {
		Mrl mrl=URIParser.getMrl("ftp://localhost:9765/test/test/test.wmv");
		Assert.assertNotNull(mrl);
		Assert.assertTrue(FtpMrl.class.isAssignableFrom(mrl.getClass()));
		Assert.assertNotNull(mrl.value());
		mrl = URIParser.getMrl("bla");
	}
	
	@Test(expected=NullPointerException.class)
	public void testParseHTTPUrl() throws URISyntaxException {
		Mrl mrl=URIParser.getMrl("http://localhost:9765/test/test/test.wmv");
		Assert.assertNotNull(mrl);
		Assert.assertTrue(HttpMrl.class.isAssignableFrom(mrl.getClass()));
		Assert.assertNotNull(mrl.value());
		mrl = URIParser.getMrl("bla");
	}
	
	@Test(expected=NullPointerException.class)
	public void testParseFile() throws URISyntaxException {
		Mrl mrl=URIParser.getMrl("file:///home/test/test/test.xml");
		Assert.assertNotNull(mrl);
		Assert.assertTrue(FileMrl.class.isAssignableFrom(mrl.getClass()));
		Assert.assertNotNull(mrl.value());
		mrl = URIParser.getMrl("bla");
	}
	
	@Test(expected=NullPointerException.class)
	public void testParseCDDAUrl() throws URISyntaxException {
		Mrl mrl=URIParser.getMrl("cdda:///dev/cdrom/@3");
		Assert.assertNotNull(mrl);
		Assert.assertTrue(CdMrl.class.isAssignableFrom(mrl.getClass()));
		Assert.assertNotNull(mrl.value());
		mrl=URIParser.getMrl("cdda:///dev/cdrom");
		Assert.assertNotNull(mrl);
		Assert.assertTrue(CdMrl.class.isAssignableFrom(mrl.getClass()));
		Assert.assertNotNull(mrl.value());
		mrl = URIParser.getMrl("bla");
	}
}
