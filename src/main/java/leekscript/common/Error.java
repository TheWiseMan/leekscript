package leekscript.common;

public enum Error {
	NONE,
	FUNCTION_NAME_UNAVAILABLE, // 1
	PARAMETER_NAME_UNAVAILABLE, // 2
	OPENING_PARENTHESIS_EXPECTED, // 3
	OPENING_CURLY_BRACKET_EXPECTED, // 4
	PARAMETER_NAME_EXPECTED, // 5
	FUNCTION_NAME_EXPECTED, // 6
	PARENTHESIS_EXPECTED_AFTER_PARAMETERS, // 7
	OPEN_BLOC_REMAINING, // 8
	NO_BLOC_TO_CLOSE, // 9
	END_OF_SCRIPT_UNEXPECTED, // 10
	END_OF_INSTRUCTION_EXPECTED, // 11
	BREAK_OUT_OF_LOOP, // 12
	CONTINUE_OUT_OF_LOOP, // 13
	INCLUDE_ONLY_IN_MAIN_BLOCK, // 14
	AI_NAME_EXPECTED, // 15
	AI_NOT_EXISTING, // 16
	CLOSING_PARENTHESIS_EXPECTED, // 17
	CLOSING_SQUARE_BRACKET_EXPECTED, // 18
	FUNCTION_ONLY_IN_MAIN_BLOCK, // 19
	VARIABLE_NAME_EXPECTED, // 20
	VARIABLE_NAME_UNAVAILABLE, // 21
	VARIABLE_NOT_EXISTS, // 22
	KEYWORD_UNEXPECTED, // 23
	KEYWORD_IN_EXPECTED, // 24
	WHILE_EXPECTED_AFTER_DO, // 25
	NO_IF_BLOCK, // 26
	GLOBAL_ONLY_IN_MAIN_BLOCK, // 27
	VAR_NAME_EXPECTED_AFTER_GLOBAL, // 28
	VAR_NAME_EXPECTED, // 29
	SIMPLE_ARRAY, // 30
	ASSOCIATIVE_ARRAY, // 31
	PARENTHESIS_EXPECTED_AFTER_FUNCTION, // 32
	UNKNOWN_VARIABLE_OR_FUNCTION, // 33
	OPERATOR_UNEXPECTED, // 34
	VALUE_EXPECTED, // 35
	CANT_ADD_INSTRUCTION_AFTER_BREAK, // 36
	UNCOMPLETE_EXPRESSION, // 37
	CANT_ASSIGN_VALUE, // 38
	FUNCTION_NOT_EXISTS, // 39
	INVALID_PARAMETER_COUNT, // 40
	INVALID_CHAR, // 41
	INVALID_NUMBER, // 42
	CONSTRUCTOR_ALREADY_EXISTS, // 43
	END_OF_CLASS_EXPECTED, // 44
	FIELD_ALREADY_EXISTS, // 45
	NO_SUCH_CLASS, // 46
	THIS_NOT_ALLOWED_HERE, // 47
	KEYWORD_MUST_BE_IN_CLASS, // 48
	SUPER_NOT_AVAILABLE_PARENT, // 49
	CLASS_MEMBER_DOES_NOT_EXIST, // 50
	CLASS_STATIC_MEMBER_DOES_NOT_EXIST, // 51
	EXTENDS_LOOP, // 52
	REFERENCE_DEPRECATED, // 53
	DUPLICATED_METHOD, // 54
	DEPRECATED_FUNCTION, // 55
	UNKNOWN_FUNCTION, // 56
	DIVISION_BY_ZERO, // 57
	CAN_NOT_EXECUTE_VALUE, // 58
	CAN_NOT_EXECUTE_WITH_ARGUMENTS, // 59
	NO_AI_EQUIPPED, // 60
	INVALID_AI, // 61
	COMPILE_JAVA, // 62
	AI_DISABLED, // 63
	AI_INTERRUPTED, // 64
	AI_TIMEOUT, // 65
	CODE_TOO_LARGE, // 66
	CODE_TOO_LARGE_FUNCTION, // 67
	INTERNAL_ERROR, // 68
	UNKNOWN_METHOD, // 69
	UNKNOWN_STATIC_METHOD, // 70
	STRING_METHOD_MUST_RETURN_STRING, // 71
	UNKNOWN_FIELD, // 72
	UNKNOWN_CONSTRUCTOR, // 73
	INSTANCEOF_MUST_BE_CLASS, // 74
	NOT_ITERABLE, // 75
	STACKOVERFLOW, // 76
	INVALID_OPERATOR, // 77
	PRIVATE_FIELD, // 78
	PROTECTED_FIELD, // 79
	PRIVATE_STATIC_FIELD, // 80
	PROTECTED_STATIC_FIELD, // 81
	PRIVATE_METHOD, // 82
	PROTECTED_METHOD, // 83
	PRIVATE_CONSTRUCTOR, // 84
	PROTECTED_CONSTRUCTOR, // 85
	PRIVATE_STATIC_METHOD, // 86
	PROTECTED_STATIC_METHOD, // 87
	CANNOT_LOAD_AI, // 88
	TRANSPILE_TO_JAVA, // 89
	CANNOT_WRITE_AI, // 90
	RESERVED_FIELD, // 91
	VALUE_IS_NOT_AN_ARRAY, // 92
	TRIPLE_EQUALS_DEPRECATED, // 93
}