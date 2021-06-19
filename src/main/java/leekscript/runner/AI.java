package leekscript.runner;

import leekscript.AILog;
import leekscript.compiler.LeekScript;
import leekscript.compiler.RandomGenerator;
import leekscript.runner.PhpArray.Element;
import leekscript.runner.values.ArrayLeekValue;
import leekscript.runner.values.ClassLeekValue;
import leekscript.runner.values.FunctionLeekValue;
import leekscript.runner.values.LeekValue;
import leekscript.runner.values.ObjectLeekValue;
import leekscript.runner.values.Box;
import leekscript.runner.values.ArrayLeekValue.ArrayIterator;
import leekscript.common.Error;

import java.util.Comparator;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

public abstract class AI {

	public static final int DOUBLE = 1;
	public static final int INT = 2;
	public static final int BOOLEAN = 3;
	public static final int STRING = 4;
	public static final int NULL = 5;
	public static final int ARRAY = 6;
	public static final int NUMBER = 7;
	public static final int FUNCTION = 8;

	public static final int ERROR_LOG_COST = 1000;

	public final static int MAX_MEMORY = 100000;

	protected int mOperations = 0;
	public final static int MAX_OPERATIONS = 20000000;
	public int maxOperations = MAX_OPERATIONS;

	protected JSONArray mErrorObject = null;
	protected String thisObject = null;

	protected int id;
	protected int version;
	protected AILog logs;
	// protected AI mUAI;
	protected int mInstructions;
	protected RandomGenerator randomGenerator;
	private long analyzeTime;
	private long compileTime;
	private long loadTime;

	public AI(int instructions, int version) {
		this.mInstructions = instructions;
		this.version = version;
		logs = new AILog();
		randomGenerator = LeekScript.getRandom();
		try {
			init();
		} catch (Exception e) {}
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	// Method that can be overriden in each AI
	protected void init() throws Exception {}

	public int getInstructions() {
		return mInstructions;
	}

	public int operations() {
		return mOperations;
	}

	public int getOperations() throws LeekRunException {
		ops(LeekFunctions.getOperations.getOperations());
		return mOperations;
	}

	public AILog getLogs() {
		return logs;
	}

	public void ops(int nb) throws LeekRunException {
		// System.out.println("ops " + nb);
		mOperations += nb;
		if (mOperations >= maxOperations) {
			throw new LeekRunException(LeekRunException.TOO_MUCH_OPERATIONS);
		}
	}

	public Object ops(Object x, int nb) throws LeekRunException {
		ops(nb);
		return x;
	}

	public int ops(int x, int nb) throws LeekRunException {
		ops(nb);
		return x;
	}

	public boolean ops(boolean x, int nb) throws LeekRunException {
		ops(nb);
		return x;
	}

	public void addOperationsNoCheck(int nb) {
		// System.out.println("ops " + nb);
		mOperations += nb;
	}

	public void resetCounter() {
		mOperations = 0;
	}

	protected Object nothing(Object obj) throws LeekRunException {
		return null;
	}

	public String getErrorMessage(StackTraceElement[] elements) {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (StackTraceElement element : elements) {
			// System.out.println(element.getClassName() + " " + element.getMethodName() + " " + element.getLineNumber());
			if (element.getClassName().startsWith("AI_")) {
				sb.append(getErrorLocalisation(element.getLineNumber())).append("\n");
				if (count++ > 50) {
					sb.append("[...]");
					break;
				}
			}
		}
		// for (StackTraceElement element : elements) {
		// 	sb.append("\t▶ " + element.getClassName() + "." + element.getMethodName() + ", line " + element.getLineNumber()).append("\n");
		// }
		return sb.toString();
	}

	public String getErrorMessage(Throwable e) {
		return getErrorMessage(e.getStackTrace());
	}

	protected String getErrorLocalisation(int line) {
		if (mErrorObject == null) {
			mErrorObject = new JSONArray();
			var errorString = getErrorString();
			if (errorString != null) {
				// System.out.println("errorString = " + errorString.length);
				for (String error : errorString) {
					mErrorObject.add(JSON.parseArray(error));
				}
			}
			thisObject = getAIString();
		}
		int value = 0;
		for (int i = 0; i < mErrorObject.size(); i++) {
			if (mErrorObject.getJSONArray(i).getInteger(0) > line) {
				break;
			}
			value = i;
		}
		if (mErrorObject.size() > value) {
			JSONArray l = mErrorObject.getJSONArray(value);
			if (l != null && l.size() >= 3) {
				var files = getErrorFiles();
				var f = l.getIntValue(1);
				String file = f < files.length ? files[f] : "?";
				return "\t▶ AI " + file + ", line " + l.getString(2); // + ", java " + line;
			}
		}
		return "";
	}

	public int abs(int x) throws LeekRunException {
		ops(LeekFunctions.abs.getOperations());
		return Math.abs(x);
	}

	public double abs(Number x) throws LeekRunException {
		ops(LeekFunctions.abs.getOperations());
		return Math.abs(x.doubleValue());
	}

	public Object abs(Object... args) throws LeekRunException {
		if (check("abs", new int[] { NUMBER }, args)) {
			ops(LeekFunctions.abs.getOperations());
			if (args[0] instanceof Integer) {
				return Math.abs(((Integer) args[0]).intValue());
			}
			return Math.abs(((Number) args[0]).doubleValue());
		}
		return 0;
	}

	public int ceil(int x) throws LeekRunException {
		ops(LeekFunctions.ceil.getOperations());
		return x;
	}

	public int ceil(double x) throws LeekRunException {
		ops(LeekFunctions.ceil.getOperations());
		return (int) Math.ceil(x);
	}

	public int ceil(Object... args) throws LeekRunException {
		if (check("ceil", new int[] { NUMBER }, args)) {
			ops(LeekFunctions.ceil.getOperations());
			return (int) Math.ceil(((Number) args[0]).doubleValue());
		}
		return 0;
	}

	public int floor(Number x) throws LeekRunException {
		ops(LeekFunctions.floor.getOperations());
		return (int) Math.floor(x.doubleValue());
	}

	public int floor(double x) throws LeekRunException {
		ops(LeekFunctions.floor.getOperations());
		return (int) Math.floor(x);
	}

	public int floor(Object... args) throws LeekRunException {
		if (check("floor", new int[] { NUMBER }, args)) {
			ops(LeekFunctions.floor.getOperations());
			return (int) Math.floor(((Number) args[0]).doubleValue());
		}
		return 0;
	}

	public int round(int x) throws LeekRunException {
		ops(LeekFunctions.round.getOperations());
		return x;
	}

	public int round(double x) throws LeekRunException {
		ops(LeekFunctions.round.getOperations());
		return (int) Math.round(x);
	}

	public int round(Object... args) throws LeekRunException {
		if (check("round", new int[] { NUMBER }, args)) {
			ops(LeekFunctions.round.getOperations());
			return (int) Math.round(((Number) args[0]).doubleValue());
		}
		return 0;
	}

	public double cos(double x) throws LeekRunException {
		ops(LeekFunctions.cos.getOperations());
		return Math.cos(x);
	}

	public double cos(Object... args) throws LeekRunException {
		if (check("cos", new int[] { NUMBER }, args)) {
			ops(LeekFunctions.cos.getOperations());
			return Math.cos(((Number) args[0]).doubleValue());
		}
		return 0;
	}

	public double acos(double x) throws LeekRunException {
		ops(LeekFunctions.acos.getOperations());
		return Math.acos(x);
	}

	public double acos(Object... args) throws LeekRunException {
		if (check("acos", new int[] { NUMBER }, args)) {
			ops(LeekFunctions.acos.getOperations());
			return Math.acos(((Number) args[0]).doubleValue());
		}
		return 0;
	}

	public double sin(double x) throws LeekRunException {
		ops(LeekFunctions.sin.getOperations());
		return Math.sin(x);
	}

	public double sin(Object... args) throws LeekRunException {
		if (check("sin", new int[] { NUMBER }, args)) {
			ops(LeekFunctions.sin.getOperations());
			return Math.sin(((Number) args[0]).doubleValue());
		}
		return 0;
	}

	public double asin(double x) throws LeekRunException {
		ops(LeekFunctions.asin.getOperations());
		return Math.asin(x);
	}

	public double asin(Object... args) throws LeekRunException {
		if (check("asin", new int[] { NUMBER }, args)) {
			ops(LeekFunctions.asin.getOperations());
			return Math.asin(((Number) args[0]).doubleValue());
		}
		return 0;
	}

	public double tan(double x) throws LeekRunException {
		ops(LeekFunctions.tan.getOperations());
		return Math.tan(x);
	}

	public double tan(Object... args) throws LeekRunException {
		if (check("tan", new int[] { NUMBER }, args)) {
			ops(LeekFunctions.tan.getOperations());
			return Math.tan(((Number) args[0]).doubleValue());
		}
		return 0;
	}

	public double atan(double x) throws LeekRunException {
		ops(LeekFunctions.atan.getOperations());
		return Math.atan(x);
	}

	public double atan(Object... args) throws LeekRunException {
		if (check("atan", new int[] { NUMBER }, args)) {
			ops(LeekFunctions.atan.getOperations());
			return Math.atan(((Number) args[0]).doubleValue());
		}
		return 0;
	}

	public int count(ArrayLeekValue array) throws LeekRunException {
		ops(LeekFunctions.count.getOperations());
		return array.size();
	}

	public int count(Object... args) throws LeekRunException {
		if (check("count", new int[] { ARRAY }, args)) {
			ops(LeekFunctions.count.getOperations());
			var array = (ArrayLeekValue) args[0];
			return array.size();
		}
		return 0;
	}

	public Object debug(Object x) throws LeekRunException {
		String p = LeekValueManager.getString(this, x);
		getLogs().addLog(AILog.STANDARD, p);
		ops(p.length());
		ops(LeekFunctions.debug.getOperations());
		return null;
	}

	public Object debugW(Object x) throws LeekRunException {
		String p = LeekValueManager.getString(this, x);
		getLogs().addLog(AILog.WARNING, p);
		ops(p.length());
		ops(LeekFunctions.debug.getOperations());
		return null;
	}

	public Object debugE(Object x) throws LeekRunException {
		String p = LeekValueManager.getString(this, x);
		getLogs().addLog(AILog.ERROR, p);
		ops(p.length());
		ops(LeekFunctions.debug.getOperations());
		return null;
	}

	public int color(Object red, Object green, Object blue) throws LeekRunException {
		int r = integer(red);
		int g = integer(green);
		int b = integer(blue);
		return ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
	}

	public void arrayFlatten(ArrayLeekValue array, ArrayLeekValue retour, int depth) throws LeekRunException {
		for (var value : array) {
			if (value.getValue() instanceof ArrayLeekValue && depth > 0) {
				arrayFlatten((ArrayLeekValue) value.getValue(), retour, depth - 1);
			} else
				retour.push(this, LeekOperations.clone(this, value.getValue()));
		}
	}

	public Object arrayFoldLeft(ArrayLeekValue array, FunctionLeekValue function, Object start_value) throws LeekRunException {
		Object result = LeekOperations.clone(this, start_value);
		for (var value : array) {
			result = function.execute(this, result, value.getValue());
		}
		return result;
	}

	public Object arrayFoldRight(ArrayLeekValue array, FunctionLeekValue function, Object start_value) throws LeekRunException {
		Object result = LeekOperations.clone(this, start_value);
		// Object prev = null;
		var it = array.getReversedIterator();
		while (it.hasNext()) {
			result = function.execute(this, it.next(), result);
		}
		return result;
	}

	public ArrayLeekValue arrayPartition(ArrayLeekValue array, FunctionLeekValue function) throws LeekRunException {
		ArrayLeekValue list1 = new ArrayLeekValue();
		ArrayLeekValue list2 = new ArrayLeekValue();
		int nb = function.getArgumentsCount(this);
		if (nb != 1 && nb != 2)
			return new ArrayLeekValue();
		ArrayIterator iterator = array.getArrayIterator();
		boolean b;
		while (!iterator.ended()) {
			var value = iterator.getValueBox();
			if (nb == 1)
				b = bool(LeekValueManager.execute(this, function, new Object[] { value }));
			else
				b = bool(LeekValueManager.execute(this, function, new Object[] { iterator.getKey(this), value }));
			(b ? list1 : list2).getOrCreate(this, iterator.getKey(this)).set(iterator.getValue(this));
			iterator.next();
		}
		return new ArrayLeekValue(this, new Object[] { list1, list2 }, false);
	}

	public ArrayLeekValue arrayMap(ArrayLeekValue array, FunctionLeekValue function) throws LeekRunException {
		ArrayLeekValue retour = new ArrayLeekValue();
		ArrayIterator iterator = array.getArrayIterator();
		int nb = function.getArgumentsCount(this);
		if (nb != 1 && nb != 2)
			return retour;
		while (!iterator.ended()) {
			var value = iterator.value();
			if (nb == 1)
				retour.getOrCreate(this, iterator.getKey(this)).set(LeekValueManager.execute(this, function, value));
			else
				retour.getOrCreate(this, iterator.getKey(this)).set(LeekValueManager.execute(this, function, iterator.getKey(this), value));
			iterator.next();
		}
		return retour;
	}

	public ArrayLeekValue arrayMapV10(ArrayLeekValue array, FunctionLeekValue function) throws LeekRunException {
		ArrayLeekValue retour = new ArrayLeekValue();
		ArrayIterator iterator = array.getArrayIterator();
		int nb = function.getArgumentsCount(this);
		if (nb != 1 && nb != 2)
			return retour;
		while (!iterator.ended()) {
			var value = iterator.getValueBox();
			if (nb == 1)
				retour.getOrCreate(this, iterator.getKey(this)).setRef(LeekValueManager.execute(this, function, value));
			else
				retour.getOrCreate(this, iterator.getKey(this)).setRef(LeekValueManager.execute(this, function, iterator.getKey(this), value));
			iterator.next();
		}
		return retour;
	}

	public ArrayLeekValue arrayFilterV10(ArrayLeekValue array, FunctionLeekValue function) throws LeekRunException {
		ArrayLeekValue retour = new ArrayLeekValue();
		ArrayIterator iterator = array.getArrayIterator();
		int nb = function.getArgumentsCount(this);
		if (nb != 1 && nb != 2)
			return retour;
		while (!iterator.ended()) {
			var value = iterator.getValueBox();
			if (nb == 1) {
				if (bool(LeekValueManager.execute(this, function, new Object[] { value }))) {
					// In LeekScript < 1.0, arrayFilter had a bug, the result array was not reindexed
					retour.getOrCreate(this, iterator.getKey(this)).set(iterator.getValue(this));
				}
			} else {
				if (bool(LeekValueManager.execute(this, function, new Object[] { iterator.getKey(this), value }))) {
					retour.getOrCreate(this, iterator.getKey(this)).set(iterator.getValue(this));
				}
			}
			iterator.next();
		}
		return retour;
	}

	public ArrayLeekValue arrayFilter(ArrayLeekValue array, FunctionLeekValue function) throws LeekRunException {
		ArrayLeekValue retour = new ArrayLeekValue();
		ArrayIterator iterator = array.getArrayIterator();
		int nb = function.getArgumentsCount(this);
		if (nb != 1 && nb != 2)
			return retour;
		while (!iterator.ended()) {
			var value = iterator.value();
			if (nb == 1) {
				if (bool(LeekValueManager.execute(this, function, new Object[] { value }))) {
					retour.push(this, iterator.getValue(this));
				}
			} else {
				if (bool(LeekValueManager.execute(this, function, new Object[] { iterator.getKey(this), value }))) {
					retour.push(this, iterator.getValue(this));
				}
			}
			iterator.next();
		}
		return retour;
	}

	public Object arrayIter(ArrayLeekValue array, FunctionLeekValue function) throws LeekRunException {
		ArrayIterator iterator = array.getArrayIterator();
		if (function == null) {
			return null;
		}
		int nb = function.getArgumentsCount(this);
		if (nb != 1 && nb != 2)
			return null;
		while (!iterator.ended()) {
			var value = iterator.getValueBox();
			if (nb == 1) {
				function.execute(this, value);
			} else {
				function.execute(this, iterator.getKey(this), value);
			}
			iterator.next();
		}
		return null;
	}

	public ArrayLeekValue arraySort(ArrayLeekValue origin, final FunctionLeekValue function) throws LeekRunException {
		try {
			int nb = function.getArgumentsCount(this);
			if (nb == 2) {
				ArrayLeekValue array = (ArrayLeekValue) LeekOperations.clone(this, origin);
				array.sort(this, new Comparator<PhpArray.Element>() {
					@Override
					public int compare(Element o1, Element o2) {
						try {
							return integer(LeekValueManager.execute(AI.this, function, o1.value(), o2.value()));
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				});
				return array;
			} else if (nb == 4) {
				ArrayLeekValue array = (ArrayLeekValue) LeekOperations.clone(this, origin);
				array.sort(this, new Comparator<PhpArray.Element>() {
					@Override
					public int compare(Element o1, Element o2) {
						try {
							return integer(LeekValueManager.execute(AI.this, function, o1.key(), o1.value(), o2.key(), o2.value()));
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				});
				return array;
			}
		} catch (RuntimeException e) {
			if (e.getCause() instanceof LeekRunException) {
				throw (LeekRunException) e.getCause();
			}
		}
		return null;
	}


	public String jsonEncode(AI ai, Object object) {

		try {

			String json = JSON.toJSONString(toJSON(object));
			ops(json.length() * 10);
			return json;

		} catch (Exception e) {

			getLogs().addLog(AILog.ERROR, "Cannot encode object \"" + object.toString() + "\"");
			try {
				ops(100);
			} catch (Exception e1) {}
			return null;
		}
	}

	public Object jsonDecode(String json) {

		try {

			var obj = LeekValueManager.parseJSON(JSON.parse(json), this);
			ops(json.length() * 10);
			return obj;

		} catch (Exception e) {

			getLogs().addLog(AILog.ERROR, "Cannot parse json \"" + json + "\"");
			try {
				ops(100);
			} catch (Exception e1) {}
			return null;
		}
	}

	public void addSystemLog(int type, Error error) throws LeekRunException {
		addSystemLog(type, error.ordinal(), null);
	}

	public void addSystemLog(int type, int error) throws LeekRunException {
		addSystemLog(type, error, null);
	}

	public void addSystemLog(int type, Error error, String[] parameters) throws LeekRunException {
		addSystemLog(type, error.ordinal(), parameters);
	}

	public void addSystemLog(int type, int error, String[] parameters) throws LeekRunException {
		ops(AI.ERROR_LOG_COST);
		if (type == AILog.WARNING)
			type = AILog.SWARNING;
		else if (type == AILog.ERROR)
			type = AILog.SERROR;
		else if (type == AILog.STANDARD)
			type = AILog.SSTANDARD;

		logs.addSystemLog(type, getErrorMessage(Thread.currentThread().getStackTrace()), error, parameters);
	}

	abstract protected String[] getErrorString();

	protected String[] getErrorFiles() { return null; }

	protected String getAIString() { return ""; }

	public abstract Object runIA() throws LeekRunException;

	public int userFunctionCount(int id) { return 0; }

	public boolean[] userFunctionReference(int id) { return null; }

	public Object userFunctionExecute(int id, Object[] value) throws LeekRunException { return null; }

	public int anonymousFunctionCount(int id) { return 0; }

	public boolean[] anonymousFunctionReference(int id) { return null; }

	public RandomGenerator getRandom() {
		return randomGenerator;
	}

	public int getVersion() { return this.version; }

	public static Object load(Object value) {
		if (value instanceof Box) {
			return ((Box) value).getValue();
		}
		return value;
	}

	public boolean eq(Object x, Object y) throws LeekRunException {
		// ops(1);
		if (x == null) return y == null;
		if (x instanceof Number) {
			var n = ((Number) x).doubleValue();
			if (y instanceof Number) {
				return n == ((Number) y).doubleValue();
			}
			if (y instanceof Boolean) {
				if ((Boolean) y) return n != 0;
				return n == 0;
			}
			if (y instanceof String) {
				if (((String) y).equals("true")) return n != 0;
				if (((String) y).equals("false")) return n == 0;
			}
			if (y instanceof ArrayLeekValue) {
				return ((ArrayLeekValue) y).equals(this, (Number) x);
			}
			return n == getDouble(y);
		}
		if (y instanceof Number) {
			return getDouble(x) == ((Number) y).doubleValue();
		}
		if (x instanceof Boolean) {
			if (y instanceof String) {
				if (((String) y).equals("true")) return ((Boolean) x) == true;
				if (((String) y).equals("false")) return ((Boolean) x) == false;
				return false;
			}
			if (y instanceof ArrayLeekValue) {
				return ((ArrayLeekValue) y).equals(this, (Boolean) x);
			}
		}
		if (x instanceof ArrayLeekValue) {
			return ((ArrayLeekValue) x).equals(this, y);
		}
		if (x instanceof FunctionLeekValue) {
			return ((FunctionLeekValue) x).equals(this, y);
		}
		if (x instanceof String && y instanceof String) {
			ops(Math.min(((String) x).length(), ((String) y).length()));
		}
		return x.equals(y);
	}

	public boolean neq(Object x, Object y) throws LeekRunException {
		if (x == null) return y != null;
		if (y == null) return x != null;
		return !eq(x, y);
	}

	public boolean equals_equals(Object x, Object y) throws LeekRunException {
		if (x == null) return y == null;
		if (x instanceof ObjectLeekValue && y instanceof ObjectLeekValue) {
			return x.equals(y);
		}
		return LeekValueManager.getType(x) == LeekValueManager.getType(y) && eq(x, y);
	}

	public boolean notequals_equals(Object x, Object y) throws LeekRunException {
		return !equals_equals(x, y);
	}

	public boolean less(Object x, Object y) throws LeekRunException {
		if (x instanceof Number && y instanceof Number) {
			if (x instanceof Integer && y instanceof Integer) {
				return (Integer) x < (Integer) y;
			}
			return ((Number) x).doubleValue() < ((Number) y).doubleValue();
		}
		return integer(x) < integer(y);
	}

	public boolean more(Object x, Object y) throws LeekRunException {
		if (x instanceof Number && y instanceof Number) {
			if (x instanceof Integer && y instanceof Integer) {
				return (Integer) x > (Integer) y;
			}
			return ((Number) x).doubleValue() > ((Number) y).doubleValue();
		}
		return integer(x) > integer(y);
	}

	public boolean lessequals(Object x, Object y) throws LeekRunException {
		if (x instanceof Number && y instanceof Number) {
			if (x instanceof Integer && y instanceof Integer) {
				return (Integer) x <= (Integer) y;
			}
			return ((Number) x).doubleValue() <= ((Number) y).doubleValue();
		}
		return integer(x) <= integer(y);
	}

	public boolean moreequals(Object x, Object y) throws LeekRunException {
		if (x instanceof Number && y instanceof Number) {
			if (x instanceof Integer && y instanceof Integer) {
				return (Integer) x >= (Integer) y;
			}
			return ((Number) x).doubleValue() >= ((Number) y).doubleValue();
		}
		return integer(x) >= integer(y);
	}

	public boolean bool(Object value) {
		if (value instanceof Double) {
			return (Double) value != 0;
		} else if (value instanceof Integer) {
			return (Integer) value != 0;
		} else if (value instanceof Boolean) {
			return (Boolean) value;
		} else if (value instanceof ObjectLeekValue) {
			return ((ObjectLeekValue) value).size() != 0;
		} else if (value instanceof ArrayLeekValue) {
			return ((ArrayLeekValue) value).size() != 0;
		} else if (value instanceof String) {
			var s = (String) value;
			if (s.equals("false") || s.equals("0")) {
				return false;
			}
			return !s.isEmpty();
		} else if (value instanceof Box) {
			return bool(((Box) value).getValue());
		// } else if (value instanceof ReferenceLeekValue) {
		// 	return bool(((ReferenceLeekValue) value).getValue());
		}
		return false;
	}

	public int integer(Object value) throws LeekRunException {
		if (value instanceof Double) {
			return (int) (double) value;
		} else if (value instanceof Integer) {
			return (Integer) value;
		} else if (value instanceof Boolean) {
			return ((Boolean) value) ? 1 : 0;
		} else if (value instanceof ObjectLeekValue) {
			return ((ObjectLeekValue) value).size();
		// } else if (value instanceof ArrayLeekValue) {
		// 	return ((ArrayLeekValue) value).size();
		} else if (value instanceof String) {
			var s = (String) value;
			// ops(2);
			if (s.equals("true")) return 1;
			if (s.equals("false")) return 0;
			if (s.isEmpty()) return 0;
			ops(s.length());
			try {
				return Integer.parseInt(s);
			} catch (Exception e) {
				return 1;
			}
		} else if (value instanceof Box) {
			return integer(((Box) value).getValue());
		}
		return 0;
	}

	public double getDouble(Object value) throws LeekRunException {
		return LeekValueManager.getDouble(this, value);
	}

	public boolean not(Object value) throws LeekRunException {
		return !bool(value);
	}

	public Object minus(Object value) throws LeekRunException {
		if (value instanceof Integer) return -((Integer) value);
		if (value instanceof Double) return -((Double) value);
		return -integer(value);
	}

	public int bnot(Object value) throws LeekRunException {
		return LeekValueManager.bnot(this, value);
	}

	public int bor(Object x, Object y) throws LeekRunException {
		return integer(x) | integer(y);
	}

	public int band(Object x, Object y) throws LeekRunException {
		return integer(x) & integer(y);
	}

	public int bxor(Object x, Object y) throws LeekRunException {
		return integer(x) ^ integer(y);
	}

	public int shl(Object x, Object y) throws LeekRunException {
		return integer(x) << integer(y);
	}

	public int shr(Object x, Object y) throws LeekRunException {
		return integer(x) >> integer(y);
	}

	public int ushr(Object x, Object y) throws LeekRunException {
		return integer(x) >>> integer(y);
	}

	public Object add(Object v1, Object v2) throws LeekRunException {
		if (v1 instanceof Number) {
			if (v2 instanceof Number) {
				if (v1 instanceof Double) return (Double) v1 + ((Number) v2).doubleValue();
				if (v2 instanceof Double) return (Double) v2 + ((Number) v1).doubleValue();
				return ((Number) v1).intValue() + ((Number) v2).intValue();
			}
			if (v2 instanceof Boolean) {
				if (v1 instanceof Integer) {
					return ((Integer) v1) + (((Boolean) v2) ? 1 : 0);
				}
				return ((Number) v1).doubleValue() + (((Boolean) v2) ? 1 : 0);
			}
			if (v2 == null) return v1;
		}

		// Concatenate arrays
		if (v1 instanceof ArrayLeekValue && v2 instanceof ArrayLeekValue) {

			var array1 = (ArrayLeekValue) v1;
			var array2 = (ArrayLeekValue) v2;

			ops((array1.size() + array2.size()) * 2);

			ArrayLeekValue retour = new ArrayLeekValue();
			ArrayIterator iterator = array1.getArrayIterator();

			while (!iterator.ended()) {
				if (iterator.key() instanceof String) {
					retour.getOrCreate(this, iterator.getKey(this)).set(iterator.getValue(this));
				} else {
					retour.push(this, iterator.getValue(this));
				}
				iterator.next();
			}
			iterator = array2.getArrayIterator();
			while (!iterator.ended()) {
				if (iterator.key() instanceof String) {
					retour.getOrCreate(this, iterator.getKey(this)).set(iterator.getValue(this));
				} else {
					retour.push(this, iterator.getValue(this));
				}
				iterator.next();
			}
			return retour;
		}

		if (v1 == null) {
			if (v2 instanceof Number) {
				return v2;
			}
			if (v2 == null) return 0;
		}

		String v1_string = LeekValueManager.getString(this, v1);
		String v2_string = LeekValueManager.getString(this, v2);
		ops(v1_string.length() + v2_string.length());
		return v1_string + v2_string;
	}

	public Object sub(Object v1, Object v2) throws LeekRunException {
		if (v1 instanceof Number) {
			if (v2 instanceof Number) {
				if (v1 instanceof Double) return (Double) v1 - ((Number) v2).doubleValue();
				if (v2 instanceof Double) return ((Number) v1).doubleValue() - (Double) v2;
				return ((Number) v1).intValue() - ((Number) v2).intValue();
			}
			if (v1 instanceof Integer) {
				return ((Integer) v1).intValue() - integer(v2);
			}
			if (v2 == null) return v1;
			return ((Number) v1).doubleValue() - integer(v2);
		}
		if (v1 == null) {
			if (v2 instanceof Integer) return -(Integer) v2;
			if (v2 instanceof Double) return -(Double) v2;
			if (v2 == null) return 0;
		}
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public Object mul(Object v1, Object v2) throws LeekRunException {
		if (v1 instanceof Number) {
			if (v2 instanceof Number) {
				if (v1 instanceof Double || v2 instanceof Double) {
					return ((Number) v1).doubleValue() * ((Number) v2).doubleValue();
				} else {
					return ((Number) v1).intValue() * ((Number) v2).intValue();
				}
			}
			if (v2 instanceof Boolean) {
				if (v1 instanceof Integer) {
					return ((Integer) v1) * (((Boolean) v2) ? 1 : 0);
				}
				return ((Number) v1).doubleValue() * (((Boolean) v2) ? 1 : 0);
			}
			if (v2 == null) {
				return 0;
			}
		}
		if (v1 == null) return 0;
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public Object div(Object x, Object y) throws LeekRunException {
		if (x == null) return 0.0;
		if (y == null) {
			addSystemLog(AILog.ERROR, Error.DIVISION_BY_ZERO);
			return null;
		}
		if (x instanceof Number) {
			if (y instanceof Number) {
				if (((Number) y).doubleValue() == 0) {
					addSystemLog(AILog.ERROR, Error.DIVISION_BY_ZERO);
					return null;
				}
				return ((Number) x).doubleValue() / ((Number) y).doubleValue();
			}
			if (y instanceof Boolean) {
				if ((Boolean) y) return ((Number) x).doubleValue();
				addSystemLog(AILog.ERROR, Error.DIVISION_BY_ZERO);
			}
		}
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public Object mod(Object x, Object y) throws LeekRunException {
		if (x == null) return 0;

		if (x instanceof Number && y instanceof Number) {
			if (((Number) y).doubleValue() == 0) {
				addSystemLog(AILog.ERROR, Error.DIVISION_BY_ZERO);
				return null;
			}
			if (x instanceof Integer && y instanceof Integer) {
				return ((Integer) x) % ((Integer) y);
			}
			return ((Number) x).doubleValue() % ((Number) y).doubleValue();
		}
		if (x instanceof Boolean) {
			if (((Number) y).doubleValue() == 0) {
				addSystemLog(AILog.ERROR, Error.DIVISION_BY_ZERO);
				return null;
			}
			if ((Boolean) x) return 1;
			return 0;
		}
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public Number pow(Object x, Object y) throws LeekRunException {
		if (x instanceof Number) {
			if (y instanceof Number) {
				if (x instanceof Integer && y instanceof Integer) {
					return (int) Math.pow((Integer) x, (Integer) y);
				}
				return Math.pow(((Number) x).doubleValue(), ((Number) y).doubleValue());
			}
			if (y instanceof Boolean) {
				if (((Boolean) y) == true) return (Number) x;
				return 1;
			}
			if (y == null) {
				return 1;
			}
		}
		if (x == null) {
			if (y == null) {
				return 1;
			}
			return 0;
		}
		throw new LeekRunException(LeekRunException.INVALID_OPERATOR);
	}

	public Object add_eq(Object x, Object y) throws LeekRunException {
		if (x instanceof ArrayLeekValue) {
			return ((ArrayLeekValue) x).add_eq(this, y);
		}
		return add(x, y);
	}
	public Object sub_eq(Object x, Object y) throws LeekRunException {
		if (x instanceof Box) {
			return ((Box) x).sub_eq(y);
		}
		return null;
	}

	public Object mul_eq(Object x, Object y) throws LeekRunException {
		if (x instanceof Box) {
			return ((Box) x).mul_eq(y);
		}
		return null;
	}

	public Object div_eq(Object x, Object y) throws LeekRunException {
		if (x instanceof Box) {
			return ((Box) x).div_eq(y);
		}
		return null;
	}

	public Object mod_eq(Object x, Object y) throws LeekRunException {
		if (x instanceof Box) {
			return ((Box) x).mod_eq(y);
		}
		return null;
	}

	public int bor_eq(Object x, Object y) throws LeekRunException {
		if (x instanceof Box) {
			return ((Box) x).bor_eq(y);
		}
		return 0;
	}

	public int band_eq(Object x, Object y) throws LeekRunException {
		if (x instanceof Box) {
			return ((Box) x).band_eq(y);
		}
		return 0;
	}

	public int bxor_eq(Object x, Object y) throws LeekRunException {
		if (x instanceof Box) {
			return ((Box) x).bxor_eq(y);
		}
		return 0;
	}

	public Object increment(Object x) throws LeekRunException {
		if (x instanceof Box) {
			return ((Box) x).increment();
		}
		return null;
	}

	public int intOrNull(Object value) throws LeekRunException {
		if (value == null)
			return -1;
		return integer(value);
	}

	public Object copy(Object value) throws LeekRunException {
		return LeekOperations.clone(this, value);
	}

	public String string(Object value) throws LeekRunException {
		return LeekValueManager.getString(this, value);
	}

	public String getString(Object value, Set<Object> visited) throws LeekRunException {
		return LeekValueManager.getString(this, value, visited);
	}
	public String toJSON(Object value) {
		return LeekValueManager.toJSON(this, value);
	}

	public boolean isPrimitive(Object value) {
		return !(value instanceof ArrayLeekValue || value instanceof ObjectLeekValue);
	}
	public boolean isIterable(Object value) {
		return value instanceof ArrayLeekValue;
	}
	public boolean getBooleanTernary(Object value) throws LeekRunException {
		ops(1);
		return bool(value);
	}

	public Object getField(Object value, String field, ClassLeekValue fromClass) throws LeekRunException {
		if (value instanceof ObjectLeekValue) {
			return ((ObjectLeekValue) value).getField(field, fromClass);
		}
		if (value instanceof ClassLeekValue) {
			return ((ClassLeekValue) value).getField(this, field, fromClass);
		}
		throw new LeekRunException(LeekRunException.UNKNOWN_FIELD);
	}

	public Object setField(Object object, String field, Object value) throws LeekRunException {
		if (object instanceof ObjectLeekValue) {
			return ((ObjectLeekValue) object).setField(field, value);
		}
		if (object instanceof ClassLeekValue) {
			return ((ClassLeekValue) object).setField(field, value);
		}
		throw new LeekRunException(LeekRunException.UNKNOWN_FIELD);
	}

	public Object field_inc(Object object, String field) throws LeekRunException {
		if (object instanceof ObjectLeekValue) {
			return ((ObjectLeekValue) object).field_inc(field);
		}
		if (object instanceof ClassLeekValue) {
			// return ((ClassLeekValue) object).field_add_eq(field, value);
		}
		throw new LeekRunException(LeekRunException.UNKNOWN_FIELD);
	}

	public Object field_add_eq(Object object, String field, Object value) throws LeekRunException {
		if (object instanceof ObjectLeekValue) {
			return ((ObjectLeekValue) object).field_add_eq(field, value);
		}
		if (object instanceof ClassLeekValue) {
			// return ((ClassLeekValue) object).field_add_eq(field, value);
		}
		throw new LeekRunException(LeekRunException.UNKNOWN_FIELD);
	}

	public Object field_sub_eq(Object object, String field, Object value) throws LeekRunException {
		if (object instanceof ObjectLeekValue) {
			return ((ObjectLeekValue) object).field_sub_eq(field, value);
		}
		if (object instanceof ClassLeekValue) {
			// return ((ClassLeekValue) object).field_add_eq(field, value);
		}
		throw new LeekRunException(LeekRunException.UNKNOWN_FIELD);
	}

	public Object field_mul_eq(Object object, String field, Object value) throws LeekRunException {
		if (object instanceof ObjectLeekValue) {
			return ((ObjectLeekValue) object).field_mul_eq(field, value);
		}
		if (object instanceof ClassLeekValue) {
			// return ((ClassLeekValue) object).field_add_eq(field, value);
		}
		throw new LeekRunException(LeekRunException.UNKNOWN_FIELD);
	}

	public Object field_bor_eq(Object object, String field, Object value) throws LeekRunException {
		if (object instanceof ObjectLeekValue) {
			return ((ObjectLeekValue) object).field_bor_eq(field, value);
		}
		if (object instanceof ClassLeekValue) {
			// return ((ClassLeekValue) object).field_add_eq(field, value);
		}
		throw new LeekRunException(LeekRunException.UNKNOWN_FIELD);
	}

	public Object put(Object array, Object key, Object value) throws LeekRunException {
		if (array instanceof ArrayLeekValue) {
			return ((ArrayLeekValue) array).put(this, key, value);
		}
		if (array instanceof ClassLeekValue) {
			var field = string(key);
			return ((ClassLeekValue) array).setField(field, value);
		}
		return null;
	}

	public Object put_inc(Object array, Object key) throws LeekRunException {
		if (array instanceof ArrayLeekValue) {
			return ((ArrayLeekValue) array).put_inc(this, key);
		}
		if (array instanceof ClassLeekValue) {
			// var field = string(key);
			// return ((ClassLeekValue) array).getField(field), value);
		}
		return null;
	}

	public Object put_dec(Object array, Object key) throws LeekRunException {
		if (array instanceof ArrayLeekValue) {
			return ((ArrayLeekValue) array).put_dec(this, key);
		}
		if (array instanceof ClassLeekValue) {
			// var field = string(key);
			// return ((ClassLeekValue) array).getField(field), value);
		}
		return null;
	}

	public Object put_add_eq(Object array, Object key, Object value) throws LeekRunException {
		if (array instanceof ArrayLeekValue) {
			return ((ArrayLeekValue) array).put_add_eq(this, key, value);
		}
		if (array instanceof ClassLeekValue) {
			// var field = string(key);
			// return ((ClassLeekValue) array).getField(field), value);
		}
		return null;
	}

	public Object put_sub_eq(Object array, Object key, Object value) throws LeekRunException {
		if (array instanceof ArrayLeekValue) {
			return ((ArrayLeekValue) array).put_sub_eq(this, key, value);
		}
		if (array instanceof ClassLeekValue) {
			// var field = string(key);
			// return ((ClassLeekValue) array).getField(field), value);
		}
		return null;
	}
	public Object put_mul_eq(Object array, Object key, Object value) throws LeekRunException {
		if (array instanceof ArrayLeekValue) {
			return ((ArrayLeekValue) array).put_mul_eq(this, key, value);
		}
		if (array instanceof ClassLeekValue) {
			// var field = string(key);
			// return ((ClassLeekValue) array).getField(field), value);
		}
		return null;
	}
	public Object put_div_eq(Object array, Object key, Object value) throws LeekRunException {
		if (array instanceof ArrayLeekValue) {
			return ((ArrayLeekValue) array).put_div_eq(this, key, value);
		}
		if (array instanceof ClassLeekValue) {
			// var field = string(key);
			// return ((ClassLeekValue) array).getField(field), value);
		}
		return null;
	}


	public Object put_bor_eq(Object array, Object key, Object value) throws LeekRunException {
		if (array instanceof ArrayLeekValue) {
			return ((ArrayLeekValue) array).put_bor_eq(this, key, value);
		}
		if (array instanceof ClassLeekValue) {
			// var field = string(key);
			// return ((ClassLeekValue) array).getField(field), value);
		}
		return null;
	}


	public Object put_band_eq(Object array, Object key, Object value) throws LeekRunException {
		if (array instanceof ArrayLeekValue) {
			return ((ArrayLeekValue) array).put_band_eq(this, key, value);
		}
		if (array instanceof ClassLeekValue) {
			// var field = string(key);
			// return ((ClassLeekValue) array).getField(field), value);
		}
		return null;
	}

	public Object put_shl_eq(Object array, Object key, Object value) throws LeekRunException {
		if (array instanceof ArrayLeekValue) {
			return ((ArrayLeekValue) array).put_shl_eq(this, key, value);
		}
		if (array instanceof ClassLeekValue) {
			// var field = string(key);
			// return ((ClassLeekValue) array).getField(field), value);
		}
		return null;
	}

	public Object put_shr_eq(Object array, Object key, Object value) throws LeekRunException {
		if (array instanceof ArrayLeekValue) {
			return ((ArrayLeekValue) array).put_shr_eq(this, key, value);
		}
		if (array instanceof ClassLeekValue) {
			// var field = string(key);
			// return ((ClassLeekValue) array).getField(field), value);
		}
		return null;
	}

	public Object put_ushr_eq(Object array, Object key, Object value) throws LeekRunException {
		if (array instanceof ArrayLeekValue) {
			return ((ArrayLeekValue) array).put_ushr_eq(this, key, value);
		}
		if (array instanceof ClassLeekValue) {
			// var field = string(key);
			// return ((ClassLeekValue) array).getField(field), value);
		}
		return null;
	}

	public Object put_bxor_eq(Object array, Object key, Object value) throws LeekRunException {
		if (array instanceof ArrayLeekValue) {
			return ((ArrayLeekValue) array).put_bxor_eq(this, key, value);
		}
		if (array instanceof ClassLeekValue) {
			// var field = string(key);
			// return ((ClassLeekValue) array).getField(field), value);
		}
		return null;
	}

	public Object set(Object variable, Object value) throws LeekRunException {
		if (variable instanceof Box) {
			return ((Box) variable).set(value);
		}
		return null;
	}

	public Object get(Object value, Object index) throws LeekRunException {
		if (value instanceof ArrayLeekValue) {
			return ((ArrayLeekValue) value).get(this, index);
		}
		return null;
	}

	public Box getBox(Object value, Object index) throws LeekRunException {
		if (value instanceof ArrayLeekValue) {
			return ((ArrayLeekValue) value).getBox(this, index);
		}
		// return new Box(this, null);
		return null;
	}

	public Object callMethod(Object value, String method, ClassLeekValue fromClass, Object... args) throws LeekRunException {
		if (value instanceof ObjectLeekValue) {
			return ((ObjectLeekValue) value).callMethod(method, fromClass, args);
		}
		// if (value instanceof ClassLeekValue) {
		// 	return ((ClassLeekValue) value).callMethod(method, args);
		// }
		return null;
	}

	public Object execute(Object function, Object... args) throws LeekRunException {
		if (function instanceof FunctionLeekValue) {
			return ((FunctionLeekValue) function).execute(this, args);
		}
		if (function instanceof ClassLeekValue) {
			return ((ClassLeekValue) function).execute(args);
		}
		addSystemLog(AILog.ERROR, Error.CAN_NOT_EXECUTE_VALUE, new String[] { string(function) });
		return null;
	}


	public Object sysexec(ILeekFunction function, Object... arguments) throws LeekRunException {
		// Vérification parametres
		int[] parameters = function.getParameters();
		if (parameters == null || verifyParameters(parameters, arguments)) {
			Object retour = function.run(this, function, arguments);
			function.addOperations(this, function, arguments, retour);
			return retour;
		} else {
			// Message d'erreur
			String ret = LeekValue.getParamString(arguments);
			addSystemLog(AILog.ERROR, Error.UNKNOWN_FUNCTION, new String[] { function + "(" + ret + ")" });
			return null;
		}
	}

	public boolean check(String functionName, int[] types, Object... arguments) throws LeekRunException {
		if (verifyParameters(types, arguments)) {
			return true;
		}
		String ret = LeekValue.getParamString(arguments);
		addSystemLog(AILog.ERROR, Error.UNKNOWN_FUNCTION, new String[] { functionName + "(" + ret + ")" });
		return false;
	}

	public static boolean verifyParameters(int[] types, Object... parameters) {
		if (types.length != parameters.length) return false;
		for (int i = 0; i < types.length; i++) {
			if (types[i] == -1) continue;
			if (i >= parameters.length || !isType(parameters[i], types[i])) {
				return false;
			}
		}
		return true;
	}

	public static boolean isType(Object value, int type) {
		// value = LeekValueManager.getValue(value);
		switch (type) {
			case BOOLEAN: return value instanceof Boolean;
			case INT: return value instanceof Integer;
			case DOUBLE: return value instanceof Double;
			case STRING: return value instanceof String;
			case NULL: return value == null;
			case ARRAY: return value instanceof ArrayLeekValue;
			case FUNCTION: return value instanceof FunctionLeekValue;
			case NUMBER: return value instanceof Integer || value instanceof Double;
		}
		return true;
	}

	public boolean instanceOf(Object value, Object clazz) throws LeekRunException {
		ops(2);
		clazz = LeekValueManager.getValue(clazz);
		if (!(clazz instanceof ClassLeekValue)) {
			addSystemLog(AILog.ERROR, Error.INSTANCEOF_MUST_BE_CLASS);
			return false;
		}
		var v = load(value);
		if (v instanceof ObjectLeekValue && ((ObjectLeekValue) v).getClazz().descendsFrom((ClassLeekValue) clazz)) {
			return true;
		}
		return false;
	}

	public long getAnalyzeTime() {
		return analyzeTime;
	}

	public long getCompileTime() {
		return compileTime;
	}

	public void setAnalyzeTime(long analyze_time) {
		this.analyzeTime = analyze_time;
	}

	public void setCompileTime(long compile_time) {
		this.compileTime = compile_time;
	}

	public void setLoadTime(long load_time) {
		this.loadTime = load_time;
	}

	public long getLoadTime() {
		return loadTime;
	}
}
