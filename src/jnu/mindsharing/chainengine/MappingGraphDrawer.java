package jnu.mindsharing.chainengine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import jnu.mindsharing.common.HList;
import jnu.mindsharing.common.Hana;

public class MappingGraphDrawer
{
	final String outputPath = "graph.jpg";
	final int X=800;
	final int Y=800;
	
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
		drawXYcoordinates();
		
		int point;
		String[] labels = {"-5", "-4", "-3", "-2", "-1", "0", "1", "2", "3", "4", "5"};
		drawText("MindSharing Emotional Word Mapping Graph", 15, 15);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		
		drawText("생성시각  " + dateFormat.format(new Date()), 15, 40);
		drawText("감정+", X-60, Y/2 - 20);
		drawText("감정-", 20, Y/2 - 20);
		drawText("인과적+", X/2 + 20, 20);
		drawText("인과적-", X/2 + 20, Y-20);
		
		// 눈금 그리기
		// XXX: 아직 스케일링은 구현되어있지 않아서 고정 스케일로 그려야할듯
		// X눈금
		for (point = 0; point < labels.length; point++)
		{
			g2d.drawLine(80*point, Y/2 + 10, 80*point, Y/2 - 10);
			drawText(labels[point], 4 + 80*point, Y/2 + 14);
		}
		// Y눈금
		for (point = 0; point < labels.length; point++)
		{
			g2d.drawLine(X/2 - 10, Y - 80*point, X/2 + 10, Y - 80*point);
			drawText(labels[point], X/2 + 8, Y - 80*point);
		}
		
	}
	
	public void drawText(String msg, int x, int y)
	{
		g2d.setColor(Color.BLACK);
		g2d.setFont(new Font("Gulim", Font.PLAIN, 12));
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
			double[] projectile = wi.getProjectiles();
			int translated_x=0, translated_y=0;
			translated_x = X/2 + (int)(projectile[0] * X/10);
			translated_y = Y/2 + -(int)(projectile[1] * Y/10);
			
			g2d.drawRect(translated_x, translated_y, 2, 2); // 점찍기
			drawText(wi.toString(), translated_x+5, translated_y+5);
		}
	}
}
