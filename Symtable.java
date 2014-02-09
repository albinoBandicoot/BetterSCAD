import java.util.HashMap;
public class Symtable {

	public HashMap <String, STE> entries;

	public Symtable parent;

	public Symtable (Symtable parent) {
		this.parent = parent;
		entries = new HashMap <String, STE> ();
	}

	public void put (String name, Tree t) {
		entries.put (name, new STE(t));
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
			return parent.findVar (name);
		}
		return false;
	}

	public STE findSTE (String name) {
		if (entries.containsKey (name)) return entries.get(name);
		if (parent != null) {
			return parent.findSTE (name);
		}
		return null;
	}
}
