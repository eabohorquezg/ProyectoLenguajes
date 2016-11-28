
package Model;

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
    private static ArrayList<String> methodParameters = new ArrayList<>();
    private static HashMap<Integer, String> rules = new HashMap<>();
    private static ArrayList<String> fileTypeVariables = new ArrayList<String>();    
    private static ArrayList<String> stringTypeVariables = new ArrayList<String>();    
    private static HashMap<String, String> variables = new HashMap<>();
    private static PrintWriter reportFile = null;
    private static String failureInformation = "";
    private static int line=0,column=0;
    
    static{
        rules.put(1, "MET09-J Classes that define an equals() method must also define a hashCode() method");
        rules.put(2, "MET01-J Never use assertions to validate method arguments");
        rules.put(3, "OBJ01-J Limit accessibility of fields");
        rules.put(4, "FIO02-J Detect and handle file-related errors");
        rules.put(5, "EXP00-J Do not ignore values returned by methods");
        rules.put(6, "IDS00-J Prevent SQL injection");
        rules.put(7, "DCL57-J Avoid ambiguous overloading of variable arity methods");                
        rules.put(8, "EXP53-J Use parentheses for precedence of operation for ternary conditional");                                
        rules.put(9, "EXP55-J Use the same type for the second and third operands in conditional expressions");                        
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
            if( failureInformation.equals("") ){
                reportFile.println("No se presentan fallas de seguridad en el codigo. \n");
            }else{
                reportFile.println("Security Failures: \n");                        
                reportFile.println(failureInformation);
            }
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
        cleanVariables();
        return null;
    }

    public void cleanVariables(){
        failureInformation = "";
        methodNames.clear();
        methodParameters.clear();
        fileTypeVariables.clear();
        stringTypeVariables.clear();
        variables.clear();
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
        methodParameters.add(ctx.formalParameterList()==null ? "":ctx.formalParameterList().getText());
        for (int i = 0; i < methodParameters.size(); i++) {
            if( methodParameters.get(i).contains("...") ){
                for (int j = 0; j < methodNames.size(); j++) {
                    if( i!=j && methodNames.get(i).equals(methodNames.get(j)) ){
                        String argslist = methodParameters.get(j);
                        String l[] = argslist.split(",");
                        String type = methodParameters.get(i).substring(0,methodParameters.get(i).indexOf("..."));                                           
                        int cont=0;
                        for (String st : l) {
                            if( st.contains(type) ){
                                cont++;                                
                            }
                        }
                        if( cont == l.length && !ctx.Identifier().getText().equals("main") ){
                            line = ctx.start.getLine();
                            column = ctx.start.getCharPositionInLine();
                            securityFailure(line, column, rules.get(7));
                        }                                                
                    }
                }
            }
        }                
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
     
    public static boolean isNumeric(String cadena){
	try {
            Integer.parseInt(cadena);
            return true;
	} catch (NumberFormatException nfe){
            return false;
	}
    }        
    
    public void evaluateTernaryConditionalOperator(Java8Parser.MethodInvocationContext ctx){
        if( ctx.getText().contains("System.out.print") && ctx.getText().contains("?") ){
            String exprleft = ctx.getText().substring(ctx.getText().indexOf("(")+1,ctx.getText().indexOf("?"));
            if( exprleft.contains("+") && !exprleft.contains("(") ){
                line = ctx.Identifier().getSymbol().getLine();
                column = ctx.Identifier().getSymbol().getCharPositionInLine()+8;
                securityFailure(line, column, rules.get(8));                    
            }
            String id = ctx.getText().substring(ctx.getText().indexOf("?")+1,ctx.getText().indexOf(":")).trim();
            String secondexpr = ctx.getText().substring(ctx.getText().indexOf(":")+1,ctx.getText().indexOf(")")).trim();                                                
            if( variables.keySet().contains(id) ){
                if( variables.get(id).equals("char") && !secondexpr.contains("char") && !isNumeric(secondexpr) ){
                    if( !variables.get(secondexpr).equals("char") ){
                        line = ctx.Identifier().getSymbol().getLine();
                        column = ctx.Identifier().getSymbol().getCharPositionInLine();
                        securityFailure(line, column, rules.get(9));
                    }        
                }                        
            }
        }     
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
        evaluateTernaryConditionalOperator(ctx);                                       
        return visitChildren(ctx);                 
    }    
    
    public void preventSQLInjection(Java8Parser.LocalVariableDeclarationContext ctx){
        if( ctx.variableDeclaratorList().variableDeclarator().get(0).variableInitializer()!=null ){
            String statement = ctx.variableDeclaratorList().variableDeclarator().get(0).variableInitializer().getText();
            if( statement.contains("executeQuery") ){
                String query = statement.substring(statement.indexOf("(")+1,statement.indexOf(")")-1).trim();
                if( !query.contains("?") ){
                    line = ctx.variableDeclaratorList().variableDeclarator().get(0).variableInitializer().getStart().getLine();
                    column = ctx.variableDeclaratorList().variableDeclarator().get(0).variableInitializer().getStart().getCharPositionInLine();
                    securityFailure(line, column, rules.get(6));                    
                }
            }      
        }        
    }
    
    @Override 
    public T visitLocalVariableDeclaration(Java8Parser.LocalVariableDeclarationContext ctx) { 
        String type = ctx.unannType().getText();
        String id = ctx.variableDeclaratorList().variableDeclarator().get(0).variableDeclaratorId().Identifier().getText();                
        variables.put(id,type);  
        preventSQLInjection(ctx);
        if( type.equals("File") ){
            fileTypeVariables.add(id);
        }else if( type.equals("String") ){
            stringTypeVariables.add(id);
        }                        
        return visitChildren(ctx);  
    }
    
    @Override 
    public T visitAssignment(Java8Parser.AssignmentContext ctx) {
        if( ctx.getText().contains("?") ){
            String exprleft = ctx.getText().substring(ctx.getText().indexOf("=")+1,ctx.getText().indexOf("?"));
            if( exprleft.contains("+") && !exprleft.contains("(") ){
                line = ctx.start.getLine();
                column = ctx.start.getCharPositionInLine();
                securityFailure(line, column, rules.get(8));                    
            }                                
        }                
        return visitChildren(ctx); 
    }
    
}



