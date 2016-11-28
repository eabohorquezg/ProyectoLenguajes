
class Varargs {
    
  private static void displayBooleans(boolean... bool) {
    System.out.print("Number of arguments: " + bool.length + ", Contents: ");
 
    for (boolean b : bool)
      System.out.print("[" + b + "]");
  }
  private static void displayBooleans(boolean bool1, boolean bool2) {
    System.out.println("Overloaded method invoked"); 
  }
  public static void main(String[] args) {
    displayBooleans(true, false);
  }
  
}