// Msimamaisi Mwandla
// MWNMSI001
// 16/08/2022

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;


public  class MedianFilterParallel
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

            System.out.println("Processing...");
            MedianMultithreader parallelFilter = new MedianMultithreader(unfilteredPixelChunks, 0, unfilteredPixelChunks.size());
            ForkJoinPool.commonPool().invoke(parallelFilter);
            ArrayList<int[]> filteredPixelChunks = parallelFilter.filtered;


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


    // Loop through image and add pixels to arrayList
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

    // Write output image
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

//Parallelized filtering process
class MedianMultithreader extends RecursiveAction{


    ArrayList<int[]> unfiltered, filtered = new ArrayList<>();
    int startPos, endPos;
    int SEQ_CUTOFF;
    int[] filteredWindow;


    public MedianMultithreader(ArrayList<int[]> unfiltered, int startPos, int endPos){
        this.unfiltered = unfiltered;
        this.startPos = startPos;
        this.endPos = endPos;
        this.SEQ_CUTOFF = (int)(unfiltered.size()*0.6); // Sequential cutoff
    }

    @Override
    protected void compute() {
        int forkSize = endPos - startPos;
        if (forkSize < SEQ_CUTOFF) {
            for(int i=0; i<unfiltered.size(); i++)
            {
                ArrayList<Integer> redPixelArray = new ArrayList<>(), greenPixelArray = new ArrayList<>(), bluePixelArray = new ArrayList<>();

                for(int j=0; j<unfiltered.get(0).length; j++)
                {
                    redPixelArray.add((unfiltered.get(i)[j] >> 16) & 0xFF);
                    greenPixelArray.add((unfiltered.get(i)[j] >> 8) & 0xFF);
                    bluePixelArray.add((unfiltered.get(i)[j]) & 0xFF);
                }
                // Sorting pixel arrays
                Collections.sort(redPixelArray);
                Collections.sort(greenPixelArray);
                Collections.sort(bluePixelArray);

                int redPixelMedian, greenPixelMedian, bluePixelMedian; // store median values
                if(redPixelArray.size() % 2 == 0)
                {
                    redPixelMedian = (redPixelArray.get(redPixelArray.size()/2) + redPixelArray.get((redPixelArray.size()/2)-1))/2;
                    greenPixelMedian = (greenPixelArray.get(greenPixelArray.size()/2) + greenPixelArray.get((greenPixelArray.size()/2)-1))/2;
                    bluePixelMedian = (bluePixelArray.get(bluePixelArray.size()/2) + bluePixelArray.get((bluePixelArray.size()/2)-1))/2;
                }
                else
                {
                    redPixelMedian = redPixelArray.get(Math.round(redPixelArray.size()/2));
                    greenPixelMedian = greenPixelArray.get(Math.round(greenPixelArray.size()/2));
                    bluePixelMedian = bluePixelArray.get(Math.round(bluePixelArray.size()/2));
                }


                int[] filteredWindow = new int[unfiltered.get(i).length]; //filtered window

                for(int j=0; j<filteredWindow.length; j++)
                {
                    filteredWindow[j] = (redPixelMedian << 16) | (greenPixelMedian << 8) | (bluePixelMedian);
                }
                filtered.add(filteredWindow);
            }
            filtered.add(filteredWindow); // add filtered window to image array
            }

        else {
            int midPos = (startPos + endPos) / 2;
            MedianMultithreader left = new MedianMultithreader(unfiltered, startPos, midPos);
            MedianMultithreader right = new MedianMultithreader(unfiltered, midPos, endPos);

            left.fork();

            right.compute();
            left.join();

            left.filtered.addAll(right.filtered); // add all forks

            filtered = left.filtered; // set filtered thread to the left joined thread
        }
    }

}