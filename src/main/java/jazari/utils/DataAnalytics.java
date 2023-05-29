/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.utils;

import java.util.Objects;

/**
 *
 * @author cezerilab
 */
public class DataAnalytics {
    public String dataName;
    public float frequency=0;
    public float ratio=0;

    public DataAnalytics(String dataName) {
        this.dataName = dataName;
    }
    
    

    @Override
    public String toString() {
        return "DataAnalytics{" + "dataName=" + dataName + ", frequency=" + frequency + ", ratio=" + ratio + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.dataName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataAnalytics other = (DataAnalytics) obj;
        return Objects.equals(this.dataName, other.dataName);
    }

    
}
