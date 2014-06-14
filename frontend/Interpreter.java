package frontend;
import common.*;
import gui.*;
import java.util.*;
public class Interpreter {

	/* This class takes the parse tree after semantics has constructed the static symbol tables and
	 * runs it, producing our internal representation. */

	private Tree root;
	private Stack<Frame> rts;

	public Interpreter (Tree t) throws RTException {
		root = t;
		rts = new Stack <Frame> ();
		// push a Frame that has the predefined variables in it.
		Frame b = new Frame ("ROOT", null, t);		// being the root, it has no static link
		for (ListMap.Entry e : t.st.parent.vars.entries.entrySet()) {
			System.err.println("Adding " + e.getKey() + " to root frame");
			b.put ((String) e.getKey(), evalExpr (((STE) e.getValue()).t));	// these will actually just be constants
		}
		rts.push(b);
		// now run the tree.
	}

	public void error (String message) throws RTException {
		throw new RTException ("runtime ERROR: " + message);
	}

	public void error (String message, Tree t) throws RTException {
		Thread.currentThread().dumpStack();
		throw new RTException ("runtime ERROR at " + t.fm + ": " + message, t);
	}

	private Datum findVar (String name, Tree t) throws RTException {
		Frame f = findVarFrame (name);
		if (f == null) {
			error ("Undefined variable: " + name, t);
			return new Undef();
		} else {
			return f.find (name);
		}
	}

	public Frame findVarFrame (String name) {
		if (name.charAt(0) == '$') {
			for (int i = rts.size()-1; i>=0; i--) {
				if (rts.get(i).find(name) != null) return rts.get(i);
			}
		} else {
			Frame f = rts.peek();
			while (f != null) {
				if (f.find(name) != null) return f;
				f = f.static_link;
			}
		}
		return null;
	}

	public Frame getStaticLink (Tree t) {
		System.out.println ("Getting static link for " + t.name());
		int levels = rts.peek().tr.nest_depth - t.nest_depth;
		System.out.println ("Current nest depth = " + rts.peek().tr.nest_depth + "; target nest depth = " + t.nest_depth);
		if (levels == -1) {
			System.out.println ("Using current stack top (nested deeper)");
			return rts.peek();
		}
		Frame f = rts.peek();
		for (int i=0; i<=levels; i++) {
			f = f.static_link;
		}
		System.out.println ("Using frame #" + f.id);
		return f;
	}

	private Datum evalExpr (Tree t) throws RTException {
		if (t.type == Treetype.FLIT) {
			return new Scalar ((Double) t.data);
		} else if (t.type == Treetype.SLIT) {
			return new Str (t.name());
		} else if (t.type == Treetype.BLIT) {
			return new Bool ((Boolean) t.data);
		} else if (t.type == Treetype.UNDEF) {
			return new Undef();
		} else if (t.type == Treetype.VECTOR) {
			Vec v = new Vec ();
			for (Tree ch : t.children) {
				v.vals.add (evalExpr (ch));
			}
			return v;
		} else if (t.type == Treetype.OP) {
			Datum lhs = evalExpr (t.children.get(0));
			Datum rhs = null;
			if (t.children.size() > 1) {
				rhs = evalExpr(t.children.get(1));
			}
			try {
				return ((Op) t.data).eval (lhs, rhs);
			} catch (RTException e) {	// catch the exception so we can call error (rethrowing the exception) with the
				error (e.getMessage(), t); // file mark filled in
			}
		} else if (t.type == Treetype.RANGE) {
			Datum dstart = evalExpr (t.children.get(0));
			Datum dend   = evalExpr (t.children.get(1));
			Datum dstep  = evalExpr (t.children.get(2));
			if (!(dstart instanceof Scalar)) 	error ("Starting value for range must be a scalar", t);
			if (!(dend instanceof Scalar))		error ("Ending value for range must be a scalar", t);
			if (!(dstep instanceof Scalar))		error ("Step value for range must be a scalar", t);

			double start = ((Scalar) dstart).d;
			double end = ((Scalar) dend).d;
			double step = ((Scalar) dstep).d;
			return new Range (start, end, step);
		} else if (t.type == Treetype.COND_OP) {
			Datum cond = evalExpr (t.children.get(0));
			if (cond.isTrue()) {
				return evalExpr (t.children.get(1));
			} else {
				return evalExpr (t.children.get(2));
			}
		} else if (t.type == Treetype.FCALL) {
			return runFcall (t);
		} else if (t.type == Treetype.IDENT) {
			return findVar (t.name(), t);
		} else if (t.type == Treetype.NOP) {	// this is used for the else branch of conditionals{
			return new Bool (true);
		} else {
			System.err.println ("Uh oh, bad tree type in evalExpr: " + t);
			Thread.currentThread().dumpStack();
			error ("Internal error: bad tree type in evalExpr" + t.type, t);
			return new Undef();
		}
		return null;
	}

	public Node run () throws RTException {
		Node n = run (root);
		if (n == null) {
			error ("No geometry");
			return null;
		}
		smushTransforms (n);
		buildParentPointers (n);
		if (n.mat == null) {	// set up default material
			n.mat = new Material (Prefs.current.DEFAULT_OBJ_COLOR);
		}
		propogateMaterials (n);
		System.out.println ("FINAL CSG TREE: \n" + n);

		return n;
	}

	private void printStack () {
		for (int i=0; i<rts.size(); i++) {
			rts.get(i).print();
		}
	}

	private Node run (Tree t) throws RTException {
		System.out.println ("RUNNING node. type = " + t.type);
		printStack();
//		Thread.currentThread().dumpStack();
		if (t.type == Treetype.IF) {
			for (Tree ch : t.children) {	// the CONDITION trees
				Datum cond = evalExpr (ch.children.get(0));
				System.out.println ("The value of the condition is " + cond);

				if (cond.isTrue()) {
					// we need to push a smallframe with the locals.
					createFrame (ch);
					Node n = makeExplicit (runList (ch.children,1), CSG.UNION);
					rts.pop();
					return n;
				}
			}
			return null;
		} else if (t.type == Treetype.FOR || t.type == Treetype.INTFOR) {
			ArrayList<Node> res = new ArrayList<Node>();
			String lvname = t.children.get(0).name();	// loop variable name
			Datum v = evalExpr(t.children.get(0).children.get(0));
			createFrame (t);
			if (v instanceof Vec) {
				for (int i=0; i<v.size(); i++) {
					rts.peek().put (lvname, v.get(i));
					System.out.println ("\tLoop index put: '" + lvname + "' = " + v.get(i));
					res.add (makeExplicit (runList (t.children, 1), CSG.UNION));
				}
			} else if (v instanceof Range) {
				Range r = (Range) v;
				if (r.step == 0 || (r.end-r.start)/r.step < 0) {
					// bad range
					error ("Bad range", t.children.get(0));
					return null;
				}
				double x = r.start;
				while (x <= r.end) {
					rts.peek().put (lvname, new Scalar (x));
					res.add (makeExplicit (runList (t.children, 1), CSG.UNION));
					x += r.step;
				}
			} else {
				error ("Bad tree type for for loop statement", t);
			}
			rts.pop();
			return makeExplicit (res, t.type == Treetype.FOR ? CSG.UNION : CSG.INTERSECTION);

		} else if (t.type == Treetype.ASSIGN || t.type == Treetype.DECLARE) {
			/* This should only be run if we're doing runtime assignment. */
			if (Prefs.current.RUNTIME_VARS) {
				if (t.type == Treetype.DECLARE) {
					rts.peek().put (t.name(), evalExpr (t.children.get(0)));
				} else {
					Frame f = findVarFrame (t.name());
					if (f == null) {	// not found -> create local
						rts.peek().put (t.name(), evalExpr (t.children.get(0)));
					} else {
						f.put (t.name(), evalExpr (t.children.get(0)));
					}
				}
			}
		} else if (t.type == Treetype.MCALL) {
			if (t.name().equals("echo")) {
				// echo is special. We don't evaluate its children (it usually has none), and
				// there is some trickiness in the parameters - things that look like named 
				// parameters do not cause actual assignments to happen, but cause the name of
				// the variable to be printed as well.
				Tree plist = t.children.get(0);
				StringBuffer output = new StringBuffer("ECHO: ");
				for (Tree p : plist.children) {
					if (p.type == Treetype.PARAM) {
						output.append (p.name() + " = " + evalExpr (p.children.get(0)) + ", ");
					} else {
						output.append (evalExpr (p) + ", ");
					}
				}
				output.deleteCharAt (output.length() - 2);	// delete the last comma
				BetterSCAD.current.cons.append (output.toString());

			} else {
				return runMcall (t);
			}
		} else if (t.type == Treetype.ROOT) {
			Frame b = new Frame ("Root", rts.get(0), t);
			rts.push (b);
			for (ListMap.Entry e : t.st.vars.entries.entrySet()) {
				if (Prefs.current.RUNTIME_VARS) {
					b.put ((String) e.getKey(), new Undef());
				} else {
					b.put ((String) e.getKey(), evalExpr ( ((STE) (e.getValue())).t ));
				}
			}
			return makeExplicit (runList (t.children, 0), CSG.UNION);
		} else if (t.type == Treetype.MODULE || t.type == Treetype.FUNCTION) {
			// skip over.
		} else {
			System.err.println ("Bad tree type in run. " + t);
			System.exit(1);
		}
		return null;
	}

	/* Interpret a list of trees, starting at index 'start' */
	private ArrayList<Node> runList (ArrayList<Tree> t, int start) throws RTException {	
		ArrayList<Node> ch = new ArrayList<Node>();
		for (int i=start; i<t.size(); i++) {
			Node n = run (t.get(i));
			if (n != null) {	// it would be null if, for example, the node was an assignment or a definition
				ch.add (n);
			}
		}
		return ch;
	}

	/* Creates a frame for the tree t and pushes it onto the stack. 
	 * t should have a static symbol table associated with it. 
	 *
	 * If RUNTIME_VARS is true, these symbols are entered into the table but initialized to undef
	 * If RUNTIME_VARS if false, their trees from the static ST are evaluated and then they are entered.
	*/
	private void createFrame (Tree t) throws RTException {
		Frame f = new Frame (t.type.toString() + ((t.data instanceof String) ? (" " + t.name()) : ""), getStaticLink(t), t);
		rts.push (f);
		loadFrame (t, f);
	}

	private void loadFrame (Tree t, Frame f) throws RTException {
		System.out.println ("Loading frame " + f.id + " with vars from tree " + t.id);
		if (Prefs.current.RUNTIME_VARS) {
			for (String vname : t.st.vars.entries.keySet()) {
		//		f.put (vname, new Undef());	// I don't think I want to do this!
			}
		} else {
			for (ListMap.Entry e : t.st.vars.entries.entrySet ()) {
				System.out.println ("\tloading " + (String) e.getKey());
				f.put ((String) e.getKey(), evalExpr (((STE) e.getValue()).t));
			}
		}
	}
		

	// According to the parameter profile in def (corresponds to a module or function definition), 
	// add the parameters in plist to the bigframe's base. def is needed to resolve the names
	// of positional parameters.
	private void populateParameters (Frame b, Tree def, Tree plist, boolean predef) throws RTException {
		int pos = 0;

		// first initialize the defaults
		for (Tree ch : def.children) {
			if (!ch.children.isEmpty()) {	// has a default value
				b.put (ch.name(), evalExpr (ch.children.get(0)));
			} else {
				// and we do want this branch, since the parameter name should shadow locals of the 
				// same name in the enclosing scope, even if the parameter itself was not passed
				b.put (ch.name(), new Undef());
			}
		}

		// now go through the given parameter list.
		for (Tree ch : plist.children) {
			if (ch.type == Treetype.PARAM) {	// named
				b.put (ch.name(), evalExpr (ch.children.get(0)));
			} else {
				if (pos < def.children.size()) {
					Datum d = evalExpr (ch);
					if (d instanceof Undef) {
						int klass = predef ? 1 : 2;
						if (klass <= Prefs.current.PROTECT_FROM_UNDEF) {
							continue;
						}
					}
					b.put (def.children.get(pos).name(), d);
				}
				pos++;
			}
		}
	}

	/* Some terminology about modules: the module's parameters are what get passed in parentheses. 
	 * The module's children or body is the stuff that gets passed in braces.
	 * The module's definition is the actual code associated with the module. */

	private Node runMcall (Tree mc) throws RTException {
		if (rts.size() > Prefs.current.STACK_HEIGHT_CAP) {
			error ("Stack height limit of " + Prefs.current.STACK_HEIGHT_CAP + " reached.", mc);
		}
		System.out.println ("Running mcall " + mc.name());
		/* To run a module: first, get the Node that represents the children. This will involve pushing
		 * a smallframe (so that local variables to the module body are scoped properly) and recursing.
		 *
		 * Pop this subframe, then evaluate the parameters. Now, push a new bigframe, whose base is populated
		 * with the parameters and copies of the variables from the statically enclosing scope (which may be 
		 * in smallframes since the module definition can be nested inside other constructs). 
		 * Now run the module definition. In some (many) cases, this will be one of the predefined modules 
		 * (union, linear_extrude, etc.)
		*/

		Tree mdef = mc.st.modules.findTree(mc.name());
		if (mdef == null) {
			error ("Undefined module " + mc.name(), mc);
		}

		createFrame (mc);
		rts.peek().name = rts.peek().name + "_ch";
		ArrayList<Node> children = runList (mc.children, 1);	// don't run the parameters list (0th child)
		rts.pop();

		System.out.println ("Finished running children of " + mc.name() + "(" + mc.id + ")");


		// the parameters get added in, shadowing any variables already present of the same name
		STSet st = mdef.findST();
		boolean predef = mdef.parent.parent == null;
		Frame f = new Frame (mc.name(), getStaticLink (mdef), mdef);

		System.out.println ("Populating parameters for " + mc.name() + "(" + mc.id + ")");
		Frame oldtop = rts.peek();
		rts.push (f);	// need to push here so parameters can reference each other.

		Frame slsave = f.static_link;
		f.static_link = oldtop;	// temporarily set the static link to the current stack top, for evaulating params.
		// This is an issue since we need to push the frame before we populate parameters so the params can see each other,
		// but then the scoping for expressions in the params is wrong. 
		populateParameters (f, mdef.children.get(0), mc.children.get(0), predef);
		f.static_link = slsave;

		System.out.println ("Loading locals for " + mc.name() + "(" + mc.id + ")");
		loadFrame (mdef, f);

		System.out.println ("Now going off to run the definition of " + mc.name() + "; the stack is: ");
		printStack();

		Node result;
		if (predef) {
			result = runPredefModule (mc, mdef, children);
		} else {
			result = runUserModule (mdef, children);
		}
		rts.pop();
		return result;
	}

	/* This will be in many ways similar to runMcall, except there is no child evaluation */
	private Datum runFcall (Tree fc) throws RTException {
		if (rts.size() > Prefs.current.STACK_HEIGHT_CAP) {
			error ("Stack height limit reached", fc);
			System.exit(1);
		}
		Tree fdef = fc.findST().functions.findTree(fc.name());
		if (fdef == null) {
			error ("Undefined function " + fc.name(), fc);
		}
		STSet st = fdef.findST ();
		boolean predef = st == null;
		System.err.println ("Found function definition tree # " + (fdef == null ? "null" : fdef.id));

		Frame b = new Frame (fc.name(), getStaticLink(fdef), fdef);
		Frame oldtop = rts.peek();
		rts.push(b);

		Frame slsave = b.static_link;	// see comments in the similar portion of runMcall
		b.static_link = oldtop;
		if (predef) {
			// we need to special-case this because the predefined functions completely ignore their parameters' names.
			// So we will create a set of fake param names that are illegal OpenSCAD identifiers that are just filled in
			// positionally from the inputs to the function.
			Tree plist = fc.children.get(0);
			int i = 1;
			for (Tree param : plist.children) {
				if (param.type == Treetype.PARAM) {	// named. We discard the given name.
					b.put ("@" + i, evalExpr (param.children.get(0)));
				} else {
					b.put ("@" + i, evalExpr (param));
				}
				i++;
			}

		} else {
			populateParameters (b, fdef.children.get(0), fc.children.get(0), false);
		}
		b.static_link = slsave;

		System.err.println ("Stack after function " + fc.name() + "'s parameters have been loaded is: ");
		printStack();

		Datum res;
		if (predef) {	// function in the runtimes
			res = runPredefFunc (fdef, fc);
		} else {
			res = evalExpr (fdef.children.get(1));
		}
		rts.pop();
		return res;
	}

	private Datum runPredefFunc (Tree fdef, Tree fc) throws RTException {
		String n = fdef.name();
		Frame top = rts.peek();
		if (n.equals ("len")) {
			Datum d = top.find ("@1");
			if (d instanceof Vec || d instanceof Str) {
				return new Scalar (d.size());
			} else {
				error ("Can only take lengths of vectors and strings.", fc);
			}
		} else if (n.equals ("cross")) {
			Datum v1 = top.find ("@1");
			Datum v2 = top.find ("@2");
			if (v1 instanceof Vec && v2 instanceof Vec) {
				if (v1.size() == 3 && v2.size() == 3) {
					return new Vec (((Vec) v1).getFloat3().cross (((Vec) v2).getFloat3()));
				} else {
					error ("Cross product only defined for 3-dimensional vectors.", fc);
				}
			} else {
				error ("Cross product takes two vector parameters.", fc);
			}
		} else if (n.equals ("norm")) {
			Datum v = top.find ("@1");
			if (v instanceof Vec) {
				double res = 0;
				for (int i = 0; i < v.size(); i++) {
					Datum e = v.get(i);
					if (e instanceof Scalar) {
						double x = ((Scalar) e).d;
						res += x*x;
					} else {
						error ("All entries in vector must be scalars in order to compute norm", fc);
					}
				}
				return new Scalar (Math.sqrt(res));
			} else {
				error ("norm can only be applied to vectors", fc);
			}
		} else if (n.equals ("rands")) {
			Datum dmin = top.find ("@1");
			Datum dmax = top.find ("@2");
			Datum dnvals = top.find ("@3");
			Datum dseed = top.find ("@4");
			double min = 0;
			double max = 0;
			int nvals = 0;
			int seed = 0;
			boolean use_seed = false;
			if (dmin instanceof Scalar) {
				min = ((Scalar) dmin).d;
			} else {
				error ("minimum value for rands must be a scalar", fc);
			}
			if (dmax instanceof Scalar) {
				max = ((Scalar) dmax).d;
			} else {
				error ("maximum value for rands must be a scalar", fc);
			}
			if (dnvals instanceof Scalar) {
				nvals = (int) ((Scalar) dnvals).d;
			} else {
				error ("#values for rands must be a scalar", fc);
			}
			if (dseed == null) {
				use_seed =false;
			} else if (dseed instanceof Scalar) {
				use_seed = true;
				seed = (int) ((Scalar) dseed).d;
			} else {
				error ("Seed value for rands must be a scalar", fc);
			}
			Random rand;
			if (use_seed) {
				rand = new Random (seed);
			} else {
				rand = new Random();
			}
			Vec res = new Vec ();
			for (int i=0; i<nvals; i++) {
				res.vals.add (new Scalar (rand.nextDouble() * (max - min) + min));
			}
			return res;
		} else if (n.equals ("str")) {
			StringBuffer sb = new StringBuffer();
			Datum d = top.find ("@1");
			int i = 1;
			while (d != null) {
				sb.append (d);
				i++;
				d = top.find ("@" + i);
			}
			return new Str (sb.toString());
		}
				

		if (n.equals ("min") || n.equals ("max")) {
			boolean min = n.equals("min");
			double result = min ? 1e20 : -1e20;
			Datum d = top.find ("@1");
			if (d instanceof Vec) {
				Vec v = (Vec) d;
				for (int i = 0; i < v.size(); i++) {
					Datum vi = v.get(i);
					if (vi instanceof Scalar) {
						double x = ((Scalar) vi).d;
						if (min) {
							result = Math.min (result, x);
						} else {
							result = Math.max (result, x);
						}
					} else {
						error ("Found non-scalar element of vector in call to " + n, fc);
					}
				}
			} else if (d instanceof Scalar) {
				int i = 2;
				while (d != null && d instanceof Scalar) {
					if (min) {
						result = Math.min (result, ((Scalar) d).d);
					} else {
						result = Math.max (result, ((Scalar) d).d);
					}
					d = top.find ("@" + i);
					i++;
				}
				if (d != null) {
					error ("Expecting scalars as parameters to " + n, fc);
				}
			} else {
				error ("Expecting either a series of scalars or a vector as parameters to " + n, fc);
			}
		}

		if (fdef.idata == 1) {
			Datum d = top.find ("@1");
			double x = 0;
			if (d instanceof Scalar) {
				x = ((Scalar) d).d;
			} else {
				error ("Expecting single scalar parameter for function " + n, fc);
			}
			if (n.equals ("sqrt")) {
				if (x >= 0) {
					return new Scalar (Math.sqrt(x));
				} else {
					error ("Square root of " + x + " is complex!", fc);
				}
			} else if (n.equals ("cos")) {
				return new Scalar (Math.cos(x * 180 / Math.PI));
			} else if (n.equals ("sin")) {
				return new Scalar (Math.sin (x * 180 / Math.PI));
			} else if (n.equals ("tan")) {
				return new Scalar (Math.tan (x * 180 / Math.PI));
			} else if (n.equals ("acos")) {
				if (x >= -1 && x <= 1) {
					return new Scalar (Math.acos (x) * 180/Math.PI);
				} else {
					error ("acos of " + x + " does not exist", fc);
				}
			} else if (n.equals ("asin")) {
				if (x >= -1 && x <= 1) {
					return new Scalar (Math.asin (x) * 180/Math.PI);
				} else {
					error ("asin of " + x + " does not exist", fc);
				}
			} else if (n.equals ("atan")) {
				return new Scalar (Math.atan (x) * 180/Math.PI);
			} else if (n.equals ("abs")) {
				return new Scalar (Math.abs(x));
			} else if (n.equals ("ceil")) {
				return new Scalar (Math.ceil (x));
			} else if (n.equals ("floor")) {
				return new Scalar (Math.floor(x));
			} else if (n.equals ("round")) {
				return new Scalar (Math.round (x));
			} else if (n.equals ("exp")) {
				return new Scalar (Math.exp (x));
			} else if (n.equals ("log")) {
				return new Scalar (Math.log10 (x));
			} else if (n.equals ("ln")) {
				return new Scalar (Math.log (x));
			} else if (n.equals ("sign")) {
				return new Scalar (Math.signum (x));
			}
		} else if (fdef.idata == 2) {
			Datum a = top.find ("@1");
			Datum b = top.find ("@2");
			double x = 0;
			double y = 0;
			if (a instanceof Scalar) {
				x = ((Scalar) a).d;
			} else {
				error ("Function " + n + " expects two scalar arguments", fc);
			}
			if (b instanceof Scalar) {
				y = ((Scalar) b).d;
			} else {
				error ("Function " + n + " expects two scalar arguments", fc);
			}
			if (n.equals ("pow")) {
				return new Scalar (Math.pow (x,y));
			} else if (n.equals ("atan2")) {
				return new Scalar (Math.atan2 (x,y));
			}
		}

		return new Undef();
	}
/*
		String[] modules = {"union", "intersection", "difference", "assign", "square", "circle", "polygon", "cube", "cylinder", "sphere", "linear_extrude", "rotate_extrude", "translate", "scale", "rotate", "mirror", "multmatrix", "color"};
		String[][] mparam = {{}, {}, {}, {}, {"size", "center"}, {"r", "center"}, {"points", "paths", "convexity"}, {"size", "center"}, {"r", "center"}, {"height", "center", "convexity", "twist", "slices", "scale"}, {"convexity"}, {"v"}, {"v"}, {"a", "v"}, {"v"}, {"m"}, {"c", "alpha"}};
		*/
	// surface module??
	// polyhedron module??
	// import dxf? stl?
	private Node runPredefModule (Tree mc, Tree mdef, ArrayList<Node> children) throws RTException {
		String n = mdef.name();
		Frame top = rts.peek();
		if (n.equals("union")) {
			return makeExplicit (children, CSG.UNION);
		} else if (n.equals ("intersection")) {
			return makeExplicit (children, CSG.INTERSECTION);
		} else if (n.equals ("difference")) {
			Node u = makeExplicit (children, CSG.DIFFERENCE);
			/*
			Node u = new CSG (CSG.DIFFERENCE);
			u.left = children.get(0);
			children.remove (0);
			u.right = makeExplicit (children, CSG.UNION);
			*/
			return u;
		} else if (n.equals ("linear_extrude")) {
			// TODO: get the parameters in; may involve creating a transform for a tapered extrusion, or whatever we do for twisted extrudes.`
			Datum h = top.find( "height");
			if (! (h instanceof Scalar)) {
				return null;
			}
			Node e = new Extrude (((Scalar) h).d);
			return inject (children, e, CSG.UNION);
//			e.left = makeExplicit (children, CSG.UNION);
//			return e;
		} else if (n.equals ("rotate_extrude")) {
			Node r = new Revolve ();
			r.left = makeExplicit (children, CSG.UNION);
			return r;
		} else if (n.equals ("sphere")) {
			Datum r = top.find ("r");
			if (r instanceof Scalar) {
				System.err.println ("GOOD: Returning new sphere");
				return new Sphere (((Scalar) r).d);
			} else {
				System.err.println ("ERROR: non-scalar sphere radius");
			}
			return null;

		} else if (n.equals ("circle")) {
			Datum d = top.find ("r");
			if (d instanceof Scalar) {
				return new Circle (((Scalar) d).d);
			} else {
				error ("Expecting scalar for circle radius", mc);
			}
			return null;

		} else if (n.equals ("polygon")) {
			Datum points = top.find ("points");
			Datum paths = top.find ("paths");
			if (points instanceof Vec) {
				Vec p = (Vec) points;
				int npts = p.vals.size();
				Float3[] pts = new Float3[npts];
//				int[][] ind = new int[1][npts];	// for now, just make one loop with everythin
				for (int i=0; i<npts; i++) {
					pts[i] = ((Vec) p.get(i)).getFloat3();
//					ind[0][i] = i;
				}
				return new SimplePolygon (pts);
//				return new Polygon (pts, ind);
			} else {
				error ("Expecting list of points in polygon", mc);
			}
			return null;

		} else if (n.equals ("square")) {
			Datum size = top.find ("size");
			Datum cent = top.find ("center");
			double xs = 0;
			double ys = 0;
			if (size instanceof Scalar) {
				xs = ((Scalar) size).d;
				ys = xs;
			} else if (size instanceof Vec) {
				Float3 s = ((Vec) size).getFloat3();
				xs = s.x;
				ys = s.y;
			} else {
				error ("Square size must be either a scalar or a vector", mc);
			}
			Rectangle rect = new Rectangle (xs, ys);
			if (cent.isTrue ()) {
				TransformNode c = new TransformNode (Transform.makeTranslate (new Float3 (xs/2, ys/2, 0)));
				c.left = rect;
				return c;
			}
			return rect;

		} else if (n.equals ("cube")) {		// linear extrude a square
			Datum size = top.find ("size");
			Datum cent = top.find ("center");
			Float3 s = new Float3();
			if (size instanceof Scalar) {
				double dim = ((Scalar) size).d;
				s = new Float3 (dim, dim, dim);
			} else if (size instanceof Vec) {
				s = ((Vec) size).getFloat3();
			} else {
				error ("Cube size must be either a scalar or a vector", mc);
			}
			Rectangle rect = new Rectangle (s.x, s.y);
			Extrude e = new Extrude (s.z);
			e.left = rect;

			if (cent.isTrue()) {
				TransformNode c = new TransformNode (Transform.makeTranslate (s.mul(-0.5)));
				c.left = e;
				return c;
			} else {
				return e;
			}

		} else if (n.equals ("cylinder")) {	// linear extrude a circle
			Datum h = top.find ("h");
			Datum dr = top.find ("r");
			Datum dr1 = top.find ("r1");
			Datum dr2 = top.find ("r2");
			Datum cent = top.find ("center");
			double r1 = 1;
			double r2 = 1;
			if (dr instanceof Scalar) {
				r1 = ((Scalar) dr).d;
				r2 =r1;
			} else {
				if (!(dr instanceof Undef || dr == null)) error ("Cylinder radius must be a scalar", mc);
			}
			if (dr1 instanceof Scalar) {
				r1 = ((Scalar) dr1).d;
			} else {
				if (!(dr1 instanceof Undef || dr1 == null)) error ("Cylinder r1 must be a scalar", mc);
			}
			if (dr2 instanceof Scalar) {
				r2 = ((Scalar) dr2).d;
			} else {
				if (!(dr2 instanceof Undef || dr2 == null)) error ("Cylinder r2 must be a scalar", mc);
			}
			if (h instanceof Scalar) {
				Circle c = new Circle (r1);
				Extrude e = new Extrude (((Scalar) h).d);
				e.left = c;
				if (cent.isTrue()) {
					System.out.println ("CENTERED CYLINDER");
					TransformNode ce = new TransformNode (Transform.makeTranslate (new Float3 (0,0,-e.h/2)));
					ce.left = e;
					return ce;
				} else {
					return e;
				}
			} else {
				error ("Cylinder height and radii must be scalars", mc);
			}

		} else if (n.equals ("translate")) {
			Datum d = top.find ("v");
			if (d instanceof Vec) {
				Vec v = (Vec) d;
				if (v.isFlat() && v.size() == 2 || v.size() == 3) {
					TransformNode tn = new TransformNode (Transform.makeTranslate (v.getFloat3()));
					tn.left = makeExplicit (children, CSG.UNION);
					return tn;
				}
			}
			error ("Translate expects a flat vector of length 2 or 3", mc);
			return null;
		} else if (n.equals ("scale")) {
			Datum d = top.find ("v");
			TransformNode tn;
			if (d instanceof Vec) {
				Vec v = (Vec) d;
				if (v.isFlat() && v.size() == 2 || v.size() == 3) {
					Float3 s = v.getFloat3();
					if (v.size() == 2) {
						s.z = 1;
					}
					tn = new TransformNode (Transform.makeScale (s));
				} else {
					error ("Scale expects a scalar or flat vector of length 2 or 3", mc);
					return null;
				}
			} else if (d instanceof Scalar) {
				double sfac = ((Scalar) d).d;
				tn = new TransformNode (Transform.makeScale (new Float3 (sfac, sfac, sfac)));
			} else {
				error ("Scale expects a scalar or flat vector of length 2 or 3", mc);
				return null;
			}
			tn.left = makeExplicit (children, CSG.UNION);
			return tn;
		} else if (n.equals ("rotate")) {
			Datum ad = top.find ("a");
			Datum vd = top.find ("v");
			/*
			if (! (vd instanceof Vec)) {
				error ("Rotate expects a vector for its axis");
				return null;
			}
			*/
			if (ad instanceof Vec) {	// x, y, z angles
				if (! ((Vec) ad).isFlat()) {
					error ("Rotate given a non-flat x,y,z angle vector", mc);
					return null;
				}
				Vec vad = (Vec) ad;
				Transform x, y, z;
				x = Transform.makeRotate (new Float3 (1,0,0), vad.getd(0));
				y = Transform.makeRotate (new Float3 (0,1,0), vad.getd(1));
				z = Transform.makeRotate (new Float3 (0,0,1), vad.getd(2));
				x = z.append (y.append (x));
//				x = x.append(y.append(z));
				TransformNode tn = new TransformNode (x);
				tn.left = makeExplicit (children, CSG.UNION);
				return tn;

			} else if (ad instanceof Scalar) {	// angle around axis v
				if (vd instanceof Vec) {
					TransformNode tn;
					if (((Vec) vd).isZeroVec()) {	// OpenSCAD takes this to mean rotation about Z-axis.
						tn = new TransformNode (Transform.makeRotate (new Float3(0,0,1), ((Scalar) ad).d));
					} else {
						tn = new TransformNode (Transform.makeRotate (((Vec) vd).getFloat3(), ((Scalar) ad).d));
					}
					tn.left = makeExplicit (children, CSG.UNION);
					return tn;
				} else if (vd instanceof Undef) {
					// then we have rotation about z-axis.
					Transform z = Transform.makeRotate (new Float3(0,0,1), ((Scalar) ad).d);
					TransformNode tn = new TransformNode (z);
					tn.left = makeExplicit (children, CSG.UNION);
					return tn;
				} else {
					error ("Expecting vector for rotation", mc);
				}
			}
			error ("Rotate expects either a scalar or a flat vector for its angle", mc);
			return null;
		} else if (n.equals ("mirror")) {
		} else if (n.equals ("multmatrix")) {
			Datum m = top.find ("m");
			if (! (m instanceof Vec)) {
				error ("Expecting matrix in multmatrix", mc);
			}
			Vec vm = (Vec) m;
			if (vm.wellFormedMatrix() == -1) {
				error ("Bad matrix", mc);
				return null;
			}
			double[][] mat = new double[4][4];
			for (int i=0; i<4; i++){
				 for (int j=0; j<4; j++) {
					 mat[i][j] = ((Scalar) m.get(i).get(j)).d;
				 }
			}
			TransformNode tn = new TransformNode (new Transform (mat));
			tn.left = makeExplicit (children, CSG.UNION);
			return tn;
		} else if (n.equals ("color")) {
			Datum col = top.find ("c");
			Float3 color = Prefs.current.DEFAULT_OBJ_COLOR;
			if (col instanceof Vec) {
				color = ((Vec) col).getFloat3();
			} else if (col instanceof Str) {
				color = NamedColor.getColorByName (col.toString());
				if (color == null) {
					error ("Unknown color name " + col, mc);
				}
			} else {
				error ("Expecting an rgb vector or named color", mc);
			}
			Node c = makeExplicit (children, CSG.UNION);
			c.mat = new Material (color);
			return c;
		} else {
			error ("Unsupported or erroneous module: " + n, mc);
		}
		return null;

	}

	private Node runUserModule (Tree mdef, ArrayList<Node> children) throws RTException {
		rts.peek().put ("$children", new Scalar (children.size()));

		// run the code
		ArrayList<Node> result = runList (mdef.children, 1);
		Node res = makeExplicit (result, CSG.UNION);
		return res;
	}


	/* Makes implicit unions or intersections between a list of nodes explicit. 
	 * The 'type' parameter corresponds to values defined in common/CSG.java */
	private Node makeExplicit (ArrayList<Node> nodes, int type) {
		if (nodes.isEmpty()) {
			System.err.println ("WARNING - makeExplicit called with empty list");
			return null;
		}
		if (nodes.size() == 1) return nodes.get(0);
		CSG top = new CSG (type);
		top.children = new Node[nodes.size()];
		top.children = nodes.toArray(top.children);
		return top;
	}

	/* Does a very similar thing to makeExplicit, except it injects a copy of 'op'
	 * as a parent of each node in 'nodes' before adding it. This is useful for converting,
	 * say, an extrude of a union to a union of extrudes. */
	private Node inject (ArrayList<Node> nodes, Node op, int type) {
		if (nodes.isEmpty()) return null;
		if (nodes.size() == 1) {
			op.left = nodes.get(0);
			return op;
		}
		CSG top = new CSG (type);
		top.children = new Node[nodes.size()];
		int i = 0;
		for (Node n : nodes) {
			Node copy_op = op.copy();
			copy_op.left = nodes.get(i);
			top.children[i] = copy_op;
			i++;
		}
		return top;
	}

	public void smushTransforms (Node n) {
		if (n == null) {
			System.err.println ("WARNING - unexpected NULL node in smushTransforms()");
			return;
		}
		if (n instanceof TransformNode) {
			if (n.left instanceof TransformNode) {
				smushTransforms (n.left);
				TransformNode tnl = (TransformNode) n.left;
				TransformNode tn = (TransformNode) n;
				tn.xform = tnl.xform.append (tn.xform);
				tn.inverse = tnl.inverse.append (tn.inverse);
				n.left = n.left.left;
			}
		} else if (n instanceof CSG) {
			CSG c = (CSG) n;
			for (int i=0; i<c.children.length; i++) {
				smushTransforms (c.children[i]);
			}
		} else {
			if (n.left != null) smushTransforms (n.left);
			if (n.right != null) smushTransforms (n.right);
		}
	}
	
	public void buildParentPointers (Node n) {
		if (n.left != null) {
			n.left.parent = n;
			buildParentPointers (n.left);
		}
		if (n.right != null) {
			n.right.parent = n;
			buildParentPointers (n.right);
		}
		if (n instanceof CSG) {
			CSG c = (CSG) n;
			for (int i=0; i<c.children.length; i++) {
				c.children[i].parent = c;
				buildParentPointers (c.children[i]);
			}
		}
	}

	public void propogateMaterials (Node n) {
		// CAUTION: Only run after parent pointers have been built!
		if (n.mat == null) {
			if (n.parent != null) {
				n.mat = n.parent.mat;
			}
		}
		if (n.left != null) propogateMaterials (n.left);
		if (n.right != null) propogateMaterials (n.right);
		if (n instanceof CSG) {
			CSG c = (CSG) n;
			for (Node ch : c.children) {
				propogateMaterials (ch);
			}
		}
	}

}

