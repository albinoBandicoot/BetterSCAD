package frontend;

public class Semantics {

	public static void error (String message) {
		System.err.println ("Semantics ERROR: " + message);
		System.exit(1);
	}

	private static boolean isRMF (Tree t) {
		return t.type == Treetype.ROOT || t.type == Treetype.MODULE || t.type == Treetype.FUNCTION;
	}

	public static void makeSymtables (Tree root) {
		root.createST();
		STSet runtimes = new STSet (null, null);
		runtimes.tree = new Tree (Treetype.ROOT);
		root.st.parent = runtimes;
		// now add all of the predefined modules and functions
		String[] modules = {"union", "intersection", "difference", "assign", "square", "circle", "polygon", "cube", "cylinder", "sphere", "linear_extrude", "rotate_extrude", "translate", "scale", "rotate", "mirror", "multmatrix", "color"};
		String[][] mparam = {{}, {}, {}, {}, {"size", "center"}, {"r", "center"}, {"points", "paths", "convexity"}, {"size", "center"}, {"r", "h", "center"}, {"r", "center"}, {"height", "center", "convexity", "twist", "slices", "scale"}, {"convexity"}, {"v"}, {"v"}, {"a", "v"}, {"v"}, {"m"}, {"c", "alpha"}};
		Datum[][] mdefaults = {{}, {}, {}, {}, {new Scalar(1), new Scalar (0)}, {new Scalar(1), new Scalar(0)}, {new Undef(), new Undef(), new Scalar(5)}, {new Scalar(1), new Scalar(0)}, {new Scalar(1), new Scalar(1), new Scalar(0)}, {new Scalar(1), new Scalar(0)}, {new Scalar(1), new Scalar(0), new Scalar(5), new Scalar(0), new Undef(), new Scalar(1)}, {new Scalar(5)}, {new Vec(0,0,0)}, {new Vec(1,1,1)}, {new Vec(0,0,0), new Undef()}, {new Vec(1,0,0)}, {new Vec(new Vec(1,0,0), new Vec(0,1,0), new Vec(0,0,1))}, {new Vec(0.8, 0.8, 0), new Scalar(1)}};
		String[] functions = {"cos", "sin", "tan", "acos", "asin", "atan", "atan2", "abs", "ceil", "exp", "floor", "ln", "len", "log", "lookup", "max", "min", "norm", "pow", "rands", "round", "sign", "sqrt", "str"};

		for (int i=0; i<modules.length; i++) {
			Tree mdef = createModuleTree (mparam[i], mdefaults[i]);
			mdef.data = modules[i];
			runtimes.modules.put (modules[i], mdef);
		}

		for (String f : functions) {
			runtimes.functions.put (f, null);
		}

		runtimes.vars.put ("true", new Tree (Treetype.FLIT, 1.0));
		runtimes.vars.put ("false", new Tree (Treetype.FLIT, 0.0));
		runtimes.vars.put ("PI", new Tree (Treetype.FLIT, 3.14159265358979232));
		runtimes.vars.put ("undef", new Tree (Treetype.UNDEF));

		fillST (root);
		System.err.println ("Symbol tables complete");
	}

	private static Tree createModuleTree (String[] pnames, Datum[] defs) {
		Tree mdef = new Tree (Treetype.MODULE);
		Tree plist = new Tree (Treetype.PARAMLIST);
		mdef.addChild (plist);	// remember this makes a parent pointer in plist.
		mdef.createST();
		for (int i=0; i<pnames.length; i++) {
			Tree p = new Tree (Treetype.PARAM, pnames[i]);
			if (defs[i] == null || defs[i] instanceof Undef) {
				plist.addChild (new Tree (Treetype.PARAM, pnames[i]));
			} else {
				p.addChild (getTreeFromDatum (defs[i]));
			}
			plist.addChild (p);
		}
		for (Tree p : plist.children) {
			mdef.st.vars.put (p.name(), p.children.isEmpty() ? null : p.children.get(0));	// that child will be null if there's no default value. This is OK.
		}
		return mdef;
	}

	private static Tree getTreeFromDatum (Datum d) {
		if (d instanceof Scalar) {
			return new Tree (Treetype.FLIT, ((Scalar) d).d);
		} else if (d instanceof Vec) {
			Tree v = new Tree (Treetype.VECTOR);
			for (Datum elem : ((Vec) d).vals) {
				v.children.add (getTreeFromDatum (elem));
			}
			return v;
		} else if (d instanceof Undef) {
			return null;
		} else if (d instanceof Str) {
			return new Tree (Treetype.SLIT, ((Str) d).data);
		} else if (d instanceof Bool) {
			// TODO: I guess we need boolean trees also.
		}
		return null;
	}

	private static void fillST (Tree t) {
		System.out.println ("Will fill symbol table for tree of type " + t.type);
		if (t.type == Treetype.ASSIGN) {
			t.findPST().vars.put (t.name(), t.children.get(0));
			return;
		}
		if (t.type == Treetype.MODULE || t.type == Treetype.FUNCTION) {
			t.createST();
			if (t.type == Treetype.MODULE) {
				t.findPST().modules.put (t.name(), t);
			} else {
				t.findPST().functions.put (t.name(), t);
			}
			// add the parameters. It's actually easier to allow parameters to refer to previous ones than to forbid it, so I'll keep that functionality even though OpenSCAD doesn't implement it (it would actually be really useful. After all, this is BetterSCAD).
			Tree plist = t.children.get(0);
			for (Tree p : plist.children) {
				t.st.vars.put (p.name(), p.children.isEmpty() ? null : p.children.get(0));	// that child will be null if there's no default value. This is OK.
			}
		}
		if (t.type == Treetype.ROOT || t.type == Treetype.MODULE) {
			// now go through and add the regular assignments.
			for (Tree ch : t.children) {
				fillST (ch);
			}
		} else if (t.type == Treetype.FOR || t.type == Treetype.INTFOR) {
			t.createST();
			t.st.vars.put (t.children.get(0).name(), t.children.get(0));	// I guess. We might change this later.
			fillST (t.children.get(1));
		} else if (t.type == Treetype.IF) {
			for (Tree cond : t.children) {
				cond.createST();
				for (int i=1; i<cond.children.size(); i++) {
					fillST (cond.children.get(i));
				}
			}
		} else if (t.type == Treetype.MCALL) {
			t.createST();	// for locals
			System.err.println ("Filling MCALL with name " + t.name());
			if (t.name().equals("assign")) {	// then we pre-populate the ST with all of the assignments given in the block header.
				Tree plist = t.children.get(0);
				for (Tree p : plist.children) {
					if (p.children.isEmpty()) {
						error ("Missing assignment to variable " + p.name() + " in assign block");
					}
					t.st.vars.put (p.name(), p.children.get(0));
				}
			}
			for (int i=1; i<t.children.size(); i++) {
				fillST (t.children.get(i));
			}
		} else {
			System.err.println ("Doing nothing for node of type " + t.type);
		}
	}

}
