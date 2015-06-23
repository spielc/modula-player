/**
 * 
 */
package org.bragi.query;

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
	
	public ParseException(TokenType pActualTokenType, TokenType... pExpectedTokenTypes) {
		actualTokenType = pActualTokenType;
		expectedTokenTypes = pExpectedTokenTypes;
	}

	public TokenType getActualTokenType() {
		return actualTokenType;
	}

	public TokenType[] getExpectedTokenType() {
		return expectedTokenTypes;
	}

	@Override
	public String getMessage() {
		StringBuffer message=new StringBuffer();
		message.append("Expected tokentypes=");
		for (TokenType tokenType : expectedTokenTypes) {
			message.append(tokenType+" ");
		}
		message.append("\nActual tokentype="+actualTokenType);
		return message.toString();
	}

	
	
}
