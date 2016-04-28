grammar Bitsy;

program: programPart+ ;

programPart: statement		#MainStatement
           | functionDefinition  #ProgPartFunctionDefinition
           ;

statement: print ';'
         | varDeclaration ';'
         | assignment ';'
         | returnStat ';'
         | prePostStat ';'
         | ifStat
         | whileStat
         | assignWithDecl ';'
         | expression ';'
         ;

baseExpression: 'input()' #Input
		  | stackExpression #StackExpr
		  | functionCall #funcCallExpr
		  | varName=IDENTIFIER #Variable
		  ;

numExpression: numExpression '+' '+' #PostIncExpr
		  | numExpression '-' '-' #PostDecExpr
		  | '+' numExpression #Positive
		  | '-' numExpression #Negative
		  | '+' '+' numExpression #PreIncExpr
		  | '-' '-' numExpression #PreDecExpr
		  | left=numExpression '^' right=numExpression #Power
		  | left=numExpression '%' right=numExpression #Mod
		  | left=numExpression '/' right=numExpression #Div
		  | left=numExpression '*' right=numExpression #Mult
		  | left=numExpression '-' right=numExpression #Minus
		  | left=numExpression '+' right=numExpression #Plus
		  | number=signedNum #Number
		  | baseExpression #BaseNumExpr
		  ;

boolExpression: left=numExpression '<' right=numExpression #Less
		  | left=numExpression '>' right=numExpression #Greater
		  | left=numExpression '<=' right=numExpression #LessEq
		  | left=numExpression '>=' right=numExpression #GreaterEq
		  | left=boolExpression '&&' right=boolExpression #LogicalAND
		  | left=boolExpression '||' right=boolExpression #LogicalOR
		  | boolValue=BOOLEAN #Boolean
		  | baseExpression #BaseBoolExpr
		  ;

expression: '(' expr=expression ')' #Paran
		  | numExpression #NumExpr
		  | boolExpression #BoolExpr
		  | left=expression '==' right=expression #IsEq
		  | left=expression '!=' right=expression #NotEq
		  ;

stackExpression: varName=IDENTIFIER '.' 'push' '(' expr=expression ')' #StackPush
          | varName=IDENTIFIER '.' 'pop' '(' ')' #StackPop
          | varName=IDENTIFIER '.' 'peek' '(' ')' #StackPeek
          | varName=IDENTIFIER '.' 'isEmpty' '(' ')' #StackIsEmpty
          ; 

varDeclaration: ('int' | 'bool' ) varName=IDENTIFIER #VariableDeclaration
          | 'stack' varName=IDENTIFIER #stackVariableDeclaration
          ;

assignment: varName=IDENTIFIER '=' expr=expression ;

assignWithDecl: varDeclaration '=' expr=expression ;

print: 'print(' argument=expression ')' #printExpr
		  | 'print(' text=QUOTED_STRING ')' #printText
		  ;

returnStat: 'return' (returnValue=expression)? ;

functionDefinition: 'func' funcName=IDENTIFIER '(' params=parameterDeclaration ')' '{' statements=statementList '}' ;

parameterDeclaration: declarations+=varDeclaration (',' declarations+=varDeclaration)*
                    |
                    ;

statementList: (statement)* ;

ifStat: 'if' ifBlock=conditionBlock ( elifBlock+=elifStat )* ( elseBlock= elseStat )? ;

elifStat: 'elif' cond=conditionBlock ;

elseStat: 'else' '{' statements=statementList '}' ;
 
conditionBlock: '(' expr=expression ')' '{' statements=statementList '}';

whileStat: 'while' whileBock=whileConditionBlock;

whileConditionBlock: '(' expr=expression ')' '{' statements=statementList '}';

prePostStat: varName=IDENTIFIER '+' '+' #PostIncVar
         | varName=IDENTIFIER '-' '-' #PostDecVar
         | '+' '+' varName=IDENTIFIER #PreIncVar
         | '-' '-' varName=IDENTIFIER #PreDecVar
         ;

functionCall: funcName=IDENTIFIER '(' arguments=expressionList ')' ;

expressionList: expressions+=expression (',' expressions+=expression)*
              |
              ;

signedNum:  '-' number=NUMBER #NegNum
  |  '+' number=NUMBER #PosNum
  |  number=NUMBER #Num
  ;
  
BOOLEAN: 'true' | 'false';
IDENTIFIER: [a-zA-Z][a-zA-Z0-9]* ;
NUMBER: [0-9]+;
QUOTED_STRING: '"' (~["])* '"' ;
WHITESPACE: [ \t\n\r]+ -> skip;
COMMENT
    :   '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    :   '//' ~[\r\n]* -> skip
    ;