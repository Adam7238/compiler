# Compiler
A small compiler to compile code into Forth instructions

The program currently accepts multiple ‘;’ separated statements that are valid mathematical expressions. 
Such as: 2+2; 3*3; etc. 
There is support for ‘while’ loops within the program structured as: while(a<b){ a= a+5;} 
As well as support for standalone ‘if’ statements: if(a >7){b=3;} 
‘if else’ if(a >7){b=3;}else{b=5;} 
The program supports nested ‘if’ and ‘if else’ statements. 
Error checking is informative and accurate at picking up invalid characters in the input stream, as well as indicating the presence of unmatched braces and parenthesis. However, if the program does not reach the accept state there is limited useful information provided as to the whereabouts of the error. 
The problem lies in having to unpack the sub trees left on the stack and the difficulty in finding the exact point where the error has occurred in what could potentially be a very large tree. An effort has been made to indicate if there is an incorrect sequence of operators. However, if there are other errors that have prevented a complete parse then the error messages are not completely helpful in identifying the source of the error. 
Input is read from a file and the entire contents of the file are parsed completely before the output is generated and the tree visualisation file is created. 
The output generated is stored in a fileof the same name with the suffix ‘_Parsed.txt’ in the correct format. Data to allow the data to be visualised with ‘graphviz’ is generated and placed in a file of the same name with the suffix ‘_Visualiser.gv’. 
Four demo files are provided inside the submission folder:  
‘demo_assign.txt’, ‘demo_if.txt’, ‘nested_condition.txt’ and ‘demo_while.txt’ along with their output files. 
