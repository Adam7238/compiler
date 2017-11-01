package CE305_assignment2;

/**
 * Created by arkins on 09/02/2017.
 * This is the main method for instantiating the
 * parser and calling the parsing methods.
 * IO exceptions are catered for in the main method.
 */
public class RunParser {

    private static ParseTreeNode parser;

    public static void main(String[] args) {
        System.out.println("Adam 1401853");

        do{
            try {
                System.out.println("Please type an expression without spaces: ");
                parser = new MyParser().parse();
            }catch (ParserException e){
                System.out.println(e.getMessage());
            }
            System.out.println("Would you like to enter another expression? y/n");
        }while(!new MyParser().getLine().equals("n"));
    }
}
