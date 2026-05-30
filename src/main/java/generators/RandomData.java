package generators;

import org.apache.commons.lang3.RandomStringUtils;
import java.util.Random;


public class RandomData {
    private RandomData(){};

    public static String getUserName(){
        return RandomStringUtils.randomAlphabetic(10).toLowerCase();
    }
    public static String getUserPassword(){
        return RandomStringUtils.randomNumeric(4) +
                RandomStringUtils.randomAlphabetic(3).toUpperCase() +
                RandomStringUtils.randomAlphabetic(4).toLowerCase() + "%*";
    }

    public static long getRandomAmount(long min, long max) {
        return min + new Random().nextLong(max - min + 1);
    }

}
