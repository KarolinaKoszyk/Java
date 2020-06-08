import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateChart extends JFrame {
    private Map<String, String> currencyNames;
    private JComboBox<String> cbxCurrencyChoose;
    private Currency chosenCurrency;
    private ChartPanel currentChart;
    private ScrollPane tablePanel;

    public CreateChart(Map<String, String> currencyNames){
        this.currencyNames = currencyNames;
        try {
            this.chosenCurrency = JsonParsing.getCurrencyDetails("USD",
                    LocalDate.now().minusMonths(2), LocalDate.now());
        } catch (IOException e) {
            e.printStackTrace();
        }

        initUI();
    }

    private void initUI(){
        List<String> cbxObjectList = new ArrayList<>();
        for(String name : currencyNames.keySet()){
            cbxObjectList.add(name);
        }
        cbxObjectList.sort(String::compareTo);
        cbxCurrencyChoose = new JComboBox(cbxObjectList.toArray());
        cbxCurrencyChoose.setSelectedItem("USD");
        cbxCurrencyChoose.addActionListener((ActionEvent e) ->{
            try {
                chosenCurrency = JsonParsing.getCurrencyDetails((String)cbxCurrencyChoose.getSelectedItem(),
                        LocalDate.now().minusMonths(2), LocalDate.now());
                JFreeChart chart = createChart();
                getContentPane().remove(currentChart);

                loadChart(chart);

                loadTable();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        getContentPane().add(cbxCurrencyChoose, BorderLayout.NORTH);

        JFreeChart chart = createChart();
        loadChart(chart);

        loadTable();

        setTitle("API NBP");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private XYDataset prepareDataset(){
        Map<LocalDate, Double> ratings = chosenCurrency.getRates();
        XYSeries rat = new XYSeries(chosenCurrency.getName());
        for(LocalDate date : ratings.keySet()){
            rat.add(date.toEpochDay(), ratings.get(date).doubleValue());
        }
        var dataset = new XYSeriesCollection();
        dataset.addSeries(rat);
        return dataset;
    }

    private JFreeChart createChart(){
        JFreeChart chart = ChartFactory.createXYLineChart(
                chosenCurrency.getName(),
                "Day number",
                "Value in PLN",
                prepareDataset(),
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();


        var renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesStroke(0, new BasicStroke(0.5f));

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.white);

        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.BLACK);
        double minVale = chosenCurrency.getMin();
        double maxVale = chosenCurrency.getMax();
        double diff = maxVale - minVale;
        plot.getRangeAxis().setRange(chosenCurrency.getMin() - diff/10, chosenCurrency.getMax() + diff/10);

        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.BLACK);

        chart.setTitle(new TextTitle(chosenCurrency.getName() + " ("+chosenCurrency.getCode()+")",
                        new Font("Serif", java.awt.Font.BOLD, 18)
                )
        );

        return chart;
    }

    public void loadChart(JFreeChart chart){
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(Color.white);
        add(chartPanel);
        currentChart = chartPanel;
        pack();
    }

    private void loadTable(){
        if(tablePanel != null){
            getContentPane().remove(tablePanel);
        }
        ScrollPane dashboardPanel = new ScrollPane();

        String[] columnNames = {"Category", "Value"};
        String[][] tableData = chosenCurrency.getTableData();

        JTable table = new JTable(tableData, columnNames);
        table.setEnabled(false);
        dashboardPanel.add(table);
        getContentPane().add(dashboardPanel, BorderLayout.SOUTH);
        this.tablePanel = dashboardPanel;
        pack();
    }
}
