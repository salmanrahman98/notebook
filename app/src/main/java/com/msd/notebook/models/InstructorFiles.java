package com.msd.notebook.models;

public class InstructorFiles {

    String fileName, fileUrl, id, fileExtenstion;

    public InstructorFiles(String id, String fileName, String fileUrl, String fileExtenstion) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.id = id;
        this.fileExtenstion = fileExtenstion;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileExtenstion() {
        return fileExtenstion;
    }
}
