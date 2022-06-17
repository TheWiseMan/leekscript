package leekscript.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import com.alibaba.fastjson.JSON;

import leekscript.compiler.bloc.AbstractLeekBlock;
import leekscript.common.Type;
import leekscript.compiler.bloc.MainLeekBlock;
import leekscript.compiler.expression.AbstractExpression;
import leekscript.runner.CallableVersion;
import leekscript.runner.LeekFunctions;

public class JavaWriter {

	private final StringBuilder mCode;
	private final StringBuilder mLinesFile;
	private int mLine;
	private final TreeMap<Integer, LineMapping> mLines = new TreeMap<>();
	private final HashMap<AIFile<?>, Integer> mFiles = new HashMap<>();
	private final ArrayList<AIFile<?>> mFilesList = new ArrayList<>();
	private final boolean mWithDebug;
	private final String className;
	public AbstractLeekBlock currentBlock = null;
	public HashMap<String, CallableVersion> genericFunctions = new HashMap<>();
	public HashSet<LeekFunctions> anonymousSystemFunctions = new HashSet<>();

	public JavaWriter(boolean debug, String className) {
		mCode = new StringBuilder();
		mLinesFile = new StringBuilder();
		mLine = 1;
		mWithDebug = debug;
		this.className = className;
	}

	public boolean hasDebug() {
		return mWithDebug;
	}

	public void addLine(String datas, int line, AIFile<?> ai) {
		mCode.append(datas).append("\n");
		int fileIndex = getFileIndex(ai);
		mLines.put(mLine, new LineMapping(line, fileIndex));
		mLine++;
	}

	private int getFileIndex(AIFile<?> ai) {
		var index = mFiles.get(ai);
		if (index != null) return index;
		var new_index = mFiles.size();
		mFiles.put(ai, new_index);
		mFilesList.add(ai);
		return new_index;
	}

	public void addLine(String datas) {
		mCode.append(datas).append("\n");
		mLine++;
	}

	public void addLine() {
		mCode.append("\n");
		mLine++;
	}

	public void addCode(String datas) {
		mCode.append(datas);
	}

	public AICode getCode() {
		return new AICode(mCode.toString(), mLinesFile.toString());
	}

	public void writeErrorFunction(IACompiler comp, String ai) {
		String aiJson = JSON.toJSONString(ai);
		for (var e : mLines.entrySet()) {
			var line = e.getValue();
			mLinesFile.append(e.getKey() + " " + line.getAI() + " " + line.getLeekScriptLine() + "\n");
			// System.out.println(l.mAI.getPath() + ":" + l.mCodeLine + " -> " + l.mJavaLine);
		}
		mCode.append("protected String getAIString() { return ");
		mCode.append(aiJson);
		mCode.append(";}\n");

		mCode.append("protected String[] getErrorFiles() { return new String[] {");
		for (var f : mFilesList) {
			mCode.append("\"" + f.getPath().replaceAll("\\\\/", "/").replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"") + "\"");
			mCode.append(", ");
		}
		mCode.append("};}\n\n");
	}

	public void addCounter(int count) {
		addCode("ops(" + count + ");");
	}

	public int getCurrentLine() {
		return mLine;
	}

	public void addPosition(IAWord token) {
		var index = getFileIndex(token.getAI());
		mLines.put(mLine, new LineMapping(token.getLine(), index));
	}

	public String getAIThis() {
		return className + ".this";
	}

	public String getClassName() {
		return className;
	}

	public void getBoolean(MainLeekBlock mainblock, AbstractExpression expression) {
		if (expression.getType() == Type.BOOL) {
			expression.writeJavaCode(mainblock, this);
		} else if (expression.getType() == Type.INT) {
			addCode("((");
			expression.writeJavaCode(mainblock, this);
			addCode(") != 0)");
		} else {
			addCode("bool(");
			expression.writeJavaCode(mainblock, this);
			addCode(")");
		}
	}

	public void getString(MainLeekBlock mainblock, AbstractExpression expression) {
		if (expression.getType() == Type.STRING) {
			expression.writeJavaCode(mainblock, this);
		} else {
			addCode("string(");
			expression.writeJavaCode(mainblock, this);
			addCode(")");
		}
	}

	public void getInt(MainLeekBlock mainblock, AbstractExpression expression) {
		if (expression.getType() == Type.INT) {
			expression.writeJavaCode(mainblock, this);
		} else {
			addCode("longint(");
			expression.writeJavaCode(mainblock, this);
			addCode(")");
		}
	}

	public void compileLoad(MainLeekBlock mainblock, AbstractExpression expr) {
		if (expr.getType() == Type.NULL || expr.getType() == Type.BOOL || expr.getType().isNumber() || expr.getType() == Type.STRING || expr.getType() == Type.ARRAY) {
			expr.writeJavaCode(mainblock, this);
		} else {
			addCode("load(");
			expr.writeJavaCode(mainblock, this);
			addCode(")");
		}
	}

	public void compileClone(MainLeekBlock mainblock, AbstractExpression expr) {
		if (expr.getType() == Type.NULL || expr.getType() == Type.BOOL || expr.getType().isNumber() || expr.getType() == Type.STRING) {
			expr.writeJavaCode(mainblock, this);
		} else {
			addCode("copy(");
			expr.writeJavaCode(mainblock, this);
			addCode(")");
		}
	}

	public void compileConvert(MainLeekBlock mainblock, AbstractExpression value, Type type) {
		// var v_type = value.getType();
		// System.out.println("convert " + v_type + " to " + type);
		if (type == Type.ARRAY) {
			addCode(mainblock.getVersion() >= 4 ? "toArray(" : "toLegacyArray(");
			value.writeJavaCode(mainblock, this);
			addCode(")");
			return;
		}
		if (type == Type.INT) {
			if (value.getType() == Type.REAL) {
				addCode("(long) (");
				value.writeJavaCode(mainblock, this);
				addCode(")");
				return;
			}
		}
		value.writeJavaCode(mainblock, this);
	}

	public void generateGenericFunction(CallableVersion system_function) {
		genericFunctions.put(system_function.function.getName() + "_" + system_function.arguments.length, system_function);
	}

	public void generateAnonymousSystemFunction(LeekFunctions system_function) {
		anonymousSystemFunctions.add(system_function);
		for (var version : system_function.getVersions()) {
			genericFunctions.put(system_function.getName() + "_" + version.arguments.length, version);
		}
	}

	public void writeGenericFunctions(MainLeekBlock block) {

		for (var version : genericFunctions.values()) {
			addCode("private " + version.return_type.getJavaName(block.getVersion()) + " " + version.function.getStandardClass() + "_" + version.function.getName() + "_" + version.arguments.length + "(");
			for (int a = 0; a < version.arguments.length; ++a) {
				if (a > 0) addCode(", ");
				addCode("Object a" + a);
			}
			addLine(") throws LeekRunException {");
			int a = 0;
			for (var argument : version.arguments) {
				if (argument != Type.ANY) {
					addLine(argument.getJavaName(block.getVersion()) + " x" + a + "; try { x" + a + " = " + convert("a" + a, argument, block.getVersion()) + "; } catch (ClassCastException e) { return " + version.return_type.getDefaultValue(block.getVersion()) + "; }");
				}
				a++;
			}
			// if (function.getOperations() >= 0) {
			// 	addCode("return x0." + function.toString() + "(");
			// } else {
			if (version.function.isStatic()) {
				var function_name = version.function.getName();
				if (version.return_type == Type.ARRAY && block.getVersion() <= 3) {
					function_name += "_v1_3";
				}
				addCode("return " + version.function.getStandardClass() + "Class." + function_name + "(");
			} else {
				addCode("return x0." + version.function.getName() + "(");
			}
			ArrayList<String> args = new ArrayList<>();
			args.add("this");
			int start_index = version.function.isStatic() ? 0 : 1;
			for (a = start_index; a < version.arguments.length; ++a) {
				if (version.arguments[a] != Type.ANY) {
					args.add("x" + a);
				} else {
					args.add("a" + a);
				}
			}
			addCode(String.join(", ", args));
			addLine(");");
			addLine("}");
			addLine();
		}
	}

	public void writeAnonymousSystemFunctions(MainLeekBlock block) {

		for (var function : anonymousSystemFunctions) {
			addLine("private FunctionLeekValue " + function.getStandardClass() + "_" + function.getName() + " = new FunctionLeekValue(" + function.getVersions()[0].arguments.length + ", \"#Function " + function.getName() + "\") { public Object run(AI ai, ObjectLeekValue thiz, Object... values) throws LeekRunException {");
			if (function.getOperations() >= 0) {
				addLine("ops(" + function.getOperations() + ");");
			}
			if (function.getVersions().length > 1) {
				for (var version : function.getVersions()) {
					addCode("if (values.length == " + version.arguments.length + ") return " + function.getStandardClass() + "_" + function.getName() + "_" + version.arguments.length + "(");
					for (var a = 0; a < version.arguments.length; ++a) {
						if (a > 0) addCode(", ");
						if (block.getVersion() == 1) {
							addCode("load(values[" + a + "])");
						} else {
							addCode("values[" + a + "]");
						}
					}
					addLine(");");
				}
			}
			addCode("return " + function.getStandardClass() + "_" + function.getName() + "_" + function.getVersions()[0].arguments.length + "(");
			for (var a = 0; a < function.getVersions()[0].arguments.length; ++a) {
				if (a > 0) addCode(", ");
				if (block.getVersion() == 1) {
					addCode("load(values[" + a + "])");
				} else {
					addCode("values[" + a + "]");
				}
			}
			addLine(");");
			addLine("}};");
			addLine();
		}
	}

	private String convert(String v, Type type, int version) {
		if (type == Type.ARRAY) {
			if (version >= 4) return "toArray(" + v + ")";
			else return "toLegacyArray(" + v + ")";
		}
		if (type == Type.MAP) {
			return "toMap(" + v + ")";
		}
		if (type == Type.FUNCTION) {
			return "toFunction(" + v + ")";
		}
		if (type == Type.INT) {
			return "longint(" + v + ")";
		}
		if (type == Type.REAL) {
			return "real(" + v + ")";
		}
		if (type == Type.STRING) {
			return "string(" + v + ")";
		}
		return v;
	}
}
