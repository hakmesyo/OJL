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
    // Yeni eklenen Line konteyner
    public PascalVocLine lineContainer;
    public List<PascalVocAttribute> attributeList;

    /**
     * Ana Constructor. LineContainer da parametre olarak eklendi.
     */
    public PascalVocObject(String name, String pose, int truncated, int diffucult, int occluded,
            PascalVocBoundingBox bndbox,
            PascalVocPolygon polygonContainer,
            PascalVocLine lineContainer, // Yeni parametre
            List<PascalVocAttribute> attributeList) {
        this.name = name;
        this.pose = (pose == null || pose.isEmpty()) ? "Unspecified" : pose;
        this.truncated = truncated;
        this.diffucult = diffucult;
        this.occluded = occluded;
        this.bndbox = bndbox;
        this.polygonContainer = polygonContainer;
        this.lineContainer = lineContainer;
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

        if (polygonContainer != null) {
            ret += polygonContainer.toString();
        } else if (lineContainer != null) {
            // Line XML formatı için basit bir placeholder, ileride detaylandırılabilir.
            ret += "\t\t<line>\n";
            ret += "\t\t\t<x1>" + lineContainer.startPoint.x + "</x1>\n";
            ret += "\t\t\t<y1>" + lineContainer.startPoint.y + "</y1>\n";
            ret += "\t\t\t<x2>" + lineContainer.endPoint.x + "</x2>\n";
            ret += "\t\t\t<y2>" + lineContainer.endPoint.y + "</y2>\n";
            ret += "\t\t</line>\n";
        } else if (bndbox != null) {
            ret += bndbox.toString();
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
