package ru.spbu.astro.core.graph;

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
import ru.spbu.astro.core.Star;
import ru.spbu.astro.core.hist.StarCounter;
import ru.spbu.astro.core.StarFilter;
import ru.spbu.astro.util.Value;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class OutlierHistogram {
    @NotNull
    private final List<Star> outliers;

    public <T extends Comparable<T>> OutlierHistogram(@NotNull final List<Star> stars, @NotNull final Predicate<Star> outlierFilter, @NotNull final StarCounter<T> counter) {
        outliers = new StarFilter(stars).filter(outlierFilter).getStars();

        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        final Map<T, Integer> outlierCounts = counter.count(outliers);
        final int outlierCount = IntStream.of(Ints.toArray(outlierCounts.values())).sum();
        final Map<T, Integer> counts = counter.count(stars);
        final int count = IntStream.of(Ints.toArray(counts.values())).sum();

        for (final T type : counts.keySet()) {
            if (outlierCounts.get(type) == null) {
                outlierCounts.put(type, 0);
            }
            dataset.addValue(1.0 * outlierCounts.get(type) / counts.get(type), "pizza", type);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                String.format(
                        "%s\nОтрицательное покраснение у %d%% (%d/%d)",
                        counter.getName(), 100 * outlierCount / count, outlierCount, count),
                counter.getName(),
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
            public String generateRowLabel(CategoryDataset categoryDataset, int i) {
                return null;
            }

            @Nullable
            @Override
            public String generateColumnLabel(CategoryDataset categoryDataset, int i) {
                return null;
            }

            @NotNull
            @Override
            public String generateLabel(CategoryDataset categoryDataset, int i, int i2) {
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

        final ChartFrame frame = new ChartFrame("Звезды с отрицательным покраснением", chart);
        frame.pack();
        frame.setVisible(true);
    }

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
