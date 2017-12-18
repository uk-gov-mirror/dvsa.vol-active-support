package activesupport.string;

import activesupport.bool.Bool;
import activesupport.number.Int;

public class Str {

    public static java.lang.String randomWord(int numberOfCharacters){
        StringBuilder word = new StringBuilder(numberOfCharacters);

        for(int i = 0; i < numberOfCharacters; i++){
            word.append(randomLetter());
        }

        return word.toString();
    }

    public static char randomLetter(){
        int lowercaseASCIICode = Int.random(97, 122);
        int uppercaseASCIICode = Int.random(65, 90);

        return Bool.random() ? (char) uppercaseASCIICode : (char) lowercaseASCIICode;
    }

}
