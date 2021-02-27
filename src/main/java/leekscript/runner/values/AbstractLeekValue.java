package leekscript.runner.values;

import leekscript.runner.AI;
import leekscript.runner.LeekRunException;
import leekscript.runner.LeekValueManager;

public abstract class AbstractLeekValue {

	public final static int NUMBER = 1;
	public final static int BOOLEAN = 2;
	public final static int ARRAY = 3;
	public final static int NULL = 4;
	public final static int STRING = 5;
	public final static int FUNCTION = 6;

	public final static int ADD_COST = 1;
	public final static int MUL_COST = 5;
	public final static int DIV_COST = 5;
	public final static int MOD_COST = 5;
	public final static int POW_COST = 140;

	public int getSize() throws LeekRunException {
		return 1;
	}

	public int getInt(AI ai) throws LeekRunException {
		return 0;
	}

	public void setInt(int nb) {}

	public double getDouble(AI ai) throws LeekRunException {
		return 0;
	}

	public String getString(AI ai) throws LeekRunException {
		return "";
	}

	public boolean getBoolean() {
		return false;
	}

	public boolean getBooleanTernary(AI ai) throws LeekRunException {
		ai.addOperations(1);
		return getBoolean();
	}

	public boolean isNumeric() {
		return false;
	}

	public boolean isArray() {
		return false;
	}

	public boolean isNull() {
		return false;
	}

	public ArrayLeekValue getArray() {
		return null;
	}

	public AbstractLeekValue get(AI ai, int value) throws LeekRunException, Exception {
		return LeekValueManager.NULL;
	}

	public AbstractLeekValue get(AI ai, AbstractLeekValue value) throws Exception {
		return LeekValueManager.NULL;
	}

	public AbstractLeekValue getOrCreate(AI ai, AbstractLeekValue value) throws Exception {
		return LeekValueManager.NULL;
	}

	public AbstractLeekValue getValue() {
		return this;
	}

	public AbstractLeekValue not(AI ai) {
		return LeekValueManager.getLeekBooleanValue(!getBoolean());
	}

	public AbstractLeekValue bnot(AI ai) throws Exception {
		return LeekValueManager.getLeekIntValue(~getInt(ai));
	}

	public AbstractLeekValue opposite(AI ai) throws Exception {
		return LeekValueManager.getLeekIntValue(-getInt(ai));
	}

	public AbstractLeekValue set(AI ai, AbstractLeekValue value) throws LeekRunException, Exception {
		return this;
	}
	public AbstractLeekValue setRef(AI ai, AbstractLeekValue value) throws LeekRunException, Exception {
		return this;
	}

	public int getArgumentsCount(AI ai) throws Exception {
		return -1;
	}

	// Fonctions de comparaison
	public abstract boolean equals(AI ai, AbstractLeekValue comp) throws LeekRunException;

	public boolean notequals(AI ai, AbstractLeekValue comp) throws LeekRunException {
		return !equals(ai, comp);
	}

	public boolean less(AI ai, AbstractLeekValue comp) throws LeekRunException {
		ai.addOperations(1);
		return getInt(ai) < comp.getInt(ai);
	}

	public boolean moreequals(AI ai, AbstractLeekValue comp) throws LeekRunException {
		return !less(ai, comp);
	}

	public boolean more(AI ai, AbstractLeekValue comp) throws LeekRunException {
		ai.addOperations(1);
		return getInt(ai) > comp.getInt(ai);
	}

	public boolean lessequals(AI ai, AbstractLeekValue comp) throws LeekRunException {
		return !more(ai, comp);
	}

	// Fonctions pour L-Values
	public AbstractLeekValue increment(AI ai) throws Exception {
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public AbstractLeekValue decrement(AI ai) throws Exception {
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public AbstractLeekValue pre_increment(AI ai) throws Exception {
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public AbstractLeekValue pre_decrement(AI ai) throws Exception {
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public AbstractLeekValue add(AI ai, AbstractLeekValue value) throws Exception {
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public AbstractLeekValue minus(AI ai, AbstractLeekValue value) throws Exception {
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public AbstractLeekValue multiply(AI ai, AbstractLeekValue value) throws Exception {
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public AbstractLeekValue divide(AI ai, AbstractLeekValue value) throws Exception {
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public AbstractLeekValue modulus(AI ai, AbstractLeekValue value) throws Exception {
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public AbstractLeekValue power(AI ai, AbstractLeekValue value) throws Exception {
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public AbstractLeekValue band(AI ai, AbstractLeekValue value) throws Exception {
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public AbstractLeekValue bor(AI ai, AbstractLeekValue value) throws Exception {
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public AbstractLeekValue bxor(AI ai, AbstractLeekValue value) throws Exception {
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public AbstractLeekValue bleft(AI ai, AbstractLeekValue value) throws Exception {
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public AbstractLeekValue bright(AI ai, AbstractLeekValue value) throws Exception {
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public AbstractLeekValue brotate(AI ai, AbstractLeekValue value) throws Exception {
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public abstract int getType();

	public boolean isReference() {
		return false;
	}

	public abstract Object toJSON(AI ai) throws Exception;

	public AbstractLeekValue executeFunction(AI ai, AbstractLeekValue[] value) throws Exception {

		// On ne peux pas exécuter ce type de variable
		ai.addOperations(AI.ERROR_LOG_COST);
		return LeekValueManager.NULL;
	}

	public static String getParamString(AbstractLeekValue[] parameters) {
		String ret = "";
		for (int j = 0; j < parameters.length; j++) {
			if (j != 0)
				ret += ", ";
			if (parameters[j].getValue().getType() == NUMBER)
				ret += "number";
			else if (parameters[j].getValue().getType() == BOOLEAN)
				ret += "boolean";
			else if (parameters[j].getValue().getType() == STRING)
				ret += "string";
			else if (parameters[j].getValue().getType() == ARRAY)
				ret += "array";
			else if (parameters[j].getValue().getType() == FUNCTION)
				ret += "function";
			else if (parameters[j].getValue().getType() == NULL)
				ret += "null";
			else
				ret += "?";
		}
		return ret;
	}
}
