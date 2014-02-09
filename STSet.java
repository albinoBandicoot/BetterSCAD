import java.util.HashMap;
public class STSet {	
	
	// A set of three symbol tables, one for modules, one for functions, and one for variables.
	// In some cases, the module and function tables won't be used.
	
	public Symtable modules, functions, vars;
	public STSet parent;

	public STSet (STSet parent) {
		this.parent = parent;
		modules = new Symtable();
		functions = new Symtable ();
		vars = new Symtable();
	}

	public Symtable getMatchingParent (Symtable s) {
		if (s == modules) return parent.modules;
		if (s == functions) return parent.functions;
		if (s == vars) return parent.vars;
		return null;
	}

	public class Symtable {

		public HashMap <String, Tree> entries;

		public Symtable () {
			entries = new HashMap <String, Tree> ();
		}

		public void put (String name, Tree t) {
			entries.put (name, t);
			// now we check the tree to make sure that all of the symbols have been defined. 
			if (!depsOK (t)) {
				Semantics.error ("Failed dependency check");
			}
		}

		/* Check whether all of the variables in the tree 't' have been defined at this point. */
		public boolean depsOK (Tree t) {
			if (t.type == Treetype.FUNCTION || t.type == Treetype.MODULE) return true;
			if (t.type == Treetype.OP) {
				for (Tree ch : t.children) {
					if (!depsOK (ch)) return false;
				}
				return true;
			} else if (t.type == Treetype.IDENT) {
				return findVar ((String) t.data);
			} else if (t.type == Treetype.CALL) {
				Tree plist = t.children.get(0);
				for (Tree param : plist.children) {
					if (param.type == Treetype.PARAM) {
						if (!depsOK (param.children.get(0))) return false;
					} else {
						if (!depsOK (param)) return false;
					}
				}
				return true;
			} else if (t.type == Treetype.VECTOR) {
				for (Tree e : t.children) {
					if (!depsOK (e)) return false;
				}
				return true;
			} else {	// a literal
				return true;
			}
		}

		public boolean findVar (String name) {
			if (entries.containsKey (name)) return true;
			if (parent != null) {
				return getMatchingParent(this).findVar (name);
			}
			return false;
		}

		public Tree findVartree (String name) {
			if (entries.containsKey (name)) return entries.get(name);
			if (parent != null) {
				return getMatchingParent(this).findVartree (name);
			}
			return null;
		}
	}
}
