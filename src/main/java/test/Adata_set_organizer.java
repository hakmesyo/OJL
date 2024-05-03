/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.io.File;
import jazari.factory.FactoryUtils;

/**
 *
 * @author cezerilab
 */
public class Adata_set_organizer {
    public static void main(String[] args) {
        String path="D:\\DATASETS\\african_wildlife";
        File[] dirs=FactoryUtils.getDirectories(path);
        int k=0;
        for (File dir : dirs) {
            File[] files=FactoryUtils.getFileArrayInFolderByExtension(dir.getAbsolutePath(), "jpg");
            for (File file : files) {
                String fileName=FactoryUtils.getFileName(file.getName());
                FactoryUtils.copyFile(file, new File(path+"/all/"+dir.getName()+"_"+file.getName()));
                File txtFile=new File(file.getParent()+"/"+fileName+".txt");
                FactoryUtils.copyFile(txtFile, new File(path+"/all/"+dir.getName()+"_"+txtFile.getName()));
                System.out.println((k++)+".copied "+file.getName());
            }
        }
        
        
//        //String path="C:\\Users\\cezerilab\\Downloads\\road signs.v2-release.voc\\train";
//        String path="C:\\Users\\cezerilab\\Downloads\\peanuts.v2-release.voc\\train";
//        File[] files=FactoryUtils.getFileArrayInFolderNoSorting(path);
//        for (File file : files) {
//            System.out.println("file = " + file);
//            String ext=FactoryUtils.getFileExtension(file);
//            System.out.println("ext = " + ext);
//            String[] s=file.getName().split("_jpg");
//            String newFileName=file.getParent()+"/"+s[0]+"."+ext;
//            FactoryUtils.renameFile(file, new File(newFileName));
//            int a=1;
//            
//        }
    }
}
