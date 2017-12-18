package activesupport.string;

import activesupport.number.Number;

public class String {


    public static java.lang.String generateRandomWordOfLength(int numberOfCharacters){
        StringBuilder word = new StringBuilder(numberOfCharacters);

        for(int i = 0; i < numberOfCharacters; i++){
            word.append(generateRandomLetter());
        }

        return word.toString();
    }

    public static char generateRandomLetter(){
        int lowercaseASCIICode = Number.random(97, 122);
        int uppercaseASCIICode = Number.random(65, 90);

        return generateRandomBoolean() ? (char) uppercaseASCIICode : (char) lowercaseASCIICode;
    }

    public static boolean generateRandomBoolean(){
        return (Number.random(0, 1) == 1) ? true : false;
    }
}
