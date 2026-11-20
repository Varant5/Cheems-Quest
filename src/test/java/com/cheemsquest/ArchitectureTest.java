package com.cheemsquest;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "com.cheemsquest", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule engineIsPureJava = classes()
            .that().resideInAPackage("com.cheemsquest.engine..")
            .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "com.cheemsquest.engine..")
            .because("the combat engine must stay framework-free and unit-testable without Spring");

    @ArchTest
    static final ArchRule consoleClientUsesOnlyEngine = classes()
            .that().resideInAPackage("com.cheemsquest.console..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "java..", "com.cheemsquest.engine..", "com.cheemsquest.console..")
            .because("the console client is a thin adapter over the engine");
}
