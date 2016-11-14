
package clases;

import java.io.File;
import java.io.FileInputStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;


public class Main {
    
    public static void main(String [] args) throws Exception{
        
		System.setIn(new FileInputStream(new File("input.txt")));
		ANTLRInputStream input = new ANTLRInputStream(System.in);
		Java8Lexer lexer = new Java8Lexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		Java8Parser parser = new Java8Parser(tokens);
		ParseTree tree = parser.compilationUnit();
		MyVisitor<Object> loader = new MyVisitor<Object>();
		loader.visit(tree);
    }
    
}
