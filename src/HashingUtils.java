import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class HashingUtils {
    final private static int a_ASCII = 97;
    final private static int z_ASCII = 122;
    final private Random rand;

    public HashingUtils() {
        rand = new Random(System.currentTimeMillis()); // Using current time as the random seed
    }
    
    public static long mod(long x, long m) {
        long res = x % m;

        return (res < 0) ? res + m : res;
    }

    public static int mod(int x, int m) {
        int res = x % m;

        return (res < 0) ? res + m : res;
    }
    
    /***
     * Generates random prime in the range [lower, higher].
     * @param lower - a lower bound for the returned value
     * @param higher - an upper bound for the returned value
     * @return a random prime, between lower and higher
     */    
    public long genPrime(long lower, long higher) {
    	long suspectPrime = genLong(lower,higher);
        while (((suspectPrime & 1) == 0) || !runMillerRabinTest(suspectPrime, 50)){
            suspectPrime = genLong(lower,higher);
        }
        return suspectPrime;
    }
    
    /***
     * Generates random long values until getting a value in the range [lower, higher].
     * @param lower - a lower bound for the returned value
     * @param higher - an upper bound for the returned value
     * @return a random long, between lower and higher
     */
    public long genLong(long lower, long higher) {
    	long value = rand.nextLong();
    	while (value < lower | value > higher)
    		value = rand.nextLong();
    	return value;
    }
    
    public Integer[] genUniqueIntegers(int numOfItemsToGen) {
        return Stream.generate(() -> rand.ints(0, Integer.MAX_VALUE))
                .flatMap(IntStream::boxed)
                .distinct()
                .limit(numOfItemsToGen)
                .toArray(Integer[]::new);
    }

    public Long[] genUniqueLong(int numOfItemsToGen) {
        return Stream.generate(() -> rand.longs(0, Long.MAX_VALUE))
                     .flatMap(LongStream::boxed)
                     .distinct()
                     .limit(numOfItemsToGen)
                     .toArray(Long[]::new);
    }

    public List<String> genUniqueStrings(int numOfItemsToGen, int stringMinLength, int stringMaxLength) {
       return rand.ints(stringMinLength, stringMaxLength)
                   .limit(numOfItemsToGen)
                   .mapToObj(length -> rand.ints(a_ASCII,
                                                 z_ASCII
                                                 + 1)
                                           .limit(length)
                                           .collect(StringBuilder::new,
                                                    StringBuilder::appendCodePoint,
                                                    StringBuilder::append)
                                           .toString())
                   .distinct()
                   .collect(Collectors.toList());
    }

    private static Pair<Integer, Long> calculateEvenDivisorSplit(long num) {
        int s = 0;
        while ((num & 1) == 0) {
            num >>>= 1;
            ++s;
        }

        return new Pair<>(s, num);
    }

    /***
     * Evaluates the power of x at y, done at modulo mod in an efficient matter,
     * using the characteristics of modulo: (x * y) mod n = ((x mod n) * (y mod n)) mod n
     * @param x - The basis
     * @param y - The power
     * @param mod - The intended modulo of the value
     * @return (x ^ y) % mod
     */
    private static long multiplyMod(long a, long b, long mod) {
        final BigInteger aBig = BigInteger.valueOf(a);
        final BigInteger bBig = BigInteger.valueOf(b);
        final BigInteger multiplyRes = aBig.multiply(bBig);
        final BigInteger modBig = BigInteger.valueOf(mod);

        return multiplyRes.mod(modBig).longValue();
    }

    private static long modPow(long a, long b, long mod) {
        final BigInteger aBig = BigInteger.valueOf(a);
        final BigInteger bBig = BigInteger.valueOf(b);
        final BigInteger modBig = BigInteger.valueOf(mod);
        final BigInteger res = aBig.modPow(bBig, modBig);

        return res.longValue();
    }

    
    /**
     * An implementation of the Rabin-Miller probabilistic primality test as defined in the following link:
     * <a href="https://en.wikipedia.org/wiki/Miller%E2%80%93Rabin_primality_test#Miller%E2%80%93Rabin_test">...</a>
     * This process requires theta of (rounds * log(suspect) ^ 3)
     * @param suspect - The number suspected of being prime, assuming suspect isn't even.
     * @param rounds - The number of rounds to run the test. A good default value is 50.
     * @return True if suspect is probably prime with a false positive p of 4^(-rounds)
     */
    private boolean runMillerRabinTest(long suspect, int rounds) {
        Pair<Integer, Long> split = calculateEvenDivisorSplit(suspect - 1);
        final int s = split.first();
        final long d = split.second();

        long y = 1;

        for (int i = 0; i < rounds; ++i) {
            final long a = genLong(2L, suspect - 1);
            long x = modPow(a, d, suspect);

            for (int j = 0; j < s; ++j) {
                y = multiplyMod(x, x, suspect);

                if (y == 1 && x != 1 && x != suspect - 1) {
                    return false;
                }

                x = y;
            }
        }

        return (y == 1);
    }
}
