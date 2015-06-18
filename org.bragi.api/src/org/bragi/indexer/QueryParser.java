/**
 * 
 */
package org.bragi.indexer;

import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bragi.metadata.MetaDataEnum;

/**
 * @author christoph
 *
 */
class QueryParser {
	
	private AbstractIndexer indexer;
	private List<MetaDataEnum> queriedMetaData;
	private Predicate<Entry<MetaDataEnum,String>> filter;
	
	protected QueryParser(AbstractIndexer pIndexer) {
		indexer=pIndexer;
		queriedMetaData=new ArrayList<>();
		filter=null;
	}
	
	private Predicate<Entry<MetaDataEnum,String>> filter(MetaDataEnum field, String operator, String value) {
		if (!queriedMetaData.contains(field))
			queriedMetaData.add(field);
		switch(operator) {
		case "=":
			try {
				Object realValue=Long.parseLong(value);
				return entry -> entry.getKey()==field && realValue.equals(Long.parseLong(entry.getValue()));
			} catch (NumberFormatException nfe) {
				
			}
			try {
				Object realValue=Double.parseDouble(value);
				return entry -> entry.getKey()==field && realValue.equals(Double.parseDouble(entry.getValue()));
			} catch (NumberFormatException nfe) {
				
			}
			return entry -> entry.getKey()==field && entry.getValue().equals(value);
		case "<>":
			return entry -> entry.getKey()==field && !entry.getValue().equals(value);
		}
		return entry -> false;
	}
	
	private void parse(String query) throws ParseException {
		QueryScanner scanner=new QueryScanner(query);
		Token token = scanner.scan();
		if (token.getType()!=TokenType.SELECT) 
			throw new ParseException(token.getType(), TokenType.SELECT);
		
		token = scanner.scan();
		
		String columnName, value;
		switch (token.getType()) {
		case ALL_SELECTOR:
			queriedMetaData=EnumSet.allOf(MetaDataEnum.class).stream().collect(Collectors.toList());
			token=scanner.scan();
			break;
		case COLUMN_NAME:
			columnName=token.getValue();
			queriedMetaData.add(Enum.valueOf(MetaDataEnum.class, columnName));
			token = scanner.scan();
			switch (token.getType()) {
			case COMMA:
				token=parseColumnSpecifier(scanner, queriedMetaData);
				break;
			default:
				break;
			}
			break;
		default:
			throw new ParseException(token.getType(), TokenType.ALL_SELECTOR, TokenType.COLUMN_NAME);
		}
		switch(token.getType()) {
		case WHERE:
			token=scanner.scan();
			if (token.getType()==TokenType.COLUMN_NAME) {
				columnName=token.getValue();
				MetaDataEnum filteredMetaData=Enum.valueOf(MetaDataEnum.class, columnName);
				token=scanner.scan();
				if (token.getType()!=TokenType.OPERATOR)
					throw new ParseException(token.getType(), TokenType.OPERATOR);
				String operator=token.getValue();
				token=scanner.scan();
				switch (token.getType()) {
				case NUMBER:
					value = token.getValue();
					token=scanner.scan();
					switch(token.getType()) {
					case PERIOD:
						token=scanner.scan();
						if (token.getType()!=TokenType.NUMBER)
							throw new ParseException(token.getType(), TokenType.NUMBER);
						value+="."+token.getValue();
						break;
					case MINUS:
						token=scanner.scan();
						if (token.getType()!=TokenType.NUMBER)
							throw new ParseException(token.getType(), TokenType.NUMBER);
						value+="-"+token.getValue();
						token=scanner.scan();
						if (token.getType()!=TokenType.MINUS)
							throw new ParseException(token.getType(), TokenType.MINUS);
						token=scanner.scan();
						if (token.getType()!=TokenType.NUMBER)
							throw new ParseException(token.getType(), TokenType.NUMBER);
						value+="-"+token.getValue();
						break;
					}
					break;
				case APOSTROPHE:
					value="";
					do {
						Token lastToken=token;
						token=scanner.scan();
						value+=token.getValue();
						if (lastToken.getType()==TokenType.NONE && token.getType()==TokenType.NONE)
							throw new ParseException(token.getType(), TokenType.APOSTROPHE);
					} while(token.getType()!=TokenType.APOSTROPHE);
					break;
				default:
					throw new ParseException(token.getType(), TokenType.NUMBER, TokenType.APOSTROPHE);
				}
				if (filter==null)
					filter=filter(filteredMetaData, operator, value);
				else
					filter=filter.and(filter(filteredMetaData, operator, value));
			}
			else
				throw new ParseException(token.getType(), TokenType.COLUMN_NAME);
			break;
		case NONE:
			break;
		default:
			throw new ParseException(token.getType(), TokenType.WHERE);
		}
	}
	
	protected Map<URI,Map<MetaDataEnum,String>> execute(String query) throws ParseException {
		parse(query);
		Map<URI,Map<MetaDataEnum,String>> metaData=indexer.fetch();
		if (filter!=null) {
			metaData.replaceAll((key, value)->value.entrySet().stream().filter(entry->queriedMetaData.contains(entry.getKey())).collect(Collectors.toMap(entry->entry.getKey(), entry->entry.getValue())));
			metaData=metaData.entrySet().stream().filter(entry->entry.getValue().entrySet().stream().anyMatch(filter)).collect(Collectors.toMap(entry->entry.getKey(), entry->entry.getValue()));
		}
		return metaData;
	}
	
	private Token parseColumnSpecifier(QueryScanner scanner, List<MetaDataEnum> queriedMetaData) throws ParseException {
		Token token = scanner.scan();
		
		switch (token.getType()) {
		case COLUMN_NAME:
			String columnName=token.getValue();
			queriedMetaData.add(Enum.valueOf(MetaDataEnum.class, columnName));
			token = scanner.scan();
			if (token.getType()==TokenType.COMMA)
				parseColumnSpecifier(scanner, queriedMetaData);
			break;
		default:
			throw new ParseException(token.getType(), TokenType.COLUMN_NAME);
		}
		return token;
	}
	
}
