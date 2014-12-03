package jnu.mindsharing.chainengine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import jnu.mindsharing.common.HList;
import jnu.mindsharing.common.Hana;

public class MappingGraphDrawer
{
	final String outputPath = "graph.jpg";
	final int X=1024;
	final int Y=1024;
	
	private BufferedImage imgBuffer;
	private Graphics2D g2d;
	
	public MappingGraphDrawer()
	{
		imgBuffer = new BufferedImage(X, Y, BufferedImage.TYPE_INT_RGB);
		g2d = imgBuffer.createGraphics();
		g2d.setBackground(Color.WHITE);
		g2d.clearRect(0, 0, X, Y);
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(2));
	}
	
	public void drawXYcoordinates()
	{
		g2d.setColor(Color.BLACK);
		g2d.drawLine(0, Y/2, X, Y/2);
		g2d.drawLine(X/2, 0, X/2, Y);
	}
	
	public void drawLegend()
	{
		drawText("MindSharing Emotional Word Mapping Graph", 15, 15);
		drawText("감정+", X-60, Y/2 - 20);
		drawText("감정-", 20, Y/2 - 20);
		drawText("인과적+", X/2 + 20, 20);
		drawText("인과적-", X/2 + 20, Y-20);
	}
	
	public void drawText(String msg, int x, int y)
	{
		g2d.setColor(Color.BLACK);
		g2d.setFont(new Font("Droid Sans Fallback", Font.PLAIN, 14));
		g2d.drawString(msg, x, y);
	}
	
	public void writeImage()
	{
		drawLegend();
		try
		{
			ImageIO.write(imgBuffer, "JPEG", new File(outputPath));
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void drawEmotionalWords(HList wl)
	{
		for (Hana wi: wl)
		{
			// TODO:혹시 (0, 0)은 그리지 말아야할까?
			double[] projectile = wi.getProjectiles();
			int translated_x=0, translated_y=0;
			translated_x = X/2 + (int)(projectile[0] * X/4);
			translated_y = Y/2 + -(int)(projectile[0] * Y/4);
			drawText(wi.toString(), translated_x, translated_y);
		}
	}
}
