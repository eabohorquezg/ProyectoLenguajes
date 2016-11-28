
import java.io.File;


public class Example2 {
    
    public static void main(String[] args) {
        File file = new File(args[0]);
        file.delete();
    }
    
}
