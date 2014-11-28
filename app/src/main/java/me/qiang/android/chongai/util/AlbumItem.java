package me.qiang.android.chongai.util;

public class AlbumItem implements Comparable<AlbumItem>{
    private String topImagePath;
    private String folderName;
    private int imageCounts;

    public String getTopImagePath() {
        return topImagePath;
    }
    public void setTopImagePath(String topImagePath) {
        this.topImagePath = topImagePath;
    }
    public String getFolderName() {
        return folderName;
    }
    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
    public int getImageCounts() {
        return imageCounts;
    }
    public void setImageCounts(int imageCounts) {
        this.imageCounts = imageCounts;
    }

    public int compareTo(AlbumItem album) {

        int compareQuantity = ((AlbumItem) album).getImageCounts();

        //descending order
        return compareQuantity - this.imageCounts;
    }
}