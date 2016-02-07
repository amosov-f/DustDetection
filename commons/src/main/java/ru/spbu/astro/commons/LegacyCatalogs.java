package ru.spbu.astro.commons;

/**
 * User: amosov-f
 * Date: 07.02.16
 * Time: 16:16
 */
public enum LegacyCatalogs {
    ;

    static final Catalog HIPPARCOS = Catalog.read("hipparcos", Catalog.class.getResourceAsStream("/catalog/hipparcos.txt"));
    static final Catalog HIPNEWCAT = Catalogs.innerJoin("hipnewcat", Catalog.read("hipnewcat", Catalog.class.getResourceAsStream("/catalog/hipnewcat.txt")), HIPPARCOS);
}
