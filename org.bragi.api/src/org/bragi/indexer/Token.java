/**
 * 
 */
package org.bragi.indexer;

/**
 * @author christoph
 *
 */
public class Token {
	private TokenType type;
	private String value;
	
	public Token() {
		type=TokenType.NONE;
		value="";
	}

	public TokenType getType() {
		return type;
	}

	public void setType(TokenType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
