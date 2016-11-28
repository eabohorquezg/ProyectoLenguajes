public class Expr {
    
  public static void main(String[] args) {
    char alpha = 'A';
    int i = 0;
    // Other code. Value of i may change
    boolean trueExp = true; // Some expression that evaluates to true
    System.out.print(trueExp ? alpha : 0); // prints A
    System.out.print(trueExp ? alpha : i); // prints 65    
  }
  
}