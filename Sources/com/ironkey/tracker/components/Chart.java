package com.ironkey.tracker.components;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import er.plot.ERPChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.AbstractDataset;


public class Chart extends ERPChart {
	private static final long serialVersionUID = 1L;
    protected PlotOrientation _orientation;
	protected DefaultCategoryDataset aDataset;
	//protected JFreeChart chart;
	public static final NSArray<String> SUPPORTED_TYPES = new NSArray<String>(new String[]{
            "BarChart", "StackedBarChart", "BarChart3D", "StackedBarChart3D", "AreaChart", 
            "StackedAreaChart", "LineChart", "WaterfallChart"
    });
	
	/*
	public boolean synchronizeVariablesWithBindings() {
		return true;
	}
	*/

	public void awake() {
	 //   System.out.println("Chart.awake()");
	}
	
	/*
	public JFreeChart chart() {
	    System.out.println("Chart.chart()");
		chart = (JFreeChart)valueForBinding("chart");
		return chart;
	}
	
	public void setChart(JFreeChart pChart) {
	    System.out.println("Chart.setChart()");	
		
		chart = pChart;
	}
	*/

    public Chart(WOContext context) {
        super(context);
    }
		
	protected NSArray<String> supportedTypes() {
        return SUPPORTED_TYPES;
    }

	protected JFreeChart createChart() {
		return chart();
		//return (JFreeChart)valueForBinding("chart");
	}
	
   protected AbstractDataset createDataset() {
        return (AbstractDataset)dataset();
	}
	
	public PlotOrientation orientation() {
        if(_orientation == null) {
            _orientation = ("horizontal".equals(stringValueForBinding("orientation", "vertical")) ? 
                    PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL);
        }
        return _orientation;
    }

}
