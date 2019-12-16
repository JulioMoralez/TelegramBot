package pack;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import pack.dao.DaoImpl;
import pack.model.Agent;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Parser implements Runnable{
    private double contrast=1.0;
    private String nick="";
    private String level="";
    private String ap="";
    private String builder="";
    private String trekker="";
    private String purifier="";
    private String recharger="";
    private int fraction=0;
    private Agent agent;

    public Agent getAgent() {
        return agent;
    }

    private String imageIn="";
    private String imageOut="";



    private static DaoImpl agentDao=Bot.getAgentDao();

    Parser(){
    }

    Parser(String imageIn) {
        this.imageIn = "screenIn\\"+imageIn;
        this.imageOut = "screenOut\\"+imageIn;
    }

    Parser(String imageIn, double contrast) {
        this(imageIn);
        this.contrast = contrast;
    }

    public String getImageIn() {
        return imageIn;
    }

    public void setImageIn(String imageIn) {
        this.imageIn = imageIn;
    }



    private void changeContrast(Mat image, double contrast){
        Scalar meanBGR = Core.mean(image);
        double mean = meanBGR.val[0] * 0.114 + meanBGR.val[1]*0.587 + meanBGR.val[2]*0.29;
        Mat lut = new Mat(1,256, CvType.CV_8UC1);
        byte[] arr = new byte[256];
        int color =0;
        for (int i=0; i<256; i++){
            color=(int)(contrast*(i-mean)+mean);
            color=color>255?255:(color<0?0:color);
            arr[i]=(byte)(color);
        }
        lut.put(0,0,arr);
        Core.LUT(image,lut,image);
        lut.release();
    }

    private int getColorForFraction(double[] pixel){
        if (((pixel[0]>=150) && (pixel[0]<=255)) &&
            ((pixel[1]>=105) && (pixel[1]<=200)) &&
            ((pixel[2]>=0)   && (pixel[2]<=40))){
            return 1;   //res
        }
        if (((pixel[0]>=0) && (pixel[0]<=150)) &&
                ((pixel[1]>=150) && (pixel[1]<=255)) &&
                ((pixel[2]>=0)   && (pixel[2]<=70))){
            return 2;   //enl
        }
        return 0;
    }

    private int findPosFractionLogo(Mat image, int cols, int rows){
        int fractionSearchEnd = 0;
        int pixelColor=0;
        int sumPixelColorRes=0;
        int sumPixelColorEnl=0;
        int countRes=0;
        int countEnl=0;
        for (int j=50; j<cols; j++){
            sumPixelColorRes=0;
            sumPixelColorEnl=0;
            for (int i=0; i<rows/4; i++){
                pixelColor=getColorForFraction(image.get(i, j));
                if (pixelColor==1){
                    sumPixelColorRes++;
                }
                if (pixelColor==2){
                    sumPixelColorEnl++;
                }
                //  System.out.println(i + " " + Arrays.toString(source.get(i, j)));
            }
            if (sumPixelColorRes>sumPixelColorEnl){
                countRes++;
            }
            else {
                countEnl++;
            }
            if ((sumPixelColorRes==0) && (sumPixelColorEnl==0)){
                fractionSearchEnd=j;
                break;
            }
            //      System.out.println(j + " " + sumPixelColorRes + " " + sumPixelColorEnl);
        }
        if (countRes>countEnl){
            fraction=1;
        }
        else {
            fraction=2;
        }
        return fractionSearchEnd;
    }

    private void goTesseract(){
        ITesseract tes = new Tesseract();
        tes.setDatapath("tessdata");

        tes.setLanguage("eng");
        String[] list = new String[0];
        try {
            list = (tes.doOCR(new File(imageOut)).split("\n"));
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        list= Arrays.stream(list).filter(s->s.length()>1).toArray(String[]::new);
        for (String s:list){
            System.out.println(s);
        }
        System.out.println("==================================");

        int i=0;
        int len=list.length;
        while (i<len){
            if (((list[i].toLowerCase().contains("yp.")) || (list[i].startsWith("LVL"))) && (nick.equals(""))){
                String nickT=list[i-1];
                String levelT=list[i];
                String apT=list[i+1];

                int pos = nickT.indexOf(" ");
                if (pos!=-1){
                    nick=nickT.substring(0,pos);
                }
                else {
                    nick=nickT;
                }

                levelT=levelT.replaceAll("I","1");
                level=levelT.replaceAll("[\\D]","");

                apT=apT.replaceAll(" ","");
                apT=apT.replaceAll(",","");
                apT=apT.replaceAll("]","1");
                apT=apT.replaceAll("B","8");
                apT=apT.replaceAll("O","0");
                Pattern pattern = Pattern.compile("\\D");
                Matcher matcher = pattern.matcher(apT);
                pos=apT.length();
                if (matcher.find()){
                    pos=matcher.start();
                }
                ap=apT.substring(0,pos);

            }
            if (!nick.equals("")){
                if (list[i].toLowerCase().contains("builder")){
                    while ((builder.length()<2) && (i<len-1)){
                        i++;
                        list[i]=list[i].replaceAll("]","1");
                        builder= list[i].replaceAll("[\\D]","");
                    }
                    break;
                }
                if (list[i].toLowerCase().contains("trekker")){
                    while ((trekker.length()<2) && (i<len-1)){
                        i++;
                        list[i]=list[i].replaceAll("]","1");
                        trekker= list[i].replaceAll("[\\D]","");
                    }
                    break;
                }
                if (list[i].toLowerCase().contains("purifier")){
                    while ((purifier.length()<2) && (i<len-1)){
                        i++;
                        list[i]=list[i].replaceAll("]","1");
                        purifier= list[i].replaceAll("[\\D]","");
                    }
                    break;
                }
                if (list[i].toLowerCase().contains("recharger")){
                    while ((recharger.length()<2) && (i<len-1)){
                        i++;
                        list[i]=list[i].replaceAll("]","1");
                        recharger= list[i].replaceAll("[\\D]","");
                    }
                    break;
                }
            }
            i++;
        }
        System.out.println(contrast);
        System.out.println("Fraction: " + fraction + " Nick: " + nick);
        System.out.println("Level: " + level + " Ap: " + ap);
        System.out.println(trekker);
        System.out.println(builder);
        System.out.println(purifier);
        System.out.println(recharger);
        if (((nick.equals("") || (level.equals("") || (ap.equals("")))) && (contrast<2.0))){
            contrast+=0.1;
            nick="";
            level="";
            ap="";
            builder="";
            purifier="";
            trekker="";
            recharger="";
            imageIn=imageOut;
            parsItWithContrast();
        }
        if (!nick.equals("")){
            agent = agentDao.findByField("nick",nick);
            if (agent==null){
                agent = new Agent();
                setAgent(agent);
                agentDao.add(agent);
            }
            else {
                setAgent(agent);
                agentDao.update(agent);
            }
        }
    }

    private void setAgent(Agent agent) {
        try{
            agent.setNick(nick);
            agent.setLevel(Integer.valueOf(level));
            agent.setAp(Integer.valueOf(ap));
            if (!trekker.equals("")){
                agent.setTrekker(Integer.valueOf(trekker));
            }
            if (!builder.equals("")){
                agent.setBuilder(Integer.valueOf(builder));
            }
            if (!purifier.equals("")){
                agent.setPurifier(Integer.valueOf(purifier));
            }
            if (!recharger.equals("")){
                agent.setRecharger(Integer.valueOf(recharger));
            }
            agent.setFraction(fraction);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void parsItWithContrast(){
        Mat source = Imgcodecs.imread(imageIn,Imgcodecs.IMREAD_COLOR);
        changeContrast(source, contrast);
        Imgcodecs.imwrite(imageOut,source);
        source.release();
        goTesseract();
    }

    void parsIt(){
        Mat source = Imgcodecs.imread(imageIn,Imgcodecs.IMREAD_COLOR);
        final int cols=source.cols();
        final int rows=source.rows();

        int fractionSearchEnd = findPosFractionLogo(source,cols,rows);
        Imgproc.rectangle(source,new Point(0,0),new Point(fractionSearchEnd,rows/4), new Scalar(0,0,0), Core.FILLED);

        Imgproc.cvtColor(source,source,Imgproc.COLOR_BGR2GRAY);

        Mat m = new Mat(rows,cols,source.type(),new Scalar(255,255,255));
        Core.subtract(m,source,source);

      //  changeContrast(source, 1.1);

        Imgcodecs.imwrite(imageOut,source);

        source.release();
        m.release();

        goTesseract();
    }



    @Override
    public void run() {
        parsIt();
    }
}
