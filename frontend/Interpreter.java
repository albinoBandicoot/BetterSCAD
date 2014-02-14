package frontend;
import common.*;
import java.util.*;
public class Interpreter {

	/* This class takes the parse tree after semantics has constructed the static symbol tables and
	 * runs it, producing our internal representation. */

	public static boolean RUNTIME_VARS = false;	// false is for traditional OpenSCAD behavior

	private Tree root;
	private Stack<Bigframe> rts;

	public Interpreter (Tree t) {
	}

	private Datum findVar (String name) {
		Bigframe top = rts.peek();
		Datum d = top.findDeep (name);	// always do a full search of the top bigframe.

		int i = rts.size() - 2;
		while (d.isUndef () && i >= 0) {
			if (name.charAt(0) == '$') {	// special variable
				d = rts.get(i).findDeep (name);
			} else {
				d = rts.get(i).findShallow (name);
			}
			i--;
		}
		return d;
	}

	private Datum evalExpr (Tree t) {
	}

	private Node run (Tree t) {
		/* FIXME: for IFs and FORs we need to do stuff to the stack */
		if (t.type == Treetype.IF) {
			for (Tree ch : t.children) {	// the CONDITION trees
				if (evalExpr (ch.children.get(0)).isTrue()) {
					return run (ch.children.get(1));
				}
			}
			return null;
		} else if (t.type == Treetype.FOR || t.type == Treetype.INTFOR) {
			ArrayList<Node> res = new ArrayList<Node>();

		} else if (t.type == Treetype.ASSIGN) {
			/* This should only be run if we're doing runtime assignment. */
			if (RUNTIME_VARS) {
				rts.peek().put (t.name(), evalExpr (t.children.get(0)));
			}
		} else if (t.type == Treetype.MCALL) {
			return runMcall (t);
		}
	}

	/* Interpret a list of trees, starting at index 'start' */
	private ArrayList<Node> runList (ArrayList<Tree> t, int start) {	
		ArrayList<Node> ch = new ArrayList<Node>();
		for (int i=start; i<t.size(); i++) {
			Node n = run (t.get(idx));
			if (n != null) {	// it would be null if, for example, the node was an assignment or a definition
				ch.add (run (t.get(idx)));
			}
		}
		return ch;
	}

	/* Creates a smallframe for the tree t and pushes it onto the top of the top bigframe's stack. 
	 * t should have a static symbol table associated with it. 
	 *
	 * If RUNTIME_VARS is true, these symbols are entered into the table but initialized to undef
	 * If RUNTIME_VARS if false, their trees from the static ST are evaluated and then they are entered.
	*/
	private void createSmallframe (Tree t) {
		Smallframe sf = new Smallframe (t.type.toString());
		if (RUNTIME_VARS) {
			for (String vname : t.st.vars.entries.keySet()) {
				sf.entries.put (vname, new Undef());
			}
		} else {
			for (Map.Entry e : t.st.vars.entries.entrySet ()) {
				sf.entries.put (e.getKey(), e.getValue().t);
			}
		}
	}

	// According to the parameter profile in stat (corresponds to a module or function definition), 
	// add the parameters in plist to the bigframe's base. stat is needed to resolve the names
	// of positional parameters.
	private void populateParameters (Bigframe b, Tree stat, Tree plist) {
		int pos = 0;

		// first initialize the defaults
		for (Tree ch : stat.children) {
			if (!ch.children.isEmpty()) {	// has a default value
				b.base.entries.put (ch.name(), evalExpr (ch.children.get(0)));
			}
		}

		// now go through the given parameter list.
		for (Tree ch : plist.children) {
			if (ch.type == Treetype.PARAM) {	// named
				b.base.entries.put (ch.name(), evalExpr (ch.children.get(0)));
			} else {
				if (pos < stat.children.size()) {
					b.base.entries.put (stat.children.get(pos).name(), evalExpr (ch.children.get(0)));
				}
				pos++;
			}
		}
	}

	/* Some terminology about modules: the module's parameters are what get passed in parentheses. 
	 * The module's children or body is the stuff that gets passed in braces.
	 * The module's definition is the actual code associated with the module. */

	private Node runMcall (Tree mc) {
		/* To run a module: first, get the Node that represents the children. This will involve pushing
		 * a smallframe (so that local variables to the module body are scoped properly) and recursing.
		 *
		 * Pop this subframe, then evaluate the parameters. Now, push a new bigframe, whose base is populated
		 * with the parameters and copies of the variables from the statically enclosing scope (which may be 
		 * in smallframes since the module definition can be nested inside other constructs). 
		 * Now run the module definition. In some (many) cases, this will be one of the predefined modules 
		 * (union, linear_extrude, etc.)
		*/

		createSmallframe (mc);
		ArrayList<Node> children = runList (mc.children, 1);	// don't run the parameters list (0th child)
		popSmall();

		Bigframe b = new Bigframe (mc.name() + "_def");
		// now we need to copy the variables from the statically enclosing scope into b.base. 
		// This means we just need to copy any locals in statically enclosing smallframes, 
		// since everything else will still be accessible.
		STSet pset = mc.st.parent;
		while (pset.tree.type != Treetype.MODULE) {
			for (String vname : pset.vars.keySet()) {
				b.base.entries.put (vname, findVar (vname));	// we use findVar since we might
				// be using runtime variables, and we want the actual current value.
			}
			pset = pset.parent;
			if (pset == null) break;
		}

		// the parameters get added in, shadowing any variables already present of the same name
		Tree mdef = mc.st.modules.findSTE(mc.name()).t;
		populateParameters (b, mdef, mc.children.get(0));

		// then we can push the new bigframe and go off and execute the module code.
		rts.push (b);

		STSet st = mdef.findST();
		if (st.parent == null) {	// then it is one of the predefined modules from the runtimes
			return runPredefModule (mdef, children);
		} else {
			return runUserModule (mdef, children);
		}
	}

	/* This will be in many ways similar to runMcall, except there is 
	private Datum runFcall (Tree fc) {

	private Node runPredefModule (Tree mdef, ArrayList<Node> children) {
		String n = mdef.name();
		if (n.equals("union")) {
			return makeExplicit (children, CSG.UNION);
		} else if (n.equals ("intersection")) {
			return makeExplicit (children, CSG.INTERSECTION);
		} else if (n.equals ("difference")) {
			Node u = new CSG (CSG.DIFFERENCE);
			u.left = children.get(0);
			children.remove (0);
			u.right = makeExplicit (children, CSG.UNION);
			return u;
		} else if (n.equals ("linear_extrude")) {
			// TODO: get the parameters in; may involve creating a transform for a tapered extrusion, or whatever we do for twisted extrudes.`
			Node e = new Extrude ();
			e.left = makeExplicit (children, CSG.UNION);
			return e;
		} else if (n.equals ("rotate_extrude")) {
			// TODO: get the parameters in
			Node r = new Revolve ();
			r.left = makeExplicit (children, CSG.UNION);
		}
		// etc.

	}

	private Node runUserModule (Tree mdef, ArrayList<Node> children) {
		rts.peek().base.entries.put ("$children", new Scalar (children.size()));

		// run the code
		ArrayList<Node> result = runList (mdef.children, 1);
		Node res = makeExplicit (result, CSG.UNION);
		rts.pop();
		return res;
	}

	/* Makes implicit unions or intersections between a list of nodes explicit. 
	 * The 'type' parameter corresponds to values defined in common/CSG.java */
	private Node makeExplicit (ArrayList<Node> nodes, int type) {
	}
}

