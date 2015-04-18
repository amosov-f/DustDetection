package ru.spbu.astro.dust.algo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.dust.DustStars;
import ru.spbu.astro.healpix.Healpix;
import ru.spbu.astro.util.Value;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("MagicNumber")
public class DustTrendCalculatorRegressionTest {
    private static final int N_SIDE = 18;

    private static final DustTrendCalculator DUST_TREND_CALCULATOR = new DustTrendCalculator(DustStars.ALL, N_SIDE);

    public static void main(@NotNull final String[] args) throws IOException {
        FileUtils.writeStringToFile(
                new File("dust/src/test/resources/dust-trend-calculator.txt"),
                DUST_TREND_CALCULATOR.toString()
        );
    }

    @Test
    public void regression() throws IOException {
        assertEquals(
                IOUtils.toString(getClass().getResourceAsStream("/dust-trend-calculator.txt")),
                DUST_TREND_CALCULATOR.toString()
        );
    }

    @Test
    @Ignore
    public void showAcceptPositive() {
        assertEquals("", new DustTrendCalculator(DustStars.ALL, N_SIDE).toString(Value.POS_TWO_SIGMA));
    }

    @Test
    @Ignore
    public void showTrustGradient() throws Exception {
        assertEquals("", new DustTrendCalculator(DustStars.ALL, N_SIDE).toString(Value.filterByErr(0.00003)));
    }

    @Test
    @Ignore
    public void showCombiningFilter() throws Exception {
        assertEquals("", new DustTrendCalculator(DustStars.ALL, N_SIDE).toString(Value.POS_TWO_SIGMA.or(Value.filterByErr(0.00003))));
    }


    @Test
    public void testSigma() throws Exception {
        a:
        for (int nSide = 2; nSide < 40; nSide++) {
            final Healpix healpix = new Healpix(nSide);
            double sum = 0;
            for (final Value k : new DustTrendCalculator(DustStars.ALL, nSide).getSlopes()) {
                if (k != null) {
                    sum += k.err();
                } else {
                    break a;
                }
            }
            System.out.println(nSide + " " + sum * healpix.getPixArea() * 1000);
        }
    }
}