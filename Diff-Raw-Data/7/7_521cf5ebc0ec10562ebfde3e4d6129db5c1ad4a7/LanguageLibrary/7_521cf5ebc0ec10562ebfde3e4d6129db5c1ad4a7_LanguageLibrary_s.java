 package org.spoofax.interpreter.library.language;
 
 import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;
 import org.spoofax.interpreter.library.language.spxlang.SPX_exec_index_manage_command;
 import org.spoofax.interpreter.library.language.spxlang.SPX_index_compilation_unit;
 import org.spoofax.interpreter.library.language.spxlang.SPX_index_equal_resource_uri;
 import org.spoofax.interpreter.library.language.spxlang.SPX_index_get_compilation_unit;
 import org.spoofax.interpreter.library.language.spxlang.SPX_index_get_imported_to_references;
 import org.spoofax.interpreter.library.language.spxlang.SPX_index_get_imports;
 import org.spoofax.interpreter.library.language.spxlang.SPX_index_get_language_descriptor;
 import org.spoofax.interpreter.library.language.spxlang.SPX_index_get_module_declaration;
 import org.spoofax.interpreter.library.language.spxlang.SPX_index_get_module_declarations_of;
 import org.spoofax.interpreter.library.language.spxlang.SPX_index_get_module_definition;
 import org.spoofax.interpreter.library.language.spxlang.SPX_index_get_package_declaration;
 import org.spoofax.interpreter.library.language.spxlang.SPX_index_get_package_declarations_by_LanguageName;
 import org.spoofax.interpreter.library.language.spxlang.SPX_index_get_package_declarations_of;
 import org.spoofax.interpreter.library.language.spxlang.SPX_index_get_related_files_of_packages;
 import org.spoofax.interpreter.library.language.spxlang.SPX_index_import_references;
 import org.spoofax.interpreter.library.language.spxlang.SPX_index_language_descriptor;
 import org.spoofax.interpreter.library.language.spxlang.SPX_index_module_definition;
 import org.spoofax.interpreter.library.language.spxlang.SPX_index_package_declaration;
 import org.spoofax.interpreter.library.language.spxlang.SPX_symtab_define_symbol;
 import org.spoofax.interpreter.library.language.spxlang.SPX_symtab_destroy_scope;
 import org.spoofax.interpreter.library.language.spxlang.SPX_symtab_new_scope;
 import org.spoofax.interpreter.library.language.spxlang.SPX_symtab_resolve_symbols;
 import org.spoofax.interpreter.library.language.spxlang.SPX_symtab_undefine_symbols;
 import org.spoofax.interpreter.library.language.spxlang.SPX_symtab_verify_symbol_exists;
 import org.spoofax.interpreter.library.language.spxlang.SPX_symtab_verify_symbols_have_equal_origin;
 import org.spoofax.interpreter.library.language.spxlang.index.SpxSemanticIndex;
 
 /**
  * @author Lennart Kats <lennart add lclnet.nl>
  */
 public class LanguageLibrary extends AbstractStrategoOperatorRegistry {
     public static final String REGISTRY_NAME = "LANGUAGE";
 
     public LanguageLibrary() {
        addSpxIndexPrimitives();
    }

    /**
     * Primitives related to the Symbol-Table and Index of Spoofax-lang
     */
    private void addSpxIndexPrimitives() {
         SpxSemanticIndex spxIndex = new SpxSemanticIndex();
         add(new SPX_exec_index_manage_command(spxIndex));
 
         // primitives to index spoofaxlang compilation unit
         add(new SPX_index_compilation_unit(spxIndex));
         add(new SPX_index_get_compilation_unit(spxIndex));
 
         // primitives to index packages, language descriptions and modules
         add(new SPX_index_package_declaration(spxIndex));
         add(new SPX_index_language_descriptor(spxIndex));
         add(new SPX_index_module_definition(spxIndex));
         add(new SPX_index_import_references(spxIndex));
 
         add(new SPX_index_get_language_descriptor(spxIndex));
         add(new SPX_index_get_package_declaration(spxIndex));
         add(new SPX_index_get_module_declaration(spxIndex));
         add(new SPX_index_get_module_definition(spxIndex));
         add(new SPX_index_get_module_declarations_of(spxIndex));
         add(new SPX_index_get_package_declarations_of(spxIndex));
         add(new SPX_index_get_package_declarations_by_LanguageName(spxIndex));
 
         add(new SPX_index_get_imports(spxIndex));
         add(new SPX_index_get_imported_to_references(spxIndex));
         add(new SPX_index_get_related_files_of_packages(spxIndex));
         add(new SPX_index_equal_resource_uri(spxIndex));
 
         // Primitives related to symbol-table - i.e. symbol definition and resolving
         add(new SPX_symtab_new_scope(spxIndex));
         add(new SPX_symtab_destroy_scope(spxIndex));
         add(new SPX_symtab_define_symbol(spxIndex));
         add(new SPX_symtab_resolve_symbols(spxIndex));
         add(new SPX_symtab_verify_symbol_exists(spxIndex));
         add(new SPX_symtab_undefine_symbols(spxIndex));
         add(new SPX_symtab_verify_symbols_have_equal_origin(spxIndex));
     }
 
     public String getOperatorRegistryName() {
         return REGISTRY_NAME;
     }
 }
