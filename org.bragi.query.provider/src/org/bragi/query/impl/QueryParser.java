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
package org.bragi.query.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bragi.metadata.MetaDataEnum;
import org.bragi.query.ParseException;
import org.bragi.query.QueryParserInterface;
import org.bragi.query.Token;
import org.bragi.query.TokenType;
import org.osgi.service.component.annotations.Component;

/**
 * @author christoph
 *
 */
@Component
public class QueryParser implements QueryParserInterface {
	
	private List<MetaDataEnum> queriedMetaData;
	private Predicate<Entry<URI, Map<MetaDataEnum, String>>> filter;
	
	public QueryParser() {
		queriedMetaData=new ArrayList<>();
		filter=null;
	}
	
	private Predicate<Entry<URI, Map<MetaDataEnum, String>>> filter(MetaDataEnum field, String operator, String value) {
		if (!queriedMetaData.contains(field))
			queriedMetaData.add(field);
		switch(operator) {
		case "=":
			try {
				Object realValue=Long.parseLong(value);
				return entry -> entry.getValue().entrySet().stream().anyMatch(e->e.getKey()==field && realValue.equals(Long.parseLong(e.getValue())));
				//return entry -> entry.getKey()==field && realValue.equals(Long.parseLong(entry.getValue()));
			} catch (NumberFormatException nfe) {
				
			}
			try {
				Object realValue=Double.parseDouble(value);
				return entry -> entry.getValue().entrySet().stream().anyMatch(e->e.getKey()==field && realValue.equals(Double.parseDouble(e.getValue())));
				//return entry -> entry.getKey()==field && realValue.equals(Double.parseDouble(entry.getValue()));
			} catch (NumberFormatException nfe) {
				
			}
			return entry -> entry.getValue().entrySet().stream().anyMatch(e->e.getKey()==field && value.equals(e.getValue()));
			//return entry -> entry.getKey()==field && entry.getValue().equals(value);
		case "<>":
			//return entry -> entry.getKey()==field && !entry.getValue().equals(value);
		}
		return entry -> false;
	}
	
//	private Predicate<Entry<MetaDataEnum,String>> filter(MetaDataEnum field, String operator, String value) {
//		if (!queriedMetaData.contains(field))
//			queriedMetaData.add(field);
//		switch(operator) {
//		case "=":
//			try {
//				Object realValue=Long.parseLong(value);
//				return entry -> entry.getKey()==field && realValue.equals(Long.parseLong(entry.getValue()));
//			} catch (NumberFormatException nfe) {
//				
//			}
//			try {
//				Object realValue=Double.parseDouble(value);
//				return entry -> entry.getKey()==field && realValue.equals(Double.parseDouble(entry.getValue()));
//			} catch (NumberFormatException nfe) {
//				
//			}
//			return entry -> entry.getKey()==field && entry.getValue().equals(value);
//		case "<>":
//			return entry -> entry.getKey()==field && !entry.getValue().equals(value);
//		}
//		return entry -> false;
//	}
	
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
			parseWhereClause(scanner, TokenType.NONE);
//			token=scanner.scan();
//			if (token.getType()==TokenType.COLUMN_NAME) {
//				columnName=token.getValue();
//				MetaDataEnum filteredMetaData=Enum.valueOf(MetaDataEnum.class, columnName);
//				token=scanner.scan();
//				if (token.getType()!=TokenType.OPERATOR)
//					throw new ParseException(token.getType(), TokenType.OPERATOR);
//				String operator=token.getValue();
//				value = parseValue(scanner);
//				if (filter==null)
//					filter=filter(filteredMetaData, operator, value);
//				else
//					filter=filter.and(filter(filteredMetaData, operator, value));
//			}
//			else
//				throw new ParseException(token.getType(), TokenType.COLUMN_NAME);
			break;
		case NONE:
			break;
		default:
			throw new ParseException(token.getType(), TokenType.WHERE);
		}
	}
	
	private void parseWhereClause(QueryScanner scanner, TokenType booleanOperator) throws ParseException {
		Token token=scanner.scan();
		if (token.getType()==TokenType.COLUMN_NAME) {
			String columnName=token.getValue();
			MetaDataEnum filteredMetaData=Enum.valueOf(MetaDataEnum.class, columnName);
			token=scanner.scan();
			if (token.getType()!=TokenType.OPERATOR)
				throw new ParseException(token.getType(), TokenType.OPERATOR);
			String operator=token.getValue();
			String value = parseValue(scanner);
			if (filter==null)
				filter=filter(filteredMetaData, operator, value);
			else {
				switch (booleanOperator) {
				case AND:
					filter=filter.and(filter(filteredMetaData, operator, value));
					break;
				case OR:
					filter=filter.or(filter(filteredMetaData, operator, value));
					break;
				default:
					throw new ParseException(booleanOperator, TokenType.AND, TokenType.OR);
				}
				
			}
			token=scanner.scan();
			TokenType type=token.getType();
			if ((type==TokenType.AND) || (type==TokenType.OR))
				parseWhereClause(scanner, type);
		}
		else
			throw new ParseException(token.getType(), TokenType.COLUMN_NAME);
	}

	private String parseValue(QueryScanner scanner) throws ParseException {
		String value="";
		Token token=scanner.scan();
		switch (token.getType()) {
		case MINUS:
		case NUMBER:
			value = parseNumber(scanner, token);
			break;
		case STRING:
			value = token.getValue();
			break;
		default:
			throw new ParseException(token.getType(), TokenType.MINUS, TokenType.NUMBER, TokenType.STRING);
		}
		return value;
	}

	private String parseNumber(QueryScanner scanner, Token token) throws ParseException {
		String value="";
		switch (token.getType()) {
		case MINUS:
			value = "-";
			token=scanner.scan();
			if (token.getType()!=TokenType.NUMBER)
				throw new ParseException(token.getType(), TokenType.NUMBER);
		case NUMBER:
			value += token.getValue();
			token=scanner.scan();
			switch(token.getType()) {
			case PERIOD:
				token=scanner.scan();
				if (token.getType()!=TokenType.NUMBER)
					throw new ParseException(token.getType(), TokenType.NUMBER);
				value+="."+token.getValue();
				break;
			case MINUS:
				value = parseDate(scanner, value);
				break;
			default:
				break;
			}
			break;
		default:
			throw new ParseException(token.getType(), TokenType.MINUS, TokenType.NUMBER);
		}
		return value;
	}

	private String parseDate(QueryScanner scanner, String value) throws ParseException {
		Token token;
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
		return value;
	}
	
	@Override
	public Map<URI,Map<MetaDataEnum,String>> execute(String query, Map<URI,Map<MetaDataEnum,String>> metaData) throws ParseException {
		queriedMetaData.clear();
		filter=null;
		parse(query);
//		Map<URI,Map<MetaDataEnum,String>> metaData=indexer.fetch();
		metaData.replaceAll((key, value)->value.entrySet().stream().filter(entry->queriedMetaData.contains(entry.getKey())).collect(Collectors.toMap(entry->entry.getKey(), entry->entry.getValue())));
		if (filter!=null) 
			//metaData=metaData.entrySet().stream().filter(entry->entry.getValue().entrySet().stream().peek(System.out::println).anyMatch(filter)).collect(Collectors.toMap(entry->entry.getKey(), entry->entry.getValue()));
			metaData=metaData.entrySet().stream().filter(filter).collect(Collectors.toMap(entry->entry.getKey(), entry->entry.getValue()));
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
				token=parseColumnSpecifier(scanner, queriedMetaData);
			break;
		default:
			throw new ParseException(token.getType(), TokenType.COLUMN_NAME);
		}
		return token;
	}
	
}
