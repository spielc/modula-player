/**
 * 
 */
package org.bragi.query.impl;

import java.net.URI;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.bragi.metadata.MetaDataEnum;
import org.bragi.query.ParseException;
import org.bragi.query.QueryResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author christoph
 *
 */
public class QueryParserTest {
	
	private Map<URI, Map<MetaDataEnum,String>> metaData;
	
	@Before
	public void initTest() {
		metaData=new Hashtable<>();
		Map<MetaDataEnum,String> metaData=new Hashtable<>();
		metaData.put(MetaDataEnum.ALBUM, "Killswitch Engage");
		metaData.put(MetaDataEnum.ARTIST, "Killswitch Engage");
		metaData.put(MetaDataEnum.ENCODED_BY, "k3b");
		metaData.put(MetaDataEnum.GENRE, "Metalcore");
		metaData.put(MetaDataEnum.LANGUAGE, "English");
		metaData.put(MetaDataEnum.PUBLISHER, "Roadrunner REcords");
		metaData.put(MetaDataEnum.RATING, "3.14159");
		metaData.put(MetaDataEnum.TITLE, "The Forgotten");
		metaData.put(MetaDataEnum.TRACK_ID, "3");
		metaData.put(MetaDataEnum.TRACK_NUMBER, "3");
		metaData.put(MetaDataEnum.URL, "");
		this.metaData.put(URI.create("file:///tmp/test1.mp3"), metaData);
		metaData=new Hashtable<>();
		metaData.put(MetaDataEnum.ALBUM, "Killswitch Engage");
		metaData.put(MetaDataEnum.ARTIST, "Killswitch Engage");
		metaData.put(MetaDataEnum.ENCODED_BY, "k3b");
		metaData.put(MetaDataEnum.GENRE, "Metalcore");
		metaData.put(MetaDataEnum.LANGUAGE, "English");
		metaData.put(MetaDataEnum.PUBLISHER, "Roadrunner REcords");
		metaData.put(MetaDataEnum.RATING, "3.14159");
		metaData.put(MetaDataEnum.TITLE, "The Forgotten");
		metaData.put(MetaDataEnum.TRACK_ID, "3");
		metaData.put(MetaDataEnum.TRACK_NUMBER, "3");
		metaData.put(MetaDataEnum.URL, "");
		this.metaData.put(URI.create("file:///tmp/test2.mp3"), metaData);
		metaData=new Hashtable<>();
		metaData.put(MetaDataEnum.ALBUM, "Helvetios");
		metaData.put(MetaDataEnum.ARTIST, "Eluveitie");
		metaData.put(MetaDataEnum.ENCODED_BY, "k3b");
		metaData.put(MetaDataEnum.GENRE, "Folkmetal");
		metaData.put(MetaDataEnum.LANGUAGE, "Gaulish");
		metaData.put(MetaDataEnum.PUBLISHER, "Metalblade");
		metaData.put(MetaDataEnum.RATING, "7.9");
		metaData.put(MetaDataEnum.TITLE, "A Rose for Epona");
		metaData.put(MetaDataEnum.TRACK_ID, "9");
		metaData.put(MetaDataEnum.TRACK_NUMBER, "9");
		metaData.put(MetaDataEnum.URL, "");
		this.metaData.put(URI.create("file:///tmp/test3.mp3"), metaData);
		
	}
	
	@Test(expected=ParseException.class)
	public void testParseMissingSelectTest() throws ParseException {
		QueryParser parser=new QueryParser();
		parser.execute("bla", metaData);
	}
	
	@Test(expected=ParseException.class)
	public void testParseMissingColumnSpecifier1Test() throws ParseException {
		QueryParser parser=new QueryParser();
		parser.execute("SELECT", metaData);
	}
	
	@Test(expected=ParseException.class)
	public void testParseMissingColumnSpecifier2Test() throws ParseException {
		QueryParser parser=new QueryParser();
		parser.execute("SELECT 123", metaData);
	}
	
	@Test
	public void testParseSelectAllOrderByAscendingTest() throws ParseException {
		QueryParser parser=new QueryParser();
		List<QueryResult> result=parser.execute("SELECT * ORDER BY ARTIST ASC", metaData);
		Assert.assertEquals(3, result.size());
		Assert.assertEquals("Eluveitie", result.get(0).getMetaData().get(MetaDataEnum.ARTIST));
	}
	
	@Test
	public void testParseSelectAllOrderByDescendingTest() throws ParseException {
		QueryParser parser=new QueryParser();
		List<QueryResult> result=parser.execute("SELECT * ORDER BY ARTIST DESC", metaData);
		Assert.assertEquals(3, result.size());
		Assert.assertEquals("Eluveitie", result.get(2).getMetaData().get(MetaDataEnum.ARTIST));
	}
	
	@Test
	public void testParseSelectAllTest() throws ParseException {
		QueryParser parser=new QueryParser();
		List<QueryResult> result=parser.execute("SELECT *", metaData);
		Assert.assertEquals(3, result.size());
		result.forEach(res->Assert.assertEquals(EnumSet.allOf(MetaDataEnum.class).size(), res.getMetaData().size()));
	}
	
	@Test
	public void testParseColumnSpecifierTest() throws ParseException {
		QueryParser parser=new QueryParser();
		List<QueryResult> result=parser.execute("SELECT ALBUM", metaData);
		Assert.assertEquals(3, result.size());
		result.forEach(res->Assert.assertEquals(1, res.getMetaData().size()));
	}
	
	@Test
	public void testParseColumnSpecifier2Test() throws ParseException {
		QueryParser parser=new QueryParser();
		List<QueryResult> result=parser.execute("SELECT ALBUM, TITLE", metaData);
		Assert.assertEquals(3, result.size());
		result.forEach(res->Assert.assertEquals(2, res.getMetaData().size()));
	}
	
	@Test(expected=ParseException.class)
	public void testParsePeriodEnd() throws ParseException {
		QueryParser parser=new QueryParser();
		parser.execute("SELECT ALBUM.", metaData);
	}
	
	@Test(expected=ParseException.class)
	public void testParseWhereEnd() throws ParseException {
		QueryParser parser=new QueryParser();
		parser.execute("SELECT ALBUM WHERE", metaData);
	}
	
	@Test(expected=ParseException.class)
	public void testParseColumnNameEnd() throws ParseException {
		QueryParser parser=new QueryParser();
		parser.execute("SELECT ALBUM WHERE ENCODED_BY", metaData);
	}
	
	@Test(expected=ParseException.class)
	public void testParseUnknownOperator() throws ParseException {
		QueryParser parser=new QueryParser();
		parser.execute("SELECT ALBUM WHERE ENCODED_BY != ", metaData);
	}
	
	@Test(expected=ParseException.class)
	public void testParseOperatorEnd() throws ParseException {
		QueryParser parser=new QueryParser();
		parser.execute("SELECT ALBUM WHERE ENCODED_BY = ", metaData);
	}
	
	@Test
	public void testParseNegativeIntegerNumberEnd() throws ParseException {
		QueryParser parser=new QueryParser();
		List<QueryResult> result=parser.execute("SELECT ALBUM WHERE TRACK_ID = -300", metaData);
		Assert.assertTrue(result.isEmpty());
	}
	
	@Test
	public void testParseIntegerNumberEnd() throws ParseException {
		QueryParser parser=new QueryParser();
		List<QueryResult> result=parser.execute("SELECT ALBUM WHERE TRACK_ID = 300", metaData);
		Assert.assertTrue(result.isEmpty());
	}
	
	@Test(expected=ParseException.class)
	public void testParsePeriodEnd2() throws ParseException {
		QueryParser parser=new QueryParser();
		parser.execute("SELECT ALBUM WHERE ENCODED_BY = 300.", metaData);
	}
	
	@Test
	public void testParseFloatingPointNumberEnd() throws ParseException {
		QueryParser parser=new QueryParser();
		List<QueryResult> result=parser.execute("SELECT ALBUM WHERE RATING = 3.14159", metaData);
		Assert.assertEquals(2, result.size());
		result.forEach(res->Assert.assertEquals(2, res.getMetaData().size()));
	}
	
	@Test
	public void testParseNegativeFloatingPointNumberEnd() throws ParseException {
		QueryParser parser=new QueryParser();
		List<QueryResult> result=parser.execute("SELECT ALBUM WHERE RATING = -3.14159", metaData);
	}
		
	@Test(expected=ParseException.class)
	public void testParseMinusEnd() throws ParseException {
		QueryParser parser=new QueryParser();
		parser.execute("SELECT ALBUM WHERE ENCODED_BY = 2014-", metaData);
	}
	
	@Test(expected=ParseException.class)
	public void testParseMinusEnd2() throws ParseException {
		QueryParser parser=new QueryParser();
		parser.execute("SELECT ALBUM WHERE ENCODED_BY = 2014-11-", metaData);
	}
	
	@Test
	public void testParseDateEnd() throws ParseException {
		QueryParser parser=new QueryParser();
		List<QueryResult> result=parser.execute("SELECT ALBUM WHERE ENCODED_BY = 2014-11-15", metaData);
		Assert.assertTrue(result.isEmpty());
	}
	
	@Test(expected=ParseException.class)
	public void testParseUnterminatedString1() throws ParseException {
		QueryParser parser=new QueryParser();
		parser.execute("SELECT ALBUM WHERE ENCODED_BY ='", metaData);
	}
	
	@Test(expected=ParseException.class)
	public void testParseUnterminatedString2() throws ParseException {
		QueryParser parser=new QueryParser();
		parser.execute("SELECT ALBUM WHERE ENCODED_BY ='bla", metaData);
	}
	
	@Test(expected=ParseException.class)
	public void testParseUnterminatedString3() throws ParseException {
		QueryParser parser=new QueryParser();
		parser.execute("SELECT ALBUM WHERE ENCODED_BY ='123454", metaData);
	}
	
	@Test(expected=ParseException.class)
	public void testParseUnterminatedString4() throws ParseException {
		QueryParser parser=new QueryParser();
		parser.execute("SELECT ALBUM WHERE ENCODED_BY ='1ä2t3U4Ü5äz4", metaData);
	}
	
	@Test
	public void testParseEmptyString() throws ParseException {
		QueryParser parser=new QueryParser();
		List<QueryResult> result=parser.execute("SELECT ALBUM WHERE ENCODED_BY =\"\"", metaData);
		Assert.assertTrue(result.isEmpty());
	}
	
	@Test
	public void testParseString() throws ParseException {
		QueryParser parser=new QueryParser();
		List<QueryResult> result=parser.execute("SELECT ALBUM WHERE GENRE =\"Metalcore\"", metaData);
		Assert.assertEquals(2, result.size());
		result.forEach(res->Assert.assertEquals(2, res.getMetaData().size()));
	}
	
	@Test
	public void testParseString2() throws ParseException {
		QueryParser parser=new QueryParser();
		List<QueryResult> result=parser.execute("SELECT ALBUM, TITLE, ARTIST WHERE GENRE =\"Metalcore\"", metaData);
		Assert.assertEquals(2, result.size());
		result.forEach(res->Assert.assertEquals(4, res.getMetaData().size()));
	}
	
	@Test
	public void testParseAndQuery() throws ParseException {
		QueryParser parser=new QueryParser();
		List<QueryResult> result=parser.execute("SELECT ALBUM, TITLE, ARTIST WHERE GENRE =\"Metalcore\" AND TRACK_ID=9", metaData);
		Assert.assertTrue(result.isEmpty());
	}
	
	@Test
	public void testParseAndQuery2() throws ParseException {
		QueryParser parser=new QueryParser();
		List<QueryResult> result=parser.execute("SELECT ALBUM, TITLE, ARTIST WHERE ARTIST =\"Eluveitie\" AND TRACK_ID=9", metaData);
		Assert.assertEquals(1, result.size());
	}
	
	@Test
	public void testParseOrQuery() throws ParseException {
		QueryParser parser=new QueryParser();
		List<QueryResult> result=parser.execute("SELECT ALBUM, TITLE, ARTIST WHERE GENRE =\"bla\" OR TRACK_ID=123456789", metaData);
		Assert.assertTrue(result.isEmpty());
	}
	
	@Test
	public void testParseOrQuery2() throws ParseException {
		QueryParser parser=new QueryParser();
		List<QueryResult> result=parser.execute("SELECT ALBUM, TITLE, ARTIST WHERE ARTIST =\"Eluveitie\" OR ARTIST=\"Killswitch Engage\"", metaData);
		Assert.assertEquals(3, result.size());
	}
}
