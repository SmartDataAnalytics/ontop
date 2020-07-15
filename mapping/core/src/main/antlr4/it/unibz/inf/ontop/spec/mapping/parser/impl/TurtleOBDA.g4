/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/*
 This grammar is adapted from https://github.com/antlr/grammars-v4/tree/master/turtle,
 derived in turn from http://www.w3.org/TR/turtle/#sec-grammar-grammar,
 with the following copywright:

 [The "BSD licence"]
 Copyright (c) 2014, Alejandro Medrano (@ Universidad Politecnica de Madrid, http://www.upm.es/)
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
*/



grammar TurtleOBDA;

 /*
 Source files (Parser, Visitor, ...) are generated by the ANTLR4 Maven Plugin,
 during the Maven generate-sources phase.
 If src/main/<subPath>/TurtleOBDA.g4 is the path to this file,
 then the source files are generated in target/generated-sources/antlr4/<subPath>
 */


/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/

parse
  : directiveStatement* triplesStatement+ EOF
  ;

directiveStatement
  : directive '.'
  ;

triplesStatement
  : triples '.'
  | quads '.'
  ;

directive
  : base
  | prefixID
  ;

prefixID
  : ('@prefix' | '@PREFIX') PNAME_NS IRIREF
  ;

base
  : ('@base' | '@BASE') IRIREF
  ;

quads
  : 'GRAPH' graph '{' triples+ '}'
  ;

triples
  : subject  predicateObjectList
  ;

predicateObjectList
  : predicateObject (';' predicateObject)*
  ;

predicateObject
  : verb objectList
  ;

objectList
  : object (',' object)*
  ;

verb
  : resource
  | 'a'
  ;

graph
  : resource
  | variable
  | blank
  ;

subject
  : resource
  | variable
  | blank
  ;

object
  : resource
  | blank
  | literal
  | variableLiteral
  | variable
  ;

resource
  : iri
  | iriExt
  ;

iriExt
  : IRIREF_EXT
  | PREFIXED_NAME_EXT
  ;

blank
  : BLANK_NODE_FUNCTION
  | BLANK_NODE_LABEL
  | ANON
  ;

variable
  : STRING_WITH_CURLY_BRACKET
  ;

variableLiteral
  : variable languageTag   # variableLiteral_1
  | variable '^^' iri      # variableLiteral_2
  ;

languageTag
  : LANGTAG
  | '@' variable
  ;

iri
   : IRIREF
   | PREFIXED_NAME
   ;

literal
  : typedLiteral
  | untypedStringLiteral
  | untypedNumericLiteral
  | untypedBooleanLiteral
  ;

untypedStringLiteral
  : litString (languageTag)?
  ;

typedLiteral
  : litString '^^' iri
  ;

litString
  : STRING_LITERAL_QUOTE
//  : STRING_WITH_QUOTE_DOUBLE
  ;

untypedNumericLiteral
  : numericUnsigned
  | numericPositive
  | numericNegative
  ;

untypedBooleanLiteral
  : BOOLEAN_LITERAL
  ;

numericUnsigned
  : INTEGER | DOUBLE | DECIMAL
  ;

numericPositive
  : INTEGER_POSITIVE | DOUBLE_POSITIVE | DECIMAL_POSITIVE
  ;

numericNegative
  : INTEGER_NEGATIVE | DOUBLE_NEGATIVE  | DECIMAL_NEGATIVE
  ;

WS
  : ([\t\r\n\u000C] | ' ') + -> skip
  ;

/*------------------------------------------------------------------
 * LEXER RULES
 Applied for tokenization (before parsing), regardless of parser rules, as follows:
 - The rule matching the longest substring is applied
 - If there are several of them, the first one is applied
 *------------------------------------------------------------------*/

STRING_WITH_CURLY_BRACKET
  : '{' VARIABLE_CHAR+ '}'
  ;

BOOLEAN_LITERAL
  : 'true' | 'TRUE' | 'True' | 'false'| 'FALSE'| 'False'
  ;

// extends IRIREF to allow curly brackets, and forces one curly bracket
IRIREF_EXT
  : '<' IRIREF_INNER_CHAR_EXT* '{' IRIREF_INNER_CHAR_EXT+ '>'
  ;

IRIREF
   : '<' IRIREF_INNER_CHAR* '>'
   ;

PNAME_NS
  : PN_PREFIX? ':'
  ;

PN_PREFIX
   : PN_CHARS_BASE ((PN_CHARS | '.')* PN_CHARS)?
   ;

PREFIXED_NAME
   : PNAME_NS PN_LOCAL
  ;

// extends PREFIXED_NAME to allow right-hand side curly brackets, and force one right-hand side opening curly bracket
PREFIXED_NAME_EXT
  : PNAME_NS PN_LOCAL_EXT
  ;

// specific syntax for blank nodes with variables
BLANK_NODE_FUNCTION
  : '_:'  PN_LOCAL_EXT
  ;

BLANK_NODE_LABEL
  : '_:' (PN_CHARS_U | [0-9]) ((PN_CHARS | '.')* PN_CHARS )?
  ;

LANGTAG
  : '@' [a-zA-Z] + ('-' [a-zA-Z0-9] +)*
  ;

INTEGER
  : [0-9] +
  ;

DECIMAL
  : [0-9]* '.' [0-9] +
  ;

DOUBLE
  : ([0-9] + '.' [0-9]* EXPONENT | '.' [0-9] + EXPONENT | [0-9] + EXPONENT)
  ;

EXPONENT
  : [eE] [+-]? [0-9] +
  ;

INTEGER_POSITIVE
  : '+' INTEGER
  ;

INTEGER_NEGATIVE
  : '-' INTEGER
  ;

DOUBLE_POSITIVE
  : '+' DOUBLE
  ;

DOUBLE_NEGATIVE
  : '-' DOUBLE
  ;

DECIMAL_POSITIVE
  : '+' DECIMAL
  ;

DECIMAL_NEGATIVE
  : '-' DECIMAL
  ;

// not used
STRING_LITERAL_LONG_SINGLE_QUOTE
  : '\'\'\'' (('\'' | '\'\'')? ([^'\\] | ECHAR | UCHAR | '"'))* '\'\'\''
  ;

// not used
STRING_LITERAL_LONG_QUOTE
  : '"""' (('"' | '""')? (~ ["\\] | ECHAR | UCHAR | '\''))* '"""'
  ;

// extends STRING_LITERAL_QUOTE in the original grammar to allow curly brackets, space and escaped characters
STRING_LITERAL_QUOTE
  : '"' (~ ["\\\r\n] | '\'' | '\\"' | '{' | '}' | ' ' | ECHAR)* '"'
  ;
/* orginal version:
  STRING_LITERAL_QUOTE
  : '"' (~ ["\\\r\n] | '\'' | '\\"')* '"'
  ;
*/

// not used
STRING_LITERAL_SINGLE_QUOTE
  : '\'' (~ [\u0027\u005C\u000A\u000D] | ECHAR | UCHAR | '"')* '\''
  ;

UCHAR
  : '\\u' HEX HEX HEX HEX | '\\U' HEX HEX HEX HEX HEX HEX HEX HEX
  ;

ECHAR
  : '\\' [tbnrf"'\\]
  ;

ANON_WS
  : ' ' | '\t' | '\r' | '\n'
  ;

ANON
  : '[' ANON_WS* ']'
  ;

PN_CHARS_BASE
  : 'A' .. 'Z' | 'a' .. 'z' | '\u00C0' .. '\u00D6' | '\u00D8' .. '\u00F6' | '\u00F8' .. '\u02FF' | '\u0370' .. '\u037D' |
  '\u037F' .. '\u1FFF' | '\u200C' .. '\u200D' | '\u2070' .. '\u218F' | '\u2C00' .. '\u2FEF' | '\u3001' .. '\uD7FF' |
    '\uF900' .. '\uFDCF' | '\uFDF0' .. '\uFFFD'
// Limitation: Unicode Characters beyond \uFFFF are not (yet?) supported by ANTLR
//    | '\u10000' .. '\u1FFFD' | '\u20000' .. '\u2FFFD' |
//    '\u30000' .. '\u3FFFD' | '\u40000' .. '\u4FFFD' | '\u50000' .. '\u5FFFD' | '\u60000' .. '\u6FFFD' |
//    '\u70000' .. '\u7FFFD' | '\u80000' .. '\u8FFFD' | '\u90000' .. '\u9FFFD' | '\uA0000' .. '\uAFFFD' |
//    '\uB0000' .. '\uBFFFD' | '\uC0000' .. '\uCFFFD' | '\uD0000' .. '\uDFFFD' | '\uE1000' .. '\uEFFFD'
  ;

PN_CHARS_U
  : PN_CHARS_BASE | '_'
  ;

PN_CHARS
  : PN_CHARS_U | '-' | [0-9] | '\u00B7' | [\u0300-\u036F] | [\u203F-\u2040] | '?' | '='
  ;

// extends PN_LOCAL to allow curly brackets, and force at least one (opening) curly bracket
PN_LOCAL_EXT
  : '{' RIGHT_PART_TAIL_EXT + | RIGHT_PART_FIRST_CHAR RIGHT_PART_TAIL_EXT_MAND
  ;

// extends PN_LOCAL in the original grammar to allow '/' and '#'
PN_LOCAL
  : RIGHT_PART_FIRST_CHAR RIGHT_PART_TAIL?
  ;

PLX
  : PERCENT | PN_LOCAL_ESC
  ;

PERCENT
  : '%' HEX HEX
  ;

HEX
  : [0-9] | [A-F] | [a-f]
  ;

// RDF-specific: the backslash (first character) is ignored when parsing the IRI
PN_LOCAL_ESC
  : '\\' ('_' | '~' | '.' | '-' | '!' | '$' | '&' | '\'' | '(' | ')' | '*' | '+' | ',' | ';' | '=' | '/' | '?' | '#' | '@' | '%')
  ;

fragment RIGHT_PART_FIRST_CHAR
  : (PN_CHARS_U | ':' | '#' | [0-9] | PLX)
  ;

fragment RIGHT_PART_FIRST_CHAR_EXT
  : (RIGHT_PART_FIRST_CHAR | '{')
  ;

fragment RIGHT_PART_CHAR
  : (PN_CHARS | '.' | ':' | '/' | '#' | ';' | PLX)
  ;

fragment RIGHT_PART_CHAR_EXT
  : (RIGHT_PART_CHAR | '{' | '}')
  ;

fragment RIGHT_PART_END_CHAR
  : (PN_CHARS | ':' | '/'| PLX)
  ;

fragment RIGHT_PART_END_CHAR_EXT
  : (RIGHT_PART_END_CHAR | '}')
  ;

fragment RIGHT_PART_TAIL
  : RIGHT_PART_CHAR* RIGHT_PART_END_CHAR
  ;

fragment RIGHT_PART_TAIL_EXT
  : RIGHT_PART_CHAR_EXT* RIGHT_PART_END_CHAR_EXT
  ;

fragment RIGHT_PART_TAIL_EXT_MAND
  : RIGHT_PART_CHAR_EXT* '{' RIGHT_PART_CHAR_EXT* RIGHT_PART_END_CHAR_EXT
  ;

fragment IRIREF_INNER_CHAR
  :  (PN_CHARS | '"' | '.' | ':' | '/' | '\\' | '#' | '@' | '%' | '&' | ';' | UCHAR)
  ;

fragment IRIREF_INNER_CHAR_EXT
  :  (IRIREF_INNER_CHAR | '{' | '}')
  ;

fragment VARIABLE_CHAR
  : (PN_CHARS | '"' | '.' | ':' | '/' | '\\' | '#' | '%' | '&' | '$' | UCHAR)
  ;
