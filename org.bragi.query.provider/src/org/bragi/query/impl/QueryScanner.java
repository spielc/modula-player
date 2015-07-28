/**
 * 
 */
package org.bragi.query.impl;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bragi.metadata.MetaDataEnum;
import org.bragi.query.QueryKeywords;
import org.bragi.query.Token;
import org.bragi.query.TokenType;

/**
 * @author christoph
 *
 */
class QueryScanner {
	
	private String query;
	private int currentPosition;
	private static Map<String, TokenType> STRING_TOKENTYPE_MAP;
	private char ch;
	static final int eofCh = '\u0080'; // character that is returned at the end of the file
	private static List<String> KEYWORDS;
	
	static {
		KEYWORDS=new ArrayList<>();
		KEYWORDS.add(QueryKeywords.SELECT);
		KEYWORDS.add(QueryKeywords.WHERE);
		KEYWORDS.add(QueryKeywords.ORDER);
		KEYWORDS.add(QueryKeywords.BY);
		KEYWORDS.add(QueryKeywords.ORDER_DIRECTION_ASC);
		KEYWORDS.add(QueryKeywords.ORDER_DIRECTION_DESC);
		KEYWORDS.add(QueryKeywords.AND);
		KEYWORDS.add(QueryKeywords.OR);
		EnumSet<MetaDataEnum> metaData=EnumSet.allOf(MetaDataEnum.class);
		KEYWORDS.addAll(metaData.stream().map(MetaDataEnum::name).collect(Collectors.toList()));
		STRING_TOKENTYPE_MAP=new Hashtable<>();
		STRING_TOKENTYPE_MAP.put(QueryKeywords.SELECT, TokenType.SELECT);
		STRING_TOKENTYPE_MAP.put(QueryKeywords.WHERE, TokenType.WHERE);
		STRING_TOKENTYPE_MAP.put(QueryKeywords.ORDER, TokenType.ORDER);
		STRING_TOKENTYPE_MAP.put(QueryKeywords.BY, TokenType.BY);
		STRING_TOKENTYPE_MAP.put(QueryKeywords.ORDER_DIRECTION_ASC, TokenType.ORDER_DIRECTION);
		STRING_TOKENTYPE_MAP.put(QueryKeywords.ORDER_DIRECTION_DESC, TokenType.ORDER_DIRECTION);
		STRING_TOKENTYPE_MAP.put(QueryKeywords.OR, TokenType.OR);
		STRING_TOKENTYPE_MAP.put(QueryKeywords.AND, TokenType.AND);
		STRING_TOKENTYPE_MAP.putAll(metaData.stream().map(MetaDataEnum::name).collect(Collectors.toMap(m->m, m->TokenType.COLUMN_NAME)));
	}

	QueryScanner(String pQuery) {
		query = pQuery.trim();
		currentPosition=0;
		nextCh();
	}
	
	private void nextCh() {
		try {
			ch = query.charAt(currentPosition++);
		} catch (IndexOutOfBoundsException ioe) {
			ch=eofCh;
		}
	}
	
	private void readName(Token token) {
		StringBuffer name=new StringBuffer();
		name.append(ch);
		nextCh();
		boolean isNameChar=true;
		while(isNameChar) {
			switch (ch){
			case 'a':case 'b':case 'c':case 'd':case 'e':case 'f':case 'g':case 'h':case 'i':case 'j':case 'k':case 'l':case 'm':case 'n':case 'o':case 'p':case 'q':case 'r':case 's':case 't':case 'u':case 'v':case 'w':case 'x':case 'y':case 'z':case 'ö':case 'ä':case'ü':
			case 'A':case 'B':case 'C':case 'D':case 'E':case 'F':case 'G':case 'H':case 'I':case 'J':case 'K':case 'L':case 'M':case 'N':case 'O':case 'P':case 'Q':case 'R':case 'S':case 'T':case 'U':case 'V':case 'W':case 'X':case 'Y':case 'Z':case 'Ö':case 'Ä':case'Ü':
			case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':
			case '_':
				name.append(ch);
				nextCh();
				break;
			default:
				isNameChar=false;
				break;
			}
		}
		if (STRING_TOKENTYPE_MAP.containsKey(name.toString())) {
			token.setType(STRING_TOKENTYPE_MAP.get(name.toString()));
		}
		else
			token.setType(TokenType.NAME);
		token.setValue(name.toString());
	}
	
	private void readNumber(Token token) {
		StringBuffer name=new StringBuffer();
		name.append(ch);
		nextCh();
		boolean isNumberChar = true;
		while (isNumberChar) {
			switch (ch) {
			case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':
				name.append(ch);
				nextCh();
				break;
			default:
				isNumberChar=false;
				break;
			}
		}
		token.setValue(name.toString());
		token.setType(TokenType.NUMBER);
	}
	
	private void readString(Token token) {
		StringBuffer string=new StringBuffer();
		nextCh();
		while((ch!='"') && (ch!=eofCh)) {
			string.append(ch);
			nextCh();
		}
		if (ch==eofCh)
			token.setType(TokenType.NONE);
		else {
			nextCh();
			token.setValue(string.toString());
			token.setType(TokenType.STRING);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.bragi.indexer.QueryInterface#scan()
	 */
	Token scan() {
		while (ch <= ' ') nextCh(); // skip blanks, tabs, eols
		Token token = new Token();
		switch (ch) {
		case 'a':case 'b':case 'c':case 'd':case 'e':case 'f':case 'g':case 'h':case 'i':case 'j':case 'k':case 'l':case 'm':case 'n':case 'o':case 'p':case 'q':case 'r':case 's':case 't':case 'u':case 'v':case 'w':case 'x':case 'y':case 'z':case 'ö':case 'ä':case'ü':
		case 'A':case 'B':case 'C':case 'D':case 'E':case 'F':case 'G':case 'H':case 'I':case 'J':case 'K':case 'L':case 'M':case 'N':case 'O':case 'P':case 'Q':case 'R':case 'S':case 'T':case 'U':case 'V':case 'W':case 'X':case 'Y':case 'Z':case 'Ö':case 'Ä':case'Ü':
			readName(token);
			break;
		case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':
			readNumber(token);
			break;
		case '.':
			token.setType(TokenType.PERIOD);
			nextCh();
			break;
		case '-':
			token.setType(TokenType.MINUS);
			nextCh();
			break;
		case '"':
			readString(token);
			break;
		case ',':
			token.setType(TokenType.COMMA);
			nextCh();
			break;
		case '*':
			token.setType(TokenType.ALL_SELECTOR);
			nextCh();
			break;
		case '<':
			nextCh();
			String value="<";
			if (ch=='=' || ch=='>') {
				value+=ch;
				nextCh();
			}
			token.setValue(value);
			token.setType(TokenType.OPERATOR);
			break;
		case '>':
			nextCh();
			String val=">";
			if (ch=='=') {
				val+=ch;
				nextCh();
			}
			token.setValue(val);
			token.setType(TokenType.OPERATOR);
			break;
		case '=':
			nextCh();
			token.setValue("=");
			token.setType(TokenType.OPERATOR);
			break;
		}

		return token;
	}
	
}
