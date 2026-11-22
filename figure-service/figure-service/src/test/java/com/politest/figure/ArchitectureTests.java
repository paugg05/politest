package com.politest.figure;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Enforces the hexagonal architecture: dependencies always point inwards. */
class ArchitectureTests {

    private static final String BASE = "com.politest.figure";

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE);
    }

    @Test
    void classesAreFound() {
        org.junit.jupiter.api.Assertions.assertFalse(classes.isEmpty(), "no classes were imported");
    }

    @Test
    void domainDoesNotDependOnOuterLayersOrFrameworks() {
        noClasses().that().resideInAPackage(BASE + ".domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        BASE + ".application..", BASE + ".adapter..", BASE + ".config..",
                        "org.springframework..", "com.fasterxml..", "tools.jackson..", "java.sql..", "javax.sql..")
                .check(classes);
    }

    @Test
    void applicationDependsOnlyOnTheDomain() {
        noClasses().that().resideInAPackage(BASE + ".application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        BASE + ".adapter..", BASE + ".config..",
                        "org.springframework..", "com.fasterxml..", "tools.jackson..", "java.sql..", "javax.sql..")
                .check(classes);
    }

    @Test
    void adaptersDoNotDependOnEachOther() {
        noClasses().that().resideInAPackage(BASE + ".adapter.in..")
                .should().dependOnClassesThat().resideInAPackage(BASE + ".adapter.out..")
                .check(classes);
        noClasses().that().resideInAPackage(BASE + ".adapter.out..")
                .should().dependOnClassesThat().resideInAPackage(BASE + ".adapter.in..")
                .check(classes);
    }

    @Test
    void inboundAdaptersOnlyUseInboundPorts() {
        noClasses().that().resideInAPackage(BASE + ".adapter.in..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        BASE + ".application.service..", BASE + ".application.port.out..")
                .check(classes);
    }

    @Test
    void outboundPortsAreOnlyImplementedByOutboundAdapters() {
        classes().that().implement(BASE + ".application.port.out.FigureCatalog")
                .should().resideInAPackage(BASE + ".adapter.out..")
                .check(classes);
    }

    @Test
    void nothingOutsideConfigDependsOnConfig() {
        noClasses().that().resideOutsideOfPackage(BASE + ".config..")
                .should().dependOnClassesThat().resideInAPackage(BASE + ".config..")
                .check(classes);
    }
}
