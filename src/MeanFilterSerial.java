// Msimamaisi Mwandla
// MWNMSI001
// 16/08/2022

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public  class MeanFilterSerial
{
    public static void main(String[] args) {

        long start = System.currentTimeMillis();

        ///////////////////////////////////////EDIT PATHS//////////////////////////////////////////////////////
        String unfilteredFilepath = "/home/m/mwnmsi001/Assignment1/pics/"+args[0];   //INPUT path
        String filteredFilepath = "/home/m/mwnmsi001/Assignment1/pics/"+args[1]; //OUTPUT path
        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        File imageFile = new File(unfilteredFilepath); // Image file of unfiltered image


        // ----------- Create Buffered image ----------------- //
        try {
            BufferedImage image = ImageIO.read(imageFile); // Buffered image
            System.out.println("File reading successful");

            int imageWidth = image.getWidth(), imageHeight = image.getHeight(); // Get image width and height
            int windowSize = Integer.parseInt(args[2]); // Size of filtering window

            ArrayList<int[]> unfilteredPixelChunks = makePixelArray(image, imageWidth, imageHeight, windowSize); // Stores unfiltered pixels
            ArrayList<int[]> filteredPixelChunks = filter(unfilteredPixelChunks); // Stores filtered pixels

            File outputFile = new File(filteredFilepath); // Image file of filtered image
            writeImage(image, filteredPixelChunks, imageWidth, imageHeight, windowSize, outputFile, 0);

            System.out.println("\nImage size: "+Integer.toString(imageWidth)+"x"+Integer.toString(imageHeight));
            System.out.println("Filter window size: "+Integer.toString(windowSize)+"x"+Integer.toString(windowSize));
            long executionTime = System.currentTimeMillis() - start;
            System.out.println("Serial Mean Filter execution time: "+Long.toString(executionTime)+" milliseconds");

        } catch (IOException e) {
            System.out.println("File reading failed");
        }
    }


    public static ArrayList<int[]> makePixelArray(BufferedImage image, int imageWidth, int imageHeight, int windowSize)
    {
        ArrayList<int[]> pixelChunks = new ArrayList<int[]>();

        for(int i=0; i<imageHeight; i+=windowSize)
        {
            for(int j=0; j<imageWidth; j+=windowSize)
            {
                int currentWidthPosition = j, currentHeightPosition = i;
                if( ((imageWidth - currentWidthPosition) > windowSize)  &&  ((imageHeight - currentHeightPosition) > windowSize) )
                {
                    int width = windowSize, height = windowSize;
                    pixelChunks.add(image.getRGB(j, i, width, height, null, 0, width));
                }
            }
        }
        return pixelChunks;
    }


    public static ArrayList<int[]> filter(ArrayList<int[]> unfilteredArray)
    {
        System.out.println("processing......");
        ArrayList<int[]> filteredArray = new ArrayList<int[]>(); // Empty array to store filtered pixels

        for(int i=0; i<unfilteredArray.size(); i++)
        {
            // store sum values for pixel windows
            int redPixelSum = 0, greenPixelSum = 0, bluePixelSum = 0;

            for(int j=0; j<unfilteredArray.get(0).length; j++)
            {
                 redPixelSum += (unfilteredArray.get(i)[j] >> 16) & 0xFF;
                 greenPixelSum += (unfilteredArray.get(i)[j] >> 8) & 0xFF;
                 bluePixelSum += (unfilteredArray.get(i)[j]) & 0xFF;
            }
            // store mean values for pixel windows
            int redPixelMean = redPixelSum/unfilteredArray.get(i).length;
            int greenPixelMean = greenPixelSum/unfilteredArray.get(i).length;
            int bluePixelMean = bluePixelSum/unfilteredArray.get(i).length;

            int[] filteredWindow = new int[unfilteredArray.get(i).length];

            for(int j=0; j<filteredWindow.length; j++)
            {
                filteredWindow[j] = (redPixelMean << 16) | (greenPixelMean << 8) | (bluePixelMean);
            }
            filteredArray.add(filteredWindow);
        }
        return  filteredArray;
    }


    public static void writeImage(BufferedImage image, ArrayList<int[]> filteredArray, int imageWidth, int imageHeight, int windowSize, File outputImageFile, int windowPosition)
    {
        for(int i=0; i<imageHeight; i+=windowSize)
        {
            for(int j=0; j<imageWidth; j+=windowSize)
            {
                int currentWidthPosition = j, currentHeightPosition = i;
                if( ((imageWidth - currentWidthPosition) > windowSize)  &&  ((imageHeight - currentHeightPosition) > windowSize) )
                {
                    int width = windowSize, height = windowSize;
                    int[] filteredWindow = filteredArray.get(windowPosition);
                    image.setRGB(j, i, width, height,filteredWindow , 0, width);
                    windowPosition++;
                }
            }
        }
        try {
            ImageIO.write(image, "jpg", outputImageFile);
            System.out.println("File writing successful");
        } catch (IOException e) {
            System.out.println("File writing failed");
        }
    }
}