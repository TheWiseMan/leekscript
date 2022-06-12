package leekscript.compiler.expression;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import leekscript.common.Type;
import leekscript.compiler.JavaWriter;
import leekscript.compiler.WordCompiler;
import leekscript.compiler.bloc.MainLeekBlock;

public class LeekNumber extends AbstractExpression {

	private final double doubleValue;
	private final long longValue;
	private Type type;

	public LeekNumber(double doubleValue, long longValue, Type type) {
		this.doubleValue = doubleValue;
		this.longValue = longValue;
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
			formatter.setGroupingUsed(false);
			return formatter.format(doubleValue);
		} else {
			return String.valueOf(longValue);
		}
	}

	@Override
	public boolean validExpression(WordCompiler compiler, MainLeekBlock mainblock) throws LeekExpressionException {
		// Pour un nombre pas de soucis
		return true;
	}

	@Override
	public void writeJavaCode(MainLeekBlock mainblock, JavaWriter writer) {
		if (type == Type.INT) {
			writer.addCode(String.valueOf(longValue) + "l");
		} else {
			writer.addCode(String.valueOf(doubleValue));
		}
	}

	@Override
	public void analyze(WordCompiler compiler) {

	}

	public boolean equals(Object o) {
		if (o instanceof LeekNumber) {
			var n = (LeekNumber) o;
			if (type != n.type) return false;
			if (type == Type.INT) return longValue == n.longValue;
			return doubleValue == n.doubleValue;
		}
		return false;
	}
}
