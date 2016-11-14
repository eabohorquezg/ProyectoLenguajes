
package clases;

public class MyVisitor<T> extends Java8BaseVisitor<T>{
    
    @Override 
    public T visitClassModifier(Java8Parser.ClassModifierContext ctx) {
        System.out.println("Clase de tipo "+ctx.getText());
        return visitChildren(ctx); 
    }
    
}
