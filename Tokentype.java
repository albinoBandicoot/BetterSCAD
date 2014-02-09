public enum Tokentype {

	EOF, KEYWORD, IDENT, SLIT, FLIT, OPEN_PAREN, CLOSE_PAREN, OPEN_BRACKET, CLOSE_BRACKET, OPEN_BRACE, CLOSE_BRACE, COMMA, COLON, SEMICOLON, ASSIGN, OP, QMARK, FILENAME;

	public boolean isLiteral () {
		return this == SLIT || this == FLIT;
	}

}
