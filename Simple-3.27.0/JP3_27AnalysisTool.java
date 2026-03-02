// CPSC 499 Assignment 3 - Elda Britu - 30158734 - March 7, 2025

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.Position;

import java.io.*;
import java.util.*;

public class JP3_27AnalysisTool {

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

    static class InvocationVisitor extends VoidVisitorAdapter<Void> {

        List<InvocationInfo> invocations = new ArrayList<>();
        String fileName;

        InvocationVisitor(String fileName) {
            this.fileName = fileName;
        }

        // Visit every method call expression
        @Override
        public void visit(MethodCallExpr n, Void arg) {
            String expr = buildCallExpr(n);
            int[] pos = getPosition(n);

            invocations.add(new InvocationInfo(expr, fileName, pos[0], pos[1]));
            super.visit(n, arg);
        }

        // Visit every constructor call expression.
        @Override
        public void visit(ObjectCreationExpr n, Void arg) {
            String expr = buildCreationExpr(n);
            int[] pos = getPosition(n);

            invocations.add(new InvocationInfo(expr, fileName, pos[0], pos[1]));
            super.visit(n, arg);
        }

        // Helper functions -----------------------------------

        private String buildCallExpr(MethodCallExpr n) {
            StringBuilder sb = new StringBuilder();

            n.getScope().ifPresent(scope -> sb.append(scope.toString()).append("."));
            sb.append(n.getName().toString());
            sb.append(buildArgList(n.getArguments()));
            return sb.toString();
        }

        private String buildCreationExpr(ObjectCreationExpr n) {
            StringBuilder sb = new StringBuilder();

            n.getScope().ifPresent(scope -> sb.append(scope.toString()).append("."));
            sb.append("new ").append(n.getType().asString());
            sb.append(buildArgList(n.getArguments()));
            return sb.toString();
        }

        // Build an argument-list string.
        private String buildArgList(NodeList<Expression> args) {
            if (args.isEmpty()) return "()";
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < args.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(args.get(i).toString());
            }
            sb.append(")");
            return sb.toString();
        }

        // Retrieve line and column from a node.
        private int[] getPosition(com.github.javaparser.ast.Node n) {
            if (n.getBegin().isPresent()) {
                Position p = n.getBegin().get();
                return new int[]{p.line, p.column};
            }
            return new int[]{-1, -1};
        }
    }

    // Main -----------------------------------

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java JP3_27AnalysisTool <file1.java> [file2.java ...]");
            System.exit(1);
        }

        // Configure JavaParser for Java 8
        ParserConfiguration config = new ParserConfiguration()
            .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_8);
        StaticJavaParser.setConfiguration(config);

        List<InvocationInfo> allInvocations = new ArrayList<>();

        for (String filePath : args) {
            File file = new File(filePath);
            String fileName = file.getName();

            try {
                CompilationUnit cu = StaticJavaParser.parse(file);
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