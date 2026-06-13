package generators;

import org.apache.commons.lang3.RandomStringUtils;
import java.util.Random;


public class RandomData {
    private RandomData(){};

    public static long getRandomAmount(long min, long max) {
        return min + new Random().nextLong(max - min + 1);
    }

}
