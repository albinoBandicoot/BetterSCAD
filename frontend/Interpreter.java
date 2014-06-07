package frontend;
import common.*;
import java.util.*;
public class Interpreter {

	/* This class takes the parse tree after semantics has constructed the static symbol tables and
	 * runs it, producing our internal representation. */

	private Tree root;
	private Stack<Frame> rts;

	public Interpreter (Tree t) {
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

	public void error (String message) {
		System.err.println ("*** ERROR: " + message);
	}

	private Datum findVar (String name) {
		if (name.charAt(0) == '$') {	// special variables just search the stack directly upwards (dynamic scope)
			for (int i = rts.size()-1; i>=0; i--) {
				Datum d = rts.get(i).find (name);
				if (d != null) return d;
			}
			error ("Undefined variable: " + name);
			return new Undef();
		} else {	// follow the chain of static links until null or the variable is found
			Frame f = rts.peek();
			while (f != null) {
				Datum d = f.find (name);
				if (d != null) return d;
				f = f.static_link;
			}
			error ("Undefined variable: " + name);
			return new Undef();
		}
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

	private Datum evalExpr (Tree t) {
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
			return ((Op) t.data).eval (evalExpr (t.children.get(0)), t.children.size() > 1 ? evalExpr(t.children.get(1)) : null);
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
			return findVar (t.name());
		} else if (t.type == Treetype.NOP) {	// this is used for the else branch of conditionals{
			return new Bool (true);
		} else if (t.type == Treetype.RANGE) {	// this is only here because it will get added and then deleted when the for loop is constructed. It tries to evaulate the range before deleting it, so this is here so it doesn't crash.
			return new Undef();
		} else {
			System.err.println ("Uh oh, bad tree type in evalExpr: " + t);
			Thread.currentThread().dumpStack();
			System.exit(1);
		}
		return null;
	}

	public Node run () {
		Node n = run (root);
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

	private Node run (Tree t) {
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
			Tree v = t.children.get(0);
			createFrame (t);
			if (v.type == Treetype.VECTOR) {
				for (int i=0; i<v.children.size(); i++) {
					rts.peek().put (v.name(), evalExpr (v.children.get(i)));
					System.out.println ("\tLoop index put: '" + v.name() + "'");
					res.add (makeExplicit (runList (t.children, 1), CSG.UNION));
				}
			} else if (v.type == Treetype.RANGE){ 
				double start = ((Scalar) evalExpr(v.children.get(0))).d;
				double end = ((Scalar) evalExpr(v.children.get(1))).d;
				double inc = ((Scalar) evalExpr(v.children.get(2))).d;
				if (inc == 0 || (end-start)/inc < 0) {
					// bad range
					error ("Bad range");
					return null;
				}
				while (start <= end) {
					rts.peek().put (v.name(), new Scalar (start));
					res.add (makeExplicit (runList (t.children, 1), CSG.UNION));
					start += inc;
				}
			} else {
				error ("Bad tree type for for loop statement");
			}
			rts.pop();
			return makeExplicit (res, t.type == Treetype.FOR ? CSG.UNION : CSG.INTERSECTION);

		} else if (t.type == Treetype.ASSIGN) {
			/* This should only be run if we're doing runtime assignment. */
			if (Prefs.current.RUNTIME_VARS) {
				rts.peek().put (t.name(), evalExpr (t.children.get(0)));
			}
		} else if (t.type == Treetype.MCALL) {
			return runMcall (t);
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
	private ArrayList<Node> runList (ArrayList<Tree> t, int start) {	
		ArrayList<Node> ch = new ArrayList<Node>();
		for (int i=start; i<t.size(); i++) {
			Node n = run (t.get(i));
			if (n != null) {	// it would be null if, for example, the node was an assignment or a definition
				ch.add (n);
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
	private void createFrame (Tree t) {
		Frame f = new Frame (t.type.toString() + ((t.data instanceof String) ? (" " + t.name()) : ""), getStaticLink(t), t);
		rts.push (f);
		loadFrame (t, f);
	}

	private void loadFrame (Tree t, Frame f) {
		System.out.println ("Loading frame " + f.id + " with vars from tree " + t.id);
		if (Prefs.current.RUNTIME_VARS) {
			for (String vname : t.st.vars.entries.keySet()) {
				f.put (vname, new Undef());
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
	private void populateParameters (Frame b, Tree def, Tree plist, boolean predef) {
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

	private Node runMcall (Tree mc) {
		if (rts.size() > Prefs.current.STACK_HEIGHT_CAP) {
			error ("Stack height limit of " + Prefs.current.STACK_HEIGHT_CAP + " reachd.");
			System.exit(1);	// for now; but this should be immediately fatal, somehow, in the real thing
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
			error ("Undefined module " + mc.name());
			System.exit(1);
		}

		ArrayList<Node> children = runList (mc.children, 1);	// don't run the parameters list (0th child)

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
			result = runPredefModule (mdef, children);
		} else {
			result = runUserModule (mdef, children);
		}
		rts.pop();
		return result;
	}

	/* This will be in many ways similar to runMcall, except there is no child evaluation */
	private Datum runFcall (Tree fc) {
		if (rts.size() > Prefs.current.STACK_HEIGHT_CAP) {
			error ("Stack height limit reached");
			System.exit(1);
		}
		Tree fdef = fc.findST().functions.findTree(fc.name());
		Frame b = new Frame (fc.name(), getStaticLink(fdef), fdef);
		if (fdef == null) {
			error ("Undefined function " + fc.name());
			System.exit(1);
		}
		System.err.println ("Found function definition tree # " + (fdef == null ? "null" : fdef.id));
		Frame oldtop = rts.peek();
		rts.push(b);

		Frame slsave = b.static_link;	// see comments in the similar portion of runMcall
		b.static_link = oldtop;
		populateParameters (b, fdef.children.get(0), fc.children.get(0), false);
		b.static_link = slsave;

		STSet st = fdef.findST();

		Datum res;
		if (st.parent == null) {	// function in the runtimes
			res = runPredefFunc (fdef);
		} else {
			res = evalExpr (fdef.children.get(1));
		}
		rts.pop();
		return res;
	}

	private Datum runPredefFunc (Tree fdef) {
		String n = fdef.name();
		if (n.equals ("sqrt")) {
		}
			

		// TODO: Implement me!
		return new Undef();
	}
/*
		String[] modules = {"union", "intersection", "difference", "assign", "square", "circle", "polygon", "cube", "cylinder", "sphere", "linear_extrude", "rotate_extrude", "translate", "scale", "rotate", "mirror", "multmatrix", "color"};
		String[][] mparam = {{}, {}, {}, {}, {"size", "center"}, {"r", "center"}, {"points", "paths", "convexity"}, {"size", "center"}, {"r", "center"}, {"height", "center", "convexity", "twist", "slices", "scale"}, {"convexity"}, {"v"}, {"v"}, {"a", "v"}, {"v"}, {"m"}, {"c", "alpha"}};
		*/
	// surface module??
	// polyhedron module??
	// import dxf? stl?
	private Node runPredefModule (Tree mdef, ArrayList<Node> children) {
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
				error ("Expecting scalar for circle radius");
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
				error ("Expecting list of points in polygon");
			}
			return null;

		} else if (n.equals ("square")) {
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
				error ("Cube size must be either a scalar or a vector");
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
				error ("Cylinder radius must be a scalar");
			}
			if (dr1 instanceof Scalar) {
				r1 = ((Scalar) dr1).d;
			} else {
				error ("Cylinder r1 must be a scalar");
			}
			if (dr2 instanceof Scalar) {
				r2 = ((Scalar) dr2).d;
			} else {
				error ("Cylinder r2 must be a scalar");
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
				error ("Cylinder height and radii must be scalars");
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
			error ("Translate expects a flat vector of length 2 or 3");
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
					error ("Scale expects a scalar or flat vector of length 2 or 3");
					return null;
				}
			} else if (d instanceof Scalar) {
				double sfac = ((Scalar) d).d;
				tn = new TransformNode (Transform.makeScale (new Float3 (sfac, sfac, sfac)));
			} else {
				error ("Scale expects a scalar or flat vector of length 2 or 3");
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
					error ("Rotate given a non-flat x,y,z angle vector");
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
					TransformNode tn = new TransformNode (Transform.makeRotate (((Vec) vd).getFloat3(), ((Scalar) ad).d));
					tn.left = makeExplicit (children, CSG.UNION);
					return tn;
				} else {
					error ("Expecting vector for rotation");
				}
			}
			error ("Rotate expects either a scalar or a flat vector for its angle");
			return null;
		} else if (n.equals ("mirror")) {
		} else if (n.equals ("multmatrix")) {
			Datum m = top.find ("m");
			if (! (m instanceof Vec)) {
				error ("Expecting matrix in multmatrix");
			}
			Vec vm = (Vec) m;
			if (vm.wellFormedMatrix() == -1) {
				error ("Bad matrix");
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
			if (col instanceof Vec) {
				Node c = makeExplicit (children, CSG.UNION);
				c.mat = new Material (((Vec) col).getFloat3());
				return c;
			} else {
				error ("Expecting an rgb vector in color module invocation");
			}
		} else {
			System.err.println ("Unsupported or erroneous module: " + n);
			Thread.currentThread().dumpStack();
			System.exit(1);
		}
		return null;

	}

	private Node runUserModule (Tree mdef, ArrayList<Node> children) {
		rts.peek().put ("$children", new Scalar (children.size()));

		// run the code
		ArrayList<Node> result = runList (mdef.children, 1);
		Node res = makeExplicit (result, CSG.UNION);
		return res;
	}

	/* Makes implicit unions or intersections between a list of nodes explicit. 
	 * The 'type' parameter corresponds to values defined in common/CSG.java */
	private Node makeExplicit (ArrayList<Node> nodes, int type) {
		if (nodes.isEmpty()) return null;
		if (nodes.size() == 1) return nodes.get(0);
		CSG top = new CSG (type);
		top.children = new Node[nodes.size()];
		top.children = nodes.toArray(top.children);
		return top;
		/*
		CSG c = top;
		int i = 0;
		while (i < nodes.size() - 2) {
			c.left = nodes.get(i);
			CSG n = new CSG (type);
			c.right = n;
			c = n;
			i++;
		}
		c.left = nodes.get(i);
		c.right = nodes.get(i+1);
		return top;
		*/
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

		/*
		CSG c = top;
		int i = 0;
		while (i < nodes.size() - 2) {
			Node copy_op = op.copy();
			copy_op.left = nodes.get(i);
			c.left = copy_op;
			CSG n = new CSG (type);
			c.right = n;
			c = n;
			i++;
		}
		Node op1 = op.copy();
		Node op2 = op.copy();
		op1.left = nodes.get(i);
		op2.left = nodes.get(i+1);
		c.left = op1;
		c.right = op2;
		return top;
		*/
	}

	public void smushTransforms (Node n) {
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

