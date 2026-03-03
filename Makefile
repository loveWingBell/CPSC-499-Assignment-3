# CPSC 499 Assignment 3 - Elda Britu - 30158734 - March 7, 2025

# Requirements:
#   - JDK (javac + java)

# NOTE: This Makefile uses ';' as the classpath separator for Windows.
#	- On Linux/Mac, change every ';' in -cp arguments to ':'.

# Configurable variables -------------------------------------------

SHELL    := bash
JAVAC    := javac
JAVA     := java

# Path to the ANTLR 4 complete JAR
ANTLR_JAR ?= ./antlr-4.9-complete.jar

# Output directory for test results
OUT_DIR  := test-output

# Directory / path shorthands --------------------------------------

JP327_DIR := Simple-3.27.0
J1_DIR    := Simple-J1.0.0
J8_DIR    := Simple-J8
J12_DIR   := Simple-J1.2
TEST_DIR  := Test Files

JP327_JAR := $(JP327_DIR)/javaparser-core-3.27.0.jar
J1_JAR    := $(J1_DIR)/javaparser-1.0.0.jar

# All test input files (space-safe via $(wildcard))
TEST_FILES := $(wildcard $(TEST_DIR)/*.java) $(wildcard $(TEST_DIR)/*.txt)


# Top-level phony targets --------------------------------------
.PHONY: all build test clean \
        build-jp3 build-j1 build-j8 build-j12 \
        test-jp3  test-j1  test-j8  test-j12

all: build test

build: build-jp3 build-j1 build-j8 build-j12

test: $(OUT_DIR) test-jp3 test-j1 test-j8 test-j12
	@echo ""
	@echo "All tests complete. Results saved to: $(OUT_DIR)/"

$(OUT_DIR):
	mkdir -p "$(OUT_DIR)/jp3_27"
	mkdir -p "$(OUT_DIR)/j1"
	mkdir -p "$(OUT_DIR)/j8"
	mkdir -p "$(OUT_DIR)/j12"


# Simple-3.27.0  --------------------------------------
JP327_SRC   := $(JP327_DIR)/JP3_27AnalysisTool.java
JP327_CLASS := $(JP327_DIR)/JP3_27AnalysisTool.class

build-jp3: $(JP327_CLASS)

$(JP327_CLASS): $(JP327_SRC)
	@echo "[JP3.27] Compiling JP3_27AnalysisTool..."
	$(JAVAC) -cp "$(JP327_JAR)" -d "$(JP327_DIR)" "$<"



test-jp3: $(JP327_CLASS) $(OUT_DIR)
	@echo ""
	@echo "=== Testing Simple-3.27.0 (JP3_27AnalysisTool) ==="
	@find "$(TEST_DIR)" -type f \( -name "*.java" -o -name "*.txt" \) | while read f; do \
	  base=$$(basename "$$f"); \
	  outfile="$(OUT_DIR)/jp3_27/$$base.txt"; \
	  echo "  Running on: $$f"; \
	  $(JAVA) -cp "$(JP327_DIR);$(JP327_JAR)" JP3_27AnalysisTool "$$f" \
	    > "$$outfile" 2>&1; \
	  cat "$$outfile"; \
	  echo ""; \
	done

# Simple-J1.0.0  --------------------------------------
J1_SRC   := $(J1_DIR)/J1AnalysisTool.java
J1_CLASS := $(J1_DIR)/J1AnalysisTool.class

build-j1: $(J1_CLASS)

$(J1_CLASS): $(J1_SRC)
	@echo "[J1.0.0] Compiling J1AnalysisTool..."
	$(JAVAC) -cp "$(J1_JAR)" -d "$(J1_DIR)" "$<"

test-j1: $(J1_CLASS) $(OUT_DIR)
	@echo ""
	@echo "=== Testing Simple-J1.0.0 (J1AnalysisTool) ==="
	@find "$(TEST_DIR)" -type f \( -name "*.java" -o -name "*.txt" \) | while read f; do \
	  base=$$(basename "$$f"); \
	  outfile="$(OUT_DIR)/j1/$$base.txt"; \
	  echo "  Running on: $$f"; \
	  $(JAVA) -cp "$(J1_DIR);$(J1_JAR)" J1AnalysisTool "$$f" \
	    > "$$outfile" 2>&1; \
	  cat "$$outfile"; \
	  echo ""; \
	done

# Simple-J8  –  ANTLR Java 8 grammar --------------------------------------
J8_LEXER_G4  := $(J8_DIR)/Java8Lexer.g4
J8_PARSER_G4 := $(J8_DIR)/Java8Parser.g4
J8_LEXER_SRC := $(J8_DIR)/Java8Lexer.java
J8_PARSER_SRC:= $(J8_DIR)/Java8Parser.java
J8_TOOL_SRC  := $(J8_DIR)/J8AnalysisTool.java
J8_CLASS     := $(J8_DIR)/J8AnalysisTool.class

build-j8: $(J8_CLASS)

# Generates Java sources from the ANTLR grammars.
#	Lexer is generated first so its .tokens file exists for the parser.
$(J8_LEXER_SRC): $(J8_LEXER_G4)
	@echo "[J8] Generating Java8Lexer from grammar..."
	$(JAVA) -jar "$(ANTLR_JAR)" -o "$(J8_DIR)" "$<"
#	lib tells ANTLR where to find imported token vocabularies.
$(J8_PARSER_SRC): $(J8_PARSER_G4) $(J8_LEXER_SRC)
	@echo "[J8] Generating Java8Parser from grammar..."
	$(JAVA) -jar "$(ANTLR_JAR)" -o "$(J8_DIR)" -lib "$(J8_DIR)" "$<"

# Compiles all generated + hand-written Java sources together.
$(J8_CLASS): $(J8_LEXER_SRC) $(J8_PARSER_SRC) $(J8_TOOL_SRC)
	@echo "[J8] Compiling J8AnalysisTool and generated sources..."
	$(JAVAC) -cp "$(J8_DIR);$(ANTLR_JAR)" -d "$(J8_DIR)" \
	  $(J8_DIR)/Java8Lexer.java \
	  $(J8_DIR)/Java8Parser.java \
	  $(J8_DIR)/Java8ParserBaseListener.java \
	  $(J8_DIR)/Java8ParserListener.java \
	  "$(J8_TOOL_SRC)"

test-j8: $(J8_CLASS) $(OUT_DIR)
	@echo ""
	@echo "=== Testing Simple-J8 (J8AnalysisTool) ==="
	@find "$(TEST_DIR)" -type f \( -name "*.java" -o -name "*.txt" \) | while read f; do \
	  base=$$(basename "$$f"); \
	  outfile="$(OUT_DIR)/j8/$$base.txt"; \
	  echo "  Running on: $$f"; \
	  $(JAVA) -cp "$(J8_DIR);$(ANTLR_JAR)" J8AnalysisTool "$$f" \
	    > "$$outfile" 2>&1; \
	  cat "$$outfile"; \
	  echo ""; \
	done

# Simple-J1.2  --------------------------------------
J12_LEXER_G4  := $(J12_DIR)/Java1_2ANTLRLexer.g4
J12_PARSER_G4 := $(J12_DIR)/Java1_2ANTLRParser.g4
J12_LEXER_SRC := $(J12_DIR)/Java1_2ANTLRLexer.java
J12_PARSER_SRC:= $(J12_DIR)/Java1_2ANTLRParser.java
J12_TOOL_SRC  := $(J12_DIR)/AntlrInvocationFinder.java
J12_CLASS     := $(J12_DIR)/AntlrInvocationFinder.class

build-j12: $(J12_CLASS)

# Generates Java sources from the ANTLR grammars.
$(J12_LEXER_SRC): $(J12_LEXER_G4)
	@echo "[J1.2] Generating Java1_2ANTLRLexer from grammar..."
	$(JAVA) -jar "$(ANTLR_JAR)" -o "$(J12_DIR)" "$<"

$(J12_PARSER_SRC): $(J12_PARSER_G4) $(J12_LEXER_SRC)
	@echo "[J1.2] Generating Java1_2ANTLRParser from grammar..."
	$(JAVA) -jar "$(ANTLR_JAR)" -o "$(J12_DIR)" -lib "$(J12_DIR)" "$<"

# Compiles all generated and original Java sources together.
$(J12_CLASS): $(J12_LEXER_SRC) $(J12_PARSER_SRC) $(J12_TOOL_SRC)
	@echo "[J1.2] Compiling AntlrInvocationFinder and generated sources..."
	$(JAVAC) -cp "$(J12_DIR);$(ANTLR_JAR)" -d "$(J12_DIR)" \
	  $(J12_DIR)/Java1_2ANTLRLexer.java \
	  $(J12_DIR)/Java1_2ANTLRParser.java \
	  $(J12_DIR)/Java1_2ANTLRParserBaseListener.java \
	  $(J12_DIR)/Java1_2ANTLRParserListener.java \
	  "$(J12_TOOL_SRC)"

test-j12: $(J12_CLASS) $(OUT_DIR)
	@echo ""
	@echo "=== Testing Simple-J1.2 (AntlrInvocationFinder) ==="
	@find "$(TEST_DIR)" -type f \( -name "*.java" -o -name "*.txt" \) | while read f; do \
	  base=$$(basename "$$f"); \
	  outfile="$(OUT_DIR)/j12/$$base.txt"; \
	  echo "  Running on: $$f"; \
	  $(JAVA) -cp "$(J12_DIR);$(ANTLR_JAR)" AntlrInvocationFinder "$$f" \
	    > "$$outfile" 2>&1; \
	  cat "$$outfile"; \
	  echo ""; \
	done


# Clean  --------------------------------------
clean:
	@echo "Cleaning compiled and generated files..."
	rm -f "$(JP327_DIR)"/*.class
	rm -f "$(J1_DIR)"/*.class
	# J8: remove generated ANTLR sources and all .class files
	rm -f "$(J8_DIR)"/*.class \
	      "$(J8_DIR)"/Java8Lexer.java \
	      "$(J8_DIR)"/Java8Parser.java \
	      "$(J8_DIR)"/Java8ParserBaseListener.java \
	      "$(J8_DIR)"/Java8ParserListener.java \
	      "$(J8_DIR)"/*.tokens \
	      "$(J8_DIR)"/*.interp
	# J12: remove generated ANTLR sources and all .class files
	rm -f "$(J12_DIR)"/*.class \
	      "$(J12_DIR)"/Java1_2ANTLRLexer.java \
	      "$(J12_DIR)"/Java1_2ANTLRParser.java \
	      "$(J12_DIR)"/Java1_2ANTLRParserBaseListener.java \
	      "$(J12_DIR)"/Java1_2ANTLRParserListener.java \
	      "$(J12_DIR)"/*.tokens \
	      "$(J12_DIR)"/*.interp
	rm -rf "$(OUT_DIR)"
	@echo "Done."