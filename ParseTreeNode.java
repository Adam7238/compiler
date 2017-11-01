package CE305_assignment2;

/*
 *Created by arkins on 08/02/2017
 * This Class will create a new node ready to be passed into the parse tree.
 * The type of the node is indicative of the type of token passed as a constructor
 * parameter.
 * The value field remains an Object to allow for expansion of the parser in future
 * iterations.
 * The node is limited to 2 children to create a binary parse tree and facilitate the
 * implementation of a Abstract Syntax Tree
 */
public class ParseTreeNode {

    /*
    numNode = 1, opNode = 2, Expression = 3, LPAR = 4, RPAR = 5,
    startif = 6, startWhile = 7, LBRACE = 8, RBRACE = 9, VAR = 10,
    COMPARE = 11, SEMICOLON = 12, PRINT = 13, ASSIGN = 14, STARTELSE = 15;
     */
    int type;
    Object value;
    ParseTreeNode lChild, rChild;
    int errorTracker;

     ParseTreeNode(int type, Object value, ParseTreeNode lchild, ParseTreeNode rchild, int errorTracker){
        this.type = type;
        this.value = value;
        this.lChild = lchild;
        this.rChild = rchild;
        this.errorTracker = errorTracker;
    }
}
