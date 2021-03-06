/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.util.HashMap;
import java.util.Map;

 
public final class CreditCard{
    public final int number;    
    File file = new File("path");
    
    public CreditCard(int number) {
        this.number = number;
    }

public static int getAbsAdd(int x, int y) {
    assert x != Integer.MIN_VALUE;
    assert y != Integer.MIN_VALUE;
    int absX = Math.abs(x);
    int absY = Math.abs(y);
    assert (absX <= Integer.MAX_VALUE - absY);
    return absX + absY;
} 

public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof CreditCard)) {
      return false;
    }
    CreditCard cc = (CreditCard)o;
    return cc.number == number;
}
 
public static void main(String[] args) {
    Map<CreditCard, String> m = new HashMap<CreditCard, String>();
    m.put(new CreditCard(100), "4111111111111111");
    System.out.println(m.get(new CreditCard(100))); 
    System.out.println(getAbsAdd(Integer.MIN_VALUE, -19));       
    String microchip = "AXB";
    microchip.replace('A', 'C');    
    //file.delete();   
    microchip.concat("DHD");             
    }

    double amountMoney;
    String brand;

}