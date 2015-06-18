/**
 * 
 */
package org.bragi.indexer;

import java.net.URI;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bragi.metadata.MetaDataEnum;
import org.bragi.metadata.MetaDataProviderInterface;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author christoph
 *
 */
public class QueryParserTest {
	
	private class IndexerMock extends AbstractIndexer {

		@Override
		public boolean indexUri(String uri) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void removeUri(String uri) throws Exception {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void closeIndexWriter() throws Exception {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected Map<URI,Map<MetaDataEnum,String>> fetch() {
			Map<URI,Map<MetaDataEnum,String>> retValue=new Hashtable<>();
			Map<MetaDataEnum,String> mData=new Hashtable<>();
			mData.put(MetaDataEnum.ALBUM, "Killswitch Engage");
			mData.put(MetaDataEnum.ARTIST, "Killswitch Engage");
			mData.put(MetaDataEnum.ENCODED_BY, "");
			mData.put(MetaDataEnum.GENRE, "Metalcore");
			mData.put(MetaDataEnum.LANGUAGE, "English");
			mData.put(MetaDataEnum.PUBLISHER, "Roadrunner Records");
			mData.put(MetaDataEnum.RATING, "5.4");
			mData.put(MetaDataEnum.TITLE, "Starting Over");
			mData.put(MetaDataEnum.TRACK_ID, "8");
			mData.put(MetaDataEnum.TRACK_NUMBER, "10");
			mData.put(MetaDataEnum.URL, "");
			retValue.put(URI.create("file:///tmp/test.mp3"), mData);
			return retValue;
		}

		@Override
		public void unsetMetaDataProvider(
				MetaDataProviderInterface pMetaDataProvider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setMetaDataProvider(
				MetaDataProviderInterface pMetaDataProvider) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private AbstractIndexer indexer;
	
	@Before
	public void initTest() {
		indexer=new IndexerMock();
	}
	
	@Test(expected=ParseException.class)
	public void testParseMissingSelectTest() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("bla");
	}
	
	@Test(expected=ParseException.class)
	public void testParseMissingColumnSpecifier1Test() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT");
	}
	
	@Test(expected=ParseException.class)
	public void testParseMissingColumnSpecifier2Test() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT 123");
	}
	
	@Test
	public void testParseSelectAllTest() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT *");
	}
	
	@Test
	public void testParseColumnSpecifierTest() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT ALBUM");
	}
	
	@Test
	public void testParseColumnSpecifier2Test() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT ALBUM, TITLE");
	}
	
	@Test(expected=ParseException.class)
	public void testParsePeriodEnd() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT ALBUM.");
	}
	
	@Test(expected=ParseException.class)
	public void testParseWhereEnd() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT ALBUM WHERE");
	}
	
	@Test(expected=ParseException.class)
	public void testParseColumnNameEnd() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT ALBUM WHERE ENCODED_BY");
	}
	
	@Test(expected=ParseException.class)
	public void testParseUnknownOperator() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT ALBUM WHERE ENCODED_BY != ");
	}
	
	@Test(expected=ParseException.class)
	public void testParseOperatorEnd() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT ALBUM WHERE ENCODED_BY = ");
	}
	
	@Test
	public void testParseIntegerNumberEnd() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT ALBUM WHERE ENCODED_BY = 300");
	}
	
	@Test(expected=ParseException.class)
	public void testParsePeriodEnd2() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT ALBUM WHERE ENCODED_BY = 300.");
	}
	
	@Test
	public void testParseFloatingPointNumberEnd() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT ALBUM WHERE ENCODED_BY = 3.14159");
	}
	
	@Test(expected=ParseException.class)
	public void testParseMinusEnd() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT ALBUM WHERE ENCODED_BY = 2014-");
	}
	
	@Test(expected=ParseException.class)
	public void testParseMinusEnd2() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT ALBUM WHERE ENCODED_BY = 2014-11-");
	}
	
	@Test
	public void testParseDateEnd() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT ALBUM WHERE ENCODED_BY = 2014-11-15");
	}
	
	@Test(expected=ParseException.class)
	public void testParseUnterminatedString1() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT ALBUM WHERE ENCODED_BY ='");
	}
	
	@Test(expected=ParseException.class)
	public void testParseUnterminatedString2() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT ALBUM WHERE ENCODED_BY ='bla");
	}
	
	@Test(expected=ParseException.class)
	public void testParseUnterminatedString3() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT ALBUM WHERE ENCODED_BY ='123454");
	}
	
	@Test(expected=ParseException.class)
	public void testParseUnterminatedString4() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT ALBUM WHERE ENCODED_BY ='1ä2t3U4Ü5äz4");
	}
	
	@Test
	public void testParseEmptyString() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		parser.execute("SELECT ALBUM WHERE ENCODED_BY =''");
	}
	
	@Test
	public void testParseString() throws ParseException {
		QueryParser parser=new QueryParser(indexer);
		Map<URI,Map<MetaDataEnum,String>> result=parser.execute("SELECT ALBUM WHERE GENRE ='Metalcore'");
		double blub=Double.parseDouble("3.14159");
	}
}
