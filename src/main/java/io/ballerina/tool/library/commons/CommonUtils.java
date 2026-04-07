/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.tool.library.commons;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.FutureTypeSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.StreamTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeDescTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.TextRange;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common utility functions used in the project.
 *
 * @since 1.0.0
 */
public class CommonUtils {

    private static final String UNKNOWN_TYPE = "var";
    private static final Pattern FULLY_QUALIFIED_MODULE_ID_PATTERN =
            Pattern.compile("(\\w+)/([\\w.]+):([^:]+):(\\w+)[|]?");
    private static final String BALLERINA_ORG_NAME = "ballerina";
    private static final List<String> PRE_DECLARED_LANG_LIBS = Arrays.asList("lang.boolean", "lang.decimal",
            "lang.error", "lang.float", "lang.function", "lang.future", "lang.int", "lang.map", "lang.object",
            "lang.stream", "lang.string", "lang.table", "lang.transaction", "lang.typedesc", "lang.xml",
            "lang.natural");

    private CommonUtils() {
    }

    public static TypeSymbol getRawType(TypeSymbol typeDescriptor) {
        if (typeDescriptor.typeKind() == TypeDescKind.INTERSECTION) {
            return getRawType(((IntersectionTypeSymbol) typeDescriptor).effectiveTypeDescriptor());
        }
        if (typeDescriptor.typeKind() == TypeDescKind.TYPE_REFERENCE) {
            TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) typeDescriptor;
            if (typeRef.typeDescriptor().typeKind() == TypeDescKind.INTERSECTION) {
                return getRawType(((IntersectionTypeSymbol) typeRef.typeDescriptor()).effectiveTypeDescriptor());
            }
            TypeSymbol rawType = typeRef.typeDescriptor();
            if (rawType.typeKind() == TypeDescKind.TYPE_REFERENCE) {
                return getRawType(rawType);
            }
            return rawType;
        }
        return typeDescriptor;
    }

    public static String removeLeadingSingleQuote(String input) {
        if (input != null && input.startsWith("'")) {
            return input.substring(1);
        }
        return input;
    }

    public static String removeQuotedIdentifier(String identifier) {
        return identifier.startsWith("'") ? identifier.substring(1) : identifier;
    }

    public static String getClassType(String packageName, String clientName) {
        String importPrefix = packageName.substring(packageName.lastIndexOf('.') + 1);
        return String.format("%s:%s", importPrefix, clientName);
    }

    public static boolean subTypeOf(TypeSymbol source, TypeSymbol target) {
        TypeSymbol sourceRawType = CommonUtils.getRawType(source);
        switch (sourceRawType.typeKind()) {
            case UNION -> {
                UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) sourceRawType;
                return unionTypeSymbol.memberTypeDescriptors().stream().anyMatch(type -> subTypeOf(type, target));
            }
            case TYPEDESC -> {
            }
            case INTERSECTION -> {
                IntersectionTypeSymbol intersectionTypeSymbol = (IntersectionTypeSymbol) sourceRawType;
                return intersectionTypeSymbol.memberTypeDescriptors().stream()
                        .anyMatch(t -> subTypeOf(t, target));
            }
            case TYPE_REFERENCE -> {
                return subTypeOf(sourceRawType, target);
            }
            default -> {
                return sourceRawType.subtypeOf(target);
            }
        }
        return sourceRawType.subtypeOf(target);
    }

    public static String getTypeSignature(SemanticModel semanticModel, TypeSymbol typeSymbol, boolean ignoreError,
                                          ModuleInfo moduleInfo) {
        return switch (typeSymbol.typeKind()) {
            case COMPILATION_ERROR -> UNKNOWN_TYPE;
            case UNION -> {
                UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) typeSymbol;
                yield unionTypeSymbol.memberTypeDescriptors().stream()
                        .filter(memberType -> !ignoreError || !memberType.subtypeOf(semanticModel.types().ERROR))
                        .map(type -> getTypeSignature(semanticModel, type, ignoreError, moduleInfo))
                        .reduce((s1, s2) -> s1 + "|" + s2)
                        .orElse(getTypeSignature(unionTypeSymbol, moduleInfo));
            }
            case TYPEDESC -> {
                TypeDescTypeSymbol typeDescTypeSymbol = (TypeDescTypeSymbol) typeSymbol;
                yield typeDescTypeSymbol.typeParameter()
                        .map(typeParameter -> getTypeSignature(semanticModel, typeParameter, ignoreError, null))
                        .orElse(getTypeSignature(typeDescTypeSymbol, moduleInfo));
            }
            default -> getTypeSignature(typeSymbol, moduleInfo);
        };
    }

    public static String getTypeSignature(SemanticModel semanticModel, TypeSymbol typeSymbol, boolean ignoreError) {
        return getTypeSignature(semanticModel, typeSymbol, ignoreError, null);
    }

    public static String getTypeSignature(TypeSymbol typeSymbol, ModuleInfo moduleInfo) {
        String text = typeSymbol.signature();
        StringBuilder newText = new StringBuilder();
        Matcher matcher = FULLY_QUALIFIED_MODULE_ID_PATTERN.matcher(text);
        int nextStart = 0;
        while (matcher.find()) {
            newText.append(text, nextStart, matcher.start(1));

            String modPart = matcher.group(2);
            int last = modPart.lastIndexOf(".");
            if (last != -1) {
                modPart = modPart.substring(last + 1);
            }

            String typeName = matcher.group(4);

            if (moduleInfo == null || !modPart.equals(moduleInfo.packageName())) {
                newText.append(modPart);
                newText.append(":");
            }
            newText.append(typeName);
            nextStart = matcher.end(4);
        }
        if (nextStart != 0 && nextStart < text.length()) {
            newText.append(text.substring(nextStart));
        }
        return !newText.isEmpty() ? newText.toString() : text;
    }

    public static Optional<String> getImportStatements(TypeSymbol typeSymbol, ModuleInfo moduleInfo) {
        Set<String> imports = new HashSet<>();
        analyzeTypeSymbolForImports(imports, typeSymbol, moduleInfo);
        if (imports.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(String.join(",", imports));
    }

    private static void analyzeTypeSymbolForImports(Set<String> imports, TypeSymbol typeSymbol,
                                                    ModuleInfo moduleInfo) {
        switch (typeSymbol.typeKind()) {
            case UNION -> {
                UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) typeSymbol;
                unionTypeSymbol.memberTypeDescriptors()
                        .forEach(memberType -> analyzeTypeSymbolForImports(imports, memberType, moduleInfo));
            }
            case INTERSECTION -> {
                IntersectionTypeSymbol intersectionTypeSymbol = (IntersectionTypeSymbol) typeSymbol;
                intersectionTypeSymbol.memberTypeDescriptors()
                        .forEach(memberType -> analyzeTypeSymbolForImports(imports, memberType, moduleInfo));
            }
            case TABLE -> {
                TableTypeSymbol tableTypeSymbol = (TableTypeSymbol) typeSymbol;
                analyzeTypeSymbolForImports(imports, tableTypeSymbol.rowTypeParameter(), moduleInfo);
                tableTypeSymbol.keyConstraintTypeParameter()
                        .ifPresent(keyType -> analyzeTypeSymbolForImports(imports, keyType, moduleInfo));
            }
            case TUPLE -> {
                TupleTypeSymbol tupleTypeSymbol = (TupleTypeSymbol) typeSymbol;
                tupleTypeSymbol.memberTypeDescriptors()
                        .forEach(memberType -> analyzeTypeSymbolForImports(imports, memberType, moduleInfo));
            }
            case STREAM -> {
                StreamTypeSymbol streamTypeSymbol = (StreamTypeSymbol) typeSymbol;
                analyzeTypeSymbolForImports(imports, streamTypeSymbol.typeParameter(), moduleInfo);
                analyzeTypeSymbolForImports(imports, streamTypeSymbol.completionValueTypeParameter(), moduleInfo);
            }
            case FUTURE -> {
                FutureTypeSymbol futureTypeSymbol = (FutureTypeSymbol) typeSymbol;
                futureTypeSymbol.typeParameter()
                        .ifPresent(typeParam -> analyzeTypeSymbolForImports(imports, typeParam, moduleInfo));
            }
            case TYPEDESC -> {
                TypeDescTypeSymbol typeDescTypeSymbol = (TypeDescTypeSymbol) typeSymbol;
                typeDescTypeSymbol.typeParameter()
                        .ifPresent(typeParam -> analyzeTypeSymbolForImports(imports, typeParam, moduleInfo));
            }
            case ARRAY -> {
                ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) typeSymbol;
                analyzeTypeSymbolForImports(imports, arrayTypeSymbol.memberTypeDescriptor(), moduleInfo);
            }
            case MAP -> {
                MapTypeSymbol memberTypeSymbol = (MapTypeSymbol) typeSymbol;
                analyzeTypeSymbolForImports(imports, memberTypeSymbol.typeParam(), moduleInfo);
            }
            default -> {
                Optional<ModuleSymbol> moduleSymbol = typeSymbol.getModule();
                if (moduleSymbol.isEmpty()) {
                    return;
                }
                ModuleID moduleId = moduleSymbol.get().id();
                String orgName = moduleId.orgName();
                String packageName = moduleId.packageName();
                String moduleName = moduleId.moduleName();
                String modulePrefix = moduleId.modulePrefix();

                if (isPredefinedLangLib(orgName, packageName) || isAnnotationLangLib(orgName, packageName) ||
                        isWithinCurrentModule(moduleInfo, orgName, packageName, moduleName)) {
                    return;
                }

                if (orgName.equals(moduleInfo.org()) && modulePrefix.equals((moduleInfo.moduleName()))) {
                    imports.add(getImportStatement("", packageName, modulePrefix));
                } else {
                    imports.add(getImportStatement(orgName, packageName, moduleName));
                }
            }
        }
    }

    private static boolean isAnnotationLangLib(String orgName, String packageName) {
        return orgName.equals(BALLERINA_ORG_NAME) && packageName.equals("lang.annotations");
    }

    public static boolean isPredefinedLangLib(String orgName, String packageName) {
        return orgName.equals(BALLERINA_ORG_NAME) && PRE_DECLARED_LANG_LIBS.contains(packageName);
    }

    private static boolean isWithinCurrentModule(ModuleInfo defaultModuleInfo, String orgName, String packageName,
                                                 String moduleName) {
        return orgName.equals(defaultModuleInfo.org()) &&
                packageName.equals(defaultModuleInfo.packageName()) &&
                moduleName.equals(defaultModuleInfo.moduleName());
    }

    public static String getImportStatement(String orgName, String packageName, String moduleName) {
        StringBuilder importStatement = new StringBuilder();
        if (!orgName.isEmpty()) {
            importStatement.append(orgName).append("/");
        }
        if (moduleName != null && moduleName.startsWith(packageName + ".")) {
            importStatement.append(moduleName);
        } else if (moduleName != null && !packageName.equals(moduleName)) {
            importStatement.append(packageName).append(".").append(moduleName);
        } else {
            importStatement.append(packageName);
        }
        return importStatement.toString();
    }

    public static String resolveDefaultValue(Symbol paramSymbol, TypeSymbol typeSymbol,
                                             SemanticModel semanticModel, Package resolvedPackage,
                                             Document document) {
        String defaultValue = DefaultValueGeneratorUtil.getDefaultValueForType(typeSymbol);

        Optional<Location> symbolLocation = paramSymbol.getLocation();
        if (resolvedPackage == null || symbolLocation.isEmpty()) {
            return defaultValue;
        }
        if (document == null) {
            document = findDocument(resolvedPackage, symbolLocation.get().lineRange().fileName());
            if (document == null) {
                return defaultValue;
            }
        }

        ModulePartNode rootNode = document.syntaxTree().rootNode();
        TextRange textRange = symbolLocation.get().textRange();
        NonTerminalNode node = rootNode.findNode(TextRange.from(textRange.startOffset(), textRange.length()));

        ExpressionNode expression;
        switch (node.kind()) {
            case DEFAULTABLE_PARAM -> expression = (ExpressionNode) ((DefaultableParameterNode) node).expression();
            case RECORD_FIELD_WITH_DEFAULT_VALUE -> expression = ((RecordFieldWithDefaultValueNode) node).expression();
            default -> {
                return defaultValue;
            }
        }

        if (expression instanceof SimpleNameReferenceNode simpleNameReferenceNode) {
            String enumValue = resolveEnumMemberValue(simpleNameReferenceNode, resolvedPackage,
                    semanticModel, document);
            return enumValue != null ? enumValue : simpleNameReferenceNode.name().text();
        } else if (expression instanceof QualifiedNameReferenceNode qualifiedNameReferenceNode) {
            String enumValue = resolveEnumMemberValue(qualifiedNameReferenceNode, resolvedPackage,
                    semanticModel, document);
            return enumValue != null ? enumValue :
                    qualifiedNameReferenceNode.modulePrefix().text() + ":" + qualifiedNameReferenceNode.identifier()
                    .text();
        } else {
            return expression.toSourceCode();
        }
    }

    public static Document findDocument(Package pkg, String path) {
        if (pkg == null) {
            return null;
        }
        Project project = pkg.project();
        Module defaultModule = pkg.getDefaultModule();
        String module = pkg.packageName().value();
        Path docPath = project.sourceRoot().resolve("modules").resolve(module).resolve(path);
        try {
            io.ballerina.projects.DocumentId documentId = project.documentId(docPath);
            return defaultModule.document(documentId);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static String resolveEnumMemberValue(ExpressionNode expression, Package resolvedPackage,
                                                 SemanticModel semanticModel, Document document) {
        if (semanticModel == null) {
            return null;
        }

        Optional<Symbol> symbolOpt = semanticModel.symbol(expression);
        if (symbolOpt.isEmpty()) {
            return null;
        }

        Symbol symbol = symbolOpt.get();
        if (symbol.kind() == SymbolKind.CONSTANT) {
            if (symbol instanceof ConstantSymbol constantSymbol) {
                return String.valueOf(constantSymbol.constValue());
            }
            return null;
        }

        if (symbol.kind() != SymbolKind.ENUM_MEMBER) {
            return null;
        }

        Optional<Location> symbolLocation = symbol.getLocation();
        if (resolvedPackage == null || symbolLocation.isEmpty()) {
            return null;
        }

        if (document == null) {
            document = findDocument(resolvedPackage, symbolLocation.get().lineRange().fileName());
            if (document == null) {
                return null;
            }
        }

        ModulePartNode rootNode = document.syntaxTree().rootNode();
        TextRange textRange = symbolLocation.get().textRange();
        NonTerminalNode node = rootNode.findNode(TextRange.from(textRange.startOffset(), textRange.length()));

        if (!(node instanceof EnumMemberNode enumMemberNode)) {
            return null;
        }

        if (enumMemberNode.constExprNode().isEmpty()) {
            return enumMemberNode.identifier().text();
        }

        ExpressionNode valueExpression = enumMemberNode.constExprNode().get();
        return valueExpression.toSourceCode().trim();
    }

}
