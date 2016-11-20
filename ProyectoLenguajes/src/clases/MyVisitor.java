
package clases;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyVisitor<T> extends Java8BaseVisitor<T>{
    
    private static ArrayList<String> methodNames = new ArrayList<String>();
    private static HashMap<Integer, String> rules = new HashMap<>();
    private static ArrayList<String> fileTypeVariables = new ArrayList<String>();    
    private static ArrayList<String> stringTypeVariables = new ArrayList<String>();    
    private static PrintWriter reportFile = null;
    private static String failureInformation = "";
    private static int line=0,column=0;
    
    static{
        rules.put(1, "MET09-J Classes that define an equals() method must also define a hashCode() method");
        rules.put(2, "MET01-J Never use assertions to validate method arguments");
        rules.put(3, "OBJ01-J Limit accessibility of fields");
        rules.put(4, "FIO02-J Detect and handle file-related errors");
        rules.put(5, "EXP00-J Do not ignore values returned by methods");
        //add rules
    }
    
    static List<String> fileBooleanMethods = Arrays.asList(
        "canExecute","canRead","canWrite","equals","exists","isAbsolute","isDirectory","isFile","isHidden",                
        "createNewFile", "delete", "mkdir", "mkdirs", "renameTo", "setExecutable", "setLastModified",
        "setReadOnly", "setReadable", "setWritable");          
    
    static List<String> stringMethods = Arrays.asList(
        "chars","concat","endsWith","intern","replace","replaceAll","replaceFirst","split","startsWith", 
        "subSequence","toLowerCase","toUpperCase","trim");
    
    public static void generateFaultReport(){        
        try {
            reportFile = new PrintWriter(new FileWriter("SecurityFailures.txt"));            
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
            line = ctx.EOF().getSymbol().getLine();
            column = ctx.EOF().getSymbol().getCharPositionInLine()-1;
            securityFailure(line,column,rules.get(1));             
        }    
    }
   
    @Override 
    public T visitAssertStatement(Java8Parser.AssertStatementContext ctx) {         
        if( ctx.getChild(0).getText().contains("assert") ){
            line = ctx.start.getLine();
            column = ctx.start.getCharPositionInLine();
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
        String type = ctx.unannType().getText();
        String id = ctx.variableDeclaratorList().variableDeclarator().get(0).variableDeclaratorId().Identifier().getText();        
        if( type.equals("File") ){
            fileTypeVariables.add(id);
        }else if( type.equals("String") ){
            stringTypeVariables.add(id);
        }
        if( !ctx.getChild(0).getText().equals("private") ){
            line = ctx.start.getLine();
            column = ctx.start.getCharPositionInLine();
            securityFailure(line, column, rules.get(3));            
        }
        return visitChildren(ctx); 
    }    
    
    @Override 
    public T visitMethodInvocation(Java8Parser.MethodInvocationContext ctx) {                 
        for (int i = 0; i < fileTypeVariables.size(); i++) {
            if( fileTypeVariables.get(i).equals(ctx.getChild(0).getText()) ){
                if( fileBooleanMethods.contains(ctx.getChild(2).getText()) ){
                    //fileTypeVariables.remove(i);
                    line = ctx.start.getLine();
                    column = ctx.start.getCharPositionInLine();
                    securityFailure(line, column, rules.get(4));                    
                }
            }               
        }        
        for (int i = 0; i < stringTypeVariables.size(); i++) {
            if( stringTypeVariables.get(i).equals(ctx.getChild(0).getText()) ){
                if( stringMethods.contains(ctx.getChild(2).getText()) ){
                    //stringTypeVariables.remove(i);
                    line = ctx.start.getLine();
                    column = ctx.start.getCharPositionInLine();
                    securityFailure(line, column, rules.get(5));                    
                }
            }               
        }        
        return visitChildren(ctx); 
    }    
    
    @Override 
    public T visitLocalVariableDeclaration(Java8Parser.LocalVariableDeclarationContext ctx) { 
        String type = ctx.unannType().getText();
        String id = ctx.variableDeclaratorList().variableDeclarator().get(0).variableDeclaratorId().Identifier().getText();        
        if( type.equals("File") ){
            fileTypeVariables.add(id);
        }else if( type.equals("String") ){
            stringTypeVariables.add(id);
        }        
        return visitChildren(ctx); 
    }              
         
}

