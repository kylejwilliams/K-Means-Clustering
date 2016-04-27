import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

import javax.swing.*;

@SuppressWarnings("serial")
public class Application extends JPanel {
	static Random rand;
	static int maxDataValue = 100;
	static int minDataValue = 1;
	int padding = 30; // to prevent drawing on edges of screen
	static int numDataPoints = 20; 
	Color clusterOneColor = Color.RED;
	Color clusterTwoColor = Color.BLUE;
	Color clusterThreeColor = Color.GREEN;
	Color clusterFourColor = Color.ORANGE;
	Stroke graphLine = new BasicStroke(3f);
	int dataPointSize = 12;
	int numHatchesY = 10;
	int numHatchesX = 10;
	static ArrayList<Point> data = new ArrayList<Point>();
	static ArrayList<ArrayList<Point>> clusters = new ArrayList<ArrayList<Point>>();

	public Application() {
		data = generateData(getSeed());
		clusters = bisectingKmeans(data, 3);
		
		double sum = 0;
		for (int i = 0; i < clusters.size(); i++) {
			sum += intraClusterDistance(clusters.get(i));
			System.out.println("intra-cluster distance for cluster " + (i+1) + ": " + intraClusterDistance(clusters.get(i)));
		}
		System.out.println("sum of intra-cluster distances: " + sum);
		//System.out.println("inter-cluster distance: " + interClusterDistance(clusters));
		System.out.println("minimum distance between clusters: " + minDistanceBetweenClusters(clusters));
		System.out.println("maximum distance between clusters: " + maxDistanceBetweenClusters(clusters));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		// create x and y axes
		g2.drawLine(padding, getHeight() - padding, padding, padding);
		g2.drawLine(padding, getHeight() - padding, getWidth() - padding, getHeight() - padding);

		// create hatch marks for y axis.
		for (int i = 0; i < numHatchesY; i++) {
			int x0 = padding;
			int x1 = dataPointSize + padding;
			int y0 = getHeight() - (((i + 1) * (getHeight() - padding * 2)) / numHatchesY + padding);
			int y1 = y0;
			g2.drawLine(x0, y0, x1, y1);
		}

		// and for x axis
		for (int i = 0; i < numHatchesX; i++) {
			int x0 = (i + 1) * (getWidth() - padding * 2) / numHatchesX + padding;
			int x1 = x0;
			int y0 = getHeight() - padding;
			int y1 = y0 - dataPointSize;
			g2.drawLine(x0, y0, x1, y1);
		}

		g2.setStroke(graphLine);
		
		for (int i = 0; i < clusters.size(); i++) {
			if (i == 0) g2.setColor(clusterOneColor);
			else if (i == 1) g2.setColor(clusterTwoColor);
			else if (i == 2) g2.setColor(clusterThreeColor);
			else if (i == 3) g2.setColor(clusterFourColor);
			
			for (int j = 0; j < clusters.get(i).size(); j++) {
				int x = clusters.get(i).get(j).x * (getWidth() - 2 * padding) / (maxDataValue - minDataValue) + padding;
				int y = clusters.get(i).get(j).y * (getHeight() - 2 * padding) / (maxDataValue - minDataValue) + padding;

				int ovalW = dataPointSize;
				int ovalH = dataPointSize;
				g2.fillOval(x, y, ovalW, ovalH);
				if (j == 0) g2.fillRect(x, y, ovalW, ovalH);
			}
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(800, 800);
	}

	private static void createAndShowGui() {
		Application mainPanel = new Application();

		JFrame frame = new JFrame("Bisecting K-means");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(mainPanel);
		frame.pack();
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}

	public static int getSeed() {
		Scanner scanner = new Scanner(System.in);

		System.out.println("Enter a positive integer seed value");
		System.out.print("> ");
		int seed = scanner.nextInt();

		scanner.close();

		return seed;
	}

	public static ArrayList<ArrayList<Point>> kMeans(ArrayList<Point> data, int K) {
		ArrayList<ArrayList<Point>> clusters = new ArrayList<ArrayList<Point>>();
		ArrayList<Point> dataPoints = new ArrayList<>(data);
		boolean centroidsChanged;
		int index;
		
		// Select K points as the initial centroids
		for (int i = 0; i < K; i++) {
			index = rand.nextInt(dataPoints.size());
			ArrayList<Point> currentCluster = new ArrayList<>();
			currentCluster.add(dataPoints.get(index));
			dataPoints.remove(index);
			clusters.add(currentCluster);
		}
		
		do {
			centroidsChanged = false;
			
			// Assign all points to the closest centroid
			for (int i = 0; i < dataPoints.size(); i++) {
				int smallestDistanceIndex = 0;
				for (int j = 0; j < clusters.size(); j++) {
//					if (euclideanDistance(clusters.get(j).get(0), dataPoints.get(i)) < euclideanDistance(clusters.get(smallestDistanceIndex).get(0), dataPoints.get(i))) {
//						smallestDistanceIndex = j;
//					}
					if (manhattanDistance(clusters.get(j).get(0), dataPoints.get(i)) < manhattanDistance(clusters.get(smallestDistanceIndex).get(0), dataPoints.get(i))) {
						smallestDistanceIndex = j;
					}
				}
				clusters.get(smallestDistanceIndex).add(dataPoints.get(i));
			}
			
			// Recompute the centroid of each cluster
			for (int i = 0; i < clusters.size(); i++) {
				dataPoints.add(clusters.get(i).get(0));
				for (int j = 0; j < clusters.get(i).size(); j++) {
					if (sumSquaredError(clusters.get(i), clusters.get(i).get(j)) <
							sumSquaredError(clusters.get(i), clusters.get(i).get(0))) {
						Collections.swap(clusters.get(i), 0, j);
						centroidsChanged = true;
					}
				}
			}
		} while (centroidsChanged);
		
		return clusters;
	}
	
	public static int sumSquaredError(ArrayList<Point> data, Point p) {
		int SSE = 0;
		
		for (int i = 0; i < data.size(); i++) {
//			SSE += Math.pow(euclideanDistance(p, data.get(i)), 2);
			SSE += Math.pow(manhattanDistance(p, data.get(i)), 2);
		}
		
		return SSE;
	}
	
	public static ArrayList<Point> generateData(int seed) {
		rand = new Random(seed);
		ArrayList<Point> data = new ArrayList<>();
		int x;
		int y;
		
		for (int i = 0; i < numDataPoints; i++) {
			x = rand.nextInt(maxDataValue) + minDataValue;
			y = rand.nextInt(maxDataValue) + minDataValue;
			data.add(new Point(x,y));
		}
		
		return data;
	}
	
	public static ArrayList<ArrayList<Point>> bisectingKmeans(ArrayList<Point> data, int K) {
		// Initialize the list of clusters
		ArrayList<ArrayList<Point>> clusters = new ArrayList<ArrayList<Point>>();
		ArrayList<Point> dataCopy = new ArrayList<>(data);
		clusters.add(dataCopy);
		
		for (int i = 0; i < K; i++) {
			// Pick a cluster to split
			ArrayList<Point> selectedCluster = clusters.get(rand.nextInt(clusters.size()));
			ArrayList<ArrayList<Point>> tempClusters = new ArrayList<ArrayList<Point>>();
			ArrayList<ArrayList<Point>> subClusters = new ArrayList<ArrayList<Point>>();
			
			int currentSSE = 0;
			int lowestSSE = Integer.MAX_VALUE;
			
			for (int j = 0; j < 5; j++) {
				// find two sub-clusters of the selected array
				tempClusters = kMeans(selectedCluster, 2);
				
				currentSSE = sumSquaredError(tempClusters.get(0), tempClusters.get(0).get(0)) + sumSquaredError(tempClusters.get(1), tempClusters.get(1).get(0));
				
				if (currentSSE < lowestSSE) subClusters = tempClusters;
			}
				
			clusters.remove(selectedCluster);
			for (ArrayList<Point> cluster : subClusters) clusters.add(cluster);
		}
		
		return clusters;
	}
	
	public static double euclideanDistance(Point p1, Point p2) {
		return Math.sqrt(Math.pow(Math.abs(p1.x - p2.x), 2) + 
				Math.pow(Math.abs(p1.y - p2.y), 2));
	}
	
	public static int manhattanDistance(Point p1, Point p2) {
		return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
	}
	
	public static double intraClusterDistance(ArrayList<Point> data) {
		double sum = 0;
		
		for (int i = 0; i < data.size(); i++) {
			sum += euclideanDistance(data.get(i), data.get(0));
		}
		
		return sum;
	}
	
	public static double interClusterDistance(ArrayList<ArrayList<Point>> clusters) {
		ArrayList<ArrayList<Point>> tmpClusters = new ArrayList<ArrayList<Point>>(clusters);
		double sum = 0;
		
		while (tmpClusters.size() > 0) {
			for (ArrayList<Point> cluster : tmpClusters) {
				sum += Math.pow(euclideanDistance(cluster.get(0), tmpClusters.get(0).get(0)), 2);
			}
			tmpClusters.remove(0);
		}
		
		return sum;
	}
	
	public static double minDistanceBetweenClusters(ArrayList<ArrayList<Point>> clusters) {
		ArrayList<ArrayList<Point>> tmpClusters = new ArrayList<ArrayList<Point>>(clusters);
		double min = Integer.MAX_VALUE;
		while (tmpClusters.size() > 0) {
			for (ArrayList<Point> cluster : tmpClusters) {
				if (euclideanDistance(cluster.get(0), tmpClusters.get(0).get(0)) < min && 
						euclideanDistance(cluster.get(0), tmpClusters.get(0).get(0)) != 0) {
					min = euclideanDistance(cluster.get(0), tmpClusters.get(0).get(0));
				}
			}
			tmpClusters.remove(0);
		}
		
		return min;
	}
	
	public static double maxDistanceBetweenClusters(ArrayList<ArrayList<Point>> clusters) {
		ArrayList<ArrayList<Point>> tmpClusters = new ArrayList<ArrayList<Point>>(clusters);
		double max = Integer.MIN_VALUE;
		while (tmpClusters.size() > 0) {
			for (ArrayList<Point> cluster : tmpClusters) {
				if (euclideanDistance(cluster.get(0), tmpClusters.get(0).get(0)) > max) {
					max = euclideanDistance(cluster.get(0), tmpClusters.get(0).get(0));
				}
			}
			tmpClusters.remove(0);
		}
		
		return max;
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGui();
			}
		});
	}
}