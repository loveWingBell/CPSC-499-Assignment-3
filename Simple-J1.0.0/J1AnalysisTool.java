// CPSC 499 Assignment 3 - Elda Britu - 30158734 - March 7, 2025

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.*;
import java.util.*;

public class J1AnalysisTool {

    // Data class
    static class InvocationInfo {
        String expression;
        String fileName;
        int line;
        int column;

        InvocationInfo(String expression, String fileName, int line, int column) {
            this.expression = expression;
            this.fileName = fileName;
            this.line = line;
            this.column = column;
        }

        @Override
        public String toString() {
            return expression + ": file " + fileName + ", line " + line + ", column " + column;
        }
    }

    // Visitor -----------------------------------

    // Visits every MethodCallExpr and ObjectCreationExpr in the AST.
    static class InvocationVisitor extends VoidVisitorAdapter<Void> {

        List<InvocationInfo> invocations = new ArrayList<>();
        String fileName;

        InvocationVisitor(String fileName) {
            this.fileName = fileName;
        }

        // Called once for every method call in the file, e.g.:
        public void visit(MethodCallExpr n, Void arg) {
            // Reconstruct the full call expression text from the AST fields.
            String expr = buildCallExpr(n);

            invocations.add(new InvocationInfo(
                expr, fileName,
                n.getBeginLine(),
                n.getBeginColumn()
            ));

            // IMPORTANT: must call super so that nested calls inside argument
            // lists are also visited (e.g. foo(bar())).
            super.visit(n, arg);
        }

        // Called once for every constructor call:
        @Override
        public void visit(ObjectCreationExpr n, Void arg) {
            String expr = buildCreationExpr(n);

            invocations.add(new InvocationInfo(
                expr, fileName,
                n.getBeginLine(),
                n.getBeginColumn()
            ));

            super.visit(n, arg);
        }

        // Helpers functions -----------------------------------

        // Reconstruct a method call expression string from its AST node.
        private String buildCallExpr(MethodCallExpr n) {
            StringBuilder sb = new StringBuilder();

            // In JP 1.0.0, getScope() returns null (not Optional) when there is no qualifier (e.g. "obj", "a.b.c").
            if (n.getScope() != null) {
                sb.append(n.getScope().toString()).append(".");
            }

            sb.append(n.getName());
            sb.append(buildArgList(n.getArgs()));
            return sb.toString();
        }

        // Reconstruct a constructor call expression string.
        private String buildCreationExpr(ObjectCreationExpr n) {
            StringBuilder sb = new StringBuilder();

            // Outer expression for inner-class creation: outer.new Inner()
            //In JP 1.0.0, getScope() on ObjectCreationExpr returns the qualifying expression for inner-class creation (outer.new Inner()), or null.
            if (n.getScope() != null) {
                sb.append(n.getScope().toString()).append(".");
            }

            sb.append("new ").append(n.getType().getName());
            sb.append(buildArgList(n.getArgs()));
            return sb.toString();
        }

        // Build an argument-list string like "()" or "(x, y, z)".
        private String buildArgList(List<Expression> args) {
            if (args == null || args.isEmpty()) return "()";
            // getArgs() returns null when there are no arguments, rather than an empty list.
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < args.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(args.get(i).toString());
            }
            sb.append(")");
            return sb.toString();
        }
    }

    // Main -----------------------------------
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java J1AnalysisTool <file1.java> [file2.java ...]");
            System.exit(1);
        }

        List<InvocationInfo> allInvocations = new ArrayList<>();

        for (String filePath : args) {
            File file = new File(filePath);
            String fileName = file.getName();

            try (FileInputStream fis = new FileInputStream(file)) {
                CompilationUnit cu = JavaParser.parse(fis);

                InvocationVisitor visitor = new InvocationVisitor(fileName);
                visitor.visit(cu, null);

                allInvocations.addAll(visitor.invocations);

            } catch (Exception e) {
                System.err.println("Error processing " + fileName + ": " + e.getMessage());
            }
        }

        System.out.println(allInvocations.size() + " method/constructor invocation(s) found in the input file(s)");
        System.out.println();
        for (InvocationInfo inv : allInvocations) {
            System.out.println(inv);
        }
    }
}
