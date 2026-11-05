package com.folium.backend.ocr.service.impl;

import com.folium.backend.ocr.enums.OcrLanguage;
import com.folium.backend.ocr.exception.OcrException;
import com.folium.backend.ocr.model.OcrResult;
import com.folium.backend.ocr.service.OcrService;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;


@Service
public class TesseractOcrService implements OcrService {


    private static final String DEFAULT_TESSDATA_PATH = "/opt/homebrew/share/tessdata";

    @Override
    public OcrResult recognize(byte[] image, OcrLanguage language) {
        try {

            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));
            if (bufferedImage == null) {
                throw new OcrException("Unsupported or empty image");
            }

            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(tessdataPath());
            tesseract.setLanguage(language.getCode());


            String text = tesseract.doOCR(bufferedImage);

            return new OcrResult(text, language);
        } catch (OcrException e) {
            throw e;
        } catch (Exception e) {

            throw new OcrException("OCR failed: " + e.getMessage(), e);
        }
    }

    private String tessdataPath() {
        String fromEnv = System.getenv("TESSDATA_PREFIX");
        return (fromEnv == null || fromEnv.isBlank()) ? DEFAULT_TESSDATA_PATH : fromEnv;
    }
}
