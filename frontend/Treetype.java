package frontend;

public enum Treetype {

	ROOT, MODULE, MCALL, FUNCTION, FCALL, PARAMLIST, PARAM, IF, FOR, INTFOR, ASSIGN /* not the assign() {} block; that's an MCALL */, OP, COND_OP, IDENT, VECTOR, INDEX, FLIT, SLIT, UNDEF, INCLUDE, USE, DISPMODE, CONDITION, RANGE, NOP;

}
