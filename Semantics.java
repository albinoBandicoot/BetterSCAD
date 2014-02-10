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
		STSet runtimes = new STSet (null);
		root.st.parent = runtimes;
		// now add all of the predefined modules and functions
		String[] modules = {"union", "intersection", "difference", "assign", "square", "circle", "polygon", "cube", "cylinder", "sphere", "linear_extrude", "rotate_extrude", "translate", "scale", "rotate", "mirror", "multmatrix", "color"};
		String[] functions = {"abs", "cos", "sin", "tan", "atan", "asin", "acos", "atan2", "pow", "len", "min", "max", "sqrt", "round", "ceil", "floor", "str", "echo", "children"};

		for (String m : modules) {
			runtimes.modules.put (m, null);
		}

		for (String f : functions) {
			runtimes.functions.put (f, null);
		}

		runtimes.vars.put ("true", new Tree (Treetype.FLIT, 1.0));
		runtimes.vars.put ("false", new Tree (Treetype.FLIT, 0.0));
		runtimes.vars.put ("PI", new Tree (Treetype.FLIT, 3.14159265358979232));
		runtimes.vars.put ("undef", null);

		fillST (root);
	}

	private static void fillST (Tree t) {
		if (t.type == Treetype.ASSIGN) {
			t.findPST().vars.put (t.name(), t.children.get(0));
			return;
		}
		if (t.type == Treetype.MODULE || t.type == Treetype.FUNCTION) {
			t.createST();
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
			int i = 0;
			while (i < t.children.size()) {
				Tree ch = t.children.get(i);
				if (ch.type == Treetype.CONDITION) {
					ch.createST();
					Tree statement;
					while (i < t.children.size() && (statement = t.children.get(++i)).type != Treetype.CONDITION) {
						fillST (statement);
					}
				}
			}
		} else if (t.type == Treetype.MCALL) {
			t.createST();	// for locals
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
