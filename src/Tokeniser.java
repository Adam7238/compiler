/*
 * Created on 08/02/2017 by arkins.
 * This class will create a List of tokens from the
 * input String and pass this list to the parser.
 * A List of tokentypes are created using the inner class
 * 'TokenTypes'.
 */


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.regex.*;


class Tokeniser {

    private LinkedList<Token> tokens;
    private LinkedList<TokenTypes> tokentypes;
    private BufferedReader buffer;
    private String line = "";
    private static int PARMATCH;
    private static int tokenCounter;
    private final String operator;
    private final String integer;
    private final String LPAR;
    private final String RPAR;
    private final String startIf;
    private final String startWhile;
    private final String LBRACE;
    private final String RBRACE;
    private final String VARIABLE;
    private final String SEMICOLON;
    private final String COMPARE;
    private final String ASSIGN;
    private final String startElse;

    Tokeniser(){
        tokens = new LinkedList<>();
        tokentypes = new LinkedList<>();
        buffer = new BufferedReader(new InputStreamReader(System.in));
        PARMATCH = 0;
        tokenCounter = 0;
        operator = "[+*/%-]";
        integer = "[0-9]+";
        LPAR = "\\(";
        RPAR = "\\)";
        startIf = "if";
        startWhile = "while";
        LBRACE = "\\{";
        RBRACE = "\\}";
        VARIABLE = "[a-zA-Z]+";
        COMPARE = "[><]{1}";
        SEMICOLON = ";";
        ASSIGN = "=";
        startElse = "else";
        addTokenTypes();
    }

    String getLine(){
        init();
        return(line);
    }

    //The inti() method is repsonsible for getting each line of input
    //from the console.
    void init(){
        try
        { line = buffer.readLine().trim();
        }
        catch(Exception e)
        { System.out.println("Unexpected error in input");
            System.exit(1);
        }
    }

    //Add to list of TokenTypes compiling Regex pattern into
    //Pattern Object used for matching.
    private void add (String regex, int token){
        tokentypes.add(new TokenTypes(Pattern.compile("^("+regex+")"), token));
    }

    //Create list of TokenTypes
    private void addTokenTypes(){
        add(integer, 1); //Integer Number
        add(operator, 2); //Operator
        add(LPAR, 4); //Left parentheses
        add(RPAR, 5); //Right parentheses
        add(startIf, 6); //keyword 'if'
        add(startWhile, 7); //keyword 'while'
        add(LBRACE, 8); //Left Brace
        add(RBRACE, 9); //Right Brace
        add(startElse, 15); //keyword 'else'
        add(VARIABLE, 10); //variable
        add(COMPARE, 11); //Compare booleans
        add(SEMICOLON, 12); //statement end ';'
        add(ASSIGN, 14); //Single equals '='
    }

    //Check input for valid characters and throw exceptions
    //where required pointing out the position of the invalid
    //character.
    private void checkValid(){
        StringBuilder pointer = new StringBuilder();
        for(int i = 0; i < line.length(); i++){
            String index = Character.toString(line.charAt(i));
            pointer.append(" ");
            if(!(index.matches(integer) || index.matches(operator) || index.matches(LPAR) || index.matches(RPAR) ||
                    index.matches(LBRACE) || index.matches(RBRACE) || index.matches(COMPARE) || index.matches(SEMICOLON) ||
                    index.matches(Character.toString(startIf.charAt(i))) || index.matches(Character.toString(startWhile.charAt(i))) ||
                    index.matches(Character.toString(VARIABLE.charAt(i))))){
                pointer.insert(i,"^");
                throw new ParserException("Invalid character at index: "+i+"\n"+line+"\n"+pointer);
            }
        }
    }

    /*
    The tokeniseString() method will use the Pattern match method to
    attempt match the first item of the input string to each tokentype
    in turn. After the match a new Token of the required type is created
    and placed in the List; the front item of the String is removed.
    Parenthesis balance is checked throughout and exceptions thrown as soon
    as errors are encountered.
    If there is no match for the first item then an exception is thrown.
     */
    void tokeniseString(){
        String str = line.trim();
        tokens.clear();
        //checkValid();
        while(!str.equals("")){
            boolean matched = false;
            for (TokenTypes type : tokentypes){
                Matcher match = type.regex.matcher(str);
                if(match.find()){
                        matched = true;
                        String s = match.group().trim();
                        str = match.replaceFirst("").trim();
                        tokens.add(new Token(type.tokenCode, s, tokenCounter));
                        System.out.println("Token made: " + type.tokenCode + " Value: " + s);
                        tokenCounter++;
                        if (type.tokenCode == 4) PARMATCH++;
                        if (type.tokenCode == 5) PARMATCH--;
                        if (PARMATCH < 0) throw new ParserException("Too many ')' at token: " + tokenCounter);
                        break;
                }
            }
            if(!matched)throw new ParserException("Unexpected character in input at token: ");
        }
        if(PARMATCH != 0) throw new ParserException("Mismatched Parenthesis. Too many '('");
    }

    LinkedList<Token> getTokens() {
        return tokens;
    }

    /*
    This class creates the TokenTypes that are used by the Tokeniser class
    to match the subStrings of the input String against the correct Pattern Objects.
     */
    private class TokenTypes {
        final Pattern regex;
        final int tokenCode;

        TokenTypes(Pattern regex, int tokenCode){
            this.regex = regex;
            this.tokenCode = tokenCode;
        }

    }
}
