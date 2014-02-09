import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
public class Parser {

	private ArrayList<Token> tokens;
	private int i;
	private File sourcefile;
	private File sourcedir;

	public Parser (File f) {
		Lexer lex = new Lexer (f);
		tokens = lex.tokenize();
		tokens.add (new Token (Tokentype.EOF, -1));
		int ct = 0;
		for (Token t : tokens) {
			System.out.println (ct + ": " + t);
			ct ++;
		}
		i = 0;
		sourcefile = f.getAbsoluteFile();
		sourcedir = new File (sourcefile.getParent());
	}

	public void error (String message) {
		System.err.println ("Parser ERROR in line " + tokens.get(i).line + "(token " + i + "): " + message);
		Thread.currentThread().dumpStack();
		System.exit(1);
	}

	public void nferror (String message) {
		System.err.println ("Parser nonfatal ERROR in line " + tokens.get(i).line + "(token " + i + "): " + message);
	}

	public boolean has (int x) {
		return tokens.size () - 1 - x > i;
	}

	public Token next () {
		if (has (0)) {
			return tokens.get(++i);
		}
		error ("Reached end of file");
		return null;
	}

	public Token peek () {
		if (has(0)) {
			return tokens.get(i);
		}
		return tokens.get(tokens.size()-1);	// which will be the EOF token.
	}

	public Token prev () {
		if (i >= 1) {
			return tokens.get(--i);
		}
		return null;
	}

	public File findSource (String name) {	// for resolving include and use statements
		// first look in the same directory as the SCAD source file
		File f = new File (sourcedir + "/" + name);
		if (f.exists()) {
			return f;
		} else {
			// treat it as an absolute pathname
			f = new File (name);
			if (f.exists()) return f;
		}
		return null;
	}

	public Tree parse () {	// top-level parsing routine
		Tree root = new Tree (Treetype.ROOT);
		while (peek().type != Tokentype.EOF) {
			if (peek().is ("module")) {
				root.children.add (parseModule());
			} else if (peek().is ("function")) {
				root.children.add (parseFunction ());
			} else if (peek().is ("include")) {
				File f = findSource (next().val);
				if (f == null) {
					nferror ("Could not load '" + peek().val + "'.");
				} else {
					Tree t = new Parser (f).parse();
					root.children.addAll (t.children);
				}
				next();
			} else if (peek().is ("use")) {
				File f = findSource (next().val);
				if (f == null) {
					nferror ("Could not load '" + peek().val + "'.");
				} else {
					Tree subfile = new Parser(f).parse();
					for (int q=0; q<subfile.children.size(); q++) {
						Tree ch = (Tree) subfile.children.get(q);	// for some reason the compiler thinks that the contents of the children list are Objects, not Trees.
						if (ch.type == Treetype.MODULE || ch.type == Treetype.FUNCTION) {
							root.children.add (ch);
						}
					}
				}
				next();
			} else {
				root.children.add (parseStatement());
			}
		}
		return root;
	}

	public Tree<String> parseModule () {
		/* The tree's data is the name of the module.
		 * Child 0 contains the parameter profile.
		 * Child 1 contains the module body.
		*/
		Tree<String> res = new Tree<String> (Treetype.MODULE);
		if (peek().is("module")) {
			next();
			if (peek().type == Tokentype.IDENT) {
				res.data = peek().val;
				next();
				if (peek().type == Tokentype.OPEN_PAREN) {
					res.children.add (parseParamlist());
				} else {
					error ("Expecting open parenthesis in module declaration");
				}
				if (peek().type == Tokentype.OPEN_BRACE) {
					next();
					// now for the body of the module
					while (peek().type != Tokentype.CLOSE_BRACE) {
						if (peek().is ("module")) {
							res.children.add (parseModule());
						} else if (peek().is ("function")) {
							res.children.add (parseFunction());
						} else {
							res.children.add (parseStatement());
						}
					}
					next();	// skip the close brace
				} else {
					error ("Expecting { after module profile");
				}
			} else {
				error ("Expecting name after 'module' keyword");
			}
		} else {
			error ("Expecting module to start with 'module' keyword");
		}
		return res;
	}

	public Tree<String> parseFunction () {
		/* The tree's data is the name of the function. 
		 * Child 0 is a tree that has the parameter profile.
		 * Child 1 is the function body.
		*/
		Tree<String> res = new Tree<String>(Treetype.FUNCTION);
		if (peek().is ("function")) {
			next ();
			if (peek().type == Tokentype.IDENT) {
				res.data = peek().val;
				next();
				if (peek().type == Tokentype.OPEN_PAREN) {
					res.children.add (parseParamlist());
				} else {
					error ("Expecting open parenthesis in function declaration");
				}
				if (peek().type == Tokentype.ASSIGN) {
					next ();
					res.children.add (parseExpr());
				} else {
					error ("Expecting assignment after function prototype");
				}
			}
		} else {
			error ("Expecting 'function' keyword");
		}
		if (peek().type == Tokentype.SEMICOLON) {
			next();
		} else {
		       error ("Missing semicolon after function declaration");
		}	       
		return res;
	}

	public Tree<String> parseCall () {
		Tree<String> res = new Tree<String> (Treetype.CALL);
		if (peek().type == Tokentype.IDENT) {
			res.data = peek().val;
			if (Lexer.isBlock (res.data)) {
				res.type = Treetype.BLOCK;
			}
			next();
			if (peek().type != Tokentype.OPEN_PAREN) {
				error ("Expecting open parenthesis after identifier");
			}
			next();
			Tree plist = new Tree (Treetype.PARAMLIST);
			res.children.add (plist);

			while (true) {
				if (peek().type == Tokentype.COMMA) {
					next();
				} else if (peek().type == Tokentype.CLOSE_PAREN) {
					next();
					break;
				} else {
					if (peek().type == Tokentype.IDENT) {
						String name = peek().val;
						if (next().type == Tokentype.ASSIGN) {	// then we're assigning a named parameter.
							Tree<String> param = new Tree<String> (Treetype.PARAM);
							param.data = name;
							next();
							param.children.add (parseExpr());
							plist.children.add (param);
						} else {
							prev();
							plist.children.add (parseExpr());
						}
					} else {
						plist.children.add (parseExpr());
					}
				}
			}
		} else {
			error ("Expecting identifier to begin function call");
		}
		return res;
	}

	public Tree parseVector () {
		Tree vec = new Tree (Treetype.VECTOR);
		if (peek().type == Tokentype.OPEN_BRACKET) {
			next();
			while (true) {
				if (peek().type == Tokentype.COMMA) {
					next();
				} else if (peek().type == Tokentype.CLOSE_BRACKET) {
					next();
					break;
				} else {
					vec.children.add (parseExpr());
				}
			}
			return vec;
		} else {
			error ("Expecting open bracket to start vector");
		}
		return null;
	}

	public Tree parseParamlist () {
		Tree res = new Tree(Treetype.PARAMLIST);
		if (peek().type == Tokentype.OPEN_PAREN) {
			next();
			while (true) {
				if (peek().type == Tokentype.COMMA) {
					next();
				} else if (peek().type == Tokentype.CLOSE_PAREN) {
					next();
					break;
				} else {
					res.children.add (parseParam());
				}
			}
		} else {
			error ("Expecting open parenthesis to start parameter list");
		}
		return res;
	}

	public Tree parseParam () {
		Tree<String> res = new Tree<String>(Treetype.PARAM);
		// data is the name; optionally has one child, the default value.
		if (peek().type == Tokentype.IDENT) {
			res.data = peek().val;
			if (next().type == Tokentype.ASSIGN) {
				next();
				res.children.add (parseExpr());
			}
		} else {
			error ("Expecting identifier");
		}
		return res;
	}

	public Tree parseCondition () {
		Tree res = new Tree (Treetype.CONDITION);
		res.children.add (parseExpr());
		System.out.println ("Condition is \n" + res);
		System.out.println ("peek() is " + peek());
		return res;
	}

	public Tree parseStatement () {
		System.out.println ("In parseStatement, the leading token is " + peek());
		if (peek().is ("if")) {
			/* To disambiguate which children are conditions and which are statements, the conditions are wrapped in 
			 * a tree with type CONDITION. An else branch, if present, will have a CONDITION with no children. */
			Tree res = new Tree (Treetype.IF);
			next();
			res.children.add (parseCondition());
			if (peek().type == Tokentype.OPEN_BRACE) {
				next();
				while (peek().type != Tokentype.CLOSE_BRACE) {
					res.children.add (parseStatement());
				}
				next();	// skip the close brace
				while (peek().is ("else")) {
					next();
					if (peek().is ("if")) {	// this is an ELSE IF branch
						next();
						res.children.add (parseCondition());
						if (peek().type == Tokentype.OPEN_BRACE) {
							next();
						} else {
							error ("Expecting '{' after else if condition");
						}
					} else if (peek().type == Tokentype.OPEN_BRACE) {	// this is the ELSE branch
						next();
						res.children.add (new Tree (Treetype.CONDITION));	// empty condition signifies start of else branch
						break;	// we must stop looping once we hit an else branch
					} else {
						error ("Expecting 'if' or '{' after 'else'");
					}
					// now we add the statements
					while (peek().type != Tokentype.CLOSE_BRACE) {
						res.children.add (parseStatement());
					}
					next();
				}
				return res;
			} else {
				error ("Expecting { after if condition");
			}
		}

		if (peek().is ("for") || peek().is ("intersection_for")) {
			/*
			 * Child 0: Either a VECTOR tree (for enumerated values) or a RANGE tree (with two expression children, for start and end vals)
			 * 		This tree has the variable name as its data.
			 * Subsequent children: statements of the body
			*/
			Tree res = new Tree (peek().is("for") ? Treetype.FOR : Treetype.INTFOR);
			next();
			if (peek().type == Tokentype.OPEN_PAREN) {
				next();
			} else {
				nferror ("Expecting open parenthesis after loop keyword");
			}
			if (peek().type == Tokentype.IDENT) {	// good
				String name = peek().val;
				if (next().type == Tokentype.ASSIGN) {
					next();
					// now the fun begins, since the two cases are rather tricky to disambiguate.
					int savepoint = i;
					if (peek().type == Tokentype.OPEN_BRACKET) {
						next();
						Tree expr1 = parseExpr();
						/* Ranges can be either [start:end] or [start:increment:end]. We store them in this order:
						 * child 0 : start
						 * child 1 : end
						 * child 2 : increment
						*/
						if (peek().type == Tokentype.COLON) {	// range
							Tree<String> range = new Tree<String> (Treetype.RANGE);
							range.data = name;
							range.children.add (expr1);
							next();
							Tree expr2 = parseExpr();
							if (peek().type == Tokentype.COLON) {	// then we have the 3-element form of RANGE.
								next();
								Tree expr3 = parseExpr();
								range.children.add (expr3);
								range.children.add (expr2);
							} else {
								range.children.add (expr2);
								range.children.add (new Tree<Double> (Treetype.FLIT, 1.0));
							}
								
							res.children.add (range);
							if (peek().type == Tokentype.CLOSE_BRACKET) {
								next();
							} else {
								nferror ("Expecting close bracket ] to end loop expression");
							}
						} else {	// vector
							i = savepoint;
							Tree vec = parseVector();
							vec.data = name;
							res.children.add (vec);
						}
					} else {
						error ("Expecting either range or vector as loop expression");
					}
					// now we can proceed to get the body of the loop.
					if (peek().type == Tokentype.CLOSE_PAREN) {
						next();
					}
					if (peek().type == Tokentype.OPEN_BRACE) {
						next();
						while (peek().type != Tokentype.CLOSE_BRACE) {
							res.children.add (parseStatement());
						}
						next();
					}
				} else {
					error ("Expecting assignment to loop variable");
				}
			} else {
				error ("Loop expressions must be of the form <variable> = [start : end]  or  <variable> = <vector>");
			}
			return res;
		}

		if (peek().type != Tokentype.IDENT && peek().type != Tokentype.OP) {
			System.err.println ("Warning - returning NOP tree");
			return new Tree (Treetype.NOP);
		}
		if (peek().type == Tokentype.IDENT) {
			if (next().type == Tokentype.ASSIGN) {
				prev();
				Tree<String> t = new Tree<String> (Treetype.ASSIGN);
				t.data = peek().val;
				next();
				next();
				t.children.add (parseExpr());
				if (peek().type == Tokentype.SEMICOLON) {
					next();
				} else {
					nferror ("Missing semicolon");
				}
				return t;
			} else {
				prev();
			}
		}
		Tree<Op> dmode = null;
		if (peek().type == Tokentype.OP && Op.get(peek().val).isTreemod()) {	// for the things that change the display mode of a subtree
			dmode = new Tree<Op> (Treetype.DISPMODE);
			dmode.data = Op.get(peek().val);
			next();
		}
		Tree call = parseCall();
		if (dmode != null) call.children.add (dmode);
		if (peek().type == Tokentype.SEMICOLON) {
			next();
			return call;
		} else {
			if (peek().type == Tokentype.OPEN_BRACE) {
				next();
				while (peek().type != Tokentype.CLOSE_BRACE) {
					call.children.add (parseStatement());
				}
				next();
			} else {
				call.children.add (parseStatement());
			}
		}
		return call;
	}

	/* Expressions */

	public Tree parseL1 () {	// unaries 
		Tree root;
		if (peek().isOp("!") || peek().isOp("-")) {
			root = new Tree<Op> (Treetype.OP, peek().getOp());
			next();
		} else {
			return parseL0();
		}
		Tree parent = root;
		while (peek().isOp ("!") || peek().isOp("-")) {
			Tree res = new Tree<Op> (Treetype.OP, peek().getOp());
			next();
			parent.children.add (res);
			parent = res;
		}
		parent.children.add (parseL0());
		return root;
	}

	public Tree parseL2 () {	// and
		Tree ch = parseL1 ();
		while (peek().isOp (Op.AND)) {
			Tree and = new Tree<Op> (Treetype.OP, peek().getOp());
			next ();
			and.children.add (ch);
			and.children.add (parseL1());
			ch = and;
		}
		return ch;
	}

	public Tree parseL3 () {	// or
		Tree ch = parseL2 ();
		Token t = peek();
		while (t.isOp (Op.OR)) {
			Tree or = new Tree<Op> (Treetype.OP, t.getOp());
			next();
			or.children.add (ch);
			or.children.add (parseL2());
			ch = or;
		}
		return ch;
	}

	public Tree parseL4 () {	// relationals
		Tree lhs = parseL3 ();
		if (peek().getOp().isRelational()) {
			Tree rel = new Tree<Op> (Treetype.OP, peek().getOp());
			next();
			rel.children.add (lhs);
			rel.children.add (parseL3());
			return rel;
		} else {
			return lhs;
		}
	}

	public Tree parseL5 () {	// multiply, divide, mod
		Tree ch = parseL4 ();
		while (peek().isOp ("*") || peek().isOp ("/") || peek().isOp ("%")) {
			Tree<Op> parent = new Tree<Op> (Treetype.OP, peek().getOp());
			next();
			parent.children.add (ch);
			parent.children.add (parseL4());
			ch = parent;
		}
		return ch;
	}

	public Tree parseExpr () {	// add and subtract
		Tree ch = parseL5 ();
		while (peek().isOp ("+") || peek().isOp ("-")) {
			Tree parent = new Tree<Op> (Treetype.OP, peek().getOp());
			next();
			parent.children.add (ch);
			parent.children.add (parseL5());
			ch = parent;
		}
		return ch;
	}

	public Tree parseL0 () {	// and now the base-ish case.
		if (peek().type == Tokentype.OPEN_PAREN) {
			next();
			Tree res = parseExpr ();	// and back up we go!
			if (peek().type == Tokentype.CLOSE_PAREN) {
				next();
			} else {
				nferror ("Expecting close parenthesis");
			}
			return res;
		} else if (peek().type == Tokentype.OPEN_BRACKET) {	// vector
			return parseVector();
		} else if (peek().type == Tokentype.SLIT) {	// string literal
			Tree<String> res = new Tree<String>(Treetype.SLIT);
			res.data = peek().val;
			next();
			return res;
		} else if (peek().type == Tokentype.FLIT) {	// float literal
			Tree<Double> res = new Tree<Double> (Treetype.FLIT);
			res.data = peek().nval;
			next();
			return res;
		} else if (peek().type == Tokentype.IDENT) {
			// now it could either be a function/module call or a variable reference (possibly with vector indexing).
			// We must look at the following token: if OPEN_PAREN, then it's a function call.
			Token n = next();
			if (n.type == Tokentype.OPEN_PAREN) {
				prev();
				return parseCall ();
			} else {
				Tree top;
				Tree<String> id = new Tree<String> (Treetype.IDENT);
				top = id;
				id.data = prev().val;
				next();
				while (peek().type == Tokentype.OPEN_BRACKET) {
					next();
					Tree ptop = top;
					top = new Tree (Treetype.INDEX);
					top.children.add (ptop);
					top.children.add (parseExpr());
					if (peek().type != Tokentype.CLOSE_BRACKET) {
						error ("Expecting close bracket on vector index");
					}
					next();
				}
				return top;
			}
		}
		error ("You should not be at the bottom of parseL0!");
		return null;
	}
}
