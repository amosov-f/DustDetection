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

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * User: amosov-f
 * Date: 09.01.15
 * Time: 2:45
 */
public class Histogram {
    public <T extends Comparable<T>> Histogram(@NotNull final List<Star> stars, @NotNull final StarCounter<T> counter) {
        final Map<T, Integer> counts = counter.count(stars);
        final int count = IntStream.of(Ints.toArray(counts.values())).sum();

        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (final T type : counts.keySet()) {
            dataset.addValue(1.0 * counts.get(type) / count, "pizza", type);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Гистограмма",
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
                return counts.get(type).toString();
            }
        });
        renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));
        renderer.setBaseItemLabelsVisible(true);

        chart.getCategoryPlot().setRenderer(renderer);

        final NumberAxis rangeAxis = (NumberAxis) chart.getCategoryPlot().getRangeAxis();
        rangeAxis.setNumberFormatOverride(new DecimalFormat("#%"));
        rangeAxis.setRange(0.0, 1.0);

        final ChartFrame frame = new ChartFrame("Гистограмма", chart);
        frame.pack();
        frame.setVisible(true);
    }
}
