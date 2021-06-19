package leekscript.compiler.instruction;

import java.util.HashMap;
import java.util.Map.Entry;

import leekscript.compiler.AIFile;
import leekscript.compiler.AnalyzeError;
import leekscript.compiler.IAWord;
import leekscript.compiler.JavaWriter;
import leekscript.compiler.WordCompiler;
import leekscript.compiler.AnalyzeError.AnalyzeErrorLevel;
import leekscript.compiler.bloc.ClassMethodBlock;
import leekscript.compiler.bloc.MainLeekBlock;
import leekscript.compiler.exceptions.LeekCompilerException;
import leekscript.compiler.expression.AbstractExpression;
import leekscript.compiler.expression.LeekVariable;
import leekscript.compiler.expression.LeekVariable.VariableType;
import leekscript.common.Error;

public class ClassDeclarationInstruction implements LeekInstruction {

	private final IAWord token;
	private IAWord parentToken;
	private ClassDeclarationInstruction parent;
	private HashMap<String, AbstractExpression> fields = new HashMap<>();
	private HashMap<String, AbstractExpression> staticFields = new HashMap<>();
	private HashMap<String, LeekVariable> fieldVariables = new HashMap<>();
	private HashMap<String, LeekVariable> staticFieldVariables = new HashMap<>();
	private HashMap<String, LeekVariable> methodVariables = new HashMap<>();
	private HashMap<String, LeekVariable> staticMethodVariables = new HashMap<>();
	private HashMap<Integer, ClassMethodBlock> constructors = new HashMap<>();
	private HashMap<String, HashMap<Integer, ClassMethodBlock>> methods = new HashMap<>();
	private HashMap<String, HashMap<Integer, ClassMethodBlock>> staticMethods = new HashMap<>();

	public ClassDeclarationInstruction(IAWord token, int line, AIFile<?> ai) {
		this.token = token;
	}

	public HashMap<String, AbstractExpression> getFields() {
		return fields;
	}
	public HashMap<String, AbstractExpression> getStaticFields() {
		return staticFields;
	}

	public HashMap<String, LeekVariable> getFieldVariables() {
		return fieldVariables;
	}
	public HashMap<String, LeekVariable> getMethodVariables() {
		return methodVariables;
	}
	public HashMap<String, LeekVariable> getStaticFieldVariables() {
		return staticFieldVariables;
	}
	public HashMap<String, LeekVariable> getStaticMethodVariables() {
		return staticMethodVariables;
	}

	public String getName() {
		return token.getWord();
	}

	@Override
	public String getCode() {
		String r = "class " + token.getWord();
		if (parentToken != null) {
			r += " extends " + parentToken.getWord();
		}
		r += " {\n";

		for (Entry<String, AbstractExpression> field : staticFields.entrySet()) {
			r += "\tstatic " + field.getKey();
			if (field.getValue() != null) {
				r += " = " + field.getValue().getString();
			}
			r += "\n";
		}
		r += "\n";

		for (var method : staticMethods.entrySet()) {
			for (var version : method.getValue().entrySet()) {
				r += "\tstatic " + method.getKey() + version.getValue().getCode();
			}
			r += "\n";
		}
		r += "\n";

		for (Entry<String, AbstractExpression> field : fields.entrySet()) {
			r += "\t" + field.getKey();
			if (field.getValue() != null) {
				r += " = " + field.getValue().getString();
			}
			r += "\n";
		}
		r += "\n";

		for (var constructor : constructors.entrySet()) {
			r += "\tconstructor" + constructor.getValue().getCode();
		}
		r += "\n";

		for (var method : methods.entrySet()) {
			for (var version : method.getValue().entrySet()) {
				r += "\t" + method.getKey() + version.getValue().getCode();
			}
			r += "\n";
		}

		r += "}";
		return r;
	}

	@Override
	public int getEndBlock() {
		return 0;
	}

	@Override
	public boolean putCounterBefore() {
		return false;
	}

	public void setParent(IAWord userClass) {
		this.parentToken = userClass;
	}

	public boolean hasConstructor(int param_count) {
		return constructors.containsKey(param_count);
	}

	public void addConstructor(ClassMethodBlock block) {
		constructors.put(block.countParameters(), block);
	}

	public void addMethod(WordCompiler compiler, IAWord token, ClassMethodBlock method) {
		// On regarde si il n'y a pas déjà une méthode statique du même nom
		if (staticMethods.containsKey(token.getWord())) {
			compiler.addError(new AnalyzeError(token, AnalyzeErrorLevel.ERROR, Error.DUPLICATED_METHOD));
		}
		if (!methods.containsKey(token.getWord())) {
			methods.put(token.getWord(), new HashMap<>());
			methodVariables.put(token.getWord(), new LeekVariable(token, VariableType.METHOD));
		}
		methods.get(token.getWord()).put(method.countParameters(), method);
	}

	public boolean hasMethod(String name, int paramCount) {
		if (name.equals("constructor")) {
			return hasConstructor(paramCount);
		}
		return methods.containsKey(name + "_" + paramCount);
	}

	public void addStaticMethod(WordCompiler compiler, IAWord token, ClassMethodBlock method) {
		// On regarde si il n'y a pas déjà une méthode du même nom
		if (methods.containsKey(token.getWord())) {
			compiler.addError(new AnalyzeError(token, AnalyzeErrorLevel.ERROR, Error.DUPLICATED_METHOD));
		}
		if (!staticMethods.containsKey(token.getWord())) {
			staticMethods.put(token.getWord(), new HashMap<>());
			staticMethodVariables.put(token.getWord(), new LeekVariable(token, VariableType.STATIC_METHOD));
		}
		staticMethods.get(token.getWord()).put(method.countParameters(), method);
	}

	public boolean hasStaticMethod(String name, int paramCount) {
		return staticMethods.containsKey(name) && staticMethods.get(name).containsKey(paramCount);
	}

	public void addField(WordCompiler compiler, IAWord word, AbstractExpression expr) throws LeekCompilerException {
		if (fields.containsKey(word.getWord()) || staticFields.containsKey(word.getWord())) {
			compiler.addError(new AnalyzeError(word, AnalyzeErrorLevel.ERROR, Error.FIELD_ALREADY_EXISTS));
			return;
		}
		fields.put(word.getWord(), expr);
		fieldVariables.put(word.getWord(), new LeekVariable(word, VariableType.FIELD));
	}

	public void addStaticField(IAWord word, AbstractExpression expr) throws LeekCompilerException {
		if (staticFields.containsKey(word.getWord()) || fields.containsKey(word.getWord())) {
			throw new LeekCompilerException(word, Error.FIELD_ALREADY_EXISTS);
		}
		staticFields.put(word.getWord(), expr);
		staticFieldVariables.put(word.getWord(), new LeekVariable(word, VariableType.STATIC_FIELD));
	}

	public void declare(WordCompiler compiler) {
		// On ajoute la classe
		compiler.getCurrentBlock().addVariable(new LeekVariable(token, VariableType.CLASS, this));
	}

	public void analyze(WordCompiler compiler) {
		compiler.setCurrentClass(this);
		// Parent
		if (parentToken != null) {
			var parentVar = compiler.getCurrentBlock().getVariable(this.parentToken.getWord(), true);
			if (parentVar == null) {
				compiler.addError(new AnalyzeError(parentToken, AnalyzeErrorLevel.ERROR, Error.UNKNOWN_VARIABLE_OR_FUNCTION));
			} else if (parentVar.getVariableType() != VariableType.CLASS) {
				compiler.addError(new AnalyzeError(parentToken, AnalyzeErrorLevel.ERROR, Error.UNKNOWN_VARIABLE_OR_FUNCTION));
			} else {
				var current = parentVar.getClassDeclaration();
				boolean ok = true;
				while (current != null) {
					if (current == this) {
						compiler.addError(new AnalyzeError(parentToken, AnalyzeErrorLevel.ERROR, Error.EXTENDS_LOOP));
						ok = false;
						break;
					}
					current = current.getParent();
				}
				if (ok) {
					this.parent = parentVar.getClassDeclaration();
				}
			}
		}

		for (var constructor : constructors.values()) {
			constructor.analyze(compiler);
		}
		for (var method : methods.values()) {
			for (var version : method.values()) {
				version.analyze(compiler);
			}
		}
		for (var method : staticMethods.values()) {
			for (var version : method.values()) {
				version.analyze(compiler);
			}
		}
		compiler.setCurrentClass(null);
	}

	public void declareJava(MainLeekBlock mainblock, JavaWriter writer) {
		// Declare the class as a field of the AI
		String className = "user_" + token.getWord();
		writer.addLine("private ClassLeekValue " + className + " = new ClassLeekValue(\"" + token.getWord() + "\");");
	}

	public void createJava(MainLeekBlock mainblock, JavaWriter writer) {
		// Create the class in the constructor of the AI
		String className = "user_" + token.getWord();

		if (parent != null) {
			writer.addLine(className + ".setParent(user_" + parent.getName() + ");");
		}

		for (Entry<String, HashMap<Integer, ClassMethodBlock>> method : staticMethods.entrySet()) {
			for (Entry<Integer, ClassMethodBlock> version : method.getValue().entrySet()) {
				String methodName = className + "_" + method.getKey() + "_" + version.getKey();
				writer.addCode("final LeekAnonymousFunction " + methodName + " = new LeekAnonymousFunction() {");
				writer.addLine("public AbstractLeekValue run(AI mUAI, AbstractLeekValue u_this, AbstractLeekValue... values) throws LeekRunException {");
				writer.addLine("final var u_class = " + className + ";", version.getValue().getLine(), version.getValue().getFile());
				if (parent != null) {
					writer.addLine("final var u_super = user_" + parent.token.getWord() + ";");
				}
				version.getValue().writeJavaCode(mainblock, writer);
				writer.addLine("}};");
			}
		}

		for (Entry<String, HashMap<Integer, ClassMethodBlock>> method : methods.entrySet()) {
			for (Entry<Integer, ClassMethodBlock> version : method.getValue().entrySet()) {
				String methodName = className + "_" + method.getKey() + "_" + version.getKey();
				writer.addCode("final LeekAnonymousFunction " + methodName + " = new LeekAnonymousFunction() {");
				writer.addLine("public AbstractLeekValue run(AI mUAI, AbstractLeekValue u_this, AbstractLeekValue... values) throws LeekRunException {");
				writer.addLine("final var u_class = " + className + ";", version.getValue().getLine(), version.getValue().getFile());
				if (parent != null) {
					writer.addLine("final var u_super = user_" + parent.token.getWord() + ";");
				}
				writer.addCounter(1);
				version.getValue().writeJavaCode(mainblock, writer);
				writer.addLine("}};");
			}
		}

		for (Entry<String, AbstractExpression> field : staticFields.entrySet()) {
			writer.addCode(className);
			writer.addCode(".addStaticField(mUAI, \"" + field.getKey() + "\", ");
			if (field.getValue() != null) {
				field.getValue().writeJavaCode(mainblock, writer);
			} else {
				writer.addCode("LeekValueManager.NULL");
			}
			writer.addLine(");");
		}

		writeFields(mainblock, writer, className);

		for (Entry<String, HashMap<Integer, ClassMethodBlock>> method : staticMethods.entrySet()) {
			for (Entry<Integer, ClassMethodBlock> version : method.getValue().entrySet()) {
				String methodName = className + "_" + method.getKey() + "_" + version.getKey();
				writer.addCode(className);
				writer.addLine(".addStaticMethod(\"" + method.getKey() + "\", " + version.getKey() + ", " + methodName + ");");
			}
			writer.addCode(className);
			writer.addLine(".addGenericStaticMethod(\"" + method.getKey() + "\");");
		}

		for (Entry<Integer, ClassMethodBlock> construct : constructors.entrySet()) {
			writer.addCode(className);
			writer.addLine(".addConstructor(" + construct.getKey() + ", new LeekAnonymousFunction() {");
			writer.addLine("public AbstractLeekValue run(AI mUAI, AbstractLeekValue u_this, AbstractLeekValue... values) throws LeekRunException {");
			writer.addLine("final var u_class = " + className + ";");
			if (parent != null) {
				writer.addLine("final var u_super = user_" + parent.token.getWord() + ";");
			}
			construct.getValue().writeJavaCode(mainblock, writer);
			writer.addLine("}});");
		}

		for (Entry<String, HashMap<Integer, ClassMethodBlock>> method : methods.entrySet()) {
			for (Entry<Integer, ClassMethodBlock> version : method.getValue().entrySet()) {
				String methodName = className + "_" + method.getKey() + "_" + version.getKey();
				writer.addCode(className);
				writer.addLine(".addMethod(\"" + method.getKey() + "\", " + version.getKey() + ", " + methodName + ");");
			}
			writer.addCode(className);
			writer.addLine(".addGenericMethod(\"" + method.getKey() + "\");");
		}
	}

	@Override
	public void writeJavaCode(MainLeekBlock mainblock, JavaWriter writer) {

	}

	private void writeFields(MainLeekBlock mainblock, JavaWriter writer, String className) {

		if (parent != null) {
			parent.writeFields(mainblock, writer, className);
		}

		for (Entry<String, AbstractExpression> field : fields.entrySet()) {
			writer.addCode(className);
			writer.addCode(".addField(mUAI, \"" + field.getKey() + "\", ");
			if (field.getValue() != null) {
				field.getValue().writeJavaCode(mainblock, writer);
			} else {
				writer.addCode("LeekValueManager.NULL");
			}
			writer.addLine(");");
		}
	}

	public IAWord getParentToken() {
		return parentToken;
	}

	public ClassDeclarationInstruction getParent() {
		return parent;
	}

	public boolean hasMember(String field) {
		return getMember(field) != null;
	}

	public boolean hasStaticMember(String field) {
		return getStaticMember(field) != null;
	}

	public LeekVariable getMember(String token) {
		var f = fieldVariables.get(token);
		if (f != null) return f;

		var m = methodVariables.get(token);
		if (m != null) return m;

		if (parent != null) {
			return parent.getMember(token);
		}
		return null;
	}

	public LeekVariable getStaticMember(String token) {
		var f = staticFieldVariables.get(token);
		if (f != null) return f;

		var m = staticMethodVariables.get(token);
		if (m != null) return m;

		if (parent != null) {
			return parent.getStaticMember(token);
		}
		return null;
	}

	public String getMethodName(String name, int argumentCount) {
		var versions = methods.get(name);
		if (versions != null) {
			if (versions.containsKey(argumentCount)) return getName() + "_" + name + "_" + argumentCount;
		}
		if (parent != null) {
			return parent.getMethodName(name, argumentCount);
		}
		return null;
	}

	@Override
	public int getOperations() {
		return 0;
	}
}
