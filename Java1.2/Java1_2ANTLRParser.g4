parser grammar Java1_2ANTLRParser;

options { tokenVocab=Java1_2ANTLRLexer; }

identifier: Identifier ;
qualifiedIdentifier: Identifier ( PERIOD Identifier )* ;
literal: IntegerLiteral | FloatingPointLiteral | CharacterLiteral | StringLiteral | BooleanLiteral | NullLiteral ;
expression: expression1 ( assignmentOperator expression1 )? ;
assignmentOperator: EQUALS | PLUS_EQUALS | MINUS_EQUALS | ASTERISK_EQUALS | SLASH_EQUALS | AMPERSAND_EQUALS | PIPE_EQUALS | CARET_EQUALS | PERCENT_EQUALS | DOUBLE_LESS_THAN_EQUALS | DOUBLE_GREATER_THAN_EQUALS | TRIPLE_GREATER_THAN_EQUALS ;
type: Identifier ( PERIOD Identifier )* bracketsOpt | basicType ;
statementExpression: expression ;
constantExpression: expression ;
expression1: expression2 expression1Rest? ;
expression1Rest: QUESTION expression COLON expression1 ;
expression2: expression3 expression2Rest? ;
expression2Rest: (infixop expression3)+ |  expression3 INSTANCEOF type ;
infixop: DOUBLE_PIPE | DOUBLE_AMPERSAND | PIPE | CARET | AMPERSAND | DOUBLE_EQUALS | EXCLAMATION_EQUALS | LESS_THAN | GREATER_THAN | LESS_THAN_EQUALS | GREATER_THAN_EQUALS | DOUBLE_LESS_THAN | DOUBLE_GREATER_THAN | TRIPLE_GREATER_THAN | PLUS | MINUS | ASTERISK | SLASH | PERCENT ;
expression3: prefixOp expression3 | OPEN_PARENTHESIS ( expression | type ) CLOSE_PARENTHESIS expression3 | primary selector* postfixOp* ;
primary: 
	OPEN_PARENTHESIS expression CLOSE_PARENTHESIS 
	| THIS arguments? 
	| SUPER superSuffix 
	| literal 
	| NEW creator 
	| Identifier ( PERIOD Identifier )* identifierSuffix?
	| basicType bracketsOpt PERIOD CLASS
	| VOID PERIOD CLASS ;
identifierSuffix: 
	OPEN_BRACKET ( CLOSE_BRACKET bracketsOpt PERIOD CLASS | expression CLOSE_BRACKET)
	| arguments
	| PERIOD ( CLASS | THIS | SUPER arguments | NEW innerCreator ) ;
prefixOp: DOUBLE_PLUS | DOUBLE_MINUS | EXCLAMATION | TILDE | PLUS | MINUS ;
postfixOp: DOUBLE_PLUS | DOUBLE_MINUS ;
selector:
	PERIOD Identifier arguments?
	| PERIOD THIS
	| PERIOD SUPER superSuffix
	| PERIOD NEW innerCreator
	| OPEN_BRACKET expression CLOSE_BRACKET ;
superSuffix: arguments | PERIOD Identifier arguments? ;
basicType: BYTE | SHORT | CHAR | INT | LONG | FLOAT | DOUBLE | BOOLEAN ;
argumentsOpt: arguments? ;
arguments: OPEN_PARENTHESIS ( expression ( COMMA expression )* )? CLOSE_PARENTHESIS ;
bracketsOpt: (OPEN_BRACKET CLOSE_BRACKET)* ;
creator: qualifiedIdentifier ( arrayCreatorRest | classCreatorRest ) ;
innerCreator: Identifier classCreatorRest ;
arrayCreatorRest: OPEN_BRACKET ( CLOSE_BRACKET bracketsOpt arrayInitializer | expression CLOSE_BRACKET ( OPEN_BRACKET expression CLOSE_BRACKET )* bracketsOpt ) ;
classCreatorRest: arguments classBody? ;
arrayInitializer: OPEN_BRACE ( variableInitializer (COMMA variableInitializer)* COMMA? )? CLOSE_BRACE ;
variableInitializer: arrayInitializer | expression ;
parExpression: OPEN_PARENTHESIS expression CLOSE_PARENTHESIS ;
block: OPEN_BRACE blockStatements CLOSE_BRACE ;
blockStatements: ( blockStatement )* ;
blockStatement: localVariableDeclarationStatement | classOrInterfaceDeclaration | ( Identifier COLON )? statement ;
localVariableDeclarationStatement: FINAL? type variableDeclarators SEMICOLON ;
statement: 
    block
	| IF parExpression statement ( ELSE statement )?
	| FOR OPEN_PARENTHESIS forInit? SEMICOLON expression? SEMICOLON forUpdate? CLOSE_PARENTHESIS statement
	| WHILE parExpression statement
	| DO statement WHILE parExpression SEMICOLON
	| TRY block ( catches | catches? FINALLY block )
	| SWITCH parExpression OPEN_BRACE switchBlockStatementGroups CLOSE_BRACE
	| SYNCHRONIZED parExpression block
	| RETURN expression? SEMICOLON
	| THROW expression SEMICOLON
	| BREAK Identifier? SEMICOLON
	| CONTINUE Identifier? SEMICOLON
	| expressionStatement
	| Identifier COLON statement 
	;
catches: catchClause catchClause* ;
catchClause: CATCH OPEN_PARENTHESIS formalParameter CLOSE_PARENTHESIS block ;
switchBlockStatementGroups: switchBlockStatementGroup* ;
switchBlockStatementGroup: switchLabel blockStatements ;
switchLabel: CASE constantExpression COLON | DEFAULT COLON ;
moreStatementExpressions: ( COMMA statementExpression )* ;
forInit: statementExpression moreStatementExpressions |  FINAL? type variableDeclarators ;
forUpdate: statementExpression moreStatementExpressions ;
expressionStatement: statementExpression SEMICOLON ;
modifiersOpt: modifier* ;
modifier: PUBLIC | PROTECTED | PRIVATE | STATIC | ABSTRACT | FINAL | NATIVE | SYNCHRONIZED | TRANSIENT | VOLATILE | STRICTFP ;
variableDeclarators: variableDeclarator ( COMMA variableDeclarator )* ;
variableDeclaratorsRest: variableDeclaratorRest ( COMMA variableDeclarator )* ;
constantDeclaratorsRest: constantDeclaratorRest ( COMMA constantDeclarator )* ;
variableDeclarator: Identifier variableDeclaratorRest ;
constantDeclarator: Identifier constantDeclaratorRest ;
variableDeclaratorRest: bracketsOpt ( EQUALS variableInitializer )? ;
constantDeclaratorRest: bracketsOpt EQUALS variableInitializer ;
variableDeclaratorId: Identifier bracketsOpt ;
compilationUnit: ( PACKAGE qualifiedIdentifier SEMICOLON )? importDeclaration* typeDeclaration* EOF ;
importDeclaration: IMPORT Identifier ( PERIOD Identifier )* ( PERIOD ASTERISK )? SEMICOLON ;
typeDeclaration: classOrInterfaceDeclaration | SEMICOLON ;
classOrInterfaceDeclaration: modifiersOpt ( classDeclaration | interfaceDeclaration ) ;
classDeclaration: CLASS Identifier ( EXTENDS type )? ( IMPLEMENTS typeList )? classBody ;
interfaceDeclaration: INTERFACE Identifier ( EXTENDS typeList )? interfaceBody ;
typeList: type ( COMMA type )* ;
classBody: OPEN_BRACE classBodyDeclaration* CLOSE_BRACE ;
interfaceBody: OPEN_BRACE interfaceBodyDeclaration* CLOSE_BRACE ;
classBodyDeclaration: SEMICOLON |  STATIC? block | modifiersOpt memberDecl ;
memberDecl: methodOrFieldDecl | VOID Identifier methodDeclaratorRest | Identifier constructorDeclaratorRest | classOrInterfaceDeclaration ;
methodOrFieldDecl: type Identifier methodOrFieldRest ;
methodOrFieldRest: variableDeclaratorRest | methodDeclaratorRest ;
interfaceBodyDeclaration: SEMICOLON | modifiersOpt interfaceMemberDecl ;
interfaceMemberDecl: interfaceMethodOrFieldDecl | VOID Identifier voidInterfaceMethodDeclaratorRest | classOrInterfaceDeclaration ;
interfaceMethodOrFieldDecl: type Identifier interfaceMethodOrFieldRest ;
interfaceMethodOrFieldRest: constantDeclaratorsRest SEMICOLON | interfaceMethodDeclaratorRest ;
methodDeclaratorRest: formalParameters bracketsOpt ( THROWS qualifiedIdentifierList )? ( methodBody | SEMICOLON ) ;
voidMethodDeclaratorRest: formalParameters ( THROWS qualifiedIdentifierList )? ( methodBody | SEMICOLON ) ;
interfaceMethodDeclaratorRest: formalParameters bracketsOpt ( THROWS qualifiedIdentifierList )? SEMICOLON ;
voidInterfaceMethodDeclaratorRest: formalParameters ( THROWS qualifiedIdentifierList )? SEMICOLON ;
constructorDeclaratorRest: formalParameters ( THROWS qualifiedIdentifierList )? methodBody ;
qualifiedIdentifierList: qualifiedIdentifier ( COMMA qualifiedIdentifier )* ;
formalParameters: OPEN_PARENTHESIS ( formalParameter ( COMMA formalParameter )* )? CLOSE_PARENTHESIS ;
formalParameter: FINAL? type variableDeclaratorId ;
methodBody: block ;
