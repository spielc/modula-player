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
package org.bragi.jaudiotagger.test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.EnumSet;

import javax.imageio.ImageIO;

import junit.framework.Assert;

import org.bragi.jaudiotagger.JaudiotaggerMetaDataProvider;
import org.bragi.metadata.MetaDataEnum;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author christoph
 *
 */
public class JaudiotaggerMetaDataProviderTest {
	
	public static String OGGURL;
	public static String TAGLESSMP3URL;
	public static String MP3URL;
	public static String FLACURL;
	public static String ARTWORKURL1;
	public static String ARTWORKURL2;
	
	@BeforeClass
	public static void initTest() {
		try {
			OGGURL=JaudiotaggerMetaDataProviderTest.class.getClassLoader().getResource("test.ogg").toURI().toString();
			TAGLESSMP3URL=JaudiotaggerMetaDataProviderTest.class.getClassLoader().getResource("test_invalid.mp3").toURI().toString();
			MP3URL=JaudiotaggerMetaDataProviderTest.class.getClassLoader().getResource("test.mp3").toURI().toString();
			FLACURL=JaudiotaggerMetaDataProviderTest.class.getClassLoader().getResource("test.flac").toURI().toString();
			ARTWORKURL1=JaudiotaggerMetaDataProviderTest.class.getClassLoader().getResource("coverart_im.ogg").toURI().toString();
			ARTWORKURL2=JaudiotaggerMetaDataProviderTest.class.getClassLoader().getResource("coverart_mk.ogg").toURI().toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getMetaDataTest() throws URISyntaxException {
		JaudiotaggerMetaDataProvider metaDataProvider=new JaudiotaggerMetaDataProvider();
		EnumSet<MetaDataEnum> interestingMetaData=EnumSet.allOf(MetaDataEnum.class);
		String[] metaData=metaDataProvider.getMetaData(OGGURL, interestingMetaData);
		Assert.assertEquals(interestingMetaData.size(), metaData.length);
		metaData=metaDataProvider.getMetaData(TAGLESSMP3URL, interestingMetaData);
		Assert.assertEquals(0, metaData.length);
		metaData=metaDataProvider.getMetaData(MP3URL, interestingMetaData);
		Assert.assertEquals(interestingMetaData.size(), metaData.length);
		metaData=metaDataProvider.getMetaData(FLACURL, interestingMetaData);
		Assert.assertEquals(interestingMetaData.size(), metaData.length);
	}
	
	@Test
	public void getArtworkTest() throws URISyntaxException, IOException {
		JaudiotaggerMetaDataProvider metaDataProvider=new JaudiotaggerMetaDataProvider();
		byte[] artwork=metaDataProvider.getArtwork(ARTWORKURL1);
		Assert.assertTrue(artwork.length>0);
		ByteArrayInputStream byteStream=new ByteArrayInputStream(artwork);
		BufferedImage coverArt=ImageIO.read(byteStream);
		Assert.assertNotNull(coverArt);
		Assert.assertTrue(coverArt.getHeight()>0 && coverArt.getWidth()>0);
		artwork=metaDataProvider.getArtwork(ARTWORKURL2);
		Assert.assertTrue(artwork.length>0);
		byteStream=new ByteArrayInputStream(artwork);
		coverArt=ImageIO.read(byteStream);
		Assert.assertNotNull(coverArt);
		Assert.assertTrue(coverArt.getHeight()>0 && coverArt.getWidth()>0);
		artwork=metaDataProvider.getArtwork(MP3URL);
		Assert.assertTrue(artwork.length==0);
		artwork=metaDataProvider.getArtwork(TAGLESSMP3URL);
		Assert.assertTrue(artwork.length==0);
	}
	
	@Test
	public void getDateTest() throws URISyntaxException {
		JaudiotaggerMetaDataProvider metaDataProvider=new JaudiotaggerMetaDataProvider();
		Date d=metaDataProvider.getDate(MP3URL);
		Assert.assertNotNull(d);
		d=metaDataProvider.getDate(FLACURL);
		Assert.assertNull(d);
		d=metaDataProvider.getDate(OGGURL);
		Assert.assertNotNull(d);
		d=metaDataProvider.getDate(TAGLESSMP3URL);
		Assert.assertNull(d);
	}
}
