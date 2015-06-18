/**
 * 
 */
package org.bragi.indexer;

/**
 * @author christoph
 *
 */
public class ParseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 411605492220033496L;
	
	private TokenType actualTokenType;
	private TokenType[] expectedTokenTypes;
	
	protected ParseException(TokenType pActualTokenType, TokenType... pExpectedTokenTypes) {
		actualTokenType = pActualTokenType;
		expectedTokenTypes = pExpectedTokenTypes;
	}

	public TokenType getActualTokenType() {
		return actualTokenType;
	}

	public TokenType[] getExpectedTokenType() {
		return expectedTokenTypes;
	}

	
	
}
