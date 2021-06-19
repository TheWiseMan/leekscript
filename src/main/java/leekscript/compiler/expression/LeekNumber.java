package leekscript.compiler.expression;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import leekscript.common.Type;
import leekscript.compiler.JavaWriter;
import leekscript.compiler.WordCompiler;
import leekscript.compiler.bloc.MainLeekBlock;

public class LeekNumber extends AbstractExpression {

	private final double mValue;
	private Type type;

	public LeekNumber(double value, Type type) {
		mValue = value;
		this.type = type;
	}

	@Override
	public int getNature() {
		return NUMBER;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String getString() {
		if (type == Type.REAL) {
			var formatter = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
			formatter.setMaximumFractionDigits(15);
			return formatter.format(mValue);
		} else {
			return String.valueOf((int) mValue);
		}
	}

	@Override
	public boolean validExpression(WordCompiler compiler, MainLeekBlock mainblock) throws LeekExpressionException {
		// Pour un nombre pas de soucis
		return true;
	}

	@Override
	public void writeJavaCode(MainLeekBlock mainblock, JavaWriter writer) {
		if (type == Type.REAL) {
			writer.addCode("new DoubleLeekValue(" + mValue + ")");
		} else {
			writer.addCode("LeekValueManager.getLeekIntValue(" + ((int) mValue) + ")");
		}
	}

	@Override
	public void analyze(WordCompiler compiler) {

	}
}
