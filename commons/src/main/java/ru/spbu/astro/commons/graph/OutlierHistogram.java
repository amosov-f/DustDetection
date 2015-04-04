package ru.spbu.astro.commons.graph;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.TextAnchor;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.hist.StarHist;
import ru.spbu.astro.util.Filter;
import ru.spbu.astro.util.Value;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.stream.IntStream;

public class OutlierHistogram {
    @NotNull
    private final Star[] outliers;
    @NotNull
    private final ChartFrame frame;

    public <T extends Comparable<T>> OutlierHistogram(@NotNull final Star[] stars,
                                                      @NotNull final Filter<Star> outlierFilter,
                                                      @NotNull final StarHist<T, Integer> hist)
    {
        outliers = StarFilter.of(stars).apply(outlierFilter).stars();

        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        final Map<T, Integer> outlierCounts = hist.hist(outliers);
        final int outlierCount = IntStream.of(Ints.toArray(outlierCounts.values())).sum();
        final Map<T, Integer> counts = hist.hist(stars);
        final int count = IntStream.of(Ints.toArray(counts.values())).sum();

        for (final T type : counts.keySet()) {
            if (outlierCounts.get(type) == null) {
                outlierCounts.put(type, 0);
            }
            dataset.addValue(1.0 * outlierCounts.get(type) / counts.get(type), "pizza", type);
        }

        final JFreeChart chart = ChartFactory.createBarChart(
                String.format(
                        "%s\nОтрицательное покраснение у %d%% (%d/%d)",
                        hist.getName(), 100 * outlierCount / count, outlierCount, count),
                hist.getName(),
                "Доля",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        final StackedBarRenderer renderer = new StackedBarRenderer(false);
        renderer.setBaseItemLabelGenerator(new CategoryItemLabelGenerator() {
            @Nullable
            @Override
            public String generateRowLabel(@NotNull final CategoryDataset categoryDataset, final int i) {
                return null;
            }

            @Nullable
            @Override
            public String generateColumnLabel(@NotNull final CategoryDataset categoryDataset, final int i) {
                return null;
            }

            @NotNull
            @Override
            public String generateLabel(@NotNull final CategoryDataset categoryDataset, final int i, final int i2) {
                final T type = Iterables.get(counts.keySet(), i2);
                return outlierCounts.get(type) + "/" + counts.get(type);
            }
        });
        renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));
        renderer.setBaseItemLabelsVisible(true);

        chart.getCategoryPlot().setRenderer(renderer);

        final NumberAxis rangeAxis = (NumberAxis) chart.getCategoryPlot().getRangeAxis();
        rangeAxis.setNumberFormatOverride(new DecimalFormat("#%"));
        rangeAxis.setRange(0.0, 1.0);

        frame = new ChartFrame("Звезды с отрицательным покраснением", chart);
        frame.pack();
    }
    
    public void show() {
        frame.setVisible(true);
    }

    @NotNull
    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder();
        str.append("hip\tspect\text\tsigma_ext\n");
        for (final Star star : outliers) {
            final Value ext = star.getExtinction();
            str.append(String.format(
                    "%d\t%s\t%.2f\t%.2f\n",
                    star.getId(), star.getSpectType().toString(), ext.getValue(), star.getExtinction().getError()
            ));
        }
        return str.toString();
    }
}
