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
	final int X=800;
	final int Y=800;
	
	private BufferedImage imgBuffer;
	private Graphics2D g2d;
	
	final int delta = 10; 
	
	
	public MappingGraphDrawer()
	{
		imgBuffer = new BufferedImage(X, Y, BufferedImage.TYPE_INT_RGB);
		g2d = imgBuffer.createGraphics();
		g2d.setBackground(Color.WHITE);
		g2d.clearRect(0, 0, X, Y);
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(1));
	}
	
	public void drawXYcoordinates()
	{
		g2d.setColor(Color.GRAY);
		g2d.drawLine(0, Y/2, X, Y/2);
		g2d.drawLine(X/2, 0, X/2, Y);
	}
	
	public void drawLegend()
	{
		drawXYcoordinates();
		
		int point;
		String[] labels = {"-4", "-3", "-2", "-1", "0", "1", "2", "3", "4"};
		drawText("MindSharing Emotional Word Mapping Graph", 15, 15);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		
		drawText("생성시각  " + dateFormat.format(new Date()), 15, 30);
		drawText("감정+", X-15, Y/2 - 5);
		drawText("감정-", 15, Y/2 - 5);
		drawText("인과적+", X/2 + 5, 15);
		drawText("인과적-", X/2 + 5, Y-15);
		
		// 눈금 그리기
		// XXX: 아직 스케일링은 구현되어있지 않아서 고정 스케일로 그려야할듯
		// X눈금
		for (point = 0; point < labels.length; point++)
		{
			g2d.drawLine(100*point, Y/2 + 10, 100*point, Y/2 - 10);
			drawText(labels[point], 4 + 100*point, Y/2 + 14);
		}
		// Y눈금
		for (point = 0; point < labels.length; point++)
		{
			g2d.drawLine(X/2 - 10, Y - 100*point, X/2 + 10, Y - 100*point);
			drawText(labels[point], X/2 + 8, Y - 100*point);
		}
		
	}
	
	public void drawText(String msg, int x, int y)
	{
		g2d.setColor(Color.BLACK);
		g2d.setFont(new Font("Gulim", Font.PLAIN, 12));
		g2d.drawString(msg, x, y);
	}
	
	public void drawTextWithLine(String msg, int text_x, int text_y, int from_x, int from_y)
	{
		g2d.setColor(Color.MAGENTA);
		g2d.drawLine(text_x, text_y, from_x, from_y);
		g2d.setColor(Color.BLACK);
		drawText(msg, text_x, text_y);
	}
	
	public void writeImage(String outputFile)
	{
		drawLegend();
		try
		{
			ImageIO.write(imgBuffer, "JPEG", new File(outputFile));
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void drawEmotionalWords(HList wl)
	{
		int zero_vectors = 0;
		int neg_vectors = 0;
		int pos_vectors = 0;
		int mixed_vectors = 0;
		
		for (Hana wi: wl)
		{
			if (wi.areZeroProbs())
			{
				zero_vectors++;
				continue;
			}
			else if (wi.arePositiveProbs())
				pos_vectors++;
			else if (wi.areNegativeProbs())
				neg_vectors++;
			else
				mixed_vectors++;
			
			double[] projectile = wi.getProjectiles();
			int translated_x=0, translated_y=0;
			translated_x = X/2 + (int)(projectile[0] * X/8);
			translated_y = Y/2 + -(int)(projectile[1] * Y/8);
			
			g2d.fillOval(translated_x-2, translated_y-2, 4, 4); // 점찍기
			
			if (translated_y > 0)
			{
				drawText(wi.toString(), translated_x, translated_y-5);
			}
			else
			{
				drawText(wi.toString(), translated_x, translated_y + 5);
			}
		}
		
		drawText(String.format("제로 벡터: %d, 긍정: %d, 부정: %d, 혼합: %d", zero_vectors, pos_vectors, neg_vectors, mixed_vectors), 15, 45);
		drawText("그려진 벡터수: " + (wl.size() - zero_vectors), 15, 60);
	}
}
