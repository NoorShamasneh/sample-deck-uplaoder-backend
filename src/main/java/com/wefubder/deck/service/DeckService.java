package com.wefubder.deck.service;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wefubder.deck.config.StorageProperties;
import com.wefubder.deck.service.dto.DeckDTO;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@Service
public class DeckService {

    private final FileService fileService;
    private final StorageProperties storageProperties;
    private final Path uploadPath;

    public DeckService(FileService fileService, StorageProperties storageProperties) {
        this.fileService = fileService;
        this.storageProperties = storageProperties;
        this.uploadPath = Paths.get(this.storageProperties.getUploadDir())
                .toAbsolutePath().normalize();
    }

    public DeckDTO createNewDeck(DeckDTO deckDTO) throws Exception {
        deckDTO.setId(UUID.randomUUID());
        deckDTO.setOriginalFileURL(deckDTO.getId().toString() + File.separator + deckDTO.getOriginalFile().getOriginalFilename());
        deckDTO.setOriginalFileName(deckDTO.getOriginalFile().getOriginalFilename());
        fileService.saveFile(deckDTO.getOriginalFile(), deckDTO.getId().toString());

        String extension = FilenameUtils.getExtension(deckDTO.getOriginalFile().getOriginalFilename()).toUpperCase();
        ArrayList<BufferedImage> images;
        if (extension.startsWith("PPT")) {
            images = fileService.pptToImages2(deckDTO.getOriginalFile());
        } else {
            images = fileService.pdfToImages(deckDTO.getOriginalFile());
        }
        deckDTO.setNumberOFImages(images.size());
        for (int i = 0; i < deckDTO.getNumberOFImages(); i++) {
            fileService.writeImage(images.get(i), deckDTO.getId().toString() + File.separator + "images", i + ".png");
        }
        deckDTO.setOriginalFile(null);
        this.saveDeck(deckDTO);
        return deckDTO;
    }

    private void saveDeck(DeckDTO deckDTO) throws IOException {
        String filePath = uploadPath.toString() + File.separator + deckDTO.getId() + File.separator + "info.json";
        FileWriter fileWriter = new FileWriter(filePath);
        new GsonBuilder().setPrettyPrinting().create().toJson(deckDTO, fileWriter);
        fileWriter.close();
    }

    private DeckDTO loadDeck(UUID id) throws IOException {
        String filePath = uploadPath.toString() + File.separator + id + File.separator + "info.json";
        FileReader fileReader = new FileReader(filePath);
        DeckDTO deckDto = new Gson().fromJson(fileReader, DeckDTO.class);
        fileReader.close();
        return deckDto;
    }

    private void fillObjectImages(DeckDTO deckDTO) {
        deckDTO.setImages(new String[deckDTO.getNumberOFImages()]);
        for (int i = 0; i < deckDTO.getNumberOFImages(); i++) {
            deckDTO.getImages()[i] = deckDTO.getId().toString() + File.separator + "images" + File.separator + i + ".png";
        }
    }

    public DeckDTO getDeck(UUID id) throws IOException {
        DeckDTO deckDTO = this.loadDeck(id);
        this.fillObjectImages(deckDTO);
        return deckDTO;
    }

    public List<DeckDTO> getAllDecks() {
        File[] directories = this.uploadPath.toFile().listFiles
                (new FilenameFilter() {
                    @Override
                    public boolean accept(File current, String name) {
                        return new File(current, name).isDirectory();
                    }
                });
        List<DeckDTO> deckDTOS = new ArrayList<>();
        if (directories != null) {
            Arrays.sort(directories, Comparator.comparingLong(File::lastModified).reversed());
            for (File dir : directories) {
                try {
                    deckDTOS.add(this.loadDeck(UUID.fromString(dir.getName())));
                } catch (IllegalArgumentException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return deckDTOS;
    }
}
