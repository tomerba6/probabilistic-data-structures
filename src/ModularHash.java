import java.util.Random;

public class ModularHash implements HashFactory<Integer> {
    private Random rand;
    private HashingUtils utils;

    public ModularHash() {
        rand = new Random();
        utils = new HashingUtils();
    }

    @Override
    public HashFunctor<Integer> pickHash(int k) {
        return new Functor(k);
    }

    public class Functor implements HashFunctor<Integer> {
        final private int a;
        final private int b;
        final private long p;
        final private int m;

        public Functor(int k){
            if (k < 0 || k > 30) {
                throw new IllegalArgumentException("k must be between 0 and 30. Received: " + k);
            }
            this.a = rand.nextInt(Integer.MAX_VALUE - 1) + 1;
            this.b = rand.nextInt(Integer.MAX_VALUE);
            this.p = utils.genPrime(Integer.MAX_VALUE, Long.MAX_VALUE);
            this.m = 1 << k;
        }

        @Override
        public int hash(Integer key) {
            return (int)HashingUtils.mod(HashingUtils.mod(((long)a * key + b), p), m);
        }

        public int a() {
            return a;
        }

        public int b() {
            return b;
        }

        public long p() {
            return p;
        }

        public int m() {
            return m;
        }
    }
}
