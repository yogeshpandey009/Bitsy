grammar MyLang;

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
         ;

expression: '(' expr=expression ')' #Paran
		  | expression '+' '+' #PostIncExpr
		  | expression '-' '-' #PostDecExpr
		  | '+' '+' expression #PreIncExpr
		  | '-' '-' expression #PreDecExpr
		  | left=expression '^' right=expression #Power
		  | left=expression '%' right=expression #Mod
          | left=expression '/' right=expression #Div
          | left=expression '*' right=expression #Mult
          | left=expression '-' right=expression #Minus
          | left=expression '+' right=expression #Plus
          | left=expression '<' right=expression #Less
          | left=expression '>' right=expression #Greater
          | left=expression '<=' right=expression #LessEq
          | left=expression '>=' right=expression #GreaterEq
          | left=expression '==' right=expression #IsEq
          | left=expression '!=' right=expression #NotEq
          | left=expression '&&' right=expression #LogicalAND
          | left=expression '||' right=expression #LogicalOR
          | number=signedNum #Number
          | boolValue=BOOLEAN #Boolean
          | varName=IDENTIFIER #Variable
          | functionCall #funcCallExpression
          ;

varDeclaration: 'int' varName=IDENTIFIER 
          | 'bool' varName=IDENTIFIER ;

assignment: varName=IDENTIFIER '=' expr=expression ;

print: 'print(' argument=expression ')' ;

returnStat: 'return' returnValue=expression ;

functionDefinition: 'int' funcName=IDENTIFIER '(' params=parameterDeclaration ')' '{' statements=statementList '}' ;

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
WHITESPACE: [ \t\n\r]+ -> skip;