import java.io.*;
import java.io.Serializable;

class QuadtreeNode implements Serializable{
        int[] color;
        QuadtreeNode NW, NE, SW, SE;
        int width;
        int height;

        QuadtreeNode() {
            this.color = null;
            this.NW = this.NE = this.SW = this.SE = null;
        }
    }

class CompressedQuadtree {
    public static boolean isHomogeneous(int[][][] pixels, int xStart, int yStart, int width, int height, int tolerance) {
        int[] averageColor = averageColor(pixels, xStart, yStart, width, height);

        for (int x = xStart; x < xStart + width; x++) {
            for (int y = yStart - 1; y > yStart - height; y--) {
                int redDiff = Math.abs(pixels[x][y][0] - averageColor[0]);
                int greenDiff = Math.abs(pixels[x][y][1] - averageColor[1]);
                int blueDiff = Math.abs(pixels[x][y][2] - averageColor[2]);

                if (redDiff > tolerance || greenDiff > tolerance || blueDiff > tolerance) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int[] averageColor(int[][][] pixels, int xStart, int yStart, int width, int height) {
        long red = 0;
        long green = 0;
        long blue = 0;

        int pixelCount = width*height;

        for (int x = xStart; x < xStart + width; x++) {
            for (int y = yStart - 1; y > yStart - height; y--) {
                red += pixels[x][y][0];
                green += pixels[x][y][1];
                blue += pixels[x][y][2];
            }
        }

        int avgRed = (int) (red / pixelCount);
        int avgGreen = (int) (green / pixelCount);
        int avgBlue = (int) (blue / pixelCount);
        
        return new int[] { avgRed, avgGreen, avgBlue};
    }

    public static QuadtreeNode buildQuadtree(int[][][] pixels, int xStart, int yStart, int width, int height, int threshold, int tolerance) {
        QuadtreeNode node = new QuadtreeNode();

        if(isHomogeneous(pixels, xStart, yStart, width, height, tolerance) || (width * height <= threshold)) {
            node.color = averageColor(pixels, xStart, yStart, width, height);
            return node;
        }

        int midX = width / 2;
        int midY = height / 2;

        node.NW = buildQuadtree(pixels, xStart, yStart, midX, midY, threshold, tolerance);
        node.NE = buildQuadtree(pixels, xStart + midX, yStart, midX, midY, threshold, tolerance);
        node.SW = buildQuadtree(pixels, xStart, yStart - midY, midX, midY, threshold, tolerance);
        node.SE = buildQuadtree(pixels, xStart + midX, yStart - midY, midX, midY, threshold, tolerance);

        return node;
    }
    
}

public class Utility {

    public void fillImage(QuadtreeNode node, int[][][] image, int xStart, int yStart, int width, int height) {
        if (node.color != null) {
            for (int x = xStart; x < xStart + width; x++) {
                for (int y = yStart -1; y > yStart - height; y--) {
                    image[x][y] = node.color;
                }
            }
        } else {
            int midX = width / 2;
            int midY = height / 2;
            fillImage(node.NW, image, xStart, yStart, midX, midY);
            fillImage(node.NE, image, xStart + midX, yStart, midX, midY);
            fillImage(node.SW, image, xStart, yStart - midY, midX, midY);
            fillImage(node.SE, image, xStart + midX, yStart - midY, midX, midY);
        }
    }
    

    public void Compress(int[][][] pixels, String outputFileName) throws IOException {
        QuadtreeNode root = CompressedQuadtree.buildQuadtree(pixels, 0, pixels[0].length,pixels.length, pixels[0].length, 900, 15);
        root.width = pixels.length;
        root.height = pixels[0].length;

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFileName))) { 
            oos.writeObject(root); 
        }
    }

    public int[][][] Decompress(String inputFileName) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(inputFileName))) {
            Object object = ois.readObject();
    
            if (!(object instanceof QuadtreeNode)) {
                throw new IOException("Invalid object type in the input file");
            }

            QuadtreeNode root = (QuadtreeNode) object;
            int width = root.width;
            int height = root.height;

            int[][][] decompressedImage = new int[width][height][3];
            fillImage(root, decompressedImage, 0, decompressedImage[0].length, width, height);
            return decompressedImage;
        }
    }

}