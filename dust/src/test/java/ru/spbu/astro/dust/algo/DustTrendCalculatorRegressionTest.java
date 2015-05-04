package ru.spbu.astro.dust.algo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.dust.DustStars;
import ru.spbu.astro.util.TextUtils;
import ru.spbu.astro.util.Value;

import java.io.File;
import java.io.IOException;

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
        assertEquals("", new DustTrendCalculator(DustStars.ALL, N_SIDE).toString(Value.POS_TWO_SIGMA));
    }
}