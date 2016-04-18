grammar MyLang;

program: programPart+ ;

programPart: statement ';'       #MainStatement
           | functionDefinition  #ProgPartFunctionDefinition
           ;

statement: println
         | varDeclaration
         | assignment
         ;

expression: left=expression '/' right=expression #Div
          | left=expression '*' right=expression #Mult
          | left=expression '-' right=expression #Minus
          | left=expression '+' right=expression #Plus
          | left=expression '&&' right=expression #LogicalAND
          | left=expression '||' right=expression #LogicalOR
          | left=expression '<' right=expression #Less
          | left=expression '>' right=expression #Greater
          | left=expression '<=' right=expression #LessEq
          | left=expression '>=' right=expression #GreaterEq
          | left=expression '==' right=expression #IsEq
          | left=expression '!=' right=expression #NotEq
          | number=NUMBER #Number
          | varName=IDENTIFIER #Variable
          | functionCall #funcCallExpression
          ;

varDeclaration: 'int' varName=IDENTIFIER ;

assignment: varName=IDENTIFIER '=' expr=expression;

println: 'println(' argument=expression ')' ;

functionDefinition: 'int' funcName=IDENTIFIER '(' params=parameterDeclaration ')' '{' statements=statementList 'return' returnValue=expression ';' '}' ;

parameterDeclaration: declarations+=varDeclaration (',' declarations+=varDeclaration)*
                    |
                    ;

statementList: (statement ';')* ;

functionCall: funcName=IDENTIFIER '(' arguments=expressionList ')' ;

expressionList: expressions+=expression (',' expressions+=expression)*
              |
              ;

IDENTIFIER: [a-zA-Z][a-zA-Z0-9]* ;
NUMBER: [0-9]+;
WHITESPACE: [ \t\n\r]+ -> skip;