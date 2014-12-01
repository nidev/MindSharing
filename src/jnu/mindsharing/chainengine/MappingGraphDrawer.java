package jnu.mindsharing.chainengine;

import org.jfree.chart.JFreeChart;

public class MappingGraphDrawer
{
	final String outputPath = "./graph/graph.jpg";
	
	public MappingGraphDrawer()
	{
		Sense ss = new Sense();
		ss.genearteNewdexMap();
		
		//JFreeChart x = new JFreeChart(plot);
	}
}
