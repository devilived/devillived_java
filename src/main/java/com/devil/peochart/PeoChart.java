// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 

package com.devil.peochart;

import java.awt.Dimension;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DefaultKeyedValues2DDataset;

import com.devil.utils.DebugUtil;

public final class PeoChart{
	private static final long serialVersionUID = 1L;
	private double[] data1;
	private double[] data2;
	private String[] sideTitle;
	
	private String title;
	private String domainAxisLabel;
	private String rangeAxisLabel;
	public PeoChart(double[] data1, double[] data2){
		this.data1=data1;
		this.data2=data2;
	}
	public void setTitles(String title, String domainAxisLabel, String rangeAxisLabel){
		this.title=title;
		this.domainAxisLabel=domainAxisLabel;
		this.rangeAxisLabel=rangeAxisLabel;
	}
	public void setSideTitle(String[] sideTitle){
		this.sideTitle=sideTitle;
	}

	public JFreeChart createChart() {
		if(data1==null||data2==null||sideTitle==null||
				!(data1.length==data2.length&&data2.length==sideTitle.length)	){
			throw new RuntimeException("数据有误");
		}
		JFreeChart jfreechart = ChartFactory.createStackedBarChart(title, domainAxisLabel,rangeAxisLabel, 
																	createDataset(),
																	PlotOrientation.HORIZONTAL, true, true, false);
		return jfreechart;
	}

	private CategoryDataset createDataset() {
		DefaultKeyedValues2DDataset defaultkeyedvalues2ddataset = new DefaultKeyedValues2DDataset();
		for(int i=0;i<data1.length;i++){
			defaultkeyedvalues2ddataset.addValue(-1*data1[i], "M", sideTitle[i]);
			defaultkeyedvalues2ddataset.addValue(-1*data2[i], "F", sideTitle[i]);
		}
		return defaultkeyedvalues2ddataset;
	}

	private JPanel createDemoPanel() {
		JFreeChart jfreechart = createChart();
		ChartPanel chartpanel = new ChartPanel(jfreechart);
		chartpanel.setPreferredSize(new Dimension(500, 270));
		return chartpanel;
	}

	public static void main(String args[]) {
		String title="Population Chart";
		String domainAxisLabel="Age Group";
		String rangeAxisLabel="Population (millions)";
//		PeoChart pc=new PeoChart(data1, data2)
//		DebugUtil.UITest(new PeoChart());
		
	}
}