package activesupport.bool;

import activesupport.number.Int;

class Bool{
    public static boolean random(){
        return (Int.random(0, 1) == 1) ? true : false;
    }
}