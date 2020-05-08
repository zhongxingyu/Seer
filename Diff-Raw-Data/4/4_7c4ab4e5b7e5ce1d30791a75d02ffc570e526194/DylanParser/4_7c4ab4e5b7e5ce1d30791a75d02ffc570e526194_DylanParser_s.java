 // This is a generated file. Not intended for manual editing.
 package org.dylanfoundry.deft.filetypes.dylan.parser;
 
 import org.jetbrains.annotations.*;
 import com.intellij.lang.LighterASTNode;
 import com.intellij.lang.PsiBuilder;
 import com.intellij.lang.PsiBuilder.Marker;
 import com.intellij.openapi.diagnostic.Logger;
 import static org.dylanfoundry.deft.filetypes.dylan.psi.DylanTypes.*;
 import static org.dylanfoundry.deft.filetypes.dylan.DylanParserUtil.*;
 import com.intellij.psi.tree.IElementType;
 import com.intellij.lang.ASTNode;
 import com.intellij.psi.tree.TokenSet;
 import com.intellij.lang.PsiParser;
 
 @SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
 public class DylanParser implements PsiParser {
 
   public static Logger LOG_ = Logger.getInstance("org.dylanfoundry.deft.filetypes.dylan.parser.DylanParser");
 
   @NotNull
   public ASTNode parse(IElementType root_, PsiBuilder builder_) {
     int level_ = 0;
     boolean result_;
     builder_ = adapt_builder_(root_, builder_, this);
     if (root_ == AFTERWARDS_STATEMENT) {
       result_ = afterwards_statement(builder_, level_ + 1);
     }
     else if (root_ == ALLOCATION) {
       result_ = allocation(builder_, level_ + 1);
     }
     else if (root_ == AND_EXPR) {
       result_ = expression(builder_, level_ + 1, 0);
     }
     else if (root_ == ARGUMENT) {
       result_ = argument(builder_, level_ + 1);
     }
     else if (root_ == ARGUMENTS) {
       result_ = arguments(builder_, level_ + 1);
     }
     else if (root_ == ARITH_NEG_EXPR) {
       result_ = arith_neg_expr(builder_, level_ + 1);
     }
     else if (root_ == ASSIGN_EXPR) {
       result_ = expression(builder_, level_ + 1, -1);
     }
     else if (root_ == AUX_RULE) {
       result_ = aux_rule(builder_, level_ + 1);
     }
     else if (root_ == AUX_RULE_SET) {
       result_ = aux_rule_set(builder_, level_ + 1);
     }
     else if (root_ == AUX_RULE_SETS) {
       result_ = aux_rule_sets(builder_, level_ + 1);
     }
     else if (root_ == AUX_RULES) {
       result_ = aux_rules(builder_, level_ + 1);
     }
     else if (root_ == BASIC_FRAGMENT) {
       result_ = basic_fragment(builder_, level_ + 1);
     }
     else if (root_ == BEGIN_STATEMENT) {
       result_ = begin_statement(builder_, level_ + 1);
     }
     else if (root_ == BEGIN_WORD) {
       result_ = begin_word(builder_, level_ + 1);
     }
     else if (root_ == BINARY_OPERATOR) {
       result_ = binary_operator(builder_, level_ + 1);
     }
     else if (root_ == BINDING_PATTERN) {
       result_ = binding_pattern(builder_, level_ + 1);
     }
     else if (root_ == BINDINGS) {
       result_ = bindings(builder_, level_ + 1);
     }
     else if (root_ == BLOCK_STATEMENT) {
       result_ = block_statement(builder_, level_ + 1);
     }
     else if (root_ == BLOCK_TAIL) {
       result_ = block_tail(builder_, level_ + 1);
     }
     else if (root_ == BODY) {
       result_ = body(builder_, level_ + 1);
     }
     else if (root_ == BODY_FRAGMENT) {
       result_ = body_fragment(builder_, level_ + 1);
     }
     else if (root_ == BODY_STYLE_DEFINITION_RULE) {
       result_ = body_style_definition_rule(builder_, level_ + 1);
     }
     else if (root_ == BODY_STYLE_DEFINITION_RULES) {
       result_ = body_style_definition_rules(builder_, level_ + 1);
     }
     else if (root_ == BRACKETED_FRAGMENT) {
       result_ = bracketed_fragment(builder_, level_ + 1);
     }
     else if (root_ == BRACKETED_PATTERN) {
       result_ = bracketed_pattern(builder_, level_ + 1);
     }
     else if (root_ == BRACKETING_PUNCTUATION) {
       result_ = bracketing_punctuation(builder_, level_ + 1);
     }
     else if (root_ == CASE_BODY) {
       result_ = case_body(builder_, level_ + 1);
     }
     else if (root_ == CASE_CONSTITUENT) {
       result_ = case_constituent(builder_, level_ + 1);
     }
     else if (root_ == CASE_CONSTITUENTS) {
       result_ = case_constituents(builder_, level_ + 1);
     }
     else if (root_ == CASE_LABEL) {
       result_ = case_label(builder_, level_ + 1);
     }
     else if (root_ == CASE_STATEMENT) {
       result_ = case_statement(builder_, level_ + 1);
     }
     else if (root_ == CASE_STMT_CLAUSE) {
       result_ = case_stmt_clause(builder_, level_ + 1);
     }
     else if (root_ == CASE_STMT_CONSTITUENT) {
       result_ = case_stmt_constituent(builder_, level_ + 1);
     }
     else if (root_ == CASE_STMT_CONSTITUENTS) {
       result_ = case_stmt_constituents(builder_, level_ + 1);
     }
     else if (root_ == CASE_STMT_LABEL) {
       result_ = case_stmt_label(builder_, level_ + 1);
     }
     else if (root_ == CASE_STMT_TAIL) {
       result_ = case_stmt_tail(builder_, level_ + 1);
     }
     else if (root_ == CASES) {
       result_ = cases(builder_, level_ + 1);
     }
     else if (root_ == CLASS_DEFINITION_TAIL) {
       result_ = class_definition_tail(builder_, level_ + 1);
     }
     else if (root_ == CLAUSE_OPTION) {
       result_ = clause_option(builder_, level_ + 1);
     }
     else if (root_ == CLEANUP_STATEMENT) {
       result_ = cleanup_statement(builder_, level_ + 1);
     }
     else if (root_ == COLLECTION_CLAUSE) {
       result_ = collection_clause(builder_, level_ + 1);
     }
     else if (root_ == CONDITION) {
       result_ = condition(builder_, level_ + 1);
     }
     else if (root_ == CONSTANT) {
       result_ = constant(builder_, level_ + 1);
     }
     else if (root_ == CONSTANT_FRAGMENT) {
       result_ = constant_fragment(builder_, level_ + 1);
     }
     else if (root_ == CONSTANTS) {
       result_ = constants(builder_, level_ + 1);
     }
     else if (root_ == CORE_WORD) {
       result_ = core_word(builder_, level_ + 1);
     }
     else if (root_ == CREATE_CLAUSE) {
       result_ = create_clause(builder_, level_ + 1);
     }
     else if (root_ == DEFAULT_VALUE) {
       result_ = default_value(builder_, level_ + 1);
     }
     else if (root_ == DEFINE_BODY_WORD) {
       result_ = define_body_word(builder_, level_ + 1);
     }
     else if (root_ == DEFINE_LIST_WORD) {
       result_ = define_list_word(builder_, level_ + 1);
     }
     else if (root_ == DEFINITION) {
       result_ = definition(builder_, level_ + 1);
     }
     else if (root_ == DEFINITION_CLASS_DEFINER) {
       result_ = definition_class_definer(builder_, level_ + 1);
     }
     else if (root_ == DEFINITION_CONSTANT_DEFINER) {
       result_ = definition_constant_definer(builder_, level_ + 1);
     }
     else if (root_ == DEFINITION_COPY_DOWN_METHOD_DEFINER) {
       result_ = definition_copy_down_method_definer(builder_, level_ + 1);
     }
     else if (root_ == DEFINITION_DOMAIN_DEFINER) {
       result_ = definition_domain_definer(builder_, level_ + 1);
     }
     else if (root_ == DEFINITION_FUNCTION_DEFINER) {
       result_ = definition_function_definer(builder_, level_ + 1);
     }
     else if (root_ == DEFINITION_GENERIC_DEFINER) {
       result_ = definition_generic_definer(builder_, level_ + 1);
     }
     else if (root_ == DEFINITION_LIBRARY_DEFINER) {
       result_ = definition_library_definer(builder_, level_ + 1);
     }
     else if (root_ == DEFINITION_MACRO_CALL) {
       result_ = definition_macro_call(builder_, level_ + 1);
     }
     else if (root_ == DEFINITION_MACRO_DEFINER) {
       result_ = definition_macro_definer(builder_, level_ + 1);
     }
     else if (root_ == DEFINITION_METHOD_DEFINER) {
       result_ = definition_method_definer(builder_, level_ + 1);
     }
     else if (root_ == DEFINITION_MODULE_DEFINER) {
       result_ = definition_module_definer(builder_, level_ + 1);
     }
     else if (root_ == DEFINITION_SHARED_SYMBOLS_DEFINER) {
       result_ = definition_shared_symbols_definer(builder_, level_ + 1);
     }
     else if (root_ == DEFINITION_SUITE_DEFINER) {
       result_ = definition_suite_definer(builder_, level_ + 1);
     }
     else if (root_ == DEFINITION_TAIL) {
       result_ = definition_tail(builder_, level_ + 1);
     }
     else if (root_ == DEFINITION_TEST_DEFINER) {
       result_ = definition_test_definer(builder_, level_ + 1);
     }
     else if (root_ == DEFINITION_VARIABLE_DEFINER) {
       result_ = definition_variable_definer(builder_, level_ + 1);
     }
     else if (root_ == DIV_EXPR) {
       result_ = expression(builder_, level_ + 1, 3);
     }
     else if (root_ == DYLAN_UNRESERVED_NAME) {
       result_ = dylan_unreserved_name(builder_, level_ + 1);
     }
     else if (root_ == ELSE_STATEMENT) {
       result_ = else_statement(builder_, level_ + 1);
     }
     else if (root_ == ELSEIF_STATEMENT) {
       result_ = elseif_statement(builder_, level_ + 1);
     }
     else if (root_ == END_CLAUSE) {
       result_ = end_clause(builder_, level_ + 1);
     }
     else if (root_ == END_FOR_CLAUSE) {
       result_ = end_for_clause(builder_, level_ + 1);
     }
     else if (root_ == EQ_EXPR) {
       result_ = expression(builder_, level_ + 1, 1);
     }
     else if (root_ == ESCAPED_NAME) {
       result_ = escaped_name(builder_, level_ + 1);
     }
     else if (root_ == EXCEPTION_OPTIONS) {
       result_ = exception_options(builder_, level_ + 1);
     }
     else if (root_ == EXCEPTION_STATEMENT) {
       result_ = exception_statement(builder_, level_ + 1);
     }
     else if (root_ == EXCLUDE_OPTION) {
       result_ = exclude_option(builder_, level_ + 1);
     }
     else if (root_ == EXP_EXPR) {
       result_ = expression(builder_, level_ + 1, 4);
     }
     else if (root_ == EXPLICIT_STEP_CLAUSE) {
       result_ = explicit_step_clause(builder_, level_ + 1);
     }
     else if (root_ == EXPORT_CLAUSE) {
       result_ = export_clause(builder_, level_ + 1);
     }
     else if (root_ == EXPORT_OPTION) {
       result_ = export_option(builder_, level_ + 1);
     }
     else if (root_ == EXPRESSION) {
       result_ = expression(builder_, level_ + 1, -1);
     }
     else if (root_ == EXPRESSIONS) {
       result_ = expressions(builder_, level_ + 1);
     }
     else if (root_ == FINALLY_CLAUSE) {
       result_ = finally_clause(builder_, level_ + 1);
     }
     else if (root_ == FOR_CLAUSE) {
       result_ = for_clause(builder_, level_ + 1);
     }
     else if (root_ == FOR_CLAUSES) {
       result_ = for_clauses(builder_, level_ + 1);
     }
     else if (root_ == FOR_STATEMENT) {
       result_ = for_statement(builder_, level_ + 1);
     }
     else if (root_ == FUNCTION_DEFINITION_TAIL) {
       result_ = function_definition_tail(builder_, level_ + 1);
     }
     else if (root_ == FUNCTION_MACRO_CALL) {
       result_ = function_macro_call(builder_, level_ + 1);
     }
     else if (root_ == FUNCTION_RULE) {
       result_ = function_rule(builder_, level_ + 1);
     }
     else if (root_ == FUNCTION_RULES) {
       result_ = function_rules(builder_, level_ + 1);
     }
     else if (root_ == FUNCTION_WORD) {
       result_ = function_word(builder_, level_ + 1);
     }
     else if (root_ == GT_EXPR) {
       result_ = expression(builder_, level_ + 1, 1);
     }
     else if (root_ == GTEQ_EXPR) {
       result_ = expression(builder_, level_ + 1, 1);
     }
     else if (root_ == HANDLER) {
       result_ = handler(builder_, level_ + 1);
     }
     else if (root_ == HASH_WORD) {
       result_ = hash_word(builder_, level_ + 1);
     }
     else if (root_ == HEADER) {
       result_ = header(builder_, level_ + 1);
     }
     else if (root_ == HEADER_VALUES) {
       result_ = header_values(builder_, level_ + 1);
     }
     else if (root_ == HEADERS) {
       result_ = headers(builder_, level_ + 1);
     }
     else if (root_ == IDENT_EXPR) {
       result_ = expression(builder_, level_ + 1, 1);
     }
     else if (root_ == IF_STATEMENT) {
       result_ = if_statement(builder_, level_ + 1);
     }
     else if (root_ == IF_TAIL) {
       result_ = if_tail(builder_, level_ + 1);
     }
     else if (root_ == IMPORT_OPTION) {
       result_ = import_option(builder_, level_ + 1);
     }
     else if (root_ == INHERITED_OPTIONS) {
       result_ = inherited_options(builder_, level_ + 1);
     }
     else if (root_ == INHERITED_SLOT_SPEC) {
       result_ = inherited_slot_spec(builder_, level_ + 1);
     }
     else if (root_ == INIT_ARG_OPTION) {
       result_ = init_arg_option(builder_, level_ + 1);
     }
     else if (root_ == INIT_ARG_OPTIONS) {
       result_ = init_arg_options(builder_, level_ + 1);
     }
     else if (root_ == INIT_ARG_SPEC) {
       result_ = init_arg_spec(builder_, level_ + 1);
     }
     else if (root_ == INIT_EXPRESSION) {
       result_ = init_expression(builder_, level_ + 1);
     }
     else if (root_ == INIT_FUNCTION_SLOT_OPTION) {
       result_ = init_function_slot_option(builder_, level_ + 1);
     }
     else if (root_ == INIT_KEYWORD_SLOT_OPTION) {
       result_ = init_keyword_slot_option(builder_, level_ + 1);
     }
     else if (root_ == INIT_VALUE_SLOT_OPTION) {
       result_ = init_value_slot_option(builder_, level_ + 1);
     }
     else if (root_ == KEY_PARAMETER_LIST) {
       result_ = key_parameter_list(builder_, level_ + 1);
     }
     else if (root_ == KEYED_BY_CLAUSE) {
       result_ = keyed_by_clause(builder_, level_ + 1);
     }
     else if (root_ == KEYWORD_PARAMETER) {
       result_ = keyword_parameter(builder_, level_ + 1);
     }
     else if (root_ == KEYWORD_PARAMETERS) {
       result_ = keyword_parameters(builder_, level_ + 1);
     }
     else if (root_ == LIBRARY_DEFINITION_TAIL) {
       result_ = library_definition_tail(builder_, level_ + 1);
     }
     else if (root_ == LIST_STYLE_DEFINITION_RULE) {
       result_ = list_style_definition_rule(builder_, level_ + 1);
     }
     else if (root_ == LIST_STYLE_DEFINITION_RULES) {
       result_ = list_style_definition_rules(builder_, level_ + 1);
     }
     else if (root_ == LITERAL) {
       result_ = literal(builder_, level_ + 1);
     }
     else if (root_ == LOCAL_DECLARATION) {
       result_ = local_declaration(builder_, level_ + 1);
     }
     else if (root_ == LOCAL_METHODS) {
       result_ = local_methods(builder_, level_ + 1);
     }
     else if (root_ == LOG_NEG_EXPR) {
       result_ = log_neg_expr(builder_, level_ + 1);
     }
     else if (root_ == LT_EXPR) {
       result_ = expression(builder_, level_ + 1, 1);
     }
     else if (root_ == LTEQ_EXPR) {
       result_ = expression(builder_, level_ + 1, 1);
     }
     else if (root_ == MACRO) {
       result_ = macro(builder_, level_ + 1);
     }
     else if (root_ == MACRO_DEFINITION) {
       result_ = macro_definition(builder_, level_ + 1);
     }
     else if (root_ == MACRO_NAME) {
       result_ = macro_name(builder_, level_ + 1);
     }
     else if (root_ == MACRO_STATEMENT) {
       result_ = macro_statement(builder_, level_ + 1);
     }
     else if (root_ == MAIN_RULE_SET) {
       result_ = main_rule_set(builder_, level_ + 1);
     }
     else if (root_ == MAYBE_PATTERN_AND_SEMICOLON) {
       result_ = maybe_pattern_and_semicolon(builder_, level_ + 1);
     }
     else if (root_ == METHOD_DEFINITION) {
       result_ = method_definition(builder_, level_ + 1);
     }
     else if (root_ == METHOD_DEFINITION_TAIL) {
       result_ = method_definition_tail(builder_, level_ + 1);
     }
     else if (root_ == METHOD_STATEMENT) {
       result_ = method_statement(builder_, level_ + 1);
     }
     else if (root_ == MINUS_EXPR) {
       result_ = expression(builder_, level_ + 1, 2);
     }
     else if (root_ == MODIFIER) {
       result_ = modifier(builder_, level_ + 1);
     }
     else if (root_ == MODIFIERS) {
       result_ = modifiers(builder_, level_ + 1);
     }
     else if (root_ == MODULE_DEFINITION_TAIL) {
       result_ = module_definition_tail(builder_, level_ + 1);
     }
     else if (root_ == MUL_EXPR) {
       result_ = expression(builder_, level_ + 1, 3);
     }
     else if (root_ == NAME_NOT_END) {
       result_ = name_not_end(builder_, level_ + 1);
     }
     else if (root_ == NAME_PREFIX) {
       result_ = name_prefix(builder_, level_ + 1);
     }
     else if (root_ == NAME_STRING_OR_SYMBOL) {
       result_ = name_string_or_symbol(builder_, level_ + 1);
     }
     else if (root_ == NAME_SUFFIX) {
       result_ = name_suffix(builder_, level_ + 1);
     }
     else if (root_ == NEQ_EXPR) {
       result_ = expression(builder_, level_ + 1, 1);
     }
     else if (root_ == NEXT_REST_KEY_PARAMETER_LIST) {
       result_ = next_rest_key_parameter_list(builder_, level_ + 1);
     }
     else if (root_ == NON_BRACKETING_PUNCTUATION) {
       result_ = non_bracketing_punctuation(builder_, level_ + 1);
     }
     else if (root_ == NON_END_CORE_WORD) {
       result_ = non_end_core_word(builder_, level_ + 1);
     }
     else if (root_ == NON_STATEMENT_BASIC_FRAGMENT) {
       result_ = non_statement_basic_fragment(builder_, level_ + 1);
     }
     else if (root_ == NON_STATEMENT_BODY_FRAGMENT) {
       result_ = non_statement_body_fragment(builder_, level_ + 1);
     }
     else if (root_ == NONDEFINING_NAME) {
       result_ = nondefining_name(builder_, level_ + 1);
     }
     else if (root_ == NONDEFINING_WORD) {
       result_ = nondefining_word(builder_, level_ + 1);
     }
     else if (root_ == NONIDENT_EXPR) {
       result_ = expression(builder_, level_ + 1, 1);
     }
     else if (root_ == NUMERIC_CLAUSES) {
       result_ = numeric_clauses(builder_, level_ + 1);
     }
     else if (root_ == OPERAND) {
       result_ = operand(builder_, level_ + 1);
     }
     else if (root_ == OPERAND_EXPR) {
       result_ = operand_expr(builder_, level_ + 1);
     }
     else if (root_ == OPERATOR) {
       result_ = operator(builder_, level_ + 1);
     }
     else if (root_ == OR_EXPR) {
       result_ = expression(builder_, level_ + 1, 0);
     }
     else if (root_ == ORDINARY_BINDING_NAME) {
       result_ = ordinary_binding_name(builder_, level_ + 1);
     }
     else if (root_ == PARAMETER_LIST) {
       result_ = parameter_list(builder_, level_ + 1);
     }
     else if (root_ == PARAMETERS) {
       result_ = parameters(builder_, level_ + 1);
     }
     else if (root_ == PATTERN) {
       result_ = pattern(builder_, level_ + 1);
     }
     else if (root_ == PATTERN_KEYWORD) {
       result_ = pattern_keyword(builder_, level_ + 1);
     }
     else if (root_ == PATTERN_KEYWORDS) {
       result_ = pattern_keywords(builder_, level_ + 1);
     }
     else if (root_ == PATTERN_LIST) {
       result_ = pattern_list(builder_, level_ + 1);
     }
     else if (root_ == PATTERN_SEQUENCE) {
       result_ = pattern_sequence(builder_, level_ + 1);
     }
     else if (root_ == PATTERN_VARIABLE) {
       result_ = pattern_variable(builder_, level_ + 1);
     }
     else if (root_ == PLUS_EXPR) {
       result_ = expression(builder_, level_ + 1, 2);
     }
     else if (root_ == PREFIX_OPTION) {
       result_ = prefix_option(builder_, level_ + 1);
     }
     else if (root_ == PROPERTY) {
       result_ = property(builder_, level_ + 1);
     }
     else if (root_ == PROPERTY_LIST) {
       result_ = property_list(builder_, level_ + 1);
     }
     else if (root_ == PROPERTY_LIST_PATTERN) {
       result_ = property_list_pattern(builder_, level_ + 1);
     }
     else if (root_ == PUNCTUATION) {
       result_ = punctuation(builder_, level_ + 1);
     }
     else if (root_ == RENAME_OPTION) {
       result_ = rename_option(builder_, level_ + 1);
     }
     else if (root_ == REQUIRED_INIT_KEYWORD_SLOT_OPTION) {
       result_ = required_init_keyword_slot_option(builder_, level_ + 1);
     }
     else if (root_ == REQUIRED_PARAMETER) {
       result_ = required_parameter(builder_, level_ + 1);
     }
     else if (root_ == REQUIRED_PARAMETERS) {
       result_ = required_parameters(builder_, level_ + 1);
     }
     else if (root_ == RESERVED_WORD) {
       result_ = reserved_word(builder_, level_ + 1);
     }
     else if (root_ == REST_KEY_PARAMETER_LIST) {
       result_ = rest_key_parameter_list(builder_, level_ + 1);
     }
     else if (root_ == RHS) {
       result_ = rhs(builder_, level_ + 1);
     }
     else if (root_ == SELECT_STATEMENT) {
       result_ = select_statement(builder_, level_ + 1);
     }
     else if (root_ == SELECT_STMT_CLAUSE) {
       result_ = select_stmt_clause(builder_, level_ + 1);
     }
     else if (root_ == SELECT_STMT_CONSTITUENT) {
       result_ = select_stmt_constituent(builder_, level_ + 1);
     }
     else if (root_ == SELECT_STMT_CONSTITUENTS) {
       result_ = select_stmt_constituents(builder_, level_ + 1);
     }
     else if (root_ == SELECT_STMT_LABEL) {
       result_ = select_stmt_label(builder_, level_ + 1);
     }
     else if (root_ == SELECT_STMT_TAIL) {
       result_ = select_stmt_tail(builder_, level_ + 1);
     }
     else if (root_ == SEMICOLON_FRAGMENT) {
       result_ = semicolon_fragment(builder_, level_ + 1);
     }
     else if (root_ == SEPARATOR) {
       result_ = separator(builder_, level_ + 1);
     }
     else if (root_ == SETTER_SLOT_OPTION) {
       result_ = setter_slot_option(builder_, level_ + 1);
     }
     else if (root_ == SHARED_SYMBOLS) {
       result_ = shared_symbols(builder_, level_ + 1);
     }
     else if (root_ == SHARED_SYMBOLS_DEFINITION_TAIL) {
       result_ = shared_symbols_definition_tail(builder_, level_ + 1);
     }
     else if (root_ == SIMPLE_PATTERN) {
       result_ = simple_pattern(builder_, level_ + 1);
     }
     else if (root_ == SLOT_ADJECTIVE) {
       result_ = slot_adjective(builder_, level_ + 1);
     }
     else if (root_ == SLOT_DECLARATIONS) {
       result_ = slot_declarations(builder_, level_ + 1);
     }
     else if (root_ == SLOT_OPTION) {
       result_ = slot_option(builder_, level_ + 1);
     }
     else if (root_ == SLOT_OPTIONS) {
       result_ = slot_options(builder_, level_ + 1);
     }
     else if (root_ == SLOT_SPEC) {
       result_ = slot_spec(builder_, level_ + 1);
     }
     else if (root_ == SOURCE_RECORDS) {
       result_ = source_records(builder_, level_ + 1);
     }
     else if (root_ == STATEMENT) {
       result_ = statement(builder_, level_ + 1);
     }
     else if (root_ == STATEMENT_RULE) {
       result_ = statement_rule(builder_, level_ + 1);
     }
     else if (root_ == STATEMENT_RULES) {
       result_ = statement_rules(builder_, level_ + 1);
     }
     else if (root_ == STRING) {
       result_ = string(builder_, level_ + 1);
     }
     else if (root_ == STRING_LITERAL) {
       result_ = string_literal(builder_, level_ + 1);
     }
     else if (root_ == SUBSTITUTION) {
       result_ = substitution(builder_, level_ + 1);
     }
     else if (root_ == SUITE_COMPONENT) {
       result_ = suite_component(builder_, level_ + 1);
     }
     else if (root_ == SUITE_COMPONENTS) {
       result_ = suite_components(builder_, level_ + 1);
     }
     else if (root_ == SUITE_DEFINITION_TAIL) {
       result_ = suite_definition_tail(builder_, level_ + 1);
     }
     else if (root_ == SUITE_SUITE_COMPONENT) {
       result_ = suite_suite_component(builder_, level_ + 1);
     }
     else if (root_ == SUPERS) {
       result_ = supers(builder_, level_ + 1);
     }
     else if (root_ == SYMBOL) {
       result_ = symbol(builder_, level_ + 1);
     }
     else if (root_ == SYMBOL_VALUE) {
       result_ = symbol_value(builder_, level_ + 1);
     }
     else if (root_ == TEMPLATE) {
       result_ = template(builder_, level_ + 1);
     }
     else if (root_ == TEMPLATE_ELEMENT) {
       result_ = template_element(builder_, level_ + 1);
     }
     else if (root_ == TEST_DEFINITION_TAIL) {
       result_ = test_definition_tail(builder_, level_ + 1);
     }
     else if (root_ == TEST_SUITE_COMPONENT) {
       result_ = test_suite_component(builder_, level_ + 1);
     }
     else if (root_ == TOKEN) {
       result_ = token(builder_, level_ + 1);
     }
     else if (root_ == TYPE_SLOT_OPTION) {
       result_ = type_slot_option(builder_, level_ + 1);
     }
     else if (root_ == UNIQUE_STRING) {
       result_ = unique_string(builder_, level_ + 1);
     }
     else if (root_ == UNLESS_STATEMENT) {
       result_ = unless_statement(builder_, level_ + 1);
     }
     else if (root_ == UNRESERVED_NAME) {
       result_ = unreserved_name(builder_, level_ + 1);
     }
     else if (root_ == UNRESERVED_WORD) {
       result_ = unreserved_word(builder_, level_ + 1);
     }
     else if (root_ == UNTIL_STATEMENT) {
       result_ = until_statement(builder_, level_ + 1);
     }
     else if (root_ == USE_CLAUSE) {
       result_ = use_clause(builder_, level_ + 1);
     }
     else if (root_ == VALUES_LIST) {
       result_ = values_list(builder_, level_ + 1);
     }
     else if (root_ == VARIABLE) {
       result_ = variable(builder_, level_ + 1);
     }
     else if (root_ == VARIABLE_LIST) {
       result_ = variable_list(builder_, level_ + 1);
     }
     else if (root_ == VARIABLE_NAME) {
       result_ = variable_name(builder_, level_ + 1);
     }
     else if (root_ == VARIABLE_SPEC) {
       result_ = variable_spec(builder_, level_ + 1);
     }
     else if (root_ == VARIABLES) {
       result_ = variables(builder_, level_ + 1);
     }
     else if (root_ == WHEN_STATEMENT) {
       result_ = when_statement(builder_, level_ + 1);
     }
     else if (root_ == WHILE_STATEMENT) {
       result_ = while_statement(builder_, level_ + 1);
     }
     else if (root_ == WORD_NAME) {
       result_ = word_name(builder_, level_ + 1);
     }
     else {
       Marker marker_ = builder_.mark();
       enterErrorRecordingSection(builder_, level_, _SECTION_RECOVER_, null);
       result_ = parse_root_(root_, builder_, level_);
       exitErrorRecordingSection(builder_, level_, result_, true, _SECTION_RECOVER_, TOKEN_ADVANCER);
       marker_.done(root_);
     }
     return builder_.getTreeBuilt();
   }
 
   protected boolean parse_root_(final IElementType root_, final PsiBuilder builder_, final int level_) {
     return dylanFile(builder_, level_ + 1);
   }
 
   private static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
     TokenSet.create(AND_EXPR, ARITH_NEG_EXPR, ASSIGN_EXPR, DIV_EXPR,
       EQ_EXPR, EXPRESSION, EXP_EXPR, GTEQ_EXPR,
       GT_EXPR, IDENT_EXPR, LOG_NEG_EXPR, LTEQ_EXPR,
       LT_EXPR, MINUS_EXPR, MUL_EXPR, NEQ_EXPR,
       NONIDENT_EXPR, OPERAND_EXPR, OR_EXPR, PLUS_EXPR),
     TokenSet.create(INIT_FUNCTION_SLOT_OPTION, INIT_KEYWORD_SLOT_OPTION, INIT_VALUE_SLOT_OPTION, REQUIRED_INIT_KEYWORD_SLOT_OPTION,
       SETTER_SLOT_OPTION, SLOT_OPTION, TYPE_SLOT_OPTION),
     TokenSet.create(AFTERWARDS_STATEMENT, BEGIN_STATEMENT, BLOCK_STATEMENT, CASE_STATEMENT,
       CLEANUP_STATEMENT, ELSEIF_STATEMENT, ELSE_STATEMENT, EXCEPTION_STATEMENT,
       FOR_STATEMENT, IF_STATEMENT, MACRO_STATEMENT, METHOD_STATEMENT,
       SELECT_STATEMENT, STATEMENT, UNLESS_STATEMENT, UNTIL_STATEMENT,
       WHEN_STATEMENT, WHILE_STATEMENT),
     TokenSet.create(SUITE_COMPONENT, SUITE_SUITE_COMPONENT, TEST_SUITE_COMPONENT),
   };
 
   public static boolean type_extends_(IElementType child_, IElementType parent_) {
     for (TokenSet set : EXTENDS_SETS_) {
       if (set.contains(child_) && set.contains(parent_)) return true;
     }
     return false;
   }
 
   /* ********************************************************** */
   // AFTERWARDS body?
   public static boolean afterwards_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "afterwards_statement")) return false;
     if (!nextTokenIs(builder_, AFTERWARDS)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, AFTERWARDS);
     result_ = result_ && afterwards_statement_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(AFTERWARDS_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // body?
   private static boolean afterwards_statement_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "afterwards_statement_1")) return false;
     body(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // <<unreservedNameWithValues "instance" "each-subclass" "virtual">> | CLASS
   public static boolean allocation(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "allocation")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<allocation>");
     result_ = unreservedNameWithValues(builder_, level_ + 1, "instance", "each-subclass", "virtual");
     if (!result_) result_ = consumeToken(builder_, CLASS);
     if (result_) {
       marker_.done(ALLOCATION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // symbol expression? | expression
   public static boolean argument(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "argument")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<argument>");
     result_ = argument_0(builder_, level_ + 1);
     if (!result_) result_ = expression(builder_, level_ + 1, -1);
     if (result_) {
       marker_.done(ARGUMENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // symbol expression?
   private static boolean argument_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "argument_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = symbol(builder_, level_ + 1);
     result_ = result_ && argument_0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // expression?
   private static boolean argument_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "argument_0_1")) return false;
     expression(builder_, level_ + 1, -1);
     return true;
   }
 
   /* ********************************************************** */
   // argument (COMMA argument)*
   public static boolean arguments(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "arguments")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<arguments>");
     result_ = argument(builder_, level_ + 1);
     result_ = result_ && arguments_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(ARGUMENTS);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (COMMA argument)*
   private static boolean arguments_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "arguments_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!arguments_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "arguments_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA argument
   private static boolean arguments_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "arguments_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && argument(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // LBRACE pattern? RBRACE EQUAL_ARROW rhs
   public static boolean aux_rule(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "aux_rule")) return false;
     if (!nextTokenIs(builder_, LBRACE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LBRACE);
     result_ = result_ && aux_rule_1(builder_, level_ + 1);
     result_ = result_ && consumeTokens(builder_, 0, RBRACE, EQUAL_ARROW);
     result_ = result_ && rhs(builder_, level_ + 1);
     if (result_) {
       marker_.done(AUX_RULE);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // pattern?
   private static boolean aux_rule_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "aux_rule_1")) return false;
     pattern(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // symbol aux_rules
   public static boolean aux_rule_set(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "aux_rule_set")) return false;
     if (!nextTokenIs(builder_, KEYWORD) && !nextTokenIs(builder_, UNIQUE_STRING_CHARACTER)
         && replaceVariants(builder_, 2, "<aux rule set>")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<aux rule set>");
     result_ = symbol(builder_, level_ + 1);
     result_ = result_ && aux_rules(builder_, level_ + 1);
     if (result_) {
       marker_.done(AUX_RULE_SET);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // aux_rule_set+
   public static boolean aux_rule_sets(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "aux_rule_sets")) return false;
     if (!nextTokenIs(builder_, KEYWORD) && !nextTokenIs(builder_, UNIQUE_STRING_CHARACTER)
         && replaceVariants(builder_, 2, "<aux rule sets>")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<aux rule sets>");
     result_ = aux_rule_set(builder_, level_ + 1);
     int offset_ = builder_.getCurrentOffset();
     while (result_) {
       if (!aux_rule_set(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "aux_rule_sets");
         break;
       }
       offset_ = next_offset_;
     }
     if (result_) {
       marker_.done(AUX_RULE_SETS);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // aux_rule+
   public static boolean aux_rules(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "aux_rules")) return false;
     if (!nextTokenIs(builder_, LBRACE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = aux_rule(builder_, level_ + 1);
     int offset_ = builder_.getCurrentOffset();
     while (result_) {
       if (!aux_rule(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "aux_rules");
         break;
       }
       offset_ = next_offset_;
     }
     if (result_) {
       marker_.done(AUX_RULES);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // statement non_statement_basic_fragment? | non_statement_basic_fragment
   public static boolean basic_fragment(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "basic_fragment")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<basic fragment>");
     result_ = basic_fragment_0(builder_, level_ + 1);
     if (!result_) result_ = non_statement_basic_fragment(builder_, level_ + 1);
     if (result_) {
       marker_.done(BASIC_FRAGMENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // statement non_statement_basic_fragment?
   private static boolean basic_fragment_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "basic_fragment_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = statement(builder_, level_ + 1);
     result_ = result_ && basic_fragment_0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // non_statement_basic_fragment?
   private static boolean basic_fragment_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "basic_fragment_0_1")) return false;
     non_statement_basic_fragment(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // BEGIN body? END
   public static boolean begin_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "begin_statement")) return false;
     if (!nextTokenIs(builder_, BEGIN)) return false;
     boolean result_ = false;
     boolean pinned_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, null);
     result_ = consumeToken(builder_, BEGIN);
     pinned_ = result_; // pin = 1
     result_ = result_ && report_error_(builder_, begin_statement_1(builder_, level_ + 1));
     result_ = pinned_ && consumeToken(builder_, END) && result_;
     if (result_ || pinned_) {
       marker_.done(BEGIN_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, pinned_, _SECTION_GENERAL_, null);
     return result_ || pinned_;
   }
 
   // body?
   private static boolean begin_statement_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "begin_statement_1")) return false;
     body(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // NONDEFINING_BEGIN_WORD | DEFINE_BODY_BEGIN_WORD | DEFINE_LIST_BEGIN_WORD | METHOD
   public static boolean begin_word(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "begin_word")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<begin word>");
     result_ = consumeToken(builder_, NONDEFINING_BEGIN_WORD);
     if (!result_) result_ = consumeToken(builder_, DEFINE_BODY_BEGIN_WORD);
     if (!result_) result_ = consumeToken(builder_, DEFINE_LIST_BEGIN_WORD);
     if (!result_) result_ = consumeToken(builder_, METHOD);
     if (result_) {
       marker_.done(BEGIN_WORD);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
  // BINARY_OPERATOR_ONLY | UNARY_AND_BINARY_OPERATOR | EQUAL | EQUAL_EQUAL
   public static boolean binary_operator(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "binary_operator")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<binary operator>");
     result_ = consumeToken(builder_, BINARY_OPERATOR_ONLY);
     if (!result_) result_ = consumeToken(builder_, UNARY_AND_BINARY_OPERATOR);
     if (!result_) result_ = consumeToken(builder_, EQUAL);
     if (!result_) result_ = consumeToken(builder_, EQUAL_EQUAL);
     if (result_) {
       marker_.done(BINARY_OPERATOR);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // pattern_variable COLON_COLON pattern_variable EQUAL pattern_variable
   //     | pattern_variable COLON_COLON pattern_variable
   //     | pattern_variable EQUAL pattern_variable
   public static boolean binding_pattern(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "binding_pattern")) return false;
     if (!nextTokenIs(builder_, ELLIPSIS) && !nextTokenIs(builder_, QUERY)
         && replaceVariants(builder_, 2, "<binding pattern>")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<binding pattern>");
     result_ = binding_pattern_0(builder_, level_ + 1);
     if (!result_) result_ = binding_pattern_1(builder_, level_ + 1);
     if (!result_) result_ = binding_pattern_2(builder_, level_ + 1);
     if (result_) {
       marker_.done(BINDING_PATTERN);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // pattern_variable COLON_COLON pattern_variable EQUAL pattern_variable
   private static boolean binding_pattern_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "binding_pattern_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = pattern_variable(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, COLON_COLON);
     result_ = result_ && pattern_variable(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, EQUAL);
     result_ = result_ && pattern_variable(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // pattern_variable COLON_COLON pattern_variable
   private static boolean binding_pattern_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "binding_pattern_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = pattern_variable(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, COLON_COLON);
     result_ = result_ && pattern_variable(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // pattern_variable EQUAL pattern_variable
   private static boolean binding_pattern_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "binding_pattern_2")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = pattern_variable(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, EQUAL);
     result_ = result_ && pattern_variable(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // variable EQUAL expression | LPAREN variable_list RPAREN EQUAL expression
   public static boolean bindings(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "bindings")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<bindings>");
     result_ = bindings_0(builder_, level_ + 1);
     if (!result_) result_ = bindings_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(BINDINGS);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // variable EQUAL expression
   private static boolean bindings_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "bindings_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = variable(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, EQUAL);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // LPAREN variable_list RPAREN EQUAL expression
   private static boolean bindings_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "bindings_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LPAREN);
     result_ = result_ && variable_list(builder_, level_ + 1);
     result_ = result_ && consumeTokens(builder_, 0, RPAREN, EQUAL);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // BLOCK LPAREN variable_name? RPAREN body? afterwards_statement? cleanup_statement? exception_statement* block_tail
   public static boolean block_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "block_statement")) return false;
     if (!nextTokenIs(builder_, BLOCK)) return false;
     boolean result_ = false;
     boolean pinned_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, null);
     result_ = consumeTokens(builder_, 1, BLOCK, LPAREN);
     pinned_ = result_; // pin = 1
     result_ = result_ && report_error_(builder_, block_statement_2(builder_, level_ + 1));
     result_ = pinned_ && report_error_(builder_, consumeToken(builder_, RPAREN)) && result_;
     result_ = pinned_ && report_error_(builder_, block_statement_4(builder_, level_ + 1)) && result_;
     result_ = pinned_ && report_error_(builder_, block_statement_5(builder_, level_ + 1)) && result_;
     result_ = pinned_ && report_error_(builder_, block_statement_6(builder_, level_ + 1)) && result_;
     result_ = pinned_ && report_error_(builder_, block_statement_7(builder_, level_ + 1)) && result_;
     result_ = pinned_ && block_tail(builder_, level_ + 1) && result_;
     if (result_ || pinned_) {
       marker_.done(BLOCK_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, pinned_, _SECTION_GENERAL_, null);
     return result_ || pinned_;
   }
 
   // variable_name?
   private static boolean block_statement_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "block_statement_2")) return false;
     variable_name(builder_, level_ + 1);
     return true;
   }
 
   // body?
   private static boolean block_statement_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "block_statement_4")) return false;
     body(builder_, level_ + 1);
     return true;
   }
 
   // afterwards_statement?
   private static boolean block_statement_5(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "block_statement_5")) return false;
     afterwards_statement(builder_, level_ + 1);
     return true;
   }
 
   // cleanup_statement?
   private static boolean block_statement_6(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "block_statement_6")) return false;
     cleanup_statement(builder_, level_ + 1);
     return true;
   }
 
   // exception_statement*
   private static boolean block_statement_7(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "block_statement_7")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!exception_statement(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "block_statement_7");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   /* ********************************************************** */
   // END BLOCK?
   public static boolean block_tail(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "block_tail")) return false;
     if (!nextTokenIs(builder_, END)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, END);
     result_ = result_ && block_tail_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(BLOCK_TAIL);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // BLOCK?
   private static boolean block_tail_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "block_tail_1")) return false;
     consumeToken(builder_, BLOCK);
     return true;
   }
 
   /* ********************************************************** */
   // constituents SEMICOLON?
   public static boolean body(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "body")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<body>");
     result_ = constituents(builder_, level_ + 1);
     result_ = result_ && body_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(BODY);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // SEMICOLON?
   private static boolean body_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "body_1")) return false;
     consumeToken(builder_, SEMICOLON);
     return true;
   }
 
   /* ********************************************************** */
   // statement non_statement_body_fragment? | non_statement_body_fragment
   public static boolean body_fragment(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "body_fragment")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<body fragment>");
     result_ = body_fragment_0(builder_, level_ + 1);
     if (!result_) result_ = non_statement_body_fragment(builder_, level_ + 1);
     if (result_) {
       marker_.done(BODY_FRAGMENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // statement non_statement_body_fragment?
   private static boolean body_fragment_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "body_fragment_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = statement(builder_, level_ + 1);
     result_ = result_ && body_fragment_0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // non_statement_body_fragment?
   private static boolean body_fragment_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "body_fragment_0_1")) return false;
     non_statement_body_fragment(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // LBRACE DEFINE pattern SEMICOLON? END RBRACE EQUAL_ARROW rhs
   public static boolean body_style_definition_rule(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "body_style_definition_rule")) return false;
     if (!nextTokenIs(builder_, LBRACE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, LBRACE, DEFINE);
     result_ = result_ && pattern(builder_, level_ + 1);
     result_ = result_ && body_style_definition_rule_3(builder_, level_ + 1);
     result_ = result_ && consumeTokens(builder_, 0, END, RBRACE, EQUAL_ARROW);
     result_ = result_ && rhs(builder_, level_ + 1);
     if (result_) {
       marker_.done(BODY_STYLE_DEFINITION_RULE);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // SEMICOLON?
   private static boolean body_style_definition_rule_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "body_style_definition_rule_3")) return false;
     consumeToken(builder_, SEMICOLON);
     return true;
   }
 
   /* ********************************************************** */
   // body_style_definition_rule+
   public static boolean body_style_definition_rules(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "body_style_definition_rules")) return false;
     if (!nextTokenIs(builder_, LBRACE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = body_style_definition_rule(builder_, level_ + 1);
     int offset_ = builder_.getCurrentOffset();
     while (result_) {
       if (!body_style_definition_rule(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "body_style_definition_rules");
         break;
       }
       offset_ = next_offset_;
     }
     if (result_) {
       marker_.done(BODY_STYLE_DEFINITION_RULES);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // LPAREN body_fragment? RPAREN
   //     | LBRACKET body_fragment? RBRACKET
   //     | LBRACE body_fragment? RBRACE
   public static boolean bracketed_fragment(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "bracketed_fragment")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<bracketed fragment>");
     result_ = bracketed_fragment_0(builder_, level_ + 1);
     if (!result_) result_ = bracketed_fragment_1(builder_, level_ + 1);
     if (!result_) result_ = bracketed_fragment_2(builder_, level_ + 1);
     if (result_) {
       marker_.done(BRACKETED_FRAGMENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // LPAREN body_fragment? RPAREN
   private static boolean bracketed_fragment_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "bracketed_fragment_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LPAREN);
     result_ = result_ && bracketed_fragment_0_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // body_fragment?
   private static boolean bracketed_fragment_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "bracketed_fragment_0_1")) return false;
     body_fragment(builder_, level_ + 1);
     return true;
   }
 
   // LBRACKET body_fragment? RBRACKET
   private static boolean bracketed_fragment_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "bracketed_fragment_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LBRACKET);
     result_ = result_ && bracketed_fragment_1_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RBRACKET);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // body_fragment?
   private static boolean bracketed_fragment_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "bracketed_fragment_1_1")) return false;
     body_fragment(builder_, level_ + 1);
     return true;
   }
 
   // LBRACE body_fragment? RBRACE
   private static boolean bracketed_fragment_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "bracketed_fragment_2")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LBRACE);
     result_ = result_ && bracketed_fragment_2_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RBRACE);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // body_fragment?
   private static boolean bracketed_fragment_2_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "bracketed_fragment_2_1")) return false;
     body_fragment(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // LPAREN pattern? RPAREN
   //     | LBRACKET pattern? RBRACKET
   //     | LBRACE pattern? RBRACE
   public static boolean bracketed_pattern(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "bracketed_pattern")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<bracketed pattern>");
     result_ = bracketed_pattern_0(builder_, level_ + 1);
     if (!result_) result_ = bracketed_pattern_1(builder_, level_ + 1);
     if (!result_) result_ = bracketed_pattern_2(builder_, level_ + 1);
     if (result_) {
       marker_.done(BRACKETED_PATTERN);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // LPAREN pattern? RPAREN
   private static boolean bracketed_pattern_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "bracketed_pattern_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LPAREN);
     result_ = result_ && bracketed_pattern_0_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // pattern?
   private static boolean bracketed_pattern_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "bracketed_pattern_0_1")) return false;
     pattern(builder_, level_ + 1);
     return true;
   }
 
   // LBRACKET pattern? RBRACKET
   private static boolean bracketed_pattern_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "bracketed_pattern_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LBRACKET);
     result_ = result_ && bracketed_pattern_1_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RBRACKET);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // pattern?
   private static boolean bracketed_pattern_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "bracketed_pattern_1_1")) return false;
     pattern(builder_, level_ + 1);
     return true;
   }
 
   // LBRACE pattern? RBRACE
   private static boolean bracketed_pattern_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "bracketed_pattern_2")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LBRACE);
     result_ = result_ && bracketed_pattern_2_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RBRACE);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // pattern?
   private static boolean bracketed_pattern_2_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "bracketed_pattern_2_1")) return false;
     pattern(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // LPAREN | RPAREN | LBRACKET | RBRACKET | LBRACE | RBRACE | HASH_PAREN | HASH_BRACKET
   public static boolean bracketing_punctuation(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "bracketing_punctuation")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<bracketing punctuation>");
     result_ = consumeToken(builder_, LPAREN);
     if (!result_) result_ = consumeToken(builder_, RPAREN);
     if (!result_) result_ = consumeToken(builder_, LBRACKET);
     if (!result_) result_ = consumeToken(builder_, RBRACKET);
     if (!result_) result_ = consumeToken(builder_, LBRACE);
     if (!result_) result_ = consumeToken(builder_, RBRACE);
     if (!result_) result_ = consumeToken(builder_, HASH_PAREN);
     if (!result_) result_ = consumeToken(builder_, HASH_BRACKET);
     if (result_) {
       marker_.done(BRACKETING_PUNCTUATION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // cases SEMICOLON?
   public static boolean case_body(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_body")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<case body>");
     result_ = cases(builder_, level_ + 1);
     result_ = result_ && case_body_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(CASE_BODY);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // SEMICOLON?
   private static boolean case_body_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_body_1")) return false;
     consumeToken(builder_, SEMICOLON);
     return true;
   }
 
   /* ********************************************************** */
   // !case_label constituent
   public static boolean case_constituent(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_constituent")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<case constituent>");
     result_ = case_constituent_0(builder_, level_ + 1);
     result_ = result_ && constituent(builder_, level_ + 1);
     if (result_) {
       marker_.done(CASE_CONSTITUENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // !case_label
   private static boolean case_constituent_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_constituent_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_NOT_, null);
     result_ = !case_label(builder_, level_ + 1);
     marker_.rollbackTo();
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_NOT_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // case_constituent (SEMICOLON case_constituent)*
   public static boolean case_constituents(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_constituents")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<case constituents>");
     result_ = case_constituent(builder_, level_ + 1);
     result_ = result_ && case_constituents_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(CASE_CONSTITUENTS);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (SEMICOLON case_constituent)*
   private static boolean case_constituents_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_constituents_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!case_constituents_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "case_constituents_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // SEMICOLON case_constituent
   private static boolean case_constituents_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_constituents_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, SEMICOLON);
     result_ = result_ && case_constituent(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // LPAREN expression COMMA expressions RPAREN EQUAL_ARROW
   //     | expressions EQUAL_ARROW
   //     | OTHERWISE EQUAL_ARROW?
   public static boolean case_label(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_label")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<case label>");
     result_ = case_label_0(builder_, level_ + 1);
     if (!result_) result_ = case_label_1(builder_, level_ + 1);
     if (!result_) result_ = case_label_2(builder_, level_ + 1);
     if (result_) {
       marker_.done(CASE_LABEL);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // LPAREN expression COMMA expressions RPAREN EQUAL_ARROW
   private static boolean case_label_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_label_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LPAREN);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     result_ = result_ && consumeToken(builder_, COMMA);
     result_ = result_ && expressions(builder_, level_ + 1);
     result_ = result_ && consumeTokens(builder_, 0, RPAREN, EQUAL_ARROW);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // expressions EQUAL_ARROW
   private static boolean case_label_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_label_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = expressions(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, EQUAL_ARROW);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // OTHERWISE EQUAL_ARROW?
   private static boolean case_label_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_label_2")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, OTHERWISE);
     result_ = result_ && case_label_2_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // EQUAL_ARROW?
   private static boolean case_label_2_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_label_2_1")) return false;
     consumeToken(builder_, EQUAL_ARROW);
     return true;
   }
 
   /* ********************************************************** */
   // CASE case_stmt_clause* case_stmt_tail
   public static boolean case_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_statement")) return false;
     if (!nextTokenIs(builder_, CASE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, CASE);
     result_ = result_ && case_statement_1(builder_, level_ + 1);
     result_ = result_ && case_stmt_tail(builder_, level_ + 1);
     if (result_) {
       marker_.done(CASE_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // case_stmt_clause*
   private static boolean case_statement_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_statement_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!case_stmt_clause(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "case_statement_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   /* ********************************************************** */
   // case_stmt_label case_stmt_constituents? (SEMICOLON | &case_stmt_tail)
   public static boolean case_stmt_clause(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_stmt_clause")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<case stmt clause>");
     result_ = case_stmt_label(builder_, level_ + 1);
     result_ = result_ && case_stmt_clause_1(builder_, level_ + 1);
     result_ = result_ && case_stmt_clause_2(builder_, level_ + 1);
     if (result_) {
       marker_.done(CASE_STMT_CLAUSE);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // case_stmt_constituents?
   private static boolean case_stmt_clause_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_stmt_clause_1")) return false;
     case_stmt_constituents(builder_, level_ + 1);
     return true;
   }
 
   // SEMICOLON | &case_stmt_tail
   private static boolean case_stmt_clause_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_stmt_clause_2")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, SEMICOLON);
     if (!result_) result_ = case_stmt_clause_2_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // &case_stmt_tail
   private static boolean case_stmt_clause_2_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_stmt_clause_2_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_AND_, null);
     result_ = case_stmt_tail(builder_, level_ + 1);
     marker_.rollbackTo();
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_AND_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // !case_stmt_label constituent
   public static boolean case_stmt_constituent(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_stmt_constituent")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<case stmt constituent>");
     result_ = case_stmt_constituent_0(builder_, level_ + 1);
     result_ = result_ && constituent(builder_, level_ + 1);
     if (result_) {
       marker_.done(CASE_STMT_CONSTITUENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // !case_stmt_label
   private static boolean case_stmt_constituent_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_stmt_constituent_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_NOT_, null);
     result_ = !case_stmt_label(builder_, level_ + 1);
     marker_.rollbackTo();
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_NOT_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // case_stmt_constituent (SEMICOLON case_stmt_constituent)*
   public static boolean case_stmt_constituents(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_stmt_constituents")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<case stmt constituents>");
     result_ = case_stmt_constituent(builder_, level_ + 1);
     result_ = result_ && case_stmt_constituents_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(CASE_STMT_CONSTITUENTS);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (SEMICOLON case_stmt_constituent)*
   private static boolean case_stmt_constituents_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_stmt_constituents_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!case_stmt_constituents_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "case_stmt_constituents_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // SEMICOLON case_stmt_constituent
   private static boolean case_stmt_constituents_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_stmt_constituents_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, SEMICOLON);
     result_ = result_ && case_stmt_constituent(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // OTHERWISE EQUAL_ARROW?
   //     | expression EQUAL_ARROW
   public static boolean case_stmt_label(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_stmt_label")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<case stmt label>");
     result_ = case_stmt_label_0(builder_, level_ + 1);
     if (!result_) result_ = case_stmt_label_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(CASE_STMT_LABEL);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // OTHERWISE EQUAL_ARROW?
   private static boolean case_stmt_label_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_stmt_label_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, OTHERWISE);
     result_ = result_ && case_stmt_label_0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // EQUAL_ARROW?
   private static boolean case_stmt_label_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_stmt_label_0_1")) return false;
     consumeToken(builder_, EQUAL_ARROW);
     return true;
   }
 
   // expression EQUAL_ARROW
   private static boolean case_stmt_label_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_stmt_label_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = expression(builder_, level_ + 1, -1);
     result_ = result_ && consumeToken(builder_, EQUAL_ARROW);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // END CASE?
   public static boolean case_stmt_tail(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_stmt_tail")) return false;
     if (!nextTokenIs(builder_, END)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, END);
     result_ = result_ && case_stmt_tail_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(CASE_STMT_TAIL);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // CASE?
   private static boolean case_stmt_tail_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_stmt_tail_1")) return false;
     consumeToken(builder_, CASE);
     return true;
   }
 
   /* ********************************************************** */
   // case_label case_constituents? (SEMICOLON case_constituents)*
   public static boolean cases(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "cases")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<cases>");
     result_ = case_label(builder_, level_ + 1);
     result_ = result_ && cases_1(builder_, level_ + 1);
     result_ = result_ && cases_2(builder_, level_ + 1);
     if (result_) {
       marker_.done(CASES);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // case_constituents?
   private static boolean cases_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "cases_1")) return false;
     case_constituents(builder_, level_ + 1);
     return true;
   }
 
   // (SEMICOLON case_constituents)*
   private static boolean cases_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "cases_2")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!cases_2_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "cases_2");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // SEMICOLON case_constituents
   private static boolean cases_2_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "cases_2_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, SEMICOLON);
     result_ = result_ && case_constituents(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // END CLASS variable_name? | END variable_name?
   public static boolean class_definition_tail(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_definition_tail")) return false;
     if (!nextTokenIs(builder_, END)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = class_definition_tail_0(builder_, level_ + 1);
     if (!result_) result_ = class_definition_tail_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(CLASS_DEFINITION_TAIL);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // END CLASS variable_name?
   private static boolean class_definition_tail_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_definition_tail_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, END, CLASS);
     result_ = result_ && class_definition_tail_0_2(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // variable_name?
   private static boolean class_definition_tail_0_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_definition_tail_0_2")) return false;
     variable_name(builder_, level_ + 1);
     return true;
   }
 
   // END variable_name?
   private static boolean class_definition_tail_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_definition_tail_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, END);
     result_ = result_ && class_definition_tail_1_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // variable_name?
   private static boolean class_definition_tail_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_definition_tail_1_1")) return false;
     variable_name(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // <<clauseOption>>
   public static boolean clause_option(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "clause_option")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<clause option>");
     result_ = clauseOption(builder_, level_ + 1);
     if (result_) {
       marker_.done(CLAUSE_OPTION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // CLEANUP body?
   public static boolean cleanup_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "cleanup_statement")) return false;
     if (!nextTokenIs(builder_, CLEANUP)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, CLEANUP);
     result_ = result_ && cleanup_statement_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(CLEANUP_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // body?
   private static boolean cleanup_statement_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "cleanup_statement_1")) return false;
     body(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // variable IN expression (USING expression)?
   public static boolean collection_clause(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "collection_clause")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<collection clause>");
     result_ = variable(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, IN);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     result_ = result_ && collection_clause_3(builder_, level_ + 1);
     if (result_) {
       marker_.done(COLLECTION_CLAUSE);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (USING expression)?
   private static boolean collection_clause_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "collection_clause_3")) return false;
     collection_clause_3_0(builder_, level_ + 1);
     return true;
   }
 
   // USING expression
   private static boolean collection_clause_3_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "collection_clause_3_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, USING);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // operand_expr
   //     | LPAREN operand_expr COMMA property_list RPAREN
   public static boolean condition(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "condition")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<condition>");
     result_ = operand_expr(builder_, level_ + 1);
     if (!result_) result_ = condition_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(CONDITION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // LPAREN operand_expr COMMA property_list RPAREN
   private static boolean condition_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "condition_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LPAREN);
     result_ = result_ && operand_expr(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, COMMA);
     result_ = result_ && property_list(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // literal | symbol
   public static boolean constant(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "constant")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<constant>");
     result_ = literal(builder_, level_ + 1);
     if (!result_) result_ = symbol(builder_, level_ + 1);
     if (result_) {
       marker_.done(CONSTANT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // NUMBER
   //     | CHARACTER_LITERAL
   //     | string
   //     | symbol
   //     | HASH_PAREN constants DOT constant RPAREN
   //     | HASH_PAREN constants? RPAREN
   //     | HASH_BRACKET constants? RBRACKET
   //     | PARSED_LIST_CONSTANT
   //     | PARSED_VECTOR_CONSTANT
   public static boolean constant_fragment(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "constant_fragment")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<constant fragment>");
     result_ = consumeToken(builder_, NUMBER);
     if (!result_) result_ = consumeToken(builder_, CHARACTER_LITERAL);
     if (!result_) result_ = string(builder_, level_ + 1);
     if (!result_) result_ = symbol(builder_, level_ + 1);
     if (!result_) result_ = constant_fragment_4(builder_, level_ + 1);
     if (!result_) result_ = constant_fragment_5(builder_, level_ + 1);
     if (!result_) result_ = constant_fragment_6(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, PARSED_LIST_CONSTANT);
     if (!result_) result_ = consumeToken(builder_, PARSED_VECTOR_CONSTANT);
     if (result_) {
       marker_.done(CONSTANT_FRAGMENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // HASH_PAREN constants DOT constant RPAREN
   private static boolean constant_fragment_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "constant_fragment_4")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_PAREN);
     result_ = result_ && constants(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, DOT);
     result_ = result_ && constant(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // HASH_PAREN constants? RPAREN
   private static boolean constant_fragment_5(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "constant_fragment_5")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_PAREN);
     result_ = result_ && constant_fragment_5_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // constants?
   private static boolean constant_fragment_5_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "constant_fragment_5_1")) return false;
     constants(builder_, level_ + 1);
     return true;
   }
 
   // HASH_BRACKET constants? RBRACKET
   private static boolean constant_fragment_6(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "constant_fragment_6")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_BRACKET);
     result_ = result_ && constant_fragment_6_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RBRACKET);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // constants?
   private static boolean constant_fragment_6_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "constant_fragment_6_1")) return false;
     constants(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // constant (COMMA constant)*
   public static boolean constants(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "constants")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<constants>");
     result_ = constant(builder_, level_ + 1);
     result_ = result_ && constants_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(CONSTANTS);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (COMMA constant)*
   private static boolean constants_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "constants_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!constants_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "constants_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA constant
   private static boolean constants_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "constants_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && constant(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // definition | local_declaration | expression
   static boolean constituent(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "constituent")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = definition(builder_, level_ + 1);
     if (!result_) result_ = local_declaration(builder_, level_ + 1);
     if (!result_) result_ = expression(builder_, level_ + 1, -1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // constituent (SEMICOLON constituent)* | COMMENT
   static boolean constituents(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "constituents")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = constituents_0(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, COMMENT);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // constituent (SEMICOLON constituent)*
   private static boolean constituents_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "constituents_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = constituent(builder_, level_ + 1);
     result_ = result_ && constituents_0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (SEMICOLON constituent)*
   private static boolean constituents_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "constituents_0_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!constituents_0_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "constituents_0_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // SEMICOLON constituent
   private static boolean constituents_0_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "constituents_0_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, SEMICOLON);
     result_ = result_ && constituent(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // non_end_core_word | END
   public static boolean core_word(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "core_word")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<core word>");
     result_ = non_end_core_word(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, END);
     if (result_) {
       marker_.done(CORE_WORD);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // CREATE (ordinary_binding_name (COMMA ordinary_binding_name)*)?
   public static boolean create_clause(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "create_clause")) return false;
     if (!nextTokenIs(builder_, CREATE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, CREATE);
     result_ = result_ && create_clause_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(CREATE_CLAUSE);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // (ordinary_binding_name (COMMA ordinary_binding_name)*)?
   private static boolean create_clause_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "create_clause_1")) return false;
     create_clause_1_0(builder_, level_ + 1);
     return true;
   }
 
   // ordinary_binding_name (COMMA ordinary_binding_name)*
   private static boolean create_clause_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "create_clause_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = ordinary_binding_name(builder_, level_ + 1);
     result_ = result_ && create_clause_1_0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (COMMA ordinary_binding_name)*
   private static boolean create_clause_1_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "create_clause_1_0_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!create_clause_1_0_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "create_clause_1_0_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA ordinary_binding_name
   private static boolean create_clause_1_0_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "create_clause_1_0_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && ordinary_binding_name(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // EQUAL expression
   public static boolean default_value(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "default_value")) return false;
     if (!nextTokenIs(builder_, EQUAL)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, EQUAL);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     if (result_) {
       marker_.done(DEFAULT_VALUE);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // DEFINE_BODY_NONEXPRESSION_WORD | DEFINE_BODY_BEGIN_WORD | DEFINE_BODY_FUNCTION_WORD
   public static boolean define_body_word(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "define_body_word")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<define body word>");
     result_ = consumeToken(builder_, DEFINE_BODY_NONEXPRESSION_WORD);
     if (!result_) result_ = consumeToken(builder_, DEFINE_BODY_BEGIN_WORD);
     if (!result_) result_ = consumeToken(builder_, DEFINE_BODY_FUNCTION_WORD);
     if (result_) {
       marker_.done(DEFINE_BODY_WORD);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // DEFINE_LIST_NONEXPRESSION_WORD | DEFINE_LIST_BEGIN_WORD | DEFINE_LIST_FUNCTION_WORD
   public static boolean define_list_word(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "define_list_word")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<define list word>");
     result_ = consumeToken(builder_, DEFINE_LIST_NONEXPRESSION_WORD);
     if (!result_) result_ = consumeToken(builder_, DEFINE_LIST_BEGIN_WORD);
     if (!result_) result_ = consumeToken(builder_, DEFINE_LIST_FUNCTION_WORD);
     if (result_) {
       marker_.done(DEFINE_LIST_WORD);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // definition_class_definer
   //     | definition_constant_definer
   //     | definition_copy_down_method_definer
   //     | definition_domain_definer
   //     | definition_function_definer
   //     | definition_generic_definer
   //     | definition_library_definer
   //     | definition_module_definer
   //     | definition_macro_definer
   //     | definition_method_definer
   //     | definition_shared_symbols_definer
   //     | definition_suite_definer
   //     | definition_test_definer
   //     | definition_variable_definer
   //     | definition_macro_call
   //     | PARSED_DEFINITION
   public static boolean definition(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition")) return false;
     if (!nextTokenIs(builder_, DEFINE) && !nextTokenIs(builder_, PARSED_DEFINITION)
         && replaceVariants(builder_, 2, "<definition>")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<definition>");
     result_ = definition_class_definer(builder_, level_ + 1);
     if (!result_) result_ = definition_constant_definer(builder_, level_ + 1);
     if (!result_) result_ = definition_copy_down_method_definer(builder_, level_ + 1);
     if (!result_) result_ = definition_domain_definer(builder_, level_ + 1);
     if (!result_) result_ = definition_function_definer(builder_, level_ + 1);
     if (!result_) result_ = definition_generic_definer(builder_, level_ + 1);
     if (!result_) result_ = definition_library_definer(builder_, level_ + 1);
     if (!result_) result_ = definition_module_definer(builder_, level_ + 1);
     if (!result_) result_ = definition_macro_definer(builder_, level_ + 1);
     if (!result_) result_ = definition_method_definer(builder_, level_ + 1);
     if (!result_) result_ = definition_shared_symbols_definer(builder_, level_ + 1);
     if (!result_) result_ = definition_suite_definer(builder_, level_ + 1);
     if (!result_) result_ = definition_test_definer(builder_, level_ + 1);
     if (!result_) result_ = definition_variable_definer(builder_, level_ + 1);
     if (!result_) result_ = definition_macro_call(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, PARSED_DEFINITION);
     if (result_) {
       marker_.done(DEFINITION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // DEFINE modifiers? CLASS variable_name LPAREN supers? RPAREN slot_declarations? class_definition_tail
   public static boolean definition_class_definer(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_class_definer")) return false;
     if (!nextTokenIs(builder_, DEFINE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, DEFINE);
     result_ = result_ && definition_class_definer_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLASS);
     result_ = result_ && variable_name(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, LPAREN);
     result_ = result_ && definition_class_definer_5(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     result_ = result_ && definition_class_definer_7(builder_, level_ + 1);
     result_ = result_ && class_definition_tail(builder_, level_ + 1);
     if (result_) {
       marker_.done(DEFINITION_CLASS_DEFINER);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // modifiers?
   private static boolean definition_class_definer_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_class_definer_1")) return false;
     modifiers(builder_, level_ + 1);
     return true;
   }
 
   // supers?
   private static boolean definition_class_definer_5(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_class_definer_5")) return false;
     supers(builder_, level_ + 1);
     return true;
   }
 
   // slot_declarations?
   private static boolean definition_class_definer_7(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_class_definer_7")) return false;
     slot_declarations(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // DEFINE modifiers? CONSTANT_T (variable | variable_list) EQUAL expression
   public static boolean definition_constant_definer(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_constant_definer")) return false;
     if (!nextTokenIs(builder_, DEFINE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, DEFINE);
     result_ = result_ && definition_constant_definer_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CONSTANT_T);
     result_ = result_ && definition_constant_definer_3(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, EQUAL);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     if (result_) {
       marker_.done(DEFINITION_CONSTANT_DEFINER);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // modifiers?
   private static boolean definition_constant_definer_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_constant_definer_1")) return false;
     modifiers(builder_, level_ + 1);
     return true;
   }
 
   // variable | variable_list
   private static boolean definition_constant_definer_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_constant_definer_3")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = variable(builder_, level_ + 1);
     if (!result_) result_ = variable_list(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // DEFINE modifiers? COPY_DOWN_METHOD variable_name list_fragment?
   public static boolean definition_copy_down_method_definer(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_copy_down_method_definer")) return false;
     if (!nextTokenIs(builder_, DEFINE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, DEFINE);
     result_ = result_ && definition_copy_down_method_definer_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, COPY_DOWN_METHOD);
     result_ = result_ && variable_name(builder_, level_ + 1);
     result_ = result_ && definition_copy_down_method_definer_4(builder_, level_ + 1);
     if (result_) {
       marker_.done(DEFINITION_COPY_DOWN_METHOD_DEFINER);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // modifiers?
   private static boolean definition_copy_down_method_definer_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_copy_down_method_definer_1")) return false;
     modifiers(builder_, level_ + 1);
     return true;
   }
 
   // list_fragment?
   private static boolean definition_copy_down_method_definer_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_copy_down_method_definer_4")) return false;
     list_fragment(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // DEFINE modifiers? DOMAIN variable_name bracketed_fragment
   public static boolean definition_domain_definer(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_domain_definer")) return false;
     if (!nextTokenIs(builder_, DEFINE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, DEFINE);
     result_ = result_ && definition_domain_definer_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, DOMAIN);
     result_ = result_ && variable_name(builder_, level_ + 1);
     result_ = result_ && bracketed_fragment(builder_, level_ + 1);
     if (result_) {
       marker_.done(DEFINITION_DOMAIN_DEFINER);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // modifiers?
   private static boolean definition_domain_definer_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_domain_definer_1")) return false;
     modifiers(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // DEFINE modifiers? FUNCTION variable_name parameter_list body? function_definition_tail
   public static boolean definition_function_definer(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_function_definer")) return false;
     if (!nextTokenIs(builder_, DEFINE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, DEFINE);
     result_ = result_ && definition_function_definer_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, FUNCTION);
     result_ = result_ && variable_name(builder_, level_ + 1);
     result_ = result_ && parameter_list(builder_, level_ + 1);
     result_ = result_ && definition_function_definer_5(builder_, level_ + 1);
     result_ = result_ && function_definition_tail(builder_, level_ + 1);
     if (result_) {
       marker_.done(DEFINITION_FUNCTION_DEFINER);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // modifiers?
   private static boolean definition_function_definer_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_function_definer_1")) return false;
     modifiers(builder_, level_ + 1);
     return true;
   }
 
   // body?
   private static boolean definition_function_definer_5(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_function_definer_5")) return false;
     body(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // DEFINE modifiers? GENERIC variable_name list_fragment
   public static boolean definition_generic_definer(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_generic_definer")) return false;
     if (!nextTokenIs(builder_, DEFINE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, DEFINE);
     result_ = result_ && definition_generic_definer_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, GENERIC);
     result_ = result_ && variable_name(builder_, level_ + 1);
     result_ = result_ && list_fragment(builder_, level_ + 1);
     if (result_) {
       marker_.done(DEFINITION_GENERIC_DEFINER);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // modifiers?
   private static boolean definition_generic_definer_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_generic_definer_1")) return false;
     modifiers(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // DEFINE LIBRARY variable_name ((export_clause|use_clause) SEMICOLON)* library_definition_tail
   public static boolean definition_library_definer(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_library_definer")) return false;
     if (!nextTokenIs(builder_, DEFINE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, DEFINE, LIBRARY);
     result_ = result_ && variable_name(builder_, level_ + 1);
     result_ = result_ && definition_library_definer_3(builder_, level_ + 1);
     result_ = result_ && library_definition_tail(builder_, level_ + 1);
     if (result_) {
       marker_.done(DEFINITION_LIBRARY_DEFINER);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // ((export_clause|use_clause) SEMICOLON)*
   private static boolean definition_library_definer_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_library_definer_3")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!definition_library_definer_3_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "definition_library_definer_3");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // (export_clause|use_clause) SEMICOLON
   private static boolean definition_library_definer_3_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_library_definer_3_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = definition_library_definer_3_0_0(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, SEMICOLON);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // export_clause|use_clause
   private static boolean definition_library_definer_3_0_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_library_definer_3_0_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = export_clause(builder_, level_ + 1);
     if (!result_) result_ = use_clause(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // DEFINE modifiers? define_body_word body_fragment? definition_tail
   //     | DEFINE modifiers? define_list_word list_fragment?
   public static boolean definition_macro_call(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_macro_call")) return false;
     if (!nextTokenIs(builder_, DEFINE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = definition_macro_call_0(builder_, level_ + 1);
     if (!result_) result_ = definition_macro_call_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(DEFINITION_MACRO_CALL);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // DEFINE modifiers? define_body_word body_fragment? definition_tail
   private static boolean definition_macro_call_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_macro_call_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, DEFINE);
     result_ = result_ && definition_macro_call_0_1(builder_, level_ + 1);
     result_ = result_ && define_body_word(builder_, level_ + 1);
     result_ = result_ && definition_macro_call_0_3(builder_, level_ + 1);
     result_ = result_ && definition_tail(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // modifiers?
   private static boolean definition_macro_call_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_macro_call_0_1")) return false;
     modifiers(builder_, level_ + 1);
     return true;
   }
 
   // body_fragment?
   private static boolean definition_macro_call_0_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_macro_call_0_3")) return false;
     body_fragment(builder_, level_ + 1);
     return true;
   }
 
   // DEFINE modifiers? define_list_word list_fragment?
   private static boolean definition_macro_call_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_macro_call_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, DEFINE);
     result_ = result_ && definition_macro_call_1_1(builder_, level_ + 1);
     result_ = result_ && define_list_word(builder_, level_ + 1);
     result_ = result_ && definition_macro_call_1_3(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // modifiers?
   private static boolean definition_macro_call_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_macro_call_1_1")) return false;
     modifiers(builder_, level_ + 1);
     return true;
   }
 
   // list_fragment?
   private static boolean definition_macro_call_1_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_macro_call_1_3")) return false;
     list_fragment(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // DEFINE modifiers? MACRO_T macro_definition
   public static boolean definition_macro_definer(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_macro_definer")) return false;
     if (!nextTokenIs(builder_, DEFINE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, DEFINE);
     result_ = result_ && definition_macro_definer_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, MACRO_T);
     result_ = result_ && macro_definition(builder_, level_ + 1);
     if (result_) {
       marker_.done(DEFINITION_MACRO_DEFINER);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // modifiers?
   private static boolean definition_macro_definer_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_macro_definer_1")) return false;
     modifiers(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // DEFINE modifiers? METHOD variable_name parameter_list body? method_definition_tail
   public static boolean definition_method_definer(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_method_definer")) return false;
     if (!nextTokenIs(builder_, DEFINE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, DEFINE);
     result_ = result_ && definition_method_definer_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, METHOD);
     result_ = result_ && variable_name(builder_, level_ + 1);
     result_ = result_ && parameter_list(builder_, level_ + 1);
     result_ = result_ && definition_method_definer_5(builder_, level_ + 1);
     result_ = result_ && method_definition_tail(builder_, level_ + 1);
     if (result_) {
       marker_.done(DEFINITION_METHOD_DEFINER);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // modifiers?
   private static boolean definition_method_definer_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_method_definer_1")) return false;
     modifiers(builder_, level_ + 1);
     return true;
   }
 
   // body?
   private static boolean definition_method_definer_5(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_method_definer_5")) return false;
     body(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // DEFINE MODULE variable_name ((export_clause|create_clause|use_clause) SEMICOLON)* module_definition_tail
   public static boolean definition_module_definer(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_module_definer")) return false;
     if (!nextTokenIs(builder_, DEFINE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, DEFINE, MODULE);
     result_ = result_ && variable_name(builder_, level_ + 1);
     result_ = result_ && definition_module_definer_3(builder_, level_ + 1);
     result_ = result_ && module_definition_tail(builder_, level_ + 1);
     if (result_) {
       marker_.done(DEFINITION_MODULE_DEFINER);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // ((export_clause|create_clause|use_clause) SEMICOLON)*
   private static boolean definition_module_definer_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_module_definer_3")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!definition_module_definer_3_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "definition_module_definer_3");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // (export_clause|create_clause|use_clause) SEMICOLON
   private static boolean definition_module_definer_3_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_module_definer_3_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = definition_module_definer_3_0_0(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, SEMICOLON);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // export_clause|create_clause|use_clause
   private static boolean definition_module_definer_3_0_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_module_definer_3_0_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = export_clause(builder_, level_ + 1);
     if (!result_) result_ = create_clause(builder_, level_ + 1);
     if (!result_) result_ = use_clause(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // DEFINE modifiers? SHARED_SYMBOLS_T variable_name shared_symbols? shared_symbols_definition_tail
   public static boolean definition_shared_symbols_definer(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_shared_symbols_definer")) return false;
     if (!nextTokenIs(builder_, DEFINE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, DEFINE);
     result_ = result_ && definition_shared_symbols_definer_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, SHARED_SYMBOLS_T);
     result_ = result_ && variable_name(builder_, level_ + 1);
     result_ = result_ && definition_shared_symbols_definer_4(builder_, level_ + 1);
     result_ = result_ && shared_symbols_definition_tail(builder_, level_ + 1);
     if (result_) {
       marker_.done(DEFINITION_SHARED_SYMBOLS_DEFINER);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // modifiers?
   private static boolean definition_shared_symbols_definer_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_shared_symbols_definer_1")) return false;
     modifiers(builder_, level_ + 1);
     return true;
   }
 
   // shared_symbols?
   private static boolean definition_shared_symbols_definer_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_shared_symbols_definer_4")) return false;
     shared_symbols(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // DEFINE SUITE variable_name LPAREN RPAREN suite_components? suite_definition_tail
   public static boolean definition_suite_definer(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_suite_definer")) return false;
     if (!nextTokenIs(builder_, DEFINE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, DEFINE, SUITE);
     result_ = result_ && variable_name(builder_, level_ + 1);
     result_ = result_ && consumeTokens(builder_, 0, LPAREN, RPAREN);
     result_ = result_ && definition_suite_definer_5(builder_, level_ + 1);
     result_ = result_ && suite_definition_tail(builder_, level_ + 1);
     if (result_) {
       marker_.done(DEFINITION_SUITE_DEFINER);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // suite_components?
   private static boolean definition_suite_definer_5(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_suite_definer_5")) return false;
     suite_components(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // END define_body_word macro_name | END macro_name | END
   public static boolean definition_tail(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_tail")) return false;
     if (!nextTokenIs(builder_, END)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = definition_tail_0(builder_, level_ + 1);
     if (!result_) result_ = definition_tail_1(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, END);
     if (result_) {
       marker_.done(DEFINITION_TAIL);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // END define_body_word macro_name
   private static boolean definition_tail_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_tail_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, END);
     result_ = result_ && define_body_word(builder_, level_ + 1);
     result_ = result_ && macro_name(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // END macro_name
   private static boolean definition_tail_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_tail_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, END);
     result_ = result_ && macro_name(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // DEFINE TEST variable_name LPAREN RPAREN body? test_definition_tail
   public static boolean definition_test_definer(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_test_definer")) return false;
     if (!nextTokenIs(builder_, DEFINE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, DEFINE, TEST);
     result_ = result_ && variable_name(builder_, level_ + 1);
     result_ = result_ && consumeTokens(builder_, 0, LPAREN, RPAREN);
     result_ = result_ && definition_test_definer_5(builder_, level_ + 1);
     result_ = result_ && test_definition_tail(builder_, level_ + 1);
     if (result_) {
       marker_.done(DEFINITION_TEST_DEFINER);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // body?
   private static boolean definition_test_definer_5(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_test_definer_5")) return false;
     body(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // DEFINE modifiers? VARIABLE_T (variable | variable_list) EQUAL expression
   public static boolean definition_variable_definer(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_variable_definer")) return false;
     if (!nextTokenIs(builder_, DEFINE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, DEFINE);
     result_ = result_ && definition_variable_definer_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, VARIABLE_T);
     result_ = result_ && definition_variable_definer_3(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, EQUAL);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     if (result_) {
       marker_.done(DEFINITION_VARIABLE_DEFINER);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // modifiers?
   private static boolean definition_variable_definer_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_variable_definer_1")) return false;
     modifiers(builder_, level_ + 1);
     return true;
   }
 
   // variable | variable_list
   private static boolean definition_variable_definer_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "definition_variable_definer_3")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = variable(builder_, level_ + 1);
     if (!result_) result_ = variable_list(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // headers source_records?
   static boolean dylanFile(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "dylanFile")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = headers(builder_, level_ + 1);
     result_ = result_ && dylanFile_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // source_records?
   private static boolean dylanFile_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "dylanFile_1")) return false;
     source_records(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // ABOVE
   //     | AFTERWARDS
   //     | ALL
   //     | BELOW
   //     | BY
   //     | COPY_DOWN_METHOD
   //     | CLEANUP
   //     | CREATE
   //     | EXPORT
   //     | FINALLY
   //     | FROM
   //     | IN
   //     | KEYED_BY
   //     | THEN
   //     | TO
   //     | USE
   //     | USING
   //     | CLASS
   //     | DOMAIN
   //     | EXCEPTION
   //     | FUNCTION
   //     | GENERIC
   //     | LIBRARY
   //     | METHOD
   //     | MODULE
   //     | SHARED_SYMBOLS_T
   //     | SLOT
   //     | SUITE
   //     | TEST
   //     | VARIABLE_T
   public static boolean dylan_unreserved_name(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "dylan_unreserved_name")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<dylan unreserved name>");
     result_ = consumeToken(builder_, ABOVE);
     if (!result_) result_ = consumeToken(builder_, AFTERWARDS);
     if (!result_) result_ = consumeToken(builder_, ALL);
     if (!result_) result_ = consumeToken(builder_, BELOW);
     if (!result_) result_ = consumeToken(builder_, BY);
     if (!result_) result_ = consumeToken(builder_, COPY_DOWN_METHOD);
     if (!result_) result_ = consumeToken(builder_, CLEANUP);
     if (!result_) result_ = consumeToken(builder_, CREATE);
     if (!result_) result_ = consumeToken(builder_, EXPORT);
     if (!result_) result_ = consumeToken(builder_, FINALLY);
     if (!result_) result_ = consumeToken(builder_, FROM);
     if (!result_) result_ = consumeToken(builder_, IN);
     if (!result_) result_ = consumeToken(builder_, KEYED_BY);
     if (!result_) result_ = consumeToken(builder_, THEN);
     if (!result_) result_ = consumeToken(builder_, TO);
     if (!result_) result_ = consumeToken(builder_, USE);
     if (!result_) result_ = consumeToken(builder_, USING);
     if (!result_) result_ = consumeToken(builder_, CLASS);
     if (!result_) result_ = consumeToken(builder_, DOMAIN);
     if (!result_) result_ = consumeToken(builder_, EXCEPTION);
     if (!result_) result_ = consumeToken(builder_, FUNCTION);
     if (!result_) result_ = consumeToken(builder_, GENERIC);
     if (!result_) result_ = consumeToken(builder_, LIBRARY);
     if (!result_) result_ = consumeToken(builder_, METHOD);
     if (!result_) result_ = consumeToken(builder_, MODULE);
     if (!result_) result_ = consumeToken(builder_, SHARED_SYMBOLS_T);
     if (!result_) result_ = consumeToken(builder_, SLOT);
     if (!result_) result_ = consumeToken(builder_, SUITE);
     if (!result_) result_ = consumeToken(builder_, TEST);
     if (!result_) result_ = consumeToken(builder_, VARIABLE_T);
     if (result_) {
       marker_.done(DYLAN_UNRESERVED_NAME);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // ELSE body?
   public static boolean else_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "else_statement")) return false;
     if (!nextTokenIs(builder_, ELSE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, ELSE);
     result_ = result_ && else_statement_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(ELSE_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // body?
   private static boolean else_statement_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "else_statement_1")) return false;
     body(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // ELSEIF LPAREN expression RPAREN body?
   public static boolean elseif_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "elseif_statement")) return false;
     if (!nextTokenIs(builder_, ELSEIF)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, ELSEIF, LPAREN);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     result_ = result_ && elseif_statement_4(builder_, level_ + 1);
     if (result_) {
       marker_.done(ELSEIF_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // body?
   private static boolean elseif_statement_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "elseif_statement_4")) return false;
     body(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // END begin_word?
   public static boolean end_clause(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "end_clause")) return false;
     if (!nextTokenIs(builder_, END)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, END);
     result_ = result_ && end_clause_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(END_CLAUSE);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // begin_word?
   private static boolean end_clause_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "end_clause_1")) return false;
     begin_word(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // KEYWORD expression
   public static boolean end_for_clause(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "end_for_clause")) return false;
     if (!nextTokenIs(builder_, KEYWORD)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, KEYWORD);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     if (result_) {
       marker_.done(END_FOR_CLAUSE);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // ESCAPED_WORD | OPERATOR_NAME
   public static boolean escaped_name(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "escaped_name")) return false;
     if (!nextTokenIs(builder_, ESCAPED_WORD) && !nextTokenIs(builder_, OPERATOR_NAME)
         && replaceVariants(builder_, 2, "<escaped name>")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<escaped name>");
     result_ = consumeToken(builder_, ESCAPED_WORD);
     if (!result_) result_ = consumeToken(builder_, OPERATOR_NAME);
     if (result_) {
       marker_.done(ESCAPED_NAME);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // KEYWORD expression
   public static boolean exception_options(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "exception_options")) return false;
     if (!nextTokenIs(builder_, KEYWORD)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, KEYWORD);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     if (result_) {
       marker_.done(EXCEPTION_OPTIONS);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // EXCEPTION LPAREN variable (COMMA exception_options)* RPAREN body?
   public static boolean exception_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "exception_statement")) return false;
     if (!nextTokenIs(builder_, EXCEPTION)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, EXCEPTION, LPAREN);
     result_ = result_ && variable(builder_, level_ + 1);
     result_ = result_ && exception_statement_3(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     result_ = result_ && exception_statement_5(builder_, level_ + 1);
     if (result_) {
       marker_.done(EXCEPTION_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // (COMMA exception_options)*
   private static boolean exception_statement_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "exception_statement_3")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!exception_statement_3_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "exception_statement_3");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA exception_options
   private static boolean exception_statement_3_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "exception_statement_3_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && exception_options(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // body?
   private static boolean exception_statement_5(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "exception_statement_5")) return false;
     body(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // KEYWORD LBRACE (variable_name (COMMA variable_name)*)? RBRACE
   public static boolean exclude_option(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "exclude_option")) return false;
     if (!nextTokenIs(builder_, KEYWORD)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, KEYWORD, LBRACE);
     result_ = result_ && exclude_option_2(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RBRACE);
     if (result_) {
       marker_.done(EXCLUDE_OPTION);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // (variable_name (COMMA variable_name)*)?
   private static boolean exclude_option_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "exclude_option_2")) return false;
     exclude_option_2_0(builder_, level_ + 1);
     return true;
   }
 
   // variable_name (COMMA variable_name)*
   private static boolean exclude_option_2_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "exclude_option_2_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = variable_name(builder_, level_ + 1);
     result_ = result_ && exclude_option_2_0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (COMMA variable_name)*
   private static boolean exclude_option_2_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "exclude_option_2_0_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!exclude_option_2_0_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "exclude_option_2_0_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA variable_name
   private static boolean exclude_option_2_0_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "exclude_option_2_0_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && variable_name(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // variable EQUAL expression THEN expression
   public static boolean explicit_step_clause(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "explicit_step_clause")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<explicit step clause>");
     result_ = variable(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, EQUAL);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     result_ = result_ && consumeToken(builder_, THEN);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     if (result_) {
       marker_.done(EXPLICIT_STEP_CLAUSE);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // EXPORT (ordinary_binding_name (COMMA ordinary_binding_name)*)?
   public static boolean export_clause(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "export_clause")) return false;
     if (!nextTokenIs(builder_, EXPORT)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, EXPORT);
     result_ = result_ && export_clause_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(EXPORT_CLAUSE);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // (ordinary_binding_name (COMMA ordinary_binding_name)*)?
   private static boolean export_clause_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "export_clause_1")) return false;
     export_clause_1_0(builder_, level_ + 1);
     return true;
   }
 
   // ordinary_binding_name (COMMA ordinary_binding_name)*
   private static boolean export_clause_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "export_clause_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = ordinary_binding_name(builder_, level_ + 1);
     result_ = result_ && export_clause_1_0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (COMMA ordinary_binding_name)*
   private static boolean export_clause_1_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "export_clause_1_0_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!export_clause_1_0_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "export_clause_1_0_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA ordinary_binding_name
   private static boolean export_clause_1_0_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "export_clause_1_0_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && ordinary_binding_name(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // KEYWORD (ALL|LBRACE ((variable_name | NONDEFINING_BEGIN_WORD) (COMMA (variable_name | NONDEFINING_BEGIN_WORD))*)? RBRACE)
   public static boolean export_option(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "export_option")) return false;
     if (!nextTokenIs(builder_, KEYWORD)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, KEYWORD);
     result_ = result_ && export_option_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(EXPORT_OPTION);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // ALL|LBRACE ((variable_name | NONDEFINING_BEGIN_WORD) (COMMA (variable_name | NONDEFINING_BEGIN_WORD))*)? RBRACE
   private static boolean export_option_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "export_option_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, ALL);
     if (!result_) result_ = export_option_1_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // LBRACE ((variable_name | NONDEFINING_BEGIN_WORD) (COMMA (variable_name | NONDEFINING_BEGIN_WORD))*)? RBRACE
   private static boolean export_option_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "export_option_1_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LBRACE);
     result_ = result_ && export_option_1_1_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RBRACE);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // ((variable_name | NONDEFINING_BEGIN_WORD) (COMMA (variable_name | NONDEFINING_BEGIN_WORD))*)?
   private static boolean export_option_1_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "export_option_1_1_1")) return false;
     export_option_1_1_1_0(builder_, level_ + 1);
     return true;
   }
 
   // (variable_name | NONDEFINING_BEGIN_WORD) (COMMA (variable_name | NONDEFINING_BEGIN_WORD))*
   private static boolean export_option_1_1_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "export_option_1_1_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = export_option_1_1_1_0_0(builder_, level_ + 1);
     result_ = result_ && export_option_1_1_1_0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // variable_name | NONDEFINING_BEGIN_WORD
   private static boolean export_option_1_1_1_0_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "export_option_1_1_1_0_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = variable_name(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, NONDEFINING_BEGIN_WORD);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (COMMA (variable_name | NONDEFINING_BEGIN_WORD))*
   private static boolean export_option_1_1_1_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "export_option_1_1_1_0_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!export_option_1_1_1_0_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "export_option_1_1_1_0_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA (variable_name | NONDEFINING_BEGIN_WORD)
   private static boolean export_option_1_1_1_0_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "export_option_1_1_1_0_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && export_option_1_1_1_0_1_0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // variable_name | NONDEFINING_BEGIN_WORD
   private static boolean export_option_1_1_1_0_1_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "export_option_1_1_1_0_1_0_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = variable_name(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, NONDEFINING_BEGIN_WORD);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // expression (COMMA expression)*
   public static boolean expressions(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expressions")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<expressions>");
     result_ = expression(builder_, level_ + 1, -1);
     result_ = result_ && expressions_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(EXPRESSIONS);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (COMMA expression)*
   private static boolean expressions_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expressions_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!expressions_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "expressions_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA expression
   private static boolean expressions_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expressions_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // FINALLY body?
   public static boolean finally_clause(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "finally_clause")) return false;
     if (!nextTokenIs(builder_, FINALLY)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, FINALLY);
     result_ = result_ && finally_clause_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(FINALLY_CLAUSE);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // body?
   private static boolean finally_clause_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "finally_clause_1")) return false;
     body(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // explicit_step_clause | collection_clause | numeric_clauses | keyed_by_clause
   public static boolean for_clause(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "for_clause")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<for clause>");
     result_ = explicit_step_clause(builder_, level_ + 1);
     if (!result_) result_ = collection_clause(builder_, level_ + 1);
     if (!result_) result_ = numeric_clauses(builder_, level_ + 1);
     if (!result_) result_ = keyed_by_clause(builder_, level_ + 1);
     if (result_) {
       marker_.done(FOR_CLAUSE);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // ((for_clause COMMA)* end_for_clause) | (for_clause (COMMA for_clause)*)
   public static boolean for_clauses(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "for_clauses")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<for clauses>");
     result_ = for_clauses_0(builder_, level_ + 1);
     if (!result_) result_ = for_clauses_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(FOR_CLAUSES);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (for_clause COMMA)* end_for_clause
   private static boolean for_clauses_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "for_clauses_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = for_clauses_0_0(builder_, level_ + 1);
     result_ = result_ && end_for_clause(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (for_clause COMMA)*
   private static boolean for_clauses_0_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "for_clauses_0_0")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!for_clauses_0_0_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "for_clauses_0_0");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // for_clause COMMA
   private static boolean for_clauses_0_0_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "for_clauses_0_0_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = for_clause(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, COMMA);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // for_clause (COMMA for_clause)*
   private static boolean for_clauses_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "for_clauses_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = for_clause(builder_, level_ + 1);
     result_ = result_ && for_clauses_1_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (COMMA for_clause)*
   private static boolean for_clauses_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "for_clauses_1_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!for_clauses_1_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "for_clauses_1_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA for_clause
   private static boolean for_clauses_1_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "for_clauses_1_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && for_clause(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // FOR LPAREN for_clauses RPAREN body? finally_clause? END FOR?
   public static boolean for_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "for_statement")) return false;
     if (!nextTokenIs(builder_, FOR)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, FOR, LPAREN);
     result_ = result_ && for_clauses(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     result_ = result_ && for_statement_4(builder_, level_ + 1);
     result_ = result_ && for_statement_5(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, END);
     result_ = result_ && for_statement_7(builder_, level_ + 1);
     if (result_) {
       marker_.done(FOR_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // body?
   private static boolean for_statement_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "for_statement_4")) return false;
     body(builder_, level_ + 1);
     return true;
   }
 
   // finally_clause?
   private static boolean for_statement_5(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "for_statement_5")) return false;
     finally_clause(builder_, level_ + 1);
     return true;
   }
 
   // FOR?
   private static boolean for_statement_7(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "for_statement_7")) return false;
     consumeToken(builder_, FOR);
     return true;
   }
 
   /* ********************************************************** */
   // END FUNCTION variable_name? | END variable_name?
   public static boolean function_definition_tail(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "function_definition_tail")) return false;
     if (!nextTokenIs(builder_, END)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = function_definition_tail_0(builder_, level_ + 1);
     if (!result_) result_ = function_definition_tail_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(FUNCTION_DEFINITION_TAIL);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // END FUNCTION variable_name?
   private static boolean function_definition_tail_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "function_definition_tail_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, END, FUNCTION);
     result_ = result_ && function_definition_tail_0_2(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // variable_name?
   private static boolean function_definition_tail_0_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "function_definition_tail_0_2")) return false;
     variable_name(builder_, level_ + 1);
     return true;
   }
 
   // END variable_name?
   private static boolean function_definition_tail_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "function_definition_tail_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, END);
     result_ = result_ && function_definition_tail_1_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // variable_name?
   private static boolean function_definition_tail_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "function_definition_tail_1_1")) return false;
     variable_name(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // function_word LPAREN body_fragment? RPAREN
   public static boolean function_macro_call(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "function_macro_call")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<function macro call>");
     result_ = function_word(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, LPAREN);
     result_ = result_ && function_macro_call_2(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     if (result_) {
       marker_.done(FUNCTION_MACRO_CALL);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // body_fragment?
   private static boolean function_macro_call_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "function_macro_call_2")) return false;
     body_fragment(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // LBRACE macro_name LPAREN pattern? RPAREN RBRACE EQUAL_ARROW rhs
   public static boolean function_rule(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "function_rule")) return false;
     if (!nextTokenIs(builder_, LBRACE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LBRACE);
     result_ = result_ && macro_name(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, LPAREN);
     result_ = result_ && function_rule_3(builder_, level_ + 1);
     result_ = result_ && consumeTokens(builder_, 0, RPAREN, RBRACE, EQUAL_ARROW);
     result_ = result_ && rhs(builder_, level_ + 1);
     if (result_) {
       marker_.done(FUNCTION_RULE);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // pattern?
   private static boolean function_rule_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "function_rule_3")) return false;
     pattern(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // function_rule+
   public static boolean function_rules(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "function_rules")) return false;
     if (!nextTokenIs(builder_, LBRACE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = function_rule(builder_, level_ + 1);
     int offset_ = builder_.getCurrentOffset();
     while (result_) {
       if (!function_rule(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "function_rules");
         break;
       }
       offset_ = next_offset_;
     }
     if (result_) {
       marker_.done(FUNCTION_RULES);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // NONDEFINING_FUNCTION_WORD | DEFINE_BODY_FUNCTION_WORD | DEFINE_LIST_FUNCTION_WORD
   public static boolean function_word(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "function_word")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<function word>");
     result_ = consumeToken(builder_, NONDEFINING_FUNCTION_WORD);
     if (!result_) result_ = consumeToken(builder_, DEFINE_BODY_FUNCTION_WORD);
     if (!result_) result_ = consumeToken(builder_, DEFINE_LIST_FUNCTION_WORD);
     if (result_) {
       marker_.done(FUNCTION_WORD);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // expression
   public static boolean handler(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "handler")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<handler>");
     result_ = expression(builder_, level_ + 1, -1);
     if (result_) {
       marker_.done(HANDLER);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // HASH_T | HASH_F | HASH_NEXT | HASH_REST | HASH_KEY | HASH_ALL_KEYS | HASH_INCLUDE
   public static boolean hash_word(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "hash_word")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<hash word>");
     result_ = consumeToken(builder_, HASH_T);
     if (!result_) result_ = consumeToken(builder_, HASH_F);
     if (!result_) result_ = consumeToken(builder_, HASH_NEXT);
     if (!result_) result_ = consumeToken(builder_, HASH_REST);
     if (!result_) result_ = consumeToken(builder_, HASH_KEY);
     if (!result_) result_ = consumeToken(builder_, HASH_ALL_KEYS);
     if (!result_) result_ = consumeToken(builder_, HASH_INCLUDE);
     if (result_) {
       marker_.done(HASH_WORD);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // KEY HEADER_SEPARATOR header_values
   public static boolean header(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "header")) return false;
     if (!nextTokenIs(builder_, KEY)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, KEY, HEADER_SEPARATOR);
     result_ = result_ && header_values(builder_, level_ + 1);
     if (result_) {
       marker_.done(HEADER);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // CRLF | (VALUE CRLF)+
   public static boolean header_values(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "header_values")) return false;
     if (!nextTokenIs(builder_, CRLF) && !nextTokenIs(builder_, VALUE)
         && replaceVariants(builder_, 2, "<header values>")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<header values>");
     result_ = consumeToken(builder_, CRLF);
     if (!result_) result_ = header_values_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(HEADER_VALUES);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (VALUE CRLF)+
   private static boolean header_values_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "header_values_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = header_values_1_0(builder_, level_ + 1);
     int offset_ = builder_.getCurrentOffset();
     while (result_) {
       if (!header_values_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "header_values_1");
         break;
       }
       offset_ = next_offset_;
     }
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // VALUE CRLF
   private static boolean header_values_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "header_values_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, VALUE, CRLF);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // header*
   public static boolean headers(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "headers")) return false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<headers>");
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!header(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "headers");
         break;
       }
       offset_ = next_offset_;
     }
     marker_.done(HEADERS);
     exitErrorRecordingSection(builder_, level_, true, false, _SECTION_GENERAL_, null);
     return true;
   }
 
   /* ********************************************************** */
   // IF LPAREN expression RPAREN body? elseif_statement* else_statement? if_tail
   public static boolean if_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "if_statement")) return false;
     if (!nextTokenIs(builder_, IF)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, IF, LPAREN);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     result_ = result_ && if_statement_4(builder_, level_ + 1);
     result_ = result_ && if_statement_5(builder_, level_ + 1);
     result_ = result_ && if_statement_6(builder_, level_ + 1);
     result_ = result_ && if_tail(builder_, level_ + 1);
     if (result_) {
       marker_.done(IF_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // body?
   private static boolean if_statement_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "if_statement_4")) return false;
     body(builder_, level_ + 1);
     return true;
   }
 
   // elseif_statement*
   private static boolean if_statement_5(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "if_statement_5")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!elseif_statement(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "if_statement_5");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // else_statement?
   private static boolean if_statement_6(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "if_statement_6")) return false;
     else_statement(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // END IF?
   public static boolean if_tail(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "if_tail")) return false;
     if (!nextTokenIs(builder_, END)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, END);
     result_ = result_ && if_tail_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(IF_TAIL);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // IF?
   private static boolean if_tail_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "if_tail_1")) return false;
     consumeToken(builder_, IF);
     return true;
   }
 
   /* ********************************************************** */
   // KEYWORD (ALL|LBRACE variable_specs RBRACE)
   public static boolean import_option(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "import_option")) return false;
     if (!nextTokenIs(builder_, KEYWORD)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, KEYWORD);
     result_ = result_ && import_option_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(IMPORT_OPTION);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // ALL|LBRACE variable_specs RBRACE
   private static boolean import_option_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "import_option_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, ALL);
     if (!result_) result_ = import_option_1_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // LBRACE variable_specs RBRACE
   private static boolean import_option_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "import_option_1_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LBRACE);
     result_ = result_ && variable_specs(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RBRACE);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // init_value_slot_option | init_function_slot_option
   static boolean inherited_option(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "inherited_option")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = init_value_slot_option(builder_, level_ + 1);
     if (!result_) result_ = init_function_slot_option(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // (COMMA inherited_option)*
   public static boolean inherited_options(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "inherited_options")) return false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<inherited options>");
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!inherited_options_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "inherited_options");
         break;
       }
       offset_ = next_offset_;
     }
     marker_.done(INHERITED_OPTIONS);
     exitErrorRecordingSection(builder_, level_, true, false, _SECTION_GENERAL_, null);
     return true;
   }
 
   // COMMA inherited_option
   private static boolean inherited_options_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "inherited_options_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && inherited_option(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // <<unreservedNameWithValues "inherited">> SLOT variable_name init_expression? inherited_options?
   public static boolean inherited_slot_spec(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "inherited_slot_spec")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<inherited slot spec>");
     result_ = unreservedNameWithValues(builder_, level_ + 1, "inherited");
     result_ = result_ && consumeToken(builder_, SLOT);
     result_ = result_ && variable_name(builder_, level_ + 1);
     result_ = result_ && inherited_slot_spec_3(builder_, level_ + 1);
     result_ = result_ && inherited_slot_spec_4(builder_, level_ + 1);
     if (result_) {
       marker_.done(INHERITED_SLOT_SPEC);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // init_expression?
   private static boolean inherited_slot_spec_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "inherited_slot_spec_3")) return false;
     init_expression(builder_, level_ + 1);
     return true;
   }
 
   // inherited_options?
   private static boolean inherited_slot_spec_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "inherited_slot_spec_4")) return false;
     inherited_options(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // type_slot_option | init_value_slot_option | init_function_slot_option
   public static boolean init_arg_option(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "init_arg_option")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<init arg option>");
     result_ = type_slot_option(builder_, level_ + 1);
     if (!result_) result_ = init_value_slot_option(builder_, level_ + 1);
     if (!result_) result_ = init_function_slot_option(builder_, level_ + 1);
     if (result_) {
       marker_.done(INIT_ARG_OPTION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // (COMMA init_arg_option)*
   public static boolean init_arg_options(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "init_arg_options")) return false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<init arg options>");
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!init_arg_options_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "init_arg_options");
         break;
       }
       offset_ = next_offset_;
     }
     marker_.done(INIT_ARG_OPTIONS);
     exitErrorRecordingSection(builder_, level_, true, false, _SECTION_GENERAL_, null);
     return true;
   }
 
   // COMMA init_arg_option
   private static boolean init_arg_options_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "init_arg_options_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && init_arg_option(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // <<unreservedNameWithValues "required">>? <<unreservedNameWithValues "keyword">> symbol init_expression? init_arg_options?
   public static boolean init_arg_spec(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "init_arg_spec")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<init arg spec>");
     result_ = init_arg_spec_0(builder_, level_ + 1);
     result_ = result_ && unreservedNameWithValues(builder_, level_ + 1, "keyword");
     result_ = result_ && symbol(builder_, level_ + 1);
     result_ = result_ && init_arg_spec_3(builder_, level_ + 1);
     result_ = result_ && init_arg_spec_4(builder_, level_ + 1);
     if (result_) {
       marker_.done(INIT_ARG_SPEC);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // <<unreservedNameWithValues "required">>?
   private static boolean init_arg_spec_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "init_arg_spec_0")) return false;
     unreservedNameWithValues(builder_, level_ + 1, "required");
     return true;
   }
 
   // init_expression?
   private static boolean init_arg_spec_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "init_arg_spec_3")) return false;
     init_expression(builder_, level_ + 1);
     return true;
   }
 
   // init_arg_options?
   private static boolean init_arg_spec_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "init_arg_spec_4")) return false;
     init_arg_options(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // EQUAL expression
   public static boolean init_expression(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "init_expression")) return false;
     if (!nextTokenIs(builder_, EQUAL)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, EQUAL);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     if (result_) {
       marker_.done(INIT_EXPRESSION);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // <<keywordWithValue "init-function:">> expression
   public static boolean init_function_slot_option(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "init_function_slot_option")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<init function slot option>");
     result_ = keywordWithValue(builder_, level_ + 1, "init-function:");
     result_ = result_ && expression(builder_, level_ + 1, -1);
     if (result_) {
       marker_.done(INIT_FUNCTION_SLOT_OPTION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // <<keywordWithValue "init-keyword:">> symbol
   public static boolean init_keyword_slot_option(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "init_keyword_slot_option")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<init keyword slot option>");
     result_ = keywordWithValue(builder_, level_ + 1, "init-keyword:");
     result_ = result_ && symbol(builder_, level_ + 1);
     if (result_) {
       marker_.done(INIT_KEYWORD_SLOT_OPTION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // <<keywordWithValue "init-value:">> expression
   public static boolean init_value_slot_option(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "init_value_slot_option")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<init value slot option>");
     result_ = keywordWithValue(builder_, level_ + 1, "init-value:");
     result_ = result_ && expression(builder_, level_ + 1, -1);
     if (result_) {
       marker_.done(INIT_VALUE_SLOT_OPTION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // HASH_KEY COMMA HASH_ALL_KEYS
   //     | HASH_KEY keyword_parameters COMMA HASH_ALL_KEYS
   //     | HASH_KEY keyword_parameters
   //     | HASH_KEY
   public static boolean key_parameter_list(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "key_parameter_list")) return false;
     if (!nextTokenIs(builder_, HASH_KEY)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = key_parameter_list_0(builder_, level_ + 1);
     if (!result_) result_ = key_parameter_list_1(builder_, level_ + 1);
     if (!result_) result_ = key_parameter_list_2(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, HASH_KEY);
     if (result_) {
       marker_.done(KEY_PARAMETER_LIST);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // HASH_KEY COMMA HASH_ALL_KEYS
   private static boolean key_parameter_list_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "key_parameter_list_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, HASH_KEY, COMMA, HASH_ALL_KEYS);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // HASH_KEY keyword_parameters COMMA HASH_ALL_KEYS
   private static boolean key_parameter_list_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "key_parameter_list_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_KEY);
     result_ = result_ && keyword_parameters(builder_, level_ + 1);
     result_ = result_ && consumeTokens(builder_, 0, COMMA, HASH_ALL_KEYS);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // HASH_KEY keyword_parameters
   private static boolean key_parameter_list_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "key_parameter_list_2")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_KEY);
     result_ = result_ && keyword_parameters(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // variable KEYED_BY variable IN expression
   public static boolean keyed_by_clause(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "keyed_by_clause")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<keyed by clause>");
     result_ = variable(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, KEYED_BY);
     result_ = result_ && variable(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, IN);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     if (result_) {
       marker_.done(KEYED_BY_CLAUSE);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // symbol? variable default_value?
   public static boolean keyword_parameter(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "keyword_parameter")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<keyword parameter>");
     result_ = keyword_parameter_0(builder_, level_ + 1);
     result_ = result_ && variable(builder_, level_ + 1);
     result_ = result_ && keyword_parameter_2(builder_, level_ + 1);
     if (result_) {
       marker_.done(KEYWORD_PARAMETER);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // symbol?
   private static boolean keyword_parameter_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "keyword_parameter_0")) return false;
     symbol(builder_, level_ + 1);
     return true;
   }
 
   // default_value?
   private static boolean keyword_parameter_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "keyword_parameter_2")) return false;
     default_value(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // keyword_parameter (COMMA keyword_parameter)*
   public static boolean keyword_parameters(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "keyword_parameters")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<keyword parameters>");
     result_ = keyword_parameter(builder_, level_ + 1);
     result_ = result_ && keyword_parameters_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(KEYWORD_PARAMETERS);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (COMMA keyword_parameter)*
   private static boolean keyword_parameters_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "keyword_parameters_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!keyword_parameters_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "keyword_parameters_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA keyword_parameter
   private static boolean keyword_parameters_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "keyword_parameters_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && keyword_parameter(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // literal
   //     | statement
   //     | function_macro_call
   //     | variable_name
   //     | LPAREN expression RPAREN
   //     | PARSED_FUNCTION_CALL
   //     | PARSED_MACRO_CALL
   static boolean leaf(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "leaf")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = literal(builder_, level_ + 1);
     if (!result_) result_ = statement(builder_, level_ + 1);
     if (!result_) result_ = function_macro_call(builder_, level_ + 1);
     if (!result_) result_ = variable_name(builder_, level_ + 1);
     if (!result_) result_ = leaf_4(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, PARSED_FUNCTION_CALL);
     if (!result_) result_ = consumeToken(builder_, PARSED_MACRO_CALL);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // LPAREN expression RPAREN
   private static boolean leaf_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "leaf_4")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LPAREN);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // END LIBRARY variable_name? | END variable_name?
   public static boolean library_definition_tail(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "library_definition_tail")) return false;
     if (!nextTokenIs(builder_, END)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = library_definition_tail_0(builder_, level_ + 1);
     if (!result_) result_ = library_definition_tail_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(LIBRARY_DEFINITION_TAIL);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // END LIBRARY variable_name?
   private static boolean library_definition_tail_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "library_definition_tail_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, END, LIBRARY);
     result_ = result_ && library_definition_tail_0_2(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // variable_name?
   private static boolean library_definition_tail_0_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "library_definition_tail_0_2")) return false;
     variable_name(builder_, level_ + 1);
     return true;
   }
 
   // END variable_name?
   private static boolean library_definition_tail_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "library_definition_tail_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, END);
     result_ = result_ && library_definition_tail_1_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // variable_name?
   private static boolean library_definition_tail_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "library_definition_tail_1_1")) return false;
     variable_name(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // statement non_statement_list_fragment? | non_statement_list_fragment
   static boolean list_fragment(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "list_fragment")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = list_fragment_0(builder_, level_ + 1);
     if (!result_) result_ = non_statement_list_fragment(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // statement non_statement_list_fragment?
   private static boolean list_fragment_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "list_fragment_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = statement(builder_, level_ + 1);
     result_ = result_ && list_fragment_0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // non_statement_list_fragment?
   private static boolean list_fragment_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "list_fragment_0_1")) return false;
     non_statement_list_fragment(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // LBRACE DEFINE pattern RBRACE EQUAL_ARROW rhs
   public static boolean list_style_definition_rule(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "list_style_definition_rule")) return false;
     if (!nextTokenIs(builder_, LBRACE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, LBRACE, DEFINE);
     result_ = result_ && pattern(builder_, level_ + 1);
     result_ = result_ && consumeTokens(builder_, 0, RBRACE, EQUAL_ARROW);
     result_ = result_ && rhs(builder_, level_ + 1);
     if (result_) {
       marker_.done(LIST_STYLE_DEFINITION_RULE);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // list_style_definition_rule+
   public static boolean list_style_definition_rules(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "list_style_definition_rules")) return false;
     if (!nextTokenIs(builder_, LBRACE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = list_style_definition_rule(builder_, level_ + 1);
     int offset_ = builder_.getCurrentOffset();
     while (result_) {
       if (!list_style_definition_rule(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "list_style_definition_rules");
         break;
       }
       offset_ = next_offset_;
     }
     if (result_) {
       marker_.done(LIST_STYLE_DEFINITION_RULES);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // NUMBER
   //     | CHARACTER_LITERAL
   //     | string_literal
   //     | unique_string
   //     | HASH_T
   //     | HASH_F
   //     | HASH_PAREN constants DOT constant RPAREN
   //     | HASH_PAREN constants? RPAREN
   //     | HASH_BRACKET constants? RBRACKET
   //     | PARSED_LIST_CONSTANT
   //     | PARSED_VECTOR_CONSTANT
   public static boolean literal(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "literal")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<literal>");
     result_ = consumeToken(builder_, NUMBER);
     if (!result_) result_ = consumeToken(builder_, CHARACTER_LITERAL);
     if (!result_) result_ = string_literal(builder_, level_ + 1);
     if (!result_) result_ = unique_string(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, HASH_T);
     if (!result_) result_ = consumeToken(builder_, HASH_F);
     if (!result_) result_ = literal_6(builder_, level_ + 1);
     if (!result_) result_ = literal_7(builder_, level_ + 1);
     if (!result_) result_ = literal_8(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, PARSED_LIST_CONSTANT);
     if (!result_) result_ = consumeToken(builder_, PARSED_VECTOR_CONSTANT);
     if (result_) {
       marker_.done(LITERAL);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // HASH_PAREN constants DOT constant RPAREN
   private static boolean literal_6(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "literal_6")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_PAREN);
     result_ = result_ && constants(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, DOT);
     result_ = result_ && constant(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // HASH_PAREN constants? RPAREN
   private static boolean literal_7(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "literal_7")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_PAREN);
     result_ = result_ && literal_7_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // constants?
   private static boolean literal_7_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "literal_7_1")) return false;
     constants(builder_, level_ + 1);
     return true;
   }
 
   // HASH_BRACKET constants? RBRACKET
   private static boolean literal_8(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "literal_8")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_BRACKET);
     result_ = result_ && literal_8_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RBRACKET);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // constants?
   private static boolean literal_8_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "literal_8_1")) return false;
     constants(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // LET bindings
   //     | LET HANDLER_T condition EQUAL handler
   //     | LOCAL local_methods
   //     | PARSED_LOCAL_DECLARATION
   public static boolean local_declaration(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "local_declaration")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<local declaration>");
     result_ = local_declaration_0(builder_, level_ + 1);
     if (!result_) result_ = local_declaration_1(builder_, level_ + 1);
     if (!result_) result_ = local_declaration_2(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, PARSED_LOCAL_DECLARATION);
     if (result_) {
       marker_.done(LOCAL_DECLARATION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // LET bindings
   private static boolean local_declaration_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "local_declaration_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LET);
     result_ = result_ && bindings(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // LET HANDLER_T condition EQUAL handler
   private static boolean local_declaration_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "local_declaration_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, LET, HANDLER_T);
     result_ = result_ && condition(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, EQUAL);
     result_ = result_ && handler(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // LOCAL local_methods
   private static boolean local_declaration_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "local_declaration_2")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LOCAL);
     result_ = result_ && local_methods(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // method_definition (COMMA method_definition)*
   public static boolean local_methods(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "local_methods")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<local methods>");
     result_ = method_definition(builder_, level_ + 1);
     result_ = result_ && local_methods_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(LOCAL_METHODS);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (COMMA method_definition)*
   private static boolean local_methods_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "local_methods_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!local_methods_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "local_methods_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA method_definition
   private static boolean local_methods_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "local_methods_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && method_definition(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // definition_macro_call | statement | function_macro_call | PARSED_MACRO_CALL
   public static boolean macro(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "macro")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<macro>");
     result_ = definition_macro_call(builder_, level_ + 1);
     if (!result_) result_ = statement(builder_, level_ + 1);
     if (!result_) result_ = function_macro_call(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, PARSED_MACRO_CALL);
     if (result_) {
       marker_.done(MACRO);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // macro_name main_rule_set aux_rule_sets? END MACRO_T? macro_name?
   public static boolean macro_definition(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "macro_definition")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<macro definition>");
     result_ = macro_name(builder_, level_ + 1);
     result_ = result_ && main_rule_set(builder_, level_ + 1);
     result_ = result_ && macro_definition_2(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, END);
     result_ = result_ && macro_definition_4(builder_, level_ + 1);
     result_ = result_ && macro_definition_5(builder_, level_ + 1);
     if (result_) {
       marker_.done(MACRO_DEFINITION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // aux_rule_sets?
   private static boolean macro_definition_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "macro_definition_2")) return false;
     aux_rule_sets(builder_, level_ + 1);
     return true;
   }
 
   // MACRO_T?
   private static boolean macro_definition_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "macro_definition_4")) return false;
     consumeToken(builder_, MACRO_T);
     return true;
   }
 
   // macro_name?
   private static boolean macro_definition_5(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "macro_definition_5")) return false;
     macro_name(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // nondefining_name | define_body_word | define_list_word
   public static boolean macro_name(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "macro_name")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<macro name>");
     result_ = nondefining_name(builder_, level_ + 1);
     if (!result_) result_ = define_body_word(builder_, level_ + 1);
     if (!result_) result_ = define_list_word(builder_, level_ + 1);
     if (result_) {
       marker_.done(MACRO_NAME);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // begin_word body_fragment? end_clause
   public static boolean macro_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "macro_statement")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<macro statement>");
     result_ = begin_word(builder_, level_ + 1);
     result_ = result_ && macro_statement_1(builder_, level_ + 1);
     result_ = result_ && end_clause(builder_, level_ + 1);
     if (result_) {
       marker_.done(MACRO_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // body_fragment?
   private static boolean macro_statement_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "macro_statement_1")) return false;
     body_fragment(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // body_style_definition_rules
   //     | list_style_definition_rules
   //     | statement_rules
   //     | function_rules
   public static boolean main_rule_set(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "main_rule_set")) return false;
     if (!nextTokenIs(builder_, LBRACE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = body_style_definition_rules(builder_, level_ + 1);
     if (!result_) result_ = list_style_definition_rules(builder_, level_ + 1);
     if (!result_) result_ = statement_rules(builder_, level_ + 1);
     if (!result_) result_ = function_rules(builder_, level_ + 1);
     if (result_) {
       marker_.done(MAIN_RULE_SET);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // pattern SEMICOLON? | SEMICOLON?
   public static boolean maybe_pattern_and_semicolon(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "maybe_pattern_and_semicolon")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<maybe pattern and semicolon>");
     result_ = maybe_pattern_and_semicolon_0(builder_, level_ + 1);
     if (!result_) result_ = maybe_pattern_and_semicolon_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(MAYBE_PATTERN_AND_SEMICOLON);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // pattern SEMICOLON?
   private static boolean maybe_pattern_and_semicolon_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "maybe_pattern_and_semicolon_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = pattern(builder_, level_ + 1);
     result_ = result_ && maybe_pattern_and_semicolon_0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // SEMICOLON?
   private static boolean maybe_pattern_and_semicolon_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "maybe_pattern_and_semicolon_0_1")) return false;
     consumeToken(builder_, SEMICOLON);
     return true;
   }
 
   // SEMICOLON?
   private static boolean maybe_pattern_and_semicolon_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "maybe_pattern_and_semicolon_1")) return false;
     consumeToken(builder_, SEMICOLON);
     return true;
   }
 
   /* ********************************************************** */
   // METHOD? variable_name parameter_list body? method_definition_tail
   public static boolean method_definition(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "method_definition")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<method definition>");
     result_ = method_definition_0(builder_, level_ + 1);
     result_ = result_ && variable_name(builder_, level_ + 1);
     result_ = result_ && parameter_list(builder_, level_ + 1);
     result_ = result_ && method_definition_3(builder_, level_ + 1);
     result_ = result_ && method_definition_tail(builder_, level_ + 1);
     if (result_) {
       marker_.done(METHOD_DEFINITION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // METHOD?
   private static boolean method_definition_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "method_definition_0")) return false;
     consumeToken(builder_, METHOD);
     return true;
   }
 
   // body?
   private static boolean method_definition_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "method_definition_3")) return false;
     body(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // END METHOD variable_name? | END variable_name?
   public static boolean method_definition_tail(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "method_definition_tail")) return false;
     if (!nextTokenIs(builder_, END)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = method_definition_tail_0(builder_, level_ + 1);
     if (!result_) result_ = method_definition_tail_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(METHOD_DEFINITION_TAIL);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // END METHOD variable_name?
   private static boolean method_definition_tail_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "method_definition_tail_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, END, METHOD);
     result_ = result_ && method_definition_tail_0_2(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // variable_name?
   private static boolean method_definition_tail_0_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "method_definition_tail_0_2")) return false;
     variable_name(builder_, level_ + 1);
     return true;
   }
 
   // END variable_name?
   private static boolean method_definition_tail_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "method_definition_tail_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, END);
     result_ = result_ && method_definition_tail_1_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // variable_name?
   private static boolean method_definition_tail_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "method_definition_tail_1_1")) return false;
     variable_name(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // METHOD parameter_list body? END METHOD?
   public static boolean method_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "method_statement")) return false;
     if (!nextTokenIs(builder_, METHOD)) return false;
     boolean result_ = false;
     boolean pinned_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, null);
     result_ = consumeToken(builder_, METHOD);
     pinned_ = result_; // pin = 1
     result_ = result_ && report_error_(builder_, parameter_list(builder_, level_ + 1));
     result_ = pinned_ && report_error_(builder_, method_statement_2(builder_, level_ + 1)) && result_;
     result_ = pinned_ && report_error_(builder_, consumeToken(builder_, END)) && result_;
     result_ = pinned_ && method_statement_4(builder_, level_ + 1) && result_;
     if (result_ || pinned_) {
       marker_.done(METHOD_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, pinned_, _SECTION_GENERAL_, null);
     return result_ || pinned_;
   }
 
   // body?
   private static boolean method_statement_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "method_statement_2")) return false;
     body(builder_, level_ + 1);
     return true;
   }
 
   // METHOD?
   private static boolean method_statement_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "method_statement_4")) return false;
     consumeToken(builder_, METHOD);
     return true;
   }
 
   /* ********************************************************** */
   // nondefining_name
   public static boolean modifier(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "modifier")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<modifier>");
     result_ = nondefining_name(builder_, level_ + 1);
     if (result_) {
       marker_.done(MODIFIER);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // modifier+
   public static boolean modifiers(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "modifiers")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<modifiers>");
     result_ = modifier(builder_, level_ + 1);
     int offset_ = builder_.getCurrentOffset();
     while (result_) {
       if (!modifier(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "modifiers");
         break;
       }
       offset_ = next_offset_;
     }
     if (result_) {
       marker_.done(MODIFIERS);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // END MODULE variable_name? | END variable_name?
   public static boolean module_definition_tail(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "module_definition_tail")) return false;
     if (!nextTokenIs(builder_, END)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = module_definition_tail_0(builder_, level_ + 1);
     if (!result_) result_ = module_definition_tail_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(MODULE_DEFINITION_TAIL);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // END MODULE variable_name?
   private static boolean module_definition_tail_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "module_definition_tail_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, END, MODULE);
     result_ = result_ && module_definition_tail_0_2(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // variable_name?
   private static boolean module_definition_tail_0_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "module_definition_tail_0_2")) return false;
     variable_name(builder_, level_ + 1);
     return true;
   }
 
   // END variable_name?
   private static boolean module_definition_tail_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "module_definition_tail_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, END);
     result_ = result_ && module_definition_tail_1_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // variable_name?
   private static boolean module_definition_tail_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "module_definition_tail_1_1")) return false;
     variable_name(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // macro_name | non_end_core_word | dylan_unreserved_name
   public static boolean name_not_end(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "name_not_end")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<name not end>");
     result_ = macro_name(builder_, level_ + 1);
     if (!result_) result_ = non_end_core_word(builder_, level_ + 1);
     if (!result_) result_ = dylan_unreserved_name(builder_, level_ + 1);
     if (result_) {
       marker_.done(NAME_NOT_END);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // string HASH_HASH
   public static boolean name_prefix(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "name_prefix")) return false;
     if (!nextTokenIs(builder_, STRING_CHARACTER) && !nextTokenIs(builder_, STRING_ESCAPE_CHARACTER)
         && replaceVariants(builder_, 2, "<name prefix>")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<name prefix>");
     result_ = string(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, HASH_HASH);
     if (result_) {
       marker_.done(NAME_PREFIX);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // word_name | string | symbol
   public static boolean name_string_or_symbol(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "name_string_or_symbol")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<name string or symbol>");
     result_ = word_name(builder_, level_ + 1);
     if (!result_) result_ = string(builder_, level_ + 1);
     if (!result_) result_ = symbol(builder_, level_ + 1);
     if (result_) {
       marker_.done(NAME_STRING_OR_SYMBOL);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // HASH_HASH string
   public static boolean name_suffix(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "name_suffix")) return false;
     if (!nextTokenIs(builder_, HASH_HASH)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_HASH);
     result_ = result_ && string(builder_, level_ + 1);
     if (result_) {
       marker_.done(NAME_SUFFIX);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // HASH_NEXT variable_name COMMA rest_key_parameter_list
   //     | HASH_NEXT variable_name
   //     | rest_key_parameter_list
   public static boolean next_rest_key_parameter_list(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "next_rest_key_parameter_list")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<next rest key parameter list>");
     result_ = next_rest_key_parameter_list_0(builder_, level_ + 1);
     if (!result_) result_ = next_rest_key_parameter_list_1(builder_, level_ + 1);
     if (!result_) result_ = rest_key_parameter_list(builder_, level_ + 1);
     if (result_) {
       marker_.done(NEXT_REST_KEY_PARAMETER_LIST);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // HASH_NEXT variable_name COMMA rest_key_parameter_list
   private static boolean next_rest_key_parameter_list_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "next_rest_key_parameter_list_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_NEXT);
     result_ = result_ && variable_name(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, COMMA);
     result_ = result_ && rest_key_parameter_list(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // HASH_NEXT variable_name
   private static boolean next_rest_key_parameter_list_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "next_rest_key_parameter_list_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_NEXT);
     result_ = result_ && variable_name(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // COMMA
   //     | DOT
   //     | SEMICOLON
   //     | COLON_COLON
   //     | MINUS
   //     | EQUAL
   //     | EQUAL_EQUAL
   //     | EQUAL_ARROW
   //     | HASH_HASH
   //     | QUERY
   //     | QUERY_QUERY
   //     | QUERY_EQUAL
   //     | ELLIPSIS
   public static boolean non_bracketing_punctuation(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "non_bracketing_punctuation")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<non bracketing punctuation>");
     result_ = consumeToken(builder_, COMMA);
     if (!result_) result_ = consumeToken(builder_, DOT);
     if (!result_) result_ = consumeToken(builder_, SEMICOLON);
     if (!result_) result_ = consumeToken(builder_, COLON_COLON);
     if (!result_) result_ = consumeToken(builder_, MINUS);
     if (!result_) result_ = consumeToken(builder_, EQUAL);
     if (!result_) result_ = consumeToken(builder_, EQUAL_EQUAL);
     if (!result_) result_ = consumeToken(builder_, EQUAL_ARROW);
     if (!result_) result_ = consumeToken(builder_, HASH_HASH);
     if (!result_) result_ = consumeToken(builder_, QUERY);
     if (!result_) result_ = consumeToken(builder_, QUERY_QUERY);
     if (!result_) result_ = consumeToken(builder_, QUERY_EQUAL);
     if (!result_) result_ = consumeToken(builder_, ELLIPSIS);
     if (result_) {
       marker_.done(NON_BRACKETING_PUNCTUATION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // DEFINE | HANDLER_T | LET | LOCAL | MACRO_T | OTHERWISE
   public static boolean non_end_core_word(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "non_end_core_word")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<non end core word>");
     result_ = consumeToken(builder_, DEFINE);
     if (!result_) result_ = consumeToken(builder_, HANDLER_T);
     if (!result_) result_ = consumeToken(builder_, LET);
     if (!result_) result_ = consumeToken(builder_, LOCAL);
     if (!result_) result_ = consumeToken(builder_, MACRO_T);
     if (!result_) result_ = consumeToken(builder_, OTHERWISE);
     if (result_) {
       marker_.done(NON_END_CORE_WORD);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // simple_fragment basic_fragment?
   public static boolean non_statement_basic_fragment(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "non_statement_basic_fragment")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<non statement basic fragment>");
     result_ = simple_fragment(builder_, level_ + 1);
     result_ = result_ && non_statement_basic_fragment_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(NON_STATEMENT_BASIC_FRAGMENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // basic_fragment?
   private static boolean non_statement_basic_fragment_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "non_statement_basic_fragment_1")) return false;
     basic_fragment(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // definition semicolon_fragment?
   //     | local_declaration semicolon_fragment?
   //     | simple_fragment body_fragment?
   //     | COMMA body_fragment?
   //     | semicolon_fragment?
   public static boolean non_statement_body_fragment(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "non_statement_body_fragment")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<non statement body fragment>");
     result_ = non_statement_body_fragment_0(builder_, level_ + 1);
     if (!result_) result_ = non_statement_body_fragment_1(builder_, level_ + 1);
     if (!result_) result_ = non_statement_body_fragment_2(builder_, level_ + 1);
     if (!result_) result_ = non_statement_body_fragment_3(builder_, level_ + 1);
     if (!result_) result_ = non_statement_body_fragment_4(builder_, level_ + 1);
     if (result_) {
       marker_.done(NON_STATEMENT_BODY_FRAGMENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // definition semicolon_fragment?
   private static boolean non_statement_body_fragment_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "non_statement_body_fragment_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = definition(builder_, level_ + 1);
     result_ = result_ && non_statement_body_fragment_0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // semicolon_fragment?
   private static boolean non_statement_body_fragment_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "non_statement_body_fragment_0_1")) return false;
     semicolon_fragment(builder_, level_ + 1);
     return true;
   }
 
   // local_declaration semicolon_fragment?
   private static boolean non_statement_body_fragment_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "non_statement_body_fragment_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = local_declaration(builder_, level_ + 1);
     result_ = result_ && non_statement_body_fragment_1_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // semicolon_fragment?
   private static boolean non_statement_body_fragment_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "non_statement_body_fragment_1_1")) return false;
     semicolon_fragment(builder_, level_ + 1);
     return true;
   }
 
   // simple_fragment body_fragment?
   private static boolean non_statement_body_fragment_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "non_statement_body_fragment_2")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = simple_fragment(builder_, level_ + 1);
     result_ = result_ && non_statement_body_fragment_2_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // body_fragment?
   private static boolean non_statement_body_fragment_2_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "non_statement_body_fragment_2_1")) return false;
     body_fragment(builder_, level_ + 1);
     return true;
   }
 
   // COMMA body_fragment?
   private static boolean non_statement_body_fragment_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "non_statement_body_fragment_3")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && non_statement_body_fragment_3_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // body_fragment?
   private static boolean non_statement_body_fragment_3_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "non_statement_body_fragment_3_1")) return false;
     body_fragment(builder_, level_ + 1);
     return true;
   }
 
   // semicolon_fragment?
   private static boolean non_statement_body_fragment_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "non_statement_body_fragment_4")) return false;
     semicolon_fragment(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // simple_fragment list_fragment? | COMMA list_fragment?
   static boolean non_statement_list_fragment(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "non_statement_list_fragment")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = non_statement_list_fragment_0(builder_, level_ + 1);
     if (!result_) result_ = non_statement_list_fragment_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // simple_fragment list_fragment?
   private static boolean non_statement_list_fragment_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "non_statement_list_fragment_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = simple_fragment(builder_, level_ + 1);
     result_ = result_ && non_statement_list_fragment_0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // list_fragment?
   private static boolean non_statement_list_fragment_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "non_statement_list_fragment_0_1")) return false;
     list_fragment(builder_, level_ + 1);
     return true;
   }
 
   // COMMA list_fragment?
   private static boolean non_statement_list_fragment_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "non_statement_list_fragment_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && non_statement_list_fragment_1_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // list_fragment?
   private static boolean non_statement_list_fragment_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "non_statement_list_fragment_1_1")) return false;
     list_fragment(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // nondefining_word | escaped_name
   public static boolean nondefining_name(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "nondefining_name")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<nondefining name>");
     result_ = nondefining_word(builder_, level_ + 1);
     if (!result_) result_ = escaped_name(builder_, level_ + 1);
     if (result_) {
       marker_.done(NONDEFINING_NAME);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // NONDEFINING_BEGIN_WORD | NONDEFINING_FUNCTION_WORD | NONDEFINING_NONEXPRESSION_WORD
   public static boolean nondefining_word(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "nondefining_word")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<nondefining word>");
     result_ = consumeToken(builder_, NONDEFINING_BEGIN_WORD);
     if (!result_) result_ = consumeToken(builder_, NONDEFINING_FUNCTION_WORD);
     if (!result_) result_ = consumeToken(builder_, NONDEFINING_NONEXPRESSION_WORD);
     if (result_) {
       marker_.done(NONDEFINING_WORD);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // NONDEFINING_NONEXPRESSION_WORD | DEFINE_BODY_NONEXPRESSION_WORD | DEFINE_LIST_NONEXPRESSION_WORD | FUNCTION | CLASS | DOMAIN | LIBRARY | MODULE | GENERIC | SUITE | TEST
   static boolean nonexpression_word(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "nonexpression_word")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, NONDEFINING_NONEXPRESSION_WORD);
     if (!result_) result_ = consumeToken(builder_, DEFINE_BODY_NONEXPRESSION_WORD);
     if (!result_) result_ = consumeToken(builder_, DEFINE_LIST_NONEXPRESSION_WORD);
     if (!result_) result_ = consumeToken(builder_, FUNCTION);
     if (!result_) result_ = consumeToken(builder_, CLASS);
     if (!result_) result_ = consumeToken(builder_, DOMAIN);
     if (!result_) result_ = consumeToken(builder_, LIBRARY);
     if (!result_) result_ = consumeToken(builder_, MODULE);
     if (!result_) result_ = consumeToken(builder_, GENERIC);
     if (!result_) result_ = consumeToken(builder_, SUITE);
     if (!result_) result_ = consumeToken(builder_, TEST);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // variable FROM expression ((TO|ABOVE|BELOW) expression)? (BY expression)?
   public static boolean numeric_clauses(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "numeric_clauses")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<numeric clauses>");
     result_ = variable(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, FROM);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     result_ = result_ && numeric_clauses_3(builder_, level_ + 1);
     result_ = result_ && numeric_clauses_4(builder_, level_ + 1);
     if (result_) {
       marker_.done(NUMERIC_CLAUSES);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // ((TO|ABOVE|BELOW) expression)?
   private static boolean numeric_clauses_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "numeric_clauses_3")) return false;
     numeric_clauses_3_0(builder_, level_ + 1);
     return true;
   }
 
   // (TO|ABOVE|BELOW) expression
   private static boolean numeric_clauses_3_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "numeric_clauses_3_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = numeric_clauses_3_0_0(builder_, level_ + 1);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // TO|ABOVE|BELOW
   private static boolean numeric_clauses_3_0_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "numeric_clauses_3_0_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, TO);
     if (!result_) result_ = consumeToken(builder_, ABOVE);
     if (!result_) result_ = consumeToken(builder_, BELOW);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (BY expression)?
   private static boolean numeric_clauses_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "numeric_clauses_4")) return false;
     numeric_clauses_4_0(builder_, level_ + 1);
     return true;
   }
 
   // BY expression
   private static boolean numeric_clauses_4_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "numeric_clauses_4_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, BY);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // leaf ( LPAREN arguments? RPAREN | LBRACKET arguments? RBRACKET | DOT variable_name )*
   public static boolean operand(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "operand")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<operand>");
     result_ = leaf(builder_, level_ + 1);
     result_ = result_ && operand_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(OPERAND);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // ( LPAREN arguments? RPAREN | LBRACKET arguments? RBRACKET | DOT variable_name )*
   private static boolean operand_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "operand_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!operand_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "operand_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // LPAREN arguments? RPAREN | LBRACKET arguments? RBRACKET | DOT variable_name
   private static boolean operand_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "operand_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = operand_1_0_0(builder_, level_ + 1);
     if (!result_) result_ = operand_1_0_1(builder_, level_ + 1);
     if (!result_) result_ = operand_1_0_2(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // LPAREN arguments? RPAREN
   private static boolean operand_1_0_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "operand_1_0_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LPAREN);
     result_ = result_ && operand_1_0_0_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // arguments?
   private static boolean operand_1_0_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "operand_1_0_0_1")) return false;
     arguments(builder_, level_ + 1);
     return true;
   }
 
   // LBRACKET arguments? RBRACKET
   private static boolean operand_1_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "operand_1_0_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LBRACKET);
     result_ = result_ && operand_1_0_1_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RBRACKET);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // arguments?
   private static boolean operand_1_0_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "operand_1_0_1_1")) return false;
     arguments(builder_, level_ + 1);
     return true;
   }
 
   // DOT variable_name
   private static boolean operand_1_0_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "operand_1_0_2")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, DOT);
     result_ = result_ && variable_name(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // TILDE | PLUS | MINUS | STAR | SLASH | CARET
   //     | EQUAL | EQUAL_EQUAL | TILDE_EQUAL | TILDE_EQUAL_EQUAL
   //     | LESS_THAN | LESS_THAN_EQUAL | GREATER_THAN | GREATER_THAN_EQUAL
   //     | AMPERSAND | VERT_BAR | COLON_EQUAL
   public static boolean operator(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "operator")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<operator>");
     result_ = consumeToken(builder_, TILDE);
     if (!result_) result_ = consumeToken(builder_, PLUS);
     if (!result_) result_ = consumeToken(builder_, MINUS);
     if (!result_) result_ = consumeToken(builder_, STAR);
     if (!result_) result_ = consumeToken(builder_, SLASH);
     if (!result_) result_ = consumeToken(builder_, CARET);
     if (!result_) result_ = consumeToken(builder_, EQUAL);
     if (!result_) result_ = consumeToken(builder_, EQUAL_EQUAL);
     if (!result_) result_ = consumeToken(builder_, TILDE_EQUAL);
     if (!result_) result_ = consumeToken(builder_, TILDE_EQUAL_EQUAL);
     if (!result_) result_ = consumeToken(builder_, LESS_THAN);
     if (!result_) result_ = consumeToken(builder_, LESS_THAN_EQUAL);
     if (!result_) result_ = consumeToken(builder_, GREATER_THAN);
     if (!result_) result_ = consumeToken(builder_, GREATER_THAN_EQUAL);
     if (!result_) result_ = consumeToken(builder_, AMPERSAND);
     if (!result_) result_ = consumeToken(builder_, VERT_BAR);
     if (!result_) result_ = consumeToken(builder_, COLON_EQUAL);
     if (result_) {
       marker_.done(OPERATOR);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // ordinary_name
   public static boolean ordinary_binding_name(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "ordinary_binding_name")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<ordinary binding name>");
     result_ = ordinary_name(builder_, level_ + 1);
     if (result_) {
       marker_.done(ORDINARY_BINDING_NAME);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // nonexpression_word | escaped_name
   static boolean ordinary_name(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "ordinary_name")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = nonexpression_word(builder_, level_ + 1);
     if (!result_) result_ = escaped_name(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // LPAREN parameters? RPAREN EQUAL_ARROW variable SEMICOLON
   //     | LPAREN parameters? RPAREN EQUAL_ARROW LPAREN values_list? RPAREN semicolon?
   //     | LPAREN parameters? RPAREN SEMICOLON?
   public static boolean parameter_list(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "parameter_list")) return false;
     if (!nextTokenIs(builder_, LPAREN)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = parameter_list_0(builder_, level_ + 1);
     if (!result_) result_ = parameter_list_1(builder_, level_ + 1);
     if (!result_) result_ = parameter_list_2(builder_, level_ + 1);
     if (result_) {
       marker_.done(PARAMETER_LIST);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // LPAREN parameters? RPAREN EQUAL_ARROW variable SEMICOLON
   private static boolean parameter_list_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "parameter_list_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LPAREN);
     result_ = result_ && parameter_list_0_1(builder_, level_ + 1);
     result_ = result_ && consumeTokens(builder_, 0, RPAREN, EQUAL_ARROW);
     result_ = result_ && variable(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, SEMICOLON);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // parameters?
   private static boolean parameter_list_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "parameter_list_0_1")) return false;
     parameters(builder_, level_ + 1);
     return true;
   }
 
   // LPAREN parameters? RPAREN EQUAL_ARROW LPAREN values_list? RPAREN semicolon?
   private static boolean parameter_list_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "parameter_list_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LPAREN);
     result_ = result_ && parameter_list_1_1(builder_, level_ + 1);
     result_ = result_ && consumeTokens(builder_, 0, RPAREN, EQUAL_ARROW, LPAREN);
     result_ = result_ && parameter_list_1_5(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     result_ = result_ && parameter_list_1_7(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // parameters?
   private static boolean parameter_list_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "parameter_list_1_1")) return false;
     parameters(builder_, level_ + 1);
     return true;
   }
 
   // values_list?
   private static boolean parameter_list_1_5(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "parameter_list_1_5")) return false;
     values_list(builder_, level_ + 1);
     return true;
   }
 
   // semicolon?
   private static boolean parameter_list_1_7(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "parameter_list_1_7")) return false;
     consumeToken(builder_, SEMICOLON);
     return true;
   }
 
   // LPAREN parameters? RPAREN SEMICOLON?
   private static boolean parameter_list_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "parameter_list_2")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LPAREN);
     result_ = result_ && parameter_list_2_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     result_ = result_ && parameter_list_2_3(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // parameters?
   private static boolean parameter_list_2_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "parameter_list_2_1")) return false;
     parameters(builder_, level_ + 1);
     return true;
   }
 
   // SEMICOLON?
   private static boolean parameter_list_2_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "parameter_list_2_3")) return false;
     consumeToken(builder_, SEMICOLON);
     return true;
   }
 
   /* ********************************************************** */
   // required_parameters COMMA next_rest_key_parameter_list
   //     | required_parameters
   //     | next_rest_key_parameter_list
   public static boolean parameters(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "parameters")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<parameters>");
     result_ = parameters_0(builder_, level_ + 1);
     if (!result_) result_ = required_parameters(builder_, level_ + 1);
     if (!result_) result_ = next_rest_key_parameter_list(builder_, level_ + 1);
     if (result_) {
       marker_.done(PARAMETERS);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // required_parameters COMMA next_rest_key_parameter_list
   private static boolean parameters_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "parameters_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = required_parameters(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, COMMA);
     result_ = result_ && next_rest_key_parameter_list(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // pattern_list (SEMICOLON pattern_list)*
   public static boolean pattern(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<pattern>");
     result_ = pattern_list(builder_, level_ + 1);
     result_ = result_ && pattern_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(PATTERN);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (SEMICOLON pattern_list)*
   private static boolean pattern_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!pattern_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "pattern_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // SEMICOLON pattern_list
   private static boolean pattern_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, SEMICOLON);
     result_ = result_ && pattern_list(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // QUERY word_name default_value?
   //     | QUERY CONSTRAINED_NAME default_value?
   //     | QUERY_QUERY word_name default_value?
   //     | QUERY_QUERY CONSTRAINED_NAME default_value?
   public static boolean pattern_keyword(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern_keyword")) return false;
     if (!nextTokenIs(builder_, QUERY) && !nextTokenIs(builder_, QUERY_QUERY)
         && replaceVariants(builder_, 2, "<pattern keyword>")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<pattern keyword>");
     result_ = pattern_keyword_0(builder_, level_ + 1);
     if (!result_) result_ = pattern_keyword_1(builder_, level_ + 1);
     if (!result_) result_ = pattern_keyword_2(builder_, level_ + 1);
     if (!result_) result_ = pattern_keyword_3(builder_, level_ + 1);
     if (result_) {
       marker_.done(PATTERN_KEYWORD);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // QUERY word_name default_value?
   private static boolean pattern_keyword_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern_keyword_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, QUERY);
     result_ = result_ && word_name(builder_, level_ + 1);
     result_ = result_ && pattern_keyword_0_2(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // default_value?
   private static boolean pattern_keyword_0_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern_keyword_0_2")) return false;
     default_value(builder_, level_ + 1);
     return true;
   }
 
   // QUERY CONSTRAINED_NAME default_value?
   private static boolean pattern_keyword_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern_keyword_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, QUERY, CONSTRAINED_NAME);
     result_ = result_ && pattern_keyword_1_2(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // default_value?
   private static boolean pattern_keyword_1_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern_keyword_1_2")) return false;
     default_value(builder_, level_ + 1);
     return true;
   }
 
   // QUERY_QUERY word_name default_value?
   private static boolean pattern_keyword_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern_keyword_2")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, QUERY_QUERY);
     result_ = result_ && word_name(builder_, level_ + 1);
     result_ = result_ && pattern_keyword_2_2(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // default_value?
   private static boolean pattern_keyword_2_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern_keyword_2_2")) return false;
     default_value(builder_, level_ + 1);
     return true;
   }
 
   // QUERY_QUERY CONSTRAINED_NAME default_value?
   private static boolean pattern_keyword_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern_keyword_3")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, QUERY_QUERY, CONSTRAINED_NAME);
     result_ = result_ && pattern_keyword_3_2(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // default_value?
   private static boolean pattern_keyword_3_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern_keyword_3_2")) return false;
     default_value(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // HASH_ALL_KEYS
   //     | pattern_keyword COMMA pattern_keywords
   //     | pattern_keyword
   public static boolean pattern_keywords(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern_keywords")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<pattern keywords>");
     result_ = consumeToken(builder_, HASH_ALL_KEYS);
     if (!result_) result_ = pattern_keywords_1(builder_, level_ + 1);
     if (!result_) result_ = pattern_keyword(builder_, level_ + 1);
     if (result_) {
       marker_.done(PATTERN_KEYWORDS);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // pattern_keyword COMMA pattern_keywords
   private static boolean pattern_keywords_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern_keywords_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = pattern_keyword(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, COMMA);
     result_ = result_ && pattern_keywords(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // pattern_sequence COMMA pattern_list
   //     | pattern_sequence
   //     | property_list_pattern
   public static boolean pattern_list(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern_list")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<pattern list>");
     result_ = pattern_list_0(builder_, level_ + 1);
     if (!result_) result_ = pattern_sequence(builder_, level_ + 1);
     if (!result_) result_ = property_list_pattern(builder_, level_ + 1);
     if (result_) {
       marker_.done(PATTERN_LIST);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // pattern_sequence COMMA pattern_list
   private static boolean pattern_list_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern_list_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = pattern_sequence(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, COMMA);
     result_ = result_ && pattern_list(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // simple_pattern+
   public static boolean pattern_sequence(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern_sequence")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<pattern sequence>");
     result_ = simple_pattern(builder_, level_ + 1);
     int offset_ = builder_.getCurrentOffset();
     while (result_) {
       if (!simple_pattern(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "pattern_sequence");
         break;
       }
       offset_ = next_offset_;
     }
     if (result_) {
       marker_.done(PATTERN_SEQUENCE);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // QUERY CONSTRAINED_NAME
   //     | QUERY word_name
   //     | ELLIPSIS
   public static boolean pattern_variable(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern_variable")) return false;
     if (!nextTokenIs(builder_, ELLIPSIS) && !nextTokenIs(builder_, QUERY)
         && replaceVariants(builder_, 2, "<pattern variable>")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<pattern variable>");
     result_ = pattern_variable_0(builder_, level_ + 1);
     if (!result_) result_ = pattern_variable_1(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, ELLIPSIS);
     if (result_) {
       marker_.done(PATTERN_VARIABLE);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // QUERY CONSTRAINED_NAME
   private static boolean pattern_variable_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern_variable_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, QUERY, CONSTRAINED_NAME);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // QUERY word_name
   private static boolean pattern_variable_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pattern_variable_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, QUERY);
     result_ = result_ && word_name(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // KEYWORD string
   public static boolean prefix_option(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "prefix_option")) return false;
     if (!nextTokenIs(builder_, KEYWORD)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, KEYWORD);
     result_ = result_ && string(builder_, level_ + 1);
     if (result_) {
       marker_.done(PREFIX_OPTION);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // symbol symbol_value
   public static boolean property(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "property")) return false;
     if (!nextTokenIs(builder_, KEYWORD) && !nextTokenIs(builder_, UNIQUE_STRING_CHARACTER)
         && replaceVariants(builder_, 2, "<property>")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<property>");
     result_ = symbol(builder_, level_ + 1);
     result_ = result_ && symbol_value(builder_, level_ + 1);
     if (result_) {
       marker_.done(PROPERTY);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // property (COMMA property)*
   public static boolean property_list(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "property_list")) return false;
     if (!nextTokenIs(builder_, KEYWORD) && !nextTokenIs(builder_, UNIQUE_STRING_CHARACTER)
         && replaceVariants(builder_, 2, "<property list>")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<property list>");
     result_ = property(builder_, level_ + 1);
     result_ = result_ && property_list_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(PROPERTY_LIST);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (COMMA property)*
   private static boolean property_list_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "property_list_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!property_list_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "property_list_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA property
   private static boolean property_list_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "property_list_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && property(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // HASH_REST pattern_variable COMMA HASH_KEY pattern_keywords?
   //     | HASH_REST pattern_variable
   //     | HASH_KEY pattern_keywords?
   public static boolean property_list_pattern(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "property_list_pattern")) return false;
     if (!nextTokenIs(builder_, HASH_KEY) && !nextTokenIs(builder_, HASH_REST)
         && replaceVariants(builder_, 2, "<property list pattern>")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<property list pattern>");
     result_ = property_list_pattern_0(builder_, level_ + 1);
     if (!result_) result_ = property_list_pattern_1(builder_, level_ + 1);
     if (!result_) result_ = property_list_pattern_2(builder_, level_ + 1);
     if (result_) {
       marker_.done(PROPERTY_LIST_PATTERN);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // HASH_REST pattern_variable COMMA HASH_KEY pattern_keywords?
   private static boolean property_list_pattern_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "property_list_pattern_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_REST);
     result_ = result_ && pattern_variable(builder_, level_ + 1);
     result_ = result_ && consumeTokens(builder_, 0, COMMA, HASH_KEY);
     result_ = result_ && property_list_pattern_0_4(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // pattern_keywords?
   private static boolean property_list_pattern_0_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "property_list_pattern_0_4")) return false;
     pattern_keywords(builder_, level_ + 1);
     return true;
   }
 
   // HASH_REST pattern_variable
   private static boolean property_list_pattern_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "property_list_pattern_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_REST);
     result_ = result_ && pattern_variable(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // HASH_KEY pattern_keywords?
   private static boolean property_list_pattern_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "property_list_pattern_2")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_KEY);
     result_ = result_ && property_list_pattern_2_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // pattern_keywords?
   private static boolean property_list_pattern_2_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "property_list_pattern_2_1")) return false;
     pattern_keywords(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // bracketing_punctuation | non_bracketing_punctuation
   public static boolean punctuation(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "punctuation")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<punctuation>");
     result_ = bracketing_punctuation(builder_, level_ + 1);
     if (!result_) result_ = non_bracketing_punctuation(builder_, level_ + 1);
     if (result_) {
       marker_.done(PUNCTUATION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // KEYWORD LBRACE (variable_name EQUAL_ARROW variable_name (COMMA variable_name EQUAL_ARROW variable_name)*)? RBRACE
   public static boolean rename_option(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "rename_option")) return false;
     if (!nextTokenIs(builder_, KEYWORD)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, KEYWORD, LBRACE);
     result_ = result_ && rename_option_2(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RBRACE);
     if (result_) {
       marker_.done(RENAME_OPTION);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // (variable_name EQUAL_ARROW variable_name (COMMA variable_name EQUAL_ARROW variable_name)*)?
   private static boolean rename_option_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "rename_option_2")) return false;
     rename_option_2_0(builder_, level_ + 1);
     return true;
   }
 
   // variable_name EQUAL_ARROW variable_name (COMMA variable_name EQUAL_ARROW variable_name)*
   private static boolean rename_option_2_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "rename_option_2_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = variable_name(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, EQUAL_ARROW);
     result_ = result_ && variable_name(builder_, level_ + 1);
     result_ = result_ && rename_option_2_0_3(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (COMMA variable_name EQUAL_ARROW variable_name)*
   private static boolean rename_option_2_0_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "rename_option_2_0_3")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!rename_option_2_0_3_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "rename_option_2_0_3");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA variable_name EQUAL_ARROW variable_name
   private static boolean rename_option_2_0_3_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "rename_option_2_0_3_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && variable_name(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, EQUAL_ARROW);
     result_ = result_ && variable_name(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // <<keywordWithValue "required-init-keyword:">> symbol
   public static boolean required_init_keyword_slot_option(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "required_init_keyword_slot_option")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<required init keyword slot option>");
     result_ = keywordWithValue(builder_, level_ + 1, "required-init-keyword:");
     result_ = result_ && symbol(builder_, level_ + 1);
     if (result_) {
       marker_.done(REQUIRED_INIT_KEYWORD_SLOT_OPTION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // variable EQUAL_EQUAL expression
   //     | variable
   public static boolean required_parameter(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "required_parameter")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<required parameter>");
     result_ = required_parameter_0(builder_, level_ + 1);
     if (!result_) result_ = variable(builder_, level_ + 1);
     if (result_) {
       marker_.done(REQUIRED_PARAMETER);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // variable EQUAL_EQUAL expression
   private static boolean required_parameter_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "required_parameter_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = variable(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, EQUAL_EQUAL);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // required_parameter (COMMA required_parameter)*
   public static boolean required_parameters(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "required_parameters")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<required parameters>");
     result_ = required_parameter(builder_, level_ + 1);
     result_ = result_ && required_parameters_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(REQUIRED_PARAMETERS);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (COMMA required_parameter)*
   private static boolean required_parameters_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "required_parameters_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!required_parameters_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "required_parameters_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA required_parameter
   private static boolean required_parameters_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "required_parameters_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && required_parameter(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // core_word | NONDEFINING_FUNCTION_WORD | NONDEFINING_BEGIN_WORD | define_body_word | define_list_word
   public static boolean reserved_word(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "reserved_word")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<reserved word>");
     result_ = core_word(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, NONDEFINING_FUNCTION_WORD);
     if (!result_) result_ = consumeToken(builder_, NONDEFINING_BEGIN_WORD);
     if (!result_) result_ = define_body_word(builder_, level_ + 1);
     if (!result_) result_ = define_list_word(builder_, level_ + 1);
     if (result_) {
       marker_.done(RESERVED_WORD);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // HASH_REST variable_name COMMA key_parameter_list
   //     | HASH_REST variable_name
   //     | key_parameter_list
   public static boolean rest_key_parameter_list(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "rest_key_parameter_list")) return false;
     if (!nextTokenIs(builder_, HASH_KEY) && !nextTokenIs(builder_, HASH_REST)
         && replaceVariants(builder_, 2, "<rest key parameter list>")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<rest key parameter list>");
     result_ = rest_key_parameter_list_0(builder_, level_ + 1);
     if (!result_) result_ = rest_key_parameter_list_1(builder_, level_ + 1);
     if (!result_) result_ = key_parameter_list(builder_, level_ + 1);
     if (result_) {
       marker_.done(REST_KEY_PARAMETER_LIST);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // HASH_REST variable_name COMMA key_parameter_list
   private static boolean rest_key_parameter_list_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "rest_key_parameter_list_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_REST);
     result_ = result_ && variable_name(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, COMMA);
     result_ = result_ && key_parameter_list(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // HASH_REST variable_name
   private static boolean rest_key_parameter_list_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "rest_key_parameter_list_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_REST);
     result_ = result_ && variable_name(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // LBRACE template? RBRACE SEMICOLON?
   public static boolean rhs(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "rhs")) return false;
     if (!nextTokenIs(builder_, LBRACE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LBRACE);
     result_ = result_ && rhs_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RBRACE);
     result_ = result_ && rhs_3(builder_, level_ + 1);
     if (result_) {
       marker_.done(RHS);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // template?
   private static boolean rhs_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "rhs_1")) return false;
     template(builder_, level_ + 1);
     return true;
   }
 
   // SEMICOLON?
   private static boolean rhs_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "rhs_3")) return false;
     consumeToken(builder_, SEMICOLON);
     return true;
   }
 
   /* ********************************************************** */
   // SELECT LPAREN expression (BY expression)? RPAREN select_stmt_clause* select_stmt_tail
   public static boolean select_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_statement")) return false;
     if (!nextTokenIs(builder_, SELECT)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, SELECT, LPAREN);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     result_ = result_ && select_statement_3(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     result_ = result_ && select_statement_5(builder_, level_ + 1);
     result_ = result_ && select_stmt_tail(builder_, level_ + 1);
     if (result_) {
       marker_.done(SELECT_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // (BY expression)?
   private static boolean select_statement_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_statement_3")) return false;
     select_statement_3_0(builder_, level_ + 1);
     return true;
   }
 
   // BY expression
   private static boolean select_statement_3_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_statement_3_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, BY);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // select_stmt_clause*
   private static boolean select_statement_5(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_statement_5")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!select_stmt_clause(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "select_statement_5");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   /* ********************************************************** */
   // select_stmt_label select_stmt_constituents? (SEMICOLON | &select_stmt_tail)
   public static boolean select_stmt_clause(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_stmt_clause")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<select stmt clause>");
     result_ = select_stmt_label(builder_, level_ + 1);
     result_ = result_ && select_stmt_clause_1(builder_, level_ + 1);
     result_ = result_ && select_stmt_clause_2(builder_, level_ + 1);
     if (result_) {
       marker_.done(SELECT_STMT_CLAUSE);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // select_stmt_constituents?
   private static boolean select_stmt_clause_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_stmt_clause_1")) return false;
     select_stmt_constituents(builder_, level_ + 1);
     return true;
   }
 
   // SEMICOLON | &select_stmt_tail
   private static boolean select_stmt_clause_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_stmt_clause_2")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, SEMICOLON);
     if (!result_) result_ = select_stmt_clause_2_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // &select_stmt_tail
   private static boolean select_stmt_clause_2_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_stmt_clause_2_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_AND_, null);
     result_ = select_stmt_tail(builder_, level_ + 1);
     marker_.rollbackTo();
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_AND_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // !select_stmt_label constituent
   public static boolean select_stmt_constituent(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_stmt_constituent")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<select stmt constituent>");
     result_ = select_stmt_constituent_0(builder_, level_ + 1);
     result_ = result_ && constituent(builder_, level_ + 1);
     if (result_) {
       marker_.done(SELECT_STMT_CONSTITUENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // !select_stmt_label
   private static boolean select_stmt_constituent_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_stmt_constituent_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_NOT_, null);
     result_ = !select_stmt_label(builder_, level_ + 1);
     marker_.rollbackTo();
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_NOT_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // select_stmt_constituent (SEMICOLON select_stmt_constituent)*
   public static boolean select_stmt_constituents(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_stmt_constituents")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<select stmt constituents>");
     result_ = select_stmt_constituent(builder_, level_ + 1);
     result_ = result_ && select_stmt_constituents_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(SELECT_STMT_CONSTITUENTS);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (SEMICOLON select_stmt_constituent)*
   private static boolean select_stmt_constituents_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_stmt_constituents_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!select_stmt_constituents_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "select_stmt_constituents_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // SEMICOLON select_stmt_constituent
   private static boolean select_stmt_constituents_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_stmt_constituents_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, SEMICOLON);
     result_ = result_ && select_stmt_constituent(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // OTHERWISE EQUAL_ARROW?
   //     | LPAREN expressions RPAREN EQUAL_ARROW
   //     | expressions EQUAL_ARROW
   public static boolean select_stmt_label(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_stmt_label")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<select stmt label>");
     result_ = select_stmt_label_0(builder_, level_ + 1);
     if (!result_) result_ = select_stmt_label_1(builder_, level_ + 1);
     if (!result_) result_ = select_stmt_label_2(builder_, level_ + 1);
     if (result_) {
       marker_.done(SELECT_STMT_LABEL);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // OTHERWISE EQUAL_ARROW?
   private static boolean select_stmt_label_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_stmt_label_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, OTHERWISE);
     result_ = result_ && select_stmt_label_0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // EQUAL_ARROW?
   private static boolean select_stmt_label_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_stmt_label_0_1")) return false;
     consumeToken(builder_, EQUAL_ARROW);
     return true;
   }
 
   // LPAREN expressions RPAREN EQUAL_ARROW
   private static boolean select_stmt_label_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_stmt_label_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LPAREN);
     result_ = result_ && expressions(builder_, level_ + 1);
     result_ = result_ && consumeTokens(builder_, 0, RPAREN, EQUAL_ARROW);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // expressions EQUAL_ARROW
   private static boolean select_stmt_label_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_stmt_label_2")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = expressions(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, EQUAL_ARROW);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // END SELECT?
   public static boolean select_stmt_tail(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_stmt_tail")) return false;
     if (!nextTokenIs(builder_, END)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, END);
     result_ = result_ && select_stmt_tail_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(SELECT_STMT_TAIL);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // SELECT?
   private static boolean select_stmt_tail_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "select_stmt_tail_1")) return false;
     consumeToken(builder_, SELECT);
     return true;
   }
 
   /* ********************************************************** */
   // SEMICOLON body_fragment?
   public static boolean semicolon_fragment(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "semicolon_fragment")) return false;
     if (!nextTokenIs(builder_, SEMICOLON)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, SEMICOLON);
     result_ = result_ && semicolon_fragment_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(SEMICOLON_FRAGMENT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // body_fragment?
   private static boolean semicolon_fragment_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "semicolon_fragment_1")) return false;
     body_fragment(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // SEMICOLON
   //     | COMMA
   //     | binary_operator
   public static boolean separator(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "separator")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<separator>");
     result_ = consumeToken(builder_, SEMICOLON);
     if (!result_) result_ = consumeToken(builder_, COMMA);
     if (!result_) result_ = binary_operator(builder_, level_ + 1);
     if (result_) {
       marker_.done(SEPARATOR);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // <<keywordWithValue "setter:">> (variable_name|HASH_F)
   public static boolean setter_slot_option(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "setter_slot_option")) return false;
     boolean result_ = false;
     int start_ = builder_.getCurrentOffset();
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<setter slot option>");
     result_ = keywordWithValue(builder_, level_ + 1, "setter:");
     result_ = result_ && setter_slot_option_1(builder_, level_ + 1);
     LighterASTNode last_ = result_? builder_.getLatestDoneMarker() : null;
     if (last_ != null && last_.getStartOffset() == start_ && type_extends_(last_.getTokenType(), SETTER_SLOT_OPTION)) {
       marker_.drop();
     }
     else if (result_) {
       marker_.done(SETTER_SLOT_OPTION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // variable_name|HASH_F
   private static boolean setter_slot_option_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "setter_slot_option_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = variable_name(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, HASH_F);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // (symbol (COMMA symbol)*)?
   public static boolean shared_symbols(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "shared_symbols")) return false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<shared symbols>");
     shared_symbols_0(builder_, level_ + 1);
     marker_.done(SHARED_SYMBOLS);
     exitErrorRecordingSection(builder_, level_, true, false, _SECTION_GENERAL_, null);
     return true;
   }
 
   // symbol (COMMA symbol)*
   private static boolean shared_symbols_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "shared_symbols_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = symbol(builder_, level_ + 1);
     result_ = result_ && shared_symbols_0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (COMMA symbol)*
   private static boolean shared_symbols_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "shared_symbols_0_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!shared_symbols_0_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "shared_symbols_0_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA symbol
   private static boolean shared_symbols_0_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "shared_symbols_0_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && symbol(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // END SHARED_SYMBOLS_T variable_name? | END variable_name?
   public static boolean shared_symbols_definition_tail(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "shared_symbols_definition_tail")) return false;
     if (!nextTokenIs(builder_, END)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = shared_symbols_definition_tail_0(builder_, level_ + 1);
     if (!result_) result_ = shared_symbols_definition_tail_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(SHARED_SYMBOLS_DEFINITION_TAIL);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // END SHARED_SYMBOLS_T variable_name?
   private static boolean shared_symbols_definition_tail_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "shared_symbols_definition_tail_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, END, SHARED_SYMBOLS_T);
     result_ = result_ && shared_symbols_definition_tail_0_2(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // variable_name?
   private static boolean shared_symbols_definition_tail_0_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "shared_symbols_definition_tail_0_2")) return false;
     variable_name(builder_, level_ + 1);
     return true;
   }
 
   // END variable_name?
   private static boolean shared_symbols_definition_tail_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "shared_symbols_definition_tail_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, END);
     result_ = result_ && shared_symbols_definition_tail_1_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // variable_name?
   private static boolean shared_symbols_definition_tail_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "shared_symbols_definition_tail_1_1")) return false;
     variable_name(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // function_macro_call
   //     | variable_name
   //     | constant_fragment
   //     | operator
   //     | bracketed_fragment
   //     | hash_word
   //     | DOT
   //     | COLON_COLON
   //     | EQUAL_ARROW
   //     | QUERY
   //     | QUERY_QUERY
   //     | QUERY_EQUAL
   //     | ELLIPSIS
   //     | HASH_HASH
   //     | OTHERWISE
   //     | PARSED_FUNCTION_CALL
   //     | PARSED_MACRO_CALL
   static boolean simple_fragment(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "simple_fragment")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = function_macro_call(builder_, level_ + 1);
     if (!result_) result_ = variable_name(builder_, level_ + 1);
     if (!result_) result_ = constant_fragment(builder_, level_ + 1);
     if (!result_) result_ = operator(builder_, level_ + 1);
     if (!result_) result_ = bracketed_fragment(builder_, level_ + 1);
     if (!result_) result_ = hash_word(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, DOT);
     if (!result_) result_ = consumeToken(builder_, COLON_COLON);
     if (!result_) result_ = consumeToken(builder_, EQUAL_ARROW);
     if (!result_) result_ = consumeToken(builder_, QUERY);
     if (!result_) result_ = consumeToken(builder_, QUERY_QUERY);
     if (!result_) result_ = consumeToken(builder_, QUERY_EQUAL);
     if (!result_) result_ = consumeToken(builder_, ELLIPSIS);
     if (!result_) result_ = consumeToken(builder_, HASH_HASH);
     if (!result_) result_ = consumeToken(builder_, OTHERWISE);
     if (!result_) result_ = consumeToken(builder_, PARSED_FUNCTION_CALL);
     if (!result_) result_ = consumeToken(builder_, PARSED_MACRO_CALL);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // name_not_end
   //     | EQUAL_ARROW
   //     | bracketed_pattern
   //     | binding_pattern
   //     | pattern_variable
   public static boolean simple_pattern(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "simple_pattern")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<simple pattern>");
     result_ = name_not_end(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, EQUAL_ARROW);
     if (!result_) result_ = bracketed_pattern(builder_, level_ + 1);
     if (!result_) result_ = binding_pattern(builder_, level_ + 1);
     if (!result_) result_ = pattern_variable(builder_, level_ + 1);
     if (result_) {
       marker_.done(SIMPLE_PATTERN);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // <<unreservedNameWithValues "sealed">> | CONSTANT_T
   public static boolean slot_adjective(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "slot_adjective")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<slot adjective>");
     result_ = unreservedNameWithValues(builder_, level_ + 1, "sealed");
     if (!result_) result_ = consumeToken(builder_, CONSTANT_T);
     if (result_) {
       marker_.done(SLOT_ADJECTIVE);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // ((slot_spec|init_arg_spec|inherited_slot_spec) SEMICOLON)*
   public static boolean slot_declarations(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "slot_declarations")) return false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<slot declarations>");
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!slot_declarations_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "slot_declarations");
         break;
       }
       offset_ = next_offset_;
     }
     marker_.done(SLOT_DECLARATIONS);
     exitErrorRecordingSection(builder_, level_, true, false, _SECTION_GENERAL_, null);
     return true;
   }
 
   // (slot_spec|init_arg_spec|inherited_slot_spec) SEMICOLON
   private static boolean slot_declarations_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "slot_declarations_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = slot_declarations_0_0(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, SEMICOLON);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // slot_spec|init_arg_spec|inherited_slot_spec
   private static boolean slot_declarations_0_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "slot_declarations_0_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = slot_spec(builder_, level_ + 1);
     if (!result_) result_ = init_arg_spec(builder_, level_ + 1);
     if (!result_) result_ = inherited_slot_spec(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // setter_slot_option
   //     | init_keyword_slot_option
   //     | required_init_keyword_slot_option
   //     | init_value_slot_option
   //     | init_function_slot_option
   //     | type_slot_option
   public static boolean slot_option(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "slot_option")) return false;
     boolean result_ = false;
     int start_ = builder_.getCurrentOffset();
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<slot option>");
     result_ = setter_slot_option(builder_, level_ + 1);
     if (!result_) result_ = init_keyword_slot_option(builder_, level_ + 1);
     if (!result_) result_ = required_init_keyword_slot_option(builder_, level_ + 1);
     if (!result_) result_ = init_value_slot_option(builder_, level_ + 1);
     if (!result_) result_ = init_function_slot_option(builder_, level_ + 1);
     if (!result_) result_ = type_slot_option(builder_, level_ + 1);
     LighterASTNode last_ = result_? builder_.getLatestDoneMarker() : null;
     if (last_ != null && last_.getStartOffset() == start_ && type_extends_(last_.getTokenType(), SLOT_OPTION)) {
       marker_.drop();
     }
     else if (result_) {
       marker_.done(SLOT_OPTION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // (COMMA slot_option)*
   public static boolean slot_options(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "slot_options")) return false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<slot options>");
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!slot_options_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "slot_options");
         break;
       }
       offset_ = next_offset_;
     }
     marker_.done(SLOT_OPTIONS);
     exitErrorRecordingSection(builder_, level_, true, false, _SECTION_GENERAL_, null);
     return true;
   }
 
   // COMMA slot_option
   private static boolean slot_options_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "slot_options_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && slot_option(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // slot_adjective* allocation? SLOT variable_name (COLON_COLON operand_expr)? init_expression? slot_options?
   public static boolean slot_spec(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "slot_spec")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<slot spec>");
     result_ = slot_spec_0(builder_, level_ + 1);
     result_ = result_ && slot_spec_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, SLOT);
     result_ = result_ && variable_name(builder_, level_ + 1);
     result_ = result_ && slot_spec_4(builder_, level_ + 1);
     result_ = result_ && slot_spec_5(builder_, level_ + 1);
     result_ = result_ && slot_spec_6(builder_, level_ + 1);
     if (result_) {
       marker_.done(SLOT_SPEC);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // slot_adjective*
   private static boolean slot_spec_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "slot_spec_0")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!slot_adjective(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "slot_spec_0");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // allocation?
   private static boolean slot_spec_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "slot_spec_1")) return false;
     allocation(builder_, level_ + 1);
     return true;
   }
 
   // (COLON_COLON operand_expr)?
   private static boolean slot_spec_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "slot_spec_4")) return false;
     slot_spec_4_0(builder_, level_ + 1);
     return true;
   }
 
   // COLON_COLON operand_expr
   private static boolean slot_spec_4_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "slot_spec_4_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COLON_COLON);
     result_ = result_ && operand_expr(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // init_expression?
   private static boolean slot_spec_5(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "slot_spec_5")) return false;
     init_expression(builder_, level_ + 1);
     return true;
   }
 
   // slot_options?
   private static boolean slot_spec_6(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "slot_spec_6")) return false;
     slot_options(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // body
   static boolean source_record(PsiBuilder builder_, int level_) {
     return body(builder_, level_ + 1);
   }
 
   /* ********************************************************** */
   // source_record | variable | expression | word_name | token | case_body | macro
   public static boolean source_records(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "source_records")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<source records>");
     result_ = source_record(builder_, level_ + 1);
     if (!result_) result_ = variable(builder_, level_ + 1);
     if (!result_) result_ = expression(builder_, level_ + 1, -1);
     if (!result_) result_ = word_name(builder_, level_ + 1);
     if (!result_) result_ = token(builder_, level_ + 1);
     if (!result_) result_ = case_body(builder_, level_ + 1);
     if (!result_) result_ = macro(builder_, level_ + 1);
     if (result_) {
       marker_.done(SOURCE_RECORDS);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // begin_statement
   //     | block_statement
   //     | case_statement
   //     | for_statement
   //     | if_statement
   //     | method_statement
   //     | select_statement
   //     | unless_statement
   //     | until_statement
   //     | when_statement
   //     | while_statement
   //     | macro_statement
   public static boolean statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "statement")) return false;
     boolean result_ = false;
     int start_ = builder_.getCurrentOffset();
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<statement>");
     result_ = begin_statement(builder_, level_ + 1);
     if (!result_) result_ = block_statement(builder_, level_ + 1);
     if (!result_) result_ = case_statement(builder_, level_ + 1);
     if (!result_) result_ = for_statement(builder_, level_ + 1);
     if (!result_) result_ = if_statement(builder_, level_ + 1);
     if (!result_) result_ = method_statement(builder_, level_ + 1);
     if (!result_) result_ = select_statement(builder_, level_ + 1);
     if (!result_) result_ = unless_statement(builder_, level_ + 1);
     if (!result_) result_ = until_statement(builder_, level_ + 1);
     if (!result_) result_ = when_statement(builder_, level_ + 1);
     if (!result_) result_ = while_statement(builder_, level_ + 1);
     if (!result_) result_ = macro_statement(builder_, level_ + 1);
     LighterASTNode last_ = result_? builder_.getLatestDoneMarker() : null;
     if (last_ != null && last_.getStartOffset() == start_ && type_extends_(last_.getTokenType(), STATEMENT)) {
       marker_.drop();
     }
     else if (result_) {
       marker_.done(STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // LBRACE macro_name maybe_pattern_and_semicolon END RBRACE EQUAL_ARROW rhs
   public static boolean statement_rule(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "statement_rule")) return false;
     if (!nextTokenIs(builder_, LBRACE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LBRACE);
     result_ = result_ && macro_name(builder_, level_ + 1);
     result_ = result_ && maybe_pattern_and_semicolon(builder_, level_ + 1);
     result_ = result_ && consumeTokens(builder_, 0, END, RBRACE, EQUAL_ARROW);
     result_ = result_ && rhs(builder_, level_ + 1);
     if (result_) {
       marker_.done(STATEMENT_RULE);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // statement_rule+
   public static boolean statement_rules(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "statement_rules")) return false;
     if (!nextTokenIs(builder_, LBRACE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = statement_rule(builder_, level_ + 1);
     int offset_ = builder_.getCurrentOffset();
     while (result_) {
       if (!statement_rule(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "statement_rules");
         break;
       }
       offset_ = next_offset_;
     }
     if (result_) {
       marker_.done(STATEMENT_RULES);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // (STRING_CHARACTER|STRING_ESCAPE_CHARACTER)+
   public static boolean string(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "string")) return false;
     if (!nextTokenIs(builder_, STRING_CHARACTER) && !nextTokenIs(builder_, STRING_ESCAPE_CHARACTER)
         && replaceVariants(builder_, 2, "<string>")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<string>");
     result_ = string_0(builder_, level_ + 1);
     int offset_ = builder_.getCurrentOffset();
     while (result_) {
       if (!string_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "string");
         break;
       }
       offset_ = next_offset_;
     }
     if (result_) {
       marker_.done(STRING);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // STRING_CHARACTER|STRING_ESCAPE_CHARACTER
   private static boolean string_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "string_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, STRING_CHARACTER);
     if (!result_) result_ = consumeToken(builder_, STRING_ESCAPE_CHARACTER);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // string+
   public static boolean string_literal(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "string_literal")) return false;
     if (!nextTokenIs(builder_, STRING_CHARACTER) && !nextTokenIs(builder_, STRING_ESCAPE_CHARACTER)
         && replaceVariants(builder_, 2, "<string literal>")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<string literal>");
     result_ = string(builder_, level_ + 1);
     int offset_ = builder_.getCurrentOffset();
     while (result_) {
       if (!string(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "string_literal");
         break;
       }
       offset_ = next_offset_;
     }
     if (result_) {
       marker_.done(STRING_LITERAL);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // name_prefix? QUERY name_string_or_symbol name_suffix?
   //     | QUERY_QUERY word_name separator? ELLIPSIS
   //     | ELLIPSIS
   //     | QUERY_EQUAL word_name
   public static boolean substitution(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "substitution")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<substitution>");
     result_ = substitution_0(builder_, level_ + 1);
     if (!result_) result_ = substitution_1(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, ELLIPSIS);
     if (!result_) result_ = substitution_3(builder_, level_ + 1);
     if (result_) {
       marker_.done(SUBSTITUTION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // name_prefix? QUERY name_string_or_symbol name_suffix?
   private static boolean substitution_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "substitution_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = substitution_0_0(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, QUERY);
     result_ = result_ && name_string_or_symbol(builder_, level_ + 1);
     result_ = result_ && substitution_0_3(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // name_prefix?
   private static boolean substitution_0_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "substitution_0_0")) return false;
     name_prefix(builder_, level_ + 1);
     return true;
   }
 
   // name_suffix?
   private static boolean substitution_0_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "substitution_0_3")) return false;
     name_suffix(builder_, level_ + 1);
     return true;
   }
 
   // QUERY_QUERY word_name separator? ELLIPSIS
   private static boolean substitution_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "substitution_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, QUERY_QUERY);
     result_ = result_ && word_name(builder_, level_ + 1);
     result_ = result_ && substitution_1_2(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, ELLIPSIS);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // separator?
   private static boolean substitution_1_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "substitution_1_2")) return false;
     separator(builder_, level_ + 1);
     return true;
   }
 
   // QUERY_EQUAL word_name
   private static boolean substitution_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "substitution_3")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, QUERY_EQUAL);
     result_ = result_ && word_name(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // test_suite_component | suite_suite_component
   public static boolean suite_component(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "suite_component")) return false;
     if (!nextTokenIs(builder_, SUITE) && !nextTokenIs(builder_, TEST)
         && replaceVariants(builder_, 2, "<suite component>")) return false;
     boolean result_ = false;
     int start_ = builder_.getCurrentOffset();
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<suite component>");
     result_ = test_suite_component(builder_, level_ + 1);
     if (!result_) result_ = suite_suite_component(builder_, level_ + 1);
     LighterASTNode last_ = result_? builder_.getLatestDoneMarker() : null;
     if (last_ != null && last_.getStartOffset() == start_ && type_extends_(last_.getTokenType(), SUITE_COMPONENT)) {
       marker_.drop();
     }
     else if (result_) {
       marker_.done(SUITE_COMPONENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // suite_component (SEMICOLON suite_component?)*
   public static boolean suite_components(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "suite_components")) return false;
     if (!nextTokenIs(builder_, SUITE) && !nextTokenIs(builder_, TEST)
         && replaceVariants(builder_, 2, "<suite components>")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<suite components>");
     result_ = suite_component(builder_, level_ + 1);
     result_ = result_ && suite_components_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(SUITE_COMPONENTS);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (SEMICOLON suite_component?)*
   private static boolean suite_components_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "suite_components_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!suite_components_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "suite_components_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // SEMICOLON suite_component?
   private static boolean suite_components_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "suite_components_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, SEMICOLON);
     result_ = result_ && suite_components_1_0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // suite_component?
   private static boolean suite_components_1_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "suite_components_1_0_1")) return false;
     suite_component(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // END SUITE variable_name? | END variable_name?
   public static boolean suite_definition_tail(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "suite_definition_tail")) return false;
     if (!nextTokenIs(builder_, END)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = suite_definition_tail_0(builder_, level_ + 1);
     if (!result_) result_ = suite_definition_tail_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(SUITE_DEFINITION_TAIL);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // END SUITE variable_name?
   private static boolean suite_definition_tail_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "suite_definition_tail_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, END, SUITE);
     result_ = result_ && suite_definition_tail_0_2(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // variable_name?
   private static boolean suite_definition_tail_0_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "suite_definition_tail_0_2")) return false;
     variable_name(builder_, level_ + 1);
     return true;
   }
 
   // END variable_name?
   private static boolean suite_definition_tail_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "suite_definition_tail_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, END);
     result_ = result_ && suite_definition_tail_1_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // variable_name?
   private static boolean suite_definition_tail_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "suite_definition_tail_1_1")) return false;
     variable_name(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // SUITE variable_name
   public static boolean suite_suite_component(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "suite_suite_component")) return false;
     if (!nextTokenIs(builder_, SUITE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, SUITE);
     result_ = result_ && variable_name(builder_, level_ + 1);
     if (result_) {
       marker_.done(SUITE_SUITE_COMPONENT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // variable_name (COMMA variable_name)*
   public static boolean supers(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "supers")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<supers>");
     result_ = variable_name(builder_, level_ + 1);
     result_ = result_ && supers_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(SUPERS);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (COMMA variable_name)*
   private static boolean supers_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "supers_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!supers_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "supers_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA variable_name
   private static boolean supers_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "supers_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && variable_name(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // unique_string | KEYWORD
   public static boolean symbol(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "symbol")) return false;
     if (!nextTokenIs(builder_, KEYWORD) && !nextTokenIs(builder_, UNIQUE_STRING_CHARACTER)
         && replaceVariants(builder_, 2, "<symbol>")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<symbol>");
     result_ = unique_string(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, KEYWORD);
     if (result_) {
       marker_.done(SYMBOL);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // basic_fragment
   public static boolean symbol_value(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "symbol_value")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<symbol value>");
     result_ = basic_fragment(builder_, level_ + 1);
     if (result_) {
       marker_.done(SYMBOL_VALUE);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // template_element+
   public static boolean template(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "template")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<template>");
     result_ = template_element(builder_, level_ + 1);
     int offset_ = builder_.getCurrentOffset();
     while (result_) {
       if (!template_element(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "template");
         break;
       }
       offset_ = next_offset_;
     }
     if (result_) {
       marker_.done(TEMPLATE);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // word_name
   //     | symbol
   //     | NUMBER
   //     | CHARACTER_LITERAL
   //     | string
   //     | UNARY_OPERATOR_ONLY
   //     | separator
   //     | hash_word
   //     | DOT
   //     | COLON_COLON
   //     | EQUAL_ARROW
   //     | LPAREN template? RPAREN
   //     | LBRACKET template? RBRACKET
   //     | LBRACE template? RBRACE
   //     | HASH_PAREN template? RPAREN
   //     | HASH_BRACKET template? RBRACKET
   //     | PARSED_LIST_CONSTANT
   //     | PARSED_VECTOR_CONSTANT
   //     | substitution
   public static boolean template_element(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "template_element")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<template element>");
     result_ = word_name(builder_, level_ + 1);
     if (!result_) result_ = symbol(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, NUMBER);
     if (!result_) result_ = consumeToken(builder_, CHARACTER_LITERAL);
     if (!result_) result_ = string(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, UNARY_OPERATOR_ONLY);
     if (!result_) result_ = separator(builder_, level_ + 1);
     if (!result_) result_ = hash_word(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, DOT);
     if (!result_) result_ = consumeToken(builder_, COLON_COLON);
     if (!result_) result_ = consumeToken(builder_, EQUAL_ARROW);
     if (!result_) result_ = template_element_11(builder_, level_ + 1);
     if (!result_) result_ = template_element_12(builder_, level_ + 1);
     if (!result_) result_ = template_element_13(builder_, level_ + 1);
     if (!result_) result_ = template_element_14(builder_, level_ + 1);
     if (!result_) result_ = template_element_15(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, PARSED_LIST_CONSTANT);
     if (!result_) result_ = consumeToken(builder_, PARSED_VECTOR_CONSTANT);
     if (!result_) result_ = substitution(builder_, level_ + 1);
     if (result_) {
       marker_.done(TEMPLATE_ELEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // LPAREN template? RPAREN
   private static boolean template_element_11(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "template_element_11")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LPAREN);
     result_ = result_ && template_element_11_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // template?
   private static boolean template_element_11_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "template_element_11_1")) return false;
     template(builder_, level_ + 1);
     return true;
   }
 
   // LBRACKET template? RBRACKET
   private static boolean template_element_12(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "template_element_12")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LBRACKET);
     result_ = result_ && template_element_12_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RBRACKET);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // template?
   private static boolean template_element_12_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "template_element_12_1")) return false;
     template(builder_, level_ + 1);
     return true;
   }
 
   // LBRACE template? RBRACE
   private static boolean template_element_13(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "template_element_13")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LBRACE);
     result_ = result_ && template_element_13_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RBRACE);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // template?
   private static boolean template_element_13_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "template_element_13_1")) return false;
     template(builder_, level_ + 1);
     return true;
   }
 
   // HASH_PAREN template? RPAREN
   private static boolean template_element_14(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "template_element_14")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_PAREN);
     result_ = result_ && template_element_14_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // template?
   private static boolean template_element_14_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "template_element_14_1")) return false;
     template(builder_, level_ + 1);
     return true;
   }
 
   // HASH_BRACKET template? RBRACKET
   private static boolean template_element_15(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "template_element_15")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_BRACKET);
     result_ = result_ && template_element_15_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, RBRACKET);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // template?
   private static boolean template_element_15_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "template_element_15_1")) return false;
     template(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // END TEST variable_name? | END variable_name?
   public static boolean test_definition_tail(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "test_definition_tail")) return false;
     if (!nextTokenIs(builder_, END)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = test_definition_tail_0(builder_, level_ + 1);
     if (!result_) result_ = test_definition_tail_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(TEST_DEFINITION_TAIL);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // END TEST variable_name?
   private static boolean test_definition_tail_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "test_definition_tail_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, END, TEST);
     result_ = result_ && test_definition_tail_0_2(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // variable_name?
   private static boolean test_definition_tail_0_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "test_definition_tail_0_2")) return false;
     variable_name(builder_, level_ + 1);
     return true;
   }
 
   // END variable_name?
   private static boolean test_definition_tail_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "test_definition_tail_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, END);
     result_ = result_ && test_definition_tail_1_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // variable_name?
   private static boolean test_definition_tail_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "test_definition_tail_1_1")) return false;
     variable_name(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // TEST variable_name
   public static boolean test_suite_component(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "test_suite_component")) return false;
     if (!nextTokenIs(builder_, TEST)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, TEST);
     result_ = result_ && variable_name(builder_, level_ + 1);
     if (result_) {
       marker_.done(TEST_SUITE_COMPONENT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // word_name | symbol | NUMBER | CHARACTER_LITERAL | string | operator | punctuation | hash_word
   public static boolean token(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "token")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<token>");
     result_ = word_name(builder_, level_ + 1);
     if (!result_) result_ = symbol(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, NUMBER);
     if (!result_) result_ = consumeToken(builder_, CHARACTER_LITERAL);
     if (!result_) result_ = string(builder_, level_ + 1);
     if (!result_) result_ = operator(builder_, level_ + 1);
     if (!result_) result_ = punctuation(builder_, level_ + 1);
     if (!result_) result_ = hash_word(builder_, level_ + 1);
     if (result_) {
       marker_.done(TOKEN);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // <<keywordWithValue "type:">> expression
   public static boolean type_slot_option(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "type_slot_option")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<type slot option>");
     result_ = keywordWithValue(builder_, level_ + 1, "type:");
     result_ = result_ && expression(builder_, level_ + 1, -1);
     if (result_) {
       marker_.done(TYPE_SLOT_OPTION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // UNIQUE_STRING_CHARACTER+
   public static boolean unique_string(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "unique_string")) return false;
     if (!nextTokenIs(builder_, UNIQUE_STRING_CHARACTER)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, UNIQUE_STRING_CHARACTER);
     int offset_ = builder_.getCurrentOffset();
     while (result_) {
       if (!consumeToken(builder_, UNIQUE_STRING_CHARACTER)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "unique_string");
         break;
       }
       offset_ = next_offset_;
     }
     if (result_) {
       marker_.done(UNIQUE_STRING);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // UNLESS LPAREN expression RPAREN body? END UNLESS?
   public static boolean unless_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "unless_statement")) return false;
     if (!nextTokenIs(builder_, UNLESS)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, UNLESS, LPAREN);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     result_ = result_ && unless_statement_4(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, END);
     result_ = result_ && unless_statement_6(builder_, level_ + 1);
     if (result_) {
       marker_.done(UNLESS_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // body?
   private static boolean unless_statement_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "unless_statement_4")) return false;
     body(builder_, level_ + 1);
     return true;
   }
 
   // UNLESS?
   private static boolean unless_statement_6(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "unless_statement_6")) return false;
     consumeToken(builder_, UNLESS);
     return true;
   }
 
   /* ********************************************************** */
   // unreserved_word | escaped_name | dylan_unreserved_name
   public static boolean unreserved_name(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "unreserved_name")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<unreserved name>");
     result_ = unreserved_word(builder_, level_ + 1);
     if (!result_) result_ = escaped_name(builder_, level_ + 1);
     if (!result_) result_ = dylan_unreserved_name(builder_, level_ + 1);
     if (result_) {
       marker_.done(UNRESERVED_NAME);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // NONDEFINING_NONEXPRESSION_WORD
   public static boolean unreserved_word(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "unreserved_word")) return false;
     if (!nextTokenIs(builder_, NONDEFINING_NONEXPRESSION_WORD)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, NONDEFINING_NONEXPRESSION_WORD);
     if (result_) {
       marker_.done(UNRESERVED_WORD);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // UNTIL LPAREN expression RPAREN body? END UNTIL?
   public static boolean until_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "until_statement")) return false;
     if (!nextTokenIs(builder_, UNTIL)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, UNTIL, LPAREN);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     result_ = result_ && until_statement_4(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, END);
     result_ = result_ && until_statement_6(builder_, level_ + 1);
     if (result_) {
       marker_.done(UNTIL_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // body?
   private static boolean until_statement_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "until_statement_4")) return false;
     body(builder_, level_ + 1);
     return true;
   }
 
   // UNTIL?
   private static boolean until_statement_6(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "until_statement_6")) return false;
     consumeToken(builder_, UNTIL);
     return true;
   }
 
   /* ********************************************************** */
   // USE ordinary_binding_name (COMMA clause_option)*
   public static boolean use_clause(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "use_clause")) return false;
     if (!nextTokenIs(builder_, USE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, USE);
     result_ = result_ && ordinary_binding_name(builder_, level_ + 1);
     result_ = result_ && use_clause_2(builder_, level_ + 1);
     if (result_) {
       marker_.done(USE_CLAUSE);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // (COMMA clause_option)*
   private static boolean use_clause_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "use_clause_2")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!use_clause_2_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "use_clause_2");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA clause_option
   private static boolean use_clause_2_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "use_clause_2_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && clause_option(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // variables COMMA HASH_REST variable
   //     | variables
   //     | HASH_REST variable
   public static boolean values_list(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "values_list")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<values list>");
     result_ = values_list_0(builder_, level_ + 1);
     if (!result_) result_ = variables(builder_, level_ + 1);
     if (!result_) result_ = values_list_2(builder_, level_ + 1);
     if (result_) {
       marker_.done(VALUES_LIST);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // variables COMMA HASH_REST variable
   private static boolean values_list_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "values_list_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = variables(builder_, level_ + 1);
     result_ = result_ && consumeTokens(builder_, 0, COMMA, HASH_REST);
     result_ = result_ && variable(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // HASH_REST variable
   private static boolean values_list_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "values_list_2")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_REST);
     result_ = result_ && variable(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // variable_name COLON_COLON operand_expr | variable_name
   public static boolean variable(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "variable")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<variable>");
     result_ = variable_0(builder_, level_ + 1);
     if (!result_) result_ = variable_name(builder_, level_ + 1);
     if (result_) {
       marker_.done(VARIABLE);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // variable_name COLON_COLON operand_expr
   private static boolean variable_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "variable_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = variable_name(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, COLON_COLON);
     result_ = result_ && operand_expr(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // variables COMMA HASH_REST variable_name | variables | HASH_REST variable_name
   public static boolean variable_list(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "variable_list")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<variable list>");
     result_ = variable_list_0(builder_, level_ + 1);
     if (!result_) result_ = variables(builder_, level_ + 1);
     if (!result_) result_ = variable_list_2(builder_, level_ + 1);
     if (result_) {
       marker_.done(VARIABLE_LIST);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // variables COMMA HASH_REST variable_name
   private static boolean variable_list_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "variable_list_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = variables(builder_, level_ + 1);
     result_ = result_ && consumeTokens(builder_, 0, COMMA, HASH_REST);
     result_ = result_ && variable_name(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // HASH_REST variable_name
   private static boolean variable_list_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "variable_list_2")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, HASH_REST);
     result_ = result_ && variable_name(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // ordinary_name | VARIABLE_IGNORE
   public static boolean variable_name(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "variable_name")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<variable name>");
     result_ = ordinary_name(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, VARIABLE_IGNORE);
     if (result_) {
       marker_.done(VARIABLE_NAME);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // (variable_name | NONDEFINING_BEGIN_WORD) (EQUAL_ARROW (variable_name | NONDEFINING_BEGIN_WORD))?
   public static boolean variable_spec(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "variable_spec")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<variable spec>");
     result_ = variable_spec_0(builder_, level_ + 1);
     result_ = result_ && variable_spec_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(VARIABLE_SPEC);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // variable_name | NONDEFINING_BEGIN_WORD
   private static boolean variable_spec_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "variable_spec_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = variable_name(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, NONDEFINING_BEGIN_WORD);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (EQUAL_ARROW (variable_name | NONDEFINING_BEGIN_WORD))?
   private static boolean variable_spec_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "variable_spec_1")) return false;
     variable_spec_1_0(builder_, level_ + 1);
     return true;
   }
 
   // EQUAL_ARROW (variable_name | NONDEFINING_BEGIN_WORD)
   private static boolean variable_spec_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "variable_spec_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, EQUAL_ARROW);
     result_ = result_ && variable_spec_1_0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // variable_name | NONDEFINING_BEGIN_WORD
   private static boolean variable_spec_1_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "variable_spec_1_0_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = variable_name(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, NONDEFINING_BEGIN_WORD);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // (variable_spec (COMMA variable_spec)*)?
   static boolean variable_specs(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "variable_specs")) return false;
     variable_specs_0(builder_, level_ + 1);
     return true;
   }
 
   // variable_spec (COMMA variable_spec)*
   private static boolean variable_specs_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "variable_specs_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = variable_spec(builder_, level_ + 1);
     result_ = result_ && variable_specs_0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (COMMA variable_spec)*
   private static boolean variable_specs_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "variable_specs_0_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!variable_specs_0_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "variable_specs_0_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA variable_spec
   private static boolean variable_specs_0_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "variable_specs_0_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && variable_spec(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // variable (COMMA variable)*
   public static boolean variables(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "variables")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<variables>");
     result_ = variable(builder_, level_ + 1);
     result_ = result_ && variables_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(VARIABLES);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (COMMA variable)*
   private static boolean variables_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "variables_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!variables_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "variables_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA variable
   private static boolean variables_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "variables_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && variable(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // WHEN LPAREN expression RPAREN body? END WHEN?
   public static boolean when_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "when_statement")) return false;
     if (!nextTokenIs(builder_, WHEN)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, WHEN, LPAREN);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     result_ = result_ && when_statement_4(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, END);
     result_ = result_ && when_statement_6(builder_, level_ + 1);
     if (result_) {
       marker_.done(WHEN_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // body?
   private static boolean when_statement_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "when_statement_4")) return false;
     body(builder_, level_ + 1);
     return true;
   }
 
   // WHEN?
   private static boolean when_statement_6(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "when_statement_6")) return false;
     consumeToken(builder_, WHEN);
     return true;
   }
 
   /* ********************************************************** */
   // WHILE LPAREN expression RPAREN body? END WHILE?
   public static boolean while_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "while_statement")) return false;
     if (!nextTokenIs(builder_, WHILE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, WHILE, LPAREN);
     result_ = result_ && expression(builder_, level_ + 1, -1);
     result_ = result_ && consumeToken(builder_, RPAREN);
     result_ = result_ && while_statement_4(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, END);
     result_ = result_ && while_statement_6(builder_, level_ + 1);
     if (result_) {
       marker_.done(WHILE_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // body?
   private static boolean while_statement_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "while_statement_4")) return false;
     body(builder_, level_ + 1);
     return true;
   }
 
   // WHILE?
   private static boolean while_statement_6(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "while_statement_6")) return false;
     consumeToken(builder_, WHILE);
     return true;
   }
 
   /* ********************************************************** */
   // reserved_word | unreserved_name
   public static boolean word_name(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "word_name")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<word name>");
     result_ = reserved_word(builder_, level_ + 1);
     if (!result_) result_ = unreserved_name(builder_, level_ + 1);
     if (result_) {
       marker_.done(WORD_NAME);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // Expression root: expression
   // Operator priority table:
   // 0: BINARY(assign_expr)
   // 1: BINARY(and_expr) BINARY(or_expr)
   // 2: BINARY(ident_expr) BINARY(eq_expr) BINARY(nonident_expr) BINARY(neq_expr) BINARY(lt_expr) BINARY(gt_expr) BINARY(lteq_expr) BINARY(gteq_expr)
   // 3: BINARY(plus_expr) BINARY(minus_expr)
   // 4: BINARY(mul_expr) BINARY(div_expr)
   // 5: POSTFIX(exp_expr)
   // 6: PREFIX(arith_neg_expr) PREFIX(log_neg_expr)
   // 7: ATOM(operand_expr)
   public static boolean expression(PsiBuilder builder_, int level_, int priority_) {
     if (!recursion_guard_(builder_, level_, "expression")) return false;
     Marker marker_ = builder_.mark();
     boolean result_ = false;
     boolean pinned_ = false;
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<expression>");
     result_ = arith_neg_expr(builder_, level_ + 1);
     if (!result_) result_ = log_neg_expr(builder_, level_ + 1);
     if (!result_) result_ = operand_expr(builder_, level_ + 1);
     pinned_ = result_;
     result_ = result_ && expression_0(builder_, level_ + 1, priority_);
     if (!result_ && !pinned_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, pinned_, _SECTION_GENERAL_, null);
     return result_ || pinned_;
   }
 
   public static boolean expression_0(PsiBuilder builder_, int level_, int priority_) {
     if (!recursion_guard_(builder_, level_, "expression_0")) return false;
     boolean result_ = true;
     while (true) {
       Marker left_marker_ = (Marker) builder_.getLatestDoneMarker();
       if (!invalid_left_marker_guard_(builder_, left_marker_, "expression_0")) return false;
       Marker marker_ = builder_.mark();
       if (priority_ < 0 && consumeToken(builder_, COLON_EQUAL)) {
         result_ = report_error_(builder_, expression(builder_, level_, -1));
         marker_.drop();
         left_marker_.precede().done(ASSIGN_EXPR);
       }
       else if (priority_ < 1 && consumeToken(builder_, AMPERSAND)) {
         result_ = report_error_(builder_, expression(builder_, level_, 1));
         marker_.drop();
         left_marker_.precede().done(AND_EXPR);
       }
       else if (priority_ < 1 && consumeToken(builder_, VERT_BAR)) {
         result_ = report_error_(builder_, expression(builder_, level_, 1));
         marker_.drop();
         left_marker_.precede().done(OR_EXPR);
       }
       else if (priority_ < 2 && consumeToken(builder_, EQUAL_EQUAL)) {
         result_ = report_error_(builder_, expression(builder_, level_, 2));
         marker_.drop();
         left_marker_.precede().done(IDENT_EXPR);
       }
       else if (priority_ < 2 && consumeToken(builder_, EQUAL)) {
         result_ = report_error_(builder_, expression(builder_, level_, 2));
         marker_.drop();
         left_marker_.precede().done(EQ_EXPR);
       }
       else if (priority_ < 2 && consumeToken(builder_, TILDE_EQUAL_EQUAL)) {
         result_ = report_error_(builder_, expression(builder_, level_, 2));
         marker_.drop();
         left_marker_.precede().done(NONIDENT_EXPR);
       }
       else if (priority_ < 2 && consumeToken(builder_, TILDE_EQUAL)) {
         result_ = report_error_(builder_, expression(builder_, level_, 2));
         marker_.drop();
         left_marker_.precede().done(NEQ_EXPR);
       }
       else if (priority_ < 2 && consumeToken(builder_, LESS_THAN)) {
         result_ = report_error_(builder_, expression(builder_, level_, 2));
         marker_.drop();
         left_marker_.precede().done(LT_EXPR);
       }
       else if (priority_ < 2 && consumeToken(builder_, GREATER_THAN)) {
         result_ = report_error_(builder_, expression(builder_, level_, 2));
         marker_.drop();
         left_marker_.precede().done(GT_EXPR);
       }
       else if (priority_ < 2 && consumeToken(builder_, LESS_THAN_EQUAL)) {
         result_ = report_error_(builder_, expression(builder_, level_, 2));
         marker_.drop();
         left_marker_.precede().done(LTEQ_EXPR);
       }
       else if (priority_ < 2 && consumeToken(builder_, GREATER_THAN_EQUAL)) {
         result_ = report_error_(builder_, expression(builder_, level_, 2));
         marker_.drop();
         left_marker_.precede().done(GTEQ_EXPR);
       }
       else if (priority_ < 3 && consumeToken(builder_, PLUS)) {
         result_ = report_error_(builder_, expression(builder_, level_, 3));
         marker_.drop();
         left_marker_.precede().done(PLUS_EXPR);
       }
       else if (priority_ < 3 && consumeToken(builder_, MINUS)) {
         result_ = report_error_(builder_, expression(builder_, level_, 3));
         marker_.drop();
         left_marker_.precede().done(MINUS_EXPR);
       }
       else if (priority_ < 4 && consumeToken(builder_, STAR)) {
         result_ = report_error_(builder_, expression(builder_, level_, 4));
         marker_.drop();
         left_marker_.precede().done(MUL_EXPR);
       }
       else if (priority_ < 4 && consumeToken(builder_, SLASH)) {
         result_ = report_error_(builder_, expression(builder_, level_, 4));
         marker_.drop();
         left_marker_.precede().done(DIV_EXPR);
       }
       else if (priority_ < 5 && exp_expr_0(builder_, level_ + 1)) {
         result_ = true;
         marker_.drop();
         left_marker_.precede().done(EXP_EXPR);
       }
       else {
         marker_.rollbackTo();
         break;
       }
     }
     return result_;
   }
 
   public static boolean arith_neg_expr(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "arith_neg_expr")) return false;
     if (!nextTokenIs(builder_, MINUS) && replaceVariants(builder_, 1, "<expression>")) return false;
     boolean result_ = false;
     boolean pinned_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, null);
     result_ = consumeToken(builder_, MINUS);
     pinned_ = result_;
     result_ = pinned_ && expression(builder_, level_, 6) && result_;
     if (result_ || pinned_) {
       marker_.done(ARITH_NEG_EXPR);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, pinned_, _SECTION_GENERAL_, null);
     return result_ || pinned_;
   }
 
   // CARET expr
   private static boolean exp_expr_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "exp_expr_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, CARET, EXPR);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   public static boolean log_neg_expr(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "log_neg_expr")) return false;
     if (!nextTokenIs(builder_, TILDE) && replaceVariants(builder_, 1, "<expression>")) return false;
     boolean result_ = false;
     boolean pinned_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, null);
     result_ = consumeToken(builder_, TILDE);
     pinned_ = result_;
     result_ = pinned_ && expression(builder_, level_, 6) && result_;
     if (result_ || pinned_) {
       marker_.done(LOG_NEG_EXPR);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, pinned_, _SECTION_GENERAL_, null);
     return result_ || pinned_;
   }
 
   // operand
   public static boolean operand_expr(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "operand_expr")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<operand expr>");
     result_ = operand(builder_, level_ + 1);
     if (result_) {
       marker_.done(OPERAND_EXPR);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
 }
