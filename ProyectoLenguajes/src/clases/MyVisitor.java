
package clases;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyVisitor<T> extends Java8BaseVisitor<T>{
    
    private static ArrayList<String> methodNames = new ArrayList<String>();
    private static HashMap<Integer, String> rules = new HashMap<>();
    private static PrintWriter reportFile = null;
    
    static{
        rules.put(1, "Classes that define an equals() method must also define a hashCode() method");
        //add rules
    }
    
    public static void createFile(){
        try {
            reportFile = new PrintWriter(new FileWriter("SecurityFailures.txt",true));
            reportFile.println("Security Failures: \n\n");            
        } catch (IOException ex) {
            Logger.getLogger(MyVisitor.class.getName()).log(Level.SEVERE, null, ex);
        }         
    }
    
    @Override 
    public T visitCompilationUnit(Java8Parser.CompilationUnitContext ctx) {                                        
        createFile();
        visitTypeDeclaration(ctx.typeDeclaration(0));
        endOfClass(ctx);
        return null;
    }

    public static void securityFailure( int line, int col, String info ){        
        reportFile.println(String.format("<%d:%d> %s", line, col, info));       
        reportFile.close();        
    }
    
    public void endOfClass( Java8Parser.CompilationUnitContext ctx ){
        if( methodNames.contains("equals") && !methodNames.contains("hashCode") ){ 
            int line = ctx.EOF().getSymbol().getLine();
            int column = ctx.EOF().getSymbol().getCharPositionInLine();
            securityFailure(line,column,rules.get(1));             
        }    
    }
    
    @Override 
    public T visitMethodDeclarator(Java8Parser.MethodDeclaratorContext ctx) {                 
        methodNames.add(ctx.Identifier().getText());            
        return visitChildren(ctx); 
    }
            
}
