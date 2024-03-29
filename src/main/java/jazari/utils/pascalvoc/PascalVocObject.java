/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.utils.pascalvoc;

import java.util.List;

/**
 *
 * @author cezerilab
 */
public class PascalVocObject {

    public String name;
    public String pose = "Unspecified";
    public int truncated;
    public int diffucult;
    public int occluded;
    public PascalVocBoundingBox bndbox;
    public PascalVocPolygon polygonContainer;
    public List<PascalVocAttribute> attributeList;

    public PascalVocObject(String name, String pose, int truncated, int diffucult, int occluded, PascalVocBoundingBox bndbox, PascalVocPolygon polygonContainer, List<PascalVocAttribute> attributeList) {
        this.name = name;
        this.pose = (pose == "") ? "Unspecified" : pose;
        this.truncated = truncated;
        this.diffucult = diffucult;
        this.occluded = occluded;
        this.bndbox = bndbox;
        this.polygonContainer = polygonContainer;
        this.attributeList = attributeList;
    }

    @Override
    public String toString() {
        String ret = "\t<object>\n"
                + "\t\t<name>" + name + "</name>\n"
                + "\t\t<occluded>" + occluded + "</occluded>\n"
                + "\t\t<pose>" + pose + "</pose>\n"
                + "\t\t<truncated>" + truncated + "</truncated>\n"
                + "\t\t<difficult>" + diffucult + "</difficult>\n";
        if(polygonContainer==null){
            ret+=bndbox.toString();
        }else{
            ret+=polygonContainer.toString();
        }
        if (attributeList != null) {
            ret += "\t\t<attributes>\n";
            for (PascalVocAttribute boundingBoxAttribute : attributeList) {
                ret += boundingBoxAttribute.toString();
            }
            ret += "\t\t</attributes>\n";
        }

        ret += "\t</object>\n";

        return ret;
    }

}
