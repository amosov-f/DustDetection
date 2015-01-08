package ru.spbu.astro.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TabPaster {

    final List<List<String>> table = new ArrayList<>();

    public TabPaster(final String path) throws FileNotFoundException {
        final Scanner fin = new Scanner(new FileInputStream(path));
        //

        while (fin.hasNextLine()) {
            final String s = fin.nextLine();
            table.add(new ArrayList<>());
            for (final String w : s.trim().split("\\s+")) {
                table.get(table.size() - 1).add(w);
            }
        }

    }

    public void write(final String path) throws FileNotFoundException {
        final PrintWriter fout = new PrintWriter(new FileOutputStream(path));

        for (final List<String> row : table) {
            for (final String w : row) {
                fout.print(w + "\t");
            }
            fout.println();
        }

        fout.flush();
    }

    public static void main(final String[] args) throws FileNotFoundException {
        {
            final TabPaster tabPaster = new TabPaster("documents/related-articles/tables/I.txt");
            tabPaster.write("documents/related-articles/tables/I.txt");
        }
        {
            final TabPaster tabPaster = new TabPaster("documents/related-articles/tables/III.txt");
            tabPaster.write("documents/related-articles/tables/III.txt");
        }
        {
            final TabPaster tabPaster = new TabPaster("documents/related-articles/tables/V.txt");
            tabPaster.write("documents/related-articles/tables/V.txt");
        }
    }

}
