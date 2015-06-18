/**
 * 
 */
package org.bragi.indexer;

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
	APOSTROPHE, 
	PERIOD,
	STRING,
	COLUMN_NAME
}
