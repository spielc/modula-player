/**
 * 
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
