//
// Created by 庾金科 on 20/09/2017.
//
#include "../include/PlateDetection.h"
#include "../include/PlateInfo.h"

#include "util.h"

#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"

namespace pr {


    PlateDetection::PlateDetection(std::string filename_cascade) {
        cascade.load(filename_cascade);

    };


    void
    PlateDetection::plateDetectionRough(cv::Mat InputImage, std::vector<pr::PlateInfo> &plateInfos,
                                        int min_w, int max_w) {

        cv::Mat processImage(InputImage);
//        cv::Mat processImage;

//         cv::cvtColor(InputImage,processImage,cv::COLOR_BGR2GRAY);

        //add by jxl
        int bluenum = 0;
        int yellownum = 0;
        int whitenum = 0;
        int greennum = 0;
        int blacknum = 0;
        int othernum = 0;
        unsigned char hvalue;
        unsigned char svalue;
        unsigned char vvalue;
        cv::Mat hsv;
        cv::Mat imgH;
        cv::Mat imgS;
        cv::Mat imgV;


        std::vector<cv::Rect> platesRegions;
//        std::vector<PlateInfo> plates;
        cv::Size minSize(min_w, min_w / 4);
        cv::Size maxSize(max_w, max_w / 4);
//        cv::imshow("input",InputImage);
//                cv::waitKey(0);
        cascade.detectMultiScale(processImage, platesRegions,
                                 1.1, 3, cv::CASCADE_SCALE_IMAGE, minSize, maxSize);
        for (auto plate:platesRegions) {
            // extend rects
//            x -= w * 0.14
//            w += w * 0.28
//            y -= h * 0.6
//            h += h * 1.1;
            int zeroadd_w = static_cast<int>(plate.width * 0.30);
            int zeroadd_h = static_cast<int>(plate.height * 2);
            int zeroadd_x = static_cast<int>(plate.width * 0.15);
            int zeroadd_y = static_cast<int>(plate.height * 1);
            plate.x -= zeroadd_x;
            plate.y -= zeroadd_y;
            plate.height += zeroadd_h;
            plate.width += zeroadd_w;
            cv::Mat plateImage = util::cropFromImage(InputImage, plate);
            PlateInfo plateInfo(plateImage, plate);

            //add by jxl
            cv::cvtColor(plateImage, hsv, CV_BGR2HSV);
            std::vector<cv::Mat> channels;
            split(hsv, channels);
            imgH = channels.at(0);
            imgS = channels.at(1);
            imgV = channels.at(2);
            for (int i = 0; i < hsv.rows; i++) {
                for (int j = 0; j < hsv.cols; j++) {
                    //////////////////h通道/-/////////////////////
                    hvalue = imgH.at<unsigned char>(i, j);  //
                    //////////////////s通道/-/////////////////////
                    svalue = imgS.at<unsigned char>(i, j);  //
                    //////////////////v通道/-/////////////////////
                    vvalue = imgV.at<unsigned char>(i, j);  //
                    // printf("x=%d,y=%d,HSV: H=%d,S=%d,V=%d\n",i,j,hvalue,svalue,vvalue);
                    //https://blog.csdn.net/dieju8330/article/details/82465616，色值范围
                    if ((hvalue > 100 && hvalue < 124) && (svalue > 43 && svalue < 255) &&
                        (vvalue > 46 && vvalue < 255))//hsv图像在蓝色区域里
                    {
                        //蓝色+1
                        bluenum++;
                    } else if ((hvalue > 26 && hvalue < 34) && (svalue > 43 && svalue < 255) &&
                               (vvalue > 46 && vvalue < 255))//hsv在黄色区域里
                    {
                        //黄色+1
                        yellownum++;
                    }
                        /* else if ((hvalue>0&&hvalue<180)&&(svalue>0&&svalue<255)&&(vvalue>0&&vvalue<46))//hsv在黑色区域里
                         {
                             blacknum++;
                             //黑色+1
                         }
                         else  if ((hvalue>0&&hvalue<180)&&(svalue>0&&svalue<30)&&(vvalue>221&&vvalue<255))//hsv在白色区域里
                         {
                             //白色+1
                             whitenum++;
                         }*/
                    else if ((hvalue > 35 && hvalue < 77) && (svalue > 43 && svalue < 255) &&
                             (vvalue > 46 && vvalue < 255))//hsv在绿色区域里
                    {
                        greennum++;
                    }
//                    else
//                    {
//                        //其他颜色
//                        othernum++;
                    //                   }
                }
            }
            ////////////四个颜色值找最大值//////////////
            if ((bluenum > yellownum) & (bluenum > blacknum) & (bluenum > greennum) &
                (bluenum > whitenum) & (bluenum > othernum)) {
                plateInfo.setPlateType(0);
            } else if ((yellownum > whitenum) & (yellownum > blacknum) & (yellownum > greennum) &
                       (yellownum > bluenum) & (yellownum > othernum)) {
                plateInfo.setPlateType(1);
            } else if ((whitenum > yellownum) & (whitenum > blacknum) & (whitenum > greennum) &
                       (whitenum > bluenum) & (whitenum > othernum)) {
                plateInfo.setPlateType(2);
            } else if ((blacknum > yellownum) & (blacknum > whitenum) & (blacknum > greennum) &
                       (blacknum > bluenum) & (blacknum > othernum)) {
                plateInfo.setPlateType(4);
            } else if ((greennum > yellownum) & (greennum > blacknum) & (greennum > whitenum) &
                       (greennum > bluenum) & (greennum > othernum)) {
                plateInfo.setPlateType(3);
            } else if ((othernum > yellownum) & (othernum > whitenum) & (othernum > greennum) &
                       (othernum > bluenum) & (othernum > blacknum)) {
                plateInfo.setPlateType(5);
            }


            plateInfos.push_back(plateInfo);

        }
    }
//    std::vector<pr::PlateInfo> PlateDetection::plateDetectionRough(cv::Mat InputImage,cv::Rect roi,int min_w,int max_w){
//        cv::Mat roi_region = util::cropFromImage(InputImage,roi);
//        return plateDetectionRough(roi_region,min_w,max_w);
//    }




}//namespace pr
