/**
 * 
 */
package org.bragi.query;

/**
 * @author christoph
 *
 */
public enum TokenType {
	NONE,
	SELECT,
	WHERE,
	ORDER_DIRECTION,
	OPERATOR,
	ALL_SELECTOR, 
	ORDER,
	BY,
	COMMA, 
	NUMBER,
	MINUS, 
	PERIOD,
	STRING,
	COLUMN_NAME,
	AND,
	OR,
	NAME
}
