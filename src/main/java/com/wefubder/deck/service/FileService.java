package com.wefubder.deck.service;


import com.spire.presentation.FileFormat;
import com.spire.presentation.Presentation;
import com.wefubder.deck.config.StorageProperties;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

@Service
public class FileService {

    private final StorageProperties storageProperties;
    private final Path uploadPath;


    public FileService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
        this.uploadPath = Paths.get(storageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload folder!");
        }
    }


    public void saveFile(MultipartFile file, String targetLocation) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new IOException("Invalid Characters");
            }
            Path folder = uploadPath.resolve(targetLocation + File.separator);
            folder.toFile().mkdirs();
            Files.copy(file.getInputStream(), folder.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could'nt store file", ex);
        }
    }

    public void writeImage(BufferedImage image, String targetPath, String fileName) throws IOException {
        Path destDir = uploadPath.resolve(targetPath + File.separator + fileName);
        destDir.toFile().mkdirs();
        ImageIO.write(image, "png", new File(destDir.toString()));
    }

    public ArrayList<BufferedImage> pdfToImages(MultipartFile file) throws IOException {
        PDDocument document = PDDocument.load(file.getInputStream());
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        ArrayList<BufferedImage> images = new ArrayList<>();

        for (int pageNumber = 0; pageNumber < document.getNumberOfPages(); ++pageNumber) {
            images.add(pdfRenderer.renderImage(pageNumber));
        }
        document.close();
        return images;
    }

    public ArrayList<BufferedImage> pptToImages(MultipartFile file) throws IOException {
        XMLSlideShow ppt = new XMLSlideShow(file.getInputStream());
        ArrayList<BufferedImage> images = new ArrayList<>();
        Dimension pgsize = ppt.getPageSize();
        for (XSLFSlide slide : ppt.getSlides()) {
            BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = img.createGraphics();

            //clear the drawing area
            graphics.setPaint(Color.white);
            graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));

            //render
            slide.draw(graphics);
            images.add(img);

        }
        return images;
    }

    public ArrayList<BufferedImage> pptToImages2(MultipartFile file) throws Exception {
        Presentation ppt = new Presentation();
        ppt.loadFromStream(file.getInputStream(), FileFormat.PPT);
        ArrayList<BufferedImage> images = new ArrayList<>();
        for (int i = 0; i < ppt.getSlides().getCount(); i++) {
            BufferedImage image = ppt.getSlides().get(i).saveAsImage();
            images.add(image);
        }
        ppt.dispose();
        return images;
    }

    public Resource loadFileAsResource(String filePath) throws FileNotFoundException {
        try {
            Path path = this.uploadPath.resolve(filePath).normalize();
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("file not found " + filePath);
            }
        } catch (MalformedURLException | FileNotFoundException ex) {
            throw new FileNotFoundException("file not found " + filePath);
        }
    }

}
