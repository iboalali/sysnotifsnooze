package com.iboalali.sysnotifsnooze;

import java.util.Arrays;
import java.util.List;

/**
 * Created by iboalali on 25-Oct-17.
 */

public class PackageNameList {
    private List<String> packageNames;

    public List<String> getPackageNames(){
        return packageNames;
    }

    public void setPackageNames(List<String> packageNames){
        this.packageNames = packageNames;
    }

    public void setPackageNames(String[] packageName){
        this.packageNames = Arrays.asList(packageName);
    }

}
