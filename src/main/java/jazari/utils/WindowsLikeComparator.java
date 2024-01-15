/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.utils;

import java.io.File;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author cezerilab
 */
public class WindowsLikeComparator implements Comparator<File> {
    //Regexp to make the 3 part split of the filename.

    private static final Pattern splitPattern = Pattern.compile("^(.*?)(\\d*)(?:\\.([^.]*))?$");

    @Override
    public int compare(File o1, File o2) {
        SplitteFileName data1 = getSplittedFileName(o1);
        SplitteFileName data2 = getSplittedFileName(o2);
        //Compare the namepart caseinsensitive.
        int result = data1.name.compareToIgnoreCase(data2.name);
        //If name is equal, then compare by number
        if (result == 0) {
            result = data1.number.compareTo(data2.number);
        }
        //If numbers are equal then compare by length text of number. This
        //is valid because it differs only by heading zeros. Longer comes
        //first.
        if (result == 0) {
            result = -Integer.compare(data1.numberText.length(), data2.numberText.length());
        }
        //If all above is equal, compare by ext.
        if (result == 0) {
            result = data1.ext.compareTo(data2.ext);
        }
        return result;
    }

    private SplitteFileName getSplittedFileName(File f) {
        Matcher matcher = splitPattern.matcher(f.getName());
        if (matcher.matches()) {
            return new SplitteFileName(matcher.group(1), matcher.group(2), matcher.group(3));
        } else {
            return new SplitteFileName(f.getName(), null, null);
        }
    }

    static class SplitteFileName {

        String name;
        Long number;
        String numberText;
        String ext;

        public SplitteFileName(String name, String numberText, String ext) {
            this.name = name;
            if ("".equals(numberText)) {
                this.number = -1L;
            } else {
                this.number = Long.valueOf(numberText);
            }
            this.numberText = numberText;
            this.ext = ext;
        }
    }
}
