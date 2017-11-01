package CE305_assignment2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

/**
 * Created by arkins on 09/02/2017.
 * This class contains the methods for parsing the input
 * String that has been tokenised by the Tokeniser class.
 * The results of the method will be a Stack containing a
 * single ParseTreeNode Object with the rest of the tree
 * being accessible through the l/r child assignments.
 * Any Stack that fails to meet this condition will throw an
 * Exception.
 * An output of the post-fix order of the expression is
 * saved to a .txt file for later use.
 * A .gv GraphViz file is also created ready for tree visualisation
 * using the GraphViz library.
 */

class MyParser {

    private Tokeniser tokeniser;
    private Stack<ParseTreeNode> currentState;
    private LinkedList<Token> tokenStream;
    private ArrayList<ParseTreeNode> reduceAttempt;
    private ArrayList<ParseTreeNode> compare;
    private int[][] productionRules;
    private int[] ruleResults;
    private ParseTreeNode lookahead;
    private Token current;
    private boolean hasReduced;
    private ArrayList<String> declaredVariables;

    MyParser(){
        tokeniser = new Tokeniser();
        currentState = new Stack<ParseTreeNode>();
        compare = new ArrayList<>();
        declaredVariables = new ArrayList<>();
        hasReduced = false;
        makeRules();
    }

    //Create the Parse rules used to decide on the reduce action.
    private void makeRules(){
        productionRules =
                new int[][]{
                        {1},         //0 NUM ---EXP
                        {10,14,3},   //1 VAR | ASSIGN | EXP --- ASSIGN
                        {3,2,3},     //2 Exp | OP | EXP --- EXP
                        {4,3,5},     //3 LPAR | EXP | RPAR --- EXP
                        {3,12},      //4 EXP | SEMICOLON --- SEMICOLON
                        {12,12},     //5 SEMICOLON | SEMICOLON --- SEMICOLON
                        {10,12},     //6 VARIABLE | SEMICOLON --- SEMICOLON
                        {14,12},     //7 ASSIGN | SEMICOLON --- SEMICOLON
                        {10,14,10},  //8 VAR | ASSIGN | VAR --- ASSIGN
                        {3,11,3},    //9 EXP | BOOL | EXP --- BOOL
                        {3,11,10},   //10 EXP | BOOL | VAR --- BOOL
                        {10,11,3},   //11 VAR | BOOL | EXP --- BOOL
                        {10,11,10}, //12 VAR | BOOL | VAR --- BOOL
                        {6,4,11,5,8,12,9,15,8,12,9}, //13 STARTIF | LPAR | BOOL | RPAR | LBRACE | SEMICOLON | RBRACE | STARTELSE | LBRACE | SEMICOLON | RBRACE --- STARTELSE
                        {6,4,11,5,8,12,9} //14 STARTIF | LPAR | BOOL | RPAR | LBRACE | SEMICOLON | RBRACE --- STARTIF


        };
        ruleResults = new int[]{3,14,3,3,12,12,12,12,14,11,11,11,11,12,12};
    }


    String getLine(){ return tokeniser.getLine();}

    /*
    * This method will be called from the main method. It will tokenise
    * the input String and then initiate the Stack before passing control
    * to the parserAction() method.
    */
    ParseTreeNode parse(){
        tokeniser.init();
        tokeniser.tokeniseString();
        tokenStream = tokeniser.getTokens();
        try{
            begin();
            parserAction();
        }catch (ParserException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    /*
     * Initiate parsing by assigning lookahead to
     * first token of the token stream.
     */
    private void begin(){
        current = tokenStream.removeFirst();
        lookahead = new ParseTreeNode(current.token, current.value, null, null, current.errorTracker);
    }

    /*
    * This method will decide on hte current action
    * that the parser will take depending on the current
    * state of the stack, token stream, and previously
    * completed actions.
    * If the parse reaches the end of the tokens but does not
    * reach the accept state it will attempt to find the cause
    * of the error by calling the findError() method.
    * If the accept state is reached a .gv file is created and
    * a .txt file of the post order output is created.
    *
    * The Boolean 'hasReduced' allows the parser to reduce repeatedly
    * to ensure the top of the stack has been reduced as far as possible
    * before the next item is shifted onto the stack.
    */
    private void parserAction(){
        while(lookahead != null || currentState.size() > 1 ) {
            if(lookahead == null && !hasReduced){
                //System.out.println("Lookahead: "+lookahead.type+" Value: "+lookahead.value);
                System.out.println("Current State: "+currentState.size());
                findError();
                throw new ParserException("Invalid Expression");
            }
            if (hasReduced) {
                reduce();
            } else {
                shift();
                reduce();
            }
        }
        if(tokenStream.isEmpty() && currentState.size() == 1 && currentState.peek().type == 12){
            System.out.println("Accept state reached");
            System.out.println(postOrder(currentState.peek()));
            dotCreator(currentState.peek());
            writeOutput(postOrder(currentState.pop()));
        }
    }

    //Iterate through the elements of the tree to find two consecutive operators.
    //Throw an exception pointing out he location of the error.
    private void findError(){
        ParseTreeNode node = currentState.pop();
        lookahead = currentState.pop();
        while (currentState.size() != 0){
            if(node.type == 2 && lookahead.type == 2){
                throw new ParserException("Incorrect Operators at position: "+lookahead.errorTracker+" and: "+node.errorTracker);
            }
            else{
                node = lookahead;
                lookahead = currentState.pop();
            }
        }
    }

    /*
    Move topmost token onto the stack.
    Update lookahead.
    */
    private void shift(){
        if(lookahead == null){}
        else {
            currentState.push(lookahead);
            if (tokenStream.isEmpty()) {
                lookahead = null;
            } else {
                current = tokenStream.pop();
                lookahead = new ParseTreeNode(current.token, current.value, null, null, current.errorTracker);
            }
        }
    }

    /*
    If size of stack is equal to length of rule, pop that many items into arraylist.
    Check each item in list against rule, if rule matches reduce items and replace new
    item onto stack.
    Else push items back onto stack & move onto next rule.
    If no rules match at end of reduction shift next token.
    */
    private void reduce(){
        hasReduced = false;
        for(int i = 0; i < productionRules.length; i++){
            boolean ruleMatch = true;
            compare.clear();
            if(currentState.size() >= productionRules[i].length){
                for(int j = 0; j < productionRules[i].length; j++) {
                    compare.add(0,currentState.pop());
                }
                for(int k = 0; k < compare.size(); k++){
                    if(compare.get(k).type != productionRules[i][k]){
                        ruleMatch = false;
                        break;
                    }
                }
                if(ruleMatch){
                    createSubTree(i);
                }
                else{
                    for(ParseTreeNode node: compare){
                        currentState.push(node);
                    }
                }
            }
        }
    }

    /*
    * If a rule is matched this method will create the new ParseTreeNode with
    * according to the rule and assign any children necessary to maintain the
    * tree structure.
    * The lookahead is used to determine operator precedence within the tokenstream.
    * Parenthesis take the highest priority, if the lookahead has a higher priority
    * than the node that has matched the rule the shift(0 method is called to force
    * the parse to move on without reducing.
    */
    private void createSubTree(int i){
        hasReduced = true;
        ParseTreeNode temp;
        switch (i){
            case 0://0 NUM ---EXP
                temp = new ParseTreeNode(ruleResults[i], compare.get(0).value, null, null, compare.get(0).errorTracker);
                currentState.push(temp);
                break;
            case 1://1 VAR | ASSIGN | EXP --- ASSIGN
                if(!declaredVariables.contains((String)compare.get(0).value)){
                    declaredVariables.add((String) compare.get(0).value);
                }
                //ParseTreeNode equals = new ParseTreeNode(14, compare.get(1).value, compare.get(0), compare.get(2), compare.get(1).errorTracker);
                temp = new ParseTreeNode(ruleResults[i], compare.get(1).value, compare.get(2),compare.get(0),compare.get(1).errorTracker);
                currentState.push(temp);
                break;
            case 2://2 Exp | OP | EXP --- EXP
                if(lookahead != null && lookahead.type == 2 && returnPrecedence(String.valueOf(lookahead.value))
                        > returnPrecedence(String.valueOf(compare.get(1).value))){
                    for(ParseTreeNode node: compare){
                        currentState.push(node);
                    }
                    compare.clear();
                    hasReduced = false;
                    shift();
                    break;
                }
                temp = new ParseTreeNode(ruleResults[i], compare.get(1).value, compare.get(0), compare.get(2), compare.get(0).errorTracker);
                currentState.push(temp);
                break;
            case 3://3 LPAR | EXP | RPAR --- EXP
                currentState.push(compare.get(1));
                break;
            case 4://4 EXP | SEMICOLON --- SEMICOLON
                temp = new ParseTreeNode(ruleResults[i], compare.get(1).value, compare.get(0), null, compare.get(1).errorTracker);
                currentState.push(temp);
                break;

            case 5://5 SEMICOLON | SEMICOLON --- SEMICOLON
                temp = new ParseTreeNode(ruleResults[i], compare.get(1).value, compare.get(1).lChild, compare.get(0), compare.get(0).errorTracker);
                currentState.push(temp);
                break;
            case 6://6 VARIABLE | SEMICOLON --- SEMICOLON
                if(declaredVariables.contains((String)compare.get(0).value)){
                    ParseTreeNode print = new ParseTreeNode(13, "@", compare.get(0), null, compare.get(0).errorTracker);
                    temp = new ParseTreeNode(ruleResults[i], compare.get(1).value, print, null, compare.get(1).errorTracker);
                    currentState.push(temp);
                }
                else{
                    declaredVariables.add((String)compare.get(0).value);
                    temp = new ParseTreeNode(ruleResults[i], compare.get(1).value, compare.get(0), null, compare.get(1).errorTracker);
                    currentState.push(temp);
                }
                break;
            case 7://7 ASSIGN | SEMICOLON --- SEMICOLON
                temp = new ParseTreeNode(ruleResults[i], compare.get(1).value, compare.get(0), null, compare.get(1).errorTracker);
                currentState.push(temp);
                break;
            case 8://8 VAR | ASSIGN | VAR --- ASSIGN
                if(!declaredVariables.contains((String)compare.get(0).value)) {
                    declaredVariables.add((String) compare.get(0).value);
                }
                temp = new ParseTreeNode(ruleResults[i], compare.get(1).value, compare.get(0), compare.get(2), compare.get(0).errorTracker);
                currentState.push(temp);
                break;
            case 9://9 EXP | BOOL | EXP --- BOOL
                temp = new ParseTreeNode(ruleResults[i], compare.get(1).value, compare.get(2),compare.get(0), compare.get(1).errorTracker);
                currentState.push(temp);
                break;
            case 10://10 EXP | BOOL | VAR --- BOOL
                temp = new ParseTreeNode(ruleResults[i], compare.get(1).value, compare.get(2),compare.get(0), compare.get(1).errorTracker);
                currentState.push(temp);
                break;
            case 11://11 VAR | BOOL | EXP --- BOOL
                temp = new ParseTreeNode(ruleResults[i], compare.get(1).value, compare.get(2),compare.get(0), compare.get(1).errorTracker);
                currentState.push(temp);
                break;
            case 12://12 VAR | BOOL | VAR --- BOOL
                temp = new ParseTreeNode(ruleResults[i], compare.get(1).value, compare.get(2),compare.get(0), compare.get(1).errorTracker);
                currentState.push(temp);
                break;
            case 13://13 STARTIF | LPAR | BOOL | RPAR | LBRACE | SEMICOLON | RBRACE | STARTELSE | LBRACE | SEMICOLON | RBRACE --- STARTELSE
                ParseTreeNode elseNode = new ParseTreeNode(15, "else", compare.get(5), compare.get(9), compare.get(0).errorTracker);
                ParseTreeNode ifNode = new ParseTreeNode(6, "if", compare.get(2), elseNode, compare.get(2).errorTracker);
                temp = new ParseTreeNode(ruleResults[i], ";", ifNode, null, ifNode.errorTracker);
                currentState.push(temp);
                break;
            case 14://14 STARTIF | LPAR | BOOL | RPAR | LBRACE | SEMICOLON | RBRACE --- STARTIF
                if(lookahead != null && lookahead.type == 15){
                    System.out.println("Rule 14 condition satisfied. Shift() called.");
                    for(ParseTreeNode node: compare){
                        currentState.push(node);
                    }
                    compare.clear();
                    hasReduced = false;
                    shift();
                    break;
                }
                System.out.println("Rule 14 entered. Lookahead: "+lookahead.type);
                ParseTreeNode temp1 = new ParseTreeNode(compare.get(0).type, compare.get(0).value, compare.get(2), compare.get(5), compare.get(0).errorTracker);
                temp = new ParseTreeNode(ruleResults[i], ";", temp1, null, temp1.errorTracker);
                currentState.push(temp);
                break;
        }
    }

    //Return the precedence of each operator.
    private int returnPrecedence(String s){
        switch (s){
            case "*":
            case "/":
                return 2;
            case "+":
            case "-":
                return 1;
            default:
                return  0;
        }
    }

    //Calls private method postOrderWrite() to write out the tree in postorder notation
    private String postOrder(ParseTreeNode node){return postOrderWrite(node);}

    //Recursively call the method travelling down the tree to write out the tree in post Order.
    //Taking care to omit printing out the semicolons so that the file will run properly.
    private String postOrderWrite(ParseTreeNode tr){
        if(tr.type == 12 && tr.rChild != null && tr.rChild.type ==12 ){
           return postOrderWrite(tr.rChild)+postOrderWrite(tr.lChild);
        }
        if(tr.type == 6){
            return postOrderWrite(tr.lChild)+tr.value+" "+postOrderWrite(tr.rChild)+"endif ";
        }
        if(tr.type == 15){
            return postOrderWrite(tr.lChild)+tr.value+" "+postOrderWrite(tr.rChild)+" ";
        }
        if (tr.rChild == null && tr.lChild == null) {
           if (tr.type == 12) {
                return "";
            } else {
                return String.valueOf(tr.value + " ");
            }
        }
        else if (tr.rChild == null) {
            if (tr.type == 12) {
                return postOrderWrite(tr.lChild) + String.valueOf("");
            } else if (tr.type == 13) {
                return String.valueOf(tr.lChild.value + " " + tr.value + " ");
            } else {
                return postOrderWrite(tr.lChild) + String.valueOf(tr.value + " ");
            }
        }
        else {
            if(tr.type == 14){
                return postOrderWrite(tr.lChild) + postOrderWrite(tr.rChild) + "! ";
            }
           else if (tr.type == 12) {
                return postOrderWrite(tr.lChild) + postOrderWrite(tr.rChild) + "";
            } else {
                return postOrderWrite(tr.lChild) + postOrderWrite(tr.rChild) + String.valueOf(tr.value + " ");
            }
        }
    }

    /*
    * Create the .gv file for tree visualisation.
    * Uses a string builder to append the value of each tree node to the
    * file. Gets the value of each tree node by calling the dotForamtter()
    * using the root node as an argument.
     */
    private void dotCreator(ParseTreeNode node){
        String output = dotFormatter(node);
        StringBuilder buildFormat = new StringBuilder();
        buildFormat.append("digraph G { \n");
        buildFormat.append(output);
        buildFormat.append("}");
        try {
            File file = new File("Tree_Visualiser.gv");
            FileWriter writer = new FileWriter(file);
            writer.write(String.valueOf(buildFormat));
            writer.close();
        }catch (IOException e){
            System.out.println("An error occurred whilst saving the .dot file.");
            e.printStackTrace();
        }
    }

    /*
    * Recursively calls the same method by walking through the tree
    * until the base case is reached, formatting the string appropriately
    * so that a vaid .gv file is created.
    * The hashCode() for each node is used to ensure that each node has a unique
    * ID to ensure correct cration of the graph. The letter 'n' is placed in front
    * asa GraphViz does not allow node names to begin with numbers.
     */
    private String dotFormatter(ParseTreeNode node){
        if(node.lChild == null && node.rChild == null){
            return String.valueOf("n"+node.hashCode())+" [label=\"" + node.value + "\"];\n";
        }
        else if (node.rChild == null){
            return String.valueOf("n"+node.hashCode())+" [label=\"" + node.value + "\"];\n"+dotFormatter(node.lChild)+
                    ("n" + node.hashCode()) + " -> " + ("n" + node.lChild.hashCode()) + ";\n";
        }
        else {
            return String.valueOf("n"+node.hashCode())+" [label=\"" + node.value + "\"];\n" +
                    dotFormatter(node.lChild) + ("n" + node.hashCode()) + " -> " + ("n" + node.lChild.hashCode()) + ";\n"+
                    dotFormatter(node.rChild) + ("n" + node.hashCode()) + " -> " + ("n" + node.rChild.hashCode()) + ";\n";
        }
    }

    /*
      Write the result of the post order output to the .txt file.
      All output is placed within a compliled function called 'main'.
      This function is then explicitly called as the last argument.
     */
    private void writeOutput(String s){
        try {
            File file = new File("Post_Order_Output.txt");
            FileWriter writer = new FileWriter(file);
            StringBuilder str = new StringBuilder();
            for (String declaredVariable : declaredVariables) {
                str.append("VARIABLE " + declaredVariable + "\n");
            }
            str.append(": main \n");
            str.append(s+"\n");
            str.append("; \nmain");
            writer.write(String.valueOf(str));
            writer.close();
        }catch (IOException e){
            System.out.println("An error occurred whilst saving the file.");
            e.printStackTrace();
        }
    }
}
