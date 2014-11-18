/*
 * Copyright (c) 2011-2014, Microsoft Mobile
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.juniversal.translator.cplusplus.astwriters;
import org.juniversal.translator.core.ASTUtil;
import org.juniversal.translator.cplusplus.OutputType;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;


public class CompilationUnitWriter extends CPlusPlusASTWriter {
    public CompilationUnitWriter(CPlusPlusASTWriters cPlusPlusASTWriters) {
        super(cPlusPlusASTWriters);
    }

    public void write(ASTNode node) {
		CompilationUnit compilationUnit = (CompilationUnit) node;

		TypeDeclaration mainTypeDeclaration = ASTUtil.getFirstTypeDeclaration(compilationUnit);

		setPosition(mainTypeDeclaration.getStartPosition());

		if (getContext().getOutputType() == OutputType.HEADER)
			writeHeader(compilationUnit, mainTypeDeclaration);
		else writeSource(compilationUnit, mainTypeDeclaration);
	}

	private void writeHeader(CompilationUnit compilationUnit, TypeDeclaration mainTypeDeclaration) {
		String name = mainTypeDeclaration.getName().getIdentifier();
		String multiIncludeDefine = name.toUpperCase() + "_H";
		Type superclassType = mainTypeDeclaration.getSuperclassType();

		writeln("#ifndef " + multiIncludeDefine);
		writeln("#define " + multiIncludeDefine);
		writeln();

		writeln("#include \"juniversal.h\"");
		if (superclassType != null) {
			if (superclassType instanceof SimpleType)
				writeIncludeForTypeName(((SimpleType) superclassType).getName());
			else if (superclassType instanceof ParameterizedType) {
				// TODO: Finish this; make check for dependencies everywhere in all code via visitor
			}
		}
		writeln();

		writeln("namespace " + getPackageNamespaceName(compilationUnit) + " {");
		writeln("JU_USING_STD_NAMESPACES");
		writeln();

		// Copy class Javadoc or other comments before the class starts
		copySpaceAndComments();

		writeNode(mainTypeDeclaration);

		copySpaceAndComments();

		writeln();
		writeln("}");   // Close namespace definition

		writeln("#endif // " + multiIncludeDefine);
	}

	private void writeSource(CompilationUnit compilationUnit, TypeDeclaration mainTypeDeclaration) {
		writeIncludeForTypeName(mainTypeDeclaration.getName());
		writeln();
		
		writeln("JU_USING_STD_NAMESPACES");
		writeln("using namespace " + getPackageNamespaceName(compilationUnit) + ";");
		writeln();

		setPosition(mainTypeDeclaration.getStartPosition());
		skipSpaceAndComments();   // Skip any Javadoc included in the node

        writeNode(mainTypeDeclaration);

		skipSpaceAndComments();
	}

	private String getPackageNamespaceName(CompilationUnit compilationUnit) {
		PackageDeclaration packageDeclaration = compilationUnit.getPackage();
		return getNamespaceNameForPackageName(packageDeclaration == null ? null : packageDeclaration .getName());
	}
}
