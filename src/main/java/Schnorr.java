import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Schnorr {
    BigInteger q, p, h, a, v;


    // a - klucz prywatny
    // p, q, h - udostepniane parametry
    // v - klucz publiczny
    MessageDigest dig;
    int keyLength = 513;
    int qLength = 141;
    Random rnd = new Random();
    public Schnorr() {
        try {
            dig = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void generateKey() {
        q = BigInteger.probablePrime(qLength, new Random());
        BigInteger remainder;
        do {
            p = BigInteger.probablePrime(keyLength, new Random());
            remainder = p.subtract(BigInteger.ONE).remainder(q);
            p = p.subtract(remainder);
        } while (!p.isProbablePrime(2));

        h = new BigInteger(keyLength - 1, rnd);
        h = h.modPow(p.subtract(BigInteger.ONE).divide(q), p);
        do {
            a = new BigInteger(keyLength - 1, rnd);

        } while (a.equals(BigInteger.ONE));
        v = h.modPow(a, p).modInverse(p);
    }

    public BigInteger[] podpisz(byte[] msg) {
        BigInteger[] out = new BigInteger[2];
        BigInteger r = new BigInteger(qLength - 1, rnd);
        BigInteger x = h.modPow(r, p);
        byte[] xBytes = x.toByteArray();
        byte[] concat = new byte[msg.length + xBytes.length];
        System.arraycopy(msg, 0, concat, 0, msg.length);
        System.arraycopy(xBytes, 0, concat, msg.length, xBytes.length);
        dig.update(concat);
        out[0] = new BigInteger(1, dig.digest());
        out[1] = r.add(a.multiply(out[0])).mod(q);
        return out;
    }

    public boolean weryfikuj(byte[] msg, BigInteger[] podpis) {
        BigInteger z = h.modPow(podpis[1], p).multiply(v.modPow(podpis[0], p)).mod(p);
        byte[] zBytes = z.toByteArray();
        byte[] concat = new byte[msg.length + zBytes.length];
        System.arraycopy(msg, 0, concat, 0, msg.length);
        System.arraycopy(zBytes, 0, concat, msg.length, zBytes.length);
        dig.update(concat);
        BigInteger hash = new BigInteger(1, dig.digest());
        return hash.equals(podpis[0]);
    }
}
