package leekscript.compiler.bloc;

import leekscript.compiler.AnalyzeError;
import leekscript.compiler.Token;
import leekscript.compiler.JavaWriter;
import leekscript.compiler.Location;
import leekscript.compiler.WordCompiler;
import leekscript.compiler.AnalyzeError.AnalyzeErrorLevel;
import leekscript.compiler.expression.Expression;
import leekscript.compiler.expression.LeekExpressionException;
import leekscript.common.Error;
import leekscript.common.Type;
import leekscript.compiler.instruction.LeekVariableDeclarationInstruction;

public class ForeachKeyBlock extends AbstractLeekBlock {

	private final Token token;
	private Token mIterator;
	private Expression mArray;
	private boolean mIsDeclaration = false;
	private Token mKeyIterator = null;
	private boolean mIsKeyDeclaration = false;
	private boolean mKeyReference = false;
	private boolean mValueReference = false;
	private LeekVariableDeclarationInstruction iteratorDeclaration;
	private LeekVariableDeclarationInstruction iteratorKeyDeclaration;

	public ForeachKeyBlock(AbstractLeekBlock parent, MainLeekBlock main, boolean isKeyDeclaration, boolean isValueDeclaration, Token token, boolean keyReference, boolean valueReference) {
		super(parent, main);
		mIsDeclaration = isValueDeclaration;
		mIsKeyDeclaration = isKeyDeclaration;
		mKeyReference = keyReference;
		mValueReference = valueReference;
		this.token = token;
	}

	public void setValueIterator(WordCompiler compiler, Token iterator, boolean declaration) {
		if (declaration) {
			iteratorDeclaration = new LeekVariableDeclarationInstruction(compiler, iterator, compiler.getCurrentFunction());
			// addVariable(new LeekVariable(iterator, VariableType.ITERATOR, iteratorDeclaration));
		}
		mIterator = iterator;
	}

	public void setKeyIterator(WordCompiler compiler, Token iterator, boolean declaration) {
		if (declaration) {
			iteratorKeyDeclaration = new LeekVariableDeclarationInstruction(compiler, iterator, compiler.getCurrentFunction());
			// addVariable(new LeekVariable(iterator, VariableType.ITERATOR, iteratorKeyDeclaration));
		}
		mKeyIterator = iterator;
	}

	public void setArray(Expression exp) {
		mArray = exp;
	}

	@Override
	public String getCode() {
		return "for (" + (mIsKeyDeclaration ? "var " : "") + mKeyIterator + " : " + (mIsDeclaration ? "var " : "") + mIterator + " in " + mArray.toString() + ") {\n" + super.getCode() + "}";
	}

	public void preAnalyze(WordCompiler compiler) {
		AbstractLeekBlock initialBlock = compiler.getCurrentBlock();
		compiler.setCurrentBlock(this);

		// On analyse d'abord le container puis la variable
		mArray.preAnalyze(compiler);

		// Si c'est une déclaration on vérifie que le nom est disponible
		if (mIsKeyDeclaration) {
			if ((compiler.getVersion() >= 2 && (compiler.getMainBlock().hasGlobal(mKeyIterator.getWord()) || compiler.getMainBlock().hasUserFunction(mKeyIterator.getWord(), true))) || compiler.getCurrentBlock().hasVariable(mKeyIterator.getWord())) {
				compiler.addError(new AnalyzeError(mKeyIterator, AnalyzeErrorLevel.ERROR, Error.VARIABLE_NAME_UNAVAILABLE));
			} else {
				// this.addVariable(new LeekVariable(mKeyIterator, VariableType.LOCAL, iteratorKeyDeclaration));
			}
			iteratorKeyDeclaration.preAnalyze(compiler);
		} else {
			var v = compiler.getCurrentBlock().getVariable(mKeyIterator.getWord(), true);
			if (v == null) {
				compiler.addError(new AnalyzeError(mKeyIterator, AnalyzeErrorLevel.ERROR, Error.UNKNOWN_VARIABLE_OR_FUNCTION));
			}
		}
		// Si c'est une déclaration on vérifie que le nom est disponnible
		if (mIsDeclaration) {
			if ((compiler.getVersion() >= 2 && (compiler.getMainBlock().hasGlobal(mIterator.getWord()) || compiler.getMainBlock().hasUserFunction(mIterator.getWord(), true))) || compiler.getCurrentBlock().hasVariable(mIterator.getWord())) {
				compiler.addError(new AnalyzeError(mIterator, AnalyzeErrorLevel.ERROR, Error.VARIABLE_NAME_UNAVAILABLE));
			} else {
				// this.addVariable(new LeekVariable(mIterator, VariableType.LOCAL, iteratorDeclaration));
			}
			iteratorDeclaration.preAnalyze(compiler);
		} else {
			var v = compiler.getCurrentBlock().getVariable(mIterator.getWord(), true);
			if (v == null) {
				compiler.addError(new AnalyzeError(mIterator, AnalyzeErrorLevel.ERROR, Error.UNKNOWN_VARIABLE_OR_FUNCTION));
			}
		}
		if (iteratorDeclaration != null)
			iteratorDeclaration.setFunction(compiler.getCurrentFunction());
		if (iteratorKeyDeclaration != null)
			iteratorKeyDeclaration.setFunction(compiler.getCurrentFunction());

		compiler.setCurrentBlock(initialBlock);
		super.preAnalyze(compiler);
	}

	public void analyze(WordCompiler compiler) {
		AbstractLeekBlock initialBlock = compiler.getCurrentBlock();
		compiler.setCurrentBlock(this);
		mArray.analyze(compiler);

		if (!mArray.getType().canBeIterable()) {
			compiler.addError(new AnalyzeError(mArray.getLocation(), AnalyzeErrorLevel.WARNING, Error.NOT_ITERABLE, new String[] { mArray.getType().name } ));
		}

		// Le type n'est pas forcé par le conteneur
		// if (mIsDeclaration && iteratorDeclaration.getVariable() != null) {
		// 	iteratorDeclaration.getVariable().setType(mArray.getType().pureElement());
		// }

		// if (mIsKeyDeclaration && iteratorKeyDeclaration.getVariable() != null) {
		// 	iteratorKeyDeclaration.getVariable().setType(mArray.getType().key());
		// }

		compiler.setCurrentBlock(initialBlock);
		super.analyze(compiler);
	}

	@Override
	public void writeJavaCode(MainLeekBlock mainblock, JavaWriter writer) {

		AbstractLeekBlock initialBlock = mainblock.getWordCompiler().getCurrentBlock();
		mainblock.getWordCompiler().setCurrentBlock(this);

		// On prend un nombre unique pour les noms de variables temporaires
		int block_count = getCount();
		String var = "v" + block_count;
		String it = "i" + block_count;
		String ar = "ar" + block_count;

		String key_iterator = mainblock.hasGlobal(mKeyIterator.getWord()) ? ("g_" + mKeyIterator) : ("u_" + mKeyIterator);
		String val_iterator = mainblock.hasGlobal(mIterator.getWord()) ? ("g_" + mIterator) : ("u_" + mIterator);

		var iteratorVariable = mainblock.getWordCompiler().getCurrentBlock().getVariable(mIterator.getWord(), false);
		var iteratorKeyVariable = mainblock.getWordCompiler().getCurrentBlock().getVariable(mKeyIterator.getWord(), false);

		// Container
		writer.addCode("final var " + ar + " = ops(");
		if (mainblock.getCompiler().getCurrentAI().getVersion() >= 2) {
			mArray.writeJavaCode(mainblock, writer);
		} else {
			writer.compileLoad(mainblock, mArray);
		}
		writer.addCode(", " + mArray.getOperations() + ");");

		StringBuilder sb = new StringBuilder();
		sb.append("if (isIterable(").append(ar).append(")) {");
		// Clé
		if (mIsKeyDeclaration) {
			if (iteratorKeyVariable != null && iteratorKeyVariable.getDeclaration() != null && iteratorKeyVariable.getDeclaration().isCaptured()) {
				sb.append("final Wrapper " + key_iterator + " = new Wrapper(new Box(" + writer.getAIThis() + ", null));");
			} else if (mainblock.getCompiler().getCurrentAI().getVersion() <= 1) {
				sb.append("var " + key_iterator + " = new Box(" + writer.getAIThis() + ", null);");
			} else {
				sb.append(iteratorKeyDeclaration.getVariable().getType().getJavaName() + " ").append(key_iterator).append(" = null; ops(1); ");
			}
		}
		// Valeur
		if (mIsDeclaration) {
			if (iteratorVariable != null && iteratorVariable.getDeclaration() != null && iteratorVariable.getDeclaration().isCaptured()) {
				sb.append("final Wrapper " + val_iterator + " = new Wrapper(new Box(" + writer.getAIThis() + ", null));");
			} else if (mainblock.getCompiler().getCurrentAI().getVersion() >= 2) {
				sb.append(iteratorDeclaration.getVariable().getType().getJavaName() + " " + val_iterator + " = null; ops(1);");
			} else if (mainblock.getCompiler().getCurrentAI().getVersion() <= 1 || iteratorVariable.getDeclaration().isCaptured()) {
				sb.append("var " + val_iterator + " = new Box(" + writer.getAIThis() + ", null);");
			} else {
				sb.append("Object ").append(val_iterator).append(" = null; ops(1);");
			}
		}

		// On fait le parcours
		sb.append("var ").append(it).append(" = iterator(").append(ar).append("); while (").append(it).append(".hasNext()) { var ").append(var).append(" = ").append(it).append(".next(); ");

		// Maj de la clé
		if (mainblock.getVersion() >= 4) {
			if (iteratorKeyVariable != null && iteratorKeyVariable.getDeclaration() != null && iteratorKeyVariable.getDeclaration().isCaptured()) {
				sb.append(key_iterator).append(".set(").append(var).append(".getKey()); ");
			} else if (mIsKeyDeclaration) {
				sb.append(key_iterator).append(" = (" + iteratorKeyDeclaration.getVariable().getType().getJavaName() + ")").append(var).append(".getKey(); ");
			} else {
				sb.append(key_iterator).append(" = ").append(var).append(".getKey(); ");
			}
		} else if (mainblock.getVersion() >= 2) {
			if (iteratorKeyVariable != null && iteratorKeyVariable.getDeclaration() != null && iteratorKeyVariable.getDeclaration().isCaptured()) {
				sb.append(key_iterator).append(".set(").append(var).append(".getKey()); ");
			} else if (mIsKeyDeclaration) {
				sb.append(key_iterator).append(" = (" + iteratorKeyDeclaration.getVariable().getType().getJavaName() + ")").append(var).append(".getKey(); ");
			} else {
				sb.append(key_iterator).append(" = ").append(var).append(".getKey(); ");
			}
		} else {
			if (iteratorKeyVariable != null && iteratorKeyVariable.getDeclaration() != null && iteratorKeyVariable.getDeclaration().isCaptured()) {
				sb.append(key_iterator).append(".set(").append(var).append(".getKey()); ops(1); ");
			} else if (mKeyReference) {
				sb.append(key_iterator).append(".set(").append(var).append(".getKey()); ");
			} else {
				sb.append(key_iterator).append(".set(").append(var).append(".getKey()); ops(1); ");
			}
		}
		// Maj de la valeur
		if (mainblock.getVersion() >= 4) {
			if (iteratorVariable != null && iteratorVariable.getDeclaration() != null && iteratorVariable.getDeclaration().isCaptured()) {
				sb.append(val_iterator).append(".set(").append(var).append(".getValue());");
			} else if (mIsDeclaration) {
				sb.append(val_iterator).append(" = (" + iteratorDeclaration.getVariable().getType().getJavaName() + ")").append(var).append(".getValue();");
			} else {
				sb.append(val_iterator).append(" = ").append(var).append(".getValue();");
			}
		} else if (mainblock.getVersion() >= 2) {
			if (iteratorVariable != null && iteratorVariable.getDeclaration() != null && iteratorVariable.getDeclaration().isCaptured()) {
				sb.append(val_iterator).append(".set(").append(var).append(".getValue());");
			} else if (mIsDeclaration) {
				sb.append(val_iterator).append(" = (" + iteratorDeclaration.getVariable().getType().getJavaName() + ")").append(var).append(".getValue();");
			} else {
				sb.append(val_iterator).append(" = ").append(var).append(".getValue();");
			}
		} else {
			if (mValueReference) {
				sb.append(val_iterator).append(".set(").append(var).append(".getValue());");
			} else if (iteratorVariable != null && iteratorVariable.getDeclaration() != null && iteratorVariable.getDeclaration().isCaptured()) {
				sb.append(val_iterator).append(".set(").append(var).append(".getValue()); ops(1);");
			} else {
				sb.append(val_iterator).append(".set(").append(var).append(".getValue()); ops(1);");
			}
		}

		mainblock.getWordCompiler().setCurrentBlock(initialBlock);

		writer.addLine(sb.toString(), getLocation());
		// Instructions
		super.writeJavaCode(mainblock, writer);

		// Fin
		writer.addLine("}}");
	}

	@Override
	public boolean isBreakable() {
		return true;
	}

	@Override
	public int getEndBlock() {
		return 0;
	}

	@Override
	public Location getLocation() {
		return token.getLocation();
	}

	@Override
	public int getNature() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean validExpression(WordCompiler compiler, MainLeekBlock mainblock) throws LeekExpressionException {
		// TODO Auto-generated method stub
		return false;
	}
}
