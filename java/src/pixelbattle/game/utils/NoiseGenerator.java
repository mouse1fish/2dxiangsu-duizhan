package pixelbattle.game.utils;

public class NoiseGenerator {
    private long seed;
    private int[] perm;

    public NoiseGenerator(long seed) {
        this.seed = seed;
        this.perm = new int[512];
        int[] p = new int[256];
        java.util.Random r = new java.util.Random(seed);
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }
        for (int i = 255; i > 0; i--) {
            int j = r.nextInt(i + 1);
            int tmp = p[i];
            p[i] = p[j];
            p[j] = tmp;
        }
        for (int i = 0; i < 512; i++) {
            perm[i] = p[i & 255];
        }
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x) {
        return (hash & 1) == 0 ? x : -x;
    }

    private static double grad(int hash, double x, double y) {
        int h = hash & 3;
        double u = h < 2 ? x : y;
        double v = h < 2 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    public double noise(double x) {
        int xi = (int)Math.floor(x) & 255;
        double xf = x - Math.floor(x);
        double u = fade(xf);
        return lerp(u, grad(perm[xi], xf), grad(perm[xi + 1], xf - 1));
    }

    public double noise(double x, double y) {
        int xi = (int)Math.floor(x) & 255;
        int yi = (int)Math.floor(y) & 255;
        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);
        double u = fade(xf);
        double v = fade(yf);

        int aa = perm[perm[xi] + yi];
        int ab = perm[perm[xi] + yi + 1];
        int ba = perm[perm[xi + 1] + yi];
        int bb = perm[perm[xi + 1] + yi + 1];

        return lerp(v,
            lerp(u, grad(aa, xf, yf), grad(ba, xf - 1, yf)),
            lerp(u, grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1))
        );
    }

    public double octaveNoise(double x, double y, int octaves, double persistence) {
        double total = 0;
        double frequency = 1;
        double amplitude = 1;
        double maxValue = 0;
        for (int i = 0; i < octaves; i++) {
            total += noise(x * frequency, y * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2;
        }
        return total / maxValue;
    }

    public double getValue(double x, double y) {
        return octaveNoise(x * 0.01, y * 0.01, 4, 0.5);
    }
}
