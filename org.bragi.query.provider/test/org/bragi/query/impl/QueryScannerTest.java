/**
 * 
 */
package org.bragi.query.impl;

import org.bragi.query.QueryKeywords;
import org.bragi.query.Token;
import org.bragi.query.TokenType;
import org.bragi.query.impl.QueryScanner;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author christoph
 *
 */
public class QueryScannerTest {
	
	@Test
	public void scanSelectTest() {
		QueryScanner scanner=new QueryScanner(QueryKeywords.SELECT);
		Token token=scanner.scan();
		Assert.assertEquals(TokenType.SELECT, token.getType());
		token=scanner.scan();
		Assert.assertEquals(TokenType.NONE, token.getType());
	}
	
	@Test
	public void scanAllSelectorTest() {
		QueryScanner scanner=new QueryScanner("*");
		Token token=scanner.scan();
		Assert.assertEquals(TokenType.ALL_SELECTOR, token.getType());
		token=scanner.scan();
		Assert.assertEquals(TokenType.NONE, token.getType());
	}
	
	@Test
	public void scanWhereTest() {
		QueryScanner scanner=new QueryScanner(QueryKeywords.WHERE);
		Token token=scanner.scan();
		Assert.assertEquals(TokenType.WHERE, token.getType());
		token=scanner.scan();
		Assert.assertEquals(TokenType.NONE, token.getType());
	}
	
	@Test
	public void scanOrTest() {
		QueryScanner scanner=new QueryScanner(QueryKeywords.OR);
		Token token=scanner.scan();
		Assert.assertEquals(TokenType.OR, token.getType());
		token=scanner.scan();
		Assert.assertEquals(TokenType.NONE, token.getType());
	}
	
	@Test
	public void scanAndTest() {
		QueryScanner scanner=new QueryScanner(QueryKeywords.AND);
		Token token=scanner.scan();
		Assert.assertEquals(TokenType.AND, token.getType());
		token=scanner.scan();
		Assert.assertEquals(TokenType.NONE, token.getType());
	}
	
	@Test
	public void scanOrderByTest() {
		QueryScanner scanner=new QueryScanner("ORDER BY");
		Token token=scanner.scan();
		Assert.assertEquals(TokenType.ORDER, token.getType());
		token=scanner.scan();
		Assert.assertEquals(TokenType.BY, token.getType());
		token=scanner.scan();
		Assert.assertEquals(TokenType.NONE, token.getType());
	}
	
	@Test
	public void scanOperatorTest() {
		for (String operatorString : "<:<=:=:>=:>:<>".split(":")) {
			QueryScanner scanner=new QueryScanner(operatorString);
			Token token=scanner.scan();
			Assert.assertEquals(TokenType.OPERATOR, token.getType());
			token=scanner.scan();
			Assert.assertEquals(TokenType.NONE, token.getType());
		}
	}
	
	@Test
	public void scanOrderDirectionAscTest() {
		QueryScanner scanner=new QueryScanner(QueryKeywords.ORDER_DIRECTION_ASC);
		Token token=scanner.scan();
		Assert.assertEquals(TokenType.ORDER_DIRECTION, token.getType());
		token=scanner.scan();
		Assert.assertEquals(TokenType.NONE, token.getType());
	}
	
	@Test
	public void scanOrderDirectionDescTest() {
		QueryScanner scanner=new QueryScanner(QueryKeywords.ORDER_DIRECTION_DESC);
		Token token=scanner.scan();
		Assert.assertEquals(TokenType.ORDER_DIRECTION, token.getType());
		token=scanner.scan();
		Assert.assertEquals(TokenType.NONE, token.getType());
	}
	
	@Test
	public void scanMinusTest() {
		QueryScanner scanner=new QueryScanner("-");
		Token token=scanner.scan();
		Assert.assertEquals(TokenType.MINUS, token.getType());
		token=scanner.scan();
		Assert.assertEquals(TokenType.NONE, token.getType());
	}
	
	@Test
	public void scanNameTest() {
		QueryScanner scanner=new QueryScanner("bla");
		Token token=scanner.scan();
		Assert.assertEquals(TokenType.NAME, token.getType());
		String[] keywords={QueryKeywords.SELECT, QueryKeywords.FROM, QueryKeywords.WHERE, QueryKeywords.ORDER_DIRECTION_ASC, QueryKeywords.ORDER_DIRECTION_DESC, QueryKeywords.AND, QueryKeywords.OR};
		for (String keyword : keywords) {
			scanner=new QueryScanner(keyword.substring(0, keyword.length()-1));
			token=scanner.scan();
			Assert.assertEquals(TokenType.NAME, token.getType());
		}
		
		scanner=new QueryScanner("test123");
		token=scanner.scan();
		Assert.assertEquals(TokenType.NAME, token.getType());
		scanner=new QueryScanner("öäü");
		token=scanner.scan();
		Assert.assertEquals(TokenType.NAME, token.getType());
		scanner=new QueryScanner("ÄÖÜ");
		token=scanner.scan();
		Assert.assertEquals(TokenType.NAME, token.getType());
	}
	
	@Test
	public void scanStringTest() {
		QueryScanner scanner=new QueryScanner("\"Killswitch Engage\"");
		Token token=scanner.scan();
		Assert.assertEquals(TokenType.STRING, token.getType());
		scanner=new QueryScanner("\"^!§$%&/()=?\f+#-.,<>|;:_'*\rabcdefghijklmnopqrstuvwxyzöäü\tABCDEFGHIJKLMNOPQRSTUVWXYZÖÄÜ\n1234567890\"");
		token=scanner.scan();
		Assert.assertEquals(TokenType.STRING, token.getType());
	}
	
	@Test
	public void scanNumberTest() {
		//valid values
		String intValue = "123";
		QueryScanner scanner=new QueryScanner(intValue);
		Token token=scanner.scan();
		Assert.assertEquals(TokenType.NUMBER, token.getType());
		Assert.assertEquals(intValue, token.getValue());
//		token=scanner.scan();
//		Assert.assertEquals(TokenType.NONE, token.getType());
//		String pi = "3.14159";
//		scanner=new QueryScanner(pi);
//		token=scanner.scan();
//		Assert.assertEquals(TokenType.VALUE, token.getType());
//		Assert.assertEquals(pi, token.getValue());
//		token=scanner.scan();
//		Assert.assertEquals(TokenType.NONE, token.getType());
//		String piTimes100 = "314.159";
//		scanner=new QueryScanner(piTimes100);
//		token=scanner.scan();
//		Assert.assertEquals(TokenType.VALUE, token.getType());
//		Assert.assertEquals(piTimes100, token.getValue());
//		token=scanner.scan();
//		Assert.assertEquals(TokenType.NONE, token.getType());
//		String helloWorld = "'hello,world!'";
//		scanner=new QueryScanner(helloWorld);
//		token=scanner.scan();
//		Assert.assertEquals(TokenType.VALUE, token.getType());
//		Assert.assertEquals(helloWorld, token.getValue());
//		token=scanner.scan();
//		Assert.assertEquals(TokenType.NONE, token.getType());
//		String helloWorldWithTab = "'hello,\tworld!'";
//		scanner=new QueryScanner(helloWorldWithTab);
//		token=scanner.scan();
//		Assert.assertEquals(TokenType.VALUE, token.getType());
//		Assert.assertEquals(helloWorldWithTab, token.getValue());
//		token=scanner.scan();
//		Assert.assertEquals(TokenType.NONE, token.getType());
//		scanner=new QueryScanner("''");
//		token=scanner.scan();
//		Assert.assertEquals(TokenType.VALUE, token.getType());
//		Assert.assertEquals("''", token.getValue());
//		token=scanner.scan();
//		Assert.assertEquals(TokenType.NONE, token.getType());
//		String date="1980-11-15";
//		scanner=new QueryScanner(date);
//		token=scanner.scan();
//		Assert.assertEquals(TokenType.VALUE, token.getType());
//		Assert.assertEquals(date, token.getValue());
//		token=scanner.scan();
//		Assert.assertEquals(TokenType.NONE, token.getType());
		//invalid values
	}
	
	@Test
	public void scanCommaTest() {
		QueryScanner scanner=new QueryScanner(",");
		Token token=scanner.scan();
		Assert.assertEquals(TokenType.COMMA, token.getType());
		token=scanner.scan();
		Assert.assertEquals(TokenType.NONE, token.getType());
	}
	
	@Test
	public void scanSimpleQueryTest() {
		QueryScanner scanner=new QueryScanner("SELECT *");
		Token token=scanner.scan();
		Assert.assertEquals(TokenType.SELECT, token.getType());
		token=scanner.scan();
		Assert.assertEquals(TokenType.ALL_SELECTOR, token.getType());
		token=scanner.scan();
		Assert.assertEquals(TokenType.NONE, token.getType());
	}
	
	@Test
	public void scanQueryMultipleColumnsTest() {
		QueryScanner scanner=new QueryScanner("SELECT ALBUM,TITLE,ARTIST");
		Token token=scanner.scan();
		Assert.assertEquals(TokenType.SELECT, token.getType());
		token=scanner.scan();
		Assert.assertEquals(TokenType.COLUMN_NAME, token.getType());
		Assert.assertEquals("ALBUM", token.getValue());
		token=scanner.scan();
		Assert.assertEquals(TokenType.COMMA, token.getType());
		token=scanner.scan();
		Assert.assertEquals(TokenType.COLUMN_NAME, token.getType());
		Assert.assertEquals("TITLE", token.getValue());
		token=scanner.scan();
		Assert.assertEquals(TokenType.COMMA, token.getType());
		token=scanner.scan();
		Assert.assertEquals(TokenType.COLUMN_NAME, token.getType());
		Assert.assertEquals("ARTIST", token.getValue());
		token=scanner.scan();
		Assert.assertEquals(TokenType.NONE, token.getType());
		
	}
	
	@Test
	public void scanSimpleQueryWithWhereTest() {
		QueryScanner scanner=new QueryScanner("SELECT * WHERE ALBUM = \"Runes\"");
		Token token=scanner.scan();
		Assert.assertEquals(TokenType.SELECT, token.getType());
		token=scanner.scan();
		Assert.assertEquals(TokenType.ALL_SELECTOR, token.getType());
		token=scanner.scan();
		Assert.assertEquals(TokenType.WHERE, token.getType());
		token=scanner.scan();
		Assert.assertEquals(TokenType.COLUMN_NAME, token.getType());
		Assert.assertEquals("ALBUM", token.getValue());
		token=scanner.scan();
		Assert.assertEquals(TokenType.OPERATOR, token.getType());
		Assert.assertEquals("=", token.getValue());
		token=scanner.scan();
		Assert.assertEquals(TokenType.STRING, token.getType());
		Assert.assertEquals("Runes", token.getValue());
		token=scanner.scan();
		Assert.assertEquals(TokenType.NONE, token.getType());
	}
}
