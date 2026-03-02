// CPSC 499 Assignment 3 - Elda Britu - 30158734 - March 7, 2025

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.*;
import java.util.*;

public class J8AnalysisTool {

    // Represents a single found invocation.
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

    // Java 8 listener

    static class InvocationListener extends Java8ParserBaseListener {

        List<InvocationInfo> invocations = new ArrayList<>();
        String fileName;
        CommonTokenStream tokens;

        InvocationListener(String fileName, CommonTokenStream tokens) {
            this.fileName = fileName;
            this.tokens = tokens;
        }

        // Method invocations -----------------------------------

        @Override
        public void enterMethodInvocation(Java8Parser.MethodInvocationContext ctx) {
            recordMethodInvocation(ctx, ctx.getStart());
        }

        @Override
        public void enterMethodInvocation_lf_primary(
                Java8Parser.MethodInvocation_lf_primaryContext ctx) {
            // The leading primary is the left sibling in the parent; just record
            // the full source text of this suffix and the token position.
            recordMethodInvocation(ctx, ctx.getStart());
        }

        // Method invocations in sub-expression contexts that don't start with a primary.
        @Override
        public void enterMethodInvocation_lfno_primary(
                Java8Parser.MethodInvocation_lfno_primaryContext ctx) {
            recordMethodInvocation(ctx, ctx.getStart());
        }

        // Constructor invocations -----------------------------------

        @Override
        public void enterClassInstanceCreationExpression(
                Java8Parser.ClassInstanceCreationExpressionContext ctx) {
            recordCreation(ctx, ctx.getStart());
        }

        // Inner creation suffixed to a primary: '.' 'new' Type ( args )
        @Override
        public void enterClassInstanceCreationExpression_lf_primary(
                Java8Parser.ClassInstanceCreationExpression_lf_primaryContext ctx) {
            recordCreation(ctx, ctx.getStart());
        }

        // Creation in sub-expression contexts without a leading primary.
        @Override
        public void enterClassInstanceCreationExpression_lfno_primary(
                Java8Parser.ClassInstanceCreationExpression_lfno_primaryContext ctx) {
            recordCreation(ctx, ctx.getStart());
        }

        // Helper methods -----------------------------------

        private void recordMethodInvocation(ParserRuleContext ctx, Token start) {
            invocations.add(new InvocationInfo(
                srcText(ctx), fileName,
                start.getLine(),
                start.getCharPositionInLine() + 1
            ));
        }

        private void recordCreation(ParserRuleContext ctx, Token start) {
            invocations.add(new InvocationInfo(
                srcText(ctx), fileName,
                start.getLine(),
                start.getCharPositionInLine() + 1
            ));
        }

        // Return the original source text for any parser rule context.
        private String srcText(ParserRuleContext ctx) {
            if (ctx == null) return "";
            return tokens.getText(ctx.getSourceInterval());
        }
    }

    // Main -----------------------------------
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java J8AnalysisTool <file1.java> [file2.java ...]");
            System.exit(1);
        }

        List<InvocationInfo> allInvocations = new ArrayList<>();

        for (String filePath : args) {
            File file = new File(filePath);
            String fileName = file.getName();

            try {
                CharStream input = CharStreams.fromPath(file.toPath());
                Java8Lexer lexer = new Java8Lexer(input);
                CommonTokenStream tokenStream = new CommonTokenStream(lexer);
                Java8Parser parser = new Java8Parser(tokenStream);

                ParseTree tree = parser.compilationUnit();

                if (parser.getNumberOfSyntaxErrors() > 0) {
                    System.err.println("Parse errors found in " + fileName);
                    continue;
                }

                InvocationListener listener = new InvocationListener(fileName, tokenStream);
                ParseTreeWalker walker = new ParseTreeWalker();
                walker.walk(listener, tree);

                allInvocations.addAll(listener.invocations);

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