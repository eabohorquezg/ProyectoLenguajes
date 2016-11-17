
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
    private static String failureInformation = "";
    
    static{
        rules.put(1, "MET09-J Classes that define an equals() method must also define a hashCode() method");
        rules.put(2, "MET01-J Never use assertions to validate method arguments");
        rules.put(3, "OBJ01-J Limit accessibility of fields");
        //add rules
    }
    
    public static void generateFaultReport(){
        try {
            reportFile = new PrintWriter(new FileWriter("SecurityFailures.txt",true));            
            reportFile.println("Security Failures: \n\n");                        
            reportFile.println(failureInformation);
            reportFile.close();        
        } catch (IOException ex) {
            Logger.getLogger(MyVisitor.class.getName()).log(Level.SEVERE, null, ex);
        }         
    }
    
    @Override 
    public T visitCompilationUnit(Java8Parser.CompilationUnitContext ctx) {                                                
        visitTypeDeclaration(ctx.typeDeclaration(0));//declaracion de clase
        endOfClass(ctx);
        generateFaultReport();
        return null;
    }

    public static void securityFailure( int line, int col, String info ){        
        failureInformation += String.format("<%d:%d> %s \n", line, col, info);      
    }
    
    public void endOfClass( Java8Parser.CompilationUnitContext ctx ){
        if( methodNames.contains("equals") && !methodNames.contains("hashCode") ){ 
            int line = ctx.EOF().getSymbol().getLine();
            int column = ctx.EOF().getSymbol().getCharPositionInLine();
            securityFailure(line,column,rules.get(1));             
        }    
    }
   
    @Override 
    public T visitAssertStatement(Java8Parser.AssertStatementContext ctx) {         
        if( ctx.getChild(0).getText().contains("assert") ){
            int line = ctx.start.getLine();
            int column = ctx.start.getCharPositionInLine();
            securityFailure(line, column, rules.get(2));            
        }              
        return visitChildren(ctx); 
    }
    
    @Override 
    public T visitMethodDeclarator(Java8Parser.MethodDeclaratorContext ctx) {        
        methodNames.add(ctx.Identifier().getText());                              
        return visitChildren(ctx); 
    }
    
    @Override 
    public T visitFieldDeclaration(Java8Parser.FieldDeclarationContext ctx) { 
        if( !ctx.getChild(0).getText().equals("private") ){
            int line = ctx.start.getLine();
            int column = ctx.start.getCharPositionInLine();
            securityFailure(line, column, rules.get(3));            
        }
        return visitChildren(ctx); 
    }
    
}
