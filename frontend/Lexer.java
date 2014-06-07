package frontend;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Lexer {

	public static final String[] keywords = {"if", "else", "for", "intersection_for", "function", "module", "include", "use", "undef", "true", "false"};
	public static final String[] blocks = {"rotate", "translate", "scale", "mirror", "multmatrix", "color", "assign", "render", "linear_extrude", "rotate_extrude", "union", "difference", "intersection"};

	public static boolean isBlock (String s) {
		for (String b : blocks) if (s.equals(b)) return true;
		return false;
	}

	public static final String[] operators = {"+", "-", "*", "/", "%", "<=", "==", "!=", ">=", ">", "<", "&&", "||", "!", "#"};

	public String s;
	public int i;

	private ArrayList<Integer> linebreaks;

	public Lexer (File f) throws ParseException {
		try {
			setupLexer (new Scanner (f));
		} catch (IOException e) {
			error ("Could not open file for reading");
		}
	}

	public Lexer (String s) throws ParseException {
		setupLexer (new Scanner (s));
	}

	public void setupLexer (Scanner sc) throws ParseException {
		linebreaks = new ArrayList<Integer>();
		linebreaks.add(0);
		StringBuilder sb = new StringBuilder ();
		int cpos = 0;
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			int hashidx = line.indexOf ("//");
			if (hashidx != -1) {
				sb.append (line.substring (0, hashidx) + " ");
				linebreaks.add (sb.length());
			} else {
				sb.append (line + " ");
				linebreaks.add (sb.length());
			}
		}
		// now remove the block comments
		int idx = sb.indexOf ("/*");
		while (idx != -1) {
			int endidx = sb.indexOf ("*/", idx);
			collapseComment (idx, endidx+2);
			sb.replace (idx, endidx+2, " ");
			idx = sb.indexOf ("/*", idx);
		}
		s = sb.toString();
		i = 0;
		/*
		   System.out.println ("Line breaks!");
		   for (int i=0; i<linebreaks.size(); i++) {
		   System.out.println ("breaks[" + i + "] = " + linebreaks.get(i));
		   }
		   */
	}

	private void collapseComment (int start, int end) {
		int sline = line (start);
		int eline = line (end);
		for (int l = sline; l < eline; l++){
			linebreaks.set (l, start);
		}
		for (int l = eline; l < linebreaks.size(); l++) {
			linebreaks.set (l, linebreaks.get(l) - (end - start));
		}
	}
	
	private int line (int pos) {
		int j=0;
		while (j < linebreaks.size() && linebreaks.get(j) < pos) {
			j++;
		}
		return j;
	}

	public String getEscape (char c) throws ParseException {
		switch (c) {
			case 'n':	return "\n";
			case 'r':	return "\r";
			case 't':	return "\t";
			case '\\':	return "\\";
			case '\'':	return "'";
			case '"':	return "\"";
		}
		error ("Bad escape sequence: \\" + c);
		return "";
	}

	public void skipWhitespace () {
		char c = s.charAt(i);
		while (c == ' ' || c == '\t' || c == '\n') {
			i++;
			if (i == s.length()) return;
			c = s.charAt(i);
		}
	}

	public boolean isWhitespace (int idx) {
		char c = s.charAt(idx);
		return c == ' ' || c == '\t' || c == '\n';
	}

	public boolean isIdentChar (int idx) {
		char c = s.charAt(idx);
		return ( c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '$';
	}

	public boolean isDigit (int idx) {
		char c = s.charAt(idx);
		return c >= '0' && c <= '9';
	}

	public boolean has (int x) {
		return s.length () - i > x;
	}

	public String getKeyword () {
		for (String key : keywords) {
			if (s.startsWith (key, i)) {
				if (!isIdentChar(i + key.length()) && !isDigit(i + key.length())) {	// otherwise it's the prefix of an identifier.
					return key;
				}
			}
		}
		return null;
	}

	public String getOperator () {
		for (String op : operators) {
			if (s.startsWith (op, i)) {
				return op;
			}
		}
		return null;
	}

	public void error (String message) throws ParseException{
		int line = line (i);
		int pos = i - linebreaks.get (line-1);
		throw new ParseException ("Lexer ERROR at " + line + ":" + pos + "  " + message);
	}

	public ArrayList<Token> tokenize () throws ParseException {
		ArrayList<Token> tokens = new ArrayList<Token>();
		if (s.length() == 0) {
			return tokens;
		}
		skipWhitespace ();
		while (i < s.length()) {
			String key = getKeyword ();
			String op = getOperator ();

			if (key != null) {
				if (key.equals ("true") || key.equals("false")) {
					tokens.add (new Token (Tokentype.BLIT, key, line(i)));
					i += key.length();
					skipWhitespace();
					continue;
				}
				Token t = new Token (Tokentype.KEYWORD, key, line(i));
				tokens.add (t);
				i += key.length();
				skipWhitespace();
				if (key.equals("include") || key.equals ("use")) {
					skipWhitespace();
					if (s.charAt(i) == '<') {
						i++;
						int endpos = s.indexOf ('>', i);
						if (endpos == -1) {
							error ("Unclosed filename angle brackets");
						} else {
							Token fp = new Token (Tokentype.FILENAME, s.substring (i, endpos), line(i));
							tokens.add (fp);
							i = endpos + 1;
							skipWhitespace();
							continue;
						}
					} else {
						error ("Expecting a filename/path enclosed in < > after " + key);
					}
				}
				continue;

			}
			if (op != null) {
				Token t = new Token (Tokentype.OP, op, line(i));
				tokens.add (t); 
				i += op.length();
				skipWhitespace();
				continue;
			}

			char c = s.charAt(i);
			Token t = null;
			if (c == ';') {
				t = new Token (Tokentype.SEMICOLON, line(i));
			} else if (c == ':') {
				t = new Token (Tokentype.COLON, line(i));
			} else if (c == ',') {
				t = new Token (Tokentype.COMMA, line(i));
			} else if (c == '?') {
				t = new Token (Tokentype.QMARK, line(i));
			} else if (c == '(') {
				t = new Token (Tokentype.OPEN_PAREN, line(i));
			} else if (c == ')') {
				t = new Token (Tokentype.CLOSE_PAREN, line(i));
			} else if (c == '[') {
				t = new Token (Tokentype.OPEN_BRACKET, line(i));
			} else if (c == ']') {
				t = new Token (Tokentype.CLOSE_BRACKET, line(i));
			} else if (c == '{') {
				t = new Token (Tokentype.OPEN_BRACE, line(i));
			} else if (c == '}') {
				t = new Token (Tokentype.CLOSE_BRACE, line(i));
			} else if (c == '=') {
				if (has (1)) {
					if (s.charAt(i+1) == '=') {
						t = new Token (Tokentype.OP, "==", line(i));
						i++;
					} else {
						t = new Token (Tokentype.ASSIGN, line(i)); 
					}
				} else {
					t  = new Token (Tokentype.ASSIGN, line(i));
				}
			} else if (c == '"') {
				int start = i;
				i++;
				StringBuilder sb = new StringBuilder ();
				while (has(1) && s.charAt(i) != '"') {
					if (s.charAt(i) == '\\') {
						if (has(1)) {
							sb.append (getEscape (s.charAt(i+1)));
							i+=2;
						} else {
							error ("Unclosed string literal");
						}
					} else {
						sb.append (s.charAt(i));
						i++;
					}
				}
				if (s.charAt(i) != '"') {
					error ("Unclosed string literal");
				}
				t = new Token (Tokentype.SLIT, sb.toString(), line(i));
			} else if (isDigit(i)) {	// TODO: add checks for bad floating point formatting so the program doesn't crash on bad input
				int start = i;
				double res = 0;
				while (isDigit (i)) {
					res *= 10;
					res += s.charAt(i) - '0';
					i++;
				}
				double powten = 0.1;
				if (s.charAt(i) == '.') {
					i++;
					while (isDigit (i)) {
						res += powten * (s.charAt(i) - '0');
						powten *= 0.1;
						i++;
					}
				}
				int exp = 0;
				if (s.charAt(i) == 'e' || s.charAt(i) == 'E') {
					i++;
					int sign = 1;
					if (s.charAt(i) == '+') {
						i++;
					} else if (s.charAt(i) == '-') {
						sign = -1;
						i++;
					}
					while (isDigit (i)) {
						exp = exp*10 + (s.charAt(i) - '0');
						i++;
					}
				}
				res *= Math.pow (10, exp);
				t = new Token (Tokentype.FLIT, res, line(i));
				i--;	// to counteract the increment that comes at the end
			} else if (isIdentChar(i)) {
				int start = i;
				while (isIdentChar(i) || isDigit (i)) {
					i++;
				}
				t = new Token (Tokentype.IDENT, s.substring (start, i), line(i));
				i--;	// to counteract the increment that comes at the end
			} else {
				error ("Bad character '" + c + "'");
			}
			if (t == null) {
				error ("Could not find valid token. sitting on character '" + c + "'");
			}
		//	System.out.println("Adding regular token: " + t);
			tokens.add (t);
			i++;
			skipWhitespace ();
		}
		return tokens;
	}

}
