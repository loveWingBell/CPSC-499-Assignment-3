import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.*;
import java.util.*;

/**
 * ANTLR-based analysis tool for Java 1.2.
 * Identifies method and constructor invocations in input files.
 */
public class AntlrInvocationFinder {

    /**
     * Represents a found method/constructor invocation.
     */
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

    /**
     * Listener that walks the parse tree and collects method/constructor invocations.
     **/
    static class InvocationListener extends Java1_2ANTLRParserBaseListener {
        List<InvocationInfo> invocations = new ArrayList<>();
        String fileName;
        CommonTokenStream tokens;

        InvocationListener(String fileName, CommonTokenStream tokens) {
            this.fileName = fileName;
            this.tokens = tokens;
        }

        /**
         * Detect method invocations in enterExpression3
         * A method invocation is: primary (. Identifier)* arguments
         * We look for the pattern where 'arguments' appears as a suffix.
         */
        @Override
        public void enterExpression3(Java1_2ANTLRParser.Expression3Context ctx) {
            // Only the "primary selector* postfixOp*" alternative has a primary child.
            if (ctx.primary() == null) return;

            Java1_2ANTLRParser.PrimaryContext primary = ctx.primary();

            /**
            // 1.  this(args)
            //     THIS arguments?
            **/
            if (primary.THIS() != null && primary.arguments() != null) {
                addInvocation(
                    "this" + srcText(primary.arguments()),
                    primary.getStart()
                );
            }

            /**
            // 2.  super(args)  or  super.method(args)
            //       SUPER superSuffix
            //       superSuffix  ->  arguments
            //                    |   PERIOD Identifier arguments?
            */
            if (primary.SUPER() != null && primary.superSuffix() != null) {
                Java1_2ANTLRParser.SuperSuffixContext ss = primary.superSuffix();

                if (ss.Identifier() == null && ss.arguments() != null) {
                    // super(args)
                    addInvocation("super" + srcText(ss.arguments()), primary.getStart());

                } else if (ss.Identifier() != null && ss.arguments() != null) {
                    // super.method(args)
                    addInvocation(
                        "super." + ss.Identifier().getText() + srcText(ss.arguments()),
                        primary.getStart()
                    );
                }
            }

            /**3.  Identifier { . Identifier } identifierSuffix
            //     Grammar:  Identifier (PERIOD Identifier)* identifierSuffix?
            //
            //     identifierSuffix alternatives that are invocations:
            //       a)  arguments                     ->  method(args)
            //       b)  PERIOD SUPER arguments        ->  Foo.super(args)
            //       (c) PERIOD NEW innerCreator       ->  handled by enterInnerCreator
            */
            if (!primary.Identifier().isEmpty() && primary.identifierSuffix() != null) {
                Java1_2ANTLRParser.IdentifierSuffixContext suffix = primary.identifierSuffix();
                String identChain = buildIdentifierChain(primary);

                // a) Direct call:  method(args)  or  pkg.Class.method(args)
                //    When identifierSuffix is just 'arguments', SUPER() is null.
                if (suffix.arguments() != null && suffix.SUPER() == null) {
                    addInvocation(identChain + srcText(suffix.arguments()), primary.getStart());
                }

                // b) Qualified super delegation:  Foo.super(args)
                //    Grammar:  PERIOD SUPER arguments
                if (suffix.SUPER() != null && suffix.arguments() != null) {
                    addInvocation(identChain + ".super" + srcText(suffix.arguments()), primary.getStart());
                }
            }

            /**4.  Chained method calls via selectors:  a.b().c(args)
            //      PERIOD Identifier arguments?
            //      PERIOD SUPER superSuffix
            // We accumulate the expression text as we go so each callincludes its full left-hand side.
            */
            // Start the running expression from the primary text.
            StringBuilder exprBuilder = new StringBuilder(srcText(primary));

            for (Java1_2ANTLRParser.SelectorContext sel : ctx.selector()) {

                // .method(args)
                if (sel.Identifier() != null && sel.arguments() != null) {
                    String callExpr = exprBuilder.toString()
                            + "." + sel.Identifier().getText()
                            + srcText(sel.arguments());
                    addInvocation(callExpr, sel.getStart());
                }

                // .super(args)  or  .super.method(args)
                if (sel.SUPER() != null && sel.superSuffix() != null) {
                    Java1_2ANTLRParser.SuperSuffixContext ss = sel.superSuffix();

                    if (ss.Identifier() == null && ss.arguments() != null) {
                        // expr.super(args)
                        addInvocation(
                            exprBuilder.toString() + ".super" + srcText(ss.arguments()),
                            sel.getStart()
                        );
                    } else if (ss.Identifier() != null && ss.arguments() != null) {
                        // expr.super.method(args)
                        addInvocation(
                            exprBuilder.toString() + ".super." + ss.Identifier().getText()
                                + srcText(ss.arguments()),
                            sel.getStart()
                        );
                    }
                }

                // Advance the running expression text to include this selector
                // (getSourceText already includes the leading '.').
                exprBuilder.append(srcText(sel));
            }
        }

        /**
         * Detect constructor invocations via 'new'.
         */
        @Override
        public void enterCreator(Java1_2ANTLRParser.CreatorContext ctx) {
            // Array creation (basicType arrayCreatorRest, or qualified arrayCreatorRest)
            // is not a constructor invocation.
            if (ctx.classCreatorRest() == null) return;

            String typeName = ctx.qualifiedIdentifier().getText();
            String argsText = classCreatorArgsText(ctx.classCreatorRest());
            String fullExpr = "new " + typeName + argsText;

            // 'new' is always in the parent PrimaryContext — no need to search.
            ParserRuleContext parent = ctx.getParent();
            if (parent instanceof Java1_2ANTLRParser.PrimaryContext) {
                Java1_2ANTLRParser.PrimaryContext primaryParent =
                        (Java1_2ANTLRParser.PrimaryContext) parent;
                if (primaryParent.NEW() != null) {
                    addInvocation(fullExpr, primaryParent.NEW().getSymbol());
                }
            }
        }

        /**
         * Handles inner class constructor invocations:  expr.new Inner(args)
         *
         * innerCreator is always a child of either a SelectorContext
         * (PERIOD NEW innerCreator) or an IdentifierSuffixContext
         * (PERIOD NEW innerCreator), so 'new' is always in the immediate parent.
         */
        @Override
        public void enterInnerCreator(Java1_2ANTLRParser.InnerCreatorContext ctx) {
            if (ctx.classCreatorRest() == null) return;

            String typeName = ctx.Identifier().getText();
            String argsText = classCreatorArgsText(ctx.classCreatorRest());
            String fullExpr = "new " + typeName + argsText;

            Token newToken = findNewTokenInParent(ctx.getParent());
            if (newToken != null) {
                addInvocation(fullExpr, newToken);
            }
        }

        // ---- Helper methods ----

        private void addInvocation(String expr, Token startToken) {
            invocations.add(new InvocationInfo(
                expr,
                fileName,
                startToken.getLine(),
                startToken.getCharPositionInLine() + 1  // 1-based column
            ));
        }

        // Return the original source text for a parser rule context.
        private String srcText(ParserRuleContext ctx) {
            if (ctx == null) return "";
            return tokens.getText(ctx.getSourceInterval());
        }

        /**
         * Build the dot-separated identifier chain from a primary's Identifier list.
         * e.g. "pkg.ClassName.methodName"
         */
        private String buildIdentifierChain(Java1_2ANTLRParser.PrimaryContext primary) {
            StringBuilder sb = new StringBuilder();
            for (TerminalNode id : primary.Identifier()) {
                if (sb.length() > 0) sb.append(".");
                sb.append(id.getText());
            }
            return sb.toString();
        }

        // Extract the arguments text from a classCreatorRest, e.g. "(x, y)". 
        private String classCreatorArgsText(Java1_2ANTLRParser.ClassCreatorRestContext ctx) {
            if (ctx == null || ctx.arguments() == null) return "()";
            return srcText(ctx.arguments());
        }

        // FIX #7: Bounded search for the NEW token.
        private Token findNewTokenInParent(ParserRuleContext parent) {
            if (parent == null) return null;
            for (int i = 0; i < parent.getChildCount(); i++) {
                ParseTree child = parent.getChild(i);
                if (child instanceof TerminalNode) {
                    Token t = ((TerminalNode) child).getSymbol();
                    if (t.getType() == Java1_2ANTLRLexer.NEW) {
                        return t;
                    }
                }
            }
            return null;  // Not found — do NOT recurse further.
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java AntlrInvocationFinder <file1.java> [file2.java ...]");
            System.exit(1);
        }

        List<InvocationInfo> allInvocations = new ArrayList<>();

        for (String filePath : args) {
        File file = new File(filePath);
        String fileName = file.getName();

        try {
            CharStream input = CharStreams.fromPath(file.toPath());
            Java1_2ANTLRLexer lexer = new Java1_2ANTLRLexer(input);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            Java1_2ANTLRParser parser = new Java1_2ANTLRParser(tokenStream);

            // Parse the compilation unit
            ParseTree tree = parser.compilationUnit();

            if (parser.getNumberOfSyntaxErrors() > 0) {
                System.err.println("Parse errors found in " + fileName);
                continue;
            }

            // Walk the tree and find invocations
            InvocationListener listener = new InvocationListener(fileName, tokenStream);
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(listener, tree);

            allInvocations.addAll(listener.invocations);
            
        } catch (Exception e) {
            System.err.println("Parse errors found in " + fileName);
        }
    }

        // Print results
        System.out.println(allInvocations.size() + " method/constructor invocation(s) found in the input file(s)");
        System.out.println();
        for (InvocationInfo inv : allInvocations) {
            System.out.println(inv);
        }
    }
}
