package ru.spbu.astro.dust.algo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.spect.LuminosityClass;
import ru.spbu.astro.commons.spect.TempClass;
import ru.spbu.astro.commons.spect.SpectTable;
import ru.spbu.astro.dust.DustStars;
import ru.spbu.astro.util.Filter;
import ru.spbu.astro.util.MathTools;
import ru.spbu.astro.util.TextUtils;
import ru.spbu.astro.util.Value;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("MagicNumber")
public class DustTrendCalculatorRegressionTest {
    private static final int N_SIDE = 18;

    public static void main(@NotNull final String[] args) throws IOException {
        FileUtils.writeStringToFile(new File("dust/src/test/resources/table-k.txt"), actual());
    }

    @Test
    public void regression() throws IOException {
        assertEquals(IOUtils.toString(getClass().getResourceAsStream("/table-k.txt")), actual());
    }

    @NotNull
    private static String actual() {
        return new DustTrendCalculator(DustStars.ALL, N_SIDE).toString();
    }

    @Test
    @Ignore
    public void printTable() {
        System.out.println(TextUtils.tex(new DustTrendCalculator(DustStars.ALL, N_SIDE).toTable(null), 3, 1));
    }

    @Test
    @Ignore
    public void showAcceptPositive() {
        assertEquals("", new DustTrendCalculator(DustStars.DR30, N_SIDE).toString(Value.POS_TWO_SIGMA));
    }

    @Test
    @Ignore
    public void testName() throws Exception {
        final DustTrendCalculator calculator = new DustTrendCalculator(DustStars.DR30, N_SIDE);
        final Star[] allStars = StarFilter.of(DustStars.DR30)
                .r(250)
                .apply(Filter.by("small gradient", s -> Math.abs(calculator.getSlope(s.getDir()).val()) <= 0.0001))
                .stars();
        final PrintWriter fout = new PrintWriter("pizza.txt");
        for (final LuminosityClass lumin : LuminosityClass.MAIN) {
            for (int code = 5; code < 70; code++) {
                final TempClass spect = TempClass.valueOf(code);
                final Star[] stars = StarFilter.of(allStars).temp(spect).lumin(lumin).stars();
                fout.print(lumin + "\t" + spect + "\t" + stars.length + "\t" + SpectTable.getInstance().getBV(spect, lumin).val());
                if (stars.length != 0) {
                    final Value bvObs = MathTools.average(Arrays.stream(stars).mapToDouble(s -> s.getBVColor().val()).toArray());
                    fout.print("\t" + bvObs);
                }
                fout.println();
            }
        }
        fout.close();
    }
}