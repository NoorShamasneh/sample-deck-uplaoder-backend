package com.wefubder.deck.service.dto;

import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.UUID;

public class DeckDTO implements Serializable {
    private UUID id;

    private String companyName;

    private String description;

    private String originalFileURL;

    private String originalFileName;

    private Integer numberOFImages;

    private String[] images;

    private MultipartFile originalFile;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOriginalFileURL() {
        return originalFileURL;
    }

    public void setOriginalFileURL(String originalFileURL) {
        this.originalFileURL = originalFileURL;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public Integer getNumberOFImages() {
        return numberOFImages;
    }

    public void setNumberOFImages(Integer numberOFImages) {
        this.numberOFImages = numberOFImages;
    }

    public String[] getImages() {
        return images;
    }

    public void setImages(String[] images) {
        this.images = images;
    }

    public MultipartFile getOriginalFile() {
        return originalFile;
    }

    public void setOriginalFile(MultipartFile originalFile) {
        this.originalFile = originalFile;
    }
}
